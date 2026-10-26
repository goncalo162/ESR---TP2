# Estrutura do projeto

Os caminhos seguintes são relativos à raiz do repositório. **E** significa existente e observado; **P** significa planeado. E não significa implementado. A maior parte dos ficheiros Java/config/testes existentes está vazia; ver [01](01-PROJECT-OVERVIEW.md).

~~~text
ESR---TP2/
├── ESR-TP2-26-27.pdf                     E fonte principal
├── docs/                               documentação criada neste planeamento
│   ├── 00-README.md ... 16-PLAN-VALIDATION.md
│   ├── AI-IMPLEMENTATION-GUIDE.md
│   ├── phases/                         sete fases
│   └── tasks/PHASE-01 ... PHASE-07       31 tarefas
├── Projeto/                            E raiz Maven
│   ├── pom.xml                         E build Java 21; testes por configurar
│   ├── README.md                       E atualizar em TASK-030
│   ├── docs/                           E documentação histórica; não duplicar este plano
│   ├── src/main/java/pt/uminho/esr/ott/ E pacotes preparados
│   │   ├── app/                        E OttApplication
│   │   ├── bootstrap/                  E BootstrapServer, NodeRegistry
│   │   ├── config/                     E NodeConfiguration, ConfigurationLoader
│   │   ├── overlay/                    E OverlayNode, NodeRole, Neighbor, NeighborManager
│   │   ├── transport/                  E Transport, Connection
│   │   ├── protocol/                   E ControlMessage, MessageType, MessageCodec
│   │   ├── monitoring/                 E LinkMetrics, NetworkMonitor
│   │   ├── routing/                    E Route, RouteMetric, RouteState, RoutingTable, RoutingService
│   │   ├── streaming/                  E StreamInfo, StreamCatalog, StreamPacket, StreamForwarder
│   │   ├── server/                     E StreamingServer, VideoSource
│   │   └── client/                     E StreamingClient, VideoPlayer
│   ├── src/main/resources/config/      E properties vazios e topology.json vazio
│   │   ├── topology.properties         P substituir uso do JSON vazio
│   │   └── streams-s1.properties / streams-s2.properties P dois fluxos por origem
│   ├── src/test/java/.../ott/           E cinco placeholders; restantes testes P
│   ├── src/test/resources/             E JSON vazios; fixtures binárias/properties P
│   └── target/                         E artefactos gerados antigos; não editar
├── CORE/                               E bancada preparada
│   ├── container/                      E Dockerfile e pacotes
│   ├── docker-compose.yml              E volumes/serviços
│   ├── core-gui                        E launcher
│   ├── topologies/cenario1.xml ... 3.xml E underlay inicial
│   ├── topologies/ott-cenario1.xml ... 3.xml P cenários de validação com terminais
│   └── volume/ott/                     P material de execução partilhado
│       ├── ott-streaming-1.0-SNAPSHOT.jar
│       ├── config/                     cópias específicas da experiência
│       └── media/                      vídeos obtidos legalmente; não gerar neste plano
├── experiments/                        P evidências futuras, apenas após execução
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
- Atualizar ligações do README existente em TASK-030; nesta fase apenas docs/ é criada/modificada.
