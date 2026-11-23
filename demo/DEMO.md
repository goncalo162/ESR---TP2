# Checkpoint OTT — demonstração validada (série formal congelada `8303c0b`)

Demonstração do plano de controlo concluído na PHASE-04, executada e repetida
em 2026-11-23: 3 PASS consecutivas de cada cenário na versão congelada
`8303c0b` (c1 `ff3a5f83`/`9deaebcc`/`28c6b934`, c2 `4b4adbaf`/`67a5116e`/`1b1daa64`),
mais séries no HEAD final `63d0c97` (c1 3/3 PASS) e cenário3 (11 nós da Figura 1)
com PASS repetido (`96352b48`, `73408404`, `149ff3ed`, `e135a4d5`); FAILs
preservados como corrida residual do sistema. Builds inicial/final 1459/0/0/0,
21 XML; JAR `dc192971…` inalterado. Runs em
`experiments/checkpoint-phase04/runs/`; detalhes e limitações no
[handoff](../memory-bank/demonstrations/DEMO-001-checkpoint-phase04/HANDOFF.md)
e na [investigação](../memory-bank/demonstrations/DEMO-001-checkpoint-phase04/investigation-iteration2.md).
PHASE-05–07 não iniciadas. Não é aprovação docente nem execução CORE.

## Pré-requisitos e arranque

- Linux com loopback 127/8, Java/Javac 21 e Maven; sockets locais e criação de
  processos permitidos. Sem root, Python obrigatório ou dependências novas.
- JAR atual produzido por `cd Projeto && mvn -B -ntp clean verify`.
- Numa shell da raiz:

```sh
export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"  # qualquer JDK 21
export PATH="$JAVA_HOME/bin:$PATH"
./demo/run.sh cenario2
```

O comando compila apenas os auxiliares em `demo/.classes` e imprime o caminho
do `dashboard.html` e da run. Abrir o HTML no browser; a consola permanece o
controlo. O HTML é regenerado a partir de snapshots locais e recarrega a cada
dois segundos. Nenhum browser ou servidor HTTP é necessário para a validação.

`./demo/run.sh cenario1` seleciona os quatro nós; `cenario3` seleciona os onze
nós da Figura 1 (ensaiado com PASS em 2026-11-23). `--auto` executa o guião automaticamente;
`--port-base 24000` escolhe as portas (0/default escolhe portas efémeras).
`DEMO_JAR=/caminho/ficheiro.jar` seleciona explicitamente outro JAR, com hash
registado. Não atribuir resultados antigos a esse ficheiro.

## O que está a correr

Uma JVM por nó overlay, mais bootstrap. Os nós usam `OverlayNode`, monitorização,
transporte e routing do mesmo JAR. O adaptador não reimplementa políticas:
expõe snapshots e chama as APIs de subscrição/trace. Bootstrap e uma etapa
própria da CLI executam a main real `OttApplication`.

`subscribe`/`cancel` são comandos **do auxiliar**, porque a CLI de produção
ainda não os oferece. A etapa da CLI mostra neighbors/recovery/routes/catalog/
metrics/trace/play/quit. O resultado negativo de trace observado antes de ACK
é preservado (a rota pode estar momentaneamente withdrawna quando a CLI substitui
o adaptador); um trace positivo por vários saltos é exigido separadamente depois
de ACK, observado numa janela com prazo como as restantes condições.

Os endereços anunciados 127.0.0.N passam pelo proxy e os listeners reais usam
127.0.1.N. O bootstrap usa 127.0.0.200. A identificação de cada ligação é lida
do HELLO v2. O proxy atrasa **bytes reais de todos os frames** nos dois sentidos:
cada frame é agendado para `chegada + atraso` por direção, sem fila de
serialização entre frames (um burst não atrasa os seguintes para além do seu
próprio atraso) e preservando a ordenação TCP. Não injeta `LinkMetrics` e não
chama `onMetrics`; o custo medido é aproximadamente duas vezes o atraso
configurado mais scheduling real da JVM/TCP. Nas iterações anteriores o proxy
dormia no ciclo de leitura, o que enfileirava frames e inflava o custo medido
(>900ms com atraso de 120ms); esse artefacto da ferramenta foi removido e os
negativos preservados não lhe são atribuídos sem prova.

## Guião previsto — 10–15 minutos

Tempo alvo de apresentação; ainda não é uma medição de três ensaios estáveis.
Executar os cenários 1 e 2; reservar o cenário 3 para depois de validado.

1. **Arrancar e explicar limites** (2 min). Conferir validação, PIDs, ACTIVE,
   HELLO/HELLO_OK, DISCOVERY_QUERY/SNAPSHOT, RTT e catálogo no HTML. SERVER
   anuncia metadados de dois streams; não lê vídeos.
2. **Primeiro `next`: CLI e ativação** (2 min). A ferramenta substitui
   temporariamente C2 por uma JVM com CLI real no mesmo perfil e repõe o
   adaptador. Mostrar `logs/production-cli.log`, incluindo `NOT_IMPLEMENTED play`.
   Esperar catálogo/rotas elegíveis antes de pedir A em dois clientes e B em C2.
   Ver ACK e forwarding confirmado, uma ativação inicial no tronco comum e
   ausência de B no forwarding de C1. O ticket inicial e a confirmação atual
   são projeções diferentes: recuperação posterior não reescreve um resultado
   terminal do pedido inicial.
