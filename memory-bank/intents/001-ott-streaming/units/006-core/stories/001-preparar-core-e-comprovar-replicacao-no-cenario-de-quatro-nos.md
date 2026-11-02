---
id: "001-preparar-core-e-comprovar-replicacao-no-cenario-de-quatro-nos"
unit: "006-core"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-025"]
implementation_tasks: ["IMP-024"]
requirements: ["CON-001", "INT-001", "INT-002", "NFR-004", "REQ-011", "REQ-020", "REQ-023"]
adr_ids: ["ADR-001", "ADR-008", "ADR-011", "ADR-024"]
bolt: "014-core"
size: "M"
---
# História — Preparar ambiente e mapa CORE

## Valor e âmbito

Como responsável pelo serviço OTT, quero preparar ambiente e mapa core, para cumprir os drivers CON-001, INT-001, INT-002, NFR-004, REQ-011, REQ-020, REQ-023 com validação concreta.

A tarefa normativa é [IMP-024](../../../../../../add.md#imp-024); o bolt só organiza a execução. A origem legada TASK-025 é parcial (A); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] JDK/JAR/volumes/display verificados em sessão CORE real.
- [ ] Mapa S/R/C1/C2 e bootstrap, IPs/interfaces/portas e rotas IP documentados; advertised UDP pertence ao nó.
- [ ] Perfis válidos e reachability confirmada sem substituir o XML por defeito.

## Testes

- V-025-A: preflight e arranque/terminação dos processos; não alegar cópia única nesta preparação.

## Fonte, dependências e revisão

- [TASK-025](../../../../../../docs/tasks/PHASE-06/TASK-025.md).
- Bolts anteriores: 028-video.
- Responsável de origem: Waheed; revisor: Denji.
- Gates: G-CORE, G-GUI.
