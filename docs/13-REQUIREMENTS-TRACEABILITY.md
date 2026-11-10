# Rastreabilidade de requisitos

> Arquitetura normativa: [add.md](../add.md). Requisitos consolidados: [specs.md](../specs.md). Este documento mantém detalhe técnico/proveniência e deve ser interpretado pela precedência do ADD; não é uma autoridade arquitetural concorrente.

Estado inicial do plano: TODO para todos os requisitos. Estado atual: RF-001/003/004/012/013, RP-001/006 e RNF-004/005 em IN PROGRESS; os restantes requisitos continuam TODO. PHASE-01/TASK-001–004 e TASK-005 estão DONE com configuração, codec e TCP testados; overlay completa/streaming pendentes. Cobertura planeada não equivale a cumprimento. Os detalhes e fontes estão em [02](02-REQUIREMENTS.md); V-XXX é definido em [09](09-TESTING-STRATEGY.md). Todas as obrigações e recomendações adotadas têm tarefas concretas e um oráculo.

Um requisito só passa a DONE quando todas as tarefas necessárias e os oráculos relevantes estiverem concluídos. Pode passar a IN PROGRESS com evidência parcial ou BLOCKED com causa concreta; nunca usar DONE por a documentação estar escrita. Registar ligações para evidências na coluna final durante a implementação.

