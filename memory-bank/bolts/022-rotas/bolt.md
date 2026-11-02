---
id: "022-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["004-diagnosticar-percursos-pela-tabela-de-rotas"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["004-overlay", "006-rotas", "020-overlay"]
enables_bolts: ["007-arvores", "008-arvores", "009-arvores", "023-arvores", "024-arvores"]
requires_units: ["002-overlay"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-03"
source_tasks: ["TASK-013"]
source_task_order: ["TASK-013"]
implementation_tasks: ["IMP-010"]
size: "S"
validation_ids: ["V-013"]
test_ids: ["V-013"]
external_gates: []
adr_ids: ["ADR-004", "ADR-022", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-010", "REQ-019", "REQ-023"]
architecture_source: "add.md"
---
# Bolt 022-rotas — Diagnosticar percursos pela CLI

## Fonte normativa e âmbito

Ler [IMP-010](../../../add.md#imp-010) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-013](../../../docs/tasks/PHASE-03/TASK-013.md).

## História, dependências e gates

- [ ] [004-diagnosticar-percursos-pela-tabela-de-rotas](../../intents/001-ott-streaming/units/003-rotas/stories/004-diagnosticar-percursos-pela-tabela-de-rotas.md).
- Dependências: [004-overlay](../004-overlay/bolt.md), [006-rotas](../006-rotas/bolt.md), [020-overlay](../020-overlay/bolt.md).
- Unidade: [003-rotas](../../intents/001-ott-streaming/units/003-rotas/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-010 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-010 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-010 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Trace reflete a tabela de cada nó.
- [ ] Falhas terminam por erro/timeout em prazo.
- [ ] Diagnóstico não altera subscrições.
- [ ] Validação V-013 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-013, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-010](../../../add.md#imp-010).
- Prova futura: `experiments/construction/022-rotas/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
