# Waheed — Implementação e revisões

Data de distribuição: 27-10-2026. Este documento atribui responsabilidade futura; não altera estados nem atesta trabalho executado. As TASK-001–004/PHASE-01 já estão DONE e não são redistribuídas retroativamente. Os estados vivos pertencem aos ficheiros TASK e a [15 — Progresso](../15-PROJECT-PROGRESS.md).

## 1. Papel e ponto de partida

Transporte TCP/UDP, vizinhança, monitorização e robustez da comunicação. Também implementa recuperação de árvores, o primeiro cenário CORE e o empacotamento.

Implementações principais: **8 tarefas**. Revisões principais: **10 tarefas**. A contagem não mede esforço; ajustar atribuições explicitamente nos documentos se a equipa redistribuir trabalho.

A primeira implementação disponível é TASK-005. Antes de começar, acordar o contrato de transporte com Fillmore e os cenários de ciclo de vida com Denji; só depois marcar TASK-005 IN PROGRESS.

Não avançar para a próxima tarefa própria só por ser o menor número da sua lista. Exigir todas as dependências integradas e o fecho da fase anterior. Se não há tarefa pronta, preparar casos/revisões/documentação da fase atual; não escrever código de tarefas dependentes ou futuras para contornar o plano.

## 2. Leitura obrigatória e regras partilhadas

Ler [coordenação e matriz completa](00-COORDINATION.md), [guia da IA](../AI-IMPLEMENTATION-GUIDE.md), [progresso](../15-PROJECT-PROGRESS.md), [roadmap](../08-IMPLEMENTATION-ROADMAP.md), [arquitetura](../04-ARCHITECTURE.md), [modelo](../06-DATA-MODEL.md), [contratos](../07-API-AND-INTERFACES.md), [ADR-013 e decisões](../12-RISKS-AND-DECISIONS.md) e [Definition of Done](../14-DEFINITION-OF-DONE.md). Depois ler a tarefa atual na íntegra, os requisitos relacionados e a sua fase.

A evidência anterior da fase 1 é [131 testes aprovados](../../experiments/bootstrap-discovery-correction.md); não prova rede/CORE/vídeo nem substitui a execução necessária na sua tarefa. Atribuições não autorizam enviar mensagens externas ou submeter artefactos automaticamente.

## 3. Lista completa de implementações atribuídas

| Ordem própria | Tarefa | Fase | Dependências a integrar antes | Revisor principal |
|---:|---|---|---|---|
| 1 | [TASK-005](../tasks/PHASE-02/TASK-005.md) — Estabelecer transporte TCP full-duplex com recursos limitados | [PHASE-02](../phases/PHASE-02-OVERLAY.md) | [TASK-004](../tasks/PHASE-01/TASK-004.md) (base concluída) | [Fillmore](FILLMORE.md) |
| 2 | [TASK-007](../tasks/PHASE-02/TASK-007.md) — Gerir handshake, unicidade e reconexão de vizinhos | [PHASE-02](../phases/PHASE-02-OVERLAY.md) | [TASK-005](../tasks/PHASE-02/TASK-005.md) (Waheed), [TASK-006](../tasks/PHASE-02/TASK-006.md) (Fillmore) | [Denji](DENJI.md) |
| 3 | [TASK-010](../tasks/PHASE-03/TASK-010.md) — Medir RTT e publicar falhas de vizinhança | [PHASE-03](../phases/PHASE-03-ROUTING.md) | [TASK-007](../tasks/PHASE-02/TASK-007.md) (Waheed), [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), [TASK-009](../tasks/PHASE-02/TASK-009.md) (Denji) | [Fillmore](FILLMORE.md) |
| 4 | [TASK-016](../tasks/PHASE-04/TASK-016.md) — Recuperar árvores após falha de upstream | [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md) | [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), [TASK-014](../tasks/PHASE-04/TASK-014.md) (Fillmore), [TASK-015](../tasks/PHASE-04/TASK-015.md) (Fillmore) | [Fillmore](FILLMORE.md) |
| 5 | [TASK-019](../tasks/PHASE-05/TASK-019.md) — Definir codec UDP e fragmentação de frames | [PHASE-05](../phases/PHASE-05-STREAMING.md) | [TASK-002](../tasks/PHASE-01/TASK-002.md) (base concluída), [TASK-004](../tasks/PHASE-01/TASK-004.md) (base concluída), [TASK-011](../tasks/PHASE-03/TASK-011.md) (Fillmore), [TASK-018](../tasks/PHASE-04/TASK-018.md) (Denji) | [Denji](DENJI.md) |
| 6 | [TASK-021](../tasks/PHASE-05/TASK-021.md) — Reencaminhar e replicar datagramas pela árvore ativa | [PHASE-05](../phases/PHASE-05-STREAMING.md) | [TASK-014](../tasks/PHASE-04/TASK-014.md) (Fillmore), [TASK-015](../tasks/PHASE-04/TASK-015.md) (Fillmore), [TASK-016](../tasks/PHASE-04/TASK-016.md) (Waheed), [TASK-017](../tasks/PHASE-04/TASK-017.md) (Fillmore), [TASK-019](../tasks/PHASE-05/TASK-019.md) (Waheed) | [Fillmore](FILLMORE.md) |
| 7 | [TASK-025](../tasks/PHASE-06/TASK-025.md) — Preparar CORE e comprovar replicação no cenário de quatro nós | [PHASE-06](../phases/PHASE-06-CORE-VALIDATION.md) | [TASK-024](../tasks/PHASE-05/TASK-024.md) (Denji) | [Denji](DENJI.md) |
| 8 | [TASK-030](../tasks/PHASE-07/TASK-030.md) — Empacotar código e verificar reprodução da entrega | [PHASE-07](../phases/PHASE-07-DELIVERY.md) | [TASK-029](../tasks/PHASE-07/TASK-029.md) (Fillmore) | [Fillmore](FILLMORE.md) |

