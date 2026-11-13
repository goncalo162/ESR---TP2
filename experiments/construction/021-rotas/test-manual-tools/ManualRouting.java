import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.LongSupplier;
import pt.uminho.esr.ott.routing.*;
import pt.uminho.esr.ott.routing.RoutingTable.*;

/** Offline, independent demonstration against the verified JAR, not product CLI or JUnit. */
public final class ManualRouting {
    private static final UUID A = uuid("manual-S1-A"), B = uuid("manual-S1-B");
    private static final UUID C = uuid("manual-S2-C"), D = uuid("manual-S2-D");
    private static final UUID E1 = uuid("manual-S1-epoch"), E2 = uuid("manual-S2-epoch");
    private static int checks;
    private static boolean interactive;
    private static final BufferedReader INPUT = new BufferedReader(new InputStreamReader(System.in));

    private static final class Clock implements LongSupplier {
        long nanos;
        public long getAsLong() { return nanos; }
        void millis(long ms) { nanos = Math.multiplyExact(ms, 1_000_000L); System.out.println("SIMULATED_TIME_MS=" + ms); }
    }
    private static UUID uuid(String name) { return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)); }
    private static void check(boolean value, String description) {
        if (!value) throw new AssertionError("CHECK_FAIL " + description);
        checks++; System.out.println("CHECK_PASS " + checks + " " + description);
    }
    private static void step(String title, String expected) throws Exception {
        System.out.println("\nSTEP " + title + "\nESPERADO: " + expected);
        if (interactive) {
            System.out.println("Prima Enter para executar esta etapa (Ctrl+C para interromper)."); System.out.flush();
            if (INPUT.readLine() == null) throw new IllegalStateException("EOF before manual step confirmation");
        }
    }
    private static void apply(Outcome result) {
        if (result.status() != Status.APPLIED) throw new AssertionError("Unexpected setup/operation result: " + result);
    }
    private static SessionKey session(String node, String connection) { return new SessionKey(node, uuid(node), uuid(connection)); }
    private static void announce(RoutingTable table, UUID stream, String server, UUID epoch,
            SessionKey sender, long seq, long cost, String... path) {
        apply(table.announce(new Advertisement(stream, server, epoch, sender, seq, cost, List.of(path))));
    }
    private static Route view(RoutingTable table, UUID stream) { return table.snapshot(stream).orElseThrow(); }
    private static long cost(RoutingTable table, UUID stream) { return view(table, stream).selectedMetric().orElseThrow().costMicros(); }
    private static boolean blocked(Runnable operation) {
        try { operation.run(); return false; } catch (UnsupportedOperationException expected) { return true; }
    }
    private static boolean dataDisabled(Route route) {
        return route.activeUpstream().isEmpty() && route.pendingUpstream().isEmpty()
                && route.downstreams().isEmpty() && route.state() != RouteState.ACTIVE;
    }
    private static void show(String label, Route route) {
        System.out.println(label + " stream=" + route.streamId() + " state=" + route.state()
                + " server=" + route.serverId() + " epoch=" + route.serverEpoch()
                + " knownUpstream=" + route.knownUpstream().map(SessionKey::nodeId).orElse("none")
                + " costMicros=" + route.selectedMetric().map(m -> Long.toString(m.costMicros())).orElse("none")
                + " candidates=" + route.candidates().size() + " dataAuthorized=false");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !Set.of("--interactive", "--automatic").contains(args[0]))
            throw new IllegalArgumentException("Select --interactive or --automatic");
        interactive = args[0].equals("--interactive");
        System.out.println("CODE_SOURCE=" + RoutingTable.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println("OFFLINE_ONLY; costs and time are controlled inputs, not network measurements.");
        var clock = new Clock(); var limits = Limits.forNode(64);
        var table = new RoutingTable("Client", limits, 8000, 16000, clock);

        step("1 — Quatro streams e seleção", "S1/A=130 por R2; S1/B=240; S2/C=340; S2/D=450. Nenhum dado autorizado.");
        apply(table.bindStream(A, "S1")); apply(table.bindStream(B, "S1"));
        apply(table.bindStream(C, "S2")); apply(table.bindStream(D, "S2"));
        apply(table.confirmEpoch("S1", Optional.empty(), E1)); apply(table.confirmEpoch("S2", Optional.empty(), E2));
        var r1 = session("R1", "first-R1"); var r2 = session("R2", "first-R2");
        apply(table.replaceSession(Optional.empty(), r1)); apply(table.replaceSession(Optional.empty(), r2));
        apply(table.setLinkCost(r1, OptionalLong.of(40))); apply(table.setLinkCost(r2, OptionalLong.of(50)));
        announce(table, A, "S1", E1, r1, 1, 100, "S1", "R1");
        announce(table, A, "S1", E1, r2, 1, 80, "S1", "R2");
        announce(table, B, "S1", E1, r1, 1, 200, "S1", "R1");
        announce(table, C, "S2", E2, r1, 1, 300, "S2", "R1");
        announce(table, D, "S2", E2, r2, 1, 400, "S2", "R2");
        check(limits.origins() == 64 && limits.streamsPerOrigin() == 256 && limits.neighbors() == 63
                && limits.retiredEpochsPerOrigin() == 8 && limits.slotCapacity() == 1_032_192, "approved budgets; no maximum allocation required");
        check(cost(table, A) == 130 && view(table, A).knownUpstream().orElseThrow().equals(r2), "100+40=140 loses to 80+50=130");
        check(cost(table, B) == 240 && cost(table, C) == 340 && cost(table, D) == 450, "four stream costs remain independent");
        check(view(table, A).candidates().size() == 2, "alternative retained");
        check(view(table, A).serverEpoch().equals(view(table, B).serverEpoch())
                && !view(table, A).serverEpoch().equals(view(table, C).serverEpoch()), "epoch shared only within origin");
        var initial = view(table, A);
        check(dataDisabled(initial) && initial.state() == RouteState.INACTIVE, "selected route is not data authorization");
        check(blocked(() -> initial.candidates().clear()), "candidate list immutable");
        check(blocked(() -> initial.selectedCandidate().orElseThrow().path().clear()), "nested path immutable");
        show("S1/A", initial); show("S1/B", view(table, B)); show("S2/C", view(table, C)); show("S2/D", view(table, D));

        step("2 — Empates e TTL", "Empate por nodeId; depois menos saltos. Melhoria aos7s não prolonga prazo de8s.");
        announce(table, A, "S1", E1, r1, 2, 90, "S1", "R1");
        check(view(table, A).knownUpstream().orElseThrow().equals(r1) && cost(table, A) == 130, "equal cost/hops chooses R1 before R2");
        announce(table, A, "S1", E1, r1, 3, 90, "S1", "X", "R1");
        check(view(table, A).knownUpstream().orElseThrow().equals(r2), "equal cost chooses fewer hops");
        clock.millis(7000);
        announce(table, A, "S1", E1, r2, 2, 80, "S1", "R2");
        announce(table, B, "S1", E1, r1, 2, 200, "S1", "R1");
        announce(table, C, "S2", E2, r1, 2, 300, "S2", "R1");
        announce(table, D, "S2", E2, r2, 2, 400, "S2", "R2");
        announce(table, A, "S1", E1, r1, 3, 70, "S1", "R1");
        var improved = view(table, A); var others = List.of(view(table, B), view(table, C), view(table, D));
        check(cost(table, A) == 110 && improved.selectedCandidate().orElseThrow().generationStartedAtNanos() == 0, "same generation improves without resetting start");
        clock.millis(8000);
        check(cost(table, A) == 130 && view(table, A).knownUpstream().orElseThrow().equals(r2), "expired best falls back to refreshed alternative");
        check(view(table, A).candidates().size() == 1, "expired candidate filtered before physical cleanup");
        check(others.equals(List.of(view(table, B), view(table, C), view(table, D))), "other refreshed streams unchanged");
        check(improved.selectedMetric().orElseThrow().costMicros() == 110 && initial.selectedMetric().orElseThrow().costMicros() == 130, "previous snapshots remain stable");
        clock.millis(9000);
        var stale = table.announce(new Advertisement(A, "S1", E1, r1, 3, 60, List.of("S1", "R1")));
        check(stale.reason() == Reason.GENERATION_EXPIRED && cost(table, A) == 130, "retained watermark prevents resurrection");
        show("S1/A", view(table, A));

        step("3 — Retirada e sessão", "Retirada afeta só o emissor; conexão nova não herda medição; callbacks antigos são ignorados.");
        announce(table, A, "S1", E1, r1, 4, 100, "S1", "R1");
        apply(table.withdraw(A, E1, r2, 2));
        check(cost(table, A) == 140 && view(table, A).knownUpstream().orElseThrow().equals(r1), "withdraw keeps valid alternative");
        check(table.announce(new Advertisement(A, "S1", E1, r2, 2, 1, List.of("S1", "R2"))).reason()
                == Reason.WITHDRAWN_GENERATION, "withdrawn generation cannot return");
        var next = new SessionKey("R1", r1.instanceId(), uuid("replacement-R1"));
        apply(table.replaceSession(Optional.of(r1), next));
        announce(table, A, "S1", E1, next, 1, 10, "S1", "R1");
        check(view(table, A).state() == RouteState.UNAVAILABLE && view(table, A).candidates().size() == 1, "new connection candidate waits for new measurement");
        apply(table.setLinkCost(next, OptionalLong.of(1)));
        check(cost(table, A) == 11 && r1.instanceId().equals(next.instanceId()) && !r1.connectionId().equals(next.connectionId()), "same instance, new connection and measured cost");
        var healthy = view(table, A);
        check(table.setLinkCost(r1, OptionalLong.of(99)).reason() == Reason.STALE_SESSION
                && table.withdraw(A, E1, r1, 99).reason() == Reason.STALE_SESSION
                && table.removeSession(r1).reason() == Reason.STALE_SESSION, "old callbacks rejected by full session");
        check(healthy.equals(view(table, A)), "old callbacks preserve replacement snapshot");

        step("4 — Mudança de época", "S1/A e S1/B mudam juntos; S2/C e S2/D permanecem. UUID antigo protegido não pode regressar.");
        announce(table, C, "S2", E2, next, 1, 300, "S2", "R1");
        var secondOrigin = List.of(view(table, C), view(table, D)); UUID current = uuid("replacement-epoch-1");
        var changed = table.confirmEpoch("S1", Optional.of(E1), current); apply(changed);
        check(changed.changedStreams().equals(Set.of(A, B)), "epoch change affects exactly both streams of S1");
        check(view(table, A).state() == RouteState.UNAVAILABLE && view(table, B).state() == RouteState.UNAVAILABLE
                && view(table, A).serverEpoch().equals(Optional.of(current)), "new epoch published without old candidates");
        check(secondOrigin.equals(List.of(view(table, C), view(table, D))), "other origin snapshots preserved atomically");
        check(table.confirmEpoch("S1", Optional.of(current), E1).reason() == Reason.RETIRED_EPOCH, "retired epoch cannot revert current origin");
        check(table.announce(new Advertisement(A, "S1", E1, next, 99, 1, List.of("S1", "R1"))).reason() == Reason.STALE_EPOCH, "old announcement never changes epoch implicitly");
        show("S1/A", view(table, A)); show("S1/B", view(table, B)); show("S2/C", view(table, C)); show("S2/D", view(table, D));

        step("5 — Oito épocas protegidas", "Nona retirada recusa sem apagar candidatos; aos25s simulados expiram os históricos iniciados aos9s.");
        for (int i = 2; i <= 8; i++) {
            UUID replacement = uuid("replacement-epoch-" + i);
            apply(table.confirmEpoch("S1", Optional.of(current), replacement)); current = replacement;
        }
        announce(table, A, "S1", current, next, 1, 5, "S1", "R1");
        announce(table, B, "S1", current, next, 1, 6, "S1", "R1");
        var protectedViews = List.of(view(table, A), view(table, B)); UUID ninth = uuid("replacement-epoch-9");
        check(table.confirmEpoch("S1", Optional.of(current), ninth).reason() == Reason.CAPACITY_EXCEEDED, "ninth protected retirement rejected");
        check(protectedViews.equals(List.of(view(table, A), view(table, B))) && cost(table, A) == 6 && cost(table, B) == 7, "capacity rejection preserves both healthy streams");
        check(table.confirmEpoch("S1", Optional.of(current), E1).reason() == Reason.RETIRED_EPOCH, "protected oldest epoch not evicted");
        clock.millis(24999);
        check(table.confirmEpoch("S1", Optional.of(current), ninth).reason() == Reason.CAPACITY_EXCEEDED, "history protected before exact deadline");
        clock.millis(25000); apply(table.confirmEpoch("S1", Optional.of(current), ninth)); current = ninth;
        check(view(table, A).serverEpoch().equals(Optional.of(ninth)) && view(table, A).state() == RouteState.UNAVAILABLE, "history capacity released only after horizon");

        step("6 — Capacidades pequenas", "Saturar bindings/sessões não expulsa estado; atualizar slot existente continua permitido.");
        var small = new RoutingTable("Client", new Limits(1, 1, 1, 1), 8000, 16000, clock);
        apply(small.bindStream(A, "S1")); apply(small.confirmEpoch("S1", Optional.empty(), E1));
        apply(small.replaceSession(Optional.empty(), r2)); apply(small.setLinkCost(r2, OptionalLong.of(1)));
        announce(small, A, "S1", E1, r2, 1, 5, "S1", "R2"); var smallBefore = view(small, A);
        check(small.bindStream(B, "S1").reason() == Reason.CAPACITY_EXCEEDED
                && small.bindStream(C, "S2").reason() == Reason.CAPACITY_EXCEEDED, "stream and origin limits reject explicitly");
        check(small.replaceSession(Optional.empty(), next).reason() == Reason.CAPACITY_EXCEEDED
                && smallBefore.equals(view(small, A)), "session saturation preserves candidate and binding");
        announce(small, A, "S1", E1, r2, 2, 4, "S1", "R2");
        check(cost(small, A) == 5 && view(small, A).candidates().size() == 1, "update reuses existing bounded slot");

        step("7 — Fonte local", "Custo zero sem parent/dados; nova época retira ambas as ofertas locais.");
        var source = new RoutingTable("S1", Limits.forNode(64), 8000, 16000, clock);
        apply(source.bindStream(A, "S1")); apply(source.bindStream(B, "S1"));
        apply(source.confirmEpoch("S1", Optional.empty(), E1)); apply(source.offerLocal(A, E1)); apply(source.offerLocal(B, E1));
        check(cost(source, A) == 0 && view(source, A).localSource() && view(source, A).knownUpstream().isEmpty(), "local source cost is zero with no parent");
        check(dataDisabled(view(source, A)) && dataDisabled(view(source, B)), "local planning offers still authorize no data");
        apply(source.confirmEpoch("S1", Optional.of(E1), current));
        check(!view(source, A).localSource() && !view(source, B).localSource(), "epoch invalidates local offers together");

        step("8 — Overflow e imutabilidade", "Soma exata aceita Long.MAX_VALUE; overflow recusa sem substituir candidato; medição impossível não renova TTL.");
        announce(table, A, "S1", current, next, 1, Long.MAX_VALUE - 1, "S1", "R1");
        check(cost(table, A) == Long.MAX_VALUE, "representable accumulated cost accepted");
        var exact = view(table, A);
        check(table.announce(new Advertisement(A, "S1", current, next, 2, Long.MAX_VALUE, List.of("S1", "R1"))).reason()
                == Reason.INVALID_INPUT && exact.equals(view(table, A)), "overflowing update rejected atomically");
        apply(table.setLinkCost(next, OptionalLong.of(2)));
        check(view(table, A).state() == RouteState.UNAVAILABLE && view(table, A).candidates().size() == 1, "later metric overflow keeps announcement but removes eligibility");
        apply(table.setLinkCost(next, OptionalLong.of(1)));
        check(cost(table, A) == Long.MAX_VALUE && view(table, A).selectedCandidate().orElseThrow().generationStartedAtNanos() == 25_000_000_000L, "measurement recovery preserves original generation start");
        check(dataDisabled(view(table, A)) && exact.selectedMetric().orElseThrow().costMicros() == Long.MAX_VALUE, "data remains disabled and old snapshot unchanged");
        System.out.println("CHECK_COUNT=" + checks);
        System.out.println("MANUAL_PASS");
    }
}
