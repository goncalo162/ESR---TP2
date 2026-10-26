# Tecnologias e restrições

O enunciado permite escolher linguagem e transporte. As escolhas abaixo são decisões de implementação deste plano, não imposições da unidade curricular. Não atualizar versões ou acrescentar frameworks por conveniência sem verificar necessidade e impacto nos contratos.

| Tecnologia | Finalidade e razão | Natureza / alternativa |
|---|---|---|
| Java 21 | Linguagem já indicada em Projeto/pom.xml; sockets, concorrência, I/O e GUI na mesma plataforma | Escolha mantida do repositório; Python seria permitido pelo PDF, mas mudaria a base preparada |
| Maven | Compilação, dependências de testes e JAR | Existente; manter compiler-plugin 3.13.0 até haver motivo documentado para alterar |
| JDK completo, módulo java.desktop | Swing e ImageIO na reprodução | Proposto para cliente; runtime headless pode bastar nos servidores, não comprova capacidade de abrir uma janela |
| java.net Socket/ServerSocket | Controlo fiável e full-duplex entre vizinhos | Proposto TCP; fila de escrita única por ligação e framing explícito |
| DatagramSocket/DatagramPacket | Transporte de fragmentos de vídeo, sem bloquear a árvore por uma retransmissão de transporte | Proposto UDP; perdas e reordenação tratadas no cliente, sem ARQ |
| DataInputStream/DataOutputStream, ByteBuffer | Formato binário pequeno e independente de serialização de objetos | Proposto; JSON não é exigido e os JSON existentes estão vazios |
| Properties | Configuração de nós, topologia e catálogo de fontes | Proposto; evita biblioteca de parsing adicional; criar topology.properties em vez de implementar parser JSON próprio |
| Swing/AWT + ImageIO | Janela simples de visualização de JPEG | Proposto; não requer JavaFX, FFmpeg como runtime nem browser |
| JUnit Jupiter 5.13.4 + Surefire 3.5.4 | Testes unitários e de integração local | Configurados em TASK-001 com scope test e engine resolvido; descoberta/execução de testes reais fica para TASK-003 |
| CORE | Emulação IP, topologias, atrasos e falhas | Obrigatório como bancada principal; não substituível por testes localhost |
| Docker/Compose | Reutilizar a bancada preparada | Conveniência existente; CORE nativo também poderia satisfazer o PDF |
| tcpdump/tshark, iproute2 | Captura, contadores e inspeção de endereçamento | Ferramentas propostas; confirmar versões disponíveis na bancada |
| MJPEG e recursos docentes | Vídeo simples, frames JPEG independentes | Estratégia sugerida adotada; confirmar bytes do ficheiro e obter conteúdos distintos |
| LNCS e ferramenta de PDF | Relatório académico | Formato recomendado; limite de 12 páginas é obrigatório |
| ZIP | Empacotar código e instruções | Escolha entre ZIP/RAR admitidos |

Não há base de dados: estado de vizinhança, rotas, catálogo, subscrições e métricas fica em memória. Configuração e fontes de vídeo são ficheiros locais. Logs e capturas são evidências de experiências, não uma fonte persistente de controlo da overlay.

## Fontes técnicas consultadas

Consulta em 26-10-2026 para confirmar capacidades, sem executar exemplos. As decisões do projeto são inferências arquiteturais a partir destas capacidades, não recomendações impostas pelas fontes.

- A API [DatagramSocket, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/DatagramSocket.html) admite datagramas que podem chegar fora de ordem; por isso o contrato de vídeo inclui identificação e reassemblagem.
- A política de threading de [Swing, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/package-summary.html) exige acesso aos componentes no event dispatch thread. Descodificar JPEG e receber rede fora dessa thread.
- [DataInputStream, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/DataInputStream.html) suporta leitura de primitivas e readFully; um único leitor por ligação evita concorrência na mesma stream de bytes.
- A documentação [Surefire e JUnit Platform](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html) confirma a necessidade de um engine de testes; não basta criar ficheiros chamados Test. Versões do build futuro devem ser fixadas e registadas em TASK-001.
- Os [serviços CORE](https://coreemu.github.io/core/services.html) permitem iniciar processos nos nós; o plano prefere início manual para satisfazer RF-001 e facilitar demonstrações.
- A página [do livro, Programming Assignments](https://gaia.cs.umass.edu/kurose_ross/programming.php) referencia exercícios de streaming RTSP/RTP. Isto não demonstra que o nosso protocolo proposto seja RTP.

Na sessão inicial de planeamento, a ferramenta web não conseguiu inspecionar [ProgEx.zip](https://www.di.uminho.pt/~flavio/ESR/ProgEx.zip). A TASK-002 obteve-o por curl com HTTP 200 e confirmou o formato de cinco dígitos ASCII + JPEG de movie.Mjpeg; não havia licença explícita no ZIP. [Revisão V-002](../experiments/task-002-contract-review.md) regista hashes, 500 frames e quatro conteúdos identificados. TASK-020 ainda implementará o leitor e preparará os outros media; TASK-030 verificará créditos/distribuição. Nenhum programa descarregado foi executado.

## Configuração confirmada pela TASK-001

Build validado em 26-10-2026 com Java 21.0.12.1 e Maven 3.6.3. Compiler 3.13.0 preservado; clean 3.4.1, resources 3.3.1, Surefire 3.5.4 e jar 3.4.2 fixados no POM para estabilizar o ciclo clean/package. JUnit Jupiter 5.13.4 inclui engine e API com scope test. Zero testes funcionais executados; não houve teste artificial. Valores efetivos, comandos, saídas e limites em [baseline](../experiments/build-baseline.md).
