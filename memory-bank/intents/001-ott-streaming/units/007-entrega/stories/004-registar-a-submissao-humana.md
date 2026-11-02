---
id: "004-registar-a-submissao-humana"
unit: "007-entrega"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-031"]
implementation_tasks: ["IMP-033"]
requirements: ["CON-005", "CON-006", "CON-007", "INT-001", "INT-002", "NFR-005"]
adr_ids: ["ADR-023", "ADR-024"]
bolt: "033-entrega"
size: "S"
---
# História — Registar a submissão humana

## Valor e âmbito

Como responsável pelo serviço OTT, quero registar a submissão humana, para cumprir os drivers CON-005, CON-006, CON-007, INT-001, INT-002, NFR-005 com validação concreta.

A tarefa normativa é [IMP-033](../../../../../../add.md#imp-033); o bolt só organiza a execução. A origem legada TASK-031 é parcial (B); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] A equipa confirma upload no grupo correto com nomes reais antes do prazo conhecido.
- [ ] Registar data, versão/hashes e confirmação sem credenciais; IA não automatiza login nem presume upload.
- [ ] TASK-031 e este bolt só ficam DONE/complete após essa confirmação humana.

## Testes

- V-031-B: inspeção humana da submissão e do manifesto; enquanto faltar confirmação, estado permanece pendente.

## Fonte, dependências e revisão

- [TASK-031](../../../../../../docs/tasks/PHASE-07/TASK-031.md).
- Bolts anteriores: 031-core, 032-entrega.
- Responsável de origem: Denji; revisor: Waheed.
- Gates: G-SUBMISSION, G-DELIVERY.
