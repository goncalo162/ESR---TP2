# TASK-017 — Adaptar rota por RTT com histerese e troca confirmada

## 1. Identificação

- ID: TASK-017
- Fase: [PHASE-04 — Árvores de distribuição e adaptação](../../phases/PHASE-04-FLOW-MANAGEMENT.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-010](../PHASE-03/TASK-010.md), [TASK-016](../PHASE-04/TASK-016.md).
- Requisitos relacionados: [RF-009](../../02-REQUIREMENTS.md#rf-009), [RF-014](../../02-REQUIREMENTS.md#rf-014), [RNF-001](../../02-REQUIREMENTS.md#rnf-001), [RNF-003](../../02-REQUIREMENTS.md#rnf-003).

## 2. Objetivo

Reagir a degradação sustentada sem oscilar entre caminhos.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Dois consumidores partilham ativação, remoção de um preserva o outro, falha troca upstream e segundo marco fica reproduzível. A responsabilidade específica é adaptar rota por rtt com histerese e troca confirmada. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Aplicar melhoria mínima relativa de 20%, três observações e permanência de cinco segundos para trocas por qualidade; falha ignora esses limites. Comparar candidatos contemporâneos e válidos. Numa mudança planeada ativar novo ramo, manter antigo até ACK, trocar snapshot e podar ramo anterior. Se a nova ativação falhar, manter anterior válido e fazer backoff. Custos e razões de troca são logados.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: monitoring/NetworkMonitor.java, routing/RouteMetric.java, RoutingService.java; planeado: routing/RouteSwitchTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Implementar política de histerese configurável com relógio injetado.
- [ ] Coordenar estado SWITCHING com procura e pedidos pendentes.
- [ ] Trocar parent atomicamente e desativar anterior.
- [ ] Publicar métricas de trocas, duração e motivos.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

RTT ruidoso junto do limiar, custo antigo de zero na fonte, child stop durante troca, duas melhorias concorrentes e falha do parent velho antes de ACK novo.

## 10. Testes necessários

V-017: séries temporais predeterminadas não oscilam junto do limiar; melhoria sustentada troca; falha troca sem esperar permanência; troca falhada mantém parent antigo válido.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Política corresponde aos parâmetros documentados.
- [ ] Um upstream é confirmado no snapshot.
- [ ] Tráfego transitório e limites da troca são explicitados.
- [ ] Validação V-017 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
