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
