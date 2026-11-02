---
id: "013-video"
unit: "005-video"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["005-apresentar-catalogo-e-video-numa-janela-swing"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["009-arvores", "027-video"]
enables_bolts: ["028-video"]
requires_units: ["004-arvores"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-05"
source_tasks: ["TASK-023"]
source_task_order: ["TASK-023"]
implementation_tasks: ["IMP-022"]
size: "M"
validation_ids: ["V-023"]
test_ids: ["V-023"]
external_gates: ["G-GUI"]
adr_ids: ["ADR-006", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-015", "REQ-016", "REQ-018"]
architecture_source: "add.md"
---
# Bolt 013-video — Apresentar catálogo e vídeo Swing

## Fonte normativa e âmbito

Ler [IMP-022](../../../add.md#imp-022) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-023](../../../docs/tasks/PHASE-05/TASK-023.md).

## História, dependências e gates

- [ ] [005-apresentar-catalogo-e-video-numa-janela-swing](../../intents/001-ott-streaming/units/005-video/stories/005-apresentar-catalogo-e-video-numa-janela-swing.md).
- Dependências: [009-arvores](../009-arvores/bolt.md), [027-video](../027-video/bolt.md).
- Unidade: [005-video](../../intents/001-ott-streaming/units/005-video/unit-brief.md).
- Gates: G-GUI.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-022 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-022 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-022 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Janela reproduz frames de conteúdo identificável.
- [ ] Seleção e stop respeitam uma subscrição local.
- [ ] Rede e descodificação não bloqueiam EDT.
- [ ] Validação V-023 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-023, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-022](../../../add.md#imp-022).
- Prova futura: `experiments/construction/013-video/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
