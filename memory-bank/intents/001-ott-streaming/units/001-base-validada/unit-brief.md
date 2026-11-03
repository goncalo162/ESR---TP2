---
unit: "001-base-validada"
intent: "001-ott-streaming"
phase: "construction"
source_phase: "BASELINE"
status: "complete"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-03T21:59:14Z"
unit_type: "cli"
default_bolt_type: "simple-construction-bolt"
owned_requirements: ["NFR-005","REQ-021","CON-009","INT-002"]
requires_units: []
completed: "2026-11-03T21:56:48Z"
---
# Unidade — 001-base-validada

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

## Requisitos atribuídos

- NFR-005 — Especificação, implementação e avaliação crítica verificáveis.
- REQ-021 — Código testável, entradas validadas e recursos limitados.
- CON-009 — Qualidade com ferramentas existentes, sem limiar percentual global.
- INT-002 — Architecture Decision Document mestre e execução rastreável por Construction.

## Histórias e bolts

- [x] [001-verificar-base](stories/001-verificar-base.md) → [001-base-validada](../../../../bolts/001-base-validada/bolt.md); IMP-001 (S).

## Dependências e critérios de fecho

Checkpoint 4 aprovado pelo utilizador, com registo em 2026-11-03T21:40:56Z.
Plan/Implement/Test concluídos; [checkpoint 5 aprovado e aplicado](../../../../bolts/001-base-validada/closure-walkthrough.md)
em 2026-11-03T21:56:48Z, depois da integração da versão revista em main
(d65f94740ef95ffc67d312f7fbccac7575c86f7a). Unidade complete, sem promover os
requisitos globais a DONE. 019-overlay não foi iniciado.

Concluir verificação da base herdada; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
