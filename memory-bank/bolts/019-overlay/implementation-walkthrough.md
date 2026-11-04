# IMP-002 — Implement: walkthrough para revisão

Revisão ADR aprovada pelo utilizador, com registo em 2026-11-03T22:33:00Z.
Autorizado somente Implement nos quatro ficheiros de IMP-002 e documentação,
com casos V-032/falhas reais e testes incrementais. Bolt in-progress/implement;
a implementação foi aprovada pelo utilizador em 2026-11-03T22:55:46Z. O
[relatório Test](ddd-03-test-report.md) consolida a execução posterior, com 824
testes e manual do agente/25 checks. Test foi depois aprovado pelo utilizador,
incluindo a repetição pessoal por Luis/25 checks em 2026-11-04T10:15:14Z.
[Fecho preparado](closure-walkthrough.md), com G-REVIEW-032 pendente; os resultados/
limites abaixo retratam o checkpoint Implement.

## Resultado local e âmbito

Implementados os sete records de RuntimeConfiguration, os 30 defaults/chaves e
validações, a extensão aditiva de NodeConfiguration e a carga estrita/agregada.
O construtor anterior de 13 argumentos continua público e delega nos defaults;
o novo canónico recebe runtime como 14.º componente. Testes antigos preservados.

Só estes ficheiros de fonte/teste foram alterados/criados:

- [RuntimeConfiguration.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/RuntimeConfiguration.java):
  novo valor imutável com grupos Control/Overlay/Monitoring/Routing/Subscription/
  Video/Source, guards também na construção direta e defaults centrais imutáveis.
- [NodeConfiguration.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/NodeConfiguration.java):
  runtime não nulo, overload anterior e List.copyOf(streams) preservado.
- [ConfigurationLoader.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/ConfigurationLoader.java):
  allowlists por papel, parsing opcional, defaults só na ausência, quatro relações
  e publicação somente após validar; os métodos públicos não mudaram.
- [ConfigurationLoaderTest.java](../../../Projeto/src/test/java/pt/uminho/esr/ott/config/ConfigurationLoaderTest.java):
  casos novos acrescentados sem apagar/enfraquecer os testes existentes.

