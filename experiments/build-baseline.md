# Baseline de build — TASK-001

Data: 26-10-2026. Resultado: **V-001 executada com sucesso; TASK-001 concluída**. Esta validação cobre configuração e execução do build. **Testes funcionais executados: 0.** Não existe aplicação executável ou comportamento OTT validado.

## Âmbito e estado inicial

Revisão observada antes das alterações: 04d02fef9f4646266de0fca428ca4d680597ba36 (Specs). A árvore de trabalho estava limpa. Não foram encontrados AGENTS.md aplicáveis nos diretórios inspecionados. A raiz Maven continua em [Projeto](../Projeto/pom.xml).

A auditoria atual confirma 29 ficheiros Java em main, dos quais 28 vazios e um apenas com comentário; cinco ficheiros de testes vazios, sete recursos principais vazios e três fixtures vazias. Os hashes dos 44 ficheiros de Projeto/src foram recolhidos antes da alteração e conferidos no fecho. Não foi modificado nenhum desses ficheiros. Não se reorganizou a estrutura: a existente é adequada à tarefa.

## Ambiente verificado

| Elemento | Resultado real |
|---|---|
| Sistema | Linux amd64, kernel 6.8.0-138-generic |
| Java por omissão no host | OpenJDK 25.0.4.1, build 25.0.4.1+1-1-22.04.4-Ubuntu |
| JDK selecionado para Maven | /usr/lib/jvm/java-21-openjdk-amd64 |
| Java desse JDK | OpenJDK 21.0.12.1, build 21.0.12.1+1-1-22.04.4-Ubuntu |
| Maven | Apache Maven 3.6.3, /usr/share/maven |
| Coordenadas preservadas | pt.uminho.esr:ott-streaming:1.0-SNAPSHOT, packaging jar |
| Release de compilação | 21 |
| Encoding do projeto | UTF-8 |

Não foi instalado um JDK nem alterado Java globalmente. A seleção é feita com JAVA_HOME por comando, confirmada também na saída de mvn -version. O ambiente validado é o host; o runtime dentro de CORE continua reservado à TASK-025. Não se iniciou CORE/Docker.

## Alterações e decisões de build

No [POM](../Projeto/pom.xml), JUnit Jupiter foi acrescentado com scope test. O agregador inclui API, testes parametrizados e engine; nenhuma dependência runtime foi acrescentada à aplicação. Surefire está associado à fase test. Preservaram-se o compiler-plugin existente e as coordenadas Maven.

| Artefacto | Versão fixada | Justificação |
|---|---|---|
| org.junit.jupiter:junit-jupiter | 5.13.4 | API/engine Jupiter alinhados; compatibilidade validada por resolução do grafo e build com JDK 21 |
| maven-surefire-plugin | 3.5.4 | Integração com JUnit Platform; compatível com o Maven 3.6.3 disponível |
| maven-compiler-plugin | 3.13.0 | Versão já existente; compilação com release 21 preservada |
| maven-clean-plugin | 3.4.1 | Fixar o comportamento da limpeza em vez de depender da versão implícita do Maven |
| maven-resources-plugin | 3.3.1 | Fixar cópia de recursos/test resources usada pelo build |
| maven-jar-plugin | 3.4.2 | Fixar o empacotamento usado pelo build |

As versões foram escolhidas como base explícita e validada; não se afirma que sejam as versões mais recentes. Plugins não executados nesta tarefa, como install/deploy/site, não foram alterados.

A configuração failIfNoTests=false torna explícita a exceção prevista para TASK-001. **Não desativa a execução de testes existentes**: não se configurou skipTests nem maven.test.skip. A TASK-003 deverá exigir descoberta e execução de testes reais e remover a tolerância a ausência de testes. Não foi criado qualquer teste artificial para demonstrar um sucesso sem comportamento.

A [.gitignore](../.gitignore) passou a ignorar /Projeto/target/, para evitar acrescentar novos artefactos gerados. Os ficheiros de target já versionados foram preservados na indexação; não houve git rm, reset ou alteração da estrutura de controlo de versões.

