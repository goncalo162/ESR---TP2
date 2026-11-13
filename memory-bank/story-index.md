# Índice global de histórias

33 histórias geradas na reconciliação inicial. IDs antigos conservados.
GENERATED indica criação documental, não implementação. Fonte de arquitetura: [ADD](../add.md).

Estado Construction atual: IMP-001 com Plan/Implement/Test concluídos e resultados
aprovados; [checkpoint 5 concluído](bolts/001-base-validada/closure-walkthrough.md)
em 2026-11-03T21:56:48Z, após integração em main. Três histórias já completas antes de IMP-004:
019-overlay/IMP-002 fechado em 2026-11-04T10:33:12Z, após as cinco etapas, revisão
final por Luis e integração em main. G-REVIEW-032 cumprido. 002-overlay/IMP-003
com cinco etapas concluídas e [Test aprovado](bolts/002-overlay/ddd-03-test-report.md),
registado em 2026-11-04T14:05:14Z; fechado em 2026-11-04T14:18:23Z, após revisão confirmada
pelo utilizador e integração da versão revista em main;
003-overlay/IMP-004 complete, história implemented=true, TASK-007 DONE após revisão Denji e integração local 8db7b78. 020-overlay/IMP-005 complete após revisão final confirmada pelo utilizador e integração da versão revista em main. 004-overlay/IMP-006 complete após Test, revisão confirmada e integração. Total atual: nove histórias/bolts complete, incluindo005-rotas/IMP-007, e25 planned;021-rotas/IMP-008 com Test/revisão final aceites, integração pendente; unidade002-overlay complete, PHASE-02 DONE incluindo EXTRA-01 de controlo.
Intent ainda em construção.

## 001-base-validada/001-verificar-base.md ✅ GENERATED — COMPLETE

**Tarefa:** IMP-001. **Estado:** complete. **Bolt:** 001-base-validada.
Bolt/história/unidade complete pela cascata real em 2026-11-03T21:56:48Z;
história implemented=true. Versão revista integrada em main: d65f947.
**Ficheiro:** [001-verificar-base](intents/001-ott-streaming/units/001-base-validada/stories/001-verificar-base.md).

## 002-overlay/005-implementar-parametrizacao-operacional.md ✅ GENERATED

**Tarefa:** IMP-002. **Estado:** complete. **Bolt:** 019-overlay.
Test aprovado: 824 testes completos e execução pessoal por Luis/25 checks/saída 0.
[Fecho concluído](bolts/019-overlay/closure-walkthrough.md) em 2026-11-04T10:33:12Z; implemented=true.
G-REVIEW-032 cumprido: revisão final por Luis e integração da versão revista em main.
**Ficheiro:** [005-implementar-parametrizacao-operacional](intents/001-ott-streaming/units/002-overlay/stories/005-implementar-parametrizacao-operacional.md).

## 002-overlay/001-fornecer-localizacao-inicial-dos-vizinhos-configurados.md ✅ GENERATED

**Tarefa:** IMP-003. **Estado:** complete. **Bolt:** 002-overlay; implemented=true.
Cinco etapas concluídas; Test aprovado, registado em 2026-11-04T14:05:14Z.
Clean verify: 856 testes/zero falhas; manuais separados Codex/Luis: 34 checks e
saída 0 cada. Luis executou em 2026-11-04T13:58:36Z–13:58:38Z, mesmo JAR.
M01–M18/V-006 executados. [Fecho concluído](bolts/002-overlay/closure-walkthrough.md) em 2026-11-04T14:18:23Z.
Revisão final atribuída a Waheed confirmada pelo utilizador; versão revista
03345e9e1ab56ec04302676cca6fce32d168223d integrada em main antes do fecho. Unidade in-progress.
**Ficheiro:** [001-fornecer-localizacao-inicial-dos-vizinhos-configurados](intents/001-ott-streaming/units/002-overlay/stories/001-fornecer-localizacao-inicial-dos-vizinhos-configurados.md).

## 002-overlay/002-gerir-handshake-unicidade-e-reconexao-de-vizinhos.md ✅ GENERATED

**Tarefa:** IMP-004. **Estado:** complete; implemented=true. **Bolt:** 003-overlay.
Model iniciado em 2026-11-10T14:01:42Z; [preparado para revisão](bolts/003-overlay/ddd-01-domain-model.md)
em 2026-11-10T14:11:43Z, dependências integradas em 26aaa4ae6775e9c78e1d7658472ccaca0b376427.
V-007/M01–M22 planeados, sem código/teste/build novo; parar antes de Design.
Revisão final atribuída a Denji e integração continuam exigidas antes de DONE.
**Ficheiro:** [002-gerir-handshake-unicidade-e-reconexao-de-vizinhos](intents/001-ott-streaming/units/002-overlay/stories/002-gerir-handshake-unicidade-e-reconexao-de-vizinhos.md).

## 002-overlay/003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel.md ✅ GENERATED — COMPLETE

