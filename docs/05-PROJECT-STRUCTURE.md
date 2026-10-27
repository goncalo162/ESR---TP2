# Estrutura do projeto

Os caminhos seguintes são relativos à raiz do repositório. **E** significa existente e observado; **P** significa planeado. E não significa implementado. Configuração, codec e TCP estão implementados nas TASK-003–005; restantes componentes maioritariamente placeholders. [Auditoria histórica](01-PROJECT-OVERVIEW.md).

~~~text
ESR---TP2/
├── ESR-TP2-26-27.pdf                     E fonte principal
├── docs/                               documentação criada neste planeamento
│   ├── 00-README.md ... 16-PLAN-VALIDATION.md
│   ├── AI-IMPLEMENTATION-GUIDE.md
│   ├── phases/                         sete fases
│   └── tasks/PHASE-01 ... PHASE-07       31 tarefas
├── Projeto/                            E raiz Maven
│   ├── pom.xml                         E build Java 21; JUnit/Surefire configurados
│   ├── README.md                       E atualizar em TASK-030
│   ├── docs/                           E documentação histórica; não duplicar este plano
│   ├── src/main/java/pt/uminho/esr/ott/ E pacotes preparados
│   │   ├── app/                        E OttApplication
│   │   ├── bootstrap/                  E BootstrapServer apenas comentado; consulta da configuração em TASK-006, sem NodeRegistry
│   │   ├── config/                     E TASK-003: loader, NodeConfiguration, TopologyConfiguration, StreamConfiguration, erros agregados
│   │   ├── overlay/                    E OverlayNode, NodeRole, Neighbor, NeighborManager
│   │   ├── transport/                  E TASK-005: Transport, Connection (TCP, filas/eventos/shutdown)
│   │   ├── protocol/                   E TASK-004: ControlMessage, MessageType, MessageCodec, ErrorCode, ProtocolException
│   │   ├── monitoring/                 E LinkMetrics, NetworkMonitor
│   │   ├── routing/                    E Route, RouteMetric, RouteState, RoutingTable, RoutingService
│   │   ├── streaming/                  E StreamInfo, StreamCatalog, StreamPacket, StreamForwarder
│   │   ├── server/                     E StreamingServer, VideoSource
│   │   └── client/                     E StreamingClient, VideoPlayer
│   ├── src/main/resources/config/      E perfis .properties válidos; topology.json histórico vazio
│   │   ├── topology.properties         E TASK-003: grafo simétrico de desenvolvimento
│   │   └── streams-s1.properties / streams-s2.properties E TASK-003: dois fluxos por origem
│   ├── src/test/java/.../ott/           E ConfigurationLoaderTest, MessageCodecTest e TransportTest; restantes placeholders/P
│   ├── src/test/resources/             E fixtures de protocolo; JSON históricos vazios
│   │   └── protocol/                   E TASK-002: 45 pares .bin/.hex, manifesto e README
│   └── target/                         E artefactos de build; não usar como evidência isolada
├── CORE/                               E bancada preparada
│   ├── container/                      E Dockerfile e pacotes
│   ├── docker-compose.yml              E volumes/serviços
│   ├── core-gui                        E launcher
│   ├── topologies/cenario1.xml ... 3.xml E reutilizar; variantes só por necessidade documentada
│   └── volume/ott/                     P material de execução partilhado
│       ├── ott-streaming-1.0-SNAPSHOT.jar
│       ├── config/                     cópias específicas da experiência
│       └── media/                      vídeos obtidos legalmente; não gerar neste plano
├── experiments/                        E evidências V-001–005; experiências CORE ainda P
│   ├── milestone-1/ ... milestone-2/
│   └── scenario-1/ ... scenario-3/
│       └── RUN-ID/                     manifesto, logs, CSV e capturas selecionadas
├── report/                             P fontes do artigo e roteiro da defesa
└── delivery/                           P PDF e ZIP finais com turno/grupo reais
~~~

## Regras de organização

- O código continua em Projeto; não criar um segundo projeto Java na raiz.
- Novas classes pequenas podem ser adicionadas no pacote responsável: Clock/TimeSource, Subscription, RouteCandidate, StreamPacketCodec, FrameAssembler e UdpTransport são caminhos **planeados**. Não são bibliotecas ou componentes já disponíveis.
- Constantes e modelos de mensagens ficam em protocol; o transporte não interpreta regras de subscrição. Contratos entre módulos em [07](07-API-AND-INTERFACES.md).
- Fontes MJPEG em disco são endereçadas por configuração. Não incluir ficheiros grandes ou com distribuição não autorizada no ZIP; documentar obtenção e verificar que a entrega continua reprodutível.
- Os JSON existentes, vazios, não serão interpretados; TASK-003 cria propriedades válidas e TASK-025 documenta a opção. Remoção de placeholders só quando fizer parte de uma tarefa autorizada de implementação.
- Evidências devem identificar execução e configuração. Não versionar target, caches, segredos nem todas as capturas indiscriminadamente. TASK-030 define exclusões e conteúdo necessário da entrega.
- READMEs e documentação refletem TASK-005; rever entrega completa em TASK-030. Código implementado permanece em Projeto/src.
