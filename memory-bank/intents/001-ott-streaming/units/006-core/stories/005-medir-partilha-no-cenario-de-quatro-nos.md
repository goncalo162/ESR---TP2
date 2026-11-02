---
id: "005-medir-partilha-no-cenario-de-quatro-nos"
unit: "006-core"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-025"]
implementation_tasks: ["IMP-025"]
requirements: ["CON-001", "INT-001", "INT-002", "NFR-004", "REQ-011", "REQ-020", "REQ-023"]
adr_ids: ["ADR-001", "ADR-008", "ADR-011", "ADR-024"]
bolt: "029-core"
size: "M"
---
# História — Medir partilha no cenário de quatro nós

## Valor e âmbito

Como responsável pelo serviço OTT, quero medir partilha no cenário de quatro nós, para cumprir os drivers CON-001, INT-001, INT-002, NFR-004, REQ-011, REQ-020, REQ-023 com validação concreta.

A tarefa normativa é [IMP-025](../../../../../../add.md#imp-025); o bolt só organiza a execução. A origem legada TASK-025 é parcial (B); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] Duas saídas recebem a mesma stream com uma cópia por chave no tronco estável sem perda configurada.
- [ ] Parar um consumidor preserva o outro; depois do último stop a poda remove vídeo no prazo medido.
- [ ] Capturas e contadores correspondem a ligações lógicas e manifestos identificam a versão/condições.

## Testes

- V-025-B: cruzar (stream,session,frame,index) no tronco e nas saídas; distinguir warm-up e transição.

## Fonte, dependências e revisão

- [TASK-025](../../../../../../docs/tasks/PHASE-06/TASK-025.md).
- Bolts anteriores: 014-core, 028-video.
- Responsável de origem: Waheed; revisor: Denji.
- Gates: G-CORE.
