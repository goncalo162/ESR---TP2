---
unit: "003-rotas"
bolt: "006-rotas"
stage: "adr"
status: "complete"
---
# IMP-009 — Revisão ADR e conferência dos contratos reais

✅ Model e Design completos, incluindo Q-C-28 Opção1 e Q-C-29/30 integralmente
aprovadas pelo utilizador. ⏳ Esta revisão ADR aguarda aprovação, incluindo a
concretização Q-C-31 abaixo. Implement/Test não iniciados. D01–D32/V-012 por executar.
Autorização e entrada: [design-approval.json](../../../experiments/construction/006-rotas/adr-review/design-approval.json)
e [entry.json](../../../experiments/construction/006-rotas/adr-review/entry.json).

## 1. Método e âmbito efetivamente conferido

Base164ac0dc477c02bc7fde816c761d137ee18ea55d, ramo Denji. Conferência estática de
fontes agora autorizada na etapa ADR; não confundir com a leitura documental
anterior de Model/Design. Nenhuma compilação, teste Java, rede ou edição Java.

1 - `routing/RoutingService.java`: existente,0 bytes; API do Design é nova, não
    assinatura já implementada.
2 - `streaming/StreamInfo.java`: existente,0 bytes; record de domínio proposto,
    diferente de ControlMessage.StreamInfo já existente no wire.
3 - `streaming/StreamCatalog.java`: existente,0 bytes; projeção nova.
4 - `routing/RoutingServiceTest.java`: existente,0 bytes; D01–D32 por construir.
5 - `overlay/OverlayNode.java`: existente,10648 bytes; owner, factory do monitor,
    callback central e lifecycle reais permitem a composição aprovada Q-C-28.
6 - `overlay/OverlayNodeRoutingTest.java`: ausente; teste novo dentro do âmbito.

Dependências e testes antigos foram lidos apenas para conferir interfaces,
sem os promover a caminhos autorizados para alteração. RoutingTable, Route,
RouteCandidate, RouteMetric, NetworkMonitor, LinkMetrics, NeighborManager,
Neighbor, configuração, protocolo e transporte permanecem intocados.

## 2. Assinaturas reais e ligação às APIs propostas

### 2.1. OverlayNode e owner

[OverlayNode](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java)
tem construtor público `(NodeConfiguration, UUID, NeighborManager.Observer)` e
overload package-private com `NetworkMonitor.Factory`. Ambos permanecem.
Já expõe start():CompletionStage<Void>, monitoringSnapshot(), snapshot(),
diagnostics(), discoveryStatus(), candidates(), recoveryStatus(), failure(),
requestShutdown(int,Duration), awaitTermination(Duration) e close(). RoutingSnapshot
e overload com RoutingService.Factory são adições futuras, não APIs presentes.

start cria uma única instância de NeighborManager.SerialOwner, empresta-a ao
manager e ao monitor, agenda monitor.start antes do bind do manager e trata o
resultado fora do lock do nó. onNeighborEvent já preserva observer.onEvent num
finally. O observer interno do monitor trata LocalFailure fatal, mas não entrega
Updated/Invalidated a routing: essa é precisamente a adição autorizada.

`NeighborManager.SerialOwner` real declara:

```java
int capacity();
boolean isOwnerThread();
boolean offer(Runnable event);
Registration onStop(Runnable cleanup);
void fail(Throwable cause);
void requestClose();
boolean awaitTermination(Duration timeout);
void close();
```

Registration tem remove(). O owner existente aceita até16 hooks; manager,
monitor e routing ocupam três, sem exigir alteração do cap. Queue e stop são
limitados; hooks de cleanup executam no owner ao terminar, mesmo após descartar
eventos pendentes. O router precisa do seu hook, não depende da entrega de um
último NeighborDown. await não pode bloquear esse mesmo owner.

**Pormenor relevante Q-C-31:** offer(false) não representa fila temporariamente
ocupada com garantia de retoma: fila cheia chama fail e inicia fecho do owner.
Uma callback que guardou resultado deve permitir recolha pelo cleanup/hook e
terminação física; não prometer que o wake seguinte executará depois dessa recusa.

### 2.2. Vizinhança e controlo

