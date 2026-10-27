# TASK-024 — Integrar duas origens e quatro streams ponta a ponta

## 1. Identificação

- ID: TASK-024
- Fase: [PHASE-05 — Vídeo, replicação e reprodução](../../phases/PHASE-05-STREAMING.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Denji](../../team/DENJI.md).
- Revisor: [Waheed](../../team/WAHEED.md), antes de integrar e marcar DONE.
- Dependências: [TASK-008](../PHASE-02/TASK-008.md), [TASK-018](../PHASE-04/TASK-018.md), [TASK-020](../PHASE-05/TASK-020.md), [TASK-021](../PHASE-05/TASK-021.md), [TASK-022](../PHASE-05/TASK-022.md), [TASK-023](../PHASE-05/TASK-023.md).
- Requisitos relacionados: [RF-007](../../02-REQUIREMENTS.md#rf-007), [RF-012](../../02-REQUIREMENTS.md#rf-012), [RF-013](../../02-REQUIREMENTS.md#rf-013), [RF-015](../../02-REQUIREMENTS.md#rf-015), [RP-001](../../02-REQUIREMENTS.md#rp-001), [RP-008](../../02-REQUIREMENTS.md#rp-008).

## 2. Objetivo

Obter versão integrada com todas as funcionalidades principais antes do CORE.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa. A responsabilidade específica é integrar duas origens e quatro streams ponta a ponta. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Compor transporte de dados, fontes, tabelas e cliente. Iniciar duas origens com dois vídeos cada, relays e pelo menos dois clientes em loopback com portas distintas. Catálogo deve expor quatro UUID e cada um ser reproduzível. Testar dois clientes na mesma stream, em streams diferentes e troca play/stop. Não forçar GUI em CI: receção automática usa headless e reprodução visual tem roteiro próprio. Atualizar comandos para os comportamentos agora disponíveis.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: app/OttApplication.java, OverlayNode.java, StreamingServer.java, StreamingClient.java e config/*.properties; planeado: integração MultiStreamIntegrationTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Integrar source demand com alterações da tabela ativa.
- [ ] Preencher perfis locais sem colisões e identificar vídeos/hashes.
- [ ] Executar casos multi-cliente e multi-stream com verificações de conteúdo.
- [ ] Registar evidência integrada e limitações antes de iniciar emulação.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Fonte B indisponível não afeta A, catálogo em atraso, desativação durante frame, cliente troca stream rapidamente e início de fontes depois dos clientes.

## 10. Testes necessários

V-024: duas origens anunciam e fornecem quatro vídeos; sinks confirmam stream/session/conteúdo; partilha e isolamento são medidos; GUI reproduz pelo menos uma amostra de cada UUID com evidência.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Quatro fluxos distintos são utilizáveis.
- [ ] Partilha e isolamento passam a validação local.
- [ ] Versão integrada encerra sem threads/sockets pendentes.
- [ ] Validação V-024 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
