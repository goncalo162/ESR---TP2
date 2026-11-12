package pt.uminho.esr.ott.overlay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.Connection;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static pt.uminho.esr.ott.overlay.NeighborManagerTest.*;

/** Uses the existing deterministic ports without modifying the historical tests. */
@Timeout(5)
class NeighborManagerLifecycleTest {
    private static final Duration BUDGET = Duration.ofSeconds(3);
    private static NeighborManager.ShutdownReport result(CompletionStage<NeighborManager.ShutdownReport> stage) {
        return assertDoesNotThrow(() -> stage.toCompletableFuture().get(1, TimeUnit.SECONDS));
    }
    private static CompletionStage<NeighborManager.ShutdownReport> shutdown(Fixture f, Duration budget) {
        return assertDoesNotThrow(() -> f.manager.requestShutdown(0, budget), "lifecycle must accept a valid request");
    }
    private static FakeChannel active(Fixture f) {
        f.ready("B"); var channel = f.outbound(true); f.ok(channel, UUID.randomUUID());
        assertEquals(1, f.manager.snapshot().size()); return channel;
    }
    @Test void reportCopiesAttemptsAndRejectsNullElements() {
        var attempts = new ArrayList<NeighborManager.LeaveAttempt>();
        var report = new NeighborManager.ShutdownReport(attempts, false);
        var remote = UUID.randomUUID();
        attempts.add(new NeighborManager.LeaveAttempt(new Neighbor.Session("B", remote,
                UUID.randomUUID(), endpoint("B", remote, 19002)), NeighborManager.LeaveOutcome.ABORTED));
        assertTrue(report.attempts().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> report.attempts().clear());
        assertThrows(NullPointerException.class, () -> new NeighborManager.ShutdownReport(Arrays.asList((NeighborManager.LeaveAttempt)null), false));
    }
    @Test void attemptRejectsNullSessionAndOutcome() {
        assertThrows(NullPointerException.class, () -> new NeighborManager.LeaveAttempt(null, NeighborManager.LeaveOutcome.ABORTED));
        var remote = UUID.randomUUID();
        var session = new Neighbor.Session("B", remote, UUID.randomUUID(), endpoint("B", remote, 19002));
        assertThrows(NullPointerException.class, () -> new NeighborManager.LeaveAttempt(session, null));
    }
    @Test void l01InvalidReasonDoesNotCreateServices() {
        try (var f = new Fixture("A")) {
            assertThrows(IllegalArgumentException.class, () -> f.manager.requestShutdown(2, BUDGET));
            assertEquals(0, f.factories);
        }
    }
    @Test void l01InvalidDurationDoesNotCreateServices() {
        try (var f = new Fixture("A")) {
            assertThrows(IllegalArgumentException.class, () -> f.manager.requestShutdown(0, Duration.ofNanos(-1)));
            assertThrows(NullPointerException.class, () -> f.manager.requestShutdown(0, null));
            assertThrows(IllegalArgumentException.class, () -> f.manager.requestShutdown(0, Duration.ofSeconds(Long.MAX_VALUE)));
            assertEquals(0, f.factories);
        }
    }
    @Test void l01BeforeStartClosesWithoutFactoriesAndCannotRevive() {
        try (var f = new Fixture("A")) {
            var report = result(shutdown(f, BUDGET));
            assertTrue(report.attempts().isEmpty()); assertEquals(0, f.factories);
            assertTrue(f.manager.start().toCompletableFuture().isCompletedExceptionally());
        }
    }
    @Test void l01ZeroBudgetSkipsLeaveAndCloses() {
        try (var f = new Fixture("A")) {
            var c = active(f); int before = c.writes.size();
            var stage = shutdown(f, Duration.ZERO); f.pump(); result(stage);
            assertEquals(before, c.writes.size()); assertFalse(c.open);
            assertTrue(f.manager.awaitTermination(BUDGET));
        }
    }
    @Test void l02OnlyConfirmedDirectSessionGetsV2Leave() {
        try (var f = new Fixture("A")) {
            var c = active(f); var session = f.manager.snapshot().get("B");
            var stage = shutdown(f, BUDGET); f.pump(); var report = result(stage);
            var leave = c.last(); assertEquals(MessageType.NODE_LEAVE, leave.type());
            assertEquals(2, leave.version()); assertEquals("A", leave.senderId()); assertEquals(f.instance, leave.senderInstance());
            assertNotEquals(ZERO, leave.requestId()); assertEquals(new ControlMessage.Leave(0), leave.payload());
            assertEquals(List.of(new NeighborManager.LeaveAttempt(session, NeighborManager.LeaveOutcome.LOCAL_WRITE_CONFIRMED)), report.attempts());
            assertFalse(report.notificationTimedOut()); assertFalse(c.open);
        }
    }
    @Test void l02DiscoveryAndUnconfirmedHelloAreNotNotified() {
        try (var f = new Fixture("A")) {
            var bootstrap = f.discovery(); var stage = shutdown(f, BUDGET); f.pump();
            assertTrue(result(stage).attempts().isEmpty());
            assertEquals(List.of(MessageType.REGISTER), bootstrap.writes.stream().map(ControlMessage::type).toList());
            assertFalse(bootstrap.open);
        }
        try (var f = new Fixture("A")) {
            f.ready("B"); var c = f.outbound(false); var stage = shutdown(f, BUDGET); f.pump();
            assertTrue(result(stage).attempts().isEmpty());
            assertEquals(List.of(MessageType.HELLO), c.writes.stream().map(ControlMessage::type).toList());
            assertFalse(c.open);
        }
    }
    @Test void l03ApplicationSendStillRejectsLeaveWithoutClosingHealthySession() {
        try (var f = new Fixture("A")) {
            var c = active(f); var session = f.manager.snapshot().get("B");
            var leave = new ControlMessage(MessageType.NODE_LEAVE, UUID.randomUUID(), "A", f.instance, new ControlMessage.Leave(0));
            var sent = f.manager.send(session, leave); f.pump();
            assertEquals(Connection.SendResult.INVALID_MESSAGE, sent.toCompletableFuture().join());
            assertTrue(c.open); assertEquals(1, c.writes.size());
        }
    }
    @Test void l04CompletionWaitsForLocalFlush() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false;
            var stage = shutdown(f, BUDGET); f.pump();
            assertFalse(stage.toCompletableFuture().isDone()); assertTrue(c.open);
            c.written(); f.pump();
            assertEquals(NeighborManager.LeaveOutcome.LOCAL_WRITE_CONFIRMED, result(stage).attempts().getFirst().outcome());
            assertFalse(c.open);
        }
    }
    @Test void l05FailedWriteStillClosesWithoutNewDial() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false; int dials = f.io.dials.size();
            var stage = shutdown(f, BUDGET); f.pump();
            c.confirmations.getLast().completeExceptionally(new java.io.IOException("controlled write failure")); f.pump();
            assertEquals(NeighborManager.LeaveOutcome.WRITE_FAILED, result(stage).attempts().getFirst().outcome());
            assertFalse(c.open); assertEquals(dials, f.io.dials.size());
        }
    }
    @Test void l06HalfBudgetDeadlineWinsAndLateFlushCannotChangeReport() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false;
            var stage = shutdown(f, BUDGET); f.pump();
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(1499)); f.pump(); assertFalse(stage.toCompletableFuture().isDone());
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(1)); f.pump(); var report = result(stage);
            assertTrue(report.notificationTimedOut()); assertEquals(NeighborManager.LeaveOutcome.TIMED_OUT, report.attempts().getFirst().outcome());
            assertFalse(c.open); c.written(); f.pump(); assertEquals(report, result(stage));
            assertTrue(f.manager.awaitTermination(Duration.ofMillis(1500)));
        }
    }
    @Test void l07RepeatedRequestKeepsFirstReasonAndDeadline() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false;
            var first = shutdown(f, BUDGET); f.pump();
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(1000)); f.pump();
            var second = assertDoesNotThrow(() -> f.manager.requestShutdown(1, BUDGET));
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(500)); f.pump();
            assertEquals(result(first), result(second));
            assertEquals(1, c.writes.stream().filter(m -> m.type() == MessageType.NODE_LEAVE).count());
            assertEquals(new ControlMessage.Leave(0), c.last().payload());
        }
    }
    @Test void l07ImmediateClosePreemptsPendingFlush() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false; var stage = shutdown(f, BUDGET); f.pump();
            f.manager.requestClose(); f.pump();
            assertEquals(NeighborManager.LeaveOutcome.ABORTED, result(stage).attempts().getFirst().outcome());
            assertFalse(c.open); assertTrue(f.manager.awaitTermination(BUDGET));
        }
    }
    @Test void l08QueuedAndNewApplicationCommandsCannotWriteAfterRequest() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false; var session = f.manager.snapshot().get("B");
            var app = new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), "A", f.instance, new ControlMessage.Probe(7));
            var queued = f.manager.send(session, app); var shutdown = shutdown(f, BUDGET);
            var later = f.manager.send(session, app); f.pump();
            assertEquals(Connection.SendResult.CLOSED, queued.toCompletableFuture().join());
            assertEquals(Connection.SendResult.CLOSED, later.toCompletableFuture().join());
            assertTrue(c.writes.stream().noneMatch(m -> m.type() == MessageType.PROBE_PING));
            c.written(); f.pump(); result(shutdown);
        }
    }
    @Test void l08RetryAndLateDialCannotReviveDuringShutdown() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var stage = shutdown(f, BUDGET); f.pump();
            var late = new FakeChannel(); f.io.dials.get(1).future().complete(late); f.pump();
            f.timers.advance(TimeUnit.SECONDS.toNanos(30)); f.pump();
            result(stage); assertFalse(late.open); assertTrue(late.writes.isEmpty()); assertEquals(2, f.io.dials.size());
        }
    }
    @Test void l10RejectedOwnerCannotLeaveShutdownFuturePending() {
        try (var f = new Fixture("A")) {
            var c = active(f); f.owner.reject = true; var stage = shutdown(f, BUDGET); f.pump();
            result(stage); assertFalse(c.open); assertTrue(f.manager.awaitTermination(BUDGET));
        }
    }
    @Test void l12BorrowedOwnerRemainsOwnedByComposition() {
        try (var f = new Fixture(config("A", 19001), true)) {
            var c = active(f); var stage = shutdown(f, BUDGET); f.pump(); result(stage);
            assertFalse(c.open); assertTrue(f.manager.awaitTermination(BUDGET)); assertFalse(f.owner.stopped);
            f.owner.requestClose(); assertTrue(f.owner.awaitTermination(BUDGET));
        }
    }
    @Test void l09StaleSessionCallbacksCannotNotifyOrDeleteReplacement() {
        try (var f = new Fixture("A")) {
            var old = active(f); var oldSession = f.manager.snapshot().get("B");
            var disconnected = f.manager.disconnect(oldSession); f.pump(); assertTrue(disconnected.toCompletableFuture().join());
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(500)); f.pump();
            var fresh = f.connect(2, false); f.ok(fresh, UUID.randomUUID());
            var freshSession = f.manager.snapshot().get("B");
            f.io.sink.closed(old, Connection.CloseReason.PEER_EOF, f.clock.get());
            var stage = shutdown(f, BUDGET); f.pump(); var report = result(stage);
            assertEquals(List.of(freshSession), report.attempts().stream().map(NeighborManager.LeaveAttempt::session).toList());
            assertNotEquals(oldSession.instanceId(), freshSession.instanceId());
            assertTrue(old.writes.stream().noneMatch(m -> m.type() == MessageType.NODE_LEAVE));
            assertEquals(MessageType.NODE_LEAVE, fresh.last().type());
        }
    }
    @Test void l10RejectedTimerStillClosesAndCompletesRequest() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false; f.timers.stopped = true;
            var stage = shutdown(f, BUDGET); f.pump(); result(stage);
            assertFalse(c.open); assertTrue(f.manager.awaitTermination(BUDGET));
        }
    }
    @Test void l11OwnerQueueTimeConsumesTheSameHalfBudget() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false; var stage = shutdown(f, BUDGET);
            f.clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(1000)); f.pump();
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(499)); f.pump(); assertFalse(stage.toCompletableFuture().isDone());
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(1)); f.pump();
            assertTrue(result(stage).notificationTimedOut()); assertFalse(c.open);
        }
    }
    @Test void l12ErrorReasonUsesExistingV1Payload() {
        try (var f = new Fixture("A")) {
            var c = active(f); var stage = assertDoesNotThrow(() -> f.manager.requestShutdown(1, BUDGET));
            f.pump(); result(stage); assertEquals(new ControlMessage.Leave(1), c.last().payload());
        }
    }

    @Test void l05ClosedChannelCannotClaimSuccessfulLocalWrite() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.close(); var stage = shutdown(f, BUDGET); f.pump();
            assertEquals(NeighborManager.LeaveOutcome.WRITE_FAILED, result(stage).attempts().getFirst().outcome());
            assertEquals(1, c.writes.size());
        }
    }
    @Test void l11MaximumDegreeUsesOneNotificationTimerAndBoundedReport() {
        try (var f = new Fixture("Z")) {
            var bootstrap = f.discovery(); var endpoints = new ArrayList<ControlMessage.Endpoint>();
            for (int n = 0; n < 63; n++) endpoints.add(endpoint(String.format("P%02d", n), ZERO, 20000 + n * 2));
            f.message(bootstrap, new ControlMessage(MessageType.REGISTER_OK, bootstrap.last().requestId(), "BOOT", UUID.randomUUID(), new ControlMessage.NeighborList(endpoints)));
            var channels = new ArrayList<FakeChannel>();
            for (var configured : endpoints) {
                var c = new FakeChannel(); var remote = UUID.randomUUID();
                f.io.sink.opened(c, f.clock.get()); f.pump();
                f.message(c, new ControlMessage(MessageType.HELLO, UUID.randomUUID(), configured.nodeId(), remote,
                    new ControlMessage.Hello(endpoint(configured.nodeId(), remote, configured.controlPort()),3,3)));
                c.flush = false; channels.add(c);
            }
            assertEquals(63, f.manager.snapshot().size());
            var stage = shutdown(f, BUDGET); f.pump();
            assertEquals(1, f.timers.tasks.size());
            assertTrue(f.timers.peak <= f.timers.limit); assertFalse(stage.toCompletableFuture().isDone());
            channels.forEach(FakeChannel::written); f.pump();
            var report = result(stage); assertEquals(63, report.attempts().size());
            assertEquals(63, new HashSet<>(report.attempts().stream().map(NeighborManager.LeaveAttempt::session).toList()).size());
            assertTrue(report.attempts().stream().allMatch(a -> a.outcome() == NeighborManager.LeaveOutcome.LOCAL_WRITE_CONFIRMED));
            assertThrows(UnsupportedOperationException.class, () -> report.attempts().clear());
            assertTrue(f.timers.tasks.isEmpty()); assertTrue(channels.stream().noneMatch(c -> c.open));
        }
    }
    @Test void l11LargeBudgetIsCappedAtConfiguredTimeoutAndWrapIsSafe() {
        try (var f = new Fixture("A")) {
            f.clock.set(Long.MAX_VALUE - TimeUnit.SECONDS.toNanos(1)); var c = active(f); c.flush = false;
            var stage = shutdown(f, Duration.ofDays(1)); f.pump();
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(1500)); f.pump();
            assertTrue(result(stage).notificationTimedOut()); assertFalse(c.open);
        }
    }
    @Test void l12RealTcpReceivesV2LeaveBeforeConnectionCloses() throws Exception {
        try (var d = new Deployment()) {
            var a = d.managers.getFirst(); a.start().toCompletableFuture().get(3, TimeUnit.SECONDS);
            var config = d.configs.get(1); var remote = UUID.randomUUID();
            // N1 initiates only toward larger IDs, so replace N0 by an inbound-capable N2 service.
            var receiver = d.managers.get(2); receiver.start().toCompletableFuture().get(3, TimeUnit.SECONDS);
            try (var socket = new java.net.Socket("127.0.0.1", d.configs.get(2).controlPort())) {
                socket.setSoTimeout(2500);
                var ep = new ControlMessage.Endpoint("N1", remote, "127.0.0.1", config.controlPort(), config.dataPort(), NodeRole.RELAY);
                MessageCodec.write(new ControlMessage(MessageType.HELLO, UUID.randomUUID(), "N1", remote,
                    new ControlMessage.Hello(ep,3,3)), socket.getOutputStream()); socket.getOutputStream().flush();
                assertEquals(MessageType.HELLO_OK, MessageCodec.read(socket.getInputStream()).orElseThrow().type());
                until(() -> receiver.snapshot().containsKey("N1"));
                var report = receiver.requestShutdown(1, BUDGET).toCompletableFuture().get(2, TimeUnit.SECONDS);
                var leave = MessageCodec.read(socket.getInputStream()).orElseThrow();
                assertEquals(2, leave.version()); assertEquals(MessageType.NODE_LEAVE, leave.type());
                assertEquals(new ControlMessage.Leave(1), leave.payload()); assertNotEquals(ZERO, leave.senderInstance());
                assertTrue(MessageCodec.read(socket.getInputStream()).isEmpty());
                assertTrue(report.attempts().stream().anyMatch(t -> t.session().nodeId().equals("N1") && t.outcome() == NeighborManager.LeaveOutcome.LOCAL_WRITE_CONFIRMED));
                assertTrue(receiver.awaitTermination(BUDGET));
            }
        }
    }

    @Test void l11ReportChecksDegreeBeforeCopyingInput() {
        var remote = UUID.randomUUID();
        var attempt = new NeighborManager.LeaveAttempt(new Neighbor.Session("B", remote, UUID.randomUUID(), endpoint("B", remote, 19002)), NeighborManager.LeaveOutcome.ABORTED);
        assertThrows(IllegalArgumentException.class, () -> new NeighborManager.ShutdownReport(Collections.nCopies(64, attempt), false));
    }
    @Test void l10ConsumerFailureDuringCleanupCannotRetainFutureOrChannels() {
        try (var f = new Fixture("A")) {
            var c = active(f); f.observerFails = true;
            var stage = shutdown(f, BUDGET); f.pump(); result(stage);
            assertFalse(c.open); assertTrue(f.manager.awaitTermination(BUDGET));
        }
    }
    @Test void l07ConcurrentRequestsEmitOnceAndKeepOneFuture() throws Exception {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false;
            var requests = new java.util.concurrent.CopyOnWriteArrayList<CompletionStage<NeighborManager.ShutdownReport>>();
            var threads = new ArrayList<Thread>();
            for (int n = 0; n < 8; n++) threads.add(Thread.ofVirtual().start(() -> requests.add(f.manager.requestShutdown(0, BUDGET))));
            for (var thread : threads) { thread.join(Duration.ofSeconds(1)); assertFalse(thread.isAlive()); }
            assertEquals(8, requests.size()); assertTrue(requests.stream().allMatch(r -> r == requests.getFirst()));
            f.pump(); c.written(); f.pump();
            assertEquals(1, c.writes.stream().filter(m -> m.type() == MessageType.NODE_LEAVE).count());
            assertTrue(requests.stream().allMatch(r -> result(r).equals(result(requests.getFirst()))));
        }
    }

    @Test void l05SynchronousOverflowStillClosesAndReportsWriteFailure() {
        try (var f = new Fixture("A")) {
            f.ready("B"); var delegate = new FakeChannel();
            var channel = new NeighborManager.Channel() {
                public UUID id() { return delegate.id(); }
                public boolean isOpen() { return delegate.isOpen(); }
                public Connection.SendResult send(ControlMessage message) { return delegate.send(message); }
                public CompletableFuture<Void> sendAndFlush(ControlMessage message) {
                    if (message.type() == MessageType.NODE_LEAVE) throw new RejectedExecutionException("controlled output overflow");
                    return delegate.sendAndFlush(message);
                }
                public Connection.ExtensionEnqueue trySendExtension(ControlMessage message){return delegate.trySendExtension(message);}
                public void close() { delegate.close(); }
            };
            f.io.sink.opened(channel, f.clock.get()); f.io.dials.get(1).future().complete(channel); f.pump();
            f.ok(delegate, UUID.randomUUID()); assertEquals(1, f.manager.snapshot().size());
            var stage = shutdown(f, BUDGET); f.pump();
            assertEquals(NeighborManager.LeaveOutcome.WRITE_FAILED, result(stage).attempts().getFirst().outcome());
            assertFalse(delegate.open); assertEquals(2, f.io.dials.size());
        }
    }
    @Test void l10OwnerStopsBetweenAdmittingRequestAndRunningIt() {
        try (var f = new Fixture("A")) {
            var c = active(f); var stage = shutdown(f, BUDGET);
            f.owner.requestClose(); result(stage);
            assertFalse(c.open); assertTrue(f.manager.awaitTermination(BUDGET));
        }
    }

    @Test void l12CloseDoesNotRenewBudgetAfterNotificationTimeout() {
        try (var f = new Fixture("A")) {
            var c = active(f); c.flush = false;
            var stage = shutdown(f, Duration.ofMillis(200)); f.pump();
            f.timers.advance(TimeUnit.MILLISECONDS.toNanos(100)); f.pump(); assertTrue(result(stage).notificationTimedOut());
            f.io.awaitClock = f.clock; f.io.consumeMillis = 60;
            assertThrows(IllegalStateException.class, f.manager::close);
            assertEquals(List.of(Duration.ofMillis(100), Duration.ofMillis(40)), f.io.awaitBudgets);
            f.io.consumeMillis = 0;
        }
    }

    @Test void D10DiscoveryArrivingDuringShutdownCannotCloseBeforeLeaveFlush() {
        try(var f=new Fixture("A")) {
            var channel=active(f);var session=f.manager.snapshot().get("B");var token=channel.writes.getFirst().requestId();
            var shutdown=shutdown(f,BUDGET); // draining set, notification event still queued
            f.io.sink.message(channel,new ControlMessage(MessageType.DISCOVERY_QUERY,UUID.randomUUID(),"B",session.instanceId(),
                new pt.uminho.esr.ott.protocol.DiscoveryMessage.Query(token,"A",f.instance)),f.clock.get());
            assertTrue(channel.open,"normal input after stopping admissions cannot preempt the leave writer");
            f.pump();var report=result(shutdown);assertEquals(MessageType.NODE_LEAVE,channel.last().type());
            assertEquals(NeighborManager.LeaveOutcome.LOCAL_WRITE_CONFIRMED,report.attempts().getFirst().outcome());
            assertFalse(channel.open);assertTrue(f.manager.awaitTermination(BUDGET));
        }
    }

    @Test void D19_repairedActiveSessionReceivesV2LeaveAndTerminatesWithinGlobalBudget()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var c=OverlayRecoveryTest.repair(f,"A",UUID.randomUUID(),18000,true);var session=f.manager.snapshot().get("A");
            var stage=shutdown(f,BUDGET);f.pump();var report=result(stage);
            assertEquals(MessageType.NODE_LEAVE,c.last().type());assertEquals(2,c.last().version());assertEquals(new ControlMessage.Leave(0),c.last().payload());
            assertEquals(List.of(new NeighborManager.LeaveAttempt(session,NeighborManager.LeaveOutcome.LOCAL_WRITE_CONFIRMED)),report.attempts());
            assertFalse(report.notificationTimedOut());assertFalse(c.open);assertTrue(f.manager.awaitTermination(BUDGET));assertEquals(OverlayRecovery.Phase.STOPPING,f.manager.recoveryStatus().phase());
        }
    }
}
