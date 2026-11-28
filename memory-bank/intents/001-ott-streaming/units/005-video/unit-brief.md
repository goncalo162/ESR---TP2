---
unit: "005-video"
intent: "001-ott-streaming"
phase: "construction"
source_phase: "PHASE-05"
status: "in-progress"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-28T19:55:07Z"
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

## IMP-019 iniciado apenas em Model — 2026-11-28T14:54:27Z

026-video in-progress, current_stage=model, stages_completed=[]; história008
planned, sem implementação. [Modelo proposto](../../../../bolts/026-video/ddd-01-domain-model.md)
com Q-C-48/49 para revisão;P01–P20/V-020-C não executados. Sem fontes/Java,
Design/ADR/Implement, Maven, commits/push; três caminhos futuros do ADD mantidos.
TASK-020 apenasA/B concluídas; C emModel não fecha o pai.20 bolts complete,
1 in-progress,14 planned. Fecho:regressãoJDK21, manual aplicável, Denji e
integração;sem gates externos adicionais. Exceção pessoal de025 não transferida.

## IMP-019 — Model aprovado, Design para revisão (2026-11-28T15:18:50Z)

Model/Q-C-48/49 aprovados; [Design proposto](../../../../bolts/026-video/ddd-02-technical-design.md)
concretiza cadência/procura e recursos finitos W2+coordenador1. Qualificar todas
as fontes antes de criação/start com routing; portos reais por conferir emADR,
sem alargar os três caminhos. current_stage=design;apenas Model concluído,
história008 planned;P01–P20/V-020-C não executados. Sem fonte/Java/Maven,
Implement, commits/push ou novo gate externo. Manual humano aplicável,
regressãoJDK21, Denji e integração obrigatórios; TASK-020 só apósA/B/C.

## IMP-019 — revisão ADR preparada (2026-11-28T15:37:32Z)

Model/Design aprovados; [revisão A01–A08](../../../../bolts/026-video/adr-review.md) conferiu APIs reais.
Pontes de owner/procura/revogação/disponibilidade ausentes; Q-C-50 propõe oito
caminhos, mantendo os três do ADD até autorização. ADR pendente, sem Implement,
P01–P20/V-020-C não executados. UNWIRED recusado, sem UDP; cleanup incerto
preservado. História008 planned; TASK-020 C não concluída, pai só após A/B/C,
regressão JDK21/manual aplicável/Denji/integração. Sem commits/push/gates novos.

## IMP-019 / 026-video concluído — 2026-11-28T19:55:07Z

Test aceite pelo utilizador e bolt fechado pelo fluxo oficial. JDK 21:
seleção t14–t16 5/5, suíte pertinente 150/150, suíte completa 1599/1599;
JaCoCo bundle 7573/8252 (91,77%) e linhas executáveis adicionadas 673/825
(81,58%). História 008 complete/implemented=true. Unidade mantém status
`in-progress`: quatro bolts permanecem incompletos. TASK-020 continua IN
PROGRESS; este fecho de 026 não fecha as partes A/B/C nem o pai, e não presume
manual humano, revisão Denji ou integração.

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
- [x] [008-emitir-fontes-com-pacing-e-procura](stories/008-emitir-fontes-com-pacing-e-procura.md) → [026-video](../../../../bolts/026-video/bolt.md); IMP-019 (M).
- [ ] [003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa](stories/003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa.md) → [012-video](../../../../bolts/012-video/bolt.md); IMP-020 (M).
- [ ] [004-reconstituir-frames-com-buffers-e-prazo-limitados](stories/004-reconstituir-frames-com-buffers-e-prazo-limitados.md) → [027-video](../../../../bolts/027-video/bolt.md); IMP-021 (M).
- [ ] [005-apresentar-catalogo-e-video-numa-janela-swing](stories/005-apresentar-catalogo-e-video-numa-janela-swing.md) → [013-video](../../../../bolts/013-video/bolt.md); IMP-022 (M).
- [ ] [006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta](stories/006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta.md) → [028-video](../../../../bolts/028-video/bolt.md); IMP-023 (M).

## Dependências e critérios de fecho

Concluir 004-arvores; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
