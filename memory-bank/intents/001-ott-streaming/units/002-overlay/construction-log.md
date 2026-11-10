---
unit: "002-overlay"
intent: "001-ott-streaming"
created: "2026-11-03T22:09:01Z"
updated: "2026-11-04T12:29:39Z"
---
# Registo Construction — 002-overlay

- **2026-11-03T22:09:01Z**: iniciado 019-overlay/IMP-002/TASK-032 em model, por pedido explícito
  do utilizador para executar o próximo bolt e apenas a primeira etapa. Leitura
  e estado comunicados antes de escrever. Dependência 001-base-validada complete
  e integrada; árvore inicial limpa em Denji, HEAD 6bef7425a1f9237f9f744f26f3aa7e6067778025.
  Baseline preservada em experiments/construction/019-overlay/baseline.json.
- G-REVIEW-032: designar revisor humano distinto do autor antes de DONE;
  não se presume designação/aceitação. Nenhum código/teste/build executado.
- Parar no checkpoint Model antes de Design, ADR, Implement ou Test.
- **2026-11-03T22:11:18Z**: modelo concreto preparado: 30 chaves/sete grupos do ADD 6.3,
  quatro relações cruzadas, aplicabilidade por papel, carga sem rede/estado parcial
  e invariantes de imutabilidade/compatibilidade. Nenhuma decisão nova requerida.
  V-032/build/regressão/manual não executados; nenhuma fundação/teste alterado.
  Aguardar aprovação do checkpoint Model antes de iniciar Design.

Modelo: [ddd-01-domain-model.md](../../../../bolts/019-overlay/ddd-01-domain-model.md).

- **2026-11-03T22:16:54Z**: Model aprovado explicitamente pelo utilizador. Model concluído;
  iniciado somente Design. Autorizados assinaturas/tipos imutáveis, unidades e
  conversões, compatibilidade do construtor, erros agregados/casos V-032 e prova
  offline de consumo dentro dos quatro ficheiros autorizados. Parar em Design,
  antes de ADR/Implement/Test. G-REVIEW-032 e execuções Java continuam pendentes.
- **2026-11-03T22:20:11Z**: Design preparado para revisão: sete records imutáveis com Duration
  em ms exatos, overload anterior de NodeConfiguration preservado, parsing sem
  fallback inválido e relações/erros agregados. D01–D17 especificam V-032 e o
  consumo offline pelo record Transport.Options, sem new Transport/socket/wiring
  de app fora dos ficheiros autorizados. Nenhuma implementação/teste Java executado.
  Aguardar revisão de Design antes de ADR/Implement/Test; gate distinto pendente.

Design: [ddd-02-technical-design.md](../../../../bolts/019-overlay/ddd-02-technical-design.md).

- **2026-11-03T22:25:30Z**: Design e gramática decimal de alpha da secção 4 aprovados
  explicitamente pelo utilizador. Design concluído; iniciado apenas ADR.
  Registar Q-C-03 e confirmar ADR-007/010/016/024/026 antes de código. Parar
  para revisão antes de Implement/Test. G-REVIEW-032 continua pendente; não há
  revisor distinto designado nem V-032/regressão/manual deste incremento executados.
- **2026-11-03T22:27:22Z**: revisão ADR preparada: conformidade de Design com
  ADR-007/010/016/024/026 e reutilização, sem decisão substituída/ADR nova ou
  divergência material identificada. Q-C-03 regista a gramática decimal de alpha
  com aprovação explícita. Construtor/testes/bytes/caps preservados; a análise
  documental não é prova de implementação. Aguardar revisão antes de Implement/Test.

ADR: [adr-review.md](../../../../bolts/019-overlay/adr-review.md).

- **2026-11-03T22:33:00Z**: revisão ADR aprovada explicitamente pelo utilizador. Iniciado
  apenas Implement, nos quatro ficheiros autorizados e documentação. Introduzir
  casos V-032 e registar falhas reais, implementar incrementalmente e preservar
  construtor/testes/erros/bytes/caps. Testes incrementais autorizados com logs;
  parar antes da etapa Test/fecho. G-REVIEW-032 permanece pendente.
- **2026-11-03T22:36:54Z**: primeiro red real: 739 testes de configuração,
  130 falhas/160 erros novos; 65 casos anteriores aprovados. API/defaults mínimos,
  sem guards/parsing completos. Log/XML preservados; testes antigos não alterados.
