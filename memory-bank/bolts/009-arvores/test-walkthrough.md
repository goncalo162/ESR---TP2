---
stage: test
bolt: 009-arvores
created: "2026-11-22T16:55:00Z"
---
# IMP-015 — Test / V-018 consolidado

V-018: *reprodução independente da ativação/poda/failover usando logs e
snapshots; revisão do material contra os critérios de gestão de fluxos e
controlo do PDF.* [docs/09 §V-018](../../../docs/09-TESTING-STRATEGY.md) ·
[TASK-018](../../../docs/tasks/PHASE-04/TASK-018.md) ·
[ADD §IMP-015](../../../add.md#imp-015).

## Build e regressão

- `mvn -B -ntp clean verify` (JDK 21.0.13.0.3, Maven 3.10.0, 2026-11-22T16:30Z):
  **1459 testes, 0 falhas, 0 erros, 0 skips — BUILD SUCCESS**.
  JAR `dc192971c9ba3a7f66df0e20b3851b4c176c8f4d34158267610b4d7eebab920f` (517927 bytes).
- `mvn -B -ntp test` após o incremento (2026-11-22T16:47:24Z): **1459/0/0/0 —
  BUILD SUCCESS**. Nenhuma fonte de produção foi alterada neste bolt; a
  regressão confirma a base intacta.

## Execução da demonstração (prova aceite)

Run `experiments/milestone-2/runs/20261122T164428Z-f6n15e/`
(`result.json`: exit 0, checks_pass 44, checks_fail 0, verdict PASS;
`manifest.json` com commit `15c74b1`, branch Waheed, hash do JAR e comandos).

| Critério V-018 / TASK-018 | Resultado | Evidência |
|---|---|---|
| Isolamento entre fluxos | PASS (C1, 14 checks) | 2 servidores (A,B), 4 consumidores; forwarding de cada consumidor contém só o seu stream; cancelar C1 não afeta C3(A)/C2/C4(B); S1 abaixo → A indisponível em C3, B preservado — `snapshots-C1*.txt` |
| Procura agregada | PASS (C2, 12 checks) | 1 ACTIVATE R→S no tap (`messages-C2.jsonl`, requestId único `aa310335-…`) com 2 consumidores; ACK do S no mesmo request; R: downstreams=[C1,C2], upstream=S; último consumidor sai → DEACTIVATE R→S e poda — RF-011 |
| Falha e recuperação | PASS (C3, 10 checks) | morte abrupta de R1 (TRANSPORT, sem NODE_LEAVE) → NeighborDown + comuta R1→R2 com ticket SUCCESS; sem alternativa → forwarding removido (explícito), router RUNNING — `snapshots-C3*.txt` |
| Alteração de RTT | PASS (C4, 8 checks) | custo sustentado melhor em B → `SwitchEvent SUCCESS reason=CONFIRMED previousCostMicros=20 replacementCostMicros=11 [S,A,R]→[S,B,R]`; ruído sem melhoria → sem oscilação (1 SUCCESS, sem reativação) — `snapshots-C4.txt` |
| Registo do conteúdo/estado do marco | PASS | roteiro `README.md`, manifest por run, logs/snapshots/taps preservados |

Reprodução por terceiro: o roteiro em `experiments/milestone-2/README.md`
descreve pré-requisitos (JDK 21, Python 3.9+), o comando exato e os oráculos;
qualquer operador pode repetir com `run-milestone2.py` e comparar os
`CHECK_PASS`/`result.json`. Runs anteriores (`…-7as5rr`, `…-fjxg3e`) ficam
preservadas como histórico de iterações do próprio oráculo.

## Revisão contra os critérios do PDF (gestão de fluxos e controlo)

- Ativação agregada no tronco sem cópias redundantes: satisfeito (C2, 1 ACTIVATE
  no tronco com 2 consumidores) — RF-011/RF-007 ao nível de controlo.
- Poda preserva consumidores restantes e remove o ramo ao sair o último:
  satisfeito (C1, C2) — RF-005/RF-009.
- Falha de upstream comuta para alternativa sem ciclo e sem intervenção humana;
  sem alternativa, indisponibilidade explícita: satisfeito (C3) — RF-006/RNF-003.
- Adaptação por custo com histerese/dwell e troca confirmada, sem oscilação por
  ruído: satisfeito (C4) — RF-009/RF-014.
- Isolamento entre streams/origens: satisfeito (C1).
- **Não alegado:** vídeo, GUI, CORE, datagramas UDP, `play` (NOT_IMPLEMENTED),
  SIGKILL de processo SO, datas de avaliação (G-ASSESSMENTS pendente).

## Limites registados

- Métricas de custo controladas (RTT real validado em V-010); TCP/protocolo/lógica
  de árvore reais. Morte de relay = corte TRANSPORT em JVM única.
- Sem vídeo: os "dados" são mensagens de controlo sintéticas, como a TASK-018
  prevê para este marco.

## Estado atual reconciliado

Estado reconciliado em 2026-11-22T23:10:04Z: **024-arvores/IMP-014 e 009-arvores/IMP-015 complete; TASK-017/018 DONE**, com V-017/S01–S18 e V-018 executadas. Manual humano e Test aprovados; revisão final de Waheed das duas versões confirmada pelo utilizador. Integração em main comprovada pelos PRs #5 (ce76726) e #6 (1ee8db6). Regressão atual JDK21: **1459/0/0/0, 21 XML**. PHASE-04/unidade004 **DONE/complete**, com coordenação de Fillmore e revisão dos três membros confirmadas pelo utilizador. Total: **17 bolts complete, 0 in-progress, 18 planned**; nenhum novo IMP iniciado. Negativos e hashes preservados. R17 inicial continua sem causa estabelecida; a integração com métricas controladas não demonstra a sua causa. G-ASSESSMENTS mantém a pendência de informação docente sobre data/conteúdo; o marco técnico concluído não prova avaliação ou submissão externa. [Evidência e proveniência das confirmações](../../../experiments/construction/024-arvores/closure-reconciliation-01/README.md).

V-018: run aceite 20261122T164428Z-f6n15e e repetição independente 20261122T165306Z-2hy8eu, ambas 44/44. As runs e os manifestos originais são preservados.
