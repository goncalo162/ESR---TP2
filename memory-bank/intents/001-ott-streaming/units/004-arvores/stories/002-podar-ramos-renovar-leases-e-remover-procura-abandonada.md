---
id: "002-podar-ramos-renovar-leases-e-remover-procura-abandonada"
unit: "004-arvores"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-015"]
implementation_tasks: ["IMP-012"]
requirements: ["INT-001", "INT-002", "NFR-003", "REQ-002", "REQ-005", "REQ-007", "REQ-009", "REQ-011", "REQ-017"]
adr_ids: ["ADR-005", "ADR-024"]
bolt: "023-arvores"
size: "M"
---
# História — Renovar e podar subscrições

## Valor e âmbito

Como responsável pelo serviço OTT, quero renovar e podar subscrições, para cumprir os drivers INT-001, INT-002, NFR-003, REQ-002, REQ-005, REQ-007, REQ-009, REQ-011, REQ-017 com validação concreta.

A tarefa normativa é [IMP-012](../../../../../../add.md#imp-012); o bolt só organiza a execução. A origem legada TASK-015 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Vídeo só permanece autorizado enquanto há procura efetiva.
- [ ] Ramos órfãos desaparecem por timeout.
- [ ] Outros consumidores e fluxos mantêm-se ativos.
- [ ] Validação V-015 executada e evidência ligada no progresso.

## Testes

- V-015: sair um de dois consumidores mantém tronco ativo; sair o último remove; relógio falso elimina lease órfão; cancelamento velho não remove ramo novo.

## Fonte, dependências e revisão

- [TASK-015](../../../../../../docs/tasks/PHASE-04/TASK-015.md).
- Bolts anteriores: 007-arvores, 022-rotas.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: nenhum específico adicional.