- **2026-11-03T22:37:56Z**: guards/defaults: 127 testes selecionados aprovados.
- **2026-11-03T22:39:28Z**: carga/overrides/erros agregados: 739 testes aprovados.
- **2026-11-03T22:41:46Z**: última execução: 806 testes (740 configuração, 66 codec),
  zero falhas/erros/skips. Inclui 65 casos de configuração anteriores e 675 novos.
  Após o primeiro green, acrescentada cobertura de espaços/zeros e node.role obrigatório.
- **2026-11-03T22:46:19Z**: walkthrough D01–D17 preparado para revisão. Só quatro ficheiros
  autorizados de fonte/teste; duas linhas originais substituídas para runtime,
  construtor de 13 argumentos mantido e nenhuma linha de teste antigo removida.
  Etapa Test, clean verify/transporte/manual e G-REVIEW-032/revisão/integração
  pendentes. Parar em Implement, sem commit/push ou fecho.

Implement: [walkthrough](../../../../bolts/019-overlay/implementation-walkthrough.md).

- **2026-11-03T22:55:46Z**: Implement aprovado explicitamente pelo utilizador. Iniciado apenas
  Test: clean verify/JDK21 com TCP real e log/XML novos, consolidação D01–D17,
  manual de defaults/overrides/consumidor offline/erros agregados executado pelo
  agente e comandos para repetição pessoal do utilizador. Parar antes de fecho,
  commits/push ou outro IMP. G-REVIEW-032/revisão final/integração pendentes.
- **2026-11-03T22:56:28Z**: clean verify/JDK21 completo terminou com código 0,
  824 testes, zero falhas/erros/skips: configuração 740, codec 66, TCP real 18.
  Execução autorizada fora da sandbox para loopback; log/XML atuais preservados.
  Hashes de código iguais à versão revista de Implement, sem alterações nesta etapa.
- **2026-11-03T22:58:31Z**: manual operacional executado pelo agente Codex sobre
  o JAR produzido, código 0, 25 CHECK_PASS e MANUAL_PASS: 30 defaults/overrides,
  consumidor Options sem sockets, construtor anterior e cinco erros agregados.
  Roteiro/comandos e hashes preservados; não é repetição pessoal pelo utilizador.
- **2026-11-03T23:04:14Z**: Test preparado para revisão e D01–D17/V-032 consolidados. Pendente
  repetição pessoal antes de aprovar Test e revisor humano distinto designado,
  revisão final/integração antes de DONE. Parar sem fechar, commitar/publicar
  ou iniciar outro IMP. As listas geradas em target/ ficam separadas.

Test: [relatório](../../../../bolts/019-overlay/ddd-03-test-report.md).
Manual: [roteiro e comandos](../../../../../experiments/construction/019-overlay/manual-runtime.md).

- **2026-11-04T10:15:14Z**: execução pessoal por Luis, confirmada pelo utilizador:
  25 CHECK_PASS, MANUAL_PASS e saída Java 0. Log /tmp/ott-imp002-manual.tak20f.log
  preservado byte a byte em test-manual-luis.log, separado do agente; original
  mantido. Saída 0 registada como declaração do utilizador, não extraída do log.
- **2026-11-04T10:19:06Z**: aprovação explícita de Test e do manual pessoal registada.
  Test concluído; todas as cinco etapas em stages_completed. Três critérios
  técnicos assinalados, último critério de revisão/integração ainda aberto.
- **2026-11-04T10:21:41Z**: fecho preparado para revisão, sem aplicar cascata complete.
  Bolt/história/unidade/TASK-032 em progresso. G-REVIEW-032: revisor distinto
  designado, revisão final e integração pendentes. Sem commits/push ou outro IMP;
  código/JAR/XML/log do agente intactos e listas geradas separadas.

Fecho: [preparação para revisão](../../../../bolts/019-overlay/closure-walkthrough.md).
Luis: [log pessoal preservado](../../../../../experiments/construction/019-overlay/test-manual-luis.log).

- **2026-11-04T10:31:39Z**: utilizador autorizou fecho/commits/push e confirmou a revisão final
  («sim já revi»): Luis, distinto do agente Codex que produziu a implementação.
  Quatro hashes revistos preservados; identidade Git não é prova de autoria humana.
  Preparar commit da versão revista, integrar em main e só então fechar.

