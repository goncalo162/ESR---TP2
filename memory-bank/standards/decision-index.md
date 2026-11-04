# Índice derivado de decisões

Fonte normativa: [ADD](../../add.md).

- [ADR-001 — Java 21 e Maven](../../add.md#adr-001) — Aceite.
- [ADR-002 — TCP de controlo e UDP de vídeo](../../add.md#adr-002) — Aceite.
- [ADR-003 — Bootstrap por grafo permitido — registo histórico](../../add.md#adr-003) — Substituída.
- [ADR-004 — Anúncios path vector e custo RTT](../../add.md#adr-004) — Aceite.
- [ADR-005 — Árvores sob procura, ACK, revisões e leases](../../add.md#adr-005) — Aceite.
- [ADR-006 — MJPEG independente, UDP próprio e Swing](../../add.md#adr-006) — Aceite.
- [ADR-007 — Propriedades UTF-8 e binário sem dependências runtime](../../add.md#adr-007) — Aceite.
- [ADR-008 — Testes incrementais e aceitação no CORE](../../add.md#adr-008) — Aceite.
- [ADR-009 — Fixtures independentes e perfil confirmado](../../add.md#adr-009) — Aceite.
- [ADR-010 — Carga estrita e configuração imutável](../../add.md#adr-010) — Aceite.
- [ADR-011 — Reutilizar os XML CORE existentes](../../add.md#adr-011) — Aceite.
- [ADR-012 — Codec separado de transporte e contexto](../../add.md#adr-012) — Aceite.
- [ADR-013 — Bootstrap só para localização inicial](../../add.md#adr-013) — Aceite.
- [ADR-014 — TCP assíncrono com recursos limitados](../../add.md#adr-014) — Aceite.
- [ADR-015 — Confirmação assíncrona de escrita da resposta](../../add.md#adr-015) — Aceite.
- [ADR-016 — Parâmetros centrais e overrides validados](../../add.md#adr-016) — Aceite.
- [ADR-017 — Um projeto com componentes e proprietário serial do estado](../../add.md#adr-017) — Aceite.
- [ADR-018 — Estado volátil e identidades por stream/execução](../../add.md#adr-018) — Aceite.
- [ADR-019 — Handshake, iniciador determinístico e reconexão direta](../../add.md#adr-019) — Aceite.
- [ADR-020 — Erros tipados, falhas locais e shutdown limitado](../../add.md#adr-020) — Aceite.
- [ADR-021 — Segurança de laboratório e validação de fronteiras](../../add.md#adr-021) — Aceite.
- [ADR-022 — Observabilidade local e experiências reproduzíveis](../../add.md#adr-022) — Aceite.
- [ADR-023 — Entrega reproduzível, artigo e integração Git](../../add.md#adr-023) — Aceite.
- [ADR-024 — ADD mestre e incrementos atómicos rastreáveis](../../add.md#adr-024) — Aceite.
- [ADR-025 — Sem recuperação automática de falha exclusiva UDP](../../add.md#adr-025) — Aceite.
- [ADR-026 — Qualidade sem ferramentas ou limiar percentual novos](../../add.md#adr-026) — Aceite.

## Clarificações aprovadas do checkpoint — 03-11-2026

Fonte normativa: [ADD, secção 13](../../add.md#13-questões-em-aberto-e-changelog).

- CP-01: manter nodeId e REGISTER/REGISTER_OK na consulta inicial; esclarece ADR-013/018.
- CP-02: regresso e entrada tardia de nós configurados, com nova instância após reinício; esclarece ADR-013/018/019 e TASK-007/016.
- CP-03: alternativas no grafo configurado; novas vizinhanças fora do grafo adiadas como extra; esclarece âmbito e ADR-013/019.

As CP confirmam decisões existentes; conservam-se as 26 ADR. A aprovação do
utilizador é documental, sem prova de implementação ou revisão individual de código.

## Detalhe de Construction aprovado — IMP-002

[Q-C-03 — gramática decimal de alpha](../../docs/construction-duvidas.md#q-c-03--gramática-decimal-de-alpha)
regista a aprovação explícita do detalhe lexical de monitor.ewma.alpha no Design.
As ADR-007/010/016/024/026 são reutilizadas sem substituição ou ADR nova;
[revisão de conformidade](../bolts/019-overlay/adr-review.md) aprovada antes de código.
Implement e [Test aprovados](../bolts/019-overlay/ddd-03-test-report.md): 824 testes
completos e repetição pessoal por Luis/25 checks confirmados. [Fecho concluído](../bolts/019-overlay/closure-walkthrough.md)
em 2026-11-04T10:33:12Z, revisão final confirmada por Luis e versão revista integrada
em main. G-REVIEW-032 cumprido; sem ADR nova.

## Concretizações de Model aprovadas — IMP-003

[Q-C-04](../../docs/construction-duvidas.md#q-c-04--coerência-da-instância-e-resposta-única-na-consulta-inicial),
aprovação explícita registada em 2026-11-04T10:50:52Z: igualdade de senderInstance e
Endpoint.instanceId no REGISTER e uma decisão de resposta por conexão. Aplicam
ADR-013/014/015; ADR-003 permanece substituída. [Design para revisão](../bolts/002-overlay/ddd-02-technical-design.md)
explicita também a normalização lexical de hosts já documentada em docs/07,
sem DNS. Design aprovado explicitamente em 2026-11-04T11:25:40Z, incluindo requestClose
como início de shutdown sem espera e preservação de close público. Autorizada
apenas ADR: [revisão de conformidade preparada](../bolts/002-overlay/adr-review.md)
em 2026-11-04T11:29:33Z; sem inviabilidade/divergência material identificada nesta inspeção.
ADR-013/014/015/017/020 e transversais reutilizadas; sem ADR nova ou alteração
do ADD. Aguardar revisão antes de Implement/Test.

Revisão ADR aprovada em 2026-11-04T11:37:45Z; autorizado apenas Implement. [Walkthrough](../bolts/002-overlay/implementation-walkthrough.md)
preparado em 2026-11-04T12:04:53Z: bootstrap e transporte nos cinco ficheiros, 856 testes
incrementais aprovados, red real guardado e M01–M18 mapeados. Sem ADR nova,
wire/caps alterados ou novo âmbito. Parar para revisão antes de Test/fecho;
clean verify/manual e revisão final por Waheed/integração pendentes.

Implement aprovado em 2026-11-04T12:18:01Z; autorizada apenas Test, com [relatório](../bolts/002-overlay/ddd-03-test-report.md)
preparado em 2026-11-04T12:29:39Z: clean verify/JDK21 com 856 testes e manual Codex/34 checks,
ambos saída 0. Sem código/ADR/dependência nova; cinco hashes de código iguais à
versão aprovada e 155 ficheiros congelados. V-006/M01–M18 executados tecnicamente;
repetição pessoal/aprovação Test, revisão final por Waheed e integração pendentes.

Test do IMP-003 aprovado explicitamente, com registo em 2026-11-04T14:05:14Z, incluindo
856 testes e execução pessoal por Luis em 2026-11-04T13:58:36Z–13:58:38Z:
34 CHECK_PASS/MANUAL_PASS/JAVA_EXIT=0, mesmo JAR. [Fecho preparado](../bolts/002-overlay/closure-walkthrough.md),
commits e push de Denji autorizados. Nenhuma nova decisão arquitetural ou ADR;
revisão final atribuída a Waheed e integração pendentes antes de DONE.