A ordem acima é uma fila pessoal; entre duas tarefas próprias podem existir tarefas de outros membros que é obrigatório aguardar. O revisor verifica também os critérios do ficheiro TASK e não apenas os pontos resumidos aqui.

## 4. O que entregar e quando pedir revisão

### TASK-005 — Estabelecer transporte TCP full-duplex com recursos limitados

- **Especificação completa:** [TASK-005](../tasks/PHASE-02/TASK-005.md); fase [PHASE-02](../phases/PHASE-02-OVERLAY.md).
- **Iniciar depois de:** [TASK-004](../tasks/PHASE-01/TASK-004.md) (base concluída), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Transportar mensagens enquadradas entre endpoints sem bloquear o controlo.
- **Âmbito indicado na tarefa:** Existentes: transport/Transport.java e Connection.java; planeado: transport/TransportTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Acordar com Fillmore o contrato de accept/connect/send, eventos e propriedade de flush/close. Implementar um único escritor por conexão, fila limitada e encerramento de I/O pendente; manter o listener operacional após falha de um peer.
- **Validação obrigatória:** V-005: dois endpoints loopback com portas efémeras trocam mensagens simultâneas intactas; parar um gera evento único; reconexão e saturação não deixam threads/sockets abertos.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-006](../tasks/PHASE-02/TASK-006.md), [TASK-007](../tasks/PHASE-02/TASK-007.md).
- **Pontos que o revisor deve comprovar:**
  - Troca simultânea nos dois sentidos mantém frames íntegros, incluindo leituras parciais.
  - Overflow, EOF e shutdown libertam recursos; uma conexão com falha não mata o listener.

### TASK-007 — Gerir handshake, unicidade e reconexão de vizinhos

