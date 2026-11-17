---
id: "001-ativar-arvores-com-ack-e-procura-agregada"
unit: "004-arvores"
intent: "001-ott-streaming"
status: "complete"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-014"]
implementation_tasks: ["IMP-011"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-007", "REQ-011", "REQ-017", "REQ-021"]
adr_ids: ["ADR-005", "ADR-018", "ADR-024"]
bolt: "007-arvores"
size: "M"
implemented: true
completed: "2026-11-17T23:51:24Z"
---
# História — Ativar procura com confirmação

## Valor e âmbito

Como responsável pelo serviço OTT, quero ativar procura com confirmação, para cumprir os drivers INT-001, INT-002, REQ-005, REQ-007, REQ-011, REQ-017, REQ-021 com validação concreta.

A tarefa normativa é [IMP-011](../../../../../../add.md#imp-011); o bolt só organiza a execução. A origem legada TASK-014 mantém os seus critérios e revisão.

## Critérios de aceitação

- [x] Árvore ativa apenas após confirmação válida.
- [x] Uma referência agregada a montante por stream.
- [x] Repetições são idempotentes e erros não deixam PENDING eterno.
- [x] Validação V-014 executada e evidência ligada no progresso.

## Testes

- V-014: dois clientes em tronco comum geram uma subscrição no tronco; ACK atrasado/cancelado não ativa; prefixo incorreto gera PATH_CHANGED; streams independentes mantêm estados separados.

## Fonte, dependências e revisão

- [TASK-014](../../../../../../docs/tasks/PHASE-04/TASK-014.md).
- Bolts anteriores: 022-rotas.
- Responsável de origem: Fillmore; revisor: Waheed.
- Gates: nenhum específico adicional.

## Evidência atual

Implement aprovado; [Test técnico](../../../../../bolts/007-arvores/ddd-03-test-report.md)
1328/0/0/0 e V-014 consolidado. Manual humano e aprovação de Test pendentes;
revisão final Waheed e integração também pendentes. História não concluída.

Manual humano Francisco e revisão final Waheed confirmados/conferidos para
a versão atual; Test e integração continuam pendentes. História in-progress,
sem declaração de implementação concluída/fecho.

## Fecho autorizado

Complete, implemented=true, TASK-014 DONE após Test1328/0/0/0, manual humano
Francisco24, revisão final Waheed e integração local37fa863 comprovada.
[Fecho](../../../../../bolts/007-arvores/closure-walkthrough.md).
Unidade004-arvores/PHASE-04 em progresso; sem push/outro IMP.
