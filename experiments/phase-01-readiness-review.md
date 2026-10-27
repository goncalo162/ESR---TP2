# Revisão da fase 1 e condições de entrada na fase 2

> Atualização posterior: a revisão abaixo antecede a instrução explícita de limitar o bootstrap à descoberta inicial. ADR-013 e a [correção revalidada](bootstrap-discovery-correction.md) substituem apenas os detalhes de registo/renovação e o respetivo formato; a conclusão de prontidão mantém-se. Esta nota preserva os resultados observados na revisão original.

Data: 27-10-2026. Revisão analisada: `4105429a76f67a5acf0579db8719b0ba2460b549`. O repositório estava limpo no início. Âmbito: verificar a implementação e evidência de TASK-001–004 e as dependências de PHASE-02; não executar TASK-005 nem iniciar CORE.

## Conclusão

**PHASE-01 pode manter-se DONE e TASK-005 pode começar.** Não foi identificado bloqueio técnico ou decisão humana obrigatória em falta para iniciar o transporte TCP. PHASE-02 e TASK-005–009 permanecem TODO. Esta revisão não dá como implementadas a comunicação entre processos, a descoberta de vizinhos ou as restantes funcionalidades da overlay.

## O que existe e foi revisto

| Tarefa | Implementação/artefacto confirmado | Evidência |
|---|---|---|
| TASK-001 | POM com release 21, JUnit Jupiter 5.13.4, Surefire 3.5.4 e plugins do build fixados; atualmente failIfNoTests=true | [Baseline](build-baseline.md), POM efetivo e build repetidos nesta revisão |
| TASK-002 | 45 pares .bin/.hex e manifesto, incluindo 29 casos TCP e 16 UDP; revisão do recurso docente e hipóteses identificadas | [Revisão dos contratos](task-002-contract-review.md); integridade das fixtures reconfirmada |
| TASK-003 | Loader UTF-8, configuração imutável, erros agregados, grafo/endpoints/catálogos e validação conjunta dos cinco perfis locais | [V-003](task-003-configuration-validation.md); 65 testes repetidos |
| TASK-004 | Codec de controlo v1, 17 tipos explícitos, payloads imutáveis, limites, UTF-8 estrito, framing e distinção entre EOF limpo/truncatura/corpo inválido | [V-004](task-004-codec-validation.md); 66 testes repetidos |

Inspecionados o código de configuração e codec, os testes, as quatro tarefas e os contratos utilizados pela [TASK-005](../docs/tasks/PHASE-02/TASK-005.md). Os testes incluem bytes de referência independentes do encoder, leituras parciais, mensagens consecutivas, limites e integração configuração/codec em memória. O ficheiro BootstrapServer.java contém apenas um comentário, não um servidor implementado. Os restantes componentes futuros não são contabilizados como funcionalidades.

## Comandos e resultados desta revisão

Diretório dos comandos Maven: Projeto/.

```bash
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -version
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -o -Dstyle.color=never clean package
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -o -Dstyle.color=never org.apache.maven.plugins:maven-help-plugin:3.5.1:effective-pom -Doutput=/tmp/esr-phase01-readiness-effective-pom.xml
```

- Maven 3.6.3, executado com Java 21.0.12.1, confirmados.
- Build limpo: código 0, BUILD SUCCESS, terminado às 14:12:33 UTC; 131 testes, zero falhas, erros ou ignorados. ConfigurationLoaderTest: 65; MessageCodecTest: 66.
- POM efetivo: código 0, BUILD SUCCESS, terminado às 14:12:56 UTC. Parsing XML confirmou release 21, Jupiter 5.13.4, Surefire 3.5.4 e failIfNoTests=true.
- Inspeção do JAR por ZIP/cabeçalhos .class: 34 classes, todas major version 65 (Java 21), sem Main-Class. O JAR ainda não é uma aplicação de nós executável.
- Verificação temporária com Python: 45 pares binário/hex coincidem, comprimentos e SHA-256 coincidem com o manifesto; 1379 referências relativas a ficheiros em docs/ e experiments/ válidas antes da criação desta nota.
- Parsing dos três XML cenario1/2/3 concluído. Isto verifica sintaxe XML; não comprova CORE, conectividade, IP routing ou tráfego.
- Estados revistos: TASK-001–004 DONE, sem checklists pendentes; TASK-005–009 TODO. Rastreabilidade mantém requisitos transversais parcialmente implementados em IN PROGRESS.
- git diff --check e git diff --exit-code -- Projeto/src Projeto/pom.xml CORE terminaram com código 0 após o build, antes da edição desta nota. Nenhuma alteração ao código de produção, POM, fixtures ou CORE nesta revisão.

