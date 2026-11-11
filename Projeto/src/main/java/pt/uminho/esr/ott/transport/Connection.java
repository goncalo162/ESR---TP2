package pt.uminho.esr.ott.transport;

import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.protocol.ErrorCode;
import pt.uminho.esr.ott.protocol.MessageCodec;
import pt.uminho.esr.ott.protocol.ProtocolException;
import pt.uminho.esr.ott.protocol.MessageType;
import java.util.concurrent.CompletionStage;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/** One reader, one writer, bounded output queue; identity independent of neighbor identity. */
public final class Connection implements AutoCloseable {
    public enum SendResult { ACCEPTED, CLOSED, OVERFLOW, INVALID_MESSAGE }
    public enum CloseReason { LOCAL_CLOSE, SHUTDOWN, PEER_EOF, TRUNCATED_FRAME,
        PROTOCOL_ERROR, IO_ERROR, OUTPUT_OVERFLOW, CALLBACK_ERROR }

    public enum ExtensionStatus { ACCEPTED, BACKPRESSURED, CLOSED, INVALID_MESSAGE }
    /** No mutable/cancellable view of the internal physical write confirmation. */
    public record ExtensionEnqueue(ExtensionStatus status, CompletionStage<Void> written) {
        public ExtensionEnqueue { java.util.Objects.requireNonNull(status);java.util.Objects.requireNonNull(written); }
    }
    public static ExtensionEnqueue refused(ExtensionStatus status) {
        return new ExtensionEnqueue(status,CompletableFuture.completedStage(null));
    }
    interface WriteDriver {
        default void dequeued() throws IOException { }
        void write(byte[] frame) throws IOException;
        void flush() throws IOException;
    }
    interface WriteDriverFactory { WriteDriver open(Socket socket) throws IOException; }
    static final WriteDriverFactory SOCKET_WRITER = socket -> {
        var stream = socket.getOutputStream();
        return new WriteDriver() {
            @Override public void write(byte[] frame) throws IOException { stream.write(frame); }
            @Override public void flush() throws IOException { stream.flush(); }
        };
    };
    private static final class PendingWrite {
        final byte[] frame;
        final CompletableFuture<Void> confirmation;
        // Decisions are protected by the Connection monitor; publication is outside it.
        final boolean extra;
        boolean extraQueued, released;
        boolean decided;
        IOException failure;
        PendingWrite(byte[] frame, CompletableFuture<Void> confirmation) {
            this(frame,confirmation,false);
        }
        PendingWrite(byte[] frame,CompletableFuture<Void> confirmation,boolean extra) {
            this.frame=frame;this.confirmation=confirmation;this.extra=extra;extraQueued=extra;
        }
        void publish() {
            if (confirmation == null) return;
            if (failure == null) confirmation.complete(null);
            else confirmation.completeExceptionally(failure);
        }
    }

    private final UUID id = UUID.randomUUID();
    private final Transport owner;
    private final Socket socket;
    private final Transport.Listener listener;
    private final ArrayBlockingQueue<PendingWrite> output;
    // Includes a frame between take() and inFlight publication. Bound: queue capacity + one.
    private final Set<PendingWrite> confirmations = new LinkedHashSet<>();
    private PendingWrite inFlight;
    private int queuedExtra;
    private final WriteDriverFactory writerFactory;
    private final AtomicInteger remainingWorkers = new AtomicInteger(2);
    private final InetSocketAddress remote;
    private volatile CloseReason reason;
    private Thread writer;

    Connection(Transport owner, Socket socket, Transport.Listener listener, int capacity) {
        this(owner, socket, listener, capacity, SOCKET_WRITER);
    }

    Connection(Transport owner, Socket socket, Transport.Listener listener, int capacity,
               WriteDriverFactory writerFactory) {
        this.owner = owner;
        this.socket = socket;
        this.listener = listener;
        this.writerFactory = writerFactory;
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
        if(message!=null && (message.type()==MessageType.DISCOVERY_QUERY || message.type()==MessageType.DISCOVERY_SNAPSHOT))
            return SendResult.INVALID_MESSAGE;
        if (!isOpen()) return SendResult.CLOSED;
        final byte[] frame;
        try { frame = MessageCodec.encode(message); }
        catch (IOException | IllegalArgumentException | NullPointerException e) { return SendResult.INVALID_MESSAGE; }
        synchronized (this) {
            if (!isOpen()) return SendResult.CLOSED;
            if (output.offer(new PendingWrite(frame, null))) return SendResult.ACCEPTED;
            markClosed(CloseReason.OUTPUT_OVERFLOW);
        }
        finishClose();
        return SendResult.OVERFLOW;
    }

