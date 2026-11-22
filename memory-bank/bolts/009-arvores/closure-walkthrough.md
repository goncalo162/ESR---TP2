---
stage: closure
bolt: 009-arvores
completed: "2026-11-22T17:12:18Z"
---
# IMP-015 / 009-arvores / TASK-018 — Fecho

Fecho registado em **2026-11-22T17:12:18Z**. O utilizador autorizou
explicitamente o DONE após verificação ("está verificado mete como done"),
apoiado no **PASS global do validador-bolt independente** (que reexecutou a
demonstração numa run nova, `20261122T165306Z-2hy8eu`, 44/44, e conferiu hash
do JAR, commit e manifesto).

## Evidência que sustenta o fecho

- Run aceite: `experiments/milestone-2/runs/20261122T164428Z-f6n15e/` —
  `result.json` exit 0, **44 CHECK_PASS / 0 CHECK_FAIL**, verdict PASS.
  - C1 isolamento entre fluxos (2 servidores, 4 consumidores): 14 checks.
  - C2 procura agregada (1 ACTIVATE R→S com 2 consumidores, poda ao sair o
    último): 12 checks — RF-011.
  - C3 falha/recuperação (morte abrupta de relay → comuta para alternativa;
    sem alternativa → UNAVAILABLE explícito): 10 checks.
  - C4 alteração de RTT/custo (`SwitchEvent SUCCESS reason=CONFIRMED 20→11 µs,
    [S,A,R]→[S,B,R]`; ruído sem melhoria não comuta): 8 checks.
- Regressão: `mvn -B -ntp clean verify` e `mvn -B -ntp test` →
  **1459 testes, 0 falhas/erros/skips**, JDK 21.0.13.0.3.
- JAR: `dc192971c9ba3a7f66df0e20b3851b4c176c8f4d34158267610b4d7eebab920f`
  (517927 bytes), registado no `manifest.json` da run.
- [Plan](implementation-plan.md) · [Implement](implementation-walkthrough.md) ·
  [Test](test-walkthrough.md) · [Roteiro](../../../experiments/milestone-2/README.md).

## Âmbito respeitado

Bolt de demonstração, sem código de produção novo: apenas
`experiments/milestone-2/` e documentação de estado. Sem vídeo/GUI/CORE/UDP;
`play` continua NOT_IMPLEMENTED; métricas de custo controladas (RTT real em
V-010); morte de relay = corte TRANSPORT em JVM única. G-ASSESSMENTS pendente
(data/conteúdo da 2.ª avaliação por informação docente).

## Estado após o fecho

- TASK-018 **DONE**; bolt 009-arvores **complete**; história 005
  **complete/implemented=true**; V-018 **EXECUTADA com sucesso** (docs/09,
  docs/13, docs/15 atualizados).
- PHASE-04 permanece IN PROGRESS (TASK-017/024-arvores ainda em curso).
- Sem commits/push nesta sessão — a integração em main requer autorização
  explícita própria, como nos fechos anteriores.
