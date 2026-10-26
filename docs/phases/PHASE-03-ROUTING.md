# PHASE-03 — Monitorização e descoberta de rotas

## 1. Objetivo

Construir catálogo e candidatos por fluxo usando RTT medido e comprovar percursos sem vídeo.

## 2. Contexto

Incremento 3 de sete, segundo [o roadmap](../08-IMPLEMENTATION-ROADMAP.md). O resultado é verificável antes da fase seguinte. Estado inicial: TODO. A fase descreve trabalho futuro; não atesta execução.

## 3. Pré-requisitos

PHASE-02 concluída, com evidência do seu entregável; TASK-009 está DONE. Ler os contratos de [07](../07-API-AND-INTERFACES.md), a [arquitetura](../04-ARCHITECTURE.md) e os gates em [12](../12-RISKS-AND-DECISIONS.md). Dependências específicas estão na tabela e em cada tarefa.

## 4. Requisitos abrangidos

RF-003, RF-005, RF-006, RF-008, RF-009, RF-010, RF-012, RF-014, RP-001, RP-004, RP-006, RP-008. As descrições e critérios estão em [02](../02-REQUIREMENTS.md); esta fase pode implementar apenas parte de um requisito transversal, como mostra [13](../13-REQUIREMENTS-TRACEABILITY.md).

## 5. Tarefas

| Tarefa | Objetivo concreto | Dependências | Validação | Estado |
|---|---|---|---|---|
| [TASK-010](../tasks/PHASE-03/TASK-010.md) | Medir RTT e publicar falhas de vizinhança | TASK-007, TASK-008, TASK-009 | V-010 | TODO |
| [TASK-011](../tasks/PHASE-03/TASK-011.md) | Modelar candidatos, custos e snapshots por stream | TASK-003, TASK-010 | V-011 | TODO |
| [TASK-012](../tasks/PHASE-03/TASK-012.md) | Difundir anúncios e construir catálogo e rotas | TASK-007, TASK-010, TASK-011 | V-012 | TODO |
| [TASK-013](../tasks/PHASE-03/TASK-013.md) | Diagnosticar percursos pela tabela de rotas | TASK-008, TASK-012 | V-013 | TODO |

## 6. Ordem de execução

TASK-010 → TASK-011 → TASK-012 → TASK-013. Esta ordem é segura; não iniciar a fase seguinte antes de cumprir o gate desta fase. A estrutura permite trabalho independente dentro da fase se as dependências explícitas estiverem concluídas e o progresso ficar coerente.

## 7. Dependências

O gate anterior e as dependências de cada tarefa são cumulativos. A tarefa inicial depende do fecho da fase anterior, direta ou transitivamente. Não há dependências em tarefas ainda não identificadas. Resolver recursos externos apenas no gate documentado, sem inventar resultados.

## 8. Componentes afetados

monitoring; routing; catálogo de streaming; comandos de diagnóstico em app. Os caminhos precisos e a distinção existente/planeado estão nas tarefas e em [05](../05-PROJECT-STRUCTURE.md).

## 9. Entregáveis

Rotas para várias streams, alternativas sem ciclos e trace até à origem. Preservar configurações, comandos e evidência de V-010 a V-013 nas tarefas e no progresso. Artefactos de testes não são prova de submissão ou de fases seguintes.

## 10. Estratégia de testes

Executar as validações da tabela nas tarefas correspondentes e a demonstração integrada da fase. Usar [09](../09-TESTING-STRATEGY.md) para oráculos, ambiente, evidência e casos de erro. Não adiar os unitários desta fase para PHASE-06.

## 11. Critérios de aceitação

- [ ] Cada tarefa da tabela está DONE com critérios individuais satisfeitos.
- [ ] Quatro streams são descobertas com rotas e alternativas, incluindo melhor anúncio que chega depois.
- [ ] Trace termina na origem esperada e ciclos/expirações não causam flooding ilimitado.
- [ ] O resultado «Rotas para várias streams, alternativas sem ciclos e trace até à origem.» é demonstrável no ambiente da fase.
- [ ] Requisitos cobertos, limitações e recursos pendentes estão coerentes em 13 e 15.
- [ ] Não há regressão nos componentes anteriores afetados pelas alterações.

## 12. Definition of Done

Cumprir [14](../14-DEFINITION-OF-DONE.md), incluindo evidência real e passagem de contexto. Atualizar [15](../15-PROJECT-PROGRESS.md); marcar a fase DONE apenas depois de todas as tarefas e critérios.

## 13. Riscos e observações

Não suprimir melhorias de anúncio por uma cache que só considera a primeira chegada. Consultar [12](../12-RISKS-AND-DECISIONS.md) para mitigação e responsável. Metas numéricas do plano são propostas; obrigações do PDF continuam vinculativas.
