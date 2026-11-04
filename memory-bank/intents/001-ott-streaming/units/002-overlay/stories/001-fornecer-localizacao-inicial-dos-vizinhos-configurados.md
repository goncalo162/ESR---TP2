---
id: "001-fornecer-localizacao-inicial-dos-vizinhos-configurados"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "complete"
priority: "Must"
created: "2026-11-02T15:36:48Z"
updated: "2026-11-04T12:29:39Z"
source_tasks: ["TASK-006"]
implementation_tasks: ["IMP-003"]
requirements: ["INT-001","INT-002","REQ-002","REQ-003","REQ-004"]
adr_ids: ["ADR-003","ADR-013","ADR-014","ADR-015","ADR-024"]
bolt: "002-overlay"
size: "M"
implemented: true
---
# História — Descoberta inicial e escrita confirmada

## Valor e âmbito

Model preparado no bolt 002-overlay em 2026-11-04T10:46:15Z, após seleção explícita
do utilizador para implementar o bolt e a primeira etapa. [Modelo para revisão](../../../../../bolts/002-overlay/ddd-01-domain-model.md):
consulta ao grafo, identidades, estados efémeros, escrita/flush com prazo e M01–M18.
Dependências 001-base-validada e 019-overlay complete/integradas; sem alteração
Java ou testes executados nesta etapa. Model aprovado em 2026-11-04T10:50:52Z, com Q-C-04; autorizado apenas Design,
[preparado para revisão](../../../../../bolts/002-overlay/ddd-02-technical-design.md) em 2026-11-04T11:04:53Z.
Fila comum/inFlight, future fora de locks, requestClose/prazos, hosts normalizados
e M01–M18 definidos. Design aprovado em 2026-11-04T11:25:40Z, incluindo requestClose
sem espera/close compatível. [Revisão ADR preparada](../../../../../bolts/002-overlay/adr-review.md)
em 2026-11-04T11:29:33Z; sem divergência material/ADR nova identificadas. A aprovação
da revisão ADR foi dada em 2026-11-04T11:37:45Z, autorizando somente Implement.
[Walkthrough](../../../../../bolts/002-overlay/implementation-walkthrough.md) preparado
em 2026-11-04T12:08:01Z: 856 testes incrementais aprovados, cinco ficheiros +1226/−35,
150 hashes restantes e os 18 testes anteriores de transporte preservados.
Implement aprovado em 2026-11-04T12:18:01Z; [Test preparado para revisão](../../../../../bolts/002-overlay/ddd-03-test-report.md)
em 2026-11-04T12:29:39Z. Clean verify/JDK21: 856 testes sem falhas/erros/skips; manual do
agente com 34 CHECK_PASS/MANUAL_PASS e saída 0. V-006/M01–M18 executados
tecnicamente. Test aprovado explicitamente, com registo em 2026-11-04T14:05:14Z, incluindo
manual pessoal de Luis em 2026-11-04T13:58:36Z–13:58:38Z: 34 checks/MANUAL_PASS/
JAVA_EXIT=0, mesmo JAR. [Fecho preparado](../../../../../bolts/002-overlay/closure-walkthrough.md);
commits e push de Denji autorizados. Revisão final atribuída a Waheed e integração
pendentes; história in-progress, sem implemented=true/DONE.


Como responsável pelo serviço OTT, quero descoberta inicial e escrita confirmada, para cumprir os drivers INT-001, INT-002, REQ-002, REQ-003, REQ-004 com validação concreta.

A tarefa normativa é [IMP-003](../../../../../../add.md#imp-003); o bolt só organiza a execução. A origem legada TASK-006 mantém os seus critérios e revisão.

## Critérios de aceitação

- [x] Resposta corresponde ao grafo e respeita X por nó.
- [x] Resposta independe da disponibilidade e de consultas anteriores; não existem registo vivo, lease, renovação ou monitorização.
- [x] IDs e endpoints inválidos são rejeitados sem alterar a configuração ou bloquear outras consultas.
- [x] Conexão inicial é fechada após resposta; não há mensagens de saída/renovação para bootstrap.
- [x] Confirmação de escrita/flush tem prazo e termina em sucesso ou erro; testes observam resposta completa antes de EOF e não deixam futures pendentes após falha/shutdown.
- [x] Validação V-006 executada e evidência ligada no progresso.

## Testes

- V-006: consultas repetidas/concorrentes recebem só os vizinhos configurados, incluindo offline, com instanceId zero e sem lease; duas instâncias do mesmo ID recebem a mesma lista; operações não permitidas são rejeitadas sem alterar a configuração; a conexão fecha após a resposta completa e não existe renovação ou estado vivo no bootstrap.

## Fonte, dependências e revisão

- [TASK-006](../../../../../../docs/tasks/PHASE-02/TASK-006.md).
- Bolts anteriores: 001-base-validada, 019-overlay.
- Responsável de origem: Fillmore; revisor: Waheed.
- Gates: nenhum específico adicional.
