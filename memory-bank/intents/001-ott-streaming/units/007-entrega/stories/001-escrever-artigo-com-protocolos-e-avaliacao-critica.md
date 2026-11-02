---
id: "001-escrever-artigo-com-protocolos-e-avaliacao-critica"
unit: "007-entrega"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-029"]
implementation_tasks: ["IMP-030"]
requirements: ["CON-002", "CON-003", "INT-001", "INT-002", "NFR-005", "REQ-022"]
adr_ids: ["ADR-023", "ADR-024"]
bolt: "017-entrega"
size: "M"
---
# História — Produzir e rever o artigo

## Valor e âmbito

Como responsável pelo serviço OTT, quero produzir e rever o artigo, para cumprir os drivers CON-002, CON-003, INT-001, INT-002, NFR-005, REQ-022 com validação concreta.

A tarefa normativa é [IMP-030](../../../../../../add.md#imp-030); o bolt só organiza a execução. A origem legada TASK-029 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Artigo cobre arquitetura, protocolos, implementação e avaliação.
- [ ] Até 12 páginas no ficheiro final.
- [ ] Todas as afirmações experimentais têm evidência e limites.
- [ ] Validação V-029 executada e evidência ligada no progresso.

## Testes

- V-029: revisão por checklist do enunciado; correspondência de figuras/tabelas com dados; contagem de páginas do PDF até 12; protocolo no artigo corresponde aos bytes e comportamento implementados.

## Fonte, dependências e revisão

- [TASK-029](../../../../../../docs/tasks/PHASE-07/TASK-029.md).
- Bolts anteriores: 031-core.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: nenhum específico adicional.
