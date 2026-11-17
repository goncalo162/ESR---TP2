---
id: "007-arvores"
unit: "004-arvores"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "complete"
stories: ["001-ativar-arvores-com-ack-e-procura-agregada"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-17T23:51:24Z"
started: "2026-11-17T15:29:06Z"
completed: "2026-11-17T23:51:24Z"
current_stage: null
stages_completed: [{"name": "model", "completed": "2026-11-17T17:02:14Z", "artifact": "ddd-01-domain-model.md"}, {"name": "design", "completed": "2026-11-17T17:30:07Z", "artifact": "ddd-02-technical-design.md"}, {"name": "adr", "completed": "2026-11-17T17:50:02Z", "artifact": "adr-review.md"}, {"name": "implement", "completed": "2026-11-17T23:06:56Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-17T23:45:22Z", "artifact": "ddd-03-test-report.md"}]
requires_bolts: ["022-rotas"]
enables_bolts: ["023-arvores"]
requires_units: ["003-rotas"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-04"
source_tasks: ["TASK-014"]
source_task_order: ["TASK-014"]
implementation_tasks: ["IMP-011"]
size: "M"
validation_ids: ["V-014"]
test_ids: ["V-014"]
external_gates: []
adr_ids: ["ADR-005", "ADR-018", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-007", "REQ-011", "REQ-017", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 007-arvores — Ativar procura com confirmação

## Fecho atual — autorizado

Bolt complete; história complete/implemented=true; TASK-014 DONE.
[Fecho e provas](closure-walkthrough.md): clean verify1328/0/0/0, Test aprovado,
manual humano Francisco24 e revisão final Waheed cumpridos. Versão revista
integrada em main37fa863ed416017e733e692f9565899fb61285ae, conteúdo e V-ADD da
cópia extraída conferidos. Unidade004-arvores/PHASE-04 continuam em progresso.
Registos de etapas abaixo são histórico; nenhum outro IMP iniciado, sem push.

## Fonte normativa e âmbito

Ler [IMP-011](../../../add.md#imp-011) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-014](../../../docs/tasks/PHASE-04/TASK-014.md).

## História, dependências e gates

- [x] [001-ativar-arvores-com-ack-e-procura-agregada](../../intents/001-ott-streaming/units/004-arvores/stories/001-ativar-arvores-com-ack-e-procura-agregada.md).
- Dependências: [022-rotas](../022-rotas/bolt.md).
- Unidade: [004-arvores](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; aprovado pelo utilizador, incluindo Q-C-34/35, em 2026-11-17T17:02:14Z.
2. [x] **design** → `ddd-02-technical-design.md`; aprovado integralmente pelo utilizador em 2026-11-17T17:30:07Z.
3. [x] **adr** → `adr-review.md`; aprovada pelo utilizador em 2026-11-17T17:50:02Z.
4. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-011 e as ADR do ADD.
5. [x] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-011 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [x] Árvore ativa apenas após confirmação válida.
- [x] Uma referência agregada a montante por stream.
- [x] Repetições são idempotentes e erros não deixam PENDING eterno.
- [x] Validação V-014 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-014, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-011](../../../add.md#imp-011).
- Prova futura: `experiments/construction/007-arvores/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.


## Dependência concluída, sem iniciar execução

Registo 2026-11-15T16:36:52Z.022-rotas e unidade003-rotas complete após integração
em main 3349d575ae38c772b38496cc08bc1d44ded764ff; PHASE-03 DONE. Apenas blocks=false;
status planned,started/completed/current_stage null e stages_completed=[] mantidos.
Nenhum Model/Java/teste ou outro IMP iniciado.

## Início autorizado — Model

Registo 2026-11-17T15:29:06Z. O utilizador selecionou a opção1 de iniciar
007-arvores/IMP-011. Dependências documentais concluídas; primeira etapa Model
em execução. Model não autoriza Design/ADR/Implement/Test nem fecha V-014.
A análise anterior desta sessão encontrou1296 testes/1falha/0erros e a mesma
falha de NeighborDown na repetição isolada. Esse resultado anterior fica como
limitação de regressão a investigar antes de aceitar a implementação; não
se reabre uma fundação DONE nem se declara a regressão atual verde.

## Model preparado — aprovação pendente

Registo 2026-11-17T15:35:13Z. [Modelo](ddd-01-domain-model.md) preparado:
entidades, identidades, agregação, confirmação, eventos, snapshots e M01–M14
planeados. Q-C-34/35 propostas no ADD, sem mudança de ADR aceite ou de wire.
Nenhum código-fonte lido/alterado nesta etapa; nenhum teste Java novo executado.
current_stage=model e stages_completed=[] até aprovação explícita do utilizador.
Design/ADR/Implement/Test e demais bolts não iniciados.

## Model aprovado — apenas Design autorizado

Registo 2026-11-17T17:02:14Z. O utilizador aprovou explicitamente Model e
Q-C-34/35 e autorizou apenas Design. Decisões também registadas em
docs/construction-duvidas.md. Preservar logs de NeighborDown em evidência
duradoura e investigar antes de aceitar Implement/Test; não alterar testes
ou fundações fora de âmbito por inferência. Sem gates externos adicionais.
M01–M14/V-014 continuam NÃO EXECUTADOS; sem Java, Implement, commits ou push.
Os registos anteriores de aprovação pendente descrevem o histórico.

## Design preparado — aprovação pendente

Registo 2026-11-17T17:17:16Z. [Design](ddd-02-technical-design.md) preparado:
grant dos filhos limitado ao restante confirmado, prazo global T/até A
tentativas, ledger limitado de revisões por Session/época, quotas de respostas,
reservas físicas e testes de seleção divergente preservando o ramo saudável.
Detalhes de Design por aprovar; apenas Model em stages_completed.
[NeighborDown preservado](../../../experiments/construction/007-arvores/neighbor-down-baseline/README.md),
com manifesto SHA-256; sem execução Java nova ou causa afirmada.
M01–M14/V-014 por executar. Sem ADR/Implement/Test, Java, commits ou push;
external_gates=[] preservado. Investigação técnica antes da aceitação de
Implement/Test, sem autorização inferida para modificar testes/fundações.

Verificação documental em 2026-11-17T17:18:44Z: V-ADD zero erros/4870 referências,
git diff --check e hashes/cópias dos logs passaram; Projeto/CORE sem diff,
HEAD preservado. Log em experiments/construction/007-arvores/design-validation.log.
Apenas Model concluído; Design por aprovar, os14 cenários por executar.

## Design aprovado — apenas revisão ADR autorizada

Registo 2026-11-17T17:30:07Z. O utilizador aprovou o Design completo, incluindo
ancoramento dos leases, prazo global T/até A tentativas, lineagens, capacidades
e quotas. Apenas revisão ADR autorizada: conferir APIs reais, projeções e
lifecycle nos quatro caminhos previstos, sem alterar Java ou iniciar Implement.
NeighborDown continua pendente de investigação antes da aceitação de
Implement/Test; negativos preservados. Correção fora do âmbito exige apresentação
prévia. Sem commits/push; as menções a aprovação pendente acima são históricas.

## Revisão ADR preparada — aceitação pendente

Registo 2026-11-17T17:35:51Z. [Revisão](adr-review.md) preparada: APIs reais,
projeções, dispatch, scheduler e lifecycle conferidos estaticamente. Mantêm-se
os quatro caminhos; Subscription.java/ActivationTest.java ainda ausentes.
Não se identificou necessidade de nova ADR/substituição ou extensão de caminhos.
Nenhuma implementação, compilação ou teste Java executados nesta etapa.
current_stage=adr, apenas Model/Design em stages_completed. Revisão por aceitar;
NeighborDown pendente e negativos preservados antes da aceitação Implement/Test.
Sem alterações Java, Implement, commits/push ou gates externos adicionais.

Conferência final:253 ficheiros Projeto/CORE sem diferenças, dois alvos futuros
ausentes e três negativos intactos por SHA-256; resultado em
experiments/construction/007-arvores/adr-preservation-check.json.
V-ADD documental em2026-11-17T17:44:08Z passou, zero erros/4881 referências;
git diff --check passou. Lista inline de stages_completed mantém as duas etapas
e os seus timestamps, conforme formato já utilizado; primeiro log documental
negativo e log final preservados. Nenhum teste Java novo ou causa investigada.

## Revisão ADR aprovada — apenas Implement autorizado

Registo 2026-11-17T17:50:02Z. O utilizador aprovou ADR e autorizou Implement
nos quatro caminhos. Route/catálogo mantêm seleção, forwarding separado,
Snapshot com construtor anterior, ERROR11/15 isolados e reservas físicas no
shutdown existente. Investigar/consolidar NeighborDown antes de reapresentar
Implement para aceitação; apresentar correções fora do âmbito antes de aplicar.
Sem Test/fecho/commits/push/outro IMP. M01–M14 ainda por executar à entrada.
Sem gates externos adicionais; fecho exige V-014, regressão aprovada, manual
humano, revisão final Waheed e integração. Nenhum desses resultados é presumido.

## Histórico anterior à autorização Q-C-36 — Implement preparado

Registo2026-11-17T18:36:18Z. [Walkthrough](implementation-walkthrough.md): código
nos quatro caminhos, compilação e regressão incremental1324/0/0/0,18 suites XML
preservadas e28 testes novos. Route/catalog continuam seleção; forwarding,
Snapshot4, correlação11/15 e reservas físicas integrados conforme Design/ADR.
Os14 grupos têm checks incrementais parciais; V-014 integral ainda não consolidado
ou aceite. Test/fecho/outro IMP não iniciados; sem commits/push.

[NeighborDown investigado](../../../experiments/construction/007-arvores/neighbor-down-investigation/README.md):
corrida de observação reproduzida e eventos esperados entregues após barreira;
negativos antigos intactos e cronologia exata não inferida. Q-C-36 apresenta
patch de sincronização do teste histórico, ainda não aplicado por ser fora dos
quatro caminhos. Implement não é declarado aceite; aguarda decisão de âmbito.
Fecho continua a exigir V-014, regressão aprovada, manual humano, revisão final
Waheed e integração. external_gates=[] preservado; current_stage=implement,
apenas Model/Design/ADR concluídos.

Conferência final2026-11-17T18:42:01Z: quatro caminhos Java apenas,251 ficheiros
restantes Projeto/CORE e todos os testes históricos intactos; quatro hashes
iguais à regressão02, negativos preservados e patch não aplicado.
implement-preservation-check.json guarda o resultado. V-ADD documental zero
erros/4898 referências e git diff --check passaram. Implement ainda não aceite;
nenhum gate de fecho dispensado e nenhum avanço à etapa Test.

## Q-C-36 autorizada e aplicada — revisão de Implement em 2026-11-17T18:56:41Z

Exceção específica de âmbito concedida pelo utilizador: apenas o patch apresentado
em Projeto/src/test/java/pt/uminho/esr/ott/overlay/OverlayNodeMonitoringTest.java,
além dos quatro caminhos originais. Todas as asserções, prazos e restantes testes
mantidos. Nenhuma outra alteração Java nesta retoma.

[Resultados novos e conferência de preservação](../../../experiments/construction/007-arvores/q-c-36/README.md):
focado NeighborDown 1/0/0/0; duas regressões completas 1324/1/0/0, ambas apenas
com falha D11a em OttApplicationTest.java:562; D11a isolado 1/0/0/0. Diagnóstico
controlado e NeighborDown passaram nas duas suites; evidência anterior intacta.
Causa de D11a não estabelecida, sem correção por inferência ou alargamento de âmbito.

Implement reapresentado para revisão, não aprovado/concluído. current_stage,
stages_completed e external_gates mantidos. V-014 consolidado, regressão aprovada,
manual humano, revisão final Waheed e integração pendentes. Sem Test, fecho,
commits, push ou outro IMP.

## Investigação D11a/Q-C-37 — proposta para autorização em 2026-11-17T19:13:11Z

[Prova e limites](../../../experiments/construction/007-arvores/d11a-investigation/README.md):
trace correlacionado,48 testes da classe original verdes sob instrumentação e
três ordens controladas que reproduzem perda da rejeição pela continuação de
escrita. Negativos completos/sucesso isolado preservados; sequência histórica
exata não reconstruída. Nenhuma alteração Java, teste, fundação ou prazo.
Patch proposto apenas em NeighborManager.java/NeighborManagerTest.java, não
aplicado e dependente de autorização específica antes da verificação pertinente.
Implement não aceite; current_stage/stages_completed/external_gates mantidos.

## Q-C-37 aplicada — Implement reapresentado em 2026-11-17T21:49:25Z

Exceção explicitamente autorizada: apenas o patch apresentado em
Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java e
Projeto/src/test/java/pt/uminho/esr/ott/overlay/NeighborManagerTest.java.
Sete caminhos autorizados no total; testes/asserções/prazos anteriores mantidos.
[Resultados novos](../../../experiments/construction/007-arvores/q-c-37/README.md):
D11b1/0/0/0, D11a1/0/0/0, handshake/lifecycle202/0/0/0, regressão1325/0/0/0.
Closed/prazo existente sem resposta retiram contexto/tentativa/timer; TCP real
com zero reservas e awaitTermination confirmado. Evidência anterior preservada.
Não se presume aprovação de Implement, regressão ou V-014. current_stage=implement,
só Model/Design/ADR concluídos; sem Test/fecho/commits/push/outro IMP/gates novos.
V-014 consolidado, regressão aprovada, manual humano, revisão Waheed e integração pendentes.

## Retoma de Implement — M07

Cancelamento terminal corrigido; Q-C-38 aplicada com autorização exclusiva.
Regressão incremental atual: 1328/0/0/0; Implement por aceitar, Test não autorizado.
Ver secção 11 do [walkthrough](implementation-walkthrough.md) e respetiva evidência.

Q-C-39 resolvida pelo patch documental autorizado; V-ADD zero erros.
M07 aceite tecnicamente; Implement continua por aprovar, Test não autorizado.

## Checkpoint Test autorizado

Implement aprovado pelo utilizador; Test autorizado e em progresso.
Clean verify atual1328/0/0/0; V-014 técnico consolidado no
[relatório Test](ddd-03-test-report.md). Manual humano pendente; depois aprovação
de Test, revisão final Waheed e integração. Bolt aberto, sem commits/push/fecho.
A data de conclusão de Implement acima é a data de registo desta autorização.

## Manual humano e revisão final confirmados

Francisco executou as cinco etapas/24 checks, conferidos com o JAR/hash fixo.
Revisão final de Waheed da versão atual confirmada pelo utilizador, sem alterações
pendentes; hashes vinculados no pacote integration-01. Test continua por aprovar,
bolt aberto e integração por autorizar/executar. Sem staging/commits/push/fecho.
