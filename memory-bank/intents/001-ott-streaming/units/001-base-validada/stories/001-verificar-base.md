---
id: "001-verificar-base"
unit: "001-base-validada"
intent: "001-ott-streaming"
status: "complete"
priority: "Must"
created: "2026-11-02T15:36:48Z"
updated: "2026-11-03T21:59:14Z"
source_tasks: ["TASK-001","TASK-002","TASK-003","TASK-004","TASK-005"]
implementation_tasks: ["IMP-001"]
requirements: ["CON-009","INT-001","INT-002","NFR-004","NFR-005","REQ-001","REQ-003","REQ-004","REQ-012","REQ-013","REQ-016","REQ-021"]
adr_ids: ["ADR-001","ADR-002","ADR-007","ADR-008","ADR-009","ADR-010","ADR-012","ADR-014","ADR-020","ADR-021","ADR-024","ADR-026"]
bolt: "001-base-validada"
size: "S"
implemented: true
completed: "2026-11-03T21:56:48Z"
---
# História — Verificar a base herdada

## Valor e âmbito

Como responsável pelo serviço OTT, quero verificar a base herdada, para cumprir os drivers CON-009, INT-001, INT-002, NFR-004, NFR-005, REQ-001, REQ-003, REQ-004, REQ-012, REQ-013, REQ-016, REQ-021 com validação concreta.

A tarefa normativa é [IMP-001](../../../../../../add.md#imp-001); o bolt só organiza a execução. A origem legada TASK-001, TASK-002, TASK-003, TASK-004, TASK-005 mantém os seus critérios e revisão.

## Critérios de aceitação

- [x] Confirmar componentes reais e manter TASK-001–005 DONE, sem os reimplementar.
- [x] Conferir 45 fixtures, 16 tipos ativos e o ADD/specs/DAG antes de libertar nova construção.
- [x] Executar a regressão existente no ambiente de Construction, guardando um novo log; 149 é total histórico, não resultado atual.

## Testes

- V-001: inspeção do POM efetivo e build limpo com release 21; verificar engine escolhido. Zero testes nesta base não valida funcionalidades; repetir descoberta real em V-003.
- V-002: revisão independente dos comprimentos, offsets, exemplos de identidade e tabela de requisitos; evidência de acesso ou falha de recurso, sem execução de streaming.
- V-003: fixtures válidas/invalidas com diretório temporário, paths fora do cwd, ID de um carácter e quatro streams; Maven reporta testes executados e falha perante um requisito de configuração violado.
- V-004: golden bytes para HELLO/ANNOUNCE/ACTIVATE/ERROR e descoberta, round-trip dos 16 tipos ativos, leituras parciais, concatenação e rejeições sem alocação não limitada. Rejeitar código 3, layout antigo de resposta com lease e vizinhos com instância viva na resposta bootstrap.
- V-005: dois endpoints loopback com portas efémeras trocam mensagens simultâneas intactas; parar um gera evento único; reconexão e saturação não deixam threads/sockets abertos.

## Fonte, dependências e revisão

- [TASK-001](../../../../../../docs/tasks/PHASE-01/TASK-001.md).
- Bolts anteriores: nenhum.
- Responsável de origem: histórico; revisor: histórico.
- Gates: nenhum específico adicional.

## Fecho de IMP-001

A cascata de fecho marcou esta história complete/implemented=true em 2026-11-03T21:56:48Z;
os critérios foram verificados e os resultados/fecho aprovados pelo utilizador.
Versão revista integrada em main: d65f94740ef95ffc67d312f7fbccac7575c86f7a.
[Relatório final](../../../../../../memory-bank/bolts/001-base-validada/closure-walkthrough.md)
e [teste manual por Luis](../../../../../../experiments/construction/001-base-validada/manual-smoke-luis.log),
com 22 CHECK_PASS e MANUAL_PASS. TASK-001–005 mantêm os seus estados DONE.
