# Modelo de dados e invariantes

Não existe persistência transacional. Ficheiros locais definem configuração e conteúdos; o estado operacional é reconstruído a cada arranque. Não carregar rotas ou subscrições de uma execução anterior. Relógios de expiração usam tempo monotónico local; timestamps civis servem apenas logs.

## Identidades e entidades

| Entidade | Campos e tipos propostos | Validação / relações |
|---|---|---|
| NodeConfiguration | sourceFile Path, nodeId string ASCII 1–32, role NodeRole, bindHost, advertisedHost, controlPort/dataPort int, contacto bootstrap opcional, paths opcionais, topologia opcional e lista de streams | Modelo imutável implementado em TASK-003. Portas 1–65535; dataPort=0 apenas no BOOTSTRAP sem UDP. Verifica sintaxe de endpoint; alcance é futuro. instanceId só é gerado na execução, TASK-008 |
| Neighbor | nodeId, instanceId, endpoint TCP/UDP, connectionId UUID, state, lastSeen monotónico | No máximo uma conexão confirmada por nodeId; eventos antigos da connectionId são ignorados |
| NodeRegistry | mapa nodeId → endpoints/papel/leaseUntil e grafo permitido | O grafo é simétrico, sem self-link, sem endpoint duplicado por namespace; membership expirável não apaga a configuração |
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

protocol.ControlMessage contém o envelope e payloads imutáveis associados aos 17 tipos MessageType, com DTOs Endpoint e StreamInfo de rede. ErrorCode fixa os códigos 1–12 e ProtocolException classifica falhas de formato. Os DTOs de rede não substituem os serviços de vizinhança, routing ou catálogo dinâmico. O codec verifica tipos/domínios/limites; correspondência com ligação, autorização, caminhos e leases efetivos são responsabilidade dos handlers futuros. [API/contrato](07-API-AND-INTERFACES.md), [validação V-004](../experiments/task-004-codec-validation.md).
