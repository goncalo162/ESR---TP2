# TASK-015 — Podar ramos, renovar leases e remover procura abandonada

## 1. Identificação

- ID: TASK-015
- Fase: [PHASE-04 — Árvores de distribuição e adaptação](../../phases/PHASE-04-FLOW-MANAGEMENT.md)
- Prioridade: P0
- Estado: DONE
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

- [x] Implementar remoção idempotente e recomputação de procura agregada.
- [x] Renovar leases confirmados e verificar prefixo/epoch.
- [x] Expirar filhos sem renovação e limpar pendentes.
- [x] Integrar eventos de saída e desconexão com poda.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Cliente crasha sem LEAVE, stop repetido, cancelamento atrasado, renovação após expiração e duas instâncias sequenciais do mesmo nodeId.

## 10. Testes necessários

V-015: sair um de dois consumidores mantém tronco ativo; sair o último remove; relógio falso elimina lease órfão; cancelamento velho não remove ramo novo.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [x] Vídeo só permanece autorizado enquanto há procura efetiva.
- [x] Ramos órfãos desaparecem por timeout.
- [x] Outros consumidores e fluxos mantêm-se ativos.
- [x] Validação V-015 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.

## Model em preparação — checkpoint anterior

Base main/b273b258, dependências IMP-011/007-arvores e IMP-010/022-rotas complete.
[Model](../../../memory-bank/bolts/023-arvores/ddd-01-domain-model.md) por aprovar;
Q-C-40/41/42 decididas explicitamente, sem extensão dos três caminhos do ADD.
A referência legada a NeighborManager na secção6 não autoriza alteração desse
componente. Cancelamento por ticket/DEACTIVATE; composição CLI posterior.
V-015/Java não executados; os1328 testes anteriores são históricos de IMP-011.
Revisão final Denji/integração permanecem requisitos de fecho, não presumidos.

## Design autorizado — preparado para aprovação

Model aprovado e Q-C-40–42 reconfirmadas pelo utilizador.
[Design](../../../memory-bank/bolts/023-arvores/ddd-02-technical-design.md) com
APIs/estados, corrida RENEW/DEACTIVATE, prazos/cadência, limites físicos e L01–L24.
Nenhum teste executado, Java alterado ou ADR/Implement iniciado. TASK em progresso.

## Revisão ADR autorizada — checkpoint atual

Design aprovado pelo utilizador. [Revisão ADR](../../../memory-bank/bolts/023-arvores/adr-review.md)
preparada, sem nova ADR/divergência concreta; adaptações futuras F01–F07 nos
três ficheiros, preservando grant/M07/quotas/físico/shutdown. Implement não
autorizado, V-015 não executado. Sem Java/testes Java/staging/commits/push.

## Implement — interfaces/tipos e testes vermelhos

Revisão ADR aprovada, somente primeiro passo de Implement autorizado.
[Walkthrough](../../../memory-bank/bolts/023-arvores/implementation-walkthrough.md):
22 testes novos,20 falhas esperadas/0 erros/0 ignorados; compilação passou.
30 históricos de ativação selecionados passaram, incluindo6 M07. APIs e testes
históricos intactos; tipos main não ligados ao engine. Parar antes dos
comportamentos, V-015 integral/Test/regressão pendentes. Sem staging/commit/push.

## Implement — comportamentos para revisão

Primeiro checkpoint aprovado; comportamentos autorizados nos três ficheiros.
[Resultados incrementais e preservação](../../../memory-bank/bolts/023-arvores/implementation-walkthrough.md),
incluindo TCP real, M07 e limites. Implement por aceitar; tarefa em progresso,
sem DONE, Test/clean verify/JAR/manual/Denji/integração concluídos.
Sem staging/commits/push e sem gates externos adicionais.

## Checkpoint atual — Test, manual humano pendente

Implement aprovado; Test autorizado pelo utilizador. [Relatório](../../../memory-bank/bolts/023-arvores/ddd-03-test-report.md)
consolida clean verify1379/0/0/0,19 XML,V-015/L01–L24 e JAR/hash.
Código/históricos/negativos/Q-C-40–42 intactos. [Manual](../../../experiments/construction/023-arvores/manual-instructions.md)
preparado, por executar por humano; ensaio kind=agent não o substitui.
Test por aprovar; depois revisão Denji e integração. TASK-015 IN PROGRESS,
sem DONE/staging/commits/push/fecho.

## Checkpoint Test após manual humano

Francisco confirmou pessoalmente seis etapas sem divergências;30 checks,
JAVA_EXIT0,hashes/origem no JAR e JVM terminada conferidos. [Prova](../../../experiments/construction/023-arvores/manual-human-verification-01/verification.json).
WRAPPER_EXIT não fornecido/não inferido. Test reapresentado para aprovação;
revisão final Denji e integração pendentes. TASK-015 continua IN PROGRESS.
Sem staging/commits/push/fecho; código e evidências originais preservados.

## Test aprovado — revisão final Denji e integração pendentes

Utilizador aprovou Test1379/0/0/0,19 XML,V-ADD e Francisco30.
[Resumo final](../../../memory-bank/bolts/023-arvores/final-review-summary.md)
e [plano/allowlist](../../../memory-bank/bolts/023-arvores/integration-plan.md)
preparados, preservando código/JAR/negativos/Q-C-40–42. Denji ainda não confirmado,
integração não autorizada/executada. TASK-015 IN PROGRESS,sem DONE/fecho/staging/
commits/push/outro IMP. Target e alterações alheias excluídos.

## Revisão final Denji confirmada — integração por autorizar

Utilizador confirmou aprovação final da implementação atual sem alterações pendentes.
[Confirmação/hashes](../../../experiments/construction/023-arvores/integration-01/review-confirmation.json)
e [plano final](../../../memory-bank/bolts/023-arvores/integration-plan.md).
Mesmo código/JAR/Test/manual,sem repetição. Simulação exata com referências/V-ADD;
TASK-015 IN PROGRESS até integração comprovada/fecho. Sem staging/commits/push.

## Fecho autorizado e evidência integrada

TASK-015 DONE,bolt/história023-arvores complete/implemented=true por autorização
explícita após integração comprovada4d18d1dd8fb50d14ace02609a9133733c20cabfc.
[Fecho](../../../memory-bank/bolts/023-arvores/closure-walkthrough.md):
Test1379/0/0/0,19 XML,V-015/L01–L24,V-ADD,manual Francisco30 e revisão final
Denji cumpridos. [Prova pós-commit](../../../experiments/construction/023-arvores/integration-proof-01/README.md).
Âmbito é controlo/autorização/leases/poda; não comprova vídeo/GUI/CORE.
Unidade004-arvores/PHASE-04 continuam IN PROGRESS. Sem alterações Java,push ou
início de outro IMP; target/alterações alheias excluídos do fecho.
