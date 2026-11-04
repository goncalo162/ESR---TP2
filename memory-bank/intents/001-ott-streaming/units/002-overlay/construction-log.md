---
unit: "002-overlay"
intent: "001-ott-streaming"
created: "2026-11-03T22:09:01Z"
updated: "2026-11-04T10:21:41Z"
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
