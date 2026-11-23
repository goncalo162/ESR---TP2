# Guia de uso da DEMO OTT (plano de controlo, PHASE-04)

A demo mostra o plano de controlo concluído (descoberta, rotas, subscrições,
renovação, adaptação por qualidade com atraso real, falha/recuperação, poda)
com **uma JVM real por nó overlay**, bootstrap separado, CLI de produção real e
proxy TCP de atraso sem root (local) ou netem do CORE (em coreemu). **Não
inclui vídeo/UDP/player** (isso é a PHASE-05) — ver "Limites" no fim.

## 1. Pré-requisitos

- Linux, JDK 21, Maven, Python 3; sockets locais permitidos; sem root.
- Build: `cd Projeto && mvn -B -ntp clean verify` → esperado **1459/0/0/0**,
  JAR em `Projeto/target/ott-streaming-1.0-SNAPSHOT.jar`.

```sh
cd <raiz do repositório>
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk   # ou qualquer JDK 21
export PATH="$JAVA_HOME/bin:$PATH"
```

## 2. Correr localmente

```sh
./demo/run.sh cenario1 --auto    # 4 nós (S1,R1,C1,C2)
./demo/run.sh cenario2 --auto    # 6 nós com triângulo R1/R2/R3 + troca por qualidade
./demo/run.sh cenario3 --auto    # 11 nós da Figura 1 (O1–O11)
```

- O script compila os auxiliares (`demo/.classes`) e imprime `RUN <dir>` e
  `DASHBOARD <ficheiro>`; abrir o `dashboard.html` no browser (recarrega a cada
  2 s). A consola é o controlo.
- Resultado: `experiments/checkpoint-phase04/runs/<cenario>/<id>/result.json`
  (`verdict`, checks, processos, portas) + `wire.jsonl`, `snapshots/`,
  `logs/`, `manifest.json` (commit/JDK/JAR), `tool-source/`+`tool-sha256.json`.
- Runs FAIL são preservadas — nunca apagar.

## 3. Modo interativo (apresentação, 10–15 min)

```sh
./demo/run.sh cenario2           # sem --auto
```

Comandos: `next` (6 passos do guião), `snapshot`, `raw C1 routes|metrics`,
`raw C1 trace 00000000-0000-4000-8000-00000000000a`, `delay R1-R2 120`,
`kill R2`, `restart R2`, `quit`. Tempos por passo: `demo/DEMO.md`.
Opções: `--port-base 24000` (base de portas), `DEMO_JAR=<jar>` (outro JAR, hash
registado; não atribui runs antigas a esse ficheiro).

## 4. O que observar (oráculos)

- Arranque validado (`ConfigurationLoader.loadDeployment`), HELLO/discovery
  TCP v2, ACTIVE, RTT real, catálogo.
- CLI real (`OttApplication`): neighbors/recovery/routes/catalog/metrics/trace/
  play (`play` = `NOT_IMPLEMENTED` de propósito)/quit.
- ACK, isolamento A/B, um tronco agregado no servidor, renovação (tipo 14)
  além dos 6 s iniciais, trace positivo multi-salto após ACK.
- Cenário2/3: pulso transitório de 150 ms não oscila; atraso sustentado de
  120 ms/direção em R1–R2 (O2–O3) produz `STARTED→SUCCESS` com o novo
  upstream/caminho já publicados.
- Falha real (SIGKILL do relay), `NeighborDown`, recuperação **sem bootstrap**;
  reinício com bootstrap novo só para a JVM nova; poda seletiva e remoção de
  procura abandonada após morte real de cliente.
- Em todas as runs: processos terminados e portas reutilizáveis.

## 5. Correr a demo no CORE (coreemu dockerizado, PHASE-06)

O contentor `core` (Coreemu, `CORE/docker-compose.yml`) expõe o daemon gRPC em
`localhost:50051` e monta `CORE/volume` em `/volume`. Com o docker do CORE a
correr:

```sh
cd CORE && docker compose up -d core        # contentor `core` Up
cd ..
python3 demo/core/run_core.py cenario1      # 4 nós (S1,R1,C1,C2)
python3 demo/core/run_core.py cenario2      # 6 nós + troca por qualidade via netem
python3 demo/core/run_core.py cenario3      # 11 nós da Figura 1 (O1–O11)
```

`run_core.py` (host) faz tudo automaticamente:

1. valida pré-requisitos (contentor Up, JDK 21, JAR + sha256, compila
   `demo/.classes`);
2. gera os perfis por nó com os IPs do XML (`prepare_core.build_deployment`) e
   copia JAR + classes + driver para `CORE/volume/ott/<run-id>/`;