- **2026-11-04T10:33:12Z**: 019-overlay completed — cinco etapas aprovadas; revisão final
  de Luis registada, implementação do agente Codex integrada em main por
  fast-forward no commit 09a068f68eca473451e1e80a045c1cf9cf15a1b2. Cascata specsmd executada
  com código 0: bolt/história complete, implemented=true; unidade in-progress
  com quatro bolts planned. G-REVIEW-032 cumprido. target/ excluído, nenhum outro IMP iniciado.

- **2026-11-04T10:40:58Z**: iniciado 002-overlay/IMP-003/TASK-006, apenas Model, por seleção
  explícita do utilizador. Leitura/estado comunicados antes de escrever; base
  Denji 4ac5808dacf6e97bfd3636d2788db363e926bc95, árvore inicialmente limpa, dependências
  001-base-validada e 019-overlay complete/integradas. Parar no checkpoint Model
  antes de Design/ADR/Implement/Test. Revisor de origem Waheed, sem aceitação presumida.
  Sem alteração Java, build, commit/push ou demonstração de rede nesta etapa.

- **2026-11-04T10:46:15Z**: Model do IMP-003 preparado para revisão: grafo imutável e identidade
  nodeId, contextos temporários por connectionId, estados da consulta, deadline
  total/escrita confirmada e M01–M18 para V-006. Cinco ficheiros Java futuros,
  nenhum alterado; preservados os 155 hashes fora de target. Detalhes de Design
  pendentes; stages_completed vazio até aprovação. Correção derivada da tabela
  PHASE-02: TASK-032 já DONE/integrada; TASK-006 agora IN PROGRESS/Model.
  Sem nova decisão/ADR ou impedimento material identificado nesta análise;
  V-006/build/regressão/manual e revisão final por Waheed continuam pendentes.
  Parar em Model, sem commit/push ou outro IMP.

Model IMP-003: [modelo para revisão](../../../../bolts/002-overlay/ddd-01-domain-model.md).

- **2026-11-04T10:50:52Z**: Model do IMP-003 aprovado pelo utilizador, incluindo coerência
  senderInstance/Endpoint.instanceId e uma decisão de resposta por conexão.
  Registar Q-C-04; Model concluído e iniciado apenas Design. Autorizados fila
  comum, futures/locks, prazos/saturação/shutdown, normalização lexical de hosts
  e injeções de relógio/escrita para M01–M18 dentro dos cinco ficheiros.
  Parar antes de ADR/Implement/Test; sem alterar código ou executar Java.

- **2026-11-04T11:04:53Z**: Design do IMP-003 preparado para revisão: fila única com PendingWrite,
  inFlight e registo limitado de confirmações; publicação no escritor fora de
  locks, snapshot/fecho fora de lifecycle, requestClose aditivo e único budget
  de shutdown. Hosts com normalização lexical existente, sem DNS/identidade IP.
  Injeções Core/Channel/clock/deadlines/eventos e WriteDriver aninhadas nos cinco
  ficheiros autorizados; M01–M18 com oráculos controlados e TCP real planeados.
  Preservados 155 hashes de Projeto/ e evidências Model. Nenhuma alteração/teste
  Java ou revisão ADR executados; parar antes de ADR/Implement/Test, commits/push
  ou fecho. Waheed continua revisor final atribuído, sem aceitação presumida.

Design IMP-003: [desenho para revisão](../../../../bolts/002-overlay/ddd-02-technical-design.md).

- **2026-11-04T11:25:40Z**: Design do IMP-003 aprovado explicitamente pelo utilizador, incluindo
  Transport.requestClose como início de shutdown sem espera e preservação de
  close público. Design concluído, iniciado apenas ADR: conferir sobretudo
  013/014/015/017/020 e necessidade das mudanças internas do transporte.
  Manter conclusões fora de locks, reservas até ambos os workers terminarem,
  limites de filas/timers e M01–M18 com tempo/escrita controláveis. Parar antes
  de Implement/Test; inviabilidade aceite segue ADD 11, sem contornar o gate.

