---
id: "002-empacotar-codigo-e-verificar-reproducao-da-entrega"
unit: "007-entrega"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-030"]
implementation_tasks: ["IMP-031"]
requirements: ["CON-003", "CON-004", "CON-005", "INT-001", "INT-002", "REQ-023"]
adr_ids: ["ADR-023", "ADR-024"]
bolt: "018-entrega"
size: "M"
---
# História — Validar o pacote de entrega

## Valor e âmbito

Como responsável pelo serviço OTT, quero validar o pacote de entrega, para cumprir os drivers CON-003, CON-004, CON-005, INT-001, INT-002, REQ-023 com validação concreta.

A tarefa normativa é [IMP-031](../../../../../../add.md#imp-031); o bolt só organiza a execução. A origem legada TASK-030 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] PDF e ZIP usam turno/grupo reais.
- [ ] Entrega é reproduzível a partir da extração.
- [ ] Não há segredos, caches ou dependências locais não documentadas.
- [ ] Validação V-030 executada e evidência ligada no progresso.

## Testes

- V-030: arquivo abre sem paths absolutos/escape; extração limpa passa instruções e testes; PDF abre e continua até 12 páginas; nomes e hashes correspondem ao manifesto.

## Fonte, dependências e revisão

- [TASK-030](../../../../../../docs/tasks/PHASE-07/TASK-030.md).
- Bolts anteriores: 017-entrega, 031-core.
- Responsável de origem: Waheed; revisor: Fillmore.
- Gates: G-MEDIA-DISTRIBUTION, G-DELIVERY.
