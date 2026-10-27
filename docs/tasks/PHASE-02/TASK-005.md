# TASK-005 — Estabelecer transporte TCP full-duplex com recursos limitados

## 1. Identificação

- ID: TASK-005
- Fase: [PHASE-02 — Overlay, bootstrap e ciclo de vida](../../phases/PHASE-02-OVERLAY.md)
- Prioridade: P0
- Estado: DONE
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

Implementados: transport/Transport.java, Connection.java e o teste transport/TransportTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [x] Implementar accept/connect com timeout e identidade de conexão.
- [x] Integrar MessageCodec sem leituras concorrentes.
- [x] Adicionar fila de escrita e política explícita de overflow.
- [x] Testar tráfego nos dois sentidos e encerramento com I/O pendente.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Peer sem leitura, porta ocupada, conexão recusada, EOF parcial, duas threads a enviar e shutdown durante accept. Overflow fecha apenas a conexão saturada com razão.

## 10. Testes necessários

V-005: dois endpoints loopback com portas efémeras trocam mensagens simultâneas intactas; parar um gera evento único; reconexão e saturação não deixam threads/sockets abertos.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado atual da validação: EXECUTADA e revista, com 18 testes TCP aprovados e 149 testes no build limpo. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [x] Há troca full-duplex com framing íntegro.
- [x] Filas são limitadas e encerramento termina recursos.
- [x] Falha de um peer não mata o listener.
- [x] Validação V-005 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Evidência: [V-005](../../../experiments/task-005-transport-validation.md).

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.

### Resultado da execução — 27-10-2026

Transport/Connection implementam listener e dial assíncrono com timeout, UUID por conexão, leitor/escritor únicos, eventos ordenados, fila/admissão limitadas, cancelamento e shutdown com deadline. Defaults: 256 frames, 64 reservas incluindo dials, connect 2 s e shutdown externo 3 s. Overflow fecha apenas a conexão saturada; callbacks devem retornar rapidamente. API em 07 e ADR-014 em 12.

Build `mvn -B -ntp clean verify`: 146 testes aprovados (65 configuração, 66 codec, 15 transporte), zero falhas/erros/skips. Sockets reais em loopback; sem CORE/processos JVM independentes demonstrados. Preservados codec/fixtures corrigidos por ADR-013, enunciado, Java/Maven e configuração local. [Evidência](../../../experiments/task-005-transport-validation.md). PHASE-02 IN PROGRESS; TASK-006 continua TODO, apenas descoberta inicial sem registo vivo ou leases no bootstrap.

### Revisão — 27-10-2026

Revisão de contratos, implementação, recursos e testes sem defeito funcional confirmado no transporte. Acrescentados três testes de callbacks: falhas em open/closed sem retenção de slots, deadline explícito com callback preso e shutdown simultâneo por duas ligações sem join circular. Build limpo com 149 testes aprovados (18 TCP), zero falhas/erros/skips. [Evidência da revisão](../../../experiments/task-005-transport-validation.md#revisão-da-task-005--27-10-2026). Código do transporte preservado; TASK-005 permanece DONE. O fecho imediato continua a descartar fila: confirmar escrita completa da resposta será necessário na integração da TASK-006.
