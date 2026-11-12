# IMP-035 — versão aprovada em Test para integração

Registo 2026-11-12T10:35:23Z. Test aprovado por Luis, incluindo66 CHECK_PASS/MANUAL_PASS,
passed=true e no_processes_pending=true da execução20261112T102931Z-pr_nynth.
A execução anterior de Luis20261112T004311Z-ilbihqij permanece preservada.
[Proveniência/hashes](../../../experiments/construction/035-overlay-extra/closure-human-evidence.json).

## Versão a integrar

Código/testes/fixtures iguais ao clean verify JDK21/1113 testes,11 XML e JAR
SHA-256dea01ffa2c501b9b948594005e7e8e9ee70b387981dc5b3d83f9d7c331cc2526.
[Relatório aprovado](ddd-03-test-report.md). Nenhum manual/build foi repetido.

Revisão técnica documental/de código por Codex nesta sessão: protocolo v2 único,
tipo20/masks3, same-TCP20/6, correlação ERROR5/20/6, slot/dial barrier, quotas,
TTL original antes de envio/commit, menor originator só pendente, ACTIVE preservada,
grau total≤min(C,63) no commit serial, reservas até término físico e shutdown.
Não foi identificado impedimento técnico adicional nesta inspeção estática;
ela não constitui uma revisão humana independente da implementação.

## Estado de aceitação

Luis aprovou Test e autorizou commits e integração em main. O revisor de código
Fillmore consta apenas como proposto; não se atribui revisão executada a esse
nome, a Waheed ou a outro colega. A revisão humana final da implementação
continua pendente conforme o checkpoint anterior. O bolt conserva in-progress,
current_stage=null/completed=null, cinco etapas concluídas; TASK-034/história/
unidade/EXTRA-01 não são promovidos a DONE por uma integração isolada.

A versão aprovada 15efdf280b97327cfdc73e9040b9dfed4d4a5658 foi integrada em main local por fast-forward
a partir de22622c1, sem conflitos. [Prova Git](../../../experiments/construction/035-overlay-extra/integration-proof.json).
O commit documental seguinte regista este resultado e não altera o runtime.
Sem push autorizado ou início de outro IMP. Vídeo/routing/CORE permanecem fora
desta prova de reparação do controlo em loopback.

## Verificações antes dos commits

V-ADD passou com78 fixtures/zero erros. Código/testes/fixtures/documentação e
auxiliares passaram git diff --cached --check com a exclusão explícita da
pasta de evidências brutas. Logs Maven/XML/diffs históricos conservam espaços
originais protegidos por hashes; o check bruto dessa pasta regista os avisos,
sem normalizar ou esconder a execução. [Check operacional](../../../experiments/construction/035-overlay-extra/integration-code-docs-diff-check.log),
[avisos das evidências brutas](../../../experiments/construction/035-overlay-extra/integration-whitespace-raw-evidence.log).
Nenhum caminho Projeto/target está preparado para commit.

## Integração concluída

Registo 2026-11-12T10:39:28Z. main e Denji receberam a versão aprovada. Código/testes/fixtures
iguais ao clean verify1113 e aos manuais humanos. Nenhum target nos commits.
O fecho continua pendente apenas da revisão humana final da implementação;
005-rotas não foi iniciado. Sem push.
