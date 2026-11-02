---
id: "004-recolher-metricas-e-avaliar-resultados-e-limites"
unit: "006-core"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-028"]
implementation_tasks: ["IMP-029"]
requirements: ["CON-001", "CON-008", "INT-001", "INT-002", "NFR-001", "NFR-002", "NFR-005", "REQ-008", "REQ-011", "REQ-023"]
adr_ids: ["ADR-008", "ADR-022", "ADR-024", "ADR-025"]
bolt: "031-core"
size: "M"
---
# História — Consolidar métricas e limites

## Valor e âmbito

Como responsável pelo serviço OTT, quero consolidar métricas e limites, para cumprir os drivers CON-001, CON-008, INT-001, INT-002, NFR-001, NFR-002, NFR-005, REQ-008, REQ-011, REQ-023 com validação concreta.

A tarefa normativa é [IMP-029](../../../../../../add.md#imp-029); o bolt só organiza a execução. A origem legada TASK-028 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Resultados são reais, datados e repetíveis.
- [ ] Critérios de eficiência e adaptação têm evidência.
- [ ] Limitações e desvios de metas estão documentados.
- [ ] Validação V-028 executada e evidência ligada no progresso.

## Testes

- V-028: auditoria cruzada entre capturas, contadores e eventos de rota; tabela reproduzível de condições/resultados; análise distingue perda de datagrama, frame incompleto e frame não mostrado.

## Fonte, dependências e revisão

- [TASK-028](../../../../../../docs/tasks/PHASE-06/TASK-028.md).
- Bolts anteriores: 016-core, 028-video.
- Responsável de origem: Denji; revisor: Fillmore.
- Gates: G-CORE.
