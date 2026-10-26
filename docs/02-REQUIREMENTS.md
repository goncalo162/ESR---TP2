# Requisitos, restrições e critérios de aceitação

Fonte: [PDF original](../ESR-TP2-26-27.pdf), cinco páginas. RF = funcional; RNF = qualidade/arquitetura; RC = restrição de avaliação/entrega; RP = recomendação ou escolha técnica adotada. Cada identificação é única. P0 = obrigatório a cumprir para a entrega; P1 = necessário para a solução técnica escolhida; P2 = recomendação de menor prioridade. Prioridade não dispensa requisito obrigatório.

A lista mínima da p. 3 exige duas origens com pelo menos dois vídeos cada; a referência a um único fluxo na p. 2 é uma simplificação, não limite do modelo. As etapas 0–4 são sugestões de estratégia e ordem. Java, Maven, TCP/UDP separados, MJPEG, numeração e GUI são escolhas do plano, com a natureza identificada abaixo.

Não há requisitos explícitos de autenticação, base de dados, framework, percentagem de cobertura, SLA numérico ou número fixo de vizinhos. Recomendações de segurança/manutenção não devem ser apresentadas como exigências docentes. Hipóteses e gates em [12](12-RISKS-AND-DECISIONS.md).

## Índice dos requisitos

