---
unit: "002-overlay"
intent: "001-ott-streaming"
phase: "construction"
source_phase: "PHASE-02"
status: "complete"
created: "2026-11-02T15:02:08Z"
updated: "2026-11-10T23:27:50Z"
completed: "2026-11-10T23:27:50Z"
unit_type: "backend"
default_bolt_type: "ddd-construction-bolt"
owned_requirements: ["REQ-001", "REQ-002", "REQ-003", "REQ-004", "NFR-004", "REQ-024"]
requires_units: ["001-base-validada"]
---
# Unidade — 002-overlay

Arquitetura e critérios normativos no [ADD](../../../../../add.md). Esta unidade agrupa trabalho do mesmo projeto Java e não cria um deployable.

## Requisitos atribuídos

- REQ-001 — Executar nós como aplicações em máquinas IP.
- REQ-002 — Entrada e saída dinâmica de nós.
- REQ-003 — Estabelecer e manter X vizinhos TCP ou UDP.
- REQ-004 — Descobrir vizinhos por contacto conhecido.
- NFR-004 — Overlay aplicacional sobre transporte IP.
- REQ-024 — Configuração operacional opcional, centralizada e validada.

## Histórias e bolts

- [x] [005-implementar-parametrizacao-operacional](stories/005-implementar-parametrizacao-operacional.md) → [019-overlay](../../../../bolts/019-overlay/bolt.md); IMP-002 (M).
- [x] [001-fornecer-localizacao-inicial-dos-vizinhos-configurados](stories/001-fornecer-localizacao-inicial-dos-vizinhos-configurados.md) → [002-overlay](../../../../bolts/002-overlay/bolt.md); IMP-003 (M).
- [x] [002-gerir-handshake-unicidade-e-reconexao-de-vizinhos](stories/002-gerir-handshake-unicidade-e-reconexao-de-vizinhos.md) → [003-overlay](../../../../bolts/003-overlay/bolt.md); IMP-004 (M).
- [x] [003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel](stories/003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel.md) → [020-overlay](../../../../bolts/020-overlay/bolt.md); IMP-005 (M).
- [x] [004-preparar-evidencia-do-primeiro-marco-intermedio](stories/004-preparar-evidencia-do-primeiro-marco-intermedio.md) → [004-overlay](../../../../bolts/004-overlay/bolt.md); IMP-006 (S).

## Dependências e critérios de fecho

019-overlay/IMP-002: Model aprovado pelo utilizador em 2026-11-03T22:16:54Z;
Design aprovado, com registo em 2026-11-03T22:25:30Z, incluindo alpha/Q-C-03.
Revisão ADR e Implement aprovados; Implement concluído em 2026-11-03T22:55:46Z.
Test aprovado pelo utilizador em 2026-11-04T10:19:06Z (registo), incluindo 824 testes
completos e a sua execução pessoal como Luis em 2026-11-04T10:15:14Z, 25 checks/
MANUAL_PASS e saída Java 0. [Fecho concluído](../../../../bolts/019-overlay/closure-walkthrough.md) em 2026-11-04T10:33:12Z.
Bolt/história complete; revisão final confirmada por Luis, distinta da autoria
do agente Codex, e versão revista integrada em main (09a068f68eca473451e1e80a045c1cf9cf15a1b2).
G-REVIEW-032 cumprido. Unidade continua in-progress: 019-overlay complete,
002-overlay complete e três bolts planned. IMP-003 iniciado em 2026-11-04T10:40:58Z,
apenas Model, por seleção explícita; [modelo preparado para revisão](../../../../bolts/002-overlay/ddd-01-domain-model.md).
Model aprovado em 2026-11-04T10:50:52Z, incluindo Q-C-04. [Design preparado para revisão](../../../../bolts/002-overlay/ddd-02-technical-design.md)
em 2026-11-04T11:04:53Z, aprovado em 2026-11-04T11:25:40Z, incluindo requestClose.
Autorizada apenas ADR, com [revisão preparada](../../../../bolts/002-overlay/adr-review.md)
em 2026-11-04T11:29:33Z, aprovada em 2026-11-04T11:37:45Z. Implement autorizado e
[preparado para revisão](../../../../bolts/002-overlay/implementation-walkthrough.md)
em 2026-11-04T12:08:01Z: cinco ficheiros +1226/−35 e 856 testes incrementais aprovados.
150 hashes restantes e testes anteriores preservados. Implement aprovado em
2026-11-04T12:18:01Z; [Test preparado](../../../../bolts/002-overlay/ddd-03-test-report.md) em 2026-11-04T12:29:39Z:
clean verify com 856 testes/zero falhas e manual Codex/34 checks/saída 0. Nenhuma
alteração Java durante Test. Test aprovado, com registo em 2026-11-04T14:05:14Z, incluindo
repetição pessoal por Luis em 2026-11-04T13:58:36Z–13:58:38Z, 34 CHECK_PASS/
MANUAL_PASS/JAVA_EXIT=0, mesmo JAR. [Fecho concluído](../../../../bolts/002-overlay/closure-walkthrough.md)
em 2026-11-04T14:18:23Z: revisão final atribuída a Waheed confirmada pelo utilizador,
registada em 2026-11-04T14:18:23Z; versão revista 03345e9e1ab56ec04302676cca6fce32d168223d integrada em main.
Bolt/história complete, implemented=true, TASK-006 DONE; unidade mantém in-progress.
A dependência 001-base-validada permanece concluída/integrada.

