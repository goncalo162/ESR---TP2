---
unit: "004-arvores"
bolt: "008-arvores"
stage: "test"
status: "complete"
created: "2026-11-20T16:06:41Z"
---
# IMP-013 — Test aprovado / V-016 e manual humano Luis

Test aprovado explicitamente pelo utilizador após confirmação pessoal de Luis:
sete etapas/28 verificações. Relatório humano e hashes conferidos; revisão final
Fillmore aprovada sem alterações por confirmação explícita. Commit/push autorizados.
Integração e fecho documental em preparação; completed=null até prova de integração.
Registos anteriores de manual pendente abaixo permanecem históricos.

## 1. Clean verify e JAR novo

JDK OpenJDK21.0.13.1 Ubuntu. Em Projeto/, com JAVA_HOME do JDK21:
`mvn -o -B -ntp clean verify`. Exit0;1419 testes/0falhas/0erros/0ignorados,
20 XML Surefire frescos, terminado em2026-11-20T16:00:57Z, Maven38.335s.
[Log](../../../experiments/construction/008-arvores/test-01/clean-verify.log),
[resultado e proveniência](../../../experiments/construction/008-arvores/test-01/clean-verify-result.json).
São1379 testes históricos e40 RecoveryTest, incluindo os cinco casos da correção
R10. Nenhum código aprovado, teste histórico, asserção ou prazo foi alterado.

JAR congelado: [ott-streaming-1.0-SNAPSHOT.jar](../../../experiments/construction/008-arvores/test-01/ott-streaming-1.0-SNAPSHOT.jar),
504997 bytes; SHA-256:
`81779be388778fd7dfacba5957e74ccffe5da45e86a403ea1ec5626f52c8f49d`.
As267 classes do JAR correspondem às classes do clean build,
[conferidas por hash](../../../experiments/construction/008-arvores/test-01/jar-classes-sha256.json).
O ensaio do manual confirmou RoutingService carregado deste JAR, sem target no
classpath, e fixtures de teste vindas das classes congeladas correspondentes.

## 2. V-016/R01–R18 consolidado tecnicamente

[Mapa método→XML](../../../experiments/construction/008-arvores/test-01/v016-oracles.json):
18 grupos com métodos reais e PASS nesta construção limpa; inclui os complementos
históricos M06/M07/M12/M13 e L12/L14/L19 para correlação, precisão e quotas.
O teste adicional de construtores/projeção também passou. Consolidação técnica
não equivale a aprovação Test, manual humano ou fecho dos critérios globais.
Sem percentagem de cobertura/JaCoCo/CI novos, conforme ADR-026.

