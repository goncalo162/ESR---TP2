---
unit: "004-arvores"
bolt: "023-arvores"
stage: "test"
status: "complete"
---
# IMP-012 — checkpoint Test / V-015

Implement aprovado pelo utilizador, Test autorizado. **Test aprovado pelo utilizador; manual humano Francisco cumprido.** Revisão final Denji posteriormente confirmada; integração continua pendente.
Bolt/história/TASK-015/unidade004/PHASE-04 em progresso, sem fecho/staging/commits/push.
Nenhum gate externo adicional. Base main/b273b258a4642f94f8614ae00f060a46318d77a5.

## Construção e resultados reais

Em Projeto/, JDK Temurin21.0.13+9/Maven3.9.9:
`mvn -B -ntp clean verify`, JAVA_HOME do JDK21 indicado no resultado.
Maven **exit0**, **1379 testes,0 falhas,0 erros,0 ignorados**.
[Log integral](../../../experiments/construction/023-arvores/test-01/clean-verify.log),
[resultado/inventário XML/hashes](../../../experiments/construction/023-arvores/test-01/clean-verify-result.json).
**19 XML Surefire** preservados, não20. São19 suites executadas,51 métodos novos
SubscriptionLeaseTest e31 ActivationTest, incluindo6 M07 históricos, na suite total.
Não se usa1328 ou149 históricos como resultado desta construção.

JAR congelado: [ott-streaming-1.0-SNAPSHOT.jar](../../../experiments/construction/023-arvores/test-01/ott-streaming-1.0-SNAPSHOT.jar),
495568 bytes; SHA-256 `e2a41113b8dbb4d3d68024f546bb570e7509fdd47a2d09366c4b96ef907a1188`.
[264 classes do JAR](../../../experiments/construction/023-arvores/test-01/jar-classes-sha256.json) iguais byte a byte
às classes produzidas pelo clean build. O ensaio do auxiliar confirmou também
RoutingService.class carregado desse JAR, com classpath JAR primeiro e sem target.

## Errata documental de XML

A prosa anterior dizia20 XML; inventário real de cada regressão incremental
contém19. Corrigido apenas o walkthrough vivo. [Errata e inventários](../../../experiments/construction/023-arvores/test-01/xml-count-erratum.json)
identificam ambas as pastas/19 ficheiros. Os originais README/diff/log/XML/JSON/
manifestos de Implement são preservados tal como recebidos, incluindo a prosa
histórica incorreta. Nenhuma evidência foi fabricada, removida ou reescrita para
ajustar contagem.20 fontes compiladas de teste não implicam20 XML.

## V-015 / L01–L24 consolidado

[Mapa executável→XML](../../../experiments/construction/023-arvores/test-01/v015-oracles.json):24 grupos ligados
nominalmente a51 métodos com resultado PASS no XML desta execução limpa.
O mapa é por critério/caso crítico, sem JaCoCo/CI/linter/percentagem de cobertura.
Owner/relógio/scheduler/port falsos controlam admissão, ACK/callback, revisão,
expiração e limites; L24 compõe TCP real na mesma JVM com manager/monitor/routing.
Testes históricos não foram alterados ou enfraquecidos.

