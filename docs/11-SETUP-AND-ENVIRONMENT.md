# Ambiente de desenvolvimento e execução futura

**Classificação:** O = observado por leitura de ficheiros ou disponibilidade do executável; P = comando/procedimento proposto e ainda não validado por execução. Nenhum comando de instalação, build ou teste foi executado para validar a aplicação nesta fase.

## Pré-requisitos

- Java 21 e Maven no host de construção. O POM usa release 21; os executáveis java, mvn e docker estão presentes no host, mas versões/capacidade operacional não foram verificadas.
- CORE como bancada principal. A imagem base declarada é ghcr.io/eivarin/coreemu:9.2.1; disponibilidade e runtime efetivos são por confirmar.
- Docker e Compose, ou instalação CORE nativa equivalente. Utilizador com acesso ao ambiente de emulação.
- JDK/runtime com java.desktop e display X11/Xwayland para cliente visual. DISPLAY configurado não prova acesso; verificar janela real.
- Fontes MJPEG confirmadas, dois vídeos diferentes por servidor, espaço em disco para resultados e captura.
- tcpdump/tshark e ferramentas IP para experiências; parser de PDF/ZIP para entrega, sem impor ferramenta exclusiva.

## Comandos e estado

Os comandos seguintes partem da raiz ou do diretório indicado. São exemplos futuros. Antes de os usar, ler a tarefa responsável.

| Diretório | Comando | Estado e condição |
|---|---|---|
| raiz | java -version; mvn -version; docker --version | P: confirmação de versões em TASK-001/025; presença dos executáveis é O |
| Projeto | mvn clean package | O no README/POM; execução funcional não validada; TASK-001 |
| Projeto | mvn test | P: só valida comportamento depois de testes implementados em TASK-003 em diante |
| Projeto | mvn -Dtest=MessageCodecTest test | P: classe existe vazia, só executável após TASK-004 |
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
5. TASK-025 verifica imagem, runtime, DISPLAY, paths/volumes e cria topologia de quatro terminais; TASK-026/027 criam os outros cenários.
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
