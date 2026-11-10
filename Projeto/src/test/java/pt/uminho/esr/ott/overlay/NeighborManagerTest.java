package pt.uminho.esr.ott.overlay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.protocol.*;
import java.nio.file.Path;
import java.util.*;
import java.net.*;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import pt.uminho.esr.ott.transport.*;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(20)
class NeighborManagerTest {
    static final UUID ZERO = new UUID(0, 0);
    static ControlMessage.Endpoint endpoint(String id, UUID instance, int port) {
        return new ControlMessage.Endpoint(id, instance, "127.0.0.1", port, port + 100, NodeRole.RELAY);
    }
    static NodeConfiguration config(String id, int port) {
        return new NodeConfiguration(Path.of("fixture.properties"), id, NodeRole.RELAY,
                "127.0.0.1", "127.0.0.1", port, port + 100,
                Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1", 19000)),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), List.of());
    }
    @Test void d01RejectsZeroInstanceBeforeStart() {
        assertThrows(IllegalArgumentException.class, () -> new NeighborManager(config("A", 19001), ZERO, e -> {}));
    }
    @Test void d03SnapshotRequiresConfiguredZeroAndSessionOnlyWhenActive() {
        var e = endpoint("B", ZERO, 19002);
        assertThrows(IllegalArgumentException.class, () -> new Neighbor("A", e, Neighbor.State.DISCONNECTED, Optional.empty(), 0));
        assertThrows(IllegalArgumentException.class, () -> new Neighbor("B", e, Neighbor.State.ACTIVE, Optional.empty(), 0));
        assertThrows(IllegalArgumentException.class, () -> new Neighbor.Session("B", ZERO, UUID.randomUUID(), e));
    }
    @Test void d19CloseBeforeStartIsIdempotentAndStartCannotRevive() {
        var m = new NeighborManager(config("A", 19001), UUID.randomUUID(), e -> {});
        m.close(); m.close();
        assertTrue(m.snapshot().isEmpty());
        assertTrue(m.start().toCompletableFuture().isCompletedExceptionally());
    }

    static final class ManualOwner implements NeighborManager.SerialOwner {
        final ArrayDeque<Runnable> events = new ArrayDeque<>();
        final List<Runnable> hooks = new ArrayList<>();
        boolean running, stopped, reject; int capacity = 4096;
        public int capacity() { return capacity; }
        public boolean isOwnerThread() { return running; }
        public boolean offer(Runnable event) {
            if (stopped || reject || events.size() >= capacity) return false;
            events.add(event); return true;
        }
        public NeighborManager.Registration onStop(Runnable r) { hooks.add(r); return () -> hooks.remove(r); }
        public void fail(Throwable t) { requestClose(); }
        public void requestClose() {
            stopped = true; running = true;
            try { for (var hook : List.copyOf(hooks)) hook.run(); } finally { running = false; events.clear(); }
        }
        public boolean awaitTermination(Duration d) { return stopped && !running; }
        public void close() { requestClose(); }
        void pump() {
            int count = 0;
            while (!events.isEmpty()) {
                assertTrue(++count < 1000, "No unbounded immediate retry");
                running = true; try { events.remove().run(); } finally { running = false; }
            }
        }
    }
    static final class FakeChannel implements NeighborManager.Channel {
        final UUID id = UUID.randomUUID(); boolean open = true, flush = true;
        final List<ControlMessage> writes = new ArrayList<>();
        final List<CompletableFuture<Void>> confirmations = new ArrayList<>();
        public UUID id() { return id; }
        public boolean isOpen() { return open; }
        public Connection.SendResult send(ControlMessage m) { if (!open) return Connection.SendResult.CLOSED; writes.add(m); return Connection.SendResult.ACCEPTED; }
        public CompletableFuture<Void> sendAndFlush(ControlMessage m) {
            writes.add(m); var f = new CompletableFuture<Void>(); confirmations.add(f);
            if (flush) f.complete(null); return f;
        }
        public void close() { open = false; }
        ControlMessage last() { return writes.getLast(); }
        void written() { confirmations.getLast().complete(null); }
    }
    static final class FakeTimers implements NeighborManager.Deadlines {
        record Task(long began, long delay, Runnable action) {}
        final AtomicLong clock; final List<Task> tasks = new ArrayList<>();
        int limit, peak; boolean stopped;
        FakeTimers(AtomicLong clock) { this.clock = clock; }
        public void limit(int n) { limit = n; }
        public NeighborManager.Ticket schedule(Runnable r, long delay) {
            if (stopped || tasks.size() >= limit) throw new RejectedExecutionException("Physical timer capacity");
            var t = new Task(clock.get(), delay, r); tasks.add(t); peak = Math.max(peak, tasks.size());
            return () -> tasks.remove(t);
        }
        void advance(long nanos) {
            clock.addAndGet(nanos);
            for (var t : List.copyOf(tasks)) if (clock.get() - t.began >= t.delay) { tasks.remove(t); t.action.run(); }
        }
        public void requestClose() { stopped = true; tasks.clear(); }
        public boolean awaitTermination(Duration d) { return stopped; }
    }
    static final class FakeIo implements NeighborManager.IoServices, NeighborManager.Resolver, NeighborManager.Network {
        record Dial(InetSocketAddress remote, CompletableFuture<NeighborManager.Channel> future) {}
        final List<Dial> dials = new ArrayList<>();
        final List<String> resolved = new ArrayList<>();
        final Map<UUID, CompletableFuture<InetAddress>> jobs = new LinkedHashMap<>();
        final List<UUID> retired = new ArrayList<>();
        NeighborManager.Sink sink; Transport.Options options;
        boolean stopped, holdDns, holdBind, holdTermination; CompletableFuture<InetSocketAddress> binding;
        AtomicLong awaitClock; long consumeMillis; final List<Duration> awaitBudgets = new ArrayList<>();
        public NeighborManager.Resolver resolver() { return this; }
        public NeighborManager.Network network() { return this; }
        public CompletableFuture<InetAddress> resolve(UUID id, String host) {
            resolved.add(host); var f = new CompletableFuture<InetAddress>(); jobs.put(id, f);
            if (!holdDns) f.complete(InetAddress.getLoopbackAddress()); return f;
        }
        public void retire(UUID id) { retired.add(id); }
        public CompletableFuture<InetSocketAddress> open(InetSocketAddress address, Transport.Options options, NeighborManager.Sink sink) {
            this.options = options; this.sink = sink; binding = new CompletableFuture<>();
            if (!holdBind) binding.complete(address); return binding;
        }
        public CompletableFuture<NeighborManager.Channel> connect(InetSocketAddress address) {
            var f = new CompletableFuture<NeighborManager.Channel>(); dials.add(new Dial(address, f)); return f;
        }
        public int reservations() { return dials.size(); }
        public void requestClose() { stopped = true; }
        public boolean awaitTermination(Duration d) {
            awaitBudgets.add(d);
            if (awaitClock != null) awaitClock.addAndGet(TimeUnit.MILLISECONDS.toNanos(Math.min(consumeMillis, d.toMillis())));
            return stopped && !holdTermination && consumeMillis <= d.toMillis();
        }
    }
    static final class Fixture implements AutoCloseable {
        final AtomicLong clock = new AtomicLong(1000);
        final ManualOwner owner = new ManualOwner();
        final FakeIo io = new FakeIo(); final FakeTimers timers = new FakeTimers(clock);
        final List<NeighborManager.Event> events = new ArrayList<>();
        final UUID instance = UUID.randomUUID();
        long jitterMode; boolean observerFails;
        final NeighborManager manager; int factories;
        Fixture(String id) { this(config(id, 19001), false); }
        Fixture(NodeConfiguration config, boolean borrow) {
            owner.capacity = config.runtime().overlay().eventsCapacity();
            manager = new NeighborManager(config, instance, borrow ? owner : null, event -> { events.add(event); if (observerFails) throw new IllegalStateException("Controlled consumer failure"); }, clock::get,
                (min, max) -> jitterMode < 0 ? min : jitterMode > 0 ? max : 0,
                new NeighborManager.Ports() {
                    public NeighborManager.SerialOwner owner(int n) { factories++; assertEquals(owner.capacity, n); return owner; }
                    public NeighborManager.IoServices io(int n) { factories++; assertEquals(65, n); return io; }
                    public NeighborManager.Deadlines deadlines(int n) { factories++; return timers; }
                });
        }
        void pump() { owner.pump(); }
        void message(FakeChannel c, ControlMessage m) { io.sink.message(c, m, clock.get()); pump(); }
        FakeChannel connect(int index, boolean callbacksFirst) {
            var c = new FakeChannel();
            if (callbacksFirst) io.sink.opened(c, clock.get());
            io.dials.get(index).future.complete(c);
            if (!callbacksFirst) io.sink.opened(c, clock.get());
            pump(); return c;
        }
        FakeChannel discovery() {
            assertEquals(0, factories); manager.start(); pump(); return connect(0, true);
        }
        void ready(String peer) {
            var boot = discovery();
            message(boot, new ControlMessage(MessageType.REGISTER_OK, boot.last().requestId(), "BOOT", UUID.randomUUID(),
                    new ControlMessage.NeighborList(List.of(endpoint(peer, ZERO, 19002)))));
            assertTrue(manager.start().toCompletableFuture().isDone()); assertFalse(boot.open);
        }
        FakeChannel outbound(boolean flush) {
            var c = new FakeChannel(); c.flush = flush; io.sink.opened(c, clock.get()); io.dials.get(1).future.complete(c); pump(); return c;
        }
        void ok(FakeChannel c, UUID remote) {
            message(c, new ControlMessage(MessageType.HELLO_OK, c.last().requestId(), "B", remote, ControlMessage.Empty.INSTANCE));
        }
        FakeChannel inbound(String id, UUID remote, boolean flush) {
            var c = new FakeChannel(); c.flush = flush; io.sink.opened(c, clock.get()); pump();
            message(c, new ControlMessage(MessageType.HELLO, UUID.randomUUID(), id, remote,
                    new ControlMessage.EndpointPayload(endpoint(id, remote, 19002)))); return c;
        }
        long ups() { return events.stream().filter(e -> e instanceof NeighborManager.NeighborUp).count(); }
        long downs() { return events.stream().filter(e -> e instanceof NeighborManager.NeighborDown).count(); }
        List<NeighborManager.Received> received() { return events.stream().filter(e -> e instanceof NeighborManager.Received).map(e -> (NeighborManager.Received)e).toList(); }
        public void close() { manager.requestClose(); pump(); assertTrue(manager.awaitTermination(Duration.ofSeconds(1))); }
    }
    static ControlMessage error(FakeChannel c, String id, UUID instance, ErrorCode code, int related) {
        return new ControlMessage(MessageType.ERROR, c.last().requestId(), id, instance, new ControlMessage.ErrorPayload(code, related, "remote detail"));
    }
    @Test void d02DiscoveryRejectsPermanentErrorIntactWithoutErrorResponse() {
        try (var f = new Fixture("A")) {
            var boot = f.discovery(); var error = error(boot, "BOOT", UUID.randomUUID(), ErrorCode.UNAUTHORIZED_NEIGHBOR, 1);
            f.message(boot, error);
            var thrown = assertThrows(CompletionException.class, () -> f.manager.start().toCompletableFuture().join());
            var rejection = assertInstanceOf(NeighborManager.DiscoveryRejected.class, thrown.getCause());
            assertSame(error, rejection.remoteMessage()); assertEquals(boot.id, rejection.connectionId());
            assertEquals(1, boot.writes.size()); assertTrue(f.manager.snapshot().isEmpty()); assertEquals(0, f.ups());
        }
    }
    @Test void d02TransientDiscoveryErrorRetriesWithBackoff() {
        for (var code : List.of(ErrorCode.TIMEOUT, ErrorCode.RESOURCE_LIMIT)) try (var f = new Fixture("A")) {
            var boot = f.discovery(); f.message(boot, error(boot, "BOOT", UUID.randomUUID(), code, 1));
            assertFalse(f.manager.start().toCompletableFuture().isDone()); assertEquals(1, f.io.dials.size());
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(500)); f.pump();
            assertEquals(2, f.io.dials.size()); assertEquals(0, f.ups()); assertEquals(1, boot.writes.size());
        }
    }
    @Test void d02WrongRequestFailsLocallyAndUnrelatedInboundCannotFailDiscovery() {
        try (var f = new Fixture("A")) {
            var boot = f.discovery(); var other = new FakeChannel();
            f.io.sink.opened(other, f.clock.get()); f.pump();
            f.message(other, new ControlMessage(MessageType.ERROR, UUID.randomUUID(), "X", UUID.randomUUID(), new ControlMessage.ErrorPayload(ErrorCode.TIMEOUT, 1, "x")));
            assertFalse(f.manager.start().toCompletableFuture().isDone()); assertFalse(other.open);
            f.message(boot, new ControlMessage(MessageType.ERROR, UUID.randomUUID(), "BOOT", UUID.randomUUID(), new ControlMessage.ErrorPayload(ErrorCode.TIMEOUT, 1, "wrong request")));
            assertTrue(f.manager.start().toCompletableFuture().isCompletedExceptionally()); assertEquals(1, boot.writes.size());
        }
    }
    @Test void d03InvalidListHasNoPartialInstallOrPeerDial() {
        for (int kind = 0; kind < 4; kind++) try (var f = new Fixture("A")) {
            var boot = f.discovery();
            var e = endpoint(kind == 0 ? "A" : "B", kind == 1 ? UUID.randomUUID() : ZERO, 19002);
            var list = kind == 2 ? List.of(e, e) : kind == 3 ? List.of(e, endpoint("C", ZERO, 19002)) : List.of(e);
            f.message(boot, new ControlMessage(MessageType.REGISTER_OK, boot.last().requestId(), "BOOT", UUID.randomUUID(), new ControlMessage.NeighborList(list)));
            assertTrue(f.manager.start().toCompletableFuture().isCompletedExceptionally());
            assertTrue(f.manager.diagnostics().isEmpty()); assertEquals(1, f.io.dials.size());
        }
    }
    @Test void d05OnlySmallerIdDialsAndSnapshotIsImmutable() {
        try (var small = new Fixture("A"); var big = new Fixture("Z")) {
            small.ready("B"); big.ready("B");
            assertEquals(2, small.io.dials.size()); assertEquals(1, big.io.dials.size());
            assertThrows(UnsupportedOperationException.class, () -> small.manager.diagnostics().clear());
            assertEquals(ZERO, small.manager.diagnostics().get("B").configured().instanceId());
        }
    }
    @Test void d08OutboundNeedsBothFlushAndHelloOkThenDrainsApplicationAfterUp() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(false); var remote = UUID.randomUUID();
            f.ok(c, remote); assertEquals(0, f.ups()); assertTrue(f.manager.snapshot().isEmpty());
            var app = new ControlMessage(MessageType.ERROR, UUID.randomUUID(), "B", remote, new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE, 255, "route"));
            f.message(c, app); assertTrue(f.received().isEmpty()); c.written(); f.pump();
            assertEquals(1, f.ups()); assertEquals(1, f.received().size()); assertSame(app, f.received().getFirst().message());
            assertInstanceOf(NeighborManager.NeighborUp.class, f.events.get(f.events.indexOf(f.received().getFirst()) - 1));
            assertEquals(c.id, f.manager.snapshot().get("B").connectionId());
        }
    }
    @Test void d08InboundFlushAndErrorRejectionOfHelloOk() {
        try (var f = new Fixture("Z")) {
            f.ready("B"); var remote = UUID.randomUUID(); var c = f.inbound("B", remote, false);
            assertEquals(MessageType.HELLO_OK, c.last().type()); assertEquals(0, f.ups());
            f.message(c, error(c, "B", remote, ErrorCode.DUPLICATE_ID, 6));
            c.written(); f.pump(); assertEquals(0, f.ups()); assertFalse(c.open); assertEquals(1, c.writes.size());
        }
    }
    @Test void d07ErrorRejectsHelloBeforeFlushAndOldFlushCannotAuthorize() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(false);
            f.message(c, error(c, "B", UUID.randomUUID(), ErrorCode.DUPLICATE_ID, 5));
            c.written(); f.pump(); assertEquals(0, f.ups()); assertEquals(1, c.writes.size()); assertFalse(c.open);
            assertEquals(Neighbor.State.DISCONNECTED, f.manager.diagnostics().get("B").state());
        }
    }
    @Test void d07UnexpectedErrorBeforeLegitimateWindowClosesLocally() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(true);
            f.message(c, error(c, "B", UUID.randomUUID(), ErrorCode.NO_ROUTE, 11));
            assertFalse(c.open); assertEquals(0, f.ups()); assertEquals(1, c.writes.size());
        }
    }
    @Test void d11ActiveErrorsPreserveEveryCodeAndRawRelatedType() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(true); var remote = UUID.randomUUID(); f.ok(c, remote);
            for (var code : ErrorCode.values()) for (int related : List.of(0, 3, 17, 255)) {
                var m = new ControlMessage(MessageType.ERROR, UUID.randomUUID(), "B", remote, new ControlMessage.ErrorPayload(code, related, "original"));
                f.message(c, m); assertSame(m, f.received().getLast().message()); assertEquals(c.id, f.received().getLast().session().connectionId());
            }
            assertTrue(c.open); assertEquals(0, f.downs()); assertEquals(1, c.writes.size());
            var session = f.manager.snapshot().get("B");
            var invalid = new ControlMessage(MessageType.ERROR, UUID.randomUUID(), "A", f.instance, new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE, 17, "never error to error"));
            var result = f.manager.send(session, invalid); f.pump(); assertEquals(Connection.SendResult.INVALID_MESSAGE, result.toCompletableFuture().join());
        }
    }
    @Test void d10OldCallbacksCannotDeleteReconnectedInstance() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var old = f.outbound(true); f.ok(old, UUID.randomUUID());
            var stale = f.manager.snapshot().get("B"); var lost = f.manager.disconnect(stale); f.pump(); assertTrue(lost.toCompletableFuture().join());
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(500)); f.pump(); var c = f.connect(2, false); var remote = UUID.randomUUID(); f.ok(c, remote);
            var fresh = f.manager.snapshot().get("B"); assertEquals(remote, fresh.instanceId());
            f.io.sink.closed(old, Connection.CloseReason.PEER_EOF, f.clock.get());
            f.message(old, error(old, "B", stale.instanceId(), ErrorCode.TIMEOUT, 5));
            var no = f.manager.disconnect(stale); f.pump(); assertFalse(no.toCompletableFuture().join());
            assertEquals(fresh, f.manager.snapshot().get("B")); assertEquals(2, f.ups()); assertEquals(1, f.downs());
            assertEquals(3, f.io.dials.size()); // Exactly one bootstrap query.
        }
    }
    @Test void d12DeadlineIncludesQueueAndWrapAndWinsOverLateOk() {
        for (long begin : List.of(1000L, Long.MAX_VALUE - TimeUnit.MILLISECONDS.toNanos(100))) try (var f = new Fixture("A")) {
            f.clock.set(begin); f.ready("B"); var c = f.outbound(false);
            f.clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(3000));
            f.ok(c, UUID.randomUUID()); c.written(); f.pump(); assertEquals(0, f.ups()); assertFalse(c.open);
        }
    }
    @Test void d14ForgedHelloBeforeDialResultCannotBecomeInbound() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = new FakeChannel(); f.io.sink.opened(c, f.clock.get());
            f.message(c, new ControlMessage(MessageType.HELLO, UUID.randomUUID(), "B", UUID.randomUUID(), new ControlMessage.EndpointPayload(endpoint("B", UUID.randomUUID(), 19002))));
            assertTrue(c.writes.isEmpty()); assertEquals(0, f.ups());
            f.io.dials.get(1).future.complete(c); f.pump(); assertFalse(c.open); assertEquals(0, f.ups());
        }
    }
    @Test void d15RetiredNativeDialClosesLateSuccessAndDoesNotDuplicate() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var nativeFuture = f.io.dials.get(1).future;
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(2000)); f.pump();
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(500)); f.pump(); assertEquals(2, f.io.dials.size());
            assertFalse(nativeFuture.isCancelled()); var late = new FakeChannel(); nativeFuture.complete(late); f.pump();
            assertFalse(late.open); assertEquals(0, f.ups());
        }
    }
    @Test void d16LeaveProducesOneDownAndKeepsConfiguredEdge() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(true); var remote = UUID.randomUUID(); f.ok(c, remote);
            var leave = new ControlMessage(MessageType.NODE_LEAVE, UUID.randomUUID(), "B", remote, new ControlMessage.Leave(0));
            f.message(c, leave); f.message(c, leave); f.io.sink.closed(c, Connection.CloseReason.PEER_EOF, f.clock.get()); f.pump();
            assertEquals(1, f.downs()); assertTrue(f.manager.snapshot().isEmpty()); assertTrue(f.manager.diagnostics().containsKey("B"));
        }
    }
    @Test void d17RetentionOverflowClosesOnlyCandidateAndFreesBufferedFrames() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(false); var remote = UUID.randomUUID(); f.ok(c, remote);
            for (int i = 0; i < 5; i++) f.message(c, new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), "B", remote, new ControlMessage.Probe(i)));
            c.written(); f.pump(); assertFalse(c.open); assertEquals(0, f.ups()); assertTrue(f.received().isEmpty());
            assertTrue(f.timers.peak <= 3);
        }
    }
    @Test void d18SaturatedOwnerCleansUpWithoutAnotherQueueSlot() {
        var f = new Fixture("A"); f.ready("B"); f.owner.reject = true;
        var c = new FakeChannel(); f.io.sink.opened(c, f.clock.get());
        assertTrue(f.io.stopped); assertTrue(f.timers.stopped); assertFalse(c.open); assertTrue(f.manager.snapshot().isEmpty());
        assertTrue(f.manager.awaitTermination(Duration.ofSeconds(1)));
    }
    @Test void d19StopDuringDnsAndBindCannotRevive() {
        try (var f = new Fixture("A")) {
            f.io.holdDns = true; f.manager.start(); f.pump(); f.manager.requestClose(); f.pump();
            f.io.jobs.values().forEach(job -> job.complete(InetAddress.getLoopbackAddress())); f.pump();
            assertTrue(f.io.dials.isEmpty()); assertTrue(f.manager.start().toCompletableFuture().isCompletedExceptionally());
        }
        try (var f = new Fixture("A")) {
            f.io.holdBind = true; f.manager.start(); f.pump(); f.manager.requestClose(); f.pump();
            f.io.binding.complete(new InetSocketAddress(InetAddress.getLoopbackAddress(), 19001)); f.pump();
            assertTrue(f.io.stopped); assertTrue(f.io.dials.isEmpty());
        }
    }
    @Test void d20BorrowedOwnerSurvivesManagerShutdownAndReceivesRuntimeOptions() {
        try (var f = new Fixture(config("A", 19001), true)) {
            f.ready("B"); assertEquals(256, f.io.options.queueCapacity()); assertEquals(64, f.io.options.maxConnections());
            f.manager.requestClose(); f.pump(); assertFalse(f.owner.stopped);
            var ran = new boolean[1]; assertTrue(f.owner.offer(() -> ran[0] = true)); f.pump(); assertTrue(ran[0]);
        }
    }

    static RuntimeConfiguration fastRuntime() {
        var r = RuntimeConfiguration.defaults();
        return new RuntimeConfiguration(new RuntimeConfiguration.Control(Duration.ofMillis(300), Duration.ofSeconds(3), 64, 256, Duration.ofMillis(500)),
                new RuntimeConfiguration.Overlay(Duration.ofMillis(500), Duration.ofMillis(20), Duration.ofMillis(100), 0, 4096),
                r.monitoring(), r.routing(), r.subscription(), r.video(), r.source());
    }
    static void until(java.util.function.BooleanSupplier condition) throws Exception {
        long began = System.nanoTime();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() - began > TimeUnit.SECONDS.toNanos(5)) fail("TCP condition did not converge within 5 seconds");
            Thread.sleep(5);
        }
    }
    static final class Deployment implements AutoCloseable {
        final NodeConfiguration bootstrapConfig;
        final List<NodeConfiguration> configs = new ArrayList<>();
        final List<NeighborManager> managers = new ArrayList<>();
        pt.uminho.esr.ott.bootstrap.BootstrapServer bootstrap;
        Deployment() throws Exception {
            var sockets = new ArrayList<ServerSocket>(); var ports = new ArrayList<Integer>();
            try {
                for (int i = 0; i < 7; i++) { var s = new ServerSocket(0, 1, InetAddress.getLoopbackAddress()); sockets.add(s); ports.add(s.getLocalPort()); }
            } finally { for (var socket : sockets) socket.close(); }
            var nodes = new LinkedHashMap<String, TopologyConfiguration.Node>();
            for (int i = 0; i < 3; i++) {
                String id = "N" + i;
                nodes.put(id, new TopologyConfiguration.Node(id, "127.0.0.1", ports.get(i + 1), ports.get(i + 4), NodeRole.RELAY,
                        new LinkedHashSet<>(List.of("N0", "N1", "N2").stream().filter(n -> !n.equals(id)).toList())));
                configs.add(new NodeConfiguration(Path.of("test-" + id), id, NodeRole.RELAY, "127.0.0.1", "127.0.0.1", ports.get(i + 1), ports.get(i + 4),
                        Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1", ports.getFirst())), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), List.of(), fastRuntime()));
            }
            bootstrapConfig = new NodeConfiguration(Path.of("test-bootstrap"), "BOOT", NodeRole.BOOTSTRAP, "127.0.0.1", "127.0.0.1", ports.getFirst(), 0,
                    Optional.empty(), Optional.of(Path.of("test-topology")), Optional.empty(), Optional.empty(), Optional.of(new TopologyConfiguration(Path.of("test-topology"), nodes)), List.of(), fastRuntime());
            bootstrap = new pt.uminho.esr.ott.bootstrap.BootstrapServer(bootstrapConfig);
            for (var config : configs) managers.add(new NeighborManager(config, UUID.randomUUID(), e -> {}));
        }
        void start(int[] order, boolean simultaneous) throws Exception {
            if (simultaneous) {
                var starts = new ArrayList<CompletableFuture<Void>>();
                for (int i : order) starts.add(CompletableFuture.runAsync(() -> managers.get(i).start().toCompletableFuture().join()));
                CompletableFuture.allOf(starts.toArray(CompletableFuture[]::new)).get(5, TimeUnit.SECONDS);
            } else for (int i : order) managers.get(i).start().toCompletableFuture().get(3, TimeUnit.SECONDS);
            until(() -> managers.stream().allMatch(m -> m.snapshot().size() == 2));
            for (var m : managers) assertEquals(2, new HashSet<>(m.snapshot().values().stream().map(Neighbor.Session::connectionId).toList()).size());
        }
        public void close() {
            for (var m : managers) m.requestClose();
            try { for (var m : managers) assertTrue(m.awaitTermination(Duration.ofSeconds(3)), "All real TCP/owner/I/O/timer resources terminate"); }
            finally { if (bootstrap != null) bootstrap.close(); }
        }
    }
    @Test void d21SixOrdersAndSimultaneousStartConvergeOnRealTcp() throws Exception {
        int[][] orders = {{0,1,2}, {0,2,1}, {1,0,2}, {1,2,0}, {2,0,1}, {2,1,0}};
        for (var order : orders) try (var deployment = new Deployment()) { deployment.start(order, false); }
        try (var deployment = new Deployment()) { deployment.start(orders[0], true); }
    }
    @Test void d21RealReconnectWithoutBootstrapUnknownPeerAndRestartNewInstance() throws Exception {
        try (var d = new Deployment()) {
            d.start(new int[]{0,1,2}, false);
            try (var bad = new Socket("127.0.0.1", d.configs.get(0).controlPort())) {
                bad.setSoTimeout(2000); var remote = UUID.randomUUID();
                MessageCodec.write(new ControlMessage(MessageType.HELLO, UUID.randomUUID(), "UNKNOWN", remote,
                        new ControlMessage.EndpointPayload(endpoint("UNKNOWN", remote, 18002))), bad.getOutputStream());
                bad.getOutputStream().flush(); var reply = MessageCodec.read(bad.getInputStream()).orElseThrow();
                assertEquals(MessageType.ERROR, reply.type());
                assertEquals(ErrorCode.UNAUTHORIZED_NEIGHBOR, ((ControlMessage.ErrorPayload)reply.payload()).code());
                assertTrue(MessageCodec.read(bad.getInputStream()).isEmpty());
            }
            var a = d.managers.getFirst(); var old = a.snapshot().get("N1");
            d.bootstrap.close(); d.bootstrap = null;
            assertTrue(a.disconnect(old).toCompletableFuture().get(2, TimeUnit.SECONDS));
            until(() -> a.snapshot().containsKey("N1") && !a.snapshot().get("N1").connectionId().equals(old.connectionId()));
            assertEquals(old.instanceId(), a.snapshot().get("N1").instanceId());
            assertFalse(a.disconnect(old).toCompletableFuture().get(2, TimeUnit.SECONDS));
            d.managers.get(1).close(); until(() -> !a.snapshot().containsKey("N1"));
            d.bootstrap = new pt.uminho.esr.ott.bootstrap.BootstrapServer(d.bootstrapConfig);
            var restartEvents = new java.util.concurrent.CopyOnWriteArrayList<NeighborManager.Event>();
            var newInstance = UUID.randomUUID(); var restarted = new NeighborManager(d.configs.get(1), newInstance, restartEvents::add);
            d.managers.set(1, restarted);
            try { restarted.start().toCompletableFuture().get(3, TimeUnit.SECONDS); }
            catch (Exception e) { throw new AssertionError("Restart events: " + restartEvents, e); }
            until(() -> a.snapshot().containsKey("N1") && a.snapshot().get("N1").instanceId().equals(newInstance));
            assertFalse(a.disconnect(old).toCompletableFuture().get(2, TimeUnit.SECONDS));
        }
    }

    @Test void d02CompleteBootstrapReplyRemainsValidWhenSocketHasAlreadyReachedEof() {
        try (var f = new Fixture("Z")) {
            var boot = f.discovery();
            var reply = new ControlMessage(MessageType.REGISTER_OK, boot.last().requestId(), "BOOT", UUID.randomUUID(),
                    new ControlMessage.NeighborList(List.of(endpoint("B", ZERO, 19002))));
            boot.close(); // Socket state can lead delivery of the already captured reader events.
            f.io.sink.message(boot, reply, f.clock.get());
            f.io.sink.closed(boot, Connection.CloseReason.PEER_EOF, f.clock.get()); f.pump();
            f.manager.start().toCompletableFuture().join();
            assertEquals(Set.of("B"), f.manager.diagnostics().keySet()); assertEquals(1, f.io.dials.size());
        }
    }
    @Test void d06InvalidInboundEndpointDirectionAndIdentityDoNotAuthorize() {
        for (int kind = 0; kind < 6; kind++) try (var f = new Fixture("Z")) {
            f.ready("B"); var c = new FakeChannel(); var remote = UUID.randomUUID();
            var ep = switch (kind) {
                case 0 -> new ControlMessage.Endpoint("B", remote, "other.invalid", 19002, 19102, NodeRole.RELAY);
                case 1 -> new ControlMessage.Endpoint("B", remote, "127.0.0.1", 19003, 19102, NodeRole.RELAY);
                case 2 -> new ControlMessage.Endpoint("B", remote, "127.0.0.1", 19002, 19102, NodeRole.CLIENT);
                case 3 -> endpoint("B", UUID.randomUUID(), 19002);
                case 4 -> endpoint("UNKNOWN", remote, 19002);
                default -> endpoint("B", ZERO, 19002);
            };
            f.io.sink.opened(c, f.clock.get()); f.pump();
            f.message(c, new ControlMessage(MessageType.HELLO, UUID.randomUUID(), "B", remote, new ControlMessage.EndpointPayload(ep)));
            assertEquals(0, f.ups()); assertFalse(c.open); assertEquals(2, f.io.resolved.size(), "No DNS from remote HELLO");
        }
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.inbound("B", UUID.randomUUID(), true);
            assertEquals(0, f.ups()); // Still unclassified while the native outbound dial is pending.
            f.io.dials.get(1).future.completeExceptionally(new java.io.IOException("Dial failed")); f.pump();
            assertFalse(c.open); assertEquals(0, f.ups()); // B must not dial smaller A.
        }
    }
    @Test void d09DuplicateLiveIdPreservesHealthySession() {
        try (var f = new Fixture("Z")) {
            f.ready("B"); var first = f.inbound("B", UUID.randomUUID(), true);
            var original = f.manager.snapshot().get("B");
            var duplicate = f.inbound("B", UUID.randomUUID(), true);
            assertEquals(ErrorCode.DUPLICATE_ID, ((ControlMessage.ErrorPayload)duplicate.last().payload()).code());
            assertFalse(duplicate.open); assertTrue(first.open); assertEquals(original, f.manager.snapshot().get("B"));
            assertEquals(1, f.ups()); assertEquals(0, f.downs());
        }
    }
    @Test void d08RetentionAtLimitDrainsInFifoOnlyAfterFlush() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(false); var remote = UUID.randomUUID(); f.ok(c, remote);
            var originals = new ArrayList<ControlMessage>();
            for (int i = 0; i < 4; i++) {
                var m = new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), "B", remote, new ControlMessage.Probe(i));
                originals.add(m); f.message(c, m);
            }
            assertTrue(f.received().isEmpty()); c.written(); f.pump();
            assertEquals(originals, f.received().stream().map(NeighborManager.Received::message).toList()); assertEquals(1, f.ups());
        }
    }
    @Test void d12DnsAndBindBudgetsIncludeQueueDelayBeforeResultsAreProcessed() {
        try (var f = new Fixture("A")) {
            f.io.holdDns = true; f.manager.start(); f.pump();
            f.io.jobs.values().forEach(job -> job.complete(InetAddress.getLoopbackAddress()));
            f.clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(2000)); f.pump();
            assertTrue(f.manager.start().toCompletableFuture().isCompletedExceptionally()); assertNull(f.io.sink);
        }
        try (var f = new Fixture("A")) {
            f.io.holdBind = true; f.manager.start(); f.pump();
            f.io.binding.complete(new InetSocketAddress(InetAddress.getLoopbackAddress(), 19001));
            f.clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(2000)); f.pump();
            assertTrue(f.manager.start().toCompletableFuture().isCompletedExceptionally()); assertTrue(f.io.dials.isEmpty());
        }
    }
    @Test void d15RealDnsRetirementKeepsPhysicalReservationUntilLookupActuallyReturns() throws Exception {
        var entered = new CountDownLatch(1); var release = new CountDownLatch(1);
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        var io = new NeighborManager.RealIo(65, host -> {
            calls.incrementAndGet(); entered.countDown();
            boolean interrupted = false;
            while (true) try { release.await(); break; } catch (InterruptedException e) { interrupted = true; }
            if (interrupted) Thread.currentThread().interrupt();
            return InetAddress.getLoopbackAddress();
        });
        try {
            var id = UUID.randomUUID(); var pending = io.resolve(id, "blocked.invalid");
            assertTrue(entered.await(1, TimeUnit.SECONDS)); io.retire(id);
            assertFalse(pending.isDone(), "Logical cancellation is not physical DNS completion");
            assertTrue(io.resolve(UUID.randomUUID(), "BLOCKED.INVALID.").isCompletedExceptionally()); assertEquals(1, calls.get());
            var queued = new ArrayList<CompletableFuture<InetAddress>>();
            for (int n = 0; n < 64; n++) queued.add(io.resolve(UUID.randomUUID(), "queued" + n + ".invalid"));
            assertTrue(queued.stream().noneMatch(CompletableFuture::isDone));
            assertTrue(io.resolve(UUID.randomUUID(), "overflow.invalid").isCompletedExceptionally());
            io.requestClose(); assertFalse(io.awaitTermination(Duration.ofMillis(10)));
            release.countDown(); assertTrue(io.awaitTermination(Duration.ofSeconds(2))); assertTrue(pending.isDone());
        } finally { release.countDown(); io.requestClose(); assertTrue(io.awaitTermination(Duration.ofSeconds(2))); }
    }
    @Test void d17CancelledRunningTimerStillOccupiesItsPhysicalPermit() throws Exception {
        var timers = new NeighborManager.RealDeadlines(2); timers.limit(1);
        var entered = new CountDownLatch(1); var release = new CountDownLatch(1);
        try {
            var ticket = timers.schedule(() -> {
                entered.countDown();
                try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, 1);
            assertTrue(entered.await(1, TimeUnit.SECONDS)); ticket.cancel();
            assertThrows(RejectedExecutionException.class, () -> timers.schedule(() -> {}, 1));
            release.countDown();
        } finally { release.countDown(); timers.requestClose(); assertTrue(timers.awaitTermination(Duration.ofSeconds(2))); }
    }

    static NodeConfiguration runtimeConfig(RuntimeConfiguration runtime) {
        var c = config("A", 19001);
        return new NodeConfiguration(c.sourceFile(), c.nodeId(), c.role(), c.bindHost(), c.advertisedHost(), c.controlPort(), c.dataPort(),
                c.bootstrap(), c.topologyFile(), c.streamsFile(), c.mediaBase(), c.topology(), c.streams(), runtime);
    }
    @Test void d13BackoffSaturatesAndJitterExtremesStayWithinOneAndMax() {
        var r = RuntimeConfiguration.defaults();
        for (long mode : List.of(-1L, 0L, 1L)) {
            var runtime = new RuntimeConfiguration(r.control(),
                    new RuntimeConfiguration.Overlay(r.overlay().helloTimeout(), Duration.ofMillis(Integer.MAX_VALUE - 1L), Duration.ofMillis(Integer.MAX_VALUE), 100, 4096),
                    r.monitoring(), r.routing(), r.subscription(), r.video(), r.source());
            try (var f = new Fixture(runtimeConfig(runtime), false)) {
                f.jitterMode = mode; f.ready("B");
                f.io.dials.get(1).future.completeExceptionally(new java.io.IOException("offline")); f.pump();
                long delay = f.manager.diagnostics().get("B").retryDelayMillis();
                assertEquals(mode < 0 ? 1L : mode > 0 ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1L, delay);
                f.timers.advance(TimeUnit.MILLISECONDS.toNanos(delay)); f.pump();
                f.io.dials.get(2).future.completeExceptionally(new java.io.IOException("still offline")); f.pump();
                assertEquals(mode < 0 ? 1L : Integer.MAX_VALUE, f.manager.diagnostics().get("B").retryDelayMillis());
            }
        }
    }
    @Test void d13BackoffResetsAfterActiveAndNoImmediateRetryOccurs() {
        try (var f = new Fixture("A")) {
            f.ready("B"); f.io.dials.get(1).future.completeExceptionally(new java.io.IOException("offline")); f.pump();
            assertEquals(500, f.manager.diagnostics().get("B").retryDelayMillis());
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(500)); f.pump(); var c = f.connect(2, false); f.ok(c, UUID.randomUUID());
            assertEquals(0, f.manager.diagnostics().get("B").retryDelayMillis());
            f.manager.disconnect(f.manager.snapshot().get("B")); f.pump();
            assertEquals(500, f.manager.diagnostics().get("B").retryDelayMillis()); assertEquals(3, f.io.dials.size());
        }
    }
    @Test void d14InboundWaitsForOtherDialBarrierWithoutExtendingHelloDeadline() {
        try (var f = new Fixture(config("M", 19001), false)) {
            var boot = f.discovery();
            f.message(boot, new ControlMessage(MessageType.REGISTER_OK, boot.last().requestId(), "BOOT", UUID.randomUUID(),
                    new ControlMessage.NeighborList(List.of(endpoint("A", ZERO, 19002), endpoint("Z", ZERO, 19003)))));
            var remote = UUID.randomUUID(); var inbound = f.inbound("A", remote, true);
            assertTrue(inbound.writes.isEmpty()); assertEquals(0, f.ups());
            f.io.dials.get(1).future.completeExceptionally(new java.io.IOException("No Z")); f.pump();
            assertEquals(1, f.ups()); assertEquals(remote, f.manager.snapshot().get("A").instanceId());
        }
        try (var f = new Fixture(config("M", 19001), false)) {
            var boot = f.discovery();
            f.message(boot, new ControlMessage(MessageType.REGISTER_OK, boot.last().requestId(), "BOOT", UUID.randomUUID(),
                    new ControlMessage.NeighborList(List.of(endpoint("A", ZERO, 19002), endpoint("Z", ZERO, 19003)))));
            var c = f.inbound("A", UUID.randomUUID(), true);
            f.clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(3000));
            f.io.dials.get(1).future.completeExceptionally(new java.io.IOException("Late dial failure")); f.pump();
            assertEquals(0, f.ups()); assertFalse(c.open);
        }
    }
    @Test void d18ObserverFailureHasExplicitCleanupAndCannotLeaveAnActiveSnapshot() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(true); f.observerFails = true; f.ok(c, UUID.randomUUID());
            assertTrue(f.io.stopped); assertTrue(f.manager.snapshot().isEmpty()); assertFalse(c.open);
            assertTrue(f.events.stream().anyMatch(e -> e instanceof NeighborManager.Diagnostic d && d.reason() == NeighborManager.Reason.CONSUMER_FAILURE));
        }
    }
    @Test void d20SmallerRuntimeCapsReachTransportAndHandshakeRetention() {
        var r = RuntimeConfiguration.defaults();
        var runtime = new RuntimeConfiguration(new RuntimeConfiguration.Control(Duration.ofMillis(31), Duration.ofMillis(97), 2, 1, Duration.ofMillis(53)),
                new RuntimeConfiguration.Overlay(Duration.ofMillis(71), Duration.ofMillis(11), Duration.ofMillis(47), 0, 8),
                r.monitoring(), r.routing(), r.subscription(), r.video(), r.source());
        try (var f = new Fixture(runtimeConfig(runtime), true)) {
            f.ready("B"); assertEquals(1, f.io.options.queueCapacity()); assertEquals(2, f.io.options.maxConnections());
            assertEquals(Duration.ofMillis(31), f.io.options.connectTimeout()); assertEquals(Duration.ofMillis(97), f.io.options.shutdownTimeout());
            var c = f.outbound(false); var remote = UUID.randomUUID(); f.ok(c, remote);
            f.message(c, new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), "B", remote, new ControlMessage.Probe(1)));
            assertTrue(c.open);
            f.message(c, new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), "B", remote, new ControlMessage.Probe(2)));
            assertFalse(c.open); assertEquals(0, f.ups()); assertEquals(11, f.manager.diagnostics().get("B").retryDelayMillis());
        }
    }
    @Test void d19SuccessfulCloseRemainsIdempotentAfterItsOriginalBudget() {
        var f = new Fixture("A"); f.ready("B"); f.manager.requestClose(); f.pump();
        assertTrue(f.manager.awaitTermination(Duration.ofSeconds(1)));
        f.clock.addAndGet(TimeUnit.SECONDS.toNanos(10)); assertDoesNotThrow(f.manager::close);
    }

    @Test void d11ActiveErrorCapturedBeforeEofIsDeliveredBeforeDown() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(true); var remote = UUID.randomUUID(); f.ok(c, remote);
            var m = error(c, "B", remote, ErrorCode.PATH_CHANGED, 11);
            c.close(); f.io.sink.message(c, m, f.clock.get()); f.io.sink.closed(c, Connection.CloseReason.PEER_EOF, f.clock.get()); f.pump();
            assertSame(m, f.received().getLast().message()); assertEquals(1, f.downs());
            int received = f.events.indexOf(f.received().getLast());
            int down = java.util.stream.IntStream.range(0, f.events.size()).filter(i -> f.events.get(i) instanceof NeighborManager.NeighborDown).findFirst().orElseThrow();
            assertTrue(received < down);
        }
    }
    @Test void d19AwaitPassesOnlyRemainingBudgetAndCloseDoesNotRenewDeadline() {
        var f = new Fixture("A");
        try {
            f.ready("B"); f.manager.requestClose(); f.pump();
            f.io.awaitClock = f.clock; f.io.consumeMillis = 40;
            assertTrue(f.manager.awaitTermination(Duration.ofMillis(100)));
            assertEquals(List.of(Duration.ofMillis(100), Duration.ofMillis(60)), f.io.awaitBudgets);
        } finally { f.io.consumeMillis = 0; f.close(); }
        var stuck = new Fixture("A");
        try {
            stuck.ready("B"); stuck.io.holdTermination = true; stuck.manager.requestClose(); stuck.pump();
            stuck.clock.addAndGet(TimeUnit.SECONDS.toNanos(4));
            assertThrows(IllegalStateException.class, stuck.manager::close);
            assertFalse(stuck.manager.awaitTermination(Duration.ofMillis(10)));
            stuck.io.holdTermination = false;
            assertTrue(stuck.manager.awaitTermination(Duration.ofMillis(100)));
        } finally { stuck.io.holdTermination = false; stuck.close(); }
    }
    @Test void d03DegreeCapAndConfiguredCopiesAreCheckedBeforeAnyPartialDial() {
        for (int count : List.of(63, 64)) try (var f = new Fixture("Z")) {
            var boot = f.discovery(); var source = new ArrayList<ControlMessage.Endpoint>();
            for (int i = 0; i < count; i++) source.add(endpoint("A" + i, ZERO, 20000 + i));
            var payload = new ControlMessage.NeighborList(source); source.clear();
            f.message(boot, new ControlMessage(MessageType.REGISTER_OK, boot.last().requestId(), "BOOT", UUID.randomUUID(), payload));
            if (count == 63) { assertEquals(63, f.manager.diagnostics().size()); assertFalse(f.manager.start().toCompletableFuture().isCompletedExceptionally()); }
            else { assertTrue(f.manager.diagnostics().isEmpty()); assertTrue(f.manager.start().toCompletableFuture().isCompletedExceptionally()); }
            assertEquals(1, f.io.dials.size());
        }
    }
    @Test void d17GlobalRetentionLimitPreservesOtherCandidatesAndReleasesCharges() {
        var r = RuntimeConfiguration.defaults();
        var runtime = new RuntimeConfiguration(new RuntimeConfiguration.Control(r.control().connectTimeout(), r.control().shutdownTimeout(), 4, 4, r.control().bootstrapQueryTimeout()),
                new RuntimeConfiguration.Overlay(r.overlay().helloTimeout(), r.overlay().retryInitial(), r.overlay().retryMax(), 0, 8),
                r.monitoring(), r.routing(), r.subscription(), r.video(), r.source());
        var base = runtimeConfig(runtime);
        var config = new NodeConfiguration(base.sourceFile(), "Z", base.role(), base.bindHost(), base.advertisedHost(), base.controlPort(), base.dataPort(), base.bootstrap(),
                base.topologyFile(), base.streamsFile(), base.mediaBase(), base.topology(), base.streams(), runtime);
        try (var f = new Fixture(config, true)) {
            var boot = f.discovery();
            var desired = List.of(endpoint("A", ZERO, 19002), endpoint("B", ZERO, 19003), endpoint("C", ZERO, 19004));
            f.message(boot, new ControlMessage(MessageType.REGISTER_OK, boot.last().requestId(), "BOOT", UUID.randomUUID(), new ControlMessage.NeighborList(desired)));
            var candidates = new ArrayList<FakeChannel>();
            for (int i = 0; i < 3; i++) {
                var c = new FakeChannel(); c.flush = false; candidates.add(c); var ep = desired.get(i); var remote = UUID.randomUUID();
                f.io.sink.opened(c, f.clock.get()); f.pump();
                f.message(c, new ControlMessage(MessageType.HELLO, UUID.randomUUID(), ep.nodeId(), remote, new ControlMessage.EndpointPayload(
                        new ControlMessage.Endpoint(ep.nodeId(), remote, ep.host(), ep.controlPort(), ep.dataPort(), ep.role()))));
                for (int n = 0; n < 3; n++) f.message(c, new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), ep.nodeId(), remote, new ControlMessage.Probe(n)));
            }
            assertFalse(candidates.get(2).open); // Nine frames exceed R=8 while B=4 was not exceeded.
            candidates.get(0).written(); candidates.get(1).written(); f.pump();
            assertEquals(2, f.ups()); assertEquals(6, f.received().size()); assertEquals(Set.of("A", "B"), f.manager.snapshot().keySet());
        }
    }

    @Test void d03DegreeAboveEffectiveConnectionCapFailsWithoutPartialInstall() {
        var r = RuntimeConfiguration.defaults();
        var runtime = new RuntimeConfiguration(new RuntimeConfiguration.Control(r.control().connectTimeout(), r.control().shutdownTimeout(), 2, 4, r.control().bootstrapQueryTimeout()),
                r.overlay(), r.monitoring(), r.routing(), r.subscription(), r.video(), r.source());
        try (var f = new Fixture(runtimeConfig(runtime), false)) {
            var boot = f.discovery();
            f.message(boot, new ControlMessage(MessageType.REGISTER_OK, boot.last().requestId(), "BOOT", UUID.randomUUID(), new ControlMessage.NeighborList(
                    List.of(endpoint("B", ZERO, 19002), endpoint("C", ZERO, 19003), endpoint("D", ZERO, 19004)))));
            assertTrue(f.manager.start().toCompletableFuture().isCompletedExceptionally());
            assertEquals(1, f.io.dials.size()); assertTrue(f.manager.diagnostics().isEmpty());
        }
    }
    @Test void d01InvalidOwnerAndEndpointFailBeforeFactories() {
        var owner = new ManualOwner(); owner.capacity = 1;
        assertThrows(IllegalArgumentException.class, () -> new NeighborManager(config("A", 19001), UUID.randomUUID(), owner, e -> {}));
        assertThrows(IllegalArgumentException.class, () -> new Neighbor.Session("B", UUID.randomUUID(), UUID.randomUUID(),
                new ControlMessage.Endpoint("B", UUID.randomUUID(), "0.0.0.0", 19002, 19102, NodeRole.RELAY)));
        assertThrows(IllegalArgumentException.class, () -> new NeighborManager(config("bad id", 19001), UUID.randomUUID(), e -> {}));
    }

    @Test void d07InvalidHelloOkEnvelopeAndRequestCannotPublishUp() {
        for (int kind = 0; kind < 4; kind++) try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(true);
            f.message(c, new ControlMessage(MessageType.HELLO_OK, kind == 0 ? UUID.randomUUID() : c.last().requestId(),
                    kind == 1 ? "WRONG" : "B", kind == 2 ? ZERO : UUID.randomUUID(),
                    kind == 3 ? new ControlMessage.Probe(1) : ControlMessage.Empty.INSTANCE));
            assertFalse(c.open); assertEquals(0, f.ups()); assertTrue(f.manager.snapshot().isEmpty());
        }
    }
    @Test void d07UnexpectedAndWrongIdentityHandshakeErrorsNeverGenerateErrorResponse() {
        for (int kind = 0; kind < 3; kind++) try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(false);
            f.message(c, new ControlMessage(MessageType.ERROR, kind == 0 ? UUID.randomUUID() : c.last().requestId(),
                    kind == 1 ? "OTHER" : "B", UUID.randomUUID(), new ControlMessage.ErrorPayload(ErrorCode.TIMEOUT, kind == 2 ? 6 : 5, "not correlated")));
            assertEquals(1, c.writes.size()); assertFalse(c.open); assertEquals(0, f.ups());
            c.written(); f.pump(); assertEquals(0, f.ups());
        }
    }
    @Test void d11WrongActiveIdentityIsIsolatedAndLocalSendValidatesIdentityAndSession() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(true); var remote = UUID.randomUUID(); f.ok(c, remote);
            var session = f.manager.snapshot().get("B");
            var wrongLocal = new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), "OTHER", f.instance, new ControlMessage.Probe(1));
            var bad = f.manager.send(session, wrongLocal); f.pump(); assertEquals(Connection.SendResult.INVALID_MESSAGE, bad.toCompletableFuture().join());
            var local = new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), "A", f.instance, new ControlMessage.Probe(2));
            var sent = f.manager.send(session, local); f.pump(); assertEquals(Connection.SendResult.ACCEPTED, sent.toCompletableFuture().join()); assertSame(local, c.last());
            f.message(c, new ControlMessage(MessageType.ERROR, UUID.randomUUID(), "B", UUID.randomUUID(), new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE, 11, "wrong instance")));
            assertTrue(f.received().isEmpty()); assertFalse(c.open); assertEquals(1, f.downs());
            var stale = f.manager.send(session, local); f.pump(); assertEquals(Connection.SendResult.CLOSED, stale.toCompletableFuture().join());
        }
    }
    @Test void d19ExternalBorrowedOwnerFailureRunsManagerCleanupOnItsStopHook() {
        try (var f = new Fixture(config("A", 19001), true)) {
            f.ready("B"); var c = f.outbound(true); f.ok(c, UUID.randomUUID());
            f.owner.fail(new IllegalStateException("Other node component failed"));
            assertTrue(f.manager.snapshot().isEmpty()); assertFalse(c.open); assertTrue(f.io.stopped); assertTrue(f.timers.stopped);
        }
    }
}
