# TASK-023 — Apresentar catálogo e vídeo numa janela Swing

## 1. Identificação

- ID: TASK-023
- Fase: [PHASE-05 — Vídeo, replicação e reprodução](../../phases/PHASE-05-STREAMING.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-012](../PHASE-03/TASK-012.md), [TASK-022](../PHASE-05/TASK-022.md).
- Requisitos relacionados: [RF-015](../../02-REQUIREMENTS.md#rf-015), [RP-001](../../02-REQUIREMENTS.md#rp-001), [RP-003](../../02-REQUIREMENTS.md#rp-003).

## 2. Objetivo

Permitir escolher, ver e parar uma stream com feedback de disponibilidade.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa. A responsabilidade específica é apresentar catálogo e vídeo numa janela swing. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

GUI mínima com catálogo, seleção de stream e área de imagem; permitir operação equivalente pela CLI para diagnóstico. Receber/descodificar fora do EDT, apresentar imagem pronta via SwingUtilities.invokeLater. Manter apenas último frame pronto se EDT estiver lento, sem criar fila visual ilimitada. Fechar janela equivale a stop/quit e remove intenção local. Headless serve testes de pipeline, mas não satisfaz demonstração de reprodução visual.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existente: client/VideoPlayer.java e StreamingClient.java; planeados: client/VideoPlayerTest.java e roteiro de teste GUI.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Criar janela e seleção a partir do catálogo válido.
- [ ] Ligar play/stop aos contratos de subscrição sem lógica de roteamento na GUI.
- [ ] Descodificar JPEG e apresentar com EDT respeitado.
- [ ] Mostrar estado esperando/ativo/indisponível e limpar recursos ao fechar.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

DISPLAY ausente, runtime sem java.desktop, JPEG corrompido, stream retirada durante seleção e janela lenta e JPEG com bytes completos mas conteúdo inválido. Não implementar pause/seek ou autenticação.

## 10. Testes necessários

V-023: testar presenter e frame sink sem GUI; na bancada gráfica verificar vídeo, seleção e fecho manualmente, registando imagem/roteiro. Ausência de display é erro explicativo, não sucesso GUI.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Janela reproduz frames de conteúdo identificável.
- [ ] Seleção e stop respeitam uma subscrição local.
- [ ] Rede e descodificação não bloqueiam EDT.
- [ ] Validação V-023 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
