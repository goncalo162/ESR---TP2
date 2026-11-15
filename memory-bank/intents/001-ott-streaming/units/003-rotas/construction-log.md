# Construction log — unidade 003-rotas

- 2026-11-12T14:21:47Z: 005-rotas/IMP-007 iniciado exclusivamente em Model na base
  3dedf007f52f43f53eaf254bef7c25e35f986e5a; dependências completas. Decisões da retoma cancelada não
  recuperadas como aprovadas; só documentação e validação documental autorizadas.

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


## IMP-008 / 021-rotas / TASK-011 — fecho após integração comprovada

Registo 2026-11-13T18:09:37Z. Versão revista integrada em main: 3b7aa745cac62b71ac8ce7e036656bf7ab21692f;192 caminhos/hashes
iguais ao pacote, V-ADD0/78 numa cópia extraída do commit. Fontes/JAR e relatório
humano Luis conferidos no commit, sem overlay do diretório de trabalho.
Test1211/0/0/0,JDK21/14XML; manual humano Luis43 CHECK_PASS/MANUAL_PASS/Java0/
passed=true/JVM terminada aceites. Revisão final Luis confirmada pelo utilizador,
substituindo Waheed. D22 cumprido; fecho explicitamente autorizado.
Bolt/história complete, implemented=true, TASK-011 DONE. Dez bolts complete,
25 planned, nenhum em execução. Unidade003-rotas/PHASE-03 continuam em progresso;
006-rotas apenas desimpedido, sem início ou autorização para outro IMP.
Código/JAR/logs originais preservados, sem repetição de build/manual. EXTRA-02
preexistente e target gerado fora dos commits. Publicação main autorizada,
resultado só será declarado após push efetivo; nenhum force push.


## IMP-009 / 006-rotas — Model iniciado, para revisão

Registo 2026-11-14T11:54:45Z. Opção1 do utilizador autoriza apenas Model. Base limpa
164ac0dc477c02bc7fde816c761d137ee18ea55d, Denji/main alinhados; dependências
004-overlay/021-rotas e unidade002-overlay complete, IMP-007 transitivo complete.
JAR e seis hashes revistos do IMP-008 conferidos. Modelo de anúncios/catálogo,
validação/path, gerações, cache/quotas, épocas, callbacks e M01–M22/V-012 preparados.
Q-C-28 propõe composição em OverlayNode/OverlayNodeRoutingTest além dos quatro
caminhos; Q-C-29 propõe concretização de geração/retirada/publicidade/saturação.
Ambas pendentes, sem ADD/ADR/âmbito alterado por inferência. current_stage=model,
stages_completed=[],completed=null; critérios funcionais por cumprir. V-012,
regressão/manual atuais, revisão final Denji e integração pendentes. Sem gates
externos adicionais. Sem análise/alteração Java, Maven, sockets, commits/push
ou outro IMP. Unidade003-rotas/PHASE-03 permanecem em progresso.
Artefacto: memory-bank/bolts/006-rotas/ddd-01-domain-model.md; evidência em
experiments/construction/006-rotas/. Parar antes de Design.

## IMP-009 / 006-rotas — Model aprovado; Design para revisão

Registo 2026-11-14T12:31:20Z. Utilizador aprovou Model, Q-C-28 Opção1 e princípios Q-C-29.
ADD/IMP-009 agora explicita seis caminhos: os quatro originais mais OverlayNode
java e OverlayNodeRoutingTest java, compondo routing no owner/lifecycle existentes.
Design propõe limites de cache/pendências/quotas/tickets, ordens de retirada e
publicação por geração, preservação de TTL e tratamento de callbacks antigos.
D01–D32/V-012 planeados, nenhum executado. Só Model concluído; current_stage=design,
stages_completed=[model], completed=null. ADR/Implement/Test não iniciados.
V-012, regressão atual, manual humano antes de aprovar Test, revisão final atribuída
a Denji e integração pendentes; sem gates externos adicionais. Nenhum Java,
Maven, sockets, commit/push ou início de outro IMP. Parar antes de ADR.
Artefacto: memory-bank/bolts/006-rotas/ddd-02-technical-design.md; evidência em
experiments/construction/006-rotas/. Registos anteriores de Model pendente são histórico.

## IMP-009 / 006-rotas — Design aprovado; revisão ADR para aprovação

