---
id: "005-implementar-parametrizacao-operacional"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "complete"
priority: "Must"
created: "2026-11-02T15:36:48Z"
updated: "2026-11-04T10:34:52Z"
source_tasks: ["TASK-032"]
implementation_tasks: ["IMP-002"]
requirements: ["CON-009","INT-001","INT-002","REQ-021","REQ-024"]
adr_ids: ["ADR-007","ADR-010","ADR-016","ADR-024","ADR-026"]
bolt: "019-overlay"
size: "M"
implemented: true
---
# História — Implementar parametrização operacional

## Valor e âmbito

Model aprovado no bolt 019-overlay, em 2026-11-03T22:16:54Z;
Design aprovado pelo utilizador em 2026-11-03T22:25:30Z;
revisão ADR, Implement e Test aprovados. Test concluído em 2026-11-04T10:19:06Z (registo),
824 testes e manual pessoal de Luis com 25 checks/saída Java 0 confirmados.
[Fecho concluído](../../../../../bolts/019-overlay/closure-walkthrough.md) em 2026-11-04T10:33:12Z:
status=complete, implemented=true, por cascata specsmd. Luis confirmou a revisão
final («sim já revi»), distinta da autoria de implementação do agente Codex.
Versão revista integrada em main: 09a068f68eca473451e1e80a045c1cf9cf15a1b2. G-REVIEW-032 cumprido.

Como responsável pelo serviço OTT, quero implementar parametrização operacional, para cumprir os drivers CON-009, INT-001, INT-002, REQ-021, REQ-024 com validação concreta.

A tarefa normativa é [IMP-002](../../../../../../add.md#imp-002); o bolt só organiza a execução. A origem legada TASK-032 mantém os seus critérios e revisão.

## Critérios de aceitação

- [x] Chaves, defaults, papéis e relações correspondem ao ADD secção 6.3.
- [x] Carregamento inválido falha antes de rede e sem configuração parcial.
- [x] Perfis existentes continuam válidos sem propriedades novas.
- [x] V-032 e revisão/integração exigidas têm evidência real.

## Testes

- V-032: perfis atuais mantêm os defaults; overrides válidos são preservados;
valores/papéis/relações inválidos produzem ConfigurationException agregada,
sem sockets/estado parcial; regressão de configuração/codec/transporte preservada.

## Fonte, dependências e revisão

- [TASK-032](../../../../../../docs/tasks/PHASE-02/TASK-032.md).
- Bolts anteriores: 001-base-validada.
- Responsável de origem: Construction; implementação: agente Codex; revisor humano: Luis.
- Gates: G-REVIEW-032.
