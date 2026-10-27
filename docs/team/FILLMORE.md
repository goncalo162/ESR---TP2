# Fillmore — Implementação e revisões

Data de distribuição: 27-10-2026. Este documento atribui responsabilidade futura; não altera estados nem atesta trabalho executado. As TASK-001–004/PHASE-01 já estão DONE e não são redistribuídas retroativamente. Os estados vivos pertencem aos ficheiros TASK e a [15 — Progresso](../15-PROJECT-PROGRESS.md).

> Este é o documento de trabalho do utilizador desta sessão, identificado como Fillmore em 27-10-2026.

## 1. Papel e ponto de partida

Descoberta inicial, modelos/anúncios de rotas, ativação/poda/adaptação e fontes de vídeo. Também implementa o segundo cenário CORE e coordena a escrita do artigo.

Implementações principais: **9 tarefas**. Revisões principais: **10 tarefas**. A contagem não mede esforço; ajustar atribuições explicitamente nos documentos se a equipa redistribuir trabalho.

Para ti, Fillmore, TASK-006 é a próxima implementação indicada no [progresso atual](../15-PROJECT-PROGRESS.md). Usar o transporte e os contratos da TASK-005, com [evidência V-005](../../experiments/task-005-transport-validation.md). Consultar a tarefa e confirmar a base integrada antes de iniciar; bootstrap exclusivamente para descoberta inicial, sem registo vivo ou leases. Atribuição não inicia automaticamente a tarefa.

Não avançar para a próxima tarefa própria só por ser o menor número da sua lista. Exigir todas as dependências integradas e o fecho da fase anterior. Se não há tarefa pronta, preparar casos/revisões/documentação da fase atual; não escrever código de tarefas dependentes ou futuras para contornar o plano.

## 2. Leitura obrigatória e regras partilhadas

Ler [coordenação e matriz completa](00-COORDINATION.md), [guia da IA](../AI-IMPLEMENTATION-GUIDE.md), [progresso](../15-PROJECT-PROGRESS.md), [roadmap](../08-IMPLEMENTATION-ROADMAP.md), [arquitetura](../04-ARCHITECTURE.md), [modelo](../06-DATA-MODEL.md), [contratos](../07-API-AND-INTERFACES.md), [ADR-013 e decisões](../12-RISKS-AND-DECISIONS.md) e [Definition of Done](../14-DEFINITION-OF-DONE.md). Depois ler a tarefa atual na íntegra, os requisitos relacionados e a sua fase.

A evidência anterior da fase 1 é [131 testes aprovados](../../experiments/bootstrap-discovery-correction.md); não prova rede/CORE/vídeo nem substitui a execução necessária na sua tarefa. Atribuições não autorizam enviar mensagens externas ou submeter artefactos automaticamente.

## 3. Lista completa de implementações atribuídas