- **Especificação completa:** [TASK-007](../tasks/PHASE-02/TASK-007.md); fase [PHASE-02](../phases/PHASE-02-OVERLAY.md).
- **Iniciar depois de:** [TASK-005](../tasks/PHASE-02/TASK-005.md) (Waheed), [TASK-006](../tasks/PHASE-02/TASK-006.md) (Fillmore), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Manter uma única ligação lógica ativa por par permitido.
- **Âmbito indicado na tarefa:** Existentes: overlay/Neighbor.java, NeighborManager.java, NodeRole.java e overlay/NeighborManagerTest.java; planeados: eventos imutáveis de vizinhança. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Separar identidade lógica, instanceId e connectionId. A ordenação dos IDs determina quem inicia a ligação. A lista inicial é suficiente para reconectar sem bootstrap; validar HELLO antes de aceitar o endpoint UDP.
- **Validação obrigatória:** V-007: três nós iniciados em ordens diferentes convergem para o grafo esperado sem duplicados; novo instanceId após reinício não é apagado por callback antigo; vizinho desconhecido é rejeitado; após descoberta, desligar bootstrap e comprovar reconexão direta sem novas consultas.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Denji](DENJI.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Denji](DENJI.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-008](../tasks/PHASE-02/TASK-008.md), [TASK-010](../tasks/PHASE-03/TASK-010.md), [TASK-012](../tasks/PHASE-03/TASK-012.md).
- **Pontos que o revisor deve comprovar:**
  - Uma ligação confirmada por par, instância correta e rejeição de eventos antigos.
  - Após descoberta e paragem do bootstrap, vizinhos já conhecidos reconectam diretamente sem novas consultas.

### TASK-010 — Medir RTT e publicar falhas de vizinhança

- **Especificação completa:** [TASK-010](../tasks/PHASE-03/TASK-010.md); fase [PHASE-03](../phases/PHASE-03-ROUTING.md).
- **Iniciar depois de:** [TASK-007](../tasks/PHASE-02/TASK-007.md) (Waheed), [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), [TASK-009](../tasks/PHASE-02/TASK-009.md) (Denji), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Produzir uma métrica medida e detetar peers sem resposta.
- **Âmbito indicado na tarefa:** Existentes: monitoring/LinkMetrics.java e NetworkMonitor.java; planeados: monitoring/NetworkMonitorTest.java e abstração TimeSource. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Usar relógio monotónico, correlação de sondas e EWMA. Não converter RTT em largura de banda ou latência unidirecional de vídeo. Reiniciar métricas ao mudar a instância e emitir falha sem confundir callback antigo com conexão nova.
- **Validação obrigatória:** V-010: relógio falso confirma EWMA, reset após nova instância, perda de três sondas e ausência de falso positivo por um timeout; EOF invalida imediatamente.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-011](../tasks/PHASE-03/TASK-011.md), [TASK-012](../tasks/PHASE-03/TASK-012.md), [TASK-015](../tasks/PHASE-04/TASK-015.md), [TASK-017](../tasks/PHASE-04/TASK-017.md).
- **Pontos que o revisor deve comprovar:**
  - PONG não correlacionado ou de instância antiga não atualiza a métrica.
  - Relógio/scheduler controlados comprovam EWMA, timeouts e publicação de falha.

### TASK-016 — Recuperar árvores após falha de upstream

- **Especificação completa:** [TASK-016](../tasks/PHASE-04/TASK-016.md); fase [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md).
- **Iniciar depois de:** [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), [TASK-014](../tasks/PHASE-04/TASK-014.md) (Fillmore), [TASK-015](../tasks/PHASE-04/TASK-015.md) (Fillmore), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Reativar procura num caminho alternativo válido após falha.
- **Âmbito indicado na tarefa:** Existentes: routing/RoutingService.java, RoutingTable.java; planeado: routing/FailoverTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Coordenar alterações a RoutingService com Fillmore, que revê esta tarefa. Preservar intenção de consumo, selecionar alternativa sem ciclo e só trocar upstream após ACK; sem caminho, declarar UNAVAILABLE.
- **Validação obrigatória:** V-016: cortar upstream num losango leva a caminho alternativo sem ciclo; sem alternativa termina UNAVAILABLE; regresso permite recuperar; streams da outra origem continuam.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-017](../tasks/PHASE-04/TASK-017.md), [TASK-018](../tasks/PHASE-04/TASK-018.md), [TASK-021](../tasks/PHASE-05/TASK-021.md).
- **Pontos que o revisor deve comprovar:**
  - Falha num losango seleciona alternativa válida e preserva outras streams.
  - Partição produz UNAVAILABLE e retorno recupera sem reiniciar clientes.

### TASK-019 — Definir codec UDP e fragmentação de frames

