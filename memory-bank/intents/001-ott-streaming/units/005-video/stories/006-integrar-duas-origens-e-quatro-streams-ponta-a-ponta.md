---
id: "006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta"
unit: "005-video"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-024"]
implementation_tasks: ["IMP-023"]
requirements: ["INT-001", "INT-002", "REQ-007", "REQ-012", "REQ-013", "REQ-015", "REQ-016", "REQ-023"]
adr_ids: ["ADR-006", "ADR-008", "ADR-020", "ADR-024"]
bolt: "028-video"
size: "M"
---
# História — Integrar duas origens e quatro streams

## Valor e âmbito

Como responsável pelo serviço OTT, quero integrar duas origens e quatro streams, para cumprir os drivers INT-001, INT-002, REQ-007, REQ-012, REQ-013, REQ-015, REQ-016, REQ-023 com validação concreta.

A tarefa normativa é [IMP-023](../../../../../../add.md#imp-023); o bolt só organiza a execução. A origem legada TASK-024 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Quatro fluxos distintos são utilizáveis.
- [ ] Partilha e isolamento passam a validação local.
- [ ] Versão integrada encerra sem threads/sockets pendentes.
- [ ] Validação V-024 executada e evidência ligada no progresso.

## Testes

- V-024: duas origens anunciam e fornecem quatro vídeos; sinks confirmam stream/session/conteúdo; partilha e isolamento são medidos; GUI reproduz pelo menos uma amostra de cada UUID com evidência.

## Fonte, dependências e revisão

- [TASK-024](../../../../../../docs/tasks/PHASE-05/TASK-024.md).
- Bolts anteriores: 009-arvores, 013-video, 026-video.
- Responsável de origem: Denji; revisor: Waheed.
- Gates: G-GUI.
