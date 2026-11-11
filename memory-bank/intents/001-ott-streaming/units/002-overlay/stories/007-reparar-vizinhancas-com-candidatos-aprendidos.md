---
id: "007-reparar-vizinhancas-com-candidatos-aprendidos"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "planned"
priority: "Must"
created: "2026-11-11T12:26:03Z"
source_tasks: ["TASK-034"]
implementation_tasks: ["IMP-035"]
requirements: ["REQ-025"]
adr_ids: ["ADR-013", "ADR-014", "ADR-017", "ADR-018", "ADR-019", "ADR-020", "ADR-024", "ADR-033", "ADR-034"]
bolt: "035-overlay-extra"
size: "M"
extra: true
updated: "2026-11-11T14:33:20Z"
---
# História — Extra — reparar ligações com candidatos aprendidos

Como nó afetado por falhas, quero usar candidatos aprendidos para criar uma ligação nova, conservando os processos e as sessões saudáveis.
[IMP-035](../../../../../../add.md#imp-035).

## Critérios de aceitação

- [ ] Com o extra desativado, conservar o comportamento e os testes da overlay base.
- [ ] Após aprendizagem pelos vizinhos e morte dos relays, formar S–C no primeiro cenário e R1–C no segundo, preservando S–R1.
- [ ] Candidatos aprendidos, grau, tentativas e temporizadores têm limites verificáveis e uma sessão confirmada por par.
- [ ] Callbacks antigos e reparações concorrentes ou assimétricas não apagam ligações novas nem ultrapassam os limites.
- [ ] Sem candidatos aprendidos válidos, manter estado explícito de isolamento e tentativas limitadas.
- [ ] V-034, regressão atual e ensaio de controlo executados com evidência. A prova de vídeo fica ligada à integração futura de routing e streaming.

## Validação e revisão

V-034. Responsável proposto Denji, revisor proposto Fillmore.
[Bolt](../../../../../bolts/035-overlay-extra/bolt.md).
