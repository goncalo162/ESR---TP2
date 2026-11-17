---
unit: "004-arvores"
intent: "001-ott-streaming"
created: "2026-11-17T15:29:06Z"
last_updated: "2026-11-17T23:51:24Z"
---
# Construction Log — 004-arvores

## Plano de Inception

Cinco bolts DDD, sem replaneamento:007-arvores/IMP-011,023-arvores/IMP-012,
008-arvores/IMP-013,024-arvores/IMP-014 e009-arvores/IMP-015.
Inception concluído em2026-11-03T20:54:11Z;003-rotas e022-rotas complete.

## Estrutura atual

1 - ✅ **007-arvores**: complete; história001 complete/implemented=true, TASK-014
DONE. Test1328/0/0/0, manual Francisco24, revisão final Waheed e integração37fa863
comprovada. [Fecho](../../../../bolts/007-arvores/closure-walkthrough.md).
2 - [ ] **023-arvores**: planned; história002, renovação e poda.
3 - [ ] **008-arvores**: planned; história003, recuperação de upstream.
4 - [ ] **024-arvores**: planned; história004, adaptação por RTT.
5 - [ ] **009-arvores**: planned; história005, segundo marco.

## Histórico de execução

- **2026-11-17T15:29:06Z**:007-arvores iniciado por seleção explícita do
  utilizador, apenas Model. Não presumir aprovação de etapas seguintes.
- **2026-11-17T15:35:13Z**: Model preparado para revisão em
  [ddd-01-domain-model.md](../../../../bolts/007-arvores/ddd-01-domain-model.md).
  Q-C-34/35 propostas, M01–M14 planeados. Aprovação pendente; nenhuma etapa
  marcada concluída, sem Java novo ou execução V-014.
- **2026-11-17T15:36:08Z**: verificação documental V-ADD passou (saída 0,
  zero erros, 4843 referências locais/âncoras); git diff --check passou e
  Projeto/CORE sem diff. Não é aprovação de Model nem execução de V-014.

## Continuação do histórico

- **2026-11-17T17:02:14Z**: Model e Q-C-34/35 aprovados explicitamente pelo
  utilizador. Apenas Design autorizado; decisões também em construction-duvidas.
- **2026-11-17T17:03:23Z**: preservados os três logs anteriores de NeighborDown,
  byte a byte, com SHA-256 em experiments/construction/007-arvores/neighbor-down-baseline.
  Nenhum teste repetido e causa não estabelecida.
- **2026-11-17T17:17:16Z**: Design preparado para revisão; grant remanescente,
  limites de tentativas/respostas/históricos e variantes de seleção divergente
  concretizados. Os14 cenários continuam por executar; nenhuma etapa além de
  Model concluída. Investigação de NeighborDown antes da aceitação de Implement/Test,
  sem alterar testes/fundações fora do âmbito por inferência. Sem gates externos,
  Java, ADR/Implement/Test, commits ou push novos.
- **2026-11-17T17:18:44Z**: V-ADD documental passou (zero erros,4870 referências),
  git diff --check e hashes/cópias dos três logs passaram. Apenas Model concluído;
  12complete/1in-progress/22planned no projeto, current_stage=design e
  external_gates=[] preservados. Projeto/CORE sem diff, HEAD inalterado.
  Log: experiments/construction/007-arvores/design-validation.log.

## Estado atual

- **2026-11-17T17:30:07Z**: Design completo aprovado, incluindo leases/T/A,
  lineagens, capacidades e quotas; apenas revisão ADR autorizada.
- **2026-11-17T17:35:51Z**: revisão ADR preparada após inspeção real de
  Route/RoutingService e dependências somente em leitura. Dois caminhos futuros
  continuam ausentes. Compatibilidade estática nos quatro caminhos; sem nova
  ADR/substituição/extensão identificada como necessária. Precisões de construtor
  Snapshot4, ERROR11/15, scheduler/callbacks e espera física registadas na revisão.
  Etapa ADR por aceitar; só Model/Design concluídos. NeighborDown pendente, sem
  nova investigação/teste/correção. Sem Java, Implement, commits ou push.

## Resumo atual

