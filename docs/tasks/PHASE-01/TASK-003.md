# TASK-003 — Carregar e validar configurações de nós, grafo e streams

## 1. Identificação

- ID: TASK-003
- Fase: [PHASE-01 — Fundações, configuração e protocolo](../../phases/PHASE-01-FOUNDATION.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-001](../PHASE-01/TASK-001.md), [TASK-002](../PHASE-01/TASK-002.md).
- Requisitos relacionados: [RF-001](../../02-REQUIREMENTS.md#rf-001), [RF-004](../../02-REQUIREMENTS.md#rf-004), [RF-012](../../02-REQUIREMENTS.md#rf-012), [RF-013](../../02-REQUIREMENTS.md#rf-013), [RP-001](../../02-REQUIREMENTS.md#rp-001), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Obter configurações utilizáveis e rejeitar erros antes de abrir sockets.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Configuração e mensagens de controlo validadas por testes locais, sem necessidade de CORE. A responsabilidade específica é carregar e validar configurações de nós, grafo e streams. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Implementar o esquema de propriedades de docs/07, incluindo resolução de paths relativos ao ficheiro de configuração. Validar IDs, portos, endpoints, simetria do grafo, FPS, UUID global e associação a servidor. Separar validação estrutural do catálogo de validação dos bytes de vídeo em TASK-020. Criar exemplos locais para bootstrap, dois servidores, relay e cliente com portas sem colisão. Os perfis CORE serão concretizados em TASK-025–027.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: config/NodeConfiguration.java, ConfigurationLoader.java e resources/config/*.properties vazios; planeados: resources/config/topology.properties, streams-s1.properties, streams-s2.properties e config/ConfigurationLoaderTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Criar modelos imutáveis para valores de configuração.
- [ ] Implementar parsing estrito e erros agregados com chave e origem.
- [ ] Preencher exemplos .properties e substituir o uso planeado de JSON vazio por propriedades.
- [ ] Criar testes positivos/negativos e exigir testes realmente descobertos pelo Maven.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Ficheiro inexistente, chave duplicada ou desconhecida, CSV vazio, path relativo, UUID repetido, grafo assimétrico, host wildcard publicitado e porto inválido. Definir rejeição de chaves desconhecidas para apanhar erros de escrita.

## 10. Testes necessários

V-003: fixtures válidas/invalidas com diretório temporário, paths fora do cwd, ID de um carácter e quatro streams; Maven reporta testes executados e falha perante um requisito de configuração violado.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Configuração válida carrega sem rede.
- [ ] Erros identificam a propriedade e não deixam estado parcial.
- [ ] Há dois servidores com dois UUID por servidor nos exemplos finais.
- [ ] Validação V-003 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
