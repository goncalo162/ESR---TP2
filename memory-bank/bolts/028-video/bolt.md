---
id: "028-video"
unit: "005-video"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["009-arvores", "013-video", "026-video"]
enables_bolts: ["014-core", "015-core", "016-core", "029-core", "030-core", "031-core"]
requires_units: ["004-arvores"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-05"
source_tasks: ["TASK-024"]
source_task_order: ["TASK-024"]
implementation_tasks: ["IMP-023"]
size: "M"
validation_ids: ["V-024"]
test_ids: ["V-024"]
external_gates: ["G-GUI"]
adr_ids: ["ADR-006", "ADR-008", "ADR-020", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-007", "REQ-012", "REQ-013", "REQ-015", "REQ-016", "REQ-023"]
architecture_source: "add.md"
---
# Bolt 028-video — Integrar duas origens e quatro streams

## Fonte normativa e âmbito

Ler [IMP-023](../../../add.md#imp-023) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-024](../../../docs/tasks/PHASE-05/TASK-024.md).

## História, dependências e gates

- [ ] [006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta](../../intents/001-ott-streaming/units/005-video/stories/006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta.md).
- Dependências: [009-arvores](../009-arvores/bolt.md), [013-video](../013-video/bolt.md), [026-video](../026-video/bolt.md).
- Unidade: [005-video](../../intents/001-ott-streaming/units/005-video/unit-brief.md).
- Gates: G-GUI.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-023 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-023 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-023 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Quatro fluxos distintos são utilizáveis.
- [ ] Partilha e isolamento passam a validação local.
- [ ] Versão integrada encerra sem threads/sockets pendentes.
- [ ] Validação V-024 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-024, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-023](../../../add.md#imp-023).
- Prova futura: `experiments/construction/028-video/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
