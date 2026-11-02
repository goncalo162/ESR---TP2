---
id: "010-video"
unit: "005-video"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["001-definir-codec-udp-e-fragmentacao-de-frames"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["009-arvores"]
enables_bolts: ["011-video", "012-video"]
requires_units: ["004-arvores"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-05"
source_tasks: ["TASK-019"]
source_task_order: ["TASK-019"]
implementation_tasks: ["IMP-016"]
size: "M"
validation_ids: ["V-019"]
test_ids: ["V-019"]
external_gates: []
adr_ids: ["ADR-002", "ADR-006", "ADR-007", "ADR-009", "ADR-021", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-010", "REQ-015", "REQ-018", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 010-video — Codificar datagramas e fragmentos

## Fonte normativa e âmbito

Ler [IMP-016](../../../add.md#imp-016) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-019](../../../docs/tasks/PHASE-05/TASK-019.md).

## História, dependências e gates

- [ ] [001-definir-codec-udp-e-fragmentacao-de-frames](../../intents/001-ott-streaming/units/005-video/stories/001-definir-codec-udp-e-fragmentacao-de-frames.md).
- Dependências: [009-arvores](../009-arvores/bolt.md).
- Unidade: [005-video](../../intents/001-ott-streaming/units/005-video/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-016 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-016 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-016 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Formato coincide com contrato v1.
- [ ] Limites são impostos antes de alocar.
- [ ] Fragmentação cobre todas as fronteiras sem bytes perdidos.
- [ ] Validação V-019 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-019, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-016](../../../add.md#imp-016).
- Prova futura: `experiments/construction/010-video/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