**Tarefa:** IMP-005. **Estado:** complete. **Bolt:** 020-overlay.
**Ficheiro:** [003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel](intents/001-ott-streaming/units/002-overlay/stories/003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel.md).

## 002-overlay/004-preparar-evidencia-do-primeiro-marco-intermedio.md ✅ GENERATED — COMPLETE

**Tarefa:** IMP-006. **Estado:** complete. **Bolt:** 004-overlay.
**Ficheiro:** [004-preparar-evidencia-do-primeiro-marco-intermedio](intents/001-ott-streaming/units/002-overlay/stories/004-preparar-evidencia-do-primeiro-marco-intermedio.md).

## 003-rotas/001-medir-rtt-e-publicar-falhas-de-vizinhanca.md ✅ GENERATED

**Tarefa:** IMP-007. **Estado:** complete/implemented=true, TASK-010 DONE. **Bolt:** 005-rotas; integração f990503 e fecho documental autorizado.
**Ficheiro:** [001-medir-rtt-e-publicar-falhas-de-vizinhanca](intents/001-ott-streaming/units/003-rotas/stories/001-medir-rtt-e-publicar-falhas-de-vizinhanca.md).

## 003-rotas/002-modelar-candidatos-custos-e-snapshots-por-stream.md ✅ GENERATED

**Tarefa:** IMP-008. **Estado:** in-progress (Test/revisão final Luis aceites; integração pendente). **Bolt:** 021-rotas.
**Ficheiro:** [002-modelar-candidatos-custos-e-snapshots-por-stream](intents/001-ott-streaming/units/003-rotas/stories/002-modelar-candidatos-custos-e-snapshots-por-stream.md).

## 003-rotas/003-difundir-anuncios-e-construir-catalogo-e-rotas.md ✅ GENERATED

**Tarefa:** IMP-009. **Estado:** planned. **Bolt:** 006-rotas.
**Ficheiro:** [003-difundir-anuncios-e-construir-catalogo-e-rotas](intents/001-ott-streaming/units/003-rotas/stories/003-difundir-anuncios-e-construir-catalogo-e-rotas.md).

## 003-rotas/004-diagnosticar-percursos-pela-tabela-de-rotas.md ✅ GENERATED

**Tarefa:** IMP-010. **Estado:** planned. **Bolt:** 022-rotas.
**Ficheiro:** [004-diagnosticar-percursos-pela-tabela-de-rotas](intents/001-ott-streaming/units/003-rotas/stories/004-diagnosticar-percursos-pela-tabela-de-rotas.md).

## 004-arvores/001-ativar-arvores-com-ack-e-procura-agregada.md ✅ GENERATED

**Tarefa:** IMP-011. **Estado:** planned. **Bolt:** 007-arvores.
**Ficheiro:** [001-ativar-arvores-com-ack-e-procura-agregada](intents/001-ott-streaming/units/004-arvores/stories/001-ativar-arvores-com-ack-e-procura-agregada.md).

## 004-arvores/002-podar-ramos-renovar-leases-e-remover-procura-abandonada.md ✅ GENERATED

**Tarefa:** IMP-012. **Estado:** planned. **Bolt:** 023-arvores.
**Ficheiro:** [002-podar-ramos-renovar-leases-e-remover-procura-abandonada](intents/001-ott-streaming/units/004-arvores/stories/002-podar-ramos-renovar-leases-e-remover-procura-abandonada.md).

## 004-arvores/003-recuperar-arvores-apos-falha-de-upstream.md ✅ GENERATED

**Tarefa:** IMP-013. **Estado:** planned. **Bolt:** 008-arvores.
**Ficheiro:** [003-recuperar-arvores-apos-falha-de-upstream](intents/001-ott-streaming/units/004-arvores/stories/003-recuperar-arvores-apos-falha-de-upstream.md).

## 004-arvores/004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada.md ✅ GENERATED

**Tarefa:** IMP-014. **Estado:** planned. **Bolt:** 024-arvores.
**Ficheiro:** [004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada](intents/001-ott-streaming/units/004-arvores/stories/004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada.md).

## 004-arvores/005-preparar-o-segundo-marco-com-arvores-e-adaptacao.md ✅ GENERATED

**Tarefa:** IMP-015. **Estado:** planned. **Bolt:** 009-arvores.
**Ficheiro:** [005-preparar-o-segundo-marco-com-arvores-e-adaptacao](intents/001-ott-streaming/units/004-arvores/stories/005-preparar-o-segundo-marco-com-arvores-e-adaptacao.md).

## 005-video/001-definir-codec-udp-e-fragmentacao-de-frames.md ✅ GENERATED

**Tarefa:** IMP-016. **Estado:** planned. **Bolt:** 010-video.
**Ficheiro:** [001-definir-codec-udp-e-fragmentacao-de-frames](intents/001-ott-streaming/units/005-video/stories/001-definir-codec-udp-e-fragmentacao-de-frames.md).

## 005-video/002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao.md ✅ GENERATED

**Tarefa:** IMP-017. **Estado:** planned. **Bolt:** 011-video.
**Ficheiro:** [002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao](intents/001-ott-streaming/units/005-video/stories/002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao.md).

