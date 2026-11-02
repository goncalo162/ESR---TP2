---
id: "002-gerir-handshake-unicidade-e-reconexao-de-vizinhos"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-007"]
implementation_tasks: ["IMP-004"]
requirements: ["INT-001", "INT-002", "NFR-004", "REQ-002", "REQ-003", "REQ-004", "REQ-021"]
adr_ids: ["ADR-012", "ADR-013", "ADR-018", "ADR-019", "ADR-021", "ADR-024"]
bolt: "003-overlay"
size: "M"
---
# História — Estabelecer e reconectar vizinhos

## Valor e âmbito

Como responsável pelo serviço OTT, quero estabelecer e reconectar vizinhos, para cumprir os drivers INT-001, INT-002, NFR-004, REQ-002, REQ-003, REQ-004, REQ-021 com validação concreta.

A tarefa normativa é [IMP-004](../../../../../../add.md#imp-004); o bolt só organiza a execução. A origem legada TASK-007 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Uma conexão confirmada por vizinho.
- [ ] Entrada tardia e reconexão funcionam.
- [ ] Endereço UDP só é aceite após handshake.
- [ ] Validação V-007 executada e evidência ligada no progresso.

## Testes

- V-007: três nós iniciados em ordens diferentes convergem para o grafo esperado sem duplicados; novo instanceId após reinício não é apagado por callback antigo; vizinho desconhecido é rejeitado; após descoberta, desligar bootstrap e comprovar reconexão direta sem novas consultas.

## Fonte, dependências e revisão

- [TASK-007](../../../../../../docs/tasks/PHASE-02/TASK-007.md).
- Bolts anteriores: 001-base-validada, 002-overlay.
- Responsável de origem: Waheed; revisor: Denji.
- Gates: nenhum específico adicional.
