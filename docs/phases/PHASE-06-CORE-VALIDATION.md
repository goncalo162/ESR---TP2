# PHASE-06 — Integração e avaliação em CORE

## 1. Objetivo

Executar cenários reproduzíveis na rede emulada, comprovar adaptação e quantificar eficiência e limites.

## 2. Contexto

Incremento 6 de sete, segundo [o roadmap](../08-IMPLEMENTATION-ROADMAP.md). O resultado é verificável antes da fase seguinte. Estado inicial: TODO. A fase descreve trabalho futuro; não atesta execução.

## 3. Pré-requisitos

PHASE-05 concluída, com evidência do seu entregável; TASK-024 está DONE. Ler os contratos de [07](../07-API-AND-INTERFACES.md), a [arquitetura](../04-ARCHITECTURE.md) e os gates em [12](../12-RISKS-AND-DECISIONS.md). Dependências específicas estão na tabela e em cada tarefa.

## 4. Requisitos abrangidos

RF-006, RF-007, RF-008, RF-009, RF-011, RF-012, RF-013, RF-014, RNF-001, RNF-002, RNF-003, RNF-004, RNF-005, RC-001, RP-005, RP-008. As descrições e critérios estão em [02](../02-REQUIREMENTS.md); esta fase pode implementar apenas parte de um requisito transversal, como mostra [13](../13-REQUIREMENTS-TRACEABILITY.md).

## 5. Tarefas

| Tarefa | Objetivo concreto | Dependências | Validação | Estado |
|---|---|---|---|---|
| [TASK-025](../tasks/PHASE-06/TASK-025.md) | Preparar CORE e comprovar replicação no cenário de quatro nós | TASK-024 | V-025 | TODO |
| [TASK-026](../tasks/PHASE-06/TASK-026.md) | Demonstrar alternativas e adaptação no cenário de seis nós | TASK-025 | V-026 | TODO |
| [TASK-027](../tasks/PHASE-06/TASK-027.md) | Validar cenário complexo com duas origens e cinco clientes | TASK-026 | V-027 | TODO |
| [TASK-028](../tasks/PHASE-06/TASK-028.md) | Recolher métricas e avaliar resultados e limites | TASK-025, TASK-026, TASK-027 | V-028 | TODO |

## 6. Ordem de execução

TASK-025 → TASK-026 → TASK-027 → TASK-028. Esta ordem é segura; não iniciar a fase seguinte antes de cumprir o gate desta fase. A estrutura permite trabalho independente dentro da fase se as dependências explícitas estiverem concluídas e o progresso ficar coerente.

## 7. Dependências

O gate anterior e as dependências de cada tarefa são cumulativos. A tarefa inicial depende do fecho da fase anterior, direta ou transitivamente. Não há dependências em tarefas ainda não identificadas. Resolver recursos externos apenas no gate documentado, sem inventar resultados.

## 8. Componentes afetados

CORE/container e volumes quando necessário; topologias existentes CORE/topologies/cenario1.xml, cenario2.xml e cenario3.xml; perfis de configuração por cenário e experiments. Variantes do XML apenas por necessidade concreta documentada, preservando os originais. Os caminhos precisos estão nas tarefas e em [05](../05-PROJECT-STRUCTURE.md).

## 9. Entregáveis

Manifestos, logs, capturas e resultados reais dos três cenários; as metas são aprovadas ou limitações registadas. Preservar configurações, comandos e evidência de V-025 a V-028 nas tarefas e no progresso. Artefactos de testes não são prova de submissão ou de fases seguintes.

## 10. Estratégia de testes

Executar as validações da tabela nas tarefas correspondentes e a demonstração integrada da fase. Usar [09](../09-TESTING-STRATEGY.md) para oráculos, ambiente, evidência e casos de erro. Executar testes relevantes durante cada tarefa; a avaliação CORE complementa as verificações anteriores.

## 11. Critérios de aceitação

- [ ] Cada tarefa da tabela está DONE com critérios individuais satisfeitos.
- [ ] Cenários de quatro, seis e overlay complexa têm mapeamentos e execuções reproduzíveis.
- [ ] Capturas provam cópia única no tronco em estado estável; falhas e condições têm resultados reais.
- [ ] O resultado «Manifestos, logs, capturas e resultados reais dos três cenários; as metas são aprovadas ou limitações registadas.» é demonstrável no ambiente da fase.
- [ ] Requisitos cobertos, limitações e recursos pendentes estão coerentes em 13 e 15.
- [ ] Não há regressão nos componentes anteriores afetados pelas alterações.

## 12. Definition of Done

Cumprir [14](../14-DEFINITION-OF-DONE.md), incluindo evidência real e passagem de contexto. Atualizar [15](../15-PROJECT-PROGRESS.md); marcar a fase DONE apenas depois de todas as tarefas e critérios.

## 13. Riscos e observações

Falha de link IP não é automaticamente falha de link overlay; separar recuperação da underlay da adaptação da aplicação. Consultar [12](../12-RISKS-AND-DECISIONS.md) para mitigação e responsável. Metas numéricas do plano são propostas; obrigações do PDF continuam vinculativas.