| ID | Descrição | Natureza | Prioridade |
|---|---|---|---|
| [RF-001](#rf-001) | Executar nós como aplicações em máquinas IP | Obrigatório explícito | P0 |
| [RF-002](#rf-002) | Entrada e saída dinâmica de nós | Obrigatório explícito | P0 |
| [RF-003](#rf-003) | Estabelecer e manter X vizinhos TCP ou UDP | Obrigatório explícito | P0 |
| [RF-004](#rf-004) | Descobrir vizinhos por contacto conhecido | Obrigatório explícito | P0 |
| [RF-005](#rf-005) | Manter tabela por fluxo com custo, origem, destinos e estado | Obrigatório explícito | P0 |
| [RF-006](#rf-006) | Suportar caminhos alternativos | Obrigatório explícito | P0 |
| [RF-007](#rf-007) | Servir vários clientes em simultâneo | Obrigatório explícito | P0 |
| [RF-008](#rf-008) | Monitorizar a rede overlay | Obrigatório explícito | P0 |
| [RF-009](#rf-009) | Atualizar rotas perante entradas, saídas e condições de rede | Obrigatório explícito | P0 |
| [RF-010](#rf-010) | Reencaminhar dados segundo a tabela | Obrigatório explícito | P0 |
| [RF-011](#rf-011) | Partilhar segmentos comuns e replicar nos pontos de ramificação | Obrigatório pelo comportamento descrito | P0 |
| [RF-012](#rf-012) | Disponibilizar pelo menos dois servidores de conteúdos | Obrigatório explícito | P0 |
| [RF-013](#rf-013) | Cada servidor oferece pelo menos dois vídeos distintos | Obrigatório explícito | P0 |
| [RF-014](#rf-014) | Usar métricas de rede para construir rotas | Obrigatório explícito | P0 |
| [RF-015](#rf-015) | Entregar e reproduzir vídeo com objetivo de tempo real | Obrigatório explícito | P0 |
| [RNF-001](#rnf-001) | Atraso baixo e utilização eficiente de largura de banda | Obrigatório qualitativo | P0 |
| [RNF-002](#rnf-002) | Escalabilidade para N clientes | Obrigatório qualitativo | P0 |
| [RNF-003](#rnf-003) | Fiabilidade sob alterações e falhas | Obrigatório qualitativo | P0 |
| [RNF-004](#rnf-004) | Overlay aplicacional sobre transporte IP | Obrigatório explícito | P0 |
| [RNF-005](#rnf-005) | Especificação, implementação e avaliação crítica verificáveis | Obrigatório de avaliação | P0 |
| [RC-001](#rc-001) | Usar CORE como bancada principal | Obrigatório explícito | P0 |
| [RC-002](#rc-002) | Relatório em formato de artigo | Obrigatório explícito | P0 |
| [RC-003](#rc-003) | Relatório com máximo de 12 páginas | Obrigatório explícito | P0 |
| [RC-004](#rc-004) | Arquivo de código ZIP ou RAR | Obrigatório explícito | P0 |
| [RC-005](#rc-005) | Nomes de entrega com turno e grupo | Obrigatório explícito | P0 |
| [RC-006](#rc-006) | Submeter na troca de ficheiros do grupo Blackboard | Obrigatório explícito | P0 |
| [RC-007](#rc-007) | Preparar duas avaliações intermédias e defesa final | Obrigatório de avaliação | P0 |
| [RP-001](#rp-001) | Catálogo e seleção explícita de stream | Decisão técnica proposta | P1 |
| [RP-002](#rp-002) | Ativar sob pedido e podar ramo sem recetores | Recomendação adotada | P1 |
| [RP-003](#rp-003) | MJPEG, pacotes numerados e janela de reprodução | Estratégia sugerida adotada | P1 |
| [RP-004](#rp-004) | Diagnóstico do percurso antes do vídeo | Recomendação adotada | P1 |
| [RP-005](#rp-005) | Testar cenários de 4, 6 e overlay complexa | Cenários sugeridos adotados | P1 |
| [RP-006](#rp-006) | Código testável, entradas validadas e recursos limitados | Recomendação técnica | P1 |
| [RP-007](#rp-007) | Adotar formato LNCS para o artigo | Recomendação adotada | P2 |
| [RP-008](#rp-008) | Documentar comandos, logs e operação reproduzível | Recomendação técnica | P1 |

## Requisitos funcionais


<a id="rf-001"></a>
### RF-001 — Executar nós como aplicações em máquinas IP

- Natureza: Obrigatório explícito.
- Origem: p. 2, mínimo, ponto 1 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: nenhuma.
- Fases de implementação/validação: PHASE-01, PHASE-02; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-008](tasks/PHASE-02/TASK-008.md).
- Validação: V-003, V-008 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Iniciar manualmente servidor, relay e cliente em processos independentes; cada um escuta no endpoint configurado e reporta o seu papel.
- Estado inicial: TODO.


<a id="rf-002"></a>
### RF-002 — Entrada e saída dinâmica de nós

- Natureza: Obrigatório explícito.
- Origem: p. 2, mínimo, ponto 2 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-001.
- Fases de implementação/validação: PHASE-02, PHASE-04; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-006](tasks/PHASE-02/TASK-006.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-008](tasks/PHASE-02/TASK-008.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-016](tasks/PHASE-04/TASK-016.md).
- Validação: V-006, V-007, V-008, V-015, V-016 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Adicionar um nó a uma overlay em execução, removê-lo com encerramento normal e abrupto, e observar atualização de vizinhos e rotas sem reinício dos restantes.
- Estado inicial: TODO.


<a id="rf-003"></a>
### RF-003 — Estabelecer e manter X vizinhos TCP ou UDP

- Natureza: Obrigatório explícito.
- Origem: p. 2, mínimo, ponto 3 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-001.
- Fases de implementação/validação: PHASE-02, PHASE-03; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-005](tasks/PHASE-02/TASK-005.md), [TASK-006](tasks/PHASE-02/TASK-006.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-010](tasks/PHASE-03/TASK-010.md).
- Validação: V-005, V-006, V-007, V-010 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Estabelecer uma ligação lógica bidirecional por vizinho permitido disponível; repetir tentativas para vizinhos indisponíveis e mostrar grau desejado/atual. X é configurável por nó e limitado pela topologia, não fixado em três.
- Estado inicial: TODO.


<a id="rf-004"></a>
### RF-004 — Descobrir vizinhos por contacto conhecido

- Natureza: Obrigatório explícito.
- Origem: p. 2, mínimo, ponto 4 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-001.
- Fases de implementação/validação: PHASE-01, PHASE-02; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-006](tasks/PHASE-02/TASK-006.md), [TASK-007](tasks/PHASE-02/TASK-007.md).
- Validação: V-003, V-006, V-007 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Um nó iniciado apenas com configuração local e contacto bootstrap recebe os endpoints dos vizinhos permitidos, sem precisar de os fornecer na CLI.
- Estado inicial: TODO.


<a id="rf-005"></a>
### RF-005 — Manter tabela por fluxo com custo, origem, destinos e estado

- Natureza: Obrigatório explícito.
- Origem: p. 2, mínimo, ponto 5; p. 3, múltiplas streams do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-003.
- Fases de implementação/validação: PHASE-03, PHASE-04; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md).
- Validação: V-011, V-012, V-014, V-015 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Expor snapshot de cada StreamKey com custo, upstream, conjunto de downstream e estado; mudanças num fluxo não alteram os restantes. Não limitar o modelo a uma única stream.
- Estado inicial: TODO.


<a id="rf-006"></a>
### RF-006 — Suportar caminhos alternativos

- Natureza: Obrigatório explícito.
- Origem: p. 2, mínimo, ponto 6 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-005.
- Fases de implementação/validação: PHASE-03, PHASE-04, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-016](tasks/PHASE-04/TASK-016.md), [TASK-026](tasks/PHASE-06/TASK-026.md).
- Validação: V-011, V-012, V-016, V-026 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Numa topologia com dois caminhos overlay até à mesma fonte, guardar alternativa válida e restabelecer receção após falha do upstream, quando a conectividade o permite.
- Estado inicial: TODO.


<a id="rf-007"></a>
### RF-007 — Servir vários clientes em simultâneo

- Natureza: Obrigatório explícito.
- Origem: p. 2, mínimo, ponto 6; objetivos p. 1 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-005.
- Fases de implementação/validação: PHASE-04, PHASE-05, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-027](tasks/PHASE-06/TASK-027.md).
- Validação: V-014, V-015, V-021, V-024, V-027 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Dois clientes consomem a mesma stream e clientes diferentes consomem streams distintas sem interferência; parar um cliente não interrompe o outro.
- Estado inicial: TODO.


<a id="rf-008"></a>
### RF-008 — Monitorizar a rede overlay

- Natureza: Obrigatório explícito.
- Origem: p. 2, mínimo, ponto 7 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-003.
- Fases de implementação/validação: PHASE-03, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-010](tasks/PHASE-03/TASK-010.md), [TASK-028](tasks/PHASE-06/TASK-028.md).
- Validação: V-010, V-028 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Medir RTT por vizinho com sondas correlacionadas, detetar falta de resposta e disponibilizar métricas datadas; não apresentar RTT TCP como perda de vídeo ou largura de banda disponível.
- Estado inicial: TODO.


<a id="rf-009"></a>
### RF-009 — Atualizar rotas perante entradas, saídas e condições de rede

- Natureza: Obrigatório explícito.
- Origem: p. 2, mínimo, ponto 7 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-005, RF-008.
- Fases de implementação/validação: PHASE-03, PHASE-04, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-016](tasks/PHASE-04/TASK-016.md), [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-026](tasks/PHASE-06/TASK-026.md).
- Validação: V-012, V-015, V-016, V-017, V-026 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Demonstrar recuperação após falha e regresso de relay, expiração de anúncios e troca de rota por aumento sustentado de RTT; partição produz estado UNAVAILABLE.
- Estado inicial: TODO.


<a id="rf-010"></a>
### RF-010 — Reencaminhar dados segundo a tabela

- Natureza: Obrigatório explícito.
- Origem: p. 3, mínimo, ponto 8 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-005.
- Fases de implementação/validação: PHASE-03, PHASE-05; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-013](tasks/PHASE-03/TASK-013.md), [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-021](tasks/PHASE-05/TASK-021.md).
- Validação: V-013, V-019, V-021 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Aceitar dados apenas do upstream ativo da stream e época corretos; enviar apenas para downstream ativos, uma vez por destino; stream desconhecida/inativa é descartada.
- Estado inicial: TODO.


<a id="rf-011"></a>
### RF-011 — Partilhar segmentos comuns e replicar nos pontos de ramificação

- Natureza: Obrigatório pelo comportamento descrito.
- Origem: p. 2, exemplo O1–O2–O3/O7 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-007, RF-010.
- Fases de implementação/validação: PHASE-04, PHASE-05, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-028](tasks/PHASE-06/TASK-028.md).
- Validação: V-014, V-015, V-021, V-025, V-028 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Em estado estável, dois clientes a jusante partilham uma única cópia de cada datagrama no segmento comum; a prova combina contadores e captura por ligação lógica.
- Estado inicial: TODO.


<a id="rf-012"></a>
### RF-012 — Disponibilizar pelo menos dois servidores de conteúdos

- Natureza: Obrigatório explícito.
- Origem: p. 3, mínimo, ponto 9 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-001.
- Fases de implementação/validação: PHASE-01, PHASE-03, PHASE-05, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-027](tasks/PHASE-06/TASK-027.md).
- Validação: V-003, V-012, V-020, V-024, V-027 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Executar dois processos de origem, em nós de teste identificados, com catálogos próprios e anúncios simultâneos; bootstrap não conta como servidor de vídeo.
- Estado inicial: TODO.


<a id="rf-013"></a>
### RF-013 — Cada servidor oferece pelo menos dois vídeos distintos

- Natureza: Obrigatório explícito.
- Origem: p. 3, mínimo, ponto 9 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-012.
- Fases de implementação/validação: PHASE-01, PHASE-05, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-027](tasks/PHASE-06/TASK-027.md).
- Validação: V-003, V-020, V-024, V-027 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Configurar quatro UUID e pelo menos dois conteúdos visualmente distintos em cada servidor; registar origem, ficheiro, hash e evidência de reprodução. Aliases do mesmo ficheiro não bastam.
- Estado inicial: TODO.


<a id="rf-014"></a>
### RF-014 — Usar métricas de rede para construir rotas

- Natureza: Obrigatório explícito.
- Origem: p. 3, mínimo, ponto 10; p. 2, métricas exemplificadas do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-008.
- Fases de implementação/validação: PHASE-03, PHASE-04, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-010](tasks/PHASE-03/TASK-010.md), [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-026](tasks/PHASE-06/TASK-026.md).
- Validação: V-010, V-011, V-012, V-017, V-026 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: O custo é calculado de RTT medido e acumulado; alterar o atraso de um caminho pode alterar a seleção, após histerese documentada; saltos só desempata.
- Estado inicial: TODO.


<a id="rf-015"></a>
### RF-015 — Entregar e reproduzir vídeo com objetivo de tempo real

- Natureza: Obrigatório explícito.
- Origem: p. 1, objetivos; p. 3, dois fluxos de vídeo; p. 4, etapa 4 sugerida do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-010, RF-013.
- Fases de implementação/validação: PHASE-05; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-023](tasks/PHASE-05/TASK-023.md), [TASK-024](tasks/PHASE-05/TASK-024.md).
- Validação: V-019, V-020, V-021, V-022, V-023, V-024 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Receber vídeo identificável dos quatro fluxos e reproduzi-lo continuamente em cliente; demonstrar pacing, buffers limitados e descarte de frames atrasados. Limiares numéricos são metas técnicas, não promessas do PDF.
- Estado inicial: TODO.


## Requisitos não funcionais


<a id="rnf-001"></a>
### RNF-001 — Atraso baixo e utilização eficiente de largura de banda

- Natureza: Obrigatório qualitativo.
- Origem: p. 1, objetivos e motivação do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-011, RF-014.
- Fases de implementação/validação: PHASE-04, PHASE-05, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-028](tasks/PHASE-06/TASK-028.md).
- Validação: V-017, V-019, V-020, V-021, V-022, V-028 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Medir RTT, FPS, interrupção, overhead e tráfego por segmento; comparar duas condições de rede e discutir limites. Não declarar garantia absoluta de ausência de perdas.
- Estado inicial: TODO.


<a id="rnf-002"></a>
### RNF-002 — Escalabilidade para N clientes

- Natureza: Obrigatório qualitativo.
- Origem: p. 1, objetivos; p. 2, crítica à repetição unicast do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-007, RF-011.
- Fases de implementação/validação: PHASE-05, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-027](tasks/PHASE-06/TASK-027.md), [TASK-028](tasks/PHASE-06/TASK-028.md).
- Validação: V-021, V-027, V-028 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Comparar 1, 2 e 5 clientes na topologia complexa, com capacidade disponível, verificando que o tronco comum não cresce proporcionalmente ao número de clientes da mesma stream.
- Estado inicial: TODO.


<a id="rnf-003"></a>
### RNF-003 — Fiabilidade sob alterações e falhas

- Natureza: Obrigatório qualitativo.
- Origem: p. 2, mínimo, ponto 7 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RF-009.
- Fases de implementação/validação: PHASE-02, PHASE-04, PHASE-05, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-008](tasks/PHASE-02/TASK-008.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-016](tasks/PHASE-04/TASK-016.md), [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-026](tasks/PHASE-06/TASK-026.md).
- Validação: V-008, V-015, V-016, V-017, V-022, V-026 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Não bloquear permanentemente após falha de nó; recuperar com alternativa ou apresentar indisponibilidade; reconexão não mantém subscrições fantasma indefinidas.
- Estado inicial: TODO.


<a id="rnf-004"></a>
### RNF-004 — Overlay aplicacional sobre transporte IP

- Natureza: Obrigatório explícito.
- Origem: p. 2, mapeamento overlay/underlay e mínimo ponto 3 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: nenhuma.
- Fases de implementação/validação: PHASE-02, PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-005](tasks/PHASE-02/TASK-005.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-025](tasks/PHASE-06/TASK-025.md).
- Validação: V-005, V-007, V-025 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Os links overlay são sockets TCP/UDP entre processos; não usar multicast IP para substituir replicação aplicacional; distinguir caminho overlay de rota IP.
- Estado inicial: TODO.


<a id="rnf-005"></a>
### RNF-005 — Especificação, implementação e avaliação crítica verificáveis

- Natureza: Obrigatório de avaliação.
- Origem: p. 4, subitens a, b, c dos itens 1–5 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: nenhuma.
- Fases de implementação/validação: PHASE-01, PHASE-02, PHASE-04, PHASE-06, PHASE-07; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-002](tasks/PHASE-01/TASK-002.md), [TASK-004](tasks/PHASE-01/TASK-004.md), [TASK-009](tasks/PHASE-02/TASK-009.md), [TASK-018](tasks/PHASE-04/TASK-018.md), [TASK-028](tasks/PHASE-06/TASK-028.md), [TASK-029](tasks/PHASE-07/TASK-029.md), [TASK-031](tasks/PHASE-07/TASK-031.md).
- Validação: V-002, V-004, V-009, V-018, V-028, V-029, V-031 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Ligar desenho e contratos a implementação futura, testes e apreciação crítica; incluir evidências e limitações, sem resultados inventados.
- Estado atual: IN PROGRESS — TASK-002 DONE; [V-002 e fixtures](../experiments/task-002-contract-review.md) concluídas. Implementação, testes de comportamento e apreciação crítica ainda pendentes.


## Restrições obrigatórias de teste e entrega


<a id="rc-001"></a>
### RC-001 — Usar CORE como bancada principal

- Natureza: Obrigatório explícito.
- Origem: p. 1, objetivos; p. 5, cenários do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: nenhuma.
- Fases de implementação/validação: PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-026](tasks/PHASE-06/TASK-026.md), [TASK-027](tasks/PHASE-06/TASK-027.md), [TASK-028](tasks/PHASE-06/TASK-028.md).
- Validação: V-025, V-026, V-027, V-028 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Validar solução integrada no CORE em cenários incrementalmente representativos, com ficheiros, endereços, processos e condições reproduzíveis.
- Estado inicial: TODO.


<a id="rc-002"></a>
### RC-002 — Relatório em formato de artigo

- Natureza: Obrigatório explícito.
- Origem: p. 5, Relatório do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: nenhuma.
- Fases de implementação/validação: PHASE-07; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-029](tasks/PHASE-07/TASK-029.md).
- Validação: V-029 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Produzir PDF em formato de artigo que descreva desenho, protocolos, implementação, testes, conclusões e crítica.
- Estado inicial: TODO.


<a id="rc-003"></a>
### RC-003 — Relatório com máximo de 12 páginas

- Natureza: Obrigatório explícito.
- Origem: p. 5, Relatório do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RC-002.
- Fases de implementação/validação: PHASE-07; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-029](tasks/PHASE-07/TASK-029.md), [TASK-030](tasks/PHASE-07/TASK-030.md).
- Validação: V-029, V-030 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Contar todas as páginas do PDF final, incluindo referências e anexos incorporados; total até 12, salvo esclarecimento docente expresso.
- Estado inicial: TODO.


<a id="rc-004"></a>
### RC-004 — Arquivo de código ZIP ou RAR

- Natureza: Obrigatório explícito.
- Origem: p. 5, Submissão do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: nenhuma.
- Fases de implementação/validação: PHASE-07; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-030](tasks/PHASE-07/TASK-030.md).
- Validação: V-030 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Gerar ZIP, opção proposta, que extrai e permite repetir build/testes com instruções; não omitir ficheiros/configurações necessários.
- Estado inicial: TODO.


<a id="rc-005"></a>
### RC-005 — Nomes de entrega com turno e grupo

- Natureza: Obrigatório explícito.
- Origem: p. 5, Submissão do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RC-002, RC-004.
- Fases de implementação/validação: PHASE-07; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-030](tasks/PHASE-07/TASK-030.md), [TASK-031](tasks/PHASE-07/TASK-031.md).
- Validação: V-030, V-031 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Usar ESR-TP2-PLx-Gy-Rel.pdf e ESR-TP2-PLx-Gy-Codigo.zip; obter x/y reais antes da entrega, sem os inventar.
- Estado inicial: TODO.


<a id="rc-006"></a>
### RC-006 — Submeter na troca de ficheiros do grupo Blackboard

- Natureza: Obrigatório explícito.
- Origem: p. 5, Submissão do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: RC-005.
- Fases de implementação/validação: PHASE-07; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-031](tasks/PHASE-07/TASK-031.md).
- Validação: V-031 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: A equipa submete os dois artefactos no grupo correto em elearning.uminho.pt e guarda confirmação; documentação não equivale a upload.
- Estado inicial: TODO.


<a id="rc-007"></a>
### RC-007 — Preparar duas avaliações intermédias e defesa final

- Natureza: Obrigatório de avaliação.
- Origem: p. 4, itens 5 e 6, pesos 30% e 5%+5% do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P0.
- Dependências de requisitos: nenhuma.
- Fases de implementação/validação: PHASE-02, PHASE-04, PHASE-07; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-009](tasks/PHASE-02/TASK-009.md), [TASK-018](tasks/PHASE-04/TASK-018.md), [TASK-031](tasks/PHASE-07/TASK-031.md).
- Validação: V-009, V-018, V-031 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Preparar duas demonstrações intermédias reproduzíveis e defesa final, alinhando datas e conteúdo com o docente antes das sessões.
- Estado inicial: TODO.


## Recomendações e decisões técnicas adotadas


<a id="rp-001"></a>
### RP-001 — Catálogo e seleção explícita de stream

- Natureza: Decisão técnica proposta.
- Origem: derivado de RF-012/RF-013, não API exigida do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P1.
- Dependências de requisitos: RF-012, RF-013.
- Fases de implementação/validação: PHASE-01, PHASE-03, PHASE-05; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-023](tasks/PHASE-05/TASK-023.md), [TASK-024](tasks/PHASE-05/TASK-024.md).
- Validação: V-003, V-012, V-023, V-024 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Mostrar UUID, título e origem dos anúncios ainda válidos; selecionar um fluxo sem confundir fontes.
- Estado inicial: TODO.


<a id="rp-002"></a>
### RP-002 — Ativar sob pedido e podar ramo sem recetores

- Natureza: Recomendação adotada.
- Origem: p. 3–4, etapa 2, estratégia 1 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P1.
- Dependências de requisitos: RF-005.
- Fases de implementação/validação: PHASE-04; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md).
- Validação: V-014, V-015 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Sem subscritores não há vídeo no ramo; dois clientes partilham uma ativação a montante; saída do último remove o ramo, por cancelamento ou expiração de lease.
- Estado inicial: TODO.


<a id="rp-003"></a>
### RP-003 — MJPEG, pacotes numerados e janela de reprodução

- Natureza: Estratégia sugerida adotada.
- Origem: p. 4, etapa 4, estratégia 1 e referência 2 do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P1.
- Dependências de requisitos: RF-015.
- Fases de implementação/validação: PHASE-05; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-023](tasks/PHASE-05/TASK-023.md).
- Validação: V-019, V-020, V-022, V-023 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Validar o formato real de movie.Mjpeg antes de escrever o leitor; numerar frames, fragmentar/reconstituir e mostrar imagens numa janela. Pacote proposto não é declarado RTP.
- Estado inicial: TODO.


<a id="rp-004"></a>
### RP-004 — Diagnóstico do percurso antes do vídeo

- Natureza: Recomendação adotada.
- Origem: p. 4, etapa 3, PING e logs do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P1.
- Dependências de requisitos: RF-005.
- Fases de implementação/validação: PHASE-03; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-013](tasks/PHASE-03/TASK-013.md).
- Validação: V-013 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Usar ROUTE_TRACE sintético e logs para comprovar o percurso selecionado sem depender da GUI ou de ficheiros de vídeo.
- Estado inicial: TODO.


<a id="rp-005"></a>
### RP-005 — Testar cenários de 4, 6 e overlay complexa

- Natureza: Cenários sugeridos adotados.
- Origem: p. 5, Cenários de teste do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P1.
- Dependências de requisitos: RC-001.
- Fases de implementação/validação: PHASE-06; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-026](tasks/PHASE-06/TASK-026.md), [TASK-027](tasks/PHASE-06/TASK-027.md).
- Validação: V-025, V-026, V-027 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Demonstrar 4 e 6 processos overlay nos cenários simples e uma overlay complexa inspirada na figura 1, com extensão explícita para duas fontes.
- Estado inicial: TODO.


<a id="rp-006"></a>
### RP-006 — Código testável, entradas validadas e recursos limitados

- Natureza: Recomendação técnica.
- Origem: decisão deste plano, não limiar imposto no PDF do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P1.
- Dependências de requisitos: nenhuma.
- Fases de implementação/validação: PHASE-01, PHASE-02, PHASE-03, PHASE-04, PHASE-05; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-001](tasks/PHASE-01/TASK-001.md), [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-004](tasks/PHASE-01/TASK-004.md), [TASK-005](tasks/PHASE-02/TASK-005.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-010](tasks/PHASE-03/TASK-010.md), [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-022](tasks/PHASE-05/TASK-022.md).
- Validação: V-001, V-003, V-004, V-005, V-007, V-010, V-011, V-014, V-019, V-021, V-022 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Validar limites antes de alocar; injetar relógio/transportes nos testes; filas e caches têm máximo e política de overflow; não aceitar objetos Java serializados da rede.
- Estado atual: IN PROGRESS — TASK-001 DONE; V-001 concluída com [evidência](../experiments/build-baseline.md), sem testes funcionais. As restantes tarefas deste requisito estão pendentes.


<a id="rp-007"></a>
### RP-007 — Adotar formato LNCS para o artigo

- Natureza: Recomendação adotada.
- Origem: p. 5, recomenda-se LNCS do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P2.
- Dependências de requisitos: RC-002.
- Fases de implementação/validação: PHASE-07; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-029](tasks/PHASE-07/TASK-029.md).
- Validação: V-029 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Usar template LNCS obtido de fonte oficial compatível; distinguir recomendação de formato de artigo obrigatório.
- Estado inicial: TODO.


<a id="rp-008"></a>
### RP-008 — Documentar comandos, logs e operação reproduzível

- Natureza: Recomendação técnica.
- Origem: derivado de avaliação de testes e defesa do [enunciado](../ESR-TP2-26-27.pdf), quando aplicável; decisões derivadas estão assinaladas.
- Prioridade: P1.
- Dependências de requisitos: nenhuma.
- Fases de implementação/validação: PHASE-02, PHASE-03, PHASE-05, PHASE-06, PHASE-07; o requisito só está concluído após a última tarefa e validação relacionada.
- Tarefas: [TASK-008](tasks/PHASE-02/TASK-008.md), [TASK-013](tasks/PHASE-03/TASK-013.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-028](tasks/PHASE-06/TASK-028.md), [TASK-030](tasks/PHASE-07/TASK-030.md).
- Validação: V-008, V-013, V-024, V-025, V-028, V-030 em [09](09-TESTING-STRATEGY.md).
- Critério de aceitação: Guardar passos, versões, configuração e evidência correlacionada por stream e nó; validar instruções numa extração limpa do pacote.
- Estado inicial: TODO.


## Cobertura de qualidade não imposta

Segurança e manutenção são tratadas por RP-006 e [10](10-SECURITY.md). Usabilidade proposta é seleção simples, indicação de indisponibilidade e fecho da janela; não há avaliação UX numérica. Compatibilidade aplica-se ao Java 21 escolhido e ao CORE exigido. Não se inventa obrigação de Windows, cloud, base de dados ou serviço em produção. Escalabilidade e performance exigidas são qualitativas; metas propostas e experiências estão separadas em [09](09-TESTING-STRATEGY.md).

## Restrições contextuais

Duração indicada: oito aulas / nove semanas (p. 1). Tratar como limite de organização, sem inventar datas ou duração por tarefa. Pesos de avaliação: overlay 10%, gestão de fluxos 25%, controlo 15%, streaming 10%, integração final 30%, dois marcos 5% cada (p. 4). Qualidade conceptual, da implementação futura e dos testes/apreciação crítica é transversal; relatório e defesa integram a avaliação final.

Todas as obrigações têm tarefa e oráculo em [13](13-REQUIREMENTS-TRACEABILITY.md). Nenhuma recomendação dispensa um requisito explícito. O registo de decisões resolve hipóteses e distingue gates de implementação de bloqueios ao próprio planeamento.