| Ordem própria | Tarefa | Fase | Dependências a integrar antes | Revisor principal |
|---:|---|---|---|---|
| 1 | [TASK-006](../tasks/PHASE-02/TASK-006.md) — Fornecer localização inicial dos vizinhos configurados | [PHASE-02](../phases/PHASE-02-OVERLAY.md) | [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-005](../tasks/PHASE-02/TASK-005.md) (Waheed) | [Waheed](WAHEED.md) |
| 2 | [TASK-011](../tasks/PHASE-03/TASK-011.md) — Modelar candidatos, custos e snapshots por stream | [PHASE-03](../phases/PHASE-03-ROUTING.md) | [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed) | [Waheed](WAHEED.md) |
| 3 | [TASK-012](../tasks/PHASE-03/TASK-012.md) — Difundir anúncios e construir catálogo e rotas | [PHASE-03](../phases/PHASE-03-ROUTING.md) | [TASK-007](../tasks/PHASE-02/TASK-007.md) (Waheed), [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), [TASK-011](../tasks/PHASE-03/TASK-011.md) (Fillmore) | [Denji](DENJI.md) |
| 4 | [TASK-014](../tasks/PHASE-04/TASK-014.md) — Ativar árvores com ACK e procura agregada | [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md) | [TASK-011](../tasks/PHASE-03/TASK-011.md) (Fillmore), [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), [TASK-013](../tasks/PHASE-03/TASK-013.md) (Denji) | [Waheed](WAHEED.md) |
| 5 | [TASK-015](../tasks/PHASE-04/TASK-015.md) — Podar ramos, renovar leases e remover procura abandonada | [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md) | [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), [TASK-014](../tasks/PHASE-04/TASK-014.md) (Fillmore) | [Denji](DENJI.md) |
| 6 | [TASK-017](../tasks/PHASE-04/TASK-017.md) — Adaptar rota por RTT com histerese e troca confirmada | [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md) | [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), [TASK-016](../tasks/PHASE-04/TASK-016.md) (Waheed) | [Waheed](WAHEED.md) |
| 7 | [TASK-020](../tasks/PHASE-05/TASK-020.md) — Ler fontes MJPEG e controlar ritmo de publicação | [PHASE-05](../phases/PHASE-05-STREAMING.md) | [TASK-002](../tasks/PHASE-01/TASK-002.md) (base concluída), [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-019](../tasks/PHASE-05/TASK-019.md) (Waheed) | [Denji](DENJI.md) |
| 8 | [TASK-026](../tasks/PHASE-06/TASK-026.md) — Demonstrar alternativas e adaptação no cenário de seis nós | [PHASE-06](../phases/PHASE-06-CORE-VALIDATION.md) | [TASK-025](../tasks/PHASE-06/TASK-025.md) (Waheed) | [Waheed](WAHEED.md) |
| 9 | [TASK-029](../tasks/PHASE-07/TASK-029.md) — Escrever artigo com protocolos e avaliação crítica | [PHASE-07](../phases/PHASE-07-DELIVERY.md) | [TASK-028](../tasks/PHASE-06/TASK-028.md) (Denji) | [Denji](DENJI.md) |

A ordem acima é uma fila pessoal; entre duas tarefas próprias podem existir tarefas de outros membros que é obrigatório aguardar. O revisor verifica também os critérios do ficheiro TASK e não apenas os pontos resumidos aqui.

## 4. O que entregar e quando pedir revisão

### TASK-006 — Fornecer localização inicial dos vizinhos configurados

- **Especificação completa:** [TASK-006](../tasks/PHASE-02/TASK-006.md); fase [PHASE-02](../phases/PHASE-02-OVERLAY.md).
- **Iniciar depois de:** [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-005](../tasks/PHASE-02/TASK-005.md) (Waheed), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Devolver a um nó conhecido os endpoints dos seus vizinhos permitidos.
- **Âmbito indicado na tarefa:** Existentes: bootstrap/BootstrapServer.java contém apenas documentação; bootstrap/BootstrapServerTest.java é placeholder; config/TopologyConfiguration.java e resources/config/topology.properties já estão implementados/preenchidos. Implementar a consulta diretamente sobre TopologyConfiguration. O placeholder NodeRegistry.java foi removido por não existir registo operacional a implementar. Confirmar estes estados no início da tarefa. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Bootstrap é exclusivamente consulta inicial da configuração (ADR-013). Não criar registo vivo, lease, renovação ou monitorização. Devolver também vizinhos offline, UUID de instância zero e fechar a conexão após a resposta completa.
- **Validação obrigatória:** V-006: consultas repetidas/concorrentes recebem só os vizinhos configurados, incluindo offline, com instanceId zero e sem lease; duas instâncias do mesmo ID recebem a mesma lista; operações não permitidas são rejeitadas sem alterar a configuração; a conexão fecha após a resposta completa e não existe renovação ou estado vivo no bootstrap.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-007](../tasks/PHASE-02/TASK-007.md), [TASK-008](../tasks/PHASE-02/TASK-008.md).
- **Pontos que o revisor deve comprovar:**
  - Resposta depende só do grafo configurado e inclui offline, sem estado vivo, lease ou renovação.
  - Pedidos repetidos/concorrentes não alteram configuração; saída completa precede o fecho da conexão.

### TASK-011 — Modelar candidatos, custos e snapshots por stream

