# IMP-004 / 003-overlay / TASK-007 — fecho documental

**Bolt complete; história complete/implemented=true; TASK-007 DONE.** Fecho
explicitamente autorizado pelo utilizador após Test, manual humano, revisão final
e integração local comprovados. Unidade 002-overlay continua in-progress, com
dois bolts planned. Não foi iniciado outro IMP nem efetuado push.

## Integração e condições cumpridas

Versão revista integrada localmente em main:
`8db7b78950e444f3c2917844617be4b9b236ec0f` (`8db7b78`),
`feat(IMP-004/TASK-007): integrar vizinhanca revista e evidencias`.

[Prova de integração](../../../experiments/construction/003-overlay/integration-commit-proof.json):
145 caminhos exatamente iguais à allowlist, hashes dos blobs iguais aos revistos,
exclusão de target/ e alterações alheias, Q-C-08 limitada a patch/evidência.
[V-ADD sobre o arquivo do commit](../../../experiments/construction/003-overlay/integration-committed-v-add.log):
saída 0 e zero erros. Integração é local; origin/main não foi atualizado por push.

- Test aprovado explicitamente: clean verify de **903 testes**, zero falhas/erros/
  ignorados; [log original](../../../experiments/construction/003-overlay/test-clean-verify-20261110T161706Z.log).
- Manual humano Francisco cumprido e aprovado: **49 CHECK_PASS**, MANUAL_PASS,
  CLEANUP_COMPLETE; Java 0 confirmado pelo utilizador.
  [Log preservado](../../../experiments/construction/003-overlay/manual-Francisco-execucao-01/manual.log)
  e [proveniência](../../../experiments/construction/003-overlay/manual-Francisco-evidence.json).
- Revisão humana final de Denji realizada e aprovada sem alterações pendentes,
  segundo confirmação explícita do utilizador:
  [registo](../../../experiments/construction/003-overlay/denji-final-review-confirmation.json).
- Q-C-08 resolvida após patch mínimo autorizado e V-ADD com zero erros. Validador
  e âmbito intactos; nenhuma ferramenta genérica alheia importada na integração.

O JAR e os hashes continuam os já aprovados; não houve alteração Java ou nova
execução Maven/manual nesta continuação exclusivamente documental. Não se
reutilizam contagens históricas como se fossem uma nova execução.

## Mecanismo specsmd executado

Foi reutilizado o adaptador de ferramenta já validado em Q-C-02, sem instalação
de dependências nem alteração do script ou do runtime do projeto:

```sh
node --require experiments/construction/001-base-validada/bolt-complete-json-frontmatter.cjs \
  .specsmd/aidlc/scripts/bolt-complete.cjs 003-overlay --last-stage test
```

[Entrada/hashes](../../../experiments/construction/003-overlay/closure-entry.json),
[ensaio isolado](../../../experiments/construction/003-overlay/closure-completion-rehearsal.json),
[log do ensaio](../../../experiments/construction/003-overlay/closure-completion-rehearsal.log)
e [execução real](../../../experiments/construction/003-overlay/closure-completion.log).
A saída real foi 0: bolt in-progress→complete, uma história atualizada; unidade
com dois bolts incompletos e intent inalterados. O ensaio alterou exclusivamente
bolt e história; os estados de história não foram fabricados por edição manual.
Timestamps são os gravados pela execução real e pelos registos desta continuação.

## Estado final e âmbito

| Artefacto | Estado |
|---|---|
| IMP-004 / bolt 003-overlay | complete; cinco etapas concluídas; completed preenchido pelo script |
| História de handshake/reconexão | complete; implemented=true |
| TASK-007 | DONE, critérios de aceitação assinalados com evidência |
| Unidade 002-overlay | in-progress; 019-overlay, 002-overlay e 003-overlay complete |
| Bolts restantes da unidade | 020-overlay e 004-overlay planned, sem started ou etapas |
| PHASE-02 / intent / projeto | não promovidos a concluídos |

`020-overlay.blocks` foi atualizado para false porque as dependências terminaram;
essa atualização derivada é necessária para coerência de V-ADD e não inicia
IMP-005. Nenhum serviço/CLI/GUI/CORE/vídeo adicional foi implementado ou alegado.
Os estados e critérios globais não são satisfeitos automaticamente pelo fecho
deste incremento.

## Validação e commit documental

[V-ADD do fecho](../../../experiments/construction/003-overlay/closure-v-add.log)
e [verificação dos estados/hashes](../../../experiments/construction/003-overlay/closure-validation.json).
O [diff/numstat de fecho](../../../experiments/construction/003-overlay/closure-diff.numstat)
e a [allowlist documental](../../../experiments/construction/003-overlay/closure-file-list.txt)
separam o fecho da implementação já integrada. O commit de fecho é posterior a
8db7b78 e contém apenas documentação/evidência, sem alterações Java, target/,
ferramentas locais alheias, push ou início de outro IMP.