Concluir 001-base-validada; aplicar também requires_bolts de cada incremento. Histórias e validações devem ter evidência real; TASK pai composta só fecha após todas as partes, revisão e integração. Não presumir GUI, CORE ou submissão.

## Estado atual — IMP-004 em Model

003-overlay iniciado em 2026-11-10T14:01:42Z, apenas Model por seleção explícita.
[Modelo para revisão](../../../../bolts/003-overlay/ddd-01-domain-model.md) preparado em 2026-11-10T14:11:43Z:
três ficheiros autorizados vazios, lista desejada/sessões/handshake/retries e M01–M22.
155 ficheiros de Projeto/ preservados; V-007, regressão/manual e revisão final
atribuída a Denji pendentes. A unidade mantém in-progress: dois bolts complete,
um em Model e dois planned. A descrição acima conserva os checkpoints anteriores;
o estado atual supera a referência histórica a três bolts planned.
Aguardar aprovação antes de Design/ADR/Implement/Test; sem commits/push/fecho.

Revisão Model do IMP-004 em 2026-11-10T14:24:46Z: ERROR (17) agora explícito na descoberta,
handshake e ACTIVE, com M02/M07/M11 ampliados. Q-C-05 pendente de decisão sobre
retry e ERROR inesperado antes de ACTIVE. Model ainda não aprovado; sem Design,
Java, execução V-007/regressão/manual, commits ou push.

## Estado atual — IMP-004 em Design

Model/Q-C-05 aprovados em 2026-11-10T14:48:50Z; apenas Design autorizado.
[Desenho para revisão](../../../../bolts/003-overlay/ddd-02-technical-design.md) preparado em 2026-11-10T15:00:14Z:
owner serial partilhável, B=min(4,Q)/R=min(N,C×B), worker DNS/I/O e barreira dos dials
nativos ainda não concluídos. D01–D22 previstos. As pendências Model/Q-C-05 dos
retratos anteriores estão resolvidas por esta aprovação; Design ainda não aprovado.
Unidade in-progress, 003-overlay em Design; V-007/regressão/manual, revisão final
atribuída a Denji e integração pendentes. Nenhum Java/build/teste/commit/push novo.

## Estado atual — IMP-004 em ADR

