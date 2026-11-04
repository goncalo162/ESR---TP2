# TASK-006 — Fornecer localização inicial dos vizinhos configurados

## 1. Identificação

- ID: TASK-006
- Fase: [PHASE-02 — Overlay, bootstrap e ciclo de vida](../../phases/PHASE-02-OVERLAY.md)
- Prioridade: P0
- Estado: DONE
- Responsável: [Fillmore](../../team/FILLMORE.md).
- Revisor: [Waheed](../../team/WAHEED.md), antes de integrar e marcar DONE.
- Dependências: [TASK-003](../PHASE-01/TASK-003.md), [TASK-005](../PHASE-02/TASK-005.md), [TASK-032](TASK-032.md).
- Requisitos relacionados: [RF-002](../../02-REQUIREMENTS.md#rf-002), [RF-003](../../02-REQUIREMENTS.md#rf-003), [RF-004](../../02-REQUIREMENTS.md#rf-004).

## 2. Objetivo

Devolver a um nó conhecido os endpoints dos seus vizinhos permitidos.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. A responsabilidade específica é responder à consulta inicial de vizinhos apenas a partir da configuração. O utilizador restringiu explicitamente o papel do bootstrap; não criar um controlador de nós ativos. Consultar [arquitetura](../../04-ARCHITECTURE.md), ADR-013 em [decisões](../../12-RISKS-AND-DECISIONS.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md).

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

Dependências prontas após TASK-005: ConfigurationLoader, Transport.connect assíncrono, Connection.send e eventos ordenados por ligação. Callbacks não devem bloquear. [API disponível](../../07-API-AND-INTERFACES.md#api-de-transporte-implementada-na-task-005), [V-005](../../../experiments/task-005-transport-validation.md). TASK-006 DONE, com cinco etapas concluídas, Test aprovado, revisão final confirmada pelo utilizador e versão revista integrada em main; preservar ADR-013, sem membership/lease no bootstrap.

## 5. Especificação técnica

Usar exclusivamente o grafo simétrico imutável carregado por ConfigurationLoader. Receber REGISTER como consulta: verificar correspondência de senderId com Endpoint.nodeId, identidade configurada e endpoint/papel compatíveis; responder com REGISTER_OK contendo apenas os endpoints dos vizinhos definidos no ficheiro. Nunca atualizar a configuração com os dados do pedido. Todos os instanceId dos vizinhos devolvidos são zero, sem consultar ou descobrir se estão ativos. Enviar a resposta completa e fechar a conexão de consulta; timeout, erro ou desconexão de um requerente não afetam o listener nem outras consultas.

Pedidos repetidos, simultâneos ou de instâncias diferentes do mesmo ID recebem a mesma lista configurada; não há exclusividade ou inscrição no bootstrap. Duplicados ativos são tratados no handshake direto (TASK-007). Não criar NodeRegistry, leases, renovações, heartbeats, timers de membership, monitorização, tratamento de NODE_LEAVE, anúncio de streams ou cálculo de rotas. Código 3 está retirado e é inválido. Outras mensagens válidas do codec comum são operações inválidas neste handler: rejeitar com ERROR INVALID_MESSAGE quando for seguro e fechar, sem efeitos operacionais. Retentar a consulta apenas se não se obteve resposta; após sucesso não manter canal periódico com bootstrap.

Integração de escrita / ADR-015: acrescentar ao transporte uma confirmação assíncrona de escrita/flush, concluída pelo escritor exclusivo depois do frame inteiro. Encadear o fecho da consulta nessa conclusão, com prazo limitado; nunca bloquear o callback de leitura. Terminar confirmações pendentes em falha, cancelamento, overflow ou shutdown. Preservar a semântica de send=ACCEPTED, o codec e os bytes v1; esta extensão pertence à TASK-006, sem reabrir TASK-005.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: bootstrap/BootstrapServer.java contém apenas documentação; bootstrap/BootstrapServerTest.java é placeholder; config/TopologyConfiguration.java e resources/config/topology.properties já estão implementados/preenchidos. Implementar a consulta diretamente sobre TopologyConfiguration. O placeholder NodeRegistry.java foi removido por não existir registo operacional a implementar. Confirmar estes estados no início da tarefa.

ADR-015 envolve também transport/Connection.java e, se necessário para integrar o ciclo de vida, Transport.java; acrescentar casos à TransportTest.java e BootstrapServerTest.java. A extensão de confirmação de escrita mantém os limites/semântica pública existentes e a regressão V-005.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [x] Consultar a lista de vizinhos do requerente na configuração imutável.
- [x] Validar identidade/endpoint/papel sem persistir o pedido.
- [x] Enviar REGISTER_OK sem lease, com instâncias desconhecidas, e fechar a conexão de consulta.
- [x] Rejeitar operações alheias à consulta sem alterar estado da topologia.
- [x] Testar repetição/concorrência, duas instâncias do mesmo ID e vizinhos offline com resposta igual.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Nó não permitido, endpoint/papel incompatível, consulta repetida, duas instâncias do mesmo ID, vizinhos offline, requerente que fecha cedo, topologia assimétrica e bootstrap indisponível. Não distinguir online/offline nem arbitrar IDs vivos; a consulta não depende de saídas ou expiração. Não calcular rotas nem transportar streams.

## 10. Testes necessários

V-006: consultas repetidas/concorrentes recebem só os vizinhos configurados, incluindo offline, com instanceId zero e sem lease; duas instâncias do mesmo ID recebem a mesma lista; operações não permitidas são rejeitadas sem alterar a configuração; a conexão fecha após a resposta completa e não existe renovação ou estado vivo no bootstrap.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [x] Resposta corresponde ao grafo e respeita X por nó.
- [x] Resposta independe da disponibilidade e de consultas anteriores; não existem registo vivo, lease, renovação ou monitorização.
- [x] IDs e endpoints inválidos são rejeitados sem alterar a configuração ou bloquear outras consultas.
- [x] Conexão inicial é fechada após resposta; não há mensagens de saída/renovação para bootstrap.
- [x] Confirmação de escrita/flush tem prazo e termina em sucesso ou erro; testes observam resposta completa antes de EOF e não deixam futures pendentes após falha/shutdown.
- [x] Validação V-006 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.


## Arranque Construction — Model para revisão

Iniciado em 2026-11-04T10:40:58Z por seleção explícita do utilizador, só a
primeira etapa de 002-overlay/IMP-003. Base Denji
4ac5808dacf6e97bfd3636d2788db363e926bc95, igual a main e referências locais de
origin, árvore inicialmente limpa. Dependências IMP-001/002 complete/integradas.
[Modelo](../../../memory-bank/bolts/002-overlay/ddd-01-domain-model.md) preparado
em 2026-11-04T10:46:15Z: identidades/grafo/contextos, escrita antes de EOF,
prazo total, limites e M01–M18 para V-006. Nenhum critério de implementação
assinalado, nenhuma alteração Java/teste/build, nenhuma execução V-006 nova.
Aguardar aprovação de Model antes de Design/ADR/Implement/Test. Revisor
atribuído Waheed, sem aceitação presumida; revisão final e integração antes
de DONE. Sem commits/push, fecho ou outro IMP nesta continuação.

### Model aprovado — Design para revisão

Aprovação explícita de Model registada em 2026-11-04T10:50:52Z: coerência de instância
e resposta única em [Q-C-04](../../construction-duvidas.md#q-c-04--coerência-da-instância-e-resposta-única-na-consulta-inicial).
Autorizado apenas Design, [preparado](../../../memory-bank/bolts/002-overlay/ddd-02-technical-design.md)
em 2026-11-04T11:04:53Z: fila comum/inFlight, publicação de futures fora de locks, requestClose
aditivo para budget de shutdown, normalização lexical de hosts e injeções de
relógio/escrita para M01–M18 nos cinco ficheiros. Sem alterações/execução Java;
V-006/regressão/manual e revisão final atribuída a Waheed pendentes. Parar antes
de ADR/Implement/Test; sem commits/push, integração deste IMP ou DONE.

### Design aprovado — revisão ADR para aprovação

Design aprovado em 2026-11-04T11:25:40Z, incluindo Transport.requestClose() sem espera
com close público preservado. Autorizada apenas ADR, [revisão preparada](../../../memory-bank/bolts/002-overlay/adr-review.md)
em 2026-11-04T11:29:33Z: 013/014/015/017/020 e transversais conformes ao desenho; ADR-003
continua substituída. Alterações internas do transporte justificadas, conclusões
fora de locks, reservas até ambos os workers terminarem e limites/relógio/escrita
controláveis M01–M18 preservados. Sem inviabilidade material identificada nesta
inspeção; sem ADR nova, código/testes executados ou alteração do ADD.
Aguardar aprovação antes de Implement/Test; Waheed/revisão final e integração
continuam exigidos antes de DONE. Sem commits/push ou fecho nesta etapa.

### Implement preparado para revisão

ADR aprovada em 2026-11-04T11:37:45Z; autorizado apenas Implement. [Walkthrough](../../../memory-bank/bolts/002-overlay/implementation-walkthrough.md)
preparado em 2026-11-04T12:08:01Z, com consultas/identidades/deadlines e escrita confirmada.
Red real das APIs (23 testes/5 erros novos, 18 antigos aprovados) e do Core
(10 testes/7 falhas/2 erros) guardados antes dos comportamentos correspondentes.
Último incremento: 856 testes aprovados, zero falhas/erros/skips, incluindo TCP
real e drivers/relógio controláveis. Cinco ficheiros +1226/−35, remoções
justificadas, 150 hashes restantes e todos os testes anteriores preservados.
M01–M18 mapeados; M18 parcial até clean verify/manual de Test. Parar para revisão
antes de Test/fecho. Revisor final Waheed e integração continuam exigidos;
sem commits/push, DONE ou outro IMP.

### Test preparado para revisão

Implement aprovado em 2026-11-04T12:18:01Z; autorizada apenas Test. [Relatório](../../../memory-bank/bolts/002-overlay/ddd-03-test-report.md)
preparado em 2026-11-04T12:29:39Z: clean verify/JDK21/TCP real em 2026-11-04T12:18:10Z, saída 0,
856 testes sem falhas/erros/skips. Manual agente Codex em 2026-11-04T12:22:53Z,
34 CHECK_PASS/MANUAL_PASS/saída Java 0; [comandos pessoais](../../../experiments/construction/002-overlay/manual-bootstrap.md)
disponíveis. V-006/M01–M18 executados tecnicamente, sem alterar os cinco ficheiros
ou qualquer outro dos 155 ficheiros de Projeto/ fora de target durante Test.
Test aprovado, com registo em 2026-11-04T14:05:14Z, incluindo repetição pessoal por Luis
em 2026-11-04T13:58:36Z–13:58:38Z, 34 CHECK_PASS/MANUAL_PASS/JAVA_EXIT=0, mesmo JAR.
[Log pessoal preservado](../../../experiments/construction/002-overlay/test-manual-luis.log),
separado de Codex. [Fecho preparado](../../../memory-bank/bolts/002-overlay/closure-walkthrough.md),
commits/push de Denji autorizados. Revisão final atribuída a Waheed e integração
em main pendentes; TASK-006 continua IN PROGRESS. Nenhum outro IMP iniciado.

### Fecho concluído

Em 2026-11-04T14:18:23Z, o utilizador confirmou «fecha o bolt, a revisão esta feita»,
relativamente à revisão final atribuída a Waheed. A versão revista
03345e9e1ab56ec04302676cca6fce32d168223d já estava em main e Denji, locais e remotos, antes do fecho.
[Prova](../../../experiments/construction/002-overlay/integration-proof.json).
Mecanismo specsmd executado em 2026-11-04T14:18:23Z, saída 0:
[log](../../../experiments/construction/002-overlay/bolt-complete.log).
Bolt/história complete, implemented=true; TASK-006 DONE. Unidade in-progress,
três bolts planned. Nenhum código alterado ou nova execução Java no fecho;
856 testes e manuais Codex/Luis preservados. Nenhum outro IMP iniciado.
