---
id: "007-ler-o-perfil-mjpeg-confirmado"
unit: "005-video"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-020"]
implementation_tasks: ["IMP-018"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-012", "REQ-013", "REQ-015", "REQ-018"]
adr_ids: ["ADR-006", "ADR-009", "ADR-021", "ADR-024"]
bolt: "025-video"
size: "M"
---
# História — Ler o perfil MJPEG confirmado

## Valor e âmbito

Como responsável pelo serviço OTT, quero ler o perfil mjpeg confirmado, para cumprir os drivers INT-001, INT-002, NFR-001, REQ-012, REQ-013, REQ-015, REQ-018 com validação concreta.

A tarefa normativa é [IMP-018](../../../../../../add.md#imp-018); o bolt só organiza a execução. A origem legada TASK-020 é parcial (B); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] Ler L bytes após cinco dígitos ASCII; EOF só é limpo entre registos.
- [ ] Prefixo inválido/parcial, zero, excesso de tamanho, truncatura e JPEG inválido têm erro localizado antes de publicação.
- [ ] Fixtures independentes produzem os bytes esperados e o leitor fecha recursos.

## Testes

- V-020-B: ficheiros de dois frames, leituras parciais, cada truncatura e limite; teste de ImageIO sem GUI.

## Fonte, dependências e revisão

- [TASK-020](../../../../../../docs/tasks/PHASE-05/TASK-020.md).
- Bolts anteriores: 009-arvores, 011-video.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: nenhum específico adicional.
