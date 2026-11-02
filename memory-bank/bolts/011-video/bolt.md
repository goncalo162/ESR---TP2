---
id: "011-video"
unit: "005-video"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["009-arvores", "010-video"]
enables_bolts: ["025-video"]
requires_units: ["004-arvores"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-05"
source_tasks: ["TASK-020"]
source_task_order: ["TASK-020"]
implementation_tasks: ["IMP-017"]
size: "M"
validation_ids: ["V-020"]
test_ids: ["V-020-A"]
external_gates: ["G-MEDIA"]
adr_ids: ["ADR-006", "ADR-009", "ADR-021", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-012", "REQ-013", "REQ-015", "REQ-018"]
architecture_source: "add.md"
---
# Bolt 011-video — Preparar quatro conteúdos distintos

## Fonte normativa e âmbito

Ler [IMP-017](../../../add.md#imp-017) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md).

## História, dependências e gates

- [ ] [002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao](../../intents/001-ott-streaming/units/005-video/stories/002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao.md).
- Dependências: [009-arvores](../009-arvores/bolt.md), [010-video](../010-video/bolt.md).
- Unidade: [005-video](../../intents/001-ott-streaming/units/005-video/unit-brief.md).
- Gates: G-MEDIA.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-017 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-017 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-017 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Quatro conteúdos identificáveis, dois por origem, preparados no perfil de cinco dígitos/JPEG.
- [ ] Proveniência, parâmetros de preparação, hashes e FPS registados; cópias idênticas não contam como vídeos distintos.
- [ ] Cada JPEG respeita o limite e é descodificável; condições de redistribuição ainda pendentes ficam explícitas para TASK-030.

## Evidência e fecho

- Testes: V-020-A, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-017](../../../add.md#imp-017).
- Prova futura: `experiments/construction/011-video/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
