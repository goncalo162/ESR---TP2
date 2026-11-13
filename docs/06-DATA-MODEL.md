# Modelo de dados e invariantes

> Arquitetura normativa: [add.md](../add.md). Requisitos consolidados: [specs.md](../specs.md). Este documento mantém detalhe técnico/proveniência e deve ser interpretado pela precedência do ADD; não é uma autoridade arquitetural concorrente.

Não existe persistência transacional. Ficheiros locais definem configuração e conteúdos; o estado operacional é reconstruído a cada arranque. Não carregar rotas ou subscrições de uma execução anterior. Relógios de expiração usam tempo monotónico local; timestamps civis servem apenas logs.

## Identidades e entidades

| Entidade | Campos e tipos propostos | Validação / relações |
|---|---|---|
| NodeConfiguration | sourceFile Path, nodeId string ASCII 1–32, role NodeRole, bindHost, advertisedHost, controlPort/dataPort int, contacto bootstrap opcional, paths opcionais, topologia opcional e lista de streams | Modelo imutável implementado em TASK-003. Portas 1–65535; dataPort=0 apenas no BOOTSTRAP sem UDP. Verifica sintaxe de endpoint; alcance é futuro. instanceId só é gerado na execução, TASK-008 |
| Neighbor | nodeId, instanceId, endpoint TCP/UDP, connectionId UUID, state, lastSeen monotónico | No máximo uma conexão confirmada por nodeId; eventos antigos da connectionId são ignorados |
| TopologyConfiguration no bootstrap | mapa imutável de nodeId → endpoints/papel/vizinhos configurados | Já implementado na configuração. É a única fonte da resposta de descoberta; sem NodeRegistry, disponibilidade, instâncias ativas ou leaseUntil |
| StreamInfo | streamId UUID, serverId nodeId, title string até 128 pontos de código Unicode, codec MJPEG, fps int, serverEpoch UUID | Stream UUID único em toda a configuração; cada UUID pertence a uma origem; pelo menos dois vídeos por origem na configuração final |
| VideoDefinition | StreamInfo + mediaPath, loop boolean, maxFrameBytes | Path é exclusivamente configuração local de SERVER; nunca recebido do cliente; validar acessibilidade e conteúdo real |
| StreamKey | streamId UUID | A origem é validada por StreamInfo; não usar título como chave |
| RouteCandidate | StreamKey, serverId, epoch, upstreamNode, path list[nodeId], announceSeq long, costMicros long, hopCount, expiresAt | Um candidato por vizinho/stream; custo positivo e sem overflow; path começa na fonte e termina no nó local; epoch identifica arranque da fonte |
| Route | StreamKey, selectedCandidate, activeUpstream, pendingUpstream, downstream map, localConsumers set, state, routeRevision | Origem do PDF = upstream; Destinos = downstream; uma Route por fluxo e vários candidatos; fonte tem upstream nulo e custo zero |
| Subscription | streamId, epoch, childNodeId, subscriptionId UUID, revision long, state, expiresAt, requestId UUID | Revisão crescente por subscrição; ACK e cancelamento correlacionados; no máximo um downstream efetivo por vizinho/stream |
| LinkMetrics | neighborId, rttEwmaMicros long, sampleCount, outstandingProbe, missedCount, updatedAt | RTT calculado no mesmo relógio, EWMA alpha=0,2; não admitir negativos, NaN ou resultado sem amostra |
| StreamPacket | streamId UUID, sessionId UUID, frameSeq long, mediaTimeUs long, frameLength int, fragmentIndex/count int, payload byte[] | sessionId corresponde ao serverEpoch atual; bytes e limites em [07](07-API-AND-INTERFACES.md) |
| FrameAssembly | (streamId,sessionId,frameSeq), firstSeen, deadline, expectedLength/count, bitmap, fragments | Máximo oito frames em montagem por stream; verificar coerência em todos os fragmentos; remover no prazo |
| Counters | pacotes/bytes por fluxo e direção, descartes por razão, mudanças de rota, frames recebidos/mostrados | Incrementos monotónicos na execução; snapshots com RUN-ID; não confundir perdas IP com frames não reconstruídos |

## Modelos de configuração disponíveis

