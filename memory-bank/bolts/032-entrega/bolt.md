---
id: "032-entrega"
unit: "007-entrega"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["003-preparar-defesa-e-checklist-de-submissao-humana"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["018-entrega", "031-core"]
enables_bolts: ["033-entrega"]
requires_units: ["006-core"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 3
execution_agent: "construction"
source_phase: "PHASE-07"
source_tasks: ["TASK-031"]
source_task_order: ["TASK-031"]
implementation_tasks: ["IMP-032"]
size: "S"
validation_ids: ["V-031"]
test_ids: ["V-031-A"]
external_gates: ["G-ASSESSMENTS", "G-DELIVERY"]
adr_ids: ["ADR-023", "ADR-024"]
requirements: ["CON-005", "CON-006", "CON-007", "INT-001", "INT-002", "NFR-005"]
architecture_source: "add.md"
---
# Bolt 032-entrega — Preparar e ensaiar a defesa

## Fonte normativa e âmbito

Ler [IMP-032](../../../add.md#imp-032) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-031](../../../docs/tasks/PHASE-07/TASK-031.md).

## História, dependências e gates

- [ ] [003-preparar-defesa-e-checklist-de-submissao-humana](../../intents/001-ott-streaming/units/007-entrega/stories/003-preparar-defesa-e-checklist-de-submissao-humana.md).
- Dependências: [018-entrega](../018-entrega/bolt.md), [031-core](../031-core/bolt.md).
- Unidade: [007-entrega](../../intents/001-ott-streaming/units/007-entrega/unit-brief.md).
- Gates: G-ASSESSMENTS, G-DELIVERY.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-032 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-032 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-032 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Ensaio cobre critérios de avaliação, decisões, demonstrações intermédias e limites conhecidos.
- [ ] Roteiro identifica exatamente PDF/ZIP e versão validados e possui fallback de evidência real.
- [ ] Prazo/grupo/artefactos conhecidos ou pendências explícitas; preparação não equivale a upload.

## Evidência e fecho

- Testes: V-031-A, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-032](../../../add.md#imp-032).
- Prova futura: `experiments/construction/032-entrega/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
