# IMP-013 — revisão ADR estática

O utilizador aprovou Design revisto e clarificação Q-C-44; autorizou apenas ADR.
[Entrada/autorização](entry.json), [Design de entrada preservado](design-input.md)
e [revisão para aprovação](../../../../memory-bank/bolts/008-arvores/adr-review.md).

As APIs reais suportam a entrega nos três caminhos do ADD. A01–A08 identificam
adaptações futuras de prova G/watermark, intenção, época, revisões/interesse filho,
projeção, contexto físico e cleanup. Nenhuma foi implementada. Não se identificou
impedimento de ampliação; não se cria ADR nova nem altera decisão aceite.
[Inspeção estática e hashes](api-inspection.json) identifica os pontos revistos.
Receção validada é observada no owner; não existe timestamp de socket/remoto no
evento Received e não se alega tal informação.

O Snapshot de entrada preserva exatamente o Design aprovado antes de acrescentar
a nota documental de aprovação. Model-01/design-01/design-02 não são reescritos:
18 ficheiros conferidos contra a entrada. O hash histórico do artefacto vivo em
manifestos antigos representa os bytes àquela data, preservados nos snapshots;
não é uma obrigação de manter o ficheiro vivo sem atualização documental.

Verificação documental: executar `python3 scripts/validate-planning.py --report
experiments/construction/008-arvores/adr-01/v-add.md` e `git diff --check`.
Fontes/recursos de Projeto/CORE conferidos por SHA-256 e diff contra a entrada.
Resultados de âmbito em status-and-scope.json; saída do diff em diff-check.log.
Nenhum Maven ou teste Java executado. V-016/R01–R18 NÃO EXECUTADOS.

Só Model/Design aprovados/concluídos; current_stage=adr, revisão por aceitar,
completed=null. Sem Implement/Test, fecho, staging, commits, push ou outro IMP.
Regressão JDK21, manual humano, revisão final Fillmore e integração continuam
obrigatórios para fecho; sem gates externos adicionais. Negativos antigos intactos.
