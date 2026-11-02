---
id: "003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa"
unit: "005-video"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-021"]
implementation_tasks: ["IMP-020"]
requirements: ["INT-001", "INT-002", "NFR-001", "NFR-002", "REQ-007", "REQ-010", "REQ-011", "REQ-015", "REQ-021"]
adr_ids: ["ADR-002", "ADR-006", "ADR-016", "ADR-017", "ADR-021", "ADR-022", "ADR-024"]
bolt: "012-video"
size: "M"
---
# História — Encaminhar datagramas pela árvore

## Valor e âmbito

Como responsável pelo serviço OTT, quero encaminhar datagramas pela árvore, para cumprir os drivers INT-001, INT-002, NFR-001, NFR-002, REQ-007, REQ-010, REQ-011, REQ-015, REQ-021 com validação concreta.

A tarefa normativa é [IMP-020](../../../../../../add.md#imp-020); o bolt só organiza a execução. A origem legada TASK-021 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Envios correspondem apenas ao snapshot ativo.
- [ ] Replicação é única por destino em estado estável.
- [ ] Counters distinguem descarte, envio e entrega local.
- [ ] Validação V-021 executada e evidência ligada no progresso.

## Testes

- V-021: fonte→relay→dois sinks recebe uma cópia por fragmento no tronco e duas nas saídas; origem não autorizada/epoch velha não segue; saturar uma saída não bloqueia controlo nem outras streams.

## Fonte, dependências e revisão

- [TASK-021](../../../../../../docs/tasks/PHASE-05/TASK-021.md).
- Bolts anteriores: 009-arvores, 010-video.
- Responsável de origem: Waheed; revisor: Fillmore.
- Gates: nenhum específico adicional.
