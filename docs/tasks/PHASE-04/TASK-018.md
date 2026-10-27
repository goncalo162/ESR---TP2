# TASK-018 — Preparar o segundo marco com árvores e adaptação

## 1. Identificação

- ID: TASK-018
- Fase: [PHASE-04 — Árvores de distribuição e adaptação](../../phases/PHASE-04-FLOW-MANAGEMENT.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Denji](../../team/DENJI.md).
- Revisor: [Waheed](../../team/WAHEED.md), antes de integrar e marcar DONE.
- Dependências: [TASK-009](../PHASE-02/TASK-009.md), [TASK-013](../PHASE-03/TASK-013.md), [TASK-015](../PHASE-04/TASK-015.md), [TASK-016](../PHASE-04/TASK-016.md), [TASK-017](../PHASE-04/TASK-017.md).
- Requisitos relacionados: [RNF-005](../../02-REQUIREMENTS.md#rnf-005), [RC-007](../../02-REQUIREMENTS.md#rc-007).

## 2. Objetivo

Demonstrar gestão de fluxos e protocolo de controlo antes da integração final.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Dois consumidores partilham ativação, remoção de um preserva o outro, falha troca upstream e segundo marco fica reproduzível. A responsabilidade específica é preparar o segundo marco com árvores e adaptação. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Usar múltiplas streams anunciadas e dois consumidores simulados/reais de controlo para demonstrar ativação agregada, poda, trace, falha e alteração sustentada de custo. Sem vídeo ainda, os dados podem ser mensagens sintéticas de diagnóstico claramente identificadas; não apresentar vídeo como funcional. Datas/conteúdo da segunda avaliação dependem do docente.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Planeados: experiments/milestone-2/README.md, logs e sequência de falhas; existentes: docs/15 e docs/12.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Preparar roteiro com caminhos e resultados esperados.
- [ ] Executar cenário de controlo e guardar logs correlacionados.
- [ ] Relacionar snapshots antes/depois e estados de subscrição.
- [ ] Registar feedback e limitações para fase de vídeo.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Se avaliação exigir vídeo, reagendar marco após TASK-024 por decisão documentada; não marcar TASK-024 como dispensada.

## 10. Testes necessários

V-018: reprodução independente da ativação/poda/failover usando logs e snapshots; revisão do material contra os critérios de gestão de fluxos e controlo do PDF.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Demonstração mostra isolamento entre fluxos e procura agregada.
- [ ] Falha e alteração de RTT têm evidência verificável.
- [ ] Conteúdo e estado do marco estão registados.
- [ ] Validação V-018 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
