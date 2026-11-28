package pt.uminho.esr.ott.routing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import pt.uminho.esr.ott.app.OttApplication;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.routing.RoutingService.*;

/**
 * Incremental V-013 oracles with deterministic fakes and real CLI/TCP; formal
 * Test remains pending.
 */
@Timeout(10)
class RouteTraceTest {

    // Reuse the existing fake owner/clock/scheduler/control port without changing its tests.
    private static final UUID STREAM = RoutingServiceTest.STREAM;
    private static final UUID EPOCH = RoutingServiceTest.EPOCH;

    private static TraceTicket request(RoutingServiceTest.Rig r) {
        return assertDoesNotThrow(() -> r.router.requestTrace(STREAM),
                "requestTrace must admit or return a terminal ticket, never a placeholder exception");
    }

    private static TraceOutcome terminal(TraceTicket ticket) {
        return ticket.result().orElseGet(() -> fail("Expected an explicit terminal diagnostic outcome"));
    }

    private static List<ControlMessage> traces(RoutingServiceTest.Rig r) {
        return r.sent.stream().map(RoutingServiceTest.Sent::message)
                .filter(m -> m.type() == MessageType.ROUTE_TRACE).toList();
    }

    private static void receive(RoutingServiceTest.Rig r, Neighbor.Session peer, ControlMessage m) {
        r.act(() -> r.router.onNeighborEvent(new NeighborManager.Received(peer, m)));
    }

    @Test
    void outcomeOwnsItsVisitedAndKeepsOpaqueIds() {
        var path = new ArrayList<>(List.of("C", "S"));
        UUID id = new UUID(-1, -2);
        var value = new TraceOutcome(STREAM, id, Optional.of(EPOCH), TraceState.SUCCESS,
                Optional.empty(), "", path, 17);
        path.clear();
        assertEquals(List.of("C", "S"), value.visited());
        assertEquals(id, value.traceId());
        assertThrows(UnsupportedOperationException.class, () -> value.visited().add("X"));
    }

    @Test
    void outcomeRejectsOverlongVisitedUtf8AndNegativeDuration() {
        assertThrows(IllegalArgumentException.class, () -> new TraceOutcome(STREAM, EPOCH,
                Optional.empty(), TraceState.ERROR, Optional.of(ErrorCode.TIMEOUT), "é".repeat(129), List.of(), 0));
        assertThrows(IllegalArgumentException.class, () -> new TraceOutcome(STREAM, EPOCH,
                Optional.empty(), TraceState.ERROR, Optional.empty(), "", Collections.nCopies(33, "X"), 0));
        assertThrows(IllegalArgumentException.class, () -> new TraceOutcome(STREAM, EPOCH,
                Optional.empty(), TraceState.ERROR, Optional.empty(), "", List.of(), -1));
    }

    @Test
    void t01LocalSourceReturnsSingletonWithoutDemandOrControlSend() {
        try (var r = new RoutingServiceTest.Rig("S", true, 4096, 4096, 10,
                new RoutingTable.Limits(64, 256, 63, 8))) {
            var before = r.router.snapshot();
            var ticket = request(r);
            r.owner.drain();
            var result = terminal(ticket);
            assertEquals(TraceState.SUCCESS, result.state());
            assertEquals(List.of("S"), result.visited());
            assertEquals(before.routes(), r.router.snapshot().routes());
            assertTrue(r.sent.isEmpty(), "Local diagnostic cannot emit activation or video control");
        }
    }

    @Test
    void t04UnknownStreamHasExplicitErrorWithoutNetwork() {
        try (var r = new RoutingServiceTest.Rig()) {
            var ticket = request(r);
            r.owner.drain();
            assertEquals(Optional.of(ErrorCode.UNKNOWN_STREAM), terminal(ticket).errorCode());
            assertTrue(r.sent.isEmpty());
        }
    }

