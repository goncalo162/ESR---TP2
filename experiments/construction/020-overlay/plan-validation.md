# Checkpoint Plan — apenas documentação

Model aceite por confirmação do utilizador. Plan preparado, por aprovar.
Q-C-09 resolvida quanto à decisão autorizada de âmbito; implementação e prova pendentes.

- Base operacional/HEAD: `90cded6b8d321cfcbe369dd4f8dbed9a01ddf430`.
- Referência histórica: `2ea748efd61ba5bdea355d2b5a02de6f6bd9a8c5`.
- V-ADD: `python3 scripts/validate-planning.py`, saída 0, zero erros; ver plan-v-add.log.
- O campo Base 85e1b33 no log é a referência interna histórica do validador,
  preservado sem alterações; não identifica o HEAD deste trabalho.
- Projeto/ incluindo target, ferramentas e fecho de IMP-004 preservados por hashes/diff.
- Alterações anteriores do Model preservadas; só documentação deste incremento foi acrescentada/atualizada.
- Índice vazio; sem Java, Maven, subprocessos de serviço, commits ou push.
- Diff/numstat cumulativos do Model+Plan contra base operacional e referência histórica
  guardados nos ficheiros plan-vs-*.diff / plan-vs-*.numstat; incluem os dois documentos novos sem staging.
- Evidência desta etapa: plan-entry.json, plan-validation.json, plan-v-add.log e diffs.
  Logs model-* são do checkpoint anterior; não são resultado do Plan ou V-008.

Ainda exigidos: aprovação de Plan, interfaces/testes primeiro, Implement, Test
com V-008 real/regressão/manual humano, revisão final de Waheed e integração.
Sem gates externos adicionais. Bolt aberto e unidade em progresso.
