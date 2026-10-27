# TASK-014 — Ativar árvores com ACK e procura agregada

## 1. Identificação

- ID: TASK-014
- Fase: [PHASE-04 — Árvores de distribuição e adaptação](../../phases/PHASE-04-FLOW-MANAGEMENT.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Fillmore](../../team/FILLMORE.md).
- Revisor: [Waheed](../../team/WAHEED.md), antes de integrar e marcar DONE.
- Dependências: [TASK-011](../PHASE-03/TASK-011.md), [TASK-012](../PHASE-03/TASK-012.md), [TASK-013](../PHASE-03/TASK-013.md).
- Requisitos relacionados: [RF-005](../../02-REQUIREMENTS.md#rf-005), [RF-007](../../02-REQUIREMENTS.md#rf-007), [RF-011](../../02-REQUIREMENTS.md#rf-011), [RP-002](../../02-REQUIREMENTS.md#rp-002), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Confirmar um único ramo a montante para toda a procura do mesmo fluxo num nó.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Dois consumidores partilham ativação, remoção de um preserva o outro, falha troca upstream e segundo marco fica reproduzível. A responsabilidade específica é ativar árvores com ack e procura agregada. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Implementar ACTIVATE/ACK assíncronos, subscriptionId/revision e verificação do prefixo esperado. Registar filho PENDING até ACK e depois ACTIVE; source confirma diretamente. Um ramo já confirmado só agrega novo filho se a epoch e prefixo coincidirem. Consumidores locais são identidades num conjunto. Duplicar pedido não duplica upstream nem downstream. Timeout limpa provisórios e devolve erro correlacionado aos filhos.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: routing/RoutingService.java, Route.java; planeados: routing/Subscription.java e ActivationTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Modelar procura e pedidos pendentes por stream/child.
- [ ] Validar epoch, prefixo, ausência de loops e lease solicitado.
- [ ] Propagar pedido a montante só na primeira procura efetiva.
- [ ] Propagar ACK/ERROR e publicar snapshot ativo atomicamente.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

ACK depois de stop, dois pedidos simultâneos, pedido repetido, ramo já ativo, caminho do filho obsoleto e ACK de revisão anterior. A fonte não cria duas saídas para o mesmo vizinho.

## 10. Testes necessários

V-014: dois clientes em tronco comum geram uma subscrição no tronco; ACK atrasado/cancelado não ativa; prefixo incorreto gera PATH_CHANGED; streams independentes mantêm estados separados.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Árvore ativa apenas após confirmação válida.
- [ ] Uma referência agregada a montante por stream.
- [ ] Repetições são idempotentes e erros não deixam PENDING eterno.
- [ ] Validação V-014 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
