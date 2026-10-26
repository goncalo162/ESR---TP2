# PHASE-05 — Vídeo, replicação e reprodução

## 1. Objetivo

Entregar vídeo MJPEG fragmentado a clientes, com quatro streams distintas em duas origens.

## 2. Contexto

Incremento 5 de sete, segundo [o roadmap](../08-IMPLEMENTATION-ROADMAP.md). O resultado é verificável antes da fase seguinte. Estado inicial: TODO. A fase descreve trabalho futuro; não atesta execução.

## 3. Pré-requisitos

PHASE-04 concluída, com evidência do seu entregável; TASK-018 está DONE. Ler os contratos de [07](../07-API-AND-INTERFACES.md), a [arquitetura](../04-ARCHITECTURE.md) e os gates em [12](../12-RISKS-AND-DECISIONS.md). Dependências específicas estão na tabela e em cada tarefa.

## 4. Requisitos abrangidos

RF-007, RF-010, RF-011, RF-012, RF-013, RF-015, RNF-001, RNF-002, RNF-003, RP-001, RP-003, RP-006, RP-008. As descrições e critérios estão em [02](../02-REQUIREMENTS.md); esta fase pode implementar apenas parte de um requisito transversal, como mostra [13](../13-REQUIREMENTS-TRACEABILITY.md).

## 5. Tarefas

| Tarefa | Objetivo concreto | Dependências | Validação | Estado |
|---|---|---|---|---|
| [TASK-019](../tasks/PHASE-05/TASK-019.md) | Definir codec UDP e fragmentação de frames | TASK-002, TASK-004, TASK-011, TASK-018 | V-019 | TODO |
| [TASK-020](../tasks/PHASE-05/TASK-020.md) | Ler fontes MJPEG e controlar ritmo de publicação | TASK-002, TASK-003, TASK-019 | V-020 | TODO |
| [TASK-021](../tasks/PHASE-05/TASK-021.md) | Reencaminhar e replicar datagramas pela árvore ativa | TASK-014, TASK-015, TASK-016, TASK-017, TASK-019 | V-021 | TODO |
| [TASK-022](../tasks/PHASE-05/TASK-022.md) | Reconstituir frames com buffers e prazo limitados | TASK-019, TASK-021 | V-022 | TODO |
| [TASK-023](../tasks/PHASE-05/TASK-023.md) | Apresentar catálogo e vídeo numa janela Swing | TASK-012, TASK-022 | V-023 | TODO |
| [TASK-024](../tasks/PHASE-05/TASK-024.md) | Integrar duas origens e quatro streams ponta a ponta | TASK-008, TASK-018, TASK-020, TASK-021, TASK-022, TASK-023 | V-024 | TODO |

## 6. Ordem de execução

TASK-019 → TASK-020 → TASK-021 → TASK-022 → TASK-023 → TASK-024. Esta ordem é segura; não iniciar a fase seguinte antes de cumprir o gate desta fase. A estrutura permite trabalho independente dentro da fase se as dependências explícitas estiverem concluídas e o progresso ficar coerente.

## 7. Dependências

O gate anterior e as dependências de cada tarefa são cumulativos. A tarefa inicial depende do fecho da fase anterior, direta ou transitivamente. Não há dependências em tarefas ainda não identificadas. Resolver recursos externos apenas no gate documentado, sem inventar resultados.

## 8. Componentes afetados

streaming, transporte UDP, server, client, app e perfis de conteúdos por origem. Os caminhos precisos e a distinção existente/planeado estão nas tarefas e em [05](../05-PROJECT-STRUCTURE.md).

## 9. Entregáveis

Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa. Preservar configurações, comandos e evidência de V-019 a V-024 nas tarefas e no progresso. Artefactos de testes não são prova de submissão ou de fases seguintes.

## 10. Estratégia de testes

Executar as validações da tabela nas tarefas correspondentes e a demonstração integrada da fase. Usar [09](../09-TESTING-STRATEGY.md) para oráculos, ambiente, evidência e casos de erro. Não adiar os unitários desta fase para PHASE-06.

## 11. Critérios de aceitação

- [ ] Cada tarefa da tabela está DONE com critérios individuais satisfeitos.
- [ ] Duas origens oferecem dois vídeos distintos cada, identificados e reproduzidos por UUID.
- [ ] Fragmentação, reassemblagem, descarte de incompletos e replicação passam testes locais e GUI real.
- [ ] O resultado «Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa.» é demonstrável no ambiente da fase.
- [ ] Requisitos cobertos, limitações e recursos pendentes estão coerentes em 13 e 15.
- [ ] Não há regressão nos componentes anteriores afetados pelas alterações.

## 12. Definition of Done

Cumprir [14](../14-DEFINITION-OF-DONE.md), incluindo evidência real e passagem de contexto. Atualizar [15](../15-PROJECT-PROGRESS.md); marcar a fase DONE apenas depois de todas as tarefas e critérios.

## 13. Riscos e observações

Recurso MJPEG não confirmado, frames maiores que MTU, perda UDP e GUI headless devem ter tratamento explícito. Consultar [12](../12-RISKS-AND-DECISIONS.md) para mitigação e responsável. Metas numéricas do plano são propostas; obrigações do PDF continuam vinculativas.
