package pt.uminho.esr.ott.routing;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.LongSupplier;
import pt.uminho.esr.ott.monitoring.LinkMetrics;
import pt.uminho.esr.ott.overlay.Neighbor;
import pt.uminho.esr.ott.overlay.NodeRole;
import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.routing.RoutingTable.*;
import static org.junit.jupiter.api.Assertions.*;

class RoutingTableTest {
    static final UUID A = id("A"), A2 = id("A2"), B = id("B"), B2 = id("B2");
    static final UUID ES = id("epoch-S"), ET = id("epoch-T");
    static UUID id(String name) { return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)); }
    static final class Clock implements LongSupplier {
        long now;
        public long getAsLong() { return now; }
        void ms(long value) { now = value * 1_000_000L; }
    }
    static void applied(Outcome result) { assertEquals(Status.APPLIED, result.status(), result.toString()); }
    static void reason(Reason reason, Outcome result) { assertEquals(reason, result.reason(), result.toString()); }
    static SessionKey key(String node, String connection) { return new SessionKey(node, id(node), id(connection)); }
    static final class F {
        final Clock clock = new Clock();
        final RoutingTable t;
        F() { this("C", new Limits(64, 256, 63, 8)); }
        F(String local, Limits limits) {
            t = new RoutingTable(local, limits, 8000, 16000, clock);
            applied(t.bindStream(A, "S")); applied(t.bindStream(A2, "S"));
            applied(t.bindStream(B, "T")); applied(t.bindStream(B2, "T"));
            applied(t.confirmEpoch("S", Optional.empty(), ES));
            applied(t.confirmEpoch("T", Optional.empty(), ET));
        }
        SessionKey link(String node, long cost) {
            var s = key(node, node + "-1"); applied(t.replaceSession(Optional.empty(), s));
            applied(t.setLinkCost(s, OptionalLong.of(cost))); return s;
        }
        Advertisement ad(UUID stream, SessionKey s, long seq, long cost, String... path) {
            boolean first = stream.equals(A) || stream.equals(A2);
            return new Advertisement(stream, first ? "S" : "T", first ? ES : ET, s, seq, cost, List.of(path));
        }
        void announce(UUID stream, SessionKey s, long seq, long cost, String... path) {
            applied(t.announce(ad(stream, s, seq, cost, path)));
        }
        Route route(UUID stream) { return t.snapshot(stream).orElseThrow(); }
        long cost(UUID stream) { return route(stream).selectedMetric().orElseThrow().costMicros(); }
    }

    @Test void d01FourStreamsHaveIndependentCandidatesAndWithdrawals() {
        var f = new F(); var r = f.link("R", 10);
        f.announce(A, r, 1, 100, "S", "R"); f.announce(A2, r, 1, 200, "S", "R");
        f.announce(B, r, 1, 300, "T", "R"); f.announce(B2, r, 1, 400, "T", "R");
        var untouched = List.of(f.route(A2), f.route(B), f.route(B2));
        applied(f.t.withdraw(A, ES, r, 2));
        assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
        assertEquals(untouched, List.of(f.route(A2), f.route(B), f.route(B2)));
        assertEquals(List.of(210L, 310L, 410L), List.of(f.cost(A2), f.cost(B), f.cost(B2)));
    }
    @Test void d02LocalSourceHasZeroCostButNoDataAuthorization() {
        var f = new F("S", Limits.forNode(64)); applied(f.t.offerLocal(A, ES));
        Route view = f.route(A);
        assertEquals(new RouteMetric(0, 0), view.selectedMetric().orElseThrow());
        assertTrue(view.localSource()); assertTrue(view.selectedCandidate().isEmpty());
        assertTrue(view.knownUpstream().isEmpty()); assertTrue(view.activeUpstream().isEmpty());
        assertTrue(view.pendingUpstream().isEmpty()); assertTrue(view.downstreams().isEmpty());
        assertEquals(RouteState.INACTIVE, view.state()); f.clock.ms(1_000_000);
        assertTrue(f.route(A).localSource());
        reason(Reason.STALE_EPOCH, f.t.removeLocalOffer(A, id("old")));
        applied(f.t.removeLocalOffer(A, ES)); assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
        reason(Reason.DUPLICATE, f.t.removeLocalOffer(A, ES));
    }
    @Test void d02RemoteNodeCannotOfferAnotherServersStream() {
        var f = new F(); reason(Reason.INVALID_INPUT, f.t.offerLocal(A, ES));
        assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
    }
    @Test void d03KnownCostsWinInBothArrivalOrders() {
        for (boolean reverse : List.of(false, true)) {
            var f = new F(); var r1 = f.link("R1", 40); var r2 = f.link("R2", 50);
            var one = f.ad(A, r1, 1, 100, "S", "R1"); var two = f.ad(A, r2, 1, 80, "S", "R2");
            applied(f.t.announce(reverse ? two : one)); applied(f.t.announce(reverse ? one : two));
            assertEquals(130, f.cost(A)); assertEquals(r2, f.route(A).knownUpstream().orElseThrow());
            assertEquals(2, f.route(A).candidates().size());
        }
    }
    @Test void d04TiesUseHopsThenAsciiNodeIdAndKeepAlternatives() {
        var f = new F(); var r1 = f.link("R1", 40); var r2 = f.link("R2", 50);
        f.announce(A, r1, 1, 100, "S", "X", "R1"); f.announce(A, r2, 1, 90, "S", "R2");
        assertEquals(r2, f.route(A).knownUpstream().orElseThrow());
        f.announce(A, r1, 1, 100, "S", "R1");
        assertEquals(r1, f.route(A).knownUpstream().orElseThrow());
        assertEquals(2, f.route(A).selectedMetric().orElseThrow().hopCount());
        assertEquals(2, f.route(A).candidates().size());
    }
    @Test void d05InvalidPathsLeaveHealthyRouteUnchanged() {
        var f = new F(); var r = f.link("R", 1); f.announce(A, r, 1, 10, "S", "R");
        var healthy = f.route(A);
        for (var path : List.of(List.of("S", "C", "R"), List.of("S", "X", "X", "R"),
                List.of("T", "R"), List.of("S", "X"))) {
            reason(Reason.INVALID_INPUT, f.t.announce(new Advertisement(A, "S", ES, r, 2, 1, path)));
            assertEquals(healthy, f.route(A));
        }
    }
    @Test void d05ThirtyOneAdvertisedIdsPlusLocalFitButThirtyTwoDoNot() {
        var f = new F(); var r = f.link("R", 1); var path = new ArrayList<String>();
        path.add("S"); for (int i = 0; i < 29; i++) path.add("N" + i); path.add("R");
        applied(f.t.announce(new Advertisement(A, "S", ES, r, 1, 1, path)));
        assertEquals(32, f.route(A).selectedCandidate().orElseThrow().path().size());
        path.add(1, "extra");
        assertThrows(IllegalArgumentException.class, () -> new Advertisement(A, "S", ES, r, 2, 1, path));
    }
    @Test void d06MetricArithmeticChecksExactLongBoundary() {
        assertEquals(new RouteMetric(Long.MAX_VALUE, 2), RouteMetric.accumulate(Long.MAX_VALUE - 1, 1, 2));
        assertThrows(ArithmeticException.class, () -> RouteMetric.accumulate(Long.MAX_VALUE - 1, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> new RouteMetric(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> RouteMetric.accumulate(1, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> new RouteMetric(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new RouteMetric(1, 0));
    }
    @Test void d06OverflowingAnnouncementCannotReplaceHealthyCandidate() {
        var f = new F(); var r = f.link("R", 2); f.announce(A, r, 1, 10, "S", "R");
        var healthy = f.route(A);
        reason(Reason.INVALID_INPUT, f.t.announce(f.ad(A, r, 2, Long.MAX_VALUE - 1, "S", "R")));
        assertEquals(healthy, f.route(A));
        reason(Reason.INVALID_INPUT, f.t.announce(f.ad(A, r, 2, 0, "S", "R")));
        assertThrows(IllegalArgumentException.class, () -> f.ad(A, r, 2, -1, "S", "R"));
        var source = f.link("S", 1); f.announce(A, source, 1, 0, "S"); assertEquals(1, f.cost(A));
    }
    @Test void d07UnmeasuredCandidateWaitsForCostWithoutRenewal() {
        var f = new F(); var r = key("R", "conn"); applied(f.t.replaceSession(Optional.empty(), r));
        f.announce(A, r, 1, 10, "S", "R"); var pending = f.route(A);
        assertEquals(RouteState.UNAVAILABLE, pending.state()); assertEquals(1, pending.candidates().size());
        assertTrue(pending.candidates().getFirst().metric().isEmpty());
        f.clock.ms(7000); applied(f.t.setLinkCost(r, OptionalLong.of(3))); assertEquals(13, f.cost(A));
        f.clock.ms(8000); assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
    }
    @Test void d07MeasuredSuspectCostIsConsumedDirectlyFromRealDto() {
        var f = new F(); var r = f.link("R", 9);
        var endpoint = new ControlMessage.Endpoint("R", r.instanceId(), "127.0.0.1", 9001, 9002, NodeRole.RELAY);
        var session = new Neighbor.Session("R", r.instanceId(), r.connectionId(), endpoint);
        var metrics = new LinkMetrics(session, LinkMetrics.State.SUSPECT, OptionalDouble.of(1.2),
                OptionalLong.of(2), 1, false, OptionalLong.of(0), 1);
        applied(f.t.setLinkCost(r, metrics.costMicros())); f.announce(A, r, 1, 10, "S", "R");
        assertEquals(12, f.cost(A));
        applied(f.t.setLinkCost(r, OptionalLong.empty()));
        assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
        assertThrows(IllegalArgumentException.class, () -> f.t.setLinkCost(r, OptionalLong.of(0)));
    }
    @Test void d08ExpiryAndRemovalRetainValidAlternative() {
        var f = new F(); var r1 = f.link("R1", 1); var r2 = f.link("R2", 10);
        f.announce(A, r1, 1, 1, "S", "R1"); f.clock.ms(2000); f.announce(A, r2, 1, 2, "S", "R2");
        f.clock.ms(7999); assertEquals(2, f.cost(A));
        f.clock.ms(8000); applied(f.t.expire()); assertEquals(12, f.cost(A));
        assertEquals(1, f.route(A).candidates().size());
        applied(f.t.removeSession(r2)); assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
    }
    @Test void d09SameGenerationImprovementKeepsOriginalDeadlineAndHistory() {
        var f = new F(); var r = f.link("R", 1); f.announce(A, r, 1, 100, "S", "R");
        f.clock.ms(7000); f.announce(A, r, 1, 50, "S", "R"); assertEquals(51, f.cost(A));
        assertEquals(0, f.route(A).selectedCandidate().orElseThrow().generationStartedAtNanos());
        f.clock.ms(8000); assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
        f.clock.ms(15000); reason(Reason.GENERATION_EXPIRED, f.t.announce(f.ad(A, r, 1, 20, "S", "R")));
        f.clock.ms(16000); f.announce(A, r, 1, 20, "S", "R"); assertEquals(21, f.cost(A));
    }
    @Test void d09ImprovedPendingAdvertisementDoesNotNeedAnRttToCompareSameLink() {
        var f = new F(); var r = key("R", "c"); applied(f.t.replaceSession(Optional.empty(), r));
        f.announce(A, r, 1, 100, "S", "R"); f.clock.ms(7000); f.announce(A, r, 1, 50, "S", "R");
        assertTrue(f.route(A).selectedCandidate().isEmpty());
        applied(f.t.setLinkCost(r, OptionalLong.of(1))); assertEquals(51, f.cost(A));
        f.clock.ms(8000); assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
    }
    @Test void d09OnlyNewerSequenceRenewsEvenWhenItsCostIsWorse() {
        var f = new F(); var r = f.link("R", 1); f.announce(A, r, 10, 10, "S", "R");
        f.clock.ms(7000); reason(Reason.DUPLICATE, f.t.announce(f.ad(A, r, 10, 10, "S", "R")));
        reason(Reason.DUPLICATE, f.t.announce(f.ad(A, r, 10, 20, "S", "R")));
        reason(Reason.STALE_SEQUENCE, f.t.announce(f.ad(A, r, 9, 1, "S", "R")));
        f.announce(A, r, 11, 30, "S", "R"); f.clock.ms(8000); assertEquals(31, f.cost(A));
        f.clock.ms(15000); assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
    }
    @Test void d10ExpiredWatermarkCannotKeepRejectingAfterItsHorizon() {
        var f = new F(); var r = f.link("R", 1); f.announce(A, r, 100, 10, "S", "R");
        f.clock.ms(9000); applied(f.t.expire());
        reason(Reason.GENERATION_EXPIRED, f.t.announce(f.ad(A, r, 100, 1, "S", "R")));
        reason(Reason.STALE_SEQUENCE, f.t.announce(f.ad(A, r, 99, 1, "S", "R")));
        f.clock.ms(16000); f.announce(A, r, 1, 2, "S", "R"); assertEquals(3, f.cost(A));
    }
    @Test void d11WithdrawalWatermarkPreventsEqualGenerationResurrection() {
        var f = new F(); var r = f.link("R", 1); f.announce(A, r, 10, 10, "S", "R");
        f.clock.ms(7000); applied(f.t.withdraw(A, ES, r, 10));
        reason(Reason.WITHDRAWN_GENERATION, f.t.announce(f.ad(A, r, 10, 1, "S", "R")));
        reason(Reason.DUPLICATE, f.t.withdraw(A, ES, r, 10));
        reason(Reason.STALE_SEQUENCE, f.t.withdraw(A, ES, r, 9));
        f.clock.ms(16000); f.announce(A, r, 10, 1, "S", "R"); assertEquals(2, f.cost(A));
    }
    @Test void d11NewerWithdrawalWithoutCandidateUsesBoundedSlotAndNewHorizon() {
        var f = new F(); var r = f.link("R", 1); applied(f.t.withdraw(A, ES, r, 20));
        assertTrue(f.route(A).candidates().isEmpty());
        reason(Reason.WITHDRAWN_GENERATION, f.t.announce(f.ad(A, r, 20, 1, "S", "R")));
        f.clock.ms(7000); applied(f.t.withdraw(A, ES, r, 21));
        f.clock.ms(22000); reason(Reason.WITHDRAWN_GENERATION, f.t.announce(f.ad(A, r, 21, 1, "S", "R")));
        f.clock.ms(23000); applied(f.t.expire()); f.announce(A, r, 1, 1, "S", "R");
    }
    @Test void d12EpochChangeIsExplicitAtomicAndIsolatedByOrigin() {
        var f = new F(); var r = f.link("R", 1);
        f.announce(A, r, 1, 10, "S", "R"); f.announce(A2, r, 1, 20, "S", "R");
        f.announce(B, r, 1, 30, "T", "R"); f.announce(B2, r, 1, 40, "T", "R");
        var other = List.of(f.route(B), f.route(B2)); var epoch = id("unordered-new");
        var newAd = new Advertisement(A, "S", epoch, r, 0, 1, List.of("S", "R"));
        reason(Reason.STALE_EPOCH, f.t.announce(newAd));
        var changed = f.t.confirmEpoch("S", Optional.of(ES), epoch); applied(changed);
        assertEquals(Set.of(A, A2), changed.changedStreams());
        for (UUID stream : List.of(A, A2)) {
            assertEquals(Optional.of(epoch), f.route(stream).serverEpoch());
            assertEquals(RouteState.UNAVAILABLE, f.route(stream).state()); assertTrue(f.route(stream).candidates().isEmpty());
        }
        assertEquals(other, List.of(f.route(B), f.route(B2)));
        reason(Reason.RETIRED_EPOCH, f.t.confirmEpoch("S", Optional.of(epoch), ES));
        reason(Reason.STALE_EPOCH, f.t.confirmEpoch("S", Optional.of(ES), id("wrong")));
        applied(f.t.announce(newAd)); assertEquals(2, f.cost(A));
    }
    @Test void d12LocalOffersAreInvalidatedTogetherOnEpochChange() {
        var f = new F("S", Limits.forNode(64)); applied(f.t.offerLocal(A, ES)); applied(f.t.offerLocal(A2, ES));
        var next = id("next"); applied(f.t.confirmEpoch("S", Optional.of(ES), next));
        assertFalse(f.route(A).localSource()); assertFalse(f.route(A2).localSource());
        reason(Reason.STALE_EPOCH, f.t.offerLocal(A, ES)); applied(f.t.offerLocal(A, next));
    }
    @Test void d13EightRetiredEpochsSaturateWithoutPartialInvalidation() {
        var f = new F(); var r = f.link("R", 1); UUID current = ES;
        for (int i = 0; i < 8; i++) {
            UUID next = id("e" + i); applied(f.t.confirmEpoch("S", Optional.of(current), next)); current = next;
        }
        var ad = new Advertisement(A, "S", current, r, 1, 10, List.of("S", "R")); applied(f.t.announce(ad));
        var healthy = List.of(f.route(A), f.route(A2)); UUID full = current;
        reason(Reason.CAPACITY_EXCEEDED, f.t.confirmEpoch("S", Optional.of(current), id("ninth")));
        assertEquals(healthy, List.of(f.route(A), f.route(A2)));
        reason(Reason.RETIRED_EPOCH, f.t.confirmEpoch("S", Optional.of(current), ES));
        f.clock.ms(15000); reason(Reason.DUPLICATE, f.t.confirmEpoch("S", Optional.of(full), full));
        f.clock.ms(16000); applied(f.t.confirmEpoch("S", Optional.of(full), ES));
        assertEquals(Optional.of(ES), f.route(A).serverEpoch());
    }
    @Test void d14OldCallbacksCannotChangeReplacementEvenInSameInstance() {
        var f = new F(); var old = f.link("R", 1); f.announce(A, old, 1, 10, "S", "R");
        var next = new SessionKey("R", old.instanceId(), id("next-connection"));
        applied(f.t.replaceSession(Optional.of(old), next));
        f.announce(A, next, 1, 10, "S", "R"); assertTrue(f.route(A).selectedCandidate().isEmpty());
        var healthy = f.route(A);
        reason(Reason.STALE_SESSION, f.t.setLinkCost(old, OptionalLong.of(1)));
        reason(Reason.STALE_SESSION, f.t.removeSession(old));
        reason(Reason.STALE_SESSION, f.t.announce(f.ad(A, old, 2, 1, "S", "R")));
        reason(Reason.STALE_SESSION, f.t.withdraw(A, ES, old, 3));
        reason(Reason.STALE_SESSION, f.t.replaceSession(Optional.of(old), key("R", "third")));
        assertEquals(healthy, f.route(A));
        applied(f.t.setLinkCost(next, OptionalLong.of(3))); assertEquals(13, f.cost(A));
        reason(Reason.DUPLICATE, f.t.replaceSession(Optional.empty(), next)); assertEquals(13, f.cost(A));
    }
    @Test void d15InputsNestedSnapshotsAndOutcomesAreDefensive() {
        var f = new F(); var r = f.link("R", 1); var path = new ArrayList<>(List.of("S", "R"));
        var ad = new Advertisement(A, "S", ES, r, 1, 10, path); path.clear();
        var changed = f.t.announce(ad); applied(changed); var old = f.route(A);
        assertThrows(UnsupportedOperationException.class, () -> ad.advertisedPath().clear());
        assertThrows(UnsupportedOperationException.class, () -> old.candidates().clear());
        assertThrows(UnsupportedOperationException.class, () -> old.selectedCandidate().orElseThrow().path().clear());
        assertThrows(UnsupportedOperationException.class, () -> old.downstreams().add("X"));
        assertThrows(UnsupportedOperationException.class, () -> changed.changedStreams().clear());
        f.announce(A, r, 2, 20, "S", "R"); assertEquals(11, old.selectedMetric().orElseThrow().costMicros());
        assertEquals(List.of("S", "R", "C"), old.selectedCandidate().orElseThrow().path());
        assertEquals(21, f.cost(A));
    }
    @Test void d16BindingsPersistAndUnknownOrConflictingStreamsAreRejected() {
        var f = new F(); var r = f.link("R", 1); var old = f.route(A);
        reason(Reason.DUPLICATE, f.t.bindStream(A, "S")); reason(Reason.ORIGIN_CONFLICT, f.t.bindStream(A, "T"));
        reason(Reason.UNKNOWN_STREAM, f.t.announce(f.ad(id("unknown"), r, 1, 1, "T", "R")));
        reason(Reason.UNKNOWN_STREAM, f.t.withdraw(id("unknown"), ES, r, 1));
        reason(Reason.ORIGIN_CONFLICT, f.t.announce(new Advertisement(A, "T", ET, r, 1, 1, List.of("T", "R"))));
        assertEquals(old, f.route(A)); assertTrue(f.t.snapshot(id("missing")).isEmpty());
        f.clock.ms(100_000); applied(f.t.expire()); reason(Reason.ORIGIN_CONFLICT, f.t.bindStream(A, "T"));
    }
    @Test void d17ExactProductionLimitsAreIndependentOfFloodingCapacity() {
        var limits = Limits.forNode(64); assertEquals(new Limits(64, 256, 63, 8), limits);
        assertEquals(16_384, limits.streamCapacity()); assertEquals(1_032_192, limits.slotCapacity());
        assertEquals(1, Limits.forNode(1).neighbors());
        for (int c : List.of(0, 65, -1)) assertThrows(IllegalArgumentException.class, () -> Limits.forNode(c));
        assertThrows(IllegalArgumentException.class, () -> new Limits(65, 256, 63, 8));
        assertThrows(IllegalArgumentException.class, () -> new Limits(64, 257, 63, 8));
        assertThrows(IllegalArgumentException.class, () -> new Limits(64, 256, 64, 8));
        assertThrows(IllegalArgumentException.class, () -> new Limits(64, 256, 63, 9));
    }
    @Test void d17SmallBudgetsRejectWithoutEvictingBindingsOrSessions() {
        var clock = new Clock(); var t = new RoutingTable("C", new Limits(1, 1, 1, 1), 8000, 16000, clock);
        applied(t.bindStream(A, "S")); applied(t.confirmEpoch("S", Optional.empty(), ES));
        var r = key("R", "one"); applied(t.replaceSession(Optional.empty(), r)); applied(t.setLinkCost(r, OptionalLong.of(1)));
        applied(t.announce(new Advertisement(A, "S", ES, r, 1, 1, List.of("S", "R")))); var old = t.snapshot(A);
        reason(Reason.CAPACITY_EXCEEDED, t.bindStream(A2, "S"));
        reason(Reason.CAPACITY_EXCEEDED, t.bindStream(B, "T"));
        reason(Reason.CAPACITY_EXCEEDED, t.replaceSession(Optional.empty(), key("X", "x")));
        assertEquals(old, t.snapshot(A));
        applied(t.announce(new Advertisement(A, "S", ES, r, 2, 2, List.of("S", "R"))));
        applied(t.withdraw(A, ES, r, 2)); reason(Reason.WITHDRAWN_GENERATION,
                t.announce(new Advertisement(A, "S", ES, r, 2, 1, List.of("S", "R"))));
        clock.ms(16000); applied(t.expire());
        applied(t.announce(new Advertisement(A, "S", ES, r, 1, 1, List.of("S", "R"))));
        assertEquals(1, t.snapshot(A).orElseThrow().candidates().size());
    }
    @Test void d18ConfirmedRepairSessionNeedsNoConfiguredGraphEdge() {
        var f = new F(); var repair = f.link("LearnedPeer", 2);
        f.announce(A, repair, 1, 3, "S", "LearnedPeer"); var route = f.route(A);
        assertEquals(RouteState.INACTIVE, route.state()); assertEquals(5, f.cost(A));
        assertTrue(route.activeUpstream().isEmpty()); assertTrue(route.pendingUpstream().isEmpty());
        assertTrue(route.downstreams().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new Route(A, "S", Optional.of(ES), false,
                route.selectedCandidate(), route.selectedMetric(), route.knownUpstream(), Optional.of(repair),
                Optional.empty(), Set.of(), route.candidates(), RouteState.ACTIVE, 1));
    }
    @Test void d19MetricOverflowRetainsAnnouncementAndCanRecoverWithinSameTtl() {
        var f = new F(); var r = f.link("R", 1); var alt = f.link("X", 100);
        f.announce(A, r, 1, Long.MAX_VALUE - 1, "S", "R");
        f.announce(A, alt, 1, 10, "S", "X");
        f.clock.ms(7000); applied(f.t.setLinkCost(r, OptionalLong.of(2)));
        var blocked = f.route(A).candidates().stream().filter(c -> c.upstream().equals(r)).findFirst().orElseThrow();
        assertTrue(blocked.metric().isEmpty()); assertEquals(110, f.cost(A));
        applied(f.t.setLinkCost(r, OptionalLong.of(1)));
        assertTrue(f.route(A).candidates().stream().filter(c -> c.upstream().equals(r)).findFirst().orElseThrow().metric().isPresent());
        f.clock.ms(8000); assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
    }
    @Test void d20TickWrapAndExactNanosecondBoundaryAreValid() {
        var f = new F(); f.clock.now = Long.MAX_VALUE - 4_000_000_000L; var r = f.link("R", 1);
        f.announce(A, r, 1, 1, "S", "R"); long began = f.clock.now;
        f.clock.now = began + 7_999_999_999L; assertEquals(2, f.cost(A));
        f.clock.now = began + 8_000_000_000L; assertEquals(RouteState.UNAVAILABLE, f.route(A).state());
    }
    @Test void d20ClockRegressionRejectsBeforeDomainMutation() {
        var f = new F(); var r = f.link("R", 1); f.announce(A, r, 1, 1, "S", "R"); var old = f.route(A);
        f.clock.now = -1; assertThrows(IllegalStateException.class, () -> f.t.withdraw(A, ES, r, 2));
        f.clock.now = 0; assertEquals(old, f.route(A));
    }
    @Test void d20DurationsAreCheckedBeforeConvertingAndNegativeTicksAreAllowed() {
        var clock = new Clock(); clock.now = -9_000_000_000L;
        var t = new RoutingTable("C", Limits.forNode(64), 1, Integer.MAX_VALUE, clock);
        applied(t.bindStream(A, "S")); applied(t.confirmEpoch("S", Optional.empty(), ES));
        var r = key("S", "s"); applied(t.replaceSession(Optional.empty(), r)); applied(t.setLinkCost(r, OptionalLong.of(1)));
        applied(t.announce(new Advertisement(A, "S", ES, r, 0, 0, List.of("S"))));
        clock.now += 999_999; assertEquals(RouteState.INACTIVE, t.snapshot(A).orElseThrow().state());
        clock.now++; assertEquals(RouteState.UNAVAILABLE, t.snapshot(A).orElseThrow().state());
        assertThrows(IllegalArgumentException.class, () -> new RoutingTable("C", Limits.forNode(64), 0, 1, clock));
        assertThrows(IllegalArgumentException.class, () -> new RoutingTable("C", Limits.forNode(64), 2, 1, clock));
        assertThrows(IllegalArgumentException.class, () -> new RoutingTable("C", Limits.forNode(64), 1, Long.MAX_VALUE, clock));
    }
    @Test void d21RevisionBoundsAndRejectedTransactionsPreservePublishedViews() {
        assertEquals(Long.MAX_VALUE, RoutingTable.nextRevision(Long.MAX_VALUE - 1));
        assertThrows(ArithmeticException.class, () -> RoutingTable.nextRevision(Long.MAX_VALUE));
        var f = new F(); var r = f.link("R", 1); f.announce(A, r, 1, 1, "S", "R");
        var old = f.route(A); reason(Reason.STALE_SEQUENCE, f.t.withdraw(A, ES, r, 0));
        assertEquals(old, f.route(A)); f.announce(A, r, 2, 2, "S", "R");
        assertEquals(old.revision() + 1, f.route(A).revision());
        assertEquals(2, old.selectedMetric().orElseThrow().costMicros());
    }
    @Test void d21InvalidDomainObjectsAreRejectedBeforeRetention() {
        assertThrows(IllegalArgumentException.class, () -> key("bad.id", "c"));
        assertThrows(IllegalArgumentException.class, () -> new SessionKey("R", new UUID(0, 0), id("c")));
        assertThrows(IllegalArgumentException.class, () -> new Advertisement(A, "S", ES, key("R", "c"), -1, 1, List.of("S", "R")));
        assertThrows(IllegalArgumentException.class, () -> new RouteCandidate(A, "S", ES, key("R", "c"),
                List.of("S", "R", "R"), 1, 1, Optional.empty(), 0, 1));
    }
    @Test void d21SourceCandidateCannotClaimPositiveAdvertisedCost() {
        assertThrows(IllegalArgumentException.class, () -> new RouteCandidate(A, "S", ES, key("S", "source"),
                List.of("S", "C"), 1, 1, Optional.of(new RouteMetric(2, 1)), 0, 1));
        var f = new F(); var s = f.link("S", 2);
        reason(Reason.INVALID_INPUT, f.t.announce(f.ad(A, s, 1, 1, "S")));
        f.announce(A, s, 1, 0, "S"); assertEquals(2, f.cost(A));
    }
    @Test void d20EachOperationObservesTimeExactlyOnce() {
        int[] reads = {0};
        var t = new RoutingTable("C", Limits.forNode(64), 8000, 16000, () -> { reads[0]++; return 0; });
        assertEquals(0, reads[0]); applied(t.bindStream(A, "S")); assertEquals(1, reads[0]);
        applied(t.confirmEpoch("S", Optional.empty(), ES)); assertEquals(2, reads[0]);
        var s = key("S", "source"); applied(t.replaceSession(Optional.empty(), s)); assertEquals(3, reads[0]);
        applied(t.setLinkCost(s, OptionalLong.of(1))); assertEquals(4, reads[0]);
        applied(t.announce(new Advertisement(A, "S", ES, s, 1, 0, List.of("S")))); assertEquals(5, reads[0]);
        assertTrue(t.snapshot(A).isPresent()); assertEquals(6, reads[0]);
        applied(t.withdraw(A, ES, s, 2)); assertEquals(7, reads[0]);
        applied(t.expire()); assertEquals(8, reads[0]);
    }
}