Duration é validado antes de conversões, com precisão exata de ms e teto
Integer.MAX_VALUE ms. Alpha segue a gramática aprovada em
[Q-C-03](../../../docs/construction-duvidas.md#q-c-03--gramática-decimal-de-alpha),
com finitude/domínio e rejeição de underflow para zero. Tipos inválidos entram
em ConfigurationException com file/key; não se usa default para esconder erro.
Relações com operando inválido não inventam diagnósticos com sentinelas.

POM, perfis, fixtures, codec, transporte e respetivos testes permanecem intactos.
Sem sockets/threads/timers na carga ou no novo modelo; sem dependência runtime,
mudança de bytes/caps, ADR nova ou âmbito adicional. Wiring real dos serviços
permanece nos seus IMP. O consumidor offline é apenas o record Transport.Options.

## Falhas reais e implementação incremental

Comandos a partir da raiz, através de
[run-incremental.py](../../../experiments/construction/019-overlay/run-incremental.py),
que executa Maven em Projeto/ com JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64,
captura stdout/stderr/código de saída e copia só os relatórios da seleção atual.
JDK observado nos XML finais: 21.0.12.1. Cada fase tem nome/log novo.

| Fase real | Seleção Maven (-Dtest) | Resultado |
|---|---|---|
| implement-red, 22:36:49Z–22:36:54Z | ConfigurationLoaderTest | Código 1; 739 testes, 130 falhas, 160 erros, zero skips |
| implement-records-green, 22:37:53Z–22:37:56Z | ConfigurationLoaderTest#runtimeConstructorsRejectInvalidValues+runtimeConstructorsRejectNullsAndInvalidRelations+runtimeDefaultsPreserveFiveRealProfiles | Código 0; 127 testes, zero falhas/erros/skips |
| implement-config-green, 22:39:24Z–22:39:28Z | ConfigurationLoaderTest | Código 0; 739 testes, zero falhas/erros/skips |
| implement-config-codec-green, 22:41:41Z–22:41:46Z | ConfigurationLoaderTest,MessageCodecTest | Código 0; 806 testes = 740 configuração + 66 codec, zero falhas/erros/skips |

As horas desta tabela são UTC de 2026-11-03. Os números não são somados entre
fases, pois as seleções sobrepõem-se. A última execução contém 65 casos antigos
de configuração, 675 novos e 66 de codec, todos aprovados. Uma nova verificação
de inteiros com espaços/leading zeros foi acrescentada após o primeiro green;
também se incluiu node.role entre as propriedades obrigatórias do caso D16.

O primeiro red teve a API/defaults mínimos e o construtor compatível; os guards
dos grupos e o parsing operacional ainda não estavam completos. Os 65 casos
anteriores passaram; todos os 290 resultados negativos pertenciam aos casos novos:
overrides reconhecidos como desconhecidos, valores inválidos aceites por records
e diagnósticos/receção de overrides ainda ausentes. Não se alterou um teste antigo
para fabricar falha. Depois passaram os guards e os casos da carga, por partes.

Evidência preservada: [red completo](../../../experiments/construction/019-overlay/implement-red.log),
[guards](../../../experiments/construction/019-overlay/implement-records-green-command.json),
[configuração](../../../experiments/construction/019-overlay/implement-config-green-command.json),
[último comando/resultados](../../../experiments/construction/019-overlay/implement-config-codec-green-command.json)
e [resumo extraído dos XML](../../../experiments/construction/019-overlay/implement-results.json).
Os relatórios XML/txt de cada fase estão nas suas pastas *-surefire. O comando
final guarda hashes dos quatro ficheiros antes/depois: idênticos durante a execução.

## Correspondência D01–D17

Métodos abaixo pertencem ao ConfigurationLoaderTest existente. As parametrizações
usam uma tabela independente de 30 chaves, valores literais e accessors esperados;
não geram o oráculo a partir do parser/defaults de produção.

| Design | Métodos/evidência incremental | Estado e limite |
|---|---|---|
| D01 | runtimeDefaultsPreserveFiveRealProfiles | Aprovado nos cinco perfis e nos 30 valores, incluindo grupos inativos |
| D02 | runtimeOverridesReachExactAccessorForEveryAllowedRole | Aprovado: 93 combinações chave/papel, accessor correto e restantes defaults preservados |
| D03 | runtimeScalarBoundariesRespectCrossRelations; runtimeAcceptsApprovedAlphaGrammar | Aprovado: 58 fronteiras escalares e sete alpha válidos; ms→nanos/inteiro exatos e erros relacionais em fronteiras impossíveis |
| D04 | runtimeRejectsInvalidIntegerTokensAndCaps; runtimeAcceptsApprovedAlphaGrammar; runtimeRejectsAlphaGrammarDomainAndUnderflow; runtimeAcceptsWhitespaceAndLeadingZeroIntegers | Aprovado: léxico aprovado, espaços/zeros, overflow/underflow, NaN/infinito e representação proibida |
| D05 | runtimeRejectsInvalidIntegerTokensAndCaps; runtimeConstructorsRejectInvalidValues | Aprovado: limites reduzíveis em carga/construção direta, sem alterar wire |
| D06 | runtimeOverridesReachExactAccessorForEveryAllowedRole; runtimeRejectsInapplicableKeysEvenAtDefault | Aprovado: Control para todos, overlay nos três papéis, Source só SERVER; 54 casos de chave inaplicável, inclusive igual ao default |
| D07 | runtimeRejectsUnknownDuplicatesAndInvalidRoleWithoutFallback; testes existentes rejectsDuplicateDecodedProperties/rejectsUnknownAndRoleInapplicableKeys | Aprovado: typo, duplicada literal/escapada, papel inválido; casos antigos intactos |
| D08 | runtimeEnforcesFourCrossRelations; runtimeScalarBoundariesRespectCrossRelations; runtimeConstructorsRejectNullsAndInvalidRelations | Aprovado: igualdades permitidas/proibidas, chave correta e guards diretos |
| D09 | runtimeValidatesOverridesTogetherWithDefaults | Aprovado: quatro pares inválidos com default, restaurados por overrides coerentes |
| D10 | runtimeAggregatesIndependentLegacyAndRuntimeProblems | Aprovado: problemas independentes de grupos diferentes e erro legado, coleção de problemas imutável |
| D11 | runtimeDoesNotInventRelationErrorsFromInvalidOperands | Aprovado: sem erro falso em retry.max; outra relação continua a ser diagnosticada |
| D12 | runtimeConstructorsRejectInvalidValues; runtimeConstructorsRejectNullsAndInvalidRelations | Aprovado: 125 casos de valores inválidos, grupos/Duration nulos, frações e Duration enorme sem overflow do diagnóstico |
| D13 | runtimePreservesOldConstructorAndCanonicalOverrides | Aprovado: overload 13 argumentos, canónico 14, cópia defensiva e runtime não nulo |
| D14 | runtimeOverridesReachTransportOptionsWithoutOpeningSockets | Aprovado: queue=7, connections=13, connect=1111 ms, shutdown=2222 ms chegam ao record real; não instancia Transport |
| D15 | runtimeDeploymentIsAtomicAndOptionsRemainPerNode; runtimePropertiesStayOutOfTopologyAndCatalog | Aprovado: falha conjunta sem resultado parcial, valores por nó e defaults globais intactos; escopo das chaves preservado |
| D16 | runtimeDefaultsDoNotReplaceRequiredConfiguration; resolvesPathsAgainstConfigurationNotWorkingDirectory | Aprovado: identidade/papel/endpoints/paths obrigatórios; perfis temporários fora do cwd |
| D17 | 65 testes anteriores de configuração e 66 de codec aprovados; preservação das fixtures por hash | Parcial: regressão completa de transporte/clean verify e manual deste incremento pendentes para Test, além da revisão/integração |

O teste D14 demonstra carga → RuntimeConfiguration → record consumidor existente.
Não alega OttApplication/bootstrap ligados nem vídeo/GUI/CORE. A ausência de
sockets é sustentada pelo caminho puro do loader e do construtor de Options;
os testes selecionados executaram no ambiente restrito sem permissões TCP.

## Linhas substituídas nas fundações

Comparação contra a [baseline anterior a Implement](../../../experiments/construction/019-overlay/implement-baseline.json):

| Ficheiro | Linha original substituída | Justificação |
|---|---|---|
| NodeConfiguration.java:19 | Cabeçalho terminava em List<StreamConfiguration> streams) | Acrescentar runtime como componente final; o overload público anterior mantém exatamente os 13 argumentos |
| ConfigurationLoader.java:114 | Último argumento da construção era streams | Passar também runtime validado ao novo canónico, preservando todos os anteriores |
| ConfigurationLoaderTest.java | Nenhuma linha original removida | Import de Duration e novos casos/helpers acrescentados; corpos/casos antigos intactos |

Nenhum outro comportamento antigo foi removido. Guards/listas de erros/cópias,
framing, enums com códigos, bytes v1 e hard caps permanecem. RuntimeConfiguration
é ficheiro novo; não substitui fundações. As listas Maven geradas em target/
podem diferir pelo build e são mostradas separadamente, sem lhes atribuir autoria
de código ou incluí-las em qualquer commit por inferência.

## Checkpoint e pendências

Walkthrough e implementação foram revistos/aprovados; Implement concluído após
a aprovação explícita, Test autorizado e executado com relatório próprio. O bolt,
história/unidade e TASK-032 continuam em progresso; critérios de fecho não presumidos.

As pendências de execução acima eram da apresentação de Implement. Clean verify,
TCP e manual do agente foram depois executados em Test; continuam pendentes a
revisor humano distinto designado (G-REVIEW-032), revisão final e integração
na base comum. Test e a repetição pessoal foram aprovados pelo utilizador; o
fecho permanece pendente. Sem commit/push,
fecho, medição ou resultado GUI/CORE inventados. Os 806 testes incrementais não
substituem essas condições nem reutilizam os 149 testes históricos de IMP-001.