## 005-video/007-ler-o-perfil-mjpeg-confirmado.md ✅ GENERATED

**Tarefa:** IMP-018. **Estado:** planned. **Bolt:** 025-video.
**Ficheiro:** [007-ler-o-perfil-mjpeg-confirmado](intents/001-ott-streaming/units/005-video/stories/007-ler-o-perfil-mjpeg-confirmado.md).

## 005-video/008-emitir-fontes-com-pacing-e-procura.md ✅ GENERATED

**Tarefa:** IMP-019. **Estado:** planned. **Bolt:** 026-video.
**Ficheiro:** [008-emitir-fontes-com-pacing-e-procura](intents/001-ott-streaming/units/005-video/stories/008-emitir-fontes-com-pacing-e-procura.md).

## 005-video/003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa.md ✅ GENERATED

**Tarefa:** IMP-020. **Estado:** planned. **Bolt:** 012-video.
**Ficheiro:** [003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa](intents/001-ott-streaming/units/005-video/stories/003-reencaminhar-e-replicar-datagramas-pela-arvore-ativa.md).

## 005-video/004-reconstituir-frames-com-buffers-e-prazo-limitados.md ✅ GENERATED

**Tarefa:** IMP-021. **Estado:** planned. **Bolt:** 027-video.
**Ficheiro:** [004-reconstituir-frames-com-buffers-e-prazo-limitados](intents/001-ott-streaming/units/005-video/stories/004-reconstituir-frames-com-buffers-e-prazo-limitados.md).

## 005-video/005-apresentar-catalogo-e-video-numa-janela-swing.md ✅ GENERATED

**Tarefa:** IMP-022. **Estado:** planned. **Bolt:** 013-video.
**Ficheiro:** [005-apresentar-catalogo-e-video-numa-janela-swing](intents/001-ott-streaming/units/005-video/stories/005-apresentar-catalogo-e-video-numa-janela-swing.md).

## 005-video/006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta.md ✅ GENERATED

**Tarefa:** IMP-023. **Estado:** planned. **Bolt:** 028-video.
**Ficheiro:** [006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta](intents/001-ott-streaming/units/005-video/stories/006-integrar-duas-origens-e-quatro-streams-ponta-a-ponta.md).

## 006-core/001-preparar-core-e-comprovar-replicacao-no-cenario-de-quatro-nos.md ✅ GENERATED

**Tarefa:** IMP-024. **Estado:** planned. **Bolt:** 014-core.
**Ficheiro:** [001-preparar-core-e-comprovar-replicacao-no-cenario-de-quatro-nos](intents/001-ott-streaming/units/006-core/stories/001-preparar-core-e-comprovar-replicacao-no-cenario-de-quatro-nos.md).

## 006-core/005-medir-partilha-no-cenario-de-quatro-nos.md ✅ GENERATED

**Tarefa:** IMP-025. **Estado:** planned. **Bolt:** 029-core.
**Ficheiro:** [005-medir-partilha-no-cenario-de-quatro-nos](intents/001-ott-streaming/units/006-core/stories/005-medir-partilha-no-cenario-de-quatro-nos.md).

## 006-core/002-demonstrar-alternativas-e-adaptacao-no-cenario-de-seis-nos.md ✅ GENERATED

**Tarefa:** IMP-026. **Estado:** planned. **Bolt:** 015-core.
**Ficheiro:** [002-demonstrar-alternativas-e-adaptacao-no-cenario-de-seis-nos](intents/001-ott-streaming/units/006-core/stories/002-demonstrar-alternativas-e-adaptacao-no-cenario-de-seis-nos.md).

## 006-core/006-medir-adaptacao-por-rtt-e-estabilidade.md ✅ GENERATED

**Tarefa:** IMP-027. **Estado:** planned. **Bolt:** 030-core.
**Ficheiro:** [006-medir-adaptacao-por-rtt-e-estabilidade](intents/001-ott-streaming/units/006-core/stories/006-medir-adaptacao-por-rtt-e-estabilidade.md).

## 006-core/003-validar-cenario-complexo-com-duas-origens-e-cinco-clientes.md ✅ GENERATED

**Tarefa:** IMP-028. **Estado:** planned. **Bolt:** 016-core.
**Ficheiro:** [003-validar-cenario-complexo-com-duas-origens-e-cinco-clientes](intents/001-ott-streaming/units/006-core/stories/003-validar-cenario-complexo-com-duas-origens-e-cinco-clientes.md).

## 006-core/004-recolher-metricas-e-avaliar-resultados-e-limites.md ✅ GENERATED

**Tarefa:** IMP-029. **Estado:** planned. **Bolt:** 031-core.
**Ficheiro:** [004-recolher-metricas-e-avaliar-resultados-e-limites](intents/001-ott-streaming/units/006-core/stories/004-recolher-metricas-e-avaliar-resultados-e-limites.md).

## 007-entrega/001-escrever-artigo-com-protocolos-e-avaliacao-critica.md ✅ GENERATED

