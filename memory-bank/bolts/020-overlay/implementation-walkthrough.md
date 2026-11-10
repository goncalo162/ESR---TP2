---
stage: implement
bolt: 020-overlay
created: "2026-11-10T18:15:17Z"
---
# IMP-005 — Implement aprovado

## Summary

Composição CLI dos quatro papéis e ciclo de vida do nó implementados nos cinco
ficheiros autorizados. Saída best effort usa sessões confirmadas e orçamento único;
I/O e callbacks têm limites e observação explícita da terminação física.
Implement aprovado pelo utilizador, com registo em 2026-11-10T22:05:26Z. Consultar o [relatório de Test](test-walkthrough.md).

## Structure Overview

A aplicação valida argumentos/configuração e compõe apenas o serviço do papel.
OverlayNode possui o executor serial emprestado ao NeighborManager. Drenagem e
fecho imediato são estados distintos: bloqueiam novo trabalho sem destruir antes
do flush os canais usados para notificar a saída. A consola corre fora do owner,
com filas limitadas e canais de descritor canceláveis no processo CLI.

## Completed Work

- [x] `Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java` — parser,
  composição, comandos, logs, streams e shutdown coordenado.
- [x] `Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java` — um owner
  por nó, manager embutido, falhas e terminação do owner emprestado.
- [x] `Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java` — extensão
  lifecycle autorizada por Q-C-09, sem alterar envio de aplicação.
- [x] `Projeto/src/test/java/pt/uminho/esr/ott/app/OttApplicationTest.java` — 46 testes,
  incluindo JVMs distintas, pipes reais e prova de joins anterior à saída da JVM.
- [x] `Projeto/src/test/java/pt/uminho/esr/ott/overlay/NeighborManagerLifecycleTest.java`
  — 32 testes, relógio/transporte falsos, máximo de 63 peers e NODE_LEAVE em TCP real.

## Key Decisions

Q-C-09–12 mantidas: extensão mínima de lifecycle, metade do restante reservada ao
cleanup, gramática CLI estrita e buffers locais aprovados. O relatório de aviso
confirma escrita local, sem ACK remoto; a terminação continua a exigir espera física.
Fecho a partir do owner só solicita paragem, sem esperar pela própria thread.
O fecho posterior ao aviso conserva o orçamento originalmente oferecido.

## Deviations from Plan

Nenhum alargamento de ficheiros, dependências, wire ou caps. Os tipos e métodos
pendentes do checkpoint anterior foram concretizados. Uma vista interna dos
resultados de cleanup permite provar joins antes de sair da JVM, sem acrescentar
um comando ou formato de rede. O descritor bootstrap conserva identidade wire
interna não exposta, conforme Plan; a CLI não inventa um UUID para esse serviço.

## Dependencies Added

Nenhuma. JUnit existente apenas em testes; sem linter, CI ou cobertura percentual.

## Validation and Evidence

Regressão atual de Implement: 981 testes, zero falhas/erros/ignorados, saída 0,
com Maven test e Java 21. São 903 históricos preservados + 78 novos, incluindo
subprocessos reais dos quatro papéis e TCP local. Não é clean verify nem Test aprovado.

- [Regressão verificada](../../../experiments/construction/020-overlay/terminal-fix-regression.log)
- [Resultados/XML](../../../experiments/construction/020-overlay/terminal-fix-test-results.json)
- [Oráculos L01–L12/M01–M18](implement-oracles.md)
- [Justificação das substituições no manager](neighbor-manager-adaptations.md)
- [Roteiro manual humano](../../../experiments/construction/020-overlay/manual-instructions.md)
- [Diff acumulado](../../../experiments/construction/020-overlay/implement-vs-operational-base.diff)
- [Numstat](../../../experiments/construction/020-overlay/implement-vs-operational-base.numstat)
- [Preservação e hashes](../../../experiments/construction/020-overlay/implement-preservation.json)
- [V-ADD desta etapa](../../../experiments/construction/020-overlay/implement-v-add.log)

## Developer Notes

Logs preliminares preservam falhas reais e respetivas correções: cleanup do stdin
do harness após processo morto e observação de evento explicitamente sincronizada
no teste do owner. A versão final passa sem editar testes históricos. As execuções manuais e o
clean verify estão registados no [relatório de Test](test-walkthrough.md). Test foi aprovado. A revisão
final de Waheed e a integração da versão revista estão pendentes.

## Correção terminal — Implement reapresentado

Registada em 2026-11-10T18:39:44Z. A última mensagem é SHUTDOWN com phase=closing-output,
sem código final. ExecutionResult/status do processo só são definidos depois
do fecho/join do writer; não se escreve novamente depois de fechar a saída.
Dois testes novos reproduzem falha/bloqueio no cleanup, verificam saída 3,
join físico, ausência de anúncio prematuro e orçamento global sem renovação.
Vermelho: 2 falhas; verde: 2 sucessos; regressão atual: 981/zero falhas/erros/ignorados.
[Revisão da correção](terminal-fix-review.md). Test continua não iniciado;
clean verify/manual humano/revisão Waheed/integração pendentes.
