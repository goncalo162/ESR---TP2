---
id: "004-diagnosticar-percursos-pela-tabela-de-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-013"]
implementation_tasks: ["IMP-010"]
requirements: ["INT-001", "INT-002", "REQ-010", "REQ-019", "REQ-023"]
adr_ids: ["ADR-004", "ADR-022", "ADR-024"]
bolt: "022-rotas"
size: "S"
---
# História — Diagnosticar percursos pela CLI

## Valor e âmbito

Como responsável pelo serviço OTT, quero diagnosticar percursos pela cli, para cumprir os drivers INT-001, INT-002, REQ-010, REQ-019, REQ-023 com validação concreta.

A tarefa normativa é [IMP-010](../../../../../../add.md#imp-010); o bolt só organiza a execução. A origem legada TASK-013 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Trace reflete a tabela de cada nó.
- [ ] Falhas terminam por erro/timeout em prazo.
- [ ] Diagnóstico não altera subscrições.
- [ ] Validação V-013 executada e evidência ligada no progresso.

## Testes

- V-013: duas rotas alternativas produzem visited correto; NO_ROUTE e TTL são explícitos; não há ativação nem saída de vídeo após trace.

## Fonte, dependências e revisão

- [TASK-013](../../../../../../docs/tasks/PHASE-03/TASK-013.md).
- Bolts anteriores: 004-overlay, 006-rotas, 020-overlay.
- Responsável de origem: Denji; revisor: Fillmore.
- Gates: nenhum específico adicional.
