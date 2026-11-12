# TASK-010 — Medir RTT e publicar falhas de vizinhança

## 1. Identificação

- ID: TASK-010
- Fase: [PHASE-03 — Monitorização e descoberta de rotas](../../phases/PHASE-03-ROUTING.md)
- Prioridade: P0
- Estado: IN PROGRESS
- Responsável: [Waheed](../../team/WAHEED.md).
- Revisor: [Fillmore](../../team/FILLMORE.md), antes de integrar e marcar DONE.
- Dependências: [TASK-007](../PHASE-02/TASK-007.md), [TASK-008](../PHASE-02/TASK-008.md), [TASK-009](../PHASE-02/TASK-009.md), [TASK-034 — Extra, reparação](../PHASE-02/TASK-034.md).
- Requisitos relacionados: [RF-003](../../02-REQUIREMENTS.md#rf-003), [RF-008](../../02-REQUIREMENTS.md#rf-008), [RF-014](../../02-REQUIREMENTS.md#rf-014), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Produzir uma métrica medida e detetar peers sem resposta.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Rotas para várias streams, alternativas sem ciclos e trace até à origem. A responsabilidade específica é medir rtt e publicar falhas de vizinhança. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Sondas PROBE_PING/PONG usam probeId e tempo monotónico guardado no emissor. Manter uma sonda pendente por vizinho, ignorar respostas não correlacionadas e atualizar EWMA com alpha=0,2. Após três timeouts consecutivos publicar falha e fechar a conexão afetada; EOF é falha imediata. Reiniciar métricas quando muda instanceId. Injetar scheduler/relógio nos testes. RTT TCP mede ida/volta do controlo, não atraso de vídeo unidirecional, perda UDP ou capacidade de banda.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: monitoring/LinkMetrics.java e NetworkMonitor.java; planeados: monitoring/NetworkMonitorTest.java e abstração TimeSource.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Correlacionar PING/PONG e validar IDs de sonda.
- [ ] Calcular RTT/EWMA com unidades explícitas em microssegundos.
- [ ] Implementar timeout, missedCount e evento neighborDown.
- [ ] Expor snapshot com idade e número de amostras.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

PONG duplicado, atrasado, de instância antiga, relógio civil corrigido, RTT zero por resolução e peer sem amostra. Usar custo mínimo positivo de 1 us; candidato sem amostra fica pendente até medir.

## 10. Testes necessários

V-010: relógio falso confirma EWMA, reset após nova instância, perda de três sondas e ausência de falso positivo por um timeout; EOF invalida imediatamente.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] RTT é calculado sem comparar relógios de máquinas diferentes.
- [ ] Falhas são correlacionadas à conexão correta.
- [ ] Métrica sem amostra não é publicada como zero confiável.
- [ ] Validação V-010 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.

## IMP-007 — Model v2 preparado para aprovação

Registo em 2026-11-12T14:27:02Z. Base 3dedf007f52f43f53eaf254bef7c25e35f986e5a, main,
entrada limpa. Dependências IMP-006/004-overlay e IMP-035/035-overlay-extra
complete; unidade002-overlay complete. Apenas Model iniciado; current_stage=model,
stages_completed=[], completed=null. Modelo preparado com TCP v2/19 tipos/3 rejeitado,
identidades, sondas, RTT/EWMA, falhas, limites, ownership e shutdown. Q-C-23/24
registam composição e concretizações pendentes; nenhuma aprovação da retoma
cancelada foi reutilizada. V-010/regressão/manual/revisão Fillmore/integração
continuam futuros, sem gates externos adicionais. Sem Java, serviços, Maven,
staging, commits/push ou avanço Design/ADR/Implement/Test.


## IMP-007 — Model aprovado; Design para aprovação

Model e Q-C-23 opção 1/Q-C-24 aprovados explicitamente pelo utilizador nesta
retoma. current_stage=design; só Model concluído; Design/ADR/Implement/Test não
aprovados. Design preparado com allowlist futura de cinco ficheiros, APIs,
limites físicos, admissão/PONG, falhas locais, composição e budget global.
Decisões registadas em ADD e contratos; nenhum Java alterado ou teste executado.
V-010, regressão atual, manual humano antes de aprovar Test, revisão de Fillmore e
integração continuam pendentes. Sem gates externos adicionais. Parar antes de
ADR/Implement; sem staging, commits, push ou fecho. Os registos Model acima são
históricos e não substituem este estado atual.


## IMP-007 — Design aprovado; checkpoint ADR

Registo 2026-11-12T14:48:27Z: utilizador aprovou Design e allowlist exata dos cinco ficheiros.
Confirmou revisão de Fillmore do Design atual; não constitui revisão final do
código ainda não implementado. Q-C-23/24 preservadas. Análise ADR preparada para
aprovação, sem divergência/inviabilidade concreta identificada documentalmente;
reutiliza decisões aceites, sem ADR nova ou alteração de estado das existentes.
current_stage=adr; só Model/Design concluídos. Implement/Test não iniciados.
V-010, regressão atual, manual humano antes de aprovar Test, revisão final do
código e integração pendentes; sem gates externos adicionais. Sem Java, staging,
commits, push ou fecho. Relatório: memory-bank/bolts/005-rotas/adr-review.md.


## IMP-007 — ADR aprovada; primeiro checkpoint Implement (vermelho)

