# TASK-002 — Fixar contratos, fixtures e decisões pendentes

> Atualização posterior: a execução original abaixo criou 17 tipos aceites. ADR-013 retirou a renovação de registo e reviu a resposta bootstrap; o inventário atual mantém 45 casos, com 16 tipos ativos. [Correção e revalidação](../../../experiments/bootstrap-discovery-correction.md). A tarefa mantém-se DONE.

## 1. Identificação

- ID: TASK-002
- Fase: [PHASE-01 — Fundações, configuração e protocolo](../../phases/PHASE-01-FOUNDATION.md)
- Prioridade: P1
- Estado: DONE
- Dependências: [TASK-001](../PHASE-01/TASK-001.md).
- Requisitos relacionados: [RNF-005](../../02-REQUIREMENTS.md#rnf-005).

## 2. Objetivo

Eliminar divergências de formato antes de criar codecs e verificar as dependências externas de vídeo.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Configuração e mensagens de controlo validadas por testes locais, sem necessidade de CORE. A responsabilidade específica é fixar contratos, fixtures e decisões pendentes. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Rever limites de 37 bytes do envelope mínimo, tipos de mensagem e cabeçalho UDP de 64 bytes. Definir fixtures em hexadecimal com valores calculados independentemente da implementação; não gerar expected bytes com o próprio codec. Verificar acesso aos recursos docentes, autoria e formato por inspeção; ainda não implementar o leitor. Identificar quatro conteúdos de teste, dois distintos por origem. Confirmar com o docente apenas decisões que exijam esclarecimento, registando H-01 e H-02 como hipóteses até haver resposta.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existente: docs/02, 06, 07 e 12; planeado: Projeto/src/test/resources/protocol/README.md e fixtures binárias de referência.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [x] Rever o PDF contra RF/RC e distinguir recomendações.
- [x] Produzir exemplos mínimos válidos e malformados dos contratos de controlo e dados.
- [x] Verificar acesso a ProgEx.zip/movie.Mjpeg e registar origem/hash/formato se obtidos.
- [x] Registar decisões ou bloqueios concretos em docs/12 sem alterar requisitos obrigatórios.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

ZIP inacessível ou sem autorização de redistribuição; incoerência de campo; inexistência de vídeo distinto. Pode concluir o contrato de controlo e registar gate de TASK-020 sem fingir que o recurso existe.

## 10. Testes necessários

V-002: revisão independente dos comprimentos, offsets, exemplos de identidade e tabela de requisitos; evidência de acesso ou falha de recurso, sem execução de streaming.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado atual da validação: V-002 EXECUTADA com sucesso, sem testes funcionais da aplicação. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [x] Fixtures têm bytes e interpretação explícitos.
- [x] Não há conflito entre modelo e interface.
- [x] Situação dos recursos e hipóteses está registada; gates futuros têm tarefa responsável.
- [x] Validação V-002 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Evidência de execução em [experiments/task-002-contract-review.md](../../../experiments/task-002-contract-review.md). RNF-005 continua parcialmente coberto.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.

### Resultado da execução — 26-10-2026

45 pares .bin/.hex, manifesto e README criados e inspecionados: os 17 tipos TCP, limites/UTF-8 e cabeçalhos/fragmentos UDP têm exemplos e oráculos. Corrigido o invariante de serverEpoch e fixados os limites do perfil MJPEG de cinco dígitos. Recurso docente obtido com hash e 500 frames inspecionados; quatro conteúdos identificados, restantes ficheiros por preparar em TASK-020. Utilizador sem indicações docentes adicionais conhecidas; nenhuma mensagem foi enviada ao docente.

Todos os critérios cumpridos; nenhum bloqueio atual desta tarefa. Fontes Java, POM e CORE preservados. TASK-003 é a próxima tarefa, ainda TODO e não iniciada. [Revisão completa](../../../experiments/task-002-contract-review.md) e [fixtures](../../../Projeto/src/test/resources/protocol/README.md).
