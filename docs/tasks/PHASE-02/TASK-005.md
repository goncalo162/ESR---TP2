# TASK-005 — Estabelecer transporte TCP full-duplex com recursos limitados

## 1. Identificação

- ID: TASK-005
- Fase: [PHASE-02 — Overlay, bootstrap e ciclo de vida](../../phases/PHASE-02-OVERLAY.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-004](../PHASE-01/TASK-004.md).
- Requisitos relacionados: [RF-003](../../02-REQUIREMENTS.md#rf-003), [RNF-004](../../02-REQUIREMENTS.md#rnf-004), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Transportar mensagens enquadradas entre endpoints sem bloquear o controlo.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. A responsabilidade específica é estabelecer transporte tcp full-duplex com recursos limitados. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Criar listener e dial TCP, um leitor por conexão e um escritor com fila limitada. Expor eventos connectionOpen/message/connectionClosed e send que devolve sucesso de enfileiramento ou erro. Nenhuma lógica de vizinhança ou rota nesta camada. IDs de conexão distinguem callbacks antigos. Encerrar listener, sockets e executores em prazo definido.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: transport/Transport.java e Connection.java; planeado: transport/TransportTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Implementar accept/connect com timeout e identidade de conexão.
- [ ] Integrar MessageCodec sem leituras concorrentes.
- [ ] Adicionar fila de escrita e política explícita de overflow.
- [ ] Testar tráfego nos dois sentidos e encerramento com I/O pendente.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Peer sem leitura, porta ocupada, conexão recusada, EOF parcial, duas threads a enviar e shutdown durante accept. Overflow fecha apenas a conexão saturada com razão.

## 10. Testes necessários

V-005: dois endpoints loopback com portas efémeras trocam mensagens simultâneas intactas; parar um gera evento único; reconexão e saturação não deixam threads/sockets abertos.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Há troca full-duplex com framing íntegro.
- [ ] Filas são limitadas e encerramento termina recursos.
- [ ] Falha de um peer não mata o listener.
- [ ] Validação V-005 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
