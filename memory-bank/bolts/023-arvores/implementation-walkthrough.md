---
unit: "004-arvores"
bolt: "023-arvores"
stage: "implement"
status: "complete"
---
# IMP-012 — checkpoint Implement dos comportamentos

Model/Design/ADR e primeiro passo de tipos/testes vermelhos aprovados pelo
utilizador. Comportamentos e ampliação V-015/L01–L24 autorizados nos três
ficheiros. **Implement aprovado pelo utilizador; Test autorizado e em curso.** Base main/b273b258a4642f94f8614ae00f060a46318d77a5.
Sem staging/commits/push/fecho ou outro IMP. Unidade004/PHASE-04 em progresso.

## Resultado atual e âmbito

Regressão incremental JDK Temurin21.0.13+9/Maven3.9.9: **1379 testes,
0 falhas,0 erros,0 ignorados**, Maven exit0;19 XML preservados. Inclui51 novos
SubscriptionLeaseTest,31 ActivationTest e os6 M07 históricos. Resultado calculado
a partir dos XML, não por somar contagens históricas.
[Resultado/hashes/mapa](../../../experiments/construction/023-arvores/implement-behavior-01/regression-02-result.json),
[log completo](../../../experiments/construction/023-arvores/implement-behavior-01/regression-02.log) e
[preservação](../../../experiments/construction/023-arvores/implement-behavior-01/preservation.json).

Comando executado: `JAVA_HOME=/Users/maria/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home mvn -B -ntp -f Projeto/pom.xml test`.
Sockets locais executados fora da sandbox após bind recusado; negativo preservado.
É regressão incremental em Implement, **não clean verify nem aprovação Test/V-015**.
Não foi produzido JAR final ou executado manual humano. Revisão final Denji e
integração continuam pendentes; nenhum gate externo adicional.

Somente Subscription.java, RoutingService.java e o novo SubscriptionLeaseTest.java.
[Diff Java contra base](../../../experiments/construction/023-arvores/implement-behavior-01/java.diff),
[numstat](../../../experiments/construction/023-arvores/implement-behavior-01/java.numstat):

```text
141	53	Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java
23	0	Projeto/src/main/java/pt/uminho/esr/ott/routing/Subscription.java
574	0	/dev/null => Projeto/src/test/java/pt/uminho/esr/ott/routing/SubscriptionLeaseTest.java
```

Target contém saídas de Maven e fica fora da entrega, sem restauro/limpeza.
APIs públicas, record Subscription/Lease, State PENDING/ACTIVE, tickets e snapshots
preservados. Sem alterações a NeighborManager/send(), OverlayNode, transporte,
protocolo, enums wire, configuração, POM ou testes históricos. TCPv2/19 tipos,
código3 rejeitado/framing/caps/UTF-8 e UDP permanecem intactos.

## Adaptações justificadas de código existente

Cada substituição é local à ativação/leases nos ficheiros aprovados; não há
remoção de testes, fundações ou tratamento de eventos alheios.