**Tarefa:** IMP-030. **Estado:** planned. **Bolt:** 017-entrega.
**Ficheiro:** [001-escrever-artigo-com-protocolos-e-avaliacao-critica](intents/001-ott-streaming/units/007-entrega/stories/001-escrever-artigo-com-protocolos-e-avaliacao-critica.md).

## 007-entrega/002-empacotar-codigo-e-verificar-reproducao-da-entrega.md ✅ GENERATED

**Tarefa:** IMP-031. **Estado:** planned. **Bolt:** 018-entrega.
**Ficheiro:** [002-empacotar-codigo-e-verificar-reproducao-da-entrega](intents/001-ott-streaming/units/007-entrega/stories/002-empacotar-codigo-e-verificar-reproducao-da-entrega.md).

## 007-entrega/003-preparar-defesa-e-checklist-de-submissao-humana.md ✅ GENERATED

**Tarefa:** IMP-032. **Estado:** planned. **Bolt:** 032-entrega.
**Ficheiro:** [003-preparar-defesa-e-checklist-de-submissao-humana](intents/001-ott-streaming/units/007-entrega/stories/003-preparar-defesa-e-checklist-de-submissao-humana.md).

## 007-entrega/004-registar-a-submissao-humana.md ✅ GENERATED

**Tarefa:** IMP-033. **Estado:** planned. **Bolt:** 033-entrega.
**Ficheiro:** [004-registar-a-submissao-humana](intents/001-ott-streaming/units/007-entrega/stories/004-registar-a-submissao-humana.md).

