package pt.uminho.esr.ott.overlay;

import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.config.RuntimeConfiguration;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.protocol.ProtocolException;
import pt.uminho.esr.ott.transport.*;
import java.net.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.LongSupplier;

/** Initial discovery and persistent, authorized neighbors. All domain changes run on SerialOwner. */
public final class NeighborManager implements AutoCloseable {
    private static final UUID ZERO = new UUID(0, 0);
    public enum Phase { NOT_STARTED, DISCOVERING, READY, FAILED, CLOSED }
    public enum Reason { REMOTE_REJECTION, CONTEXT, TRANSPORT, TIMEOUT, RESOURCE_LIMIT,
        SHUTDOWN, LEAVE, CONSUMER_FAILURE, STALE, REQUIRED_DISCOVERY_MISSING, UNSUPPORTED_VERSION,
        INVALID_HANDSHAKE, DISCOVERY_BACKPRESSURE, DISCOVERY_TIMEOUT, DISCOVERY_STALE,
        DISCOVERY_INVALID_CONTENT, DISCOVERY_RATE_LIMIT, DISCOVERY_REMOTE_ERROR,
        DISCOVERY_WRITE_FAILED, DISCOVERY_GENERATION_EXHAUSTED, DISCOVERY_CACHE_REJECTION }
    public enum CapabilityState { NEGOTIATING_REQUIRED, ACTIVE_DISCOVERY, TEMPORARILY_UNAVAILABLE, INCOMPATIBLE, INVALID_ATTEMPT }
    public record PeerStatus(CapabilityState state, Reason currentReason, Optional<Reason> lastConfirmedRejection,
            int observedVersion,long supportedMask,long requiredMask,long selectedMask,long retryDelayMillis) { }
    public Map<String,PeerStatus> discoveryStatus(){return peerStatusView;}
    /** Local diagnostics only; these values are never serialized on the wire. */
    public enum LeaveOutcome { LOCAL_WRITE_CONFIRMED, WRITE_FAILED, TIMED_OUT, ABORTED }
    public record LeaveAttempt(Neighbor.Session session, LeaveOutcome outcome) {
        public LeaveAttempt { Objects.requireNonNull(session); Objects.requireNonNull(outcome); }
    }
    public record ShutdownReport(List<LeaveAttempt> attempts, boolean notificationTimedOut) {
        public ShutdownReport {
            Objects.requireNonNull(attempts);
            if (attempts.size() > 63) throw new IllegalArgumentException("Configured degree cap is 63");
            attempts = List.copyOf(attempts);
        }
    }
    /** Completion confirms only the local notification phase, never a peer ACK or termination. */
    public synchronized CompletionStage<ShutdownReport> requestShutdown(int reason, Duration remainingBudget) {
        Objects.requireNonNull(remainingBudget);
        if (reason < 0 || reason > 1 || remainingBudget.isNegative())
            throw new IllegalArgumentException("Leave reason 0/1 and nonnegative budget required");
        final long nanos;
        try { nanos = remainingBudget.toNanos(); }
        catch (ArithmeticException e) { throw new IllegalArgumentException("Budget overflow", e); }
        if (shutdownResult != null) return shutdownView;
        shutdownResult = new CompletableFuture<>(); shutdownView = shutdownResult.minimalCompletionStage();
        notificationStarted = now();
        notificationTotalBudget = Math.min(nanos, plan.config.runtime().control().shutdownTimeout().toNanos());
        notificationBudget = notificationTotalBudget / 2;
        draining.set(true);
        if (!launched || stopping.get() || notificationBudget == 0) {
            requestClose(); publishShutdown(LeaveOutcome.ABORTED); return shutdownView;
        }
        if (!post(() -> beginShutdown(reason), true)) {
            requestClose();
            // No notification event was admitted, so there is no owner-owned attempt map to read.
            shutdownResult.complete(new ShutdownReport(List.of(), false));
        }
        return shutdownView;
    }
    private void beginShutdown(int reason) {
        if(learner!=null)learner.close();
        cancel(initialSlot); initialSlot = null;
        if (discovery != null) retire(discovery);
        for (var p : peers.values()) {
            cancel(p.slot); p.slot = null;
            if (p.attempt != null) retire(p.attempt);
        }
        for (var c : List.copyOf(contexts.values())) {
            cancel(c.deadline); c.deadline = null;
            if (!c.bootstrap && c.active && c.peer != null && c.peer.context == c
                    && c.peer.session != null && c.channel.id().equals(c.peer.session.connectionId())
                    && Objects.equals(c.remoteInstance, c.peer.session.instanceId()))
                leaveResults.put(c.peer.session, null);
            else finish(c, Reason.SHUTDOWN, null);
        }
        long left = notificationBudget - (now() - notificationStarted);
        if (left <= 0) { finishShutdown(LeaveOutcome.TIMED_OUT, !leaveResults.isEmpty()); return; }
        if (leaveResults.isEmpty()) { finishShutdown(LeaveOutcome.ABORTED, false); return; }
        notificationTicket = deadlines.schedule(() -> post(() -> finishShutdown(LeaveOutcome.TIMED_OUT, true), true), left);
        for (var session : List.copyOf(leaveResults.keySet())) {
            if (stopping.get()) break;
            if (now() - notificationStarted >= notificationBudget) { finishShutdown(LeaveOutcome.TIMED_OUT, true); break; }
            var peer = current(session);
            if (peer == null || peer.context == null || !peer.context.active || !peer.context.channel.isOpen()
                    || !peer.context.channel.id().equals(session.connectionId())
                    || !Objects.equals(peer.context.remoteInstance, session.instanceId())) {
                writtenLeave(session, new IllegalStateException("Session retired")); continue;
            }
            try {
                peer.context.channel.sendAndFlush(new ControlMessage(MessageType.NODE_LEAVE, UUID.randomUUID(),
                        plan.local.nodeId(), instance, new ControlMessage.Leave(reason)))
                    .whenComplete((v, error) -> post(() -> writtenLeave(session, error), true));
            } catch (RuntimeException e) { writtenLeave(session, e); }
        }
    }
    private void writtenLeave(Neighbor.Session session, Throwable error) {
        if (shutdownResult.isDone() || !leaveResults.containsKey(session) || leaveResults.get(session) != null) return;
        if (now() - notificationStarted >= notificationBudget) { finishShutdown(LeaveOutcome.TIMED_OUT, true); return; }
        leaveResults.put(session, error == null ? LeaveOutcome.LOCAL_WRITE_CONFIRMED : LeaveOutcome.WRITE_FAILED);
        if (leaveResults.values().stream().allMatch(Objects::nonNull)) finishShutdown(LeaveOutcome.ABORTED, false);
    }
    private void finishShutdown(LeaveOutcome pending, boolean timedOut) {
        if (shutdownResult == null || shutdownResult.isDone()) return;
        notificationTimedOut |= timedOut;
        leaveResults.replaceAll((session, outcome) -> outcome == null ? pending : outcome);
        if (notificationTicket != null) { notificationTicket.cancel(); notificationTicket = null; }
        requestClose(); publishShutdown(pending);
    }
    private void publishShutdown(LeaveOutcome pending) {
        if (shutdownResult == null || shutdownResult.isDone()) return;
        if (notificationTicket != null) { notificationTicket.cancel(); notificationTicket = null; }
        var attempts = new ArrayList<LeaveAttempt>(leaveResults.size());
        leaveResults.forEach((session, outcome) -> attempts.add(new LeaveAttempt(session, outcome == null ? pending : outcome)));
        shutdownResult.complete(new ShutdownReport(attempts, notificationTimedOut));
    }
    public interface Observer { void onEvent(Event event); }
    public sealed interface Event permits NeighborUp, NeighborDown, Received, Diagnostic {}
    public record NeighborUp(Neighbor.Session session) implements Event {
        public NeighborUp { Objects.requireNonNull(session); }
    }
    public record NeighborDown(Neighbor.Session session, Reason reason) implements Event {
        public NeighborDown { Objects.requireNonNull(session); Objects.requireNonNull(reason); }
    }
    public record Received(Neighbor.Session session, ControlMessage message) implements Event {
        public Received { Objects.requireNonNull(session); Objects.requireNonNull(message); }
    }
    public record Diagnostic(Phase phase, Reason reason, Optional<UUID> attemptId,
            Optional<UUID> connectionId, Optional<Neighbor.Session> session,
            Optional<ControlMessage> remoteMessage) implements Event {
        public Diagnostic {
            Objects.requireNonNull(phase); Objects.requireNonNull(reason); Objects.requireNonNull(attemptId);
            Objects.requireNonNull(connectionId); Objects.requireNonNull(session); Objects.requireNonNull(remoteMessage);
        }
    }
    public static final class DiscoveryRejected extends IllegalStateException {
        private final ControlMessage remoteMessage;
        private final UUID attemptId, connectionId;
        DiscoveryRejected(ControlMessage message, UUID attempt, UUID connection) {
            super("Initial discovery rejected by " + message.senderId());
            remoteMessage = message; attemptId = attempt; connectionId = connection;
        }
        public ControlMessage remoteMessage() { return remoteMessage; }
        public UUID attemptId() { return attemptId; }
        public UUID connectionId() { return connectionId; }
    }
    public interface Registration { void remove(); }
    public interface SerialOwner extends AutoCloseable {
        int capacity(); boolean isOwnerThread(); boolean offer(Runnable event);
        Registration onStop(Runnable cleanup); void fail(Throwable cause); void requestClose();
        boolean awaitTermination(Duration timeout); @Override void close();
    }
    public static SerialOwner createSerialOwner(int capacity) { return new Owner(capacity); }
    interface Channel {
        UUID id(); boolean isOpen(); Connection.SendResult send(ControlMessage message);
        CompletableFuture<Void> sendAndFlush(ControlMessage message); void close();
        Connection.ExtensionEnqueue trySendExtension(ControlMessage message);
    }
    interface Ticket { void cancel(); }
    interface Deadlines {
        void limit(int maxPhysicalTickets); Ticket schedule(Runnable task, long delayNanos);
        Ticket periodic(Runnable task,long intervalNanos);
        void requestClose(); boolean awaitTermination(Duration timeout);
    }
    interface Jitter { long choose(long minInclusive, long maxInclusive); }
    interface Resolver {
        CompletableFuture<InetAddress> resolve(UUID jobId, String configuredHost); void retire(UUID jobId);
    }
    interface Sink {
        void opened(Channel channel, long observedAt);
        void message(Channel channel, ControlMessage message, long observedAt);
        void closed(Channel channel, Connection.CloseReason reason, long observedAt);
        void rejected(Channel channel,MessageCodec.RejectedExtraContent rejection,long observedAt);
        void protocolError(Channel channel,ProtocolException error,long observedAt);
    }
    interface Network {
        CompletableFuture<InetSocketAddress> open(InetSocketAddress bind, Transport.Options options, Sink sink);
        CompletableFuture<Channel> connect(InetSocketAddress remote); int reservations();
        void requestClose(); boolean awaitTermination(Duration timeout);
    }
    interface IoServices {
        Resolver resolver(); Network network(); void requestClose(); boolean awaitTermination(Duration timeout);
    }
    interface Ports {
        SerialOwner owner(int capacity); IoServices io(int maxPhysicalJobs); Deadlines deadlines(int maxPhysicalTickets);
    }
    private record Plan(NodeConfiguration config, ControlMessage.Endpoint local, Transport.Options options) {}
    private final Plan plan;
    private final UUID instance;
    private final Observer observer;
    private final LongSupplier clock;
    private final Jitter jitter;
    private final Ports ports;
    private final boolean borrowed;
    private volatile SerialOwner owner;
    private volatile IoServices io;
    private volatile Deadlines deadlines;
    private Registration registration;
    private final AtomicBoolean stopping = new AtomicBoolean();
    private final AtomicBoolean draining = new AtomicBoolean();
    private volatile CompletableFuture<ShutdownReport> shutdownResult;
    private CompletionStage<ShutdownReport> shutdownView;
    // Notification state is owned by SerialOwner, except the before-start empty report.
    private final Map<Neighbor.Session, LeaveOutcome> leaveResults = new LinkedHashMap<>();
    private long notificationStarted, notificationBudget, notificationTotalBudget;
    private boolean notificationTimedOut;
    private Ticket notificationTicket;
    private final AtomicReference<Throwable> failure = new AtomicReference<>();
    private final AtomicBoolean terminationObserved = new AtomicBoolean();
    private final CompletableFuture<Void> started = new CompletableFuture<>(), cleaned = new CompletableFuture<>();
    private final CompletionStage<Void> startView = started.minimalCompletionStage();
    private volatile Map<String, Neighbor.Session> activeView = Map.of();
    private volatile Map<String, Neighbor> diagnosticView = Map.of();
    private volatile Map<String,PeerStatus> peerStatusView=Map.of();
    private NeighborDiscovery learner;
    private final Semaphore cacheCommands=new Semaphore(4);
    private final Set<CompletableFuture<?>> commands = ConcurrentHashMap.newKeySet();
    private final Semaphore commandSlots;
    private volatile boolean launched;
    private volatile long closeStarted;
    // The following fields belong exclusively to the serial owner.
    private Phase phase = Phase.NOT_STARTED;
    private final Map<String, Peer> peers = new LinkedHashMap<>();
    private final Map<UUID, Context> contexts = new LinkedHashMap<>();
    private final Map<UUID, Attempt> nativeDials = new LinkedHashMap<>();
    private Attempt discovery;
    private Slot initialSlot;
    private long discoveryBase;
    private int retained;
    private boolean cleanupDone;
    private enum Direction { UNCLASSIFIED, INBOUND, OUTBOUND }
    private static final class Peer {
        final ControlMessage.Endpoint configured;
        Neighbor.State state = Neighbor.State.DISCONNECTED;
        Attempt attempt; Context context; Slot slot;
        Neighbor.Session session; long base, delay;
        CapabilityState capability=CapabilityState.NEGOTIATING_REQUIRED;
        Reason currentReason=Reason.STALE,lastConfirmed;
        int observedVersion;long supported,required,selected;
        Peer(ControlMessage.Endpoint endpoint) { configured = endpoint; }
    }
    private static final class Attempt {
        final UUID id = UUID.randomUUID(); final Peer peer;
        UUID job; boolean live = true; Context context;
        Attempt(Peer peer) { this.peer = peer; }
    }
    private static final class Slot { Ticket ticket; boolean live = true; }
    private record Frame(ControlMessage message, long observedAt) {}
    private static final class Context {
        final Channel channel; long openedAt;
        Direction direction = Direction.UNCLASSIFIED;
        final Set<UUID> barrier = new HashSet<>();
        final ArrayDeque<Frame> frames = new ArrayDeque<>();
        Attempt attempt; Peer peer; Slot deadline;
        UUID request, remoteInstance; ControlMessage.Endpoint endpoint;
        boolean bootstrap, sent, written, acknowledged, responding, active;
        Context(Channel channel, long at) { this.channel = channel; openedAt = at; }
    }
    public NeighborManager(NodeConfiguration config, UUID instanceId, Observer observer) {
        this(config, instanceId, null, observer, System::nanoTime,
                (min, max) -> ThreadLocalRandom.current().nextLong(min, max + 1), new RealPorts());
    }
    public NeighborManager(NodeConfiguration config, UUID instanceId, SerialOwner borrowedOwner, Observer observer) {
        this(config, instanceId, Objects.requireNonNull(borrowedOwner), observer, System::nanoTime,
                (min, max) -> ThreadLocalRandom.current().nextLong(min, max + 1), new RealPorts());
    }
    NeighborManager(NodeConfiguration config, UUID instanceId, SerialOwner borrowedOwner,
                    Observer observer, LongSupplier clock, Jitter jitter, Ports ports) {
        plan = validate(config, instanceId); instance = instanceId;
        this.observer = Objects.requireNonNull(observer); this.clock = Objects.requireNonNull(clock);
        this.jitter = Objects.requireNonNull(jitter); this.ports = Objects.requireNonNull(ports);
        borrowed = borrowedOwner != null; owner = borrowedOwner;
        if (borrowed && owner.capacity() != config.runtime().overlay().eventsCapacity())
            throw new IllegalArgumentException("Owner capacity must equal overlay.eventsCapacity");
        commandSlots = new Semaphore(config.runtime().overlay().eventsCapacity() + 1);
    }
    public synchronized CompletionStage<Void> start() {
        if (launched || stopping.get() || draining.get()) return startView;
        launched = true;
        try {
            if (!borrowed) owner = ports.owner(plan.config.runtime().overlay().eventsCapacity());
            registration = owner.onStop(this::ownerStopped);
            io = ports.io(65);
            deadlines = ports.deadlines(63 + plan.options.maxConnections() + 1);
            deadlines.limit(plan.options.maxConnections() + 1);
            post(() -> { phase = Phase.DISCOVERING; bind(); });
        } catch (RuntimeException e) { fatal(e); }
        return startView;
    }
    public Map<String, Neighbor.Session> snapshot() { return stopping.get() ? Map.of() : activeView; }
    public Map<String, Neighbor> diagnostics() { return diagnosticView; }
    public CompletionStage<List<CandidateCache.CandidateView>> candidates() {
        if(!cacheCommands.tryAcquire())return CompletableFuture.failedStage(new RejectedExecutionException("Four cache commands maximum"));
        var result=command(List.<CandidateCache.CandidateView>of(),()->learner==null?List.<CandidateCache.CandidateView>of():learner.snapshot());
        result.whenComplete((v,e)->cacheCommands.release());return result;
    }
    public CompletionStage<Connection.SendResult> send(Neighbor.Session expected, ControlMessage message) {
        Objects.requireNonNull(expected); Objects.requireNonNull(message);
        return command(Connection.SendResult.CLOSED, () -> {
            var peer = current(expected);
            if (peer == null) return Connection.SendResult.CLOSED;
            if (!envelope(message) || !message.senderId().equals(plan.local.nodeId())
                    || !message.senderInstance().equals(instance) || !application(message.type()) || extra(message.type())
                    || message.payload() instanceof ControlMessage.ErrorPayload p && p.relatedType() == 17)
                return Connection.SendResult.INVALID_MESSAGE;
            return peer.context.channel.send(message);
        });
    }
    public CompletionStage<Boolean> disconnect(Neighbor.Session expected) {
        Objects.requireNonNull(expected);
        return command(false, () -> {
            var peer = current(expected); if (peer == null) return false;
            finish(peer.context, Reason.TRANSPORT, null); return true;
        });
    }
    private Peer current(Neighbor.Session expected) {
        var peer = peers.get(expected.nodeId());
        return peer != null && expected.equals(peer.session) ? peer : null;
    }
    private <T> CompletionStage<T> command(T closedValue, java.util.function.Supplier<T> operation) {
        if (stopping.get() || draining.get() || !launched) return CompletableFuture.completedStage(closedValue);
        if (!commandSlots.tryAcquire()) {
            fatal(new RejectedExecutionException("Command capacity"));
            return CompletableFuture.failedStage(new RejectedExecutionException("Command capacity"));
        }
        var result = new CompletableFuture<T>(); commands.add(result);
        result.whenComplete((v, e) -> { commands.remove(result); commandSlots.release(); });
        if (!post(() -> { if (!result.isDone()) result.complete(draining.get() ? closedValue : operation.get()); }, true)) result.complete(closedValue);
        return result.minimalCompletionStage();
    }
    private void postOrClose(Channel channel, Runnable event) { if (!post(event)) channel.close(); }
    private void postFrameOrClose(Channel channel,Runnable event) {
        // Stop application admissions during the notification phase, but leave
        // healthy sockets alive until NODE_LEAVE has actually written/flushed.
        if(!post(event) && !draining.get())channel.close();
    }
    private boolean post(Runnable event) { return post(event, false); }
    private boolean post(Runnable event, boolean lifecycle) {
        var target = owner;
        if (stopping.get() || target == null || draining.get() && !lifecycle) return false;
        if (target.offer(() -> {
            if (stopping.get()) { cleanup(); return; }
            if (draining.get() && !lifecycle) return;
            try { event.run(); } catch (RuntimeException e) { fatal(e); }
        })) return true;
        fatal(new RejectedExecutionException("Serial event capacity exceeded")); return false;
    }
    private void fatal(Throwable cause) {
        failure.compareAndSet(null, cause);
        started.completeExceptionally(cause);
        requestClose();
        var target = owner; if (target != null) target.fail(cause);
    }
    private void emit(Event event) {
        try { observer.onEvent(event); }
        catch (RuntimeException e) { fatal(new IllegalStateException("Observer failed", e)); }
    }
    private void diagnostic(Reason reason, Attempt attempt, Context context, ControlMessage remote) {
        emit(new Diagnostic(phase, reason, Optional.ofNullable(attempt == null ? null : attempt.id),
                Optional.ofNullable(context == null ? null : context.channel.id()),
                Optional.ofNullable(context == null || context.peer == null ? null : context.peer.session),
                Optional.ofNullable(remote)));
    }
    private long now() { return clock.getAsLong(); }
    private long connectBudget() { return plan.config.runtime().control().connectTimeout().toNanos(); }
    private long helloBudget() { return plan.config.runtime().overlay().helloTimeout().toNanos(); }
    private long budget(Context c) { return c.bootstrap ? plan.config.runtime().control().bootstrapQueryTimeout().toNanos() : helloBudget(); }
    private boolean expired(long began, long duration) { return now() - began >= duration; }
    private boolean current(Context c) { return contexts.get(c.channel.id()) == c; }
    private Slot schedule(long nanos, Runnable action) {
        var slot = new Slot();
        slot.ticket = deadlines.schedule(() -> post(() -> {
            if (slot.live) { slot.live = false; action.run(); }
        }), Math.max(1, nanos));
        return slot;
    }
    private void cancel(Slot slot) { if (slot != null) { slot.live = false; slot.ticket.cancel(); } }
    private void publish() {
        var active = new LinkedHashMap<String, Neighbor.Session>();
        var all = new LinkedHashMap<String, Neighbor>();
        for (var p : peers.values()) {
            if (p.session != null) active.put(p.configured.nodeId(), p.session);
            all.put(p.configured.nodeId(), new Neighbor(p.configured.nodeId(), p.configured,
                    p.state, Optional.ofNullable(p.session), p.delay));
        }
        var status=new LinkedHashMap<String,PeerStatus>();
        for(var p:peers.values())status.put(p.configured.nodeId(),new PeerStatus(p.capability,p.currentReason,
                Optional.ofNullable(p.lastConfirmed),p.observedVersion,p.supported,p.required,p.selected,p.delay));
        peerStatusView=Map.copyOf(status);
        activeView = Map.copyOf(active); diagnosticView = Map.copyOf(all);
    }
    private final Sink sink = new Sink() {
        public void opened(Channel ch, long at) { postOrClose(ch, () -> openedOnOwner(ch, at)); }
        public void message(Channel ch, ControlMessage m, long at) { postFrameOrClose(ch, () -> messageOnOwner(ch, m, at)); }
        public void rejected(Channel ch,MessageCodec.RejectedExtraContent rejection,long at) {
            postFrameOrClose(ch,()->{
                var c=contexts.get(ch.id());
                if(c==null)return;
                if(!c.active || learner==null || c.peer==null || c.peer.context!=c
                        || !learner.rejected(c.peer.session,rejection))finish(c,Reason.CONTEXT,null);
            });
        }
        public void protocolError(Channel ch,ProtocolException error,long at) {
            postOrClose(ch,()->{
                var c=contexts.get(ch.id());if(c==null)return;
                var remote=error.getCause() instanceof MessageCodec.HandshakeFailure h?h.message():null;
                if(remote!=null) {
                    if(remote.type()==MessageType.HELLO && c.direction==Direction.INBOUND && c.peer==null) {
                        // Reuse normal endpoint/identity authorization; no unknown peer status can be created.
                        hello(c,remote);return;
                    }
                    if(c.peer!=null && identity(c,remote) && Objects.equals(c.request,remote.requestId())) {
                        incompatible(c.peer,remote,Reason.REQUIRED_DISCOVERY_MISSING);
                        finish(c,Reason.REQUIRED_DISCOVERY_MISSING,remote);return;
                    }
                }
                Reason reason=error.errorCode()==ErrorCode.UNSUPPORTED_VERSION?Reason.UNSUPPORTED_VERSION:Reason.INVALID_HANDSHAKE;
                if(c.peer!=null&&c.peer.context==c) {
                    c.peer.capability=reason==Reason.UNSUPPORTED_VERSION?CapabilityState.INCOMPATIBLE:CapabilityState.INVALID_ATTEMPT;
                    if(error.getCause() instanceof MessageCodec.VersionFailure version)c.peer.observedVersion=version.observed();
                    c.peer.currentReason=reason;c.peer.lastConfirmed=reason;publish();
                }
                finish(c,reason,null);
            });
        }
        public void closed(Channel ch, Connection.CloseReason why, long at) {
            postOrClose(ch, () -> { var c = contexts.get(ch.id()); if (c != null) finish(c, Reason.TRANSPORT, null); });
        }
    };
    private void bind() {
        final long began = now(); final UUID job = UUID.randomUUID();
        initialSlot = schedule(connectBudget(), () -> failDiscovery(new IllegalStateException("Bind resolution timeout"), Reason.TIMEOUT, null, null));
        io.resolver().resolve(job, plan.config.bindHost()).whenComplete((address, error) -> post(() -> {
            if (phase != Phase.DISCOVERING || stopping.get() || draining.get()) return;
            if (error != null || expired(began, connectBudget())) {
                failDiscovery(error == null ? new IllegalStateException("Bind resolution timeout") : error, Reason.TRANSPORT, null, null); return;
            }
            cancel(initialSlot);
            long bindStart = now();
            initialSlot = schedule(connectBudget(), () -> failDiscovery(new IllegalStateException("Bind timeout"), Reason.TIMEOUT, null, null));
            io.network().open(new InetSocketAddress(address, plan.config.controlPort()), plan.options, sink)
                    .whenComplete((bound, bindError) -> {
                if (stopping.get()) { io.network().requestClose(); return; }
                post(() -> {
                    cancel(initialSlot);
                    if (bindError != null || expired(bindStart, connectBudget())) {
                        failDiscovery(bindError == null ? new IllegalStateException("Bind timeout") : bindError, Reason.TRANSPORT, null, null); return;
                    }
                    discover();
                });
            });
        }));
    }
    private boolean live(Attempt a) {
        return !draining.get() && a.live && (a.peer == null ? phase == Phase.DISCOVERING && discovery == a
                : phase == Phase.READY && a.peer.attempt == a);
    }
    private void discover() {
        if (phase != Phase.DISCOVERING || discovery != null) return;
        discovery = new Attempt(null);
        resolve(discovery, plan.config.bootstrap().orElseThrow().host(), plan.config.bootstrap().orElseThrow().port());
    }
    private void dialPeer(Peer p) {
        if (phase != Phase.READY || p.session != null || p.attempt != null || !initiates(p)) return;
        // Logically retired native dials remain reserved until their native completion.
        if (nativeDials.values().stream().anyMatch(a -> a.peer == p)) { retry(p); return; }
        p.attempt = new Attempt(p); p.state = Neighbor.State.CONNECTING; publish();
        resolve(p.attempt, p.configured.host(), p.configured.controlPort());
    }
    private boolean initiates(Peer peer) { return plan.local.nodeId().compareTo(peer.configured.nodeId()) < 0; }
    private void phaseSlot(Attempt a, Slot slot) {
        if (a.peer == null) { cancel(initialSlot); initialSlot = slot; }
        else { cancel(a.peer.slot); a.peer.slot = slot; }
    }
    private void resolve(Attempt a, String host, int port) {
        long began = now(); a.job = UUID.randomUUID();
        phaseSlot(a, schedule(connectBudget(), () -> { if (live(a)) attemptFailed(a, Reason.TIMEOUT, null); }));
        io.resolver().resolve(a.job, host).whenComplete((address, error) -> post(() -> {
            if (!live(a)) return;
            io.resolver().retire(a.job); a.job = null;
            if (error != null || expired(began, connectBudget())) {
                attemptFailed(a, error == null ? Reason.TIMEOUT : error instanceof RejectedExecutionException
                        ? Reason.RESOURCE_LIMIT : Reason.TRANSPORT, null); return;
            }
            phaseSlot(a, schedule(connectBudget(), () -> { if (live(a)) attemptFailed(a, Reason.TIMEOUT, null); }));
            if (nativeDials.size() >= plan.options.maxConnections()) { attemptFailed(a, Reason.RESOURCE_LIMIT, null); return; }
            nativeDials.put(a.id, a);
            io.network().connect(new InetSocketAddress(address, port)).whenComplete((ch, dialError) -> {
                long observed = now();
                if (stopping.get()) { if (ch != null) ch.close(); return; }
                if (!post(() -> dialResult(a, ch, dialError, observed)) && ch != null) ch.close();
            });
        }));
    }
    private void dialResult(Attempt a, Channel ch, Throwable error, long observed) {
        // Associate this handle before excluding the token from any other channel's barrier.
        if (ch != null) {
            var c = contexts.get(ch.id());
            if (c == null && ch.isOpen() && live(a)) c = makeContext(ch, observed);
            if (c == null || !ch.isOpen() || !live(a)) {
                if (c != null) finish(c, Reason.STALE, null); else ch.close();
                if (live(a)) attemptFailed(a, Reason.TRANSPORT, null);
            } else {
                c.direction = Direction.OUTBOUND; c.barrier.clear(); c.attempt = a; a.context = c;
                c.peer = a.peer; c.bootstrap = a.peer == null;
                if (a.peer != null) { a.peer.context = c; a.peer.state = Neighbor.State.HANDSHAKING; }
                phaseSlotCancel(a);
                armContext(c);
                if (current(c)) {
                    if (expired(c.openedAt, budget(c))) finish(c, Reason.TIMEOUT, null);
                    else { beginOutbound(c); drain(c); }
                }
            }
        } else if (live(a)) attemptFailed(a, Reason.TRANSPORT, null);
        nativeDials.remove(a.id);
        for (var c : List.copyOf(contexts.values())) {
            if (c.direction == Direction.UNCLASSIFIED && c.barrier.remove(a.id) && c.barrier.isEmpty()) classifyInbound(c);
        }
    }
    private void phaseSlotCancel(Attempt a) {
        if (a.peer == null) { cancel(initialSlot); initialSlot = null; }
        else { cancel(a.peer.slot); a.peer.slot = null; }
    }
    private Context makeContext(Channel ch, long at) {
        if (!ch.isOpen() || contexts.size() >= plan.options.maxConnections()) { ch.close(); return null; }
        var c = new Context(ch, at); c.barrier.addAll(nativeDials.keySet()); contexts.put(ch.id(), c);
        armContext(c); return c;
    }
    private void openedOnOwner(Channel ch, long at) {
        var c = contexts.get(ch.id());
        if (c != null) {
            // Select the earlier observation using elapsed subtraction, including nanoTime wrap.
            if (c.openedAt - at > 0) { c.openedAt = at; armContext(c); }
            return;
        }
        c = makeContext(ch, at);
        if (c != null && c.barrier.isEmpty()) classifyInbound(c);
    }
    private void armContext(Context c) {
        cancel(c.deadline);
        long left = budget(c) - (now() - c.openedAt);
        if (left <= 0) { finish(c, Reason.TIMEOUT, null); return; }
        c.deadline = schedule(left, () -> { if (current(c)) finish(c, Reason.TIMEOUT, null); });
    }
    private void classifyInbound(Context c) {
        if (!current(c)) return;
        c.direction = Direction.INBOUND;
        if (phase != Phase.READY) { finish(c, Reason.CONTEXT, null); return; }
        drain(c);
    }
    private void beginOutbound(Context c) {
        c.request = UUID.randomUUID(); c.sent = true;
        var type = c.bootstrap ? MessageType.REGISTER : MessageType.HELLO;
        write(c, new ControlMessage(type, c.request, plan.local.nodeId(), instance,
                c.bootstrap?new ControlMessage.EndpointPayload(plan.local):new ControlMessage.Hello(plan.local,1,1)), false);
    }
    private void write(Context c, ControlMessage message, boolean closeAfter) {
        if (c.responding) { finish(c, Reason.RESOURCE_LIMIT, null); return; }
        c.responding = true;
        c.channel.sendAndFlush(message).whenComplete((v, error) -> post(() -> {
            if (!current(c)) return;
            // A bootstrap deliberately closes after its complete reply. Its reader can
            // enqueue that reply before Closed while the writer's callback is pending.
            // Only the reader's ordered Closed event/deadline may discard that reply.
            if (c.bootstrap && error != null) return;
            if (error != null || !c.bootstrap && !c.channel.isOpen()) { finish(c, Reason.TRANSPORT, null); return; }
            if (expired(c.openedAt, budget(c))) { finish(c, Reason.TIMEOUT, null); return; }
            c.responding = false;
            if (closeAfter) { finish(c, Reason.CONTEXT, null); return; }
            c.written = true;
            if (!c.bootstrap) activate(c);
        }));
    }
    private void messageOnOwner(Channel ch, ControlMessage message, long at) {
        var c = contexts.get(ch.id());
        if (c == null) {
            emit(new Diagnostic(phase, Reason.STALE, Optional.empty(), Optional.of(ch.id()), Optional.empty(), Optional.of(message)));
            return; // No tombstones: a retired connection cannot authorize anything.
        }
        // Reader callbacks are ordered. Deliver a captured ACTIVE frame before its
        // queued Closed event even if the physical socket has already reached EOF.
        if (!c.bootstrap && !c.active && !ch.isOpen() || !c.active && (expired(c.openedAt, budget(c)) || at - c.openedAt >= budget(c))) {
            finish(c, Reason.TIMEOUT, null); return;
        }
        if (c.direction == Direction.UNCLASSIFIED) { retain(c, message, at); return; }
        handle(c, message, at);
    }
    private boolean envelope(ControlMessage m) {
        return m != null && m.version() == MessageCodec.VERSION && m.type() != null && m.requestId() != null
                && validId(m.senderId()) && m.senderInstance() != null && !ZERO.equals(m.senderInstance()) && m.payload() != null;
    }
    private static boolean application(MessageType type) { return type != null && type.code() >= 7 && type.code() <= 19; }
    private static boolean extra(MessageType type){return type==MessageType.DISCOVERY_QUERY||type==MessageType.DISCOVERY_SNAPSHOT;}
    private boolean identity(Context c, ControlMessage m) {
        return c.peer != null && c.peer.configured.nodeId().equals(m.senderId())
                && (c.remoteInstance == null || c.remoteInstance.equals(m.senderInstance()));
    }
    private void handle(Context c, ControlMessage m, long at) {
        if (!c.active && expired(c.openedAt, budget(c))) { finish(c, Reason.TIMEOUT, null); return; }
        if (!envelope(m)) { contextFailure(c, m); return; }
        if (c.bootstrap) { discoveryReply(c, m); return; }
        if (c.active) {
            if (!identity(c, m)) { contextFailure(c, m); return; }
            if (m.type() == MessageType.NODE_LEAVE && m.payload() instanceof ControlMessage.Leave) finish(c, Reason.LEAVE, null);
            else if(extra(m.type())) {
                if(learner==null||!learner.receive(c.peer.session,m,at))contextFailure(c,m);
            }
            else if (application(m.type())) {
                if(m.type()==MessageType.ERROR && learner!=null)learner.error(c.peer.session,m);
                emit(new Received(c.peer.session, m));
            }
            else contextFailure(c, m);
            return;
        }
        if (c.direction == Direction.INBOUND && c.peer == null) {
            if (m.type() != MessageType.HELLO) { contextFailure(c, m); return; }
            hello(c, m); return;
        }
        if (!identity(c, m)) { contextFailure(c, m); return; }
        if (m.type() == MessageType.ERROR && m.payload() instanceof ControlMessage.ErrorPayload p) {
            int related = c.direction == Direction.OUTBOUND ? 5 : 6;
            if (c.request.equals(m.requestId()) && p.relatedType() == related) {
                finish(c, Reason.REMOTE_REJECTION, m); return;
            }
            if (legitimateWindow(c)) { retain(c, m, at); return; }
            contextFailure(c, m); return;
        }
        if (c.direction == Direction.OUTBOUND && m.type() == MessageType.HELLO_OK
                && m.payload() instanceof ControlMessage.HelloOk ack && c.request.equals(m.requestId()) && !c.acknowledged) {
            if(ack.selectedMask()!=1){incompatible(c.peer,m,Reason.REQUIRED_DISCOVERY_MISSING);finish(c,Reason.REQUIRED_DISCOVERY_MISSING,m);return;}
            c.peer.observedVersion=2;c.peer.selected=1;
            c.remoteInstance = m.senderInstance(); c.endpoint = withInstance(c.peer.configured, c.remoteInstance);
            c.acknowledged = true; activate(c); return;
        }
        if (application(m.type()) && legitimateWindow(c)) { retain(c, m, at); return; }
        contextFailure(c, m);
    }
    private boolean legitimateWindow(Context c) {
        return c.peer != null && c.remoteInstance != null && (c.direction == Direction.INBOUND || c.acknowledged);
    }
    private void hello(Context c, ControlMessage m) {
        if(ZERO.equals(m.requestId())){reject(c,m,ErrorCode.INVALID_MESSAGE);return;}
        var p = peers.get(m.senderId());
        if (p == null || !(m.payload() instanceof ControlMessage.Hello ep) || ep.endpoint() == null) {
            reject(c, m, ErrorCode.UNAUTHORIZED_NEIGHBOR); return;
        }
        var e = ep.endpoint();
        boolean allowed;
        try { allowed = m.senderId().equals(e.nodeId()) && m.senderInstance().equals(e.instanceId())
                && sameEndpoint(e, p.configured) && m.senderId().compareTo(plan.local.nodeId()) < 0; }
        catch (IllegalArgumentException ex) { allowed = false; }
        if (!allowed) { reject(c, m, ErrorCode.UNAUTHORIZED_NEIGHBOR); return; }
        if (p.context != null || p.session != null || p.attempt != null) { reject(c, m, ErrorCode.DUPLICATE_ID); return; }
        if(ep.supportedMask()!=1||ep.requiredMask()!=1) {
            incompatible(p,m,Reason.REQUIRED_DISCOVERY_MISSING);reject(c,m,ErrorCode.INVALID_MESSAGE);return;
        }
        p.observedVersion=2;p.supported=1;p.required=1;
        c.peer = p; c.endpoint = withInstance(p.configured, m.senderInstance()); c.remoteInstance = m.senderInstance();
        c.request = m.requestId(); c.sent = true; p.context = c; p.state = Neighbor.State.HANDSHAKING; publish();
        write(c, new ControlMessage(MessageType.HELLO_OK, c.request, plan.local.nodeId(), instance, new ControlMessage.HelloOk(1)), false);
    }
    private void incompatible(Peer p,ControlMessage m,Reason reason) {
        if(p.session!=null)return;
        p.capability=CapabilityState.INCOMPATIBLE;p.currentReason=reason;p.lastConfirmed=reason;p.observedVersion=m.version();
        if(m.payload() instanceof ControlMessage.Hello h){p.supported=h.supportedMask();p.required=h.requiredMask();}
        if(m.payload() instanceof ControlMessage.HelloOk h)p.selected=h.selectedMask();publish();
    }
    private void reject(Context c, ControlMessage m, ErrorCode code) {
        diagnostic(Reason.CONTEXT, c.attempt, c, m);
        if (!envelope(m) || m.type() == MessageType.ERROR || c.responding) { finish(c, Reason.CONTEXT, null); return; }
        write(c, new ControlMessage(MessageType.ERROR, m.requestId(), plan.local.nodeId(), instance,
                new ControlMessage.ErrorPayload(code, m.type().code(), "Neighbor handshake rejected")), true);
    }
    private void contextFailure(Context c, ControlMessage m) {
        if (c.bootstrap) {
            diagnostic(Reason.CONTEXT, c.attempt, c, m);
            failDiscovery(new IllegalStateException("Invalid initial discovery context"), Reason.CONTEXT, c, m);
        } else { diagnostic(Reason.CONTEXT, c.attempt, c, m); finish(c, Reason.CONTEXT, null); }
    }
    private void discoveryReply(Context c, ControlMessage m) {
        if (c.attempt == null || !live(c.attempt) || !c.sent || !c.request.equals(m.requestId())) { contextFailure(c, m); return; }
        if (m.type() == MessageType.ERROR && m.payload() instanceof ControlMessage.ErrorPayload e && e.relatedType() == 1) {
            if (e.code() == ErrorCode.TIMEOUT || e.code() == ErrorCode.RESOURCE_LIMIT) finish(c, Reason.REMOTE_REJECTION, m);
            else failDiscovery(new DiscoveryRejected(m, c.attempt.id, c.channel.id()), Reason.REMOTE_REJECTION, c, m);
            return;
        }
        if (m.type() != MessageType.REGISTER_OK || !(m.payload() instanceof ControlMessage.NeighborList list)) { contextFailure(c, m); return; }
        final Map<String, Peer> installed;
        try { installed = validateList(list); }
        catch (IllegalArgumentException e) { failDiscovery(e, Reason.CONTEXT, c, m); return; }
        phase = Phase.READY; cancel(initialSlot); initialSlot = null;
        c.attempt.live = false; discovery = null;
        removeContext(c); c.channel.close(); peers.putAll(installed);
        deadlines.limit(peers.size() + plan.options.maxConnections() + 1);
        learner=new NeighborDiscovery(plan.config,instance,peers.entrySet().stream().collect(
                java.util.stream.Collectors.toMap(Map.Entry::getKey,e->e.getValue().configured)),clock,new NeighborDiscovery.Host() {
            public boolean post(Runnable task){return NeighborManager.this.post(task);}
            public Map<String,Neighbor.Session> active(){return activeView;}
            public Connection.ExtensionEnqueue enqueue(Neighbor.Session session,ControlMessage message) {
                var p=current(session);
                return p==null?Connection.refused(Connection.ExtensionStatus.CLOSED):p.context.channel.trySendExtension(message);
            }
            public void diagnostic(NeighborDiscovery.Issue issue) {
                NeighborManager.this.diagnostic(Reason.valueOf("DISCOVERY_"+issue.name()),null,null,null);
            }
        });
        learner.start(deadlines);
        publish(); started.complete(null);
        for (var p : peers.values()) if (initiates(p)) dialPeer(p);
    }
    private Map<String, Peer> validateList(ControlMessage.NeighborList list) {
        if (list.neighbors().size() > 63 || list.neighbors().size() > plan.options.maxConnections())
            throw new IllegalArgumentException("Configured degree exceeds capacity");
        var result = new LinkedHashMap<String, Peer>(); var tcp = new HashSet<String>(); var udp = new HashSet<String>();
        tcp.add(key(plan.local.host(), plan.local.controlPort())); udp.add(key(plan.local.host(), plan.local.dataPort()));
        var bootstrap = plan.config.bootstrap().orElseThrow(); tcp.add(key(bootstrap.host(), bootstrap.port()));
        for (var e : list.neighbors()) {
            validateEndpoint(e, true);
            if (e.nodeId().equals(plan.local.nodeId()) || result.containsKey(e.nodeId())
                    || !tcp.add(key(e.host(), e.controlPort())) || !udp.add(key(e.host(), e.dataPort())))
                throw new IllegalArgumentException("Conflicting configured neighbor");
            result.put(e.nodeId(), new Peer(withInstance(e, ZERO)));
        }
        return result;
    }
    private void activate(Context c) {
        if (!current(c) || c.active || !c.written || c.peer == null || c.remoteInstance == null
                || c.direction == Direction.OUTBOUND && !c.acknowledged) return;
        if (!c.channel.isOpen() || expired(c.openedAt, budget(c))) { finish(c, Reason.TIMEOUT, null); return; }
        var p = c.peer;
        if (p.context != c || p.session != null) { finish(c, Reason.CONTEXT, null); return; }
        c.active = true; cancel(c.deadline); c.deadline = null;
        if (c.attempt != null) { c.attempt.live = false; p.attempt = null; }
        p.session = new Neighbor.Session(p.configured.nodeId(), c.remoteInstance, c.channel.id(), c.endpoint);
        p.state = Neighbor.State.ACTIVE; p.base = 0; p.delay = 0;
        p.capability=CapabilityState.ACTIVE_DISCOVERY;p.currentReason=Reason.STALE;p.lastConfirmed=null;
        learner.up(p.session,c.request);
        publish(); emit(new NeighborUp(p.session)); drain(c);
    }
    private void retain(Context c, ControlMessage m, long at) {
        int b = Math.min(4, plan.options.queueCapacity());
        int r = Math.min(plan.config.runtime().overlay().eventsCapacity(), plan.options.maxConnections() * b);
        if (c.frames.size() >= b || retained >= r) { finish(c, Reason.RESOURCE_LIMIT, m); return; }
        c.frames.addLast(new Frame(m, at)); retained++;
    }
    private void drain(Context c) {
        while (current(c) && !stopping.get() && !c.frames.isEmpty() && c.direction != Direction.UNCLASSIFIED) {
            // Until ACTIVE, application frames must stay behind the local flush barrier.
            var next = c.frames.peekFirst();
            if (!c.active && legitimateWindow(c) && application(next.message.type())
                    && !(next.message.type() == MessageType.ERROR && next.message.requestId().equals(c.request)
                    && ((ControlMessage.ErrorPayload) next.message.payload()).relatedType() == (c.direction == Direction.OUTBOUND ? 5 : 6))) return;
            c.frames.removeFirst(); retained--; handle(c, next.message, next.observedAt);
        }
    }
    private void removeContext(Context c) {
        if (!contexts.remove(c.channel.id(), c)) return;
        cancel(c.deadline); c.deadline = null;
        retained -= c.frames.size(); c.frames.clear();
    }
    private void finish(Context c, Reason reason, ControlMessage remote) {
        if (!current(c)) return;
        diagnostic(reason, c.attempt, c, remote);
        removeContext(c); c.channel.close();
        var p = c.peer;
        if (p != null && p.context == c) {
            var old = p.session;
            if(old!=null&&learner!=null)learner.down(old);
            if(reason!=Reason.REQUIRED_DISCOVERY_MISSING&&reason!=Reason.UNSUPPORTED_VERSION&&reason!=Reason.INVALID_HANDSHAKE) {
                p.capability=CapabilityState.TEMPORARILY_UNAVAILABLE;p.currentReason=reason;
            }
            p.context = null; p.session = null; p.state = stopping.get() ? Neighbor.State.CLOSED : Neighbor.State.DISCONNECTED;
            if (c.attempt != null) retire(c.attempt);
            publish(); if (old != null) emit(new NeighborDown(old, reason));
            if (!stopping.get()) retry(p);
        } else if (c.bootstrap && c.attempt != null && live(c.attempt)) {
            retire(c.attempt); retryDiscovery();
        }
    }
    private void retire(Attempt a) {
        a.live = false; phaseSlotCancel(a);
        if (a.job != null) { io.resolver().retire(a.job); a.job = null; }
        if (a.peer == null) { if (discovery == a) discovery = null; }
        else if (a.peer.attempt == a) { a.peer.attempt = null; a.peer.state = Neighbor.State.DISCONNECTED; }
    }
    private void attemptFailed(Attempt a, Reason reason, ControlMessage remote) {
        if (!live(a)) return;
        if (a.context != null && current(a.context)) { finish(a.context, reason, remote); return; }
        if(a.peer!=null){a.peer.capability=CapabilityState.TEMPORARILY_UNAVAILABLE;a.peer.currentReason=reason;}
        diagnostic(reason, a, null, remote); retire(a);
        if (a.peer == null) retryDiscovery(); else { publish(); retry(a.peer); }
    }
    private long nextDelay(long base) {
        var o = plan.config.runtime().overlay();
        long spread = base * o.retryJitterPercent() / 100;
        long noise = jitter.choose(-spread, spread);
        if (noise < -spread || noise > spread) throw new IllegalStateException("Jitter outside requested range");
        return Math.max(1, Math.min(o.retryMax().toMillis(), base + noise));
    }
    private long nextBase(long previous) {
        long max = plan.config.runtime().overlay().retryMax().toMillis();
        return previous == 0 ? plan.config.runtime().overlay().retryInitial().toMillis()
                : previous > max / 2 ? max : Math.min(max, previous * 2);
    }
    private void retry(Peer p) {
        if (phase != Phase.READY || stopping.get() || draining.get() || !initiates(p) || p.session != null || p.attempt != null) return;
        cancel(p.slot); p.base = nextBase(p.base); p.delay = nextDelay(p.base);
        p.slot = schedule(TimeUnit.MILLISECONDS.toNanos(p.delay), () -> { p.slot = null; dialPeer(p); }); publish();
    }
    private void retryDiscovery() {
        if (phase != Phase.DISCOVERING || stopping.get() || draining.get()) return;
        discoveryBase = nextBase(discoveryBase); cancel(initialSlot);
        initialSlot = schedule(TimeUnit.MILLISECONDS.toNanos(nextDelay(discoveryBase)), () -> {
            initialSlot = null;
            if (nativeDials.values().stream().anyMatch(a -> a.peer == null)) retryDiscovery(); else discover();
        });
    }
    private void failDiscovery(Throwable error, Reason reason, Context c, ControlMessage remote) {
        if (phase != Phase.DISCOVERING) return;
        phase = Phase.FAILED; diagnostic(reason, c == null ? discovery : c.attempt, c, remote);
        failure.compareAndSet(null, error); started.completeExceptionally(error); requestClose();
    }
    public synchronized void requestClose() {
        if (!stopping.compareAndSet(false, true)) return;
        closeStarted = draining.get() ? notificationStarted : now();
        var services = io; if (services != null) services.requestClose();
        var timers = deadlines; if (timers != null) timers.requestClose();
        if (owner == null) { started.completeExceptionally(new IllegalStateException("Manager closed")); cleaned.complete(null); publishShutdown(LeaveOutcome.ABORTED); return; }
        if (owner.isOwnerThread()) cleanup();
        else if (!owner.offer(this::cleanup)) owner.fail(new RejectedExecutionException("Stop could not enter owner"));
    }
    private void ownerStopped() {
        stopping.set(true);
        if (closeStarted == 0) closeStarted = draining.get() ? notificationStarted : now();
        if (io != null) io.requestClose(); if (deadlines != null) deadlines.requestClose(); cleanup();
    }
    private void cleanup() {
        if (cleanupDone) return; cleanupDone = true;
        if (phase != Phase.FAILED) phase = Phase.CLOSED;
        if (phase != Phase.FAILED && failure.get() != null) diagnostic(failure.get() instanceof RejectedExecutionException
                ? Reason.RESOURCE_LIMIT : Reason.CONSUMER_FAILURE, null, null, null);
        if(learner!=null)learner.close();
        cancel(initialSlot); initialSlot = null;
        if (discovery != null) retire(discovery);
        for (var c : List.copyOf(contexts.values())) finish(c, Reason.SHUTDOWN, null);
        for (var p : peers.values()) {
            cancel(p.slot); p.slot = null; if (p.attempt != null) retire(p.attempt);
            p.state = Neighbor.State.CLOSED; p.session = null;
        }
        publish();
        started.completeExceptionally(new IllegalStateException("Manager closed"));
        for (var command : commands) command.completeExceptionally(new IllegalStateException("Manager closed"));
        if (registration != null) registration.remove();
        cleaned.complete(null); publishShutdown(LeaveOutcome.ABORTED); if (!borrowed) owner.requestClose();
    }
    public boolean awaitTermination(Duration timeout) {
        Objects.requireNonNull(timeout);
        if (timeout.isNegative()) throw new IllegalArgumentException("Negative timeout");
        if (terminationObserved.get()) return true;
        if (owner != null && owner.isOwnerThread()) return cleaned.isDone();
        long began = now(), nanos = timeout.toNanos();
        try {
            if (!cleaned.isDone()) cleaned.get(Math.max(0, remainingNanos(nanos, began)), TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
        catch (ExecutionException | TimeoutException e) { return false; }
        Duration left = remainingMillis(nanos, began);
        if (io != null && (left.isZero() || !io.network().awaitTermination(left))) return false;
        left = remainingMillis(nanos, began);
        if (deadlines != null && (left.isZero() || !deadlines.awaitTermination(left))) return false;
        left = remainingMillis(nanos, began);
        if (io != null && (left.isZero() || !io.awaitTermination(left))) return false;
        left = remainingMillis(nanos, began);
        boolean done = borrowed || owner == null || !left.isZero() && owner.awaitTermination(left);
        if (done) terminationObserved.set(true);
        return done;
    }
    private long remainingNanos(long budget, long began) { return Math.max(0, budget - (now() - began)); }
    private Duration remainingMillis(long budget, long began) { return Duration.ofMillis(TimeUnit.NANOSECONDS.toMillis(remainingNanos(budget, began))); }
    @Override public void close() {
        requestClose(); if (owner != null && owner.isOwnerThread()) return;
        long total = draining.get() ? notificationTotalBudget : plan.config.runtime().control().shutdownTimeout().toNanos();
        long left = remainingNanos(total, closeStarted);
        if (!awaitTermination(Duration.ofNanos(left))) throw new IllegalStateException("Neighbor shutdown deadline: owner, TCP, timers or I/O worker still pending", failure.get());
    }
    private static Plan validate(NodeConfiguration config, UUID instance) {
        Objects.requireNonNull(config); Objects.requireNonNull(instance);
        if (ZERO.equals(instance) || config.role() == NodeRole.BOOTSTRAP || config.bootstrap().isEmpty()
                || !validId(config.nodeId()) || config.topology().isPresent() || config.topologyFile().isPresent())
            throw new IllegalArgumentException("Overlay profile and nonzero instance required");
        normalizeHost(config.bindHost(), true);
        var local = new ControlMessage.Endpoint(config.nodeId(), instance, normalizeHost(config.advertisedHost(), false),
                config.controlPort(), config.dataPort(), config.role()); validateEndpoint(local, false);
        var contact = config.bootstrap().orElseThrow(); normalizeHost(contact.host(), false); port(contact.port());
        if (config.role() != NodeRole.SERVER && (!config.streams().isEmpty() || config.streamsFile().isPresent() || config.mediaBase().isPresent()))
            throw new IllegalArgumentException("Media configuration requires SERVER");
        var r = config.runtime(); var c = r.control();
        return new Plan(config, local, new Transport.Options(c.queueCapacity(), c.maxConnections(), c.connectTimeout(), c.shutdownTimeout()));
    }
    private static boolean validId(String id) { return id != null && id.matches("[A-Za-z0-9_-]{1,32}"); }
    private static void port(int value) { if (value < 1 || value > 65535) throw new IllegalArgumentException("Invalid port"); }
    static void validateEndpoint(ControlMessage.Endpoint e, boolean configured) {
        Objects.requireNonNull(e);
        if (!validId(e.nodeId()) || e.instanceId() == null || configured != ZERO.equals(e.instanceId())
                || e.role() == null || e.role() == NodeRole.BOOTSTRAP) throw new IllegalArgumentException("Invalid endpoint identity");
        normalizeHost(e.host(), false); port(e.controlPort()); port(e.dataPort());
    }
    private static String key(String host, int port) { return normalizeHost(host, false) + ":" + port; }
    private static boolean sameEndpoint(ControlMessage.Endpoint a, ControlMessage.Endpoint b) {
        return a.nodeId().equals(b.nodeId()) && a.role() == b.role() && a.controlPort() == b.controlPort()
                && a.dataPort() == b.dataPort() && normalizeHost(a.host(), false).equals(normalizeHost(b.host(), false));
    }
    private static ControlMessage.Endpoint withInstance(ControlMessage.Endpoint e, UUID instance) {
        return new ControlMessage.Endpoint(e.nodeId(), instance, normalizeHost(e.host(), false), e.controlPort(), e.dataPort(), e.role());
    }
    private static String normalizeHost(String host, boolean wildcardAllowed) {
        try {
            if (host == null || host.isEmpty() || host.length() > 253 || !host.chars().allMatch(c -> c > 32 && c < 127))
                throw new IllegalArgumentException("Invalid host");
            new URI("ott", null, host, -1, null, null, null).parseServerAuthority();
            String result = host.toLowerCase(Locale.ROOT);
            if (result.startsWith("[") && result.endsWith("]")) result = result.substring(1, result.length() - 1);
            if (result.endsWith(".")) result = result.substring(0, result.length() - 1);
            if (result.matches("[0-9.]+") && result.contains(".")) {
                var parts = result.split("\\.", -1);
                if (parts.length != 4) throw new IllegalArgumentException("Invalid IPv4");
                for (int i = 0; i < 4; i++) { int n = Integer.parseInt(parts[i]); if (n > 255) throw new IllegalArgumentException("Invalid IPv4"); parts[i] = Integer.toString(n); }
                result = String.join(".", parts);
            }
            String address = result.split("%", 2)[0];
            if (!wildcardAllowed && (address.equals("0.0.0.0") || address.contains(":") && address.replace(":", "").matches("0*")))
                throw new IllegalArgumentException("Wildcard advertised host");
            return result;
        } catch (URISyntaxException e) { throw new IllegalArgumentException("Invalid host", e); }
    }

    private static boolean joinThread(Thread thread, Duration timeout) {
        if (thread == Thread.currentThread()) return false;
        try { thread.join(timeout); return !thread.isAlive(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
    }
    /** One bounded queue; stop/failure is out of band, so cleanup never needs another slot. */
    private static final class Owner implements SerialOwner {
        private final int capacity;
        private final ArrayBlockingQueue<Runnable> queue;
        private final AtomicBoolean stopped = new AtomicBoolean();
        private final AtomicReference<Throwable> failure = new AtomicReference<>();
        private final Map<Object, Runnable> hooks = new LinkedHashMap<>();
        private final Thread thread;
        Owner(int capacity) {
            if (capacity < 1 || capacity > 4096) throw new IllegalArgumentException("Owner capacity 1..4096");
            this.capacity = capacity; queue = new ArrayBlockingQueue<>(capacity);
            thread = Thread.ofVirtual().name("ott-neighbor-events").unstarted(this::loop); thread.start();
        }
        private void loop() {
            try {
                while (!stopped.get()) {
                    try { var action = queue.take(); if (!stopped.get()) action.run(); }
                    catch (InterruptedException e) { if (!stopped.get()) fail(e); }
                    catch (RuntimeException e) { fail(e); }
                }
            } finally {
                queue.clear(); List<Runnable> cleanups;
                synchronized (hooks) { cleanups = List.copyOf(hooks.values()); hooks.clear(); }
                for (var hook : cleanups) try { hook.run(); } catch (RuntimeException e) { failure.compareAndSet(null, e); }
            }
        }
        public int capacity() { return capacity; }
        public boolean isOwnerThread() { return Thread.currentThread() == thread; }
        public boolean offer(Runnable action) {
            Objects.requireNonNull(action);
            if (stopped.get()) return false;
            if (queue.offer(action)) return true;
            fail(new RejectedExecutionException("Owner queue full")); return false;
        }
        public Registration onStop(Runnable cleanup) {
            Objects.requireNonNull(cleanup); var token = new Object();
            synchronized (hooks) {
                if (stopped.get() || hooks.size() >= 16) throw new RejectedExecutionException("Owner stopped or cleanup hook capacity");
                hooks.put(token, cleanup);
            }
            return () -> { synchronized (hooks) { hooks.remove(token); } };
        }
        public void fail(Throwable cause) { failure.compareAndSet(null, Objects.requireNonNull(cause)); requestClose(); }
        public void requestClose() { stopped.set(true); if (!isOwnerThread()) thread.interrupt(); }
        public boolean awaitTermination(Duration timeout) { return joinThread(thread, timeout); }
        public void close() {
            requestClose(); if (!isOwnerThread() && !awaitTermination(Duration.ofSeconds(3))) throw new IllegalStateException("Serial owner did not terminate");
        }
    }
    private static final class RealPorts implements Ports {
        public SerialOwner owner(int capacity) { return createSerialOwner(capacity); }
        public IoServices io(int maxJobs) { return new RealIo(maxJobs); }
        public Deadlines deadlines(int maxTickets) { return new RealDeadlines(maxTickets); }
    }
    private record TcpChannel(Connection connection) implements Channel {
        public UUID id() { return connection.id(); }
        public boolean isOpen() { return connection.isOpen(); }
        public Connection.SendResult send(ControlMessage message) { return connection.send(message); }
        public CompletableFuture<Void> sendAndFlush(ControlMessage message) { return connection.sendAndFlush(message); }
        public Connection.ExtensionEnqueue trySendExtension(ControlMessage message){return connection.trySendExtension(message);}
        public void close() { connection.close(); }
    }
    /** Physical ticket accounting includes callbacks already running when cancelled. */
    static final class RealDeadlines implements Deadlines {
        private final int maximum;
        private int limit, physical;
        private boolean stopped;
        private final Set<TimerJob> jobs = new HashSet<>();
        private final ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1,
                Thread.ofVirtual().name("ott-neighbor-deadlines").factory());
        RealDeadlines(int max) {
            maximum = max; limit = max;
            worker.setRemoveOnCancelPolicy(true); worker.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        }
        public synchronized void limit(int value) {
            if (value < 1 || value > maximum) throw new IllegalArgumentException("Invalid timer limit"); limit = value;
        }
        private final class TimerJob implements Runnable, Ticket {
            final Runnable task; ScheduledFuture<?> future; boolean running, done,periodic;
            TimerJob(Runnable task) { this.task = task; }
            public void run() {
                synchronized (RealDeadlines.this) { if (done) return; running = true; }
                try { task.run(); } finally { synchronized (RealDeadlines.this) {
                    running=false;if(!periodic||future.isCancelled()||stopped)release();
                } }
            }
            private void release() { if (!done) { done = true; physical--; jobs.remove(this); } }
            public void cancel() {
                synchronized (RealDeadlines.this) {
                    if (done) return;
                    future.cancel(false);
                    if (!running) release();
                }
            }
        }
        public synchronized Ticket schedule(Runnable task, long nanos) {
            if (stopped || physical >= limit) throw new RejectedExecutionException("Physical timer capacity");
            var job = new TimerJob(Objects.requireNonNull(task)); physical++; jobs.add(job);
            try { job.future = worker.schedule(job, Math.max(1, nanos), TimeUnit.NANOSECONDS); }
            catch (RuntimeException e) { job.release(); throw e; }
            return job;
        }
        public synchronized Ticket periodic(Runnable task,long nanos) {
            if(stopped||physical>=limit)throw new RejectedExecutionException("Physical timer capacity");
            var job=new TimerJob(Objects.requireNonNull(task));job.periodic=true;physical++;jobs.add(job);
            try{job.future=worker.scheduleWithFixedDelay(job,Math.max(1,nanos),Math.max(1,nanos),TimeUnit.NANOSECONDS);}
            catch(RuntimeException error){job.release();throw error;}return job;
        }
        public synchronized void requestClose() {
            stopped = true; for (var job : List.copyOf(jobs)) job.cancel(); worker.shutdownNow();
        }
        public boolean awaitTermination(Duration timeout) {
            try { return worker.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
        }
    }
    /** One bounded worker for configured DNS and bind, independent of the serial domain owner. */
    interface AddressLookup { InetAddress resolve(String host) throws Exception; }
    static final class RealIo implements IoServices, Resolver, Network {
        private final int maximum;
        private final AddressLookup lookup;
        private final ThreadPoolExecutor worker;
        private final Map<String, ResolveJob> hosts = new HashMap<>();
        private final Map<UUID, ResolveJob> jobs = new HashMap<>();
        private final AtomicReference<Transport> transport = new AtomicReference<>();
        private boolean stopped; private int physical;
        RealIo(int maximum) {
            this(maximum, InetAddress::getByName);
        }
        RealIo(int maximum, AddressLookup lookup) {
            this.maximum = maximum;
            this.lookup = Objects.requireNonNull(lookup);
            worker = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(maximum),
                    Thread.ofVirtual().name("ott-neighbor-io").factory(), new ThreadPoolExecutor.AbortPolicy());
        }
        public Resolver resolver() { return this; }
        public Network network() { return this; }
        private final class ResolveJob implements Runnable {
            final UUID id; final String host;
            final CompletableFuture<InetAddress> result = new CompletableFuture<>();
            Thread runner; boolean running, retired;
            ResolveJob(UUID id, String host) { this.id = id; this.host = host; }
            public void run() {
                synchronized (RealIo.this) {
                    if (retired) return; running = true; runner = Thread.currentThread();
                }
                InetAddress address = null; Throwable error = null;
                try { address = lookup.resolve(host); } catch (Exception e) { error = e; }
                // Complete only after physical resolution returns, never on logical retirement.
                // Continuations also remain inside this job's physical reservation.
                try { if (error == null) result.complete(address); else result.completeExceptionally(error); }
                finally { synchronized (RealIo.this) { physical--; hosts.remove(host, this); jobs.remove(id, this); runner = null; } }
            }
        }
        public CompletableFuture<InetAddress> resolve(UUID id, String host) {
            host = normalizeHost(host, true);
            ResolveJob job;
            synchronized (this) {
                if (stopped || physical >= maximum || hosts.containsKey(host))
                    return CompletableFuture.failedFuture(new RejectedExecutionException("DNS job busy or capacity"));
                job = new ResolveJob(id, host); hosts.put(host, job); jobs.put(id, job); physical++;
                try { worker.execute(job); }
                catch (RuntimeException e) { physical--; hosts.remove(host); jobs.remove(id); return CompletableFuture.failedFuture(e); }
            }
            return job.result;
        }
        public void retire(UUID id) {
            ResolveJob job; boolean removed = false;
            synchronized (this) {
                job = jobs.get(id); if (job == null) return;
                if (!job.running && worker.remove(job)) {
                    job.retired = true; jobs.remove(id); hosts.remove(job.host, job); physical--; removed = true;
                } else if (job.runner != null) job.runner.interrupt();
            }
            if (removed) job.result.completeExceptionally(new CancellationException("DNS waiter retired"));
        }
        public CompletableFuture<InetSocketAddress> open(InetSocketAddress bind, Transport.Options options, Sink sink) {
            var result = new CompletableFuture<InetSocketAddress>();
            synchronized (this) {
                if (stopped || physical >= maximum) return CompletableFuture.failedFuture(new RejectedExecutionException("Bind job capacity"));
                physical++;
                try { worker.execute(() -> {
                    Transport opened = null; Throwable error = null;
                    try {
                        opened = new Transport(bind, new Transport.Listener() {
                            public void connectionOpen(Connection c) { sink.opened(new TcpChannel(c), System.nanoTime()); }
                            public void message(Connection c, ControlMessage m) { sink.message(new TcpChannel(c), m, System.nanoTime()); }
                            public void rejectedExtraContent(Connection c,MessageCodec.RejectedExtraContent rejection){sink.rejected(new TcpChannel(c),rejection,System.nanoTime());}
                            public void protocolError(Connection c,ProtocolException error){sink.protocolError(new TcpChannel(c),error,System.nanoTime());}
                            public void connectionClosed(Connection c, Connection.CloseReason why) { sink.closed(new TcpChannel(c), why, System.nanoTime()); }
                        }, options);
                        transport.set(opened);
                        synchronized (RealIo.this) { if (stopped) opened.requestClose(); }
                    } catch (Exception e) { error = e; }
                    try { if (error == null) result.complete(opened.localAddress()); else result.completeExceptionally(error); }
                    finally { synchronized (RealIo.this) { physical--; } }
                }); } catch (RuntimeException e) {
                    physical--;
                    // The failed future is published only after leaving this lock.
                    return CompletableFuture.failedFuture(e);
                }
            }
            return result;
        }
        public CompletableFuture<Channel> connect(InetSocketAddress remote) {
            var tcp = transport.get();
            if (tcp == null) return CompletableFuture.failedFuture(new IllegalStateException("Network not open"));
            return tcp.connect(remote).thenApply(TcpChannel::new);
        }
        public int reservations() { var tcp = transport.get(); return tcp == null ? 0 : tcp.connectionCount(); }
        public void requestClose() {
            List<UUID> pending;
            synchronized (this) { stopped = true; pending = List.copyOf(jobs.keySet()); }
            var tcp = transport.get(); if (tcp != null) tcp.requestClose();
            for (var id : pending) retire(id);
            // Queued bind must run its rollback; do not silently drop the job/future.
            worker.shutdown();
        }
        public boolean awaitTermination(Duration timeout) {
            // IoServices and Network share this adapter: verify both within a single supplied budget.
            long began = System.nanoTime();
            try { if (!worker.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS)) return false; }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
            var tcp = transport.get(); if (tcp == null) return true;
            long remaining = TimeUnit.NANOSECONDS.toMillis(timeout.toNanos() - (System.nanoTime() - began));
            return remaining > 0 && tcp.awaitTermination(Duration.ofMillis(remaining));
        }
    }
}
