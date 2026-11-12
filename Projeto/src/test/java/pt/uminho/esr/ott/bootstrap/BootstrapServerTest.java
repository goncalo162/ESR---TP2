package pt.uminho.esr.ott.bootstrap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import pt.uminho.esr.ott.config.ConfigurationLoader;
import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.overlay.NodeRole;
import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.protocol.MessageCodec;
import pt.uminho.esr.ott.protocol.MessageType;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class BootstrapServerTest {
    private static NodeConfiguration configuration() throws Exception {
        return ConfigurationLoader.load(Path.of("src/main/resources/config/bootstrap.properties"), NodeRole.BOOTSTRAP);
    }

    @Test void queryReturnsConfiguredOfflineNeighborsThenEof() throws Exception {
        var config = configurationWithClosedNeighborPorts();
        try (var service = new BootstrapServer(config, new InetSocketAddress("127.0.0.1", 0));
             var peer = new Socket()) {
            var address = service.localAddress();
            peer.connect(address); peer.setSoTimeout(3000);
            var request = request(config, "R1");
            MessageCodec.write(request, peer.getOutputStream()); peer.getOutputStream().flush();
            var reply = MessageCodec.read(peer.getInputStream()).orElseThrow();
            assertEquals(MessageType.REGISTER_OK, reply.type()); assertEquals(request.requestId(), reply.requestId());
            assertEquals("B", reply.senderId());
            var neighbors = ((ControlMessage.NeighborList) reply.payload()).neighbors();
            assertEquals(java.util.List.of("S1", "S2", "C1"), neighbors.stream().map(ControlMessage.Endpoint::nodeId).toList());
            assertTrue(neighbors.stream().allMatch(n -> n.instanceId().equals(new UUID(0, 0))));
            assertTrue(MessageCodec.read(peer.getInputStream()).isEmpty());
        }
    }

    @Test void endpointComparisonUsesLexicalNormalizationWithoutDns() throws Exception {
        assertEquals("node.example", BootstrapServer.normalizeHost("Node.Example."));
        assertEquals("127.0.0.1", BootstrapServer.normalizeHost("127.000.000.001"));
        assertEquals("::1", BootstrapServer.normalizeHost("[::1]"));
    }
    private static NodeConfiguration configurationWithClosedNeighborPorts() throws Exception {
        var base = configuration();
        var nodes = new java.util.LinkedHashMap<>(base.topology().orElseThrow().nodes());
        var reservations = new java.util.ArrayList<java.net.ServerSocket>();
        try {
            for (var node : java.util.List.copyOf(nodes.values())) {
                var socket = new java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress());
                reservations.add(socket);
                nodes.put(node.nodeId(), new pt.uminho.esr.ott.config.TopologyConfiguration.Node(node.nodeId(),
                        "127.0.0.1", socket.getLocalPort(), node.dataPort(), node.role(), node.neighbors()));
            }
        } finally { for (var socket : reservations) socket.close(); }
        for (var node : nodes.values()) try (var probe = new Socket()) {
            assertThrows(java.net.ConnectException.class,
                    () -> probe.connect(new InetSocketAddress(node.host(), node.controlPort()), 500));
        }
        return copy(base, NodeRole.BOOTSTRAP, nodes, base.runtime());
    }

    private static final class ManualEvents implements BootstrapServer.SerialEvents {
        final java.util.ArrayDeque<Runnable> queue = new java.util.ArrayDeque<>();
        final int capacity; boolean running, stopped;
        ManualEvents() { this(4096); }
        ManualEvents(int capacity) { this.capacity = capacity; }
        @Override public void execute(Runnable task) {
            if (stopped || queue.size() >= capacity) throw new java.util.concurrent.RejectedExecutionException("Controlled event capacity");
            queue.add(task);
        }
        void drain() {
            while (!queue.isEmpty()) {
                running = true;
                try { queue.remove().run(); } finally { running = false; }
            }
        }
        @Override public boolean isOwnerThread() { return running; }
        @Override public void close() { stopped = true; queue.clear(); }
        @Override public boolean await(java.time.Duration timeout) { return stopped && !running; }
    }
    private static final class ManualDeadlines implements BootstrapServer.Deadlines {
        private static final class Task { long due; Runnable action; }
        final java.util.concurrent.atomic.AtomicLong clock;
        final java.util.ArrayList<Task> tasks = new java.util.ArrayList<>();
        boolean stopped; int peak;
        ManualDeadlines(java.util.concurrent.atomic.AtomicLong clock) { this.clock = clock; }
        @Override public BootstrapServer.Ticket schedule(Runnable action, long delay) {
            assertFalse(stopped); assertTrue(delay > 0); assertTrue(tasks.size() < 64);
            var task = new Task(); task.due = clock.get() + delay; task.action = action;
            tasks.add(task); peak = Math.max(peak, tasks.size()); return () -> tasks.remove(task);
        }
        void advance(long nanos) {
            clock.addAndGet(nanos);
            for (var task : java.util.List.copyOf(tasks)) if (clock.get() - task.due >= 0) {
                tasks.remove(task); task.action.run();
            }
        }
        @Override public void close() { stopped = true; tasks.clear(); }
        @Override public boolean await(java.time.Duration timeout) { return stopped && tasks.isEmpty(); }
    }
    private static final class FakeChannel implements BootstrapServer.Channel {
        final UUID id = UUID.randomUUID(); boolean open = true;
        final java.util.List<ControlMessage> sent = new java.util.ArrayList<>();
        java.util.concurrent.CompletableFuture<Void> pending;
        @Override public UUID id() { return id; }
        @Override public boolean isOpen() { return open; }
        @Override public java.util.concurrent.CompletableFuture<Void> sendAndFlush(ControlMessage m) {
            assertTrue(open); assertTrue(sent.isEmpty(), "Only one response decision per connection");
            sent.add(m); pending = new java.util.concurrent.CompletableFuture<>(); return pending;
        }
        @Override public void close() {
            open = false;
            if (pending != null) pending.completeExceptionally(new java.io.IOException("Fake channel closed"));
        }
    }
    private static final class Rig implements AutoCloseable {
        final java.util.concurrent.atomic.AtomicLong clock = new java.util.concurrent.atomic.AtomicLong();
        final ManualDeadlines deadlines = new ManualDeadlines(clock);
        final ManualEvents events;
        final BootstrapServer.Core core;
        final NodeConfiguration config;
        Rig(NodeConfiguration config) { this(config, new ManualEvents()); }
        Rig(NodeConfiguration config, ManualEvents events) {
            this.config = config; this.events = events;
            core = new BootstrapServer.Core(config, new UUID(7, 7), clock::get, deadlines, events);
        }
        FakeChannel opened() { var ch = new FakeChannel(); core.open(ch); events.drain(); return ch; }
        FakeChannel query(String id) { var ch = opened(); core.message(ch, request(config, id)); events.drain(); return ch; }
        void finish(FakeChannel ch) {
            assertNotNull(ch.pending); ch.pending.complete(null); events.drain(); assertFalse(ch.open);
        }
        @Override public void close() { core.requestStop(); events.drain(); assertEquals(0, core.contextCount()); assertTrue(deadlines.tasks.isEmpty()); }
    }
    private static ControlMessage request(NodeConfiguration config, String id) {
        var n = config.topology().orElseThrow().nodes().get(id); var instance = UUID.randomUUID();
        return new ControlMessage(MessageType.REGISTER, UUID.randomUUID(), id, instance,
                new ControlMessage.EndpointPayload(new ControlMessage.Endpoint(id, instance, n.host(), n.controlPort(), n.dataPort(), n.role())));
    }
    private static NodeConfiguration copy(NodeConfiguration b, NodeRole role,
            java.util.Map<String, pt.uminho.esr.ott.config.TopologyConfiguration.Node> nodes,
            pt.uminho.esr.ott.config.RuntimeConfiguration runtime) {
        return new NodeConfiguration(b.sourceFile(), b.nodeId(), role, b.bindHost(), b.advertisedHost(),
                b.controlPort(), b.dataPort(), b.bootstrap(), b.topologyFile(), b.streamsFile(), b.mediaBase(),
                java.util.Optional.of(new pt.uminho.esr.ott.config.TopologyConfiguration(b.topology().orElseThrow().sourceFile(), nodes)),
                b.streams(), runtime);
    }
    private static void assertError(FakeChannel ch, pt.uminho.esr.ott.protocol.ErrorCode code, UUID requestId) {
        assertEquals(1, ch.sent.size()); var m = ch.sent.getFirst();
        assertEquals(MessageType.ERROR, m.type()); assertEquals(requestId, m.requestId());
        assertEquals(code, ((ControlMessage.ErrorPayload) m.payload()).code());
    }

    @Test void coreConfiguredNeighborsAndCorrelationAreIndependentOfHistoryAndOfflineState() throws Exception {
        var config = configuration(); var before = config.topology().orElseThrow();
        try (var rig = new Rig(config)) {
            for (var id : java.util.List.of("S1", "S2", "R1", "C1", "R1")) {
                var ch = rig.opened(); var q = request(config, id); rig.core.message(ch, q); rig.events.drain();
                assertEquals(1, ch.sent.size()); var m = ch.sent.getFirst();
                assertEquals(q.requestId(), m.requestId()); assertEquals("B", m.senderId()); assertEquals(new UUID(7, 7), m.senderInstance());
                var neighbors = ((ControlMessage.NeighborList) m.payload()).neighbors();
                assertEquals(java.util.List.copyOf(before.nodes().get(id).neighbors()), neighbors.stream().map(ControlMessage.Endpoint::nodeId).toList());
                for (var n : neighbors) {
                    var expected = before.nodes().get(n.nodeId());
                    assertEquals(new UUID(0, 0), n.instanceId()); assertEquals(expected.host(), n.host());
                    assertEquals(expected.controlPort(), n.controlPort()); assertEquals(expected.dataPort(), n.dataPort()); assertEquals(expected.role(), n.role());
                }
                assertTrue(ch.open); rig.finish(ch); assertEquals(0, rig.core.contextCount()); assertTrue(rig.deadlines.tasks.isEmpty());
            }
            rig.deadlines.advance(java.time.Duration.ofDays(1).toNanos()); rig.events.drain();
            assertEquals(before, config.topology().orElseThrow()); assertTrue(rig.deadlines.tasks.isEmpty());
        }
    }

    @Test void coreDifferentInstancesOfSameIdCoexistAndOnlyOneResponseIsChosen() throws Exception {
        try (var rig = new Rig(configuration())) {
            var channels = new java.util.ArrayList<FakeChannel>();
            for (int i = 0; i < 16; i++) channels.add(rig.query(i % 2 == 0 ? "R1" : "S1"));
            assertEquals(16, rig.core.contextCount()); assertEquals(16, rig.deadlines.tasks.size());
            for (int i = 15; i >= 0; i--) {
                var ch = channels.get(i); assertEquals(1, ch.sent.size());
                rig.core.message(ch, request(rig.config, "C1")); rig.events.drain(); assertEquals(1, ch.sent.size());
                rig.finish(ch);
            }
            assertEquals(0, rig.core.contextCount());
        }
    }

    @Test void coreIdentityInstanceAndUnknownIdAreRejectedWithoutPoisoningLaterQueries() throws Exception {
        var config = configuration(); var good = request(config, "R1"); var ep = ((ControlMessage.EndpointPayload) good.payload()).endpoint();
        var bad = java.util.List.of(
                new ControlMessage(MessageType.REGISTER, UUID.randomUUID(), "S1", ep.instanceId(), good.payload()),
                new ControlMessage(MessageType.REGISTER, UUID.randomUUID(), "R1", UUID.randomUUID(), good.payload()),
                new ControlMessage(MessageType.REGISTER, UUID.randomUUID(), "R1", new UUID(0, 0), good.payload()),
                new ControlMessage(MessageType.REGISTER, UUID.randomUUID(), "X", ep.instanceId(),
                        new ControlMessage.EndpointPayload(new ControlMessage.Endpoint("X", ep.instanceId(), ep.host(), ep.controlPort(), ep.dataPort(), ep.role()))));
        try (var rig = new Rig(config)) {
            for (int i = 0; i < bad.size(); i++) {
                var ch = rig.opened(); rig.core.message(ch, bad.get(i)); rig.events.drain();
                assertError(ch, i == 3 ? pt.uminho.esr.ott.protocol.ErrorCode.UNAUTHORIZED_NEIGHBOR : pt.uminho.esr.ott.protocol.ErrorCode.INVALID_MESSAGE, bad.get(i).requestId());
                rig.finish(ch);
            }
            var healthy = rig.query("R1"); assertEquals(MessageType.REGISTER_OK, healthy.sent.getFirst().type()); rig.finish(healthy);
        }
    }

    @Test void coreEndpointHostPortsAndRoleAreValidatedLexicallyWithoutDns() throws Exception {
        var config = configuration(); var original = config.topology().orElseThrow().nodes().get("R1");
        var nodes = new java.util.LinkedHashMap<>(config.topology().orElseThrow().nodes());
        nodes.put("R1", new pt.uminho.esr.ott.config.TopologyConfiguration.Node("R1", "Node.Example.", 5300, 5301, NodeRole.RELAY, original.neighbors()));
        config = copy(config, NodeRole.BOOTSTRAP, nodes, config.runtime());
        try (var rig = new Rig(config)) {
            var inst = UUID.randomUUID();
            var endpoints = java.util.List.of(
                    new ControlMessage.Endpoint("R1", inst, "node.example", 5300, 5301, NodeRole.RELAY),
                    new ControlMessage.Endpoint("R1", inst, "different.example", 5300, 5301, NodeRole.RELAY),
                    new ControlMessage.Endpoint("R1", inst, "NODE.EXAMPLE.", 5302, 5301, NodeRole.RELAY),
                    new ControlMessage.Endpoint("R1", inst, "node.example", 5300, 5302, NodeRole.RELAY),
                    new ControlMessage.Endpoint("R1", inst, "node.example", 5300, 5301, NodeRole.CLIENT));
            for (int i = 0; i < endpoints.size(); i++) {
                var ch = rig.opened(); var q = new ControlMessage(MessageType.REGISTER, UUID.randomUUID(), "R1", inst, new ControlMessage.EndpointPayload(endpoints.get(i)));
                rig.core.message(ch, q); rig.events.drain();
                if (i == 0) assertEquals(MessageType.REGISTER_OK, ch.sent.getFirst().type());
                else assertError(ch, pt.uminho.esr.ott.protocol.ErrorCode.UNAUTHORIZED_NEIGHBOR, q.requestId());
                rig.finish(ch);
            }
            assertNotEquals(BootstrapServer.normalizeHost("localhost"), BootstrapServer.normalizeHost("127.0.0.1"));
            assertNotEquals(BootstrapServer.normalizeHost("::1"), BootstrapServer.normalizeHost("0:0:0:0:0:0:0:1"));
        }
    }

    @Test void coreAllEighteenOtherActiveTypesAreRejectedWithoutSideEffects() throws Exception {
        var names = java.util.List.of("register-ok", "node-leave", "hello", "hello-ok", "probe-ping", "probe-pong", "stream-announce", "stream-withdraw", "activate", "activate-ack", "deactivate", "subscription-renew", "route-trace", "trace-result", "error-no-route", "discovery-query", "discovery-snapshot", "repair-hello-min");
        var seen = new java.util.HashSet<MessageType>();
        try (var rig = new Rig(configuration())) {
            for (var name : names) {
                var m = MessageCodec.decode(java.nio.file.Files.readAllBytes(Path.of("src/test/resources/protocol/v2/tcp-" + name + ".bin")));
                var ch = rig.opened(); rig.core.message(ch, m); rig.events.drain();
                if(m.type()==MessageType.ERROR) { assertTrue(ch.sent.isEmpty());assertFalse(ch.open); }
                else {
                    assertError(ch, pt.uminho.esr.ott.protocol.ErrorCode.INVALID_MESSAGE, m.requestId());
                    assertEquals(m.type().code(), ((ControlMessage.ErrorPayload) ch.sent.getFirst().payload()).relatedType());
                    rig.finish(ch);
                }
                seen.add(m.type());
            }
            assertEquals(18, seen.size()); assertFalse(seen.contains(MessageType.REGISTER));
        }
    }

    @Test void coreTotalDeadlineClosesPendingWriteAndLateEventsCannotAffectAnotherInstance() throws Exception {
        try (var rig = new Rig(configuration())) {
            var old = rig.query("R1"); var oldFuture = old.pending;
            assertNotNull(oldFuture); assertFalse(oldFuture.isDone());
            rig.deadlines.advance(java.time.Duration.ofMillis(2999).toNanos()); rig.events.drain(); assertTrue(old.open);
            rig.core.message(old, request(rig.config, "R1")); rig.events.drain();
            rig.deadlines.advance(java.time.Duration.ofMillis(1).toNanos()); rig.events.drain();
            assertFalse(old.open); assertTrue(oldFuture.isCompletedExceptionally()); assertEquals(0, rig.core.contextCount());
            var next = rig.query("R1"); rig.core.closed(old); oldFuture.complete(null); rig.events.drain();
            assertTrue(next.open); assertEquals(1, rig.core.contextCount()); rig.finish(next);
        }
    }

    @Test void coreQueueDelayAndNoRequestDoNotExtendDeadlineAndWrapIsSupported() throws Exception {
        try (var rig = new Rig(configuration())) {
            var delayed = new FakeChannel(); rig.core.open(delayed);
            rig.clock.addAndGet(java.time.Duration.ofMillis(3001).toNanos()); rig.events.drain();
            assertFalse(delayed.open); assertTrue(rig.deadlines.tasks.isEmpty());
            rig.clock.set(Long.MAX_VALUE - 2);
            var silent = rig.opened(); rig.deadlines.advance(java.time.Duration.ofMillis(3000).toNanos()); rig.events.drain();
            assertFalse(silent.open); assertEquals(0, rig.core.contextCount());
        }
    }

    @Test void coreWriteFailureCancellationAndShutdownRemoveEveryContextAndTicket() throws Exception {
        try (var rig = new Rig(configuration())) {
            var failed = rig.query("S1"); failed.pending.completeExceptionally(new java.io.IOException("Injected write failure")); rig.events.drain();
            assertFalse(failed.open);
            var cancelled = rig.query("R1"); assertTrue(cancelled.pending.cancel(false)); rig.events.drain(); assertFalse(cancelled.open);
            var active = rig.query("R1"); var waiting = rig.opened(); rig.core.requestStop(); rig.events.drain();
            assertFalse(active.open); assertFalse(waiting.open); assertTrue(active.pending.isCompletedExceptionally());
            assertEquals(0, rig.core.contextCount()); assertTrue(rig.deadlines.tasks.isEmpty()); assertTrue(rig.events.stopped);
        }
    }

    @Test void coreSaturationFailsExplicitlyAndCleanupDoesNotNeedAnotherQueueSlot() throws Exception {
        try (var rig = new Rig(configuration(), new ManualEvents(1))) {
            var active = rig.query("R1"); rig.core.message(active, request(rig.config, "R1"));
            rig.core.message(active, request(rig.config, "R1")); // Rejection while the one slot is occupied.
            assertNotNull(rig.core.failure); rig.events.drain();
            assertFalse(active.open); assertTrue(active.pending.isDone());
            assertEquals(0, rig.core.contextCount()); assertTrue(rig.deadlines.tasks.isEmpty());
        }
    }

    @Test void coreInvalidDirectConfigurationsAreRejectedBeforeTransportFactory() throws Exception {
        var b = configuration(); var nodes = new java.util.LinkedHashMap<>(b.topology().orElseThrow().nodes());
        var r = nodes.get("R1"); nodes.put("R1", new pt.uminho.esr.ott.config.TopologyConfiguration.Node("R1", r.host(), r.controlPort(), r.dataPort(), r.role(), java.util.Set.of("S1")));
        var opened = new java.util.concurrent.atomic.AtomicInteger(); var clock = new java.util.concurrent.atomic.AtomicLong();
        var timer = new ManualDeadlines(clock); var events = new ManualEvents();
        for (var invalid : java.util.List.of(copy(b, NodeRole.RELAY, b.topology().orElseThrow().nodes(), b.runtime()), copy(b, NodeRole.BOOTSTRAP, nodes, b.runtime()))) {
            assertThrows(IllegalArgumentException.class, () -> new BootstrapServer(invalid, new InetSocketAddress("127.0.0.1", 0),
                    clock::get, timer, events, (bind, listener, options) -> { opened.incrementAndGet(); throw new AssertionError("Network opened before validation"); }));
        }
        assertEquals(0, opened.get()); assertTrue(timer.tasks.isEmpty()); assertTrue(events.queue.isEmpty());
    }
    private static NodeConfiguration control(NodeConfiguration b, int queue, int max, int queryMs) {
        var r = b.runtime();
        var control = new pt.uminho.esr.ott.config.RuntimeConfiguration.Control(java.time.Duration.ofMillis(111),
                java.time.Duration.ofMillis(2222), max, queue, java.time.Duration.ofMillis(queryMs));
        var runtime = new pt.uminho.esr.ott.config.RuntimeConfiguration(control, r.overlay(), r.monitoring(), r.routing(), r.subscription(), r.video(), r.source());
        return copy(b, NodeRole.BOOTSTRAP, b.topology().orElseThrow().nodes(), runtime);
    }
    private static ControlMessage realQuery(BootstrapServer service, NodeConfiguration config, String id) throws Exception {
        try (var peer = new Socket()) {
            peer.connect(service.localAddress()); peer.setSoTimeout(3000);
            var q = request(config, id); MessageCodec.write(q, peer.getOutputStream()); peer.getOutputStream().flush();
            var reply = MessageCodec.read(peer.getInputStream()).orElseThrow();
            assertEquals(q.requestId(), reply.requestId()); assertEquals(MessageType.REGISTER_OK, reply.type());
            assertTrue(MessageCodec.read(peer.getInputStream()).isEmpty()); return reply;
        }
    }

    @Test void coreControlOverridesLimitContextsTimersAndTotalQueryDeadline() throws Exception {
        var config = control(configuration(), 7, 2, 17);
        try (var rig = new Rig(config)) {
            var first = rig.query("R1"); var second = rig.opened(); var excess = rig.opened();
            assertFalse(excess.open); assertEquals(2, rig.core.contextCount()); assertEquals(2, rig.deadlines.tasks.size());
            rig.deadlines.advance(java.time.Duration.ofMillis(16).toNanos()); rig.events.drain(); assertTrue(first.open); assertTrue(second.open);
            rig.deadlines.advance(java.time.Duration.ofMillis(1).toNanos()); rig.events.drain();
            assertFalse(first.open); assertFalse(second.open); assertTrue(first.pending.isDone());
            assertEquals(0, rig.core.contextCount()); assertTrue(rig.deadlines.peak <= 2);
        }
        var clock = new java.util.concurrent.atomic.AtomicLong(); var timer = new ManualDeadlines(clock); var events = new ManualEvents();
        var captured = new java.util.concurrent.atomic.AtomicReference<pt.uminho.esr.ott.transport.Transport.Options>();
        assertThrows(java.io.IOException.class, () -> new BootstrapServer(config, new InetSocketAddress("127.0.0.1", 0),
                clock::get, timer, events, (bind, listener, options) -> { captured.set(options); throw new java.io.IOException("Sentinel factory"); }));
        events.drain();
        assertEquals(7, captured.get().queueCapacity()); assertEquals(2, captured.get().maxConnections());
        assertEquals(java.time.Duration.ofMillis(111), captured.get().connectTimeout());
        assertEquals(java.time.Duration.ofMillis(2222), captured.get().shutdownTimeout());
        assertTrue(timer.stopped); assertTrue(events.stopped);
    }

    @Test void lexicalNormalizationMatchesExistingLoaderAndRejectsInvalidHosts(@org.junit.jupiter.api.io.TempDir Path temp) throws Exception {
        var source = java.nio.file.Files.readString(Path.of("src/main/resources/config/topology.properties"));
        java.nio.file.Files.copy(Path.of("src/main/resources/config/bootstrap.properties"), temp.resolve("bootstrap.properties"));
        for (String host : java.util.List.of("Node.Example.", "127.000.000.001", "[::1]", "0:0:0:0:0:0:0:1")) {
            java.nio.file.Files.writeString(temp.resolve("topology.properties"), source.replace("node.R1.host=127.0.0.1", "node.R1.host=" + host));
            var loaded = ConfigurationLoader.load(temp.resolve("bootstrap.properties"), NodeRole.BOOTSTRAP);
            assertEquals(loaded.topology().orElseThrow().nodes().get("R1").host(), BootstrapServer.normalizeHost(host));
        }
        for (String host : java.util.List.of("", "bad host", "256.1.2.3", "0.0.0.0", "::", "[::]", "127.0.0.1:5000")) {
            assertThrows(IllegalArgumentException.class, () -> BootstrapServer.normalizeHost(host));
            java.nio.file.Files.writeString(temp.resolve("topology.properties"), source.replace("node.R1.host=127.0.0.1", "node.R1.host=" + host));
            assertThrows(pt.uminho.esr.ott.config.ConfigurationException.class,
                    () -> ConfigurationLoader.load(temp.resolve("bootstrap.properties"), NodeRole.BOOTSTRAP));
        }
    }

    @Test void additionalDirectTopologyViolationsFailBeforeNetworkOrTimers() throws Exception {
        var b = configuration(); var r = b.topology().orElseThrow().nodes().get("R1");
        var invalid = java.util.List.of(
                new pt.uminho.esr.ott.config.TopologyConfiguration.Node("WRONG", r.host(), r.controlPort(), r.dataPort(), r.role(), r.neighbors()),
                new pt.uminho.esr.ott.config.TopologyConfiguration.Node("R1", "0.0.0.0", r.controlPort(), r.dataPort(), r.role(), r.neighbors()),
                new pt.uminho.esr.ott.config.TopologyConfiguration.Node("R1", r.host(), 0, r.dataPort(), r.role(), r.neighbors()),
                new pt.uminho.esr.ott.config.TopologyConfiguration.Node("R1", r.host(), r.controlPort(), r.dataPort(), NodeRole.BOOTSTRAP, r.neighbors()),
                new pt.uminho.esr.ott.config.TopologyConfiguration.Node("R1", r.host(), r.controlPort(), r.dataPort(), r.role(), java.util.Set.of("R1")),
                new pt.uminho.esr.ott.config.TopologyConfiguration.Node("R1", r.host(), r.controlPort(), r.dataPort(), r.role(), java.util.Set.of("missing")),
                new pt.uminho.esr.ott.config.TopologyConfiguration.Node("R1", r.host(), 5100, r.dataPort(), r.role(), r.neighbors()));
        var calls = new java.util.concurrent.atomic.AtomicInteger(); var clock = new java.util.concurrent.atomic.AtomicLong();
        var timer = new ManualDeadlines(clock); var events = new ManualEvents();
        for (var replacement : invalid) {
            var nodes = new java.util.LinkedHashMap<>(b.topology().orElseThrow().nodes()); nodes.put("R1", replacement);
            var config = copy(b, NodeRole.BOOTSTRAP, nodes, b.runtime());
            assertThrows(IllegalArgumentException.class, () -> new BootstrapServer(config, new InetSocketAddress("127.0.0.1", 0),
                    clock::get, timer, events, (bind, listener, opts) -> { calls.incrementAndGet(); throw new AssertionError(); }));
        }
        assertEquals(0, calls.get()); assertTrue(events.queue.isEmpty()); assertTrue(timer.tasks.isEmpty());
    }

    @Test void completeErrorThenEofAndTwoValidRequestsChooseOnlyOneResponse() throws Exception {
        var config = configuration();
        try (var service = new BootstrapServer(config, new InetSocketAddress("127.0.0.1", 0))) {
            try (var peer = new Socket()) {
                peer.connect(service.localAddress()); peer.setSoTimeout(3000);
                var q = new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), "R1", UUID.randomUUID(), new ControlMessage.Probe(1));
                MessageCodec.write(q, peer.getOutputStream()); peer.getOutputStream().flush();
                var error = MessageCodec.read(peer.getInputStream()).orElseThrow();
                assertEquals(MessageType.ERROR, error.type()); assertEquals(q.requestId(), error.requestId());
                assertEquals(pt.uminho.esr.ott.protocol.ErrorCode.INVALID_MESSAGE, ((ControlMessage.ErrorPayload) error.payload()).code());
                assertTrue(MessageCodec.read(peer.getInputStream()).isEmpty());
            }
            try (var peer = new Socket()) {
                peer.connect(service.localAddress()); peer.setSoTimeout(3000);
                var first = request(config, "S1"); MessageCodec.write(first, peer.getOutputStream());
                MessageCodec.write(request(config, "S2"), peer.getOutputStream()); peer.getOutputStream().flush();
                assertEquals(first.requestId(), MessageCodec.read(peer.getInputStream()).orElseThrow().requestId());
                assertTrue(MessageCodec.read(peer.getInputStream()).isEmpty());
            }
        }
    }

    @Test void simultaneousRealTcpQueriesFromDifferentInstancesReturnOnlyConfiguredNeighbors() throws Exception {
        var config = configuration();
        try (var service = new BootstrapServer(config, new InetSocketAddress("127.0.0.1", 0));
             var jobs = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            var tasks = new java.util.ArrayList<java.util.concurrent.Future<ControlMessage>>();
            for (int i = 0; i < 16; i++) {
                String id = i % 2 == 0 ? "R1" : "S1";
                tasks.add(jobs.submit(() -> realQuery(service, config, id)));
            }
            for (int i = 0; i < tasks.size(); i++) {
                var m = tasks.get(i).get(5, java.util.concurrent.TimeUnit.SECONDS);
                var ids = ((ControlMessage.NeighborList) m.payload()).neighbors().stream().map(ControlMessage.Endpoint::nodeId).toList();
                assertEquals(i % 2 == 0 ? java.util.List.of("S1", "S2", "C1") : java.util.List.of("S2", "R1"), ids);
            }
        }
    }

    @Test void malformedFramesAndEarlyCloseDoNotKillBootstrapListener() throws Exception {
        var config = configuration();
        try (var service = new BootstrapServer(config, new InetSocketAddress("127.0.0.1", 0))) {
            for (String name : java.util.List.of("invalid-type", "invalid-length-small", "invalid-identity-utf8", "invalid-trailing-byte", "invalid-short-prefix", "invalid-version-1")) {
                try (var peer = new Socket()) {
                    peer.connect(service.localAddress()); peer.setSoTimeout(3000);
                    peer.getOutputStream().write(java.nio.file.Files.readAllBytes(Path.of("src/test/resources/protocol/v2/tcp-" + name + ".bin")));
                    peer.getOutputStream().flush(); peer.shutdownOutput();
                    try { assertTrue(MessageCodec.read(peer.getInputStream()).isEmpty()); }
                    catch (java.net.SocketException reset) { /* Reset is also a closed malformed connection. */ }
                }
                realQuery(service, config, "R1");
            }
            try (var peer = new Socket()) { peer.connect(service.localAddress()); }
            realQuery(service, config, "S1");
        }
    }

    @Test void silentAndPartialRealTcpPeersExpireAndShutdownReleasesListenerPort() throws Exception {
        var config = control(configuration(), 7, 4, 40);
        try (var service = new BootstrapServer(config, new InetSocketAddress("127.0.0.1", 0))) {
            for (boolean partial : java.util.List.of(false, true)) try (var peer = new Socket()) {
                peer.connect(service.localAddress()); peer.setSoTimeout(3000);
                if (partial) { peer.getOutputStream().write(new byte[]{0, 0}); peer.getOutputStream().flush(); }
                try { assertEquals(-1, peer.getInputStream().read()); }
                catch (java.net.SocketException reset) { /* Timed-out partial connection is closed. */ }
            }
            realQuery(service, config, "C1");
        }
        var idle = new BootstrapServer(configuration(), new InetSocketAddress("127.0.0.1", 0));
        var address = idle.localAddress(); idle.close(); idle.close();
        try (var rebound = new java.net.ServerSocket()) { rebound.bind(address); assertTrue(rebound.isBound()); }
    }
    @Test void D11bVersionOneAndErrorInputsNeverProduceRegistrationOrAnErrorLoop() throws Exception {
        try(var rig=new Rig(configuration())) {
            var ordinary=request(configuration(),"R1");
            var v1=new ControlMessage(1,ordinary.type(),ordinary.requestId(),ordinary.senderId(),ordinary.senderInstance(),ordinary.payload());
            var old=rig.opened();rig.core.message(old,v1);rig.events.drain();assertFalse(old.open);assertTrue(old.sent.isEmpty());
            var error=new ControlMessage(MessageType.ERROR,UUID.randomUUID(),"R1",UUID.randomUUID(),new ControlMessage.ErrorPayload(pt.uminho.esr.ott.protocol.ErrorCode.INVALID_MESSAGE,1,"negative input"));
            var incoming=rig.opened();rig.core.message(incoming,error);rig.events.drain();assertFalse(incoming.open);assertTrue(incoming.sent.isEmpty());
            var valid=rig.opened();rig.core.message(valid,ordinary);rig.events.drain();assertEquals(2,valid.sent.getFirst().version());assertEquals(MessageType.REGISTER_OK,valid.sent.getFirst().type());rig.finish(valid);
        }
    }

}