    @Test
    void t02InitialRequestUsesSelectedSessionAndTtlCountsInitiator() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            var b = RoutingServiceTest.session("B");
            r.up(a);
            r.up(b);
            r.metric(a, 40);
            r.metric(b, 10);
            r.ad(a, 1, 100);
            r.ad(b, 1, 100);
            assertEquals("B", r.route().knownUpstream().orElseThrow().nodeId());
            var ticket = request(r);
            r.owner.drain();
            var sent = r.sent.stream().filter(x -> x.message().type() == MessageType.ROUTE_TRACE).toList();
            assertEquals(1, sent.size());
            assertEquals(b, sent.getFirst().peer());
            var m = sent.getFirst().message();
            var p = assertInstanceOf(ControlMessage.Trace.class, m.payload());
            assertEquals(ticket.traceId(), m.requestId());
            assertEquals(ticket.traceId(), p.traceId());
            assertEquals(List.of("C"), p.visited());
            assertEquals(31, p.ttl());
            assertEquals(EPOCH, p.serverEpoch());
            assertTrue(ticket.result().isEmpty(), "ACCEPTED is not a result");
        }
    }

    @Test
    void t05TtlZeroIsExplicitTimeOutWithoutForwarding() {
        try (var r = new RoutingServiceTest.Rig()) {
            var c = RoutingServiceTest.session("D");
            r.up(c);
            UUID id = UUID.randomUUID();
            receive(r, c, new ControlMessage(MessageType.ROUTE_TRACE, id, c.nodeId(), c.instanceId(),
                    new ControlMessage.Trace(STREAM, EPOCH, id, 0, List.of("D"))));
            var errors = r.to(c).stream().filter(m -> m.type() == MessageType.ERROR).toList();
            assertEquals(1, errors.size(), "TTL0 needs an explicit correlated ERROR, not silence");
            assertEquals(id, errors.getFirst().requestId());
            var p = assertInstanceOf(ControlMessage.ErrorPayload.class, errors.getFirst().payload());
            assertEquals(ErrorCode.TIMEOUT, p.code());
            assertEquals(15, p.relatedType());
            assertEquals("TTL_EXHAUSTED", p.detail());
            assertTrue(traces(r).isEmpty());
        }
    }

    @Test
    void t06TimeInAdmissionQueueConsumesEntireGlobalTraceDeadline() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            var ticket = request(r);
            r.clock.addAndGet(Duration.ofMillis(3000).toNanos());
            r.owner.drain();
            assertEquals(TraceState.TIMEOUT, terminal(ticket).state());
            assertTrue(traces(r).isEmpty(), "Expired admission cannot send a new trace");
        }
    }

    @Test
    void t07ResultCanArriveBeforeAdmissionFutureCompletes() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            r.hold = true;
            var ticket = request(r);
            r.owner.drain();
            assertEquals(1, traces(r).size());
            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, ticket.traceId(), a.nodeId(), a.instanceId(),
                    new ControlMessage.TraceResult(STREAM, ticket.traceId(), List.of("C", "A", "S"))));
            var result = terminal(ticket);
            assertEquals(TraceState.SUCCESS, result.state());
            assertEquals(List.of("C", "A", "S"), result.visited());
            r.sent.stream().filter(s -> s.message().type() == MessageType.ROUTE_TRACE)
                    .forEach(s -> s.result().complete(pt.uminho.esr.ott.transport.Connection.SendResult.CLOSED));
            r.owner.drain();
            assertEquals(result, terminal(ticket), "Late callback must not replace success");
        }
    }

    @Test
    void t08OldConnectionCannotCompleteOperationOnReplacementSession() {
        try (var r = new RoutingServiceTest.Rig()) {
            var old = RoutingServiceTest.session("A");
            r.up(old);
            r.metric(old, 10);
            r.ad(old, 1, 100);
            var fresh = new Neighbor.Session(old.nodeId(), old.instanceId(), UUID.randomUUID(), old.endpoint());
            r.up(fresh);
            r.metric(fresh, 10);
            r.ad(fresh, 1, 100);
            var ticket = request(r);
            r.owner.drain();
            receive(r, old, new ControlMessage(MessageType.TRACE_RESULT, ticket.traceId(), old.nodeId(), old.instanceId(),
                    new ControlMessage.TraceResult(STREAM, ticket.traceId(), List.of("C", "A", "S"))));
            assertTrue(ticket.result().isEmpty(), "Same instanceId does not make an old connection current");
            receive(r, fresh, new ControlMessage(MessageType.TRACE_RESULT, ticket.traceId(), fresh.nodeId(), fresh.instanceId(),
                    new ControlMessage.TraceResult(STREAM, ticket.traceId(), List.of("C", "A", "S"))));
            assertEquals(TraceState.SUCCESS, terminal(ticket).state());
        }
    }

    @Test
    void t10CancellationHasOneTerminalValueAndDoesNotSendFromQueue() {
        try (var r = new RoutingServiceTest.Rig()) {
            var ticket = request(r);
            ticket.cancel();
            ticket.cancel();
            r.owner.drain();
            var result = terminal(ticket);
            assertEquals(TraceState.CANCELLED, result.state());
            r.tick(3001);
            assertEquals(result, terminal(ticket));
            assertTrue(traces(r).isEmpty());
        }
    }

    @Test
    void t11FifthLocalRequestIsRejectedBeforeOwnerAdmission() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            var admitted = new ArrayList<TraceTicket>();
            for (int i = 0; i < 4; i++) {
                admitted.add(request(r));
            }
            assertEquals(4, r.owner.queue.size());
            var excess = request(r);
            assertEquals(TraceState.RESOURCE_LIMIT, terminal(excess).state());
            assertEquals(4, r.owner.queue.size(), "Rejected command must not occupy owner queue");
        }
    }

    @Test
    void facadeBeforeStartHasTerminalNotRunningWithoutCreatingOwner() {
        var node = new OverlayNode(RoutingServiceTest.config("C", false, 4096, 4096, 10),
                UUID.randomUUID(), event -> fail("No events before start"));
        var ticket = assertDoesNotThrow(() -> node.requestTrace(STREAM), "Unstarted facade needs bounded terminal value");
        assertEquals(TraceState.NOT_RUNNING, terminal(ticket).state());
        assertEquals(RoutingService.Phase.NEW, node.routingSnapshot().diagnostics().phase());
    }

    @Test
    void t14PublicCliHelpAdvertisesApprovedTraceAndSnapshotCommands() {
        var output = new ByteArrayOutputStream();
        var errors = new ByteArrayOutputStream();
        assertEquals(0, OttApplication.run(new String[]{"--help"}, InputStream.nullInputStream(),
                new PrintStream(output, true, StandardCharsets.UTF_8), new PrintStream(errors)));
        String help = output.toString(StandardCharsets.UTF_8);
        for (String command : List.of("trace", "routes", "catalog", "metrics")) {
            assertTrue(help.contains(command), "Missing CLI diagnostic command: " + command);
        }
        assertEquals("", errors.toString(StandardCharsets.UTF_8));
    }

    private static RoutingService.TraceLoad load(RoutingServiceTest.Rig r) {
        var value = new java.util.concurrent.atomic.AtomicReference<RoutingService.TraceLoad>();
        r.owner.run(() -> value.set(r.router.traceLoad()));
        return value.get();
    }

    private static void pingTrace(RoutingServiceTest.Rig r, Neighbor.Session peer, UUID id, int ttl, List<String> path) {
        receive(r, peer, new ControlMessage(MessageType.ROUTE_TRACE, id, peer.nodeId(), peer.instanceId(),
                new ControlMessage.Trace(STREAM, EPOCH, id, ttl, path)));
    }

    @Test
    void t06Boundary2999Then3000ExpiresWithoutNewAttempt() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            var ticket = request(r);
            r.owner.drain();
            assertEquals(1, traces(r).size());
            r.tick(2999);
            assertTrue(ticket.result().isEmpty());
            r.tick(1);
            assertEquals(TraceState.TIMEOUT, terminal(ticket).state());
            assertEquals(1, traces(r).size());
            assertEquals(0, load(r).contexts());
            assertEquals(0, load(r).reservations());
        }
    }

    @Test
    void t06LateResultAfterGlobalTimeoutCannotReplaceTerminalOutcome() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            var ticket = request(r);
            r.owner.drain();
            assertEquals(1, traces(r).size());

            r.tick(3000);
            var timeout = terminal(ticket);
            assertEquals(TraceState.TIMEOUT, timeout.state());

            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, ticket.traceId(), a.nodeId(), a.instanceId(),
                    new ControlMessage.TraceResult(STREAM, ticket.traceId(), List.of("C", "A", "S"))));
            assertEquals(timeout, terminal(ticket), "A late upstream reply cannot replace the terminal timeout");
            assertEquals(1, traces(r).size(), "A late reply must not trigger another trace request");
        }
    }

    @Test
    void t03ReturnUsesStoredDownstreamAndNeverAnAlternativeRoute() {
        try (var r = new RoutingServiceTest.Rig()) {
            var down = RoutingServiceTest.session("D");
            var a = RoutingServiceTest.session("A");
            var b = RoutingServiceTest.session("B");
            for (var s : List.of(down, a, b)) {
                r.up(s);
            }r.metric(a, 10);
            r.metric(b, 20);
            r.ad(a, 1, 100);
            r.ad(b, 1, 100);
            UUID id = UUID.randomUUID();
            pingTrace(r, down, id, 31, List.of("D"));
            var sent = r.sent.stream().filter(x -> x.message().type() == MessageType.ROUTE_TRACE).findFirst().orElseThrow();
            assertEquals(a, sent.peer());
            r.metric(b, 1);
            assertEquals(b.nodeId(), r.route().knownUpstream().orElseThrow().nodeId());
            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, id, a.nodeId(), a.instanceId(),
                    new ControlMessage.TraceResult(STREAM, id, List.of("D", "C", "A", "S"))));
            var reply = r.sent.stream().filter(x -> x.message().type() == MessageType.TRACE_RESULT).toList();
            assertEquals(1, reply.size());
            assertEquals(down, reply.getFirst().peer());
            assertEquals(List.of("D", "C", "A", "S"), ((ControlMessage.TraceResult) reply.getFirst().message().payload()).visited());
            assertEquals(id, reply.getFirst().message().requestId());
        }
    }

    @Test
    void t04KnownButUnmeasuredAndExpiredIsNoRouteNotUnknown() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.ad(a, 1, 100);
            var first = request(r);
            r.owner.drain();
            assertEquals(Optional.of(ErrorCode.NO_ROUTE), terminal(first).errorCode());
            r.metric(a, 10);
            r.tick(8000);
            var second = request(r);
            r.owner.drain();
            assertEquals(Optional.of(ErrorCode.NO_ROUTE), terminal(second).errorCode());
            assertTrue(traces(r).isEmpty());
        }
    }

    @Test
    void t05SourceAtLastAllowedNodeWorksButNextNodeIsNeverAdmitted() {
        try (var r = new RoutingServiceTest.Rig("S", true, 4096, 4096, 10, new RoutingTable.Limits(64, 256, 63, 8))) {
            var peer = RoutingServiceTest.session("D");
            r.up(peer);
            var path = new ArrayList<String>();
            for (int i = 0; i < 30; i++) {
                path.add("V" + i);
            }path.add("D");
            UUID id = UUID.randomUUID();
            receive(r, peer, new ControlMessage(MessageType.ROUTE_TRACE, id, "D", peer.instanceId(),
                    new ControlMessage.Trace(STREAM, RoutingServiceTest.LOCAL, id, 1, path)));
            var result = r.to(peer).stream().filter(m -> m.type() == MessageType.TRACE_RESULT).findFirst().orElseThrow();
            assertEquals(32, ((ControlMessage.TraceResult) result.payload()).visited().size());
            var full = new ArrayList<String>(path);
            full.add(0, "EXTRA");
            UUID rejected = UUID.randomUUID();
            receive(r, peer, new ControlMessage(MessageType.ROUTE_TRACE, rejected, "D", peer.instanceId(),
                    new ControlMessage.Trace(STREAM, RoutingServiceTest.LOCAL, rejected, 1, full)));
            assertTrue(r.to(peer).stream().anyMatch(m -> m.requestId().equals(rejected) && m.payload() instanceof ControlMessage.ErrorPayload e && e.code() == ErrorCode.RESOURCE_LIMIT));
        }
    }

    @Test
    void t05RepeatedOwnIdAndUnsafeVisitedCannotForward() {
        try (var r = new RoutingServiceTest.Rig()) {
            var peer = RoutingServiceTest.session("D");
            r.up(peer);
            UUID loop = UUID.randomUUID();
            pingTrace(r, peer, loop, 20, List.of("C", "D"));
            assertTrue(r.to(peer).stream().anyMatch(m -> m.requestId().equals(loop) && m.payload() instanceof ControlMessage.ErrorPayload e && e.code() == ErrorCode.LOOP_DETECTED));
            UUID repeated = UUID.randomUUID();
            pingTrace(r, peer, repeated, 20, List.of("D", "D"));
            assertTrue(r.to(peer).stream().anyMatch(m -> m.requestId().equals(repeated) && m.payload() instanceof ControlMessage.ErrorPayload e && e.code() == ErrorCode.LOOP_DETECTED));
            int count = r.sent.size();
            for (var path : List.of(List.of("X"), List.of("bad!", "D"), Collections.nCopies(33, "D"))) {
                pingTrace(r, peer, UUID.randomUUID(), 20, path);
            }
            assertEquals(count, r.sent.size());
            assertEquals(0, load(r).contexts());
        }
    }

    @Test
    void t09WrongIdentityStreamPrefixAndUnrelatedErrorNeverCompleteTicket() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            var ticket = request(r);
            r.owner.drain();
            UUID id = ticket.traceId();
            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, id, "A", UUID.randomUUID(), new ControlMessage.TraceResult(STREAM, id, List.of("C", "A", "S"))));
            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, id, "A", a.instanceId(), new ControlMessage.TraceResult(RoutingServiceTest.OTHER, id, List.of("C", "A", "S"))));
            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, id, "A", a.instanceId(), new ControlMessage.TraceResult(STREAM, id, List.of("OTHER", "A", "S"))));
            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, id, "A", a.instanceId(), new ControlMessage.TraceResult(STREAM, UUID.randomUUID(), List.of("C", "A", "S"))));
            receive(r, a, new ControlMessage(MessageType.ERROR, id, "A", a.instanceId(), new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE, 9, "unrelated")));
            assertTrue(ticket.result().isEmpty());
            receive(r, a, new ControlMessage(MessageType.ERROR, id, "A", a.instanceId(), new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE, 15, "NO_ROUTE")));
            assertEquals(Optional.of(ErrorCode.NO_ROUTE), terminal(ticket).errorCode());
        }
    }

    @Test
    void t08EpochChangeAndExactSessionDownHaveExplicitTerminalErrors() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            var ticket = request(r);
            r.owner.drain();
            r.ad(a, STREAM, new UUID(9, 9), 0, 100, List.of("S", "A"), "Video A", 25);
            assertEquals(Optional.of(ErrorCode.PATH_CHANGED), terminal(ticket).errorCode());
            var next = request(r);
            r.owner.drain();
            r.down(a);
            assertEquals(Optional.of(ErrorCode.NO_ROUTE), terminal(next).errorCode());
        }
    }

    @Test
    void t10DuplicateDoesNotExtendLifetimeOrForwardTwice() {
        try (var r = new RoutingServiceTest.Rig()) {
            var d = RoutingServiceTest.session("D");
            var a = RoutingServiceTest.session("A");
            r.up(d);
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            UUID id = UUID.randomUUID();
            pingTrace(r, d, id, 31, List.of("D"));
            assertEquals(1, traces(r).size());
            r.tick(2999);
            pingTrace(r, d, id, 31, List.of("D"));
            assertEquals(1, traces(r).size());
            r.tick(1);
            assertEquals(0, load(r).contexts());
        }
    }

    @Test
    void t11PerSessionQuotaIncludesTerminalTombstones() {
        try (var r = new RoutingServiceTest.Rig()) {
            var d = RoutingServiceTest.session("D");
            r.up(d);
            for (int i = 0; i < 5; i++) {
                pingTrace(r, d, UUID.randomUUID(), 31, List.of("D"));
            }
            assertEquals(4, load(r).contexts());
            assertEquals(4, load(r).reservations());
            assertTrue(r.to(d).getLast().payload() instanceof ControlMessage.ErrorPayload e && e.code() == ErrorCode.RESOURCE_LIMIT);
            r.tick(3000);
            assertEquals(0, load(r).reservations());
        }
    }

    @Test
    void t11GlobalContextCapAndFiniteFairSweepAcrossSixtyFourContexts() {
        try (var r = new RoutingServiceTest.Rig()) {
            for (int n = 0; n < 17; n++) {
                var peer = RoutingServiceTest.session("D" + n);
                r.up(peer);
                for (int k = 0; k < 4; k++) {
                    pingTrace(r, peer, UUID.randomUUID(), 31, List.of(peer.nodeId()));
                }
            }
            assertEquals(64, load(r).contexts());
            assertEquals(64, load(r).reservations());
            assertTrue(load(r).timers() <= 2);
            r.tick(3000);
            assertEquals(0, load(r).contexts());
        }
    }

    @Test
    void t11PhysicalSendCapacitySurvivesTimeAndReleasesOnlyAfterCallback() {
        try (var r = new RoutingServiceTest.Rig()) {
            var d = RoutingServiceTest.session("D");
            r.up(d);
            r.hold = true;
            for (int i = 0; i < 65; i++) {
                pingTrace(r, d, UUID.randomUUID(), 0, List.of("D"));
            }
            assertEquals(64, r.sent.size());
            assertEquals(64, load(r).sends());
            r.tick(3001);
            assertEquals(64, load(r).sends());
            for (var send : r.sent) {
                send.result().complete(pt.uminho.esr.ott.transport.Connection.SendResult.CLOSED);
            }
            r.owner.drain();
            assertEquals(0, load(r).sends());
            pingTrace(r, d, UUID.randomUUID(), 0, List.of("D"));
            assertEquals(65, r.sent.size());
        }
    }

    @Test
    void t07SendFailuresAreExplicitWithoutAckOrSubscription() {
        for (var value : pt.uminho.esr.ott.transport.Connection.SendResult.values()) {
            if (value == pt.uminho.esr.ott.transport.Connection.SendResult.ACCEPTED) {
                continue;
            }
            try (var r = new RoutingServiceTest.Rig()) {
                var a = RoutingServiceTest.session("A");
                r.up(a);
                r.metric(a, 10);
                r.ad(a, 1, 100);
                r.next = value;
                var ticket = request(r);
                r.owner.drain();
                assertEquals(Optional.of(ErrorCode.NO_ROUTE), terminal(ticket).errorCode());
                assertTrue(r.route().activeUpstream().isEmpty());
                assertTrue(r.route().downstreams().isEmpty());
                assertTrue(r.sent.stream().noneMatch(x -> x.message().type().code() >= 11 && x.message().type().code() <= 14));
            }
        }
    }

    @Test
    void t17PendingPhysicalAdmissionCannotBeDeclaredTerminatedOnLogicalClose() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            r.hold = true;
            var ticket = request(r);
            r.owner.drain();
            r.router.requestClose();
            r.owner.drain();
            assertEquals(TraceState.CLOSED, terminal(ticket).state());
            assertFalse(r.router.awaitTermination(Duration.ZERO), "Live callback keeps physical reservation");
            for (var send : r.sent) {
                send.result().complete(pt.uminho.esr.ott.transport.Connection.SendResult.CLOSED);
            }
            r.owner.drain();
            assertTrue(r.router.awaitTermination(Duration.ofMillis(20)));
        }
    }

    @Test
    void t12OwnerRejectionResolvesReservedTicketAndRunsCleanup() {
        try (var r = new RoutingServiceTest.Rig()) {
            r.owner.reject = true;
            var ticket = r.router.requestTrace(STREAM);
            assertEquals(TraceState.CLOSED, terminal(ticket).state());
            assertNotNull(r.owner.failure);
            assertEquals(RoutingService.Phase.STOPPED, r.router.snapshot().diagnostics().phase());
            assertEquals(0, load(r).reservations());
        }
    }

    @Test
    void t13TracePreservesGenerationValidityMetricAndDemandSnapshots() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            var before = r.route();
            var ticket = request(r);
            r.owner.drain();
            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, ticket.traceId(), "A", a.instanceId(), new ControlMessage.TraceResult(STREAM, ticket.traceId(), List.of("C", "A", "S"))));
            assertEquals(before, r.route());
            assertEquals(TraceState.SUCCESS, terminal(ticket).state());
            assertTrue(r.sent.stream().noneMatch(x -> x.message().type().code() >= 11 && x.message().type().code() <= 14));
        }
    }

    static final class CliProcess implements AutoCloseable {

        final Process process;
        final java.nio.file.Path log;
        final BufferedWriter input;

        CliProcess(java.nio.file.Path profile, String role, java.nio.file.Path log) throws Exception {
            this.log = log;
            var command = List.of(java.nio.file.Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp",
                    System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                    OttApplication.class.getName(), "--role", role, "--config", profile.toString());
            process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        }

        void send(String text) throws Exception {
            input.write(text);
            input.newLine();
            input.flush();
        }

        String text() throws Exception {
            return java.nio.file.Files.exists(log) ? java.nio.file.Files.readString(log) : "";
        }

        void until(java.util.function.Predicate<String> condition) throws Exception {
            long began = System.nanoTime();
            while (!condition.test(text())) {
                assertTrue(process.isAlive(), "Unexpected child exit: " + text());
                assertTrue(System.nanoTime() - began < java.util.concurrent.TimeUnit.SECONDS.toNanos(10), "CLI condition deadline: " + text());
                Thread.sleep(10);
            }
        }

        int exited() throws Exception {
            assertTrue(process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS), "child exit deadline: " + text());
            int value = process.exitValue();
            java.nio.file.Files.writeString(log.resolveSibling(log.getFileName() + ".exit"), Integer.toString(value));
            return value;
        }

        public void close() throws Exception {
            if (process.isAlive()) {
                process.destroy();
            }
            if (!process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
            assertTrue(process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS));
            input.close();
        }
    }

    static final class CliGraph implements AutoCloseable {

        final java.nio.file.Path directory;
        final int[] ports = new int[9];
        final Map<String, java.nio.file.Path> profiles = new LinkedHashMap<>();
        final Map<String, CliProcess> children = new LinkedHashMap<>();
        final List<java.net.DatagramSocket> data = new ArrayList<>();
        static final String[] IDS = {"S", "R1", "R2", "C"};
        static final UUID VIDEO = new UUID(0x4000, 1);

        /**
         * Returns a minimal valid JPEG (2×2 pixels) for use as a fixture frame.
         */
        static byte[] minimalJpeg() throws IOException {
            var img = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
            var out = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "jpg", out);
            return out.toByteArray();
        }

        CliGraph() throws Exception {
            this(2);
        }

        CliGraph(int streamCount) throws Exception {
            directory = java.nio.file.Path.of(System.getProperty("ott.trace.evidence", "target/imp010-evidence"))
                    .toAbsolutePath().resolve("cli-" + UUID.randomUUID());
            java.nio.file.Files.createDirectories(directory);
            var reserved = new ArrayList<java.net.ServerSocket>();
            try {
                for (int i = 0; i < ports.length; i++) {
                    var socket = new java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress());
                    reserved.add(socket);
                    ports[i] = socket.getLocalPort();
                }
                StringBuilder topology = new StringBuilder("node.ids=S,R1,R2,C\n");
                for (int i = 0; i < IDS.length; i++) {
                    String id = IDS[i], role = i == 0 ? "SERVER" : i == 3 ? "CLIENT" : "RELAY";
                    topology.append("node.").append(id).append(".host=127.0.0.1\nnode.").append(id).append(".controlPort=").append(ports[1 + i])
                            .append("\nnode.").append(id).append(".dataPort=").append(ports[5 + i])
                            .append("\nnode.").append(id).append(".role=").append(role)
                            .append("\nnode.").append(id).append(".neighbors=").append(i == 0 || i == 3 ? "R1,R2" : "S,C").append('\n');
                    var file = directory.resolve(id + ".properties");
                    profiles.put(id, file);
                    java.nio.file.Files.writeString(file, "node.id=" + id + "\nnode.role=" + role + "\nbind.host=127.0.0.1\nadvertised.host=127.0.0.1\ncontrol.port=" + ports[1 + i]
                            + "\ndata.port=" + ports[5 + i] + "\nbootstrap.host=127.0.0.1\nbootstrap.port=" + ports[0]
                            + "\ncontrol.shutdown.timeout.ms=1500\ncontrol.connect.timeout.ms=300\noverlay.retry.initial.ms=50\noverlay.retry.max.ms=200\noverlay.retry.jitter.percent=0\n"
                            + (i == 0 ? "streams.file=streams.properties\nmedia.base=" + directory + "\n" : ""));
                }
                java.nio.file.Files.writeString(directory.resolve("topology.properties"), topology.toString());
                StringBuilder streams = new StringBuilder("stream.ids=");
                for (int i = 1; i <= streamCount; i++) {
                    if (i > 1) {
                        streams.append(',');
                    }streams.append(new UUID(0x4000, i));
                }
                streams.append('\n');
                for (int i = 1; i <= streamCount; i++) {
                    streams.append("stream.").append(new UUID(0x4000, i)).append(".title=Live \\nINJECT ").append(i)
                            .append("\nstream.").append(new UUID(0x4000, i)).append(".file=missing").append(i).append(".Mjpeg\nstream.")
                            .append(new UUID(0x4000, i)).append(".fps=25\nstream.").append(new UUID(0x4000, i)).append(".loop=false\n");
                }
                java.nio.file.Files.writeString(directory.resolve("streams.properties"), streams.toString());
                // Fix RouteTraceTest fixtures: write minimal valid .Mjpeg files so that
                // StreamingServer.prepare() qualification succeeds for the SERVER process.
                // Format: 5-digit ASCII length prefix + JPEG bytes, one record per frame.
                byte[] jpeg = minimalJpeg();
                String prefix = String.format(java.util.Locale.ROOT, "%05d", jpeg.length);
                byte[] prefixBytes = prefix.getBytes(StandardCharsets.US_ASCII);
                for (int i = 1; i <= streamCount; i++) {
                    try (var out = java.nio.file.Files.newOutputStream(directory.resolve("missing" + i + ".Mjpeg"))) {
                        out.write(prefixBytes);
                        out.write(jpeg); // frame 0
                        out.write(prefixBytes);
                        out.write(jpeg); // frame 1
                    }
                }
                var bootstrap = directory.resolve("BOOT.properties");
                profiles.put("BOOT", bootstrap);
                java.nio.file.Files.writeString(bootstrap, "node.id=BOOT\nnode.role=BOOTSTRAP\nbind.host=127.0.0.1\nadvertised.host=127.0.0.1\ncontrol.port=" + ports[0] + "\ntopology.file=topology.properties\ncontrol.shutdown.timeout.ms=1500\n");
            } finally {
                for (var socket : reserved) {
                    socket.close();
                }}
            for (int i = 5; i < 9; i++) {
                var socket = new java.net.DatagramSocket(ports[i], java.net.InetAddress.getLoopbackAddress());
                socket.setSoTimeout(80);
                data.add(socket);
            }
        }

        CliProcess start(String id) throws Exception {
            var child = new CliProcess(profiles.get(id), id.equals("BOOT") ? "BOOTSTRAP" : id.equals("S") ? "SERVER" : id.equals("C") ? "CLIENT" : "RELAY", directory.resolve(id + ".log"));
            children.put(id, child);
            child.until(t -> t.contains("READY"));
            return child;
        }

        void assertNoVideo() throws Exception {
            for (var socket : data) {
                assertThrows(java.net.SocketTimeoutException.class, () -> socket.receive(new java.net.DatagramPacket(new byte[1200], 1200)), "No video datagram in observed diagnostic interval");
            }
            for (var child : children.values()) {
                String text = child.text();
                for (String type : List.of("type=ACTIVATE,", "type=ACTIVATE_ACK,", "type=DEACTIVATE,", "type=SUBSCRIPTION_RENEW,")) {
                    assertFalse(text.contains(type), text);
                }
                assertFalse(text.lines().anyMatch(line -> line.startsWith("INJECT")), "Titles must not inject an event/log line: " + text);
                assertFalse(text.contains("FATAL"), text);
                assertFalse(text.contains("OUTPUT_ERROR"), text);
            }
        }

        public void close() throws Exception {
            Exception failure = null;
            for (var child : children.values())try {
                child.close();
            } catch (Exception e) {
                failure = e;
            }
            for (var socket : data) {
                socket.close();
            }
            for (int port : ports)try (var socket = new java.net.ServerSocket()) {
                socket.setReuseAddress(true);
                socket.bind(new java.net.InetSocketAddress("127.0.0.1", port));
                assertTrue(socket.isBound());
            }
            if (failure != null) {
                throw failure;
            }
        }
    }

    @Test
    @Timeout(60)
    void t15T16RealCliTcpDiamondAlternativeEofAndNoVideo() throws Exception {
        try (var graph = new CliGraph()) {
            var boot = graph.start("BOOT");
            var source = graph.start("S");
            var r1 = graph.start("R1");
            var r2 = graph.start("R2");
            var client = graph.start("C");
            for (var child : List.of(source, r1, r2, client)) {
                child.until(t -> t.lines().filter(l -> l.contains("NeighborUp[")).count() >= 2);
            }
            client.until(t -> t.contains("type=STREAM_ANNOUNCE") && t.contains("type=PROBE_PONG"));
            long began = System.nanoTime();
            String rows = "";
            do {
                int length = client.text().length();
                client.send("routes");
                client.until(t -> t.substring(Math.min(length, t.length())).contains("ROUTES_END"));
                rows = client.text().substring(length);
                if (rows.contains("stream=" + CliGraph.VIDEO) && rows.contains("state=INACTIVE")) {
                    break;
                }
                assertTrue(System.nanoTime() - began < java.util.concurrent.TimeUnit.SECONDS.toNanos(8));
                Thread.sleep(50);
            } while (true);
            client.send("catalog");
            client.until(t -> t.contains("CATALOG_END"));
            client.send("metrics");
            client.until(t -> t.contains("METRICS_END"));
            client.send("trace " + CliGraph.VIDEO);
            client.until(t -> t.contains("TRACE_RESULT stream=" + CliGraph.VIDEO));
            var match = java.util.regex.Pattern.compile("TRACE_RESULT stream=" + CliGraph.VIDEO + ".*visited=\\[C, (R[12]), S\\]").matcher(client.text());
            assertTrue(match.find(), client.text());
            String chosen = match.group(1);
            String other = chosen.equals("R1") ? "R2" : "R1";
            boot.send("quit");
            assertEquals(0, boot.exited());
            graph.children.get(chosen).process.destroyForcibly();
            int killed = graph.children.get(chosen).exited();
            assertNotEquals(0, killed);
            client.until(t -> t.contains("NeighborDown[session=Session[nodeId=" + chosen));
            Thread.sleep(150);
            int previous = client.text().length();
            client.send("trace " + CliGraph.VIDEO);
            client.until(t -> t.substring(Math.min(previous, t.length())).contains("TRACE_RESULT stream=" + CliGraph.VIDEO));
            assertTrue(client.text().substring(previous).contains("visited=[C, " + other + ", S]"), client.text());
            int errorAt = client.text().length();
            client.send("trace " + UUID.randomUUID());
            client.until(t -> t.substring(Math.min(errorAt, t.length())).contains("UNKNOWN_STREAM"));
            client.send("trace bad");
            client.until(t -> t.contains("COMMAND_ERROR trace requires"));
            graph.assertNoVideo();
            for (var entry : graph.children.entrySet()) {
                if (entry.getValue().process.isAlive()) {
                    entry.getValue().send("quit");
                    assertEquals(0, entry.getValue().exited(), entry.getValue().text());
                }
            }
            java.nio.file.Files.writeString(graph.directory.resolve("result.json"), "{\"real_cli_tcp\":true,\"first_hop\":\"" + chosen + "\",\"alternative\":\"" + other + "\",\"killed_exit\":" + killed + ",\"all_processes_terminated\":true,\"udp_observed\":0}\n");
        }
    }

    @Test
    void t02EveryHopReadsItsOwnTableInsteadOfReversingOriginalAdvertisement() {
        try (var client = new RoutingServiceTest.Rig(); var relay = new RoutingServiceTest.Rig("A", false, 4096, 4096, 10, new RoutingTable.Limits(64, 256, 63, 8)); var source = new RoutingServiceTest.Rig("S", true, 4096, 4096, 10, new RoutingTable.Limits(64, 256, 63, 8))) {
            var a = liveSession("A", NodeRole.RELAY);
            var c = liveSession("C", NodeRole.CLIENT);
            var s = liveSession("S", NodeRole.SERVER);
            client.up(a);
            client.metric(a, 10);
            client.ad(a, STREAM, RoutingServiceTest.LOCAL, 1, 20, List.of("S", "X", "A"), "Video A", 25);
            relay.up(c);
            relay.up(s);
            relay.metric(s, 10);
            relay.ad(s, STREAM, RoutingServiceTest.LOCAL, 1, 0, List.of("S"), "Video A", 25);
            source.up(a);
            assertEquals(List.of("S", "X", "A", "C"), client.route().selectedCandidate().orElseThrow().path());
            client.delivery = (p, m) -> {
                if (m.type() == MessageType.ROUTE_TRACE) {
                    receive(relay, c, m);
                }};
            relay.delivery = (p, m) -> {
                if (m.type() == MessageType.ROUTE_TRACE) {
                    receive(source, a, m);
                }else if (m.type() == MessageType.TRACE_RESULT) {
                    receive(client, a, m);
                }};
            source.delivery = (p, m) -> {
                if (m.type() == MessageType.TRACE_RESULT) {
                    receive(relay, s, m);
                }};
            var ticket = request(client);
            client.owner.drain();
            assertEquals(TraceState.SUCCESS, terminal(ticket).state());
            assertEquals(List.of("C", "A", "S"), terminal(ticket).visited());
            assertEquals(s, relay.sent.stream().filter(x -> x.message().type() == MessageType.ROUTE_TRACE).findFirst().orElseThrow().peer());
        }
    }

    private static Neighbor.Session liveSession(String id, NodeRole role) {
        return new Neighbor.Session(id, RoutingServiceTest.LOCAL, UUID.randomUUID(),
                new ControlMessage.Endpoint(id, RoutingServiceTest.LOCAL, "127.0.0.1", 19001, 19101, role));
    }

    @Test
    void t07ThrownAndNullPortResultsFailOnlyTheDiagnostic() {
        for (boolean throwsFailure : List.of(false, true))try (var fixture = new RoutingServiceTest.Rig()) {
            var router = new RoutingService(RoutingServiceTest.config("C", false, 4096, 4096, 10), RoutingServiceTest.LOCAL, fixture.owner,
                    (p, m) -> {
                        if (throwsFailure) {
                            throw new IllegalStateException("injected send failure");
                        }return null;
                    }, event -> {
                    }, fixture.clock::get, fixture.timer);
            try {
                fixture.owner.run(router::start);
                var a = RoutingServiceTest.session("A");
                fixture.owner.run(() -> {
                    router.onNeighborEvent(new NeighborManager.NeighborUp(a));
                    router.onMetrics(new pt.uminho.esr.ott.monitoring.NetworkMonitor.Updated(new pt.uminho.esr.ott.monitoring.LinkMetrics(a,
                            pt.uminho.esr.ott.monitoring.LinkMetrics.State.MEASURED, OptionalDouble.of(10), OptionalLong.of(10), 1, false, OptionalLong.of(0), 0)));
                    router.onNeighborEvent(new NeighborManager.Received(a, new ControlMessage(MessageType.STREAM_ANNOUNCE, UUID.randomUUID(), "A", a.instanceId(),
                            new ControlMessage.Announcement(new ControlMessage.StreamInfo(STREAM, "S", "Video A", 1, 25, EPOCH), 1, 100, List.of("S", "A")))));
                });
                var ticket = router.requestTrace(STREAM);
                fixture.owner.drain();
                assertEquals(Optional.of(ErrorCode.NO_ROUTE), terminal(ticket).errorCode());
                assertEquals(RoutingService.Phase.RUNNING, router.snapshot().diagnostics().phase());
            } finally {
                fixture.owner.run(router::requestClose);
            }
        }
    }

    @Test
    void t11BlockedResponseIntentRemainsBoundedUntilPhysicalQuotaFrees() {
        try (var r = new RoutingServiceTest.Rig()) {
            var d = RoutingServiceTest.session("D");
            r.up(d);
            r.hold = true;
            for (int i = 0; i < 64; i++) {
                pingTrace(r, d, UUID.randomUUID(), 0, List.of("D"));
            }
            UUID request = UUID.randomUUID();
            pingTrace(r, d, request, 31, List.of("D"));
            assertEquals(1, load(r).responseIntents());
            assertEquals(64, r.sent.size());
            r.sent.getFirst().result().complete(pt.uminho.esr.ott.transport.Connection.SendResult.ACCEPTED);
            r.owner.drain();
            assertEquals(0, load(r).responseIntents());
            assertEquals(65, r.sent.size());
            assertEquals(request, r.sent.getLast().message().requestId());
            assertEquals(64, load(r).sends());
            pingTrace(r, d, request, 31, List.of("D"));
            assertEquals(65, r.sent.size());
        }
    }

    @Test
    void t17RunningTimerAndPhysicalCallbackMustBothFinish() {
        try (var r = new RoutingServiceTest.Rig()) {
            var a = RoutingServiceTest.session("A");
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            r.hold = true;
            var ticket = request(r);
            r.owner.drain();
            r.timer.pin = true;
            r.router.requestClose();
            r.owner.drain();
            assertEquals(TraceState.CLOSED, terminal(ticket).state());
            assertFalse(r.router.awaitTermination(Duration.ZERO));
            for (var send : r.sent) {
                send.result().complete(pt.uminho.esr.ott.transport.Connection.SendResult.CLOSED);
            }
            assertFalse(r.router.awaitTermination(Duration.ZERO), "Cancelled but physically live job still counts");
            r.timer.pin = false;
            r.timer.requestClose();
            assertTrue(r.router.awaitTermination(Duration.ofMillis(20)));
        }
    }

    @Test
    void t14OversizedOutcomeIsRejectedBeforeReadingOrCopyingList() {
        List<String> adversarial = new AbstractList<>() {
            public int size() {
                return 33;
            }

            public String get(int index) {
                throw new AssertionError("Must check size before copying oversized input");
            }
        };
        assertThrows(IllegalArgumentException.class, () -> new TraceOutcome(STREAM, EPOCH, Optional.empty(), TraceState.ERROR, Optional.empty(), "", adversarial, 0));
    }

    @Test
    @Timeout(30)
    void t14LargeRealCliListingEscapesTitlesYieldsToQuitAndRejectsSecondCursor() throws Exception {
        try (var graph = new CliGraph(256)) {
            var boot = graph.start("BOOT");
            var source = graph.start("S");
            source.send("catalog");
            source.send("routes");
            source.send("quit");
            assertEquals(0, source.exited(), source.text());
            String output = source.text();
            assertTrue(output.contains("diagnostic listing BUSY"), output);
            assertTrue(output.contains("CATALOG_STREAM"), output);
            assertTrue(output.contains("title=Live \\nINJECT"), output);
            assertTrue(output.lines().filter(l -> l.startsWith("CATALOG_STREAM")).count() < 256, "Listing must yield before eagerly formatting every row");
            assertTrue(output.lines().allMatch(l -> l.length() <= OttApplication.MAX_LOG_CHARS), output);
            boot.send("quit");
            assertEquals(0, boot.exited());
        }
    }

    @Test
    void t17ClockFailuresAtAdmissionAndDispatchCannotLeakUnsentReservations() {
        // Inject at successive observations, rather than assuming a private number/order of reads.
        for (int observation = 2; observation <= 20; observation++)try (var fixture = new RoutingServiceTest.Rig()) {
            var remaining = new java.util.concurrent.atomic.AtomicInteger();
            java.util.function.LongSupplier clock = () -> {
                if (remaining.get() > 0 && remaining.decrementAndGet() == 0) {
                    throw new IllegalStateException("injected clock failure");
                }
                return fixture.clock.get();
            };
            var router = new RoutingService(RoutingServiceTest.config("C", false, 4096, 4096, 10), RoutingServiceTest.LOCAL,
                    fixture.owner, (peer, message) -> java.util.concurrent.CompletableFuture.completedFuture(pt.uminho.esr.ott.transport.Connection.SendResult.ACCEPTED),
                    event -> {
                    }, clock, fixture.timer);
            try {
                fixture.owner.run(router::start);
                var a = RoutingServiceTest.session("A");
                fixture.owner.run(() -> {
                    router.onNeighborEvent(new NeighborManager.NeighborUp(a));
                    router.onMetrics(new pt.uminho.esr.ott.monitoring.NetworkMonitor.Updated(new pt.uminho.esr.ott.monitoring.LinkMetrics(a,
                            pt.uminho.esr.ott.monitoring.LinkMetrics.State.MEASURED, OptionalDouble.of(10), OptionalLong.of(10), 1, false, OptionalLong.of(0), 0)));
                    router.onNeighborEvent(new NeighborManager.Received(a, new ControlMessage(MessageType.STREAM_ANNOUNCE, UUID.randomUUID(), "A", a.instanceId(),
                            new ControlMessage.Announcement(new ControlMessage.StreamInfo(STREAM, "S", "Video A", 1, 25, EPOCH), 1, 100, List.of("S", "A")))));
                });
                remaining.set(observation);
                var ticket = router.requestTrace(STREAM);
                assertDoesNotThrow(fixture.owner::drain, "Clock failure must use bounded cleanup, never escape the owner command");
                remaining.set(0);
                fixture.owner.run(router::requestClose);
                fixture.owner.drain();
                assertTrue(ticket.result().isPresent());
                assertTrue(router.awaitTermination(Duration.ofMillis(10)), "No future/callback was ever left unresolved by this port");
            } finally {
                remaining.set(0);
                fixture.owner.run(router::requestClose);
            }
        }
    }

    private static void signal(CliProcess child, String signal) throws Exception {
        var command = new ProcessBuilder("/bin/kill", "-" + signal, Long.toString(child.process.pid())).start();
        assertTrue(command.waitFor(2, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(0, command.exitValue());
    }

    @Test
    @Timeout(50)
    void t16RealTcpEofDuringPendingTraceTerminatesWithoutWaitingForOrigin() throws Exception {
        try (var graph = new CliGraph()) {
            graph.start("BOOT");
            var source = graph.start("S");
            var a = graph.start("R1");
            var b = graph.start("R2");
            var client = graph.start("C");
            for (var node : List.of(source, a, b, client)) {
                node.until(t -> t.lines().filter(l -> l.contains("NeighborUp[")).count() >= 2);
            }
            client.until(t -> t.contains("STREAM_ANNOUNCE") && t.contains("PROBE_PONG"));
            long began = System.nanoTime();
            while (true) {
                int offset = client.text().length();
                client.send("routes");
                client.until(t -> t.substring(Math.min(offset, t.length())).contains("ROUTES_END"));
                if (client.text().substring(offset).contains("state=INACTIVE")) {
                    break;
                }
                assertTrue(System.nanoTime() - began < java.util.concurrent.TimeUnit.SECONDS.toNanos(8));
                Thread.sleep(50);
            }
            signal(source, "STOP");
            try {
                int ao = a.text().length(), bo = b.text().length(), co = client.text().length();
                client.send("trace " + CliGraph.VIDEO);
                long waiting = System.nanoTime();
                CliProcess selected = null;
                while (selected == null) {
                    if (a.text().substring(ao).contains("direction=request")) {
                        selected = a;
                    }else if (b.text().substring(bo).contains("direction=request")) {
                        selected = b;
                    }else {
                        assertTrue(System.nanoTime() - waiting < java.util.concurrent.TimeUnit.SECONDS.toNanos(2), "Trace must reach a real relay");
                        Thread.sleep(5);
                    }
                }
                assertFalse(client.text().substring(co).contains("TRACE_RESULT stream=" + CliGraph.VIDEO));
                selected.process.destroyForcibly();
                assertNotEquals(0, selected.exited());
                client.until(t -> t.substring(Math.min(co, t.length())).contains("TRACE_ERROR stream=" + CliGraph.VIDEO));
                String error = client.text().substring(co);
                assertTrue(error.contains("NO_ROUTE"), error);
                assertTrue(client.process.isAlive());
            } finally {
                signal(source, "CONT");
            }
            graph.assertNoVideo();
            for (var child : graph.children.values()) {
                if (child.process.isAlive()) {
                    child.send("quit");
                    assertEquals(0, child.exited(), child.text());
                }
            }
        }
    }

    @Test
    void t08ReconnectedDownstreamCannotReceiveAnOldResult() {
        try (var r = new RoutingServiceTest.Rig()) {
            var old = RoutingServiceTest.session("D");
            var a = RoutingServiceTest.session("A");
            r.up(old);
            r.up(a);
            r.metric(a, 10);
            r.ad(a, 1, 100);
            UUID first = UUID.randomUUID();
            pingTrace(r, old, first, 31, List.of("D"));
            var replacement = new Neighbor.Session("D", old.instanceId(), UUID.randomUUID(), old.endpoint());
            r.up(replacement);
            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, first, "A", a.instanceId(), new ControlMessage.TraceResult(STREAM, first, List.of("D", "C", "A", "S"))));
            assertTrue(r.to(replacement).stream().noneMatch(m -> m.type() == MessageType.TRACE_RESULT || m.type() == MessageType.ERROR));
            UUID second = UUID.randomUUID();
            pingTrace(r, replacement, second, 31, List.of("D"));
            receive(r, a, new ControlMessage(MessageType.TRACE_RESULT, second, "A", a.instanceId(), new ControlMessage.TraceResult(STREAM, second, List.of("D", "C", "A", "S"))));
            assertTrue(r.to(replacement).stream().anyMatch(m -> m.type() == MessageType.TRACE_RESULT && m.requestId().equals(second)));
        }
    }

    @Test
    void t08NewInstanceClosesOldTicketAndCannotInheritItsReply() {
        try (var r = new RoutingServiceTest.Rig()) {
            var old = RoutingServiceTest.session("A");
            r.up(old);
            r.metric(old, 10);
            r.ad(old, 1, 100);
            var first = request(r);
            r.owner.drain();
            var fresh = RoutingServiceTest.session("A");
            assertNotEquals(old.instanceId(), fresh.instanceId());
            r.up(fresh);
            r.metric(fresh, 10);
            r.ad(fresh, 1, 100);
            assertEquals(Optional.of(ErrorCode.NO_ROUTE), terminal(first).errorCode());
            var next = request(r);
            r.owner.drain();
            receive(r, old, new ControlMessage(MessageType.TRACE_RESULT, next.traceId(), "A", old.instanceId(), new ControlMessage.TraceResult(STREAM, next.traceId(), List.of("C", "A", "S"))));
            assertTrue(next.result().isEmpty());
            receive(r, fresh, new ControlMessage(MessageType.TRACE_RESULT, next.traceId(), "A", fresh.instanceId(), new ControlMessage.TraceResult(STREAM, next.traceId(), List.of("C", "A", "S"))));
            assertEquals(TraceState.SUCCESS, terminal(next).state());
            assertEquals(TraceState.ERROR, terminal(first).state());
        }
    }

    @Test
    void t17ExecutingCallbackRetainsPhysicalReservationAcrossCleanup() throws Exception {
        try (var fixture = new RoutingServiceTest.Rig()) {
            var inside = new java.util.concurrent.CountDownLatch(1);
            var release = new java.util.concurrent.CountDownLatch(1);
            var callback = new java.util.concurrent.atomic.AtomicReference<Thread>();
            NeighborManager.SerialOwner owner = new NeighborManager.SerialOwner() {
                public int capacity() {
                    return fixture.owner.capacity();
                }

                public boolean isOwnerThread() {
                    return fixture.owner.isOwnerThread();
                }

                public boolean offer(Runnable task) {
                    boolean accepted = fixture.owner.offer(task);
                    if (Thread.currentThread() == callback.get()) {
                        inside.countDown();
                        try {
                            release.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    return accepted;
                }

                public NeighborManager.Registration onStop(Runnable cleanup) {
                    return fixture.owner.onStop(cleanup);
                }

                public void fail(Throwable error) {
                    fixture.owner.fail(error);
                }

                public void requestClose() {
                    fixture.owner.requestClose();
                }

                public boolean awaitTermination(Duration budget) {
                    return fixture.owner.awaitTermination(budget);
                }

                public void close() {
                    fixture.owner.close();
                }
            };
            var pending = new java.util.concurrent.CompletableFuture<pt.uminho.esr.ott.transport.Connection.SendResult>();
            var router = new RoutingService(RoutingServiceTest.config("C", false, 4096, 4096, 10), RoutingServiceTest.LOCAL, owner,
                    (peer, message) -> pending, event -> {
                    }, fixture.clock::get, fixture.timer);
            try {
                fixture.owner.run(router::start);
                var a = RoutingServiceTest.session("A");
                fixture.owner.run(() -> {
                    router.onNeighborEvent(new NeighborManager.NeighborUp(a));
                    router.onMetrics(new pt.uminho.esr.ott.monitoring.NetworkMonitor.Updated(new pt.uminho.esr.ott.monitoring.LinkMetrics(a,
                            pt.uminho.esr.ott.monitoring.LinkMetrics.State.MEASURED, OptionalDouble.of(10), OptionalLong.of(10), 1, false, OptionalLong.of(0), 0)));
                    router.onNeighborEvent(new NeighborManager.Received(a, new ControlMessage(MessageType.STREAM_ANNOUNCE, UUID.randomUUID(), "A", a.instanceId(),
                            new ControlMessage.Announcement(new ControlMessage.StreamInfo(STREAM, "S", "Video A", 1, 25, EPOCH), 1, 100, List.of("S", "A")))));
                });
                var ticket = router.requestTrace(STREAM);
                fixture.owner.drain();
                callback.set(Thread.ofVirtual().unstarted(() -> pending.complete(pt.uminho.esr.ott.transport.Connection.SendResult.ACCEPTED)));
                callback.get().start();
                assertTrue(inside.await(1, java.util.concurrent.TimeUnit.SECONDS));
                router.requestClose();
                fixture.owner.drain();
                assertEquals(TraceState.CLOSED, terminal(ticket).state());
                assertFalse(router.awaitTermination(Duration.ZERO), "A completed future is not a physically finished callback");
                release.countDown();
                callback.get().join(Duration.ofSeconds(1));
                assertFalse(callback.get().isAlive());
                assertTrue(router.awaitTermination(Duration.ofMillis(20)));
            } finally {
                release.countDown();
                fixture.owner.run(router::requestClose);
                if (callback.get() != null) {
                    callback.get().join(Duration.ofSeconds(1));
                }}
        }
    }
}
