---
id: "006-medir-adaptacao-por-rtt-e-estabilidade"
unit: "006-core"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-026"]
implementation_tasks: ["IMP-027"]
requirements: ["CON-001", "CON-008", "INT-001", "INT-002", "NFR-003", "REQ-006", "REQ-009", "REQ-014", "REQ-020"]
adr_ids: ["ADR-008", "ADR-011", "ADR-024", "ADR-025"]
bolt: "030-core"
size: "M"
---
# História — Medir adaptação por RTT e estabilidade

## Valor e âmbito

Como responsável pelo serviço OTT, quero medir adaptação por rtt e estabilidade, para cumprir os drivers CON-001, CON-008, INT-001, INT-002, NFR-003, REQ-006, REQ-009, REQ-014, REQ-020 com validação concreta.

A tarefa normativa é [IMP-027](../../../../../../add.md#imp-027); o bolt só organiza a execução. A origem legada TASK-026 é parcial (B); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] Inverter atraso sustentado altera parent só após melhoria/observações/permanência válidas.
- [ ] Ruído junto do limiar não provoca oscilação; falha TCP ignora histerese.
- [ ] Três repetições mostram custos, motivo e interrupção, com limites/metas avaliados sem resultados inventados.

## Testes

- V-026-B: condições fixas/invertidas e ruído; cruzar logs/snapshots, clocks locais e frames.

## Fonte, dependências e revisão

- [TASK-026](../../../../../../docs/tasks/PHASE-06/TASK-026.md).
- Bolts anteriores: 015-core, 028-video.
- Responsável de origem: Fillmore; revisor: Waheed.
- Gates: G-CORE.
