# Rastreabilidade de requisitos

> Arquitetura normativa: [add.md](../add.md). Requisitos consolidados: [specs.md](../specs.md). Este documento mantém detalhe técnico/proveniência e deve ser interpretado pela precedência do ADD; não é uma autoridade arquitetural concorrente.

Estado inicial do plano: TODO para todos os requisitos. Estado atual: RF-001/003/004/012/013, RP-001/006 e RNF-004/005 em IN PROGRESS; os restantes requisitos continuam TODO. PHASE-01/TASK-001–004 e TASK-005 estão DONE com configuração, codec e TCP testados; overlay completa/streaming pendentes. Cobertura planeada não equivale a cumprimento. Os detalhes e fontes estão em [02](02-REQUIREMENTS.md); V-XXX é definido em [09](09-TESTING-STRATEGY.md). Todas as obrigações e recomendações adotadas têm tarefas concretas e um oráculo.

Um requisito só passa a DONE quando todas as tarefas necessárias e os oráculos relevantes estiverem concluídos. Pode passar a IN PROGRESS com evidência parcial ou BLOCKED com causa concreta; nunca usar DONE por a documentação estar escrita. Registar ligações para evidências na coluna final durante a implementação.

| ID | Descrição | Componente responsável | Fases | Tarefas | Testes / verificações | Estado | Evidência |
|---|---|---|---|---|---|---|---|
| [RF-001](02-REQUIREMENTS.md#rf-001) | Executar nós como aplicações em máquinas IP | app/config/overlay | 01, 02 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-008](tasks/PHASE-02/TASK-008.md) | V-003, V-008 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); [V-008](../memory-bank/bolts/020-overlay/test-walkthrough.md); [V-009: CLI em quatro JVMs](../memory-bank/bolts/004-overlay/test-walkthrough.md) |
| [RF-002](02-REQUIREMENTS.md#rf-002) | Entrada e saída dinâmica de nós | bootstrap/overlay/routing | 02, 04 | [TASK-006](tasks/PHASE-02/TASK-006.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-008](tasks/PHASE-02/TASK-008.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-016](tasks/PHASE-04/TASK-016.md) | V-006, V-007, V-008, V-015, V-016 | TODO | [V-008](../memory-bank/bolts/020-overlay/test-walkthrough.md); cobertura parcial, TASK-008 DONE |
| [RF-003](02-REQUIREMENTS.md#rf-003) | Estabelecer e manter X vizinhos TCP ou UDP | transport/overlay/monitoring | 02, 03 | [TASK-005](tasks/PHASE-02/TASK-005.md), [TASK-006](tasks/PHASE-02/TASK-006.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-010](tasks/PHASE-03/TASK-010.md) | V-005, V-006, V-007, V-010 | IN PROGRESS | [V-005: TCP real, 18 testes](../experiments/task-005-transport-validation.md); [V-006](../memory-bank/bolts/002-overlay/ddd-03-test-report.md), [V-007](../memory-bank/bolts/003-overlay/ddd-03-test-report.md); V-010 pendente |
| [RF-004](02-REQUIREMENTS.md#rf-004) | Descobrir vizinhos por contacto conhecido | config/bootstrap/overlay | 01, 02 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-006](tasks/PHASE-02/TASK-006.md), [TASK-007](tasks/PHASE-02/TASK-007.md) | V-003, V-006, V-007 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); [V-006](../memory-bank/bolts/002-overlay/ddd-03-test-report.md) e [V-007](../memory-bank/bolts/003-overlay/ddd-03-test-report.md) comprovam descoberta e vizinhança locais |
| [RF-005](02-REQUIREMENTS.md#rf-005) | Manter tabela por fluxo com custo, origem, destinos e estado | routing | 03, 04 | [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md) | V-011, V-012, V-014, V-015 | IN PROGRESS | [V-011/IMP-008 parcial](../memory-bank/bolts/021-rotas/ddd-03-test-report.md); routing/árvores restantes pendentes |
| [RF-006](02-REQUIREMENTS.md#rf-006) | Suportar caminhos alternativos | routing/CORE | 03, 04, 06 | [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-016](tasks/PHASE-04/TASK-016.md), [TASK-026](tasks/PHASE-06/TASK-026.md) | V-011, V-012, V-016, V-026 | IN PROGRESS | [V-011/IMP-008 parcial](../memory-bank/bolts/021-rotas/ddd-03-test-report.md); routing/árvores restantes pendentes |
| [RF-007](02-REQUIREMENTS.md#rf-007) | Servir vários clientes em simultâneo | routing/streaming/client | 04, 05, 06 | [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-027](tasks/PHASE-06/TASK-027.md) | V-014, V-015, V-021, V-024, V-027 | TODO | — |
| [RF-008](02-REQUIREMENTS.md#rf-008) | Monitorizar a rede overlay | monitoring/experiments | 03, 06 | [TASK-010](tasks/PHASE-03/TASK-010.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-010, V-028 | TODO | — |
| [RF-009](02-REQUIREMENTS.md#rf-009) | Atualizar rotas perante entradas, saídas e condições de rede | routing/monitoring | 03, 04, 06 | [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-016](tasks/PHASE-04/TASK-016.md), [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-026](tasks/PHASE-06/TASK-026.md) | V-012, V-015, V-016, V-017, V-026 | TODO | — |
| [RF-010](02-REQUIREMENTS.md#rf-010) | Reencaminhar dados segundo a tabela | routing/streaming | 03, 05 | [TASK-013](tasks/PHASE-03/TASK-013.md), [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-021](tasks/PHASE-05/TASK-021.md) | V-013, V-019, V-021 | TODO | — |
| [RF-011](02-REQUIREMENTS.md#rf-011) | Partilhar segmentos comuns e replicar nos pontos de ramificação | routing/streaming/experiments | 04, 05, 06 | [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-014, V-015, V-021, V-025, V-028 | TODO | — |
| [RF-012](02-REQUIREMENTS.md#rf-012) | Disponibilizar pelo menos dois servidores de conteúdos | config/server/app | 01, 03, 05, 06 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-027](tasks/PHASE-06/TASK-027.md) | V-003, V-012, V-020, V-024, V-027 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); restantes componentes pendentes |
| [RF-013](02-REQUIREMENTS.md#rf-013) | Cada servidor oferece pelo menos dois vídeos distintos | config/server/media | 01, 05, 06 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-027](tasks/PHASE-06/TASK-027.md) | V-003, V-020, V-024, V-027 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); restantes componentes pendentes |
| [RF-014](02-REQUIREMENTS.md#rf-014) | Usar métricas de rede para construir rotas | monitoring/routing | 03, 04, 06 | [TASK-010](tasks/PHASE-03/TASK-010.md), [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-026](tasks/PHASE-06/TASK-026.md) | V-010, V-011, V-012, V-017, V-026 | IN PROGRESS | [V-011/IMP-008 parcial](../memory-bank/bolts/021-rotas/ddd-03-test-report.md); routing/árvores restantes pendentes |
| [RF-015](02-REQUIREMENTS.md#rf-015) | Entregar e reproduzir vídeo com objetivo de tempo real | server/streaming/client | 05 | [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-023](tasks/PHASE-05/TASK-023.md), [TASK-024](tasks/PHASE-05/TASK-024.md) | V-019, V-020, V-021, V-022, V-023, V-024 | TODO | — |
| [RNF-001](02-REQUIREMENTS.md#rnf-001) | Atraso baixo e utilização eficiente de largura de banda | routing/streaming/experiments | 04, 05, 06 | [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-017, V-019, V-020, V-021, V-022, V-028 | TODO | — |
| [RNF-002](02-REQUIREMENTS.md#rnf-002) | Escalabilidade para N clientes | streaming/CORE/experiments | 05, 06 | [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-027](tasks/PHASE-06/TASK-027.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-021, V-027, V-028 | TODO | — |
| [RNF-003](02-REQUIREMENTS.md#rnf-003) | Fiabilidade sob alterações e falhas | overlay/routing/client | 02, 04, 05, 06 | [TASK-008](tasks/PHASE-02/TASK-008.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-016](tasks/PHASE-04/TASK-016.md), [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-026](tasks/PHASE-06/TASK-026.md) | V-008, V-015, V-016, V-017, V-022, V-026 | TODO | [V-008](../memory-bank/bolts/020-overlay/test-walkthrough.md); cobertura parcial, TASK-008 DONE |
| [RNF-004](02-REQUIREMENTS.md#rnf-004) | Overlay aplicacional sobre transporte IP | transport/overlay/CORE | 02, 06 | [TASK-005](tasks/PHASE-02/TASK-005.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-025](tasks/PHASE-06/TASK-025.md) | V-005, V-007, V-025 | IN PROGRESS | [V-005: TCP real, 18 testes](../experiments/task-005-transport-validation.md); [V-007](../memory-bank/bolts/003-overlay/ddd-03-test-report.md); V-025 em CORE pendente |
| [RNF-005](02-REQUIREMENTS.md#rnf-005) | Especificação, implementação e avaliação crítica verificáveis | protocol/tests/report | 01, 02, 04, 06, 07 | [TASK-002](tasks/PHASE-01/TASK-002.md), [TASK-004](tasks/PHASE-01/TASK-004.md), [TASK-009](tasks/PHASE-02/TASK-009.md), [TASK-018](tasks/PHASE-04/TASK-018.md), [TASK-028](tasks/PHASE-06/TASK-028.md), [TASK-029](tasks/PHASE-07/TASK-029.md), [TASK-031](tasks/PHASE-07/TASK-031.md) | V-002, V-004, V-009, V-018, V-028, V-029, V-031 | IN PROGRESS | [V-002/V-004: especificação e codec](../experiments/task-004-codec-validation.md); [V-009: primeiro marco técnico](../memory-bank/bolts/004-overlay/test-walkthrough.md); validações posteriores e relatório final pendentes |
| [RC-001](02-REQUIREMENTS.md#rc-001) | Usar CORE como bancada principal | CORE/experiments | 06 | [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-026](tasks/PHASE-06/TASK-026.md), [TASK-027](tasks/PHASE-06/TASK-027.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-025, V-026, V-027, V-028 | TODO | — |
| [RC-002](02-REQUIREMENTS.md#rc-002) | Relatório em formato de artigo | report | 07 | [TASK-029](tasks/PHASE-07/TASK-029.md) | V-029 | TODO | — |
| [RC-003](02-REQUIREMENTS.md#rc-003) | Relatório com máximo de 12 páginas | report/delivery | 07 | [TASK-029](tasks/PHASE-07/TASK-029.md), [TASK-030](tasks/PHASE-07/TASK-030.md) | V-029, V-030 | TODO | — |
| [RC-004](02-REQUIREMENTS.md#rc-004) | Arquivo de código ZIP ou RAR | delivery | 07 | [TASK-030](tasks/PHASE-07/TASK-030.md) | V-030 | TODO | — |
| [RC-005](02-REQUIREMENTS.md#rc-005) | Nomes de entrega com turno e grupo | delivery | 07 | [TASK-030](tasks/PHASE-07/TASK-030.md), [TASK-031](tasks/PHASE-07/TASK-031.md) | V-030, V-031 | TODO | — |
| [RC-006](02-REQUIREMENTS.md#rc-006) | Submeter na troca de ficheiros do grupo Blackboard | equipa/Blackboard | 07 | [TASK-031](tasks/PHASE-07/TASK-031.md) | V-031 | TODO | — |
| [RC-007](02-REQUIREMENTS.md#rc-007) | Preparar duas avaliações intermédias e defesa final | experiments/report/equipa | 02, 04, 07 | [TASK-009](tasks/PHASE-02/TASK-009.md), [TASK-018](tasks/PHASE-04/TASK-018.md), [TASK-031](tasks/PHASE-07/TASK-031.md) | V-009, V-018, V-031 | IN PROGRESS | [V-009: primeiro marco técnico](../memory-bank/bolts/004-overlay/test-walkthrough.md). Data e conteúdo oficiais pendentes |
| [RP-001](02-REQUIREMENTS.md#rp-001) | Catálogo e seleção explícita de stream | streaming/client | 01, 03, 05 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-023](tasks/PHASE-05/TASK-023.md), [TASK-024](tasks/PHASE-05/TASK-024.md) | V-003, V-012, V-023, V-024 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); restantes componentes pendentes |
| [RP-002](02-REQUIREMENTS.md#rp-002) | Ativar sob pedido e podar ramo sem recetores | routing | 04 | [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md) | V-014, V-015 | TODO | — |
| [RP-003](02-REQUIREMENTS.md#rp-003) | MJPEG, pacotes numerados e janela de reprodução | server/streaming/client | 05 | [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-023](tasks/PHASE-05/TASK-023.md) | V-019, V-020, V-022, V-023 | TODO | — |
| [RP-004](02-REQUIREMENTS.md#rp-004) | Diagnóstico do percurso antes do vídeo | routing/app | 03 | [TASK-013](tasks/PHASE-03/TASK-013.md) | V-013 | TODO | — |
| [RP-005](02-REQUIREMENTS.md#rp-005) | Testar cenários de 4, 6 e overlay complexa | CORE/experiments | 06 | [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-026](tasks/PHASE-06/TASK-026.md), [TASK-027](tasks/PHASE-06/TASK-027.md) | V-025, V-026, V-027 | TODO | — |
| [RP-006](02-REQUIREMENTS.md#rp-006) | Código testável, entradas validadas e recursos limitados | config/protocol/transport/routing/client | 01, 02, 03, 04, 05 | [TASK-001](tasks/PHASE-01/TASK-001.md), [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-004](tasks/PHASE-01/TASK-004.md), [TASK-005](tasks/PHASE-02/TASK-005.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-010](tasks/PHASE-03/TASK-010.md), [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-022](tasks/PHASE-05/TASK-022.md) | V-001, V-003, V-004, V-005, V-007, V-010, V-011, V-014, V-019, V-021, V-022 | IN PROGRESS | [V-003/V-004: configuração e codec](../experiments/task-004-codec-validation.md); [V-005: filas, sockets e shutdown](../experiments/task-005-transport-validation.md); restantes componentes pendentes |
| [RP-007](02-REQUIREMENTS.md#rp-007) | Adotar formato LNCS para o artigo | report | 07 | [TASK-029](tasks/PHASE-07/TASK-029.md) | V-029 | TODO | — |
| [RP-008](02-REQUIREMENTS.md#rp-008) | Documentar comandos, logs e operação reproduzível | app/experiments/delivery | 02, 03, 05, 06, 07 | [TASK-008](tasks/PHASE-02/TASK-008.md), [TASK-013](tasks/PHASE-03/TASK-013.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-028](tasks/PHASE-06/TASK-028.md), [TASK-030](tasks/PHASE-07/TASK-030.md) | V-008, V-013, V-024, V-025, V-028, V-030 | TODO | [V-008](../memory-bank/bolts/020-overlay/test-walkthrough.md); cobertura parcial, TASK-008 DONE |

## Interpretação dos estados

Correção ADR-013 por instrução do utilizador: a parte bootstrap de RF-004/TASK-006 é apenas consulta inicial de localizações configuradas. Entrada/saída e unicidade de vizinhos em RF-002/003 continuam responsabilidade da overlay, não de registo central. V-006/007/009 foram alinhadas para consultas sem estado e reconexão sem bootstrap. [Codec e fixtures revalidados](../experiments/bootstrap-discovery-correction.md); nenhum requisito global é promovido a DONE por esta correção.

RF/RNF/RC são obrigações quando assim identificadas em 02; RP são escolhas/recomendações adotadas. Uma recomendação só pode ser retirada por decisão explícita sem perder cobertura de obrigações. Nenhuma tarefa fica órfã: a coluna de requisitos de cada tarefa permite navegar no sentido inverso. As verificações de entrega V-029–031 usam inspeção de artefactos e confirmação humana, não testes de aplicação.

Depois de alterar o plano, rever caminhos, dependências, requisitos ligados e o catálogo V-XXX. A validação estrutural inicial é descrita em [16](16-PLAN-VALIDATION.md).


## Extensões e matriz normativa do ADD

IMP-001: a verificação da base e os resultados do checkpoint 4 foram aprovados
pelo utilizador, com registo em 2026-11-03T21:40:56Z. [Validação atual](../memory-bank/bolts/001-base-validada/test-walkthrough.md):
149 testes aprovados (65 configuração, 66 codec, 18 transporte), 45 fixtures,
16 tipos ativos, V-ADD sem erros e teste manual com 22 checks. Luis executou
também o roteiro: [22 CHECK_PASS e MANUAL_PASS](../experiments/construction/001-base-validada/manual-smoke-luis.log).
[Fecho aprovado e aplicado](../memory-bank/bolts/001-base-validada/closure-walkthrough.md)
em 2026-11-03T21:56:48Z após integração em main (d65f947). Bolt/história/unidade
complete; nenhum requisito global é promovido a DONE.
Os estados e evidências legados acima conservam-se como histórico das TASK.

IMP-002/REQ-024: Test aprovado, incluindo repetição pessoal de Luis; [prova executada](../memory-bank/bolts/019-overlay/ddd-03-test-report.md),
com 30 chaves/sete grupos, erros agregados e construtor anterior preservado.
Clean verify/JDK21: 824 testes aprovados (740 configuração, 66 codec, 18 TCP real),
zero falhas/erros/skips; 45 fixtures intactas e manual do agente com 25 checks.
D01–D17/V-032 executados e Test aprovado: Luis repetiu em 2026-11-04T10:15:14Z,
25 CHECK_PASS/MANUAL_PASS, saída Java 0 confirmada; log pessoal separado do agente.
[Fecho concluído](../memory-bank/bolts/019-overlay/closure-walkthrough.md) em 2026-11-04T10:33:12Z:
TASK-032 DONE e bolt/história complete, revisão final confirmada por Luis e
versão revista integrada em main. G-REVIEW-032 cumprido; unidade e requisitos
globais mantêm os respetivos estados, sem antecipar consumers/GUI/CORE.

IMP-003/REQ-002–004: [Implement preparado para revisão](../memory-bank/bolts/002-overlay/implementation-walkthrough.md)
em 2026-11-04T12:04:53Z, com consulta ao grafo/identidade/endpoint e confirmação de escrita,
856 testes incrementais aprovados, M01–M18 mapeados e bytes/fixtures preservados.
Implement aprovado em 2026-11-04T12:18:01Z; [Test para revisão](../memory-bank/bolts/002-overlay/ddd-03-test-report.md)
com clean verify/JDK21/856 testes e manual Codex/34 checks, saídas 0. V-006/M01–M18
com execução técnica e Test aprovado, registado em 2026-11-04T14:05:14Z. Luis repetiu
pessoalmente em 2026-11-04T13:58:36Z–13:58:38Z, 34 CHECK_PASS/MANUAL_PASS/JAVA_EXIT=0,
mesmo JAR e log separado de Codex. [Fecho concluído](../memory-bank/bolts/002-overlay/closure-walkthrough.md)
em 2026-11-04T14:18:23Z, após revisão final atribuída a Waheed confirmada pelo utilizador,
registada em 2026-11-04T14:18:23Z, e versão revista 03345e9e1ab56ec04302676cca6fce32d168223d integrada em main.
TASK-006 DONE, bolt/história complete, implemented=true. Unidade mantém in-progress;
não promove requisito global nem demonstra HELLO/CLI, streaming ou CORE.
Código congelado desde Test; nenhuma execução Java nova no fecho.

Aliases REQ/NFR/CON/INT em [specs](../specs.md); a matriz completa está no
[ADD, secção 12](../add.md). REQ-024 é satisfeito pela nova TASK-032/V-032.
CON-008 formaliza a limitação UDP aprovada; CON-009 mantém a qualidade existente.
O índice derivado inclui as partes IMP e não transforma a cobertura documental
em implementação DONE.

IMP-004/REQ-002–004/021/NFR-004: [Model para revisão](../memory-bank/bolts/003-overlay/ddd-01-domain-model.md),
preparado em 2026-11-10T14:11:43Z, após seleção explícita e dependências integradas em
26aaa4ae6775e9c78e1d7658472ccaca0b376427. Estados, identidades, handshake, retry e oráculos M01–M22 definidos;
V-007 NÃO EXECUTADO, 155 ficheiros de Projeto/ preservados. TASK-007/bolt/história
em progresso, sem critério de implementação ou requisito global promovido.
Revisor final atribuído Denji e integração exigidos antes de DONE; parar antes de Design.

Revisão Model do IMP-004 em 2026-11-10T14:24:46Z: erro 17 explicitado na descoberta/handshake/
sessão ACTIVE, preservando correlação e identidade para consumidores futuros.
M02/M07/M11 ampliados; Q-C-05 pendente, sem aprovação de Model ou política nova
aceite por inferência. Sem alteração de cobertura executada: V-007 NÃO EXECUTADO.

IMP-004 em Design: Model revisto e Q-C-05 aprovados explicitamente em 2026-11-10T14:48:50Z.
[Design](../memory-bank/bolts/003-overlay/ddd-02-technical-design.md) preparado em 2026-11-10T15:00:14Z,
com D01–D22 ligados a M01–M22/V-007 e políticas ERROR aprovadas preservadas.
Retenção/DNS/dials/estado serial concretizados; nenhuma prova funcional nova,
alteração Java ou requisito global promovido. V-007 NÃO EXECUTADO; revisão final
atribuída a Denji e integração antes de DONE. Parar antes de ADR/Implement/Test.

IMP-004 em ADR: Design/Q-C-06 aprovados em 2026-11-10T15:13:50Z, Q-C-05 mantida.
[Revisão ADR](../memory-bank/bolts/003-overlay/adr-review.md) preparada em 2026-11-10T15:20:09Z: compatibilidade
no desenho com as ADR aceites e APIs/bytes atuais, sem comportamento implementado
por esta revisão. D01–D22 preservados; V-007 NÃO EXECUTADO. TASK/bolt/história
em progresso, revisão final Denji/integração pendentes; parar antes de Implement/Test.

## Evidência incremental IMP-004 — Implement

ADR explicitamente aprovada; Q-C-07 resolvida com reposição externa autorizada
de EventLoop/RealDeadlines. [Implement para checkpoint](../memory-bank/bolts/003-overlay/implementation-walkthrough.md)
preparado em 2026-11-10T16:12:36Z. Regressão incremental: `mvn -B -ntp test`,
903 testes (740 configuração +66 codec +31 TCP +19 bootstrap +47 vizinhança),
zero falhas/erros/ignorados. V-007 tem execução automatizada TCP local;
Q-C-05/Q-C-06 e D01–D22 preservados. Três ficheiros do IMP +1745/−0;
BootstrapServer externo +482/−142 separado, 151 ficheiros restantes preservados.
Implement ainda aguarda aprovação; Test/clean verify/JAR/manual não iniciados,
revisão final atribuída a Denji e integração pendentes. TASK/bolt/história/unidade
continuam em progresso, sem implemented=true, commits/push ou fecho.

## IMP-004 — Test preparado, aceitação pendente

Implement aprovado explicitamente pelo utilizador; [relatório Test](../memory-bank/bolts/003-overlay/ddd-03-test-report.md)
preparado em 2026-11-10T16:26:35Z. Clean verify atual: 903 testes (47 de vizinhança),
zero falhas/erros/ignorados. Manual interativo executado pelo agente Codex no
JAR verificado: 49 CHECK_PASS, MANUAL_PASS/CLEANUP_COMPLETE, saída Java 0.
XML/JAR/hash/perfis/log e instruções de repetição pessoal preservados em
experiments/construction/003-overlay/. Código aprovado e ferramentas intactos
durante Test. V-007/D01–D22 têm consolidação técnica local, com limites explícitos.
Q-C-08 continua aberta: V-ADD reprovado com três erros, sem alteração de ferramentas
ou redução do alcance. Test ainda não aprovado; revisão final humana e integração
pendentes. TASK/bolt/história/unidade permanecem em progresso, sem implemented=true,
commits/push/fecho ou GUI/CORE presumidos.

## Manual humano aprovado — Test ainda aberto

Registo de conferência em 2026-11-10T16:38:44Z.
Manual humano executado e aprovado por Francisco, confirmado pelo utilizador.
Conferência do log original: 49 CHECK_PASS distintos, zero CHECK_FAIL,
MANUAL_PASS CHECKS=49 e CLEANUP_COMPLETE; hash do JAR coincide com o clean verify
e os cinco hashes de configuração coincidem com os ficheiros da execução.
JAVA_EXIT=0 é confirmação explícita do utilizador; esse marcador não consta
do log original do driver, que foi preservado sem edição.
Test continua por aprovar até tratar V-ADD. Q-C-08 continua aberta; correção
mínima preparada e ensaiada apenas em cópia isolada, não aplicada às ferramentas.
Sem fecho/commits/push; revisão humana final e integração continuam pendentes.

[Log Francisco](../experiments/construction/003-overlay/manual-Francisco-execucao-01/manual.log) · [Conferência](../experiments/construction/003-overlay/manual-Francisco-evidence.json) · [Proposta Q-C-08](../experiments/construction/003-overlay/q-c-08-proposal.md).

## Test para aprovação — Q-C-08 resolvida

Estado registado em 2026-11-10T16:41:30Z. Patch Q-C-08 aplicado exclusivamente
como autorizado: [diff +5/−3](../experiments/construction/003-overlay/q-c-08-applied.diff), três ficheiros, restante conteúdo
local preservado. [V-ADD real](../experiments/construction/003-overlay/q-c-08-v-add-applied.log) passou: saída 0, zero erros;
validador/alcance intactos. Q-C-08 resolvida. As anteriores referências a
V-ADD reprovado são históricas. Código Java/testes/configurações sem alteração;
clean verify de 903 testes e JAR/hash/XML preservados. Manual humano Francisco
cumprido e aprovado, 49 checks/Java 0. [Checkpoint Test atualizado](../memory-bank/bolts/003-overlay/ddd-03-test-report.md).
Test ainda por aprovar, current_stage=test, completed=null; não há fecho,
commits/push ou nova integração. Revisão humana final e integração pendentes.

## IMP-004 — Test aprovado; fecho pendente

Test aprovado explicitamente pelo utilizador, registo em 2026-11-10T16:44:29Z.
903 testes do clean verify aprovados, manual humano de Francisco cumprido
(49 checks/Java 0) e V-ADD aprovado; Q-C-08 resolvida.
[Resumo/diff para revisão humana final](../memory-bank/bolts/003-overlay/final-review-summary.md).
As cinco etapas estão concluídas; bolt in-progress, current_stage=null,
completed=null. TASK/história/unidade continuam em progresso, sem implemented=true
ou DONE. Não há gates externos adicionais; faltam revisão humana final e
integração. Não executar o fecho nem fazer commits/push nesta autorização.

## Revisão final Denji aprovada — integração por autorizar

O utilizador confirmou explicitamente que Denji realizou e aprovou a revisão
humana final desta versão, sem alterações pendentes. Confirmação registada em
2026-11-10T17:00:44Z; data de execução da revisão não fornecida e não inventada.
Código permanece idêntico ao aprovado em Test; manual humano de Francisco
já cumprido, sem repetição. Q-C-08 resolvida, Test/V-ADD aprovados.
Falta integração autorizada/comprovada para poder tratar DONE. Sem staging,
commits/push, integração ou fecho nesta autorização.

[Registo da confirmação](../experiments/construction/003-overlay/denji-final-review-confirmation.json).

## Estado atual — IMP-004 fechado

Registo documental em 2026-11-10T17:12:15Z.
IMP-004/003-overlay/TASK-007 fechado documentalmente por autorização explícita
do utilizador. Integração local comprovada em main: `8db7b78`
(`8db7b78950e444f3c2917844617be4b9b236ec0f`), com 145 caminhos e hashes conferidos.
Test aprovado: clean verify/903 testes, manual humano Francisco/49 checks/Java 0
confirmado pelo utilizador, V-ADD aprovado; revisão final de Denji aprovada sem
alterações pendentes. Q-C-08 resolvida e limitada a patch/evidência na integração.
Bolt complete; história complete/implemented=true; TASK-007 DONE. A unidade
002-overlay mantém in-progress: 020-overlay e 004-overlay continuam planned,
sem início de outro IMP. Fecho documental sem alteração Java ou repetição do manual.
Sem push ou alegação de integração remota/entrega do projeto.

[Fecho documental](../memory-bank/bolts/003-overlay/closure-walkthrough.md). As referências anteriores a estados
pendentes são históricas; não descrevem o estado atual. Não se promovem
requisitos globais, PHASE-02, unidade ou intent a concluídos por este fecho.

## Extra posterior ao âmbito base

| Requisito | TASK / IMP | Estado | Evidência |
|---|---|---|---|
| [REQ-025](../specs.md#req-025) | TASK-033/034, IMP-034/035 | IN PROGRESS apenas na integração futura de vídeo | Controlo EXTRA-01 concluído: V-033/1065/Luis53 e V-034/1113/Luis66, SIGKILL reais, sessões únicas e S–R1 preservada; tarefas e PHASE-02 DONE. [Fecho IMP-035](../memory-bank/bolts/035-overlay-extra/closure-walkthrough.md). Continuidade do vídeo por validar após routing/streaming. |

## IMP-034 — fecho integrado

2026-11-11T19:45:51Z: V-033 executado (1065 testes), manual humano Luis/53 checks/saída 0 e Test aceites; revisão técnica final do validador concluída. Versão f650be02f2af10b95328f5b50c3d6941e2e75c15 integrada em main local antes do fecho. TASK-033/bolt/história DONE/complete; Q-C-19 retira arquivos TCP v1, preservando testes operacionais v2 e UDP OTT1. [Fecho](../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md). Sem push. IMP-035 mantém planned e gate próprio; REQ-025 e unidade/fase continuam em curso.

## IMP-035 e PHASE-02 concluídos

Registo 2026-11-12T10:46:24Z. Aceitação explícita de fecho pelo utilizador após Test1113/
manual Luis66 e versão integrada em mainff0ef0b. [Fecho e evidência](<../memory-bank/bolts/035-overlay-extra/closure-walkthrough.md>).
Bolt/história/unidade002-overlay complete; TASK-034 e PHASE-02 DONE no âmbito
de controlo incluindo EXTRA-01. Oito bolts complete no projeto;27 planned.
005-rotas desimpedido e planned, sem início. Continuidade de vídeo nas novas
arestas fica para routing/streaming; projeto/intent não concluídos.


## IMP-008 — Model iniciado, sem validação funcional

2026-11-13T16:04:28Z: 021-rotas/TASK-011 em Model para aprovação; Q-C-26 proposta,
não decidida. REQ-005/006/014/021 e V-011 têm M01–M18 planeados no
[modelo](../memory-bank/bolts/021-rotas/ddd-01-domain-model.md), sem prova funcional
nova nem requisito global promovido. Dependências integradas; sem código alterado.
Design/ADR/Implement/Test, manual, revisão final Waheed e integração pendentes.


## IMP-008 / 021-rotas — Model aprovado; Design para revisão

Registo 2026-11-13T16:19:45Z. Utilizador aprovou Model e os três princípios de Q-C-26;
autorizou apenas Design. APIs/tipos imutáveis, limites exatos propostos, retenção,
transição atómica de época, snapshots sem upstream confirmado e D01–D22 descritos
em memory-bank/bolts/021-rotas/ddd-02-technical-design.md. Q-C-26 conserva valores
e horizonte pendentes de revisão; não são ADR aceite nem implementação.
current_stage=design, stages_completed=[model], completed=null; critérios funcionais
por cumprir. Seis caminhos mantidos, nenhum Java/Maven/socket/commit/push/outro IMP.
V-011/regressão atual/manual offline antes de aprovar Test/revisão final Waheed/
integração pendentes; sem gates externos adicionais. Unidade/PHASE-03 em progresso.
Registos de Model para aprovação acima são históricos; só Model está concluído.


## IMP-008 / 021-rotas — Design aprovado; ADR para aprovação

Registo 2026-11-13T16:41:04Z. Utilizador aprovou Design e limites/horizontes concretos de
Q-C-26, exigindo alocação por necessidade. Apenas revisão ADR autorizada.
APIs reais de LinkMetrics/NetworkMonitor/Neighbor/config e DTOs inspecionadas
estaticamente; custo já fornecido em µs/ceil, sem segunda conversão. ADD6.1,
contratos e índice recebem detalhe aprovado, sem ADR nova ou substituição.
Futura ponte de eventos/owner ainda não existe; não se presume integração pronta
nem se alarga âmbito. current_stage=adr, stages_completed=[model,design],
completed=null; revisão ADR para aprovação, Implement/Test não iniciados.
Seis caminhos preservados; nenhum Java alterado/Maven/commit/push/outro IMP.
V-011/regressão atual/manual offline antes de aprovar Test/revisão final Waheed/
integração pendentes; sem gates externos adicionais. Unidade/PHASE-03 em progresso.


## IMP-008 / 021-rotas — Implement para aprovação

Registo 2026-11-13T17:18:48Z. Revisão ADR aprovada pelo utilizador; Implement autorizado só
nos seis caminhos. Dados/custos/tabela implementados aditivamente, +851/−0 contra
cc952c5; nenhum teste anterior alterado. Incrementais novos:33/30falhas→33/27→
33/10→33/0; revisão de record:35/1→35/0. Regressão incremental07:1211/0falhas/
72erros de SocketException por sandbox; preservada. Repetição08 autorizada fora
da sandbox, mesmo código/JDK21:1211/0/0/0,14 XML,35 novos+1176 anteriores.
Walkthrough e D01–D22 em memory-bank/bolts/021-rotas/implementation-walkthrough.md.
D22 parcial: clean verify/JAR/manual offline humano/revisão final Waheed/integração
pendentes. Sem composição RoutingService/OverlayNode ou alteração de wire/caps.
current_stage=implement, stages_completed=[model,design,adr], completed=null;
Implement para revisão, Test não iniciado. História/unidade/TASK/PHASE-03 em
progresso, sem implemented=true ou fecho. Sem gates externos adicionais,
commits/push ou outro IMP. Target gerado inventariado separadamente.


## IMP-008 / 021-rotas — Test técnico executado; manual humano pendente

Registo 2026-11-13T17:43:51Z. Utilizador aprovou Implement e autorizou apenas Test. Novo
clean verify com JDK21:1211 testes/0falhas/0erros/0ignorados,14 XML,exit0.
JAR novo364704 bytes,SHA256 366210cf8c7656ad2948ae1c842796632085aaa4f09afcedcd00a49db0a0ecee,
cópia em experiments/construction/021-rotas/test-build-01/. Fontes aprovados
intactos antes/depois; nenhum Java ou teste do projeto alterado nesta etapa.
Manual técnico Codex em test-manual-runs/20261113T173843Z-92dfcjfm:43 CHECK_PASS,
MANUAL_PASS,compile0,Java0,passed=true,hashes/JAR corretos,JVM terminada.
Não conta como humano. Roteiro guiado para Luis em manual-instructions.md;
execução humana ainda inexistente/pendente. D22 parcial: manual humano antes de
aprovar Test, aprovação de Test, revisão final Waheed e integração pendentes.
current_stage=test, stages_completed=[model,design,adr,implement], completed=null;
bolt/história/TASK/unidade/PHASE-03 em progresso, sem implemented=true ou fecho.
Planeamento EXTRA-02 preexistente preservado; diff separado do Test e de target.
Sem gates externos adicionais, commits/push ou início de outro IMP.


## IMP-008 — Test/manual Luis e revisão final aceites; integração autorizada

Registo 2026-11-13T17:59:03Z. Test aprovado incluindo manual humano Luis em
20261113T174838Z-y23kpe_b:43 CHECK_PASS/MANUAL_PASS/Java0/passed=true/JVM terminada,
mesmo JAR e fontes do clean verify1211/0/0/0. Original preservado por hash.
Utilizador confirmou revisão final por Luis substituindo Waheed; não se presume
revisão de Waheed. Commits, integração e push main/fecho autorizados. Cinco etapas
concluídas; bolt/história/TASK ainda em progresso até integração comprovada;
D22 parcial só por integração. Sem novo Java/build/manual ou outro IMP.


## IMP-008 / 021-rotas / TASK-011 — fecho após integração comprovada

Registo 2026-11-13T18:09:37Z. Versão revista integrada em main: 3b7aa745cac62b71ac8ce7e036656bf7ab21692f;192 caminhos/hashes
iguais ao pacote, V-ADD0/78 numa cópia extraída do commit. Fontes/JAR e relatório
humano Luis conferidos no commit, sem overlay do diretório de trabalho.
Test1211/0/0/0,JDK21/14XML; manual humano Luis43 CHECK_PASS/MANUAL_PASS/Java0/
passed=true/JVM terminada aceites. Revisão final Luis confirmada pelo utilizador,
substituindo Waheed. D22 cumprido; fecho explicitamente autorizado.
Bolt/história complete, implemented=true, TASK-011 DONE. Dez bolts complete,
25 planned, nenhum em execução. Unidade003-rotas/PHASE-03 continuam em progresso;
006-rotas apenas desimpedido, sem início ou autorização para outro IMP.
Código/JAR/logs originais preservados, sem repetição de build/manual. EXTRA-02
preexistente e target gerado fora dos commits. Publicação main autorizada,
resultado só será declarado após push efetivo; nenhum force push.
