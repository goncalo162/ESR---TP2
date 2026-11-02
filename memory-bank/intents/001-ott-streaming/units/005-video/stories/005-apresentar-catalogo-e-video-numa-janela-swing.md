---
id: "005-apresentar-catalogo-e-video-numa-janela-swing"
unit: "005-video"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-023"]
implementation_tasks: ["IMP-022"]
requirements: ["INT-001", "INT-002", "REQ-015", "REQ-016", "REQ-018"]
adr_ids: ["ADR-006", "ADR-024"]
bolt: "013-video"
size: "M"
---
# História — Apresentar catálogo e vídeo Swing

## Valor e âmbito

Como responsável pelo serviço OTT, quero apresentar catálogo e vídeo swing, para cumprir os drivers INT-001, INT-002, REQ-015, REQ-016, REQ-018 com validação concreta.

A tarefa normativa é [IMP-022](../../../../../../add.md#imp-022); o bolt só organiza a execução. A origem legada TASK-023 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Janela reproduz frames de conteúdo identificável.
- [ ] Seleção e stop respeitam uma subscrição local.
- [ ] Rede e descodificação não bloqueiam EDT.
- [ ] Validação V-023 executada e evidência ligada no progresso.

## Testes

- V-023: testar presenter e frame sink sem GUI; na bancada gráfica verificar vídeo, seleção e fecho manualmente, registando imagem/roteiro. Ausência de display é erro explicativo, não sucesso GUI.

## Fonte, dependências e revisão

- [TASK-023](../../../../../../docs/tasks/PHASE-05/TASK-023.md).
- Bolts anteriores: 009-arvores, 027-video.
- Responsável de origem: Denji; revisor: Fillmore.
- Gates: G-GUI.
