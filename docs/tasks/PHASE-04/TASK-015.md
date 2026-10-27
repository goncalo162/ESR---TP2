# TASK-015 — Podar ramos, renovar leases e remover procura abandonada

## 1. Identificação

- ID: TASK-015
- Fase: [PHASE-04 — Árvores de distribuição e adaptação](../../phases/PHASE-04-FLOW-MANAGEMENT.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Fillmore](../../team/FILLMORE.md).
- Revisor: [Denji](../../team/DENJI.md), antes de integrar e marcar DONE.
- Dependências: [TASK-010](../PHASE-03/TASK-010.md), [TASK-014](../PHASE-04/TASK-014.md).
- Requisitos relacionados: [RF-002](../../02-REQUIREMENTS.md#rf-002), [RF-005](../../02-REQUIREMENTS.md#rf-005), [RF-007](../../02-REQUIREMENTS.md#rf-007), [RF-009](../../02-REQUIREMENTS.md#rf-009), [RF-011](../../02-REQUIREMENTS.md#rf-011), [RNF-003](../../02-REQUIREMENTS.md#rnf-003), [RP-002](../../02-REQUIREMENTS.md#rp-002).

## 2. Objetivo

Retirar procura sem interromper consumidores restantes nem deixar ramos fantasma.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Dois consumidores partilham ativação, remoção de um preserva o outro, falha troca upstream e segundo marco fica reproduzível. A responsabilidade específica é podar ramos, renovar leases e remover procura abandonada. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Implementar DEACTIVATE, SUBSCRIPTION_RENEW e expiração local. Cada nó renova a montante enquanto houver intenção local ou downstream válido. Perder child remove só esse child; se não houver procura, podar upstream e limpar estado. Cancelamento inclui ID/revisão para não atingir ativação nova. Callbacks de conexão antiga não removem child da nova conexão.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: routing/RoutingService.java, overlay/NeighborManager.java; planeados: routing/SubscriptionLeaseTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Implementar remoção idempotente e recomputação de procura agregada.
- [ ] Renovar leases confirmados e verificar prefixo/epoch.
- [ ] Expirar filhos sem renovação e limpar pendentes.
- [ ] Integrar eventos de saída e desconexão com poda.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Cliente crasha sem LEAVE, stop repetido, cancelamento atrasado, renovação após expiração e duas instâncias sequenciais do mesmo nodeId.

## 10. Testes necessários

V-015: sair um de dois consumidores mantém tronco ativo; sair o último remove; relógio falso elimina lease órfão; cancelamento velho não remove ramo novo.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Vídeo só permanece autorizado enquanto há procura efetiva.
- [ ] Ramos órfãos desaparecem por timeout.
- [ ] Outros consumidores e fluxos mantêm-se ativos.
- [ ] Validação V-015 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