- **Especificação completa:** [TASK-011](../tasks/PHASE-03/TASK-011.md); fase [PHASE-03](../phases/PHASE-03-ROUTING.md).
- **Iniciar depois de:** [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Manter seleção e alternativas isoladas por fluxo, com custos determinísticos.
- **Âmbito indicado na tarefa:** Existentes: routing/Route.java, RouteState.java, RouteMetric.java, RoutingTable.java; planeados: RouteCandidate.java e routing/RoutingTableTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Modelar candidatos isolados por stream e ordenar por custo RTT, saltos e nodeId. Guardar path/época/validade; publicar snapshots imutáveis. A existência de rota não ativa vídeo.
- **Validação obrigatória:** V-011: custos e desempate calculados com fixtures de resultados conhecidos; quatro streams não interferem; alternativa é preservada e snapshot não pode ser alterado por consumidor.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-012](../tasks/PHASE-03/TASK-012.md), [TASK-014](../tasks/PHASE-04/TASK-014.md), [TASK-019](../tasks/PHASE-05/TASK-019.md).
- **Pontos que o revisor deve comprovar:**
  - Empates, custo positivo, overflow, expiração e alternativas produzem seleção determinística.
  - Uma stream não altera rotas/snapshots de outra; snapshots não expõem mutação.

### TASK-012 — Difundir anúncios e construir catálogo e rotas

- **Especificação completa:** [TASK-012](../tasks/PHASE-03/TASK-012.md); fase [PHASE-03](../phases/PHASE-03-ROUTING.md).
- **Iniciar depois de:** [TASK-007](../tasks/PHASE-02/TASK-007.md) (Waheed), [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), [TASK-011](../tasks/PHASE-03/TASK-011.md) (Fillmore), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Descobrir todas as streams e caminhos sem flooding infinito.
- **Âmbito indicado na tarefa:** Existentes: routing/RoutingService.java, streaming/StreamInfo.java, StreamCatalog.java e routing/RoutingServiceTest.java; planeados: fixtures de grafos com ciclos. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Implementar anúncios periódicos por stream, custo acumulado e prevenção de ciclos por path. Controlar repetições sem descartar uma melhoria válida que chega mais tarde; expirar candidatos e preservar alternativas.
- **Validação obrigatória:** V-012: grafo em losango e grafo cíclico convergem para custo esperado; segunda chegada melhor é aceite; quatro streams aparecem; retirar uma origem não remove streams da outra; flooding estabiliza.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Denji](DENJI.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Denji](DENJI.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-013](../tasks/PHASE-03/TASK-013.md), [TASK-014](../tasks/PHASE-04/TASK-014.md), [TASK-016](../tasks/PHASE-04/TASK-016.md), [TASK-023](../tasks/PHASE-05/TASK-023.md).
- **Pontos que o revisor deve comprovar:**
  - Um anúncio melhor tardio é considerado sem introduzir ciclos ou flooding ilimitado.
  - Retirada/expiração de um candidato não elimina alternativas válidas e o catálogo mantém isolamento.

### TASK-014 — Ativar árvores com ACK e procura agregada

- **Especificação completa:** [TASK-014](../tasks/PHASE-04/TASK-014.md); fase [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md).
- **Iniciar depois de:** [TASK-011](../tasks/PHASE-03/TASK-011.md) (Fillmore), [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), [TASK-013](../tasks/PHASE-03/TASK-013.md) (Denji), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Confirmar um único ramo a montante para toda a procura do mesmo fluxo num nó.
- **Âmbito indicado na tarefa:** Existentes: routing/RoutingService.java, Route.java; planeados: routing/Subscription.java e ActivationTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Agregar consumidores por conjuntos e ativar um único upstream por stream. Manter PENDING até ACK válido, validar prefixo/época e correlacionar respostas sem bloquear o leitor TCP.
- **Validação obrigatória:** V-014: dois clientes em tronco comum geram uma subscrição no tronco; ACK atrasado/cancelado não ativa; prefixo incorreto gera PATH_CHANGED; streams independentes mantêm estados separados.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-015](../tasks/PHASE-04/TASK-015.md), [TASK-016](../tasks/PHASE-04/TASK-016.md), [TASK-021](../tasks/PHASE-05/TASK-021.md).
- **Pontos que o revisor deve comprovar:**
  - Dois consumidores partilham um upstream; pedido repetido não duplica procura.
  - ACK/erro atrasado, timeout e prefixo alterado não criam uma árvore ativa inválida.

### TASK-015 — Podar ramos, renovar leases e remover procura abandonada

