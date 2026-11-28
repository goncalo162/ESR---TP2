---
unit: "005-video"
bolt: "026-video"
stage: "implement"
status: "complete"
created: "2026-11-28T16:01:00Z"
updated: "2026-11-28T19:21:16Z"
---
# IMP-019 / 026-video — Implementation Walkthrough (Implement aprovado)

**Estado:** implementação preparada nos **8 caminhos autorizados por Q-C-50**, com o 9.º caminho fixture-only autorizado em adenda. O utilizador aprovou explicitamente “Aprovo implement e avança para Test” às 2026-11-28T19:11:45Z; o checkpoint de Implement está aceite e o bolt avançou para `current_stage=test`. A evidência de testes indicada abaixo é histórica/focalizada; a validação de Test permanece pendente neste momento.

## 1. Ficheiros alterados / criados (8 caminhos Q-C-50 + adenda fixture-only)

### Produção (4 ficheiros)
1. [RoutingService.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
   - Adicionados `AvailabilityOutcome` (`APPLIED`, `ALREADY_APPLIED`, `STALE`, `STOPPED`), `SourceDemand`, `LocalSourceListener`, `LocalSourceAccess` e `bindLocalSourcesOnOwner(LocalSourceListener)`.
   - `observeOnOwner(UUID)` calcula `SourceDemand` ao instante atual (`now()`) no owner, exigindo origem local corrente, `Session` atual, referência confirmada não cancelada e lease positivo; `version` muda apenas em alterações efetivas de autorização/disponibilidade/época.
   - `setAvailableOnOwner(UUID, UUID, boolean)` valida owner/estado/época esperada antes de delegar em `setLocalAvailable`.
   - `notifyLocalSourcesIfChanged(long)` notifica `onDemandChangedOnOwner` após `publish(finished)` no mesmo turno do owner; `cleanup()` invoca `onStoppedOnOwner()`.
2. [OverlayNode.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java)
   - Adicionados `OwnerAccess`, `LocalSourceBinding` e `withLocalSources(...)` (mantendo todas as factories/construtores anteriores intactos).
   - Em `start()`, no turno inicial do owner após `monitor.start()` e `routing.start()`, liga `routing.bindLocalSourcesOnOwner(...)` ao `LocalSourceBinding`, instala `owner.onStop(...)` e propaga `RuntimeException`/`Error` e rejeições de `owner.offer` para `failMonitoring` + `owner.fail`.
3. [StreamingServer.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/server/StreamingServer.java)
   - Implementada qualificação prévia `prepare(...)` com `DEFAULT_WORKERS = 2`, abertura e leitura/validação do primeiro frame de todas as fontes antes de qualquer nó, cancelamento via `AtomicBoolean` e limpeza de handles já abertos em caso de falha (`PreparationException` com `Map<UUID, CleanupOutcome>`).
   - Produtor com `1` coordenador + `2` workers, `ReadPermit` revogável no turno do owner, pacing exato por `fps` sem catch-up ilimitado em pausa/retoma, `frameSeq` e `timestampNanos` monotónicos em `loop`, isolamento de EOF/erro por stream com retirada via `setAvailableOnOwner(..., false)`, `FramePort.UNWIRED` explícito (`UNAVAILABLE`), `ShutdownBudget` único e preservação de `CLEANUP_UNCERTAIN` após `VideoSource.State.FAILED`.
4. [OttApplication.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java)
   - Em `NodeRole.SERVER`, qualifica todas as fontes via `StreamingServer.prepare(...)` antes de criar/iniciar `OverlayNode.withLocalSources(...)`, ligando o cancelamento de preparação ao `Console.stop(...)` (usado pelo shutdown hook) e partilhando um único `ShutdownBudget` no `RunningService`.
   - Adicionadas interfaces package-private `ServerNodeFactory` e `ConsoleAccess` para ensaios de composição mantendo `ServiceFactory` e `run(...)` históricos.

### Testes (4 ficheiros)
5. [StreamingServerTest.java](../../../Projeto/src/test/java/pt/uminho/esr/ott/server/StreamingServerTest.java) — `p01PrepareFailsBeforeAnyNodeAndCleansOpenedHandles`, `p02P03DemandGatedEmissionPauseResumeAndNoUnlimitedCatchUp`, `p12P14LoopMonotonicityAndEofIsolation`, `p18P20UnwiredRefusesAndFailedSourcePreservesCleanupUncertain`.
6. [OverlayNodeRoutingTest.java](../../../Projeto/src/test/java/pt/uminho/esr/ott/overlay/OverlayNodeRoutingTest.java) — `qC50WithLocalSourcesBindsOnOwnerReceivesDemandAndHandlesStop`.
7. [RoutingServiceTest.java](../../../Projeto/src/test/java/pt/uminho/esr/ott/routing/RoutingServiceTest.java) — `qC50LocalSourceBridgeNotifiesDemandChangesAndValidatesEpochOnWithdraw`.
8. [OttApplicationTest.java](../../../Projeto/src/test/java/pt/uminho/esr/ott/app/OttApplicationTest.java) — `LiveDeployment` atualizado para gerar `a.Mjpeg`/`b.Mjpeg` válidos para `streams-s1.properties` no `SERVER`, e `qC50ServerQualifiesAllSourcesBeforeNodeFactoryAndSharesShutdownBudget`.
9. [RouteTraceTest.java](../../../Projeto/src/test/java/pt/uminho/esr/ott/routing/RouteTraceTest.java) — adenda de 16:15, estritamente para gerar ficheiros `.Mjpeg` mínimos nos fixtures de `CliGraph` usados por `t14`–`t16`; a comparação do conteúdo Java confirma apenas a adição do helper e da criação desses fixtures, sem alteração das asserções existentes.

## 2. Resultados observados (JDK 25.0.5.1, release 21)

- `mvn -f Projeto/pom.xml test -Dtest=StreamingServerTest,OverlayNodeRoutingTest,RoutingServiceTest,OttApplicationTest#qC50*`:
  - `OverlayNodeRoutingTest`: **7/7** passaram.
  - `RoutingServiceTest`: **40/40** passaram.
  - `StreamingServerTest`: **6/6** passaram (inclui `p19FatalDoesNotPinZeroBudgetAndBlockedWorkerIsNotClosedConcurrently` e `p01PrepareRespectsBoundedBudgetWhenCancelledDuringBlockingRead`).
  - `OttApplicationTest#qC50*`: **1/1** passou.
  - Total focado: **54/54** passaram.

## 3. Correções P1 aplicadas após revisão (2026-11-28T16:11:11Z)
- **Orçamento fatal e threads daemon (`StreamingServer`):** `recordFatal()` e `onStoppedOnOwner()` chamam `initiateStop(null)` sem fixar `Duration.ZERO`, preservando o orçamento positivo da aplicação; threads de `prepare()`, coordenador e workers são `daemon`, com `W = Math.min(2, orderedCells.size())` e aritmética temporal via `Math.multiplyExact`/`Math.addExact`.
- **Posse física em `awaitTermination` (`StreamingServer`):** se algum worker continuar vivo após o prazo (`alive > 0 && cell.inFlight`), o handle ativo não é fechado concorrentemente e preserva `CLEANUP_UNCERTAIN`.
- **Limite temporal em `prepare()` (`StreamingServer` + `OttApplication`):** `prepare(...)` recebe `LongSupplier remainingNanos` (ligado ao `console.left()` em `OttApplication` após `console.stopped`), fazendo `join(remMs)` limitado quando cancelado.
- **Scope único global (`OttApplication`):** `Console.shutdownBudget()` fornece um único `ShutdownBudget` ancorado em `stopAt`, partilhado por `StreamingServer` e `OverlayNode`; `RunningService.close()` evita `node.close()` bloqueante quando o orçamento já esgotou.
- **Estado dos testes de routing:** numa execução anterior, `RouteTraceTest.t14`, `t15`, `t16` falharam porque os fixtures de `CliGraph` criavam `media.base` sem vídeos. A adenda de 16:15 autorizou os fixtures `.Mjpeg` mínimos; esses três testes não foram repetidos nesta retoma e o resultado atual permanece por confirmar.
- `mvn -f Projeto/pom.xml test -Dtest='!OttApplicationTest'`:
  - **1540 testes executados, 1537 passaram, 3 falharam** (`RouteTraceTest.t14`, `t15`, `t16`) numa execução anterior à adenda de fixtures de 16:15. Os processos `SERVER` falhavam na qualificação prévia porque `media.base` estava vazio; o resultado desses testes após a adenda ainda não foi verificado.
- `mvn -f Projeto/pom.xml test -Dtest=OttApplicationTest`:
  - **49 testes executados, 42 passaram, 7 falharam** por `process deadline` (5s) em `LiveChild.exit()` sobre subprocessos `BOOTSTRAP`/`RELAY` no ambiente JDK 25 (`25.0.5.1`).

## Retoma — validação focalizada de Implement (2026-11-28T19:04:54Z)

- Implementação existente reexaminada; não foi necessário alterar código nesta retoma.
- Comando: `mvn -f Projeto/pom.xml test -Dtest=StreamingServerTest`.
- Ambiente: OpenJDK `25.0.5.1`, compilação Maven com `--release 21`.
- Resultado observado: **7 testes executados, 0 falhas, 0 erros, 0 ignorados; BUILD SUCCESS**.
- Âmbito desta prova: testes focados de `StreamingServer`; não substitui V-020-C
  nem demonstra por si só todos os critérios de aceitação da etapa.
- A adenda fixture-only de `RouteTraceTest.java` foi inspecionada: os únicos
  tokens Java acrescentados são o helper JPEG e a criação dos ficheiros de
  fixture; os métodos/assertions de routing não foram alterados nesta retoma.
- Mantidos `current_stage=implement`, `stages_completed=[model, design, adr]` e
  `completed=null`. Parar neste checkpoint para aprovação humana de Implement;
  não promover o estágio nem avançar para Test sem aprovação explícita.

## Test — resultado em JDK 21 (2026-11-28T19:21:16Z)

O checkpoint acima foi posteriormente aprovado pelo utilizador, que autorizou
avançar para Test. A seleção `RouteTraceTest.t14`–`t16` passou 5/5 e a suíte
pertinente passou 143/143 em OpenJDK 21.0.13.1 / `--release 21`. A validação
funcional não exigiu correções de produção. A cobertura medida ficou abaixo do
critério genérico de 80%; o estado e a evidência estão em
[`ddd-03-test-report.md`](ddd-03-test-report.md). O bolt permanece em
`current_stage=test`, bloqueado nesse gate, sem fecho da história.
