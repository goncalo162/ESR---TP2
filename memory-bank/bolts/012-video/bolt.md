---
id: "012-video"
unit: "005-video"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["009-arvores", "010-video"]
enables_bolts: ["027-video"]
requires_units: ["004-arvores"]
blocks: true
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-05"
source_tasks: ["TASK-021"]
source_task_order: ["TASK-021"]
implementation_tasks: ["IMP-020"]
size: "M"
validation_ids: ["V-021"]
test_ids: ["V-021"]
external_gates: []
adr_ids: ["ADR-002", "ADR-006", "ADR-016", "ADR-017", "ADR-021", "ADR-022", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-001", "NFR-002", "REQ-007", "REQ-010", "REQ-011", "REQ-015", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 012-video — Encaminhar datagramas pela árvore

## Fonte normativa e âmbito

Ler [IMP-020](../../../add.md#imp-020) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-021](../../../docs/tasks/PHASE-05/TASK-021.md).

## História, dependências e gates

- [ ] [003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa](../../intents/001-ott-streaming/units/005-video/stories/003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa.md).
- Dependências: [009-arvores](../009-arvores/bolt.md), [010-video](../010-video/bolt.md).
- Unidade: [005-video](../../intents/001-ott-streaming/units/005-video/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-020 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-020 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-020 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-020 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-020 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Envios correspondem apenas ao snapshot ativo.
- [ ] Replicação é única por destino em estado estável.
- [ ] Counters distinguem descarte, envio e entrega local.
- [ ] Validação V-021 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-021, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-020](../../../add.md#imp-020).
- Prova futura: `experiments/construction/012-video/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
