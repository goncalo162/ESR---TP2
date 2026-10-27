# TASK-004 — Codificar e descodificar mensagens de controlo v1

## 1. Identificação

- ID: TASK-004
- Fase: [PHASE-01 — Fundações, configuração e protocolo](../../phases/PHASE-01-FOUNDATION.md)
- Prioridade: P0
- Estado: DONE
- Dependências: [TASK-002](../PHASE-01/TASK-002.md), [TASK-003](../PHASE-01/TASK-003.md).
- Requisitos relacionados: [RNF-005](../../02-REQUIREMENTS.md#rnf-005), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Implementar um codec TCP com contrato inequívoco e limites de memória.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Configuração e mensagens de controlo validadas por testes locais, sem necessidade de CORE. A responsabilidade específica é codificar e descodificar mensagens de controlo v1. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Implementar os tipos ativos 1, 2 e 4–17 e os tipos auxiliares da interface; código 3 retirado por ADR-013. REGISTER_OK contém apenas NeighborList, sem lease e com instanceId dos vizinhos obrigatoriamente zero. O codec trata bytes, sem ativar rotas ou abrir sockets. Validar comprimento antes de alocar, strings UTF-8 estritas, contagens, tipos e ausência de bytes sobrantes. Distinguir EOF limpo entre mensagens de EOF a meio do prefixo/corpo. Suportar ID de um carácter e UUID com bits de sinal preservados no envelope e campos opacos.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: protocol/MessageType.java, ControlMessage.java, MessageCodec.java e protocol/MessageCodecTest.java vazios; existentes desde TASK-002: [fixtures e manifesto](../../../Projeto/src/test/resources/protocol/README.md) em src/test/resources/protocol/; consumir sem regenerar expected bytes com o codec.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [x] Definir modelos de payload e enum com números estáveis.
- [x] Implementar escrita de frame e leitura completa limitada.
- [x] Validar payload específico e mapear erros de protocolo.
- [x] Comparar saída com fixtures independentes e testar duas mensagens concatenadas e leitura aos poucos.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

length negativo ao interpretar u32, frame acima de 64 KiB, UTF-8 inválido, type desconhecido, listas excessivas, socket que entrega um byte por leitura e EOF truncado.

## 10. Testes necessários

V-004: golden bytes para HELLO/ANNOUNCE/ACTIVATE/ERROR e descoberta, round-trip dos 16 tipos ativos, leituras parciais, concatenação e rejeições sem alocação não limitada. Rejeitar código 3, layout antigo de resposta com lease e vizinhos com instância viva na resposta bootstrap.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado atual da validação: V-004 EXECUTADA; 66 testes de codec/integração e os 65 testes de configuração aprovados no build limpo. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [x] Bytes correspondem ao contrato e às fixtures independentes.
- [x] Mensagens parciais não se confundem com mensagens completas.
- [x] Entrada malformada produz erro controlado.
- [x] Validação V-004 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Evidência real em [V-004](../../../experiments/task-004-codec-validation.md). Não foram executadas interações de rede.

## 13. Notas para a IA implementadora

Correção posterior por instrução do utilizador: o contrato atual substitui a renovação de registo por código 3 retirado, e a resposta bootstrap por NeighborList sem lease e com instâncias desconhecidas. [Revalidação](../../../experiments/bootstrap-discovery-correction.md): 131 testes aprovados. A tarefa continua DONE; o resultado original abaixo não deve ser usado para reintroduzir registo vivo ou renovação.

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.

### Resultado original — 27-10-2026

MessageType, ControlMessage e MessageCodec implementados, com ErrorCode/ProtocolException. Os 17 tipos e todos os layouts TCP têm golden bytes independentes; testes cobrem leitura parcial, concatenação, EOF, UTF-8, limites, valores unsigned, UUID e erro controlado. Todas as 45 fixtures mantêm hashes originais. Integração local carrega os perfis e produz/recupera HELLO, anúncios e resposta bootstrap em memória.

Build limpo: 131 testes (65 configuração + 66 codec), zero falhas/erros/skips; Java 21.0.12.1/Maven 3.6.3. Critérios desta tarefa satisfeitos, sem bloqueios. PHASE-01 concluída no âmbito definido. TASK-005 permanece TODO, não iniciada. Material de relatório: [notas da fase](../../PHASE-01-REPORT-NOTES.md).