- **2026-11-04T11:29:33Z**: revisão ADR do IMP-003 preparada: conformidade do Design aprovado
  com ADR-013/014/015/017/020 e transversais; ADR-003 continua substituída.
  PendingWrite/inFlight/admitidos e fecho fora de locks justificados pela ADR-015;
  requestClose aprovado mantém close e reservas até ambos os workers terminarem.
  Limites de filas/timers, deadline único e M01–M18 controlados preservados.
  Nenhuma inviabilidade aceite/divergência material identificada nesta inspeção;
  sem ADR nova, mudança do ADD/código ou execução Java. Parar para revisão antes
  de Implement/Test, commit/push, integração ou fecho. Waheed permanece revisor
  final atribuído, sem aceitação/contacto presumidos.

ADR IMP-003: [revisão para aprovação](../../../../bolts/002-overlay/adr-review.md).

- **2026-11-04T11:37:45Z**: revisão ADR aprovada explicitamente. Iniciado apenas Implement
  nos cinco ficheiros e documentação: testes novos/falhas reais primeiro,
  incrementais M01–M18, preservando testes/APIs/bytes/caps. Guardar logs;
  parar antes de Test ou fecho, sem commits/push ou outro IMP.

- **2026-11-04T11:39:11Z**: implement-api-red: 23 testes, 0 falhas, 5 erros, 0 skips; saída Maven 1. Logs/XML e hashes preservados em experiments/construction/002-overlay/.

- **2026-11-04T11:42:46Z**: implement-transport-green: 29 testes, 0 falhas, 0 erros, 0 skips; saída Maven 0. Logs/XML e hashes preservados em experiments/construction/002-overlay/.

- **2026-11-04T11:50:11Z**: implement-core-red: 10 testes, 7 falhas, 2 erros, 0 skips; saída Maven 1. Logs/XML e hashes preservados em experiments/construction/002-overlay/.

- **2026-11-04T11:51:39Z**: implement-bootstrap-green: 41 testes, 0 falhas, 0 erros, 0 skips; saída Maven 0. Logs/XML e hashes preservados em experiments/construction/002-overlay/.

- **2026-11-04T11:55:13Z**: implement-oracles: 48 testes, 0 falhas, 0 erros, 0 skips; saída Maven 0. Logs/XML e hashes preservados em experiments/construction/002-overlay/.

- **2026-11-04T11:58:10Z**: implement-incremental-final: 856 testes, 0 falhas, 0 erros, 0 skips; saída Maven 0. Logs/XML e hashes preservados em experiments/construction/002-overlay/.

- **2026-11-04T12:04:53Z**: Implement preparado para revisão com bootstrap por grafo e
  sendAndFlush/requestClose. M01–M18 mapeados; M18 parcial, clean verify/manual
  de Test pendentes. Cinco ficheiros +1210/−35; 150 hashes restantes, testes
  existentes e provas anteriores intactos. Remoções originais justificadas.
  Parar antes de Test/fecho; Waheed/revisão final/integração pendentes. Sem
  commits/push ou outro IMP.

Implement IMP-003: [walkthrough para revisão](../../../../bolts/002-overlay/implementation-walkthrough.md).

- **2026-11-04T12:06:56Z**: caso offline reforçado com portas TCP reservadas/fechadas
  e recusa de ligação comprovada pelo teste antes da consulta. Nova execução
  das quatro classes: 856 testes aprovados, zero falhas/erros/skips. Código de
  produção igual ao incremento anterior; apenas este teste foi reforçado.
- **2026-11-04T12:08:01Z**: walkthrough final preparado: cinco ficheiros +1226/−35, mapa
  M01–M18 e remoções justificadas. M18/Test/clean verify/manual e revisão final
  por Waheed/integração pendentes; parar antes de Test/fecho.

- **2026-11-04T12:18:01Z**: Implement aprovado explicitamente; iniciada apenas Test. Autorizados
  clean verify/JDK21/TCP real com log/XML/hashes novos, consolidação V-006/M01–M18
  e manual reproduzível de consulta/erro/EOF/prazo/shutdown com comandos para
  repetição pessoal. Parar antes do fecho, sem commits/push ou outro IMP.

- **2026-11-04T12:18:10Z**: clean verify/JDK21/TCP real terminou com saída Maven 0,
  856 testes/zero falhas/erros/skips (19 bootstrap, 740 configuração, 66 codec,
  31 transporte). Quatro XML/txt novos arquivados; cinco hashes iguais a Implement.
