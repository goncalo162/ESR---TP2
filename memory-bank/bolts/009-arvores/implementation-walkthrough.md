---
stage: implement
bolt: 009-arvores
created: "2026-11-22T16:38:00Z"
updated: "2026-11-22T16:50:00Z"
---
# IMP-015 — Implement executado (demonstração do 2.º marco)

Bolt de demonstração, sem código novo de produção: o deliverable é
`experiments/milestone-2/` (roteiro + auxiliar + wrapper + runs). A lógica
demonstrada (ativação agregada, poda, failover, adaptação por custo) é a dos
bolts 007/023/008/024, já validados; aqui produz-se a prova reproduzível do
marco exigida por V-018.

## Ficheiros criados (todos em `experiments/milestone-2/`)

- [x] `README.md` — roteiro: preparação, 4 cenários com oráculos, limites, limpeza.
- [x] `Milestone2.java` — auxiliar executado em modo fonte contra o JAR
  (`java --class-path <JAR> Milestone2.java <runDir>`); usa só API pública
  (`BootstrapServer`, `NeighborManager`, `RoutingService`, config records) com
  TCP real em loopback, portas efémeras, bootstrap real (ADR-013) e métricas
  de custo controladas via `onMetrics` no fio do owner. Imprime
  `CHECK_PASS`/`CHECK_FAIL` e grava `messages-CN.jsonl` (tap de controlo
  enviado por nó), `snapshots-CN.txt` (routes/forwarding/eventos/switches) e
  `result.json`.
- [x] `run-milestone2.py` — wrapper: cria `runs/<utc>-<rand>/`, grava
  `manifest.json` (commit, SHA-256 do JAR, versão Java, comando), executa o
  auxiliar com saída em `run.log`, escreve `result.json`. Nunca sobrescreve
  runs anteriores.
- [x] `runs/20261122T164428Z-f6n15e/` — run PASS preservada (44/44 checks).

## Base e build de referência

- Base: branch Waheed, HEAD `15c74b1` (git sujo apenas em artefactos não fonte:
  `Projeto/.idea/` não tracked e `Projeto/target/maven-status/.../createdFiles.lst`
  regenerado pelo build). Dependências 022-rotas e 024-arvores `complete`.
- `mvn -B -ntp clean verify` (JDK 21.0.13.0.3, Maven 3.10.0):
  **1459 testes, 0 falhas, 0 erros, 0 skips — BUILD SUCCESS**.
- JAR usado pela demonstração: `Projeto/target/ott-streaming-1.0-SNAPSHOT.jar`,
  517927 bytes, SHA-256
  `dc192971c9ba3a7f66df0e20b3851b4c176c8f4d34158267610b4d7eebab920f`
  (registado em `manifest.json`).

## Execução real — run `20261122T164428Z-f6n15e` (2026-11-22T16:44:28Z)

Comando: `python3 -B experiments/milestone-2/run-milestone2.py --jar
"$PWD/Projeto/target/ott-streaming-1.0-SNAPSHOT.jar" --java java`
→ `WRAPPER_EXIT=0`, `CHECK_PASS=44 CHECK_FAIL=0`, `verdict=PASS`.

| Cenário | Resultado | Evidência principal |
|---|---|---|
| C1 isolamento (S1,S2—R—C1..C4) | 14/14 PASS | 4 subscrições SUCCESS em 2 streams distintos; forwarding de C1 só contém A, de C2 só B; cancelar C1 mantém C3(A)/C2/C4(B); S1 abaixo → A cai em C3 e B preservado em C2/C4 |
| C2 procura agregada (S—R—C1,C2) | 12/12 PASS | **exatamente 1 ACTIVATE R→S** (tap da run aceite: `request=aa310335-7b3d-4667-8a23-731119faedde` único, ACK do S no mesmo requestId, ACTIVATE_ACK a C1 e C2); S vê 1 downstream (R); R vê downstreams=[C1,C2] e upstream=S; sair um não gera novo ACTIVATE; último sair → DEACTIVATE R→S e poda do forwarding |
| C3 falha/recuperação | 10/10 PASS | losango S—R1/R2—C: confirmado via R1; morte abrupta de R1 (disconnect TRANSPORT, sem NODE_LEAVE) → `NeighborDown` e comuta para R2 com ticket SUCCESS; cadeia S—R1—C sem alternativa → forwarding A removido (UNAVAILABLE explícito), router continua RUNNING |
| C4 alteração de RTT | 8/8 PASS | S—A/B—R: confirmado via A (custo 20); 3 amostras novas em B (custo 1) → comuta para B com `SwitchEvent phase=SUCCESS reason=CONFIRMED previousCostMicros=20 replacementCostMicros=11 paths=[S,A,R]→[S,B,R]`; ruído sem melhoria (2 amostras piores em A) → sem oscilação (SUCCESS=1, upstream=B, sem reativação) |

Runs intermédias preservadas: `20261122T163820Z-7as5rr` (39/2 — iteração do
oráculo) e `20261122T164204Z-fjxg3e` (42/1 — ruído com empate reativava via A;
corrigido para custo estritamente pior). A run final é a prova aceite.

## Limites explícitos

- Sem vídeo/GUI/CORE/UDP; `play` continua `NOT_IMPLEMENTED`; os dados são
  mensagens de controlo sintéticas (diagnóstico), como previsto na TASK-018.
- Métricas de custo controladas (RTT real validado em V-010); TCP, protocolo v2
  e lógica de árvore são reais. A "morte" de relay é corte TRANSPORT em JVM
  única, não SIGKILL de processo SO (esse caso foi provado no manual IMP-035).
- G-ASSESSMENTS pendente (data/conteúdo da 2.ª avaliação por confirmar).
- Regressão `mvn -B -ntp test` após o incremento: ver [test-walkthrough](test-walkthrough.md).

## Estado

Etapa implement concluída; `current_stage=implement`. Pendente: etapa test
(V-018 consolidada), revisão/validação e atualização de docs/13–15. Sem
commits/push sem autorização; revisão humana atribuída (Waheed) não presumida.
