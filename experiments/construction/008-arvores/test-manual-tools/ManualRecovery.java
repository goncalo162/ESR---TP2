package pt.uminho.esr.ott.routing;

import static pt.uminho.esr.ott.routing.SubscriptionLeaseTest.*;
import java.io.EOFException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.Connection;

public final class ManualRecovery
{
    //* === Atributos e Variáveis === *
    private static final long T = 3_000_000_000L;
    private static final Scanner INPUT = new Scanner(System.in, StandardCharsets.UTF_8);
    private static boolean interactive;
    private static int checks;



    //* === Auxiliar === *

    @FunctionalInterface
    private interface Action
    {
        void run() throws Exception;
    }


    private static void step(int number, String title, String expected) throws Exception
    {
        System.out.println("\nETAPA " + number + " — " + title);
        System.out.println("ESPERADO: " + expected);
        if(interactive)
        {
            System.out.println("Prima Enter para executar e observar esta etapa.");
            if(!INPUT.hasNextLine())
                throw new EOFException("Manual humano interrompido antes da etapa " + number);
            INPUT.nextLine();
        }
    }


    private static void check(String label, boolean passed)
    {
        if(!passed)
        {
            System.out.println("CHECK_FAIL " + label);
            throw new AssertionError(label);
        }
        checks++;
        System.out.println("CHECK_PASS " + label);
    }


    private static void runCase(String label, Action action) throws Exception
    {
        action.run();
        checks++;
        System.out.println("CHECK_PASS " + label);
    }


    private static Route.Recovery state(Rig rig)
    {
        return rig.router.snapshot().recoveries().get(A);
    }


    private static void observe(String label, Rig rig)
    {
        System.out.println("OBSERVADO " + label + " ACTIVATE=" + rig.messages(MessageType.ACTIVATE).size()
                + " recovery=" + state(rig) + " forwarding=" + rig.forwarding(A)
                + " resources=" + rig.load());
    }



    //* === Lógica === *

    private static void alternative() throws Exception
    {
        step(1, "Perda e alternativa", "Retirar forwarding logo; aguardar T; B só confirma após ACK novo.");
        try(var rig = new Rig())
        {
            var first = peer("A");
            var second = peer("B");
            rig.route(first);
            rig.up(second);
            rig.metric(second, 10);
            rig.ad(second, 1, 500);
            var token = UUID.randomUUID();
            var ticket = rig.local(A, token);
            rig.ack(rig.latestRequest(), 6000);
            var original = result(ticket);
            rig.act(() -> rig.router.onNeighborEvent(new NeighborManager.NeighborDown(first, NeighborManager.Reason.TRANSPORT)));
            observe("após Down(A)", rig);
            check("retirada imediata da autoridade", rig.forwarding(A) == null);
            check("sem retry antes de T", rig.messages(MessageType.ACTIVATE).size() == 1);
            rig.tick(T);
            check("novo ACTIVATE para B", rig.messages(MessageType.ACTIVATE).size() == 2
                    && rig.latestRequest().peer().equals(second));
            check("ACCEPTED não confirma sozinho", rig.forwarding(A) == null);
            rig.ack(rig.latestRequest(), 6000);
            observe("após ACK(B)", rig);
            check("mesmo consumidor autorizado em B", rig.forwarding(A).localConsumers().equals(Set.of(token)));
            check("resultado original imutável", result(ticket) == original);
        }
    }


    private static void metricRearm() throws Exception
    {
        step(2, "R10 e watermark", "G2 + PATH_CHANGED + T; métrica10→2000→10 mantém1 ACTIVATE; G3 permite2.");
        try(var rig = new Rig("R", false, 4096, 1))
        {
            var parent = peer("A");
            rig.up(parent);
            rig.metric(parent, 10);
            rig.ad(parent, 2, Long.MAX_VALUE - 1000);
            var ticket = rig.local(A, UUID.randomUUID());
            var first = rig.latestRequest();
            var before = (ControlMessage.Activation) first.message().payload();
            rig.error(first, 11, ErrorCode.PATH_CHANGED);
            var outcome = result(ticket);
            rig.tick(T);
            rig.metric(parent, 2000);
            rig.metric(parent, 10);
            observe("métrica restaurada sem ANN", rig);
            check("prova G2 bloqueada continua UNAVAILABLE", rig.messages(MessageType.ACTIVATE).size() == 1
                    && state(rig).phase() == Route.RecoveryPhase.UNAVAILABLE);
            rig.ad(parent, 2, Long.MAX_VALUE - 1000);
            check("duplicado não rearma", rig.messages(MessageType.ACTIVATE).size() == 1);
            rig.ad(parent, 3, Long.MAX_VALUE - 1000);
            var after = (ControlMessage.Activation) rig.latestRequest().message().payload();
            check("ANN G3 permite nova ronda", rig.messages(MessageType.ACTIVATE).size() == 2);
            check("Line mantida com revisão/request novos", after.subscriptionId().equals(before.subscriptionId())
                    && after.revision() > before.revision() && !rig.latestRequest().message().requestId().equals(first.message().requestId()));
            rig.ack(rig.latestRequest(), 6000);
            observe("recuperado por G3", rig);
            check("confirmação nova não reescreve erro antigo", rig.forwarding(A) != null && result(ticket) == outcome);
        }
    }


