# Investigação — iteração 2 (retoma do checkpoint)

Âmbito: só a ferramenta em `demo/`. Nenhuma alteração a fontes/testes de
produção, CORE ou scripts anteriores (verificado por `git status` e pelos hashes
do build: JAR `dc192971…` inalterado entre o build inicial e as runs).

## 1. Ambiente confirmado antes de executar

- Branch `Waheed`, HEAD `b6de848` no início; working tree limpo (só
  `Projeto/.idea/` não.tracked, intocado).
- JDK `21.0.13.0.3` (`/usr/lib/jvm/java-21-openjdk` nesta máquina; o caminho
  `java-21-openjdk-amd64` do DEMO.md era da máquina anterior — DEMO.md tornado
  genérico), Maven 3.10.0.
- Clean verify inicial: `cd Projeto && mvn -B -ntp clean verify` →
  **1459/0/0/0, 21 XML, BUILD SUCCESS**
  ([resultado](../../../experiments/checkpoint-phase04/build-start-iteration2/result.json)),
  JAR `dc192971c9ba3a7f66df0e20b3851b4c176c8f4d34158267610b4d7eebab920f`
  (reconstruído idêntico ao existente).

## 2. Negativo (a): cenário2 FAIL na troca por qualidade — reproducción e causa

Run preservada `cenario2/20261122T233636Z-e48c789f` (máquina anterior) e
reproduzido nesta máquina em `cenario2/20261123T112915Z-c8130075` (falha mais
cedo, ver §3). Evidência da run antiga:

- Evento `STARTED SUSTAINED_IMPROVEMENT` (op `05a0c597…`), caminho anterior
  `[S1,R1,R2]` → proposto `[S1,R1,R3,R2]`, custos 609166→57727µs; 61ms depois
  `FAILURE PATH_CHANGED` da mesma operação.
- Wire: `R2→ACTIVATE f9479773 rev0 path=[S1,R1,R3,R2]` (23:36:58.690) rejeitado
  por `R3→ERROR PATH_CHANGED` (23:36:58.727); novas tentativas rev1 rejeitadas,
  rev2 ACKed (23:37:05.449) e renovadas até rev12. Seleção/forwarding final
  via R3 (custo 69692µs), revisão 39 — mas **sem evento SUCCESS**: em
  `RoutingService.confirm` o evento SUCCESS de switch só é emitido quando o
  round que confirma tem `switching != null`; a ronda de switch foi terminada por
  `failedAttempt → fail()` no primeiro PATH_CHANGED (o alvo fica bloqueado com
  `rearm=false`) e a convergência posterior deu-se por rondas de recuperação
  simples, que não emitem evento de switch.
- O caminho anunciado por R2 oscilou entre `[S1,R1,R2]` e `[S1,R1,R3,R2]` a
  partir de 23:36:54.97 (logo após o pulso de 150ms), e o custo medido R2→R1
  chegou a 965620µs com atraso configurado de 120ms/direção — i.e. ~4x o dobro
  do atraso, por fila de serialização do proxy antigo (`Thread.sleep` no ciclo
  de leitura: cada frame esperava pelos anteriores num burst de anúncios).

**Separação defeito-ferramenta vs defeito-sistema:** o comportamento
FAILURE-do-round-e-convergência-sem-SUCCESS é do sistema (JAR, `RoutingService`)
e não foi alterado — está vedado. A oscilação de custos era amplificada por um
artefacto da ferramenta (fila de serialização do proxy). Âmbito de qualquer
correção de produção: nenhuma foi feita nem proposta; se o docente quiser,
o tema é o round de switch terminar no primeiro PATH_CHANGED em vez de
reaproveitar a convergência posterior — fora deste bolt.

**Ajuste justificado da ferramenta (só `demo/`, commit `94300b2`):**
`DelayProxy` passou a agendar cada frame para `chegada + atraso` por direção
(leitor e escritor desacoplados, ordem TCP preservada, bytes inalterados), sem
fila de serialização entre frames. Resultado na sonda
`cenario2/20261123T113410Z-2ccaf425`: custo R2→R1 ~244ms (≈2×120ms), switch
`STARTED→SUCCESS CONFIRMED` em 49.6ms, forwarding final `[S1,R1,R3,R2]`.