| Grupo | Métodos RecoveryTest executados neste clean verify | Resultado técnico |
|---|---|---|
| R01 | `r01DiamondLosesAuthorityBeforeConfirmingTheAlternative` | PASS |
| R02 | `r02PartitionIsUnavailableWithoutLosingAdmittedIntentOrRetryingOnTicks` | PASS |
| R03 | `r03ReturnedSessionAutomaticallyRecoversTheSameConsumer` | PASS |
| R04 | `r04RetiredSessionAckErrorAndPhysicalCallbackCannotRetireReplacement` | PASS |
| R05 | `r05FailureAndEpochChangeAreIsolatedFromTheOtherOrigin` | PASS |
| R06 | `r06InvalidAnnouncementsCannotGrantCreditOrPoisonTheValidatedWatermark`, `r06SamePathNeedsANewValidatedGenerationAfterTheFailure`, `r06PeriodicAnnouncementDoesNotRearmAPermanentProtocolFailure` | PASS |
| R07 | `r07CancellationWinsAgainstCreditAndAnOldTicketCannotCancelItsReplacement` | PASS |
| R08 | `r08ADeadChildSessionDoesNotLeaveRecoverableDemand` | PASS |
| R09 | `r09TcpTwoRelaysAutomaticallyRenegotiateAChangedPrefixWithoutHumanRetry`, `r09SuspendedChildNeedsFreshHigherRevisionForTheChangedPrefix`, `r09ExpiredSuspendedInterestCannotKeepAGhostTree` | PASS |
| R10 | `r10AttemptsAndAnnouncementBurstShareOneRoundWindow`, `r10FirstValidMetricStillAdmitsTheInitialEligibleProof`, `r10AnAlternativeConsumedInsideTheRoundIsNotNewCreditAfterItsFailure`, `r10CadenceAndWatermarkSurviveTheMonotonicClockWrap`, `r10ASecondConsumerAndAnnouncementDoNotExtendTheGlobalDeadline`, `r10AnAnnouncementSeenWhilePendingIsBlockedByTheFailureWatermark`, `r10MetricRecoveryCannotReuseANewerAnnouncementAlreadySeenBeforeFailure`, `r10MetricOverflowAndRecoveryCannotReuseTheBlockedGeneration`, `r10PostFailureAnnouncementWithoutMetricRemainsUsefulForFirstEligibleObservation`, `r10ExpiredCoalescedCreditCannotBeSpentByALateTimer`, `r10NewPostFailureAnnouncementStillRearmsAfterMetricRecovery` | PASS |
| R11 | `r11AnAckAtTheExactDeadlineOrWithExcessLeaseCannotConfirm`, `r11RecoveryAckNeedsAcceptanceAndKeepsTheOriginalRequestAnchor` | PASS |
| R12 | `r12NewEpochRetainsIntentButRequiresFreshAuthority` | PASS |
| R13 | `r13ReleasedCapacityNeedsANewAnnouncementToReopenTerminalAdmission`, `r13StillFullCapacityBlocksTheNewProofAgainWithoutAHiddenRetry` | PASS |
| R14 | `r14AProcessedContinuationDoesNotReleaseItsStillRunningCallback`, `r14NewSessionsAndEpochsCannotEscapeTheCanonicalPhysicalStreamLimit`, `r14AnEpochReplacementSharesTheSamePhysicalReservations` | PASS |
| R15 | `r15ShutdownDoesNotReleaseHeldCallbacksOrAdmitNewRecovery`, `r15ShutdownDiscardsBudgetCreditWithoutSubmittingAnotherOperation` | PASS |
| R16 | `r16NoUdpSilenceEventOrMetricRefreshCreatesRecovery` | PASS |
| R17 | `r17TcpSequentialRelayFailuresConsumeExistingSourceClientRepairWithoutBootstrap` | PASS |
| R18 | `r18FreshPostFailureGenerationAllowsLegitimateRenewOnTheSamePath`, `r18DivergentSelectionAndRenewDenialPreserveGrantWithoutRepeatingRenew`, `r18RecoveredGrantResumesRenewalAfterItsOldIncompatibilityIsRetired` | PASS |

R01/R02/R03 demonstram alternativa/UNAVAILABLE/regresso com intenção admitida.
R04/R06/R08/R10/R12 demonstram proteção de contexto, prova, época, prazo e
mensagens antigas. R05 isola outra origem. R09 exige renegociação fresca dos
filhos, sem prolongar grants antigos; R14/R15 mantêm reservas até conclusão
física e o orçamento de shutdown. R18 preserva grant saudável perante seleção
ou recusa14, sem antecipar IMP-014. R10 inclui a reprodução exata do overflow
métrico corrigido:10→2000→10 não reutiliza prova bloqueada; primeira medição
válida e ANN posterior continuam a permitir recuperação limitada.

R09 TCP usa S–(A,B)–R–C: a perda altera o prefixo, o filho emite ACTIVATE com mesma
Line e revisão/request novos automaticamente. R17 TCP usa S–(R1,R2)–C: bootstrap
fechado antes das falhas sequenciais, reparação existente cria Session S–C,
ANN/ACTIVATE/ACK confirmam path[S,C] sem novo pedido humano. Métricas fixas,
clock real, uma JVM; fecho de serviços e rebind das portas de controlo verificados.

**Limites:** R16 verifica que ausência de evento UDP não cria incidente de routing;
não é bloqueio UDP real, vídeo, GUI, CORE ou medição de SLA/FPS. TCP usa métricas
controladas e não explica flutuações reais de RTT ou toda a disponibilidade de
um deployment. Não se transforma sucesso num compromisso de recuperação em
partição permanente, saturação contínua ou falha exclusiva UDP (ADR-025).

