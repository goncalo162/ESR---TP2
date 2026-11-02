---
id: "006-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["003-difundir-anuncios-e-construir-catalogo-e-rotas"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["004-overlay", "021-rotas"]
enables_bolts: ["022-rotas"]
requires_units: ["002-overlay"]
blocks: true
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-03"
source_tasks: ["TASK-012"]
source_task_order: ["TASK-012"]
implementation_tasks: ["IMP-009"]
size: "M"
validation_ids: ["V-012"]
test_ids: ["V-012"]
external_gates: []
adr_ids: ["ADR-004", "ADR-018", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-006", "REQ-009", "REQ-012", "REQ-014", "REQ-016"]
architecture_source: "add.md"
---
# Bolt 006-rotas — Difundir anúncios e catálogo

## Fonte normativa e âmbito

Ler [IMP-009](../../../add.md#imp-009) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-012](../../../docs/tasks/PHASE-03/TASK-012.md).

## História, dependências e gates

- [ ] [003-difundir-anuncios-e-construir-catalogo-e-rotas](../../intents/001-ott-streaming/units/003-rotas/stories/003-difundir-anuncios-e-construir-catalogo-e-rotas.md).
- Dependências: [004-overlay](../004-overlay/bolt.md), [021-rotas](../021-rotas/bolt.md).
- Unidade: [003-rotas](../../intents/001-ott-streaming/units/003-rotas/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-009 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-009 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-009 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-009 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-009 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Catálogo inclui origem e validade dos quatro fluxos.
- [ ] Melhores caminhos e alternativas ficam disponíveis.
- [ ] Cache, seq, epoch e path impedem tempestade e regressão a época retirada.
- [ ] Validação V-012 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-012, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-009](../../../add.md#imp-009).
- Prova futura: `experiments/construction/006-rotas/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