| Grupo | Métodos atuais de SubscriptionLeaseTest | Resultado |
|---|---|---|
| L01 | `l01SharedTrunkRenewsOncePerCycleAfterPartialDeparture`, `l01DeactivateOneChildPreservesSiblingAndCommonTrunk` | PASS |
| L02 | `l02LastCancellationRemovesAuthorizationBeforeHeldPruneCompletes` | PASS |
| L03 | `l03ChildLeavesBeforeLocalConsumer`, `l03MixedLocalAndChildDemandPrunesOnlyAfterBothLeave` | PASS |
| L04 | `l04OrphanChildExpiresAndPrunesItsCommonTrunk`, `l04SourceExpiryAlreadyExcludesOrphanWithoutNewNetworkInput` | PASS |
| L05 | `l05ThreeCyclesUseOneLineAndStrictlyIncreasingRevisions`, `l05PeriodicRenewalKeepsIdAndAdvancesRevisionAndRequest` | PASS |
| L06 | `l06OneMillisecondGrantUsesSubMillisecondCadenceAndExactExpiry`, `l06ShortGrantSchedulesAtOneThirdAndStillExpiresWithoutAck` | PASS |
| L07 | `l07EarlyRenewAckWaitsForAcceptanceAndUsesRequestAnchor`, `l07OversizedAndExactDeadlineAckDoNotReplaceGrant`, `l07SubMillisecondParentRemainderIsNeverRoundedUp` | PASS |
| L08 | `l08PendingDuplicateAndWrongKindCannotReanchor`, `l08TerminalReplayIsBoundedAndFreshRenewCanKeepOldHealthyGrant`, `l08NewRenewalExtendsButExactDuplicateNeverRenewsAgain` | PASS |
| L09 | `l09MaximumRevisionNeverWrapsAndEqualNewRequestIsRejected`, `l09LatePreviousAckCannotReplaceNewRenewal` | PASS |
| L10 | `l10OldPruneCannotCancelNewActivationOnSameSession`, `l10OldDeactivateCannotRemoveRenewedRevision` | PASS |
| L11 | `l11CancelledRenewAckCannotRestoreChildOrChangeSiblingLease`, `l11UnsentPruneExpiresAndNewDemandInvalidatesOldIntent`, `l11PruneCapturesSubmittedRenewalRevisionAndLateAckCannotChangeReplacement`, `l11UnknownHigherPruneUsesLeaseFallbackWithoutApproximateCancellation` | PASS |
| L12 | `l12CancelledRenewAckRetainsPhysicalSlotUntilCallbackAndContinuationEnd` | PASS |
| L13 | `l13ExpiredRenewalIsExplicitlyRejectedWithoutActivating`, `l13RenewBeforeInitialAckAndAfterCancelCannotActivate` | PASS |
| L14 | `l14BothDivergentPrefixesPreserveConfirmedGrant`, `l14DivergentRenewalPreservesOldGrantAndRejectsExplicitly`, `l14SamePathMetricChangeAllowsRenewal` | PASS |
| L15 | `l15OldRenewCallbackAndAckCannotInvalidateNewInstance`, `l15OldSessionDeactivateCannotAffectReplacementConnection` | PASS |
| L16 | `l16DeactivateOneStreamPreservesOtherStreamExactly` | PASS |
| L17 | `l17PhysicalFailureInvalidatesOnlyDependentSession`, `l17LocalSendFailurePreservesGrantAndAcceptedAloneDoesNotExtend` | PASS |
| L18 | `l18RenewErrorAndTraceErrorDoNotInvalidateHealthyGrants`, `l18WrongAckFieldsAndErrorKindsAreIsolated` | PASS |
| L19 | `l19SmallSharedHistoryAndLocalCapsPreserveHealthyGrant`, `l19UnboundErrorsShareGlobalAndPerSessionPhysicalCaps`, `l19RetiredGenerationsShareStablePhysicalContext`, `l19RenewRepliesShareExistingFixedWindowQuota`, `l19RenewalsShareFourPendingPerSessionAndNoParallelPool` | PASS |
| L20 | `l20MonotonicWrapPreservesRenewalCadence`, `l20LateTimerAdmitsOneCurrentRenewalWithoutCatchup` | PASS |
| L21 | `l21SourceCatalogSurvivesLastConsumerAndOtherStream` | PASS |
| L22 | `l22ShutdownWaitsForPhysicalRenewalCompletionWithinOriginalBudget`, `l22ShutdownAfterPruneDoesNotSendMoreAndRetainsPhysicalReservation` | PASS |
| L23 | `l23OversizedSubscriptionPathIsRejectedBeforeCopyOrIteration`, `l23SubscriptionCopiesValidPathAndRetainsPublicShape` | PASS |
| L24 | `l24TcpTwoRelaysAggregateRenewPruneExpireAndReleasePorts` | PASS |

Asserções relevantes: renovação mantém ID, avança revisão/request e usa P constante;
confirma só após ACK+ACCEPTED e ancora em t0, não ganha tempo em fila/replay.
Última retirada cancela autorização antes do fecho físico; poda exata pela maior
revisão submetida não remove substituta. Grant saudável do irmão/stream fica
intacto; pending14 não cria grant novo. B/J/session/context/quota partilhados,
reservas retidas até callback e continuação, finalmente M07 preservado.
Timers comuns e primeiro budget global de shutdown mantidos.

TCP real L24: bootstrap e cinco nós S–R1–R2–(C1,C2), dois relays; lease3000ms,
renovação200ms, asserção de ambos autorizados durante3300ms (além de um lease),
retirada parcial/final, DEACTIVATE observado na fonte, reativação explícita,
routing de C2 parado com manager TCP ainda aberto para expiração órfã pelo tronco.
Await de routing/monitor/manager/owner e rebind das seis TCP verificados.
Não se afirma vídeo implementado, UDP medido, GUI, CORE ou processos separados.
Autorização de forwarding não é entrega de frames nem meta experimental7s/FPS.

## Manual humano — cumprido por Francisco

