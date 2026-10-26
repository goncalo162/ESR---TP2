# TASK-026 — Demonstrar alternativas e adaptação no cenário de seis nós

## 1. Identificação

- ID: TASK-026
- Fase: [PHASE-06 — Integração e avaliação em CORE](../../phases/PHASE-06-CORE-VALIDATION.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-025](../PHASE-06/TASK-025.md).
- Requisitos relacionados: [RF-006](../../02-REQUIREMENTS.md#rf-006), [RF-009](../../02-REQUIREMENTS.md#rf-009), [RF-014](../../02-REQUIREMENTS.md#rf-014), [RNF-003](../../02-REQUIREMENTS.md#rnf-003), [RC-001](../../02-REQUIREMENTS.md#rc-001), [RP-005](../../02-REQUIREMENTS.md#rp-005).

## 2. Objetivo

Comprovar escolha por RTT, recuperação e retorno de nós em rede emulada.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Manifestos, logs, capturas e resultados reais dos três cenários; as metas são aprovadas ou limitações registadas. A responsabilidade específica é demonstrar alternativas e adaptação no cenário de seis nós. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Criar seis terminais overlay S,R1,R2,R3,C1,C2 e arestas S–R1,S–R2,R1–R3,R2–R3,R3–C1,R3–C2. Este grafo é uma variante do desenho sugerido na p. 5, mantendo seis nós e caminhos alternativos; não é uma transcrição literal da figura. Adicionar underlay suficiente sem confundir contagem. Fixar duas condições de atraso para tornar um caminho preferível e depois inverter. Testar separadamente morte de processo relay, quebra de TCP/UDP da aresta overlay e falha física com recuperação IP. Só a primeira/segunda prova demonstra inequivocamente adaptação da aplicação; registar a rota IP antes/depois da terceira.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existente: CORE/topologies/cenario2.xml; planeados: ott-cenario2.xml e experiments/scenario-2/.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Criar configuração e verificar dois percursos overlay válidos.
- [ ] Iniciar consumo partilhado e capturar seleção inicial.
- [ ] Aplicar falhas separadas com instantes registados e observar reativação.
- [ ] Alterar atraso sustentado e medir histerese, retomar nó e repetir.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

A alternativa overlay pode partilhar underlay com o ramo falhado; assegurar que a falha não corta os dois caminhos. Numa partição total esperar UNAVAILABLE, não recuperação.

## 10. Testes necessários

V-026: três repetições por condição; medir fim do vídeo antes e primeiro frame após falha; snapshots e logs mostram parent alterado; jitter pequeno não gera oscilações; um consumidor sair não interrompe o outro.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Caminhos e condições estão identificados.
- [ ] Recuperação é atribuível ao mecanismo correto.
- [ ] Resultados incluem sucesso, interrupção e limites reais.
- [ ] Validação V-026 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
