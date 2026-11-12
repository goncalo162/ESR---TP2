# IMP-007 — Test e revisão final aprovados; integração pendente

O monitor mede RTT local por sessão, calcula EWMA/custo e deteta ausência de PONG
ou EOF; compõe-se no owner e manager existentes e participa no shutdown global.
Primeira sonda elegível no Up, uma pendência antes da admissão, early PONG limitado,
reset em nova connectionId mesmo instanceId, timeouts de sondas admitidas e reservas
físicas até concluir callbacks/continuações. Nenhum transporte, CLI, dependência
runtime ou formato/cap novo. TCP operacional v2/19tipos/3rejeitado, UDP OTT1 intacto.

Base operacional e HEAD: `3dedf007f52f43f53eaf254bef7c25e35f986e5a`, main local.
Código igual ao aprovado em Implement/Test, sem nova edição Java nesta preparação.
[Justificação de cada substituição em OverlayNode](implementation-walkthrough.md).
Q-C-23/24 preservadas; Q-C-25 limita-se à cadeia exata dos dois patches autorizados
no teste do peer que agora responde a PING/Query antes de exigir NODE_LEAVE.
O oráculo continua a exigir v2/identidade/limites, payload0 e saída0; nenhum outro
teste histórico mudou e NeighborManager/send ficaram intactos.

## Evidência aceite em Test

- clean verify em Projeto/:1176 testes,0falhas/0erros/0ignorados,exit0, incluindo52
  de monitorização e11 de integração própria TCP/JVM/reparação/shutdown físico.
- [Relatório aprovado](ddd-03-test-report.md) e [mapa V-010/M01–M22/D01–D16](test-oracles.md).
- [Log](../../../experiments/construction/005-rotas/test-clean-verify.log) e
  [XML/TXT completos](../../../experiments/construction/005-rotas/test-clean-verify-xml/).
- JAR330491bytes, SHA256
  `3f39cab445ca05360525691b15c8f7193b7e0186b8033ef8cbd86d50690cfb15`;
  [cópia de evidência](../../../experiments/construction/005-rotas/test-build/ott-streaming-1.0-SNAPSHOT.jar).
- Manual humano Francisco execução02 cumprido: quatro saídas0 e SIGKILL9/137
  intencional; cinco processos registados terminados/sem erros de logger; cinco
  portas disponíveis no bind final; sondas bidirecionais e identidades conferidas.
  [Logs originais](../../../experiments/construction/005-rotas/manual-humano-execucao-02/),
  [conferência e limites](../../../experiments/construction/005-rotas/manual-human-02-review.md).
- STOP/CONT/sem-bootstrap/contexto EOF confirmados pelo operador, consistentes
  com eventos TRANSPORT; a CLI não regista sinais nem timestamps por evento ou
  PEER_EOF literal. Não se inventam RTT/latência/perdas/FPS, GUI/CORE ou revisão.
- V-ADD aprovado pelo utilizador; nova validação documental desta preparação
  em final-review-v-add.log. Sem repetição de build/manual: código não mudou.

## Pacote exato para revisão e futura integração

[Allowlist por caminho, sem glob](../../../experiments/construction/005-rotas/integration-allowlist.txt),
[lista por grupo](../../../experiments/construction/005-rotas/integration-file-list.md),
[manifesto SHA256](../../../experiments/construction/005-rotas/integration-manifest.json),
[plano de integração pendente](../../../experiments/construction/005-rotas/integration-plan.md).

Os grupos separam os cinco ficheiros IMP-007, o único teste Q-C-25, documentação/
progresso e evidência. Incluem tentativas falhadas e ensaios do agente identificados,
logs/XML atuais, JAR/perfis e os21 ficheiros pessoais imutáveis do manual02.
Target/ não integra a lista; a cópia do JAR fica apenas na pasta de evidência.
Nenhum ficheiro alheio descoberto no pacote; os dois maven-status tracked modificados
em target são outputs excluídos e preservados no disco. Não usar git add . ou
seleção recursiva implícita; a lista preparada ainda não foi aplicada ao índice.

Diffs contra a base, incluindo ficheiros novos sem staging:
[cinco ficheiros IMP-007](../../../experiments/construction/005-rotas/final-source.diff),
[Q-C-25 separado](../../../experiments/construction/005-rotas/final-q-c-25.diff),
[documentação](../../../experiments/construction/005-rotas/final-documents.diff),
[acumulado fonte/documentos](../../../experiments/construction/005-rotas/final-review.diff),
[numstat](../../../experiments/construction/005-rotas/final-review.numstat).
Evidência bruta completa é listada/hashada, sem normalizar logs nem produzir um
patch textual de JAR; referências textuais históricas a target dentro de logs/diffs
permanecem como prova, sem integrar os próprios ficheiros de target.

## Revisão final e estado

O utilizador aprovou Test e confirmou que **Fillmore realizou e aprovou a revisão
final da implementação atual e da evidência, incluindo Q-C-25, sem alterações
pendentes**. Não é só a revisão de Design anterior.
[Confirmação vinculada aos hashes do código/JAR e pacote revisto](../../../experiments/construction/005-rotas/fillmore-final-review-confirmation.json).

Resta integrar a versão revista, mediante autorização explícita. A allowlist e o
manifesto incluem esta confirmação e a prova/preparação de simulação. A simulação
reconstrói a base Git e aplica somente os caminhos autorizáveis; o validador e
seu alcance permanecem intactos. [Script reproduzível](../../../experiments/construction/005-rotas/integration-simulation.py)
e [prova](../../../experiments/construction/005-rotas/integration-simulation-proof.json).
A última execução opera na cópia final congelada, escreve resultado num recibo
externo à árvore simulada e não modifica o pacote após validar. O log de preparação
incluído conserva a sua fase própria; não se forja hash autorreferente.

Antes de staging autorizado: reconferir HEAD/branch/status, allowlist, hashes e
diff; depois mostrar o staged diff e confrontar bytes/paths antes de commit. Nenhum
código ou JAR mudou, por isso não se repetiu build/manual. Alterações futuras ao
código exigiriam validação e revisão da nova versão; esta aprovação não se estende
a alterações que não foram revistas.

Bolt continua in-progress/completed=null, história/unidade em progresso e TASK-010
IN PROGRESS. M22 conserva integração pendente; revisão final agora cumprida. Só depois de
integração comprovada se poderá pedir/realizar fecho documental
na autorização apropriada. Sem staging, commits, push, DONE ou outro IMP.
Sem gates externos adicionais.
