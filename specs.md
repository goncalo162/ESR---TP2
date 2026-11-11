# Especificação consolidada — serviço OTT

Consolidação: 2026-11-02T15:31:47Z. Respostas do utilizador: **1A, 2A, 3B, 4A**.

## Âmbito e precedência

Enunciado → specs.md → intents → documentação existente → código. O ADD é a
única referência normativa das decisões de arquitetura para Construction, derivada
destas fontes; não pode dispensar uma obrigação do enunciado. Documentos técnicos
incorporados pelo ADD detalham os contratos e não decidem arquitetura em paralelo.

Os IDs RF/RNF/RC/RP continuam como aliases. REQ-016–023 são recomendações/estratégias
adotadas, não novos requisitos impostos pelo docente. Os critérios legados são
preservados; a localização e o estado vivo continuam em docs/02, docs/13 e docs/15.
Este ficheiro especifica o que é exigido, sem declarar funcionalidades implementadas.

<a id="req-001"></a>
## REQ-001 — Executar nós como aplicações em máquinas IP

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-001.
- Origem: [RF-001](docs/02-REQUIREMENTS.md#rf-001).
- Critério de aceitação: Iniciar manualmente servidor, relay e cliente em processos independentes; cada um escuta no endpoint configurado e reporta o seu papel.
- Tarefas relacionadas: TASK-003, TASK-008.
- Verificação: V-003, V-008.

<a id="req-002"></a>
## REQ-002 — Entrada e saída dinâmica de nós

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-002.
- Origem: [RF-002](docs/02-REQUIREMENTS.md#rf-002).
- Critério de aceitação: Adicionar um nó a uma overlay em execução, removê-lo com encerramento normal e abrupto, e observar atualização de vizinhos e rotas sem reinício dos restantes.
- Tarefas relacionadas: TASK-006, TASK-007, TASK-008, TASK-015, TASK-016.
- Verificação: V-006, V-007, V-008, V-015, V-016.

<a id="req-003"></a>
## REQ-003 — Estabelecer e manter X vizinhos TCP ou UDP

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-003.
- Origem: [RF-003](docs/02-REQUIREMENTS.md#rf-003).
- Critério de aceitação: Estabelecer uma ligação lógica bidirecional por vizinho permitido disponível; repetir tentativas para vizinhos indisponíveis e mostrar grau desejado/atual. X é configurável por nó e limitado pela topologia, não fixado em três.
- Tarefas relacionadas: TASK-005, TASK-006, TASK-007, TASK-010.
- Verificação: V-005, V-006, V-007, V-010.

<a id="req-004"></a>
## REQ-004 — Descobrir vizinhos por contacto conhecido

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-004.
- Origem: [RF-004](docs/02-REQUIREMENTS.md#rf-004).
- Critério de aceitação: Um nó iniciado apenas com configuração local e contacto bootstrap recebe os endpoints dos vizinhos permitidos, sem precisar de os fornecer na CLI. Por instrução explícita do utilizador (ADR-013), bootstrap só consulta a configuração e devolve essas localizações: sem registo vivo, leases, renovações, monitorização, avisos de saída ou cálculo/encaminhamento de rotas. Depois da descoberta, os nós comunicam e reconectam diretamente, mesmo com bootstrap parado.
- Tarefas relacionadas: TASK-003, TASK-006, TASK-007.
- Verificação: V-003, V-006, V-007.

<a id="req-005"></a>
## REQ-005 — Manter tabela por fluxo com custo, origem, destinos e estado

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-005.
- Origem: [RF-005](docs/02-REQUIREMENTS.md#rf-005).
- Critério de aceitação: Expor snapshot de cada StreamKey com custo, upstream, conjunto de downstream e estado; mudanças num fluxo não alteram os restantes. Não limitar o modelo a uma única stream.
- Tarefas relacionadas: TASK-011, TASK-012, TASK-014, TASK-015.
- Verificação: V-011, V-012, V-014, V-015.

<a id="req-006"></a>
## REQ-006 — Suportar caminhos alternativos

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-006.
- Origem: [RF-006](docs/02-REQUIREMENTS.md#rf-006).
- Critério de aceitação: Numa topologia com dois caminhos overlay até à mesma fonte, guardar alternativa válida e restabelecer receção após falha do upstream, quando a conectividade o permite.
- Tarefas relacionadas: TASK-011, TASK-012, TASK-016, TASK-026.
- Verificação: V-011, V-012, V-016, V-026.

<a id="req-007"></a>
## REQ-007 — Servir vários clientes em simultâneo

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-007.
- Origem: [RF-007](docs/02-REQUIREMENTS.md#rf-007).
- Critério de aceitação: Dois clientes consomem a mesma stream e clientes diferentes consomem streams distintas sem interferência; parar um cliente não interrompe o outro.
- Tarefas relacionadas: TASK-014, TASK-015, TASK-021, TASK-024, TASK-027.
- Verificação: V-014, V-015, V-021, V-024, V-027.

<a id="req-008"></a>
## REQ-008 — Monitorizar a rede overlay

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-008.
- Origem: [RF-008](docs/02-REQUIREMENTS.md#rf-008).
- Critério de aceitação: Medir RTT por vizinho com sondas correlacionadas, detetar falta de resposta e disponibilizar métricas datadas; não apresentar RTT TCP como perda de vídeo ou largura de banda disponível.
- Tarefas relacionadas: TASK-010, TASK-028.
- Verificação: V-010, V-028.

<a id="req-009"></a>
## REQ-009 — Atualizar rotas perante entradas, saídas e condições de rede

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-009.
- Origem: [RF-009](docs/02-REQUIREMENTS.md#rf-009).
- Critério de aceitação: Demonstrar recuperação após falha e regresso de relay, expiração de anúncios e troca de rota por aumento sustentado de RTT; partição produz estado UNAVAILABLE.
- Tarefas relacionadas: TASK-012, TASK-015, TASK-016, TASK-017, TASK-026.
- Verificação: V-012, V-015, V-016, V-017, V-026.

<a id="req-010"></a>
## REQ-010 — Reencaminhar dados segundo a tabela

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-010.
- Origem: [RF-010](docs/02-REQUIREMENTS.md#rf-010).
- Critério de aceitação: Aceitar dados apenas do upstream ativo da stream e época corretos; enviar apenas para downstream ativos, uma vez por destino; stream desconhecida/inativa é descartada.
- Tarefas relacionadas: TASK-013, TASK-019, TASK-021.
- Verificação: V-013, V-019, V-021.

<a id="req-011"></a>
## REQ-011 — Partilhar segmentos comuns e replicar nos pontos de ramificação

- Natureza: Obrigatório pelo comportamento descrito.
- Prioridade: P0.
- Alias: RF-011.
- Origem: [RF-011](docs/02-REQUIREMENTS.md#rf-011).
- Critério de aceitação: Em estado estável, dois clientes a jusante partilham uma única cópia de cada datagrama no segmento comum; a prova combina contadores e captura por ligação lógica.
- Tarefas relacionadas: TASK-014, TASK-015, TASK-021, TASK-025, TASK-028.
- Verificação: V-014, V-015, V-021, V-025, V-028.

<a id="req-012"></a>
## REQ-012 — Disponibilizar pelo menos dois servidores de conteúdos

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-012.
- Origem: [RF-012](docs/02-REQUIREMENTS.md#rf-012).
- Critério de aceitação: Executar dois processos de origem, em nós de teste identificados, com catálogos próprios e anúncios simultâneos; bootstrap não conta como servidor de vídeo.
- Tarefas relacionadas: TASK-003, TASK-012, TASK-020, TASK-024, TASK-027.
- Verificação: V-003, V-012, V-020, V-024, V-027.

<a id="req-013"></a>
## REQ-013 — Cada servidor oferece pelo menos dois vídeos distintos

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-013.
- Origem: [RF-013](docs/02-REQUIREMENTS.md#rf-013).
- Critério de aceitação: Configurar quatro UUID e pelo menos dois conteúdos visualmente distintos em cada servidor; registar origem, ficheiro, hash e evidência de reprodução. Aliases do mesmo ficheiro não bastam.
- Tarefas relacionadas: TASK-003, TASK-020, TASK-024, TASK-027.
- Verificação: V-003, V-020, V-024, V-027.

<a id="req-014"></a>
## REQ-014 — Usar métricas de rede para construir rotas

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-014.
- Origem: [RF-014](docs/02-REQUIREMENTS.md#rf-014).
- Critério de aceitação: O custo é calculado de RTT medido e acumulado; alterar o atraso de um caminho pode alterar a seleção, após histerese documentada; saltos só desempata.
- Tarefas relacionadas: TASK-010, TASK-011, TASK-012, TASK-017, TASK-026.
- Verificação: V-010, V-011, V-012, V-017, V-026.

<a id="req-015"></a>
## REQ-015 — Entregar e reproduzir vídeo com objetivo de tempo real

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RF-015.
- Origem: [RF-015](docs/02-REQUIREMENTS.md#rf-015).
- Critério de aceitação: Receber vídeo identificável dos quatro fluxos e reproduzi-lo continuamente em cliente; demonstrar pacing, buffers limitados e descarte de frames atrasados. Limiares numéricos são metas técnicas, não promessas do PDF.
- Tarefas relacionadas: TASK-019, TASK-020, TASK-021, TASK-022, TASK-023, TASK-024.
- Verificação: V-019, V-020, V-021, V-022, V-023, V-024.

<a id="nfr-001"></a>
## NFR-001 — Atraso baixo e utilização eficiente de largura de banda

- Natureza: Obrigatório qualitativo.
- Prioridade: P0.
- Alias: RNF-001.
- Origem: [RNF-001](docs/02-REQUIREMENTS.md#rnf-001).
- Critério de aceitação: Medir RTT, FPS, interrupção, overhead e tráfego por segmento; comparar duas condições de rede e discutir limites. Não declarar garantia absoluta de ausência de perdas.
- Tarefas relacionadas: TASK-017, TASK-019, TASK-020, TASK-021, TASK-022, TASK-028.
- Verificação: V-017, V-019, V-020, V-021, V-022, V-028.

<a id="nfr-002"></a>
## NFR-002 — Escalabilidade para N clientes

- Natureza: Obrigatório qualitativo.
- Prioridade: P0.
- Alias: RNF-002.
- Origem: [RNF-002](docs/02-REQUIREMENTS.md#rnf-002).
- Critério de aceitação: Comparar 1, 2 e 5 clientes na topologia complexa, com capacidade disponível, verificando que o tronco comum não cresce proporcionalmente ao número de clientes da mesma stream.
- Tarefas relacionadas: TASK-021, TASK-027, TASK-028.
- Verificação: V-021, V-027, V-028.

<a id="nfr-003"></a>
## NFR-003 — Fiabilidade sob alterações e falhas

- Natureza: Obrigatório qualitativo.
- Prioridade: P0.
- Alias: RNF-003.
- Origem: [RNF-003](docs/02-REQUIREMENTS.md#rnf-003).
- Critério de aceitação: Não bloquear permanentemente após falha de nó; recuperar com alternativa ou apresentar indisponibilidade; reconexão não mantém subscrições fantasma indefinidas.
- Tarefas relacionadas: TASK-008, TASK-015, TASK-016, TASK-017, TASK-022, TASK-026.
- Verificação: V-008, V-015, V-016, V-017, V-022, V-026.

<a id="nfr-004"></a>
## NFR-004 — Overlay aplicacional sobre transporte IP

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RNF-004.
- Origem: [RNF-004](docs/02-REQUIREMENTS.md#rnf-004).
- Critério de aceitação: Os links overlay são sockets TCP/UDP entre processos; não usar multicast IP para substituir replicação aplicacional; distinguir caminho overlay de rota IP.
- Tarefas relacionadas: TASK-005, TASK-007, TASK-025.
- Verificação: V-005, V-007, V-025.

<a id="nfr-005"></a>
## NFR-005 — Especificação, implementação e avaliação crítica verificáveis

- Natureza: Obrigatório de avaliação.
- Prioridade: P0.
- Alias: RNF-005.
- Origem: [RNF-005](docs/02-REQUIREMENTS.md#rnf-005).
- Critério de aceitação: Ligar desenho e contratos a implementação futura, testes e apreciação crítica; incluir evidências e limitações, sem resultados inventados.
- Tarefas relacionadas: TASK-002, TASK-004, TASK-009, TASK-018, TASK-028, TASK-029, TASK-031.
- Verificação: V-002, V-004, V-009, V-018, V-028, V-029, V-031.

<a id="con-001"></a>
## CON-001 — Usar CORE como bancada principal

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RC-001.
- Origem: [RC-001](docs/02-REQUIREMENTS.md#rc-001).
- Critério de aceitação: Validar solução integrada no CORE em cenários incrementalmente representativos, com ficheiros, endereços, processos e condições reproduzíveis.
- Tarefas relacionadas: TASK-025, TASK-026, TASK-027, TASK-028.
- Verificação: V-025, V-026, V-027, V-028.

<a id="con-002"></a>
## CON-002 — Relatório em formato de artigo

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RC-002.
- Origem: [RC-002](docs/02-REQUIREMENTS.md#rc-002).
- Critério de aceitação: Produzir PDF em formato de artigo que descreva desenho, protocolos, implementação, testes, conclusões e crítica.
- Tarefas relacionadas: TASK-029.
- Verificação: V-029.

<a id="con-003"></a>
## CON-003 — Relatório com máximo de 12 páginas

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RC-003.
- Origem: [RC-003](docs/02-REQUIREMENTS.md#rc-003).
- Critério de aceitação: Contar todas as páginas do PDF final, incluindo referências e anexos incorporados; total até 12, salvo esclarecimento docente expresso.
- Tarefas relacionadas: TASK-029, TASK-030.
- Verificação: V-029, V-030.

<a id="con-004"></a>
## CON-004 — Arquivo de código ZIP ou RAR

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RC-004.
- Origem: [RC-004](docs/02-REQUIREMENTS.md#rc-004).
- Critério de aceitação: Gerar ZIP, opção proposta, que extrai e permite repetir build/testes com instruções; não omitir ficheiros/configurações necessários.
- Tarefas relacionadas: TASK-030.
- Verificação: V-030.

<a id="con-005"></a>
## CON-005 — Nomes de entrega com turno e grupo

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RC-005.
- Origem: [RC-005](docs/02-REQUIREMENTS.md#rc-005).
- Critério de aceitação: Usar ESR-TP2-PLx-Gy-Rel.pdf e ESR-TP2-PLx-Gy-Codigo.zip; obter x/y reais antes da entrega, sem os inventar.
- Tarefas relacionadas: TASK-030, TASK-031.
- Verificação: V-030, V-031.

<a id="con-006"></a>
## CON-006 — Submeter na troca de ficheiros do grupo Blackboard

- Natureza: Obrigatório explícito.
- Prioridade: P0.
- Alias: RC-006.
- Origem: [RC-006](docs/02-REQUIREMENTS.md#rc-006).
- Critério de aceitação: A equipa submete os dois artefactos no grupo correto em elearning.uminho.pt e guarda confirmação; documentação não equivale a upload.
- Tarefas relacionadas: TASK-031.
- Verificação: V-031.

<a id="con-007"></a>
## CON-007 — Preparar duas avaliações intermédias e defesa final

- Natureza: Obrigatório de avaliação.
- Prioridade: P0.
- Alias: RC-007.
- Origem: [RC-007](docs/02-REQUIREMENTS.md#rc-007).
- Critério de aceitação: Preparar duas demonstrações intermédias reproduzíveis e defesa final, alinhando datas e conteúdo com o docente antes das sessões.
- Tarefas relacionadas: TASK-009, TASK-018, TASK-031.
- Verificação: V-009, V-018, V-031.

<a id="req-016"></a>
## REQ-016 — Catálogo e seleção explícita de stream

- Natureza: Decisão técnica proposta.
- Prioridade: P1.
- Alias: RP-001.
- Origem: [RP-001](docs/02-REQUIREMENTS.md#rp-001).
- Critério de aceitação: Mostrar UUID, título e origem dos anúncios ainda válidos; selecionar um fluxo sem confundir fontes.
- Tarefas relacionadas: TASK-003, TASK-012, TASK-023, TASK-024.
- Verificação: V-003, V-012, V-023, V-024.

<a id="req-017"></a>
## REQ-017 — Ativar sob pedido e podar ramo sem recetores

- Natureza: Recomendação adotada.
- Prioridade: P1.
- Alias: RP-002.
- Origem: [RP-002](docs/02-REQUIREMENTS.md#rp-002).
- Critério de aceitação: Sem subscritores não há vídeo no ramo; dois clientes partilham uma ativação a montante; saída do último remove o ramo, por cancelamento ou expiração de lease.
- Tarefas relacionadas: TASK-014, TASK-015.
- Verificação: V-014, V-015.

<a id="req-018"></a>
## REQ-018 — MJPEG, pacotes numerados e janela de reprodução

- Natureza: Estratégia sugerida adotada.
- Prioridade: P1.
- Alias: RP-003.
- Origem: [RP-003](docs/02-REQUIREMENTS.md#rp-003).
- Critério de aceitação: Validar o formato real de movie.Mjpeg antes de escrever o leitor; numerar frames, fragmentar/reconstituir e mostrar imagens numa janela. Pacote proposto não é declarado RTP.
- Tarefas relacionadas: TASK-019, TASK-020, TASK-022, TASK-023.
- Verificação: V-019, V-020, V-022, V-023.

<a id="req-019"></a>
## REQ-019 — Diagnóstico do percurso antes do vídeo

- Natureza: Recomendação adotada.
- Prioridade: P1.
- Alias: RP-004.
- Origem: [RP-004](docs/02-REQUIREMENTS.md#rp-004).
- Critério de aceitação: Usar ROUTE_TRACE sintético e logs para comprovar o percurso selecionado sem depender da GUI ou de ficheiros de vídeo.
- Tarefas relacionadas: TASK-013.
- Verificação: V-013.

<a id="req-020"></a>
## REQ-020 — Testar cenários de 4, 6 e overlay complexa

- Natureza: Cenários sugeridos adotados.
- Prioridade: P1.
- Alias: RP-005.
- Origem: [RP-005](docs/02-REQUIREMENTS.md#rp-005).
- Critério de aceitação: Demonstrar 4 e 6 processos overlay nos cenários simples e uma overlay complexa inspirada na figura 1, com extensão explícita para duas fontes.
- Tarefas relacionadas: TASK-025, TASK-026, TASK-027.
- Verificação: V-025, V-026, V-027.

<a id="req-021"></a>
## REQ-021 — Código testável, entradas validadas e recursos limitados

- Natureza: Recomendação técnica.
- Prioridade: P1.
- Alias: RP-006.
- Origem: [RP-006](docs/02-REQUIREMENTS.md#rp-006).
- Critério de aceitação: Validar limites antes de alocar; injetar relógio/transportes nos testes; filas e caches têm máximo e política de overflow; não aceitar objetos Java serializados da rede.
- Tarefas relacionadas: TASK-001, TASK-003, TASK-004, TASK-005, TASK-007, TASK-010, TASK-011, TASK-014, TASK-019, TASK-021, TASK-022.
- Verificação: V-001, V-003, V-004, V-005, V-007, V-010, V-011, V-014, V-019, V-021, V-022.

<a id="req-022"></a>
## REQ-022 — Adotar formato LNCS para o artigo

- Natureza: Recomendação adotada.
- Prioridade: P2.
- Alias: RP-007.
- Origem: [RP-007](docs/02-REQUIREMENTS.md#rp-007).
- Critério de aceitação: Usar template LNCS obtido de fonte oficial compatível; distinguir recomendação de formato de artigo obrigatório.
- Tarefas relacionadas: TASK-029.
- Verificação: V-029.

<a id="req-023"></a>
## REQ-023 — Documentar comandos, logs e operação reproduzível

- Natureza: Recomendação técnica.
- Prioridade: P1.
- Alias: RP-008.
- Origem: [RP-008](docs/02-REQUIREMENTS.md#rp-008).
- Critério de aceitação: Guardar passos, versões, configuração e evidência correlacionada por stream e nó; validar instruções numa extração limpa do pacote.
- Tarefas relacionadas: TASK-008, TASK-013, TASK-024, TASK-025, TASK-028, TASK-030.
- Verificação: V-008, V-013, V-024, V-025, V-028, V-030.

<a id="req-024"></a>
## REQ-024 — Configuração operacional opcional, centralizada e validada

- Natureza: Decisão explícita do utilizador, resposta 2A.
- Prioridade: P1.
- Alias: novo identificador; não renumera requisitos anteriores.
- Origem: registo de aprovação 1A/2A/3B/4A em add.md, secção 13.
- Critério de aceitação: Sem propriedades opcionais, usar os defaults de add.md; aceitar apenas chaves/papéis/tipos/intervalos e relações válidos, rejeitando o conjunto antes de sockets quando inválido. Uma substituição válida chega ao componente consumidor. Limites binários v1 não são parâmetros de afinação.
- Tarefas relacionadas: TASK-032.
- Verificação: V-032.

<a id="con-008"></a>
## CON-008 — Deteção automática de falhas limitada ao plano TCP

- Natureza: Decisão explícita do utilizador, resposta 3B.
- Prioridade: P1.
- Alias: novo identificador; não renumera requisitos anteriores.
- Origem: registo de aprovação 1A/2A/3B/4A em add.md, secção 13.
- Critério de aceitação: Manter sondas TCP e falhas de transporte como gatilho de recuperação. Bloquear apenas UDP com TCP saudável deve ser demonstrado como limitação, sem garantir troca de parent ou recuperação automática. Contadores/descarte continuam observáveis.
- Tarefas relacionadas: TASK-010, TASK-016, TASK-026, TASK-028.
- Verificação: V-010, V-016, V-026, V-028.

<a id="con-009"></a>
## CON-009 — Qualidade com ferramentas existentes, sem limiar percentual global

- Natureza: Decisão explícita do utilizador, resposta 4A.
- Prioridade: P1.
- Alias: novo identificador; não renumera requisitos anteriores.
- Origem: registo de aprovação 1A/2A/3B/4A em add.md, secção 13.
- Critério de aceitação: Usar Maven/JUnit, validações documentais e cenários críticos; não introduzir CI, linter ou cobertura percentual global obrigatória nesta versão. Todas as verificações aplicáveis devem passar ou permanecer explicitamente pendentes.
- Tarefas relacionadas: TASK-001, TASK-004, TASK-005, TASK-032.
- Verificação: V-001, V-004, V-005, V-032.

<a id="int-001"></a>
## INT-001 — Concluir o serviço OTT segundo as fontes do projeto

- Natureza: Intent existente 001-ott-streaming.
- Prioridade: P1.
- Alias: novo identificador; não renumera requisitos anteriores.
- Origem: memory-bank/intents/001-ott-streaming/requirements.md.
- Critério de aceitação: Preservar o âmbito e os trabalhos concluídos, construir os incrementos em falta e validar a solução integrada, distinguindo conclusão técnica e submissão humana.
- Tarefas relacionadas: TASK-001, TASK-024, TASK-027, TASK-031.
- Verificação: V-001, V-024, V-027, V-031.

<a id="int-002"></a>
## INT-002 — Architecture Decision Document mestre e execução rastreável por Construction

- Natureza: Pedido atual do utilizador e respostas 1A/2A/3B/4A.
- Prioridade: P1.
- Alias: novo identificador; não renumera requisitos anteriores.
- Origem: registo de aprovação 1A/2A/3B/4A em add.md, secção 13.
- Critério de aceitação: add.md contém as 13 secções pedidas, todas as ADR com driver/alternativa/consequência/validação, matriz sem órfãos e tarefas S/M executáveis por sessão. Bolts consultam o ADD e não contêm decisões concorrentes.
- Tarefas relacionadas: TASK-032.
- Verificação: V-ADD, V-032.

<a id="req-025"></a>
## REQ-025 — Extra — reconstrução dinâmica de vizinhanças

- Natureza: Extra pedido explicitamente pelo utilizador.
- Prioridade: P1.
- Alias: novo identificador de extra.
- Origem: pedido do utilizador para divulgação de vizinhos diretos e conservação limitada de candidatos aprendidos.
- Critério de aceitação: Aprender candidatos através da divulgação limitada dos vizinhos diretos em sessões confirmadas, sem exigir a configuração prévia de todos os endpoints; conservar os candidatos com prazo local, proveniência e limites mesmo após morrer quem os anunciou. Após aprender candidatos e perder os relays nos cenários S–R1–C / S–R2–C e S–R1–R2–C / S–R1–R3–C, formar respetivamente S–C e R1–C, mantendo os sobreviventes e sem duplicados. A continuação de vídeo nessas ligações será verificada após routing e streaming. Todos os nós overlay da aplicação suportam e exigem a capacidade de descoberta, sem ativação opcional, API/perfil desligado ou ACTIVE_BASE operacional. Todos os caminhos validam Q>=2 antes de sockets, sem reduzir funcionalidades por insuficiência de recursos. O cenário operacional usa um único programa e a mesma versão do JAR em todos os nós. Versão, identidade, endpoint e capacidade são validados no handshake; tentativa inválida é rejeitada localmente, sem NeighborUp, FAILED global ou interrupção da CLI/sessões saudáveis. Estado por peer e retries têm limites/backoff; ausência temporária distingue-se de incompatibilidade confirmada. Incompatibilidade é entrada negativa de protocolo, sem suporte operacional a versões antigas. Uniformizar todo o controlo TCP em v2, incluindo bootstrap inicial, handshake, mensagens normais e descoberta, com um contrato/codec operacional e sem runtime v1/fallback. Arquivar contrato/fixtures TCP v1 e preparar fixtures v2 independentes, justificando a migração dos testes sem perder garantias. Preservar framing, hard caps, código 3 rejeitado, identidade/unicidade/FIFO/flush/isolamento/shutdown; UDP OTT1 de vídeo permanece inalterado.
- Tarefas relacionadas: TASK-033, TASK-034.
- Verificação: V-033, V-034. Integração com vídeo nas validações posteriores de routing/streaming.

## Qualidades e métricas

As metas experimentais iniciais são ativação ≤5 s, recuperação TCP com alternativa
≤10 s, FPS mostrado ≥90% do configurado no baseline com capacidade suficiente,
zero trocas por custo nos 60 s após convergência e poda ≤7 s. São metas de afinação
de docs/09, não SLA do PDF. Desvios são medidos, explicados e sujeitos a correção
ou decisão explícita; um requisito obrigatório não se relaxa por tuning.

Escala: comparar 1/2/5 clientes; cópia única por chave no tronco em estado estável.
Protocolo de medição: 10 s de warm-up, 60 s de medição e três repetições, ou desvio
documentado. RTT TCP não mede largura de banda, perda UDP ou latência unidirecional.

## Fora de âmbito e gates

Sem DB, HTTP, multicast IP, contas de utilizador, RTP/RTSP, pause/seek ou failover
do mesmo conteúdo entre origens. Não se acrescenta watchdog UDP ou CI/linter.
Media final, GUI, CORE, datas docentes, PL/grupo/prazo e submissão humana mantêm
gates nas tarefas responsáveis. Consultar [ADD](add.md) e [auditoria](docs/17-INCEPTION-AUDIT.md).
