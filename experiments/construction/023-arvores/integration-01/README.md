# IMP-012 — revisão final Denji aprovada e plano final de integração

Confirmação explícita do utilizador: Denji reviu e aprovou a implementação atual,
sem alterações pendentes. [Confirmação/hashes](review-confirmation.json),
[resumo final](../../../../memory-bank/bolts/023-arvores/final-review-summary.md),
[plano final](../../../../memory-bank/bolts/023-arvores/integration-plan.md).

- [Allowlist exata](allowlist.txt):290 ficheiros,3 Java,22 documentos,
  265 artefactos de evidência/pacote; inclui esta confirmação/simulação.
- [Manifesto](manifest.json), [exclusões](excluded-paths.json), [preservação](preservation.json).
- [Diff acumulado](cumulative.diff), [numstat](numstat.txt), [diff Java](java.diff), [numstat Java](java.numstat).
- [Simulação](simulation.json), [árvore de paths simulada](simulation-paths.txt),
  [base extraída](base-files.json), [referências](references.json),
  [conteúdo estável validado](validated-inputs.json).
- [V-ADD local](v-add-worktree.md), [V-ADD simulado](v-add-simulation.md),
  [validação final do pacote](package-validation.json).

Test1379/0/0/0,19 XML,V-ADD e Francisco30 cumpridos; mesmos fontes/JAR/logs e
negativos. Não se repetiu build/manual nem se inventou WRAPPER_EXIT.
Review-01 e outras evidências anteriores ficam imutáveis como snapshots históricos.
A simulação não é integração real nem staging: usa git archive da base e overlay
somente da allowlist numa cópia nova. Target local e alterações alheias excluídos;
9 ficheiros target já versionados ficam iguais à base e não são incluídos na allowlist.
O manifesto lista a si mesmo sem hash próprio; restantes paths têm hash.

Integração aguarda autorização; sem staging/commits/push/fecho/outro IMP.
Bolt/história/TASK/unidade004/PHASE-04 em progresso; nenhum gate externo adicional.
