---
id: "003-difundir-anuncios-e-construir-catalogo-e-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-012"]
implementation_tasks: ["IMP-009"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-006", "REQ-009", "REQ-012", "REQ-014", "REQ-016"]
adr_ids: ["ADR-004", "ADR-018", "ADR-024"]
bolt: "006-rotas"
size: "M"
---
# História — Difundir anúncios e catálogo

## Valor e âmbito

Como responsável pelo serviço OTT, quero difundir anúncios e catálogo, para cumprir os drivers INT-001, INT-002, REQ-005, REQ-006, REQ-009, REQ-012, REQ-014, REQ-016 com validação concreta.

A tarefa normativa é [IMP-009](../../../../../../add.md#imp-009); o bolt só organiza a execução. A origem legada TASK-012 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Catálogo inclui origem e validade dos quatro fluxos.
- [ ] Melhores caminhos e alternativas ficam disponíveis.
- [ ] Cache, seq, epoch e path impedem tempestade e regressão a época retirada.
- [ ] Validação V-012 executada e evidência ligada no progresso.

## Testes

- V-012: grafo em losango e grafo cíclico convergem para custo esperado; segunda chegada melhor é aceite; quatro streams aparecem; retirar uma origem não remove streams da outra; flooding estabiliza.

## Fonte, dependências e revisão

- [TASK-012](../../../../../../docs/tasks/PHASE-03/TASK-012.md).
- Bolts anteriores: 004-overlay, 021-rotas.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: nenhum específico adicional.