| Adaptação / razão | Critérios |
|---|---|
| Dispatcher inclui13/14 e ERROR related11/13/14 antes de trace; correlação de erro também exige espécie da operação. Preserva trace15/anúncios/ERROR alheios;13 não cria ACK fictício | F04; L10/L18 |
| Child/Line guardam espécie11/14, budget e fingerprint; RENEW exige grant existente válido. Rejeição antes de admissão não consome watermark. Revisão crescente transfere o grant saudável, sem reativar cancelado | F01/F03/F04; Q-C-40/41; L08–L16 |
| Round admite renovação separada, uma operação e uma submissão; tentativa inicial mantém A/T. Grant anterior não é limpo para entrar em startRound; sucesso exige ACK correlacionado + ACCEPTED e ancora no início do pedido | F01/F05; L05–L09/L17 |
| Cadência constante P=min(renew,G/3), baseada no último início real, sem catchup; budget=min(T,restante). Timers comuns passam a considerar renovação/poda/expiração | F01/F05; L04–L07/L20 |
| Procura efetiva distingue tickets confirmados/pending e children ainda válidos; última retirada invalida operação e captura uma poda. Session/epoch/ID/revisão exatos no recetor; poda usa maior revisão submetida e Line estável, um envio e T imutável, revalidado no port | F01/F04; L01–L04/L10–L12 |
| ACK de RENEW novo deixa de ser limitado pelo grant antigo do child; ACTIVATE/replay mantêm esse limite. Duplicado pending só coalesce; replay concluído devolve restante sem substituir valor/lease/revisão de publicação | F02/F03/F05; L07/L08/L11 |
| send usa quota somente para ACK/ERROR/UNBOUND;11/13/14 partilham B/session/Line, sem quota paralela. Contexto upstream passa de UUID de stream a Line canónica para reter2/contexto entre gerações | F06; L12/L19 |
| Conclusão usa budget da operação e mantém guarda terminal M07 antes de publicar; finally e callbackDone&&continuationDone preservados. Shutdown existente não emite13/14 após stopping e mantém primeiro budget global | F03/F07; L11/L12/L22 |
| Subscription valida null/tamanho2..32 antes de List.copyOf/iteração; mantém cópia defensiva e todas as invariantes/assinaturas | L23; limites antes de alocar |

PruneRequest captura identidade e prazo imutáveis; RenewalRequest captura grant
anterior e budget. Tipos internos, sem campo novo no wire ou nova API pública.
Falha local INVALID_MESSAGE/exceção termina só a operação; CLOSED/OVERFLOW de
Session corrente mantém a política de perda física existente. Session velha não
retira substituta. Fonte sem procura mantém oferta/catálogo e zero forwarding.

## Oráculos exercitados

51 métodos novos, com owner/relógio/scheduler/port falsos para ordens determinísticas,
e um teste TCP real que compõe manager/monitor/routing no mesmo owner por nó.
O mapa liga todos os24 grupos a asserts executados; limites/fundamentos partilhados
são adicionalmente verificados pela regressão histórica intacta. Consolidação
formal V-015 e aceitação global pertencem à futura etapa Test.

