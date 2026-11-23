# Investigação — iteração 2 (retoma do checkpoint)

Âmbito: só a ferramenta em `demo/`. Nenhuma alteração a fontes/testes de
produção, CORE ou scripts anteriores (`git diff b6de848..HEAD -- Projeto CORE
scripts` vazio; JAR `dc192971…` inalterado entre o build inicial e o final).

## 1. Ambiente confirmado antes de executar

- Branch `Waheed`, HEAD `b6de848` no início; working tree limpo (só
  `Projeto/.idea/` untracked, pré-existente e intocado).
- JDK `21.0.13.0.3` (`/usr/lib/jvm/java-21-openjdk` nesta máquina; o caminho
  `java-21-openjdk-amd64` do DEMO.md era da máquina anterior — DEMO.md tornado
  genérico), Maven 3.10.0.
- Clean verify inicial e final: `cd Projeto && mvn -B -ntp clean verify` →
  **1459/0/0/0, 21 XML, BUILD SUCCESS** em ambos
  ([início](../../../experiments/checkpoint-phase04/build-start-iteration2/result.json),
  [fim](../../../experiments/checkpoint-phase04/build-end-iteration2-final/result.json)),
  JAR `dc192971c9ba3a7f66df0e20b3851b4c176c8f4d34158267610b4d7eebab920f`
  idêntico (`jarSameAsStart: true`).

## 2. Negativo (a): cenário2 FAIL na troca por qualidade — causa

Run preservada `cenario2/20261122T233636Z-e48c789f` (máquina anterior);
reproduzido nesta máquina em `cenario2/20261123T114247Z-7e4c6be5` e
`20261123T142314Z-e63bfcf3`. Evidência:

- Evento `STARTED SUSTAINED_IMPROVEMENT` (ex.: op `05a0c597…`), caminho
  anterior `[S1,R1,R2]` → proposto `[S1,R1,R3,R2]`, custos 609166→57727µs;
  52–83ms depois `FAILURE PATH_CHANGED` (ou `QUALITY_CHANGED`) na mesma
  operação.
- Wire: `R2→ACTIVATE … rev0 path=[S1,R1,R3,R2]` rejeitado por
  `R3→ERROR PATH_CHANGED`; tentativas seguintes podem ser ACKed e o
  forwarding final fica via R3 — mas **sem evento SUCCESS**: em
  `RoutingService.confirm` o SUCCESS de switch só é emitido quando o round que
  confirma tem `switching != null`; a ronda de switch é terminada por
  `failedAttempt → fail()` na primeira rejeição (alvo bloqueado com
  `rearm=false`) e a convergência posterior dá-se por rondas de recuperação
  simples, que não emitem evento de switch.
- Na run antiga o custo medido R2→R1 chegou a 965620µs com atraso configurado
  de 120ms/direção (~4× o dobro do atraso): fila de serialização do proxy
  antigo (`Thread.sleep` no ciclo de leitura — cada frame esperava pelos
  anteriores num burst de anúncios).

**Separação defeito-ferramenta vs defeito-sistema:** o FAILURE do round e a
convergência sem SUCCESS são comportamento do sistema (JAR, `RoutingService`) —
não alterado, está vedado. A amplificação da oscilação de custos era artefacto
da ferramenta (fila de serialização do proxy). Nenhuma correção de produção foi
feita nem proposta; o tema (round de switch terminar na 1.ª rejeição em vez de
reaproveitar a convergência posterior) fica fora deste bolt.

**Ajuste justificado da ferramenta (só `demo/`, commit `94300b2`):**
`DelayProxy` agenda cada frame para `chegada + atraso` por direção (leitor e
escritor desacoplados, ordem TCP preservada, bytes inalterados), sem fila de
serialização entre frames. Com isso o custo R2→R1 mede ~244ms (≈2×120ms) de
forma estável e o switch faz `STARTED→SUCCESS CONFIRMED` (ex.: sonda
`cenario2/20261123T113816Z-6a17543f`, 49.6ms).

## 3. Negativo (b): NO_ROUTE pré-ativação da CLI e trace negativo pós-ACK

- Runs `cenario2/20261122T233117Z-a9871c5f`, `20261122T233325Z-bee35742`: a CLI
  real mostrava `ROUTE … selected=SessionKey[R3…] costMicros=54842` e
  `ROUTE_CANDIDATE … expired=false`, mas `trace` devolveu `TRACE_ERROR NO_ROUTE
  visited=[C2]` sem frame ROUTE_TRACE. No log da CLI vê-se `STREAM_WITHDRAW`
  dos dois streams imediatamente antes do TRACE_STARTED: ao substituir o
  adaptador pela JVM da CLI, a sessão antiga de C2 morre e o vizinho retira e
  reanuncia; no instante do trace o `selectedCandidate` estava vazio —
  resposta NO_ROUTE legítima do sistema nesse instante (`forwardTrace`: sem
  candidato ou sessão corrente → NO_ROUTE). **Não é defeito do sistema nem da
  CLI; é uma corrida de prontidão da ferramenta**, que emitia o trace assim que
  a listagem mostrava seleção.
