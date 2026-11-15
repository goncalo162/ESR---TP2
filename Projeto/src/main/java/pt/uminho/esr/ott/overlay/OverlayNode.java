package pt.uminho.esr.ott.overlay;

import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.monitoring.NetworkMonitor;
import pt.uminho.esr.ott.routing.RoutingService;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Owns one serial executor and lends it to the embedded neighbor service. */
public final class OverlayNode implements AutoCloseable
{

    //* === Atributos e Variáveis === *

    private final NodeConfiguration configuration;
    private final UUID instance;
    private final NeighborManager.Observer observer;
    private final NetworkMonitor.Factory monitorFactory;
    private volatile NetworkMonitor monitor;
    private final RoutingService.Factory routingFactory;
    private volatile RoutingService routing;
    private final AtomicReference<Throwable> failure = new AtomicReference<>();
    private final CompletableFuture<Void> started = new CompletableFuture<>();
    private NeighborManager.SerialOwner owner;
    private volatile NeighborManager manager;
    private CompletionStage<NeighborManager.ShutdownReport> shutdown;
    private boolean closed;
    private boolean launched;
    private long stopStarted;
    private long stopBudget;



    //* === Construtores === *

    public OverlayNode(NodeConfiguration configuration, UUID instanceId, NeighborManager.Observer observer)
    {
        this(configuration, instanceId, observer, NetworkMonitor::new);
    }

    OverlayNode(NodeConfiguration configuration, UUID instanceId, NeighborManager.Observer observer,
            NetworkMonitor.Factory monitorFactory)
    {
        this(configuration, instanceId, observer, monitorFactory, RoutingService::new);
    }

    OverlayNode(NodeConfiguration configuration, UUID instanceId, NeighborManager.Observer observer,
            NetworkMonitor.Factory monitorFactory, RoutingService.Factory routingFactory)
    {
        this.configuration = Objects.requireNonNull(configuration);
        instance = Objects.requireNonNull(instanceId);
        this.observer = Objects.requireNonNull(observer);
        this.monitorFactory = Objects.requireNonNull(monitorFactory);
        this.routingFactory = Objects.requireNonNull(routingFactory);
        RoutingService.validateConfiguration(configuration, instanceId);
        new NeighborManager(configuration, instance, observer);
    }



    //* === Métodos Públicos === *

    /** Q-C-32: diagnostic delegation only, no new owner or transport. */
    public RoutingService.TraceTicket requestTrace(UUID streamId)
    {
        Objects.requireNonNull(streamId);
        var r = routing;
        return r == null ? RoutingService.unavailableTrace(streamId) : r.requestTrace(streamId);
    }

    public RoutingService.Snapshot routingSnapshot()
    {
        var r = routing;
        return r == null ? RoutingService.emptySnapshot() : r.snapshot();
    }

    public NetworkMonitor.Snapshot monitoringSnapshot()
    {
        var m = monitor;
        return m == null ? new NetworkMonitor.Snapshot(0, Map.of(), new NetworkMonitor.Diagnostics(
                NetworkMonitor.Phase.NEW, 0, 0, 0, 0, 0, 0, NetworkMonitor.Reason.NONE,
                Optional.empty(), 0, false)) : m.snapshot();
    }