[Roteiro reproduzível](../../../experiments/construction/023-arvores/manual-instructions.md) e
[manifesto congelado](../../../experiments/construction/023-arvores/test-manual-manifest.json): um terminal,
seis etapas interativas,30 checks esperados, logs pessoais/códigos de saída/origem
da classe e JVM recolhida. Etapas1–5 controladas,6 com TCP real.
[Ensaio Codex](../../../experiments/construction/023-arvores/test-manual-runs/20261118T174421Z-clqz57m8/report.json) concluído tecnicamente:30 CHECK_PASS,
CLEANUP_COMPLETE,MANUAL_PASS CHECKS=30,JAVA_EXIT0,passed=true, hashes/origem/JVM
conferidos. **kind=agent: não conta como execução humana ou aprovação de Test.**
A execução humana posterior foi realizada e confirmada pessoalmente por Francisco,
sem divergências observadas. [Relatório humano](../../../experiments/construction/023-arvores/test-manual-runs/20261118T175055Z-oiu494h1/report.json)
e [conferência independente dos ficheiros](../../../experiments/construction/023-arvores/manual-human-verification-01/verification.json):
seis etapas,30 CHECK_PASS,CLEANUP_COMPLETE,MANUAL_PASS CHECKS=30,JAVA_EXIT0,
passed=true. Hashes de fontes/JAR/auxiliares e evidências conferidos; RoutingService
carregado do JAR congelado e JVM terminada/recolhida pelo wrapper. Identidade
humana assenta na confirmação explícita do utilizador, não apenas em kind=human.
WRAPPER_EXIT não consta da saída/relatório fornecidos; não foi inferido ou declarado.
O roteiro e os relatórios anteriores de preparação mantêm o estado histórico de
manual pendente. Não se alterou qualquer evidência original para registar o sucesso.

## Código e preservação

Os três hashes da aprovação Implement coincidem antes/depois de clean verify e
do ensaio. [Preservação final](../../../experiments/construction/023-arvores/test-01/preservation.json): restantes src,
19 testes históricos, POM, validador, ADR aceites e evidências anteriores intactos.
Q-C-40–42 preservadas; Q-C-36–39/fechos anteriores não reabertos. Sem nova ADR,
wire/caps/enums/transporte/configuração/dependência runtime. Stage Test acrescenta
apenas documentação e auxiliares/evidência fora de Projeto/src.

Target foi limpo pelo clean Maven autorizado e regenerado; fica separado da
entrega. Log/XML/JAR necessários foram copiados para experiments. HEAD permanece
na base, index sem staging; sem checkout/reset/restauro ou limpeza global.

## Checkpoint e pendências

- Model/Design/ADR/Implement/Test aprovados pelo utilizador; V-015 consolidado.
- Manual humano Francisco cumprido e conferido:30 checks, código/origem/JVM intactos; sem divergências declaradas.
- Pendentes: revisão final do código por Denji e integração comprovada.
- Bolt/história/TASK-015 não complete/DONE; unidade004/PHASE-04 em progresso.
- Sem staging/commits/push/fecho/outro IMP e sem gates externos adicionais.

[V-ADD executado:0 erros](../../../experiments/construction/023-arvores/test-01/v-add-01.md),
sem alterar o validador ou reduzir o seu alcance. Negativos anteriores preservados.

## Reapresentação após manual humano — histórico anterior à aprovação

Manual cumprido na versão de JAR SHA-256
`e2a41113b8dbb4d3d68024f546bb570e7509fdd47a2d09366c4b96ef907a1188`,
fontes iguais às aprovadas em Implement/clean verify. Test está pronto para
aprovação explícita. Revisão final Denji e integração continuam pendentes.
Sem staging/commits/push/fecho/outro IMP; unidade004/PHASE-04 em progresso.

## Aprovação explícita de Test

O utilizador aprovou Test do IMP-012/023-arvores com clean verify1379/0/0/0,
19 XML,V-ADD aprovado e manual humano Francisco30 cumprido. O texto de
reapresentação anterior é histórico e não cria aprovação presumida de revisão.
Código/JAR/logs/negativos/Q-C-40–42 permanecem preservados. [Resumo final](final-review-summary.md)
e [plano/allowlist](integration-plan.md) preparados para revisão de Denji.
Revisão final Denji e integração continuam pendentes. Bolt aberto, sem fecho,
staging/commits/push/outro IMP; sem gates externos adicionais.

## Revisão final confirmada sem alteração da versão

Utilizador confirmou aprovação final de Denji desta implementação, sem alterações
pendentes. [Registo vinculado aos hashes](../../../experiments/construction/023-arvores/integration-01/review-confirmation.json).
Código/JAR e prova Test/manual intactos; não houve repetição de build/manual.
[Plano final](integration-plan.md) aguarda autorização de integração; bolt aberto,
sem staging/commits/push/fecho. Aprovação Test anterior mantida integralmente.
