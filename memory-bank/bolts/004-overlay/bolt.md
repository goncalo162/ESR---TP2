---
id: "004-overlay"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["004-preparar-evidencia-do-primeiro-marco-intermedio"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
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

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-006 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-006 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-006 e as ADR do ADD.

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
