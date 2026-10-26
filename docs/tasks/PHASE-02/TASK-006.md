# TASK-006 — Implementar registo bootstrap e descoberta configurada

## 1. Identificação

- ID: TASK-006
- Fase: [PHASE-02 — Overlay, bootstrap e ciclo de vida](../../phases/PHASE-02-OVERLAY.md)
- Prioridade: P0
- Estado: TODO
- Dependências: [TASK-003](../PHASE-01/TASK-003.md), [TASK-005](../PHASE-02/TASK-005.md).
- Requisitos relacionados: [RF-002](../../02-REQUIREMENTS.md#rf-002), [RF-003](../../02-REQUIREMENTS.md#rf-003), [RF-004](../../02-REQUIREMENTS.md#rf-004).

## 2. Objetivo

Devolver a um nó conhecido os endpoints dos seus vizinhos permitidos.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. A responsabilidade específica é implementar registo bootstrap e descoberta configurada. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Usar o grafo simétrico carregado e a mesma interface TCP para REGISTER/OK/RENEW/LEAVE. Registar instância e lease, rejeitar identidade não configurada ou endpoint/papel incompatível. Responder também com vizinhos offline, para não depender da ordem de arranque. Registo concorrente com mesmo nodeId/instância é idempotente; nova instância só substitui a anterior após saída, lease expirado ou verificação explícita de falha, evitando expulsão arbitrária.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: bootstrap/BootstrapServer.java, NodeRegistry.java e bootstrap/BootstrapServerTest.java; planeado: fixtures topology.properties.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Criar registo e resposta de vizinhança a partir do grafo.
- [ ] Implementar renovação, expiração e saída normal.
- [ ] Validar duplicados e endpoints face à configuração.
- [ ] Testar dois registos simultâneos e vizinho ainda offline.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Nó não permitido, registo repetido, mesmo ID vivo em duas instâncias, renovação tardia, topologia assimétrica e bootstrap indisponível. Não calcular rotas nem transportar streams.

## 10. Testes necessários

V-006: registos recebem só os vizinhos definidos; lease expira com relógio falso; novo arranque pode recuperar identidade após expiração; tráfego de vizinhos não passa pelo bootstrap.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Resposta corresponde ao grafo e respeita X por nó.
- [ ] Membership expira sem remover configuração estática.
- [ ] Erros não substituem um nó ativo indevidamente.
- [ ] Validação V-006 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
