# IMP-007 — integração local comprovada, fecho pendente

Commit: `f9905038c614612b6f2798d4eca46dd72934743f` em main, pai `3dedf007f52f43f53eaf254bef7c25e35f986e5a`.
Árvore `c8976e7287a0185b601806313a18c4884d7d54ee` igual à árvore staged conferida antes do commit.
Mensagem: TASK-010/IMP-007: integrar medição RTT e deteção de falhas TCP.

- Exatamente348 caminhos alterados, iguais à allowlist autorizada.
- Todos os bytes staged/commit iguais aos hashes conferidos do pacote revisto,
  incluindo o manifesto: `9d9e7e256138a77da5fe29f4d9bc6708c9ec1babf4f144a7ae42d420910bb5c7`.
- Código/JAR iguais aos aprovados em Test; Q-C-25 só os dois patches autorizados.
- Nenhuma alteração de target/ ou alheia no commit. Entradas target históricas
  da base não foram apagadas; outputs locais não selecionados mantêm os bytes.
- git diff --cached --check de fontes/documentos:0. Check bruto: avisos em
  evidência original, registados sem normalizar logs/XML/diffs ou alterar hashes.
- Cópia extraída **do commit efetivo**, via git archive, com3880 ficheiros;
  sem overlay do working tree. Bytes selecionados conferidos e cópia inalterada
  pela validação. V-ADD:exit0/zero erros, validador original.

[Prova estruturada](integration-proof.json), [validação staged](integration-proof/staged-verification.json),
[diff staged completo](integration-proof/staged-full.diff), [numstat](integration-proof/staged.numstat),
[verificação do commit](integration-proof/commit-verification.json),
[V-ADD na cópia extraída](integration-proof/commit-v-add.log).

Esta prova é posterior ao commit e fica **não staged/não commitada**, para ser
incluída na autorização futura de fecho documental. Não foi alterado o payload
revisto para inserir retroativamente um hash de commit que ainda não existia.
Índice vazio; mantêm-se os dois outputs target locais e esta evidência nova.
Bolt/história/TASK/unidade não marcados DONE/complete. Test/manual/revisão final
já cumpridos; integração agora comprovada. Falta o fecho documental autorizado.
Sem push, fecho ou outro IMP. Nenhum build/manual foi repetido sem necessidade.
