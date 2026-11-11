---
intent: "001-ott-streaming"
phase: "inception"
status: "inception-complete"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-03T20:54:11Z"
completed: "2026-11-03T20:54:11Z"
requirements_basis: "existing-project-documentation"
review_status: "approved-by-user"
---

# Requisitos — concluir o serviço OTT existente

Fonte normativa de requisitos: [specs.md](../../../specs.md). Arquitetura: [add.md](../../../add.md). Os IDs abaixo são aliases históricos; as respostas 1A/2A/3B/4A foram incorporadas sem apagar os critérios anteriores.

Objetivo: concluir o projeto segundo os Markdown existentes, corrigir lacunas antes
do agrupamento e preparar bolts faseados para **Construction**, com validação.
O âmbito e o executor foram fornecidos pelo utilizador. Reutilizam-se os critérios
existentes, sem nova numeração ou alteração das obrigações. Não se inicia implementação.
Revisão da organização aprovada pelo utilizador em 03-11-2026, com instrução
explícita para fechar Inception no âmbito atual. Os extras serão definidos e
planeados numa retoma posterior de Inception, quando forem pedidos; não
impedem este fecho nem estão implicitamente implementados pelos bolts atuais.

Fonte normativa: [docs/02](../../../docs/02-REQUIREMENTS.md).
Owner abaixo significa responsabilidade primária; outros units contribuem pelas TASK.

