---
unit: "004-arvores"
intent: "001-ott-streaming"
phase: "inception"
source_phase: "PHASE-04"
status: "draft"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
unit_type: "backend"
default_bolt_type: "ddd-construction-bolt"
owned_requirements: ["REQ-007", "REQ-009", "REQ-011", "NFR-003", "REQ-017"]
requires_units: ["003-rotas"]
---
# Unidade — 004-arvores

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

## Requisitos atribuídos

- REQ-007 — Servir vários clientes em simultâneo.
- REQ-009 — Atualizar rotas perante entradas, saídas e condições de rede.
- REQ-011 — Partilhar segmentos comuns e replicar nos pontos de ramificação.
- NFR-003 — Fiabilidade sob alterações e falhas.
- REQ-017 — Ativar sob pedido e podar ramo sem recetores.

## Histórias e bolts

- [ ] [001-ativar-arvores-com-ack-e-procura-agregada](stories/001-ativar-arvores-com-ack-e-procura-agregada.md) → [007-arvores](../../../../bolts/007-arvores/bolt.md); IMP-011 (M).
- [ ] [002-podar-ramos-renovar-leases-e-remover-procura-abandonada](stories/002-podar-ramos-renovar-leases-e-remover-procura-abandonada.md) → [023-arvores](../../../../bolts/023-arvores/bolt.md); IMP-012 (M).
- [ ] [003-recuperar-arvores-apos-falha-de-upstream](stories/003-recuperar-arvores-apos-falha-de-upstream.md) → [008-arvores](../../../../bolts/008-arvores/bolt.md); IMP-013 (M).
- [ ] [004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada](stories/004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada.md) → [024-arvores](../../../../bolts/024-arvores/bolt.md); IMP-014 (M).
- [ ] [005-preparar-o-segundo-marco-com-arvores-e-adaptacao](stories/005-preparar-o-segundo-marco-com-arvores-e-adaptacao.md) → [009-arvores](../../../../bolts/009-arvores/bolt.md); IMP-015 (S).

## Dependências e critérios de fecho

Concluir 003-rotas; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
