# TASK-008 — Compor o processo overlay e encerrar de forma previsível

## 1. Identificação

- ID: TASK-008
- Fase: [PHASE-02 — Overlay, bootstrap e ciclo de vida](../../phases/PHASE-02-OVERLAY.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-003](../PHASE-01/TASK-003.md), [TASK-006](../PHASE-02/TASK-006.md), [TASK-007](../PHASE-02/TASK-007.md).
- Requisitos relacionados: [RF-001](../../02-REQUIREMENTS.md#rf-001), [RF-002](../../02-REQUIREMENTS.md#rf-002), [RNF-003](../../02-REQUIREMENTS.md#rnf-003), [RP-008](../../02-REQUIREMENTS.md#rp-008).

## 2. Objetivo

Permitir iniciar e terminar cada papel manualmente, sem streaming antecipado.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. A responsabilidade específica é compor o processo overlay e encerrar de forma previsível. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Integrar CLI e configuração com bootstrap ou núcleo de nó. Criar executor serial proprietário do estado e encaminhar callbacks para ele. Nesta fase comandos neighbors e quit funcionam; catalog/play explicam que a funcionalidade ainda não está implementada. Shutdown notifica LEAVE quando possível, fecha transporte e termina executores mesmo com bootstrap ausente. Logs incluem identidade de processo, connectionId e razão de saída.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: app/OttApplication.java, overlay/OverlayNode.java, client/StreamingClient.java como ponto de composição futuro; planeado: app/LifecycleTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Implementar parsing de argumentos e erros de utilização.
- [ ] Compor serviços por papel e iniciar validação antes do bind.
- [ ] Publicar estados de vizinhança e comandos locais mínimos.
- [ ] Adicionar shutdown hook e encerramento idempotente.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Ctrl-C duas vezes, papel/config incompatíveis, stdin fechado, porta ocupada e bootstrap ainda não iniciado. Configuração inválida sai com código 2; falha de bind com 3.

## 10. Testes necessários

V-008: subprocessos locais iniciam por CLI, mostram vizinhos e terminam; configuração inválida não abre porto; interrupção e erro de um peer não deixam processo preso.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] BOOTSTRAP, SERVER, RELAY e CLIENT arrancam no papel pedido.
- [ ] Códigos de erro e logs são coerentes.
- [ ] O fim de um processo não exige reiniciar os restantes.
- [ ] Validação V-008 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
