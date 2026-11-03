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
