---
id: "003-overlay"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "in-progress"
stories: ["002-gerir-handshake-unicidade-e-reconexao-de-vizinhos"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-10T17:00:44Z"
started: "2026-11-10T14:01:42Z"
completed: null
current_stage: null
stages_completed: [{"name": "model", "completed": "2026-11-10T14:48:50Z", "artifact": "ddd-01-domain-model.md"}, {"name": "design", "completed": "2026-11-10T15:13:50Z", "artifact": "ddd-02-technical-design.md"}, {"name": "adr", "completed": "2026-11-10T15:33:12Z", "artifact": "adr-review.md"}, {"name": "implement", "completed": "2026-11-10T16:17:06Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-10T16:44:29Z", "artifact": "ddd-03-test-report.md"}]
requires_bolts: ["001-base-validada", "002-overlay"]
enables_bolts: ["020-overlay"]
requires_units: ["001-base-validada"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-02"
source_tasks: ["TASK-007"]
source_task_order: ["TASK-007"]
implementation_tasks: ["IMP-004"]
size: "M"
validation_ids: ["V-007"]
test_ids: ["V-007"]
external_gates: []
adr_ids: ["ADR-012", "ADR-013", "ADR-018", "ADR-019", "ADR-021", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-004", "REQ-002", "REQ-003", "REQ-004", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 003-overlay — Estabelecer e reconectar vizinhos

## Entrada em Implement — bloqueio externo encontrado

O utilizador aprovou explicitamente a etapa ADR e autorizou Implement nos três
ficheiros previstos, mantendo Q-C-05/Q-C-06 e D01–D22. A inspeção de entrada
encontrou a remoção de EventLoop/RealDeadlines na alteração externa de
BootstrapServer.java: compilação de diagnóstico falha com quatro erros.
[Q-C-07](../../../docs/construction-duvidas.md#q-c-07--base-de-implement-do-imp-004-não-compila-após-alteração-externa)
regista evidência, impacto e proposta de correção separada para aprovação.
Implement não foi executado; nenhum Java foi alterado nesta continuação.
A aprovação ADR permanece válida; metadados históricos de conclusão não foram
inventados. V-007, regressão, manual, revisão humana e integração continuam
pendentes. Paragem por contradição na base, antes do checkpoint Implement.

## Design aprovado — revisão ADR para validação

Iniciado em 2026-11-10T14:01:42Z por seleção explícita do utilizador, apenas Model.
Base Denji 26aaa4ae6775e9c78e1d7658472ccaca0b376427, árvore inicialmente limpa; dependências complete/integradas.
[Modelo concreto](ddd-01-domain-model.md) preparado em 2026-11-10T14:11:43Z: identidade/sessão,
lista inicial, estados, handshake v1, unicidade/reinício e reconexão sem bootstrap,
proprietário serial, prazos/limites e oráculos M01–M22 para V-007.
Três ficheiros autorizados atualmente vazios; nenhum dos 155 ficheiros de Projeto/
foi alterado. V-007, regressão, manual e revisão final atribuída a Denji pendentes.
Parar para aprovação de Model antes de Design/ADR/Implement/Test. Não há nova
execução Java, aprovação de etapa, commit/push ou fecho nesta continuação.
current_stage=model, stages_completed=[], completed=null.

Revisão humana pediu ajustes por lacuna no tratamento de ERROR (17), sem aprovar
Model/Design. [Modelo revisto](ddd-01-domain-model.md#8-autorização-error-17-e-entrega-ao-consumidor):
descoberta/handshake/ACTIVE separados, erros da sessão entregues intactos ao
consumidor, sem ERROR de resposta. M02/M07/M11 ampliados;
[Q-C-05](../../../docs/construction-duvidas.md#q-c-05--receção-de-error-no-imp-004)
registou inicialmente as propostas de retry/erros inesperados.
Model revisto e todas as decisões da tabela Q-C-05 aprovados explicitamente em
2026-11-10T14:48:50Z. Model concluído em stages_completed; autorizada apenas Design.
[Desenho concreto](ddd-02-technical-design.md) preparado em 2026-11-10T15:00:14Z: assinaturas e
valores imutáveis, owner serial próprio/emprestado, retenção B/R, jobs DNS/I/O
limitados, correlação por token/connectionId antes de classificar direção,
barreiras de handshake/ERROR e mapa D01–D22. APIs existentes preservadas.
Parar para revisão de Design antes de ADR/Implement/Test. Nenhum Java/build/teste,
commit/push ou outro IMP. V-007/regressão/manual, revisão final por Denji e integração
continuam pendentes. current_stage=design, completed=null.

## Fonte normativa e âmbito

Ler [IMP-004](../../../add.md#imp-004) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-007](../../../docs/tasks/PHASE-02/TASK-007.md).

## História, dependências e gates

- [ ] [002-gerir-handshake-unicidade-e-reconexao-de-vizinhos](../../intents/001-ott-streaming/units/002-overlay/stories/002-gerir-handshake-unicidade-e-reconexao-de-vizinhos.md).
- Dependências: [001-base-validada](../001-base-validada/bolt.md), [002-overlay](../002-overlay/bolt.md).
- Unidade: [002-overlay](../../intents/001-ott-streaming/units/002-overlay/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-004 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-004 e as ADR do ADD.
3. [x] **adr** → `adr-review.md`; validar a etapa contra IMP-004 e as ADR do ADD.
4. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-004 e as ADR do ADD.
5. [x] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-004 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Uma conexão confirmada por vizinho.
- [ ] Entrada tardia e reconexão funcionam.
- [ ] Endereço UDP só é aceite após handshake.
- [ ] Validação V-007 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-007, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-004](../../../add.md#imp-004).
- Prova futura: `experiments/construction/003-overlay/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Continuação ADR — aguardando revisão

Design aprovado explicitamente em 2026-11-10T15:13:50Z, incluindo prazo próprio de
resolução/bind igual a control.connectTimeout contado antes da fila, separado
do dial TCP. [Q-C-06](../../../docs/construction-duvidas.md#q-c-06--prazo-próprio-de-resolução-e-bind-no-imp-004)
regista a aprovação. [Revisão ADR](adr-review.md) preparada em 2026-11-10T15:20:09Z:
reutilização das decisões aceites, owner próprio/emprestado, limites físicos,
retenção B/R e correlação de dials conformes no desenho, Q-C-05 preservada.
Não foi identificada decisão aceite inviável/nova ADR necessária nesta inspeção.
D01–D22 mantidos, 155 hashes de Projeto/ preservados e nenhum Java/build/teste
executado. Só Model/Design concluídos; current_stage=adr, completed=null.
Parar para validação antes de Implement/Test; revisão final atribuída a Denji e
integração exigidas antes de DONE. Sem commits/push/fecho ou outro IMP.

## Continuação autorizada — Implement

Etapa ADR explicitamente aprovada pelo utilizador; registo em 2026-11-10T15:33:12Z.
Autorizada a reposição das duas classes removidas no ficheiro BootstrapServer.java
como correção externa separada, seguida de Implement nos três ficheiros previstos.
Q-C-05/Q-C-06 e D01–D22 preservados. Implement ainda sujeito a checkpoint;
Test/manual/revisão humana/integração pendentes. Sem commits/push/fecho.

## Estado atual — Implement preparado para checkpoint

Q-C-07 resolvida pela autorização e reposição das classes externas.
[Walkthrough](implementation-walkthrough.md) preparado em 2026-11-10T16:12:36Z:
três ficheiros do IMP +1745/−0; BootstrapServer separado no acumulado +482/−142.
Regressão incremental `mvn -B -ntp test`: 903 testes (47 novos), zero falhas/erros/skips.
151 ficheiros restantes de Projeto/ preservados, Q-C-05/Q-C-06 e D01–D22 intactos.
ADR aprovada; Implement aguarda validação, current_stage=implement; Test não iniciado.
V-007 automatizado TCP local executado; clean verify/JAR/manual e aceitação Test,
revisão final humana/integração pendentes. Sem commits, push ou fecho.

Validação documental global desta continuação: falhou com três erros em exemplos
preexistentes de `.specsmd/`, sem alteração desses ficheiros. Q-C-08 regista
a pendência; V-ADD não aprovado. A regressão Java de 903 testes é independente.

## Implement aprovado — apenas Test autorizado

Aprovação explícita do utilizador registada em 2026-11-10T16:17:06Z.
Autorizado clean verify, consolidação V-007/D01–D22, XML/JAR/hash e manual
reproduzível, que o agente executará antes do checkpoint Test. Código aprovado
congelado; Q-C-08 explícita, sem alterar ferramentas/alcance do validador.
Parar no checkpoint Test, sem fecho, commits/push ou integração.

## Estado atual — Test preparado para checkpoint

[Relatório Test](ddd-03-test-report.md) preparado em 2026-11-10T16:26:35Z.
Clean verify executado com 903 testes/zero falhas/erros/skips; manual interativo
do agente Codex no mesmo JAR: 49 CHECK_PASS, MANUAL_PASS, CLEANUP_COMPLETE e Java 0.
155 ficheiros de Projeto/ e 67 ferramentas congelados preservados.
V-007/D01–D22 consolidados com provas/limites; Q-C-08 continua por resolver,
V-ADD reprovado com três erros. Test não aprovado, current_stage=test,
completed=null. Aprovação Test, revisão final humana e integração pendentes.
Sem commits/push, outro IMP, alteração de ferramentas ou fecho.

## Checkpoint Test mantido aberto — 2026-11-10T16:29:38Z

O utilizador confirmou que conferiu a evidência técnica, mas manteve Test
por aprovar até executar o manual humano. Não há execução humana registada
por esta confirmação. Checkpoint aberto, current_stage=test, completed=null;
Test não entra em stages_completed. Q-C-08 continua por resolver e V-ADD
continua reprovado. Sem gates externos adicionais; critérios/validações,
revisão humana final e integração continuam exigidos antes do fecho.
Não fechar o bolt nem fazer commits/push.

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

[Log Francisco](../../../experiments/construction/003-overlay/manual-Francisco-execucao-01/manual.log) · [Conferência](../../../experiments/construction/003-overlay/manual-Francisco-evidence.json) · [Proposta Q-C-08](../../../experiments/construction/003-overlay/q-c-08-proposal.md).

## Test para aprovação — Q-C-08 resolvida

Estado registado em 2026-11-10T16:41:30Z. Patch Q-C-08 aplicado exclusivamente
como autorizado: [diff +5/−3](../../../experiments/construction/003-overlay/q-c-08-applied.diff), três ficheiros, restante conteúdo
local preservado. [V-ADD real](../../../experiments/construction/003-overlay/q-c-08-v-add-applied.log) passou: saída 0, zero erros;
validador/alcance intactos. Q-C-08 resolvida. As anteriores referências a
V-ADD reprovado são históricas. Código Java/testes/configurações sem alteração;
clean verify de 903 testes e JAR/hash/XML preservados. Manual humano Francisco
cumprido e aprovado, 49 checks/Java 0. [Checkpoint Test atualizado](ddd-03-test-report.md).
Test ainda por aprovar, current_stage=test, completed=null; não há fecho,
commits/push ou nova integração. Revisão humana final e integração pendentes.

## Estado atual — Test aprovado, aguarda revisão final e integração

Aprovação explícita de Test pelo utilizador registada em 2026-11-10T16:44:29Z:
clean verify de 903 testes, manual humano Francisco cumprido e V-ADD aprovado.
Q-C-08 resolvida após o patch exclusivo autorizado e validação com zero erros.
Cinco etapas em stages_completed; current_stage=null (nenhuma etapa em execução).
O bolt permanece status=in-progress e completed=null: o utilizador determinou
manter o fecho pendente de revisão humana final e integração. Não executar
a cascata de fecho antes dessas condições. Sem autorização de commits/push.
[Resumo e pacote para revisão final](final-review-summary.md).

## Revisão Denji pendente — plano de integração

Registo em 2026-11-10T16:50:53Z.
Plano de integração preparado para aprovação, sem staging/commits/push.
Não foi encontrada confirmação registada da revisão humana final atribuída
a Denji; essa revisão mantém-se pendente. Aprovação de Test e manual Francisco
não a substituem. Bolt permanece in-progress/completed=null até revisão e integração.
A lista proposta exclui target/ e alterações locais alheias. Q-C-08 fica
separada como patch/evidência: integrar os três ficheiros de ferramentas completos
importaria conteúdo local anterior alheio (dois não tracked e um substituído
por versão genérica). Essa importação não é proposta nem autorizada.

[Plano/lista exata](integration-plan.md).

## Revisão final Denji aprovada — integração por autorizar

O utilizador confirmou explicitamente que Denji realizou e aprovou a revisão
humana final desta versão, sem alterações pendentes. Confirmação registada em
2026-11-10T17:00:44Z; data de execução da revisão não fornecida e não inventada.
Código permanece idêntico ao aprovado em Test; manual humano de Francisco
já cumprido, sem repetição. Q-C-08 resolvida, Test/V-ADD aprovados.
Falta integração autorizada/comprovada para poder tratar DONE. Sem staging,
commits/push, integração ou fecho nesta autorização.

[Registo da confirmação](../../../experiments/construction/003-overlay/denji-final-review-confirmation.json).