| ID | Descrição | Componente responsável | Fases | Tarefas | Testes / verificações | Estado | Evidência |
|---|---|---|---|---|---|---|---|
| [RF-001](02-REQUIREMENTS.md#rf-001) | Executar nós como aplicações em máquinas IP | app/config/overlay | 01, 02 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-008](tasks/PHASE-02/TASK-008.md) | V-003, V-008 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); restantes componentes pendentes |
| [RF-002](02-REQUIREMENTS.md#rf-002) | Entrada e saída dinâmica de nós | bootstrap/overlay/routing | 02, 04 | [TASK-006](tasks/PHASE-02/TASK-006.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-008](tasks/PHASE-02/TASK-008.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-016](tasks/PHASE-04/TASK-016.md) | V-006, V-007, V-008, V-015, V-016 | TODO | — |
| [RF-003](02-REQUIREMENTS.md#rf-003) | Estabelecer e manter X vizinhos TCP ou UDP | transport/overlay/monitoring | 02, 03 | [TASK-005](tasks/PHASE-02/TASK-005.md), [TASK-006](tasks/PHASE-02/TASK-006.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-010](tasks/PHASE-03/TASK-010.md) | V-005, V-006, V-007, V-010 | IN PROGRESS | [V-005: TCP real, 18 testes](../experiments/task-005-transport-validation.md); restantes validações pendentes |
| [RF-004](02-REQUIREMENTS.md#rf-004) | Descobrir vizinhos por contacto conhecido | config/bootstrap/overlay | 01, 02 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-006](tasks/PHASE-02/TASK-006.md), [TASK-007](tasks/PHASE-02/TASK-007.md) | V-003, V-006, V-007 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); restantes componentes pendentes |
| [RF-005](02-REQUIREMENTS.md#rf-005) | Manter tabela por fluxo com custo, origem, destinos e estado | routing | 03, 04 | [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md) | V-011, V-012, V-014, V-015 | TODO | — |
| [RF-006](02-REQUIREMENTS.md#rf-006) | Suportar caminhos alternativos | routing/CORE | 03, 04, 06 | [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-016](tasks/PHASE-04/TASK-016.md), [TASK-026](tasks/PHASE-06/TASK-026.md) | V-011, V-012, V-016, V-026 | TODO | — |
| [RF-007](02-REQUIREMENTS.md#rf-007) | Servir vários clientes em simultâneo | routing/streaming/client | 04, 05, 06 | [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-027](tasks/PHASE-06/TASK-027.md) | V-014, V-015, V-021, V-024, V-027 | TODO | — |
| [RF-008](02-REQUIREMENTS.md#rf-008) | Monitorizar a rede overlay | monitoring/experiments | 03, 06 | [TASK-010](tasks/PHASE-03/TASK-010.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-010, V-028 | TODO | — |
| [RF-009](02-REQUIREMENTS.md#rf-009) | Atualizar rotas perante entradas, saídas e condições de rede | routing/monitoring | 03, 04, 06 | [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-016](tasks/PHASE-04/TASK-016.md), [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-026](tasks/PHASE-06/TASK-026.md) | V-012, V-015, V-016, V-017, V-026 | TODO | — |
| [RF-010](02-REQUIREMENTS.md#rf-010) | Reencaminhar dados segundo a tabela | routing/streaming | 03, 05 | [TASK-013](tasks/PHASE-03/TASK-013.md), [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-021](tasks/PHASE-05/TASK-021.md) | V-013, V-019, V-021 | TODO | — |
| [RF-011](02-REQUIREMENTS.md#rf-011) | Partilhar segmentos comuns e replicar nos pontos de ramificação | routing/streaming/experiments | 04, 05, 06 | [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-014, V-015, V-021, V-025, V-028 | TODO | — |
| [RF-012](02-REQUIREMENTS.md#rf-012) | Disponibilizar pelo menos dois servidores de conteúdos | config/server/app | 01, 03, 05, 06 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-027](tasks/PHASE-06/TASK-027.md) | V-003, V-012, V-020, V-024, V-027 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); restantes componentes pendentes |
| [RF-013](02-REQUIREMENTS.md#rf-013) | Cada servidor oferece pelo menos dois vídeos distintos | config/server/media | 01, 05, 06 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-027](tasks/PHASE-06/TASK-027.md) | V-003, V-020, V-024, V-027 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); restantes componentes pendentes |
| [RF-014](02-REQUIREMENTS.md#rf-014) | Usar métricas de rede para construir rotas | monitoring/routing | 03, 04, 06 | [TASK-010](tasks/PHASE-03/TASK-010.md), [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-026](tasks/PHASE-06/TASK-026.md) | V-010, V-011, V-012, V-017, V-026 | TODO | — |
| [RF-015](02-REQUIREMENTS.md#rf-015) | Entregar e reproduzir vídeo com objetivo de tempo real | server/streaming/client | 05 | [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-023](tasks/PHASE-05/TASK-023.md), [TASK-024](tasks/PHASE-05/TASK-024.md) | V-019, V-020, V-021, V-022, V-023, V-024 | TODO | — |
| [RNF-001](02-REQUIREMENTS.md#rnf-001) | Atraso baixo e utilização eficiente de largura de banda | routing/streaming/experiments | 04, 05, 06 | [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-017, V-019, V-020, V-021, V-022, V-028 | TODO | — |
| [RNF-002](02-REQUIREMENTS.md#rnf-002) | Escalabilidade para N clientes | streaming/CORE/experiments | 05, 06 | [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-027](tasks/PHASE-06/TASK-027.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-021, V-027, V-028 | TODO | — |
| [RNF-003](02-REQUIREMENTS.md#rnf-003) | Fiabilidade sob alterações e falhas | overlay/routing/client | 02, 04, 05, 06 | [TASK-008](tasks/PHASE-02/TASK-008.md), [TASK-015](tasks/PHASE-04/TASK-015.md), [TASK-016](tasks/PHASE-04/TASK-016.md), [TASK-017](tasks/PHASE-04/TASK-017.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-026](tasks/PHASE-06/TASK-026.md) | V-008, V-015, V-016, V-017, V-022, V-026 | TODO | — |
| [RNF-004](02-REQUIREMENTS.md#rnf-004) | Overlay aplicacional sobre transporte IP | transport/overlay/CORE | 02, 06 | [TASK-005](tasks/PHASE-02/TASK-005.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-025](tasks/PHASE-06/TASK-025.md) | V-005, V-007, V-025 | IN PROGRESS | [V-005: TCP real, 18 testes](../experiments/task-005-transport-validation.md); restantes validações pendentes |
| [RNF-005](02-REQUIREMENTS.md#rnf-005) | Especificação, implementação e avaliação crítica verificáveis | protocol/tests/report | 01, 02, 04, 06, 07 | [TASK-002](tasks/PHASE-01/TASK-002.md), [TASK-004](tasks/PHASE-01/TASK-004.md), [TASK-009](tasks/PHASE-02/TASK-009.md), [TASK-018](tasks/PHASE-04/TASK-018.md), [TASK-028](tasks/PHASE-06/TASK-028.md), [TASK-029](tasks/PHASE-07/TASK-029.md), [TASK-031](tasks/PHASE-07/TASK-031.md) | V-002, V-004, V-009, V-018, V-028, V-029, V-031 | IN PROGRESS | [V-002/V-004: especificação e codec](../experiments/task-004-codec-validation.md); interações/testes de rede e relatório final pendentes |
| [RC-001](02-REQUIREMENTS.md#rc-001) | Usar CORE como bancada principal | CORE/experiments | 06 | [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-026](tasks/PHASE-06/TASK-026.md), [TASK-027](tasks/PHASE-06/TASK-027.md), [TASK-028](tasks/PHASE-06/TASK-028.md) | V-025, V-026, V-027, V-028 | TODO | — |
| [RC-002](02-REQUIREMENTS.md#rc-002) | Relatório em formato de artigo | report | 07 | [TASK-029](tasks/PHASE-07/TASK-029.md) | V-029 | TODO | — |
| [RC-003](02-REQUIREMENTS.md#rc-003) | Relatório com máximo de 12 páginas | report/delivery | 07 | [TASK-029](tasks/PHASE-07/TASK-029.md), [TASK-030](tasks/PHASE-07/TASK-030.md) | V-029, V-030 | TODO | — |
| [RC-004](02-REQUIREMENTS.md#rc-004) | Arquivo de código ZIP ou RAR | delivery | 07 | [TASK-030](tasks/PHASE-07/TASK-030.md) | V-030 | TODO | — |
| [RC-005](02-REQUIREMENTS.md#rc-005) | Nomes de entrega com turno e grupo | delivery | 07 | [TASK-030](tasks/PHASE-07/TASK-030.md), [TASK-031](tasks/PHASE-07/TASK-031.md) | V-030, V-031 | TODO | — |
| [RC-006](02-REQUIREMENTS.md#rc-006) | Submeter na troca de ficheiros do grupo Blackboard | equipa/Blackboard | 07 | [TASK-031](tasks/PHASE-07/TASK-031.md) | V-031 | TODO | — |
| [RC-007](02-REQUIREMENTS.md#rc-007) | Preparar duas avaliações intermédias e defesa final | experiments/report/equipa | 02, 04, 07 | [TASK-009](tasks/PHASE-02/TASK-009.md), [TASK-018](tasks/PHASE-04/TASK-018.md), [TASK-031](tasks/PHASE-07/TASK-031.md) | V-009, V-018, V-031 | TODO | — |
| [RP-001](02-REQUIREMENTS.md#rp-001) | Catálogo e seleção explícita de stream | streaming/client | 01, 03, 05 | [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-012](tasks/PHASE-03/TASK-012.md), [TASK-023](tasks/PHASE-05/TASK-023.md), [TASK-024](tasks/PHASE-05/TASK-024.md) | V-003, V-012, V-023, V-024 | IN PROGRESS | [V-003: configuração e 65 testes](../experiments/task-003-configuration-validation.md); restantes componentes pendentes |
| [RP-002](02-REQUIREMENTS.md#rp-002) | Ativar sob pedido e podar ramo sem recetores | routing | 04 | [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-015](tasks/PHASE-04/TASK-015.md) | V-014, V-015 | TODO | — |
| [RP-003](02-REQUIREMENTS.md#rp-003) | MJPEG, pacotes numerados e janela de reprodução | server/streaming/client | 05 | [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-020](tasks/PHASE-05/TASK-020.md), [TASK-022](tasks/PHASE-05/TASK-022.md), [TASK-023](tasks/PHASE-05/TASK-023.md) | V-019, V-020, V-022, V-023 | TODO | — |
| [RP-004](02-REQUIREMENTS.md#rp-004) | Diagnóstico do percurso antes do vídeo | routing/app | 03 | [TASK-013](tasks/PHASE-03/TASK-013.md) | V-013 | TODO | — |
| [RP-005](02-REQUIREMENTS.md#rp-005) | Testar cenários de 4, 6 e overlay complexa | CORE/experiments | 06 | [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-026](tasks/PHASE-06/TASK-026.md), [TASK-027](tasks/PHASE-06/TASK-027.md) | V-025, V-026, V-027 | TODO | — |
| [RP-006](02-REQUIREMENTS.md#rp-006) | Código testável, entradas validadas e recursos limitados | config/protocol/transport/routing/client | 01, 02, 03, 04, 05 | [TASK-001](tasks/PHASE-01/TASK-001.md), [TASK-003](tasks/PHASE-01/TASK-003.md), [TASK-004](tasks/PHASE-01/TASK-004.md), [TASK-005](tasks/PHASE-02/TASK-005.md), [TASK-007](tasks/PHASE-02/TASK-007.md), [TASK-010](tasks/PHASE-03/TASK-010.md), [TASK-011](tasks/PHASE-03/TASK-011.md), [TASK-014](tasks/PHASE-04/TASK-014.md), [TASK-019](tasks/PHASE-05/TASK-019.md), [TASK-021](tasks/PHASE-05/TASK-021.md), [TASK-022](tasks/PHASE-05/TASK-022.md) | V-001, V-003, V-004, V-005, V-007, V-010, V-011, V-014, V-019, V-021, V-022 | IN PROGRESS | [V-003/V-004: configuração e codec](../experiments/task-004-codec-validation.md); [V-005: filas, sockets e shutdown](../experiments/task-005-transport-validation.md); restantes componentes pendentes |
| [RP-007](02-REQUIREMENTS.md#rp-007) | Adotar formato LNCS para o artigo | report | 07 | [TASK-029](tasks/PHASE-07/TASK-029.md) | V-029 | TODO | — |
| [RP-008](02-REQUIREMENTS.md#rp-008) | Documentar comandos, logs e operação reproduzível | app/experiments/delivery | 02, 03, 05, 06, 07 | [TASK-008](tasks/PHASE-02/TASK-008.md), [TASK-013](tasks/PHASE-03/TASK-013.md), [TASK-024](tasks/PHASE-05/TASK-024.md), [TASK-025](tasks/PHASE-06/TASK-025.md), [TASK-028](tasks/PHASE-06/TASK-028.md), [TASK-030](tasks/PHASE-07/TASK-030.md) | V-008, V-013, V-024, V-025, V-028, V-030 | TODO | — |

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