- **2026-11-04T12:22:53Z**: manual executado pelo agente Codex sobre o JAR validado:
  34 CHECK_PASS/MANUAL_PASS, saída Java 0; consulta/erro/frame antes de EOF,
  prazo/silêncio/parcial e shutdown com consulta pendente/porta reutilizável.
  JAR SHA-256 2c3c063ac58692d1fd813d7e7564b00af7068906d156cc99126827847d2a9ccc; log do agente separado.
- **2026-11-04T12:29:39Z**: relatório Test/M01–M18 preparado para revisão e roteiro/comandos
  para repetição pessoal disponíveis. Execução técnica V-006 concluída localmente;
  aprovação Test/repetição pessoal, revisão final por Waheed e integração pendentes.
  Nenhum dos 155 ficheiros de Projeto/ fora de target alterado nesta etapa.
  Parar antes de fecho/commit/push ou outro IMP.

Test IMP-003: [relatório para revisão](../../../../bolts/002-overlay/ddd-03-test-report.md).
Manual IMP-003: [roteiro pessoal](../../../../../experiments/construction/002-overlay/manual-bootstrap.md).

- **2026-11-04T13:58:36.658445362Z → 2026-11-04T13:58:38.039499140Z**: execução pessoal
  por Luis, 34 CHECK_PASS, MANUAL_PASS e JAVA_EXIT=0. JAR SHA-256
  2c3c063ac58692d1fd813d7e7564b00af7068906d156cc99126827847d2a9ccc.
- **2026-11-04T14:05:14Z**: aprovação explícita de Test registada; concluída a quinta etapa.
  /tmp/ott-imp003-manual.sivs66.log preservado byte a byte em
  experiments/construction/002-overlay/test-manual-luis.log, separado do agente;
  SHA-256 6d7cc4eb76bc0487128c685f8143be1497255a9b7048aa655424a3a9781bcaa9.
  Autorizada preparação do fecho e commits/push de Denji; revisão final atribuída
  a Waheed e integração em main pendentes, sem bolt complete/DONE.
- **2026-11-04T14:10:25Z**: [fecho preparado](../../../../bolts/002-overlay/closure-walkthrough.md),
  com cinco etapas concluídas, evidência aprovada e pendências explícitas.
  Nenhuma alteração Java nem nova execução Java nesta preparação. Nenhum outro IMP iniciado.

- **2026-11-04T14:18:23Z**: utilizador confirmou «fecha o bolt, a revisão esta feita»;
  revisão final atribuída a Waheed declarada concluída. Verificado que main e
  Denji, locais e remotos, já contêm 03345e9e1ab56ec04302676cca6fce32d168223d; 155 hashes de Projeto/ iguais à
  versão testada e 139 evidências anteriores preservadas.
- **2026-11-04T14:18:23Z**: 002-overlay completed — cinco etapas concluídas. Mecanismo
  specsmd executado com adaptador validado, saída 0: bolt/história complete,
  implemented=true, current_stage=null. TASK-006 DONE; unidade mantém in-progress,
  três bolts planned. Sem código/build/manual novos ou outro IMP iniciado.
  [Fecho](../../../../bolts/002-overlay/closure-walkthrough.md) e
  [log do mecanismo](../../../../../experiments/construction/002-overlay/bolt-complete.log).

- **2026-11-04T14:21:49Z**: V-ADD identificou o campo derivado blocks de 003-overlay ainda true.
  Sincronizado para false após o fecho de todas as dependências; status=planned,
  started=null, sem iniciar IMP-004. Repetida a validação documental.

- **2026-11-10T14:01:42Z**: 003-overlay/IMP-004 iniciado por seleção explícita do utilizador,
  apenas etapa Model. Base Denji 26aaa4ae6775e9c78e1d7658472ccaca0b376427, árvore inicialmente limpa;
  dependências 001-base-validada e 002-overlay complete/integradas em main.
  Parar para revisão antes de Design/ADR/Implement/Test.

