package pt.uminho.esr.ott.monitoring;

import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.overlay.Neighbor;
import pt.uminho.esr.ott.overlay.NeighborManager;
import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.transport.Connection;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.LongSupplier;

/** Session-scoped TCP probes. All network state belongs to the borrowed serial owner. */
public final class NetworkMonitor implements AutoCloseable {
    public interface ControlPort {
        CompletionStage<Connection.SendResult> send(Neighbor.Session session, ControlMessage message);
        CompletionStage<Boolean> disconnect(Neighbor.Session session);
    }
    @FunctionalInterface public interface Observer { void onEvent(Event event); }
    @FunctionalInterface public interface Factory {
        NetworkMonitor create(NodeConfiguration config, UUID instanceId,
                NeighborManager.SerialOwner owner, ControlPort control, Observer observer);
    }
    public interface Ticket { void cancel(); }
    public interface Scheduler {
        Ticket schedule(Runnable task, long delayNanos);
        void requestClose();
        boolean awaitTermination(Duration budget);
    }
    public enum Phase { NEW, RUNNING, CLOSING, STOPPED }
    public enum Reason {
        NONE, TIMEOUT, DOWN, ADMISSION_CLOSED, OUTPUT_OVERFLOW, INVALID_LOCAL_MESSAGE,
        ADMISSION_FAILURE, LOCAL_CAPACITY, ECHO_BACKPRESSURE, SCHEDULER_REJECTED,
        OWNER_REJECTED, CONSUMER_FAILURE, CLOCK_ERROR, PROBE_ID_EXHAUSTED, STOPPED
    }
    public sealed interface Event permits Updated, Invalidated, LocalFailure { }
    public record Updated(LinkMetrics metrics) implements Event {
        public Updated { Objects.requireNonNull(metrics); }
    }
    public record Invalidated(Neighbor.Session session, Reason reason) implements Event {
        public Invalidated { Objects.requireNonNull(session); Objects.requireNonNull(reason); }
    }
    public record LocalFailure(Optional<Neighbor.Session> session, Reason reason, boolean fatal) implements Event {
        public LocalFailure { Objects.requireNonNull(session); Objects.requireNonNull(reason); }
    }
    public record Diagnostics(Phase phase, int sessions, int pending, int sendTickets,
            int closeTickets, int timerTickets, int queuedCloses, Reason lastReason,
            Optional<Neighbor.Session> lastSession, long localFailures, boolean countersSaturated) {
        public Diagnostics {
            Objects.requireNonNull(phase); Objects.requireNonNull(lastReason);
            Objects.requireNonNull(lastSession);
            if (sessions < 0 || sessions > 63 || pending < 0 || pending > sessions
                    || sendTickets < 0 || sendTickets > 126 || closeTickets < 0 || closeTickets > 1
                    || timerTickets < 0 || timerTickets > 2 || queuedCloses < 0 || queuedCloses > 63
                    || localFailures < 0) throw new IllegalArgumentException("Invalid diagnostic counts");
        }
    }
    public record Snapshot(long publishedAtNanos, Map<String, LinkMetrics> links, Diagnostics diagnostics) {
        public Snapshot {
            Objects.requireNonNull(links); Objects.requireNonNull(diagnostics);
            if (links.size() > 63) throw new IllegalArgumentException("At most 63 monitored sessions");
            for (var entry : links.entrySet())
                if (!entry.getKey().equals(entry.getValue().session().nodeId()))
                    throw new IllegalArgumentException("Snapshot key/session mismatch");
            links = Map.copyOf(links);
        }
    }
    private static final UUID ZERO = new UUID(0, 0);
    private final NodeConfiguration config;
    private final UUID instance;
    private final NeighborManager.SerialOwner owner;
    private final ControlPort control;
    private final Observer observer;
    private final LongSupplier clock, probeIds;
    private final Scheduler scheduler;
    private final int maxSessions, maxSends, failureThreshold;
    private final long interval, timeout, shutdownNanos;
    private final double alpha;
    private final LinkedHashMap<String, Link> links = new LinkedHashMap<>();
    private final LinkedHashSet<Neighbor.Session> closes = new LinkedHashSet<>();
    // This lock protects physical reservations only, never network state.
    private final Object resources = new Object();
    private final Set<Completion> completions = new HashSet<>();
    private final AtomicBoolean stopping = new AtomicBoolean(), wakePending = new AtomicBoolean();
    private final AtomicReference<Reason> externalFailure = new AtomicReference<>();
    private final AtomicReference<Long> firstStop = new AtomicReference<>();
    private final CompletableFuture<Void> cleaned = new CompletableFuture<>();
    private final Object terminated = new Object();
    private volatile Snapshot view;
    private NeighborManager.Registration registration;
    private Ticket alarm;
    private long alarmAt, alarmDelay, nextProbe, lastProbe = -1, localFailures;
    private boolean started, driving, idExhausted, countersSaturated;
    private int cursor;
    private Phase phase = Phase.NEW;
    private Reason lastReason = Reason.NONE;
    private Optional<Neighbor.Session> lastSession = Optional.empty();

