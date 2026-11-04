---
id: "002-overlay"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "complete"
stories: ["001-fornecer-localizacao-inicial-dos-vizinhos-configurados"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-04T14:20:34Z"
started: "2026-11-04T10:40:58Z"
completed: "2026-11-04T14:18:23Z"
current_stage: null
stages_completed: [{"name":"model","completed":"2026-11-04T10:50:52Z","artifact":"ddd-01-domain-model.md"},{"name":"design","completed":"2026-11-04T11:25:40Z","artifact":"ddd-02-technical-design.md"},{"name":"adr","completed":"2026-11-04T11:37:45Z","artifact":"adr-review.md"},{"name":"implement","completed":"2026-11-04T12:18:01Z","artifact":"implementation-walkthrough.md"},{"name":"test","completed":"2026-11-04T14:05:14Z","artifact":"ddd-03-test-report.md"}]
requires_bolts: ["001-base-validada","019-overlay"]
enables_bolts: ["003-overlay"]
requires_units: ["001-base-validada"]
blocks: false
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
adr_ids: ["ADR-003","ADR-013","ADR-014","ADR-015","ADR-024"]
requirements: ["INT-001","INT-002","REQ-002","REQ-003","REQ-004"]
architecture_source: "add.md"
---
# Bolt 002-overlay — Descoberta inicial e escrita confirmada

## Fecho concluído — IMP-003

Iniciado em 2026-11-04T10:40:58Z, por seleção explícita do utilizador,
apenas a primeira etapa. Base Denji 4ac5808dacf6e97bfd3636d2788db363e926bc95,
árvore inicialmente limpa; 001-base-validada e 019-overlay complete/integrados.
[Modelo concreto](ddd-01-domain-model.md) preparado em 2026-11-04T10:46:15Z: grafo imutável,
identidade nodeId, contexto efémero por connectionId, validação contextual,
resposta completa antes de EOF, deadline total e terminação das confirmações.
O retrato de Model preserva a base de 155 ficheiros de Projeto/ fora de target.
As etapas seguintes acrescentaram apenas os cinco ficheiros autorizados ao diff;
os outros 150 mantêm os hashes. M01–M18 têm prova incremental no walkthrough,
com M18 parcial até Test. Os 824 testes referidos no Model pertencem ao IMP-002.

Model aprovado explicitamente em 2026-11-04T10:50:52Z, incluindo coerência de
senderInstance/Endpoint.instanceId e uma decisão de resposta por conexão;
[Q-C-04](../../../docs/construction-duvidas.md#q-c-04--coerência-da-instância-e-resposta-única-na-consulta-inicial)
regista a aprovação. [Design](ddd-02-technical-design.md) preparado em 2026-11-04T11:04:53Z:
fila comum/inFlight, confirmações no escritor fora de locks, requestClose aditivo,
deadlines/saturação/shutdown, hosts normalizados e injeções/oráculos M01–M18.
Design aprovado explicitamente em 2026-11-04T11:25:40Z, incluindo requestClose sem espera
e preservação de close público. Autorizada apenas ADR; [revisão preparada](adr-review.md)
em 2026-11-04T11:29:33Z: conformidade com 013/014/015/017/020 e decisões transversais,
necessidade das mudanças internas, limites/reservas e M01–M18 preservados.
ADR-003 permanece substituída; nenhuma decisão aceite inviável/divergência
material identificada nesta análise. Sem ADR nova ou alteração do ADD.
Revisão ADR aprovada explicitamente em 2026-11-04T11:37:45Z; autorizado apenas Implement.
[Walkthrough](implementation-walkthrough.md) preparado em 2026-11-04T12:08:01Z, com red real,
fila/escrita/deadlines e mapa M01–M18. Último incremento: 856 testes aprovados
(740 configuração +66 codec +31 transporte +19 bootstrap), zero falhas/erros/skips.
Cinco ficheiros +1226/−35; 18 testes antigos de transporte intactos e remoções
linha a linha justificadas. Código/API/caps fora do IMP e evidências anteriores
preservados. Listas target/ geradas separadas. V-006 parcial: clean verify da etapa
Test, manual, revisão final por Waheed e integração pendentes.
Implement aprovado explicitamente em 2026-11-04T12:18:01Z; autorizada apenas Test.
[Relatório Test](ddd-03-test-report.md) preparado em 2026-11-04T12:29:39Z: clean verify/JDK21
em 2026-11-04T12:18:10Z, 856 testes, zero falhas/erros/skips e saída 0. Manual
Codex em 2026-11-04T12:22:53Z: 34 CHECK_PASS/MANUAL_PASS, saída Java 0.
V-006/M01–M18 com execução técnica; nenhum dos 155 hashes mudou nesta etapa.
[Comandos de repetição pessoal](../../../experiments/construction/002-overlay/manual-bootstrap.md)
disponíveis e repetidos pessoalmente por Luis em 2026-11-04T13:58:36Z–13:58:38Z:
34 CHECK_PASS, MANUAL_PASS e JAVA_EXIT=0, mesmo hash do JAR validado.
[Log pessoal preservado](../../../experiments/construction/002-overlay/test-manual-luis.log),
separado do agente. Test aprovado explicitamente, com registo em 2026-11-04T14:05:14Z;
concluídas as cinco etapas em stages_completed. Revisão final atribuída a Waheed
confirmada pelo utilizador — «fecha o bolt, a revisão esta feita» — com registo
em 2026-11-04T14:18:23Z. Versão revista já integrada em main: 03345e9e1ab56ec04302676cca6fce32d168223d.
[Fecho concluído](closure-walkthrough.md) em 2026-11-04T14:18:23Z pelo mecanismo specsmd:
bolt/história complete, implemented=true na história, current_stage=null.
TASK-006 DONE; unidade permanece in-progress, três bolts planned. Nenhum outro IMP iniciado.

## Fonte normativa e âmbito

Ler [IMP-003](../../../add.md#imp-003) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-006](../../../docs/tasks/PHASE-02/TASK-006.md).

## História, dependências e gates

- [x] [001-fornecer-localizacao-inicial-dos-vizinhos-configurados](../../intents/001-ott-streaming/units/002-overlay/stories/001-fornecer-localizacao-inicial-dos-vizinhos-configurados.md).
- Dependências: [001-base-validada](../001-base-validada/bolt.md), [019-overlay](../019-overlay/bolt.md).
- Unidade: [002-overlay](../../intents/001-ott-streaming/units/002-overlay/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-003 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-003 e as ADR do ADD.
3. [x] **adr** → `adr-review.md`; validar a etapa contra IMP-003 e as ADR do ADD.
4. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-003 e as ADR do ADD.
5. [x] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-003 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [x] Resposta corresponde ao grafo e respeita X por nó.
- [x] Resposta independe da disponibilidade e de consultas anteriores; não existem registo vivo, lease, renovação ou monitorização.
- [x] IDs e endpoints inválidos são rejeitados sem alterar a configuração ou bloquear outras consultas.
- [x] Conexão inicial é fechada após resposta; não há mensagens de saída/renovação para bootstrap.
- [x] Confirmação de escrita/flush tem prazo e termina em sucesso ou erro; testes observam resposta completa antes de EOF e não deixam futures pendentes após falha/shutdown.
- [x] Validação V-006 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-006, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-003](../../../add.md#imp-003).
- Evidência executada: `experiments/construction/002-overlay/`, relatório Test e walkthrough de fecho.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
