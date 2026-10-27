# TASK-010 — Medir RTT e publicar falhas de vizinhança

## 1. Identificação

- ID: TASK-010
- Fase: [PHASE-03 — Monitorização e descoberta de rotas](../../phases/PHASE-03-ROUTING.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Waheed](../../team/WAHEED.md).
- Revisor: [Fillmore](../../team/FILLMORE.md), antes de integrar e marcar DONE.
- Dependências: [TASK-007](../PHASE-02/TASK-007.md), [TASK-008](../PHASE-02/TASK-008.md), [TASK-009](../PHASE-02/TASK-009.md).
- Requisitos relacionados: [RF-003](../../02-REQUIREMENTS.md#rf-003), [RF-008](../../02-REQUIREMENTS.md#rf-008), [RF-014](../../02-REQUIREMENTS.md#rf-014), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Produzir uma métrica medida e detetar peers sem resposta.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Rotas para várias streams, alternativas sem ciclos e trace até à origem. A responsabilidade específica é medir rtt e publicar falhas de vizinhança. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Sondas PROBE_PING/PONG usam probeId e tempo monotónico guardado no emissor. Manter uma sonda pendente por vizinho, ignorar respostas não correlacionadas e atualizar EWMA com alpha=0,2. Após três timeouts consecutivos publicar falha e fechar a conexão afetada; EOF é falha imediata. Reiniciar métricas quando muda instanceId. Injetar scheduler/relógio nos testes. RTT TCP mede ida/volta do controlo, não atraso de vídeo unidirecional, perda UDP ou capacidade de banda.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: monitoring/LinkMetrics.java e NetworkMonitor.java; planeados: monitoring/NetworkMonitorTest.java e abstração TimeSource.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Correlacionar PING/PONG e validar IDs de sonda.
- [ ] Calcular RTT/EWMA com unidades explícitas em microssegundos.
- [ ] Implementar timeout, missedCount e evento neighborDown.
- [ ] Expor snapshot com idade e número de amostras.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

PONG duplicado, atrasado, de instância antiga, relógio civil corrigido, RTT zero por resolução e peer sem amostra. Usar custo mínimo positivo de 1 us; candidato sem amostra fica pendente até medir.

## 10. Testes necessários

V-010: relógio falso confirma EWMA, reset após nova instância, perda de três sondas e ausência de falso positivo por um timeout; EOF invalida imediatamente.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] RTT é calculado sem comparar relógios de máquinas diferentes.
- [ ] Falhas são correlacionadas à conexão correta.
- [ ] Métrica sem amostra não é publicada como zero confiável.
- [ ] Validação V-010 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