Registo 2026-11-14T12:53:08Z. Utilizador aprovou Design completo, limites/ordens Q-C-29 e
metadados Q-C-30. ADD e decisões registados; seis caminhos mantidos. Assinaturas
reais conferidas: quatro ficheiros vazios, OverlayNode com owner/monitor/lifecycle
existentes, OverlayNodeRoutingTest novo; dependências apenas lidas.
Revisão ADR preparada, incluindo Q-C-31 proposta para explicitar recusa fatal
real do owner.offer e OVERFLOW terminal só na ligação. Não inventar retoma nem
alterar fundações. Sem nova ADR/substituição aceite, sem Java/Maven/socket.
current_stage=adr; stages_completed=[model,design]; completed=null;32 oráculos
continuam por executar. Implement/Test/V-012/regressão atual/manual humano antes
de Test aprovado/revisão final Denji/integração pendentes. Sem gates externos
adicionais, commits/push ou outro IMP. Parar na revisão ADR.
Artefacto: memory-bank/bolts/006-rotas/adr-review.md; evidência em
experiments/construction/006-rotas/adr-review/. Estados anteriores de Design
pendente são histórico e não anulam esta aprovação explícita.

## IMP-009 / 006-rotas — Implement para revisão antes de Test

Registo 2026-11-14T14:36:19Z. Revisão ADR/Q-C-31 aprovadas explicitamente; Design6/7 e
D14/D15/D29 clarificados conforme contratos reais. Código nos seis caminhos:
RoutingService/StreamInfo/StreamCatalog, composição aditiva OverlayNode e dois
testes autorizados. APIs/testes antigos preservados, sem alterações a codec/
transporte/manager/monitor/tabela/config/POM/fixtures. Sem ampliar âmbito.
Regressão incremental JDK21:1254/0/0/0 em16 XML,43 testes novos. Falhas reais
anteriores e restrições/erro da bancada preservados no walkthrough. D32 parcial;
V-012 com prova incremental, Test/clean verify/JAR novo/manual humano antes da
aprovação Test/revisão final Denji/integração pendentes. D29/D31 incluem detalhes
estáticos ainda sujeitos à revisão; não alegar prova exaustiva em todos os caps.
current_stage=implement; stages_completed=[model,design,adr]; completed=null.
Implement por aprovar; nenhum fecho/commit/push ou outro IMP, sem gates externos.
Artefacto: memory-bank/bolts/006-rotas/implementation-walkthrough.md.
Evidência: experiments/construction/006-rotas/implement/.

## IMP-009 — Implement reapresentado após correção TTL/visibleAt

Registo 2026-11-14T15:00:17Z. Dois desvios reproduzidos em39 testes/2 falhas antes da correção;
ANNOUNCE revalida publicação/Session/TTL na fronteira da porta com tempo novo.
visibleAt remoto avançado preserva metadados válidos e deixa custo/upstream
explicitamente ausentes porque o snapshot não contém candidatos para recalcular;
projeção pura, conservadora, snapshots anteriores intactos. Regressão final15
JDK21:1256/0/0/0,16 XML. Só três Java dentro dos seis caminhos alterados nesta
revisão; evidências anteriores preservadas. Implement para revisão,32 oráculos
mantidos/D32 parcial, sem Test/fecho/commits/push ou outro IMP. Clean verify,
JAR novo, manual humano, revisão final Denji e integração pendentes.
Walkthrough: memory-bank/bolts/006-rotas/implementation-walkthrough.md.
Evidência: experiments/construction/006-rotas/implement-revision-ttl/.

## IMP-009 / 006-rotas — Test técnico, repetição humana pendente

Registo 2026-11-14T15:24:36Z. Implement e correções TTL/visibleAt aprovados explicitamente;
Model/Design/ADR/Implement concluídos, current_stage=test, completed=null.
Clean verify JDK21:1256 testes/0 falhas/0 erros/0 skips,16 XML,45 testes
novos e1211 anteriores. JAR novo411736bytes/SHA256
1a1beb8b849ccf505e4266dda199fe439f93e2a54e2297058a2d43eb2265af93.
237 hashes de Projeto/ preservados, nenhum Java/teste alterado nesta etapa.
Manual Codex53 checks/MANUAL_PASS/Java0/recursos terminados passou; ensaio inicial
falhado e ajuste de observação do auxiliar preservados, sem alterar JAR/fontes.
Roteiro humano pronto; nenhuma execução humana ou aprovação Test presumida.
D32 parcial; revisão final atribuída a Denji e integração pendentes. Sem gates
externos adicionais; sem fecho/commits/push ou início de outro IMP.
Relatório: memory-bank/bolts/006-rotas/ddd-03-test-report.md.
Manual: experiments/construction/006-rotas/manual-instructions.md.

