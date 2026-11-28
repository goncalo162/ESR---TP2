---
unit: "005-video"
bolt: "026-video"
stage: "test"
created: "2026-11-28T19:21:16Z"
updated: "2026-11-28T19:55:07Z"
status: "accepted"
---
# IMP-019 / V-020-C — Test aceite; gates confirmados

## Resumo

- **Ambiente válido:** OpenJDK 21.0.13.1, Maven 3.6.3, compilação `--release 21`,
  Surefire 3.5.4 / JUnit Platform.
- **JDK 21 — seleção `RouteTraceTest#t14*`, `#t15*`, `#t16*`:** 5 testes, 5 passaram,
  0 falhas/erros/skips; `BUILD SUCCESS`. Os três `t14`, `t15` e `t16` foram
  executados, incluindo o grafo TCP/EOF real e os fixtures `.Mjpeg`.
- **Suíte Maven pertinente:** 150 testes, 0 falhas/erros/skips,
  `BUILD SUCCESS`.
  - `StreamingServerTest`: 8/8
  - `OverlayNodeRoutingTest`: 7/7
  - `RoutingServiceTest`: 41/41
  - `OttApplicationTest`: 53/53
  - `RouteTraceTest`: 41/41
- **Suíte Maven completa:** 1599 testes, 0 falhas/erros/skips, `BUILD SUCCESS`.
- **JaCoCo 0.8.12 — relatório regenerado após a suíte completa:** bundle **91,77%**
  (7573/8252 linhas); linhas executáveis adicionadas nos quatro ficheiros de
  produção **81,58%** (673/825). Ambos excedem estritamente o gate >80%.
- **Correções de produção após Test:** nenhuma necessária pelos resultados
  funcionais em JDK 21.