- **2026-11-10T14:11:43Z**: 003-overlay Model preparado para revisão: entidades/identidades,
  lista inicial, estados, barreiras de handshake, unicidade/reinício, retry,
  proprietário serial e limites; M01–M22 para V-007 planeados. Três ficheiros
  autorizados existem vazios; 155 hashes de Projeto/ preservados.
  Corrigida apenas a célula histórica de TASK-006 na tabela de PHASE-02, que
  ainda dizia IN PROGRESS apesar do fecho confirmado e do resumo já em DONE.
  Não existe contradição de código/ADD identificada nesta inspeção; pontos de
  integração/interleavings a detalhar em Design. Model ainda não aprovado.
  Parar antes de Design/ADR/Implement/Test, sem build/testes/commit/push/fecho.

Model IMP-004: [artefacto para revisão](../../../../bolts/003-overlay/ddd-01-domain-model.md).

- **2026-11-10T14:24:46Z**: ajuste pedido ao Model do IMP-004 por omissão de ERROR (17) na
  secção de autorização/entrega. Separadas descoberta/handshake/ACTIVE; mensagem
  e sessão preservadas para consumidores futuros; proibição explícita de ERROR
  em resposta a ERROR. M02/M07/M11 ampliados. Q-C-05 registada como proposta
  pendente para políticas não fixadas nas fontes. Sem aprovação de Model,
  avanço para Design, alteração Java, teste/build, commit ou push.

- **2026-11-10T14:48:50Z**: Model revisto e propostas da tabela Q-C-05 aprovados explicitamente
  pelo utilizador. Etapa Model concluída; autorizado apenas Design, com limites
  concretos de retenção/DNS, correlação de dial e executor serial partilhável.
  Parar antes de ADR/Implement/Test, sem Java, testes, commits ou push.

- **2026-11-10T15:00:14Z**: Design do IMP-004 preparado para revisão: assinaturas/valores
  imutáveis, B/R de retenção, worker/jobs DNS/I/O limitados, NativeDial/barreira
  de Contexts não classificados, budgets e owner serial próprio/emprestado.
  D01–D22 previstos; Q-C-05 e Model/oráculos sincronizados como aprovados.
  Contratos de Transport/Connection mantidos e 155 ficheiros de Projeto/
  preservados. Sem execução Java/testes/manuais novos, commits/push ou fecho.
  Parar para revisão antes de ADR/Implement/Test.

Design IMP-004: [artefacto para revisão](../../../../bolts/003-overlay/ddd-02-technical-design.md).

- **2026-11-10T15:13:50Z**: Design aprovado explicitamente, incluindo budget de resolução/bind
  igual a control.connectTimeout, antes da fila e separado do dial TCP.
  Autorizada apenas ADR; registar Q-C-06 e confirmar ownership/limites físicos/
  retenção/barreira de dials, preservando Q-C-05. Parar antes de Implement/Test.

- **2026-11-10T15:20:09Z**: revisão ADR do IMP-004 preparada para validação. Reutilização das
  ADR do ADD; owner próprio/emprestado, limites físicos, retenção B/R e barreira
  de dials analisados, Q-C-05 preservada e Q-C-06 aprovada registada. Sem decisão
  aceite inviável/ADR nova necessária identificada nesta análise estática.
  Explicitado isolamento: canal desconhecido durante DISCOVERING não é o canal
  da consulta legítima e não a termina por uma falha própria. D01–D22 mantidos.
  155 ficheiros de Projeto/ preservados; sem Java/build/testes/commit/push ou fecho.
  Parar para revisão antes de Implement/Test.

ADR IMP-004: [revisão para validação](../../../../bolts/003-overlay/adr-review.md).

## Continuação Implement — 2026-11-10T16:12:36Z

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

[Plano/lista exata](../../../../bolts/003-overlay/integration-plan.md).

## Revisão final Denji aprovada — integração por autorizar

O utilizador confirmou explicitamente que Denji realizou e aprovou a revisão
humana final desta versão, sem alterações pendentes. Confirmação registada em
2026-11-10T17:00:44Z; data de execução da revisão não fornecida e não inventada.
Código permanece idêntico ao aprovado em Test; manual humano de Francisco
já cumprido, sem repetição. Q-C-08 resolvida, Test/V-ADD aprovados.
Falta integração autorizada/comprovada para poder tratar DONE. Sem staging,
commits/push, integração ou fecho nesta autorização.

[Registo da confirmação](../../../../../experiments/construction/003-overlay/denji-final-review-confirmation.json).