[NeighborManager](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java)
aceita owner emprestado no construtor público; exige capacity igual a
config.runtime().overlay().eventsCapacity(). As assinaturas são:

```java
CompletionStage<Connection.SendResult> send(Neighbor.Session expected, ControlMessage message);
CompletionStage<Boolean> disconnect(Neighbor.Session expected);
CompletionStage<Void> start();
void requestClose();
CompletionStage<NeighborManager.ShutdownReport> requestShutdown(int reason, Duration remainingBudget);
boolean awaitTermination(Duration timeout);
```

Session real é `(String nodeId, UUID instanceId, UUID connectionId,
ControlMessage.Endpoint endpoint)`, imutável e validada. Up/Down/Received incluem
a Session completa; Received traz a mensagem original. Diagnostic e RepairStarted
também existem e continuam a chegar ao observer anterior uma vez, sem alteração.

send compara a Session corrente, valida identidade local e admite mensagens
aplicacionais. Tipos9/10 são permitidos;18/19 têm outra admissão própria e não
devem ser usados por routing. O comando é postado ao owner mesmo quando o
chamador já está nele: nada de join/get à espera do resultado nesse owner.
Manager reserva no máximo E+1 comandos partilhados; B do router não constitui
reserva exclusiva dessa capacidade nem da queue do owner. Sob sobrecarga, os
erros operacionais existentes permanecem observáveis, sem garantia de convergência.

SendResult real tem ACCEPTED/CLOSED/OVERFLOW/INVALID_MESSAGE. ACCEPTED é apenas
admissão. [Connection](../../../Projeto/src/main/java/pt/uminho/esr/ott/transport/Connection.java)
partilha FIFO/writer entre send e sendAndFlush; este IMP não precisa de nova porta
de flush, nem muda a escrita NODE_LEAVE já existente.

**Pormenor relevante Q-C-31:** Connection.send fecha essa ligação quando a queue
de saída está cheia e devolve OVERFLOW. Router diagnostica e acompanha o Down da
Session exata; não mantém uma tentativa reutilizável nessa conexão como se fosse
BACKPRESSURED não terminal. Outras sessões saudáveis não são fechadas por routing.
Recusa local de K/P/quota é distinta e continua não terminal, como Q-C-29 aprovada.

### 2.3. Monitor, unidades e configuração

[NetworkMonitor](../../../Projeto/src/main/java/pt/uminho/esr/ott/monitoring/NetworkMonitor.java)
e sua Factory públicas recebem `(NodeConfiguration, UUID,
NeighborManager.SerialOwner, NetworkMonitor.ControlPort, NetworkMonitor.Observer)`.
onEvent(NeighborManager.Event) e start exigem owner; snapshot é imutável;
requestClose devolve CompletionStage<Void> de cleanup lógico e awaitTermination
confere scheduler e reservas físicas. Observer.onEvent recebe Updated(LinkMetrics),
Invalidated(Session,Reason), LocalFailure(Optional<Session>,Reason,boolean).

O overload avançado do monitor é package-private em monitoring e **não** será
usado por RoutingServiceTest. Este injeta LinkMetrics/NetworkMonitor.Event no
seu próprio router e usa as suas portas/relógio/scheduler públicos propostos.
OverlayNodeRoutingTest está em overlay e usa o novo overload do nó e a factory
pública proposta do router. Sem reflection de estado privado como API de produção.

[LinkMetrics](../../../Projeto/src/main/java/pt/uminho/esr/ott/monitoring/LinkMetrics.java)
expõe costMicros():OptionalLong já coerente com ceil(ewmaMicros). Consumir esse
campo diretamente; não calcular segunda EWMA, converter ms ou arredondar de novo.
SUSPECT com amostra mantém custo; UNMEASURED deixa-o ausente.

[RuntimeConfiguration](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/RuntimeConfiguration.java)
expõe routing().announceInterval()/candidateTtl()/cacheCapacity()/cacheTtl()/
floodMaxPerSecond(); overlay().eventsCapacity(); control().maxConnections()/
shutdownTimeout(). Durations são ms inteiros positivos≤Integer.MAX_VALUE,
candidateTtl>announceInterval e cacheTtl≥candidateTtl. Conversão exata para ns
cabe em long. Nenhuma propriedade nova nem hard cap configurável.