| Grupo | Métodos executados em SubscriptionLeaseTest |
|---|---|
| L01 | `l01SharedTrunkRenewsOncePerCycleAfterPartialDeparture`, `l01DeactivateOneChildPreservesSiblingAndCommonTrunk` |
| L02 | `l02LastCancellationRemovesAuthorizationBeforeHeldPruneCompletes` |
| L03 | `l03ChildLeavesBeforeLocalConsumer`, `l03MixedLocalAndChildDemandPrunesOnlyAfterBothLeave` |
| L04 | `l04OrphanChildExpiresAndPrunesItsCommonTrunk`, `l04SourceExpiryAlreadyExcludesOrphanWithoutNewNetworkInput` |
| L05 | `l05ThreeCyclesUseOneLineAndStrictlyIncreasingRevisions`, `l05PeriodicRenewalKeepsIdAndAdvancesRevisionAndRequest` |
| L06 | `l06OneMillisecondGrantUsesSubMillisecondCadenceAndExactExpiry`, `l06ShortGrantSchedulesAtOneThirdAndStillExpiresWithoutAck` |
| L07 | `l07EarlyRenewAckWaitsForAcceptanceAndUsesRequestAnchor`, `l07OversizedAndExactDeadlineAckDoNotReplaceGrant`, `l07SubMillisecondParentRemainderIsNeverRoundedUp` |
| L08 | `l08PendingDuplicateAndWrongKindCannotReanchor`, `l08TerminalReplayIsBoundedAndFreshRenewCanKeepOldHealthyGrant`, `l08NewRenewalExtendsButExactDuplicateNeverRenewsAgain` |
| L09 | `l09MaximumRevisionNeverWrapsAndEqualNewRequestIsRejected`, `l09LatePreviousAckCannotReplaceNewRenewal` |
| L10 | `l10OldPruneCannotCancelNewActivationOnSameSession`, `l10OldDeactivateCannotRemoveRenewedRevision` |
| L11 | `l11CancelledRenewAckCannotRestoreChildOrChangeSiblingLease`, `l11UnsentPruneExpiresAndNewDemandInvalidatesOldIntent`, `l11PruneCapturesSubmittedRenewalRevisionAndLateAckCannotChangeReplacement`, `l11UnknownHigherPruneUsesLeaseFallbackWithoutApproximateCancellation` |
| L12 | `l12CancelledRenewAckRetainsPhysicalSlotUntilCallbackAndContinuationEnd` |
| L13 | `l13ExpiredRenewalIsExplicitlyRejectedWithoutActivating`, `l13RenewBeforeInitialAckAndAfterCancelCannotActivate` |
| L14 | `l14BothDivergentPrefixesPreserveConfirmedGrant`, `l14DivergentRenewalPreservesOldGrantAndRejectsExplicitly`, `l14SamePathMetricChangeAllowsRenewal` |
| L15 | `l15OldRenewCallbackAndAckCannotInvalidateNewInstance`, `l15OldSessionDeactivateCannotAffectReplacementConnection` |
| L16 | `l16DeactivateOneStreamPreservesOtherStreamExactly` |
| L17 | `l17PhysicalFailureInvalidatesOnlyDependentSession`, `l17LocalSendFailurePreservesGrantAndAcceptedAloneDoesNotExtend` |
| L18 | `l18RenewErrorAndTraceErrorDoNotInvalidateHealthyGrants`, `l18WrongAckFieldsAndErrorKindsAreIsolated` |
| L19 | `l19SmallSharedHistoryAndLocalCapsPreserveHealthyGrant`, `l19UnboundErrorsShareGlobalAndPerSessionPhysicalCaps`, `l19RetiredGenerationsShareStablePhysicalContext`, `l19RenewRepliesShareExistingFixedWindowQuota`, `l19RenewalsShareFourPendingPerSessionAndNoParallelPool` |
| L20 | `l20MonotonicWrapPreservesRenewalCadence`, `l20LateTimerAdmitsOneCurrentRenewalWithoutCatchup` |
| L21 | `l21SourceCatalogSurvivesLastConsumerAndOtherStream` |
| L22 | `l22ShutdownWaitsForPhysicalRenewalCompletionWithinOriginalBudget`, `l22ShutdownAfterPruneDoesNotSendMoreAndRetainsPhysicalReservation` |
| L23 | `l23OversizedSubscriptionPathIsRejectedBeforeCopyOrIteration`, `l23SubscriptionCopiesValidPathAndRetainsPublicShape` |
| L24 | `l24TcpTwoRelaysAggregateRenewPruneExpireAndReleasePorts` |

L24: bootstrap e cinco nós reais em loopback, S–R1–R2–(C1,C2), duas camadas de
relay. Mantém ambos os consumidores por3300ms com lease configurado3000ms,
observa14 na fonte, retira C1 preservando C2/tronco, retira último e observa13,
reativa explicitamente C2 e encerra só o seu routing mantendo TCP para testar
expiração órfã. Cleanup verifica await de routing/monitor/manager/owner e rebind
das seis portas TCP de controlo. Não abre/reivindica vídeo/GUI/CORE, subprocessos,
portas UDP ou manual humano. Grants não representam vídeo já implementado.

M07 histórico: destinos antes/depois do cancelamento/ACCEPTED tardio continuam
`[C2]`, e os6 casos de lease/substituta/reserva física passaram. Nenhum desses
asserts foi alterado. No novo L12, callback bloqueado conserva B mesmo após
continuação; L19 mantém envios retirados no mesmo limite2/Line.

## Negativos preservados e sequência

Primeiro checkpoint22/20/0/0 e anterior21/19/0/0 permanecem imutáveis em
implement-interfaces-01; [walkthrough anterior](../../../experiments/construction/023-arvores/implement-behavior-01/prior-walkthrough.md.txt).
Nesta autorização:

- attempt-01:22 testes/1 falha; replay reconstruía Subscription/Lease. Corrigido
  para manter o valor exato e não alterar revisão de publicação no replay.
