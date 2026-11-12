---
unit: "003-rotas"
bolt: "005-rotas"
stage: "test"
status: "in-progress"
created: "2026-11-12T16:16:45Z"
---
# IMP-007 — Test e revisão final aprovados; integração pendente

Implement aprovado pelo utilizador; Test autorizado. Manual humano execução02 de
Francisco confirmado pelo utilizador e conferido nos logs. Test aprovado
explicitamente pelo utilizador nesta etapa; sem fecho. Sem gates externos adicionais. Revisão final da implementação atual e evidência, incluindo Q-C-25, agora
confirmada pelo utilizador como aprovada por Fillmore; integração pendente.

## Construção real

Em Projeto/: `mvn -B -ntp -Dott.test.evidence=<pasta absoluta de evidência> clean verify`.
Exit0, **1176 testes,0falhas/0erros/0ignorados**. JDK21/Maven, versões reais registadas
em test-environment.json. Os1113/1176 anteriores são históricos; este total foi
lido dos novos XML após clean, não presumido de execução incremental.
[Log](../../../experiments/construction/005-rotas/test-clean-verify.log),
[resultado e suites](../../../experiments/construction/005-rotas/test-build.json),
[XML/TXT](../../../experiments/construction/005-rotas/test-clean-verify-xml/).

JAR novo: ott-streaming-1.0-SNAPSHOT.jar, 330491 bytes.
SHA256: `3f39cab445ca05360525691b15c8f7193b7e0186b8033ef8cbd86d50690cfb15`.
[Cópia idêntica fora de target](../../../experiments/construction/005-rotas/test-build/ott-streaming-1.0-SNAPSHOT.jar),
[SHA256SUMS](../../../experiments/construction/005-rotas/test-build/SHA256SUMS).
Target contém apenas os outputs regenerados, separado da entrega. O JAR de evidência
é uma cópia da construção, sem alterar o binário ou incorporar os helpers de manual.

## V-010 e integração/shutdown

[Mapa consolidado](test-oracles.md): M01–M21/D01–D16 exercitados; M22 mantém integração pendente; Test e revisão final Fillmore aprovados;
manual humano execução02 cumprido.52 testes monitoring com relógio/owner/transportes
falsos e11 de integração própria, além de toda a regressão histórica. Não há
nova dependência, ferramenta ou cobertura percentual. Q-C-23/24 e wire/caps
permanecem, TCPv2/19tipos/3rejeitado; NeighborManager/send e fixtures intactos.

Integração real inclui bootstrap/nós TCP, PING/PONG/custo só após amostra,
EOF, tráfego ERROR e preservação de eventos, reparação20 sem transporte novo,
JVM da CLI, factory falhada, close durante callback e pedido repetido de shutdown.
Término físico é verificado por await/join reais: helper timer canceled-running
continua contado; futures/continuações/callbacks retidos não se tornam sucesso
físico por retire lógico; fechos pendentes progridem ao libertar ticket; owner
emprestado não é fechado pelo monitor; primeiro budget global não é renovado.
O teste com admissão retida exige false enquanto houver trabalho e verifica
que uma chamada longa posterior não renova o prazo original.

## Preparação e ensaios técnicos — histórico anterior ao manual humano

[Instruções humanas de quatro terminais](../../../experiments/construction/005-rotas/manual-instructions.md),
[manifesto de JAR/perfis](../../../experiments/construction/005-rotas/manual-manifest.json).
Perfis próprios BOOT/R1/R2, localhost; TCP58020/58021/58022, UDP apenas configurado
58121/58122. Loader do JAR novo validou conjuntamente os3 perfis; interval500ms,
timeout1000ms,F3,alpha0.2,shutdown3000ms. Nenhum perfil Java ou fonte aprovada mudou.
A disponibilidade das portas observada pelo ensaio é temporária; reconfirmar.