## IMP-009 — Test/manual humano/revisão final aceites; integração pendente

Registo 2026-11-14T15:35:23Z. Utilizador aprovou Test, incluindo manual Luis da execução
20261114T152915Z-8s1k6ruu:53 CHECK_PASS/MANUAL_PASS/Java0,passed=true,
hashes corretos e recursos terminados. Report original preservado. Revisão final
atribuída a Denji confirmada concluída pelo utilizador; sem contacto inferido.
Clean verify1256/0/0/0 e JAR1a1beb8b... intactos. Fecho e commit para main
autorizados. Integração será comprovada antes de DONE; bolt/história/TASK ainda
em progresso, D32 parcial apenas até integração. Sem push/novo IMP.
Artefactos de aprovação em experiments/construction/006-rotas/closure/.

## IMP-009 / 006-rotas / TASK-012 — fecho após integração comprovada

Registo 2026-11-14T15:43:38Z. Versão revista integrada localmente em main: b7d74be5d9bad9fe7f598f29fa17188037647cf2;
268 caminhos e267 hashes do pacote conferidos,237 hashes de Projeto/ iguais ao
Test, V-ADD0/78 numa cópia extraída do commit. Fontes/JAR/manual humano imutáveis.
Clean verify JDK21:1256/0/0/0,16 XML; manual Luis53 CHECK_PASS/MANUAL_PASS/Java0/
passed=true e recursos terminados. Test aprovado; revisão final atribuída a Denji
confirmada pelo utilizador. D32 cumprido após integração; cinco etapas completas.
Bolt/história complete, implemented=true,TASK-012 DONE. Onze bolts complete,
24 planned, nenhum em execução. Unidade003-rotas/PHASE-03 continuam in-progress;
022-rotas apenas desimpedido,planned/não iniciado. Nenhum push solicitado.
Fontes/logs/XML/report humano originais preservados; target/classes/backups
transitórios fora dos commits, mantidos no disco. Commit de fecho posterior;
nenhum hash autorreferente ou resultado de publicação inventado.
Fecho: memory-bank/bolts/006-rotas/closure-walkthrough.md.
Prova: experiments/construction/006-rotas/closure/integration-proof.json.


## IMP-010 — Plan preparado, aprovação pendente

Registo 2026-11-14T16:16:15Z. Base operacional9144707, main, entrada limpa. Dependências
IMP-006/004-overlay, IMP-009/006-rotas e IMP-005/020-overlay complete;
unidade002-overlay complete. Apenas Plan do bolt Simple022-rotas iniciado,
current_stage=plan, stages_completed=[], completed=null.
Plan define APIs, consulta por salto/alternativas, correlação por Session completa,
visited/TTL/erros/prazos, reservas físicas, ownership e shutdown global.
Q-C-32(composição mínima em OverlayNode) e Q-C-33(detalhes/limites) abertas;
nenhum alargamento ou nova decisão aprovado. V-013/T01–T18 apenas propostos;
nenhum Java/Maven/teste de rede/manual executado nesta etapa.
Plan por aprovar; Implement/Test não iniciados, sem staging/commits/push.
Unidade003-rotas/PHASE-03 continuam em progresso; incrementos fechados intactos.
Manual humano, regressão, revisão final atribuída a Fillmore e integração futuros;
sem gates externos adicionais. Evidência em experiments/construction/022-rotas/.


## IMP-010 — interfaces/tipos e testes inicialmente vermelhos

Registo 2026-11-14T16:29:16Z. Plan e Q-C-32/33 opção1 aprovados; quatro caminhos autorizados,
OverlayNode exclusivamente fachada mínima. Apenas interfaces/stubs/tipos/testes:
13 selecionados,2 passam/11 falham por asserção/0 erros/0 ignorados, exit1 esperado;
compilação concluída, sem comportamentos. Algumas asserções ainda bloqueadas
pelo stub; V-013/T01–T18 integral e integração real não executados.
current_stage=implement; só Plan concluído. Parar antes dos comportamentos.
Test/regressão atual/manual humano/revisão final Fillmore/integração pendentes;
sem gates externos adicionais. Nenhum staging/commit/push/fecho ou outro IMP.
Evidência: experiments/construction/022-rotas/implement-interfaces/.


## IMP-010 — comportamentos de Implement para aprovação

