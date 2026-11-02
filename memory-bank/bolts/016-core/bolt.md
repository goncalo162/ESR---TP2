---
id: "016-core"
unit: "006-core"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["003-validar-cenario-complexo-com-duas-origens-e-cinco-clientes"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["028-video", "030-core"]
enables_bolts: ["031-core"]
requires_units: ["005-video"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 3
execution_agent: "construction"
source_phase: "PHASE-06"
source_tasks: ["TASK-027"]
source_task_order: ["TASK-027"]
implementation_tasks: ["IMP-028"]
size: "M"
validation_ids: ["V-027"]
test_ids: ["V-027"]
external_gates: ["G-CORE"]
adr_ids: ["ADR-008", "ADR-011", "ADR-024"]
requirements: ["CON-001", "INT-001", "INT-002", "NFR-002", "REQ-007", "REQ-012", "REQ-013", "REQ-020"]
architecture_source: "add.md"
---
# Bolt 016-core — Validar quatro streams e cinco clientes

## Fonte normativa e âmbito

Ler [IMP-028](../../../add.md#imp-028) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-027](../../../docs/tasks/PHASE-06/TASK-027.md).

## História, dependências e gates

- [ ] [003-validar-cenario-complexo-com-duas-origens-e-cinco-clientes](../../intents/001-ott-streaming/units/006-core/stories/003-validar-cenario-complexo-com-duas-origens-e-cinco-clientes.md).
- Dependências: [028-video](../028-video/bolt.md), [030-core](../030-core/bolt.md).
- Unidade: [006-core](../../intents/001-ott-streaming/units/006-core/unit-brief.md).
- Gates: G-CORE.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-028 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-028 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-028 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Mínimo de dois servidores com dois vídeos cada demonstrado no CORE.
- [ ] Cinco clientes e rotas alternativas têm evidência.
- [ ] Falhas e isolamento são descritos com limites.
- [ ] Validação V-027 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-027, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-028](../../../add.md#imp-028).
- Prova futura: `experiments/construction/016-core/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
