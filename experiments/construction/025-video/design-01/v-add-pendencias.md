# IMP-018 — seis pendências V-ADD antes do fecho

**Pendentes, validação não aprovada.** Obrigação interna de qualidade
confirmada pelo utilizador ao aprovar Model/LOCAL-01/02; não é gate externo
adicional e não autoriza alterações fora do âmbito ou reposição da demo.

Origem: [relatório do Model](../model-01/v-add.md), seis erros preservados.

| ID local | Problema registado | Estado |
|---|---|---|
| VADD-01 | Fence sem fecho em `.specsmd/aidlc/skills/inception/bolt-plan.md` | Pendente |
| VADD-02 | Fence sem fecho em `.specsmd/aidlc/skills/operations/deploy.md` | Pendente |
| VADD-03 | Ligação no progresso para `memory-bank/demonstrations/DEMO-001-checkpoint-phase04/bolt.md`, ausente | Pendente |
| VADD-04 | Ligação no progresso para `demo/DEMO.md`, ausente | Pendente |
| VADD-05 | Ligação no progresso para `demo/MATRIX.md`, ausente | Pendente |
| VADD-06 | Ligação no progresso para `memory-bank/demonstrations/DEMO-001-checkpoint-phase04/HANDOFF.md`, ausente | Pendente |

Não foi aplicado patch a templates, demonstração ou referências históricas.
Os ficheiros da demo continuam ausentes, como na entrada desta etapa. Não se
declara a auditoria extraída da parte A (que tinha0 erros no seu contexto)
como validação desta árvore de trabalho.

Antes do fecho: apresentar resolução concreta que respeite as remoções,
aplicar apenas com âmbito autorizado e repetir V-ADD com resultado novo.
Não remover critérios, ocultar caminhos do validador ou enfraquecer a
verificação para declarar PASS. Aprovar Design não resolve estas pendências.
