# PHASE-07 — Relatório, empacotamento e defesa

## 1. Objetivo

Produzir artigo até 12 páginas, arquivo reproduzível e materiais de defesa e submissão.

## 2. Contexto

Incremento 7 de sete, segundo [o roadmap](../08-IMPLEMENTATION-ROADMAP.md). O resultado é verificável antes da fase seguinte. Estado inicial: TODO. A fase descreve trabalho futuro; não atesta execução.

## 3. Pré-requisitos

PHASE-06 concluída, com evidência do seu entregável; TASK-028 está DONE. Ler os contratos de [07](../07-API-AND-INTERFACES.md), a [arquitetura](../04-ARCHITECTURE.md) e os gates em [12](../12-RISKS-AND-DECISIONS.md). Dependências específicas estão na tabela e em cada tarefa.

## 4. Requisitos abrangidos

RNF-005, RC-002, RC-003, RC-004, RC-005, RC-006, RC-007, RP-007, RP-008. As descrições e critérios estão em [02](../02-REQUIREMENTS.md); esta fase pode implementar apenas parte de um requisito transversal, como mostra [13](../13-REQUIREMENTS-TRACEABILITY.md).

## 5. Tarefas

| Tarefa | Objetivo concreto | Dependências | Validação | Estado |
|---|---|---|---|---|
| [TASK-029](../tasks/PHASE-07/TASK-029.md) | Escrever artigo com protocolos e avaliação crítica | TASK-028 | V-029 | TODO |
| [TASK-030](../tasks/PHASE-07/TASK-030.md) | Empacotar código e verificar reprodução da entrega | TASK-029 | V-030 | TODO |
| [TASK-031](../tasks/PHASE-07/TASK-031.md) | Preparar defesa e checklist de submissão humana | TASK-009, TASK-018, TASK-030 | V-031 | TODO |

## 6. Ordem de execução

TASK-029 → TASK-030 → TASK-031. Esta ordem é segura; não iniciar a fase seguinte antes de cumprir o gate desta fase. A estrutura permite trabalho independente dentro da fase se as dependências explícitas estiverem concluídas e o progresso ficar coerente.

## 7. Dependências

O gate anterior e as dependências de cada tarefa são cumulativos. A tarefa inicial depende do fecho da fase anterior, direta ou transitivamente. Não há dependências em tarefas ainda não identificadas. Resolver recursos externos apenas no gate documentado, sem inventar resultados.

## 8. Componentes afetados

report; delivery; README de operação; checklist da equipa e confirmação manual no Blackboard. Os caminhos precisos e a distinção existente/planeado estão nas tarefas e em [05](../05-PROJECT-STRUCTURE.md).

## 9. Entregáveis

PDF e ZIP com nomes corretos, reprodução após extração limpa e checklist de submissão com confirmação humana. Preservar configurações, comandos e evidência de V-029 a V-031 nas tarefas e no progresso. Artefactos de testes não são prova de submissão ou de fases seguintes.

## 10. Estratégia de testes

Executar as validações da tabela nas tarefas correspondentes e a demonstração integrada da fase. Usar [09](../09-TESTING-STRATEGY.md) para oráculos, ambiente, evidência e casos de erro. Executar testes relevantes durante cada tarefa; a avaliação CORE complementa as verificações anteriores.

## 11. Critérios de aceitação

- [ ] Cada tarefa da tabela está DONE com critérios individuais satisfeitos.
- [ ] PDF tem até 12 páginas e nomes reais; ZIP extraído limpo permite repetir build/testes.
- [ ] Defesa está preparada e a equipa confirma submissão no grupo Blackboard correto.
- [ ] O resultado «PDF e ZIP com nomes corretos, reprodução após extração limpa e checklist de submissão com confirmação humana.» é demonstrável no ambiente da fase.
- [ ] Requisitos cobertos, limitações e recursos pendentes estão coerentes em 13 e 15.
- [ ] Não há regressão nos componentes anteriores afetados pelas alterações.

## 12. Definition of Done

Cumprir [14](../14-DEFINITION-OF-DONE.md), incluindo evidência real e passagem de contexto. Atualizar [15](../15-PROJECT-PROGRESS.md); marcar a fase DONE apenas depois de todas as tarefas e critérios.

## 13. Riscos e observações

Turno, grupo, datas e acesso Blackboard não foram fornecidos; são gates da entrega, não motivo para inventar valores. Consultar [12](../12-RISKS-AND-DECISIONS.md) para mitigação e responsável. Metas numéricas do plano são propostas; obrigações do PDF continuam vinculativas.
