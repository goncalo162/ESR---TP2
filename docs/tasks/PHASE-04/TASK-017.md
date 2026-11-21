# TASK-017 — Adaptar rota por RTT com histerese e troca confirmada

## 1. Identificação

- ID: TASK-017
- Fase: [PHASE-04 — Árvores de distribuição e adaptação](../../phases/PHASE-04-FLOW-MANAGEMENT.md)
- Prioridade: P0
- Estado: IN PROGRESS — Model/Design/ADR aprovados; Implement para revisão
- Responsável: [Fillmore](../../team/FILLMORE.md).
- Revisor: [Waheed](../../team/WAHEED.md), antes de integrar e marcar DONE.
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

## Checkpoint anterior — apenas Model (histórico)

Seleção explícita da opção1 em2026-11-21T14:10:07Z.
[Model](../../../memory-bank/bolts/024-arvores/ddd-01-domain-model.md) e
[Q-C-45/46](../../construction-duvidas.md) propostos para revisão, não aprovados.
A referência legada a NetworkMonitor na secção6 não amplia os três caminhos
do ADD/IMP-014. Sem Java/Maven, Design/ADR/Implement/Test, commits/push ou
outro IMP. V-017/S01–S18 não executados; testes/regressão JDK21, manual humano,
revisão final Waheed e integração pendentes para futuro fecho. Sem gate externo
adicional. Negativos/hashes anteriores preservados; R17 inicial de IMP-013
continua sem causa estabelecida, não explicada pela integração controlada.

## Checkpoint anterior — apenas Design (histórico)

Model/Q-C-45/46 aprovados; registo2026-11-21T14:21:52Z.
[Design para revisão](../../../memory-bank/bolts/024-arvores/ddd-02-technical-design.md)
preparado em2026-11-21T14:29:02Z. V-017/S01–S18 por executar; viabilidade nas
APIs reais por conferir em revisão ADR posteriormente autorizada. Preservados
três caminhos/testes/históricos; sem fontes lidas/analisadas/alteradas, Maven,
ADR/Implement/Test, commits ou push. Falha efectiva ignora histerese/backoff de
qualidade; T/A e reservas físicos partilhados. Manual humano antes de aprovar
Test, regressão JDK21, Waheed e integração obrigatórios, sem gate externo novo.
R17 inicial de IMP-013 continua sem causa estabelecida; integração com métricas
controladas não demonstra a causa desse negativo.

## Checkpoint anterior — revisão ADR e impedimento (histórico)

Design aprovado; registo2026-11-21T14:46:03Z.
[Revisão ADR](../../../memory-bank/bolts/024-arvores/adr-review.md) preparada.
APIs reais suportam o núcleo, com adaptações A01–A07 nos três caminhos;
observabilidade prevista não está ligada na composição actual.
[Q-C-47](../../construction-duvidas.md) apresenta alternativas antes de alterar
contrato/âmbito; nenhuma aprovada/aplicada. Não mudar por inferência o critério
de custos/motivos logados desta tarefa. Sem Implement/Test/Maven, Java alterado,
commits/push ou outro IMP. V-017/S01–S18 não executados, testes/negativos intactos.
Regressão JDK21, manual humano antes da aprovação Test, Waheed e integração
continuam obrigatórios, sem gate externo novo; R17 histórico sem causa estabelecida.

## Checkpoint actual — Implement para revisão

[Walkthrough](../../../memory-bank/bolts/024-arvores/implementation-walkthrough.md), registo2026-11-21T15:36:31Z.
Regressão incremental actual1449/0/0/0 JDK21.0.13.1,21 XML:1419 históricos
intactos+30 novos. Q-C-47-B/ADR aprovadas; Implement em cinco caminhos para revisão,
sem avanço a Test, fecho, commits/push ou outro IMP. Snapshot/construtores/factories
preservados; payload/sink e fila limitada verificados até à saída real. Negativo
S13-transfer4vs5 reproduzido/corrigido no âmbito e preservado; R17 histórico sem
causa estabelecida, não explicada pela integração controlada. V-017 formal,
clean verify/JAR de entrega, regressão aprovada, manual humano antes de aprovar
Test, Waheed e integração pendentes; sem gate externo adicional.
