package pt.uminho.esr.ott.monitoring;

import org.junit.jupiter.api.Test;
import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.Connection;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Contract oracles with fake clock/control; explicit scheduler tests join the real helper. */
class NetworkMonitorTest {
    static final UUID LOCAL = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID REMOTE = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static NodeConfiguration config() {
        return new NodeConfiguration(Path.of("monitor-fixture.properties"), "A", NodeRole.RELAY,
                "127.0.0.1", "127.0.0.1", 19001, 19101,
                Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1", 19000)),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), List.of());
    }
    static Neighbor.Session session(long connection) {
        return new Neighbor.Session("B", REMOTE, new UUID(0, connection),
                new ControlMessage.Endpoint("B", REMOTE, "127.0.0.1", 19002, 19102, NodeRole.RELAY));
    }
    static LinkMetrics measured(long at) {
        return new LinkMetrics(session(10), LinkMetrics.State.MEASURED,
                OptionalDouble.of(1200), OptionalLong.of(1200), 2, false, OptionalLong.of(at), 0);
    }
    static final class Owner implements NeighborManager.SerialOwner {
        final ArrayDeque<Runnable> queue = new ArrayDeque<>();
        final List<Runnable> cleanup = new ArrayList<>();
        boolean stopped, inside = true, reject; int limit = 4096; volatile Runnable afterOffer = () -> {};
        public int capacity() { return limit; }
        public boolean isOwnerThread() { return inside; }
        public boolean offer(Runnable r) { if (stopped || reject || queue.size() == capacity()) return false; queue.add(r); afterOffer.run(); return true; }
        public NeighborManager.Registration onStop(Runnable r) { cleanup.add(r); return () -> cleanup.remove(r); }
        public void fail(Throwable error) { requestClose(); }
        public void requestClose() { stopped = true; queue.clear(); List.copyOf(cleanup).forEach(Runnable::run); }
        public boolean awaitTermination(Duration budget) { return stopped; }
        public void close() { requestClose(); }
        void drain() { int guard = 10000; while (!queue.isEmpty()) { assertTrue(--guard > 0, "No owner spin"); queue.remove().run(); } }
    }
    static final class Timers implements NetworkMonitor.Scheduler {
        final class Job implements NetworkMonitor.Ticket {
            final Runnable task; final long due; boolean canceled;
            Job(Runnable task, long due) { this.task = task; this.due = due; }
            public void cancel() { canceled = true; jobs.remove(this); }
        }
        final List<Job> jobs = new ArrayList<>(); long now; boolean stopped, reject;
        public NetworkMonitor.Ticket schedule(Runnable task, long delay) {
            assertTrue(delay >= 0); if (stopped || reject) throw new RejectedExecutionException();
            Job j = new Job(task, now + delay); jobs.add(j); return j;
        }
        public void requestClose() { stopped = true; jobs.clear(); }
        public boolean awaitTermination(Duration budget) { return stopped; }
        void advance(long nanos) {
            now += nanos;
            var due = jobs.stream().filter(j -> j.due <= now).toList();
            jobs.removeAll(due); due.stream().filter(j -> !j.canceled).forEach(j -> j.task.run());
        }
    }
    static final class Port implements NetworkMonitor.ControlPort {
        record Sent(Neighbor.Session session, ControlMessage message, CompletableFuture<Connection.SendResult> admission) { }
        final List<Sent> sent = new ArrayList<>(); final List<Neighbor.Session> closed = new ArrayList<>();
        boolean hold, holdClose, throwSend, failSend, failClose;
        Connection.SendResult result = Connection.SendResult.ACCEPTED;
        final List<CompletableFuture<Boolean>> closeFutures = new ArrayList<>();
        public CompletionStage<Connection.SendResult> send(Neighbor.Session session, ControlMessage message) {
            if (throwSend) throw new IllegalStateException("local send failure");
            var future = new CompletableFuture<Connection.SendResult>(); sent.add(new Sent(session, message, future));
            if (failSend) future.completeExceptionally(new IllegalStateException("admission failed"));
            else if (!hold) future.complete(result); return future;
        }
        public CompletionStage<Boolean> disconnect(Neighbor.Session session) {
            closed.add(session); var f = new CompletableFuture<Boolean>(); closeFutures.add(f);
            if (failClose) f.completeExceptionally(new IllegalStateException("close failed"));
            else if (!holdClose) f.complete(true); return f;
        }
    }
    static final class Fixture {
        final Owner owner = new Owner(); final Timers timers = new Timers(); final Port port = new Port();
        final List<NetworkMonitor.Event> events = new ArrayList<>(); long nextId;
        final NetworkMonitor monitor;
        Fixture() { this(config(), 0, null); }
        Fixture(NodeConfiguration config, long now, java.util.function.LongSupplier ids) {
            owner.limit = config.runtime().overlay().eventsCapacity(); timers.now = now;
            monitor = new NetworkMonitor(config, LOCAL, owner, port, events::add,
                    () -> timers.now, timers, ids == null ? () -> nextId++ : ids);
        }
        void up(Neighbor.Session s) { monitor.start(); monitor.onEvent(new NeighborManager.NeighborUp(s)); owner.drain(); timers.advance(0); owner.drain(); }
        Port.Sent ping() { assertFalse(port.sent.isEmpty(), "Up must make the first probe eligible"); return port.sent.getLast(); }
        void pong(Port.Sent ping) {
            monitor.onEvent(new NeighborManager.Received(ping.session(), new ControlMessage(MessageType.PROBE_PONG,
                    ping.message().requestId(), "B", REMOTE, ping.message().payload()))); owner.drain();
        }
        void advance(long nanos) { timers.advance(nanos); owner.drain(); }
        boolean terminated() { owner.inside = false; try { return monitor.awaitTermination(Duration.ZERO); } finally { owner.inside = true; } }
        LinkMetrics metricsFor(String id) { return monitor.snapshot().links().get(id); }
        LinkMetrics metrics() { var m = monitor.snapshot().links().get("B"); assertNotNull(m, "Current session must be published"); return m; }
    }
    @Test void d02AgeUsesLocalSubtractionAndAllowsNegativeTimestamp() {
        assertEquals(OptionalLong.of(250), assertDoesNotThrow(() -> measured(-500).sampleAgeNanos(-250)));
    }
    @Test void d16RejectsAFalseReliableZeroCost() {
        assertThrows(IllegalArgumentException.class, () -> new LinkMetrics(session(10), LinkMetrics.State.MEASURED,
                OptionalDouble.of(0), OptionalLong.of(0), 1, false, OptionalLong.of(0), 0));
    }
    @Test void d16SnapshotMakesADefensiveCopy() {
        var f = new Fixture(); var map = new HashMap<String, LinkMetrics>(); map.put("B", measured(0));
        var snapshot = new NetworkMonitor.Snapshot(0, map, f.monitor.snapshot().diagnostics()); map.clear();
        assertEquals(1, snapshot.links().size()); assertThrows(UnsupportedOperationException.class, snapshot.links()::clear);
    }
    @Test void d04UpStartsOneV2ProbeAndNoReliableZero() {
        var f = new Fixture(); f.up(session(10)); var ping = f.ping();
        assertEquals(2, ping.message().version()); assertEquals(MessageType.PROBE_PING, ping.message().type());
        assertEquals(LOCAL, ping.message().senderInstance()); assertEquals(1, f.port.sent.size());
        assertTrue(f.metrics().costMicros().isEmpty());
    }
    @Test void d02FirstRttAndEwmaUseIndependentExpectedValues() {
        var f = new Fixture(); f.up(session(10)); var first = f.ping(); f.advance(1_000_000); f.pong(first);
        assertEquals(1000.0, f.metrics().ewmaMicros().orElseThrow());
        f.advance(999_000_000); var second = f.ping(); assertNotEquals(first.message().requestId(), second.message().requestId());
        f.advance(2_000_000); f.pong(second);
        assertEquals(1200.0, f.metrics().ewmaMicros().orElseThrow(), 0.000001);
        assertEquals(1200L, f.metrics().costMicros().orElseThrow());
    }
    @Test void d05EarlyPongWaitsForAdmissionAndKeepsCapturedTime() {
        var f = new Fixture(); f.port.hold = true; f.up(session(10)); var ping = f.ping();
        f.advance(1_200_000); f.pong(ping); assertTrue(f.metrics().ewmaMicros().isEmpty());
        f.advance(50_000_000); ping.admission().complete(Connection.SendResult.ACCEPTED); f.owner.drain();
        assertEquals(1200.0, f.metrics().ewmaMicros().orElseThrow(), .000001);
    }
    @Test void d06RefusedAdmissionDiscardsEarlyPongWithoutRemoteLoss() {
        var f = new Fixture(); f.port.hold = true; f.up(session(10)); var ping = f.ping();
        f.advance(1_000_000); f.pong(ping); ping.admission().complete(Connection.SendResult.CLOSED); f.owner.drain();
        assertTrue(f.monitor.snapshot().links().isEmpty()); assertEquals(List.of(session(10)), f.port.closed);
        assertTrue(f.events.stream().noneMatch(e -> e instanceof NetworkMonitor.Invalidated i && i.reason() == NetworkMonitor.Reason.TIMEOUT));
    }
    @Test void d07ExactDeadlineCannotBeReclassifiedAsGoodPong() {
        var f = new Fixture(); f.up(session(10)); var ping = f.ping();
        f.advance(1_000_000_000); f.pong(ping);
        assertEquals(1, f.metrics().missedCount()); assertTrue(f.metrics().ewmaMicros().isEmpty());
    }
    @Test void d03NewConnectionSameInstanceResetsAllMetrics() {
        var f = new Fixture(); f.up(session(10)); var old = f.ping(); f.advance(1_000_000); f.pong(old);
        f.up(session(11)); f.pong(old); var m = f.metrics();
        assertEquals(session(11), m.session()); assertEquals(0, m.sampleCount());
        assertEquals(0, m.missedCount()); assertTrue(m.costMicros().isEmpty());
    }
    @Test void d08WrongRequestCannotCompleteCurrentProbe() {
        var f = new Fixture(); f.up(session(10)); var ping = f.ping(); f.advance(1_000_000);
        f.monitor.onEvent(new NeighborManager.Received(session(10), new ControlMessage(MessageType.PROBE_PONG,
                new UUID(8, 8), "B", REMOTE, ping.message().payload())));
        assertTrue(f.metrics().ewmaMicros().isEmpty()); f.pong(ping); assertEquals(1, f.metrics().sampleCount());
    }
    @Test void d09ThreeAdmittedTimeoutsCloseOnlyExpectedSession() {
        var f = new Fixture(); f.up(session(10)); f.ping();
        f.advance(1_000_000_000); assertTrue(f.port.closed.isEmpty());
        f.advance(1_000_000_000); assertTrue(f.port.closed.isEmpty());
        f.advance(1_000_000_000); assertEquals(List.of(session(10)), f.port.closed);
        assertTrue(f.monitor.snapshot().links().isEmpty());
    }
    @Test void d09DownInvalidatesImmediatelyWithoutThreeTimeouts() {
        var f = new Fixture(); f.up(session(10));
        f.monitor.onEvent(new NeighborManager.NeighborDown(session(10), NeighborManager.Reason.TRANSPORT)); f.owner.drain();
        assertTrue(f.monitor.snapshot().links().isEmpty());
        assertTrue(f.events.stream().anyMatch(e -> e instanceof NetworkMonitor.Invalidated i && i.session().equals(session(10))));
    }
    @Test void d10EchoPreservesRequestAndProbeIds() {
        var f = new Fixture(); f.up(session(10)); f.ping(); var request = new UUID(7, 9);
        f.monitor.onEvent(new NeighborManager.Received(session(10), new ControlMessage(MessageType.PROBE_PING,
                request, "B", REMOTE, new ControlMessage.Probe(42)))); f.owner.drain(); var echo = f.port.sent.getLast().message();
        assertEquals(MessageType.PROBE_PONG, echo.type()); assertEquals(request, echo.requestId());
        assertEquals(new ControlMessage.Probe(42), echo.payload()); assertEquals(LOCAL, echo.senderInstance());
    }
    @Test void d15LogicalCleanupDoesNotClaimPhysicalCompletionWithHeldAdmission() {
        var f = new Fixture(); f.port.hold = true; f.up(session(10)); var ping = f.ping();
        var closing = assertDoesNotThrow(f.monitor::requestClose); f.owner.drain();
        assertTrue(closing.toCompletableFuture().isDone());
        assertFalse(assertDoesNotThrow(f::terminated));
        ping.admission().complete(Connection.SendResult.CLOSED); f.owner.drain();
        assertTrue(assertDoesNotThrow(f::terminated));
        assertTrue(f.monitor.snapshot().links().isEmpty());
    }

    static NodeConfiguration options(long intervalMs, long timeoutMs, int failures, double alpha, int capacity, int degree) {
        var c = config(); var r = c.runtime();
        var runtime = new pt.uminho.esr.ott.config.RuntimeConfiguration(
                new pt.uminho.esr.ott.config.RuntimeConfiguration.Control(r.control().connectTimeout(),
                    r.control().shutdownTimeout(), degree, r.control().queueCapacity(), r.control().bootstrapQueryTimeout()),
                new pt.uminho.esr.ott.config.RuntimeConfiguration.Overlay(r.overlay().helloTimeout(),
                    r.overlay().retryInitial(), r.overlay().retryMax(), r.overlay().retryJitterPercent(), capacity),
                new pt.uminho.esr.ott.config.RuntimeConfiguration.Monitoring(Duration.ofMillis(intervalMs), Duration.ofMillis(timeoutMs), failures, alpha),
                r.routing(), r.subscription(), r.video(), r.source());
        return new NodeConfiguration(c.sourceFile(), c.nodeId(), c.role(), c.bindHost(), c.advertisedHost(),
                c.controlPort(), c.dataPort(), c.bootstrap(), c.topologyFile(), c.streamsFile(), c.mediaBase(), c.topology(), c.streams(), runtime);
    }
    @Test void d01ValidatesOfflineIdentityAndBorrowedOwnerCapacity() {
        var f = new Fixture();
        assertThrows(IllegalArgumentException.class, () -> new NetworkMonitor(config(), new UUID(0, 0), f.owner, f.port, e -> {}));
        f.owner.limit = 1;
        assertThrows(IllegalArgumentException.class, () -> new NetworkMonitor(config(), LOCAL, f.owner, f.port, e -> {}));
        assertTrue(f.port.sent.isEmpty()); assertTrue(f.timers.jobs.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> options(0, 1, 1, .2, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> options(1, 1, 1, Double.NaN, 1, 1));
    }
    @Test void m01m02ThirdEwmaAndAlphaOneOverride() {
        for (double alpha : new double[]{.2, 1}) {
            var f = new Fixture(options(1000, 1000, 3, alpha, 4096, 64), 0, null);
            f.up(session(10)); var ping = f.ping(); f.advance(1_000_000); f.pong(ping);
            f.advance(999_000_000); ping = f.ping(); f.advance(2_000_000); f.pong(ping);
            f.advance(998_000_000); ping = f.ping(); f.advance(3_000_000); f.pong(ping);
            assertEquals(alpha == 1 ? 3000 : 1560, f.metrics().ewmaMicros().orElseThrow(), .000001);
            assertEquals(3, f.metrics().sampleCount());
        }
    }
    @Test void m03SubMicrosecondAndFractionalSamplesNeverPublishReliableZero() {
        for (long nanos : new long[]{0, 200, 1200}) {
            var f = new Fixture(); f.up(session(10)); var ping = f.ping(); f.advance(nanos); f.pong(ping);
            assertEquals(nanos == 1200 ? 2 : 1, f.metrics().costMicros().orElseThrow());
            assertEquals(nanos == 1200 ? 1.2 : 1, f.metrics().ewmaMicros().orElseThrow(), .000001);
        }
    }
    @Test void d04m04CadenceHandlesBothIntervalRelationsAndDelayedTicksWithoutBurst() {
        for (long interval : new long[]{20, 2000}) {
            var f = new Fixture(options(interval, 1000, 5, .2, 4096, 64), 0, null);
            f.up(session(10)); var ping = f.ping();
            for (int i = 0; i < 5; i++) f.advance(1_000_000);
            assertEquals(1, f.port.sent.size());
            f.pong(ping); f.advance((interval - 5) * 1_000_000);
            assertEquals(2, f.port.sent.size());
            f.advance(10_000_000_000L); assertEquals(3, f.port.sent.size(), "one replacement after a late tick");
            assertEquals(1, f.metrics().missedCount(), "one expired admitted probe, not elapsed ticks");
        }
    }
    @Test void d07AdmissionCanOutliveDeadlineWithoutFabricatingLossOrSpinningTimer() {
        var f = new Fixture(); f.port.hold = true; f.up(session(10)); var ping = f.ping();
        f.advance(5_000_000_000L); assertEquals(0, f.metrics().missedCount());
        assertTrue(f.timers.jobs.isEmpty()); assertEquals(1, f.port.sent.size());
        ping.admission().complete(Connection.SendResult.ACCEPTED); f.owner.drain();
        assertEquals(1, f.metrics().missedCount()); assertEquals(2, f.port.sent.size());
    }
    @Test void d06AllRefusalsAndExceptionalAdmissionsDiscardEarlyReplies() {
        for (var result : new Connection.SendResult[]{Connection.SendResult.CLOSED, Connection.SendResult.OVERFLOW, Connection.SendResult.INVALID_MESSAGE}) {
            var f = new Fixture(); f.port.hold = true; f.up(session(10)); var p = f.ping(); f.pong(p);
            p.admission().complete(result); f.owner.drain();
            assertTrue(f.monitor.snapshot().links().isEmpty());
            assertTrue(f.events.stream().noneMatch(e -> e instanceof NetworkMonitor.Updated u && u.metrics().sampleCount() > 0));
            assertTrue(f.events.stream().noneMatch(e -> e instanceof NetworkMonitor.Invalidated i && i.reason() == NetworkMonitor.Reason.TIMEOUT));
        }
        var f = new Fixture(); f.port.hold = true; f.up(session(10)); var p = f.ping(); f.pong(p);
        p.admission().completeExceptionally(new IllegalStateException("local failure")); f.owner.drain();
        assertEquals(NetworkMonitor.Reason.ADMISSION_FAILURE, f.monitor.snapshot().diagnostics().lastReason());
        assertEquals(NetworkMonitor.Phase.STOPPED, f.monitor.snapshot().diagnostics().phase());
    }
    @Test void m05WrongTokensDuplicatesAndOldDownCannotModifyCurrentMeasurement() {
        var f = new Fixture(); f.up(session(10)); var p = f.ping(); f.advance(1_000_000);
        var right = p.message();
        for (var bad : List.of(
                new ControlMessage(MessageType.PROBE_PONG, right.requestId(), "B", REMOTE, new ControlMessage.Probe(999)),
                new ControlMessage(MessageType.PROBE_PONG, right.requestId(), "B", new UUID(0, 7), right.payload()),
                new ControlMessage(MessageType.PROBE_PONG, right.requestId(), "X", REMOTE, right.payload())))
            f.monitor.onEvent(new NeighborManager.Received(session(10), bad));
        f.monitor.onEvent(new NeighborManager.Received(session(11), new ControlMessage(MessageType.PROBE_PONG,
                right.requestId(), "B", REMOTE, right.payload())));
        assertEquals(0, f.metrics().sampleCount()); f.pong(p); f.pong(p); assertEquals(1, f.metrics().sampleCount());
        f.up(session(11)); f.monitor.onEvent(new NeighborManager.NeighborDown(session(10), NeighborManager.Reason.TRANSPORT));
        assertEquals(session(11), f.metrics().session());
    }
    @Test void m07SuccessResetsConsecutiveLossesAndForeignTrafficDoesNot() {
        var f = new Fixture(options(1000, 1000, 2, .2, 4096, 64), 0, null); f.up(session(10)); f.advance(1_000_000_000);
        assertEquals(1, f.metrics().missedCount());
        f.monitor.onEvent(new NeighborManager.Received(session(10), new ControlMessage(MessageType.ERROR,
                new UUID(1, 2), "B", REMOTE, new ControlMessage.ErrorPayload(ErrorCode.INVALID_MESSAGE, 7, "bad"))));
        assertEquals(1, f.metrics().missedCount()); var p = f.ping(); f.advance(2_000_000); f.pong(p);
        assertEquals(0, f.metrics().missedCount()); f.advance(998_000_000); f.advance(1_000_000_000);
        assertTrue(f.port.closed.isEmpty()); assertEquals(1, f.metrics().missedCount());
    }
    @Test void m06ConfiguredFailureThresholdOneClosesExactlyOnce() {
        var f = new Fixture(options(10, 10, 1, .2, 4096, 64), 0, null); f.up(session(10)); f.advance(10_000_000);
        f.advance(100_000_000); assertEquals(List.of(session(10)), f.port.closed);
        assertEquals(1, f.events.stream().filter(e -> e instanceof NetworkMonitor.Invalidated).count());
    }
    @Test void m09InstanceChangeAndRetiredAdmissionCannotCloseReplacement() {
        var f = new Fixture(); f.port.hold = true; f.up(session(10)); var old = f.ping();
        var other = new UUID(0, 3); var replacement = new Neighbor.Session("B", other, new UUID(0, 11),
                new ControlMessage.Endpoint("B", other, "127.0.0.1", 19002, 19102, NodeRole.RELAY));
        f.up(replacement); old.admission().complete(Connection.SendResult.CLOSED); f.owner.drain();
        assertEquals(replacement, f.metrics().session()); assertEquals(0, f.metrics().sampleCount()); assertTrue(f.port.closed.isEmpty());
    }
    @Test void d10LocalSyncThrowFutureFailureAndInvalidMessageAreFatalNotPeerLoss() {
        for (int mode = 0; mode < 3; mode++) {
            var f = new Fixture(); f.port.throwSend = mode == 0; f.port.failSend = mode == 1;
            if (mode == 2) f.port.result = Connection.SendResult.INVALID_MESSAGE;
            f.up(session(10)); assertEquals(NetworkMonitor.Phase.STOPPED, f.monitor.snapshot().diagnostics().phase());
            assertTrue(f.port.closed.isEmpty()); assertTrue(f.monitor.snapshot().links().isEmpty());
            assertTrue(f.events.stream().anyMatch(e -> e instanceof NetworkMonitor.LocalFailure l && l.fatal()));
        }
    }
    @Test void d10EchoFloodIsBoundedAndEchoFailureFollowsLocalPolicy() {
        var f = new Fixture(); f.up(session(10)); f.port.hold = true;
        var ping = new ControlMessage(MessageType.PROBE_PING, new UUID(1, 1), "B", REMOTE, new ControlMessage.Probe(2));
        for (int i = 0; i < 1000; i++) f.monitor.onEvent(new NeighborManager.Received(session(10), ping));
        assertEquals(2, f.port.sent.size()); assertEquals(NetworkMonitor.Reason.ECHO_BACKPRESSURE, f.monitor.snapshot().diagnostics().lastReason());
        f.port.sent.getLast().admission().complete(Connection.SendResult.INVALID_MESSAGE); f.owner.drain();
        assertEquals(NetworkMonitor.Reason.INVALID_LOCAL_MESSAGE, f.monitor.snapshot().diagnostics().lastReason()); assertTrue(f.port.closed.isEmpty());
    }
    @Test void d10ObserverFailureIsObservableAndStopsWithoutCallingItForever() {
        var f = new Fixture(); int[] calls = {0};
        var m = new NetworkMonitor(config(), LOCAL, f.owner, f.port, e -> { calls[0]++; throw new IllegalStateException("consumer"); }, () -> f.timers.now, f.timers, () -> 1);
        m.start(); m.onEvent(new NeighborManager.NeighborUp(session(10))); f.owner.drain();
        m.onEvent(new NeighborManager.NeighborDown(session(10), NeighborManager.Reason.TRANSPORT));
        assertEquals(NetworkMonitor.Reason.CONSUMER_FAILURE, m.snapshot().diagnostics().lastReason());
        assertTrue(calls[0] <= 2); assertEquals(NetworkMonitor.Phase.STOPPED, m.snapshot().diagnostics().phase());
    }
    @Test void d11SixtyThreeSessionsAndChurnStayWithinPhysicalSendBudget() {
        var f = new Fixture(); f.port.hold = true;
        for (int i = 0; i < 64; i++) {
            String id = "B" + i; var s = new Neighbor.Session(id, REMOTE, new UUID(0, 10 + i),
                    new ControlMessage.Endpoint(id, REMOTE, "127.0.0.1", 19002, 19102, NodeRole.RELAY)); f.up(s);
        }
        assertEquals(63, f.monitor.snapshot().links().size()); assertEquals(63, f.port.sent.size());
        for (int i = 100; i < 400; i++) {
            var s = new Neighbor.Session("B0", REMOTE, new UUID(0, i),
                    new ControlMessage.Endpoint("B0", REMOTE, "127.0.0.1", 19002, 19102, NodeRole.RELAY)); f.up(s);
        }
        assertEquals(126, f.port.sent.size()); assertEquals(126, f.monitor.snapshot().diagnostics().sendTickets());
        assertEquals(0, f.metricsFor("B0").missedCount());
    }
    @Test void d11CloseHasAnIndependentReservationAndRetiredSendsKeepTheirTickets() {
        var f = new Fixture(options(10, 10, 1, .2, 1, 1), 0, null); f.up(session(10)); var p = f.ping();
        f.advance(10_000_000); assertEquals(List.of(session(10)), f.port.closed);
        f.port.hold = true; f.up(session(11)); var held = f.ping(); f.up(session(12));
        assertEquals(2, f.port.sent.size(), "B=1 includes the retired held admission"); assertEquals(0, f.metrics().missedCount());
        assertEquals(NetworkMonitor.Reason.LOCAL_CAPACITY, f.monitor.snapshot().diagnostics().lastReason());
        held.admission().complete(Connection.SendResult.CLOSED); f.owner.drain(); assertTrue(f.port.closed.stream().noneMatch(session(12)::equals));
    }
    @Test void d12CanceledRunningTimerStillUsesPhysicalSlotAndThirdJobIsRejected() throws Exception {
        var timers = new NetworkMonitor.Timers(); var entered = new CountDownLatch(1); var release = new CountDownLatch(1);
        try {
            var running = timers.schedule(() -> { entered.countDown(); try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }, 0);
            assertTrue(entered.await(2, TimeUnit.SECONDS)); running.cancel();
            var next = timers.schedule(() -> {}, TimeUnit.HOURS.toNanos(1));
            assertThrows(RejectedExecutionException.class, () -> timers.schedule(() -> {}, 0));
            timers.requestClose(); assertFalse(timers.awaitTermination(Duration.ZERO)); next.cancel();
            assertEquals(1, timers.count());
        } finally { release.countDown(); timers.requestClose(); assertTrue(timers.awaitTermination(Duration.ofSeconds(2))); }
        assertEquals(0, timers.count());
    }
    @Test void d12GenericOldWakeupDoesNotExpireNewPendingOrLoseNextDeadline() {
        var f = new Fixture(); f.up(session(10)); var oldWake = f.timers.jobs.getFirst().task;
        f.advance(1_000_000); f.pong(f.ping()); f.up(session(11)); oldWake.run(); f.owner.drain();
        assertEquals(0, f.metrics().missedCount()); f.advance(1_000_000_000);
        assertEquals(1, f.metrics().missedCount()); assertTrue(f.timers.jobs.size() <= 1);
    }
    @Test void d13RejectedOwnerOrSchedulerStopsWithVisibleLocalReason() {
        var f = new Fixture(); f.timers.reject = true; f.up(session(10));
        assertEquals(NetworkMonitor.Reason.SCHEDULER_REJECTED, f.monitor.snapshot().diagnostics().lastReason());
        var g = new Fixture(); g.up(session(10)); g.owner.reject = true; g.advance(1_000_000_000);
        assertTrue(g.owner.stopped); assertEquals(NetworkMonitor.Reason.OWNER_REJECTED, g.monitor.snapshot().diagnostics().lastReason());
        assertEquals(NetworkMonitor.Phase.STOPPED, g.monitor.snapshot().diagnostics().phase());
    }
    @Test void d13QueuedCompletionKeepsPhysicalReservationUntilItRunsOrOwnerStops() {
        var f = new Fixture(); f.port.hold = true; f.up(session(10));
        f.ping().admission().complete(Connection.SendResult.ACCEPTED); // continuation is now queued
        f.monitor.requestClose(); assertFalse(f.terminated()); f.owner.drain(); assertTrue(f.terminated());
        var g = new Fixture(); g.port.hold = true; g.up(session(10)); g.ping().admission().complete(Connection.SendResult.ACCEPTED);
        g.monitor.requestClose(); g.owner.requestClose(); assertTrue(g.terminated());
    }
    @Test void d14NanoTimeWrapAndNegativeInstantsAreValidButBackwardElapsedIsFatal() {
        var f = new Fixture(config(), Long.MAX_VALUE - 500_000, null); f.up(session(10)); var p = f.ping();
        f.advance(1_000_000); f.pong(p); assertEquals(1000.0, f.metrics().ewmaMicros().orElseThrow());
        var g = new Fixture(config(), -2_000_000, null); g.up(session(10)); var q = g.ping();
        g.advance(1_000_000); g.pong(q); assertEquals(1000.0, g.metrics().ewmaMicros().orElseThrow());
        g.timers.now = -5_000_000; g.monitor.onEvent(new NeighborManager.NeighborUp(session(10)));
        assertEquals(NetworkMonitor.Reason.CLOCK_ERROR, g.monitor.snapshot().diagnostics().lastReason());
    }
    @Test void d14ProbeIdMaximumDoesNotWrapOrRepeat() {
        var f = new Fixture(config(), 0, () -> Long.MAX_VALUE); f.up(session(10));
        assertEquals(new ControlMessage.Probe(Long.MAX_VALUE), f.ping().message().payload()); f.pong(f.ping());
        f.advance(1_000_000_000); assertEquals(1, f.port.sent.size());
        assertEquals(NetworkMonitor.Reason.PROBE_ID_EXHAUSTED, f.monitor.snapshot().diagnostics().lastReason());
    }
    @Test void d15StopRejectsNewWorkDoesNotStopBorrowedOwnerAndRejectsSelfJoin() {
        var f = new Fixture(); f.up(session(10)); f.monitor.requestClose(); f.owner.drain();
        assertFalse(f.owner.stopped); assertFalse(f.monitor.awaitTermination(Duration.ZERO));
        int writes = f.port.sent.size(); f.up(session(11)); f.advance(10_000_000_000L);
        assertEquals(writes, f.port.sent.size()); assertTrue(f.monitor.snapshot().links().isEmpty());
        assertTrue(f.terminated());
    }
    @Test void d16CopiesAndMeasurementInvariantsRejectNaNOverflowAndMismatchedKeys() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -1, 0x1p63})
            assertThrows(IllegalArgumentException.class, () -> new LinkMetrics(session(10), LinkMetrics.State.MEASURED,
                    OptionalDouble.of(value), OptionalLong.of(1), 1, false, OptionalLong.of(0), 0));
        assertThrows(IllegalArgumentException.class, () -> measured(1000).sampleAgeNanos(999));
        assertThrows(IllegalArgumentException.class, () -> new NetworkMonitor.Snapshot(0, Map.of("wrong", measured(0)), new Fixture().monitor.snapshot().diagnostics()));
        assertTrue(new LinkMetrics(session(10), LinkMetrics.State.UNMEASURED, OptionalDouble.empty(), OptionalLong.empty(), 0, false, OptionalLong.empty(), 0).sampleAgeNanos(1).isEmpty());
    }

    static Neighbor.Session named(String node, long connection) {
        return new Neighbor.Session(node, REMOTE, new UUID(0, connection),
                new ControlMessage.Endpoint(node, REMOTE, "127.0.0.1", 19002, 19102, NodeRole.RELAY));
    }
    @Test void d11ReservedCloseProgressesWhileSendCapacityIsFull() {
        var f = new Fixture(options(10, 10, 1, .2, 1, 2), 0, null); f.up(session(10));
        f.port.hold = true; f.up(named("C", 11)); assertEquals(1, f.monitor.snapshot().diagnostics().sendTickets());
        f.advance(10_000_000); assertEquals(List.of(session(10)), f.port.closed);
        assertEquals(1, f.monitor.snapshot().diagnostics().sendTickets()); assertEquals(0, f.metricsFor("C").missedCount());
    }
    @Test void d11OnlyOnePhysicalCloseIsAdmittedAndFailureIsLocalFatal() {
        var f = new Fixture(options(10, 10, 1, .2, 4096, 64), 0, null); f.port.holdClose = true;
        f.up(session(10)); f.up(named("C", 11)); f.up(named("D", 12)); f.advance(10_000_000);
        assertEquals(1, f.port.closed.size()); assertEquals(2, f.monitor.snapshot().diagnostics().queuedCloses());
        f.port.closeFutures.getFirst().complete(false); f.owner.drain(); assertEquals(2, f.port.closed.size());
        f.port.closeFutures.getLast().completeExceptionally(new IllegalStateException("local disconnect failure")); f.owner.drain();
        assertEquals(NetworkMonitor.Reason.ADMISSION_FAILURE, f.monitor.snapshot().diagnostics().lastReason());
        assertEquals(NetworkMonitor.Phase.STOPPED, f.monitor.snapshot().diagnostics().phase()); assertEquals(2, f.port.closed.size());
    }
    @Test void d13CallbackStillUnwindingCannotBeMistakenForPhysicalTermination() throws Exception {
        var f = new Fixture(); f.port.hold = true; f.up(session(10)); var ping = f.ping();
        var offered = new CountDownLatch(1); var release = new CountDownLatch(1);
        f.owner.afterOffer = () -> { offered.countDown(); try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } };
        var callback = Thread.ofVirtual().start(() -> ping.admission().complete(Connection.SendResult.ACCEPTED));
        try {
            assertTrue(offered.await(2, TimeUnit.SECONDS)); f.owner.afterOffer = () -> {};
            f.monitor.requestClose(); f.owner.drain(); assertFalse(f.terminated(), "callback is physically still running");
        } finally { release.countDown(); callback.join(Duration.ofSeconds(2)); }
        assertFalse(callback.isAlive()); assertTrue(f.terminated());
    }
    @Test void d13PhysicalReleaseWakesAQueuedCloseAfterItsContinuationFinished() throws Exception {
        var f = new Fixture(options(10, 10, 1, .2, 4096, 64), 0, null); f.port.holdClose = true;
        f.up(session(10)); f.up(named("C", 11)); f.advance(10_000_000); assertEquals(1, f.port.closed.size());
        var offered = new CountDownLatch(1); var release = new CountDownLatch(1);
        f.owner.afterOffer = () -> { offered.countDown(); try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } };
        var callback = Thread.ofVirtual().start(() -> f.port.closeFutures.getFirst().complete(true));
        try {
            assertTrue(offered.await(2, TimeUnit.SECONDS)); f.owner.afterOffer = () -> {}; f.owner.drain();
            assertEquals(1, f.port.closed.size(), "one physical close still in callback");
        } finally { release.countDown(); callback.join(Duration.ofSeconds(2)); }
        assertFalse(callback.isAlive()); f.owner.drain(); assertEquals(2, f.port.closed.size());
    }
    @Test void d14CountersSaturateWithoutWrappingAndKeepExactSampleIdentity() throws Exception {
        var f = new Fixture(); f.up(session(10)); var ping = f.ping(); f.pong(ping);
        // Boundary fixture only: reach a lifetime counter that cannot be exercised by a loop.
        var states = NetworkMonitor.class.getDeclaredField("links"); states.setAccessible(true);
        var link = ((Map<?, ?>) states.get(f.monitor)).get("B"); var samples = link.getClass().getDeclaredField("samples"); samples.setAccessible(true);
        samples.setLong(link, Long.MAX_VALUE); f.advance(1_000_000_000); f.pong(f.ping());
        assertEquals(Long.MAX_VALUE, f.metrics().sampleCount()); assertTrue(f.metrics().sampleCountSaturated());
        var failures = NetworkMonitor.class.getDeclaredField("localFailures"); failures.setAccessible(true); failures.setLong(f.monitor, Long.MAX_VALUE);
        f.port.hold = true;
        var remotePing = new ControlMessage(MessageType.PROBE_PING, new UUID(0, 90), "B", REMOTE, new ControlMessage.Probe(90));
        f.monitor.onEvent(new NeighborManager.Received(session(10), remotePing)); f.monitor.onEvent(new NeighborManager.Received(session(10), remotePing));
        assertEquals(Long.MAX_VALUE, f.monitor.snapshot().diagnostics().localFailures()); assertTrue(f.monitor.snapshot().diagnostics().countersSaturated());
    }
    @Test void d14LongestConfiguredTimeoutStillConvertsWithoutOverflow() {
        var f = new Fixture(options(Integer.MAX_VALUE, Integer.MAX_VALUE, 3, 1, 4096, 64), 0, null);
        f.up(session(10)); var p = f.ping(); long delta = (long) Integer.MAX_VALUE * 1_000_000 - 1;
        f.advance(delta); f.pong(p);
        assertEquals((long) Math.ceil(delta / 1000.0), f.metrics().costMicros().orElseThrow());
        assertTrue(Double.isFinite(f.metrics().ewmaMicros().orElseThrow()));
    }
    @Test void d16SnapshotRejectsOverCapBeforeIteratingOrAllocatingCopy() {
        var tooMany = new AbstractMap<String, LinkMetrics>() {
            @Override public int size() { return 64; }
            @Override public Set<Entry<String, LinkMetrics>> entrySet() { throw new AssertionError("must check cap first"); }
        };
        assertThrows(IllegalArgumentException.class, () -> new NetworkMonitor.Snapshot(0, tooMany, new Fixture().monitor.snapshot().diagnostics()));
    }
    @Test void m20WireEnumAndCodecRetainV2AndRejectCodeThree() throws Exception {
        assertEquals(19, MessageType.values().length); assertThrows(ProtocolException.class, () -> MessageType.fromCode(3));
        var m = new ControlMessage(MessageType.PROBE_PING, new UUID(1, 2), "B", REMOTE, new ControlMessage.Probe(90));
        assertEquals(m, MessageCodec.decode(MessageCodec.encode(m))); assertEquals(2, m.version());
    }
    @Test void d15CloseBeforeStartIsIdempotentAndBorrowedOwnerIsNeverClosed() {
        var f = new Fixture(); var closing = f.monitor.requestClose(); assertTrue(closing.toCompletableFuture().isDone());
        f.monitor.start(); f.monitor.onEvent(new NeighborManager.NeighborUp(session(10))); f.monitor.requestClose();
        assertTrue(f.terminated()); assertFalse(f.owner.stopped); assertTrue(f.port.sent.isEmpty());
    }
    @Test void d01StateMutationOffOwnerIsRejectedWithoutTouchingTransport() {
        var f = new Fixture(); f.owner.inside = false;
        assertThrows(IllegalStateException.class, f.monitor::start);
        assertThrows(IllegalStateException.class, () -> f.monitor.onEvent(new NeighborManager.NeighborUp(session(10))));
        assertTrue(f.port.sent.isEmpty());
        assertEquals(NetworkMonitor.Phase.NEW, f.monitor.snapshot().diagnostics().phase());
    }

    @Test void d05EarliestValidReplyWinsEvenIfAdmissionArrivesAfterDeadline() {
        var f = new Fixture(); f.port.hold = true; f.up(session(10)); var p = f.ping();
        f.advance(1_000_000); f.pong(p); f.advance(2_000_000); f.pong(p);
        f.advance(3_000_000_000L); p.admission().complete(Connection.SendResult.ACCEPTED); f.owner.drain();
        assertEquals(1000.0, f.metrics().ewmaMicros().orElseThrow()); assertEquals(0, f.metrics().missedCount());
        assertEquals(1, f.metrics().sampleCount()); assertEquals(2, f.port.sent.size());
    }
    @Test void d07LateEarlyReplyCannotRescueAnAdmittedExpiredProbe() {
        var f = new Fixture(); f.port.hold = true; f.up(session(10)); var p = f.ping();
        f.advance(1_000_000_000); f.pong(p); assertEquals(0, f.metrics().missedCount());
        p.admission().complete(Connection.SendResult.ACCEPTED); f.owner.drain();
        assertEquals(1, f.metrics().missedCount()); assertEquals(0, f.metrics().sampleCount());
    }
    @Test void d10EchoClosedOverflowAndExceptionalResultAreNotRemoteTimeouts() {
        for (int mode = 0; mode < 3; mode++) {
            var f = new Fixture(); f.up(session(10));
            if (mode == 2) f.port.failSend = true;
            else f.port.result = mode == 0 ? Connection.SendResult.CLOSED : Connection.SendResult.OVERFLOW;
            f.monitor.onEvent(new NeighborManager.Received(session(10), new ControlMessage(MessageType.PROBE_PING,
                    new UUID(1, 9), "B", REMOTE, new ControlMessage.Probe(30)))); f.owner.drain();
            assertTrue(f.monitor.snapshot().links().isEmpty());
            assertEquals(mode == 0 ? NetworkMonitor.Reason.ADMISSION_CLOSED : mode == 1 ? NetworkMonitor.Reason.OUTPUT_OVERFLOW
                    : NetworkMonitor.Reason.ADMISSION_FAILURE, f.monitor.snapshot().diagnostics().lastReason());
            assertTrue(f.events.stream().noneMatch(e -> e instanceof NetworkMonitor.Invalidated i && i.reason() == NetworkMonitor.Reason.TIMEOUT));
        }
    }
    @Test void d15HeldEchoAndHeldCloseAreCountedUntilTheirRealCompletion() {
        var f = new Fixture(); f.up(session(10)); f.port.hold = true;
        f.monitor.onEvent(new NeighborManager.Received(session(10), new ControlMessage(MessageType.PROBE_PING,
                new UUID(1, 3), "B", REMOTE, new ControlMessage.Probe(88)))); f.owner.drain();
        var echo = f.port.sent.getLast(); f.monitor.requestClose(); assertFalse(f.terminated());
        echo.admission().complete(Connection.SendResult.CLOSED); assertTrue(f.terminated()); assertFalse(f.owner.stopped);
        var g = new Fixture(options(10, 10, 1, .2, 4096, 64), 0, null); g.port.holdClose = true;
        g.up(session(10)); g.advance(10_000_000); g.monitor.requestClose(); assertFalse(g.terminated());
        g.port.closeFutures.getFirst().complete(false); assertTrue(g.terminated()); assertFalse(g.owner.stopped);
    }
    @Test void d16UnmeasuredLossAndMeasuredSuspectKeepStateAndAgeConsistent() {
        var f = new Fixture(); f.up(session(10)); f.advance(1_000_000_000);
        assertEquals(LinkMetrics.State.UNMEASURED, f.metrics().state()); assertTrue(f.metrics().sampleAgeNanos(f.timers.now).isEmpty());
        var p = f.ping(); f.advance(1_000_000); f.pong(p); f.advance(999_000_000); f.advance(1_000_000_000);
        assertEquals(LinkMetrics.State.SUSPECT, f.metrics().state()); assertEquals(1, f.metrics().missedCount());
        assertEquals(OptionalLong.of(1_999_000_000), f.metrics().sampleAgeNanos(f.timers.now));
    }
}
