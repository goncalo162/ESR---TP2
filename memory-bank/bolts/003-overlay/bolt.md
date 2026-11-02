---
id: "003-overlay"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["002-gerir-handshake-unicidade-e-reconexao-de-vizinhos"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["001-base-validada", "002-overlay"]
enables_bolts: ["020-overlay"]
requires_units: ["001-base-validada"]
blocks: true
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-02"
source_tasks: ["TASK-007"]
source_task_order: ["TASK-007"]
implementation_tasks: ["IMP-004"]
size: "M"
validation_ids: ["V-007"]
test_ids: ["V-007"]
external_gates: []
adr_ids: ["ADR-012", "ADR-013", "ADR-018", "ADR-019", "ADR-021", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-004", "REQ-002", "REQ-003", "REQ-004", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 003-overlay — Estabelecer e reconectar vizinhos

## Fonte normativa e âmbito

Ler [IMP-004](../../../add.md#imp-004) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-007](../../../docs/tasks/PHASE-02/TASK-007.md).

## História, dependências e gates

- [ ] [002-gerir-handshake-unicidade-e-reconexao-de-vizinhos](../../intents/001-ott-streaming/units/002-overlay/stories/002-gerir-handshake-unicidade-e-reconexao-de-vizinhos.md).
- Dependências: [001-base-validada](../001-base-validada/bolt.md), [002-overlay](../002-overlay/bolt.md).
- Unidade: [002-overlay](../../intents/001-ott-streaming/units/002-overlay/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-004 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-004 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-004 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-004 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-004 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Uma conexão confirmada por vizinho.
- [ ] Entrada tardia e reconexão funcionam.
- [ ] Endereço UDP só é aceite após handshake.
- [ ] Validação V-007 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-007, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-004](../../../add.md#imp-004).
- Prova futura: `experiments/construction/003-overlay/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
