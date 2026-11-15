# IMP-010 / 022-rotas / TASK-013 — fecho autorizado

Registo 2026-11-15T16:36:52Z. Versão revista integrada em main **3349d575ae38c772b38496cc08bc1d44ded764ff**,
antes da cascata DONE. [Prova de integração](../../../experiments/construction/022-rotas/closure/integration-proof.json)
e [V-ADD da árvore integrada](../../../experiments/construction/022-rotas/closure/integration-commit-v-add.log).

## Aceitação e versão

Plan/Implement/Test concluídos. [Test aprovado](../../../experiments/construction/022-rotas/closure/test-acceptance.json)
e [revisão final Luis, substituindo Fillmore](../../../experiments/construction/022-rotas/closure/final-review-confirmation.json),
confirmados pelo utilizador,sem presumir contacto ou data de revisão anterior.
V-013/T01–T18 consolidados no [relatório Test](test-walkthrough.md).
Clean verify JDK21:1296 testes,0falhas/0erros/0ignorados,17XML; execução preservada,
sem repetição por alterações exclusivamente documentais. JAR436556bytes,SHA256
5f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52.
238 entradas de Projeto/ fora de target e602 evidências anteriores conferidas
contra os blobs integrados;43 hashes do manifesto de aceitação e44 caminhos
do commit correspondem ao pacote revisto. Nenhum Projeto/target novo nos commits.
O commit preexistente f15a4bf continha duas listas geradas; não foi reescrito.

Manual humano Luis: [report original](../../../experiments/construction/022-rotas/manual-runs/20261115T160738Z-me3p9b08/report.json),
34 CHECK_PASS,passed=true,quatro Java0 e SIGKILL intencional de R1; cinco JVM
recolhidas sem limpeza forçada,portas livres,zero UDP no intervalo observado.
SHA256report9f9497f36e53a769c6f90e6e835feb1b0a72b0e4fc22c36298bce0760183ea5f.
Trace local[S],C–R1–S eC–R2–S; após bootstrap parado eR1 morto,novo
traceC–R2–S. UUID inválido/UNKNOWN_STREAM eNO_ROUTE após saída de S/R2 explícitos.

## Limitações preservadas e aceites

- [Observação NO_ROUTE anterior às falhas](../../../experiments/construction/022-rotas/manual-human-review-20261115.json):
  retiradas da geração127 pelos dois relays precedem dois NO_ROUTE,seguidas de
  anúncios128. Política de retirada/geração nova aprovada em Q-C-29; não se prova
  a causa exata de cada retirada ou a ordem do owner apenas pela ordem do logger.
  Não se alega disponibilidade contínua nem ausência de interrupções.
- Falha histórica de m13PhysicalIoJoinsAreProvedBeforeJvmExitWithBlockedOutput
  em [regressão02](../../../experiments/construction/022-rotas/implement-behavior/regression-02/result.json):
  JVM não terminou em6s,causa não estabelecida. Repetição isolada,regressão03 e
  clean verify passaram sem alterar teste/prazos; execução negativa preservada.
- Ensaios agent e primeira cena parcial preservados separadamente do humano.
  Sem reprodução de vídeo,GUI,CORE,FPS,delay unidirecional ou PCAP alegados.

## Cascata e publicação

Bolt/história complete,implemented=true,TASK-013 DONE,unidade003-rotas complete,
[PHASE-03 DONE](../../../docs/phases/PHASE-03-CLOSURE-REVIEW.md).
12 bolts complete,23 planned,nenhum em execução;007-arvores só desimpedido.
A publicação de main/Denji está autorizada; resultado e hash do commit de fecho
serão apresentados após execução,sem hash autorreferente ou force push.
