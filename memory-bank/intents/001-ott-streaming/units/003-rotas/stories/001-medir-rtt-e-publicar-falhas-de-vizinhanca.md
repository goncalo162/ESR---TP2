---
id: "001-medir-rtt-e-publicar-falhas-de-vizinhanca"
unit: "003-rotas"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-010"]
implementation_tasks: ["IMP-007"]
requirements: ["CON-008", "INT-001", "INT-002", "REQ-003", "REQ-008", "REQ-014", "REQ-021"]
adr_ids: ["ADR-004", "ADR-016", "ADR-022", "ADR-024", "ADR-025"]
bolt: "005-rotas"
size: "M"
---
# História — Medir RTT e detetar falhas TCP

## Valor e âmbito

Como responsável pelo serviço OTT, quero medir rtt e detetar falhas tcp, para cumprir os drivers CON-008, INT-001, INT-002, REQ-003, REQ-008, REQ-014, REQ-021 com validação concreta.

A tarefa normativa é [IMP-007](../../../../../../add.md#imp-007); o bolt só organiza a execução. A origem legada TASK-010 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] RTT é calculado sem comparar relógios de máquinas diferentes.
- [ ] Falhas são correlacionadas à conexão correta.
- [ ] Métrica sem amostra não é publicada como zero confiável.
- [ ] Validação V-010 executada e evidência ligada no progresso.

## Testes

- V-010: relógio falso confirma EWMA, reset após nova instância, perda de três sondas e ausência de falso positivo por um timeout; EOF invalida imediatamente.

## Fonte, dependências e revisão

- [TASK-010](../../../../../../docs/tasks/PHASE-03/TASK-010.md).
- Bolts anteriores: 004-overlay.
- Responsável de origem: Waheed; revisor: Fillmore.
- Gates: nenhum específico adicional.
