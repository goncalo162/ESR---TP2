---
unit: "007-entrega"
intent: "001-ott-streaming"
phase: "inception"
source_phase: "PHASE-07"
status: "ready"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-03T20:54:11Z"
unit_type: "cli"
default_bolt_type: "simple-construction-bolt"
owned_requirements: ["CON-002", "CON-003", "CON-004", "CON-005", "CON-006", "CON-007", "REQ-022", "REQ-023", "INT-001"]
requires_units: ["006-core"]
---
# Unidade — 007-entrega

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

## Requisitos atribuídos

- CON-002 — Relatório em formato de artigo.
- CON-003 — Relatório com máximo de 12 páginas.
- CON-004 — Arquivo de código ZIP ou RAR.
- CON-005 — Nomes de entrega com turno e grupo.
- CON-006 — Submeter na troca de ficheiros do grupo Blackboard.
- CON-007 — Preparar duas avaliações intermédias e defesa final.
- REQ-022 — Adotar formato LNCS para o artigo.
- REQ-023 — Documentar comandos, logs e operação reproduzível.
- INT-001 — Concluir o serviço OTT segundo as fontes do projeto.

## Histórias e bolts

- [ ] [001-escrever-artigo-com-protocolos-e-avaliacao-critica](stories/001-escrever-artigo-com-protocolos-e-avaliacao-critica.md) → [017-entrega](../../../../bolts/017-entrega/bolt.md); IMP-030 (M).
- [ ] [002-empacotar-codigo-e-verificar-reproducao-da-entrega](stories/002-empacotar-codigo-e-verificar-reproducao-da-entrega.md) → [018-entrega](../../../../bolts/018-entrega/bolt.md); IMP-031 (M).
- [ ] [003-preparar-defesa-e-checklist-de-submissao-humana](stories/003-preparar-defesa-e-checklist-de-submissao-humana.md) → [032-entrega](../../../../bolts/032-entrega/bolt.md); IMP-032 (S).
- [ ] [004-registar-a-submissao-humana](stories/004-registar-a-submissao-humana.md) → [033-entrega](../../../../bolts/033-entrega/bolt.md); IMP-033 (S).

## Dependências e critérios de fecho

Concluir 006-core; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
