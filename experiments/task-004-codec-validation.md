# TASK-004 — Codec de controlo v1 e fecho da fase 1

> Atualização posterior: esta evidência descreve o fecho original da TASK-004, com 17 tipos. Por instrução explícita do utilizador (ADR-013), o contrato atual tem 16 tipos ativos, código 3 retirado e REGISTER_OK sem lease, apenas com vizinhos configurados/instâncias desconhecidas. A [correção e nova validação](bootstrap-discovery-correction.md) substituem os detalhes antigos afetados, preservando estes resultados como histórico.

Data: 27-10-2026. Resultado: **V-004 executada e PHASE-01 concluída no seu âmbito**. Build limpo com Java 21/Maven: 131 testes, zero falhas, erros ou skips; 65 de configuração e 66 de codec/integração local.

## Partida e limites

Revisão de partida: b67a1a719369746d7ee9a572b5ceda577f2368a8 (add: Task 003 da fase 1); árvore de trabalho inicialmente limpa. Dependências TASK-002 e TASK-003 em DONE com evidências e fontes presentes. O snapshot /tmp/esr-task004-before.json registou 162 ficheiros existentes de fontes, CORE e experiências para a conferência de fecho.

A tarefa implementa serialização/descodificação e validação de formato. Não abre sockets, regista nós, atualiza rotas ou reproduz vídeo. As mensagens existem como objetos e bytes; os handlers e transporte serão implementados depois. Os XML existentes em CORE/topologies permanecem a base de integração, conforme ADR-011; nenhum foi alterado.

## Código implementado

| Componente | Resultado |
|---|---|
| [MessageType](../Projeto/src/main/java/pt/uminho/esr/ott/protocol/MessageType.java) | Tipos 1–17 explícitos, sem serializar ordinal de enum |
| [ControlMessage](../Projeto/src/main/java/pt/uminho/esr/ott/protocol/ControlMessage.java) | Envelope, payloads tipados/imutáveis e modelos de Endpoint/StreamInfo no fio |
| [MessageCodec](../Projeto/src/main/java/pt/uminho/esr/ott/protocol/MessageCodec.java) | encode/decode de um frame; read/write de frames consecutivos através de InputStream/OutputStream |
| [ErrorCode](../Projeto/src/main/java/pt/uminho/esr/ott/protocol/ErrorCode.java) | Códigos explícitos 1–12 usados no formato e na classificação de falhas |
| [ProtocolException](../Projeto/src/main/java/pt/uminho/esr/ott/protocol/ProtocolException.java) | IOException controlada com errorCode; não envia ERROR nem fecha conexões |
| [MessageCodecTest](../Projeto/src/test/java/pt/uminho/esr/ott/protocol/MessageCodecTest.java) | Fixtures independentes, limites, truncatura, concatenação e integração configuração→mensagem→bytes |

Payloads comuns são partilhados por tipos com layout igual (REGISTER/HELLO, PING/PONG e ACTIVATE/RENEW). Listas são copiadas defensivamente. O StreamInfo aninhado no modelo é apenas o contrato de rede; o catálogo dinâmico e os serviços de streaming continuam por implementar.

## Contrato executável

- Corpo TCP v1 entre 37 e 65536 bytes, prefixo u32 big-endian excluído do comprimento. Prefixo é interpretado como unsigned, incluindo valores acima de Integer.MAX_VALUE.
- Comprimento validado antes de alocar o corpo. Strings/listas têm os seus máximos verificados antes da alocação correspondente; saída também passa por um buffer limitado.
- EOF antes do prefixo devolve Optional.empty em read. EOF a meio do prefixo/corpo lança EOFException. Campo incompleto dentro de um corpo já recebido é ProtocolException INVALID_MESSAGE.
- decode(byte[]) exige exatamente um frame; read(InputStream) permite processar vários sem consumir o seguinte. Não se usa available para decidir se a mensagem está completa.
- UTF-8 normal/estrito, sem readUTF/writeUTF; título suplementar 🎬 distingue UTF-8 de modified UTF-8. UUID preserva os 128 bits; sequências/custos i64 são não negativos. Leases u32 são representadas em long, 0..4294967295.
- Endpoint tem papéis explícitos SERVER=1, RELAY=2, CLIENT=3 e portas 1..65535; UUID de instância desconhecida só no Endpoint de REGISTER_OK. ERROR.relatedType aceita qualquer u8 para descrever tipo desconhecido.
- Validade de percurso, autorização, UUID de sessão fresco e correspondência com a conexão são verificações dos handlers futuros. O codec não calcula caminhos ou decide se uma subscrição/lease é operacionalmente válida.
- O proprietário mantém responsabilidade por flush, close e único escritor por conexão. Uma mensagem inválida é rejeitada antes de escrever bytes no OutputStream; uma falha de I/O do destino continua a ser responsabilidade do transporte.

## Testes executados

### Fixtures independentes

Todas as 29 fixtures TCP da TASK-002 foram consumidas: 18 válidas (17 tipos e variante UTF-8) e 11 malformadas. Para cada válida, compararam-se os bytes do encoder com o .bin já existente, os campos do decoder com o objeto esperado construído do contrato e o round-trip. Todas as 45 fixtures TCP/UDP conservaram os hashes; não se regenerou expected output com o codec. Não se implementou codec UDP.

### Fronteiras e erros

