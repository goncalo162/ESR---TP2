---
id: "004-preparar-evidencia-do-primeiro-marco-intermedio"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-009"]
implementation_tasks: ["IMP-006"]
requirements: ["CON-007", "INT-001", "INT-002", "NFR-005"]
adr_ids: ["ADR-008", "ADR-013", "ADR-023", "ADR-024"]
bolt: "004-overlay"
size: "S"
---
# História — Demonstrar o primeiro marco

## Valor e âmbito

Como responsável pelo serviço OTT, quero demonstrar o primeiro marco, para cumprir os drivers CON-007, INT-001, INT-002, NFR-005 com validação concreta.

A tarefa normativa é [IMP-006](../../../../../../add.md#imp-006); o bolt só organiza a execução. A origem legada TASK-009 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Roteiro repetível e logs datados disponíveis.
- [ ] Limitações da versão intermédia explicitadas.
- [ ] Situação da avaliação registada sem datas inventadas.
- [ ] Validação V-009 executada e evidência ligada no progresso.

## Testes

- V-009: outro elemento consegue repetir a demonstração e identificar consulta inicial, handshake, saída e reconexão nos logs; os nós já descobertos reconectam com bootstrap parado, sem tráfego periódico para ele.

## Fonte, dependências e revisão

- [TASK-009](../../../../../../docs/tasks/PHASE-02/TASK-009.md).
- Bolts anteriores: 001-base-validada, 020-overlay.
- Responsável de origem: Denji; revisor: Fillmore.
- Gates: G-ASSESSMENTS.
