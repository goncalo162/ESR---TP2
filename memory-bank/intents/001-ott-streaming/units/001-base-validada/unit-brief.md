---
unit: "001-base-validada"
intent: "001-ott-streaming"
phase: "inception"
source_phase: "BASELINE"
status: "ready"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-03T20:54:11Z"
unit_type: "cli"
default_bolt_type: "simple-construction-bolt"
owned_requirements: ["NFR-005", "REQ-021", "CON-009", "INT-002"]
requires_units: []
---
# Unidade — 001-base-validada

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

## Requisitos atribuídos

- NFR-005 — Especificação, implementação e avaliação crítica verificáveis.
- REQ-021 — Código testável, entradas validadas e recursos limitados.
- CON-009 — Qualidade com ferramentas existentes, sem limiar percentual global.
- INT-002 — Architecture Decision Document mestre e execução rastreável por Construction.

## Histórias e bolts

- [ ] [001-verificar-base](stories/001-verificar-base.md) → [001-base-validada](../../../../bolts/001-base-validada/bolt.md); IMP-001 (S).

## Dependências e critérios de fecho

Concluir verificação da base herdada; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
