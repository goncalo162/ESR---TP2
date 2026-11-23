# Guia de uso da DEMO OTT (plano de controlo, PHASE-04)

A demo mostra o plano de controlo concluído (descoberta, rotas, subscrições,
renovação, adaptação por qualidade com atraso real, falha/recuperação, poda)
com **uma JVM real por nó overlay**, bootstrap separado, CLI de produção real e
proxy TCP de atraso sem root. **Não inclui vídeo/UDP/player** (isso é a
PHASE-05) nem execução em CORE (PHASE-06) — ver "Limites" no fim.

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

## 5. Preparar a demo para o CORE (PHASE-06) — sem executar

O CORE usa `CORE/topologies/cenario{1,2,3}.xml` (Coreemu dockerizado). A demo
gera os perfis por nó a partir de uma run local; o script de transposição:

```sh
python3 demo/core/prepare_core.py --scenario cenario2 \
  --run 20261123T151525Z-1b1daa64 \
  --xml CORE/topologies/cenario2.xml \
  --jar Projeto/target/ott-streaming-1.0-SNAPSHOT.jar \
  --out demo/core/out/cenario2-1b1daa64
```

Produz em `--out`: `nodes/<N>.properties` (bind/advertised = IP do dispositivo
CORE), `topology.properties` reescrita, `streams.properties`, `BOOT.properties`
(bootstrap no host do servidor), `manifest.json` (mapeamento dispositivo→nó,
IPs, hash do JAR, comandos por nó) e `README.md`. Passos no CORE:

1. Abrir a topologia no core-gui e confirmar os IPs (manifest → `deviceIp`).
2. Copiar a pasta gerada para `CORE/volume/ott/<cenario>/`.
3. No host do servidor: comando BOOT do manifest. Em cada nó: o comando do
   manifest (adaptador `DemoNode` para subscribe/trace/snapshot, ou a CLI real
   `OttApplication`).
4. Recolher manifests/logs/capturas em `experiments/` segundo
   `docs/09-TESTING-STRATEGY.md`.

Notas: os aliases 127/8 e o proxy localhost **não** existem no CORE — no CORE
os atrasos de ligação vêm do próprio emulador (opções do link no XML ou
`tc/netem`). O relay pode correr no namespace do router, como admite o
enunciado. Exemplos já gerados: `demo/core/out/cenario{1,2,3}-*`.

## 6. Limites actuais (não simulados)

- Sem UDP, fragmentação, MJPEG, player Swing, vídeo real — PHASE-05 (TASK-019–
  024) continua TODO e é gate da PHASE-06.
- Execução em CORE ainda não feita nesta bancada (coreemu requer docker + X11 e
  sessão CORE real) — a PHASE-06 fica pendente até lá; aqui há apenas os
  artefactos de deployment.
- PHASE-07 (artigo/empacotamento/defesa) depende de informação docente
  (G-ASSESSMENTS: turno/grupo/datas) e de submissão externa — não é feita sem
  autorização explícita.
- ~20% das runs do cenário2 podem falhar na 1.ª tentativa do switch por uma
  corrida do próprio JAR (PATH_CHANGED no alvo); a run fica preservada como
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
