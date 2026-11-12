---
id: "005-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["001-medir-rtt-e-publicar-falhas-de-vizinhanca"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-12T10:46:24Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["004-overlay", "035-overlay-extra"]
enables_bolts: ["021-rotas"]
requires_units: ["002-overlay"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-03"
source_tasks: ["TASK-010"]
source_task_order: ["TASK-010"]
implementation_tasks: ["IMP-007"]
size: "M"
validation_ids: ["V-010"]
test_ids: ["V-010"]
external_gates: []
adr_ids: ["ADR-004", "ADR-016", "ADR-022", "ADR-024", "ADR-025"]
requirements: ["CON-008", "INT-001", "INT-002", "REQ-003", "REQ-008", "REQ-014", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 005-rotas — Medir RTT e detetar falhas TCP

## Fonte normativa e âmbito

Ler [IMP-007](../../../add.md#imp-007) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-010](../../../docs/tasks/PHASE-03/TASK-010.md).

## História, dependências e gates

- [ ] [001-medir-rtt-e-publicar-falhas-de-vizinhanca](../../intents/001-ott-streaming/units/003-rotas/stories/001-medir-rtt-e-publicar-falhas-de-vizinhanca.md).
- Dependências: [004-overlay](../004-overlay/bolt.md).
- Unidade: [003-rotas](../../intents/001-ott-streaming/units/003-rotas/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-007 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-007 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-007 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-007 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-007 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] RTT é calculado sem comparar relógios de máquinas diferentes.
- [ ] Falhas são correlacionadas à conexão correta.
- [ ] Métrica sem amostra não é publicada como zero confiável.
- [ ] Validação V-010 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-010, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-007](../../../add.md#imp-007).
- Prova futura: `experiments/construction/005-rotas/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

O EXTRA-01 exige 034-overlay-extra (aprendizagem) e 035-overlay-extra (reparação) antes deste bolt. A conclusão e integração dos dois são pré-requisito de início.

## Pré-requisitos disponíveis

Registo 2026-11-12T10:46:24Z. PHASE-02/unidade002-overlay/035-overlay-extra concluídos.
blocks=false; bolt permanece planned, sem started/current_stage/etapas executadas.
O ponto de retoma é Model do IMP-007 quando autorizado; não foi iniciado.