Relatórios da execução: Projeto/target/surefire-reports/TEST-pt.uminho.esr.ott.config.ConfigurationLoaderTest.xml e TEST-pt.uminho.esr.ott.protocol.MessageCodecTest.xml. Estes artefactos são regenerados por builds seguintes; os resultados observados ficam registados acima.

## Decisões necessárias ao arranque já documentadas

Consultar [riscos e ADR](../docs/12-RISKS-AND-DECISIONS.md): Java 21/Maven (ADR-001); TCP persistente full-duplex para controlo e UDP para vídeo (ADR-002); bootstrap com grafo permitido (ADR-003); propriedades/binário (ADR-007/010); reutilização dos XML CORE existentes (ADR-011); codec independente do transporte (ADR-012).

A TASK-005 já define um leitor e um escritor por ligação, fila limitada, eventos, identidade de conexão e encerramento. O contrato em [07](../docs/07-API-AND-INTERFACES.md) propõe timeout de conexão de 2 s e fila de saída de 256 mensagens, com fecho da conexão saturada. As escolhas de API e sincronização restantes pertencem à implementação dessa tarefa, com testes de recursos e tráfego full-duplex; não exigem nova decisão do utilizador para começar.

O codec não faz flush/close nem serializa escritores concorrentes: a camada de transporte terá de assumir essas responsabilidades. A leitura espera pelo frame completo; a estratégia de timeouts/encerramento do transporte tem de lidar com peers bloqueados ou frames incompletos. Autorização, identidade de HELLO e correspondência com a configuração pertencem aos handlers da fase 2. A ausência destes comportamentos no codec não constitui um bloqueio da fase 1.

## Pendências e momento de resolução

| Pendência | Momento/impacto | Bloqueia iniciar TASK-005? |
|---|---|---|
| H-01/H-10: áudio/texto e compatibilidade RTP/RTSP não confirmados formalmente pelo docente | Mantém-se a resposta do utilizador sem indicações adicionais conhecidas e as hipóteses explícitas; rever se surgirem novas instruções | Não |
| Datas/conteúdo das avaliações intermédias | TASK-009/018; registar situação real e alinhar a demonstração com instruções recebidas | Não |
| Preparar quatro conteúdos finais e verificar redistribuição | TASK-020/030; os nomes nos perfis não provam existência dos vídeos | Não |
| Mapear perfis/IP/papéis nos XML CORE, testar JDK, rede e GUI no ambiente emulado | TASK-025–027; loopback não substitui a aceitação em CORE | Não |
| Limiares de RTT, buffers, prazos e desempenho | Valores iniciais propostos em 07; testar nas tarefas responsáveis e avaliar em TASK-028 | Não |
| Turno, grupo, prazo e submissão | TASK-030/031; não inventar dados de entrega | Não |
| Diagnóstico de percursos do plano inicia no cliente, enquanto a sugestão da Etapa 3 inicia no servidor | Rever a adequação da demonstração em TASK-013; não confundir sondas de RTT com teste de percurso completo | Não |

Não foram feitos pedidos ao docente, testes de sockets, experiências CORE, reprodução ou novos testes artificiais. A fase 1 do plano é uma fundação local e não significa conclusão das Etapas 0/1 do enunciado.

## Próxima ação

Quando pedida a implementação, executar exclusivamente [TASK-005](../docs/tasks/PHASE-02/TASK-005.md), começando por atualizar o seu estado e o [progresso](../docs/15-PROJECT-PROGRESS.md). Não antecipar bootstrap, handshake de vizinhança, rotas ou streaming.
