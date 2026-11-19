# IMP-012 — pacote para revisão final Denji

**Test aprovado pelo utilizador. Denji e integração ainda pendentes.**
[Resumo final](../../../../memory-bank/bolts/023-arvores/final-review-summary.md),
[plano](../../../../memory-bank/bolts/023-arvores/integration-plan.md).

- [Allowlist exata](allowlist.txt): **271 ficheiros existentes**, incluindo
  3 Java, 22 documentos e 246 artefactos de evidência/pacote.
- [Manifesto de hashes](manifest.json); manifesto lista a si mesmo sem hash próprio.
- [Diff acumulado](cumulative.diff), [numstat](numstat.txt), [diff Java](java.diff), [numstat Java](java.numstat).
  Diff textual cobre código/documentação/receitas; artefactos de execução estão
  individualmente enumerados e hashados no manifesto, sem duplicar binários/XML.
- [Exclusões](excluded-paths.json): todo target,nenhuma alteração alheia identificada.
- [Preservação](preservation.json), [validação do pacote](package-validation.json),
  [V-ADD](v-add-worktree.md).

Clean verify1379/0/0/0,19 XML,V-015/L01–L24 e Francisco30 aprovados; mesmas fontes,
JAR e logs. Todos os negativos/evidências anteriores preservados. WRAPPER_EXIT
não fornecido e não inferido. Sem repetição Maven/Java nesta preparação documental.
Registos antigos de checkpoint são snapshots históricos, não estado atual.

Sem revisão presumida/contactos, staging/commits/push/fecho/outro IMP.
Bolt/história/TASK-015/unidade004/PHASE-04 continuam em progresso. Após revisão
confirmada, atualizar confirmação/manifesto e pedir autorização de integração.
Nenhum gate externo adicional.