- **Especificação completa:** [TASK-019](../tasks/PHASE-05/TASK-019.md); fase [PHASE-05](../phases/PHASE-05-STREAMING.md).
- **Iniciar depois de:** [TASK-002](../tasks/PHASE-01/TASK-002.md) (base concluída), [TASK-004](../tasks/PHASE-01/TASK-004.md) (base concluída), [TASK-011](../tasks/PHASE-03/TASK-011.md) (Fillmore), [TASK-018](../tasks/PHASE-04/TASK-018.md) (Denji), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Converter um frame em datagramas limitados e validar cada fragmento recebido.
- **Âmbito indicado na tarefa:** Existente: streaming/StreamPacket.java; planeados: streaming/StreamPacketCodec.java e StreamPacketCodecTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Consumir as 16 fixtures UDP independentes, sem usar o encoder como oráculo. Preservar o cabeçalho de 64 bytes e datagrama máximo de 1200 bytes. Acordar o contrato do pacote com Fillmore e Denji antes de os consumidores começarem.
- **Validação obrigatória:** V-019: fixtures de bytes confirmam offsets 0–64; recombinação manual dos payloads recupera exatamente o frame; nenhum datagrama excede 1200 bytes; inputs inválidos são rejeitados.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Denji](DENJI.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Denji](DENJI.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-020](../tasks/PHASE-05/TASK-020.md), [TASK-021](../tasks/PHASE-05/TASK-021.md), [TASK-022](../tasks/PHASE-05/TASK-022.md).
- **Pontos que o revisor deve comprovar:**
  - Offsets/unsigned/limites coincidem com fixtures; payloads recombinados dão os bytes originais.
  - Nenhum datagrama excede 1200 bytes e inputs malformados são rejeitados antes de alocações excessivas.

### TASK-021 — Reencaminhar e replicar datagramas pela árvore ativa

- **Especificação completa:** [TASK-021](../tasks/PHASE-05/TASK-021.md); fase [PHASE-05](../phases/PHASE-05-STREAMING.md).
- **Iniciar depois de:** [TASK-014](../tasks/PHASE-04/TASK-014.md) (Fillmore), [TASK-015](../tasks/PHASE-04/TASK-015.md) (Fillmore), [TASK-016](../tasks/PHASE-04/TASK-016.md) (Waheed), [TASK-017](../tasks/PHASE-04/TASK-017.md) (Fillmore), [TASK-019](../tasks/PHASE-05/TASK-019.md) (Waheed), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Enviar uma cópia por saída ativa sem bloquear o controlo.
- **Âmbito indicado na tarefa:** Existente: streaming/StreamForwarder.java e StreamForwarderTest.java; planeado: transport/UdpTransport.java e testes de endpoints UDP. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Replicar uma cópia por vizinho downstream ativo, não por cliente final. Validar upstream/época/stream, limitar filas e preservar progresso do controlo. O relay não reconstrói nem descodifica JPEG.
- **Validação obrigatória:** V-021: fonte→relay→dois sinks recebe uma cópia por fragmento no tronco e duas nas saídas; origem não autorizada/epoch velha não segue; saturar uma saída não bloqueia controlo nem outras streams.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-022](../tasks/PHASE-05/TASK-022.md), [TASK-024](../tasks/PHASE-05/TASK-024.md).
- **Pontos que o revisor deve comprovar:**
  - Uma cópia no tronco e uma por saída ativa, com deduplicação e autorização do upstream.
  - Saturação de uma saída não bloqueia o controlo nem streams independentes.

### TASK-025 — Preparar CORE e comprovar replicação no cenário de quatro nós

