# IMP-005 — Implement reapresentado após correção terminal

O registo STOPPED anunciava um código antes de concluir a drenagem/fecho do writer.
Uma falha nessa fase podia devolver 3 depois de anunciar exit=0.

A última mensagem é agora `SHUTDOWN node=... phase=closing-output`: progresso,
sem promessa de código final ou conclusão. Só ExecutionResult/run e o status
usado pelo main constituem resultado terminal, depois dos joins. Não existe
escrita adicional depois de fechar a saída. Budget e algoritmo de cleanup intactos.

Dois testes dirigidos começam com serviço terminado normalmente e falham/bloqueiam
o writer precisamente no registo de cleanup. Verificam saída 3, terminação física,
zero novas escritas depois do fecho e consumo do restante global sem renovar os
3000 ms originais: 1000 no serviço e, no bloqueio, mais 1000 na drenagem antes de
fechar a saída e concluir o join. O relógio falso controla essa contabilização;
a prova anterior com pipes reais continua coberta pela regressão atual.

- [Vermelho](../../../experiments/construction/020-overlay/terminal-fix-red.log): 2 falhas, zero erros; ambos detetam STOPPED/exit=0 prematuro.
- [Verde](../../../experiments/construction/020-overlay/terminal-fix-green.log): 2 testes passaram.
- [Regressão atual](../../../experiments/construction/020-overlay/terminal-fix-regression.log): 981 testes, zero falhas/erros/ignorados, Maven test/saída 0.
- [XML](../../../experiments/construction/020-overlay/terminal-fix-xml/) e [resultados](../../../experiments/construction/020-overlay/terminal-fix-test-results.json).
- [Diff só desta correção](../../../experiments/construction/020-overlay/terminal-fix-only.diff).
- [Diff acumulado contra 90cded6](../../../experiments/construction/020-overlay/terminal-fix-vs-operational-base.diff) e [numstat](../../../experiments/construction/020-overlay/terminal-fix-vs-operational-base.numstat).
- [Hashes da versão](../../../experiments/construction/020-overlay/terminal-fix-source-sha256.json) e [preservação](../../../experiments/construction/020-overlay/terminal-fix-preservation.json).
- [V-ADD](../../../experiments/construction/020-overlay/terminal-fix-v-add.log).

A correção altera apenas OttApplication.java e OttApplicationTest.java dentro dos
cinco ficheiros autorizados. NeighborManager/send(), outros Java, testes históricos,
wire/caps e Q-C-09–12 preservados. Evidência anterior permanece histórica.

Parar em Implement para aprovação; Test não iniciado. Ainda pendentes clean verify,
manual humano, revisão final Waheed e integração. Sem gates externos adicionais,
staging, commits, push ou DONE.
