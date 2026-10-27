# TASK-027 — Validar cenário complexo com duas origens e cinco clientes

## 1. Identificação

- ID: TASK-027
- Fase: [PHASE-06 — Integração e avaliação em CORE](../../phases/PHASE-06-CORE-VALIDATION.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Denji](../../team/DENJI.md).
- Revisor: [Fillmore](../../team/FILLMORE.md), antes de integrar e marcar DONE.
- Dependências: [TASK-026](../PHASE-06/TASK-026.md).
- Requisitos relacionados: [RF-007](../../02-REQUIREMENTS.md#rf-007), [RF-012](../../02-REQUIREMENTS.md#rf-012), [RF-013](../../02-REQUIREMENTS.md#rf-013), [RNF-002](../../02-REQUIREMENTS.md#rnf-002), [RC-001](../../02-REQUIREMENTS.md#rc-001), [RP-005](../../02-REQUIREMENTS.md#rp-005).

## 2. Objetivo

Demonstrar a configuração final mínima e partilha com mais clientes.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Manifestos, logs, capturas e resultados reais dos três cenários; as metas são aprovadas ou limitações registadas. A responsabilidade específica é validar cenário complexo com duas origens e cinco clientes. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Reutilizar CORE/topologies/cenario3.xml, inspirado na figura 1. O XML contém dez PCs, dez routers e cinco switches; os nomes Servidor/Cliente não limitam o papel do processo Java. Reaproveitar os PCs para duas origens, relays e pelo menos cinco clientes: uma distribuição possível é dois servidores, três relays e cinco clientes nos dez PCs existentes, com bootstrap no host S1. Definir vizinhanças e caminhos alternativos sobre os IPs reais, mantendo a overlay menor que a underlay. O número de cinco relays do desenho inicial era uma proposta, não requisito. S1 oferece A/B e S2 C/D. Não criar equipamentos/novo XML por defeito; só propor uma adaptação mínima se os requisitos não forem demonstráveis com os existentes, preservando o original.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existente: CORE/topologies/cenario3.xml; planeados: perfis .properties que mapeiam os processos para os equipamentos existentes e experiments/scenario-3/. Não há novo XML obrigatório.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Atribuir papéis aos equipamentos existentes e criar a tabela completa de processo→equipamento→IP/porto.
- [ ] Verificar alcance IP e grafo simétrico com origens e relays.
- [ ] Executar consumo combinado e concentração numa stream.
- [ ] Interromper um relay e uma origem separadamente, observando isolamento.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Nome de equipamento Servidor não prova processo de origem; um segundo processo no mesmo host não demonstra diversidade física. Perda de S1 não deve prometer recuperar A/B em S2, mas C/D devem continuar.

## 10. Testes necessários

V-027: quatro conteúdos identificáveis são recebidos de duas origens; cinco clientes funcionam dentro da capacidade configurada; falhas de uma origem não corrompem a outra; partilha no tronco tem evidência.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Mínimo de dois servidores com dois vídeos cada demonstrado no CORE.
- [ ] Cinco clientes e rotas alternativas têm evidência.
- [ ] Falhas e isolamento são descritos com limites.
- [ ] Validação V-027 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