Registo 2026-11-12T14:56:34Z: ADR/reutilização aprovadas pelo utilizador; apenas interfaces/tipos
e primeiros testes autorizados nos cinco ficheiros. current_stage=implement;
Model/Design/ADR concluídos, Implement/Test por aprovar. 16 testes selecionados:
1 passou,15 falharam por asserção,0 erros/ignorados. Não é V-010 concluído nem
regressão. Dependências entre pré-condições e asserções explicadas no walkthrough.
Revisão de Fillmore continua limitada ao Design. Sem comportamentos de sondagem,
composição ou cleanup implementados. Parar antes desses comportamentos.
V-010, regressão atual, manual humano, revisão final do código e integração
pendentes; sem gates externos adicionais. Sem staging, commits, push ou Test.


## IMP-007 — comportamentos Implement para aprovação

Registo 2026-11-12T15:56:53.776762+00:00. Comportamentos do Design implementados nos cinco caminhos aprovados,
mais exceção de teste Q-C-25 exclusivamente pelos dois patches autorizados.
Q-C-23/24 preservadas; Q-C-25 resolvida após regressão04:1176 testes,
0falhas/0erros/0ignorados,exit0 (52monitoring+11integração própria). Testes reais TCP,
reparação20 e JVM da CLI com término comprovado; não substituem manual humano.
current_stage=implement; só Model/Design/ADR concluídos. Implement para aprovação,
Test não iniciado, história não implementada/bolt não complete. M22/clean verify,
manual humano, revisão final do código e integração pendentes. Fillmore confirmou
revisão de Design, não de código final. Target separado; sem staging/commits/push,
fecho ou outro IMP; sem gates externos adicionais. Ver implementation-walkthrough
no bolt e experiments/construction/005-rotas/implement-results.json.


## IMP-007 — Implement aprovado; Test técnico executado, manual humano pendente

Registo 2026-11-12T16:16:45Z. Utilizador aprovou Implement e autorizou Test. Novo clean verify:
1176 testes,0falhas/0erros/0ignorados,exit0;52monitoring+11integração própria.
JAR novo 330491 bytes,SHA256 3f39cab445ca05360525691b15c8f7193b7e0186b8033ef8cbd86d50690cfb15; cópia de evidência fora de target.
V-010/M01–M21/D01–D16 exercitados e mapeados nos XML; M22 conserva manual humano,
aprovação de Test, revisão final do código e integração pendentes. Ensaio técnico
02:24checks e cleanup confirmado; não conta como humano. Manual reproduzível em
experiments/construction/005-rotas/manual-instructions.md, sem logs pessoais criados.
current_stage=test; Model/Design/ADR/Implement concluídos, Test por aprovar;
bolt/história/TASK/unidade ainda em progresso, sem fecho. Código aprovado intacto
por SHA256; Q-C-25 só os patches autorizados. Revisão Fillmore continua só Design.
Sem gates externos adicionais, staging, commits, push ou outro IMP.


## IMP-007 — manual humano02 conferido; Test para aprovação

Registo 2026-11-12T22:03:45Z. Utilizador confirmou manual humano de Francisco em
experiments/construction/005-rotas/manual-humano-execucao-02/. Conferência:
quatro saídas normais JAVA_EXIT0 e R2-01 por SIGKILL intencional9/estado137;
cinco processos registados terminados,logger_error=null,cinco portas disponíveis
no bind final. Sondas bidirecionais correlacionadas; mesma instanceId/nova
connectionId na reconexão e nova instanceId/conexão no reinício.
STOP/CONT/sem-bootstrap/EOF são contexto confirmado pelo operador; CLI apresenta
Down TRANSPORT sem registo de sinais/timestamps/PEER_EOF literal. Limites e
contagens/diagnósticos preservados em manual-human-02-review.json/.md, sem
medições ou resultados humanos inventados. Código/JAR e Q-C-25 intactos.
Test para aprovação; só Model/Design/ADR/Implement concluídos. Clean verify
1176/0/0/0 permanece válido; não repetido sem mudança de código. Revisão final do
código e integração pendentes; Fillmore confirmado apenas para Design. Bolt,
história,TASK e unidade continuam em progresso. Sem commits/push/fecho ou gates
externos adicionais.


## IMP-007 — Test aprovado; pacote para revisão final e integração

Registo 2026-11-12T22:07:58Z. Utilizador aprovou Test: clean verify1176/0falhas/0erros/0ignorados,
V-ADD e manual humano de Francisco execução02 cumprido com os limites documentados.
Cinco etapas concluídas; current_stage=test/status=in-progress/completed=null.
Bolt aberto, história/unidade em progresso e TASK-010 IN PROGRESS até revisão
final do código e integração comprovadas. A revisão anterior de Fillmore abrange
só Design; não há revisão final do código confirmada. Pacote em final-review-summary
no bolt, allowlist exata e hashes em experiments/construction/005-rotas.
Código aprovado e logs preservados; Q-C-25 só os dois patches autorizados;
target/ e alterações alheias excluídos. Sem staging, commits, push, fecho ou outro
IMP; sem gates externos adicionais. Faltam revisão final do código e integração.


## IMP-007 — revisão final da implementação aprovada por Fillmore

Registo 2026-11-12T22:19:48Z. Utilizador confirmou revisão final por Fillmore do código e
evidência atuais, incluindo Q-C-25, aprovada sem alterações pendentes. Confirmação
associada aos seis hashes de código, JAR e manifesto da versão revista em
experiments/construction/005-rotas/fillmore-final-review-confirmation.json.
Não se confunde esta revisão com a revisão anterior de Design. Test/manual já
aprovados. Resta integrar a versão revista; bolt/história/TASK/unidade continuam
em progresso/completed=null. Plano/allowlist/manifesto atualizados e simulação
exata preparados para autorização de integração. Nenhum staging/commit/push ou
fecho autorizado; nenhum novo gate externo ou IMP. Código/JAR/logs intactos.
