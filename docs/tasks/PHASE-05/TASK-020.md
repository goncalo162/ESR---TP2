# TASK-020 — Ler fontes MJPEG e controlar ritmo de publicação

## 1. Identificação

- ID: TASK-020
- Fase: [PHASE-05 — Vídeo, replicação e reprodução](../../phases/PHASE-05-STREAMING.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-002](../PHASE-01/TASK-002.md), [TASK-003](../PHASE-01/TASK-003.md), [TASK-019](../PHASE-05/TASK-019.md).
- Requisitos relacionados: [RF-012](../../02-REQUIREMENTS.md#rf-012), [RF-013](../../02-REQUIREMENTS.md#rf-013), [RF-015](../../02-REQUIREMENTS.md#rf-015), [RNF-001](../../02-REQUIREMENTS.md#rnf-001), [RP-003](../../02-REQUIREMENTS.md#rp-003).

## 2. Objetivo

Produzir frames válidos de vídeos distintos com pacing e procura controlada.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa. A responsabilidade específica é ler fontes mjpeg e controlar ritmo de publicação. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Gate: obter e inspecionar movie.Mjpeg/exemplo docente ou confirmar alternativa com o docente, como permite a etapa 4. Não assumir AVI, MP4 nem JPEG concatenado: verificar o delimitador real, incluindo eventual prefixo ASCII de comprimento. Definir leitor para o formato confirmado e documentar o perfil. Validar ficheiros e JPEG com limites antes de anunciar disponibilidade. Cada stream tem scheduler, sequência e posição independentes. Sem procura, parar leitura/envio; em loop manter seq e timeline; loop=false termina apenas essa stream.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: server/VideoSource.java e StreamingServer.java; planeados: server/VideoSourceTest.java e fixtures MJPEG pequenas autorizadas.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Confirmar formato, proveniência e licença dos recursos; atualizar H-02.
- [ ] Implementar leitura de um frame com EOF e truncatura explícitos.
- [ ] Configurar dois vídeos distintos por cada servidor e registar hashes.
- [ ] Agendar emissão ao FPS sem catch-up ilimitado e só com procura confirmada.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Fonte ausente, cabeçalho inválido, frame JPEG ilegível, leitura parcial, vídeo termina durante subscrição e loop curto. Se recurso estiver indisponível, marcar esta tarefa BLOCKED na construção; não inventar leitor.

## 10. Testes necessários

V-020: fixture confirmada com dois frames produz bytes esperados; ficheiro truncado falha de forma localizada; relógio falso confirma pacing/ausência de envio sem procura; quatro conteúdos são identificáveis.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Formato real está documentado e testado.
- [ ] Há dois conteúdos diferentes por origem na configuração de entrega.
- [ ] Ritmo, EOF e loop têm comportamento determinado.
- [ ] Validação V-020 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