3. **Segundo `next`: renovar e diagnosticar** (1 min). Aguardar para lá dos
   seis segundos do grant inicial. Ver SUBSCRIPTION_RENEW tipo14/ACK tipo12 e
   continuidade de forwarding; trace positivo preservado em `trace-before.json`.
4. **Terceiro `next`: adaptar RTT** (2 min, cenário2). Em R2, comparar R1 direto
   e R3 alternativo. Um pulso transitório de atraso não deve provocar troca.
   Atraso sustentado de 120ms por direção em R1–R2 deve alterar amostras reais
   e confirmar R3. Política existente: melhoria20%, três observações distintas,
   permanência5s. Observar STARTED/SUCCESS, custos, caminho e revisão já publicada.
   Cenário1 tem só uma rota configurada e remete este passo para cenário2.
5. **Quarto `next`: falha real e recuperação** (2 min). O bootstrap termina;
   SIGKILL atinge apenas o relay criado pela ferramenta. Mostrar NeighborDown,
   cache anterior e recuperação do mesmo consumidor/intent sem bootstrap.
   Cenário1 perde R1 e depende de reparação aprendida; cenário2 perde R2.
   Conferir `failure-no-bootstrap.json` e `trace-after.json`.
6. **Quinto `next`: reiniciar** (1 min). Voltar a arrancar bootstrap e o relay.
   Esta JVM nova faz descoberta inicial legítima; isso não é a reparação sem
   bootstrap já observada nos sobreviventes. Conferir ACTIVE e sessões novas.
7. **Sexto `next`: poda e procura abandonada** (1 min). Cancelar A de C1;
   C2 mantém A/B. Cancelar o último A; B continua. Matar C2 sem cancel de B;
   a procura B deve desaparecer com a falha de sessão. Não apresentar este
   passo como uma prova isolada de expiração de lease sem falha TCP.
8. **`quit`**. Conferir resultado e limpeza de todos os processos/listeners.
   No modo interativo, EOF ou saída antes dos seis passos produz uma run
   incompleta/FAIL, sem inventar aceitação.

Comandos auxiliares: `snapshot`, `raw C1 routes`, `raw C1 metrics`,
`raw C1 trace 00000000-0000-4000-8000-00000000000a`, `delay R1-R2 120`,
`kill R2`, `restart R2`. Intervenções livres podem invalidar as pré-condições
do guião; usar o caminho `next` para ensaiar a apresentação.

## Configurações, evidência e limpeza

Os descritores em `demo/config/` conservam os grafos do PDF. Cada run gera
perfis `.properties` completos por nó, topologia e catálogo em `config/`,
validados antes de abrir nós. Os caminhos media-not-used são apenas metadados;
não são vídeos de demonstração nem se criam ficheiros MJPEG falsos.

Em `experiments/checkpoint-phase04/runs/<cenario>/<utc-id>/`: manifest.json
(commit/JDK/JAR), tool-source/ e tool-sha256.json nas versões recentes,
config/, validation.log, wire.jsonl com frames descodificados sem alteração,
logs por JVM, snapshots por passo, latest.json, dashboard.html e result.json
com checks/PIDs/saídas/portas. Nas execuções da iteração 2 o output do controlador fica nos logs das séries
(`experiments/checkpoint-phase04/series-*.log`); duas runs interrompidas por
operador (`3206fc30`, `3b24ba01`) não têm `result.json` e estão preservadas sem
verdict. Não apagar runs FAIL.

`quit`, EOF e SIGTERM do controlador acionam a limpeza; só são terminados
processos que a ferramenta criou. SIGKILL do próprio controlador não executa
shutdown hooks: evitar matar a ferramenta dessa forma. Se houver interrupção
nessa condição, identificar os PIDs pelos perfis da run antes de qualquer
limpeza manual; nunca usar `pkill java`.

## Caminho para CORE — não executado

Reutilizar `CORE/topologies/cenario1.xml`, `cenario2.xml` e `cenario3.xml`.
Os perfis por nó da run são o mapa de configuração; na PHASE-06, copiar para
os nós correspondentes e trocar bind/advertised/bootstrap e hosts da topologia
pelos IPs CORE, conservando IDs/roles/grafo/UUIDs. Arrancar a CLI do mesmo JAR
em cada máquina. Os aliases 127/8 e o proxy localhost não transitam para nós
CORE separados. `tc/netem` pode ser um caminho futuro privilegiado nessa fase,
após identificar interfaces; não é pré-requisito da alternativa sem root local.

## Fora do âmbito

UDP, fragmentação, leitura MJPEG, encaminhamento/reconstrução de frames, player
Swing, vídeo real, execução CORE, FPS/QoE, artigo final, pacote e submissão.
PHASE-05–07 continuam TODO. R17 histórico permanece sem causa estabelecida;
V-018 com métricas controladas não explica essa falha. Datas/conteúdo docente
G-ASSESSMENTS não são declarados resolvidos pela ferramenta.

[Matriz para artigo](MATRIX.md) · [Estado e continuação](../memory-bank/demonstrations/DEMO-001-checkpoint-phase04/HANDOFF.md).
