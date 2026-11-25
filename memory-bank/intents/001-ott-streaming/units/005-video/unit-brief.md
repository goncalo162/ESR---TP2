---
unit: "005-video"
intent: "001-ott-streaming"
phase: "construction"
source_phase: "PHASE-05"
status: "in-progress"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-25T17:11:45Z"
unit_type: "backend"
default_bolt_type: "ddd-construction-bolt"
owned_requirements: ["REQ-010", "REQ-012", "REQ-013", "REQ-015", "NFR-001", "REQ-016", "REQ-018"]
requires_units: ["004-arvores"]
---
# Unidade — 005-video

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

Estado atual: in-progress; IMP-016/010-video/TASK-019 complete/DONE,integrado
localmente em main72c9f92. [Fecho](../../../../bolts/010-video/closure-walkthrough.md).
IMP-017/011-video complete,parte A/TASK-020 concluída e integrada em main8a91d9f;
Test/V-020-A,manual Francisco13 e revisão final Denji cumpridos.
[Fecho](../../../../bolts/011-video/closure-walkthrough.md).
TASK-020 inteira IN PROGRESS e PHASE-05 em progresso; história007 complete,
restantes histórias da unidade planned.
LOCAL-03 sem cópia runtime;LOCAL-04/G-MEDIA-DISTRIBUTION abertos para TASK-030.
Créditos B ilegíveis,sem aprovação de atribuição/redistribuição/publicação.
Dependência004-arvores/PHASE-04 complete/DONE; DEMO-001 separado/não bloqueante.
Codec e preparação dos quatro conteúdos entregues; leitor local para revisão,
pacing, streaming e reprodução futuros.
Sem push,alterações Java no fecho ou fecho da unidade. IDs locais preservados.

IMP-018/025-video complete, com Test aceite por condição expressa e revisão Denji
confirmada pelo utilizador. [Relatório](../../../../bolts/025-video/ddd-03-test-report.md):
1582/0/0/0 e JAR fixado, sem rebuild; prova nova dos22687 frames completos,
hashes/contagens/EOF/cleanup e comparação por frame. Negativo humano preservado;
execução pessoal substituída por delegação em Codex para este fecho, sem
inventar manual humano concluído. V-ADD0 erros; código/demo preservados.
Integrado localmente em maine896643. [Fecho](../../../../bolts/025-video/closure-walkthrough.md). Sem push/IMP-019; TASK-020
só fecha apósA/B/C. R17 original continua sem causa estabelecida.
20 bolts complete,0 in-progress,15 planned; nenhum gate externo adicional.

## Requisitos atribuídos

- REQ-010 — Reencaminhar dados segundo a tabela.
- REQ-012 — Disponibilizar pelo menos dois servidores de conteúdos.
- REQ-013 — Cada servidor oferece pelo menos dois vídeos distintos.
- REQ-015 — Entregar e reproduzir vídeo com objetivo de tempo real.
- NFR-001 — Atraso baixo e utilização eficiente de largura de banda.
- REQ-016 — Catálogo e seleção explícita de stream.
- REQ-018 — MJPEG, pacotes numerados e janela de reprodução.

## Histórias e bolts

- [x] [001-definir-codec-udp-e-fragmentacao-de-frames](stories/001-definir-codec-udp-e-fragmentacao-de-frames.md) → [010-video](../../../../bolts/010-video/bolt.md); IMP-016 (M).
- [x] [002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao](stories/002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao.md) → [011-video](../../../../bolts/011-video/bolt.md); IMP-017 (M).
- [x] [007-ler-o-perfil-mjpeg-confirmado](stories/007-ler-o-perfil-mjpeg-confirmado.md) → [025-video](../../../../bolts/025-video/bolt.md); IMP-018 (M).
- [ ] [008-emitir-fontes-com-pacing-e-procura](stories/008-emitir-fontes-com-pacing-e-procura.md) → [026-video](../../../../bolts/026-video/bolt.md); IMP-019 (M).
- [ ] [003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa](stories/003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa.md) → [012-video](../../../../bolts/012-video/bolt.md); IMP-020 (M).
- [ ] [004-reconstituir-frames-com-buffers-e-prazo-limitados](stories/004-reconstituir-frames-com-buffers-e-prazo-limitados.md) → [027-video](../../../../bolts/027-video/bolt.md); IMP-021 (M).
- [ ] [005-apresentar-catalogo-e-video-numa-janela-swing](stories/005-apresentar-catalogo-e-video-numa-janela-swing.md) → [013-video](../../../../bolts/013-video/bolt.md); IMP-022 (M).
- [ ] [006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta](stories/006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta.md) → [028-video](../../../../bolts/028-video/bolt.md); IMP-023 (M).

## Dependências e critérios de fecho

Concluir 004-arvores; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
