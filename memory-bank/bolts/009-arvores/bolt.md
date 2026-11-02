---
id: "009-arvores"
unit: "004-arvores"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["005-preparar-o-segundo-marco-com-arvores-e-adaptacao"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["022-rotas", "024-arvores"]
enables_bolts: ["010-video", "011-video", "012-video", "013-video", "025-video", "026-video", "027-video", "028-video"]
requires_units: ["003-rotas"]
blocks: true
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-04"
source_tasks: ["TASK-018"]
source_task_order: ["TASK-018"]
implementation_tasks: ["IMP-015"]
size: "S"
validation_ids: ["V-018"]
test_ids: ["V-018"]
external_gates: ["G-ASSESSMENTS"]
adr_ids: ["ADR-008", "ADR-023", "ADR-024"]
requirements: ["CON-007", "INT-001", "INT-002", "NFR-005"]
architecture_source: "add.md"
---
# Bolt 009-arvores — Demonstrar o segundo marco

## Fonte normativa e âmbito

Ler [IMP-015](../../../add.md#imp-015) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-018](../../../docs/tasks/PHASE-04/TASK-018.md).

## História, dependências e gates

- [ ] [005-preparar-o-segundo-marco-com-arvores-e-adaptacao](../../intents/001-ott-streaming/units/004-arvores/stories/005-preparar-o-segundo-marco-com-arvores-e-adaptacao.md).
- Dependências: [022-rotas](../022-rotas/bolt.md), [024-arvores](../024-arvores/bolt.md).
- Unidade: [004-arvores](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
- Gates: G-ASSESSMENTS.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-015 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-015 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-015 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Demonstração mostra isolamento entre fluxos e procura agregada.
- [ ] Falha e alteração de RTT têm evidência verificável.
- [ ] Conteúdo e estado do marco estão registados.
- [ ] Validação V-018 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-018, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-015](../../../add.md#imp-015).
- Prova futura: `experiments/construction/009-arvores/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