Design/Q-C-06 aprovados em 2026-11-10T15:13:50Z; apenas ADR autorizada.
[Revisão para validação](../../../../bolts/003-overlay/adr-review.md) preparada em 2026-11-10T15:20:09Z:
conformidade do owner próprio/emprestado, limites físicos, B/R e barreira de dials,
sem contrariar Q-C-05 ou APIs/bytes existentes. Unidade mantém in-progress;
003-overlay em ADR, Model/Design concluídos. Nenhum Java/build/teste ou outro IMP
iniciado. Revisão final por Denji/integração pendentes; parar antes de Implement/Test.

## Estado atual — IMP-004 em Implement

ADR explicitamente aprovada; Q-C-07 resolvida com reposição externa autorizada
de EventLoop/RealDeadlines. [Implement para checkpoint](../../../../bolts/003-overlay/implementation-walkthrough.md)
preparado em 2026-11-10T16:12:36Z. Regressão incremental: `mvn -B -ntp test`,
903 testes (740 configuração +66 codec +31 TCP +19 bootstrap +47 vizinhança),
zero falhas/erros/ignorados. V-007 tem execução automatizada TCP local;
Q-C-05/Q-C-06 e D01–D22 preservados. Três ficheiros do IMP +1745/−0;
BootstrapServer externo +482/−142 separado, 151 ficheiros restantes preservados.
Implement ainda aguarda aprovação; Test/clean verify/JAR/manual não iniciados,
revisão final atribuída a Denji e integração pendentes. TASK/bolt/história/unidade
continuam em progresso, sem implemented=true, commits/push ou fecho.

## IMP-004 — Test preparado, aceitação pendente

Implement aprovado explicitamente pelo utilizador; [relatório Test](../../../../bolts/003-overlay/ddd-03-test-report.md)
preparado em 2026-11-10T16:26:35Z. Clean verify atual: 903 testes (47 de vizinhança),
zero falhas/erros/ignorados. Manual interativo executado pelo agente Codex no
JAR verificado: 49 CHECK_PASS, MANUAL_PASS/CLEANUP_COMPLETE, saída Java 0.
XML/JAR/hash/perfis/log e instruções de repetição pessoal preservados em
experiments/construction/003-overlay/. Código aprovado e ferramentas intactos
durante Test. V-007/D01–D22 têm consolidação técnica local, com limites explícitos.
Q-C-08 continua aberta: V-ADD reprovado com três erros, sem alteração de ferramentas
ou redução do alcance. Test ainda não aprovado; revisão final humana e integração
pendentes. TASK/bolt/história/unidade permanecem em progresso, sem implemented=true,
commits/push/fecho ou GUI/CORE presumidos.

## Manual humano aprovado — Test ainda aberto

Registo de conferência em 2026-11-10T16:38:44Z.
Manual humano executado e aprovado por Francisco, confirmado pelo utilizador.
Conferência do log original: 49 CHECK_PASS distintos, zero CHECK_FAIL,
MANUAL_PASS CHECKS=49 e CLEANUP_COMPLETE; hash do JAR coincide com o clean verify
e os cinco hashes de configuração coincidem com os ficheiros da execução.
JAVA_EXIT=0 é confirmação explícita do utilizador; esse marcador não consta
do log original do driver, que foi preservado sem edição.
Test continua por aprovar até tratar V-ADD. Q-C-08 continua aberta; correção
mínima preparada e ensaiada apenas em cópia isolada, não aplicada às ferramentas.
Sem fecho/commits/push; revisão humana final e integração continuam pendentes.

[Log Francisco](../../../../../experiments/construction/003-overlay/manual-Francisco-execucao-01/manual.log) · [Conferência](../../../../../experiments/construction/003-overlay/manual-Francisco-evidence.json) · [Proposta Q-C-08](../../../../../experiments/construction/003-overlay/q-c-08-proposal.md).

## Test para aprovação — Q-C-08 resolvida