## RF-001 — Executar nós como aplicações em máquinas IP

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `002-overlay`.
- Critério de aceitação: Iniciar manualmente servidor, relay e cliente em processos independentes; cada um escuta no endpoint configurado e reporta o seu papel.
- Tarefas: [TASK-003](../../../docs/tasks/PHASE-01/TASK-003.md), [TASK-008](../../../docs/tasks/PHASE-02/TASK-008.md).
- Validação: V-003, V-008.
- Fonte completa: [RF-001](../../../docs/02-REQUIREMENTS.md#rf-001).

## RF-002 — Entrada e saída dinâmica de nós

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `002-overlay`.
- Critério de aceitação: Adicionar um nó a uma overlay em execução, removê-lo com encerramento normal e abrupto, e observar atualização de vizinhos e rotas sem reinício dos restantes.
- Tarefas: [TASK-006](../../../docs/tasks/PHASE-02/TASK-006.md), [TASK-007](../../../docs/tasks/PHASE-02/TASK-007.md), [TASK-008](../../../docs/tasks/PHASE-02/TASK-008.md), [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md), [TASK-016](../../../docs/tasks/PHASE-04/TASK-016.md).
- Validação: V-006, V-007, V-008, V-015, V-016.
- Fonte completa: [RF-002](../../../docs/02-REQUIREMENTS.md#rf-002).

## RF-003 — Estabelecer e manter X vizinhos TCP ou UDP

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `002-overlay`.
- Critério de aceitação: Estabelecer uma ligação lógica bidirecional por vizinho permitido disponível; repetir tentativas para vizinhos indisponíveis e mostrar grau desejado/atual. X é configurável por nó e limitado pela topologia, não fixado em três.
- Tarefas: [TASK-005](../../../docs/tasks/PHASE-02/TASK-005.md), [TASK-006](../../../docs/tasks/PHASE-02/TASK-006.md), [TASK-007](../../../docs/tasks/PHASE-02/TASK-007.md), [TASK-010](../../../docs/tasks/PHASE-03/TASK-010.md).
- Validação: V-005, V-006, V-007, V-010.
- Fonte completa: [RF-003](../../../docs/02-REQUIREMENTS.md#rf-003).

## RF-004 — Descobrir vizinhos por contacto conhecido

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `002-overlay`.
- Critério de aceitação: Um nó iniciado apenas com configuração local e contacto bootstrap recebe os endpoints dos vizinhos permitidos, sem precisar de os fornecer na CLI. Por instrução explícita do utilizador (ADR-013), bootstrap só consulta a configuração e devolve essas localizações: sem registo vivo, leases, renovações, monitorização, avisos de saída ou cálculo/encaminhamento de rotas. Depois da descoberta, os nós comunicam e reconectam diretamente, mesmo com bootstrap parado.
- Tarefas: [TASK-003](../../../docs/tasks/PHASE-01/TASK-003.md), [TASK-006](../../../docs/tasks/PHASE-02/TASK-006.md), [TASK-007](../../../docs/tasks/PHASE-02/TASK-007.md).
- Validação: V-003, V-006, V-007.
- Fonte completa: [RF-004](../../../docs/02-REQUIREMENTS.md#rf-004).

## RF-005 — Manter tabela por fluxo com custo, origem, destinos e estado

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `003-rotas`.
- Critério de aceitação: Expor snapshot de cada StreamKey com custo, upstream, conjunto de downstream e estado; mudanças num fluxo não alteram os restantes. Não limitar o modelo a uma única stream.
- Tarefas: [TASK-011](../../../docs/tasks/PHASE-03/TASK-011.md), [TASK-012](../../../docs/tasks/PHASE-03/TASK-012.md), [TASK-014](../../../docs/tasks/PHASE-04/TASK-014.md), [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md).
- Validação: V-011, V-012, V-014, V-015.
- Fonte completa: [RF-005](../../../docs/02-REQUIREMENTS.md#rf-005).

## RF-006 — Suportar caminhos alternativos

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `003-rotas`.
- Critério de aceitação: Numa topologia com dois caminhos overlay até à mesma fonte, guardar alternativa válida e restabelecer receção após falha do upstream, quando a conectividade o permite.
- Tarefas: [TASK-011](../../../docs/tasks/PHASE-03/TASK-011.md), [TASK-012](../../../docs/tasks/PHASE-03/TASK-012.md), [TASK-016](../../../docs/tasks/PHASE-04/TASK-016.md), [TASK-026](../../../docs/tasks/PHASE-06/TASK-026.md).
- Validação: V-011, V-012, V-016, V-026.
- Fonte completa: [RF-006](../../../docs/02-REQUIREMENTS.md#rf-006).

## RF-007 — Servir vários clientes em simultâneo

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `004-arvores`.
- Critério de aceitação: Dois clientes consomem a mesma stream e clientes diferentes consomem streams distintas sem interferência; parar um cliente não interrompe o outro.
- Tarefas: [TASK-014](../../../docs/tasks/PHASE-04/TASK-014.md), [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md), [TASK-021](../../../docs/tasks/PHASE-05/TASK-021.md), [TASK-024](../../../docs/tasks/PHASE-05/TASK-024.md), [TASK-027](../../../docs/tasks/PHASE-06/TASK-027.md).
- Validação: V-014, V-015, V-021, V-024, V-027.
- Fonte completa: [RF-007](../../../docs/02-REQUIREMENTS.md#rf-007).

## RF-008 — Monitorizar a rede overlay

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `003-rotas`.
- Critério de aceitação: Medir RTT por vizinho com sondas correlacionadas, detetar falta de resposta e disponibilizar métricas datadas; não apresentar RTT TCP como perda de vídeo ou largura de banda disponível.
- Tarefas: [TASK-010](../../../docs/tasks/PHASE-03/TASK-010.md), [TASK-028](../../../docs/tasks/PHASE-06/TASK-028.md).
- Validação: V-010, V-028.
- Fonte completa: [RF-008](../../../docs/02-REQUIREMENTS.md#rf-008).

## RF-009 — Atualizar rotas perante entradas, saídas e condições de rede

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `004-arvores`.
- Critério de aceitação: Demonstrar recuperação após falha e regresso de relay, expiração de anúncios e troca de rota por aumento sustentado de RTT; partição produz estado UNAVAILABLE.
- Tarefas: [TASK-012](../../../docs/tasks/PHASE-03/TASK-012.md), [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md), [TASK-016](../../../docs/tasks/PHASE-04/TASK-016.md), [TASK-017](../../../docs/tasks/PHASE-04/TASK-017.md), [TASK-026](../../../docs/tasks/PHASE-06/TASK-026.md).
- Validação: V-012, V-015, V-016, V-017, V-026.
- Fonte completa: [RF-009](../../../docs/02-REQUIREMENTS.md#rf-009).

## RF-010 — Reencaminhar dados segundo a tabela

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `005-video`.
- Critério de aceitação: Aceitar dados apenas do upstream ativo da stream e época corretos; enviar apenas para downstream ativos, uma vez por destino; stream desconhecida/inativa é descartada.
- Tarefas: [TASK-013](../../../docs/tasks/PHASE-03/TASK-013.md), [TASK-019](../../../docs/tasks/PHASE-05/TASK-019.md), [TASK-021](../../../docs/tasks/PHASE-05/TASK-021.md).
- Validação: V-013, V-019, V-021.
- Fonte completa: [RF-010](../../../docs/02-REQUIREMENTS.md#rf-010).

## RF-011 — Partilhar segmentos comuns e replicar nos pontos de ramificação

- Natureza: Obrigatório pelo comportamento descrito.
- Prioridade de origem: P0.
- Unidade responsável primária: `004-arvores`.
- Critério de aceitação: Em estado estável, dois clientes a jusante partilham uma única cópia de cada datagrama no segmento comum; a prova combina contadores e captura por ligação lógica.
- Tarefas: [TASK-014](../../../docs/tasks/PHASE-04/TASK-014.md), [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md), [TASK-021](../../../docs/tasks/PHASE-05/TASK-021.md), [TASK-025](../../../docs/tasks/PHASE-06/TASK-025.md), [TASK-028](../../../docs/tasks/PHASE-06/TASK-028.md).
- Validação: V-014, V-015, V-021, V-025, V-028.
- Fonte completa: [RF-011](../../../docs/02-REQUIREMENTS.md#rf-011).

## RF-012 — Disponibilizar pelo menos dois servidores de conteúdos

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `005-video`.
- Critério de aceitação: Executar dois processos de origem, em nós de teste identificados, com catálogos próprios e anúncios simultâneos; bootstrap não conta como servidor de vídeo.
- Tarefas: [TASK-003](../../../docs/tasks/PHASE-01/TASK-003.md), [TASK-012](../../../docs/tasks/PHASE-03/TASK-012.md), [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md), [TASK-024](../../../docs/tasks/PHASE-05/TASK-024.md), [TASK-027](../../../docs/tasks/PHASE-06/TASK-027.md).
- Validação: V-003, V-012, V-020, V-024, V-027.
- Fonte completa: [RF-012](../../../docs/02-REQUIREMENTS.md#rf-012).

## RF-013 — Cada servidor oferece pelo menos dois vídeos distintos

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `005-video`.
- Critério de aceitação: Configurar quatro UUID e pelo menos dois conteúdos visualmente distintos em cada servidor; registar origem, ficheiro, hash e evidência de reprodução. Aliases do mesmo ficheiro não bastam.
- Tarefas: [TASK-003](../../../docs/tasks/PHASE-01/TASK-003.md), [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md), [TASK-024](../../../docs/tasks/PHASE-05/TASK-024.md), [TASK-027](../../../docs/tasks/PHASE-06/TASK-027.md).
- Validação: V-003, V-020, V-024, V-027.
- Fonte completa: [RF-013](../../../docs/02-REQUIREMENTS.md#rf-013).

## RF-014 — Usar métricas de rede para construir rotas

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `003-rotas`.
- Critério de aceitação: O custo é calculado de RTT medido e acumulado; alterar o atraso de um caminho pode alterar a seleção, após histerese documentada; saltos só desempata.
- Tarefas: [TASK-010](../../../docs/tasks/PHASE-03/TASK-010.md), [TASK-011](../../../docs/tasks/PHASE-03/TASK-011.md), [TASK-012](../../../docs/tasks/PHASE-03/TASK-012.md), [TASK-017](../../../docs/tasks/PHASE-04/TASK-017.md), [TASK-026](../../../docs/tasks/PHASE-06/TASK-026.md).
- Validação: V-010, V-011, V-012, V-017, V-026.
- Fonte completa: [RF-014](../../../docs/02-REQUIREMENTS.md#rf-014).

## RF-015 — Entregar e reproduzir vídeo com objetivo de tempo real

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `005-video`.
- Critério de aceitação: Receber vídeo identificável dos quatro fluxos e reproduzi-lo continuamente em cliente; demonstrar pacing, buffers limitados e descarte de frames atrasados. Limiares numéricos são metas técnicas, não promessas do PDF.
- Tarefas: [TASK-019](../../../docs/tasks/PHASE-05/TASK-019.md), [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md), [TASK-021](../../../docs/tasks/PHASE-05/TASK-021.md), [TASK-022](../../../docs/tasks/PHASE-05/TASK-022.md), [TASK-023](../../../docs/tasks/PHASE-05/TASK-023.md), [TASK-024](../../../docs/tasks/PHASE-05/TASK-024.md).
- Validação: V-019, V-020, V-021, V-022, V-023, V-024.
- Fonte completa: [RF-015](../../../docs/02-REQUIREMENTS.md#rf-015).

## RNF-001 — Atraso baixo e utilização eficiente de largura de banda

- Natureza: Obrigatório qualitativo.
- Prioridade de origem: P0.
- Unidade responsável primária: `005-video`.
- Critério de aceitação: Medir RTT, FPS, interrupção, overhead e tráfego por segmento; comparar duas condições de rede e discutir limites. Não declarar garantia absoluta de ausência de perdas.
- Tarefas: [TASK-017](../../../docs/tasks/PHASE-04/TASK-017.md), [TASK-019](../../../docs/tasks/PHASE-05/TASK-019.md), [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md), [TASK-021](../../../docs/tasks/PHASE-05/TASK-021.md), [TASK-022](../../../docs/tasks/PHASE-05/TASK-022.md), [TASK-028](../../../docs/tasks/PHASE-06/TASK-028.md).
- Validação: V-017, V-019, V-020, V-021, V-022, V-028.
- Fonte completa: [RNF-001](../../../docs/02-REQUIREMENTS.md#rnf-001).

## RNF-002 — Escalabilidade para N clientes

- Natureza: Obrigatório qualitativo.
- Prioridade de origem: P0.
- Unidade responsável primária: `006-core`.
- Critério de aceitação: Comparar 1, 2 e 5 clientes na topologia complexa, com capacidade disponível, verificando que o tronco comum não cresce proporcionalmente ao número de clientes da mesma stream.
- Tarefas: [TASK-021](../../../docs/tasks/PHASE-05/TASK-021.md), [TASK-027](../../../docs/tasks/PHASE-06/TASK-027.md), [TASK-028](../../../docs/tasks/PHASE-06/TASK-028.md).
- Validação: V-021, V-027, V-028.
- Fonte completa: [RNF-002](../../../docs/02-REQUIREMENTS.md#rnf-002).

## RNF-003 — Fiabilidade sob alterações e falhas

- Natureza: Obrigatório qualitativo.
- Prioridade de origem: P0.
- Unidade responsável primária: `004-arvores`.
- Critério de aceitação: Não bloquear permanentemente após falha de nó; recuperar com alternativa ou apresentar indisponibilidade; reconexão não mantém subscrições fantasma indefinidas.
- Tarefas: [TASK-008](../../../docs/tasks/PHASE-02/TASK-008.md), [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md), [TASK-016](../../../docs/tasks/PHASE-04/TASK-016.md), [TASK-017](../../../docs/tasks/PHASE-04/TASK-017.md), [TASK-022](../../../docs/tasks/PHASE-05/TASK-022.md), [TASK-026](../../../docs/tasks/PHASE-06/TASK-026.md).
- Validação: V-008, V-015, V-016, V-017, V-022, V-026.
- Fonte completa: [RNF-003](../../../docs/02-REQUIREMENTS.md#rnf-003).

## RNF-004 — Overlay aplicacional sobre transporte IP

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `002-overlay`.
- Critério de aceitação: Os links overlay são sockets TCP/UDP entre processos; não usar multicast IP para substituir replicação aplicacional; distinguir caminho overlay de rota IP.
- Tarefas: [TASK-005](../../../docs/tasks/PHASE-02/TASK-005.md), [TASK-007](../../../docs/tasks/PHASE-02/TASK-007.md), [TASK-025](../../../docs/tasks/PHASE-06/TASK-025.md).
- Validação: V-005, V-007, V-025.
- Fonte completa: [RNF-004](../../../docs/02-REQUIREMENTS.md#rnf-004).

## RNF-005 — Especificação, implementação e avaliação crítica verificáveis

- Natureza: Obrigatório de avaliação.
- Prioridade de origem: P0.
- Unidade responsável primária: `001-base-validada`.
- Critério de aceitação: Ligar desenho e contratos a implementação futura, testes e apreciação crítica; incluir evidências e limitações, sem resultados inventados.
- Tarefas: [TASK-002](../../../docs/tasks/PHASE-01/TASK-002.md), [TASK-004](../../../docs/tasks/PHASE-01/TASK-004.md), [TASK-009](../../../docs/tasks/PHASE-02/TASK-009.md), [TASK-018](../../../docs/tasks/PHASE-04/TASK-018.md), [TASK-028](../../../docs/tasks/PHASE-06/TASK-028.md), [TASK-029](../../../docs/tasks/PHASE-07/TASK-029.md), [TASK-031](../../../docs/tasks/PHASE-07/TASK-031.md).
- Validação: V-002, V-004, V-009, V-018, V-028, V-029, V-031.
- Fonte completa: [RNF-005](../../../docs/02-REQUIREMENTS.md#rnf-005).

## RC-001 — Usar CORE como bancada principal

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `006-core`.
- Critério de aceitação: Validar solução integrada no CORE em cenários incrementalmente representativos, com ficheiros, endereços, processos e condições reproduzíveis.
- Tarefas: [TASK-025](../../../docs/tasks/PHASE-06/TASK-025.md), [TASK-026](../../../docs/tasks/PHASE-06/TASK-026.md), [TASK-027](../../../docs/tasks/PHASE-06/TASK-027.md), [TASK-028](../../../docs/tasks/PHASE-06/TASK-028.md).
- Validação: V-025, V-026, V-027, V-028.
- Fonte completa: [RC-001](../../../docs/02-REQUIREMENTS.md#rc-001).

## RC-002 — Relatório em formato de artigo

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `007-entrega`.
- Critério de aceitação: Produzir PDF em formato de artigo que descreva desenho, protocolos, implementação, testes, conclusões e crítica.
- Tarefas: [TASK-029](../../../docs/tasks/PHASE-07/TASK-029.md).
- Validação: V-029.
- Fonte completa: [RC-002](../../../docs/02-REQUIREMENTS.md#rc-002).

## RC-003 — Relatório com máximo de 12 páginas

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `007-entrega`.
- Critério de aceitação: Contar todas as páginas do PDF final, incluindo referências e anexos incorporados; total até 12, salvo esclarecimento docente expresso.
- Tarefas: [TASK-029](../../../docs/tasks/PHASE-07/TASK-029.md), [TASK-030](../../../docs/tasks/PHASE-07/TASK-030.md).
- Validação: V-029, V-030.
- Fonte completa: [RC-003](../../../docs/02-REQUIREMENTS.md#rc-003).

## RC-004 — Arquivo de código ZIP ou RAR

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `007-entrega`.
- Critério de aceitação: Gerar ZIP, opção proposta, que extrai e permite repetir build/testes com instruções; não omitir ficheiros/configurações necessários.
- Tarefas: [TASK-030](../../../docs/tasks/PHASE-07/TASK-030.md).
- Validação: V-030.
- Fonte completa: [RC-004](../../../docs/02-REQUIREMENTS.md#rc-004).

## RC-005 — Nomes de entrega com turno e grupo

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `007-entrega`.
- Critério de aceitação: Usar ESR-TP2-PLx-Gy-Rel.pdf e ESR-TP2-PLx-Gy-Codigo.zip; obter x/y reais antes da entrega, sem os inventar.
- Tarefas: [TASK-030](../../../docs/tasks/PHASE-07/TASK-030.md), [TASK-031](../../../docs/tasks/PHASE-07/TASK-031.md).
- Validação: V-030, V-031.
- Fonte completa: [RC-005](../../../docs/02-REQUIREMENTS.md#rc-005).

## RC-006 — Submeter na troca de ficheiros do grupo Blackboard

- Natureza: Obrigatório explícito.
- Prioridade de origem: P0.
- Unidade responsável primária: `007-entrega`.
- Critério de aceitação: A equipa submete os dois artefactos no grupo correto em elearning.uminho.pt e guarda confirmação; documentação não equivale a upload.
- Tarefas: [TASK-031](../../../docs/tasks/PHASE-07/TASK-031.md).
- Validação: V-031.
- Fonte completa: [RC-006](../../../docs/02-REQUIREMENTS.md#rc-006).

## RC-007 — Preparar duas avaliações intermédias e defesa final

- Natureza: Obrigatório de avaliação.
- Prioridade de origem: P0.
- Unidade responsável primária: `007-entrega`.
- Critério de aceitação: Preparar duas demonstrações intermédias reproduzíveis e defesa final, alinhando datas e conteúdo com o docente antes das sessões.
- Tarefas: [TASK-009](../../../docs/tasks/PHASE-02/TASK-009.md), [TASK-018](../../../docs/tasks/PHASE-04/TASK-018.md), [TASK-031](../../../docs/tasks/PHASE-07/TASK-031.md).
- Validação: V-009, V-018, V-031.
- Fonte completa: [RC-007](../../../docs/02-REQUIREMENTS.md#rc-007).

## RP-001 — Catálogo e seleção explícita de stream

- Natureza: Decisão técnica proposta.
- Prioridade de origem: P1.
- Unidade responsável primária: `005-video`.
- Critério de aceitação: Mostrar UUID, título e origem dos anúncios ainda válidos; selecionar um fluxo sem confundir fontes.
- Tarefas: [TASK-003](../../../docs/tasks/PHASE-01/TASK-003.md), [TASK-012](../../../docs/tasks/PHASE-03/TASK-012.md), [TASK-023](../../../docs/tasks/PHASE-05/TASK-023.md), [TASK-024](../../../docs/tasks/PHASE-05/TASK-024.md).
- Validação: V-003, V-012, V-023, V-024.
- Fonte completa: [RP-001](../../../docs/02-REQUIREMENTS.md#rp-001).

## RP-002 — Ativar sob pedido e podar ramo sem recetores

- Natureza: Recomendação adotada.
- Prioridade de origem: P1.
- Unidade responsável primária: `004-arvores`.
- Critério de aceitação: Sem subscritores não há vídeo no ramo; dois clientes partilham uma ativação a montante; saída do último remove o ramo, por cancelamento ou expiração de lease.
- Tarefas: [TASK-014](../../../docs/tasks/PHASE-04/TASK-014.md), [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md).
- Validação: V-014, V-015.
- Fonte completa: [RP-002](../../../docs/02-REQUIREMENTS.md#rp-002).

## RP-003 — MJPEG, pacotes numerados e janela de reprodução

- Natureza: Estratégia sugerida adotada.
- Prioridade de origem: P1.
- Unidade responsável primária: `005-video`.
- Critério de aceitação: Validar o formato real de movie.Mjpeg antes de escrever o leitor; numerar frames, fragmentar/reconstituir e mostrar imagens numa janela. Pacote proposto não é declarado RTP.
- Tarefas: [TASK-019](../../../docs/tasks/PHASE-05/TASK-019.md), [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md), [TASK-022](../../../docs/tasks/PHASE-05/TASK-022.md), [TASK-023](../../../docs/tasks/PHASE-05/TASK-023.md).
- Validação: V-019, V-020, V-022, V-023.
- Fonte completa: [RP-003](../../../docs/02-REQUIREMENTS.md#rp-003).

## RP-004 — Diagnóstico do percurso antes do vídeo

- Natureza: Recomendação adotada.
- Prioridade de origem: P1.
- Unidade responsável primária: `003-rotas`.
- Critério de aceitação: Usar ROUTE_TRACE sintético e logs para comprovar o percurso selecionado sem depender da GUI ou de ficheiros de vídeo.
- Tarefas: [TASK-013](../../../docs/tasks/PHASE-03/TASK-013.md).
- Validação: V-013.
- Fonte completa: [RP-004](../../../docs/02-REQUIREMENTS.md#rp-004).

## RP-005 — Testar cenários de 4, 6 e overlay complexa

- Natureza: Cenários sugeridos adotados.
- Prioridade de origem: P1.
- Unidade responsável primária: `006-core`.
- Critério de aceitação: Demonstrar 4 e 6 processos overlay nos cenários simples e uma overlay complexa inspirada na figura 1, com extensão explícita para duas fontes.
- Tarefas: [TASK-025](../../../docs/tasks/PHASE-06/TASK-025.md), [TASK-026](../../../docs/tasks/PHASE-06/TASK-026.md), [TASK-027](../../../docs/tasks/PHASE-06/TASK-027.md).
- Validação: V-025, V-026, V-027.
- Fonte completa: [RP-005](../../../docs/02-REQUIREMENTS.md#rp-005).

## RP-006 — Código testável, entradas validadas e recursos limitados

- Natureza: Recomendação técnica.
- Prioridade de origem: P1.
- Unidade responsável primária: `001-base-validada`.
- Critério de aceitação: Validar limites antes de alocar; injetar relógio/transportes nos testes; filas e caches têm máximo e política de overflow; não aceitar objetos Java serializados da rede.
- Tarefas: [TASK-001](../../../docs/tasks/PHASE-01/TASK-001.md), [TASK-003](../../../docs/tasks/PHASE-01/TASK-003.md), [TASK-004](../../../docs/tasks/PHASE-01/TASK-004.md), [TASK-005](../../../docs/tasks/PHASE-02/TASK-005.md), [TASK-007](../../../docs/tasks/PHASE-02/TASK-007.md), [TASK-010](../../../docs/tasks/PHASE-03/TASK-010.md), [TASK-011](../../../docs/tasks/PHASE-03/TASK-011.md), [TASK-014](../../../docs/tasks/PHASE-04/TASK-014.md), [TASK-019](../../../docs/tasks/PHASE-05/TASK-019.md), [TASK-021](../../../docs/tasks/PHASE-05/TASK-021.md), [TASK-022](../../../docs/tasks/PHASE-05/TASK-022.md).
- Validação: V-001, V-003, V-004, V-005, V-007, V-010, V-011, V-014, V-019, V-021, V-022.
- Fonte completa: [RP-006](../../../docs/02-REQUIREMENTS.md#rp-006).

## RP-007 — Adotar formato LNCS para o artigo

- Natureza: Recomendação adotada.
- Prioridade de origem: P2.
- Unidade responsável primária: `007-entrega`.
- Critério de aceitação: Usar template LNCS obtido de fonte oficial compatível; distinguir recomendação de formato de artigo obrigatório.
- Tarefas: [TASK-029](../../../docs/tasks/PHASE-07/TASK-029.md).
- Validação: V-029.
- Fonte completa: [RP-007](../../../docs/02-REQUIREMENTS.md#rp-007).

## RP-008 — Documentar comandos, logs e operação reproduzível

- Natureza: Recomendação técnica.
- Prioridade de origem: P1.
- Unidade responsável primária: `007-entrega`.
- Critério de aceitação: Guardar passos, versões, configuração e evidência correlacionada por stream e nó; validar instruções numa extração limpa do pacote.
- Tarefas: [TASK-008](../../../docs/tasks/PHASE-02/TASK-008.md), [TASK-013](../../../docs/tasks/PHASE-03/TASK-013.md), [TASK-024](../../../docs/tasks/PHASE-05/TASK-024.md), [TASK-025](../../../docs/tasks/PHASE-06/TASK-025.md), [TASK-028](../../../docs/tasks/PHASE-06/TASK-028.md), [TASK-030](../../../docs/tasks/PHASE-07/TASK-030.md).
- Validação: V-008, V-013, V-024, V-025, V-028, V-030.
- Fonte completa: [RP-008](../../../docs/02-REQUIREMENTS.md#rp-008).

## Restrições, hipóteses e metas

Preservar ADR-013, tecnologias e regras nos standards. ADR-015 explicita a extensão
de integração em TASK-006, ainda não implementada. Não criar DB/HTTP/failover de
conteúdo, grafo arbitrário de IDs em execução ou imposição RTP não fornecida.
Vídeo permanece o âmbito adotado para media, sem nova instrução docente sobre áudio/texto.

Metas de docs/09 a medir/calibrar: ativação até 5 s, recuperação com alternativa
até 10 s, FPS apresentado de pelo menos 90% no baseline, sem oscilação em 60 s e
poda até 7 s. São metas de engenharia, não SLA do enunciado. Protocolo proposto:
warm-up 10 s, medição 60 s e três repetições, ou desvio justificado. Nada foi medido aqui.

## Pendências

[Auditoria e gates externos](../../../docs/17-INCEPTION-AUDIT.md) identifica media, GUI, CORE, avaliações,
PL/grupo/prazo e submissão humana. A falta de resultados não é sucesso presumido.

## EXTRA-01 — Retoma após a base de Inception

Pedido de 2026-11-11T12:11:34Z, [REQ-025](../../../specs.md#req-025). A criação de
novas arestas é um extra aprovado em âmbito. A opção inicial do catálogo
foi substituída por divulgação dos vizinhos diretos e cache limitada,
por instrução posterior do utilizador. A política detalhada e ADR-033
aguardam revisão.
A reconstrução da overlay precede 005-rotas; a prova de vídeo segue a
implementação dos seus componentes. [Bolt](../../bolts/034-overlay-extra/bolt.md).

### EXTRA-01 — execução normal requerida

> Histórico: as exceções opcionais/base e eventual falha global foram
> substituídas pelas clarificações posteriores de obrigatoriedade e isolamento.

Por decisão do utilizador, todos os nós overlay da execução normal têm o extra
ativo e capacidade negociada obrigatória, com Q>=2 validado antes de I/O. Não
há degradação para base por fila/peer incompatível. APIs/perfis base conservam
compatibilidade/Q=1. A proposta de Design/ADR-034 concretiza erro explícito e
preflight normal; não é autorização de implementação.

### EXTRA-01 — mudança de requisito para todos os nós overlay

O extra é obrigatório por papel da aplicação, sem chave de ativação opcional,
construtor/API/perfil base ou ACTIVE_BASE operacional. Ausência já exige;
false é erro; Q>=2 validado antes de recursos. Preservar formatos v1 usados,
bootstrap inicial e UDP/vídeo, e justificar migração de expectativas opcionais.
IMP-034 aprende candidatos; IMP-035 concretiza reparação. Design/ADR por aprovar.

### EXTRA-01 — programa único e rejeição local

Registo de 2026-11-11T17:22:59Z. Um único programa e a mesma versão do JAR em todos os nós;
todos os overlay exigem descoberta. Versão, identidade, endpoint e capacidade
continuam validados. Entradas inválidas são negativos do handshake, rejeitados
só na tentativa, sem NeighborUp, FAILED global, fim da CLI ou fecho de sessões
saudáveis. Estado/retry por peer limitados com backoff existente; ausência
temporária distingue-se de incompatibilidade confirmada. Não desenvolver
funcionamento com versões antigas/base opcional/fallback. IMP-034 aprende;
IMP-035 repara depois. Design/ADR revistos para aprovação, código suspenso.

### EXTRA-01 — todo o TCP em v2

Registo de 2026-11-11T17:47:08Z. Decisão explícita: TCP exclusivamente v2 em bootstrap,
handshake, mensagens normais e descoberta; um contrato/codec, 18 tipos explícitos,
código 3 rejeitado, sem runtime v1/fallback. Arquivo v1 e fixtures v2 independentes
preparados; migração dos testes justificada com garantias preservadas. Bootstrap
continua só inicial, UDP OTT1 inalterado, descoberta obrigatória e isolamento por
peer/CLI saudável mantidos; cache/TTL/proveniência/shutdown conservados.
IMP-034 aprende, IMP-035 repara depois. Design/ADR-034/âmbito para aprovação;
Implement suspenso, sem execução Java, commits, push ou outro IMP.
