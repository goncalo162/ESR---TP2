# TASK-014 — Ativar árvores com ACK e procura agregada

## 1. Identificação

- ID: TASK-014
- Fase: [PHASE-04 — Árvores de distribuição e adaptação](../../phases/PHASE-04-FLOW-MANAGEMENT.md)
- Prioridade: P0
- Estado: IN PROGRESS
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

## Início de Model — 007-arvores / IMP-011

Registo 2026-11-17T15:29:06Z. Seleção explícita do utilizador para iniciar este
bolt. [Model preparado para revisão](../../../memory-bank/bolts/007-arvores/ddd-01-domain-model.md),
com propostas Q-C-34/35 sobre cancelamento mínimo e compatibilidade de prefixos.
Sem aprovação de Model, Design/ADR/Implement/Test ou execução V-014 nesta etapa.
Revisão final por Waheed e integração continuam exigidas antes de DONE.
A falha anterior de NeighborDown na regressão atual fica registada no Model;
não se declara build verde nem se alteram as dependências historicamente DONE.

## Model aprovado — Design apenas

Registo 2026-11-17T17:02:14Z. O utilizador aprovou Model e Q-C-34/35;
decisões também em [construction-duvidas](../../construction-duvidas.md).
[Design para revisão](../../../memory-bank/bolts/007-arvores/ddd-02-technical-design.md)
concretiza validade remanescente, limites, idempotência e seleção divergente
que preserva o ramo confirmado. Detalhes novos de Design por aprovar.
Os mesmos14 cenários M01–M14/V-014 por executar. Sem Java, ADR/Implement/Test,
commits ou push; nenhum gate externo adicional.
[Evidência duradoura de NeighborDown](../../../experiments/construction/007-arvores/neighbor-down-baseline/README.md)
preservada com hashes; investigação antes da aceitação de Implement/Test,
sem alterações a testes/fundações fora do âmbito por inferência.

## Design completo aprovado — revisão ADR preparada

Registo 2026-11-17T17:30:07Z. Design aprovado integralmente pelo utilizador,
incluindo leases/T/A/lineagens/capacidades/quotas; apenas revisão ADR autorizada.
[Revisão das APIs reais](../../../memory-bank/bolts/007-arvores/adr-review.md)
preparada: Route/catalog conservam seleção; Snapshot real tem quatro argumentos
e admite extensão aditiva compatível; dispatch ERROR11/15 e espera física da
ativação cabem no RoutingService. Subscription.java e ActivationTest.java ainda
não existem; a inspeção não os declara implementados.
Revisão ADR por aceitar, nenhuma necessidade de novo caminho/ADR identificada
estaticamente. NeighborDown pendente e negativos preservados; investigação antes
de aceitar Implement/Test, apresentação prévia de correção fora do âmbito.
M01–M14/V-014 por executar; sem Java, Implement, commits ou push.

## ADR aprovada — Implement incremental e investigação consolidada

Autorização2026-11-17T17:50:02Z. [Código e resultados](../../../memory-bank/bolts/007-arvores/implementation-walkthrough.md)
nos quatro caminhos, com Route/catalog como seleção, forwarding separado,
Snapshot4 compatível, ERROR11/15 isolados e reservas físicas no shutdown existente.
Regressão incremental anterior à exceção1324/0/0/0,18 XML preservados;28 testes
novos incluindo o diagnóstico de NeighborDown. Após Q-C-36, focado NeighborDown
1/0/0/0; duas regressões 1324/1/0/0 (D11a), isolado D11a 1/0/0/0.
[Resultados novos](../../../experiments/construction/007-arvores/q-c-36/README.md). Sem avanço à etapa Test.

[Investigação consolidada](../../../experiments/construction/007-arvores/neighbor-down-investigation/README.md)
e Q-C-36 fundamentam a exceção autorizada/aplicada apenas no teste histórico;
asserções/prazos e negativos/diagnóstico intactos. D11a sem causa estabelecida
e sem correção adicional. Implement continua por aceitar, sem fecho,
commits/push ou outro IMP. Os14 grupos têm checks incrementais parciais;
V-014 integral/variantes e aprovação pendentes. Fecho exige V-014, regressão
aprovada, manual humano, revisão final Waheed e integração, sem gates externos novos.

## Q-C-37 aplicada — Implement para revisão em 2026-11-17T21:49:25Z

Exceção autorizada exclusivamente em NeighborManager.java/NeighborManagerTest.java;
sete caminhos autorizados no total. [Resultados novos](../../../experiments/construction/007-arvores/q-c-37/README.md):
D11b/três ordens1/0/0/0, D11a original1/0/0/0, handshake/lifecycle202/0/0/0,
uma regressão incremental completa1325/0/0/0. Closed/prazo existente sem resposta
terminam e libertam contexto/tentativa/timer; TCP real com zero reservas e
awaitTermination no budget configurado. Sem alterar testes/asserções/prazos
anteriores ou apagar negativos. Implement não aceite; sem etapa Test, fecho,
commits, push, outro IMP ou gates novos. V-014 consolidado, regressão aprovada,
manual humano, revisão final Waheed e integração pendentes.

## Test atual — aprovação pendente

Implement aprovado pelo utilizador; clean verify1328/0/0/0.
[V-014 consolidado](../../../memory-bank/bolts/007-arvores/ddd-03-test-report.md),
com logs/XML/JAR/hash e roteiro humano reproduzível. Código aprovado intacto.
Manual humano não executado; Test não aprovado. Revisão final Waheed e integração
continuam necessárias. TASK-014 IN PROGRESS; sem staging/commit/push/fecho.

## Confirmações posteriores — Test reapresentado

Manual humano de Francisco cumprido e conferido,24 checks nas cinco etapas,
mesmo JAR/hash e fontes aprovadas. Revisão final de Waheed confirmada pelo
utilizador para esta versão, sem alterações pendentes.
[Versão revista e plano](../../../experiments/construction/007-arvores/integration-01/plan.md).
Test por aprovar e integração por autorizar/executar; TASK-014 IN PROGRESS.
Sem staging/commits/push/fecho.
