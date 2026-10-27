# TASK-025 — Preparar CORE e comprovar replicação no cenário de quatro nós

## 1. Identificação

- ID: TASK-025
- Fase: [PHASE-06 — Integração e avaliação em CORE](../../phases/PHASE-06-CORE-VALIDATION.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Waheed](../../team/WAHEED.md).
- Revisor: [Denji](../../team/DENJI.md), antes de integrar e marcar DONE.
- Dependências: [TASK-024](../PHASE-05/TASK-024.md).
- Requisitos relacionados: [RF-011](../../02-REQUIREMENTS.md#rf-011), [RNF-004](../../02-REQUIREMENTS.md#rnf-004), [RC-001](../../02-REQUIREMENTS.md#rc-001), [RP-005](../../02-REQUIREMENTS.md#rp-005), [RP-008](../../02-REQUIREMENTS.md#rp-008).

## 2. Objetivo

Validar replicação em quatro processos overlay numa bancada CORE reproduzível.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Manifestos, logs, capturas e resultados reais dos três cenários; as metas são aprovadas ou limitações registadas. A responsabilidade específica é preparar core e comprovar replicação no cenário de quatro nós. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Reutilizar CORE/topologies/cenario1.xml existente, sem criar uma topologia alternativa por defeito. Confirmar JDK/GUI e mapear os equipamentos atuais para os processos: Servidor→S, Router1→R, Cliente1→C1 e Cliente2→C2. No CORE é possível executar o relay no namespace Linux do router, como admite o enunciado (p. 2); a recomendação de terminais não obriga a recriar o cenário. Bootstrap é serviço adicional no host de S. Arestas overlay propostas S–R,R–C1,R–C2; X=1,3,1,1. Derivar os IPs/interfaces do XML e da sessão real; substituir os endpoints 127.0.0.1 dos exemplos locais por endereços CORE. Confirmar alcance antes de avaliar vídeo. Só propor alterações mínimas ao XML se uma limitação concreta impedir requisitos, preservando o original e registando motivo.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: CORE/container/Dockerfile, docker-compose.yml e CORE/topologies/cenario1.xml; planeados: perfis .properties específicos deste cenário em CORE/volume/ott/ e evidências em experiments/scenario-1/. Não há novo XML obrigatório.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Validar runtime, X11, volume /volume e MTU, sem permissões globais desnecessárias.
- [ ] Abrir cenario1.xml existente e preparar a tabela processo→equipamento→IP/porto e os perfis de configuração correspondentes.
- [ ] Copiar JAR/config/media e iniciar manualmente por terminal de nó CORE.
- [ ] Capturar tronco e ramos durante play de um e dois clientes e após stop.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

JAR não montado, JAVA_HOME incompatível com arquitetura, JDK headless, IP publicitado errado e captura da mesma trama em interfaces diferentes. Não multiplicar contagem por capturar vários pontos da underlay.

## 10. Testes necessários

V-025: no estado estável comparar chaves (stream,session,seq,index) por ligação lógica; S→R tem uma cópia, R→C1 e R→C2 uma cada; depois do último stop não há datagramas de vídeo após o prazo de poda.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Cenário tem quatro processos overlay identificados.
- [ ] Mapeamento overlay→host→IP/porto é reproduzível.
- [ ] Capturas e counters demonstram cópia única no tronco.
- [ ] Validação V-025 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
