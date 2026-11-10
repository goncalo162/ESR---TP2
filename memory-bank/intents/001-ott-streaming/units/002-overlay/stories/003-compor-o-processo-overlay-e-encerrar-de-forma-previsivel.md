---
id: "003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "complete"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-008"]
implementation_tasks: ["IMP-005"]
requirements: ["INT-001", "INT-002", "NFR-003", "REQ-001", "REQ-002", "REQ-023"]
adr_ids: ["ADR-013", "ADR-014", "ADR-016", "ADR-017", "ADR-019", "ADR-020", "ADR-022", "ADR-024"]
bolt: "020-overlay"
size: "M"
implemented: true
---
# História — Compor CLI e ciclo de processo

## Valor e âmbito

Como responsável pelo serviço OTT, quero compor cli e ciclo de processo, para cumprir os drivers INT-001, INT-002, NFR-003, REQ-001, REQ-002, REQ-023 com validação concreta.

A tarefa normativa é [IMP-005](../../../../../../add.md#imp-005); o bolt só organiza a execução. A origem legada TASK-008 mantém os seus critérios e revisão.

## Critérios de aceitação

- [x] BOOTSTRAP, SERVER, RELAY e CLIENT arrancam no papel pedido.
- [x] Códigos de erro e logs são coerentes.
- [x] O fim de um processo não exige reiniciar os restantes.
- [x] Validação V-008 executada e evidência ligada no progresso.

## Testes

- V-008: subprocessos locais iniciam por CLI, mostram vizinhos e terminam; configuração inválida não abre porto; interrupção e erro de um peer não deixam processo preso.

## Fonte, dependências e revisão

- [TASK-008](../../../../../../docs/tasks/PHASE-02/TASK-008.md).
- Bolts anteriores: 001-base-validada, 003-overlay, 019-overlay.
- Responsável de origem: Denji; revisor: Waheed.
- Gates: nenhum específico adicional.

## IMP-005 — checkpoint Model, sem implementação

[Modelo preparado](../../../../../bolts/020-overlay/ddd-01-domain-model.md) em 2026-11-10T17:31:12Z. Dependências
001-base-validada/003-overlay/019-overlay complete, presentes na base pedida
2ea748e. Checkout 90cded6 preservado; Projeto/ e ADD iguais à base. O tipo
continua Simple; apenas preparação Model de Plan, current_stage=plan,
stages_completed=[], completed=null. Q-C-09 regista a lacuna NODE_LEAVE
na API pública, sem alargar os três ficheiros do ADD ou alterar fundações.
Plan não concluído; parar no checkpoint Model. V-008/regressão/manual/revisão
Waheed/integração pendentes. Sem Java/build/testes, commits/push ou outro IMP.

## Plan preparado após aprovação do Model

Model aceite pelo utilizador; Plan de 020-overlay preparado, por aprovar.
Q-C-09: decisão de âmbito resolvida por autorização de extensão aditiva de
lifecycle no NeighborManager e testes próprios; código e validação por realizar.
Base operacional 90cded6, referência histórica 2ea748e. Unidade in-progress,
história sem implemented=true. Sem Implement/Test, commits/push ou DONE.

## IMP-005 — Plan aprovado; Implement limitado ao checkpoint interfaces/testes

Registo em 2026-11-10T17:46:21Z. Plan aprovado explicitamente pelo utilizador, incluindo
Q-C-10 (budget), Q-C-11 (gramática) e Q-C-12 (buffers locais). current_stage=implement;
apenas Plan consta em stages_completed. Interfaces/tipos e testes inicialmente
vermelhos preparados; comportamentos novos continuam pendentes. Test não iniciado.
V-008 completo/L01–L12/M01–M18, regressão completa atual, manual humano antes de
aprovar Test, revisão final Waheed e integração continuam exigidos, sem gates
externos adicionais. Bolt aberto, história não implementada, TASK-008 IN PROGRESS.
Sem commits/push, DONE ou início de outro IMP.

## IMP-005 — Implement preparado para aprovação

Registo em 2026-11-10T18:16:24Z. O utilizador aprovou interfaces/testes vermelhos e autorizou
comportamentos nos cinco ficheiros do Plan. Implement preparado, ainda por aprovar;
current_stage=implement, só Plan em stages_completed. Regressão atual Maven test:
979 testes, zero falhas/erros/ignorados, com 44 CLI e 32 lifecycle novos, TCP e
subprocessos reais. Test não iniciado; clean verify e manual humano antes da sua
aprovação continuam pendentes, tal como revisão final Waheed e integração.
Q-C-09–12 preservadas; send() e testes históricos intactos, sem alteração wire/caps
ou dependências. Não há gates externos adicionais. Bolt aberto, história ainda
não implementada, TASK-008 IN PROGRESS e unidade in-progress. Sem commits/push
ou outro IMP.

## IMP-005 — Implement aprovado; Test para revisão

Aprovação de Implement registada em 2026-11-10T22:05:26Z. O [relatório de Test](../../../../../bolts/020-overlay/test-walkthrough.md)
consolida V-008, clean verify (981 testes, zero falhas/erros/ignorados), o manual
de Luis e a verificação complementar do agente, com as sondagens preliminares
preservadas. Test aguarda aprovação. A revisão final de Waheed e a integração
da versão revista continuam pendentes.

## IMP-005 — Fecho concluído

Registo de 2026-11-10T22:30:30Z. Revisão final atribuída a Waheed dada como concluída
por confirmação do utilizador. A versão revista `8c30b04` foi integrada
por fast-forward em main. Bolt e história concluídos, TASK-008 DONE.
[Registo de fecho](../../../../../bolts/020-overlay/closure-walkthrough.md).
