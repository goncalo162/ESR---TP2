---
id: "002-overlay"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["001-fornecer-localizacao-inicial-dos-vizinhos-configurados"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["001-base-validada", "019-overlay"]
enables_bolts: ["003-overlay"]
requires_units: ["001-base-validada"]
blocks: true
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-02"
source_tasks: ["TASK-006"]
source_task_order: ["TASK-006"]
implementation_tasks: ["IMP-003"]
size: "M"
validation_ids: ["V-006"]
test_ids: ["V-006"]
external_gates: []
adr_ids: ["ADR-003", "ADR-013", "ADR-014", "ADR-015", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-002", "REQ-003", "REQ-004"]
architecture_source: "add.md"
---
# Bolt 002-overlay — Descoberta inicial e escrita confirmada

## Fonte normativa e âmbito

Ler [IMP-003](../../../add.md#imp-003) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-006](../../../docs/tasks/PHASE-02/TASK-006.md).

## História, dependências e gates

- [ ] [001-fornecer-localizacao-inicial-dos-vizinhos-configurados](../../intents/001-ott-streaming/units/002-overlay/stories/001-fornecer-localizacao-inicial-dos-vizinhos-configurados.md).
- Dependências: [001-base-validada](../001-base-validada/bolt.md), [019-overlay](../019-overlay/bolt.md).
- Unidade: [002-overlay](../../intents/001-ott-streaming/units/002-overlay/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [ ] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-003 e as ADR do ADD.
2. [ ] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-003 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-003 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-003 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-003 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Resposta corresponde ao grafo e respeita X por nó.
- [ ] Resposta independe da disponibilidade e de consultas anteriores; não existem registo vivo, lease, renovação ou monitorização.
- [ ] IDs e endpoints inválidos são rejeitados sem alterar a configuração ou bloquear outras consultas.
- [ ] Conexão inicial é fechada após resposta; não há mensagens de saída/renovação para bootstrap.
- [ ] Confirmação de escrita/flush tem prazo e termina em sucesso ou erro; testes observam resposta completa antes de EOF e não deixam futures pendentes após falha/shutdown.
- [ ] Validação V-006 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-006, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-003](../../../add.md#imp-003).
- Prova futura: `experiments/construction/002-overlay/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