Ensaio do agente02 sobre esse JAR:24 CHECK_PASS,TECHNICAL_REHEARSAL_PASS,
CLEANUP_COMPLETE,exit0,no_owned_processes_pending=true,human_manual_performed=false.
[Resultado](../../../experiments/construction/005-rotas/test-rehearsal-02/result.json).
Realizou SIGSTOP/CONT/KILL apenas no peer que lançou, observou nova conexão com
mesma instância, morte/EOF, instância nova após REGISTER e shutdown/exit dos
processos normais. R2-01 terminou por SIGKILL intencional(-9), não saída0.
Logger humano verificado separadamente: READY visível antes de quit,JAVA_EXIT0,
WRAPPER_EXIT0 após fim real; nenhuma execução pessoal foi inventada.

Ensaio01 falhou por um erro do procedimento: tentou iniciar JVM nova com bootstrap
indisponível. Contrato exige REGISTER inicial; diagnóstico DISCOVERING/TRANSPORT,
sem READY. O procedimento foi corrigido: só instância já descoberta reconecta sem
bootstrap; uma nova precisa de bootstrap temporariamente. Logs/falha/cleanup do
ensaio01 preservados. Não se alterou implementação, teste ou contrato para o passar.
JShell na sandbox falhou por socket de depuração; validação offline posterior fora
dela passou, sem iniciar serviço OTT. Evidência de ambas preservada.

## Manual humano execução02 — conferido

Francisco executou o manual e o utilizador confirmou as ações/observações.
[Conferência detalhada](../../../experiments/construction/005-rotas/manual-human-02-review.md)
e [dados/linhas/hashes](../../../experiments/construction/005-rotas/manual-human-02-review.json).
Quatro saídas normais0; R2-01 por sinal9/estado137 intencional; cinco processos
registados como terminados e sem erros do logger; cinco portas disponíveis no
bind final. Sondas v2 bidirecionais correlacionadas por requestId/probeId.
R1.log:Up43/Down625/Up668/Down849/Up964/Down1132: mesma instância/nova conexão,
seguida por nova instância/nova conexão, e Leave normal final.

STOP/CONT, sequência sem bootstrap e contexto KILL/EOF foram confirmados pelo
operador; eventos TRANSPORT e ordem por log são consistentes, mas não há registo
de sinais/timestamp por evento nem PEER_EOF literal. Não inferir RTT/latência ou
número exato de sondas deste manual; usar os oráculos para essas propriedades.
Diagnósticos TRANSPORT/TIMEOUT/STALE/DISCOVERY_CACHE_REJECTION foram preservados;
sem linhas FATAL/CLEANUP_ERROR. Conferência não equivale a revisão final de código.

Manual humano cumprido no âmbito demonstrado. Test agora aprovado explicitamente pelo utilizador; sem commit, push, fecho,
GUI ou CORE. Os ensaios do agente acima mantêm-se
separados e não são atribuídos a Francisco.

## Preservação e checkpoint

SHA256 dos seis ficheiros aprovados (cinco do Design+exceção Q-C-25) igual à entrada
Test e manifesto de Implement. Q-C-25 limitada exatamente aos dois patches já
autorizados; hashes em cadeia conservados. Nenhuma alteração Java nesta etapa,
nem mudança no validador, testes históricos ou fontes/ficheiros alheios.
Prova completa e diff/numstat contra a base em test-preservation.json/test-vs-base.*.
V-ADD executado com log próprio; resultado registado na auditoria desta etapa.

current_stage=test; Model/Design/ADR/Implement/Test concluídos por aprovação;
bolt continua in-progress até revisão final do código e integração comprovadas.
História não implemented=true, bolt não complete, TASK-010 continua IN PROGRESS,
unidade003-rotas em progresso. Sem staging, commits, push, fecho ou outro IMP.
Manual humano, Test e revisão final Fillmore cumpridos; falta integração.

Conferência humana registada em 2026-11-12T22:03:45Z; evidência e atualização documental em
manual-human-02-preservation.json/manual-human-02-v-add.log.

Aprovação explícita de Test registada em 2026-11-12T22:07:58Z: clean verify1176/0/0/0,
V-ADD e manual humano Francisco execução02 com limites aceites. Sem autorização
de staging/commit/push/fecho. Ver final-review-summary.md.


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
