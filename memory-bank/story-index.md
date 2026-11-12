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
003-overlay/IMP-004 complete, história implemented=true, TASK-007 DONE após revisão Denji e integração local 8db7b78. 020-overlay/IMP-005 complete após revisão final confirmada pelo utilizador e integração da versão revista em main. 004-overlay/IMP-006 complete após Test, revisão confirmada e integração. Total atual: oito histórias/bolts complete e27 planned; unidade002-overlay complete, PHASE-02 DONE incluindo EXTRA-01 de controlo.
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

**Tarefa:** IMP-007. **Estado:** planned. **Bolt:** 005-rotas.
**Ficheiro:** [001-medir-rtt-e-publicar-falhas-de-vizinhanca](intents/001-ott-streaming/units/003-rotas/stories/001-medir-rtt-e-publicar-falhas-de-vizinhanca.md).

## 003-rotas/002-modelar-candidatos-custos-e-snapshots-por-stream.md ✅ GENERATED

**Tarefa:** IMP-008. **Estado:** planned. **Bolt:** 021-rotas.
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