    private static final class Link {
        final Neighbor.Session session;
        Pending pending;
        boolean echoBusy, eligible = true, saturated;
        long cadenceAt, samples, sampleAt;
        int missed;
        double ewma;
        Link(Neighbor.Session session) { this.session = session; }
        LinkMetrics metrics() {
            return new LinkMetrics(session, samples == 0 ? LinkMetrics.State.UNMEASURED
                    : missed > 0 ? LinkMetrics.State.SUSPECT : LinkMetrics.State.MEASURED,
                    samples == 0 ? OptionalDouble.empty() : OptionalDouble.of(ewma),
                    samples == 0 ? OptionalLong.empty() : OptionalLong.of((long) Math.ceil(ewma)),
                    samples, saturated, samples == 0 ? OptionalLong.empty() : OptionalLong.of(sampleAt), missed);
        }
    }
    private static final class Pending {
        final long id, began;
        final UUID request;
        boolean admitted, deadlinePassed;
        Long early;
        Pending(long id, UUID request, long began) { this.id = id; this.request = request; this.began = began; }
    }
    public NetworkMonitor(NodeConfiguration config, UUID instanceId, NeighborManager.SerialOwner owner,
            ControlPort control, Observer observer) {
        this(config, instanceId, owner, control, observer, System::nanoTime, null, null);
    }
    NetworkMonitor(NodeConfiguration config, UUID instanceId, NeighborManager.SerialOwner owner,
            ControlPort control, Observer observer, LongSupplier clock, Scheduler scheduler, LongSupplier probeIds) {
        this.config = Objects.requireNonNull(config); instance = Objects.requireNonNull(instanceId);
        this.owner = Objects.requireNonNull(owner); this.control = Objects.requireNonNull(control);
        this.observer = Objects.requireNonNull(observer); this.clock = Objects.requireNonNull(clock);
        // Reuse complete offline validation. This constructor allocates no sockets or owner.
        new NeighborManager(config, instanceId, owner, e -> { });
        if (ZERO.equals(instanceId)) throw new IllegalArgumentException("Nonzero instance required");
        maxSessions = Math.min(63, config.runtime().control().maxConnections());
        maxSends = Math.min(owner.capacity(), 2 * maxSessions);
        if (maxSessions < 1 || maxSends < 1) throw new IllegalArgumentException("Positive physical limits required");
        var options = config.runtime().monitoring();
        interval = options.probeInterval().toNanos(); timeout = options.probeTimeout().toNanos();
        failureThreshold = options.probeFailures(); alpha = options.ewmaAlpha();
        shutdownNanos = config.runtime().control().shutdownTimeout().toNanos();
        this.scheduler = scheduler == null ? new Timers() : scheduler;
        this.probeIds = probeIds == null ? () -> {
            long id = nextProbe; if (nextProbe < Long.MAX_VALUE) nextProbe++; return id;
        } : probeIds;
        publish();
        registration = owner.onStop(this::ownerStopped);
    }
    private void requireOwner() {
        if (!owner.isOwnerThread()) throw new IllegalStateException("Monitoring state requires the node owner");
    }
    public void start() {
        requireOwner();
        if (started || stopping.get()) return;
        started = true;
        if (stopping.get()) { cleanup(); return; }
        phase = Phase.RUNNING; publish();
    }
    public void onEvent(NeighborManager.Event event) {
        requireOwner(); Objects.requireNonNull(event);
        if (!started || stopping.get()) return;
        try {
            if (event instanceof NeighborManager.NeighborUp up) {
                var old = links.get(up.session().nodeId());
                if (old == null || !old.session.equals(up.session())) {
                    if (old == null && links.size() == maxSessions) {
                        local(Reason.LOCAL_CAPACITY, up.session(), false); return;
                    }
                    links.put(up.session().nodeId(), new Link(up.session()));
                }
            } else if (event instanceof NeighborManager.NeighborDown down) {
                var link = current(down.session());
                if (link != null) invalidate(link, Reason.DOWN, false);
            } else if (event instanceof NeighborManager.Received received) {
                var link = current(received.session()); var m = received.message();
                if (link != null && m.version() == 2 && link.session.nodeId().equals(m.senderId())
                        && link.session.instanceId().equals(m.senderInstance()) && m.requestId() != null
                        && !ZERO.equals(m.requestId()) && m.payload() instanceof ControlMessage.Probe probe
                        && probe.probeId() >= 0) {
                    if (m.type() == pt.uminho.esr.ott.protocol.MessageType.PROBE_PING) echo(link, m);
                    else if (m.type() == pt.uminho.esr.ott.protocol.MessageType.PROBE_PONG) pong(link, m, probe);
                }
            }
            drive();
        } catch (RuntimeException e) { if (!stopping.get()) local(Reason.CLOCK_ERROR, null, true); }
    }
    private Link current(Neighbor.Session session) {
        var link = links.get(session.nodeId()); return link != null && link.session.equals(session) ? link : null;
    }
    private long elapsed(long now, long began) {
        long value = now - began;
        if (value < 0) throw new IllegalArgumentException("Monotonic horizon exceeded");
        return value;
    }
    private void pong(Link link, ControlMessage m, ControlMessage.Probe probe) {
        var p = link.pending;
        if (p == null || p.id != probe.probeId() || !p.request.equals(m.requestId())) return;
        long now = clock.getAsLong(), age = elapsed(now, p.began);
        if (!p.admitted) {
            if (age < timeout && p.early == null) p.early = now;
            else if (age >= timeout) p.deadlinePassed = true;
        } else if (age >= timeout) expire(link, p);
        else sample(link, p, now);
    }
    private void sample(Link link, Pending p, long now) {
        double value = Math.max(1, elapsed(now, p.began) / 1000.0);
        link.ewma = link.samples == 0 ? value : alpha * value + (1 - alpha) * link.ewma;
        if (!Double.isFinite(link.ewma) || link.ewma < 1 || link.ewma >= 0x1p63)
            throw new IllegalArgumentException("RTT overflow");
        if (link.samples == Long.MAX_VALUE) link.saturated = true; else link.samples++;
        link.sampleAt = now; link.missed = 0; link.pending = null;
        emit(new Updated(link.metrics()));
    }
    private void expire(Link link, Pending p) {
        if (link.pending != p) return;
        link.pending = null; link.missed++;
        if (link.missed == failureThreshold) invalidate(link, Reason.TIMEOUT, true);
        else emit(new Updated(link.metrics()));
    }
    private void probe(Link link, long now) {
        if (sendCount() >= maxSends) {
            link.cadenceAt = now; link.eligible = false; local(Reason.LOCAL_CAPACITY, link.session, false); return;
        }
        if (idExhausted) { local(Reason.PROBE_ID_EXHAUSTED, link.session, true); return; }
        long id = probeIds.getAsLong();
        if (id < 0 || id <= lastProbe) { local(Reason.PROBE_ID_EXHAUSTED, link.session, true); return; }
        lastProbe = id; idExhausted = id == Long.MAX_VALUE;
        var pending = new Pending(id, UUID.randomUUID(), now);
        link.pending = pending; link.cadenceAt = now; link.eligible = false;
        send(link, pending, new ControlMessage(pt.uminho.esr.ott.protocol.MessageType.PROBE_PING,
                pending.request, config.nodeId(), instance, new ControlMessage.Probe(id)), false);
    }
    private void echo(Link link, ControlMessage ping) {
        if (link.echoBusy || sendCount() >= maxSends) { local(Reason.ECHO_BACKPRESSURE, link.session, false); return; }
        link.echoBusy = true;
        send(link, null, new ControlMessage(pt.uminho.esr.ott.protocol.MessageType.PROBE_PONG,
                ping.requestId(), config.nodeId(), instance, ping.payload()), true);
    }
    private void send(Link link, Pending p, ControlMessage message, boolean echo) {
        var ticket = reserve(false);
        if (ticket == null) { if (!stopping.get()) local(Reason.ADMISSION_FAILURE, link.session, true); return; }
        try {
            Objects.requireNonNull(control.send(link.session, message)).whenComplete((result, error) ->
                ticket.complete(() -> {
                    if (current(link.session) != link) return;
                    if (echo) link.echoBusy = false;
                    if (error != null || result == null) { local(Reason.ADMISSION_FAILURE, link.session, true); return; }
                    switch (result) {
                        case ACCEPTED -> {
                            if (!echo && link.pending == p) {
                                p.admitted = true;
                                if (p.early != null) sample(link, p, p.early);
                                else if (elapsed(clock.getAsLong(), p.began) >= timeout) expire(link, p);
                            }
                        }
                        case CLOSED -> { local(Reason.ADMISSION_CLOSED, link.session, false); invalidate(link, Reason.ADMISSION_CLOSED, true); }
                        case OVERFLOW -> { local(Reason.OUTPUT_OVERFLOW, link.session, false); invalidate(link, Reason.OUTPUT_OVERFLOW, true); }
                        case INVALID_MESSAGE -> local(Reason.INVALID_LOCAL_MESSAGE, link.session, true);
                    }
                }));
        } catch (RuntimeException e) { ticket.complete(() -> local(Reason.ADMISSION_FAILURE, link.session, true)); }
    }
    private void invalidate(Link link, Reason reason, boolean disconnect) {
        if (!links.remove(link.session.nodeId(), link)) return;
        link.pending = null;
        emit(new Invalidated(link.session, reason));
        if (disconnect && !stopping.get()) {
            if (closes.size() == maxSessions && !closes.contains(link.session)) { local(Reason.LOCAL_CAPACITY, link.session, true); return; }
            closes.add(link.session);
        }
    }
    private void closeNext() {
        if (closes.isEmpty() || closeCount() != 0 || stopping.get()) return;
        var session = closes.iterator().next(); closes.remove(session);
        var ticket = reserve(true);
        if (ticket == null) { if (!stopping.get()) local(Reason.ADMISSION_FAILURE, session, true); return; }
        try {
            Objects.requireNonNull(control.disconnect(session)).whenComplete((result, error) ->
                    ticket.complete(() -> { if (error != null || result == null) local(Reason.ADMISSION_FAILURE, session, true); }));
        } catch (RuntimeException e) { ticket.complete(() -> local(Reason.ADMISSION_FAILURE, session, true)); }
    }
    private void drive() {
        if (driving || stopping.get()) return;
        driving = true;
        try {
            long now = clock.getAsLong(); closeNext();
            var states = new ArrayList<>(links.values()); int size = states.size();
            int start = size == 0 ? 0 : cursor++ % size;
            if (cursor < 0) cursor = 0;
            for (int i = 0; i < size && !stopping.get(); i++) {
                var link = states.get((start + i) % size);
                if (current(link.session) != link) continue;
                var p = link.pending;
                if (p != null && elapsed(now, p.began) >= timeout) {
                    if (p.admitted) expire(link, p); else p.deadlinePassed = true;
                }
                if (current(link.session) == link && link.pending == null
                        && (link.eligible || elapsed(now, link.cadenceAt) >= interval)) probe(link, now);
            }
            closeNext(); rearm(now); publish();
        } catch (RuntimeException e) { if (!stopping.get()) local(Reason.CLOCK_ERROR, null, true); }
        finally { driving = false; }
    }
    private void rearm(long now) {
        if (stopping.get()) return;
        long delay = Long.MAX_VALUE;
        for (var link : links.values()) {
            if (link.pending != null) {
                if (!link.pending.deadlinePassed)
                    delay = Math.min(delay, Math.max(0, timeout - elapsed(now, link.pending.began)));
            } else delay = Math.min(delay, link.eligible ? 0 : Math.max(0, interval - elapsed(now, link.cadenceAt)));
        }
        if (delay == Long.MAX_VALUE) { if (alarm != null) alarm.cancel(); alarm = null; return; }
        if (alarm != null && Math.max(0, alarmDelay - elapsed(now, alarmAt)) == delay) return;
        if (alarm != null) alarm.cancel();
        alarm = null;
        try {
            alarmAt = now; alarmDelay = delay;
            alarm = scheduler.schedule(this::wake, delay);
        } catch (RuntimeException e) { local(Reason.SCHEDULER_REJECTED, null, true); }
    }
    private void wake() {
        if (stopping.get() || !wakePending.compareAndSet(false, true)) return;
        if (!owner.offer(() -> {
            wakePending.set(false);
            if (!stopping.get()) { if (alarm != null) alarm.cancel(); alarm = null; drive(); }
        })) {
            wakePending.set(false); if (!stopping.get()) emergency(Reason.OWNER_REJECTED);
        }
    }
    private void emit(Event event) {
        if (stopping.get()) return;
        try { observer.onEvent(event); }
        catch (RuntimeException e) { local(Reason.CONSUMER_FAILURE, null, true); }
    }
    private void local(Reason reason, Neighbor.Session session, boolean fatal) {
        if (stopping.get()) return;
        lastReason = reason; lastSession = Optional.ofNullable(session);
        if (localFailures == Long.MAX_VALUE) countersSaturated = true; else localFailures++;
        if (fatal) {
            markStop(); scheduler.requestClose(); cleanup();
            try { observer.onEvent(new LocalFailure(lastSession, reason, true)); } catch (RuntimeException ignored) { }
        } else emit(new LocalFailure(lastSession, reason, false));
        publish();
    }
    private void emergency(Reason reason) {
        externalFailure.compareAndSet(null, reason);
        markStop(); scheduler.requestClose();
        // State retirement runs in the existing owner's emergency cleanup, never on I/O.
        owner.fail(new RejectedExecutionException("Monitor: " + reason));
    }
    private void ownerStopped() {
        markStop(); scheduler.requestClose();
        Reason reason = externalFailure.get();
        if (reason != null) { lastReason = reason; lastSession = Optional.empty(); }
        cleanup();
        synchronized (resources) { for (var ticket : List.copyOf(completions)) ticket.retire(); }
        signal();
        if (reason != null) try { observer.onEvent(new LocalFailure(Optional.empty(), reason, true)); } catch (RuntimeException ignored) { }
    }
    public Snapshot snapshot() { return view; }
    private void publish() {
        var metrics = new LinkedHashMap<String, LinkMetrics>(); int pending = 0;
        for (var entry : links.entrySet()) { metrics.put(entry.getKey(), entry.getValue().metrics()); if (entry.getValue().pending != null) pending++; }
        view = new Snapshot(clock.getAsLong(), metrics, new Diagnostics(phase, links.size(), pending,
                sendCount(), closeCount(), scheduler instanceof Timers timers ? timers.count() : 0,
                closes.size(), lastReason, lastSession, localFailures, countersSaturated));
    }
    private void markStop() { firstStop.compareAndSet(null, System.nanoTime()); stopping.set(true); }
    public CompletionStage<Void> requestClose() {
        firstStop.compareAndSet(null, System.nanoTime());
        if (stopping.compareAndSet(false, true)) {
            scheduler.requestClose();
            if (owner.isOwnerThread()) cleanup();
            else if (!owner.offer(this::cleanup)) emergency(Reason.OWNER_REJECTED);
        }
        return cleaned.minimalCompletionStage();
    }
    private void cleanup() {
        if (cleaned.isDone()) return;
        markStop(); scheduler.requestClose();
        if (alarm != null) alarm.cancel(); alarm = null;
        links.clear(); closes.clear(); phase = Phase.STOPPED;
        synchronized (resources) {
            for (var ticket : List.copyOf(completions)) if (!ticket.queued) ticket.retire();
        }
        publish(); cleaned.complete(null);
        synchronized (resources) { removeHookIfRetired(); }
        signal();
    }
    public boolean awaitTermination(Duration budget) {
        long nanos = budgetNanos(budget), began = System.nanoTime();
        if (owner.isOwnerThread() || !stopping.get()) return false;
        nanos = Math.min(nanos, left(shutdownNanos, firstStop.get()));
        try { cleaned.get(left(nanos, began), TimeUnit.NANOSECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
        catch (ExecutionException | TimeoutException e) { return false; }
        if (!scheduler.awaitTermination(Duration.ofNanos(left(nanos, began)))) return false;
        synchronized (terminated) {
            while (physicalCount() != 0) {
                long remaining = left(nanos, began); if (remaining == 0) return false;
                if (owner.isOwnerThread()) return false;
                try { TimeUnit.NANOSECONDS.timedWait(terminated, remaining); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
            }
        }
        return true;
    }
    @Override public void close() {
        requestClose(); long began = firstStop.get();
        if (owner.isOwnerThread()) return;
        try { cleaned.get(left(shutdownNanos, began), TimeUnit.NANOSECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("Interrupted monitor close", e); }
        catch (ExecutionException | TimeoutException e) { throw new IllegalStateException("Monitor cleanup deadline", e); }
        if (!awaitTermination(Duration.ofNanos(left(shutdownNanos, began)))) throw new IllegalStateException("Monitor physical shutdown deadline");
    }
    private static long budgetNanos(Duration duration) {
        Objects.requireNonNull(duration);
        if (duration.isNegative()) throw new IllegalArgumentException("Negative budget");
        try { return duration.toNanos(); } catch (ArithmeticException e) { throw new IllegalArgumentException("Budget overflow", e); }
    }
    private static long left(long budget, long began) { return Math.max(0, budget - (System.nanoTime() - began)); }
    private int sendCount() { synchronized (resources) { return (int) completions.stream().filter(t -> !t.close).count(); } }
    private int closeCount() { synchronized (resources) { return (int) completions.stream().filter(t -> t.close).count(); } }
    private int physicalCount() { synchronized (resources) { return completions.size(); } }
    private void removeHookIfRetired() {
        if (cleaned.isDone() && completions.isEmpty() && registration != null) { registration.remove(); registration = null; }
    }
    private void signal() { synchronized (terminated) { terminated.notifyAll(); } }
    private Completion reserve(boolean close) {
        synchronized (resources) {
            if (stopping.get() || (close ? closeCount() >= 1 : sendCount() >= maxSends)) return null;
            var ticket = new Completion(close); completions.add(ticket); return ticket;
        }
    }
    private final class Completion {
        final boolean close;
        boolean callbackDone, continuationDone, claimed, queued;
        Completion(boolean close) { this.close = close; }
        void complete(Runnable action) {
            synchronized (resources) { if (claimed) return; claimed = true; }
            try {
                final boolean dispatch;
                synchronized (resources) {
                    dispatch = !stopping.get() && !continuationDone;
                    if (dispatch) queued = true;
                }
                if (!dispatch) { retire(); return; }
                if (!owner.offer(() -> {
                    try {
                        synchronized (resources) { if (continuationDone) return; }
                        if (!stopping.get()) action.run();
                    } catch (RuntimeException e) { local(Reason.ADMISSION_FAILURE, null, true); }
                    finally { retire(); if (!stopping.get()) drive(); }
                })) { retire(); if (!stopping.get()) emergency(Reason.OWNER_REJECTED); }
            } finally {
                synchronized (resources) { callbackDone = true; release(); }
                signal();
            }
        }
        void retire() { synchronized (resources) { continuationDone = true; release(); } }
        void release() {
            if (callbackDone && continuationDone && completions.remove(this)) {
                removeHookIfRetired();
                // A continuation may finish before its callback unwinds: a queued close
                // must still be driven when that physical reservation finally becomes free.
                if (!stopping.get()) wake();
            }
        }
    }
    /** A single helper with an admission ledger covering queued, running and canceled-running jobs. */
    static final class Timers implements Scheduler {
        private final Set<Job> jobs = new HashSet<>();
        private ScheduledThreadPoolExecutor executor;
        private boolean stopped;
        synchronized int count() { return jobs.size(); }
        @Override public synchronized Ticket schedule(Runnable task, long delayNanos) {
            Objects.requireNonNull(task);
            if (delayNanos < 0) throw new IllegalArgumentException("Negative timer delay");
            if (stopped || jobs.size() == 2) throw new RejectedExecutionException("Two physical monitor timer jobs maximum");
            if (executor == null) {
                executor = new ScheduledThreadPoolExecutor(1, Thread.ofPlatform().name("ott-monitor-timer").factory());
                executor.setRemoveOnCancelPolicy(true);
                executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            }
            var job = new Job(task); jobs.add(job);
            try { job.future = executor.schedule(job, delayNanos, TimeUnit.NANOSECONDS); }
            catch (RuntimeException e) { jobs.remove(job); throw e; }
            return job;
        }
        private final class Job implements Ticket, Runnable {
            final Runnable task;
            ScheduledFuture<?> future;
            boolean running, retired;
            Job(Runnable task) { this.task = task; }
            public void run() {
                synchronized (Timers.this) { if (retired) return; running = true; }
                try { task.run(); }
                finally { synchronized (Timers.this) { retired = true; jobs.remove(this); Timers.this.notifyAll(); } }
            }
            public void cancel() {
                synchronized (Timers.this) {
                    if (retired) return;
                    if (!running && future.cancel(false)) { retired = true; jobs.remove(this); }
                }
            }
        }
        public synchronized void requestClose() {
            if (stopped) return; stopped = true;
            for (var job : List.copyOf(jobs)) job.cancel();
            if (executor != null) executor.shutdown();
        }
        public boolean awaitTermination(Duration budget) {
            long nanos = budgetNanos(budget); ScheduledThreadPoolExecutor current;
            synchronized (this) { if (!stopped) return false; current = executor; }
            if (current == null) return true;
            try { return current.awaitTermination(nanos, TimeUnit.NANOSECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
        }
    }
}
