# Manual humano — IMP-007 / 005-rotas / V-010

**Procedimento executado por Francisco na execução02 e conferido após confirmação
do utilizador. Test continua por aprovar.** Logs pessoais preservados em
manual-humano-execucao-02/; conferência em manual-human-02-review.md. O histórico
de preparação/ensaio do agente abaixo continua distinto da execução humana.
O agente executou separadamente o ensaio técnico02:24 CHECK_PASS,
TECHNICAL_REHEARSAL_PASS e CLEANUP_COMPLETE, human_manual_performed=false.
Não conta como manual humano, GUI/CORE, revisão final ou integração.

## Construção e alcance

clean verify em Projeto/:1176 testes,0falhas/0erros/0ignorados,exit0.
Log test-clean-verify.log; XML test-clean-verify-xml/.
JAR imutável usado neste manual: test-build/ott-streaming-1.0-SNAPSHOT.jar,
330491 bytes; cópia byte a byte da construção de target/.
SHA256:

```text
3f39cab445ca05360525691b15c8f7193b7e0186b8033ef8cbd86d50690cfb15
```

Não reconstruir nem trocar o JAR durante a execução. manual-run.py check valida
o hash do JAR e de todos os perfis/topologia antes de cada arranque. Usa a CLI
já existente via java -cp, sem introduzir comandos na aplicação ou helpers Java.
O logger grava o comando, operador declarado, PID, stdout/stderr e o returncode
real depois do processo terminar; tee não mascara o código Java. Os resultados
.exit.json distinguem saída normal de sinal, e nunca anunciam exit=0 prematuro.
Não sobrescreve nomes de execuções existentes.

Este manual usa dois nós overlay reais e bootstrap em loopback para observar
sondas v2, deteção TCP/ausência de PONG, reconexão/identidades e terminação. Não
é o cenário CORE de quatro nós nem demonstra vídeo. RTT/EWMA numéricos, caps,
corridas e snapshot programático estão cobertos nos testes V-010; a CLI atual não
tem comando metrics e não se inventa um. Não medir RTT a partir de timestamps
UTC dos logs nem atribuir um atraso de saída do logger à rede.

## Perfis e portas confirmados

ConfigurationLoader.loadDeployment do JAR novo validou conjuntamente os três
perfis, sem iniciar serviços OTT: manual-profile-validation-real.log,
MANUAL_DEPLOYMENT_VALID profiles=3. O motor padrão JShell usa um socket local de
depuração, independente de serviços OTT; a tentativa na sandbox foi preservada.

| Perfil | Papel | Host | TCP controlo | UDP configurado |
|---|---|---|---:|---:|
| BOOT.properties | BOOTSTRAP | 127.0.0.1 | 58020 | — |
| R1.properties | RELAY | 127.0.0.1 | 58021 | 58121 |
| R2.properties | RELAY | 127.0.0.1 | 58022 | 58122 |

Grafo R1↔R2; bootstrap só localiza os vizinhos iniciais. Dados UDP não são abertos
por este incremento. I=500ms,T=1000ms,F=3,alpha=0.2; shutdown3000ms por processo.
São overrides operacionais validados, sem alterar wire/caps/política. Disponibilidade
das portas é temporária, não reserva: reconfirmar antes do arranque.

## Terminal 1 — preparação e primeiro bootstrap

Abrir quatro terminais zsh, com Java21 e Python3. No primeiro:

```zsh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2
OTT_MANUAL_ROOT="/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/005-rotas"
OTT_MANUAL_DIR="$OTT_MANUAL_ROOT/manual-humano-execucao-01"
read "OTT_MANUAL_OPERATOR?Nome real do operador: "
mkdir "$OTT_MANUAL_DIR"
printf '%s\n' "$OTT_MANUAL_OPERATOR" > "$OTT_MANUAL_DIR/operator.txt"
java -version 2>&1 | tee "$OTT_MANUAL_DIR/java-version.log"
python3 "$OTT_MANUAL_ROOT/manual-run.py" check | tee "$OTT_MANUAL_DIR/hash-check.log"
python3 "$OTT_MANUAL_ROOT/manual-run.py" ports | tee "$OTT_MANUAL_DIR/ports-before.json"
```

Exigir hash acima e available=true nas cinco portas. Se houver conflito, parar e
guardar evidência; não matar serviços alheios nem editar os perfis desta construção.
Se o diretório já existir, escolher outro sufixo e usar o mesmo nos quatro terminais.
`mkdir` deve criar uma pasta nova; não avançar sobre logs pessoais anteriores.

```zsh
python3 "$OTT_MANUAL_ROOT/manual-run.py" run BOOT --name BOOT-01 --dir "$OTT_MANUAL_DIR" --operator "$OTT_MANUAL_OPERATOR"
```

Esperar READY node=BOOT. Não escrever quit ainda.

## Terminal 2 — R1

```zsh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2
OTT_MANUAL_ROOT="/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/005-rotas"
OTT_MANUAL_DIR="$OTT_MANUAL_ROOT/manual-humano-execucao-01"
OTT_MANUAL_OPERATOR="$(cat "$OTT_MANUAL_DIR/operator.txt")"
python3 "$OTT_MANUAL_ROOT/manual-run.py" run R1 --name R1-01 --dir "$OTT_MANUAL_DIR" --operator "$OTT_MANUAL_OPERATOR"
```

Esperar READY node=R1; R2 ainda indisponível pode produzir diagnóstico/retry local,
sem FATAL. A aplicação continua à espera dos comandos existentes.

