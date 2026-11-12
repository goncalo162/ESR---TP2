package pt.uminho.esr.ott.overlay;

import org.junit.jupiter.api.Test;
import pt.uminho.esr.ott.monitoring.NetworkMonitor;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.net.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.transport.Connection;
import static pt.uminho.esr.ott.overlay.NeighborManagerTest.until;
import static org.junit.jupiter.api.Assertions.*;

/** Composition, real loopback TCP, JVM smoke and physical teardown; not a human manual. */
class OverlayNodeMonitoringTest {
    @Test void snapshotBeforeStartIsEmptyAndDoesNotConstructTheMonitor() {
        int[] calls = {0};
        try (var node = new OverlayNode(NeighborManagerTest.config("A", 19001), new UUID(0, 1), e -> {},
                (c, id, owner, control, observer) -> {
                    calls[0]++; return new NetworkMonitor(c, id, owner, control, observer);
                })) {
            var snapshot = assertDoesNotThrow(node::monitoringSnapshot);
            assertTrue(snapshot.links().isEmpty()); assertEquals(0, calls[0]);
        }
    }
    @Test void snapshotAfterCloseBeforeStartRemainsReadableAndEmpty() {
        var node = new OverlayNode(NeighborManagerTest.config("A", 19001), new UUID(0, 1), e -> {});
        node.close();
        var snapshot = assertDoesNotThrow(node::monitoringSnapshot);
        assertTrue(snapshot.links().isEmpty());
    }

