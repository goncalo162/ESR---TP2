---
id: "002-demonstrar-alternativas-e-adaptacao-no-cenario-de-seis-nos"
unit: "006-core"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-026"]
implementation_tasks: ["IMP-026"]
requirements: ["CON-001", "CON-008", "INT-001", "INT-002", "NFR-003", "REQ-006", "REQ-009", "REQ-014", "REQ-020"]
adr_ids: ["ADR-008", "ADR-011", "ADR-024", "ADR-025"]
bolt: "015-core"
size: "M"
---
# História — Validar falhas TCP e limitação UDP

## Valor e âmbito

Como responsável pelo serviço OTT, quero validar falhas tcp e limitação udp, para cumprir os drivers CON-001, CON-008, INT-001, INT-002, NFR-003, REQ-006, REQ-009, REQ-014, REQ-020 com validação concreta.

A tarefa normativa é [IMP-026](../../../../../../add.md#imp-026); o bolt só organiza a execução. A origem legada TASK-026 é parcial (A); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] Morte de processo e quebra TCP recuperam por alternativa até à mesma origem, quando existe; partição fica UNAVAILABLE.
- [ ] Bloqueio apenas UDP com sondas TCP saudáveis é registado como limitação, sem exigir recuperação ou troca de parent.
- [ ] Ensaios têm deadline e cronologia; recuperação IP é distinguida da recuperação aplicacional.

## Testes

- V-026-A: três repetições, caminho antes/depois e interrupção; ensaio UDP-only em janela finita com perda/frames e parent observado.

## Fonte, dependências e revisão

- [TASK-026](../../../../../../docs/tasks/PHASE-06/TASK-026.md).
- Bolts anteriores: 028-video, 029-core.
- Responsável de origem: Fillmore; revisor: Waheed.
- Gates: G-CORE.