- attempt-02:53/0 falhas/1 erro, exclusivamente bind bloqueado pela sandbox;
  leases22/0/0/0 e30 históricos sem TCP passaram. attempt-03 fora da sandbox:
  53/0/0/0 (log preservado; XML finais vêm da regressão integral).
- expanded-01:38/2/0/0; duplicado pending marcava replay prematuramente e replay
  alterava revisão de publicação. Corrigidos sem remover/enfraquecer os asserts.
- expanded-02/03: compilação do novo harness TCP falhou por nomes de accessor
  incorretos. Corrigidos apenas no novo teste, sem adaptar APIs de produção.
- expanded-04:39/0/0/0 incluindo TCP; expanded-05:47/0/0/0.
- regression-01:1375/0/0/0,19 XML; novas quatro fronteiras justificaram a ampliação
  para51 e boundaries-01:51/0/0/0. regression-02 final:1379/0/0/0,19 XML.

Logs e XML disponíveis no [inventário de evidência](../../../experiments/construction/023-arvores/implement-behavior-01/README.md),
sem sobrescrever negativos. Não há repetição para ocultar falhas históricas;
as correções e ampliações que justificaram cada execução estão explicitadas.

## Hashes e preservação

- `Projeto/src/main/java/pt/uminho/esr/ott/routing/Subscription.java`: `7d3c8af82c551b56db14d568662711195a5bbb0592d44c057ae305c78958157a`.
- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`: `63ed46022b47258a6fcff6fa848714176b438ad19bf5a9eca3778ff9f1ca3d46`.
- `Projeto/src/test/java/pt/uminho/esr/ott/routing/SubscriptionLeaseTest.java`: `5c54b2cde853b79335ec421c9bd3bc0f6ae7d2ca21843cb7678fd0cc8e3cf53b`.

preservation.json compara230 outros ficheiros src anteriores,19 testes históricos,
31 artefactos Model/Design/ADR e27 do primeiro checkpoint: todos intactos.
POM e scripts/validate-planning.py intactos; staged diff vazio; HEAD permanece base.
Q-C-36–39 e evidências dos fechos anteriores não foram reabertas.
Q-C-40–42 preservadas, sem CLI/novo âmbito ou ADR. A lista exata de source hashes
e dos testes históricos está guardada para revisão.

## Checkpoint

Parar em Implement para aceitação. Bolt/história/TASK-015 não concluídos;
implemented=false, só Model/Design/ADR em stages_completed. Pendentes Test
(V-015 consolidado/clean verify/JAR), manual humano, revisão final Denji e integração.
Sem staging/commits/push/fecho/outro IMP. Nenhum gate externo adicional.

Validação documental: [V-ADD sem erros](../../../experiments/construction/023-arvores/implement-behavior-01/v-add-02.md).
Os relatórios v-add-initial.md/v-add-final.md preservam a rejeição do formato
do updated (offset/frações); corrigido só o formato para UTC em segundos/Z,
sem alterar o validador, os critérios ou as datas de aprovação histórica.

## Aprovação Implement e correção documental

O utilizador aprovou Implement e autorizou Test. O texto anterior de checkpoint
é histórico; estado atual no [relatório Test](ddd-03-test-report.md).
Contagem corrigida:19 XML por regressão, não20. [Inventário/errata](../../../experiments/construction/023-arvores/test-01/xml-count-erratum.json).
Originais/manifestos/negativos em experiments não foram alterados. Clean verify
1379/0/0/0 e19 XML novos consolidados em Test; hashes Java iguais aos aprovados.
Manual humano, aceitação Test, revisão final Denji e integração pendentes.

## Fecho documental autorizado — estado atual

IMP-012/023-arvores/TASK-015 concluído após integração local4d18d1d comprovada,
Test1379/0/0/0,19 XML,V-ADD,Francisco30 e revisão final Denji.
[Fecho e evidência](closure-walkthrough.md): bolt/história complete,
implemented=true,TASK-015 DONE. Unidade004/PHASE-04 em progresso.
Texto anterior preserva as etapas/planos históricos; sem Java/push/novo IMP no fecho.