    public CompletionStage<Void> start()
    {
        CompletionStage<Void> launch = null;

        synchronized(this)
        {
            if(closed || launched)
                return started.minimalCompletionStage();

            launched = true;

            try
            {
                owner = NeighborManager.createSerialOwner(configuration.runtime().overlay().eventsCapacity());

                var service = new NeighborManager(configuration, instance, owner, this::onNeighborEvent);

                monitor = Objects.requireNonNull(monitorFactory.create(configuration, instance, owner, new NetworkMonitor.ControlPort()
                {
                    public CompletionStage<pt.uminho.esr.ott.transport.Connection.SendResult> send(Neighbor.Session session,
                            pt.uminho.esr.ott.protocol.ControlMessage message)
                    {
                        return service.send(session, message);
                    }

                    public CompletionStage<Boolean> disconnect(Neighbor.Session session)
                    {
                        return service.disconnect(session);
                    }
                }, event ->
                {
                    try
                    {
                        var r = routing;
                        if(r != null)
                            r.onMetrics(event);
                    }
                    finally
                    {
                        if(event instanceof NetworkMonitor.LocalFailure f && f.fatal())
                            failMonitoring(new IllegalStateException("Network monitor: " + f.reason()));
                    }
                }));

                routing = Objects.requireNonNull(routingFactory.create(configuration, instance, owner, service::send,
                        event ->
                        {
                            if(event.fatal())
                                failMonitoring(new IllegalStateException("Routing: " + event.reason()));
                        }));

                if(!owner.offer(() ->
                {
                    try
                    {
                        monitor.start();
                        routing.start();
                    }
                    catch(RuntimeException e)
                    {
                        failMonitoring(e);
                    }
                }))
                    throw new RejectedExecutionException("Node start owner rejected");

                // Register the manager's rollback hook before close can acquire the node lock.
                // Its bind event is queued after monitor.start on the same owner.
                manager = service;
                launch = service.start();
            }
            catch(RuntimeException e)
            {
                failure.compareAndSet(null, e);
            }
        }

        // Consumer futures and cleanup are completed outside the node lock.
        if(failure.get() != null)
        {
            started.completeExceptionally(failure.get());
            stopImmediately();
        }
        else
            launch.whenComplete((v, error) ->
            {
                if(error != null)
                {
                    synchronized(this)
                    {
                        if(!closed)
                            failure.compareAndSet(null, error);
                    }

                    started.completeExceptionally(error);
                    stopImmediately();
                }
                else
                    started.complete(null);
            });

        return started.minimalCompletionStage();
    }



    //* === Event Handlers === *

    private void onNeighborEvent(NeighborManager.Event event)
    {
        try
        {
            if(event instanceof NeighborManager.Diagnostic d && (d.phase() == NeighborManager.Phase.FAILED
                    || d.phase() == NeighborManager.Phase.CLOSED && (d.reason() == NeighborManager.Reason.RESOURCE_LIMIT
                    || d.reason() == NeighborManager.Reason.CONSUMER_FAILURE)))
                failure.compareAndSet(null, new IllegalStateException("Neighbor service: " + d.reason()));

            try
            {
                var m = monitor;
                if(m != null)
                    m.onEvent(event);
            }
            finally
            {
                var r = routing;
                if(r != null)
                    r.onNeighborEvent(event);
            }
        }
        catch(RuntimeException e)
        {
            failMonitoring(e);
        }
        finally
        {
            observer.onEvent(event);
        }
    }

    private void failMonitoring(Throwable error)
    {
        failure.compareAndSet(null, error);
        stopImmediately();
    }



    //* === Shutdown === *

    private void stopImmediately()
    {
        synchronized(this)
        {
            if(!closed)
            {
                closed = true;
                stopStarted = System.nanoTime();
                stopBudget = configuration.runtime().control().shutdownTimeout().toNanos();
            }
        }

        started.completeExceptionally(new IllegalStateException("Node closed"));

        try
        {
            var r = routing;
            if(r != null)
                r.requestClose();
        }
        finally
        {
            try
            {
                var mon = monitor;
                if(mon != null)
                    mon.requestClose();
            }
            finally
            {
                try
                {
                    var service = manager;
                    if(service != null)
                        service.requestClose();
                }
                finally
                {
                    var serial = owner;
                    if(serial != null)
                        serial.requestClose();
                }
            }
        }
    }

    public Map<String, Neighbor.Session> snapshot()
    {
        var m = manager;
        return m == null ? Map.of() : m.snapshot();
    }

    public Map<String, Neighbor> diagnostics()
    {
        var m = manager;
        return m == null ? Map.of() : m.diagnostics();
    }

    public Map<String, NeighborManager.PeerStatus> discoveryStatus()
    {
        var m = manager;
        return m == null ? Map.of() : m.discoveryStatus();
    }

    public CompletionStage<java.util.List<CandidateCache.CandidateView>> candidates()
    {
        var m = manager;
        return m == null ? CompletableFuture.completedStage(java.util.List.of()) : m.candidates();
    }

    public OverlayRecovery.RecoveryStatus recoveryStatus()
    {
        var m = manager;
        return m == null ?
                new OverlayRecovery.RecoveryStatus(OverlayRecovery.Phase.WAITING, OverlayRecovery.RecoveryReason.NONE, 0, Optional.empty(), 0)
                : m.recoveryStatus();
    }

    public Optional<Throwable> failure()
    {
        return Optional.ofNullable(failure.get());
    }

