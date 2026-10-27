package pt.uminho.esr.ott.transport;

import pt.uminho.esr.ott.protocol.ControlMessage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** TCP only. Callbacks must return promptly and normally publish to the node's event queue. */
public final class Transport implements AutoCloseable {
    public interface Listener {
        void connectionOpen(Connection connection);
        void message(Connection connection, ControlMessage message);
        void connectionClosed(Connection connection, Connection.CloseReason reason);
    }

    public record Options(int queueCapacity, int maxConnections, Duration connectTimeout,
                          Duration shutdownTimeout) {
        public Options {
            if (queueCapacity < 1 || queueCapacity > 256 || maxConnections < 1 || maxConnections > 64)
                throw new IllegalArgumentException("queueCapacity=1..256; maxConnections=1..64");
            checkDuration(connectTimeout);
            checkDuration(shutdownTimeout);
        }
        private static void checkDuration(Duration value) {
            Objects.requireNonNull(value);
            if (value.toMillis() < 1 || value.toMillis() > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Timeout must fit positive milliseconds");
        }
        public static Options defaults() {
            return new Options(256, 64, Duration.ofSeconds(2), Duration.ofSeconds(3));
        }
    }

    private final Object lifecycle = new Object();
    private final ServerSocket server;
    private final InetSocketAddress address;
    private final Options options;
    private final Listener listener;
    // Includes outbound dials in progress; null means not connected yet.
    private final Map<Socket, Connection> sockets = new HashMap<>();
    private final Set<Thread> workers = new HashSet<>();
    private boolean closed;

    public Transport(InetSocketAddress bind, Listener listener) throws IOException {
        this(bind, listener, Options.defaults());
    }

    public Transport(InetSocketAddress bind, Listener listener, Options options) throws IOException {
        this.options = Objects.requireNonNull(options);
        this.listener = Objects.requireNonNull(listener);
        requireResolved(bind);
        server = new ServerSocket();
        try {
            server.bind(bind, options.maxConnections());
            address = (InetSocketAddress) server.getLocalSocketAddress();
            synchronized (lifecycle) { launch("accept", this::acceptLoop); }
        } catch (IOException | RuntimeException e) {
            server.close();
            throw e;
        }
    }

    public InetSocketAddress localAddress() { return address; }

    /** Includes pending outbound dials and connections whose I/O is still terminating. */
    public int connectionCount() {
        synchronized (lifecycle) { return sockets.size(); }
    }

    /** No DNS resolution here: resolve endpoints off the node's serial control thread. */
    public CompletableFuture<Connection> connect(InetSocketAddress remote) {
        requireResolved(remote);
        var result = new CompletableFuture<Connection>();
        var socket = new Socket();
        synchronized (lifecycle) {
            if (!reserve(socket)) {
                closeSocket(socket);
                return CompletableFuture.failedFuture(new IOException(closed ? "Transport closed" : "Connection limit"));
            }
            launch("dial", () -> {
                try {
                    socket.connect(remote, (int) options.connectTimeout().toMillis());
                    Connection connection = attach(socket);
                    if (!result.complete(connection)) connection.close();
                } catch (IOException | RuntimeException e) {
                    closeSocket(socket);
                    release(socket);
                    result.completeExceptionally(e);
                }
            });
        }
        result.whenComplete((connection, error) -> {
            if (result.isCancelled()) closeSocket(socket);
        });
        return result;
    }

    private static void requireResolved(InetSocketAddress endpoint) {
        Objects.requireNonNull(endpoint);
        if (endpoint.isUnresolved()) throw new IllegalArgumentException("Resolved endpoint required");
    }

    private boolean reserve(Socket socket) {
        if (closed || sockets.size() >= options.maxConnections()) return false;
        sockets.put(socket, null);
        return true;
    }

    private Connection attach(Socket socket) throws IOException {
        synchronized (lifecycle) {
            if (closed || socket.isClosed()) throw new IOException("Transport or dial closed");
            socket.setTcpNoDelay(true);
            var connection = new Connection(this, socket, listener, options.queueCapacity());
            sockets.put(socket, connection);
            connection.start();
            return connection;
        }
    }

    private void acceptLoop() {
        while (!server.isClosed()) {
            Socket socket = null;
            try {
                socket = server.accept();
                synchronized (lifecycle) {
                    if (!reserve(socket)) { closeSocket(socket); continue; }
                }
                attach(socket);
            } catch (IOException | RuntimeException e) {
                if (socket != null) { closeSocket(socket); release(socket); continue; }
                if (server.isClosed()) return;
                // A local listener failure must not cause an unbounded retry loop.
                close();
                return;
            }
        }
    }

    // Called under lifecycle, including when Connection.start is invoked by attach.
    Thread launch(String role, Runnable action) {
        Thread thread = Thread.ofVirtual().name("ott-tcp-" + role).unstarted(() -> {
            try { action.run(); }
            finally {
                synchronized (lifecycle) {
                    workers.remove(Thread.currentThread());
                    lifecycle.notifyAll();
                }
            }
        });
        workers.add(thread);
        thread.start();
        return thread;
    }

    void release(Socket socket) {
        synchronized (lifecycle) { sockets.remove(socket); }
    }

    static void closeSocket(Socket socket) {
        try { socket.close(); } catch (IOException ignored) { }
    }

    /** Closes I/O first; external callers await a bounded deadline, callbacks initiate shutdown only. */
    @Override public void close() {
        Thread[] snapshot;
        synchronized (lifecycle) {
            closed = true;
            try { server.close(); } catch (IOException ignored) { }
            sockets.forEach((socket, connection) -> {
                if (connection == null) closeSocket(socket);
                else connection.close(Connection.CloseReason.SHUTDOWN);
            });
            snapshot = workers.toArray(Thread[]::new);
        }
        boolean calledFromWorker = false;
        for (Thread worker : snapshot) {
            if (worker == Thread.currentThread()) calledFromWorker = true;
            else worker.interrupt();
        }
        // Two callbacks may stop the transport simultaneously: neither may join the other.
        if (calledFromWorker) return;
        if (!awaitTermination(options.shutdownTimeout()))
            throw new IllegalStateException("TCP workers did not terminate: check non-blocking callbacks");
    }

    public boolean awaitTermination(Duration timeout) {
        Options.checkDuration(timeout);
        long deadline = System.nanoTime() + timeout.toNanos();
        boolean interrupted = false;
        try {
            synchronized (lifecycle) {
                while (workers.stream().anyMatch(t -> t != Thread.currentThread())) {
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0) return false;
                    try { java.util.concurrent.TimeUnit.NANOSECONDS.timedWait(lifecycle, remaining); }
                    catch (InterruptedException e) { interrupted = true; }
                }
                return true;
            }
        } finally { if (interrupted) Thread.currentThread().interrupt(); }
    }
}