- **Especificação completa:** [TASK-015](../tasks/PHASE-04/TASK-015.md); fase [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md).
- **Iniciar depois de:** [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), [TASK-014](../tasks/PHASE-04/TASK-014.md) (Fillmore), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Retirar procura sem interromper consumidores restantes nem deixar ramos fantasma.
- **Âmbito indicado na tarefa:** Existentes: routing/RoutingService.java, overlay/NeighborManager.java; planeados: routing/SubscriptionLeaseTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Leases são de subscrição das streams, não de bootstrap. Remover apenas o consumidor correto, podar após a última procura e ignorar cancelamentos ou callbacks de instâncias antigas.
- **Validação obrigatória:** V-015: sair um de dois consumidores mantém tronco ativo; sair o último remove; relógio falso elimina lease órfão; cancelamento velho não remove ramo novo.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Denji](DENJI.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Denji](DENJI.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-016](../tasks/PHASE-04/TASK-016.md), [TASK-018](../tasks/PHASE-04/TASK-018.md), [TASK-021](../tasks/PHASE-05/TASK-021.md).
- **Pontos que o revisor deve comprovar:**
  - Saída de um consumidor preserva outro e a última saída poda o upstream.
  - Lease expirado, cancelamento velho e callback antigo não removem a subscrição nova.

### TASK-017 — Adaptar rota por RTT com histerese e troca confirmada

- **Especificação completa:** [TASK-017](../tasks/PHASE-04/TASK-017.md); fase [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md).
- **Iniciar depois de:** [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), [TASK-016](../tasks/PHASE-04/TASK-016.md) (Waheed), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Reagir a degradação sustentada sem oscilar entre caminhos.
- **Âmbito indicado na tarefa:** Existentes: monitoring/NetworkMonitor.java, routing/RouteMetric.java, RoutingService.java; planeado: routing/RouteSwitchTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Aplicar histerese a melhoria sustentada; falha física não espera esses limiares. Manter o upstream antigo numa troca planeada até confirmação do novo e evitar oscilação ou poda prematura.
- **Validação obrigatória:** V-017: séries temporais predeterminadas não oscilam junto do limiar; melhoria sustentada troca; falha troca sem esperar permanência; troca falhada mantém parent antigo válido.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-018](../tasks/PHASE-04/TASK-018.md), [TASK-021](../tasks/PHASE-05/TASK-021.md).
- **Pontos que o revisor deve comprovar:**
  - Oscilações junto ao limiar não provocam trocas repetidas.
  - Novo ramo só substitui o antigo após ACK; falha ignora permanência e troca planeada falhada mantém ramo válido.

### TASK-020 — Ler fontes MJPEG e controlar ritmo de publicação

- **Especificação completa:** [TASK-020](../tasks/PHASE-05/TASK-020.md); fase [PHASE-05](../phases/PHASE-05-STREAMING.md).
- **Iniciar depois de:** [TASK-002](../tasks/PHASE-01/TASK-002.md) (base concluída), [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-019](../tasks/PHASE-05/TASK-019.md) (Waheed), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Produzir frames válidos de vídeos distintos com pacing e procura controlada.
- **Âmbito indicado na tarefa:** Existentes: server/VideoSource.java e StreamingServer.java; planeados: server/VideoSourceTest.java e fixtures MJPEG pequenas autorizadas. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Preparar e validar quatro conteúdos reais, dois por origem; não basta mudar extensões ou UUID. Implementar leitura do perfil de cinco dígitos ASCII + JPEG e pacing sem procura nula produzir leitura/envio. Registar fontes, conversão e hashes.
- **Validação obrigatória:** V-020: fixture confirmada com dois frames produz bytes esperados; ficheiro truncado falha de forma localizada; relógio falso confirma pacing/ausência de envio sem procura; quatro conteúdos são identificáveis.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Denji](DENJI.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Denji](DENJI.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-024](../tasks/PHASE-05/TASK-024.md).
- **Pontos que o revisor deve comprovar:**
  - Quatro conteúdos são distintos e identificáveis, com proveniência e hashes.
  - Prefixo/JPEG truncados falham localmente; pacing, loop e ausência de procura têm resultados verificáveis.

### TASK-026 — Demonstrar alternativas e adaptação no cenário de seis nós

