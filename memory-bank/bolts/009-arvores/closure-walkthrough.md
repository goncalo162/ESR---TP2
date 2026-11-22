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

## Estado declarado no fecho original (histórico)

- TASK-018 **DONE**; bolt 009-arvores **complete**; história 005
  **complete/implemented=true**; V-018 **EXECUTADA com sucesso** (docs/09,
  docs/13, docs/15 atualizados).
- PHASE-04 permanece IN PROGRESS (TASK-017/024-arvores ainda em curso).
- Sem commits/push nesta sessão — a integração em main requer autorização
  explícita própria, como nos fechos anteriores.

## Estado atual após reconciliação

Estado reconciliado em 2026-11-22T23:10:04Z: **024-arvores/IMP-014 e 009-arvores/IMP-015 complete; TASK-017/018 DONE**, com V-017/S01–S18 e V-018 executadas. Manual humano e Test aprovados; revisão final de Waheed das duas versões confirmada pelo utilizador. Integração em main comprovada pelos PRs #5 (ce76726) e #6 (1ee8db6). Regressão atual JDK21: **1459/0/0/0, 21 XML**. PHASE-04/unidade004 **DONE/complete**, com coordenação de Fillmore e revisão dos três membros confirmadas pelo utilizador. Total: **17 bolts complete, 0 in-progress, 18 planned**; nenhum novo IMP iniciado. Negativos e hashes preservados. R17 inicial continua sem causa estabelecida; a integração com métricas controladas não demonstra a sua causa. G-ASSESSMENTS mantém a pendência de informação docente sobre data/conteúdo; o marco técnico concluído não prova avaliação ou submissão externa. [Evidência e proveniência das confirmações](../../../experiments/construction/024-arvores/closure-reconciliation-01/README.md).

A revisão de Waheed é comprovada pela confirmação explícita do utilizador, não por approvals no GitHub (listas de reviews vazias). O manual humano foi confirmado como concluído; operador e run não foram indicados. Os timestamps originais dos dois fechos não provam a ordem das dependências; as condições finais são ratificadas apenas agora, depois de ambas as integrações.
