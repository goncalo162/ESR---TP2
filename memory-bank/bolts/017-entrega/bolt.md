---
id: "017-entrega"
unit: "007-entrega"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["001-escrever-artigo-com-protocolos-e-avaliacao-critica"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["031-core"]
enables_bolts: ["018-entrega"]
requires_units: ["006-core"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 3
execution_agent: "construction"
source_phase: "PHASE-07"
source_tasks: ["TASK-029"]
source_task_order: ["TASK-029"]
implementation_tasks: ["IMP-030"]
size: "M"
validation_ids: ["V-029"]
test_ids: ["V-029"]
external_gates: []
adr_ids: ["ADR-023", "ADR-024"]
requirements: ["CON-002", "CON-003", "INT-001", "INT-002", "NFR-005", "REQ-022"]
architecture_source: "add.md"
---
# Bolt 017-entrega — Produzir e rever o artigo

## Fonte normativa e âmbito

Ler [IMP-030](../../../add.md#imp-030) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-029](../../../docs/tasks/PHASE-07/TASK-029.md).

## História, dependências e gates

- [ ] [001-escrever-artigo-com-protocolos-e-avaliacao-critica](../../intents/001-ott-streaming/units/007-entrega/stories/001-escrever-artigo-com-protocolos-e-avaliacao-critica.md).
- Dependências: [031-core](../031-core/bolt.md).
- Unidade: [007-entrega](../../intents/001-ott-streaming/units/007-entrega/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-030 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-030 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-030 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Artigo cobre arquitetura, protocolos, implementação e avaliação.
- [ ] Até 12 páginas no ficheiro final.
- [ ] Todas as afirmações experimentais têm evidência e limites.
- [ ] Validação V-029 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-029, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-030](../../../add.md#imp-030).
- Prova futura: `experiments/construction/017-entrega/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
