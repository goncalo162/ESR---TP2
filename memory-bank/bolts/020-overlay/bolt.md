---
id: "020-overlay"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-10T17:12:15Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["001-base-validada", "003-overlay", "019-overlay"]
enables_bolts: ["004-overlay", "022-rotas"]
requires_units: ["001-base-validada"]
blocks: false
complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-02"
source_tasks: ["TASK-008"]
source_task_order: ["TASK-008"]
implementation_tasks: ["IMP-005"]
size: "M"
validation_ids: ["V-008"]
test_ids: ["V-008"]
external_gates: []
adr_ids: ["ADR-013", "ADR-014", "ADR-016", "ADR-017", "ADR-019", "ADR-020", "ADR-022", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-003", "REQ-001", "REQ-002", "REQ-023"]
architecture_source: "add.md"
---
# Bolt 020-overlay — Compor CLI e ciclo de processo

## Fonte normativa e âmbito

Ler [IMP-005](../../../add.md#imp-005) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-008](../../../docs/tasks/PHASE-02/TASK-008.md).

## História, dependências e gates

- [ ] [003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel](../../intents/001-ott-streaming/units/002-overlay/stories/003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel.md).
- Dependências: [001-base-validada](../001-base-validada/bolt.md), [003-overlay](../003-overlay/bolt.md), [019-overlay](../019-overlay/bolt.md).
- Unidade: [002-overlay](../../intents/001-ott-streaming/units/002-overlay/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-005 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-005 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-005 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] BOOTSTRAP, SERVER, RELAY e CLIENT arrancam no papel pedido.
- [ ] Códigos de erro e logs são coerentes.
- [ ] O fim de um processo não exige reiniciar os restantes.
- [ ] Validação V-008 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-008, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-005](../../../add.md#imp-005).
- Prova futura: `experiments/construction/020-overlay/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Prontidão derivada após fecho de IMP-004

003-overlay complete, TASK-007 DONE e integração local 8db7b78 comprovada.
Atualizado apenas blocks=false: dependências concluídas. Este bolt permanece
planned, started=null, current_stage=null, stages_completed=[]; nenhum IMP iniciado.
