---
id: "004-reconstituir-frames-com-buffers-e-prazo-limitados"
unit: "005-video"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-022"]
implementation_tasks: ["IMP-021"]
requirements: ["INT-001", "INT-002", "NFR-001", "NFR-003", "REQ-015", "REQ-018", "REQ-021"]
adr_ids: ["ADR-002", "ADR-006", "ADR-016", "ADR-018", "ADR-024"]
bolt: "027-video"
size: "M"
---
# História — Reconstituir frames com prazo

## Valor e âmbito

Como responsável pelo serviço OTT, quero reconstituir frames com prazo, para cumprir os drivers INT-001, INT-002, NFR-001, NFR-003, REQ-015, REQ-018, REQ-021 com validação concreta.

A tarefa normativa é [IMP-021](../../../../../../add.md#imp-021); o bolt só organiza a execução. A origem legada TASK-022 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Reordenação não corrompe frames completos.
- [ ] Perda e overflow têm fim e razão observáveis.
- [ ] Não existe espera ilimitada por um fragmento.
- [ ] Validação V-022 executada e evidência ligada no progresso.

## Testes

- V-022: permutações determinísticas recuperam frame; perda expira; duplicado não aumenta memória nem deadline; flood não supera oito montagens; epoch novo limpa buffers. O teste de JPEG corrompido pertence ao decoder/player em V-023.

## Fonte, dependências e revisão

- [TASK-022](../../../../../../docs/tasks/PHASE-05/TASK-022.md).
- Bolts anteriores: 009-arvores, 012-video.
- Responsável de origem: Denji; revisor: Waheed.
- Gates: nenhum específico adicional.