## Comandos

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH mvn -f Projeto/pom.xml test '-Dtest=RouteTraceTest#t14*,RouteTraceTest#t15*,RouteTraceTest#t16*' '-DargLine=-javaagent:/home/luis/.m2/repository/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar=destfile=target/026-video-jacoco.exec,append=false'
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH mvn -f Projeto/pom.xml test '-Dtest=StreamingServerTest,OverlayNodeRoutingTest,RoutingServiceTest,OttApplicationTest,RouteTraceTest' '-DargLine=-javaagent:/home/luis/.m2/repository/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar=destfile=target/026-video-jacoco.exec,append=false'
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH mvn -f Projeto/pom.xml test '-DargLine=-javaagent:/home/luis/.m2/repository/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar=destfile=target/026-video-jacoco.exec,append=false'
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH mvn -f Projeto/pom.xml org.jacoco:jacoco-maven-plugin:0.8.12:report -Djacoco.dataFile=target/026-video-jacoco.exec
```

Todos os comandos de teste usaram OpenJDK 21.0.13.1. JaCoCo instrumentou o
fork Surefire, sem alteração do POM; a medição de linhas alteradas intersecta
as linhas executáveis adicionadas no diff dos quatro ficheiros de produção com
os contadores `LINE` do `jacoco.xml`. As JVMs filhas de testes CLI não foram
instrumentadas.

## Ampliação de cobertura nesta retoma

- `RouteTraceTest.t06LateResultAfterGlobalTimeoutCannotReplaceTerminalOutcome`
  verifica que uma resposta recebida depois do prazo global não substitui o
  timeout terminal nem inicia outro pedido.
- `OttApplicationTest.m17TraceAndSnapshotDiagnosticsAreBoundedAndKeepCliUsable`
  cobre validação do comando trace e os caminhos vazios de listagem
  routes/catalog/metrics.
- `OttApplicationTest.m18ServerQualificationUsesMjpegAndHandsPreparedSourcesToCompositionFactory`
  gera dois fixtures `.Mjpeg` mínimos válidos, qualifica as fontes e verifica
  transferência/fecho através do ponto de composição injetável.
- `OttApplicationTest.m19ServerCompositionRunsDiagnosticsAndClosesQualifiedSources`
  executa composição real de Bootstrap/Server/Relay in-process, espera readiness,
  verifica trace TCP remoto e diagnósticos, e confirma terminação.
- `OttApplicationTest.m20ServerCompositionFactoryFailureClosesEveryQualifiedSource`
  verifica cleanup dos dois handles qualificados quando a factory falha.
- `RoutingServiceTest.qC50LocalSourceAccessIsOwnerBoundVersionedAndRejectsStaleUpdates`
  cobre binding, versões de procura, validação de epoch e confirmação de child/local.
- `StreamingServerTest.p00ValueObjectsAndPreparationRejectInvalidBounds`
  valida os limites dos contratos e recusa consumo de fontes já fechadas.
- `OverlayNodeRoutingTest.qC50WithLocalSourcesBindsOnOwnerReceivesDemandAndHandlesStop`
  também verifica submissão ao owner e preservação do motivo primário se o listener
  falhar durante shutdown.

## Validação V-020-C e critérios

- **Procura confirmada, FPS e sem catch-up ilimitado:** `p02P03DemandGatedEmissionPauseResumeAndNoUnlimitedCatchUp` confirma ausência de emissão sem procura, primeira emissão e apenas um frame após retoma com avanço do relógio.
- **Estado independente, loop monotónico e EOF isolado:** `p12P14LoopMonotonicityAndEofIsolation` verifica sequência/timestamp crescentes no loop, retirada da stream sem loop no EOF e disponibilidade preservada na outra.
- **Qualificação, cancelamento e limites de cleanup:** `p01PrepareFailsBeforeAnyNodeAndCleansOpenedHandles`, `p01PrepareRespectsBoundedBudgetWhenCancelledDuringBlockingRead`, `p18P20UnwiredRefusesAndFailedSourcePreservesCleanupUncertain`, `p19FatalDoesNotPinZeroBudgetAndBlockedWorkerIsNotClosedConcurrently`, `p20FatalOpenerExceptionUnblocksOtherBlockedWorkerWithinBudget` e os testes de integração `qC50*` passaram.
- **`t14`–`t16`:** os 5 métodos selecionados passaram em JDK 21; cobrem help/validação/listagem e fixtures CLI, encaminhamento TCP alternativo, ausência de vídeo e EOF durante trace pendente.

## Cobertura: medições incrementais e gate final

O tipo DDD define critério de cobertura superior a 80%. As primeiras medições
incrementais ficaram abaixo do gate (61,0% no bundle e 69,0% nas linhas novas);
não foram aceites. Após os testes adicionais, as medições finais do relatório
gerado depois da suíte completa são:

- Bundle completo, suíte Maven completa: **91,77%** (7573/8252 linhas).
- Linhas executáveis adicionadas nos quatro ficheiros de produção: **81,58%**
  (673/825).
- Por ficheiro: `RoutingService` 100% (87/87), `StreamingServer` 81,21%
  (484/596), `OverlayNode` 73,81% (31/42) e `OttApplication` 71% (71/100).
  O critério para linhas alteradas é o conjunto agregado dos quatro ficheiros;
  o agregado excede >80%.

A medição principal instrumentou o fork de testes, não as JVMs filhas criadas
pelos testes CLI; a cobertura do caminho de composição Server/Relay foi obtida
em-processo pelo teste determinístico `m19`, que espera readiness e valida o trace
remoto. A tentativa exploratória anterior sem readiness não foi contada.

Uma primeira tentativa `t14`–`t16` em JDK 25 teve três timeouts de saída de
processos CLI; a execução no JDK 21 requerido passou. Não foram efetuados testes
de carga/benchmark nem uma suíte de segurança específica; V-020-C define
validação determinística de pacing, não um objetivo de throughput numérico.
Esta é a suíte mínima pertinente, não uma regressão completa do projeto nem
uma execução manual humana/revisão/integration gate de fecho.

**Estado:** o utilizador aprovou explicitamente a validação e o fecho de Test e
do bolt. Reexecução JDK 21 confirmou a suíte focada (5/5), suíte pertinente
(150/150), suíte completa (1599/1599) e ambos os gates JaCoCo (>80%). Test aceite;
`bolt-complete.cjs` concluiu o fecho, e a história 008 está `complete`/
`implemented=true`. A unidade permanece em progresso por ter outros quatro
bolts incompletos; TASK-020 não foi marcada DONE. Não se afirma execução de
manual humano, revisão Denji ou integração neste checkpoint.
