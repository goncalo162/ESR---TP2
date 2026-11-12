---
id: "021-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["002-modelar-candidatos-custos-e-snapshots-por-stream"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["004-overlay", "005-rotas"]
enables_bolts: ["006-rotas"]
requires_units: ["002-overlay"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-03"
source_tasks: ["TASK-011"]
source_task_order: ["TASK-011"]
implementation_tasks: ["IMP-008"]
size: "M"
validation_ids: ["V-011"]
test_ids: ["V-011"]
external_gates: []
adr_ids: ["ADR-004", "ADR-017", "ADR-018", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-006", "REQ-014", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 021-rotas — Modelar candidatos e snapshots

## Fonte normativa e âmbito

Ler [IMP-008](../../../add.md#imp-008) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-011](../../../docs/tasks/PHASE-03/TASK-011.md).

## História, dependências e gates

- [ ] [002-modelar-candidatos-custos-e-snapshots-por-stream](../../intents/001-ott-streaming/units/003-rotas/stories/002-modelar-candidatos-custos-e-snapshots-por-stream.md).
- Dependências: [004-overlay](../004-overlay/bolt.md), [005-rotas](../005-rotas/bolt.md).
- Unidade: [003-rotas](../../intents/001-ott-streaming/units/003-rotas/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-008 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-008 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-008 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-008 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-008 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Cada fluxo tem custo, origem, destinos e estado.
- [ ] Empates são reproduzíveis e caminhos inválidos rejeitados.
- [ ] Expirar um candidato não apaga alternativas válidas.
- [ ] Validação V-011 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-011, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-008](../../../add.md#imp-008).
- Prova futura: `experiments/construction/021-rotas/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