Registo 2026-11-14T17:19:49Z. Utilizador autorizou comportamentos nos quatro caminhos Q-C-32/33
aprovados. Implement preparado; regressão final1296/0falhas/0erros/0ignorados,
17 XML frescos,40 novos testes e1256 históricos byte preservados. CLI/TCP reais,
limites/clock/callbacks/shutdown/ausência de subscrição e vídeo observados no
âmbito dos testes. Falha transitória do teste histórico de saída bloqueada na
regressão02 preservada; repetição isolada e regressão03 passaram sem alterações,
causa não estabelecida. Sem clean verify/JAR novo/manual humano nesta etapa.
current_stage=implement; só Plan concluído, Implement por aprovar, Test não iniciado.
V-013/T01–T18 consolidação formal, clean verify/manual humano, revisão final
Fillmore e integração pendentes; sem gates externos adicionais. target/ separado.
Sem staging/commit/push/fecho ou outro IMP. Evidência:
experiments/construction/022-rotas/implement-behavior/regression-03/result.json.
Walkthrough: memory-bank/bolts/022-rotas/implementation-walkthrough.md.


## IMP-010 — Test técnico executado, manual humano pendente

Registo 2026-11-14T17:45:23Z. Implement aprovado pelo utilizador; apenas Test autorizado.
clean verify em Projeto/:1296 testes/0falhas/0erros/0ignorados,17XML novos;
JAR436556bytes,SHA2565f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52.
40 oráculos de trace/1256 históricos executados; V-013/T01–T18 consolidados
tecnicamente com CLI/TCP reais,limites,ausência de11–14/vídeo no intervalo
observado e shutdown físico. Código/POM aprovados intactos; target/ separado.
Falha histórica m13 em Implement preservada sem causa estabelecida; não
reapareceu no clean verify,sem aumentar prazos ou enfraquecer testes.
Roteiro humano pronto; ensaio kind=agent34checks separado e não conta como
manual humano. Primeira cena do auxiliar corrigida após reparação legítima C–S,
sem mudar código/JAR. current_stage=test; só Plan/Implement concluídos.
Test por aprovar até manual humano; depois revisão final Fillmore/integração.
Sem gates externos adicionais,staging/commits/push/fecho ou outro IMP.
Relatório: memory-bank/bolts/022-rotas/test-walkthrough.md.
Evidência: experiments/construction/022-rotas/test-build-01/result.json.


## IMP-010 — pausa da validação e WIP autorizado

Registo 2026-11-14T17:53:17Z. Utilizador interrompeu a validação e autorizou commit WIP local
limitado ao incremento/documentação/evidência,sem target/ ou alterações alheias.
Plan/Implement aprovados; Test técnico1296/0/0/0,JAR/hash preservados.
Manual humano e aprovação Test pendentes; ensaios agent/parciais preservados e
não substituem execução humana. Falha histórica sem causa estabelecida mantém-se.
current_stage=test,status=in-progress,completed=null; só Plan/Implement concluídos.
TASK-013/história/unidade/fase em progresso; sem implemented=true/DONE/complete.
Sem push/revisão final presumida/integração final/novo IMP. Retoma:
experiments/construction/022-rotas/RESUME.md.


## IMP-010 — Test e revisão final aprovados; integração do fecho pendente

Registo 2026-11-15T16:31:56Z. Utilizador aprovou Test, incluindo o manual humano de Luis
20261115T160738Z-me3p9b08:34 CHECK_PASS,passed=true,quatro Java0 e um SIGKILL
intencional; cinco JVM recolhidas sem cleanup forçado,zero UDP no intervalo,
portas livres e233 hashes conferidos. Report original e todos os logs preservados.
Luis confirmou revisão final e aprovação da versão, substituindo Fillmore,
incluindo os NO_ROUTE anteriores às falhas e a falha histórica m13 de shutdown.
Ambas as observações são preservadas; não se alega disponibilidade contínua nem
causa exata de cada retirada ou da falha histórica. Clean verify1296/0/0/0 e JAR
5f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52 mantêm-se
inalterados; não houve nova execução Java. Test concluído,bolt ainda in-progress
até integração desta evidência e cascata autorizada. Fecho de bolt/PHASE-03,
commits e push autorizados; nenhum outro IMP iniciado.
Aprovação: experiments/construction/022-rotas/closure/test-acceptance.json.
Revisão: experiments/construction/022-rotas/closure/final-review-confirmation.json.
