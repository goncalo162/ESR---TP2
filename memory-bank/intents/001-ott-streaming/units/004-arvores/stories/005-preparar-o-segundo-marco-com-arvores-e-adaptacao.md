---
id: "005-preparar-o-segundo-marco-com-arvores-e-adaptacao"
unit: "004-arvores"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-018"]
implementation_tasks: ["IMP-015"]
requirements: ["CON-007", "INT-001", "INT-002", "NFR-005"]
adr_ids: ["ADR-008", "ADR-023", "ADR-024"]
bolt: "009-arvores"
size: "S"
---
# História — Demonstrar o segundo marco

## Valor e âmbito

Como responsável pelo serviço OTT, quero demonstrar o segundo marco, para cumprir os drivers CON-007, INT-001, INT-002, NFR-005 com validação concreta.

A tarefa normativa é [IMP-015](../../../../../../add.md#imp-015); o bolt só organiza a execução. A origem legada TASK-018 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Demonstração mostra isolamento entre fluxos e procura agregada.
- [ ] Falha e alteração de RTT têm evidência verificável.
- [ ] Conteúdo e estado do marco estão registados.
- [ ] Validação V-018 executada e evidência ligada no progresso.

## Testes

- V-018: reprodução independente da ativação/poda/failover usando logs e snapshots; revisão do material contra os critérios de gestão de fluxos e controlo do PDF.

## Fonte, dependências e revisão

- [TASK-018](../../../../../../docs/tasks/PHASE-04/TASK-018.md).
- Bolts anteriores: 022-rotas, 024-arvores.
- Responsável de origem: Denji; revisor: Waheed.
- Gates: G-ASSESSMENTS.