## Terminal 3 — R2

```zsh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2
OTT_MANUAL_ROOT="/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/005-rotas"
OTT_MANUAL_DIR="$OTT_MANUAL_ROOT/manual-humano-execucao-01"
OTT_MANUAL_OPERATOR="$(cat "$OTT_MANUAL_DIR/operator.txt")"
python3 "$OTT_MANUAL_ROOT/manual-run.py" run R2 --name R2-01 --dir "$OTT_MANUAL_DIR" --operator "$OTT_MANUAL_OPERATOR"
```

Esperar READY e NeighborUp em ambos. Escrever `neighbors` em T2 e T3: um vizinho
ACTIVE em cada nó. Ver Received type=PROBE_PING e PROBE_PONG version=2 em ambos;
registar instanceId/connectionId de R2 observado por R1.

## Terminal 4 — observação, pausa e retoma

```zsh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2
OTT_MANUAL_ROOT="/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/005-rotas"
OTT_MANUAL_DIR="$OTT_MANUAL_ROOT/manual-humano-execucao-01"
OTT_R2_PID="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["java_pid"])' "$OTT_MANUAL_DIR/R2-01.pid.json")"
ps -p "$OTT_R2_PID" -o pid=,command= | tee "$OTT_MANUAL_DIR/R2-process-before.log"
```

Conferir que é o Java deste JAR/perfil R2 iniciado em T3, antes de qualquer sinal.
Não usar um PID de ensaio antigo. Pausar só esse processo:

```zsh
kill -STOP "$OTT_R2_PID"
```

Observar T2: R1 mantém a CLI viva e acaba por emitir NeighborDown da sessão R2.
Escrever `neighbors`: R2 já não está ACTIVE. O processo R2 permanece pausado, com
TCP aberto inicialmente; a ausência de PONG causa a retirada. O número exato de
sondas e as fronteiras são provados pelo relógio falso; não inferir um contador
de perdas inexistente na CLI ou uma medição de latência só pelo tempo humano.

Agora em T1 escrever `quit`, exigir PROCESS_RESULT JAVA_EXIT=0. Só depois, em T4:

```zsh
kill -CONT "$OTT_R2_PID"
```

Esperar novo NeighborUp em T2/T3 sem reabrir bootstrap: R2 tem a **mesma instanceId**
e **outro connectionId**, e PROBE_PONG volta a aparecer. `neighbors` volta a mostrar
ACTIVE. Esta é reconexão de uma instância já descoberta, não arranque de novo nó.

## Morte real/EOF e instância nova

Em T4, com R2 já ativo:

```zsh
kill -KILL "$OTT_R2_PID"
```

T2 deve observar NeighborDown e continuar vivo. A retirada de métricas no evento
EOF está verificada também pela integração/snapshot V-010. T3 termina o logger;
R2-01.exit.json deve guardar returncode=-9/signal=9/shell_status=137, intencional,
**não** saída0 nem falha a ocultar. Não exigir CLEANUP normal a um SIGKILL.

Uma instância nova precisa de bootstrap para o **primeiro REGISTER**. Reabrir em T1:

```zsh
python3 "$OTT_MANUAL_ROOT/manual-run.py" run BOOT --name BOOT-02 --dir "$OTT_MANUAL_DIR" --operator "$OTT_MANUAL_OPERATOR"
```

Esperar READY. Em T3:

```zsh
python3 "$OTT_MANUAL_ROOT/manual-run.py" run R2 --name R2-02 --dir "$OTT_MANUAL_DIR" --operator "$OTT_MANUAL_OPERATOR"
```

Esperar READY/NeighborUp. Comparar em T2: instanceId e connectionId de R2 são novos;
`neighbors`/PROBE_PONG funcionam e não há FATAL. Em T1 escrever quit e exigir0;
a sessão R1–R2 deve continuar com sondas sem bootstrap.

## Fecho e evidência humana

Em T3 escrever quit, exigir JAVA_EXIT=0. Em T2 escrever quit, exigir JAVA_EXIT=0.
Os logs normais terminam com SHUTDOWN phase=closing-output e PROCESS_RESULT após
saída efetiva. SHUTDOWN sozinho não prova terminação física: conferir os .exit.json.

Em T4:

```zsh
python3 "$OTT_MANUAL_ROOT/manual-run.py" ports | tee "$OTT_MANUAL_DIR/ports-after.json"
python3 -c 'import json,pathlib,sys; p=pathlib.Path(sys.argv[1]); [print(f.name,json.loads(f.read_text())) for f in sorted(p.glob("*.exit.json"))]' "$OTT_MANUAL_DIR"
```

Guardar estes resultados e uma confirmação escrita do operador, com o caminho dos
logs e as observações feitas: sondas bidirecionais; retirada com peer pausado;
mesma instância/nova conexão após CONT sem bootstrap; EOF com KILL; nova instância
após REGISTER; CLI ativa; saídas normais0 e KILL intencional9; portas libertadas.
Se algo falhar, registar a falha e não declarar aprovação do manual/Test. Nenhuma
confirmação é criada automaticamente pelo agente nem por este logger.

A prova automática de shutdown físico (owner/I/O/helper/callbacks retidos/prazo
único) consta dos XML de clean verify e testes D12/D13/D15/integração, sem a trocar
pelo mero fim de uma linha de log. Revisão final do código e integração continuam
pendentes mesmo depois de executar/aprovar o manual. Sem gates externos adicionais.