- **2026-11-17T17:43:38Z**:253 ficheiros Projeto/CORE e os três logs negativos
  conferidos por hash, sem alterações; dois caminhos novos continuam ausentes.
  Model/Design em stages_completed, current_stage=adr, HEAD preservado.
- **2026-11-17T17:44:08Z**: V-ADD documental passou, zero erros/4881 referências,
  git diff --check passou. Preservado o primeiro log com três erros de formato
  de stages_completed; corrigida só a representação para lista inline existente
  noutros bolts. Sem mudar etapas/datas, validador ou Java. Revisão ADR por aceitar;
  Implement/Test e M01–M14 por executar, NeighborDown pendente.

## Situação da unidade

- **2026-11-17T17:50:02Z**: ADR aprovada pelo utilizador, apenas Implement nos
  quatro caminhos. Investigar NeighborDown antes da aceitação, sem correção
  fora do âmbito antes de apresentar. Test/fecho/commits/push/outro IMP não autorizados.
- **2026-11-17T18:29:47Z**: regressão incremental02 passou1324/0/0/0,18 suites,
  após regressão01 de1322.28 testes novos; fontes/hashes/XML/logs preservados.
  Sem etapa Test ou aceitação V-014 integral. Manual humano/revisão Waheed/integração pendentes.
- **2026-11-17T18:36:18Z**: investigação NeighborDown consolidada, negativos intactos,
  original passa nas repetições atuais. Patch Q-C-36 apresentado e não aplicado
  ao teste histórico, fora dos quatro caminhos. Implement não marcado concluído
  ou aceite; nenhuma etapa adicional concluída, sem gate externo novo.

## Resumo da unidade

- **2026-11-17T18:42:01Z**: âmbito conferido: só quatro Java autorizados,
  251 ficheiros Projeto/CORE restantes e testes históricos intactos; hashes
  atuais iguais à regressão02, negativos preservados, Q-C-36 não aplicado.
  V-ADD zero erros/4898 referências, diff check passou. Apenas Model/Design/ADR
  concluídos, Implement em curso e Test/fecho/outro IMP não iniciados.

## Contagens finais

Cinco bolts na unidade:zero complete,um in-progress,quatro planned.
Sem replaneamento; código novo em007-arvores e verificações incrementais,
V-014 integral/aceitação pendentes. Restantes quatro bolts não iniciados.
A falha de NeighborDown observada antes desta ativação (suite1296/1falha e
repetição isolada falhada) mantém os negativos preservados; a investigação
reproduziu a lacuna de sincronização. Q-C-36 foi autorizada/aplicada como exceção
específica; restantes correções fora do âmbito exigem apresentação prévia.
Não altera o fecho histórico de003-rotas ou aprova Implement/Test.

## Retoma Q-C-36 — 2026-11-17T18:56:41Z

Exceção de âmbito explicitamente autorizada pelo utilizador, aplicada apenas como
patch apresentado em OverlayNodeMonitoringTest.java. Todas as asserções, prazos,
restantes testes e quatro fontes de Implement preservados. Negativos/diagnóstico
anteriores intactos e resultados novos separados.
[Prova e âmbito](../../../../../experiments/construction/007-arvores/q-c-36/README.md):
focado NeighborDown 1/0/0/0; duas regressões 1324/1/0/0, apenas D11a na linha562;
isolado D11a 1/0/0/0. NeighborDown/ActivationTest passaram nas duas regressões.
Causa de D11a não estabelecida; nenhuma outra correção aplicada. Implement
reapresentado para revisão, não aprovado; etapa Test/fecho/outros IMP não iniciados,
sem commits/push/gates externos adicionais. V-014 consolidado, regressão aprovada,
manual humano, revisão final Waheed e integração pendentes.

## Investigação Q-C-37 — 2026-11-17T19:13:11Z

Uma execução fixa da classe OttApplicationTest com strace:48/0/0/0. Três ordens
controladas em classes intactas reproduzem perda de erro de handshake pela
continuação de escrita; protocolo/requestId/connectionId/Session/fecho ligados em
[prova própria](../../../../../experiments/construction/007-arvores/d11a-investigation/README.md).
110 artefactos anteriores e247 fontes preservados, incluindo dois negativos e
sucesso isolado de D11a. Não se reconstrói a sequência histórica não medida.
Proposta de patch em dois caminhos de fundação fora de âmbito, não aplicada e
por autorizar; nenhuma transição ou aprovação de Implement/Test/fecho.

