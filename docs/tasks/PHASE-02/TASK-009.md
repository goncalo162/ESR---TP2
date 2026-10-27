# TASK-009 — Preparar evidência do primeiro marco intermédio

## 1. Identificação

- ID: TASK-009
- Fase: [PHASE-02 — Overlay, bootstrap e ciclo de vida](../../phases/PHASE-02-OVERLAY.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-008](../PHASE-02/TASK-008.md).
- Requisitos relacionados: [RNF-005](../../02-REQUIREMENTS.md#rnf-005), [RC-007](../../02-REQUIREMENTS.md#rc-007).

## 2. Objetivo

Conseguir demonstrar construção da overlay e explicar protocolo de controlo.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. A responsabilidade específica é preparar evidência do primeiro marco intermédio. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Preparar uma demonstração curta com bootstrap e três nós, início fora de ordem, HELLO, saída e reconexão. Registar comandos realmente executados, versões, configuração e observações. Confirmar data/conteúdo da primeira avaliação com docente; o plano não determina calendário académico. Não usar esta evidência para afirmar streaming ou roteamento ainda ausentes.

Depois de todos obterem os vizinhos, parar o bootstrap e repetir a reconexão entre nós já descobertos. Registar que o bootstrap só participou na descoberta inicial e não recebe renovações, sondas ou avisos de saída. Um processo novo sem lista inicial continua a precisar do bootstrap; não confundir esse caso com reconexão dos existentes.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Planeados: experiments/milestone-1/README.md, configurações e roteiro; existentes: docs/15 e docs/12.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Escrever roteiro com sequência de arranque e resultado esperado.
- [ ] Executar e recolher logs reais na futura construção.
- [ ] Relacionar RF-001–004 com evidências e limitações.
- [ ] Registar data/feedback docente quando fornecidos.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Avaliação pode exigir outro conteúdo ou ocorrer antes desta fase. Ajustar marco e prioridades sem saltar dependências; ausência de data não impede preparar material.

## 10. Testes necessários

V-009: outro elemento consegue repetir a demonstração e identificar consulta inicial, handshake, saída e reconexão nos logs; os nós já descobertos reconectam com bootstrap parado, sem tráfego periódico para ele.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Roteiro repetível e logs datados disponíveis.
- [ ] Limitações da versão intermédia explicitadas.
- [ ] Situação da avaliação registada sem datas inventadas.
- [ ] Validação V-009 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
