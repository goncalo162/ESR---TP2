---
id: "029-core"
unit: "006-core"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["005-medir-partilha-no-cenario-de-quatro-nos"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["014-core", "028-video"]
enables_bolts: ["015-core"]
requires_units: ["005-video"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 3
execution_agent: "construction"
source_phase: "PHASE-06"
source_tasks: ["TASK-025"]
source_task_order: ["TASK-025"]
implementation_tasks: ["IMP-025"]
size: "M"
validation_ids: ["V-025"]
test_ids: ["V-025-B"]
external_gates: ["G-CORE"]
adr_ids: ["ADR-001", "ADR-008", "ADR-011", "ADR-024"]
requirements: ["CON-001", "INT-001", "INT-002", "NFR-004", "REQ-011", "REQ-020", "REQ-023"]
architecture_source: "add.md"
---
# Bolt 029-core — Medir partilha no cenário de quatro nós

## Fonte normativa e âmbito

Ler [IMP-025](../../../add.md#imp-025) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-025](../../../docs/tasks/PHASE-06/TASK-025.md).

## História, dependências e gates

- [ ] [005-medir-partilha-no-cenario-de-quatro-nos](../../intents/001-ott-streaming/units/006-core/stories/005-medir-partilha-no-cenario-de-quatro-nos.md).
- Dependências: [014-core](../014-core/bolt.md), [028-video](../028-video/bolt.md).
- Unidade: [006-core](../../intents/001-ott-streaming/units/006-core/unit-brief.md).
- Gates: G-CORE.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-025 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-025 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-025 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Duas saídas recebem a mesma stream com uma cópia por chave no tronco estável sem perda configurada.
- [ ] Parar um consumidor preserva o outro; depois do último stop a poda remove vídeo no prazo medido.
- [ ] Capturas e contadores correspondem a ligações lógicas e manifestos identificam a versão/condições.

## Evidência e fecho

- Testes: V-025-B, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-025](../../../add.md#imp-025).
- Prova futura: `experiments/construction/029-core/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