Desde TASK-003, config contém NodeConfiguration, TopologyConfiguration (mapa de nós e sets de vizinhos imutáveis), StreamConfiguration (UUID, serverId, título, mediaFile, FPS e loop) e problemas agregados ConfigurationProblem/ConfigurationException. São metadados de arranque e não substituem tabelas de vizinhos/rotas, catálogo dinâmico ou StreamInfo com epoch, que pertencem às fases seguintes. A configuração não gera UUID de instância, não resolve DNS nem abre sockets. [API e limites](07-API-AND-INTERFACES.md), [evidência V-003](../experiments/task-003-configuration-validation.md).

## Estados

~~~mermaid
stateDiagram-v2
  [*] --> UNAVAILABLE
  UNAVAILABLE --> INACTIVE: anúncio válido
  INACTIVE --> ACTIVATING: primeira procura
  ACTIVATING --> ACTIVE: ACK correlacionado
  ACTIVATING --> UNAVAILABLE: sem candidato / timeout definitivo
  ACTIVATING --> INACTIVE: procura cancelada
  ACTIVE --> SWITCHING: candidato alternativo
  SWITCHING --> ACTIVE: ACK e troca atómica
  SWITCHING --> UNAVAILABLE: upstream falha e não há alternativa
  SWITCHING --> INACTIVE: última procura cancelada
  ACTIVE --> INACTIVE: última procura removida
  INACTIVE --> UNAVAILABLE: expiração da rota
  ACTIVE --> UNAVAILABLE: sem caminho válido
  UNAVAILABLE --> ACTIVATING: anúncio válido e intenção de consumo pendente
~~~

Uma falha de ativação com outro candidato pode repetir ACTIVATING; uma tentativa de troca falhada mantém ACTIVE no upstream antigo se este continuar válido. Os nomes anteriores STALE dos placeholders não constituem um contrato: se necessário, usar STALE apenas como razão de invalidade de candidato, não como estado ambíguo da árvore.

Vizinhos: DISCONNECTED → CONNECTING → HANDSHAKING → ACTIVE; falha/timeout → DISCONNECTED e retry; shutdown → CLOSED. Subscrições: PENDING → ACTIVE por ACK; ACTIVE → REMOVED por cancelamento/lease; erro ou timeout remove PENDING.

## Invariantes verificáveis

1. Cada StreamKey tem destinos, sequências de anúncio/frame, subscrições e buffers próprios. O serverEpoch identifica o arranque da origem e é partilhado pelas streams dessa mesma origem: S1/A e S1/B usam o mesmo serverEpoch, mas mantêm estado independente. S2 tem a sua própria época. Chaves de deduplicação incluem sempre streamId e epoch, nunca apenas epoch.
2. Em estado estável cada nó não fonte tem um único upstream ativo por stream. Nenhum downstream é o upstream nem o próprio nó.
3. Cada caminho anunciado contém IDs únicos; prefixo até à fonte é validado na ativação. Um caminho de anúncio não autoriza dados antes do ACK.
4. A procura agregada é a união de consumidores locais e downstream válidos. Uma segunda ativação do mesmo child não duplica saída ou referência.
5. Só se envia para downstream confirmado e com lease válido; o upstream antigo é desativado após trocar. Cancelamentos velhos não removem revisões novas.
6. Reencaminhar um datagrama no máximo uma vez por vizinho ativo no estado estável. O recetor elimina duplicados numa janela limitada.
7. Todos os fragmentos de um frame têm stream, session, sequência, tamanho e contagem iguais. A soma de payloads deve ser exatamente frameLength.
8. Antes de alocar, verificar comprimento máximo; deadline nunca é prolongada por duplicados. Pacotes antigos e caches expiram.
9. Uma nova época da fonte invalida os frames e candidatos antigos dessa fonte. Uma época anterior retirada não pode reverter o catálogo por mensagem atrasada; manter conjunto limitado de épocas retiradas até expirar o horizonte de anúncios.
10. Um reinício de relay invalida as subscrições daquela instância; os filhos renegociam por uma conexão nova.

## Persistência e evidência

Guardar apenas configuração, vídeos e artefactos de experiência. Não criar base de dados ou ficheiros de checkpoint de rotas. Manifestos experimentais devem incluir hash de configuração, versão do JAR, conteúdo/ficheiros usados, seed quando houver jitter, nó/host, condições e tempos de falha. Os esquemas e limites são decisões técnicas: mudanças têm de atualizar [07](07-API-AND-INTERFACES.md), ADR e testes.

