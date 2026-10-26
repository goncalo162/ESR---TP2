# TASK-011 — Modelar candidatos, custos e snapshots por stream

## 1. Identificação

- ID: TASK-011
- Fase: [PHASE-03 — Monitorização e descoberta de rotas](../../phases/PHASE-03-ROUTING.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-003](../PHASE-01/TASK-003.md), [TASK-010](../PHASE-03/TASK-010.md).
- Requisitos relacionados: [RF-005](../../02-REQUIREMENTS.md#rf-005), [RF-006](../../02-REQUIREMENTS.md#rf-006), [RF-014](../../02-REQUIREMENTS.md#rf-014), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Manter seleção e alternativas isoladas por fluxo, com custos determinísticos.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Rotas para várias streams, alternativas sem ciclos e trace até à origem. A responsabilidade específica é modelar candidatos, custos e snapshots por stream. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Implementar dados de docs/06 e seleção por soma de RTT, saltos e nodeId, nessa ordem. Candidatos guardam path completo, epoch, sequência e validade. Atualizações são proprietárias do executor serial; snapshots de leitura são imutáveis. Configurar fonte local com custo zero. Não ativar dados ou subscrições nesta tarefa.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: routing/Route.java, RouteState.java, RouteMetric.java, RoutingTable.java; planeados: RouteCandidate.java e routing/RoutingTableTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Criar modelos tipados e validar invariantes.
- [ ] Implementar insert/update/expire/remove por candidato.
- [ ] Selecionar melhor candidato e manter alternativas válidas.
- [ ] Produzir snapshot de custo, origem, destinos vazios e estado INACTIVE/UNAVAILABLE.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Soma acima de Long.MAX_VALUE, custo negativo, mesmo UUID com origem conflitante, empate, path circular, alternativa expirada e retirada de melhor candidato.

## 10. Testes necessários

V-011: custos e desempate calculados com fixtures de resultados conhecidos; quatro streams não interferem; alternativa é preservada e snapshot não pode ser alterado por consumidor.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Cada fluxo tem custo, origem, destinos e estado.
- [ ] Empates são reproduzíveis e caminhos inválidos rejeitados.
- [ ] Expirar um candidato não apaga alternativas válidas.
- [ ] Validação V-011 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
