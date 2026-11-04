---
unit: "002-overlay"
intent: "001-ott-streaming"
phase: "construction"
source_phase: "PHASE-02"
status: "in-progress"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-04T10:23:36Z"
unit_type: "backend"
default_bolt_type: "ddd-construction-bolt"
owned_requirements: ["REQ-001", "REQ-002", "REQ-003", "REQ-004", "NFR-004", "REQ-024"]
requires_units: ["001-base-validada"]
---
# Unidade — 002-overlay

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

## Requisitos atribuídos

- REQ-001 — Executar nós como aplicações em máquinas IP.
- REQ-002 — Entrada e saída dinâmica de nós.
- REQ-003 — Estabelecer e manter X vizinhos TCP ou UDP.
- REQ-004 — Descobrir vizinhos por contacto conhecido.
- NFR-004 — Overlay aplicacional sobre transporte IP.
- REQ-024 — Configuração operacional opcional, centralizada e validada.

## Histórias e bolts

- [ ] [005-implementar-parametrizacao-operacional](stories/005-implementar-parametrizacao-operacional.md) → [019-overlay](../../../../bolts/019-overlay/bolt.md); IMP-002 (M).
- [ ] [001-fornecer-localizacao-inicial-dos-vizinhos-configurados](stories/001-fornecer-localizacao-inicial-dos-vizinhos-configurados.md) → [002-overlay](../../../../bolts/002-overlay/bolt.md); IMP-003 (M).
- [ ] [002-gerir-handshake-unicidade-e-reconexao-de-vizinhos](stories/002-gerir-handshake-unicidade-e-reconexao-de-vizinhos.md) → [003-overlay](../../../../bolts/003-overlay/bolt.md); IMP-004 (M).
- [ ] [003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel](stories/003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel.md) → [020-overlay](../../../../bolts/020-overlay/bolt.md); IMP-005 (M).
- [ ] [004-preparar-evidencia-do-primeiro-marco-intermedio](stories/004-preparar-evidencia-do-primeiro-marco-intermedio.md) → [004-overlay](../../../../bolts/004-overlay/bolt.md); IMP-006 (S).

## Dependências e critérios de fecho

019-overlay/IMP-002: Model aprovado pelo utilizador em 2026-11-03T22:16:54Z;
Design aprovado, com registo em 2026-11-03T22:25:30Z, incluindo alpha/Q-C-03.
Revisão ADR e Implement aprovados; Implement concluído em 2026-11-03T22:55:46Z.
Test aprovado pelo utilizador em 2026-11-04T10:19:06Z (registo), incluindo 824 testes
completos e a sua execução pessoal como Luis em 2026-11-04T10:15:14Z, 25 checks/
MANUAL_PASS e saída Java 0. [Fecho preparado para revisão](../../../../bolts/019-overlay/closure-walkthrough.md).
As cinco etapas de 019-overlay concluídas; bolt/história/unidade em progresso.
G-REVIEW-032 pendente de revisor distinto designado, revisão final e integração;
restantes bolts planned. A aprovação de Test não é DONE nem nova implementação.

Concluir 001-base-validada; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
