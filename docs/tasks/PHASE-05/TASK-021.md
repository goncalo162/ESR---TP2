# TASK-021 — Reencaminhar e replicar datagramas pela árvore ativa

## 1. Identificação

- ID: TASK-021
- Fase: [PHASE-05 — Vídeo, replicação e reprodução](../../phases/PHASE-05-STREAMING.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Waheed](../../team/WAHEED.md).
- Revisor: [Fillmore](../../team/FILLMORE.md), antes de integrar e marcar DONE.
- Dependências: [TASK-014](../PHASE-04/TASK-014.md), [TASK-015](../PHASE-04/TASK-015.md), [TASK-016](../PHASE-04/TASK-016.md), [TASK-017](../PHASE-04/TASK-017.md), [TASK-019](../PHASE-05/TASK-019.md).
- Requisitos relacionados: [RF-007](../../02-REQUIREMENTS.md#rf-007), [RF-010](../../02-REQUIREMENTS.md#rf-010), [RF-011](../../02-REQUIREMENTS.md#rf-011), [RF-015](../../02-REQUIREMENTS.md#rf-015), [RNF-001](../../02-REQUIREMENTS.md#rnf-001), [RNF-002](../../02-REQUIREMENTS.md#rnf-002), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Enviar uma cópia por saída ativa sem bloquear o controlo.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa. A responsabilidade específica é reencaminhar e replicar datagramas pela árvore ativa. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Integrar socket UDP e StreamForwarder com snapshot imutável. Validar endpoint contra upstream confirmado, epoch e StreamKey antes de entregar localmente/replicar; SERVER injeta pacotes locais por caminho explícito equivalente, sem fingir origem UDP externa. Deduplicar por stream/session/frame/fragmento em janela limitada. Destinos são conjunto de vizinhos confirmados, nunca lista por cliente final. Filas egress limitadas, saturação descarta mais antigo e conta; controlo mantém progresso.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existente: streaming/StreamForwarder.java e StreamForwarderTest.java; planeado: transport/UdpTransport.java e testes de endpoints UDP.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Criar UDP ingress/egress com encerramento e limites.
- [ ] Aplicar filtragem de origem e estado por fluxo.
- [ ] Replicar bytes para cada downstream uma única vez.
- [ ] Instrumentar entrada/saída/descarte e testar saturação.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Pacote de vizinho válido mas não upstream, duplicado, época antiga, mudança de parent durante receção, destino lento, child lease expirado e stream inativa.

## 10. Testes necessários

V-021: fonte→relay→dois sinks recebe uma cópia por fragmento no tronco e duas nas saídas; origem não autorizada/epoch velha não segue; saturar uma saída não bloqueia controlo nem outras streams.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Envios correspondem apenas ao snapshot ativo.
- [ ] Replicação é única por destino em estado estável.
- [ ] Counters distinguem descarte, envio e entrega local.
- [ ] Validação V-021 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
