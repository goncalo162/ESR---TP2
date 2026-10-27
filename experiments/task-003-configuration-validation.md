# TASK-003 — Verificação das dependências e validação da configuração

Data: 27-10-2026. Resultado: **V-003 executada, 65 testes com sucesso; TASK-003 concluída**. Escopo: configuração de nós, topologia e fontes; nenhum socket, bootstrap de rede, codec, leitura de vídeo, GUI ou CORE implementado/iniciado.

## Verificação prévia de TASK-001 e TASK-002

Revisão de partida: dffb4fc2c45715ef99ebcf7b0a7e948de70b83f4 (Fase 1, Task 1 e 2). Árvore de trabalho inicialmente limpa; foram preservados os artefactos e registos das duas tarefas.

- TASK-001: POM com Java release 21, JUnit Jupiter 5.13.4 e Surefire 3.5.4; ambiente novamente confirmado como OpenJDK 21.0.12.1 e Maven 3.6.3. Antes das alterações desta tarefa, clean package passou novamente às 11:19:00 UTC, ainda com zero testes, de acordo com a exceção da base inicial.
- TASK-002: 45 pares .bin/.hex conferidos com manifesto, tamanhos e SHA-256; 17 tipos TCP presentes e casos UDP revistos por offsets e limites. Os registos de acesso, hashes do ZIP/movie e gates de preparação/licenciamento existem em [V-002](task-002-contract-review.md). Os scripts temporários anteriores já não existiam em /tmp; a conferência foi refeita a partir dos dados persistidos.
- Os critérios das duas tarefas estão cumpridos. Não se concluiu que a aplicação ou o streaming funcionam. B/C/D ainda precisam de preparação na TASK-020, como já registado.

Referências: [baseline V-001](build-baseline.md), [revisão V-002](task-002-contract-review.md), [TASK-003](../docs/tasks/PHASE-01/TASK-003.md).

## Implementação entregue

| Ficheiro/componente | Responsabilidade |
|---|---|
| [ConfigurationLoader](../Projeto/src/main/java/pt/uminho/esr/ott/config/ConfigurationLoader.java) | Parsing UTF-8, validação local/agregada, resolução de paths e erros |
| [NodeConfiguration](../Projeto/src/main/java/pt/uminho/esr/ott/config/NodeConfiguration.java) | Perfil imutável por processo, com contacto bootstrap e configurações dependentes |
| [TopologyConfiguration](../Projeto/src/main/java/pt/uminho/esr/ott/config/TopologyConfiguration.java) | Grafo permitido e vizinhos imutáveis |
| [StreamConfiguration](../Projeto/src/main/java/pt/uminho/esr/ott/config/StreamConfiguration.java) | UUID, origem, título, caminho, FPS e loop da fonte |
| [ConfigurationProblem](../Projeto/src/main/java/pt/uminho/esr/ott/config/ConfigurationProblem.java) / [ConfigurationException](../Projeto/src/main/java/pt/uminho/esr/ott/config/ConfigurationException.java) | Problemas agregados por ficheiro e propriedade, sem devolução de configuração parcial |
| [NodeRole](../Projeto/src/main/java/pt/uminho/esr/ott/overlay/NodeRole.java) | Enum partilhado BOOTSTRAP/SERVER/RELAY/CLIENT, necessário à configuração tipada; sem lógica overlay |
| [ConfigurationLoaderTest](../Projeto/src/test/java/pt/uminho/esr/ott/config/ConfigurationLoaderTest.java) | Testes reais de perfis, invariantes, casos inválidos e integração entre ficheiros |

A estrutura Maven em Projeto/ e as versões fixadas foram preservadas. A única alteração de build é failIfNoTests=true, terminando a tolerância inicial da TASK-001. Não foram criadas dependências runtime.

## Contrato de carga e limites

ConfigurationLoader.load(path, expectedRole) valida um perfil e os ficheiros dependentes de topologia/catálogo. ConfigurationLoader.loadDeployment(paths) valida um conjunto completo de perfis: um BOOTSTRAP, perfis dos nós do grafo, papéis/endpoints/contacto bootstrap coerentes e UUID globalmente únicos. A validação global é uma ferramenta offline para operador/testes; carregar só um servidor não permite comprovar unicidade dos UUID de servidores que não foram fornecidos.

As propriedades são UTF-8 com as regras de escaping/continuação de java.util.Properties. Chaves duplicadas, inclusive após Unicode escaping, desconhecidas ou incompatíveis com o papel são erros. Portas têm de estar explícitas; os defaults propostos no desenho inicial não são preenchidos implicitamente. Booleanos aceitam apenas true/false; FPS 1–60; nodeId ASCII de 1–32 caracteres; título com até 128 pontos Unicode/512 bytes e UTF-8 válido. UUID usa forma canónica de 36 caracteres, sem aceitar as formas abreviadas permitidas pelo parser genérico de Java.

