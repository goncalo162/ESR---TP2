# TASK-007 — Gerir handshake, unicidade e reconexão de vizinhos

## 1. Identificação

- ID: TASK-007
- Fase: [PHASE-02 — Overlay, bootstrap e ciclo de vida](../../phases/PHASE-02-OVERLAY.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-005](../PHASE-02/TASK-005.md), [TASK-006](../PHASE-02/TASK-006.md).
- Requisitos relacionados: [RF-002](../../02-REQUIREMENTS.md#rf-002), [RF-003](../../02-REQUIREMENTS.md#rf-003), [RF-004](../../02-REQUIREMENTS.md#rf-004), [RNF-004](../../02-REQUIREMENTS.md#rnf-004), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Manter uma única ligação lógica ativa por par permitido.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. A responsabilidade específica é gerir handshake, unicidade e reconexão de vizinhos. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Aplicar regra de iniciador por ordem de nodeId, validar HELLO/HELLO_OK e associar endpoint UDP publicitado à conexão. Só publicar ACTIVE após handshake válido. Agendar retries com backoff e jitter; vizinhos offline continuam desejados. Comparar connectionId e instanceId nos eventos para que a morte de uma conexão antiga não remova a nova.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: overlay/Neighbor.java, NeighborManager.java, NodeRole.java e overlay/NeighborManagerTest.java; planeados: eventos imutáveis de vizinhança.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Obter lista de vizinhos do bootstrap e iniciar os dials devidos.
- [ ] Validar identidade, permissões e endpoints no HELLO.
- [ ] Eliminar duplicados e publicar eventos de entrada/saída.
- [ ] Repetir conexão após falha até shutdown, mantendo estado observável.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Início simultâneo, IDs iguais, HELLO em atraso, peer não autorizado e regresso do iniciador após crash. Não usar heurística de primeiro socket que chega.

## 10. Testes necessários

V-007: três nós iniciados em ordens diferentes convergem para o grafo esperado sem duplicados; novo instanceId após reinício não é apagado por callback antigo; vizinho desconhecido é rejeitado.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Uma conexão confirmada por vizinho.
- [ ] Entrada tardia e reconexão funcionam.
- [ ] Endereço UDP só é aceite após handshake.
- [ ] Validação V-007 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
