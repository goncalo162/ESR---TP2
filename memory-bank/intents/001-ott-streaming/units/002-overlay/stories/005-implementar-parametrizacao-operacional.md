---
id: "005-implementar-parametrizacao-operacional"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-032"]
implementation_tasks: ["IMP-002"]
requirements: ["CON-009", "INT-001", "INT-002", "REQ-021", "REQ-024"]
adr_ids: ["ADR-007", "ADR-010", "ADR-016", "ADR-024", "ADR-026"]
bolt: "019-overlay"
size: "M"
---
# História — Implementar parametrização operacional

## Valor e âmbito

Como responsável pelo serviço OTT, quero implementar parametrização operacional, para cumprir os drivers CON-009, INT-001, INT-002, REQ-021, REQ-024 com validação concreta.

A tarefa normativa é [IMP-002](../../../../../../add.md#imp-002); o bolt só organiza a execução. A origem legada TASK-032 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Chaves, defaults, papéis e relações correspondem ao ADD secção 6.3.
- [ ] Carregamento inválido falha antes de rede e sem configuração parcial.
- [ ] Perfis existentes continuam válidos sem propriedades novas.
- [ ] V-032 e revisão/integração exigidas têm evidência real.

## Testes

- V-032: perfis atuais mantêm os defaults; overrides válidos são preservados;
valores/papéis/relações inválidos produzem ConfigurationException agregada,
sem sockets/estado parcial; regressão de configuração/codec/transporte preservada.

## Fonte, dependências e revisão

- [TASK-032](../../../../../../docs/tasks/PHASE-02/TASK-032.md).
- Bolts anteriores: 001-base-validada.
- Responsável de origem: Construction; revisor: Equipa.
- Gates: G-REVIEW-032.
