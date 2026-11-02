---
id: "002-modelar-candidatos-custos-e-snapshots-por-stream"
unit: "003-rotas"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-011"]
implementation_tasks: ["IMP-008"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-006", "REQ-014", "REQ-021"]
adr_ids: ["ADR-004", "ADR-017", "ADR-018", "ADR-024"]
bolt: "021-rotas"
size: "M"
---
# História — Modelar candidatos e snapshots

## Valor e âmbito

Como responsável pelo serviço OTT, quero modelar candidatos e snapshots, para cumprir os drivers INT-001, INT-002, REQ-005, REQ-006, REQ-014, REQ-021 com validação concreta.

A tarefa normativa é [IMP-008](../../../../../../add.md#imp-008); o bolt só organiza a execução. A origem legada TASK-011 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Cada fluxo tem custo, origem, destinos e estado.
- [ ] Empates são reproduzíveis e caminhos inválidos rejeitados.
- [ ] Expirar um candidato não apaga alternativas válidas.
- [ ] Validação V-011 executada e evidência ligada no progresso.

## Testes

- V-011: custos e desempate calculados com fixtures de resultados conhecidos; quatro streams não interferem; alternativa é preservada e snapshot não pode ser alterado por consumidor.

## Fonte, dependências e revisão

- [TASK-011](../../../../../../docs/tasks/PHASE-03/TASK-011.md).
- Bolts anteriores: 004-overlay, 005-rotas.
- Responsável de origem: Fillmore; revisor: Waheed.
- Gates: nenhum específico adicional.