    private static void validNovelty() throws Exception
    {
        step(3, "Novidade legítima", "Primeira métrica válida recupera; ANN pré-falha não; ANN posterior sem métrica conserva oportunidade.");
        var tests = new RecoveryTest();
        runCase("primeira medição válida preservada", tests::r10FirstValidMetricStillAdmitsTheInitialEligibleProof);
        runCase("ANN anterior à falha continua bloqueado", tests::r10MetricRecoveryCannotReuseANewerAnnouncementAlreadySeenBeforeFailure);
        runCase("ANN posterior sem métrica continua útil", tests::r10PostFailureAnnouncementWithoutMetricRemainsUsefulForFirstEligibleObservation);
    }


    private static void epochAndIntent() throws Exception
    {
        step(4, "Época e intenção", "Época nova exige ACK; token permanece; ACK antigo/cancelamento não ressuscitam; outra origem isolada.");
        try(var rig = new Rig())
        {
            var parent = peer("A");
            rig.route(parent);
            var token = UUID.randomUUID();
            var ticket = rig.local(A, token);
            var old = rig.latestRequest();
            rig.ack(old, 6000);
            rig.tick(T);
            var epoch = UUID.randomUUID();
            rig.ad(parent, A, "S", epoch, 0, 100);
            check("época nova retira autoridade anterior", rig.forwarding(A) == null);
            rig.ack(old, 6000);
            check("ACK antigo não confirma época nova", rig.forwarding(A) == null);
            rig.ack(rig.latestRequest(), 6000);
            observe("época nova confirmada", rig);
            check("intenção e token conservados", rig.forwarding(A).serverEpoch().equals(epoch)
                    && rig.forwarding(A).localConsumers().equals(Set.of(token)) && rig.load().locals() == 1);
            ticket.cancel();
            rig.pump();
            check("cancelamento retira procura", rig.forwarding(A) == null && rig.load().locals() == 0);
        }
        runCase("outra origem permanece intacta", () -> new RecoveryTest().r05FailureAndEpochChangeAreIsolatedFromTheOtherOrigin());
    }


    private static void physicalResources() throws Exception
    {
        step(5, "Capacidade e shutdown", "2 reservas entre gerações; libertar capacidade não rearma terminal; await(0) falso até fim físico.");
        try(var rig = new Rig())
        {
            var parent = peer("A");
            rig.route(parent);
            rig.hold = message -> message.type() == MessageType.ACTIVATE;
            for(int i = 0; i < 2; i++)
            {
                var abandoned = rig.local(A, UUID.randomUUID());
                abandoned.cancel();
                rig.pump();
            }
            rig.local(A, UUID.randomUUID());
            check("2 reservas retidas bloqueiam terceira emissão", rig.load().sends() == 2
                    && rig.messages(MessageType.ACTIVATE).size() == 2);
            rig.tick(T);
            check("saturação termina UNAVAILABLE", state(rig).phase() == Route.RecoveryPhase.UNAVAILABLE
                    && state(rig).reason().equals(Optional.of(ErrorCode.RESOURCE_LIMIT)));
            for(var send : rig.messages(MessageType.ACTIVATE))
            {
                send.result().complete(Connection.SendResult.ACCEPTED);
            }
            rig.pump();
            observe("capacidade libertada", rig);
            check("libertação não cria retry", rig.load().sends() == 0 && rig.messages(MessageType.ACTIVATE).size() == 2);
            rig.tick(1);
            rig.ad(parent, 2, 100);
            var held = rig.latestRequest();
            check("ANN novo admite uma emissão", rig.messages(MessageType.ACTIVATE).size() == 3 && rig.load().sends() == 1);
            rig.router.requestClose();
            rig.owner.drain();
            check("shutdown aguarda callback físico", !rig.router.awaitTermination(Duration.ZERO));
            held.result().complete(Connection.SendResult.ACCEPTED);
            rig.owner.drain();
            check("shutdown conclui sem rearme", rig.router.awaitTermination(Duration.ofMillis(20))
                    && rig.router.snapshot().recoveries().isEmpty() && rig.messages(MessageType.ACTIVATE).size() == 3);
        }
        runCase("continuação não termina callback retido", () -> new RecoveryTest().r14AProcessedContinuationDoesNotReleaseItsStillRunningCallback());
    }


    public static void main(String[] args) throws Exception
    {
        interactive = args.length == 1 && args[0].equals("--interactive");
        System.out.println("CODE_SOURCE=" + RoutingService.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println("TEST_CODE_SOURCE=" + RecoveryTest.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println("LIMITACAO_R17=negativo inicial sem causa estabelecida; métricas controladas não demonstram a causa");
        alternative();
        metricRearm();
        validNovelty();
        epochAndIntent();
        physicalResources();
        step(6, "TCP: prefixo com dois relays", "Mesma Line C→R, revisão/request novos, consumidores recuperam automaticamente; portas disponíveis após fecho.");
        runCase("TCP prefixo renegociado e recursos terminados",
                () -> new RecoveryTest().r09TcpTwoRelaysAutomaticallyRenegotiateAChangedPrefixWithoutHumanRetry());
        step(7, "TCP: reparação S–C", "Bootstrap fechado; falhas sequenciais; path S–C e ACK novos. Sucesso não explica negativo R17 inicial.");
        runCase("TCP reparado sem bootstrap/retry humano e recursos terminados",
                () -> new RecoveryTest().r17TcpSequentialRelayFailuresConsumeExistingSourceClientRepairWithoutBootstrap());
        System.out.println("CLEANUP_COMPLETE");
        System.out.println("MANUAL_PASS CHECKS=" + checks);
    }
}
