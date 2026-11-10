---
id: "004-overlay"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "in-progress"
stories: ["004-preparar-evidencia-do-primeiro-marco-intermedio"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-10T23:25:25Z"
started: "2026-11-10T22:38:06Z"
completed: null
current_stage: null
stages_completed: [{"name": "plan", "completed": "2026-11-10T22:50:50Z", "artifact": "implementation-plan.md"}, {"name": "implement", "completed": "2026-11-10T23:09:30Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-10T23:25:25Z", "artifact": "test-walkthrough.md"}]
requires_bolts: ["001-base-validada", "020-overlay"]
enables_bolts: ["005-rotas", "006-rotas", "021-rotas", "022-rotas"]
requires_units: ["001-base-validada"]
blocks: false
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-02"
source_tasks: ["TASK-009"]
source_task_order: ["TASK-009"]
implementation_tasks: ["IMP-006"]
size: "S"
validation_ids: ["V-009"]
test_ids: ["V-009"]
external_gates: ["G-ASSESSMENTS"]
adr_ids: ["ADR-008", "ADR-013", "ADR-023", "ADR-024"]
requirements: ["CON-007", "INT-001", "INT-002", "NFR-005"]
architecture_source: "add.md"
---
# Bolt 004-overlay — Demonstrar o primeiro marco

## Fonte normativa e âmbito

Ler [IMP-006](../../../add.md#imp-006) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-009](../../../docs/tasks/PHASE-02/TASK-009.md).

## História, dependências e gates

- [ ] [004-preparar-evidencia-do-primeiro-marco-intermedio](../../intents/001-ott-streaming/units/002-overlay/stories/004-preparar-evidencia-do-primeiro-marco-intermedio.md).
- Dependências: [001-base-validada](../001-base-validada/bolt.md), [020-overlay](../020-overlay/bolt.md).
- Unidade: [002-overlay](../../intents/001-ott-streaming/units/002-overlay/unit-brief.md).
- Gates: G-ASSESSMENTS.

## Etapas e validação

1. [x] **plan** → `implementation-plan.md`; validar a etapa contra IMP-006 e as ADR do ADD.
2. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-006 e as ADR do ADD.
3. [x] **test** → `test-walkthrough.md`; validar a etapa contra IMP-006 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Roteiro repetível e logs datados disponíveis.
- [ ] Limitações da versão intermédia explicitadas.
- [ ] Situação da avaliação registada sem datas inventadas.
- [ ] Validação V-009 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-009, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-006](../../../add.md#imp-006).
- Prova futura: `experiments/construction/004-overlay/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## IMP-006 — Plan para revisão

Registo de 2026-11-10T22:38:06Z. As dependências estão concluídas e integradas
na base `89d0201`. O Plan define o roteiro do marco, a captura e a repetição
por outro elemento. V-009 continua por executar e a informação docente
sobre a primeira avaliação está pendente. [Plan do IMP-006](implementation-plan.md).

## IMP-006 — Plan aprovado, Implement para revisão

Registo de 2026-11-10T22:50:50Z. O utilizador aprovou Plan e autorizou Implement.
O roteiro, os perfis e os auxiliares estão preparados. Q-C-14 regista as
permissões da bancada ainda por confirmar para a captura e o corte TCP.
O ensaio da overlay e a repetição de V-009 por outro elemento estão pendentes.
A data e o conteúdo da avaliação aguardam informação docente.
[Implement do IMP-006](implementation-walkthrough.md).

## IMP-006 — Implement aprovado, Test em execução

Registo de 2026-11-10T23:09:30Z. O utilizador aprovou Implement incluindo a guarda
do processo de captura. A sondagem inicial do agente falhou. A sondagem
de Luis na bancada Linux local passou para captura e corte.
Test aguarda a evidência de V-009 e a repetição por outro elemento.
[Registo de Test](test-walkthrough.md).
