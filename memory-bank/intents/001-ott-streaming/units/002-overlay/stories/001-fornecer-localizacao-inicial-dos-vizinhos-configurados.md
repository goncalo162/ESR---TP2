---
id: "001-fornecer-localizacao-inicial-dos-vizinhos-configurados"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-006"]
implementation_tasks: ["IMP-003"]
requirements: ["INT-001", "INT-002", "REQ-002", "REQ-003", "REQ-004"]
adr_ids: ["ADR-003", "ADR-013", "ADR-014", "ADR-015", "ADR-024"]
bolt: "002-overlay"
size: "M"
---
# História — Descoberta inicial e escrita confirmada

## Valor e âmbito

Como responsável pelo serviço OTT, quero descoberta inicial e escrita confirmada, para cumprir os drivers INT-001, INT-002, REQ-002, REQ-003, REQ-004 com validação concreta.

A tarefa normativa é [IMP-003](../../../../../../add.md#imp-003); o bolt só organiza a execução. A origem legada TASK-006 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Resposta corresponde ao grafo e respeita X por nó.
- [ ] Resposta independe da disponibilidade e de consultas anteriores; não existem registo vivo, lease, renovação ou monitorização.
- [ ] IDs e endpoints inválidos são rejeitados sem alterar a configuração ou bloquear outras consultas.
- [ ] Conexão inicial é fechada após resposta; não há mensagens de saída/renovação para bootstrap.
- [ ] Confirmação de escrita/flush tem prazo e termina em sucesso ou erro; testes observam resposta completa antes de EOF e não deixam futures pendentes após falha/shutdown.
- [ ] Validação V-006 executada e evidência ligada no progresso.

## Testes

- V-006: consultas repetidas/concorrentes recebem só os vizinhos configurados, incluindo offline, com instanceId zero e sem lease; duas instâncias do mesmo ID recebem a mesma lista; operações não permitidas são rejeitadas sem alterar a configuração; a conexão fecha após a resposta completa e não existe renovação ou estado vivo no bootstrap.

## Fonte, dependências e revisão

- [TASK-006](../../../../../../docs/tasks/PHASE-02/TASK-006.md).
- Bolts anteriores: 001-base-validada, 019-overlay.
- Responsável de origem: Fillmore; revisor: Waheed.
- Gates: nenhum específico adicional.