[StreamConfiguration](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/StreamConfiguration.java)
contém streamId/serverId/title/mediaFile/fps/loop, **sem campo codec**. Converter
ofertas configuradas usando codec1/MJPEG do contrato existente, validar os campos
antes de iniciar helper/sockets, não abrir mediaFile. Os construtores de config
não substituem essa validação dos novos metadados de domínio. Non-SERVER não
ganha ofertas locais. Normal funcionamento continua com descoberta obrigatória.

### 2.4. RoutingTable e metadados

[RoutingTable](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingTable.java)
tem as assinaturas propostas no Design/IMP-008: bindStream, replaceSession,
removeSession, setLinkCost, confirmEpoch, offerLocal/removeLocalOffer, announce,
withdraw, expire e snapshot(UUID). Não tem snapshot global nem catálogo de títulos.
Router guarda o índice limitado de bindings/referências e projeta snapshots no
owner, sem ler tabela em threads externas ou pedir método novo à fundação.

Advertisement real recebe UUID streamId, String serverId, UUID serverEpoch,
SessionKey, long announceSeq, long advertisedCostMicros, List<String> advertisedPath.
Receber path com≤31 IDs deixa espaço ao append local≤32; origem/último sender,
unicidade e ausência local são conferidos. Custo0 só em path da fonte. Copies
defensivas e overflow verificado permanecem.

Status/Reason/changedStreams de Outcome distinguem APPLIED/IGNORED/REJECTED.
IGNORED não equivale a admissão de candidato. Para Q-C-30, instalar referência
somente com oferta/candidato efetivamente admitido; uma reserva bindStream não
basta. Root não guarda title/codec/FPS: comparar a referência no router antes
de qualquer mutação. Rejeição do conflito não consome seq nem renova prazo.

confirmEpoch é CAS explícito com Optional<UUID> esperado e invalida todos os
streams da fonte; respeita oito épocas retiradas/cacheTtl. UUID não é ordenado.
Somente nova seq renova; melhoria de mesmo epoch/seq compara custo e comprimento,
preserva began; retirada impede ressuscitar a geração enquanto protegida. Isto
confirma a necessidade de W+geração nova para alternativa pior **ou path diferente
com custo/saltos iguais**, e o floor persistente no ledger do router.

RouteCandidate fornece path incluindo local, announceSeq, metric opcional,
generationStartedAtNanos/candidateTtlNanos. Route separa selectedCandidate/
selectedMetric/knownUpstream de activeUpstream/pendingUpstream/downstreams.
Estes últimos são vazios por validação; estado INACTIVE/UNAVAILABLE não é vídeo ativo.

### 2.5. Wire

[ControlMessage](../../../Projeto/src/main/java/pt/uminho/esr/ott/protocol/ControlMessage.java)
tem construtor abreviado que usa version2. StreamInfo wire declara exatamente
UUID streamId,String serverId,String title,int codec,int fps,UUID serverEpoch;
Announcement contém info,seq,costMicros,path; Withdrawal contém UUID,epoch,seq.
Usar nome qualificado para distinguir StreamInfo wire de streaming.StreamInfo.

[MessageCodec](../../../Projeto/src/main/java/pt/uminho/esr/ott/protocol/MessageCodec.java)
valida versão2, framing37..65536, UTF-8/título, codec1 e fps1..60.
[MessageType](../../../Projeto/src/main/java/pt/uminho/esr/ott/protocol/MessageType.java)
tem19 tipos explícitos1,2,4..20, sem3. Codec não valida referência de metadados por
época: essa é política do router. Framing/identidade inválidos conservam política
terminal atual; conflito de conteúdo contextual de ANN válido é rejeição local.
Nenhum recurso/fixture/byte TCP ou UDP alterado por este incremento documental.

## 3. Conformidade das decisões aceites

1 - **ADR-004:** candidato por vizinho, RTT+saltos+ID; admite alternativa antes
    de dedup, reenvia melhoria que o recetor realmente aceita. Q-C-29 conserva
    geração de fonte e ordem W/ANN, sem exceção silenciosa a watermark.
