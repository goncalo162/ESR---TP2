# IMP-034 / 034-overlay-extra — Revisão final e integração

Registo de 2026-11-11T19:40:54Z. Luis executou o manual humano e pediu revisão final, integração e fecho. Test aceite pela instrução explícita de fecho. Revisão técnica final: Codex validador, separado da sessão que implementou; não é atribuída revisão de código a Fillmore ou Waheed. Integração local concluída; bolt/história complete e TASK-033 DONE.

## Evidência final

- Regressão clean verify/JDK 21: 1065 testes, zero falhas, erros ou skips, dez XML. [Resumo e hashes](../../../experiments/construction/034-overlay-extra/test-build-summary.json).
- Manual Luis: 53 CHECK_PASS, MANUAL_PASS, saída 0, TTL real e mesmo JAR. [Resultado](../../../experiments/construction/034-overlay-extra/test-manual-runs/20261111T193418Z-xj_bfaej/result.json).
- Revisão de âmbito/contratos e fontes: TCP v2 único no bootstrap e vizinhos; 18 tipos, código 3 retirado; Q>=2 overlay, recursos/FIFO/flush limitados; cache 128×4/TTL 60 s/proveniência/conflitos; identidade e callbacks; shutdown. As duas divergências D07e foram reproduzidas e corrigidas antes da regressão. [Registo da revisão](../../../experiments/construction/034-overlay-extra/final-review.json) e [Test](ddd-03-test-report.md).
- Não foram detetados impedimentos técnicos ao fecho dentro de V-033. A aprendizagem não repara ligações novas: IMP-035, vídeo/GUI/CORE e morte física continuam fora desta prova. Não se alega revisão humana por um colega ausente.

## Limpeza autorizada

Q-C-19 substitui a obrigação anterior de guardar cópias TCP v1. Foram retirados 234 ficheiros (32738773 bytes): arquivos TCP v1, código/testes arquivados e evidências da variante OTD1 abandonada e preparações redundantes anteriores a v2. Nenhum teste operacional Java foi apagado. [Manifesto de paths/hashes removidos](../../../experiments/construction/034-overlay-extra/cleanup-manifest.json). Os links históricos para cópias retiradas remetem para este registo; não significam que o conteúdo antigo ainda exista.

Permanecem 50 vetores TCP v2 independentes, os negativos de versão v1, 16 vetores UDP OTT1 e a sua origem na base Git, red/green v2, XML/logs finais, manuais e JAR validado. UDP versão 1 continua a ser o protocolo de vídeo atual. Registos de execução históricos ficam intactos e podem mencionar arquivos posteriormente retirados. As exceções de retenção não mudam o runtime nem relaxam os oráculos de V-033.

## Integração

Main local avançou por fast-forward de 17c1ca27673930bfbf82078522059a243d96ccdb para f650be02f2af10b95328f5b50c3d6941e2e75c15, a versão revista. [Prova da integração](../../../experiments/construction/034-overlay-extra/closure-integration.json). Fecho registado em 2026-11-11T19:45:51Z após a integração. O commit documental de fecho será também integrado localmente; nenhum push foi executado.

## Verificação de integridade no fecho

Código Java, testes e bytes/expectativas das 66 fixtures coincidem com clean verify e o manual humano. Só documentação/notas do manifesto mudaram depois. O check de whitespace passou no código/documentação; logs Maven e diffs brutos conservam os espaços originais, sujeitos a hashes, sem normalizar evidência para satisfazer esse check. target/ fica fora dos commits.
