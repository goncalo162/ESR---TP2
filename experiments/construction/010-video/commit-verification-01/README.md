# IMP-016 — Prova do commit local, sem integração em main

Commit:72c9f926cea697c439fc1d1b398a348a1662d032, ramo imp-016-udp.
Pai e main inalterado:b6de8488f6dc99e3bf490d4cb88b31840089cb30.
208 caminhos exatos da allowlist aprovada; blobs e cópia extraída coincidem por
SHA-256 com o manifesto25b70988a04ffb6545d06838e447063b4a9791cb0c9c1f584974cc43fb8ce33f.

[Prova completa](proof.json), [verificação do staging](staged-verification.json),
[caminhos cometidos](committed-paths.txt), [numstat](committed.numstat),
[metadados do commit](commit-metadata.txt), [staged diff preservado](staged.diff).
[V-ADD na cópia extraída](v-add-extracted-commit.md):exit0,zero erros.
JAR/código/classes conferidos,380 ficheiros protegidos da demo intactos.
Patch template exatamente a linha autorizada IMP-016-LOCAL-02.
Sem alterações de target/ ou demo/ no commit. O target histórico herdado da base
não foi alterado; saídas locais geradas permanecem fora do commit.

[Whitespace](staged-checks.json): git diff --cached --check detetou espaços em
logs/diffs de evidência originais aprovados. Preservados byte a byte; check de
código/documentação passou. Não houve divergência do manifesto nem alteração do validador.

Cópia temporária extraída removida. Build/manual não repetidos.
Main não integrado,sem push,DONE ou fecho documental. Bolt permanece aberto.
Esta pasta de prova pós-commit permanece local/não cometida; nenhum amend ou
commit adicional foi feito. Index vazio. Alterações restantes:target/ gerado e
esta pasta de prova. Integração em main depende de autorização posterior.
