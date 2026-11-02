---
id: "001-base-validada"
unit: "001-base-validada"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["001-verificar-base"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: []
enables_bolts: ["002-overlay", "003-overlay", "004-overlay", "019-overlay", "020-overlay"]
requires_units: []
blocks: false
complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "BASELINE"
source_tasks: ["TASK-001", "TASK-002", "TASK-003", "TASK-004", "TASK-005"]
source_task_order: ["TASK-001", "TASK-002", "TASK-003", "TASK-004", "TASK-005"]
implementation_tasks: ["IMP-001"]
size: "S"
validation_ids: ["V-001", "V-002", "V-003", "V-004", "V-005"]
test_ids: ["V-001", "V-002", "V-003", "V-004", "V-005", "V-ADD"]
external_gates: []
adr_ids: ["ADR-001", "ADR-002", "ADR-007", "ADR-008", "ADR-009", "ADR-010", "ADR-012", "ADR-014", "ADR-020", "ADR-021", "ADR-024", "ADR-026"]
requirements: ["CON-009", "INT-001", "INT-002", "NFR-004", "NFR-005", "REQ-001", "REQ-003", "REQ-004", "REQ-012", "REQ-013", "REQ-016", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 001-base-validada — Verificar a base herdada

## Fonte normativa e âmbito

Ler [IMP-001](../../../add.md#imp-001) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-001](../../../docs/tasks/PHASE-01/TASK-001.md), [TASK-002](../../../docs/tasks/PHASE-01/TASK-002.md), [TASK-003](../../../docs/tasks/PHASE-01/TASK-003.md), [TASK-004](../../../docs/tasks/PHASE-01/TASK-004.md), [TASK-005](../../../docs/tasks/PHASE-02/TASK-005.md).

## História, dependências e gates

- [ ] [001-verificar-base](../../intents/001-ott-streaming/units/001-base-validada/stories/001-verificar-base.md).
- Dependências: nenhuma.
- Unidade: [001-base-validada](../../intents/001-ott-streaming/units/001-base-validada/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-001 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-001 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-001 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Confirmar componentes reais e manter TASK-001–005 DONE, sem os reimplementar.
- [ ] Conferir 45 fixtures, 16 tipos ativos e o ADD/specs/DAG antes de libertar nova construção.
- [ ] Executar a regressão existente no ambiente de Construction, guardando um novo log; 149 é total histórico, não resultado atual.

## Evidência e fecho

- Testes: V-001, V-002, V-003, V-004, V-005, V-ADD, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-001](../../../add.md#imp-001).
- Prova futura: `experiments/construction/001-base-validada/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