## 3. Negativo (b): NO_ROUTE pré-ativação da CLI e trace negativo pós-ACK

- Runs `cenario2/20261122T233117Z-a9871c5f`, `20261122T233325Z-bee35742`: a CLI
  real mostrava `ROUTE … selected=SessionKey[R3…] costMicros=54842` e
  `ROUTE_CANDIDATE … expired=false`, mas `trace` devolveu `TRACE_ERROR NO_ROUTE
  visited=[C2]` sem frame ROUTE_TRACE. No log da CLI vê-se `STREAM_WITHDRAW`
  de ambos os streams imediatamente antes do TRACE_STARTED: ao substituir o
  adaptador pela JVM da CLI, a sessão antiga de C2 morre, R3 retira e reanuncia;
  no instante do trace o `selectedCandidate` estava vazio — resposta NO_ROUTE
  legítima do sistema nesse instante (`forwardTrace`: sem candidato ou sessão
  corrente → NO_ROUTE). **Não é defeito do sistema nem da CLI; é uma corrida de
  prontidão da ferramenta**, que emitia o trace assim que a listagem mostrava
  seleção. A ferramenta preserva esse terminal negativo e exige
  separadamente trace positivo multi-salto após ACK.
- Reprodução nesta máquina: `cenario2/20261123T112915Z-c8130075` falhou em
  `trace-before-SUCCESS` com `NO_ROUTE visited=[C1]` 2.9ms após o ACK —
  candidato momentaneamente vazio por churn de anúncios. Ajuste da
  ferramenta: `trace-before`/`trace-after` passaram a observação com prazo
  (janela de 15s, como todas as outras condições), guardando sempre o último
  terminal no ficheiro de evidência; o requisito de SUCCESS multi-salto não foi
  removido.

## 4. Semântica dos oráculos revista

- Ticket inicial vs forwarding vivo: o resultado terminal de um pedido
  (`NO_ROUTE`/`UNKNOWN_STREAM` inicial) não é o estado vivo da árvore; ambos são
  preservados nos snapshots. A seleção do catálogo não autoriza forwarding.
- Negativo da CLI vs trace positivo multi-salto: são Sonderas distintas; o
  negativo terminal da CLI fica preservado e o trace positivo após ACK é
  exigido com janela própria.
- Atomicidade confirmação/publicação do switch: o oráculo antigo comparava
  `publishedRevision` (versão da árvore/forwarding) com `replacementRevision`
  (revisão do ACTIVATE na nova linha — espaço próprio, 0 numa linha nova), o
  que nunca podia passar. Corrigido para: no evento SUCCESS, a forwarding já
  publicada (observada no instante do evento) mostra o upstream e o caminho de
  substituição com revisão superior à anterior.
- Interrupção/SIGTERM/cleanup: `quit`, EOF e SIGTERM do controlador executam o
  shutdown hook (`cleanup`), que termina apenas processos criados pela
  ferramenta e fecha proxies/log; SIGKILL do próprio controlador não executa
  hooks (documentado em DEMO.md; nunca usar `pkill java`). Todas as runs desta
  iteração terminaram com processos mortos e portas reutilizáveis
  (`result.json` → `processes[].alive=false`, `ports[].reusable=true`).

## 5. Estado após os ajustes

- Sonda `cenario2/20261123T113816Z-6a17543f`: **PASS 37/37** (switch por
  qualidade com SUCCESS e publicação atómica, pulso transitório sem oscilação,
  morte real de R2 com recuperação sem bootstrap, reinício, poda e procura
  abandonada), processos terminados e portas reutilizáveis.
- Série formal de 3 runs consecutivas de cenario1 e 3 de cenario2 sobre a
  versão congelada (`94300b2`): ver `experiments/checkpoint-phase04/series-iteration2.log`
  e runs em `runs/cenario1|2/2026112…`.
- Negativos preservados: todas as runs FAIL anteriores permanecem intactas;
  as novas runs incluem `tool-source/` e `tool-sha256.json`.