- **Especificação completa:** [TASK-025](../tasks/PHASE-06/TASK-025.md); fase [PHASE-06](../phases/PHASE-06-CORE-VALIDATION.md).
- **Iniciar depois de:** [TASK-024](../tasks/PHASE-05/TASK-024.md) (Denji), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Validar replicação em quatro processos overlay numa bancada CORE reproduzível.
- **Âmbito indicado na tarefa:** Existentes: CORE/container/Dockerfile, docker-compose.yml e CORE/topologies/cenario1.xml; planeados: perfis .properties específicos deste cenário em CORE/volume/ott/ e evidências em experiments/scenario-1/. Não há novo XML obrigatório. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Reutilizar cenario1.xml, mapear processos/IPs e comprovar cópia única no tronco e replicação nas saídas. Não recriar o XML por defeito nem confundir reconvergência IP com comportamento da aplicação.
- **Validação obrigatória:** V-025: no estado estável comparar chaves (stream,session,seq,index) por ligação lógica; S→R tem uma cópia, R→C1 e R→C2 uma cada; depois do último stop não há datagramas de vídeo após o prazo de poda.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Denji](DENJI.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Denji](DENJI.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-026](../tasks/PHASE-06/TASK-026.md), [TASK-028](../tasks/PHASE-06/TASK-028.md).
- **Pontos que o revisor deve comprovar:**
  - IPs/processos correspondem ao XML existente e à sessão observada.
  - Capturas comprovam replicação e ausência de vídeo após última procura, com prazos documentados.

### TASK-030 — Empacotar código e verificar reprodução da entrega

- **Especificação completa:** [TASK-030](../tasks/PHASE-07/TASK-030.md); fase [PHASE-07](../phases/PHASE-07-DELIVERY.md).
- **Iniciar depois de:** [TASK-029](../tasks/PHASE-07/TASK-029.md) (Fillmore), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Preparar artefactos com nomes corretos que outro elemento consegue utilizar.
- **Âmbito indicado na tarefa:** Existentes: README.md e Projeto/README.md; planeados: delivery/ESR-TP2-PLx-Gy-Rel.pdf e ...-Codigo.zip com x/y reais. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Usar turno/grupo reais e verificar recursos redistribuíveis. Reproduzir build, testes e demonstração a partir de extração limpa; excluir caches/artefactos antigos e fixar hashes da versão entregue.
- **Validação obrigatória:** V-030: arquivo abre sem paths absolutos/escape; extração limpa passa instruções e testes; PDF abre e continua até 12 páginas; nomes e hashes correspondem ao manifesto.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-031](../tasks/PHASE-07/TASK-031.md).
- **Pontos que o revisor deve comprovar:**
  - Extração limpa reproduz instruções/testes/demonstração com ficheiros e hashes corretos.
  - Turno/grupo são reais e o pacote não inclui caches, segredos ou media sem condições resolvidas.

## 5. Lista completa das revisões que lhe cabem

Iniciar a revisão de interface antes do código consumidor e a revisão final quando o autor entregar o diff, os testes e a evidência. **Concluir antes do merge/DONE e do início dos consumidores abaixo.** Rever a versão final após correções; não aprovar apenas uma versão anterior.

