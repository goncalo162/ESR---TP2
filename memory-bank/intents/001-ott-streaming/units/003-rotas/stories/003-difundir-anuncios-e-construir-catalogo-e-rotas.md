---
id: "003-difundir-anuncios-e-construir-catalogo-e-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
status: "complete"
priority: "Must"
completed: "2026-11-14T15:43:38Z"
implemented: true
started: "2026-11-14T11:47:13Z"
updated: "2026-11-14T15:43:38Z"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-012"]
implementation_tasks: ["IMP-009"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-006", "REQ-009", "REQ-012", "REQ-014", "REQ-016"]
adr_ids: ["ADR-004", "ADR-016", "ADR-017", "ADR-018", "ADR-020", "ADR-024"]
bolt: "006-rotas"
size: "M"
---
# História — Difundir anúncios e catálogo

## Valor e âmbito

Como responsável pelo serviço OTT, quero difundir anúncios e catálogo, para cumprir os drivers INT-001, INT-002, REQ-005, REQ-006, REQ-009, REQ-012, REQ-014, REQ-016 com validação concreta.

A tarefa normativa é [IMP-009](../../../../../../add.md#imp-009); o bolt só organiza a execução. A origem legada TASK-012 mantém os seus critérios e revisão.

## Critérios de aceitação

- [x] Catálogo inclui origem e validade dos quatro fluxos.
- [x] Melhores caminhos e alternativas ficam disponíveis.
- [x] Cache, seq, epoch e path impedem tempestade e regressão a época retirada.
- [x] Validação V-012 executada e evidência ligada no progresso.

## Testes

- V-012: grafo em losango e grafo cíclico convergem para custo esperado; segunda chegada melhor é aceite; quatro streams aparecem; retirar uma origem não remove streams da outra; flooding estabiliza.

## Fonte, dependências e revisão

- [TASK-012](../../../../../../docs/tasks/PHASE-03/TASK-012.md).
- Bolts anteriores: 004-overlay, 021-rotas.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: nenhum específico adicional.


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