3. arranca `tcpdump` no contentor (pcap cru em `out/wire.pcap`);
4. executa `demo/core/driver.py` dentro do contentor: cria a sessão gRPC a
   partir de `CORE/topologies/cenarioN.xml` (devices, switches e links com os
   atrasos do cenário em netem µs), arranca o bootstrap (`OttApplication
   --role BOOTSTRAP`) no servidor e uma JVM `DemoNode` por nó overlay dentro do
   namespace de cada dispositivo (stdin por FIFO em `/volume`, stdout para
   `logs/`), e corre o guião de 6 passos igual ao local — **incluindo a etapa
   com a CLI real** `OttApplication` no cliente (neighbors/recovery/routes/
   catalog/metrics/trace/play/quit em `logs/production-cli.log`);
5. a troca por qualidade usa `edit_link` (netem, µs) na ligação R1–R2 em vez
   do proxy local; o kill é um `SIGKILL` real ao JVM no namespace do nó;
6. copia a evidência para `experiments/core-demo/<cenario>/<run-id>/` e imprime
   `RESULT PASS|FAIL`.

```sh
python3 demo/core/run_core.py cenario2 --source-run 20261123T151525Z-1b1daa64
# --source-run escolhe a run local donde vêm os perfis/portas (há um default
# PASS por cenário); --out-root muda o destino da evidência
```

Evidência por run em `experiments/core-demo/<cenario>/<run-id>/`:
`out/result.json` (verdict + checks), `out/latest.json`, `out/snapshots/`,
`out/trace-before.json` / `trace-after.json`, `out/actions.jsonl` (mudanças de
atraso), `out/wire.pcap`, `logs/` (por nó + `production-cli.log`),
`driver-stdout.log`, `plan.json`, `manifest.json`, `tool-sha256.json`.

Resultados actuais (2026-11-23, JAR `dc192971…`, JDK 21, HEAD `a7c0789`+):
**PASS em cenario1 (2× consecutivos), cenario2 e cenario3** —
`experiments/core-demo/cenario{1,2,3}/2026*/`. Ex.: cenário2 com switch
R1→R3 por atraso sustentado de 120 ms (STARTED→SUCCESS, publicação atómica),
SIGKILL real do R2, `NeighborDown` e recuperação sem bootstrap; cenário3 com
trace multi-salto `O4→O3→O2→O1` SUCCESS.

Para deploy manual (sem o runner automático), `prepare_core.py` continua
disponível:

```sh
python3 demo/core/prepare_core.py --scenario cenario2 \
  --run 20261123T151525Z-1b1daa64 \
  --xml CORE/topologies/cenario2.xml \
  --jar Projeto/target/ott-streaming-1.0-SNAPSHOT.jar \
  --out demo/core/out/cenario2-1b1daa64
```

Notas: os aliases 127/8 e o proxy localhost **não** existem no CORE — os IPs
são os do XML e os atrasos vêm do netem do próprio emulador. A contagem de
frames de controlo (HELLO/ACTIVATE/RENEW/DEACTIVATE) não é descodificada no
CORE: fica preservada como pcap cru e é substituída por estado observado nos
snapshots + contadores de interface (ver `note` em `result.json`). O JVM da
CLI pode não terminar sozinho após `quit` no CORE (teardown de rede); nesse
caso o driver termina-o e regista o método real no check `production-CLI-exit0`.

## 6. Limites actuais (não simulados)

- Sem UDP, fragmentação, MJPEG, player Swing, vídeo real — PHASE-05 (TASK-019–
  024) continua TODO e é gate da PHASE-06.
- PHASE-07 (artigo/empacotamento/defesa) depende de informação docente
  (G-ASSESSMENTS: turno/grupo/datas) e de submissão externa — não é feita sem
  autorização explícita.
- ~20% das runs do cenário2 local podem falhar na 1.ª tentativa do switch por
  uma corrida do próprio JAR (PATH_CHANGED no alvo); a run fica preservada como
  FAIL e basta repetir. R17 histórico continua sem causa establecida.

## 7. Verificação rápida do estado

```sh
cd Projeto && mvn -B -ntp clean verify | tail -5          # 1459/0/0/0 esperado
python3 - <<'EOF'
import json,glob
for f in sorted(glob.glob('../experiments/checkpoint-phase04/runs/*/*/result.json'))[-6:]:
    r=json.load(open(f)); print(f.split('runs/')[1], r['verdict'], sum(c['passed'] for c in r['checks']), '/', len(r['checks']))
EOF
```
