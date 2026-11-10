# Primeiro marco da overlay — IMP-006 / V-009

O roteiro usa quatro JVMs locais do IMP-005. BOOT fornece o grafo triangular
de N0 (SERVER), N1 (RELAY) e N2 (CLIENT). A sequência evidencia descoberta,
handshake, reconexão direta e saída voluntária. As portas TCP são 56100,
56110, 56120 e 56130. Os perfis reservam ainda as portas UDP 56111, 56121 e 56131.

**Estado da primeira avaliação (G-ASSESSMENTS)** — data e conteúdo pendentes
de informação docente. O roteiro refere-se à primeira demonstração técnica.
[ADD](../../add.md#imp-006) · [TASK-009](../../docs/tasks/PHASE-02/TASK-009.md).

## Preparar a execução

Usar Linux com suporte a pidfd, Bash, Python 3.9 ou posterior, JDK 21, `ss`,
`tcpdump` e um leitor que permita
exportar fluxos TCP reconstituídos (por exemplo Wireshark). É preciso acesso a
captura no loopback e ao corte de sockets da própria experiência. As ferramentas
são usadas na bancada. A aplicação corre com a conta normal.

Da raiz do repositório, identificar o operador e o binário Java 21 da bancada.

```bash
export M="$PWD/experiments/milestone-1"
export JAR="$PWD/experiments/construction/020-overlay/verified-jar/ott-streaming-1.0-SNAPSHOT.jar"
export JAVA=/usr/lib/jvm/java-21-openjdk-amd64/bin/java
export RUN="$(python3 "$M/bench.py" init --operator 'NOME DO OPERADOR' --java "$JAVA")"
"$JAVA" --class-path "$JAR" "$M/Inspect.java" profiles "$RUN/profiles" | tee "$RUN/profiles.log"
python3 "$M/bench.py" ports "$RUN" before
```

Confirmar quatro linhas `PROFILE`, saída 0 do validador e as sete portas livres.
Em Bash, verificar `${PIPESTATUS[0]}` imediatamente após o comando com `tee`.
O manifesto grava a versão de Java, os hashes do JAR e dos seis perfis e o nome
do operador. Usar a mesma pasta e o mesmo checkout nos terminais seguintes.

Na bancada autorizada, autenticar o próprio operador antes da sondagem.
`probe` usa um par TCP temporário criado pelo helper. Confirma a captura de
um pacote real e verifica que o socket selecionado desaparece após o corte.
O código 0 de `ss -K`, isoladamente, é insuficiente.

```bash
sudo -v
python3 "$M/bench.py" probe "$RUN"
```

Avançar se `permission-probe.json` contiver `"passed": true`. Se falhar,
conservar a pasta e registar a pendência. Se uma credencial expirar noutro
terminal, autenticar de novo nesse terminal antes de prosseguir.
[Q-C-14](../../docs/construction-duvidas.md#q-c-14--permissões-da-bancada-linux-para-v-009).

## Captura e arranque

No terminal de captura, usar a mesma raiz e os valores de `M` e `RUN` do
primeiro terminal. Usar um diretório novo para cada ensaio. Aguardar
`Capture ready` antes de arrancar BOOT. O helper grava `capture.log`,
`control.pcap` e `capture-start.json` com a identidade do processo
`tcpdump`. A captura tem um limite de 300 segundos e 20 000 pacotes.
Se qualquer limite for atingido durante o cenário, registar o resultado parcial.

```bash
python3 "$M/bench.py" capture "$RUN"
```

Arrancar cada JVM num terminal próprio. O helper grava PID, comando, logs
e saída real da JVM. Os comandos interativos são `neighbors` e `quit`.

1. Arrancar BOOTSTRAP com `python3 "$M/bench.py" node "$RUN" BOOTSTRAP`.
   Aguardar READY.
2. Arrancar CLIENT e RELAY com `python3 "$M/bench.py" node "$RUN" CLIENT` e
   `python3 "$M/bench.py" node "$RUN" RELAY`, respetivamente. Aguardar READY
   em ambos enquanto SERVER está ausente. No terminal do operador,
   `python3 "$M/bench.py" mark "$RUN" client-relay-ready-server-absent`
   fixa esse ponto.
3. Arrancar SERVER com `python3 "$M/bench.py" node "$RUN" SERVER`.
   Aguardar a convergência. Escrever `neighbors` nas três consolas e confirmar
   dois vizinhos ACTIVE por nó. Guardar os instanceIds e connectionIds das
   três arestas a partir de `NeighborUp` e dos snapshots. Marcar
   `converged` com o comando `mark`.

## Janela de observação e reconexão

Com o bootstrap ainda ativo e a overlay convergida, executar no terminal
do operador `python3 "$M/bench.py" observe "$RUN"`. São **30 segundos**
medidos entre marcas UTC e monotónicas. Esta duração é um critério de bancada:
contém dois períodos de 15 segundos, o intervalo de referência escolhido
para detetar atividade periódica frequente mesmo com fase desconhecida. Não
é um prazo do protocolo. A cobertura continua após a janela, durante a paragem
do bootstrap, a quebra da ligação e a reconexão. O backoff dos vizinhos orienta
a espera pela reconexão. A janela de observação do bootstrap é independente.

Após a janela, usar `quit` na consola BOOTSTRAP e aguardar `java_exit: 0`.
Marcar `bootstrap-off`. Verificar que a captura continua ativa.

```bash
python3 "$M/bench.py" mark "$RUN" bootstrap-off
ss -4Hltn 'sport = :56100'
python3 "$M/bench.py" pair "$RUN" | tee "$RUN/pair-inspection.json"
```

Confirmar que o comando de listener não apresenta linhas. O comando `pair`
identifica a sessão SERVER → RELAY. Confere o PID e a hora de início no
`/proc` de ambas as JVMs e as duas direções do mesmo 4-tuple IPv4. Guardar
a saída e confirmar que corresponde às sessões ACTIVE observadas.
Copiar a **porta de origem efémera** do campo `source_port`.
Antes de executar `ss -K`, o helper valida novamente a seleção e confirma
que BOOT continua sem listener. A última verificação lê o PID, a identidade
do arranque, o comando e o estado do `tcpdump` registado. A verificação exige
um único processo correspondente, em execução ou pronto a executar (R),
ou em espera interruptível (S).
Uma captura terminada, suspensa, desaparecida ou ambígua impede o corte.

```bash
read -r SOURCE_PORT
python3 "$M/bench.py" cut "$RUN" "$SOURCE_PORT"
```

O corte só é considerado bem-sucedido se a ferramenta reportar o socket,
o resultado não incluir erro e o 4-tuple antigo desaparecer. Guardar
`cut-before.json` e `cut-result.json`. Este último inclui o estado de captura
observado imediatamente antes do corte. Aguardar `NeighborDown` com motivo
`TRANSPORT` e depois `NeighborUp` nos dois processos. Pedir `neighbors`
e marcar `reconnected`. Confirmar que os PIDs e instanceIds são os mesmos,
os connectionIds da aresta afetada mudaram e as outras duas arestas conservam
as sessões. A captura permite correlacionar o novo HELLO/HELLO_OK e verificar
a porta 56100 no intervalo com BOOT desligado.

## Saída, limpeza e leitura da captura

1. Ainda com BOOT desligado, marcar `client-quit` e escrever `quit` na
   consola CLIENT. Confirmar `java_exit: 0` e `NeighborDown` com motivo
   `LEAVE` nos sobreviventes. Pedir `neighbors` em SERVER e RELAY para
   confirmar a aresta reconstituída.
2. Escrever `quit` nas consolas dos dois sobreviventes. Conferir os quatro
   ficheiros de saída, a ausência dos PIDs registados e as portas com
   `python3 "$M/bench.py" ports "$RUN" after`. Se o bind ainda não estiver
   disponível, guardar o estado TCP e uma sondagem posterior
   `ports "$RUN" after-retry`. Distinguir TIME_WAIT de um listener vivo.
   Marcar `scenario-ended` e terminar a captura com Ctrl-C. Em caso de
   falha, terminar apenas os processos desta execução e guardar os resultados
   que existirem.
3. Confirmar em `capture.log` que a captura começou antes de BOOT e terminou
   depois da última marca. Conferir o motivo de fim e a contagem de pacotes
   perdidos. Validar leitura até ao fim do PCAP. No relatório anotar o intervalo
   efetivo e perdas. Uma falha de captura ou uma janela incompleta fica
   assinalada como evidência insuficiente.
4. Guardar a cronologia de pacotes e a seleção da porta de BOOT.
   Os ACK, FIN e RST das ligações iniciais pertencem ao transporte. Usar o
   4-tuple e as marcas para os distinguir de novos SYN e frames de aplicação.

```bash
TZ=UTC tcpdump -tttt -nn -r "$RUN/control.pcap" > "$RUN/tcp-timeline.log" 2> "$RUN/tcp-read.log"
TZ=UTC tcpdump -tttt -nn -r "$RUN/control.pcap" 'tcp port 56100' > "$RUN/bootstrap-packets.log"
```

No leitor de PCAP, selecionar cada conexão de interesse pelo 4-tuple e pelo
instante, seguir o fluxo TCP e exportar **cada direção com dados** em formato
binário Raw. Em Wireshark, usar Follow TCP Stream, selecionar uma direção e
guardar com Show data as Raw. Separar a conexão inicial da nova conexão.
Identificar os ficheiros pelo ID do fluxo e direção. Descodificar cada export
com o codec do JAR. Um exemplo com o identificador que o leitor atribuiu:

```bash
"$JAVA" --class-path "$JAR" "$M/Inspect.java" frames "$RUN/stream-0-request.bin" > "$RUN/stream-0-request.frames.log"
```

Incluir os requestIds de cada REGISTER/REGISTER_OK e dos HELLO/HELLO_OK
selecionados, o endpoint devolvido do SERVER offline e os NODE_LEAVE reason 0
do CLIENT. Confrontar os frames com os eventos e IDs nos logs Java. No resumo
do operador, indicar para cada passo os ficheiros, instantes e resultado.
Indicar se foram observadas novas conexões ou mensagens para BOOT nas janelas
capturadas. Uma janela de 30 segundos só sustenta conclusões sobre o intervalo
observado, complementadas pelo contrato de descoberta e pelo teste de reconexão.

Um CLIENT reiniciado tem uma instância nova e precisa da consulta inicial.
Se esse cenário adicional for mostrado, usar outra execução, com BOOT
disponível para a nova consulta. A prova do passo de reconexão usa os dois
processos existentes.

## Limites e passagem a Test

A demonstração cobre papéis e CLI (RF-001), saída e continuidade (RF-002),
ligações TCP (RF-003) e descoberta inicial (RF-004). A topologia usa loopback.
Encaminhamento de fluxos, streaming, reprodução, GUI, cenários CORE e métricas
de desempenho pertencem a outras provas. Durante a indisponibilidade de um
vizinho, os diagnósticos podem dificultar a leitura da consola.

A evidência inicial neste diretório consiste nas sondagens de permissões.
Um ensaio técnico precisa da bancada e das permissões indicadas acima.
Para aprovar Test, outro elemento executa o roteiro e entrega o identificador
da execução, os logs, a captura e o resumo de V-009. A versão final é revista
por Fillmore e integrada antes de DONE.
