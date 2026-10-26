# TASK-013 — Diagnosticar percursos pela tabela de rotas

## 1. Identificação

- ID: TASK-013
- Fase: [PHASE-03 — Monitorização e descoberta de rotas](../../phases/PHASE-03-ROUTING.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-008](../PHASE-02/TASK-008.md), [TASK-012](../PHASE-03/TASK-012.md).
- Requisitos relacionados: [RF-010](../../02-REQUIREMENTS.md#rf-010), [RP-004](../../02-REQUIREMENTS.md#rp-004), [RP-008](../../02-REQUIREMENTS.md#rp-008).

## 2. Objetivo

Verificar o caminho escolhido sem depender de vídeo ou ativação de árvore.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Rotas para várias streams, alternativas sem ciclos e trace até à origem. A responsabilidade específica é diagnosticar percursos pela tabela de rotas. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Implementar routes/catalog/metrics e comando trace UUID. ROUTE_TRACE segue o upstream selecionado, acrescenta visited e reduz ttl, independentemente do estado de subscrição; TRACE_RESULT volta pelo percurso registado para o diagnóstico. Guardar pedidos pendentes com timeout e limite. Isto verifica cliente→origem; logs da resposta mostram a ordem inversa. Distinguir estas mensagens das sondas de RTT e dos datagramas UDP.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: app/OttApplication.java, routing/RoutingService.java; planeado: routing/RouteTraceTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Expor snapshots nos comandos locais sem mapas mutáveis.
- [ ] Implementar trace correlacionado e retorno pelo percurso inverso.
- [ ] Registar cada salto e custo observado.
- [ ] Comparar percurso com o esperado nas fixtures.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Stream desconhecida, ausência de candidato, timeout a meio, ttl esgotado e pedido repetido com visited circular. Trace nunca cria destino de vídeo.

## 10. Testes necessários

V-013: duas rotas alternativas produzem visited correto; NO_ROUTE e TTL são explícitos; não há ativação nem saída de vídeo após trace.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Trace reflete a tabela de cada nó.
- [ ] Falhas terminam por erro/timeout em prazo.
- [ ] Diagnóstico não altera subscrições.
- [ ] Validação V-013 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
