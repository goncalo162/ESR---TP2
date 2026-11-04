# IMP-002 — Design: parametrização operacional

Model aprovado explicitamente pelo utilizador, com registo em 2026-11-03T22:16:54Z.
Design e gramática de alpha aprovados pelo utilizador, com registo em
2026-11-03T22:25:30Z. A [revisão ADR](adr-review.md) foi depois aprovada em
2026-11-03T22:33:00Z, autorizando apenas Implement e testes incrementais.
[Implementação e Test aprovados](ddd-03-test-report.md): 824 testes completos e
execução pessoal por Luis/25 checks confirmados. [Fecho preparado](closure-walkthrough.md),
com revisor distinto designado, revisão final e integração pendentes em G-REVIEW-032.
As assinaturas e estados de execução abaixo retratam o Design aprovado.

Norma: [ADD 6.3](../../../add.md#63-parâmetros-operacionais-opcionais),
[IMP-002](../../../add.md#imp-002), [Model aprovado](ddd-01-domain-model.md) e
[TASK-032](../../../docs/tasks/PHASE-02/TASK-032.md). ADR 007/010/016/024/026
preservadas; revisão formal das ADR aprovada antes da implementação autorizada.

## 1. Ficheiros e limites

| Ficheiro autorizado | Alteração futura limitada a IMP-002 |
|---|---|
| Projeto/src/main/java/pt/uminho/esr/ott/config/RuntimeConfiguration.java | Novo record com sete records aninhados, defaults e invariantes |
| Projeto/src/main/java/pt/uminho/esr/ott/config/NodeConfiguration.java | Acrescentar runtime no fim; preservar construtor de 13 argumentos e accessors anteriores |
| Projeto/src/main/java/pt/uminho/esr/ott/config/ConfigurationLoader.java | Reconhecer as 30 chaves no perfil de nó; parsing opcional/por papel e erros agregados |
| Projeto/src/test/java/pt/uminho/esr/ott/config/ConfigurationLoaderTest.java | Acrescentar casos V-032 e prova offline de consumo/compatibilidade sem remover casos atuais |

Transport/Connection, codec, fixtures, perfis, POM e consumidores de app/overlay/
monitoring/routing/streaming não precisam de alteração. Não criar outro teste
Java, classe de wiring ou dependência. A composição real continua nos respetivos
IMP; este incremento disponibiliza dados validados e prova um consumo offline.

## 2. API de valores imutáveis

Assinaturas propostas em pt.uminho.esr.ott.config; corpos omitidos nesta etapa.
Os sete grupos são public records aninhados em RuntimeConfiguration. Componentes
Duration têm unidade temporal; percentagens, capacidades e contagens são int;
alpha é double finito. Defaults/chaves/papéis são exatamente os 30 do Model.

```java
public record RuntimeConfiguration(
        Control control, Overlay overlay, Monitoring monitoring,
        Routing routing, Subscription subscription, Video video, Source source) {
    public static RuntimeConfiguration defaults();

    public record Control(Duration connectTimeout, Duration shutdownTimeout,
                          int maxConnections, int queueCapacity,
                          Duration bootstrapQueryTimeout) { /* construtor validante */ }
    public record Overlay(Duration helloTimeout, Duration retryInitial,
                          Duration retryMax, int retryJitterPercent,
                          int eventsCapacity) { /* construtor validante */ }
    public record Monitoring(Duration probeInterval, Duration probeTimeout,
                             int probeFailures, double ewmaAlpha) { /* construtor validante */ }
    public record Routing(Duration announceInterval, Duration candidateTtl,
                          int cacheCapacity, Duration cacheTtl, int floodMaxPerSecond,
                          Duration activateTimeout, int activateAttempts,
                          int switchImprovementPercent, int switchObservations,
                          Duration switchMinDwell) { /* construtor validante */ }
    public record Subscription(Duration lease, Duration renew) { /* construtor validante */ }
    public record Video(int egressCapacity, int reassemblyMaxFrames,
                        Duration reassemblyDeadline) { /* construtor validante */ }
    public record Source(int maxFrameBytes) { /* construtor validante */ }
}
```

Cada componente corresponde à chave do seu grupo no Model, pela mesma semântica:
eventsCapacity é node.events.capacity; bootstrapQueryTimeout é bootstrap.query.timeout.ms;
maxFrameBytes é source.max.frame.bytes. A ordem de Control acima é explícita e
difere da ordem do construtor Transport.Options, que recebe queueCapacity primeiro.

defaults() devolve os sete grupos completos; não recebe papel nem faz I/O.
Pode partilhar um valor estático final porque todo o grafo é imutável. Não expõe
Properties, arrays, mapas mutáveis, setters ou builders públicos. Duration é
imutável; não necessita cópia. NodeConfiguration conserva List.copyOf(streams).

Construtores compactos públicos dos grupos verificam intervalos/caps e relações
internas: retryInitial≤retryMax em Overlay; candidateTtl>announceInterval e
cacheTtl≥candidateTtl em Routing; renew<lease em Subscription. Monitoring exige
Double.isFinite(ewmaAlpha), alpha>0 e alpha≤1. Todos os Duration e grupos não nulos;
null provoca NullPointerException, valor/domínio/relação inválidos provocam
IllegalArgumentException. O record exterior exige os sete grupos não nulos.
Assim, criação direta também não publica RuntimeConfiguration numericamente inválido.

A aplicabilidade por papel/presença é política do loader: um objeto de valor não
guarda quais chaves foram fornecidas. BOOTSTRAP rejeita uma chave de overlay mesmo
se o valor for igual ao default; SERVER é o único que aceita source.max.frame.bytes.
Grupos inativos ficam com defaults e não são consumidos. Não se inventa proibição
por igualdade de valores dentro de um record que não contém proveniência de chaves.

## 3. NodeConfiguration: extensão aditiva e compatibilidade

Novo cabeçalho canónico, com os 13 componentes atuais na mesma ordem e runtime
acrescentado no fim:

```java
public record NodeConfiguration(
        Path sourceFile, String nodeId, NodeRole role, String bindHost,
        String advertisedHost, int controlPort, int dataPort,
        Optional<BootstrapContact> bootstrap, Optional<Path> topologyFile,
        Optional<Path> streamsFile, Optional<Path> mediaBase,
        Optional<TopologyConfiguration> topology, List<StreamConfiguration> streams,
        RuntimeConfiguration runtime) { /* validações existentes + runtime não nulo */ }
```

Preservar este overload público, com o descritor anterior:

```java
public NodeConfiguration(
        Path sourceFile, String nodeId, NodeRole role, String bindHost,
        String advertisedHost, int controlPort, int dataPort,
        Optional<BootstrapContact> bootstrap, Optional<Path> topologyFile,
        Optional<Path> streamsFile, Optional<Path> mediaBase,
        Optional<TopologyConfiguration> topology, List<StreamConfiguration> streams);
```

O overload delega no canónico com RuntimeConfiguration.defaults(), sem duplicar
validações ou inferir identidades/hosts/portas. O loader passa explicitamente o
runtime validado ao novo construtor de 14 argumentos. Accessors existentes e
BootstrapContact mantêm nomes/tipos; runtime() é o novo accessor aprovado.
Não substituir a classe por um builder nem expor um NodeConfiguration parcial.

O novo componente participa em equals/hashCode/toString como num record normal;
overrides diferentes são configurações diferentes. A introspeção dos componentes
passa de 13 para 14; não se promete compatibilidade de reflection/layout. O
construtor de 13 argumentos continua disponível e produz valor equivalente ao
canónico quando runtime=defaults(). Sem serialização Java de objetos da rede.

## 4. Unidades, gramática e conversões verificadas

Todas as 16 chaves terminadas em .ms representam milissegundos inteiros entre
1 e Integer.MAX_VALUE. No record são Duration, não segundos, nanos crus ou relógio
UTC. A gramática inteira segue a existente: [0-9]+ após strip; leading zeros são
aceites. String ausente usa default; vazia, sinal, fração, unidade, overflow ou
separador de milhares são erro. Não aceitar silenciosamente propriedade inválida.

Parsing: Integer.parseInt após a verificação lexical, validar intervalo e criar
Duration.ofMillis((long) valor). Nunca multiplicar como int. Quando um consumidor
necessitar nanos, usar toNanos() apenas depois dos limites: máximo de
2_147_483_647_000_000 ns, representável em long. Para int ms, só Math.toIntExact
após a verificação. Não dividir por 1000/truncar nem converter por double.

Construtores diretos dos grupos também exigem precisão exata de milissegundos:
comparar primeiro Duration com 1 ms e Integer.MAX_VALUE ms, antes de toMillis/
toNanos; exigir getNano()%1_000_000==0. Rejeitar frações/submilissegundos, negativos
e Duration enormes sem overflow do próprio diagnóstico. Os limites tornam a
conversão posterior segura; não há conversão de unidade implícita no accessor.

Detalhe lexical aprovado em [Q-C-03](../../../docs/construction-duvidas.md#q-c-03--gramática-decimal-de-alpha),
coerente com o decimal do ADD: aceitar
[0-9]+(\.[0-9]+)? ou \.[0-9]+, depois de strip. Aceitar 1, 1.0, 0.2 e .2;
rejeitar expoente, hexadecimal, sufixo d/f, vírgula, sinal e unidades. Usar
Double.parseDouble e verificar finitude e (0,1]; underflow para zero também é
inválido. Não arredondar, limitar ou substituir por 0.2 um valor inválido.
Isto detalha a gramática da propriedade, sem mudar o domínio do parâmetro.

bootstrap.query.timeout.ms é prazo completo da consulta após a ligação; não se
soma/inicia timer nesta carga. As restantes políticas temporais do ADD permanecem
nos consumidores futuros. Não acrescentar timer, clock, watchdog ou negociação.

## 5. Loader e agregação dos problemas

APIs públicas preservadas:

```java
public static NodeConfiguration load(Path path, NodeRole expectedRole)
        throws ConfigurationException;
public static List<NodeConfiguration> loadDeployment(Collection<Path> paths)
        throws ConfigurationException;
```

Helpers privados propostos dentro de ConfigurationLoader, sem outro ficheiro:

```java
private static RuntimeConfiguration runtime(Properties p, Path file, NodeRole role,
        Set<String> allowed, List<ConfigurationProblem> errors);
private static Integer optionalInteger(Properties p, Path file, String key,
        int defaultValue, int min, int max, List<ConfigurationProblem> errors);
private static Duration optionalDuration(Properties p, Path file, String key,
        Duration defaultValue, List<ConfigurationProblem> errors);
private static Double optionalAlpha(Properties p, Path file, String key,
        double defaultValue, List<ConfigurationProblem> errors);
```

Null nos helpers é marcador privado de parsing inválido, nunca fallback nem
componente de configuração publicada. A ordem de trabalho é:

1. Reutilizar read() estrito e a deteção existente de duplicadas após escapes;
   não trocar por Properties.load permissivo nem perder o primeiro valor.
2. Preservar o parsing legado e as suas validações. Definir allowed por papel,
   acrescentando as cinco chaves Control a todos, 24 overlay a SERVER/RELAY/CLIENT
   e a chave Source apenas a SERVER. allowed é preenchido mesmo se algum valor
   falhar. Os grupos não aplicáveis usam defaults; chaves fornecidas desses grupos
   são rejeitadas por unknownKeys existente, inclusive se iguais aos defaults.
3. Ler todos os campos aplicáveis, agregando erros independentes. Não lançar a
   primeira IllegalArgumentException de um grupo para fora da API de carga.
   Se role for inválido, registar node.role sem adivinhar papel; nenhuma configuração
   pode ser construída. Não interpretar ausência/invalidade de papel como overlay.
4. Verificar cada relação apenas quando ambos os operandos têm valor válido
   (default ou override). Marcar o erro na chave abaixo, sem comparar sentinelas
   numéricas ou fabricar erros cruzados a partir de um parsing que falhou.
5. Construir grupos completos apenas após validar os seus campos/relações;
   o record exterior só existe se todos os grupos forem válidos. Os construtores
   são segunda defesa; uma falha inesperada não deve ser escondida por catch genérico.
6. Executar unknownKeys e o critério atual errors.size()!=start do nó; nenhuma
   configuração é devolvida se houve erro legado/runtime/duplicada/role.
   loadDeployment conserva a validação conjunta e falha inteira, não devolve
   subconjunto nem impõe igualdade nova de opções entre nós distintos.

| Relação | Chave do ConfigurationProblem | Mensagem identifica também |
|---|---|---|
| retry.initial ≤ retry.max | overlay.retry.max.ms | overlay.retry.initial.ms |
| candidate.ttl > announce.interval | routing.candidate.ttl.ms | routing.announce.interval.ms |
| cache.ttl ≥ candidate.ttl | routing.cache.ttl.ms | routing.candidate.ttl.ms |
| renew < lease | subscription.renew.ms | subscription.lease.ms |

ConfigurationProblem(file,key,message) e ConfigurationException(List<...>)
permanecem os tipos atuais. Os testes conferem fonte/chaves/motivo, sem depender
da ordem de Properties ou fixar uma quantidade global de mensagens secundárias
de deployment. Não alterar a cópia defensiva de problems() nem a dos resultados.
Erros nulos ou de argumento de programação mantêm a semântica existente; erros
de propriedades são sempre agregados como ConfigurationException.

As chaves operacionais só são reconhecidas nos perfis de nó: não entram nas
allowlists de topologia/catálogos. Carga lê configuração UTF-8, não resolve DNS,
não abre sockets e não lê bytes de vídeo. Nenhuma fila/cache real é alocada por
receber uma capacidade validada. Falha do parsing não muda o ficheiro de entrada.

## 6. Prova offline: overrides chegam a um consumidor existente

Dentro de ConfigurationLoaderTest, carregar um perfil CLIENT temporário completo
com overrides sentinela: queue.capacity=7, max.connections=13, connect.timeout=1111
ms e shutdown.timeout=2222 ms (usar os nomes completos de Control). Conferir
primeiro os valores no NodeConfiguration.runtime().control(). Depois construir
**apenas o record Transport.Options existente**, passando esses accessors:

```java
var control = loaded.runtime().control();
var consumed = new Transport.Options(
        control.queueCapacity(), control.maxConnections(),
        control.connectTimeout(), control.shutdownTimeout());
assertEquals(7, consumed.queueCapacity());
assertEquals(13, consumed.maxConnections());
assertEquals(Duration.ofMillis(1111), consumed.connectTimeout());
assertEquals(Duration.ofMillis(2222), consumed.shutdownTimeout());
```

O nome Transport.Options é o tipo público real já existente, cujo construtor
valida só números/Duration. O teste não faz new Transport, connect, bind, listener,
sleep ou resolução de endpoints. Não usa Transport.Options.defaults(), pois isso
esconderia um override perdido. Não cria um novo adapter de produção nem faz
config depender de transport: o import ocorre só no teste autorizado.

Esta prova cobre a fronteira carga → runtime validado → valor do consumidor
existente; falha se o loader perder/trocar os quatro overrides. As outras 26
propriedades são verificadas nos accessors corretos nos casos abaixo. O prazo de
consulta é conferido no Control; o seu consumo pelo bootstrap é IMP-003.
Não se alega wiring de OttApplication nem algoritmo de rede: isso é trabalho dos
consumidores futuros. A ausência de sockets é verificável pelo caminho de chamadas
do teste/loader e pelo construtor puro do record, sem SecurityManager/mock novo.

## 7. Casos concretos de V-032, ainda por executar

Todos os casos novos ficam em ConfigurationLoaderTest, com helpers privados,
@TempDir e @ParameterizedTest/@MethodSource já disponíveis no JUnit existente.
D01–D17 são identificadores locais deste desenho, não novos oráculos globais.

| Caso | Preparação e oráculo |
|---|---|
| D01 — defaults reais | loadDeployment dos cinco perfis; comparar todos os 30 accessors com defaults e conservar nós/streams/paths existentes |
| D02 — 30 overrides | Casos por chave com valor válido diferente do default e papel aplicável; assert no accessor exato, grupos restantes preservados |
| D03 — fronteiras | Cada intervalo inteiro/duração no mínimo/máximo, com parceiros coerentes quando existir combinação válida; fronteiras impossíveis falham pela relação, não por relaxar o domínio; percentagens 0 e 100 e alpha positivo representável/1 aceites |
| D04 — léxico/tipos | Vazio/espaço, negativo/sinal, fração de inteiro, unidade, milhares e overflow 2147483648 rejeitados por file/key; alpha vírgula/expoente/hex/NaN/infinito/zero/>1 rejeitados |
| D05 — limites reduzíveis | Abaixo de 1/acima de 64/256/4096/10/8/99999 conforme a chave, sem alterar os caps do wire |
| D06 — papéis | BOOTSTRAP aceita Control e rejeita cada uma das outras 25 chaves; RELAY/CLIENT rejeitam Source; SERVER aceita as 30; proibir chave inaplicável mesmo igual ao default |
| D07 — desconhecidas/duplicadas | Manter testes control.prt/streams.file atuais; novas chaves mal escritas e duplicadas literais/descodificadas geram problemas, sem último valor vencer |
| D08 — quatro relações | Fronteira válida/inválida de cada relação; igualdade válida em retry/cache e inválida em candidate/announce e renew/lease; erro na chave definida |
| D09 — defaults+override | Um override quebra relação com o default do outro campo; vários overrides restauram a validade; não validar cada linha isolada |
| D10 — agregação | Perfil completo com campos runtime inválidos de grupos diferentes e erro legado; observar todas as chaves independentes na mesma ConfigurationException, sem NodeConfiguration parcial |
| D11 — relação sem sentinela | Parsing inválido num operando não fabrica erro cruzado; outra relação válida/invalidada é avaliada independentemente |
| D12 — imutabilidade e duração direta | Records sem coleções mutáveis; null, capacidades/alpha/relações inválidos e Duration fracionário/zero/negativo/enorme rejeitados também por construção direta |
| D13 — construtores compatíveis | Recriar nó válido pelo overload antigo de 13 argumentos; defaults presentes e campos antigos/streams imutáveis preservados; canónico novo mantém overrides e rejeita runtime null |
| D14 — consumidor offline | Quatro sentinelas chegam ao record Transport.Options conforme secção 6; sem instanciar Transport nem abrir rede |
| D15 — deployment/escopo | Nó com runtime inválido faz falhar a carga conjunta inteira; opções diferentes por nó válido são preservadas; chaves runtime em topologia/catálogo continuam rejeitadas |
| D16 — obrigatórias | Omissão de node.id/host/porta/path continua inválida apesar de defaults runtime; diretório temporário fora do cwd não altera semântica |
| D17 — preservação | Manter todos os testes existentes e hashes das fixtures; depois da implementação executar V-032, regressão atual completa e manual adequado com logs novos |

As tabelas de casos devem ter expected values explícitos, sem reutilizar a lógica
do parser para gerar o oráculo. Usar valores distintos onde uma troca de campos
seria mascarada por defaults iguais. Não acrescentar teste artificial de relógio
ou transporte falso: esta carga não mede tempo e o consumidor offline não abre rede.

Domínio escalar não implica que toda a fronteira forme uma configuração completa:
candidate.ttl=1 ms não pode exceder announce.interval≥1 ms; announce.interval no
máximo não admite TTL maior. Do mesmo modo lease=1 ms e renew no máximo são
incompatíveis com renew<lease e os restantes limites. D03/D08 esperam erro de
relação nesses casos, sem aumentar caps ou inventar zero como operando válido.

## 8. Sequência posterior e checkpoint

Depois da revisão deste Design, a etapa ADR confirma reutilização das decisões
existentes ou apresenta divergência material antes de código. Só quando autorizado
em Implement, introduzir interfaces e casos novos com falhas reais por API/casos
ausentes; implementar incrementalmente preservando a regressão existente. Não
fabricar falha em testes antigos. O manual futuro deve carregar overrides válidos,
conferir o consumidor offline e rejeitar perfil inválido; execução ainda pendente.

Neste checkpoint só há documentos/estados/evidência de validação documental.
V-032, regressão atual e teste manual do IMP-002 não foram executados. G-REVIEW-032
continua pendente: designar revisor humano distinto, aceitar a revisão e integrar
antes de DONE. Aprovar Design não satisfaz esse gate nem fecha o incremento.
Parar para revisão do desenho, sem iniciar ADR, Implement ou Test.
