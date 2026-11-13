---
id: "002-modelar-candidatos-custos-e-snapshots-por-stream"
unit: "003-rotas"
intent: "001-ott-streaming"
status: "complete"
priority: "Must"
started: "2026-11-13T16:04:28Z"
updated: "2026-11-13T18:09:37Z"
completed: "2026-11-13T18:09:37Z"
implemented: true
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-011"]
implementation_tasks: ["IMP-008"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-006", "REQ-014", "REQ-021"]
adr_ids: ["ADR-004", "ADR-017", "ADR-018", "ADR-024"]
bolt: "021-rotas"
size: "M"
---
# História — Modelar candidatos e snapshots

## Valor e âmbito

Como responsável pelo serviço OTT, quero modelar candidatos e snapshots, para cumprir os drivers INT-001, INT-002, REQ-005, REQ-006, REQ-014, REQ-021 com validação concreta.

A tarefa normativa é [IMP-008](../../../../../../add.md#imp-008); o bolt só organiza a execução. A origem legada TASK-011 mantém os seus critérios e revisão.

## Critérios de aceitação

- [x] Cada fluxo tem custo, origem, destinos e estado.
- [x] Empates são reproduzíveis e caminhos inválidos rejeitados.
- [x] Expirar um candidato não apaga alternativas válidas.
- [x] Validação V-011 executada e evidência ligada no progresso.

## Testes

- V-011: custos e desempate calculados com fixtures de resultados conhecidos; quatro streams não interferem; alternativa é preservada e snapshot não pode ser alterado por consumidor.

## Fonte, dependências e revisão

- [TASK-011](../../../../../../docs/tasks/PHASE-03/TASK-011.md).
- Bolts anteriores: 004-overlay, 005-rotas.
- Responsável de origem: Fillmore; autoria desta implementação: Codex. Revisor final: Luis, substituindo Waheed por confirmação explícita do utilizador.
- Gates: nenhum específico adicional.

## Estado atual

Model do IMP-008 preparado para aprovação; Q-C-26 pendente. Sem implementação
ou V-011 executado. Critérios por cumprir, revisão final Waheed e integração
pendentes. Não existe aprovação humana deste Model por efeito do seu registo.


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