Testes adicionais cobrem leitura de um byte por vez com available=0, concatenação dos 18 frames, EOF em todas as truncaturas do HELLO, prefixos 65537/2147483648/4294967295 rejeitados sem leitura do corpo, string declarada excessiva e campo incompleto. Cobrem também leases u32 nos extremos, UUID nil/com bits de sinal, Long.MAX_VALUE, limites Unicode, 64/65 vizinhos, 32/33 IDs de caminho, endpoints, enums, payload incompatível e ausência de saída parcial por modelo inválido.

### Integração local da fase

Um teste carrega os cinco perfis reais de configuração e usa os dados para construir e recuperar quatro HELLO, quatro anúncios dos fluxos A/B/C/D e uma resposta bootstrap. As instâncias usadas são identificadores de teste, sem runtime. O caminho completo configuração→modelo de mensagem→frame→modelo foi validado em memória, sem sockets ou emulador.

### Comandos e resultados

Diretório Maven: Projeto/. JDK selecionado por JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64; Java 21.0.12.1 e Maven 3.6.3. Nenhuma dependência ou versão do POM foi alterada nesta tarefa.

1. mvn -B -o -Dstyle.color=never test: primeira execução de codec compilado ainda correu apenas os 65 testes de configuração, pois uma tentativa de escrita do ficheiro de teste usou path relativo incompatível com o workdir; não foi tratada como conclusão V-004.
2. Após corrigir o destino para o path absoluto, o mesmo comando passou 130 testes (65+65).
3. Acrescentado o teste de integração local da fase, mvn -B -o -Dstyle.color=never clean package passou 131 testes (65+66) e gerou o JAR às 12:04:46 UTC.

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
[INFO] Tests run: 65, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.270 s -- in pt.uminho.esr.ott.config.ConfigurationLoaderTest
[INFO] Running pt.uminho.esr.ott.protocol.MessageCodecTest
[INFO] Tests run: 66, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.124 s -- in pt.uminho.esr.ott.protocol.MessageCodecTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 131, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- maven-jar-plugin:3.4.2:jar (default-jar) @ ott-streaming ---
[INFO] Building jar: /home/luis/ESR/ESR---TP2/Projeto/target/ott-streaming-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.323 s
[INFO] Finished at: 2026-10-27T12:04:46Z
[INFO] ------------------------------------------------------------------------
~~~

Relatórios reais em Projeto/target/surefire-reports/TEST-pt.uminho.esr.ott.config.ConfigurationLoaderTest.xml e TEST-pt.uminho.esr.ott.protocol.MessageCodecTest.xml. O controlo failIfNoTests=true da TASK-003 continua ativo.

## Revisão de conclusão da fase

| Critério da fase | Evidência |
|---|---|
| Quatro tarefas concluídas | V-001 baseline, V-002 contratos/recursos, V-003 configuração e V-004 codec |
| Build e ferramentas de testes | Java 21/Maven, versões fixadas e build limpo com Surefire/JUnit efetivo |
| Configurações válidas/invalidas | 65 casos de ConfigurationLoaderTest preservados e aprovados |
| Codec, leituras parciais e limites | 66 casos de MessageCodecTest, incluindo bytes independentes e concatenação |
| Integração coerente | Perfis de configuração produzem mensagens/frames recuperáveis em memória |
| Sem regressão sobre base anterior | Suite de configuração continua verde, fixtures e CORE preservados |
| Documentação/rastreabilidade | Estados, contratos/API e evidências atualizados; requisitos globais permanecem parciais |

PHASE-01 pode ficar DONE por concluir a sua base técnica. Isto não conclui a Etapa 0 inteira do enunciado: o cliente/servidor de rede simples ainda pertence ao transporte da próxima fase. Não conclui os itens de avaliação de overlay, gestão de fluxos ou streaming.

## Relatório e passagem de contexto

Material do relatório está discriminado em [PHASE-01-REPORT-NOTES](../docs/PHASE-01-REPORT-NOTES.md), com referências às páginas 3–5 do enunciado e distinção entre formato de mensagem implementado, interações propostas e testes locais reais. Não foi produzido o artigo/PDF final de até 12 páginas.

Próxima tarefa recomendada: [TASK-005](../docs/tasks/PHASE-02/TASK-005.md), transporte TCP full-duplex. PHASE-02 e TASK-005 continuam TODO, sem execução automática nesta sessão. Requisitos transversais continuam IN PROGRESS; nenhum foi marcado totalmente cumprido por concluir a fase.

## Verificação final dos artefactos

Os dois XML Surefire confirmam 65+66 testes, sem falhas/erros/skips. A conferência dos 162 ficheiros de partida identificou alterações apenas nos quatro placeholders de protocolo/testes previstos; configurações, POM, CORE, fixtures e evidências históricas foram preservados. As duas novas classes de erros completam o modelo. Verificaram-se 1478 referências relativas sem ligações/âncoras quebradas, estrutura/estados das 31 tarefas e estados parciais dos requisitos. git diff --check terminou com sucesso.

O JAR final contém 34 classes com major version 65 (Java 21), sem Main-Class; não há ainda aplicação de nó executável. Os manifests de compilação em target já versionados foram regenerados pelo Maven, sem edição manual ou alteração da indexação. APIs de referência consultadas: [DataInputStream Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/DataInputStream.html) e [CharsetDecoder Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/charset/CharsetDecoder.html).
