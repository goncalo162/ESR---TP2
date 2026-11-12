package pt.uminho.esr.ott.overlay;

import pt.uminho.esr.ott.config.NodeConfiguration;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Owns one serial executor and lends it to the embedded neighbor service. */
public final class OverlayNode implements AutoCloseable {
    private final NodeConfiguration configuration;
    private final UUID instance;
    private final NeighborManager.Observer observer;
    private final AtomicReference<Throwable> failure = new AtomicReference<>();
    private final CompletableFuture<Void> started = new CompletableFuture<>();
    private NeighborManager.SerialOwner owner;
    private volatile NeighborManager manager;
    private CompletionStage<NeighborManager.ShutdownReport> shutdown;
    private boolean closed, launched;
    private long stopStarted, stopBudget;

    public OverlayNode(NodeConfiguration configuration, UUID instanceId, NeighborManager.Observer observer) {
        this.configuration = Objects.requireNonNull(configuration);
        instance = Objects.requireNonNull(instanceId); this.observer = Objects.requireNonNull(observer);
        // Reuse the existing offline validation before creating even the serial thread.
        new NeighborManager(configuration, instance, observer);
    }
    public synchronized CompletionStage<Void> start() {
        if (closed || launched) return started.minimalCompletionStage();
        launched = true;
        try {
            owner = NeighborManager.createSerialOwner(configuration.runtime().overlay().eventsCapacity());
            manager = new NeighborManager(configuration, instance, owner, event -> {
                if (event instanceof NeighborManager.Diagnostic d && (d.phase() == NeighborManager.Phase.FAILED
                        || d.phase() == NeighborManager.Phase.CLOSED && (d.reason() == NeighborManager.Reason.RESOURCE_LIMIT
                        || d.reason() == NeighborManager.Reason.CONSUMER_FAILURE)))
                    failure.compareAndSet(null, new IllegalStateException("Neighbor service: " + d.reason()));
                observer.onEvent(event);
            });
            manager.start().whenComplete((v, error) -> {
                if (error != null) {
                    synchronized (this) { if (!closed) failure.compareAndSet(null, error); }
                    started.completeExceptionally(error);
                } else started.complete(null);
            });
        } catch (RuntimeException e) {
            failure.compareAndSet(null, e); started.completeExceptionally(e);
            if (manager != null) manager.requestClose();
            if (owner != null) owner.requestClose();
        }
        return started.minimalCompletionStage();
    }
    public Map<String, Neighbor.Session> snapshot() { var m = manager; return m == null ? Map.of() : m.snapshot(); }
    public Map<String, Neighbor> diagnostics() { var m = manager; return m == null ? Map.of() : m.diagnostics(); }
    public Map<String,NeighborManager.PeerStatus> discoveryStatus(){var m=manager;return m==null?Map.of():m.discoveryStatus();}
    public CompletionStage<java.util.List<CandidateCache.CandidateView>> candidates(){var m=manager;return m==null?CompletableFuture.completedStage(java.util.List.of()):m.candidates();}
    public OverlayRecovery.RecoveryStatus recoveryStatus(){var m=manager;return m==null?
        new OverlayRecovery.RecoveryStatus(OverlayRecovery.Phase.WAITING,OverlayRecovery.RecoveryReason.NONE,0,Optional.empty(),0):m.recoveryStatus();}
    public Optional<Throwable> failure() { return Optional.ofNullable(failure.get()); }
    public synchronized CompletionStage<NeighborManager.ShutdownReport> requestShutdown(int reason, Duration remainingBudget) {
        long nanos = budget(remainingBudget);
        if (reason < 0 || reason > 1) throw new IllegalArgumentException("Leave reason 0/1 required");
        if (shutdown != null) return shutdown;
        closed = true; stopStarted = System.nanoTime();
        stopBudget = Math.min(nanos, configuration.runtime().control().shutdownTimeout().toNanos());
        started.completeExceptionally(new IllegalStateException("Node shutting down"));
        if (manager == null) shutdown = CompletableFuture.completedStage(new NeighborManager.ShutdownReport(java.util.List.of(), false));
        else shutdown = manager.requestShutdown(reason, Duration.ofNanos(stopBudget));
        return shutdown;
    }
    public boolean awaitTermination(Duration remainingBudget) {
        long nanos = budget(remainingBudget), began = System.nanoTime();
        final CompletionStage<NeighborManager.ShutdownReport> stopping;
        final NeighborManager.SerialOwner serial;
        synchronized (this) {
            if (!closed) return false; stopping = shutdown; serial = owner;
            nanos = Math.min(nanos, left(stopBudget, stopStarted));
        }
        if (serial != null && serial.isOwnerThread()) return false;
        boolean done = true;
        try { if (stopping != null) stopping.toCompletableFuture().get(left(nanos, began), TimeUnit.NANOSECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); done = false; }
        catch (ExecutionException | TimeoutException e) { done = false; }
        var m = manager;
        if (!done && m != null) m.requestClose();
        if (m != null) done &= m.awaitTermination(Duration.ofNanos(left(nanos, began)));
        if (serial != null) {
            serial.requestClose(); done &= serial.awaitTermination(Duration.ofNanos(left(nanos, began)));
        }
        return done;
    }
    @Override public void close() {
        final long nanos;
        synchronized (this) {
            if (!closed) {
                closed = true; stopStarted = System.nanoTime(); stopBudget = configuration.runtime().control().shutdownTimeout().toNanos();
                started.completeExceptionally(new IllegalStateException("Node closed"));
            }
            if (manager != null) manager.requestClose();
            if (owner != null && owner.isOwnerThread()) { owner.requestClose(); return; }
            nanos = left(stopBudget, stopStarted);
        }
        if (!awaitTermination(Duration.ofNanos(nanos))) throw new IllegalStateException("Node shutdown deadline");
    }
    private static long budget(Duration duration) {
        Objects.requireNonNull(duration);
        if (duration.isNegative()) throw new IllegalArgumentException("Negative budget");
        try { return duration.toNanos(); } catch (ArithmeticException e) { throw new IllegalArgumentException("Budget overflow", e); }
    }
    private static long left(long nanos, long began) { return Math.max(0, nanos - (System.nanoTime() - began)); }
}