    public CompletionStage<NeighborManager.ShutdownReport> requestShutdown(int reason, Duration remainingBudget)
    {
        long nanos = budget(remainingBudget);

        if(reason < 0 || reason > 1)
            throw new IllegalArgumentException("Leave reason 0/1 required");

        final CompletableFuture<NeighborManager.ShutdownReport> result;

        synchronized(this)
        {
            if(shutdown != null)
                return shutdown;

            if(!closed)
            {
                closed = true;
                stopStarted = System.nanoTime();
                stopBudget = Math.min(nanos, configuration.runtime().control().shutdownTimeout().toNanos());
            }

            result = new CompletableFuture<>();
            shutdown = result.minimalCompletionStage();
        }

        started.completeExceptionally(new IllegalStateException("Node shutting down"));

        CompletionStage<Void> routeCleanup;
        try
        {
            var r = routing;
            routeCleanup = r == null ? CompletableFuture.completedStage(null) : r.requestClose();
        }
        catch(RuntimeException e)
        {
            routeCleanup = CompletableFuture.failedStage(e);
        }

        CompletionStage<Void> monitoring;
        try
        {
            var mon = monitor;
            monitoring = mon == null ? CompletableFuture.completedStage(null) : mon.requestClose();
        }
        catch(RuntimeException e)
        {
            monitoring = CompletableFuture.failedStage(e);
        }

        CompletionStage<NeighborManager.ShutdownReport> leaving;
        try
        {
            var service = manager;
            leaving = service == null ? CompletableFuture.completedStage(new NeighborManager.ShutdownReport(java.util.List.of(), false))
                    : service.requestShutdown(reason, Duration.ofNanos(left(stopBudget, stopStarted)));
        }
        catch(RuntimeException e)
        {
            leaving = CompletableFuture.failedStage(e);
        }

        var leave = leaving;
        CompletableFuture.allOf(routeCleanup.toCompletableFuture(), monitoring.toCompletableFuture(), leave.toCompletableFuture()).whenComplete((v, error) ->
        {
            if(error != null)
                result.completeExceptionally(error);
            else
                result.complete(leave.toCompletableFuture().join());
        });

        return shutdown;
    }

    public boolean awaitTermination(Duration remainingBudget)
    {
        long nanos = budget(remainingBudget);
        long began = System.nanoTime();
        final CompletionStage<NeighborManager.ShutdownReport> stopping;
        final NeighborManager.SerialOwner serial;

        synchronized(this)
        {
            if(!closed)
                return false;

            stopping = shutdown;
            serial = owner;
            nanos = Math.min(nanos, left(stopBudget, stopStarted));
        }

        if(serial != null && serial.isOwnerThread())
            return false;

        boolean done = true;

        try
        {
            if(stopping != null)
                stopping.toCompletableFuture().get(left(nanos, began), TimeUnit.NANOSECONDS);
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
            done = false;
        }
        catch(ExecutionException | TimeoutException e)
        {
            done = false;
        }

        var m = manager;
        if(!done && m != null)
            m.requestClose();

        if(m != null)
            done &= m.awaitTermination(Duration.ofNanos(left(nanos, began)));

        var mon = monitor;
        if(mon != null)
        {
            if(!done)
                mon.requestClose();

            done &= mon.awaitTermination(Duration.ofNanos(left(nanos, began)));
        }

        var r = routing;
        if(r != null)
        {
            if(!done)
                r.requestClose();

            done &= r.awaitTermination(Duration.ofNanos(left(nanos, began)));
        }

        if(serial != null)
        {
            serial.requestClose();
            done &= serial.awaitTermination(Duration.ofNanos(left(nanos, began)));
        }

        return done;
    }

    @Override
    public void close()
    {
        stopImmediately();
        final NeighborManager.SerialOwner serial;
        final long nanos;

        synchronized(this)
        {
            serial = owner;
            nanos = left(stopBudget, stopStarted);
        }

        if(serial != null && serial.isOwnerThread())
            return;

        if(!awaitTermination(Duration.ofNanos(nanos)))
            throw new IllegalStateException("Node shutdown deadline");
    }



    //* === Utility Methods === *

    private static long budget(Duration duration)
    {
        Objects.requireNonNull(duration);
        if(duration.isNegative())
            throw new IllegalArgumentException("Negative budget");

        try
        {
            return duration.toNanos();
        }
        catch(ArithmeticException e)
        {
            throw new IllegalArgumentException("Budget overflow", e);
        }
    }

    private static long left(long nanos, long began)
    {
        return Math.max(0, nanos - (System.nanoTime() - began));
    }
}
