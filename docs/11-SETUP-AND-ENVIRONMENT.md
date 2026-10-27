# Ambiente de desenvolvimento e execução futura

**Classificação:** O = observado por leitura ou disponibilidade do executável; P = proposto e ainda não validado; V = executado e verificado no ambiente indicado. TASK-001 validou a base Maven no host; TASK-003 acrescentou 65 testes reais de configuração e build limpo com sucesso. [V-001](../experiments/build-baseline.md), [V-003](../experiments/task-003-configuration-validation.md). Comandos de CORE, GUI e aplicação continuam propostos.

## Pré-requisitos

- Java 21 e Maven no host de construção. O POM usa release 21. TASK-001 confirmou Maven 3.6.3 e JDK 21.0.12.1 selecionado por JAVA_HOME; Java 25 é o default do host. Docker está presente, mas não foi executado ou validado.
- CORE como bancada principal. A imagem base declarada é ghcr.io/eivarin/coreemu:9.2.1; disponibilidade e runtime efetivos são por confirmar.
- Docker e Compose, ou instalação CORE nativa equivalente. Utilizador com acesso ao ambiente de emulação.
- JDK/runtime com java.desktop e display X11/Xwayland para cliente visual. DISPLAY configurado não prova acesso; verificar janela real.
- Fontes MJPEG confirmadas, dois vídeos diferentes por servidor, espaço em disco para resultados e captura.
- tcpdump/tshark e ferramentas IP para experiências; parser de PDF/ZIP para entrega, sem impor ferramenta exclusiva.

## Comandos e estado

Os comandos seguintes partem da raiz ou do diretório indicado. São exemplos futuros. Antes de os usar, ler a tarefa responsável.

| Diretório | Comando | Estado e condição |
|---|---|---|
| raiz | java -version; mvn -version; docker --version | V: Java/Maven verificados em TASK-001; docker --version não executado, continua P para TASK-025 |
| Projeto | mvn clean package | V em TASK-001 e TASK-003 com JAVA_HOME do JDK 21; build final inclui classes de configuração e 65 testes aprovados |
| Projeto | mvn test | V em TASK-001 (zero testes na base inicial) e TASK-003 (65 testes de configuração aprovados) com JDK 21 |
| Projeto | mvn -Dtest=MessageCodecTest test | Classe implementada na TASK-004; suite executada pelo build completo com JDK 21 (66 casos de codec/integração) |
| Projeto | mvn verify | P: depois de TASK-024 deve executar testes unitários/integração com convenção *Test; sem depender de profile inexistente |
| CORE | bash setup.sh | O como script de build; P em execução; usa Docker e pode requerer rede; TASK-025 |
| CORE | ./core-gui | O como launcher; P em execução nesta sessão; inicia container e GUI |
| raiz | docker exec core java -version | P: só com container ativo; verificar JDK real |
| raiz | docker exec core ls /volume/ott | P: só após material copiado e container ativo |
| Projeto | java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role BOOTSTRAP --config src/main/resources/config/bootstrap.properties | O no README como intenção; CLI ainda vazia; P só após TASK-008 |
| Projeto | java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role SERVER --config src/main/resources/config/server-1.properties | P depois de TASK-024; repetir com server-2.properties para segunda origem |
| Projeto | java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role RELAY --config src/main/resources/config/relay.properties | P após TASK-008, configuração por processo |
| Projeto | java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role CLIENT --config src/main/resources/config/client.properties | P após TASK-024 para catálogo/play e GUI |

No CORE o caminho proposto do JAR é /volume/ott/ott-streaming-1.0-SNAPSHOT.jar e as configurações em /volume/ott/config/. Executar o comando Java **dentro do terminal/namespace do nó CORE**, não apenas num shell genérico do container. Todos os nós emulados podem ver o mesmo volume; cada processo recebe configuração própria. Mudar o cwd não corrige IP publicitado ou portas em conflito.

## Preparação incremental

1. TASK-001 valida JDK 21 e fixa testes; TASK-003 preenche propriedades. Não confiar em target/ antigo.
2. TASK-008 permite iniciar nós reais. Em localhost atribuir control/data ports diferentes a cada processo; em CORE podem repetir os portos em namespaces IP distintos.
3. TASK-020 confirma formato/distribuição dos vídeos e perfis. Não descarregar e executar exemplos automaticamente como instalação.
4. TASK-024 valida ponta a ponta localmente antes da emulação.
5. TASK-025 verifica imagem/runtime/DISPLAY/volumes e reutiliza CORE/topologies/cenario1.xml; TASK-026/027 usam cenario2.xml e cenario3.xml. Mapear processos e criar configurações com IPs reais. Não substituir os XML por novas topologias sem necessidade demonstrada.
6. Abrir topologia pela GUI, iniciar a sessão, confirmar interfaces e rotas IP, preencher a tabela nodeId→host CORE→IP→portos, e só depois iniciar processos manualmente.
7. Guardar manifestos e usar exatamente a configuração verificada nos resultados.