Estado registado em 2026-11-10T16:41:30Z. Patch Q-C-08 aplicado exclusivamente
como autorizado: [diff +5/−3](../../../../../experiments/construction/003-overlay/q-c-08-applied.diff), três ficheiros, restante conteúdo
local preservado. [V-ADD real](../../../../../experiments/construction/003-overlay/q-c-08-v-add-applied.log) passou: saída 0, zero erros;
validador/alcance intactos. Q-C-08 resolvida. As anteriores referências a
V-ADD reprovado são históricas. Código Java/testes/configurações sem alteração;
clean verify de 903 testes e JAR/hash/XML preservados. Manual humano Francisco
cumprido e aprovado, 49 checks/Java 0. [Checkpoint Test atualizado](../../../../bolts/003-overlay/ddd-03-test-report.md).
Test ainda por aprovar, current_stage=test, completed=null; não há fecho,
commits/push ou nova integração. Revisão humana final e integração pendentes.

## IMP-004 — Test aprovado; fecho pendente

Test aprovado explicitamente pelo utilizador, registo em 2026-11-10T16:44:29Z.
903 testes do clean verify aprovados, manual humano de Francisco cumprido
(49 checks/Java 0) e V-ADD aprovado; Q-C-08 resolvida.
[Resumo/diff para revisão humana final](../../../../bolts/003-overlay/final-review-summary.md).
As cinco etapas estão concluídas; bolt in-progress, current_stage=null,
completed=null. TASK/história/unidade continuam em progresso, sem implemented=true
ou DONE. Não há gates externos adicionais; faltam revisão humana final e
integração. Não executar o fecho nem fazer commits/push nesta autorização.

## Revisão final Denji aprovada — integração por autorizar

O utilizador confirmou explicitamente que Denji realizou e aprovou a revisão
humana final desta versão, sem alterações pendentes. Confirmação registada em
2026-11-10T17:00:44Z; data de execução da revisão não fornecida e não inventada.
Código permanece idêntico ao aprovado em Test; manual humano de Francisco
já cumprido, sem repetição. Q-C-08 resolvida, Test/V-ADD aprovados.
Falta integração autorizada/comprovada para poder tratar DONE. Sem staging,
commits/push, integração ou fecho nesta autorização.

[Registo da confirmação](../../../../../experiments/construction/003-overlay/denji-final-review-confirmation.json).

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

[Fecho documental](../../../../bolts/003-overlay/closure-walkthrough.md). As referências anteriores a estados
pendentes são históricas; não descrevem o estado atual. Não se promovem
requisitos globais, PHASE-02, unidade ou intent a concluídos por este fecho.

## IMP-005 — checkpoint Model, sem implementação

[Modelo preparado](../../../../bolts/020-overlay/ddd-01-domain-model.md) em 2026-11-10T17:31:12Z. Dependências
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

Aprovação de Implement registada em 2026-11-10T22:05:26Z. O [relatório de Test](../../../../bolts/020-overlay/test-walkthrough.md)
consolida V-008, clean verify (981 testes, zero falhas/erros/ignorados), o manual
de Luis e a verificação complementar do agente, com as sondagens preliminares
preservadas. Test aguarda aprovação. A revisão final de Waheed e a integração
da versão revista continuam pendentes.

## IMP-005 — Fecho concluído

Registo de 2026-11-10T22:30:30Z. Revisão final atribuída a Waheed dada como concluída
por confirmação do utilizador. A versão revista `8c30b04` foi integrada
por fast-forward em main. Bolt e história concluídos, TASK-008 DONE.
[Registo de fecho](../../../../bolts/020-overlay/closure-walkthrough.md).

## IMP-006 — Fecho concluído

Registo de 2026-11-10T23:27:50Z. Test foi aceite com o ensaio técnico de V-009
e a repetição foi dispensada em Q-C-15. A revisão atribuída a Fillmore
foi dada como concluída por indicação do utilizador. A versão revista
`cc478d1` foi integrada em main. Bolt e história completos,
TASK-009 DONE. A data e o conteúdo da avaliação aguardam informação docente.
[Registo de fecho](../../../../bolts/004-overlay/closure-walkthrough.md).