- **Especificação completa:** [TASK-026](../tasks/PHASE-06/TASK-026.md); fase [PHASE-06](../phases/PHASE-06-CORE-VALIDATION.md).
- **Iniciar depois de:** [TASK-025](../tasks/PHASE-06/TASK-025.md) (Waheed), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Comprovar escolha por RTT, recuperação e retorno de nós em rede emulada.
- **Âmbito indicado na tarefa:** Existente: CORE/topologies/cenario2.xml; planeados: perfis .properties para os seus processos e experiments/scenario-2/. Só adaptar o XML por necessidade concreta documentada, preservando o original. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Reutilizar cenario2.xml e separar falha de processo, conexão overlay e ligação física. Usar alterações controladas de atraso para demonstrar RTT/adaptação; identificar a rota IP e a overlay nas evidências.
- **Validação obrigatória:** V-026: três repetições por condição; medir fim do vídeo antes e primeiro frame após falha; snapshots e logs mostram parent alterado; jitter pequeno não gera oscilações; um consumidor sair não interrompe o outro.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-027](../tasks/PHASE-06/TASK-027.md), [TASK-028](../tasks/PHASE-06/TASK-028.md).
- **Pontos que o revisor deve comprovar:**
  - Falha overlay é distinguida de recuperação de encaminhamento IP.
  - Três repetições/condições, parent alterado e ausência de oscilações têm dados brutos.

### TASK-029 — Escrever artigo com protocolos e avaliação crítica

- **Especificação completa:** [TASK-029](../tasks/PHASE-07/TASK-029.md); fase [PHASE-07](../phases/PHASE-07-DELIVERY.md).
- **Iniciar depois de:** [TASK-028](../tasks/PHASE-06/TASK-028.md) (Denji), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Produzir relatório em formato de artigo com até 12 páginas e resultados verificáveis.
- **Âmbito indicado na tarefa:** Planeados: report/ com fontes e PDF; existentes: docs/07, docs/12 e evidências de experiments/. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Escrever o artigo com o comportamento efetivamente implementado, não promessas do plano. Solicitar secções/evidências aos três membros; contar todas as páginas até 12 e descrever bootstrap apenas como descoberta inicial.
- **Validação obrigatória:** V-029: revisão por checklist do enunciado; correspondência de figuras/tabelas com dados; contagem de páginas do PDF até 12; protocolo no artigo corresponde aos bytes e comportamento implementados.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Denji](DENJI.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Denji](DENJI.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-030](../tasks/PHASE-07/TASK-030.md).
- **Pontos que o revisor deve comprovar:**
  - PDF até 12 páginas incluindo referências e correspondência com implementação/evidências.
  - Protocolos, bootstrap, resultados e limitações não contradizem contrato atual.
- **Contributo obrigatório dos três:** fornecer dados/revisão da própria área; o responsável coordena e compila o resultado, não assume sozinho a implementação/avaliação dos restantes.

## 5. Lista completa das revisões que lhe cabem

Iniciar a revisão de interface antes do código consumidor e a revisão final quando o autor entregar o diff, os testes e a evidência. **Concluir antes do merge/DONE e do início dos consumidores abaixo.** Rever a versão final após correções; não aprovar apenas uma versão anterior.

