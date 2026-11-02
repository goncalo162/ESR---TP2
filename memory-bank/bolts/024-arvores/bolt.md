---
id: "024-arvores"
unit: "004-arvores"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["008-arvores", "022-rotas"]
enables_bolts: ["009-arvores"]
requires_units: ["003-rotas"]
blocks: true
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-04"
source_tasks: ["TASK-017"]
source_task_order: ["TASK-017"]
implementation_tasks: ["IMP-014"]
size: "M"
validation_ids: ["V-017"]
test_ids: ["V-017"]
external_gates: []
adr_ids: ["ADR-005", "ADR-016", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-001", "NFR-003", "REQ-009", "REQ-014"]
architecture_source: "add.md"
---
# Bolt 024-arvores — Trocar parent com histerese

## Fonte normativa e âmbito

Ler [IMP-014](../../../add.md#imp-014) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-017](../../../docs/tasks/PHASE-04/TASK-017.md).

## História, dependências e gates

- [ ] [004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada](../../intents/001-ott-streaming/units/004-arvores/stories/004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada.md).
- Dependências: [008-arvores](../008-arvores/bolt.md), [022-rotas](../022-rotas/bolt.md).
- Unidade: [004-arvores](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-014 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-014 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-014 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-014 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-014 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Política corresponde aos parâmetros documentados.
- [ ] Um upstream é confirmado no snapshot.
- [ ] Tráfego transitório e limites da troca são explicitados.
- [ ] Validação V-017 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-017, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-014](../../../add.md#imp-014).
- Prova futura: `experiments/construction/024-arvores/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
