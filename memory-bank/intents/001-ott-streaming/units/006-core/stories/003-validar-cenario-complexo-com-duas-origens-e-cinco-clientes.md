---
id: "003-validar-cenario-complexo-com-duas-origens-e-cinco-clientes"
unit: "006-core"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-027"]
implementation_tasks: ["IMP-028"]
requirements: ["CON-001", "INT-001", "INT-002", "NFR-002", "REQ-007", "REQ-012", "REQ-013", "REQ-020"]
adr_ids: ["ADR-008", "ADR-011", "ADR-024"]
bolt: "016-core"
size: "M"
---
# História — Validar quatro streams e cinco clientes

## Valor e âmbito

Como responsável pelo serviço OTT, quero validar quatro streams e cinco clientes, para cumprir os drivers CON-001, INT-001, INT-002, NFR-002, REQ-007, REQ-012, REQ-013, REQ-020 com validação concreta.

A tarefa normativa é [IMP-028](../../../../../../add.md#imp-028); o bolt só organiza a execução. A origem legada TASK-027 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Mínimo de dois servidores com dois vídeos cada demonstrado no CORE.
- [ ] Cinco clientes e rotas alternativas têm evidência.
- [ ] Falhas e isolamento são descritos com limites.
- [ ] Validação V-027 executada e evidência ligada no progresso.

## Testes

- V-027: quatro conteúdos identificáveis são recebidos de duas origens; cinco clientes funcionam dentro da capacidade configurada; falhas de uma origem não corrompem a outra; partilha no tronco tem evidência.

## Fonte, dependências e revisão

- [TASK-027](../../../../../../docs/tasks/PHASE-06/TASK-027.md).
- Bolts anteriores: 028-video, 030-core.
- Responsável de origem: Denji; revisor: Fillmore.
- Gates: G-CORE.
