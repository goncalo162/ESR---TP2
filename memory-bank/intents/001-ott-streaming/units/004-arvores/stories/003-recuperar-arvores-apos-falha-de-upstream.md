---
id: "003-recuperar-arvores-apos-falha-de-upstream"
unit: "004-arvores"
intent: "001-ott-streaming"
status: "complete"
implemented: true
completed: "2026-11-20T16:23:16Z"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-016"]
implementation_tasks: ["IMP-013"]
requirements: ["CON-008", "INT-001", "INT-002", "NFR-003", "REQ-002", "REQ-006", "REQ-009"]
adr_ids: ["ADR-005", "ADR-020", "ADR-024", "ADR-025"]
bolt: "008-arvores"
size: "M"
---
# História — Recuperar falhas do upstream TCP

## Valor e âmbito

Como responsável pelo serviço OTT, quero recuperar falhas do upstream tcp, para cumprir os drivers CON-008, INT-001, INT-002, NFR-003, REQ-002, REQ-006, REQ-009 com validação concreta.

A tarefa normativa é [IMP-013](../../../../../../add.md#imp-013); o bolt só organiza a execução. A origem legada TASK-016 mantém os seus critérios e revisão.

## Critérios de aceitação

- [x] Há recuperação quando existe caminho até à mesma fonte.
- [x] Ausência de caminho é visível e não produz laço.
- [x] Estado antigo não é reativado por mensagens atrasadas.
- [x] Validação V-016 executada e evidência ligada no progresso.

## Testes

- V-016: cortar upstream num losango leva a caminho alternativo sem ciclo; sem alternativa termina UNAVAILABLE; regresso permite recuperar; streams da outra origem continuam.

## Fonte, dependências e revisão

- [TASK-016](../../../../../../docs/tasks/PHASE-04/TASK-016.md).
- Bolts anteriores: 022-rotas, 023-arvores.
- Responsável de origem: Waheed; revisor: Fillmore.
- Gates: nenhum específico adicional.

## Execução e fecho

Test/V-0161419/0/0/0,20 XML,manual Luis7/28,Fillmore e integração
447ae4bf68bf96329bd1a0c7e16ed20df06bc55b comprovados. [Fecho](../../../../../bolts/008-arvores/closure-walkthrough.md).
R17 inicial sem causa estabelecida; métricas controladas não demonstram a causa.