    /** Success is local write AND flush, never a peer ACK. Continuations must not block. */
    public CompletableFuture<Void> sendAndFlush(ControlMessage message) {
        if(message!=null && (message.type()==MessageType.DISCOVERY_QUERY || message.type()==MessageType.DISCOVERY_SNAPSHOT))
            return CompletableFuture.failedFuture(new ProtocolException(ErrorCode.INVALID_MESSAGE,"Use bounded extension admission"));
        if (!isOpen()) return CompletableFuture.failedFuture(closedError());
        final byte[] frame;
        try { frame = MessageCodec.encode(message); }
        catch (IOException e) { return CompletableFuture.failedFuture(e); }
        catch (IllegalArgumentException | NullPointerException e) {
            return CompletableFuture.failedFuture(new ProtocolException(ErrorCode.INVALID_MESSAGE,
                    "Invalid outgoing control message", e));
        }
        var result = new CompletableFuture<Void>();
        var entry = new PendingWrite(frame, result);
        synchronized (this) {
            if (!isOpen()) return CompletableFuture.failedFuture(closedError());
            if (output.offer(entry)) {
                // Writer cannot decide/publish before taking this monitor.
                confirmations.add(entry);
                return result;
            }
            markClosed(CloseReason.OUTPUT_OVERFLOW);
        }
        finishClose();
        return CompletableFuture.failedFuture(closedError());
    }

    /** Nonclosing admission, common FIFO; acquire the node's physical quota before encoding. */
    public ExtensionEnqueue trySendExtension(ControlMessage message) {
        if(message==null || (message.type()!=MessageType.DISCOVERY_QUERY && message.type()!=MessageType.DISCOVERY_SNAPSHOT))
            return refused(ExtensionStatus.INVALID_MESSAGE);
        if(!isOpen()) return refused(ExtensionStatus.CLOSED);
        if(!owner.acquireExtraWrite())return refused(ExtensionStatus.BACKPRESSURED);
        boolean admitted=false;
        try {
            byte[] frame;
            try { frame=MessageCodec.encode(message); }
            catch(IOException|IllegalArgumentException|NullPointerException error) { return refused(ExtensionStatus.INVALID_MESSAGE); }
            var confirmation=new CompletableFuture<Void>();var entry=new PendingWrite(frame,confirmation,true);
            synchronized(this) {
                if(!isOpen()) return refused(ExtensionStatus.CLOSED);
                if(output.remainingCapacity()<2 || queuedExtra!=0)return refused(ExtensionStatus.BACKPRESSURED);
                if(!output.offer(entry))throw new IllegalStateException("FIFO admission changed under producer monitor");
                queuedExtra++;confirmations.add(entry);admitted=true;
            }
            return new ExtensionEnqueue(ExtensionStatus.ACCEPTED,confirmation.minimalCompletionStage());
        } finally { if(!admitted)owner.releaseExtraWrite(); }
    }
    private void releaseExtra(PendingWrite entry) {
        if(entry.extra && !entry.released) { entry.released=true;owner.releaseExtraWrite(); }
    }

    private void readLoop() {
        try {
            listener.connectionOpen(this);
            var input = socket.getInputStream();
            while (isOpen()) {
                var message = MessageCodec.readFrame(input);
                if(message.isEmpty()) { close(CloseReason.PEER_EOF);break; }
                if(message.get() instanceof MessageCodec.Decoded decoded) listener.message(this,decoded.message());
                else listener.rejectedExtraContent(this,(MessageCodec.RejectedExtraContent)message.get());
            }
        } catch (EOFException e) { close(CloseReason.TRUNCATED_FRAME); }
        catch (ProtocolException e) {
            try { listener.protocolError(this,e); } finally { close(CloseReason.PROTOCOL_ERROR); }
        }
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
            var destination = writerFactory.open(socket);
            while (isOpen()) {
                PendingWrite entry = output.take();
                destination.dequeued();
                synchronized (this) {
                    if (!isOpen()) break;
                    inFlight = entry;
                    if(entry.extraQueued){entry.extraQueued=false;queuedExtra--;}
                }
                destination.write(entry.frame);
                destination.flush();
                synchronized (this) {
                    if (!entry.decided) {
                        entry.decided = true;
                        if (!isOpen()) entry.failure = closedError();
                    }
                    confirmations.remove(entry);
                    releaseExtra(entry);
                    inFlight = null;
                }
                entry.publish();
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        catch (IOException e) { close(CloseReason.IO_ERROR); }
        finally {
            close(CloseReason.IO_ERROR);
            final ArrayList<PendingWrite> pending;
            synchronized (this) {
                pending = new ArrayList<>(confirmations);
                confirmations.clear();
                for(var entry:pending)releaseExtra(entry);
                queuedExtra=0;inFlight = null;
            }
            // Close has decided failure. User continuations run only here, outside both locks.
            try { for (var entry : pending) entry.publish(); }
            finally { workerFinished(); }
        }
    }

    private void workerFinished() {
        if (remainingWorkers.decrementAndGet() == 0) owner.release(socket);
    }

    @Override public void close() { close(CloseReason.LOCAL_CLOSE); }

    void close(CloseReason cause) {
        synchronized (this) {
            if (!markClosed(cause)) return;
        }
        finishClose();
    }

    private boolean markClosed(CloseReason cause) {
        if (reason != null) return false;
        reason = cause;
        output.clear();
        for (var entry : confirmations) {
            if (!entry.decided) {
                entry.decided = true;
                entry.failure = closedError();
            }
        }
        return true;
    }

    private IOException closedError() { return new IOException("Connection closed: " + reason); }

    private void finishClose() {
        Transport.closeSocket(socket);
        if (writer != null) writer.interrupt();
    }
}
