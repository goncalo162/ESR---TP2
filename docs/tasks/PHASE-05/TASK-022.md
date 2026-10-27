# TASK-022 — Reconstituir frames com buffers e prazo limitados

## 1. Identificação

- ID: TASK-022
- Fase: [PHASE-05 — Vídeo, replicação e reprodução](../../phases/PHASE-05-STREAMING.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Denji](../../team/DENJI.md).
- Revisor: [Waheed](../../team/WAHEED.md), antes de integrar e marcar DONE.
- Dependências: [TASK-019](../PHASE-05/TASK-019.md), [TASK-021](../PHASE-05/TASK-021.md).
- Requisitos relacionados: [RF-015](../../02-REQUIREMENTS.md#rf-015), [RNF-001](../../02-REQUIREMENTS.md#rnf-001), [RNF-003](../../02-REQUIREMENTS.md#rnf-003), [RP-003](../../02-REQUIREMENTS.md#rp-003), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Recuperar frames completos sob reordenação e descartar incompletos sem crescer memória.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa. A responsabilidade específica é reconstituir frames com buffers e prazo limitados. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Guardar até oito frames por stream/session, deadline de 250 ms desde primeiro fragmento. Validar consistência de todos os metadados e bitmap de fragmentos. Só entregar JPEG quando soma de bytes iguala frameLength e todos os índices estão presentes. Ordenar entrega por seq de frame e descartar frames mais antigos que o último mostrado; após timeout avançar sem esperar indefinidamente. Reinício de epoch limpa a sessão anterior. Relógio e frame sink são injetáveis.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existente: client/StreamingClient.java; planeados: client/FrameAssembler.java e FrameAssemblerTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Implementar montagem indexada e validação do tamanho exato.
- [ ] Eliminar duplicados e metadados contraditórios.
- [ ] Implementar deadline/overflow com descarte contabilizado.
- [ ] Entregar apenas frames completos e temporalmente admissíveis.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Último fragmento chega primeiro, falta um fragmento, burst de frames fora de ordem, frame incompleto mais antigo, colisão de seq entre sessões e quantidade excessiva de frames.

## 10. Testes necessários

V-022: permutações determinísticas recuperam frame; perda expira; duplicado não aumenta memória nem deadline; flood não supera oito montagens; epoch novo limpa buffers. O teste de JPEG corrompido pertence ao decoder/player em V-023.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Reordenação não corrompe frames completos.
- [ ] Perda e overflow têm fim e razão observáveis.
- [ ] Não existe espera ilimitada por um fragmento.
- [ ] Validação V-022 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