| Tarefa a rever | Autor | Finalizar revisão antes de desbloquear |
|---|---|---|
| [TASK-005](../tasks/PHASE-02/TASK-005.md) — Estabelecer transporte TCP full-duplex com recursos limitados | [Waheed](WAHEED.md) | [TASK-006](../tasks/PHASE-02/TASK-006.md), [TASK-007](../tasks/PHASE-02/TASK-007.md) |
| [TASK-009](../tasks/PHASE-02/TASK-009.md) — Preparar evidência do primeiro marco intermédio | [Denji](DENJI.md) | [TASK-010](../tasks/PHASE-03/TASK-010.md), [TASK-018](../tasks/PHASE-04/TASK-018.md), [TASK-031](../tasks/PHASE-07/TASK-031.md) |
| [TASK-010](../tasks/PHASE-03/TASK-010.md) — Medir RTT e publicar falhas de vizinhança | [Waheed](WAHEED.md) | [TASK-011](../tasks/PHASE-03/TASK-011.md), [TASK-012](../tasks/PHASE-03/TASK-012.md), [TASK-015](../tasks/PHASE-04/TASK-015.md), [TASK-017](../tasks/PHASE-04/TASK-017.md) |
| [TASK-013](../tasks/PHASE-03/TASK-013.md) — Diagnosticar percursos pela tabela de rotas | [Denji](DENJI.md) | [TASK-014](../tasks/PHASE-04/TASK-014.md), [TASK-018](../tasks/PHASE-04/TASK-018.md) |
| [TASK-016](../tasks/PHASE-04/TASK-016.md) — Recuperar árvores após falha de upstream | [Waheed](WAHEED.md) | [TASK-017](../tasks/PHASE-04/TASK-017.md), [TASK-018](../tasks/PHASE-04/TASK-018.md), [TASK-021](../tasks/PHASE-05/TASK-021.md) |
| [TASK-021](../tasks/PHASE-05/TASK-021.md) — Reencaminhar e replicar datagramas pela árvore ativa | [Waheed](WAHEED.md) | [TASK-022](../tasks/PHASE-05/TASK-022.md), [TASK-024](../tasks/PHASE-05/TASK-024.md) |
| [TASK-023](../tasks/PHASE-05/TASK-023.md) — Apresentar catálogo e vídeo numa janela Swing | [Denji](DENJI.md) | [TASK-024](../tasks/PHASE-05/TASK-024.md) |
| [TASK-027](../tasks/PHASE-06/TASK-027.md) — Validar cenário complexo com duas origens e cinco clientes | [Denji](DENJI.md) | [TASK-028](../tasks/PHASE-06/TASK-028.md) |
| [TASK-028](../tasks/PHASE-06/TASK-028.md) — Recolher métricas e avaliar resultados e limites | [Denji](DENJI.md) | [TASK-029](../tasks/PHASE-07/TASK-029.md) |
| [TASK-030](../tasks/PHASE-07/TASK-030.md) — Empacotar código e verificar reprodução da entrega | [Waheed](WAHEED.md) | [TASK-031](../tasks/PHASE-07/TASK-031.md) |

Para cada revisão, ler a especificação e critérios, analisar o diff e reproduzir as validações relevantes; usar também os pontos específicos na secção de implementação do documento do autor. Pedir correções concretas, sem alterar diretamente o trabalho do autor ou aprovar resultados não observados. Registar aceitação e evidência no handoff do ficheiro TASK; o autor corrige e volta a pedir revisão.

## 6. Passagem de trabalho e integração

Aplicar o protocolo de [coordenação](00-COORDINATION.md): uma branch por tarefa, autor responsável pelos próprios testes, revisor distinto e integração antes de disponibilizar dependências. Mudanças de routing/transport/app partilhados exigem coordenação com o responsável da tarefa em curso. A lista de revisões não autoriza implementar simultaneamente uma segunda tarefa no mesmo checkout.

No final de cada sessão registar no ficheiro TASK: revisão/branch de base, alterações, testes/comandos/resultados, critérios pendentes, contrato alterado, responsável pelo próximo passo e estado da revisão. Na integração atualizar apenas as linhas afetadas em 15 e a evidência pertinente em 13; preservar handoffs de outras pessoas.

Se os testes passam mas a revisão/integração está pendente, manter IN PROGRESS com a situação explícita. A espera normal por uma dependência mantém uma tarefa futura TODO. Usar BLOCKED para um impedimento real e explicar a condição de resolução. Não marcar requisito transversal DONE porque a sua parcela foi concluída.

## 7. Prompt para uma sessão de implementação

Substituir TASK-XXX pelo ID próprio já desbloqueado; este modelo não inicia tarefas automaticamente.

```text
Sou Fillmore. Segue docs/team/FILLMORE.md e docs/team/00-COORDINATION.md.
Executa exclusivamente TASK-XXX, lendo o seu ficheiro em docs/tasks/ e todos os contratos referenciados.
Antes de editar, verifica o estado do repositório, as dependências integradas e o gate da fase.
Se a tarefa não estiver desbloqueada, indica a dependência em falta sem a contornar.
Preserva alterações existentes e implementa apenas o âmbito da tarefa, incluindo os seus testes.
Regista comandos/resultados reais e prepara o diff/handoff para o revisor atribuído.
Mantém IN PROGRESS enquanto revisão ou integração estiverem pendentes.
Não avances automaticamente para outras tarefas nem submetas artefactos externamente.
```