O grafo limita-se a 64 IDs, vizinhos existentes/simétricos sem auto-vizinhança ou duplicação e endpoints únicos por transporte/host/porto. Cada catálogo local permite 2–256 UUID distintos, ligados ao node.id do servidor; rejeita aliases que normalizem para o mesmo caminho. O limite de 256 é uma proteção inicial do protótipo, não um requisito do enunciado.

Paths de topologia, catálogo e media.base resolvem-se relativamente ao ficheiro do nó. stream.UUID.file resolve-se dentro de media.base. Há validação lexical de escape e de paths inválidos; não se exige ainda que os vídeos existam e não se leem os seus bytes. Validação de symlinks, conteúdo real/JPEG e conteúdos distintos é da TASK-020.

Hosts são validados sintaticamente sem resolução DNS; wildcard só é aceite no bind. Endpoints comparam hosts normalizados lexicalmente, sem inferir aliases DNS ou namespaces físicos. A pertença real do IP ao nó, reachability e colisões de bind são verificadas quando se abrir transporte/testar CORE. instanceId será gerado em execução na TASK-008, não durante parsing.

Fontes técnicas consultadas: [Properties do Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Properties.html) para a leitura por Reader e [URI do Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URI.html) para validação sintática de hosts sem consultas à rede.

## Perfis de exemplo

[Configurações](../Projeto/src/main/resources/config/topology.properties) para execução local futura:

| Perfil | ID/papel | Host | TCP | UDP | Conteúdo |
|---|---|---|---:|---:|---|
| bootstrap.properties | B / BOOTSTRAP | 127.0.0.1 | 5002 | não usado | topology.properties |
| server-1.properties | S1 / SERVER | 127.0.0.1 | 5100 | 5101 | streams-s1.properties: A/B |
| server-2.properties | S2 / SERVER | 127.0.0.1 | 5200 | 5201 | streams-s2.properties: C/D |
| relay.properties | R1 / RELAY | 127.0.0.1 | 5300 | 5301 | reenvio futuro |
| client.properties | C1 / CLIENT | 127.0.0.1 | 5400 | 5401 | consumo futuro |

Grafo: S1–S2, S1–R1, S2–R1, R1–C1. Graus desejados 2/2/3/1; há alternativa entre S1/S2 e R1. B é serviço separado, sem participar no grafo de encaminhamento. O grafo é um perfil local de desenvolvimento; não é a validação dos cenários CORE.

Cada origem tem dois UUID conforme docs/07. Os ficheiros a.Mjpeg/b.Mjpeg/c.Mjpeg/d.Mjpeg continuam por preparar; só os metadados foram configurados. O media.base do exemplo aponta para Projeto/media quando o perfil é lido da árvore source. Se copiar os perfis para outra distribuição, ajustar media.base: a resolução é sempre relativa ao local efetivo do ficheiro.

O topology.json vazio permanece como placeholder histórico ignorado. streams.properties passou a ter um comentário de encaminhamento para os dois catálogos por origem; nenhum perfil o carrega. Não se introduziu parser JSON.

## Testes e resultados

Foram descobertos e executados 65 casos pelo JUnit Platform: zero falhas, zero erros e zero skips. Cobrem:

- cinco perfis de exemplo, quatro UUID de streams e associação a duas origens;
- coleções imutáveis, paths fora do cwd e com espaços e ficheiros de vídeo ainda ausentes;
- IDs de um carácter, DNS não resolvido, IPv6 literal, wildcard de bind e rejeição de wildcard publicitado;
- propriedades ausentes, duplicadas/escapadas, desconhecidas e incompatíveis com o papel;
- portos, papéis, IDs, UUID abreviado/inválido/repetido, FPS e booleanos inválidos;
- listas vazias/duplicadas/excessivas, grafos assimétricos, self-link, vizinho inexistente e colisão de endpoint;
- UTF-8 inválido, escape Unicode inválido, título suplementar de 128 pontos e limites excedidos;
- ficheiros de media repetidos, escape de media.base e caminho NUL;
- coerência do conjunto de perfis, bootstrap correto, todos os nós presentes e UUID repetido entre servidores.

### Comandos executados

Diretório dos comandos Maven: Projeto/, com JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 por processo.

