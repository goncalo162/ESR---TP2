---
id: "008-emitir-fontes-com-pacing-e-procura"
unit: "005-video"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-020"]
implementation_tasks: ["IMP-019"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-012", "REQ-013", "REQ-015", "REQ-018"]
adr_ids: ["ADR-006", "ADR-009", "ADR-021", "ADR-024"]
bolt: "026-video"
size: "M"
---
# História — Emitir fontes com pacing e procura

## Valor e âmbito

Como responsável pelo serviço OTT, quero emitir fontes com pacing e procura, para cumprir os drivers INT-001, INT-002, NFR-001, REQ-012, REQ-013, REQ-015, REQ-018 com validação concreta.

A tarefa normativa é [IMP-019](../../../../../../add.md#imp-019); o bolt só organiza a execução. A origem legada TASK-020 é parcial (C); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] Emitir só com procura confirmada, ao FPS configurado, sem catch-up ilimitado.
- [ ] Streams mantêm posição/seq/timeline independentes; loop preserva monotonia e EOF sem loop retira só essa stream.
- [ ] Stop e shutdown cancelam emissão e fecham fontes sem afetar a outra origem.

## Testes

- V-020-C: relógio/scheduler falso demonstra pacing, suspensão/retoma, loop, EOF e isolamento.

## Fonte, dependências e revisão

- [TASK-020](../../../../../../docs/tasks/PHASE-05/TASK-020.md).
- Bolts anteriores: 009-arvores, 025-video.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: nenhum específico adicional.