| Tarefa a rever | Autor | Finalizar revisão antes de desbloquear |
|---|---|---|
| [TASK-006](../tasks/PHASE-02/TASK-006.md) — Fornecer localização inicial dos vizinhos configurados | [Fillmore](FILLMORE.md) | [TASK-007](../tasks/PHASE-02/TASK-007.md), [TASK-008](../tasks/PHASE-02/TASK-008.md) |
| [TASK-008](../tasks/PHASE-02/TASK-008.md) — Compor o processo overlay e encerrar de forma previsível | [Denji](DENJI.md) | [TASK-009](../tasks/PHASE-02/TASK-009.md), [TASK-010](../tasks/PHASE-03/TASK-010.md), [TASK-013](../tasks/PHASE-03/TASK-013.md), [TASK-024](../tasks/PHASE-05/TASK-024.md) |
| [TASK-011](../tasks/PHASE-03/TASK-011.md) — Modelar candidatos, custos e snapshots por stream | [Fillmore](FILLMORE.md) | [TASK-012](../tasks/PHASE-03/TASK-012.md), [TASK-014](../tasks/PHASE-04/TASK-014.md), [TASK-019](../tasks/PHASE-05/TASK-019.md) |
| [TASK-014](../tasks/PHASE-04/TASK-014.md) — Ativar árvores com ACK e procura agregada | [Fillmore](FILLMORE.md) | [TASK-015](../tasks/PHASE-04/TASK-015.md), [TASK-016](../tasks/PHASE-04/TASK-016.md), [TASK-021](../tasks/PHASE-05/TASK-021.md) |
| [TASK-017](../tasks/PHASE-04/TASK-017.md) — Adaptar rota por RTT com histerese e troca confirmada | [Fillmore](FILLMORE.md) | [TASK-018](../tasks/PHASE-04/TASK-018.md), [TASK-021](../tasks/PHASE-05/TASK-021.md) |
| [TASK-018](../tasks/PHASE-04/TASK-018.md) — Preparar o segundo marco com árvores e adaptação | [Denji](DENJI.md) | [TASK-019](../tasks/PHASE-05/TASK-019.md), [TASK-024](../tasks/PHASE-05/TASK-024.md), [TASK-031](../tasks/PHASE-07/TASK-031.md) |
| [TASK-022](../tasks/PHASE-05/TASK-022.md) — Reconstituir frames com buffers e prazo limitados | [Denji](DENJI.md) | [TASK-023](../tasks/PHASE-05/TASK-023.md), [TASK-024](../tasks/PHASE-05/TASK-024.md) |
| [TASK-024](../tasks/PHASE-05/TASK-024.md) — Integrar duas origens e quatro streams ponta a ponta | [Denji](DENJI.md) | [TASK-025](../tasks/PHASE-06/TASK-025.md) |
| [TASK-026](../tasks/PHASE-06/TASK-026.md) — Demonstrar alternativas e adaptação no cenário de seis nós | [Fillmore](FILLMORE.md) | [TASK-027](../tasks/PHASE-06/TASK-027.md), [TASK-028](../tasks/PHASE-06/TASK-028.md) |
| [TASK-031](../tasks/PHASE-07/TASK-031.md) — Preparar defesa e checklist de submissão humana | [Denji](DENJI.md) | fecho da fase/projeto, segundo a Definition of Done |

Para cada revisão, ler a especificação e critérios, analisar o diff e reproduzir as validações relevantes; usar também os pontos específicos na secção de implementação do documento do autor. Pedir correções concretas, sem alterar diretamente o trabalho do autor ou aprovar resultados não observados. Registar aceitação e evidência no handoff do ficheiro TASK; o autor corrige e volta a pedir revisão.

## 6. Passagem de trabalho e integração

Aplicar o protocolo de [coordenação](00-COORDINATION.md): uma branch por tarefa, autor responsável pelos próprios testes, revisor distinto e integração antes de disponibilizar dependências. Mudanças de routing/transport/app partilhados exigem coordenação com o responsável da tarefa em curso. A lista de revisões não autoriza implementar simultaneamente uma segunda tarefa no mesmo checkout.

No final de cada sessão registar no ficheiro TASK: revisão/branch de base, alterações, testes/comandos/resultados, critérios pendentes, contrato alterado, responsável pelo próximo passo e estado da revisão. Na integração atualizar apenas as linhas afetadas em 15 e a evidência pertinente em 13; preservar handoffs de outras pessoas.

Se os testes passam mas a revisão/integração está pendente, manter IN PROGRESS com a situação explícita. A espera normal por uma dependência mantém uma tarefa futura TODO. Usar BLOCKED para um impedimento real e explicar a condição de resolução. Não marcar requisito transversal DONE porque a sua parcela foi concluída.

## 7. Prompt para uma sessão de implementação

Substituir TASK-XXX pelo ID próprio já desbloqueado; este modelo não inicia tarefas automaticamente.

```text
Sou Waheed. Segue docs/team/WAHEED.md e docs/team/00-COORDINATION.md.
Executa exclusivamente TASK-XXX, lendo o seu ficheiro em docs/tasks/ e todos os contratos referenciados.
Antes de editar, verifica o estado do repositório, as dependências integradas e o gate da fase.
Se a tarefa não estiver desbloqueada, indica a dependência em falta sem a contornar.
Preserva alterações existentes e implementa apenas o âmbito da tarefa, incluindo os seus testes.
Regista comandos/resultados reais e prepara o diff/handoff para o revisor atribuído.
Mantém IN PROGRESS enquanto revisão ou integração estiverem pendentes.
Não avances automaticamente para outras tarefas nem submetas artefactos externamente.
```
