---
id: "001-ativar-arvores-com-ack-e-procura-agregada"
unit: "004-arvores"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-014"]
implementation_tasks: ["IMP-011"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-007", "REQ-011", "REQ-017", "REQ-021"]
adr_ids: ["ADR-005", "ADR-018", "ADR-024"]
bolt: "007-arvores"
size: "M"
---
# História — Ativar procura com confirmação

## Valor e âmbito

Como responsável pelo serviço OTT, quero ativar procura com confirmação, para cumprir os drivers INT-001, INT-002, REQ-005, REQ-007, REQ-011, REQ-017, REQ-021 com validação concreta.

A tarefa normativa é [IMP-011](../../../../../../add.md#imp-011); o bolt só organiza a execução. A origem legada TASK-014 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Árvore ativa apenas após confirmação válida.
- [ ] Uma referência agregada a montante por stream.
- [ ] Repetições são idempotentes e erros não deixam PENDING eterno.
- [ ] Validação V-014 executada e evidência ligada no progresso.

## Testes

- V-014: dois clientes em tronco comum geram uma subscrição no tronco; ACK atrasado/cancelado não ativa; prefixo incorreto gera PATH_CHANGED; streams independentes mantêm estados separados.

## Fonte, dependências e revisão

- [TASK-014](../../../../../../docs/tasks/PHASE-04/TASK-014.md).
- Bolts anteriores: 022-rotas.
- Responsável de origem: Fillmore; revisor: Waheed.
- Gates: nenhum específico adicional.