## Modelos de controlo disponíveis após TASK-004

protocol.ControlMessage contém o envelope e payloads imutáveis associados aos 16 tipos ativos MessageType (IDs 1, 2 e 4–17; 3 retirado). NeighborList contém apenas a lista de Endpoints configurados, sem lease; em REGISTER_OK, instanceId de todos os vizinhos é zero. O envelope conserva a identidade da instância emissora para correlação, sem constituir registo de disponibilidade. ErrorCode fixa os códigos 1–12 e ProtocolException classifica falhas de formato. Os DTOs de rede não substituem os serviços de vizinhança, routing ou catálogo dinâmico. O codec verifica tipos/domínios/limites; correspondência com ligação, autorização, caminhos e leases de subscrição efetivos são responsabilidade dos handlers futuros. [API/contrato](07-API-AND-INTERFACES.md), [validação original V-004](../experiments/task-004-codec-validation.md), [correção do bootstrap](../experiments/bootstrap-discovery-correction.md).

## Transporte disponível desde TASK-005

Connection possui UUID por ligação, endpoint remoto, fila limitada e razão terminal (primeira razão vence). UUID de conexão não autentica nodeId. Eventos ordenados open → mensagens → closed, sendo closed único; slots de admissão incluem dials e permanecem ocupados até os dois workers I/O terminarem. Shutdown fecha também tentativas pendentes. A associação a Neighbor pertence à TASK-007. [API](07-API-AND-INTERFACES.md#api-de-transporte-implementada-na-task-005).


## IMP-008 — Design aprovado e checkpoint ADR

Registo 2026-11-13T16:41:04Z. Q-C-26 aprovada integralmente: capacidades e retenção no
[ADD6.1](../add.md#61-entidades-e-invariantes) e
[Design](../memory-bank/bolts/021-rotas/ddd-02-technical-design.md).
[Revisão ADR](../memory-bank/bolts/021-rotas/adr-review.md) preparada, para aprovação.
LinkMetrics.costMicros() fornece o custo opcional em µs já arredondado pelo monitor;
projetar nodeId/instanceId/connectionId para SessionKey e consumir o valor diretamente.
SUSPECT com amostra mantém o custo; ausência não é zero. O domínio não recalcula RTT.
Tabela cresce por admissão, sem máximos pré-alocados, cacheCapacity reutilizada ou
evição de histórico protegido. Snapshot de seleção não é forwarding confirmado.
A ponte de eventos de métricas para routing ainda não existe em OverlayNode;
composição futura requer checkpoint/âmbito próprio. Só Model/Design concluídos;
Implement/Test, V-011, regressão atual, manual offline, revisão final e integração
pendentes. Nenhuma alteração Java, teste executado ou ADR nova neste checkpoint.


## IMP-008 — domínio implementado para revisão, sem composição de rede

Registo 2026-11-13T17:18:48Z. [Walkthrough](../memory-bank/bolts/021-rotas/implementation-walkthrough.md):
RouteMetric/RouteCandidate/RouteState/Route/RoutingTable disponíveis nos seis
caminhos do IMP-008, conforme Design e Q-C-26 aprovados. Snapshot só de seleção,
sem parent confirmado ou destinos. Todos os métodos da tabela exigem o owner
serial do chamador; nenhuma thread, socket ou timer criado pelo domínio.
35 testes novos e regressão incremental1211/0/0/0 com JDK21; execução restrita
anterior preservada. Test/clean verify/manual humano/revisão final/integração
pendentes. RoutingService/OverlayNode e bytes operacionais preservados.


## IMP-008 — Test técnico, manual humano pendente

Registo 2026-11-13T17:43:51Z. [Relatório](../memory-bank/bolts/021-rotas/ddd-03-test-report.md):
clean verify JDK21:1211/0/0/0; novo JAR e35 testes do domínio confirmados, sem
alteração de fonte nesta etapa. Manual Codex43checks passou contra esse JAR;
[roteiro offline humano](../experiments/construction/021-rotas/manual-instructions.md)
preparado, execução humana não recebida. D22 parcial/Test por aprovar, revisão
final e integração pendentes. Sem composição de rede ou autorização de dados nova.
