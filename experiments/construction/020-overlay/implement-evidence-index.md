# IMP-005 — pacote do checkpoint Implement

Base operacional/HEAD: 90cded6b8d321cfcbe369dd4f8dbed9a01ddf430.
Referência histórica: 2ea748efd61ba5bdea355d2b5a02de6f6bd9a8c5.
Etapa atual Implement, por aprovar; Test não iniciado. Índice sem staging.

- implement-regression-verified.log: execução final Maven test, saída 0; 979 testes,
  zero falhas/erros/ignorados. implement-test-results.json e implement-xml/ copiam
  os resultados exatos dessa execução.
- implement-source-sha256.json: hashes dos cinco ficheiros de código dessa versão.
- implement-verified-subprocesses/: perfis, logs, exits e prova de joins antes
  de System.exit; implement-subprocess-results.json indexa os resultados.
- implement-v-add.log: zero erros; o campo Base 85e1b33 é a referência interna
  histórica do validador preservado, não o HEAD operacional deste incremento.
- implement-code.diff / implement-code.numstat: os cinco ficheiros, +1294/−14,
  contra 90cded6, incluindo os dois testes novos sem staging.
- implement-vs-operational-base.diff / .numstat: acumulado Model/Plan/Implement
  de código/documentação, excluindo target e ferramentas/alheios.
- implement-preservation.json: provas de send(), testes/fixtures, Q-C-05/06,
  fecho de IMP-004, HEAD/índice e âmbito; listas tracked geradas de compilação
  repostas aos bytes registados de entrada, sem tocar nos testes/fontes anteriores.
- implement-environment.log: versões reais de Java/Maven.
- manual-instructions.md: roteiro preparado; manual humano ainda não executado.

Logs anteriores não são apagados nem contados como sucesso: implement-expanded-01
teve um erro de fecho do stdin do harness após a JVM já terminar;
implement-regression.log teve uma falha na precondição de observação do owner.
Esses testes novos foram corrigidos com cleanup adequado e sincronização explícita;
nenhum teste histórico foi alterado ou enfraquecido. As duas regressões posteriores
passaram (978 e, após o último caso de callback, 979); prevalece o log verified.

Nenhum clean verify/JAR novo, Test aprovado, manual humano, revisão de Waheed,
integração, commit/push ou DONE é alegado. Não há gate externo adicional.

## Versão corrente após correção terminal

Prevalece agora terminal-fix-regression.log: 981 testes, zero falhas/erros/ignorados.
Os artefactos implement-* acima preservam o checkpoint anterior de 979 testes.
Consultar terminal-fix-test-results.json, terminal-fix-xml/, terminal-fix-subprocesses/,
terminal-fix-preservation.json, terminal-fix-source-sha256.json e terminal-fix-*.diff.
A nova revisão continua no checkpoint Implement, sem Test/fecho.
