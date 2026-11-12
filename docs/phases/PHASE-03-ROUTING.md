# PHASE-03 — Monitorização e descoberta de rotas

## 1. Objetivo

Construir catálogo e candidatos por fluxo usando RTT medido e comprovar percursos sem vídeo.

## 2. Contexto

Incremento 3 de sete, segundo [o roadmap](../08-IMPLEMENTATION-ROADMAP.md). O resultado é verificável antes da fase seguinte. Estado atual: IN PROGRESS. TASK-010 DONE; TASK-011–013 continuam TODO. O resultado global da fase ainda não foi demonstrado.

## 3. Pré-requisitos

PHASE-02 concluída, com evidência do seu entregável; TASK-009 está DONE. Ler os contratos de [07](../07-API-AND-INTERFACES.md), a [arquitetura](../04-ARCHITECTURE.md) e os gates em [12](../12-RISKS-AND-DECISIONS.md). Dependências específicas estão na tabela e em cada tarefa.

## 4. Requisitos abrangidos

RF-003, RF-005, RF-006, RF-008, RF-009, RF-010, RF-012, RF-014, RP-001, RP-004, RP-006, RP-008. As descrições e critérios estão em [02](../02-REQUIREMENTS.md); esta fase pode implementar apenas parte de um requisito transversal, como mostra [13](../13-REQUIREMENTS-TRACEABILITY.md).

## 5. Tarefas

| Tarefa | Objetivo concreto | Dependências | Validação | Estado |
|---|---|---|---|---|
| [TASK-010](../tasks/PHASE-03/TASK-010.md) | Medir RTT e publicar falhas de vizinhança | TASK-007, TASK-008, TASK-009 | V-010 | DONE |
| [TASK-011](../tasks/PHASE-03/TASK-011.md) | Modelar candidatos, custos e snapshots por stream | TASK-003, TASK-010 | V-011 | TODO |
| [TASK-012](../tasks/PHASE-03/TASK-012.md) | Difundir anúncios e construir catálogo e rotas | TASK-007, TASK-010, TASK-011 | V-012 | TODO |
| [TASK-013](../tasks/PHASE-03/TASK-013.md) | Diagnosticar percursos pela tabela de rotas | TASK-008, TASK-012 | V-013 | TODO |

## 6. Ordem de execução

TASK-010 → TASK-011 → TASK-012 → TASK-013. Esta ordem é segura; não iniciar a fase seguinte antes de cumprir o gate desta fase. A estrutura permite trabalho independente dentro da fase se as dependências explícitas estiverem concluídas e o progresso ficar coerente.

## 7. Dependências

O gate anterior e as dependências de cada tarefa são cumulativos. A tarefa inicial depende do fecho da fase anterior, direta ou transitivamente. Não há dependências em tarefas ainda não identificadas. Resolver recursos externos apenas no gate documentado, sem inventar resultados.

## 8. Componentes afetados

monitoring; routing; catálogo de streaming; comandos de diagnóstico em app. Os caminhos precisos e a distinção existente/planeado estão nas tarefas e em [05](../05-PROJECT-STRUCTURE.md).

## 9. Entregáveis

Rotas para várias streams, alternativas sem ciclos e trace até à origem. Preservar configurações, comandos e evidência de V-010 a V-013 nas tarefas e no progresso. Artefactos de testes não são prova de submissão ou de fases seguintes.

## 10. Estratégia de testes

Executar as validações da tabela nas tarefas correspondentes e a demonstração integrada da fase. Usar [09](../09-TESTING-STRATEGY.md) para oráculos, ambiente, evidência e casos de erro. Não adiar os unitários desta fase para PHASE-06.

## 11. Critérios de aceitação

- [ ] Cada tarefa da tabela está DONE com critérios individuais satisfeitos.
- [ ] Quatro streams são descobertas com rotas e alternativas, incluindo melhor anúncio que chega depois.
- [ ] Trace termina na origem esperada e ciclos/expirações não causam flooding ilimitado.
- [ ] O resultado «Rotas para várias streams, alternativas sem ciclos e trace até à origem.» é demonstrável no ambiente da fase.
- [ ] Requisitos cobertos, limitações e recursos pendentes estão coerentes em 13 e 15.
- [ ] Não há regressão nos componentes anteriores afetados pelas alterações.

## 12. Definition of Done

Coordenação do fecho: [Fillmore](../team/FILLMORE.md), com revisão dos três membros segundo a [coordenação da equipa](../team/00-COORDINATION.md). Autor e revisor de cada TASK constam na identificação; a atribuição não altera dependências ou estados.

Cumprir [14](../14-DEFINITION-OF-DONE.md), incluindo evidência real e passagem de contexto. Atualizar [15](../15-PROJECT-PROGRESS.md); marcar a fase DONE apenas depois de todas as tarefas e critérios.

## 13. Riscos e observações

Não suprimir melhorias de anúncio por uma cache que só considera a primeira chegada. Consultar [12](../12-RISKS-AND-DECISIONS.md) para mitigação e responsável. Metas numéricas do plano são propostas; obrigações do PDF continuam vinculativas.

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
