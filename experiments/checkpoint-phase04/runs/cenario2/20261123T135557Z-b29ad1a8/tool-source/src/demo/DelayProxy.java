package demo;

import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.protocol.MessageCodec;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * Frame-transparent real TCP delay. Original frame bytes are never rewritten.
 * Each frame is scheduled for {@code arrival + delay} per direction: the reader
 * thread never blocks the writer, so a burst of frames does not queue behind
 * earlier delayed frames (no artificial head-of-line serialization). TCP
 * ordering is preserved because frames are written in arrival order.
 */
final class DelayProxy implements AutoCloseable
{
    //* === Atributos e Variáveis === *
    private final ServerSocket listener = new ServerSocket();
    private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
    private final Semaphore capacity = new Semaphore(64);
    private final AtomicBoolean closed = new AtomicBoolean();
    private final String node;
    private final String destination;
    private final int port;
    private final ToIntFunction<String> delay;
    private final Consumer<ControlMessage> tap;



    //* === Construtores === *

    DelayProxy(String node, String host, String destination, int port, ToIntFunction<String> delay,
            Consumer<ControlMessage> tap) throws IOException
    {
        this.node = node;
        this.destination = destination;
        this.port = port;
        this.delay = delay;
        this.tap = tap;
        listener.setReuseAddress(true);
        listener.bind(new InetSocketAddress(host, port));
        Thread.startVirtualThread(this::accept);
    }



    //* === Lógica === *

    private void accept()
    {
        while(!closed.get())
        {
            try
            {
                Socket incoming = listener.accept();
                if(!capacity.tryAcquire())
                {
                    incoming.close();
                    continue;
                }
                sockets.add(incoming);
                Thread.startVirtualThread(() -> connect(incoming));
            }catch(IOException e){
                if(!closed.get())
                    System.err.println("PROXY_ACCEPT_ERROR " + node + " " + e);
            }
        }
    }


    private void connect(Socket incoming)
    {
        try(incoming; var outgoing = new Socket())
        {
            sockets.add(outgoing);
            incoming.setSoTimeout(5000);
            outgoing.connect(new InetSocketAddress(destination, port), 2000);
            incoming.setTcpNoDelay(true);
            outgoing.setTcpNoDelay(true);
            byte[] first = frame(new DataInputStream(incoming.getInputStream()));
            String peer = MessageCodec.decode(first).senderId();
            incoming.setSoTimeout(0);
            var toServer = new LinkedBlockingQueue<DueFrame>();
            toServer.put(new DueFrame(dueNanos(peer), first));
            var response = Thread.startVirtualThread(() -> link(outgoing, incoming, peer, new LinkedBlockingQueue<>()));
            link(incoming, outgoing, peer, toServer);
            response.join(1000);
        }catch(IOException | InterruptedException e){
            if(e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }finally{
            sockets.removeIf(Socket::isClosed);
            capacity.release();
        }
    }


    private long dueNanos(String peer)
    {
        return System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delay.applyAsInt(edge(node, peer)));
    }


    /**
     * One direction of a proxied connection: a reader virtual thread enqueues
     * frames in arrival order with their due time; the calling thread writes
     * them in arrival order, sleeping only until each frame's own due time.
     */
    private void link(Socket from, Socket to, String peer, LinkedBlockingQueue<DueFrame> queue)
    {
        Thread.startVirtualThread(() ->
        {
            try
            {
                var input = new DataInputStream(from.getInputStream());
                while(!closed.get())
                {
                    byte[] raw = frame(input);
                    queue.put(new DueFrame(dueNanos(peer), raw));
                }
            }catch(IOException | InterruptedException e){
                if(e instanceof InterruptedException)
                    Thread.currentThread().interrupt();
            }finally{
                closeQuietly(from, to);
            }
        });
        try
        {
            while(!closed.get())
            {
                DueFrame item = queue.poll(200, TimeUnit.MILLISECONDS);
                if(item == null)
                    continue;
                long wait = item.dueNanos() - System.nanoTime();
                if(wait > 0)
                    Thread.sleep(wait / 1_000_000, (int)(wait % 1_000_000));
                to.getOutputStream().write(item.raw());
                to.getOutputStream().flush();
                tap.accept(MessageCodec.decode(item.raw()));
            }
        }catch(IOException | InterruptedException e){
            if(e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }finally{
            closeQuietly(from, to);
        }
    }


    private static void closeQuietly(Socket from, Socket to)
    {
        try
        {
            from.close();
            to.close();
        }catch(IOException ignored){
            // Both directions are owned by this proxy connection.
        }
    }


    private record DueFrame(long dueNanos, byte[] raw)
    {
    }


    static String edge(String a, String b)
    {
        return a.compareTo(b) < 0 ? a + "-" + b : b + "-" + a;
    }


    private static byte[] frame(DataInputStream input) throws IOException
    {
        int length = input.readInt();
        if(length < MessageCodec.MIN_BODY_BYTES || length > MessageCodec.MAX_BODY_BYTES)
            throw new IOException("Invalid TCP frame size " + length);
        byte[] result = new byte[length + 4];
        ByteBuffer.wrap(result).putInt(length);
        input.readFully(result, 4, length);
        return result;
    }


    public void close()
    {
        closed.set(true);
        try
        {
            listener.close();
            for(var socket : sockets)
            {
                socket.close();
            }
        }catch(IOException e){
            throw new IllegalStateException("Proxy cleanup", e);
        }
    }
}
