---
id: "025-video"
unit: "005-video"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["007-ler-o-perfil-mjpeg-confirmado"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["009-arvores", "011-video"]
enables_bolts: ["026-video"]
requires_units: ["004-arvores"]
blocks: true
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-05"
source_tasks: ["TASK-020"]
source_task_order: ["TASK-020"]
implementation_tasks: ["IMP-018"]
size: "M"
validation_ids: ["V-020"]
test_ids: ["V-020-B"]
external_gates: []
adr_ids: ["ADR-006", "ADR-009", "ADR-021", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-012", "REQ-013", "REQ-015", "REQ-018"]
architecture_source: "add.md"
---
# Bolt 025-video — Ler o perfil MJPEG confirmado

## Fonte normativa e âmbito

Ler [IMP-018](../../../add.md#imp-018) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md).

## História, dependências e gates

- [ ] [007-ler-o-perfil-mjpeg-confirmado](../../intents/001-ott-streaming/units/005-video/stories/007-ler-o-perfil-mjpeg-confirmado.md).
- Dependências: [009-arvores](../009-arvores/bolt.md), [011-video](../011-video/bolt.md).
- Unidade: [005-video](../../intents/001-ott-streaming/units/005-video/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-018 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-018 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-018 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-018 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-018 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Ler L bytes após cinco dígitos ASCII; EOF só é limpo entre registos.
- [ ] Prefixo inválido/parcial, zero, excesso de tamanho, truncatura e JPEG inválido têm erro localizado antes de publicação.
- [ ] Fixtures independentes produzem os bytes esperados e o leitor fecha recursos.

## Evidência e fecho

- Testes: V-020-B, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-018](../../../add.md#imp-018).
- Prova futura: `experiments/construction/025-video/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
