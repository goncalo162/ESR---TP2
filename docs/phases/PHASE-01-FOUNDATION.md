# PHASE-01 — Fundações, configuração e protocolo

## 1. Objetivo

Obter um build Java reproduzível, configuração validada e codec de controlo sem iniciar funcionalidades de streaming.

## 2. Contexto

Incremento 1 de sete, segundo [o roadmap](../08-IMPLEMENTATION-ROADMAP.md). Estado atual: DONE. TASK-001–004 concluídas, com [131 testes no build limpo](../../experiments/task-004-codec-validation.md). O fecho cobre configuração e codec locais; nenhuma etapa de transporte/overlay/streaming é dada como executada por inferência.

## 3. Pré-requisitos

Não requer aplicação funcional; a execução das quatro tarefas e o fecho da fase foram autorizados pelo utilizador. Ler os contratos de [07](../07-API-AND-INTERFACES.md), a [arquitetura](../04-ARCHITECTURE.md) e os gates em [12](../12-RISKS-AND-DECISIONS.md). Dependências específicas estão na tabela e em cada tarefa.

## 4. Requisitos abrangidos

RF-001, RF-004, RF-012, RF-013, RNF-005, RP-001, RP-006. As descrições e critérios estão em [02](../02-REQUIREMENTS.md); esta fase pode implementar apenas parte de um requisito transversal, como mostra [13](../13-REQUIREMENTS-TRACEABILITY.md).

## 5. Tarefas

| Tarefa | Objetivo concreto | Dependências | Validação | Estado |
|---|---|---|---|---|
| [TASK-001](../tasks/PHASE-01/TASK-001.md) | Validar a base Java e configurar testes | nenhuma | V-001 | DONE |
| [TASK-002](../tasks/PHASE-01/TASK-002.md) | Fixar contratos, fixtures e decisões pendentes | TASK-001 | V-002 | DONE |
| [TASK-003](../tasks/PHASE-01/TASK-003.md) | Carregar e validar configurações de nós, grafo e streams | TASK-001, TASK-002 | V-003 | DONE |
| [TASK-004](../tasks/PHASE-01/TASK-004.md) | Codificar e descodificar mensagens de controlo v1 | TASK-002, TASK-003 | V-004 | DONE |

## 6. Ordem de execução

TASK-001 → TASK-002 → TASK-003 → TASK-004. Esta ordem é segura; não iniciar a fase seguinte antes de cumprir o gate desta fase. A estrutura permite trabalho independente dentro da fase se as dependências explícitas estiverem concluídas e o progresso ficar coerente.

## 7. Dependências

O gate anterior e as dependências de cada tarefa são cumulativos. Esta fase fornece a base de configuração e protocolo a todo o projeto. Não há dependências em tarefas ainda não identificadas. Resolver recursos externos apenas no gate documentado, sem inventar resultados.

## 8. Componentes afetados

Build Maven; config; protocol; fixtures de contratos e configuração. Os caminhos precisos e a distinção existente/planeado estão nas tarefas e em [05](../05-PROJECT-STRUCTURE.md).

## 9. Entregáveis

Configuração e mensagens de controlo validadas por testes locais, sem necessidade de CORE. Preservar configurações, comandos e evidência de V-001 a V-004 nas tarefas e no progresso. Artefactos de testes não são prova de submissão ou de fases seguintes.

## 10. Estratégia de testes

Executar as validações da tabela nas tarefas correspondentes e a demonstração integrada da fase. Usar [09](../09-TESTING-STRATEGY.md) para oráculos, ambiente, evidência e casos de erro. Não adiar os unitários desta fase para PHASE-06.

## 11. Critérios de aceitação

- [x] Cada tarefa da tabela está DONE com critérios individuais satisfeitos.
- [x] Configurações válidas/invalidas e paths relativos têm oráculos executados.
- [x] Codec passa fixtures independentes, mensagens concatenadas, leitura parcial e limites.
- [x] O resultado «Configuração e mensagens de controlo validadas por testes locais, sem necessidade de CORE.» é demonstrável no ambiente da fase.
- [x] Requisitos cobertos, limitações e recursos pendentes estão coerentes em 13 e 15.
- [x] Não há regressão nos componentes anteriores afetados pelas alterações.

## 12. Definition of Done

Cumprir [14](../14-DEFINITION-OF-DONE.md), incluindo evidência real e passagem de contexto. Atualizar [15](../15-PROJECT-PROGRESS.md); marcar a fase DONE apenas depois de todas as tarefas e critérios.

## 13. Riscos e observações

Confirmar recursos docentes cedo; o formato do vídeo não deve ser assumido. Consultar [12](../12-RISKS-AND-DECISIONS.md) para mitigação e responsável. Metas numéricas do plano são propostas; obrigações do PDF continuam vinculativas.

### Fecho da fase — 27-10-2026

V-001–004 têm evidência; configuração/codec integram-se em memória e o build executa 131 testes com sucesso. Não foram alterados CORE/topologies ou as fixtures de protocolo. Os requisitos globais permanecem parcialmente cumpridos, com limitações e estados em 13/15. [Notas para o relatório](../PHASE-01-REPORT-NOTES.md). Próxima tarefa recomendada: TASK-005, ainda TODO.
