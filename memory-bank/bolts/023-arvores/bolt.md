---
id: "023-arvores"
unit: "004-arvores"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["002-podar-ramos-renovar-leases-e-remover-procura-abandonada"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["007-arvores", "022-rotas"]
enables_bolts: ["008-arvores"]
requires_units: ["003-rotas"]
blocks: true
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-04"
source_tasks: ["TASK-015"]
source_task_order: ["TASK-015"]
implementation_tasks: ["IMP-012"]
size: "M"
validation_ids: ["V-015"]
test_ids: ["V-015"]
external_gates: []
adr_ids: ["ADR-005", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-003", "REQ-002", "REQ-005", "REQ-007", "REQ-009", "REQ-011", "REQ-017"]
architecture_source: "add.md"
---
# Bolt 023-arvores — Renovar e podar subscrições

## Fonte normativa e âmbito

Ler [IMP-012](../../../add.md#imp-012) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md).

## História, dependências e gates

- [ ] [002-podar-ramos-renovar-leases-e-remover-procura-abandonada](../../intents/001-ott-streaming/units/004-arvores/stories/002-podar-ramos-renovar-leases-e-remover-procura-abandonada.md).
- Dependências: [007-arvores](../007-arvores/bolt.md), [022-rotas](../022-rotas/bolt.md).
- Unidade: [004-arvores](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-012 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-012 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-012 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-012 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-012 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Vídeo só permanece autorizado enquanto há procura efetiva.
- [ ] Ramos órfãos desaparecem por timeout.
- [ ] Outros consumidores e fluxos mantêm-se ativos.
- [ ] Validação V-015 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-015, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-012](../../../add.md#imp-012).
- Prova futura: `experiments/construction/023-arvores/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
