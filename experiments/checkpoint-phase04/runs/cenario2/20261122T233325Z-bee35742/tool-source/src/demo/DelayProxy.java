package demo;

import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.protocol.MessageCodec;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/** Frame-transparent real TCP delay. Original frame bytes are never rewritten. */
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
            var response = Thread.startVirtualThread(() -> pump(outgoing, incoming, peer));
            send(first, outgoing, peer);
            pump(incoming, outgoing, peer);
            response.join(1000);
        }catch(IOException | InterruptedException e){
            if(e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }finally{
            sockets.removeIf(Socket::isClosed);
            capacity.release();
        }
    }


    private void pump(Socket from, Socket to, String peer)
    {
        try
        {
            var input = new DataInputStream(from.getInputStream());
            while(!closed.get())
            {
                send(frame(input), to, peer);
            }
        }catch(IOException | InterruptedException e){
            if(e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }finally{
            try
            {
                from.close();
                to.close();
            }catch(IOException ignored){
                // Both directions are owned by this proxy connection.
            }
        }
    }


    private void send(byte[] raw, Socket to, String peer) throws IOException, InterruptedException
    {
        int millis = delay.applyAsInt(edge(node, peer));
        if(millis > 0)
            Thread.sleep(millis);
        to.getOutputStream().write(raw);
        to.getOutputStream().flush();
        tap.accept(MessageCodec.decode(raw));
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
