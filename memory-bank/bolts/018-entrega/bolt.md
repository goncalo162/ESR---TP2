---
id: "018-entrega"
unit: "007-entrega"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["002-empacotar-codigo-e-verificar-reproducao-da-entrega"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["017-entrega", "031-core"]
enables_bolts: ["032-entrega"]
requires_units: ["006-core"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 3
execution_agent: "construction"
source_phase: "PHASE-07"
source_tasks: ["TASK-030"]
source_task_order: ["TASK-030"]
implementation_tasks: ["IMP-031"]
size: "M"
validation_ids: ["V-030"]
test_ids: ["V-030"]
external_gates: ["G-MEDIA-DISTRIBUTION", "G-DELIVERY"]
adr_ids: ["ADR-023", "ADR-024"]
requirements: ["CON-003", "CON-004", "CON-005", "INT-001", "INT-002", "REQ-023"]
architecture_source: "add.md"
---
# Bolt 018-entrega — Validar o pacote de entrega

## Fonte normativa e âmbito

Ler [IMP-031](../../../add.md#imp-031) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-030](../../../docs/tasks/PHASE-07/TASK-030.md).

## História, dependências e gates

- [ ] [002-empacotar-codigo-e-verificar-reproducao-da-entrega](../../intents/001-ott-streaming/units/007-entrega/stories/002-empacotar-codigo-e-verificar-reproducao-da-entrega.md).
- Dependências: [017-entrega](../017-entrega/bolt.md), [031-core](../031-core/bolt.md).
- Unidade: [007-entrega](../../intents/001-ott-streaming/units/007-entrega/unit-brief.md).
- Gates: G-MEDIA-DISTRIBUTION, G-DELIVERY.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-031 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-031 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-031 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] PDF e ZIP usam turno/grupo reais.
- [ ] Entrega é reproduzível a partir da extração.
- [ ] Não há segredos, caches ou dependências locais não documentadas.
- [ ] Validação V-030 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-030, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-031](../../../add.md#imp-031).
- Prova futura: `experiments/construction/018-entrega/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
