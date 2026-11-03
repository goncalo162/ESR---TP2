---
unit: "006-core"
intent: "001-ott-streaming"
phase: "inception"
source_phase: "PHASE-06"
status: "ready"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-03T20:54:11Z"
unit_type: "cli"
default_bolt_type: "simple-construction-bolt"
owned_requirements: ["NFR-002", "CON-001", "REQ-020"]
requires_units: ["005-video"]
---
# Unidade — 006-core

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

## Requisitos atribuídos

- NFR-002 — Escalabilidade para N clientes.
- CON-001 — Usar CORE como bancada principal.
- REQ-020 — Testar cenários de 4, 6 e overlay complexa.

## Histórias e bolts

- [ ] [001-preparar-core-e-comprovar-replicacao-no-cenario-de-quatro-nos](stories/001-preparar-core-e-comprovar-replicacao-no-cenario-de-quatro-nos.md) → [014-core](../../../../bolts/014-core/bolt.md); IMP-024 (M).
- [ ] [005-medir-partilha-no-cenario-de-quatro-nos](stories/005-medir-partilha-no-cenario-de-quatro-nos.md) → [029-core](../../../../bolts/029-core/bolt.md); IMP-025 (M).
- [ ] [002-demonstrar-alternativas-e-adaptacao-no-cenario-de-seis-nos](stories/002-demonstrar-alternativas-e-adaptacao-no-cenario-de-seis-nos.md) → [015-core](../../../../bolts/015-core/bolt.md); IMP-026 (M).
- [ ] [006-medir-adaptacao-por-rtt-e-estabilidade](stories/006-medir-adaptacao-por-rtt-e-estabilidade.md) → [030-core](../../../../bolts/030-core/bolt.md); IMP-027 (M).
- [ ] [003-validar-cenario-complexo-com-duas-origens-e-cinco-clientes](stories/003-validar-cenario-complexo-com-duas-origens-e-cinco-clientes.md) → [016-core](../../../../bolts/016-core/bolt.md); IMP-028 (M).
- [ ] [004-recolher-metricas-e-avaliar-resultados-e-limites](stories/004-recolher-metricas-e-avaliar-resultados-e-limites.md) → [031-core](../../../../bolts/031-core/bolt.md); IMP-029 (M).

## Dependências e critérios de fecho

Concluir 005-video; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
