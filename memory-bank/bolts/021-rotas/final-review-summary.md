# IMP-008 — versão revista e aceite, pronta para integração autorizada

Registo 2026-11-13T17:59:03Z. Test1211/0/0/0,JDK21/14XML, manual humano Luis43/Java0,
e revisão final Luis (substitui Waheed) aceites explicitamente. Fontes/JAR iguais
à versão validada; [confirmação](../../../experiments/construction/021-rotas/luis-final-review-confirmation.json).
[Relatório humano original](../../../experiments/construction/021-rotas/test-manual-runs/20261113T174838Z-y23kpe_b/report.json)
e [aceitação](../../../experiments/construction/021-rotas/test-acceptance.json).

Código: seis caminhos, +851/−0; POM, testes anteriores e bytes/caps preservados.
JAR364704bytes,SHA256 366210cf8c7656ad2948ae1c842796632085aaa4f09afcedcd00a49db0a0ecee.
[Walkthrough](implementation-walkthrough.md) e [Test](ddd-03-test-report.md).

Integração/push origin main e fecho autorizados; falta executar a integração e
comprová-la antes de DONE. Plano: commit da implementação/evidência/documentação
aceites em main, validação do commit isolado, seguido de commit de fecho com
cascata bolt→história→TASK e índices. Unidade003-rotas/PHASE-03 não fecham.
D22 parcial só por integração. Não iniciar outro IMP.

EXTRA-02 preexistente será preservado fora dos commits: quatro caminhos próprios
e blocos isolados de ADD/construction-duvidas. Projeto/target gerado excluído;
backup pre-clean-target.zip e classes temporárias dos manuais ficam só locais,
preservados, fora da publicação. O JAR validado é evidência explícita fora de target.
Os diffs históricos da bancada conservam a proveniência; não aplicam Extra02.

Remoto main/Denji consultado e igual à base cc952c5; sem divergência observada.
Push será sem force. Os hashes dos commits serão registados só depois de criados.


## IMP-008 / 021-rotas / TASK-011 — fecho após integração comprovada

Registo 2026-11-13T18:09:37Z. Versão revista integrada em main: 3b7aa745cac62b71ac8ce7e036656bf7ab21692f;192 caminhos/hashes
iguais ao pacote, V-ADD0/78 numa cópia extraída do commit. Fontes/JAR e relatório
humano Luis conferidos no commit, sem overlay do diretório de trabalho.
Test1211/0/0/0,JDK21/14XML; manual humano Luis43 CHECK_PASS/MANUAL_PASS/Java0/
passed=true/JVM terminada aceites. Revisão final Luis confirmada pelo utilizador,
substituindo Waheed. D22 cumprido; fecho explicitamente autorizado.
Bolt/história complete, implemented=true, TASK-011 DONE. Dez bolts complete,
25 planned, nenhum em execução. Unidade003-rotas/PHASE-03 continuam em progresso;
006-rotas apenas desimpedido, sem início ou autorização para outro IMP.
Código/JAR/logs originais preservados, sem repetição de build/manual. EXTRA-02
preexistente e target gerado fora dos commits. Publicação main autorizada,
resultado só será declarado após push efetivo; nenhum force push.
