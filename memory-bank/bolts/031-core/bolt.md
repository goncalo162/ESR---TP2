---
id: "031-core"
unit: "006-core"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["004-recolher-metricas-e-avaliar-resultados-e-limites"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["016-core", "028-video"]
enables_bolts: ["017-entrega", "018-entrega", "032-entrega", "033-entrega"]
requires_units: ["005-video"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 3
execution_agent: "construction"
source_phase: "PHASE-06"
source_tasks: ["TASK-028"]
source_task_order: ["TASK-028"]
implementation_tasks: ["IMP-029"]
size: "M"
validation_ids: ["V-028"]
test_ids: ["V-028"]
external_gates: ["G-CORE"]
adr_ids: ["ADR-008", "ADR-022", "ADR-024", "ADR-025"]
requirements: ["CON-001", "CON-008", "INT-001", "INT-002", "NFR-001", "NFR-002", "NFR-005", "REQ-008", "REQ-011", "REQ-023"]
architecture_source: "add.md"
---
# Bolt 031-core — Consolidar métricas e limites

## Fonte normativa e âmbito

Ler [IMP-029](../../../add.md#imp-029) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-028](../../../docs/tasks/PHASE-06/TASK-028.md).

## História, dependências e gates

- [ ] [004-recolher-metricas-e-avaliar-resultados-e-limites](../../intents/001-ott-streaming/units/006-core/stories/004-recolher-metricas-e-avaliar-resultados-e-limites.md).
- Dependências: [016-core](../016-core/bolt.md), [028-video](../028-video/bolt.md).
- Unidade: [006-core](../../intents/001-ott-streaming/units/006-core/unit-brief.md).
- Gates: G-CORE.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-029 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-029 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-029 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Resultados são reais, datados e repetíveis.
- [ ] Critérios de eficiência e adaptação têm evidência.
- [ ] Limitações e desvios de metas estão documentados.
- [ ] Validação V-028 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-028, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-029](../../../add.md#imp-029).
- Prova futura: `experiments/construction/031-core/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