## Retoma Q-C-37 — 2026-11-17T21:49:25Z

Utilizador autoriza exclusivamente o patch apresentado em NeighborManager.java
e NeighborManagerTest.java: dois caminhos adicionais, sete no total. Aplicado
sem ajustes, mantendo testes/asserções/prazos/evidências anteriores.
[Prova nova](../../../../../experiments/construction/007-arvores/q-c-37/README.md):
D11b1/0/0/0, D11a original1/0/0/0, seis suites handshake/lifecycle202/0/0/0,
uma regressão completa1325/0/0/0. Sem resposta, Closed e prazo existente retiram
recursos lógicos; TCP real chega a zero reservas e confirma awaitTermination no
budget configurado. Instrumento teve primeiro erro de cwd preservado, corrigido
somente no lançamento; nenhum teste/fundação adicional alterado.
Implement para revisão, sem aceitação/etapa Test/fecho/commits/push/outro IMP.
V-014 consolidado, regressão aprovada, manual humano, revisão Waheed e integração pendentes.

## Retoma M07 / Q-C-38

Implement por aceitar: três testes novos reproduzem cancelamento/revisão antiga e
reserva física. Guarda terminal mínima em RoutingService; Q-C-38 exclusivamente
para corrigir referência do leitor CLI, autorizada pelo utilizador. Regressão
incremental atual1328/0/0/0; negativos e 431 evidências anteriores preservados.
Ver secção11 do walkthrough do bolt007-arvores. Sem Test, staging, commit ou push.
V-014 consolidado, clean verify/JAR final, manual humano, revisão final Waheed e
integração continuam pendentes.

## Q-C-39 resolvida

Patch documental de seis linhas autorizado/aplicado, V-ADD zero erros.
M07 aceite tecnicamente; Implement ainda por aprovar. Java inalterado, sem
nova regressão, Test, staging, commits, push ou fecho.

## Test autorizado — validação técnica

Implement aprovado pelo utilizador, sem alteração Java nesta etapa. Clean verify
1328/0/0/0 com JDK21,18 suites. Primeira execução restrita1328/0/79/0 por sockets
negados preservada. Logs/XML/JAR/hash em experiments/construction/007-arvores/test-02.
V-014 e M01–M14 consolidados no relatório Test do bolt, com limites explícitos.
Prova TCP real19 checks e oráculos adicionais18 checks; ensaio do roteiro24 checks
como agente, não manual humano. Manual humano ainda pendente; Test por aprovar.
Revisão final Waheed/integração pendentes; sem staging, commits/push ou fecho.

## Manual humano e revisão final — confirmação do utilizador

Francisco executou as cinco etapas,24 CHECK_PASS, CLEANUP_COMPLETE, MANUAL_PASS
CHECKS=24, JAVA_EXIT0/PASSED=true e nenhuma divergência. Relatório humano
20261117T233223Z-q2zk5pu4 conferido com hashes/origem do JAR/terminação da JVM.
Waheed aprovou a revisão final da implementação atual, sem alterações pendentes,
por confirmação explícita do utilizador; vinculada aos hashes atuais dos oito
caminhos Java e JAR. Não houve alteração de código ou repetição desnecessária
de regressão/manual. Test por aprovar, plano de integração para autorização.
Sem staging/commits/push/fecho; bolt/unidade/PHASE mantidos em progresso.

## Fecho documental autorizado — registo 2026-11-17T23:51:24Z

Bolt007-arvores e história001 complete, implemented=true, TASK-014 DONE.
Test aprovado pelo utilizador após clean verify1328/0/0/0 e manual humano
Francisco24; revisão final Waheed confirmada sem alterações pendentes.
Versão revista integrada em main37fa863ed416017e733e692f9565899fb61285ae;
extração,568 caminhos/hashes/JAR e V-ADD zero erros conferidos.
Prova posterior em experiments/construction/007-arvores/integration-proof-01.
Unidade004-arvores/PHASE-04 continuam em progresso; quatro bolts planned.
023-arvores apenas desimpedido pela atualização derivada blocks=false, sem
início de execução. Fecho exclusivamente documental/evidência, sem Java/push.
