---
unit: "002-overlay"
intent: "001-ott-streaming"
phase: "construction"
source_phase: "PHASE-02"
status: "in-progress"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-10T15:20:09Z"
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

- [x] [005-implementar-parametrizacao-operacional](stories/005-implementar-parametrizacao-operacional.md) → [019-overlay](../../../../bolts/019-overlay/bolt.md); IMP-002 (M).
- [x] [001-fornecer-localizacao-inicial-dos-vizinhos-configurados](stories/001-fornecer-localizacao-inicial-dos-vizinhos-configurados.md) → [002-overlay](../../../../bolts/002-overlay/bolt.md); IMP-003 (M).
- [ ] [002-gerir-handshake-unicidade-e-reconexao-de-vizinhos](stories/002-gerir-handshake-unicidade-e-reconexao-de-vizinhos.md) → [003-overlay](../../../../bolts/003-overlay/bolt.md); IMP-004 (M).
- [ ] [003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel](stories/003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel.md) → [020-overlay](../../../../bolts/020-overlay/bolt.md); IMP-005 (M).
- [ ] [004-preparar-evidencia-do-primeiro-marco-intermedio](stories/004-preparar-evidencia-do-primeiro-marco-intermedio.md) → [004-overlay](../../../../bolts/004-overlay/bolt.md); IMP-006 (S).

## Dependências e critérios de fecho

019-overlay/IMP-002: Model aprovado pelo utilizador em 2026-11-03T22:16:54Z;
Design aprovado, com registo em 2026-11-03T22:25:30Z, incluindo alpha/Q-C-03.
Revisão ADR e Implement aprovados; Implement concluído em 2026-11-03T22:55:46Z.
Test aprovado pelo utilizador em 2026-11-04T10:19:06Z (registo), incluindo 824 testes
completos e a sua execução pessoal como Luis em 2026-11-04T10:15:14Z, 25 checks/
MANUAL_PASS e saída Java 0. [Fecho concluído](../../../../bolts/019-overlay/closure-walkthrough.md) em 2026-11-04T10:33:12Z.
Bolt/história complete; revisão final confirmada por Luis, distinta da autoria
do agente Codex, e versão revista integrada em main (09a068f68eca473451e1e80a045c1cf9cf15a1b2).
G-REVIEW-032 cumprido. Unidade continua in-progress: 019-overlay complete,
002-overlay complete e três bolts planned. IMP-003 iniciado em 2026-11-04T10:40:58Z,
apenas Model, por seleção explícita; [modelo preparado para revisão](../../../../bolts/002-overlay/ddd-01-domain-model.md).
Model aprovado em 2026-11-04T10:50:52Z, incluindo Q-C-04. [Design preparado para revisão](../../../../bolts/002-overlay/ddd-02-technical-design.md)
em 2026-11-04T11:04:53Z, aprovado em 2026-11-04T11:25:40Z, incluindo requestClose.
Autorizada apenas ADR, com [revisão preparada](../../../../bolts/002-overlay/adr-review.md)
em 2026-11-04T11:29:33Z, aprovada em 2026-11-04T11:37:45Z. Implement autorizado e
[preparado para revisão](../../../../bolts/002-overlay/implementation-walkthrough.md)
em 2026-11-04T12:08:01Z: cinco ficheiros +1226/−35 e 856 testes incrementais aprovados.
150 hashes restantes e testes anteriores preservados. Implement aprovado em
2026-11-04T12:18:01Z; [Test preparado](../../../../bolts/002-overlay/ddd-03-test-report.md) em 2026-11-04T12:29:39Z:
clean verify com 856 testes/zero falhas e manual Codex/34 checks/saída 0. Nenhuma
alteração Java durante Test. Test aprovado, com registo em 2026-11-04T14:05:14Z, incluindo
repetição pessoal por Luis em 2026-11-04T13:58:36Z–13:58:38Z, 34 CHECK_PASS/
MANUAL_PASS/JAVA_EXIT=0, mesmo JAR. [Fecho concluído](../../../../bolts/002-overlay/closure-walkthrough.md)
em 2026-11-04T14:18:23Z: revisão final atribuída a Waheed confirmada pelo utilizador,
registada em 2026-11-04T14:18:23Z; versão revista 03345e9e1ab56ec04302676cca6fce32d168223d integrada em main.
Bolt/história complete, implemented=true, TASK-006 DONE; unidade mantém in-progress.
A dependência 001-base-validada permanece concluída/integrada.

Concluir 001-base-validada; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.

## Estado atual — IMP-004 em Model

003-overlay iniciado em 2026-11-10T14:01:42Z, apenas Model por seleção explícita.
[Modelo para revisão](../../../../bolts/003-overlay/ddd-01-domain-model.md) preparado em 2026-11-10T14:11:43Z:
três ficheiros autorizados vazios, lista desejada/sessões/handshake/retries e M01–M22.
155 ficheiros de Projeto/ preservados; V-007, regressão/manual e revisão final
atribuída a Denji pendentes. A unidade mantém in-progress: dois bolts complete,
um em Model e dois planned. A descrição acima conserva os checkpoints anteriores;
o estado atual supera a referência histórica a três bolts planned.
Aguardar aprovação antes de Design/ADR/Implement/Test; sem commits/push/fecho.

Revisão Model do IMP-004 em 2026-11-10T14:24:46Z: ERROR (17) agora explícito na descoberta,
handshake e ACTIVE, com M02/M07/M11 ampliados. Q-C-05 pendente de decisão sobre
retry e ERROR inesperado antes de ACTIVE. Model ainda não aprovado; sem Design,
Java, execução V-007/regressão/manual, commits ou push.

## Estado atual — IMP-004 em Design

Model/Q-C-05 aprovados em 2026-11-10T14:48:50Z; apenas Design autorizado.
[Desenho para revisão](../../../../bolts/003-overlay/ddd-02-technical-design.md) preparado em 2026-11-10T15:00:14Z:
owner serial partilhável, B=min(4,Q)/R=min(N,C×B), worker DNS/I/O e barreira dos dials
nativos ainda não concluídos. D01–D22 previstos. As pendências Model/Q-C-05 dos
retratos anteriores estão resolvidas por esta aprovação; Design ainda não aprovado.
Unidade in-progress, 003-overlay em Design; V-007/regressão/manual, revisão final
atribuída a Denji e integração pendentes. Nenhum Java/build/teste/commit/push novo.

## Estado atual — IMP-004 em ADR

Design/Q-C-06 aprovados em 2026-11-10T15:13:50Z; apenas ADR autorizada.
[Revisão para validação](../../../../bolts/003-overlay/adr-review.md) preparada em 2026-11-10T15:20:09Z:
conformidade do owner próprio/emprestado, limites físicos, B/R e barreira de dials,
sem contrariar Q-C-05 ou APIs/bytes existentes. Unidade mantém in-progress;
003-overlay em ADR, Model/Design concluídos. Nenhum Java/build/teste ou outro IMP
iniciado. Revisão final por Denji/integração pendentes; parar antes de Implement/Test.
