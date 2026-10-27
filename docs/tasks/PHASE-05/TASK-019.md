# TASK-019 — Definir codec UDP e fragmentação de frames

## 1. Identificação

- ID: TASK-019
- Fase: [PHASE-05 — Vídeo, replicação e reprodução](../../phases/PHASE-05-STREAMING.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Waheed](../../team/WAHEED.md).
- Revisor: [Denji](../../team/DENJI.md), antes de integrar e marcar DONE.
- Dependências: [TASK-002](../PHASE-01/TASK-002.md), [TASK-004](../PHASE-01/TASK-004.md), [TASK-011](../PHASE-03/TASK-011.md), [TASK-018](../PHASE-04/TASK-018.md).
- Requisitos relacionados: [RF-010](../../02-REQUIREMENTS.md#rf-010), [RF-015](../../02-REQUIREMENTS.md#rf-015), [RNF-001](../../02-REQUIREMENTS.md#rnf-001), [RP-003](../../02-REQUIREMENTS.md#rp-003), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Converter um frame em datagramas limitados e validar cada fragmento recebido.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa. A responsabilidade específica é definir codec udp e fragmentação de frames. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Implementar o cabeçalho de 64 bytes e payload de 1136 bytes de docs/07. Não depender de uma socket ou ficheiro real de vídeo. Todas as partes do mesmo frame partilham seq, session e mediaTime. Validar count calculado pelo tamanho do frame e tamanho exato de cada payload. Usar UUID binário, i64 não negativo e rejeitar versão/flags desconhecidos. O protocolo é próprio, sem rótulo RTP.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existente: streaming/StreamPacket.java; planeados: streaming/StreamPacketCodec.java e StreamPacketCodecTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Criar parser e encoder com validação antes de alocação.
- [ ] Fragmentar JPEG opaco sem descodificar imagem.
- [ ] Criar golden fixtures independentes para offsets e UUID.
- [ ] Verificar tamanhos 1, 1136, 1137 e máximo permitido.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Frame de zero bytes, maior que 2 MiB, datagrama curto/extra, índice fora da gama, count incoerente e metadados com overflow.

## 10. Testes necessários

V-019: fixtures de bytes confirmam offsets 0–64; recombinação manual dos payloads recupera exatamente o frame; nenhum datagrama excede 1200 bytes; inputs inválidos são rejeitados.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Formato coincide com contrato v1.
- [ ] Limites são impostos antes de alocar.
- [ ] Fragmentação cobre todas as fronteiras sem bytes perdidos.
- [ ] Validação V-019 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.

### Referências fixadas pela TASK-002

Consumir [as fixtures UDP existentes](../../../Projeto/src/test/resources/protocol/README.md) e o manifesto, incluindo fronteira de 1137 bytes, metadados e casos inválidos. Os payloads são sintéticos e validam formato, não descodificação JPEG. Não atualizar bytes esperados a partir do encoder sob teste. Esta nota não inicia a TASK-019.