## Configuração do ambiente

JAVA_HOME deve apontar ao JDK 21 instalado; não reutilizar o caminho amd64 do Dockerfile num host arm64 sem verificação. DISPLAY pertence ao ambiente gráfico e requer autorização X11 adequada. Não existem segredos ou variáveis de ambiente obrigatórios para a aplicação. Os nomes e valores dos ficheiros de propriedades estão em [07](07-API-AND-INTERFACES.md).

O Dockerfile inclui pacote openjdk-21-jdk-headless; a futura preparação deve verificar se inclui os módulos e bibliotecas nativas necessários ao cliente visual e instalar o runtime completo se faltar suporte. Não afirmar que imagem constrói ou que Java GUI funciona apenas por ler packages.txt.

## Problemas comuns

| Sintoma | Diagnóstico e ação futura |
|---|---|
| mvn package passa com placeholders | Não prova funcionalidade; exigir testes descobertos e validação por tarefa |
| UnsupportedClassVersionError | Verificar JDK de execução, inclusive dentro do namespace/container |
| ClassNotFoundException | Confirmar build e JAR atualizado; main só passa a existir em TASK-008 |
| Bootstrap responde, vizinhos não ligam | Verificar allowlist simétrica, iniciador por ID, endpoint publicitado, portas e reachability IP |
| Vídeo não aparece | Confirmar ACK, stream/epoch/upstream, UDP, contadores de montagem e suporte GUI; separar pipeline e janela |
| Rota não muda após corte | Confirmar se underlay recuperou mantendo a mesma aresta overlay; cortar processo ou canal correto |
| No display / HeadlessException | Validar java.desktop, runtime completo, DISPLAY e autorização, sem permissões globais |
| Dependências/imagem não descarregam | Registar erro de rede/registry; pedir acesso conforme ambiente, sem mudar tecnologia obrigatória |
| CORE não vê vídeos/JAR | Verificar volume CORE/volume → /volume e paths nos ficheiros por nó |

Os resultados dos comandos futuros devem substituir P por evidência específica, data, ambiente e tarefa, sem reescrever a história desta fase como se os comandos já tivessem sido executados.

## Seleção de JDK verificada na TASK-001

No host desta execução, Maven por omissão usa Java 25. Para manter a base definida, usar JDK 21 apenas no processo Maven:

~~~bash
cd Projeto
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -version
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -Dstyle.color=never clean package
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -Dstyle.color=never test
~~~

Os três comandos acima foram verificados com sucesso. O caminho do JDK é específico deste host; adaptar noutros ambientes mantendo Java 21. Requer cache Maven gravável e rede quando faltarem dependências. O POM usava failIfNoTests=false só para a base inicial; desde TASK-003 usa true e a ausência de testes faz falhar o build. Relatórios futuros: Projeto/target/surefire-reports/; nesta etapa não foram produzidos relatórios de casos de teste. Ver [baseline](../experiments/build-baseline.md) para resultados e falhas de permissões resolvidas.

## Recursos identificados na TASK-002

O ZIP docente esteve acessível e o perfil de movie.Mjpeg foi confirmado; [V-002](../experiments/task-002-contract-review.md) contém URL, hashes, formato e inspeção. As cópias do ZIP são o mesmo vídeo, não três streams distintas. Foram identificados A/B para S1 e C/D para S2, mas B/C/D ainda precisam de descarga/preparação na TASK-020. As fixtures binárias em Projeto/src/test/resources/protocol são dados sintéticos de contrato e não vídeos reproduzíveis. Nenhum comando de streaming, GUI ou CORE passou a validado por esta inspeção.

## Configuração e testes disponíveis após TASK-003

Os perfis em Projeto/src/main/resources/config já contêm propriedades válidas, grafo simétrico e dois catálogos por origem; JSON vazio e streams.properties histórico não são carregados. A API de carga está em [07](07-API-AND-INTERFACES.md). Não existe ainda main funcional para iniciar um nó; não executar os comandos de aplicação antes de TASK-008. Os quatro paths de media são metadados: preparação/validação dos ficheiros continua em TASK-020.

O comando Maven de testes com JDK 21 executou 65 casos e produz TEST-pt.uminho.esr.ott.config.ConfigurationLoaderTest.xml em Projeto/target/surefire-reports/. Esses resultados substituem a ausência de relatórios da base inicial; o baseline antigo continua como histórico. A primeira execução com testes reais precisou de descarregar o provider Surefire JUnit Platform com permissão autorizada; depois o build limpo passou em modo offline.

## Fecho da fase 1 — TASK-004

O comando com JDK 21, a partir de Projeto/, passou offline: env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -o -Dstyle.color=never clean package. Resultado: 131 testes, zero falhas/erros/skips. Relatórios de ConfigurationLoaderTest e MessageCodecTest em target/surefire-reports/. O JAR contém a configuração e o codec; OttApplication ainda não tem main funcional. Os comandos de processo/CORE continuam futuros. [V-004](../experiments/task-004-codec-validation.md).