## 3. R17 inicial — negativo e causa desconhecida mantidos

O erro inicial do ticket R17 em Implement/focused-04 está preservado com log,
XML e fonte da fixture dessa execução. A razão/correlação não foi recolhida;
**a causa continua não estabelecida**. A fixture atual fixa métricas e espera
prefixo coerente. O seu sucesso, nesta construção e no ensaio, **não demonstra
a causa da falha anterior** e não autoriza apagar o negativo.
[Diagnóstico/limite histórico](../../../experiments/construction/008-arvores/implement-01/focused-04/diagnosis.md),
[log negativo](../../../experiments/construction/008-arvores/implement-01/focused-04/maven.log).
Também permanecem os negativos R13, prazo em fila, alternativa consumida e
R10/métrica, com as correções aprovadas, sem reescrever as evidências anteriores.
Qualquer nova correção requer reprodução/âmbito apresentados antes de alterar.

## 4. Manual humano preparado; ainda pendente

[Roteiro de sete etapas](../../../experiments/construction/008-arvores/manual-instructions.md) e
[manifesto](../../../experiments/construction/008-arvores/test-manual-manifest.json):
28 verificações; snapshots de recuperação/forwarding/recursos, R10, época/intenções,
reservas físicas/shutdown e dois casos TCP. Executa no JAR congelado primeiro no
classpath, usando fixtures aprovadas e oito bibliotecas de teste existentes,
congeladas fora de target. Compila só o auxiliar; não recompila o produto.
Nenhuma dependência nova no POM ou artefacto de produção.

[Ensaio Codex separado](../../../experiments/construction/008-arvores/test-01/manual-agent-verification.json):
kind=agent,28 CHECK_PASS, CLEANUP_COMPLETE, MANUAL_PASS CHECKS=28, JAVA_EXIT0,
wrapper0, passed=true, hashes/origem do produto e fixture conferidos e JVM
terminada/recolhida. **Não conta como repetição humana ou aprovação Test.**
O utilizador deve executar kind=human, devolver report.json/WRAPPER_EXIT e
confirmar pessoalmente as sete etapas. Não pedir aprovação de Test antes disso.

## 5. Preservação e estado

Antes do clean, target anterior foi arquivado integralmente em test-01 para
preservar outputs antigos. Clean removeu/regenerou só o output de build autorizado.
Fontes aprovadas/históricos/recursos/POM e evidências anteriores conferidos por
SHA-256 antes/depois; nenhuma mudança em Projeto/src ou CORE nesta etapa.
As400 evidências anteriores de008-arvores são históricas e permanecem intactas.
Atualizar metadados vivos de aprovação não reescreve os seus manifestos históricos.

Bolt/TASK/unidade004/PHASE-04 em progresso; história sem declaração de conclusão
ou implemented=true. Clean verify e V-016 técnico concluídos; **Test aguarda a
repetição humana e posterior aprovação**, depois Fillmore/integração. Nenhum
fecho/staging/commit/push ou outro IMP; external_gates=[] preservado.

[Preservação conferida](../../../experiments/construction/008-arvores/test-01/preservation.json):
257 ficheiros de fontes/recursos e400 evidências anteriores intactos.
[V-ADD passou sem erros](../../../experiments/construction/008-arvores/test-01/v-add.md);
git diff --check passou. São verificações documentais, não aprovação humana.

## Aprovação Test/manual e revisão final

Registo 2026-11-20T16:17:51Z: [manual humano original](../../../experiments/construction/008-arvores/test-manual-runs/20261120T161206Z-w1jlv9q6/report.json)
e [conferência independente](../../../experiments/construction/008-arvores/manual-human-verification-01/verification.json).
Luis confirmou pessoalmente sete etapas/28 checks; JAVA_EXIT0, hashes e origem
corretos, JVM terminada. WRAPPER_EXIT não fornecido, não inferido.
[Fillmore aprovou sem alterações](../../../experiments/construction/008-arvores/integration-01/review-confirmation.json),
por confirmação explícita do utilizador. Integração autorizada, ainda por
comprovar; nenhum código/build/manual repetido. Limitação e negativos R17 intactos.