- Reprodução nesta máquina: `cenario2/20261123T112915Z-c8130075` falhou em
  `trace-before-SUCCESS` com `NO_ROUTE visited=[C1]` 2.9ms após o ACK.
  Ajuste da ferramenta: `trace-before`/`trace-after` passaram a observação com
  prazo (15s, tentativas espaçadas 1s para não esgotar TRACE_CAPACITY),
  guardando sempre o último terminal no ficheiro de evidência; o requisito de
  SUCCESS multi-salto não foi removido.

## 4. Semântica dos oráculos revista

- Ticket inicial vs forwarding vivo: o resultado terminal de um pedido
  (`NO_ROUTE`/`UNKNOWN_STREAM` inicial) não é o estado vivo da árvore; ambos
  ficam preservados nos snapshots. A seleção do catálogo não autoriza
  forwarding.
- Negativo da CLI vs trace positivo multi-salto: são sondas distintas; o
  negativo terminal da CLI fica preservado e o trace positivo após ACK é
  exigido com janela própria.
- Atomicidade confirmação/publicação do switch: o oráculo antigo comparava
  `publishedRevision` (versão da árvore/forwarding) com `replacementRevision`
  (revisão do ACTIVATE na nova linha — espaço próprio, 0 numa linha nova), o
  que nunca podia passar. Corrigido (commit `94300b2`): no evento SUCCESS, a
  forwarding observada no instante do evento tem de mostrar o upstream e o
  caminho de substituição com revisão superior à anterior.
- Restabilização antes do atraso sustentado (commit `91db5ea`): após o pulso de
  150ms, esperar que o caminho anterior volte a confirmar e assim permaneça
  por janela superior ao dwell da política (5s). O prazo do switch (25s) e o
  requisito de SUCCESS não foram alterados.
- Esperas de convergência/recuperação escaladas com o nº de nós
  (commits `ec72427`, `f1d47f8`, `63d0c97`): 60s de arranque, 30s de ACK, 60s
  de recuperação, 30s de poda, 30s de prontidão da CLI para >6 nós; inerte nos
  cenários de 4/6 nós.
- Interrupção/SIGTERM/cleanup: `quit`, EOF e SIGTERM do controlador executam o
  shutdown hook (`cleanup`), que termina apenas processos criados pela
  ferramenta e fecha proxies/log; SIGKILL do próprio controlador não executa
  hooks (documentado em DEMO.md; nunca usar `pkill java`). Todas as runs com
  `result.json` terminaram com `processes[].alive=false` e
  `ports[].reusable=true`.

## 5. Estado final após os ajustes

- **Série formal congelada `8303c0b`** (JAR `dc192971…`, tool-sha256 comum
  `cee45de2…` nas 6 runs): cenário1 **3/3 PASS** (`ff3a5f83`, `9deaebcc`,
  `28c6b934`, 33 checks cada) e cenário2 **3/3 PASS** (`4b4adbaf`, `67a5116e`,
  `1b1daa64`, 39 checks cada) —
  [series-final-head.log](../../../experiments/checkpoint-phase04/series-final-head.log).
- HEAD final `63d0c97`: c1 3/3 PASS (`505e78d5`, `a7cbb419`, `8ba7e4cd`); c2 e c3
  com PASS e FAILs preservados —
  [series-final-head2.log](../../../experiments/checkpoint-phase04/series-final-head2.log).
- **Cenário3 (11 nós da Figura 1) completo, PASS repetido**: `96352b48`,
  `73408404`, `149ff3ed`, `e135a4d5` (39 checks cada).
- Corrida residual do sistema preservada: ~20% das runs do cenário2 falham na
  1.ª tentativa do switch (PATH_CHANGED no alvo; ex.: `e63bfcf3`, `32a314b3`,
  `3cc7f000`); o sistema converge na mesma, mas sem evento SUCCESS nesse caso.
  Duas runs interrompidas por operador sem `result.json` (`3206fc30`,
  `3b24ba01`) também preservadas.
- Todas as runs FAIL anteriores permanecem intactas; as runs novas incluem
  `tool-source/` e `tool-sha256.json`. R17 histórico continua sem causa estabelecida — ver handoff.