2 - **ADR-016:** usa valores centrais reais, K/P/B/F derivados; S/N/históricos
    são Q-C-26, não routing.cache.capacity. Não enfraquece validações antes de sockets.
3 - **ADR-017:** um owner existente; callbacks só depositam resultados físicos e
    sinalizam trabalho limitado; mudanças de rede no owner. Q-C-31 explicita a
    consequência real da fila cheia em vez de inventar backpressure recuperável.
4 - **ADR-018:** dados voláteis, UUID/origem fixos, época por fonte e seq por
    stream. Q-C-30 mantém referência por época mesmo sem candidatos, com limiteS,
    sem persistência ou proteção eterna contra épocas antigas.
5 - **ADR-020:** isolamento por Session/stream, prazo global e falhas tipadas;
    preserva fecho de ligação por OUTPUT_OVERFLOW e de owner por saturação.
    Não alegar terminação física pela conclusão lógica de future.
6 - **ADR-024:** seis caminhos aprovados, documentação/índices alinhados, um IMP;
    revisão não autoriza antecipar CLI/trace/ativação/vídeo ou alterar fundações.
7 - **Transversais012/021/026 e034/035:** codec comum/contexto separados,
    confiança de laboratório explícita, nenhuma dependência/coverage/CI nova;
    TCP v2 e sessões de reparação são consumidos sem reabrir contratos do extra.

Não foi identificada ADR aceite inviável ou necessidade de substituição/nova
ADR nesta análise. Q-C-29/30 são concretizações aprovadas das decisões existentes.
Q-C-31 propõe resolver a ambiguidade de redação contra APIs existentes; permanece
para revisão antes de Implement, sem adaptar as APIs pela nossa conta.

## 4. Alterações internas justificadas e preservação de testes

OverlayNode terá adições localizadas nos construtores/start/onNeighborEvent/
callback de monitor/stopImmediately/requestShutdown/awaitTermination e snapshot.
Inserir composição e cleanup; preservar APIs e finally original. Não é mera
concatenação no fim do ficheiro, mas está no âmbito explicitamente aprovado.
Qualquer remoção futura tem de aparecer/ser justificada no walkthrough; aqui zero.

Preservar start/rollback mesmo se factory falha a meio; pedir stop a todos os
componentes sem short-circuit. requestShutdown mantém resultado/tipo/razões e
NODE_LEAVE; router fecha gate antes de manager e joins usam prazo restante fora
do owner/locks. Manager emprestado não fecha o owner por si; nó é o responsável.

OverlayNodeMonitoringTest já verifica constructor/factory anteriores, owner
partilhado, finally com erro, ERROR real, NODE_LEAVE, rollback e shutdown. Este
IMP acrescenta OverlayNodeRoutingTest, sem editar/enfraquecer aqueles testes.
Dados literais de grafos e auxiliares ficam nos dois ficheiros de teste autorizados.

D01–D32 cobrem as obrigações; D07/D08 incluem conflito após expiração total e
aceitação posterior coerente. D15/D20/D21 cobrem reservas/recibos tardios;
D25–D29 cobrem composição/API/falhas/recurso. Q-C-31 pede que D29 espere fecho
do owner em oferta recusada, e D14/D15 distingam recusa local de OVERFLOW terminal.
Não criar D33 nem contar teste planeado como executado.

## 5. Checkpoint

1 - ✅ Model e Design/Q-C-29/30 aprovados, ADD/índices registados.
2 - ⏳ Revisão ADR e concretização Q-C-31 para aprovação; sem Implement autorizado.
3 - [ ] V-012/D01–D32, regressão atual, manual humano antes de aprovar Test.
4 - [ ] Revisão final atribuída a Denji e integração; nenhum gate externo adicional.

Sem Maven/testes, sockets, alterações Java, commits/push ou outro IMP.
Se Implement revelar outra incompatibilidade real, parar a parte afetada e
aplicar ADD11; esta análise não constitui aprovação antecipada de código.


## Aprovação posterior

Registo 2026-11-14T12:58:05Z. Utilizador aprovou esta revisão e Q-C-31; Design6/7 e
D14/D15/D29 clarificados. Apenas Implement autorizado, sem Test ou fecho.
