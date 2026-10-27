# TASK-028 — Recolher métricas e avaliar resultados e limites

## 1. Identificação

- ID: TASK-028
- Fase: [PHASE-06 — Integração e avaliação em CORE](../../phases/PHASE-06-CORE-VALIDATION.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Denji](../../team/DENJI.md).
- Revisor: [Fillmore](../../team/FILLMORE.md), antes de integrar e marcar DONE.
- Dependências: [TASK-025](../PHASE-06/TASK-025.md), [TASK-026](../PHASE-06/TASK-026.md), [TASK-027](../PHASE-06/TASK-027.md).
- Requisitos relacionados: [RF-008](../../02-REQUIREMENTS.md#rf-008), [RF-011](../../02-REQUIREMENTS.md#rf-011), [RNF-001](../../02-REQUIREMENTS.md#rnf-001), [RNF-002](../../02-REQUIREMENTS.md#rnf-002), [RNF-005](../../02-REQUIREMENTS.md#rnf-005), [RC-001](../../02-REQUIREMENTS.md#rc-001), [RP-008](../../02-REQUIREMENTS.md#rp-008).

## 2. Objetivo

Quantificar eficiência e experiência observada com experiências repetíveis.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Manifestos, logs, capturas e resultados reais dos três cenários; as metas são aprovadas ou limitações registadas. A responsabilidade específica é recolher métricas e avaliar resultados e limites. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Executar protocolo de docs/09: warm-up de 10 s, medição de 60 s e três repetições por condição, com versões/seed/perfis fixos. Comparar 1/2/5 clientes, latência de caminho e duas taxas de perda. Medir RTT, FPS completos/mostrados, bytes por ligação lógica, overhead de controlo, interrupção de recuperação e recursos. Timestamp de media não permite atraso unidirecional; se o medir, documentar referência comum e erro de relógio. Avaliar metas técnicas e justificar ajustes sem esconder resultados desfavoráveis.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Planeados: experiments/*/RUN-ID/manifest.md, metrics.csv, capturas selecionadas e resumo-results.md.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Definir manifestos e formato CSV com unidade para cada métrica.
- [ ] Executar repetições e preservar dados brutos e hashes.
- [ ] Agregar mediana, amplitude/p95 quando amostras suficientes e comparar condições.
- [ ] Registar metas cumpridas, falhas, tuning e limitações para relatório.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Captura perde pacotes, relógios não sincronizados, tráfego background, primeiro frame incompleto e largura de banda insuficiente para a soma das quatro streams. Não inferir ausência de redundância a partir de total de bytes sem chaves de pacote.

## 10. Testes necessários

V-028: auditoria cruzada entre capturas, contadores e eventos de rota; tabela reproduzível de condições/resultados; análise distingue perda de datagrama, frame incompleto e frame não mostrado.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Resultados são reais, datados e repetíveis.
- [ ] Critérios de eficiência e adaptação têm evidência.
- [ ] Limitações e desvios de metas estão documentados.
- [ ] Validação V-028 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
