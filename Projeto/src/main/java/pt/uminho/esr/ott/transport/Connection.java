package pt.uminho.esr.ott.transport;

import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.protocol.MessageCodec;
import pt.uminho.esr.ott.protocol.ProtocolException;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/** One reader, one writer, bounded output queue; identity independent of neighbor identity. */
public final class Connection implements AutoCloseable {
    public enum SendResult { ACCEPTED, CLOSED, OVERFLOW, INVALID_MESSAGE }
    public enum CloseReason { LOCAL_CLOSE, SHUTDOWN, PEER_EOF, TRUNCATED_FRAME,
        PROTOCOL_ERROR, IO_ERROR, OUTPUT_OVERFLOW, CALLBACK_ERROR }

    private final UUID id = UUID.randomUUID();
    private final Transport owner;
    private final Socket socket;
    private final Transport.Listener listener;
    private final ArrayBlockingQueue<byte[]> output;
    private final AtomicInteger remainingWorkers = new AtomicInteger(2);
    private final InetSocketAddress remote;
    private volatile CloseReason reason;
    private Thread writer;

    Connection(Transport owner, Socket socket, Transport.Listener listener, int capacity) {
        this.owner = owner;
        this.socket = socket;
        this.listener = listener;
        output = new ArrayBlockingQueue<>(capacity);
        remote = (InetSocketAddress) socket.getRemoteSocketAddress();
    }

    void start() {
        writer = owner.launch("write-" + id, this::writeLoop);
        owner.launch("read-" + id, this::readLoop);
    }

    public UUID id() { return id; }
    public InetSocketAddress remoteAddress() { return remote; }
    public boolean isOpen() { return reason == null; }
    public int queuedMessages() { return output.size(); }

    /** ACCEPTED means queued, never delivery or application ACK. No network write on caller. */
    public SendResult send(ControlMessage message) {
        if (!isOpen()) return SendResult.CLOSED;
        final byte[] frame;
        try { frame = MessageCodec.encode(message); }
        catch (IOException | IllegalArgumentException | NullPointerException e) { return SendResult.INVALID_MESSAGE; }
        synchronized (this) {
            if (!isOpen()) return SendResult.CLOSED;
            if (output.offer(frame)) return SendResult.ACCEPTED;
            close(CloseReason.OUTPUT_OVERFLOW);
            return SendResult.OVERFLOW;
        }
    }

    private void readLoop() {
        try {
            listener.connectionOpen(this);
            var input = socket.getInputStream();
            while (isOpen()) {
                var message = MessageCodec.read(input);
                if (message.isEmpty()) { close(CloseReason.PEER_EOF); break; }
                listener.message(this, message.orElseThrow());
            }
        } catch (EOFException e) { close(CloseReason.TRUNCATED_FRAME); }
        catch (ProtocolException e) { close(CloseReason.PROTOCOL_ERROR); }
        catch (IOException e) { close(CloseReason.IO_ERROR); }
        catch (RuntimeException e) { close(CloseReason.CALLBACK_ERROR); }
        finally {
            close(CloseReason.IO_ERROR);
            try { listener.connectionClosed(this, reason); }
            catch (RuntimeException ignored) { /* Consumer failure cannot retain socket/workers. */ }
            finally { workerFinished(); }
        }
    }

    private void writeLoop() {
        try {
            var destination = socket.getOutputStream();
            while (isOpen()) {
                byte[] frame = output.take();
                destination.write(frame);
                destination.flush();
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        catch (IOException e) { close(CloseReason.IO_ERROR); }
        finally { close(CloseReason.IO_ERROR); workerFinished(); }
    }

    private void workerFinished() {
        if (remainingWorkers.decrementAndGet() == 0) owner.release(socket);
    }

    @Override public void close() { close(CloseReason.LOCAL_CLOSE); }

    synchronized void close(CloseReason cause) {
        if (reason != null) return;
        reason = cause;
        output.clear();
        Transport.closeSocket(socket);
        if (writer != null) writer.interrupt();
    }
}
