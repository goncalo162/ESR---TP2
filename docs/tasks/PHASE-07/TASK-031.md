# TASK-031 — Preparar defesa e checklist de submissão humana

## 1. Identificação

- ID: TASK-031
- Fase: [PHASE-07 — Relatório, empacotamento e defesa](../../phases/PHASE-07-DELIVERY.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-009](../PHASE-02/TASK-009.md), [TASK-018](../PHASE-04/TASK-018.md), [TASK-030](../PHASE-07/TASK-030.md).
- Requisitos relacionados: [RNF-005](../../02-REQUIREMENTS.md#rnf-005), [RC-005](../../02-REQUIREMENTS.md#rc-005), [RC-006](../../02-REQUIREMENTS.md#rc-006), [RC-007](../../02-REQUIREMENTS.md#rc-007).

## 2. Objetivo

Concluir preparação de defesa e orientar submissão no grupo correto.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: PDF e ZIP com nomes corretos, reprodução após extração limpa e checklist de submissão com confirmação humana. A responsabilidade específica é preparar defesa e checklist de submissão humana. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Preparar demonstração final que cubra overlay 10%, gestão de fluxos 25%, controlo 15%, streaming 10% e integração 30%, além dos dois marcos de 5% cada. Incluir scripts/roteiros locais, fallback de evidência gravada e explicação das decisões. Confirmar prazo e grupo no Blackboard com a equipa. A equipa faz upload via troca de ficheiros do grupo e regista confirmação; não automatizar login nem pedir credenciais à IA. Só considerar projeto entregue após confirmação humana da submissão.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Planeados: report/defense.md e delivery/submission-checklist.md.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Ensaiar roteiro de quatro streams, partilha, falha, adaptação e limites.
- [ ] Verificar correspondência dos artefactos com nomes/hashes finais.
- [ ] Listar passos do upload no grupo correto e confirmação a guardar.
- [ ] Registar data real e confirmação da equipa após submissão.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Blackboard indisponível, prazo não fornecido, ficheiro rejeitado e diferença entre versão defendida e entregue. Preparação concluída não equivale a submissão concluída.

## 10. Testes necessários

V-031: ensaio cronometrado reproduz tópicos de avaliação; revisão dos dois nomes e grupo; confirmação humana documenta upload correto sem exposição de credenciais.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Defesa e evidências estão preparadas.
- [ ] Checklist identifica prazo, grupo e artefactos finais.
- [ ] DONE desta tarefa exige confirmação humana de submissão.
- [ ] Validação V-031 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
