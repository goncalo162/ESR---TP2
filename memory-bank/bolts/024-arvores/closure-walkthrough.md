---
stage: closure
bolt: 024-arvores
completed: "2026-11-22T17:47:01Z"
---
# IMP-014 / 024-arvores / TASK-017 — Fecho

Fecho em **2026-11-22T17:47:01Z**: revisão confirmada pelo utilizador ("Já
revi") após a etapa Test com JAR novo e validação independente PASS.

## Evidência

- Clean verify com JAR novo: `mvn -B -ntp clean verify` → exit 0,
  **1459/0/0/0**, 21 XML, 2026-11-22T17:35:12Z.
  [Log](../../../experiments/construction/024-arvores/test-01/clean-verify.log) ·
  [resultado](../../../experiments/construction/024-arvores/test-01/clean-verify-result.json).
- JAR congelado: SHA-256 `dc192971c9ba3a7f66df0e20b3851b4c176c8f4d34158267610b4d7eebab920f`,
  517927 bytes ([jar.sha256](../../../experiments/construction/024-arvores/test-01/jar.sha256)).
- V-017: S01–S18 = 34 métodos nos 18 grupos PASS + 6 de compatibilidade
  (40 testcases `RouteSwitchTest`, 0 failures/errors).
  [Oráculos](../../../experiments/construction/024-arvores/test-01/v017-oracles.json) ·
  [Relatório de Test](ddd-03-test-report.md).
- Validador independente (`validador-v017`): **PASS global**, com reexecução
  própria `mvn -B -ntp test` (1459/0/0/0) e conferência do JAR/XML.

## Estado declarado no fecho original (histórico)

- TASK-017 **DONE**; bolt 024-arvores **complete**; história 004
  **complete/implemented=true**; V-017 **EXECUTADA com sucesso** (docs/09/13/15).
- PHASE-04: todas as tarefas (TASK-014–018) DONE; fase pendente apenas de
  fecho de coordenação (§12 do ficheiro da fase).
- R17 histórico continua sem causa estabelecida (limitação documentada).
- Sem commits/push nesta sessão; integração em main pendente de autorização
  explícita.

## Estado atual após reconciliação

Estado reconciliado em 2026-11-22T23:10:04Z: **024-arvores/IMP-014 e 009-arvores/IMP-015 complete; TASK-017/018 DONE**, com V-017/S01–S18 e V-018 executadas. Manual humano e Test aprovados; revisão final de Waheed das duas versões confirmada pelo utilizador. Integração em main comprovada pelos PRs #5 (ce76726) e #6 (1ee8db6). Regressão atual JDK21: **1459/0/0/0, 21 XML**. PHASE-04/unidade004 **DONE/complete**, com coordenação de Fillmore e revisão dos três membros confirmadas pelo utilizador. Total: **17 bolts complete, 0 in-progress, 18 planned**; nenhum novo IMP iniciado. Negativos e hashes preservados. R17 inicial continua sem causa estabelecida; a integração com métricas controladas não demonstra a sua causa. G-ASSESSMENTS mantém a pendência de informação docente sobre data/conteúdo; o marco técnico concluído não prova avaliação ou submissão externa. [Evidência e proveniência das confirmações](../../../experiments/construction/024-arvores/closure-reconciliation-01/README.md).

A revisão de Waheed é comprovada pela confirmação explícita do utilizador, não por approvals no GitHub (listas de reviews vazias). O manual humano foi confirmado como concluído; operador e run não foram indicados. Os timestamps originais dos dois fechos não provam a ordem das dependências; as condições finais são ratificadas apenas agora, depois de ambas as integrações.