Ajuste do Model de 003-overlay em 2026-11-10T14:24:46Z, por pedido do utilizador:
ERROR (17) por fase e M02/M07/M11 ampliados. [Q-C-05](../docs/construction-duvidas.md#q-c-05--receção-de-error-no-imp-004)
pendente de decisão explícita; Model não aprovado e Design não iniciado.
Nenhum ficheiro de Projeto/ alterado.

Continuação de 003-overlay em 2026-11-10T15:00:14Z: Model e Q-C-05 aprovados explicitamente em
2026-11-10T14:48:50Z. [Design preparado para revisão](bolts/003-overlay/ddd-02-technical-design.md),
sem aprovação de Design ou avanço a ADR/Implement/Test. Nenhuma execução Java nova;
V-007 e revisão final/integração pendentes.

Continuação 003-overlay em 2026-11-10T15:20:09Z: Design/Q-C-06 aprovados explicitamente em
2026-11-10T15:13:50Z; [revisão ADR preparada](bolts/003-overlay/adr-review.md), sem aprovação desta etapa
ou início de Implement/Test. D01–D22 mantidos; V-007/regressão/manual e revisão
final/integração pendentes. Nenhum Java, commit ou push novo.

## Estado atual — IMP-004 fechado

Registo documental em 2026-11-10T17:12:15Z.
IMP-004/003-overlay/TASK-007 fechado documentalmente por autorização explícita
do utilizador. Integração local comprovada em main: `8db7b78`
(`8db7b78950e444f3c2917844617be4b9b236ec0f`), com 145 caminhos e hashes conferidos.
Test aprovado: clean verify/903 testes, manual humano Francisco/49 checks/Java 0
confirmado pelo utilizador, V-ADD aprovado; revisão final de Denji aprovada sem
alterações pendentes. Q-C-08 resolvida e limitada a patch/evidência na integração.
Bolt complete; história complete/implemented=true; TASK-007 DONE. A unidade
002-overlay mantém in-progress: 020-overlay e 004-overlay continuam planned,
sem início de outro IMP. Fecho documental sem alteração Java ou repetição do manual.
Sem push ou alegação de integração remota/entrega do projeto.

[Fecho documental](bolts/003-overlay/closure-walkthrough.md). As referências anteriores a estados
pendentes são históricas; não descrevem o estado atual. Não se promovem
requisitos globais, PHASE-02, unidade ou intent a concluídos por este fecho.

## IMP-005 — checkpoint Model, sem implementação

[Modelo preparado](bolts/020-overlay/ddd-01-domain-model.md) em 2026-11-10T17:31:12Z. Dependências
001-base-validada/003-overlay/019-overlay complete, presentes na base pedida
2ea748e. Checkout 90cded6 preservado; Projeto/ e ADD iguais à base. O tipo
continua Simple; apenas preparação Model de Plan, current_stage=plan,
stages_completed=[], completed=null. Q-C-09 regista a lacuna NODE_LEAVE
na API pública, sem alargar os três ficheiros do ADD ou alterar fundações.
Plan não concluído; parar no checkpoint Model. V-008/regressão/manual/revisão
Waheed/integração pendentes. Sem Java/build/testes, commits/push ou outro IMP.

## Plan preparado após aprovação do Model

Model aceite pelo utilizador; Plan de 020-overlay preparado, por aprovar.
Q-C-09: decisão de âmbito resolvida por autorização de extensão aditiva de
lifecycle no NeighborManager e testes próprios; código e validação por realizar.
Base operacional 90cded6, referência histórica 2ea748e. Unidade in-progress,
história sem implemented=true. Sem Implement/Test, commits/push ou DONE.

## IMP-005 — Plan aprovado; Implement limitado ao checkpoint interfaces/testes

Registo em 2026-11-10T17:46:21Z. Plan aprovado explicitamente pelo utilizador, incluindo
Q-C-10 (budget), Q-C-11 (gramática) e Q-C-12 (buffers locais). current_stage=implement;
apenas Plan consta em stages_completed. Interfaces/tipos e testes inicialmente
vermelhos preparados; comportamentos novos continuam pendentes. Test não iniciado.
V-008 completo/L01–L12/M01–M18, regressão completa atual, manual humano antes de
aprovar Test, revisão final Waheed e integração continuam exigidos, sem gates
externos adicionais. Bolt aberto, história não implementada, TASK-008 IN PROGRESS.
Sem commits/push, DONE ou início de outro IMP.

## IMP-005 — Implement preparado para aprovação

Registo em 2026-11-10T18:16:24Z. O utilizador aprovou interfaces/testes vermelhos e autorizou
comportamentos nos cinco ficheiros do Plan. Implement preparado, ainda por aprovar;
current_stage=implement, só Plan em stages_completed. Regressão atual Maven test:
979 testes, zero falhas/erros/ignorados, com 44 CLI e 32 lifecycle novos, TCP e
subprocessos reais. Test não iniciado; clean verify e manual humano antes da sua
aprovação continuam pendentes, tal como revisão final Waheed e integração.
Q-C-09–12 preservadas; send() e testes históricos intactos, sem alteração wire/caps
ou dependências. Não há gates externos adicionais. Bolt aberto, história ainda
não implementada, TASK-008 IN PROGRESS e unidade in-progress. Sem commits/push
ou outro IMP.

## IMP-005 — Implement aprovado; Test para revisão

Aprovação de Implement registada em 2026-11-10T22:05:26Z. O [relatório de Test](bolts/020-overlay/test-walkthrough.md)
consolida V-008, clean verify (981 testes, zero falhas/erros/ignorados), o manual
de Luis e a verificação complementar do agente, com as sondagens preliminares
preservadas. Test aguarda aprovação. A revisão final de Waheed e a integração
da versão revista continuam pendentes.

## 002-overlay/006-reconstruir-vizinhancas-apos-falhas.md ✅ GENERATED

**Tarefa:** IMP-034 (EXTRA-01). **Estado:** complete, implemented=true; 1065 testes, manual Luis/53 checks, Test/revisão técnica aceites e integração local f650be02f2af10b95328f5b50c3d6941e2e75c15.
**Bolt:** 034-overlay-extra. **Ficheiro:** [006-reconstruir-vizinhancas-apos-falhas](intents/001-ott-streaming/units/002-overlay/stories/006-reconstruir-vizinhancas-apos-falhas.md).
IMP-034 aprende candidatos por divulgação dos vizinhos diretos. IMP-035 repara a overlay antes de 005-rotas. Vídeo na integração posterior.

## 002-overlay/007-reparar-vizinhancas-com-candidatos-aprendidos.md ✅ GENERATED

**Tarefa:** IMP-035 (EXTRA-01 B). **Estado:** complete/implemented=true. Cinco etapas, Test1113/manual Luis66, aceitação final do utilizador e integração em main comprovadas.
**Bolt:** 035-overlay-extra. **Ficheiro:** [007-reparar-vizinhancas-com-candidatos-aprendidos](intents/001-ott-streaming/units/002-overlay/stories/007-reparar-vizinhancas-com-candidatos-aprendidos.md).
Usa a cache aprendida de IMP-034 para reparar a overlay.

## IMP-007 — Model v2 preparado para aprovação

Registo em 2026-11-12T14:27:02Z. Base 3dedf007f52f43f53eaf254bef7c25e35f986e5a, main,
entrada limpa. Dependências IMP-006/004-overlay e IMP-035/035-overlay-extra
complete; unidade002-overlay complete. Apenas Model iniciado; current_stage=model,
stages_completed=[], completed=null. Modelo preparado com TCP v2/19 tipos/3 rejeitado,
identidades, sondas, RTT/EWMA, falhas, limites, ownership e shutdown. Q-C-23/24
registam composição e concretizações pendentes; nenhuma aprovação da retoma
cancelada foi reutilizada. V-010/regressão/manual/revisão Fillmore/integração
continuam futuros, sem gates externos adicionais. Sem Java, serviços, Maven,
staging, commits/push ou avanço Design/ADR/Implement/Test.


## IMP-007 — Model aprovado; Design para aprovação

Model e Q-C-23 opção 1/Q-C-24 aprovados explicitamente pelo utilizador nesta
retoma. current_stage=design; só Model concluído; Design/ADR/Implement/Test não
aprovados. Design preparado com allowlist futura de cinco ficheiros, APIs,
limites físicos, admissão/PONG, falhas locais, composição e budget global.
Decisões registadas em ADD e contratos; nenhum Java alterado ou teste executado.
V-010, regressão atual, manual humano antes de aprovar Test, revisão de Fillmore e
integração continuam pendentes. Sem gates externos adicionais. Parar antes de
ADR/Implement; sem staging, commits, push ou fecho. Os registos Model acima são
históricos e não substituem este estado atual.


## IMP-007 — Design aprovado; checkpoint ADR

Registo 2026-11-12T14:48:27Z: utilizador aprovou Design e allowlist exata dos cinco ficheiros.
Confirmou revisão de Fillmore do Design atual; não constitui revisão final do
código ainda não implementado. Q-C-23/24 preservadas. Análise ADR preparada para
aprovação, sem divergência/inviabilidade concreta identificada documentalmente;
reutiliza decisões aceites, sem ADR nova ou alteração de estado das existentes.
current_stage=adr; só Model/Design concluídos. Implement/Test não iniciados.
V-010, regressão atual, manual humano antes de aprovar Test, revisão final do
código e integração pendentes; sem gates externos adicionais. Sem Java, staging,
commits, push ou fecho. Relatório: memory-bank/bolts/005-rotas/adr-review.md.


## IMP-007 — ADR aprovada; primeiro checkpoint Implement (vermelho)

Registo 2026-11-12T14:56:34Z: ADR/reutilização aprovadas pelo utilizador; apenas interfaces/tipos
e primeiros testes autorizados nos cinco ficheiros. current_stage=implement;
Model/Design/ADR concluídos, Implement/Test por aprovar. 16 testes selecionados:
1 passou,15 falharam por asserção,0 erros/ignorados. Não é V-010 concluído nem
regressão. Dependências entre pré-condições e asserções explicadas no walkthrough.
Revisão de Fillmore continua limitada ao Design. Sem comportamentos de sondagem,
composição ou cleanup implementados. Parar antes desses comportamentos.
V-010, regressão atual, manual humano, revisão final do código e integração
pendentes; sem gates externos adicionais. Sem staging, commits, push ou Test.


## IMP-007 — comportamentos Implement para aprovação

Registo 2026-11-12T15:56:53.776762+00:00. Comportamentos do Design implementados nos cinco caminhos aprovados,
mais exceção de teste Q-C-25 exclusivamente pelos dois patches autorizados.
Q-C-23/24 preservadas; Q-C-25 resolvida após regressão04:1176 testes,
0falhas/0erros/0ignorados,exit0 (52monitoring+11integração própria). Testes reais TCP,
reparação20 e JVM da CLI com término comprovado; não substituem manual humano.
current_stage=implement; só Model/Design/ADR concluídos. Implement para aprovação,
Test não iniciado, história não implementada/bolt não complete. M22/clean verify,
manual humano, revisão final do código e integração pendentes. Fillmore confirmou
revisão de Design, não de código final. Target separado; sem staging/commits/push,
fecho ou outro IMP; sem gates externos adicionais. Ver implementation-walkthrough
no bolt e experiments/construction/005-rotas/implement-results.json.


## IMP-007 — Implement aprovado; Test técnico executado, manual humano pendente

Registo 2026-11-12T16:16:45Z. Utilizador aprovou Implement e autorizou Test. Novo clean verify:
1176 testes,0falhas/0erros/0ignorados,exit0;52monitoring+11integração própria.
JAR novo 330491 bytes,SHA256 3f39cab445ca05360525691b15c8f7193b7e0186b8033ef8cbd86d50690cfb15; cópia de evidência fora de target.
V-010/M01–M21/D01–D16 exercitados e mapeados nos XML; M22 conserva manual humano,
aprovação de Test, revisão final do código e integração pendentes. Ensaio técnico
02:24checks e cleanup confirmado; não conta como humano. Manual reproduzível em
experiments/construction/005-rotas/manual-instructions.md, sem logs pessoais criados.
current_stage=test; Model/Design/ADR/Implement concluídos, Test por aprovar;
bolt/história/TASK/unidade ainda em progresso, sem fecho. Código aprovado intacto
por SHA256; Q-C-25 só os patches autorizados. Revisão Fillmore continua só Design.
Sem gates externos adicionais, staging, commits, push ou outro IMP.


## IMP-007 — manual humano02 conferido; Test para aprovação

Registo 2026-11-12T22:03:45Z. Utilizador confirmou manual humano de Francisco em
experiments/construction/005-rotas/manual-humano-execucao-02/. Conferência:
quatro saídas normais JAVA_EXIT0 e R2-01 por SIGKILL intencional9/estado137;
cinco processos registados terminados,logger_error=null,cinco portas disponíveis
no bind final. Sondas bidirecionais correlacionadas; mesma instanceId/nova
connectionId na reconexão e nova instanceId/conexão no reinício.
STOP/CONT/sem-bootstrap/EOF são contexto confirmado pelo operador; CLI apresenta
Down TRANSPORT sem registo de sinais/timestamps/PEER_EOF literal. Limites e
contagens/diagnósticos preservados em manual-human-02-review.json/.md, sem
medições ou resultados humanos inventados. Código/JAR e Q-C-25 intactos.
Test para aprovação; só Model/Design/ADR/Implement concluídos. Clean verify
1176/0/0/0 permanece válido; não repetido sem mudança de código. Revisão final do
código e integração pendentes; Fillmore confirmado apenas para Design. Bolt,
história,TASK e unidade continuam em progresso. Sem commits/push/fecho ou gates
externos adicionais.


## IMP-007 — Test aprovado; pacote para revisão final e integração

Registo 2026-11-12T22:07:58Z. Utilizador aprovou Test: clean verify1176/0falhas/0erros/0ignorados,
V-ADD e manual humano de Francisco execução02 cumprido com os limites documentados.
Cinco etapas concluídas; current_stage=test/status=in-progress/completed=null.
Bolt aberto, história/unidade em progresso e TASK-010 IN PROGRESS até revisão
final do código e integração comprovadas. A revisão anterior de Fillmore abrange
só Design; não há revisão final do código confirmada. Pacote em final-review-summary
no bolt, allowlist exata e hashes em experiments/construction/005-rotas.
Código aprovado e logs preservados; Q-C-25 só os dois patches autorizados;
target/ e alterações alheias excluídos. Sem staging, commits, push, fecho ou outro
IMP; sem gates externos adicionais. Faltam revisão final do código e integração.


## IMP-007 — revisão final da implementação aprovada por Fillmore

Registo 2026-11-12T22:19:48Z. Utilizador confirmou revisão final por Fillmore do código e
evidência atuais, incluindo Q-C-25, aprovada sem alterações pendentes. Confirmação
associada aos seis hashes de código, JAR e manifesto da versão revista em
experiments/construction/005-rotas/fillmore-final-review-confirmation.json.
Não se confunde esta revisão com a revisão anterior de Design. Test/manual já
aprovados. Resta integrar a versão revista; bolt/história/TASK/unidade continuam
em progresso/completed=null. Plano/allowlist/manifesto atualizados e simulação
exata preparados para autorização de integração. Nenhum staging/commit/push ou
fecho autorizado; nenhum novo gate externo ou IMP. Código/JAR/logs intactos.


## IMP-007 / 005-rotas / TASK-010 — fecho documental autorizado

Registo 2026-11-12T22:37:24Z. Integração local comprovada em main: f9905038c614612b6f2798d4eca46dd72934743f;
348 caminhos/hashes conferidos e V-ADD0 na cópia extraída do commit. Evidência
posterior incluída neste fecho em experiments/construction/005-rotas/integration-proof/,
integration-proof.json e integration-proof.md, preservada byte a byte.
Clean verify1176/0falhas/0erros/0ignorados, manual humano Francisco execução02
com limites documentados, revisão final Fillmore incluindo Q-C-25 e integração
cumpridos. Fecho explicitamente autorizado pelo utilizador: bolt/história
complete, implemented=true, TASK-010 DONE. Unidade003-rotas e PHASE-03 mantêm
in-progress;26 outros bolts planned, nenhum novo IMP iniciado. Código/JAR/logs
intactos; Q-C-23/24 e os dois patches Q-C-25 preservados. Sem Java/push/entrega.
Históricos de pendências acima descrevem os checkpoints anteriores; estado atual
neste fecho e em closure-walkthrough.md. V-ADD documental novo, sem repetir Maven
ou o manual. Commit local limitado a documentação e evidência autorizadas.


## IMP-008 / 021-rotas — Model para aprovação

Registo 2026-11-13T16:04:28Z. Opção1 do utilizador autoriza apenas Model. Base limpa
cc952c5f558930f2c67e7dea8fe92612cd8cc705 no ramo Denji, igual a main.
Dependências004-overlay/005-rotas e unidade002-overlay complete; f990503
é ancestral de main, seis hashes de código e JAR de IMP-007 conferidos.
Modelo e M01–M18/V-011 planeados; Q-C-26 proposta para validade/épocas/limites.
current_stage=model, stages_completed=[], completed=null; história/TASK-011
em progresso, sem implemented=true. Model aguarda aprovação antes de Design.
Nenhum Java/teste/config/POM/fixture alterado; apenas inventário/hashes, sem
análise de código, Maven, sockets, commits/push ou outro IMP. V-011/regressão/
manual/revisão final Waheed/integração pendentes; sem gates externos adicionais.
Artefacto: memory-bank/bolts/021-rotas/ddd-01-domain-model.md; evidência em
experiments/construction/021-rotas/. PHASE-03/unidade003-rotas mantêm in-progress.


## IMP-008 / 021-rotas — Model aprovado; Design para revisão

Registo 2026-11-13T16:19:45Z. Utilizador aprovou Model e os três princípios de Q-C-26;
autorizou apenas Design. APIs/tipos imutáveis, limites exatos propostos, retenção,
transição atómica de época, snapshots sem upstream confirmado e D01–D22 descritos
em memory-bank/bolts/021-rotas/ddd-02-technical-design.md. Q-C-26 conserva valores
e horizonte pendentes de revisão; não são ADR aceite nem implementação.
current_stage=design, stages_completed=[model], completed=null; critérios funcionais
por cumprir. Seis caminhos mantidos, nenhum Java/Maven/socket/commit/push/outro IMP.
V-011/regressão atual/manual offline antes de aprovar Test/revisão final Waheed/
integração pendentes; sem gates externos adicionais. Unidade/PHASE-03 em progresso.
Registos de Model para aprovação acima são históricos; só Model está concluído.


## IMP-008 / 021-rotas — Design aprovado; ADR para aprovação

Registo 2026-11-13T16:41:04Z. Utilizador aprovou Design e limites/horizontes concretos de
Q-C-26, exigindo alocação por necessidade. Apenas revisão ADR autorizada.
APIs reais de LinkMetrics/NetworkMonitor/Neighbor/config e DTOs inspecionadas
estaticamente; custo já fornecido em µs/ceil, sem segunda conversão. ADD6.1,
contratos e índice recebem detalhe aprovado, sem ADR nova ou substituição.
Futura ponte de eventos/owner ainda não existe; não se presume integração pronta
nem se alarga âmbito. current_stage=adr, stages_completed=[model,design],
completed=null; revisão ADR para aprovação, Implement/Test não iniciados.
Seis caminhos preservados; nenhum Java alterado/Maven/commit/push/outro IMP.
V-011/regressão atual/manual offline antes de aprovar Test/revisão final Waheed/
integração pendentes; sem gates externos adicionais. Unidade/PHASE-03 em progresso.


## IMP-008 / 021-rotas — Implement para aprovação

Registo 2026-11-13T17:18:48Z. Revisão ADR aprovada pelo utilizador; Implement autorizado só
nos seis caminhos. Dados/custos/tabela implementados aditivamente, +851/−0 contra
cc952c5; nenhum teste anterior alterado. Incrementais novos:33/30falhas→33/27→
33/10→33/0; revisão de record:35/1→35/0. Regressão incremental07:1211/0falhas/
72erros de SocketException por sandbox; preservada. Repetição08 autorizada fora
da sandbox, mesmo código/JDK21:1211/0/0/0,14 XML,35 novos+1176 anteriores.
Walkthrough e D01–D22 em memory-bank/bolts/021-rotas/implementation-walkthrough.md.
D22 parcial: clean verify/JAR/manual offline humano/revisão final Waheed/integração
pendentes. Sem composição RoutingService/OverlayNode ou alteração de wire/caps.
current_stage=implement, stages_completed=[model,design,adr], completed=null;
Implement para revisão, Test não iniciado. História/unidade/TASK/PHASE-03 em
progresso, sem implemented=true ou fecho. Sem gates externos adicionais,
commits/push ou outro IMP. Target gerado inventariado separadamente.


## IMP-008 / 021-rotas — Test técnico executado; manual humano pendente

Registo 2026-11-13T17:43:51Z. Utilizador aprovou Implement e autorizou apenas Test. Novo
clean verify com JDK21:1211 testes/0falhas/0erros/0ignorados,14 XML,exit0.
JAR novo364704 bytes,SHA256 366210cf8c7656ad2948ae1c842796632085aaa4f09afcedcd00a49db0a0ecee,
cópia em experiments/construction/021-rotas/test-build-01/. Fontes aprovados
intactos antes/depois; nenhum Java ou teste do projeto alterado nesta etapa.
Manual técnico Codex em test-manual-runs/20261113T173843Z-92dfcjfm:43 CHECK_PASS,
MANUAL_PASS,compile0,Java0,passed=true,hashes/JAR corretos,JVM terminada.
Não conta como humano. Roteiro guiado para Luis em manual-instructions.md;
execução humana ainda inexistente/pendente. D22 parcial: manual humano antes de
aprovar Test, aprovação de Test, revisão final Waheed e integração pendentes.
current_stage=test, stages_completed=[model,design,adr,implement], completed=null;
bolt/história/TASK/unidade/PHASE-03 em progresso, sem implemented=true ou fecho.
Planeamento EXTRA-02 preexistente preservado; diff separado do Test e de target.
Sem gates externos adicionais, commits/push ou início de outro IMP.


## IMP-008 — Test/manual Luis e revisão final aceites; integração autorizada

Registo 2026-11-13T17:59:03Z. Test aprovado incluindo manual humano Luis em
20261113T174838Z-y23kpe_b:43 CHECK_PASS/MANUAL_PASS/Java0/passed=true/JVM terminada,
mesmo JAR e fontes do clean verify1211/0/0/0. Original preservado por hash.
Utilizador confirmou revisão final por Luis substituindo Waheed; não se presume
revisão de Waheed. Commits, integração e push main/fecho autorizados. Cinco etapas
concluídas; bolt/história/TASK ainda em progresso até integração comprovada;
D22 parcial só por integração. Sem novo Java/build/manual ou outro IMP.
