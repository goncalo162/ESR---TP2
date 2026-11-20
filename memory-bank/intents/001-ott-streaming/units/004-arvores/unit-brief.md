---
unit: "004-arvores"
intent: "001-ott-streaming"
phase: "construction"
source_phase: "PHASE-04"
status: "in-progress"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-20T16:07:46Z"
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
IMP-013/008-arvores/TASK-016 em Test. Implement/R10 aprovados pelo utilizador;
Model/Design/ADR/Implement concluídos, current_stage=test, completed=null.
[Test técnico](../../../../bolts/008-arvores/ddd-03-test-report.md): clean verify JDK21 novo1419/0/0/0,20 XML e JAR SHA256
81779be388778fd7dfacba5957e74ccffe5da45e86a403ea1ec5626f52c8f49d.
V-016/R01–R18 consolidado tecnicamente,40 RecoveryTest/1379 históricos.
Manual preparado:7 etapas/28 checks; ensaio Codex kind=agent passou e não
substitui repetição humana. Aguardar execução pessoal antes de pedir aprovação
Test. Código aprovado/evidências preservados; R17 inicial sem causa estabelecida,
métricas controladas não demonstram a causa. Test não aprovado; sem fecho,
commits/push/outro IMP. Fillmore/integração pendentes; sem gates externos novos.

Q-C-40–42/código/JAR/evidência intactos,sem Java/push/novo IMP no fecho.

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
