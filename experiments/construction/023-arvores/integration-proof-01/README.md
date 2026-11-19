# IMP-012 / TASK-015 — integração local comprovada

Commit local em main: `4d18d1dd8fb50d14ace02609a9133733c20cabfc`.
Parent: `b273b258a4642f94f8614ae00f060a46318d77a5`.
Mensagem: `feat(IMP-012/TASK-015): integrar renovacao e poda de leases`.

- [Pré-commit](precommit.json): HEAD/main,290 caminhos,manifesto aprovado/hashes,
  target e alterações alheias excluídos; index inicial vazio.
- [Staged diff](staged.diff), [numstat](staged.numstat), [paths](staged-name-status.txt)
  e [verificação staged](staged-verification.json):290 paths/hashes/modos exatos.
- [Diagnóstico Git completo](staged-diff-check-original.txt):422 espaços finais
  em logs Maven/diffs brutos e2 linhas finais nos TXT negativos originais,
  preservados byte a byte conforme o manifesto. Código/documentação atual sem
  avisos; não se alterou evidência,validador,Git settings ou testes para obter verde.
- [Prova de integração](integration-proof.json), [hashes efetivamente integrados](committed-file-hashes.json),
  [paths da cópia extraída](extracted-tree-paths.txt), [commit](commit-show.txt).
- [V-ADD da cópia extraída](v-add-extracted-commit.md):0 erros.

Git archive do commit produziu6253 ficheiros: base mais os290 paths aprovados.
Todos os hashes/modos coincidem;264 classes do JAR e19 XML/1379/0/0/0 preservados.
JAR SHA256 `e2a41113b8dbb4d3d68024f546bb570e7509fdd47a2d09366c4b96ef907a1188`.
Manual Francisco30 e revisão Denji vinculados à mesma versão; não repetidos.
Nenhuma alteração de target/ficheiros alheios no commit; nove target da base
inalterados e delta local Maven preservado. Q-C-40–42/negativos/originais intactos.

Index vazio após commit. [Estado local](postcommit-status.txt): delta target já
existente e esta prova nova não staged. Evidência pós-commit fica fora dos290
paths aprovados,aguardando eventual autorização de fecho documental para integrar.
Sem push,DONE,fecho documental ou outro IMP. Bolt/história/TASK-015/unidade004/
PHASE-04 permanecem em progresso. Esta prova não altera os snapshots históricos
do pacote aprovado,onde a integração ainda aparecia como pendente.
