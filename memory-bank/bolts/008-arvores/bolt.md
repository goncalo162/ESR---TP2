---
id: "008-arvores"
unit: "004-arvores"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["003-recuperar-arvores-apos-falha-de-upstream"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["022-rotas", "023-arvores"]
enables_bolts: ["024-arvores"]
requires_units: ["003-rotas"]
blocks: true
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-04"
source_tasks: ["TASK-016"]
source_task_order: ["TASK-016"]
implementation_tasks: ["IMP-013"]
size: "M"
validation_ids: ["V-016"]
test_ids: ["V-016"]
external_gates: []
adr_ids: ["ADR-005", "ADR-020", "ADR-024", "ADR-025"]
requirements: ["CON-008", "INT-001", "INT-002", "NFR-003", "REQ-002", "REQ-006", "REQ-009"]
architecture_source: "add.md"
---
# Bolt 008-arvores — Recuperar falhas do upstream TCP

## Fonte normativa e âmbito

Ler [IMP-013](../../../add.md#imp-013) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-016](../../../docs/tasks/PHASE-04/TASK-016.md).

## História, dependências e gates

- [ ] [003-recuperar-arvores-apos-falha-de-upstream](../../intents/001-ott-streaming/units/004-arvores/stories/003-recuperar-arvores-apos-falha-de-upstream.md).
- Dependências: [022-rotas](../022-rotas/bolt.md), [023-arvores](../023-arvores/bolt.md).
- Unidade: [004-arvores](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-013 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-013 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-013 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-013 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-013 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Há recuperação quando existe caminho até à mesma fonte.
- [ ] Ausência de caminho é visível e não produz laço.
- [ ] Estado antigo não é reativado por mensagens atrasadas.
- [ ] Validação V-016 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-016, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-013](../../../add.md#imp-013).
- Prova futura: `experiments/construction/008-arvores/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
