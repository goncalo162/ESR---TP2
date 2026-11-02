---
id: "002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao"
unit: "005-video"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-020"]
implementation_tasks: ["IMP-017"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-012", "REQ-013", "REQ-015", "REQ-018"]
adr_ids: ["ADR-006", "ADR-009", "ADR-021", "ADR-024"]
bolt: "011-video"
size: "M"
---
# História — Preparar quatro conteúdos distintos

## Valor e âmbito

Como responsável pelo serviço OTT, quero preparar quatro conteúdos distintos, para cumprir os drivers INT-001, INT-002, NFR-001, REQ-012, REQ-013, REQ-015, REQ-018 com validação concreta.

A tarefa normativa é [IMP-017](../../../../../../add.md#imp-017); o bolt só organiza a execução. A origem legada TASK-020 é parcial (A); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] Quatro conteúdos identificáveis, dois por origem, preparados no perfil de cinco dígitos/JPEG.
- [ ] Proveniência, parâmetros de preparação, hashes e FPS registados; cópias idênticas não contam como vídeos distintos.
- [ ] Cada JPEG respeita o limite e é descodificável; condições de redistribuição ainda pendentes ficam explícitas para TASK-030.

## Testes

- V-020-A: inspeção de todos os registos/JPEG e comparação de conteúdos/hashes; recurso indisponível mantém gate G-MEDIA.

## Fonte, dependências e revisão

- [TASK-020](../../../../../../docs/tasks/PHASE-05/TASK-020.md).
- Bolts anteriores: 009-arvores, 010-video.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: G-MEDIA.
