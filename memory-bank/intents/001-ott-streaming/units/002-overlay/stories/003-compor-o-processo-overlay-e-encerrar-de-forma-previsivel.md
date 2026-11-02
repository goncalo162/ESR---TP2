---
id: "003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-008"]
implementation_tasks: ["IMP-005"]
requirements: ["INT-001", "INT-002", "NFR-003", "REQ-001", "REQ-002", "REQ-023"]
adr_ids: ["ADR-013", "ADR-014", "ADR-016", "ADR-017", "ADR-019", "ADR-020", "ADR-022", "ADR-024"]
bolt: "020-overlay"
size: "M"
---
# História — Compor CLI e ciclo de processo

## Valor e âmbito

Como responsável pelo serviço OTT, quero compor cli e ciclo de processo, para cumprir os drivers INT-001, INT-002, NFR-003, REQ-001, REQ-002, REQ-023 com validação concreta.

A tarefa normativa é [IMP-005](../../../../../../add.md#imp-005); o bolt só organiza a execução. A origem legada TASK-008 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] BOOTSTRAP, SERVER, RELAY e CLIENT arrancam no papel pedido.
- [ ] Códigos de erro e logs são coerentes.
- [ ] O fim de um processo não exige reiniciar os restantes.
- [ ] Validação V-008 executada e evidência ligada no progresso.

## Testes

- V-008: subprocessos locais iniciam por CLI, mostram vizinhos e terminam; configuração inválida não abre porto; interrupção e erro de um peer não deixam processo preso.

## Fonte, dependências e revisão

- [TASK-008](../../../../../../docs/tasks/PHASE-02/TASK-008.md).
- Bolts anteriores: 001-base-validada, 003-overlay, 019-overlay.
- Responsável de origem: Denji; revisor: Waheed.
- Gates: nenhum específico adicional.
