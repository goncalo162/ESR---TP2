---
unit: "005-video"
intent: "001-ott-streaming"
phase: "inception"
source_phase: "PHASE-05"
status: "ready"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-03T20:54:11Z"
unit_type: "backend"
default_bolt_type: "ddd-construction-bolt"
owned_requirements: ["REQ-010", "REQ-012", "REQ-013", "REQ-015", "NFR-001", "REQ-016", "REQ-018"]
requires_units: ["004-arvores"]
---
# Unidade — 005-video

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

## Requisitos atribuídos

- REQ-010 — Reencaminhar dados segundo a tabela.
- REQ-012 — Disponibilizar pelo menos dois servidores de conteúdos.
- REQ-013 — Cada servidor oferece pelo menos dois vídeos distintos.
- REQ-015 — Entregar e reproduzir vídeo com objetivo de tempo real.
- NFR-001 — Atraso baixo e utilização eficiente de largura de banda.
- REQ-016 — Catálogo e seleção explícita de stream.
- REQ-018 — MJPEG, pacotes numerados e janela de reprodução.

## Histórias e bolts

- [ ] [001-definir-codec-udp-e-fragmentacao-de-frames](stories/001-definir-codec-udp-e-fragmentacao-de-frames.md) → [010-video](../../../../bolts/010-video/bolt.md); IMP-016 (M).
- [ ] [002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao](stories/002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao.md) → [011-video](../../../../bolts/011-video/bolt.md); IMP-017 (M).
- [ ] [007-ler-o-perfil-mjpeg-confirmado](stories/007-ler-o-perfil-mjpeg-confirmado.md) → [025-video](../../../../bolts/025-video/bolt.md); IMP-018 (M).
- [ ] [008-emitir-fontes-com-pacing-e-procura](stories/008-emitir-fontes-com-pacing-e-procura.md) → [026-video](../../../../bolts/026-video/bolt.md); IMP-019 (M).
- [ ] [003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa](stories/003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa.md) → [012-video](../../../../bolts/012-video/bolt.md); IMP-020 (M).
- [ ] [004-reconstituir-frames-com-buffers-e-prazo-limitados](stories/004-reconstituir-frames-com-buffers-e-prazo-limitados.md) → [027-video](../../../../bolts/027-video/bolt.md); IMP-021 (M).
- [ ] [005-apresentar-catalogo-e-video-numa-janela-swing](stories/005-apresentar-catalogo-e-video-numa-janela-swing.md) → [013-video](../../../../bolts/013-video/bolt.md); IMP-022 (M).
- [ ] [006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta](stories/006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta.md) → [028-video](../../../../bolts/028-video/bolt.md); IMP-023 (M).

## Dependências e critérios de fecho

Concluir 004-arvores; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
