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
| JUnit Jupiter + Surefire | Testes unitários e de integração local | Proposto; TASK-001 fixa versões concretas compatíveis com JDK 21 e engine Jupiter e verifica descoberta real dos testes |
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

O endereço [ProgEx.zip do docente](https://www.di.uminho.pt/~flavio/ESR/ProgEx.zip), referido na p. 5 do PDF, não pôde ser inspecionado pela ferramenta web nesta sessão. Não se conhece ainda a estrutura interna do ZIP, licença ou codificação exata de movie.Mjpeg. TASK-020 confirma esses pontos antes de adaptar o leitor. Nenhum recurso foi descarregado ou instalado neste planeamento.
