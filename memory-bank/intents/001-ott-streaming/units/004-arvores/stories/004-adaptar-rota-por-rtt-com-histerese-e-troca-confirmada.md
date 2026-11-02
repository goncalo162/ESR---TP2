---
id: "004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada"
unit: "004-arvores"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-017"]
implementation_tasks: ["IMP-014"]
requirements: ["INT-001", "INT-002", "NFR-001", "NFR-003", "REQ-009", "REQ-014"]
adr_ids: ["ADR-005", "ADR-016", "ADR-024"]
bolt: "024-arvores"
size: "M"
---
# História — Trocar parent com histerese

## Valor e âmbito

Como responsável pelo serviço OTT, quero trocar parent com histerese, para cumprir os drivers INT-001, INT-002, NFR-001, NFR-003, REQ-009, REQ-014 com validação concreta.

A tarefa normativa é [IMP-014](../../../../../../add.md#imp-014); o bolt só organiza a execução. A origem legada TASK-017 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Política corresponde aos parâmetros documentados.
- [ ] Um upstream é confirmado no snapshot.
- [ ] Tráfego transitório e limites da troca são explicitados.
- [ ] Validação V-017 executada e evidência ligada no progresso.

## Testes

- V-017: séries temporais predeterminadas não oscilam junto do limiar; melhoria sustentada troca; falha troca sem esperar permanência; troca falhada mantém parent antigo válido.

## Fonte, dependências e revisão

- [TASK-017](../../../../../../docs/tasks/PHASE-04/TASK-017.md).
- Bolts anteriores: 008-arvores, 022-rotas.
- Responsável de origem: Fillmore; revisor: Waheed.
- Gates: nenhum específico adicional.
