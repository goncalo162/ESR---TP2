---
unit: "004-arvores"
intent: "001-ott-streaming"
phase: "construction"
source_phase: "PHASE-04"
status: "in-progress"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-21T15:36:31Z"
unit_type: "backend"
default_bolt_type: "ddd-construction-bolt"
owned_requirements: ["REQ-007", "REQ-009", "REQ-011", "NFR-003", "REQ-017"]
requires_units: ["003-rotas"]
---
# Unidade — 004-arvores

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

Estado atual: unidade **in-progress**. História001/IMP-011/007-arvores complete,
implemented=true, TASK-014 DONE após Test1328/0/0/0, manual Francisco24, revisão
final Waheed e integração37fa863 comprovada.
[Fecho](../../../../bolts/007-arvores/closure-walkthrough.md).
023-arvores/IMP-012 complete,história implemented=true,TASK-015 DONE após
Test1379/0/0/0,19 XML,V-ADD,manual humano Francisco30,revisão final Denji e
integração4d18d1d comprovada. [Fecho](../../../../bolts/023-arvores/closure-walkthrough.md).
Unidade/PHASE-04 continuam in-progress.
IMP-013/008-arvores/TASK-016 concluído após integração em main 447ae4b.
Bolt/história complete,implemented=true,TASK-016 DONE. Test1419/0/0/0,20 XML,
V-016/R01–R18,manual humano Luis7etapas/28checks e revisão final Fillmore
aprovados. [Fecho](../../../../bolts/008-arvores/closure-walkthrough.md). Código/JAR/hashes/negativos preservados;
R17 inicial continua sem causa estabelecida e métricas controladas não a
demonstram. O commit/push desse fecho está concluído; não autoriza publicação
no incremento seguinte. Estado atual: unidade004/PHASE-04 em progresso,
15 bolts complete,1 in-progress e19 planned.024-arvores/IMP-014 em Implement,
por aprovar. Model/Q-C-45/46, Design e ADR/Q-C-47-B aprovados; cinco caminhos
autorizados e registados antes de fontes. Regressão incremental JDK21 actual
1449/0/0/0 (1419 históricos+30 novos),21 XML. Implement/Test não aprovados;
V-017 por consolidar em Test, clean verify/JAR novo, regressão aprovada, manual
humano antes de aprovar Test, Waheed e integração pendentes. Sem fecho/commit/
push/outro IMP ou gate externo adicional. R17 histórico sem causa estabelecida.

Registos anteriores de Q-C-40–42 mantêm caráter histórico; código/JAR/evidência preservados.

Model/Design/ADR e Q-C-47-B aprovados; [Implement de024-arvores para revisão](../../../../bolts/024-arvores/implementation-walkthrough.md).
História004 continua planned, sem implementação.009-arvores aguarda conclusão
de024; revisão final de IMP-014 atribuída a Waheed, sem gate externo adicional.

## Requisitos atribuídos

- REQ-007 — Servir vários clientes em simultâneo.
- REQ-009 — Atualizar rotas perante entradas, saídas e condições de rede.
- REQ-011 — Partilhar segmentos comuns e replicar nos pontos de ramificação.
- NFR-003 — Fiabilidade sob alterações e falhas.
- REQ-017 — Ativar sob pedido e podar ramo sem recetores.

## Histórias e bolts

- [x] [001-ativar-arvores-com-ack-e-procura-agregada](stories/001-ativar-arvores-com-ack-e-procura-agregada.md) → [007-arvores](../../../../bolts/007-arvores/bolt.md); IMP-011 (M).
- [x] [002-podar-ramos-renovar-leases-e-remover-procura-abandonada](stories/002-podar-ramos-renovar-leases-e-remover-procura-abandonada.md) → [023-arvores](../../../../bolts/023-arvores/bolt.md); IMP-012 (M).
- [ ] [003-recuperar-arvores-apos-falha-de-upstream](stories/003-recuperar-arvores-apos-falha-de-upstream.md) → [008-arvores](../../../../bolts/008-arvores/bolt.md); IMP-013 (M).
- [ ] [004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada](stories/004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada.md) → [024-arvores](../../../../bolts/024-arvores/bolt.md); IMP-014 (M).
- [ ] [005-preparar-o-segundo-marco-com-arvores-e-adaptacao](stories/005-preparar-o-segundo-marco-com-arvores-e-adaptacao.md) → [009-arvores](../../../../bolts/009-arvores/bolt.md); IMP-015 (S).

## Dependências e critérios de fecho

Concluir 003-rotas; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.
