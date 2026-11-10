---
id: "003-overlay"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "in-progress"
stories: ["002-gerir-handshake-unicidade-e-reconexao-de-vizinhos"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-10T15:20:09Z"
started: "2026-11-10T14:01:42Z"
completed: null
current_stage: "adr"
stages_completed: [{"name": "model", "completed": "2026-11-10T14:48:50Z", "artifact": "ddd-01-domain-model.md"}, {"name": "design", "completed": "2026-11-10T15:13:50Z", "artifact": "ddd-02-technical-design.md"}]
requires_bolts: ["001-base-validada", "002-overlay"]
enables_bolts: ["020-overlay"]
requires_units: ["001-base-validada"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-02"
source_tasks: ["TASK-007"]
source_task_order: ["TASK-007"]
implementation_tasks: ["IMP-004"]
size: "M"
validation_ids: ["V-007"]
test_ids: ["V-007"]
external_gates: []
adr_ids: ["ADR-012", "ADR-013", "ADR-018", "ADR-019", "ADR-021", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-004", "REQ-002", "REQ-003", "REQ-004", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 003-overlay — Estabelecer e reconectar vizinhos

## Design aprovado — revisão ADR para validação

Iniciado em 2026-11-10T14:01:42Z por seleção explícita do utilizador, apenas Model.
Base Denji 26aaa4ae6775e9c78e1d7658472ccaca0b376427, árvore inicialmente limpa; dependências complete/integradas.
[Modelo concreto](ddd-01-domain-model.md) preparado em 2026-11-10T14:11:43Z: identidade/sessão,
lista inicial, estados, handshake v1, unicidade/reinício e reconexão sem bootstrap,
proprietário serial, prazos/limites e oráculos M01–M22 para V-007.
Três ficheiros autorizados atualmente vazios; nenhum dos 155 ficheiros de Projeto/
foi alterado. V-007, regressão, manual e revisão final atribuída a Denji pendentes.
Parar para aprovação de Model antes de Design/ADR/Implement/Test. Não há nova
execução Java, aprovação de etapa, commit/push ou fecho nesta continuação.
current_stage=model, stages_completed=[], completed=null.

Revisão humana pediu ajustes por lacuna no tratamento de ERROR (17), sem aprovar
Model/Design. [Modelo revisto](ddd-01-domain-model.md#8-autorização-error-17-e-entrega-ao-consumidor):
descoberta/handshake/ACTIVE separados, erros da sessão entregues intactos ao
consumidor, sem ERROR de resposta. M02/M07/M11 ampliados;
[Q-C-05](../../../docs/construction-duvidas.md#q-c-05--receção-de-error-no-imp-004)
registou inicialmente as propostas de retry/erros inesperados.
Model revisto e todas as decisões da tabela Q-C-05 aprovados explicitamente em
2026-11-10T14:48:50Z. Model concluído em stages_completed; autorizada apenas Design.
[Desenho concreto](ddd-02-technical-design.md) preparado em 2026-11-10T15:00:14Z: assinaturas e
valores imutáveis, owner serial próprio/emprestado, retenção B/R, jobs DNS/I/O
limitados, correlação por token/connectionId antes de classificar direção,
barreiras de handshake/ERROR e mapa D01–D22. APIs existentes preservadas.
Parar para revisão de Design antes de ADR/Implement/Test. Nenhum Java/build/teste,
commit/push ou outro IMP. V-007/regressão/manual, revisão final por Denji e integração
continuam pendentes. current_stage=design, completed=null.

## Fonte normativa e âmbito

Ler [IMP-004](../../../add.md#imp-004) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-007](../../../docs/tasks/PHASE-02/TASK-007.md).

## História, dependências e gates

- [ ] [002-gerir-handshake-unicidade-e-reconexao-de-vizinhos](../../intents/001-ott-streaming/units/002-overlay/stories/002-gerir-handshake-unicidade-e-reconexao-de-vizinhos.md).
- Dependências: [001-base-validada](../001-base-validada/bolt.md), [002-overlay](../002-overlay/bolt.md).
- Unidade: [002-overlay](../../intents/001-ott-streaming/units/002-overlay/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-004 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-004 e as ADR do ADD.
3. [ ] **adr** → `adr-review.md`; validar a etapa contra IMP-004 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-004 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-004 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Uma conexão confirmada por vizinho.
- [ ] Entrada tardia e reconexão funcionam.
- [ ] Endereço UDP só é aceite após handshake.
- [ ] Validação V-007 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-007, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-004](../../../add.md#imp-004).
- Prova futura: `experiments/construction/003-overlay/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Continuação ADR — aguardando revisão

Design aprovado explicitamente em 2026-11-10T15:13:50Z, incluindo prazo próprio de
resolução/bind igual a control.connectTimeout contado antes da fila, separado
do dial TCP. [Q-C-06](../../../docs/construction-duvidas.md#q-c-06--prazo-próprio-de-resolução-e-bind-no-imp-004)
regista a aprovação. [Revisão ADR](adr-review.md) preparada em 2026-11-10T15:20:09Z:
reutilização das decisões aceites, owner próprio/emprestado, limites físicos,
retenção B/R e correlação de dials conformes no desenho, Q-C-05 preservada.
Não foi identificada decisão aceite inviável/nova ADR necessária nesta inspeção.
D01–D22 mantidos, 155 hashes de Projeto/ preservados e nenhum Java/build/teste
executado. Só Model/Design concluídos; current_stage=adr, completed=null.
Parar para validação antes de Implement/Test; revisão final atribuída a Denji e
integração exigidas antes de DONE. Sem commits/push/fecho ou outro IMP.