    static final class Rig implements AutoCloseable {
        final NeighborManagerTest.Deployment fixtures;
        final List<OverlayNode> nodes = new ArrayList<>();
        final List<List<NeighborManager.Event>> observed = new ArrayList<>();
        final List<NetworkMonitor.ControlPort> ports = new ArrayList<>();
        final List<NeighborManager.SerialOwner> owners = new ArrayList<>();
        final List<NetworkMonitor> monitors = new ArrayList<>();
        final List<UUID> instances = new ArrayList<>();
        final List<List<ControlMessage>> sent = new ArrayList<>();
        Rig() throws Exception {
            fixtures = new NeighborManagerTest.Deployment();
            for (var config : fixtures.configs) {
                var log = new CopyOnWriteArrayList<NeighborManager.Event>(); observed.add(log);
                var writes = new CopyOnWriteArrayList<ControlMessage>(); sent.add(writes);
                UUID instance = UUID.randomUUID(); instances.add(instance);
                nodes.add(new OverlayNode(config, instance, log::add, (c, id, owner, port, observer) -> {
                    assertSame(config, c); assertEquals(instance, id); assertEquals(c.runtime().overlay().eventsCapacity(), owner.capacity());
                    owners.add(owner); ports.add(port);
                    var monitored = new NetworkMonitor(c, id, owner, new NetworkMonitor.ControlPort() {
                        public CompletionStage<Connection.SendResult> send(Neighbor.Session session, ControlMessage message) {
                            assertTrue(owner.isOwnerThread()); writes.add(message); return port.send(session, message);
                        }
                        public CompletionStage<Boolean> disconnect(Neighbor.Session session) {
                            assertTrue(owner.isOwnerThread()); return port.disconnect(session);
                        }
                    }, observer);
                    monitors.add(monitored); return monitored;
                }));
            }
        }
        void start() throws Exception {
            for (var n : nodes) n.start();
            for (var n : nodes) n.start().toCompletableFuture().get(3, TimeUnit.SECONDS);
            until(() -> nodes.stream().allMatch(n -> n.snapshot().size() == 2));
        }
        public void close() {
            RuntimeException error = null;
            for (var n : nodes) try { n.close(); } catch (RuntimeException e) { error = e; }
            try { fixtures.close(); } finally { if (error != null) throw error; }
        }
    }
    @Test @org.junit.jupiter.api.Timeout(20) void realTcpUsesSameOwnerV2ProbesAndEofInvalidatesCurrentLink() throws Exception {
        try (var rig = new Rig()) {
            rig.start();
            until(() -> rig.nodes.stream().allMatch(n -> n.monitoringSnapshot().links().size() == 2
                    && n.monitoringSnapshot().links().values().stream().allMatch(m -> m.sampleCount() > 0)));
            for (int i = 0; i < 3; i++) {
                assertEquals(rig.nodes.get(i).snapshot().keySet(), rig.nodes.get(i).monitoringSnapshot().links().keySet());
                assertTrue(rig.sent.get(i).stream().anyMatch(m -> m.type() == MessageType.PROBE_PING));
                assertTrue(rig.sent.get(i).stream().anyMatch(m -> m.type() == MessageType.PROBE_PONG));
                assertTrue(rig.sent.get(i).stream().allMatch(m -> m.version() == 2));
                var link = rig.nodes.get(i).snapshot().values().iterator().next();
                var wrong = new Neighbor.Session(link.nodeId(), link.instanceId(), UUID.randomUUID(), link.endpoint());
                assertEquals(Connection.SendResult.CLOSED, rig.ports.get(i).send(wrong,
                    new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), rig.fixtures.configs.get(i).nodeId(),
                        rig.instances.get(i), new ControlMessage.Probe(99))).toCompletableFuture().get(1, TimeUnit.SECONDS));
            }
            var survivor = rig.nodes.get(0); var old = survivor.snapshot().get("N1"); rig.nodes.get(1).close();
            until(() -> !survivor.monitoringSnapshot().links().containsKey("N1"));
            assertTrue(rig.observed.get(0).stream().anyMatch(e -> e instanceof NeighborManager.NeighborDown d && d.session().equals(old)));
            assertTrue(survivor.failure().isEmpty());
        }
    }
    @Test @org.junit.jupiter.api.Timeout(20) void allOriginalEventsKeepIdentityAndOrderIncludingErrorAndRepair() throws Exception {
        try (var rig = new Rig()) {
            rig.start(); var node = rig.nodes.get(0); var session = node.snapshot().values().iterator().next();
            var error = new ControlMessage(MessageType.ERROR, UUID.randomUUID(), session.nodeId(), session.instanceId(),
                    new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE, 9, "fixture"));
            var diagnostic = new NeighborManager.Diagnostic(NeighborManager.Phase.READY, NeighborManager.Reason.STALE,
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            var source = new CandidateCache.SourceKey(session.nodeId(), session.instanceId(), session.connectionId(), UUID.randomUUID());
            var repair = new NeighborManager.RepairStarted(new OverlayRecovery.RepairAttempt(UUID.randomUUID(), rig.instances.get(0),
                    1, source, "OTHER", 1, 0, 1000, 0));
            var received = new NeighborManager.Received(session, error);
            var events = List.<NeighborManager.Event>of(diagnostic, repair, received,
                    new NeighborManager.NeighborUp(session),
                    new NeighborManager.NeighborDown(session, NeighborManager.Reason.TRANSPORT));
            var dispatch = OverlayNode.class.getDeclaredMethod("onNeighborEvent", NeighborManager.Event.class); dispatch.setAccessible(true);
            var done = new CompletableFuture<Void>();
            assertTrue(rig.owners.get(0).offer(() -> {
                try { for (var event : events) dispatch.invoke(node, event); done.complete(null); }
                catch (Exception e) { done.completeExceptionally(e); }
            }));
            done.get(2, TimeUnit.SECONDS); var log = rig.observed.get(0); int last = -1;
            for (var event : events) {
                assertEquals(1, log.stream().filter(e -> e == event).count());
                int index = -1;
                for (int i = 0; i < log.size(); i++) if (log.get(i) == event) { index = i; break; }
                assertTrue(index > last); last = index;
            }
            assertTrue(rig.sent.get(0).stream().noneMatch(m -> m.type() == MessageType.ERROR));
            assertFalse(node.monitoringSnapshot().links().containsKey(session.nodeId()));
            // Actual ERROR delivery over the existing TCP control port remains observable.
            var remoteSession = rig.nodes.get(1).snapshot().get("N0");
            var realError = new ControlMessage(MessageType.ERROR, UUID.randomUUID(), "N1", rig.instances.get(1),
                    new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE, 9, "real tcp"));
            assertEquals(Connection.SendResult.ACCEPTED, rig.ports.get(1).send(remoteSession, realError).toCompletableFuture().get(1, TimeUnit.SECONDS));
            until(() -> log.stream().anyMatch(e -> e instanceof NeighborManager.Received r && r.message().requestId().equals(realError.requestId())));
        }
    }
    @Test @org.junit.jupiter.api.Timeout(20) void tapFailureStillDeliversTheOriginalEventAndRollsBackResources() throws Exception {
        try (var rig = new Rig()) {
            rig.start(); var node = rig.nodes.get(0); var event = new NeighborManager.Diagnostic(NeighborManager.Phase.READY,
                    NeighborManager.Reason.STALE, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            var dispatch = OverlayNode.class.getDeclaredMethod("onNeighborEvent", NeighborManager.Event.class); dispatch.setAccessible(true);
            // Deliberately invoke off-owner: the monitor rejects it; finally must still deliver the original event.
            dispatch.invoke(node, event);
            assertEquals(1, rig.observed.get(0).stream().filter(e -> e == event).count()); assertTrue(node.failure().isPresent());
            assertTrue(node.awaitTermination(Duration.ofSeconds(3)));
            assertTrue(rig.monitors.get(0).awaitTermination(Duration.ZERO));
        }
    }
    @Test @org.junit.jupiter.api.Timeout(20) void shutdownSharesOriginalDeadlineAndPreservesGracefulNodeLeave() throws Exception {
        try (var rig = new Rig()) {
            rig.start(); var node = rig.nodes.get(0); var initial = node.requestShutdown(0, Duration.ofSeconds(2));
            assertSame(initial, node.requestShutdown(1, Duration.ofSeconds(30)));
            var report = initial.toCompletableFuture().get(2, TimeUnit.SECONDS);
            assertEquals(2, report.attempts().size());
            assertTrue(report.attempts().stream().allMatch(a -> a.outcome() == NeighborManager.LeaveOutcome.LOCAL_WRITE_CONFIRMED));
            assertTrue(node.awaitTermination(Duration.ofSeconds(2)));
            assertTrue(rig.monitors.get(0).awaitTermination(Duration.ZERO)); assertTrue(rig.owners.get(0).awaitTermination(Duration.ZERO));
            assertTrue(node.monitoringSnapshot().links().isEmpty()); node.close();
        }
    }
    @Test @org.junit.jupiter.api.Timeout(10) void partialFactoryFailureAndImmediateCloseCannotLeaveOwnerAlive() throws Exception {
        var owner = new AtomicReference<NeighborManager.SerialOwner>();
        var node = new OverlayNode(NeighborManagerTest.config("A", 19001), UUID.randomUUID(), e -> {}, (c, id, serial, port, observer) -> {
            owner.set(serial); throw new IllegalStateException("factory fixture");
        });
        assertThrows(ExecutionException.class, () -> node.start().toCompletableFuture().get(2, TimeUnit.SECONDS));
        assertTrue(node.awaitTermination(Duration.ofSeconds(2))); assertTrue(owner.get().awaitTermination(Duration.ZERO)); node.close();
        try (var rig = new Rig()) {
            var n = rig.nodes.get(0); n.start(); n.close(); assertTrue(n.awaitTermination(Duration.ZERO));
            assertTrue(rig.monitors.get(0).awaitTermination(Duration.ZERO));
        }
    }
    @Test @org.junit.jupiter.api.Timeout(20) void closeInsideOriginalObserverDoesNotSelfJoinOrHideItsEvent() throws Exception {
        try (var fixtures = new NeighborManagerTest.Deployment()) {
            var ref = new AtomicReference<OverlayNode>(); var closed = new CountDownLatch(1); var seen = new CopyOnWriteArrayList<NeighborManager.Event>();
            var node = new OverlayNode(fixtures.configs.get(0), UUID.randomUUID(), event -> {
                seen.add(event); if (event instanceof NeighborManager.NeighborUp) { ref.get().close(); closed.countDown(); }
            }); ref.set(node);
            try {
                fixtures.managers.get(1).start(); fixtures.managers.get(2).start(); node.start();
                assertTrue(closed.await(5, TimeUnit.SECONDS)); assertTrue(node.awaitTermination(Duration.ofSeconds(2)));
                assertTrue(seen.stream().anyMatch(e -> e instanceof NeighborManager.NeighborUp));
            } finally { node.close(); }
        }
    }
    @Test @org.junit.jupiter.api.Timeout(30) void repairedHello20SessionGetsFreshMetricsWithoutBootstrapOrNewTransport() throws Exception {
        try (var graph = new NeighborDiscoveryTest.Graph(false)) {
            var nodes = new LinkedHashMap<String, OverlayNode>(); var log = new CopyOnWriteArrayList<NeighborManager.Event>();
            for (var entry : graph.configs.entrySet()) nodes.put(entry.getKey(), new OverlayNode(entry.getValue(), UUID.randomUUID(), log::add));
            try {
                for (var node : nodes.values()) node.start();
                for (var node : nodes.values()) node.start().toCompletableFuture().get(3, TimeUnit.SECONDS);
                until(() -> nodes.entrySet().stream().allMatch(e -> e.getValue().snapshot().size() == graph.edges.get(e.getKey()).size()));
                until(() -> nodes.get("C").candidates().toCompletableFuture().join().stream().anyMatch(c -> c.nodeId().equals("S"))
                        && nodes.get("S").candidates().toCompletableFuture().join().stream().anyMatch(c -> c.nodeId().equals("C")));
                graph.bootstrap.close();
                assertFalse(nodes.get("C").monitoringSnapshot().links().containsKey("S"), "candidate alone is not a session");
                nodes.get("R1").close(); nodes.get("R2").close();
                until(() -> nodes.get("C").snapshot().containsKey("S") && nodes.get("S").snapshot().containsKey("C"));
                until(() -> nodes.get("C").monitoringSnapshot().links().containsKey("S")
                        && nodes.get("C").monitoringSnapshot().links().get("S").sampleCount() > 0);
                assertEquals(nodes.get("C").snapshot().get("S"), nodes.get("C").monitoringSnapshot().links().get("S").session());
                assertTrue(log.stream().anyMatch(e -> e instanceof NeighborManager.RepairStarted));
                assertTrue(nodes.get("C").failure().isEmpty()); assertTrue(nodes.get("S").failure().isEmpty());
            } finally { for (var node : nodes.values()) node.close(); }
        }
    }
    @Test @org.junit.jupiter.api.Timeout(20) void realCliSubprocessUsesTheNormalCompositionAndExitsCleanly(@org.junit.jupiter.api.io.TempDir Path temp) throws Exception {
        try (var rig = new Rig()) {
            rig.nodes.get(1).start(); rig.nodes.get(2).start();
            var config = rig.fixtures.configs.get(0); var profile = temp.resolve("node.properties");
            Files.writeString(profile, "node.id=N0\nnode.role=RELAY\nbind.host=127.0.0.1\nadvertised.host=127.0.0.1\ncontrol.port="
                    + config.controlPort() + "\ndata.port=" + config.dataPort() + "\nbootstrap.host=127.0.0.1\nbootstrap.port="
                    + config.bootstrap().orElseThrow().port() + "\n", StandardCharsets.UTF_8);
            var log = temp.resolve("cli.log");
            var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp",
                    System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                    "pt.uminho.esr.ott.app.OttApplication", "--role", "RELAY", "--config", profile.toString())
                    .redirectErrorStream(true).redirectOutput(log.toFile()).start();
            try {
                until(() -> rig.nodes.get(1).monitoringSnapshot().links().containsKey("N0")
                        && rig.nodes.get(1).monitoringSnapshot().links().get("N0").sampleCount() > 0);
                process.getOutputStream().write("neighbors\nquit\n".getBytes(StandardCharsets.UTF_8)); process.getOutputStream().close();
                assertTrue(process.waitFor(5, TimeUnit.SECONDS)); assertEquals(0, process.exitValue(), Files.readString(log));
                var output = Files.readString(log);
                assertTrue(output.contains("STARTED node=N0")); assertTrue(output.contains("NEIGHBORS node=N0"));
                assertTrue(output.contains("SHUTDOWN node=N0 phase=closing-output"));
                String evidence = System.getProperty("ott.test.evidence");
                if (evidence != null) {
                    Path destination = Path.of(evidence); Files.createDirectories(destination);
                    Files.copy(log, destination.resolve("cli-smoke.log"), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(profile, destination.resolve("cli-profile.properties"), StandardCopyOption.REPLACE_EXISTING);
                    Files.writeString(destination.resolve("cli-result.txt"), "JAVA_EXIT=" + process.exitValue() + "\nPROCESS_TERMINATED=true\n");
                }
            } finally {
                if (process.isAlive()) { process.destroy(); if (!process.waitFor(2, TimeUnit.SECONDS)) process.destroyForcibly(); }
                assertTrue(process.waitFor(2, TimeUnit.SECONDS));
            }
        }
    }

    @Test @org.junit.jupiter.api.Timeout(15) void heldMonitorAdmissionUsesOnlyTheOriginalGlobalCleanupBudget() throws Exception {
        try (var fixtures = new NeighborManagerTest.Deployment()) {
            var release = new CompletableFuture<Void>(); var sends = new AtomicInteger();
            var mon = new AtomicReference<NetworkMonitor>();
            var node = new OverlayNode(fixtures.configs.get(0), UUID.randomUUID(), e -> {}, (c, id, owner, port, observer) -> {
                var monitor = new NetworkMonitor(c, id, owner, new NetworkMonitor.ControlPort() {
                    public CompletionStage<Connection.SendResult> send(Neighbor.Session session, ControlMessage message) {
                        sends.incrementAndGet(); return port.send(session, message).thenCombine(release, (result, ignored) -> result);
                    }
                    public CompletionStage<Boolean> disconnect(Neighbor.Session session) { return port.disconnect(session); }
                }, observer); mon.set(monitor); return monitor;
            });
            try {
                fixtures.managers.get(1).start(); fixtures.managers.get(2).start(); node.start().toCompletableFuture().get(3, TimeUnit.SECONDS);
                until(() -> sends.get() > 0);
                node.requestShutdown(0, Duration.ofMillis(300)).toCompletableFuture().get(1, TimeUnit.SECONDS);
                long began = System.nanoTime(); assertFalse(node.awaitTermination(Duration.ofSeconds(5)));
                assertTrue(System.nanoTime() - began < TimeUnit.SECONDS.toNanos(1), "no extra timeout per participant");
                began = System.nanoTime(); assertFalse(node.awaitTermination(Duration.ofSeconds(5)));
                assertTrue(System.nanoTime() - began < TimeUnit.MILLISECONDS.toNanos(200), "repetition cannot renew the original budget");
                assertFalse(mon.get().awaitTermination(Duration.ZERO));
            } finally {
                release.complete(null); until(() -> node.awaitTermination(Duration.ZERO)); node.close();
            }
        }
    }
}
