---
id: "003-preparar-defesa-e-checklist-de-submissao-humana"
unit: "007-entrega"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-031"]
implementation_tasks: ["IMP-032"]
requirements: ["CON-005", "CON-006", "CON-007", "INT-001", "INT-002", "NFR-005"]
adr_ids: ["ADR-023", "ADR-024"]
bolt: "032-entrega"
size: "S"
---
# História — Preparar e ensaiar a defesa

## Valor e âmbito

Como responsável pelo serviço OTT, quero preparar e ensaiar a defesa, para cumprir os drivers CON-005, CON-006, CON-007, INT-001, INT-002, NFR-005 com validação concreta.

A tarefa normativa é [IMP-032](../../../../../../add.md#imp-032); o bolt só organiza a execução. A origem legada TASK-031 é parcial (A); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] Ensaio cobre critérios de avaliação, decisões, demonstrações intermédias e limites conhecidos.
- [ ] Roteiro identifica exatamente PDF/ZIP e versão validados e possui fallback de evidência real.
- [ ] Prazo/grupo/artefactos conhecidos ou pendências explícitas; preparação não equivale a upload.

## Testes

- V-031-A: ensaio cronometrado e revisão da correspondência versão defendida/pacote.

## Fonte, dependências e revisão

- [TASK-031](../../../../../../docs/tasks/PHASE-07/TASK-031.md).
- Bolts anteriores: 018-entrega, 031-core.
- Responsável de origem: Denji; revisor: Waheed.
- Gates: G-ASSESSMENTS, G-DELIVERY.
