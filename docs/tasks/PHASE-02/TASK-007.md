# TASK-007 — Gerir handshake, unicidade e reconexão de vizinhos

## 1. Identificação

- ID: TASK-007
- Fase: [PHASE-02 — Overlay, bootstrap e ciclo de vida](../../phases/PHASE-02-OVERLAY.md)
- Prioridade: P0
- Estado: IN PROGRESS
- Responsável: [Waheed](../../team/WAHEED.md).
- Revisor: [Denji](../../team/DENJI.md), antes de integrar e marcar DONE.
- Dependências: [TASK-005](../PHASE-02/TASK-005.md), [TASK-006](../PHASE-02/TASK-006.md).
- Requisitos relacionados: [RF-002](../../02-REQUIREMENTS.md#rf-002), [RF-003](../../02-REQUIREMENTS.md#rf-003), [RF-004](../../02-REQUIREMENTS.md#rf-004), [RNF-004](../../02-REQUIREMENTS.md#rnf-004), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Manter uma única ligação lógica ativa por par permitido.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. A responsabilidade específica é gerir handshake, unicidade e reconexão de vizinhos. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Aplicar regra de iniciador por ordem de nodeId, validar HELLO/HELLO_OK e associar endpoint UDP publicitado à conexão. Só publicar ACTIVE após handshake válido. Agendar retries com backoff e jitter; vizinhos offline continuam desejados. Comparar connectionId e instanceId nos eventos para que a morte de uma conexão antiga não remova a nova.

A lista inicial do bootstrap é configuração, sem estado de disponibilidade ou instância conhecida. Depois de obtida, manter e reconectar diretamente aos mesmos vizinhos, mesmo com bootstrap desligado. Não consultar bootstrap periodicamente ou numa falha de vizinho. Identidade de execução e duplicados são tratados localmente em HELLO, nunca por registo central ou lease de bootstrap.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: overlay/Neighbor.java, NeighborManager.java, NodeRole.java e overlay/NeighborManagerTest.java; planeados: eventos imutáveis de vizinhança.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Obter uma resposta inicial do bootstrap, fechar a conexão de consulta e iniciar os dials devidos.
- [ ] Validar identidade, permissões e endpoints no HELLO.
- [ ] Eliminar duplicados e publicar eventos de entrada/saída.
- [ ] Repetir conexão após falha até shutdown, mantendo estado observável.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Início simultâneo, IDs iguais, HELLO em atraso, peer não autorizado e regresso do iniciador após crash. Não usar heurística de primeiro socket que chega.

## 10. Testes necessários

V-007: três nós iniciados em ordens diferentes convergem para o grafo esperado sem duplicados; novo instanceId após reinício não é apagado por callback antigo; vizinho desconhecido é rejeitado; após descoberta, desligar bootstrap e comprovar reconexão direta sem novas consultas.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Uma conexão confirmada por vizinho.
- [ ] Entrada tardia e reconexão funcionam.
- [ ] Endereço UDP só é aceite após handshake.
- [ ] Validação V-007 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.

## Construction — Model para revisão

Iniciado em 2026-11-10T14:01:42Z por seleção explícita do utilizador, apenas Model.
Base Denji 26aaa4ae6775e9c78e1d7658472ccaca0b376427, igual a main/referências locais de origin no arranque;
dependências IMP-001/003 complete/integradas. IMP-002 já disponibiliza runtime.
[Modelo](../../../memory-bank/bolts/003-overlay/ddd-01-domain-model.md) preparado em 2026-11-10T14:11:43Z:
lista desejada/identidades, estado serial, handshake v1/UDP confirmado,
unicidade/reinício, retry com prazo e M01–M22 para V-007. Três ficheiros autorizados
existem vazios; os 155 ficheiros de Projeto/ mantêm os hashes da base.
Model para aprovação, sem código/build/testes novos, V-007 NÃO EXECUTADO.
Parar antes de Design/ADR/Implement/Test, sem commits/push/fecho. Revisor final
atribuído Denji, revisão e integração exigidas antes de DONE.

### Ajuste pedido ao Model — ERROR (17)

Preparado em 2026-11-10T14:24:46Z, sem aprovação de Model. A
[secção 8 revista](../../../memory-bank/bolts/003-overlay/ddd-01-domain-model.md#8-autorização-error-17-e-entrega-ao-consumidor)
define correlação de ERROR por descoberta/handshake e entrega integral de ERROR
ACTIVE ao consumidor responsável com a identidade da sessão. Não responder ERROR
a ERROR, não consumir silenciosamente erros de protocolos futuros.
M02/M07/M11 ampliados; [Q-C-05](../../construction-duvidas.md#q-c-05--receção-de-error-no-imp-004)
regista retry/rejeição de HELLO_OK/ERROR inesperado como proposta pendente.
V-007 não executado; sem Design, alteração Java, commit ou push.

### Model/Q-C-05 aprovados — Design para revisão

Em 2026-11-10T14:48:50Z, o utilizador aprovou Model revisto e todas as propostas da tabela
Q-C-05, incluindo diagnóstico/fecho local de ERROR inesperado antes de ACTIVE.
[Desenho](../../../memory-bank/bolts/003-overlay/ddd-02-technical-design.md) preparado em 2026-11-10T15:00:14Z:
assinaturas e records imutáveis, owner serial partilhável, retenção B/R, worker
DNS/I/O limitado, dials correlacionados antes de classificar direção, budgets e
D01–D22 previstos. Esta aprovação resolve as pendências Model/Q-C-05 acima.
Só Design autorizado; parar para revisão antes de ADR/Implement/Test.
Nenhuma alteração Java nem build/testes/manuais novos; V-007 NÃO EXECUTADO.
Revisor final Denji e integração exigidos antes de DONE; sem commit/push/fecho.

### Design/Q-C-06 aprovados — revisão ADR preparada

Aprovação explícita em 2026-11-10T15:13:50Z; orçamento próprio de resolução/bind definido
em [Q-C-06](../../construction-duvidas.md#q-c-06--prazo-próprio-de-resolução-e-bind-no-imp-004), sem alterar timeout/API do dial TCP.
[Revisão ADR](../../../memory-bank/bolts/003-overlay/adr-review.md) preparada em 2026-11-10T15:20:09Z:
owner próprio/emprestado, B/R, limites físicos e correlação dos dials conformes
no desenho; Q-C-05 preservada, sem decisão aceite inviável ou ADR nova necessária
identificada. D01–D22 intactos, V-007 NÃO EXECUTADO; 155 hashes preservados.
Parar para revisão antes de Implement/Test. Sem Java/build/testes/commit/push
ou fecho. Revisão final atribuída a Denji e integração continuam pendentes.

## Implement preparado — aprovação pendente

ADR explicitamente aprovada; Q-C-07 resolvida com reposição externa autorizada
de EventLoop/RealDeadlines. [Implement para checkpoint](../../../memory-bank/bolts/003-overlay/implementation-walkthrough.md)
preparado em 2026-11-10T16:12:36Z. Regressão incremental: `mvn -B -ntp test`,
903 testes (740 configuração +66 codec +31 TCP +19 bootstrap +47 vizinhança),
zero falhas/erros/ignorados. V-007 tem execução automatizada TCP local;
Q-C-05/Q-C-06 e D01–D22 preservados. Três ficheiros do IMP +1745/−0;
BootstrapServer externo +482/−142 separado, 151 ficheiros restantes preservados.
Implement ainda aguarda aprovação; Test/clean verify/JAR/manual não iniciados,
revisão final atribuída a Denji e integração pendentes. TASK/bolt/história/unidade
continuam em progresso, sem implemented=true, commits/push ou fecho.

## IMP-004 — Test preparado, aceitação pendente

Implement aprovado explicitamente pelo utilizador; [relatório Test](../../../memory-bank/bolts/003-overlay/ddd-03-test-report.md)
preparado em 2026-11-10T16:26:35Z. Clean verify atual: 903 testes (47 de vizinhança),
zero falhas/erros/ignorados. Manual interativo executado pelo agente Codex no
JAR verificado: 49 CHECK_PASS, MANUAL_PASS/CLEANUP_COMPLETE, saída Java 0.
XML/JAR/hash/perfis/log e instruções de repetição pessoal preservados em
experiments/construction/003-overlay/. Código aprovado e ferramentas intactos
durante Test. V-007/D01–D22 têm consolidação técnica local, com limites explícitos.
Q-C-08 continua aberta: V-ADD reprovado com três erros, sem alteração de ferramentas
ou redução do alcance. Test ainda não aprovado; revisão final humana e integração
pendentes. TASK/bolt/história/unidade permanecem em progresso, sem implemented=true,
commits/push/fecho ou GUI/CORE presumidos.

## Manual humano aprovado — Test ainda aberto

Registo de conferência em 2026-11-10T16:38:44Z.
Manual humano executado e aprovado por Francisco, confirmado pelo utilizador.
Conferência do log original: 49 CHECK_PASS distintos, zero CHECK_FAIL,
MANUAL_PASS CHECKS=49 e CLEANUP_COMPLETE; hash do JAR coincide com o clean verify
e os cinco hashes de configuração coincidem com os ficheiros da execução.
JAVA_EXIT=0 é confirmação explícita do utilizador; esse marcador não consta
do log original do driver, que foi preservado sem edição.
Test continua por aprovar até tratar V-ADD. Q-C-08 continua aberta; correção
mínima preparada e ensaiada apenas em cópia isolada, não aplicada às ferramentas.
Sem fecho/commits/push; revisão humana final e integração continuam pendentes.

[Log Francisco](../../../experiments/construction/003-overlay/manual-Francisco-execucao-01/manual.log) · [Conferência](../../../experiments/construction/003-overlay/manual-Francisco-evidence.json) · [Proposta Q-C-08](../../../experiments/construction/003-overlay/q-c-08-proposal.md).

## Test para aprovação — Q-C-08 resolvida

Estado registado em 2026-11-10T16:41:30Z. Patch Q-C-08 aplicado exclusivamente
como autorizado: [diff +5/−3](../../../experiments/construction/003-overlay/q-c-08-applied.diff), três ficheiros, restante conteúdo
local preservado. [V-ADD real](../../../experiments/construction/003-overlay/q-c-08-v-add-applied.log) passou: saída 0, zero erros;
validador/alcance intactos. Q-C-08 resolvida. As anteriores referências a
V-ADD reprovado são históricas. Código Java/testes/configurações sem alteração;
clean verify de 903 testes e JAR/hash/XML preservados. Manual humano Francisco
cumprido e aprovado, 49 checks/Java 0. [Checkpoint Test atualizado](../../../memory-bank/bolts/003-overlay/ddd-03-test-report.md).
Test ainda por aprovar, current_stage=test, completed=null; não há fecho,
commits/push ou nova integração. Revisão humana final e integração pendentes.

## IMP-004 — Test aprovado; fecho pendente

Test aprovado explicitamente pelo utilizador, registo em 2026-11-10T16:44:29Z.
903 testes do clean verify aprovados, manual humano de Francisco cumprido
(49 checks/Java 0) e V-ADD aprovado; Q-C-08 resolvida.
[Resumo/diff para revisão humana final](../../../memory-bank/bolts/003-overlay/final-review-summary.md).
As cinco etapas estão concluídas; bolt in-progress, current_stage=null,
completed=null. TASK/história/unidade continuam em progresso, sem implemented=true
ou DONE. Não há gates externos adicionais; faltam revisão humana final e
integração. Não executar o fecho nem fazer commits/push nesta autorização.

## Revisão final Denji aprovada — integração por autorizar

O utilizador confirmou explicitamente que Denji realizou e aprovou a revisão
humana final desta versão, sem alterações pendentes. Confirmação registada em
2026-11-10T17:00:44Z; data de execução da revisão não fornecida e não inventada.
Código permanece idêntico ao aprovado em Test; manual humano de Francisco
já cumprido, sem repetição. Q-C-08 resolvida, Test/V-ADD aprovados.
Falta integração autorizada/comprovada para poder tratar DONE. Sem staging,
commits/push, integração ou fecho nesta autorização.

[Registo da confirmação](../../../experiments/construction/003-overlay/denji-final-review-confirmation.json).
