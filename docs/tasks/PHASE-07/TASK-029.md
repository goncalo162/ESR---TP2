# TASK-029 — Escrever artigo com protocolos e avaliação crítica

## 1. Identificação

- ID: TASK-029
- Fase: [PHASE-07 — Relatório, empacotamento e defesa](../../phases/PHASE-07-DELIVERY.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Fillmore](../../team/FILLMORE.md).
- Revisor: [Denji](../../team/DENJI.md), antes de integrar e marcar DONE.
- Dependências: [TASK-028](../PHASE-06/TASK-028.md).
- Requisitos relacionados: [RNF-005](../../02-REQUIREMENTS.md#rnf-005), [RC-002](../../02-REQUIREMENTS.md#rc-002), [RC-003](../../02-REQUIREMENTS.md#rc-003), [RP-007](../../02-REQUIREMENTS.md#rp-007).

## 2. Objetivo

Produzir relatório em formato de artigo com até 12 páginas e resultados verificáveis.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: PDF e ZIP com nomes corretos, reprodução após extração limpa e checklist de submissão com confirmação humana. A responsabilidade específica é escrever artigo com protocolos e avaliação crítica. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Adotar LNCS recomendado, sem o tratar como formato obrigatório do PDF. Organizar introdução, arquitetura, especificação de mensagens/interações, implementação, testes/resultados e conclusões/trabalho futuro. Descrever o protocolo efetivamente implementado, incluindo diferenças aprovadas face ao plano. Citar exemplos docentes usados e fontes técnicas. Contar todas as páginas, incluindo bibliografia; não usar anexos para contornar o limite.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Planeados: report/ com fontes e PDF; existentes: docs/07, docs/12 e evidências de experiments/.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Selecionar evidências que cubram pesos de avaliação.
- [ ] Escrever desenho, invariantes, mensagens e trade-offs.
- [ ] Incluir metodologia, resultados reais e crítica das falhas/limites.
- [ ] Gerar PDF e verificar número de páginas e legibilidade.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Gráfico sem unidade, resultados sem configuração, afirmação de RTP indevida, relatório descreve código planeado mas não existente e páginas extra de referências.

## 10. Testes necessários

V-029: revisão por checklist do enunciado; correspondência de figuras/tabelas com dados; contagem de páginas do PDF até 12; protocolo no artigo corresponde aos bytes e comportamento implementados.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Artigo cobre arquitetura, protocolos, implementação e avaliação.
- [ ] Até 12 páginas no ficheiro final.
- [ ] Todas as afirmações experimentais têm evidência e limites.
- [ ] Validação V-029 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