| Comando | Resultado |
|---|---|
| java do JDK 21 -version; env JAVA_HOME=... mvn -version | JDK 21.0.12.1 e Maven 3.6.3 confirmados |
| mvn -B -o -Dstyle.color=never clean package, antes de alterar a base | Código 0, verificação da TASK-001, zero testes na configuração anterior |
| mvn -B -o -Dstyle.color=never test, primeira tentativa na raiz do repositório | Código 1 por ausência de POM na raiz; corrigido o workdir para Projeto/ |
| mvn -B -o -Dstyle.color=never test, em Projeto/ | Código 1: provider surefire-junit-platform ainda não descarregado; não é falha de um teste |
| mvn -B -Dstyle.color=never test, com permissão alargada autorizada | Código 0; provider descarregado, primeira suite de 62 casos passou |
| mvn -B -o -Dstyle.color=never clean package, após revisão dos limites | Código 0, 65 casos aprovados e JAR gerado às 11:29:58 UTC |
| mvn -B -o -Dstyle.color=never -f /tmp/esr-task003-empty-project/pom.xml test | Código 1 esperado, No tests to run!, às 11:30:05 UTC; comprova failIfNoTests=true |

O controlo negativo usou uma cópia do POM em /tmp sem fontes/testes, não removeu nem alterou testes do projeto. Não há bloqueios de dependências após a resolução autorizada do provider.

### Excerto real do build final

~~~text
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ ott-streaming ---
[INFO] Copying 95 resources from src/test/resources to target/test-classes
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ ott-streaming ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 6 source files with javac [debug release 21] to target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:3.5.4:test (default-test) @ ott-streaming ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pt.uminho.esr.ott.config.ConfigurationLoaderTest
[INFO] Tests run: 65, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.279 s -- in pt.uminho.esr.ott.config.ConfigurationLoaderTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 65, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- maven-jar-plugin:3.4.2:jar (default-jar) @ ott-streaming ---
[INFO] Building jar: /home/luis/ESR/ESR---TP2/Projeto/target/ott-streaming-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.146 s
[INFO] Finished at: 2026-10-27T11:29:58Z
[INFO] ------------------------------------------------------------------------
~~~

### Controlo negativo de ausência de testes

~~~text
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< pt.uminho.esr:ott-streaming >---------------------
[INFO] Building ESR - Serviço OTT de Streaming 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ ott-streaming ---
[INFO] skip non existing resourceDirectory /tmp/esr-task003-empty-project/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ ott-streaming ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ ott-streaming ---
[INFO] skip non existing resourceDirectory /tmp/esr-task003-empty-project/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ ott-streaming ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:3.5.4:test (default-test) @ ott-streaming ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.379 s
[INFO] Finished at: 2026-10-27T11:30:05Z
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.5.4:test (default-test) on project ott-streaming: No tests to run! -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
~~~

Relatórios: Projeto/target/surefire-reports/TEST-pt.uminho.esr.ott.config.ConfigurationLoaderTest.xml e ficheiro .txt correspondente. Estes testes validam comportamento de configuração; não validam ainda transporte, overlay ou vídeo.

## Aceitação e contexto seguinte

Configuração válida carrega sem rede; erros indicam ficheiro/propriedade sem estado parcial; dois servidores com dois UUID distintos estão configurados; V-003 executada com evidência. TASK-003 pode ficar DONE, enquanto a fase continua IN PROGRESS. RF-001/004/012/013 e RP-001/006 ficam apenas IN PROGRESS, com os restantes componentes/testes ainda pendentes. RNF-005 conserva o progresso documental da TASK-002.

Próxima tarefa: [TASK-004](../docs/tasks/PHASE-01/TASK-004.md), ainda TODO e não iniciada. Nenhuma alteração do protocolo/binários da TASK-002, do código CORE ou das evidências históricas foi necessária.

## Verificação de fecho

O XML Surefire confirma 65 testes, zero failures/errors/skips. Os 45 pares de protocolo conservam os hashes originais. Dos 153 ficheiros protegidos no início, apenas os nove ficheiros de código/configuração existentes previstos pela tarefa foram alterados; os restantes, incluindo CORE, fixtures e evidências anteriores, foram preservados. Verificaram-se 1447 referências relativas sem ligações/âncoras quebradas e git diff --check terminou com sucesso. Os estados são TASK-001/002/003 DONE, TASK-004–031 TODO e requisitos relacionados apenas IN PROGRESS.

O JAR gerado contém 11 classes com major version 65 (Java 21), sem bibliotecas de teste e sem Main-Class. Os ficheiros de recursos/manifests de target já versionados no repositório foram regenerados pelo build; não foram editados manualmente nem removidos da indexação. Novos outputs continuam ignorados por .gitignore. A limpeza de artefactos históricos versionados não faz parte desta tarefa.
