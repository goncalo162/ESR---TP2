# TASK-006 — Fornecer localização inicial dos vizinhos configurados

## 1. Identificação

- ID: TASK-006
- Fase: [PHASE-02 — Overlay, bootstrap e ciclo de vida](../../phases/PHASE-02-OVERLAY.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Fillmore](../../team/FILLMORE.md).
- Revisor: [Waheed](../../team/WAHEED.md), antes de integrar e marcar DONE.
- Dependências: [TASK-003](../PHASE-01/TASK-003.md), [TASK-005](../PHASE-02/TASK-005.md).
- Requisitos relacionados: [RF-002](../../02-REQUIREMENTS.md#rf-002), [RF-003](../../02-REQUIREMENTS.md#rf-003), [RF-004](../../02-REQUIREMENTS.md#rf-004).

## 2. Objetivo

Devolver a um nó conhecido os endpoints dos seus vizinhos permitidos.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. A responsabilidade específica é responder à consulta inicial de vizinhos apenas a partir da configuração. O utilizador restringiu explicitamente o papel do bootstrap; não criar um controlador de nós ativos. Consultar [arquitetura](../../04-ARCHITECTURE.md), ADR-013 em [decisões](../../12-RISKS-AND-DECISIONS.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md).

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Usar exclusivamente o grafo simétrico imutável carregado por ConfigurationLoader. Receber REGISTER como consulta: verificar correspondência de senderId com Endpoint.nodeId, identidade configurada e endpoint/papel compatíveis; responder com REGISTER_OK contendo apenas os endpoints dos vizinhos definidos no ficheiro. Nunca atualizar a configuração com os dados do pedido. Todos os instanceId dos vizinhos devolvidos são zero, sem consultar ou descobrir se estão ativos. Enviar a resposta completa e fechar a conexão de consulta; timeout, erro ou desconexão de um requerente não afetam o listener nem outras consultas.

Pedidos repetidos, simultâneos ou de instâncias diferentes do mesmo ID recebem a mesma lista configurada; não há exclusividade ou inscrição no bootstrap. Duplicados ativos são tratados no handshake direto (TASK-007). Não criar NodeRegistry, leases, renovações, heartbeats, timers de membership, monitorização, tratamento de NODE_LEAVE, anúncio de streams ou cálculo de rotas. Código 3 está retirado e é inválido. Outras mensagens válidas do codec comum são operações inválidas neste handler: rejeitar com ERROR INVALID_MESSAGE quando for seguro e fechar, sem efeitos operacionais. Retentar a consulta apenas se não se obteve resposta; após sucesso não manter canal periódico com bootstrap.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: bootstrap/BootstrapServer.java contém apenas documentação; bootstrap/BootstrapServerTest.java é placeholder; config/TopologyConfiguration.java e resources/config/topology.properties já estão implementados/preenchidos. Implementar a consulta diretamente sobre TopologyConfiguration. O placeholder NodeRegistry.java foi removido por não existir registo operacional a implementar. Confirmar estes estados no início da tarefa.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Consultar a lista de vizinhos do requerente na configuração imutável.
- [ ] Validar identidade/endpoint/papel sem persistir o pedido.
- [ ] Enviar REGISTER_OK sem lease, com instâncias desconhecidas, e fechar a conexão de consulta.
- [ ] Rejeitar operações alheias à consulta sem alterar estado da topologia.
- [ ] Testar repetição/concorrência, duas instâncias do mesmo ID e vizinhos offline com resposta igual.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Nó não permitido, endpoint/papel incompatível, consulta repetida, duas instâncias do mesmo ID, vizinhos offline, requerente que fecha cedo, topologia assimétrica e bootstrap indisponível. Não distinguir online/offline nem arbitrar IDs vivos; a consulta não depende de saídas ou expiração. Não calcular rotas nem transportar streams.

## 10. Testes necessários

V-006: consultas repetidas/concorrentes recebem só os vizinhos configurados, incluindo offline, com instanceId zero e sem lease; duas instâncias do mesmo ID recebem a mesma lista; operações não permitidas são rejeitadas sem alterar a configuração; a conexão fecha após a resposta completa e não existe renovação ou estado vivo no bootstrap.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Resposta corresponde ao grafo e respeita X por nó.
- [ ] Resposta independe da disponibilidade e de consultas anteriores; não existem registo vivo, lease, renovação ou monitorização.
- [ ] IDs e endpoints inválidos são rejeitados sem alterar a configuração ou bloquear outras consultas.
- [ ] Conexão inicial é fechada após resposta; não há mensagens de saída/renovação para bootstrap.
- [ ] Validação V-006 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
