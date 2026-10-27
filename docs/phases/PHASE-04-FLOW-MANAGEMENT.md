# PHASE-04 — Árvores de distribuição e adaptação

## 1. Objetivo

Ativar e podar árvores por procura, recuperar de falhas e mudar de rota com histerese.

## 2. Contexto

Incremento 4 de sete, segundo [o roadmap](../08-IMPLEMENTATION-ROADMAP.md). O resultado é verificável antes da fase seguinte. Estado inicial: TODO. A fase descreve trabalho futuro; não atesta execução.

## 3. Pré-requisitos

PHASE-03 concluída, com evidência do seu entregável; TASK-013 está DONE. Ler os contratos de [07](../07-API-AND-INTERFACES.md), a [arquitetura](../04-ARCHITECTURE.md) e os gates em [12](../12-RISKS-AND-DECISIONS.md). Dependências específicas estão na tabela e em cada tarefa.

## 4. Requisitos abrangidos

RF-002, RF-005, RF-006, RF-007, RF-009, RF-011, RF-014, RNF-001, RNF-003, RNF-005, RC-007, RP-002, RP-006. As descrições e critérios estão em [02](../02-REQUIREMENTS.md); esta fase pode implementar apenas parte de um requisito transversal, como mostra [13](../13-REQUIREMENTS-TRACEABILITY.md).

## 5. Tarefas

| Tarefa | Objetivo concreto | Dependências | Validação | Estado |
|---|---|---|---|---|
| [TASK-014](../tasks/PHASE-04/TASK-014.md) | Ativar árvores com ACK e procura agregada | TASK-011, TASK-012, TASK-013 | V-014 | TODO |
| [TASK-015](../tasks/PHASE-04/TASK-015.md) | Podar ramos, renovar leases e remover procura abandonada | TASK-010, TASK-014 | V-015 | TODO |
| [TASK-016](../tasks/PHASE-04/TASK-016.md) | Recuperar árvores após falha de upstream | TASK-012, TASK-014, TASK-015 | V-016 | TODO |
| [TASK-017](../tasks/PHASE-04/TASK-017.md) | Adaptar rota por RTT com histerese e troca confirmada | TASK-010, TASK-016 | V-017 | TODO |
| [TASK-018](../tasks/PHASE-04/TASK-018.md) | Preparar o segundo marco com árvores e adaptação | TASK-009, TASK-013, TASK-015, TASK-016, TASK-017 | V-018 | TODO |

## 6. Ordem de execução

TASK-014 → TASK-015 → TASK-016 → TASK-017 → TASK-018. Esta ordem é segura; não iniciar a fase seguinte antes de cumprir o gate desta fase. A estrutura permite trabalho independente dentro da fase se as dependências explícitas estiverem concluídas e o progresso ficar coerente.

## 7. Dependências

O gate anterior e as dependências de cada tarefa são cumulativos. A tarefa inicial depende do fecho da fase anterior, direta ou transitivamente. Não há dependências em tarefas ainda não identificadas. Resolver recursos externos apenas no gate documentado, sem inventar resultados.

## 8. Componentes afetados

routing, subscrições e snapshots; integração com eventos de overlay/monitoring; segundo marco. Os caminhos precisos e a distinção existente/planeado estão nas tarefas e em [05](../05-PROJECT-STRUCTURE.md).

## 9. Entregáveis

Dois consumidores partilham ativação, remoção de um preserva o outro, falha troca upstream e segundo marco fica reproduzível. Preservar configurações, comandos e evidência de V-014 a V-018 nas tarefas e no progresso. Artefactos de testes não são prova de submissão ou de fases seguintes.

## 10. Estratégia de testes

Executar as validações da tabela nas tarefas correspondentes e a demonstração integrada da fase. Usar [09](../09-TESTING-STRATEGY.md) para oráculos, ambiente, evidência e casos de erro. Não adiar os unitários desta fase para PHASE-06.

## 11. Critérios de aceitação

- [ ] Cada tarefa da tabela está DONE com critérios individuais satisfeitos.
- [ ] Dois consumidores partilham uma subscrição no tronco e a última saída poda o ramo.
- [ ] Falha e mudança sustentada de RTT recuperam ou trocam parent com ACK, sem ciclo persistente.
- [ ] O resultado «Dois consumidores partilham ativação, remoção de um preserva o outro, falha troca upstream e segundo marco fica reproduzível.» é demonstrável no ambiente da fase.
- [ ] Requisitos cobertos, limitações e recursos pendentes estão coerentes em 13 e 15.
- [ ] Não há regressão nos componentes anteriores afetados pelas alterações.

## 12. Definition of Done

Coordenação do fecho: [Fillmore](../team/FILLMORE.md), com revisão dos três membros segundo a [coordenação da equipa](../team/00-COORDINATION.md). Autor e revisor de cada TASK constam na identificação; a atribuição não altera dependências ou estados.

Cumprir [14](../14-DEFINITION-OF-DONE.md), incluindo evidência real e passagem de contexto. Atualizar [15](../15-PROJECT-PROGRESS.md); marcar a fase DONE apenas depois de todas as tarefas e critérios.

## 13. Riscos e observações

ACK atrasado, cancelamento antigo e caminhos desatualizados não podem ressuscitar subscrições ou formar ciclos. Consultar [12](../12-RISKS-AND-DECISIONS.md) para mitigação e responsável. Metas numéricas do plano são propostas; obrigações do PDF continuam vinculativas.
