---
id: "014-core"
unit: "006-core"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "planned"
stories: ["001-preparar-core-e-comprovar-replicacao-no-cenario-de-quatro-nos"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["028-video"]
enables_bolts: ["029-core"]
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
implementation_tasks: ["IMP-024"]
size: "M"
validation_ids: ["V-025"]
test_ids: ["V-025-A"]
external_gates: ["G-CORE", "G-GUI"]
adr_ids: ["ADR-001", "ADR-008", "ADR-011", "ADR-024"]
requirements: ["CON-001", "INT-001", "INT-002", "NFR-004", "REQ-011", "REQ-020", "REQ-023"]
architecture_source: "add.md"
---
# Bolt 014-core — Preparar ambiente e mapa CORE

## Fonte normativa e âmbito

Ler [IMP-024](../../../add.md#imp-024) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-025](../../../docs/tasks/PHASE-06/TASK-025.md).

## História, dependências e gates

- [ ] [001-preparar-core-e-comprovar-replicacao-no-cenario-de-quatro-nos](../../intents/001-ott-streaming/units/006-core/stories/001-preparar-core-e-comprovar-replicacao-no-cenario-de-quatro-nos.md).
- Dependências: [028-video](../028-video/bolt.md).
- Unidade: [006-core](../../intents/001-ott-streaming/units/006-core/unit-brief.md).
- Gates: G-CORE, G-GUI.

## Etapas e validação

1. [ ] **plan** → `implementation-plan.md`; validar a etapa contra IMP-024 e as ADR do ADD.
2. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-024 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-024 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] JDK/JAR/volumes/display verificados em sessão CORE real.
- [ ] Mapa S/R/C1/C2 e bootstrap, IPs/interfaces/portas e rotas IP documentados; advertised UDP pertence ao nó.
- [ ] Perfis válidos e reachability confirmada sem substituir o XML por defeito.

## Evidência e fecho

- Testes: V-025-A, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-024](../../../add.md#imp-024).
- Prova futura: `experiments/construction/014-core/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
