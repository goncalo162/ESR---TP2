---
id: "001-definir-codec-udp-e-fragmentacao-de-frames"
unit: "005-video"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-019"]
implementation_tasks: ["IMP-016"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-010", "REQ-015", "REQ-018", "REQ-021"]
adr_ids: ["ADR-002", "ADR-006", "ADR-007", "ADR-009", "ADR-021", "ADR-024"]
bolt: "010-video"
size: "M"
---
# História — Codificar datagramas e fragmentos

## Valor e âmbito

Como responsável pelo serviço OTT, quero codificar datagramas e fragmentos, para cumprir os drivers INT-001, INT-002, NFR-001, REQ-010, REQ-015, REQ-018, REQ-021 com validação concreta.

A tarefa normativa é [IMP-016](../../../../../../add.md#imp-016); o bolt só organiza a execução. A origem legada TASK-019 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Formato coincide com contrato v1.
- [ ] Limites são impostos antes de alocar.
- [ ] Fragmentação cobre todas as fronteiras sem bytes perdidos.
- [ ] Validação V-019 executada e evidência ligada no progresso.

## Testes

- V-019: fixtures de bytes confirmam offsets 0–64; recombinação manual dos payloads recupera exatamente o frame; nenhum datagrama excede 1200 bytes; inputs inválidos são rejeitados.

## Fonte, dependências e revisão

- [TASK-019](../../../../../../docs/tasks/PHASE-05/TASK-019.md).
- Bolts anteriores: 009-arvores.
- Responsável de origem: Waheed; revisor: Denji.
- Gates: nenhum específico adicional.
