---
id: "015-core"
unit: "006-core"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["002-demonstrar-alternativas-e-adaptacao-no-cenario-de-seis-nos"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["028-video", "029-core"]
enables_bolts: ["030-core"]
requires_units: ["005-video"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 3
execution_agent: "construction"
source_phase: "PHASE-06"
source_tasks: ["TASK-026"]
source_task_order: ["TASK-026"]
implementation_tasks: ["IMP-026"]
size: "M"
validation_ids: ["V-026"]
test_ids: ["V-026-A"]
external_gates: ["G-CORE"]
adr_ids: ["ADR-008", "ADR-011", "ADR-024", "ADR-025"]
requirements: ["CON-001", "CON-008", "INT-001", "INT-002", "NFR-003", "REQ-006", "REQ-009", "REQ-014", "REQ-020"]
architecture_source: "add.md"
---
# Bolt 015-core — Validar falhas TCP e limitação UDP

## Fonte normativa e âmbito

Ler [IMP-026](../../../add.md#imp-026) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-026](../../../docs/tasks/PHASE-06/TASK-026.md).

## História, dependências e gates

- [ ] [002-demonstrar-alternativas-e-adaptacao-no-cenario-de-seis-nos](../../intents/001-ott-streaming/units/006-core/stories/002-demonstrar-alternativas-e-adaptacao-no-cenario-de-seis-nos.md).
- Dependências: [028-video](../028-video/bolt.md), [029-core](../029-core/bolt.md).
- Unidade: [006-core](../../intents/001-ott-streaming/units/006-core/unit-brief.md).
- Gates: G-CORE.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-026 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-026 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-026 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Morte de processo e quebra TCP recuperam por alternativa até à mesma origem, quando existe; partição fica UNAVAILABLE.
- [ ] Bloqueio apenas UDP com sondas TCP saudáveis é registado como limitação, sem exigir recuperação ou troca de parent.
- [ ] Ensaios têm deadline e cronologia; recuperação IP é distinguida da recuperação aplicacional.

## Evidência e fecho

- Testes: V-026-A, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-026](../../../add.md#imp-026).
- Prova futura: `experiments/construction/015-core/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
