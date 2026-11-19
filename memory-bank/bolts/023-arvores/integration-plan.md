# IMP-012 / bolt023-arvores — plano final para autorização de integração

**Test e revisão final Denji aprovados. Integração aguarda autorização.**
A confirmação explícita do utilizador abrange a implementação atual, sem alterações
pendentes. [Registo com hashes](../../../experiments/construction/023-arvores/integration-01/review-confirmation.json).
Base/HEAD main/b273b258a4642f94f8614ae00f060a46318d77a5. Sem staging/commit/push/fecho
nesta preparação, sem checkout/reset/restauro ou limpeza global.

## Conteúdo exato e verificações

[Allowlist atualizada](../../../experiments/construction/023-arvores/integration-01/allowlist.txt)
e [manifesto](../../../experiments/construction/023-arvores/integration-01/manifest.json)
incluem a confirmação Denji, resumo/plano, três ficheiros Java, documentação e
evidências de023-arvores: JAR, logs/XML, negativos, roteiros, execuções pessoais
e lista/prova da simulação. São caminhos de ficheiros existentes, não globs.

[Simulação](../../../experiments/construction/023-arvores/integration-01/simulation.json),
[lista completa da árvore simulada](../../../experiments/construction/023-arvores/integration-01/simulation-paths.txt),
[referências](../../../experiments/construction/023-arvores/integration-01/references.json),
[V-ADD simulado](../../../experiments/construction/023-arvores/integration-01/v-add-simulation.md)
e [validação do pacote](../../../experiments/construction/023-arvores/integration-01/package-validation.json).
A cópia usa git archive da base mais allowlist e não cria index/branch/commit.
Confere todos os paths/bytes e base fora da allowlist. Código/JAR iguais aos
aprovados em Test/revisão; nenhum build ou manual repetido.

[Exclusões](../../../experiments/construction/023-arvores/integration-01/excluded-paths.json):
zero target na allowlist e nenhum delta local de target; ficheiros target já
versionados na base ficam inalterados na simulação e no futuro commit.
Não há alterações alheias identificadas para integrar. Classes auxiliares em
experiments são evidência e não alteram a produção/classpath do JAR. Q-C-40–42
mantidas; não há patches de bootstrap/manager ou exceções novas misturadas.

O manifesto lista a própria allowlist e a si mesmo, com exceção explícita do
hash do próprio manifest.json; todos os outros ficheiros têm hash. O hash final
do manifesto é apresentado fora dele, sem ciclo de hashes. Review-01 e demais
provas originais permanecem imutáveis, mesmo quando descrevem estados históricos.

## Ações propostas após autorização explícita — não executadas

1. Conferir novamente main/HEAD, estado, número/caminhos da allowlist e hashes
   contra o manifesto final. Se houver alteração posterior, identificar e preservar
   em vez de incluir/restaurar por inferência. Não repetir manual com mesmo código.
2. Staging explícito apenas dos caminhos autorizados, sem git add -A. Conferir
   staged diff, paths e hashes antes do commit, excluindo target/alterações alheias.
3. Commit local de integração em main, mensagem proposta:
   `feat(IMP-012/TASK-015): integrar renovacao e poda de leases`.
   Sem push, DONE ou fecho documental por esta operação.
4. Extrair o commit para cópia nova, verificar conteúdo/hashes/JAR/allowlist e
   executar V-ADD nessa cópia. Apresentar hash do commit e prova de integração real.
5. Só depois de integração comprovada e autorização de fecho documental, atualizar
   bolt/história/TASK-015. Unidade004/PHASE-04 mantêm-se em progresso enquanto
   houver trabalho pendente; não iniciar outro IMP por inferência.

## Estado final desta preparação

Test1379/0/0/0,19 XML,V-ADD e Francisco30 aprovados, revisão final Denji confirmada
sem alterações pendentes. Falta autorização/execução/comprovação da integração.
Bolt/história/TASK-015/unidade004/PHASE-04 em progresso,sem staging/commits/push/fecho.
Código/JAR/logs/negativos preservados; WRAPPER_EXIT não fornecido/não inventado.
Nenhum gate externo adicional. [Resumo final](final-review-summary.md).

## Fecho documental autorizado — estado atual

IMP-012/023-arvores/TASK-015 concluído após integração local4d18d1d comprovada,
Test1379/0/0/0,19 XML,V-ADD,Francisco30 e revisão final Denji.
[Fecho e evidência](closure-walkthrough.md): bolt/história complete,
implemented=true,TASK-015 DONE. Unidade004/PHASE-04 em progresso.
Texto anterior preserva as etapas/planos históricos; sem Java/push/novo IMP no fecho.
