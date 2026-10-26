# TASK-012 — Difundir anúncios e construir catálogo e rotas

## 1. Identificação

- ID: TASK-012
- Fase: [PHASE-03 — Monitorização e descoberta de rotas](../../phases/PHASE-03-ROUTING.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-007](../PHASE-02/TASK-007.md), [TASK-010](../PHASE-03/TASK-010.md), [TASK-011](../PHASE-03/TASK-011.md).
- Requisitos relacionados: [RF-005](../../02-REQUIREMENTS.md#rf-005), [RF-006](../../02-REQUIREMENTS.md#rf-006), [RF-009](../../02-REQUIREMENTS.md#rf-009), [RF-012](../../02-REQUIREMENTS.md#rf-012), [RF-014](../../02-REQUIREMENTS.md#rf-014), [RP-001](../../02-REQUIREMENTS.md#rp-001).

## 2. Objetivo

Descobrir todas as streams e caminhos sem flooding infinito.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Rotas para várias streams, alternativas sem ciclos e trace até à origem. A responsabilidade específica é difundir anúncios e construir catálogo e rotas. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Cada SERVER publica as streams configuradas, mesmo sem consumidores. Implementar ANNOUNCE/WITHDRAW com path vector, seq e epoch, cache limitada e reenvio de melhoria estrita. Verificar origem do StreamInfo, unicidade do path e sender. Candidatos expiram por relógio local; servidor reiniciado gera epoch novo. Metadados de catálogo são derivados de anúncios válidos e removidos quando não resta candidato.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: routing/RoutingService.java, streaming/StreamInfo.java, StreamCatalog.java e routing/RoutingServiceTest.java; planeados: fixtures de grafos com ciclos.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Gerar anúncios periódicos para dois fluxos por origem.
- [ ] Validar, somar custo local e guardar candidato por vizinho.
- [ ] Reenviar primeira chegada e melhorias com rate limit.
- [ ] Tratar expiração, retirada, epoch novo e catálogo consultável.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Um caminho melhor chega depois do primeiro anúncio; ciclo na malha; duplicados de outra conexão; época velha reaparece; conflito de origem para UUID. Não inventar catálogo central acessível por HTTP.

## 10. Testes necessários

V-012: grafo em losango e grafo cíclico convergem para custo esperado; segunda chegada melhor é aceite; quatro streams aparecem; retirar uma origem não remove streams da outra; flooding estabiliza.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Catálogo inclui origem e validade dos quatro fluxos.
- [ ] Melhores caminhos e alternativas ficam disponíveis.
- [ ] Cache, seq, epoch e path impedem tempestade e regressão a época retirada.
- [ ] Validação V-012 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
