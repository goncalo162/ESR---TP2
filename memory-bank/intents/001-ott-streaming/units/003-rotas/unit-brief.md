---
unit: "003-rotas"
intent: "001-ott-streaming"
phase: "inception"
source_phase: "PHASE-03"
status: "draft"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-02T15:36:48Z"
unit_type: "backend"
default_bolt_type: "ddd-construction-bolt"
owned_requirements: ["REQ-005", "REQ-006", "REQ-008", "REQ-014", "REQ-019", "CON-008"]
requires_units: ["002-overlay"]
---
# Unidade — 003-rotas

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

## Requisitos atribuídos

- REQ-005 — Manter tabela por fluxo com custo, origem, destinos e estado.
- REQ-006 — Suportar caminhos alternativos.
- REQ-008 — Monitorizar a rede overlay.
- REQ-014 — Usar métricas de rede para construir rotas.
- REQ-019 — Diagnóstico do percurso antes do vídeo.
- CON-008 — Deteção automática de falhas limitada ao plano TCP.

## Histórias e bolts

- [ ] [001-medir-rtt-e-publicar-falhas-de-vizinhanca](stories/001-medir-rtt-e-publicar-falhas-de-vizinhanca.md) → [005-rotas](../../../../bolts/005-rotas/bolt.md); IMP-007 (M).
- [ ] [002-modelar-candidatos-custos-e-snapshots-por-stream](stories/002-modelar-candidatos-custos-e-snapshots-por-stream.md) → [021-rotas](../../../../bolts/021-rotas/bolt.md); IMP-008 (M).
- [ ] [003-difundir-anuncios-e-construir-catalogo-e-rotas](stories/003-difundir-anuncios-e-construir-catalogo-e-rotas.md) → [006-rotas](../../../../bolts/006-rotas/bolt.md); IMP-009 (M).
- [ ] [004-diagnosticar-percursos-pela-tabela-de-rotas](stories/004-diagnosticar-percursos-pela-tabela-de-rotas.md) → [022-rotas](../../../../bolts/022-rotas/bolt.md); IMP-010 (S).

## Dependências e critérios de fecho

Concluir 002-overlay; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