Referências técnicas consultadas para esta configuração: [JUnit 5.13.4](https://docs.junit.org/5.13.4/user-guide/), [requisitos do Surefire 3.5.4](https://maven.apache.org/surefire-archives/surefire-3.5.4/maven-surefire-plugin/plugin-info.html) e [JUnit Platform com Surefire](https://maven.apache.org/surefire-archives/surefire-3.5.4/maven-surefire-plugin/examples/junit-platform.html). O Maven Dependency Plugin 2.8 foi usado apenas como diagnóstico pontual, sem ser acrescentado ao POM ou às dependências da aplicação.

## Comandos executados e resultados

Diretório dos comandos Maven: Projeto/. Cada comando foi executado separadamente.

| Comando | Saída / interpretação |
|---|---|
| java -version | Código 0; Java 25 por omissão |
| mvn -version | Código 0; Maven 3.6.3 inicialmente sobre Java 25 |
| /usr/lib/jvm/java-21-openjdk-amd64/bin/java -version | Código 0; confirma Java 21 instalado |
| env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -version | Código 0; confirma Maven sobre Java 21 |
| env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -Dstyle.color=never org.apache.maven.plugins:maven-help-plugin:3.5.1:effective-pom -Doutput=/tmp/esr-task001-effective-pom.xml | Código 0, BUILD SUCCESS; 16:07:54 UTC; POM efetivo conferido por parsing XML |
| env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -Dstyle.color=never clean package | Primeira tentativa: código 1 por cache ~/.m2 só de leitura no sandbox; repetição autorizada fora do sandbox: código 0, BUILD SUCCESS, 16:08:21 UTC |
| env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -Dstyle.color=never test | Código 0, BUILD SUCCESS, 16:08:56 UTC; zero classes de teste e zero testes executados |
| env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -Dstyle.color=never org.apache.maven.plugins:maven-dependency-plugin:2.8:tree -Dscope=test | Primeira tentativa impedida de escrever dependências do diagnóstico em ~/.m2; repetição autorizada: código 0, BUILD SUCCESS, 16:09:24 UTC |

O bloqueio da cache foi resolvido por execução com permissões alargadas autorizada para os downloads necessários. Não se alterou o POM para contornar uma falha de permissões. O POM efetivo foi gerado em /tmp, fora de target, para que a limpeza seguinte não o apagasse; o registo persistente dos valores confirmados é este documento.

### POM efetivo conferido

O XML confirmou release=21, junit-jupiter=5.13.4 com scope=test, Surefire=3.5.4 e failIfNoTests=false, bem como as cinco versões de plugins de build listadas acima. Os goals resources, compile, testResources, testCompile, test e jar aparecem nas fases esperadas.

### Excerto real do build limpo

O trecho seguinte é a continuação do log após o arranque e a resolução inicial do clean-plugin:

~~~text
[INFO] 
[INFO] --- maven-clean-plugin:3.4.1:clean (default-clean) @ ott-streaming ---
[INFO] Deleting /home/luis/ESR/ESR---TP2/Projeto/target
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ ott-streaming ---
[INFO] Copying 7 resources from src/main/resources to target/classes
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ ott-streaming ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 29 source files with javac [debug release 21] to target/classes
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ ott-streaming ---
[INFO] Copying 3 resources from src/test/resources to target/test-classes
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ ott-streaming ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 5 source files with javac [debug release 21] to target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:3.5.4:test (default-test) @ ott-streaming ---
[INFO] Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.1/plexus-utils-1.1.jar
[INFO] Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.1/plexus-utils-1.1.jar (169 kB at 1.4 MB/s)
[INFO] 
[INFO] --- maven-jar-plugin:3.4.2:jar (default-jar) @ ott-streaming ---
[INFO] Building jar: /home/luis/ESR/ESR---TP2/Projeto/target/ott-streaming-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.290 s
[INFO] Finished at: 2026-10-26T16:08:21Z
[INFO] ------------------------------------------------------------------------
~~~

### Engine resolvido, sem alegar execução de testes

A árvore de dependências confirmou engine Jupiter 5.13.4 e JUnit Platform 1.13.4, todos com scope test. Foi também inspecionado o ficheiro META-INF/services/org.junit.platform.engine.TestEngine no JAR resolvido: contém org.junit.jupiter.engine.JupiterTestEngine. Isto confirma a configuração e o artefacto do engine; a descoberta/execução de testes reais será validada em V-003.

Excerto do diagnóstico, omitindo apenas linhas de download:

~~~text
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< pt.uminho.esr:ott-streaming >---------------------
[INFO] Building ESR - Serviço OTT de Streaming 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ ott-streaming ---
Warning: truncated output (original token count: 8084)
Total output lines: 228

…4584 tokens truncated… (38 kB at 150 kB/s)
[INFO] pt.uminho.esr:ott-streaming:jar:1.0-SNAPSHOT
[INFO] \- org.junit.jupiter:junit-jupiter:jar:5.13.4:test
[INFO]    +- org.junit.jupiter:junit-jupiter-api:jar:5.13.4:test
[INFO]    |  +- org.opentest4j:opentest4j:jar:1.3.0:test
[INFO]    |  +- org.junit.platform:junit-platform-commons:jar:1.13.4:test
[INFO]    |  \- org.apiguardian:apiguardian-api:jar:1.1.2:test
[INFO]    +- org.junit.jupiter:junit-jupiter-params:jar:5.13.4:test
[INFO]    \- org.junit.jupiter:junit-jupiter-engine:jar:5.13.4:test
[INFO]       \- org.junit.platform:junit-platform-engine:jar:1.13.4:test
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  8.884 s
[INFO] Finished at: 2026-10-26T16:09:24Z
[INFO] ------------------------------------------------------------------------
~~~

### Execução separada da fase test

~~~text
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< pt.uminho.esr:ott-streaming >---------------------
[INFO] Building ESR - Serviço OTT de Streaming 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ ott-streaming ---
[INFO] Copying 7 resources from src/main/resources to target/classes
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ ott-streaming ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 29 source files with javac [debug release 21] to target/classes
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ ott-streaming ---
[INFO] Copying 3 resources from src/test/resources to target/test-classes
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ ott-streaming ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 5 source files with javac [debug release 21] to target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:3.5.4:test (default-test) @ ott-streaming ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.567 s
[INFO] Finished at: 2026-10-26T16:08:56Z
[INFO] ------------------------------------------------------------------------
~~~

O Surefire não imprimiu um resumo de testes e não criou relatórios, porque os cinco ficheiros .java vazios não geram classes de teste. A inspeção posterior confirmou zero .class em target/test-classes e zero testes executáveis. Portanto, o resultado acima não é uma suite de testes aprovada e não demonstra cobertura.

## Artefacto e limites da validação

JAR gerado: Projeto/target/ott-streaming-1.0-SNAPSHOT.jar.

SHA-256 desta execução: eb27ccf94150b4feb9463491feb1c9b4d2916d0287afba15d0fcff9147c79170.

O JAR contém os sete recursos de configuração ainda vazios e metadados Maven. Não contém classes .class, bibliotecas de testes ou Main-Class. O compiler-plugin processou os 29 ficheiros main e os cinco de teste com javac release 21, mas ficheiros vazios/comentário não produzem classes. **Não há comando de arranque funcional do serviço nesta etapa.** O hash identifica esta execução; não se promete reprodução binária do mesmo hash.

Relatórios futuros Surefire: Projeto/target/surefire-reports/TEST-*.xml e ficheiros .txt, depois de existirem testes reais. Nesta execução não há relatórios de casos de teste.

## Repetir no ambiente validado

A partir da raiz, entrar em Projeto/ e selecionar o JDK adequado ao host. Aqui o caminho validado é /usr/lib/jvm/java-21-openjdk-amd64; noutro host ajustar o caminho, mantendo JDK 21.

~~~bash
cd Projeto
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -version
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -Dstyle.color=never clean package
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -Dstyle.color=never test
~~~

É necessário acesso de escrita à cache Maven e, quando faltem artefactos, acesso ao Maven Central. Após a TASK-003, os mesmos comandos deverão descobrir testes reais e a ausência de testes já não deve ser aceite.

## Aceitação e passagem de contexto

- Build base documentado com versões fixadas: cumprido.
- Placeholders não tratados como funcionalidades implementadas: cumprido.
- Comandos reproduzíveis e ausência de cobertura inicial registados: cumprido.
- V-001 executada, com evidência ligada ao progresso: cumprido.
- Bloqueios atuais da TASK-001: nenhum.

RP-006 permanece IN PROGRESS: esta tarefa fornece apenas a base de testes. Validação de entradas, filas, relógios injetáveis e testes de comportamento dependem das tarefas seguintes.

Próxima tarefa recomendada: [TASK-002](../docs/tasks/PHASE-01/TASK-002.md), ainda TODO e não executada. Não foram implementados codecs, bootstrap, transporte, rotas, leitura de vídeo ou GUI.

## Verificação de fecho

git diff --check terminou com código 0. Uma inspeção documental e dos artefactos confirmou 1319 referências relativas válidas, TASK-001 como única tarefa DONE, TASK-002–031 em TODO e RP-006 como único requisito IN PROGRESS. Os hashes dos 44 ficheiros de Projeto/src coincidem com os recolhidos no início. O POM efetivo confirmou as fases dos goals de recursos, compilação, testes e empacotamento. A inspeção do JAR e de target confirmou zero classes de aplicação/testes e ausência de Main-Class. Estas verificações são de configuração, integridade e documentação; não são testes funcionais da aplicação.
