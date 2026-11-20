# IMP-013 — correção R10 de rearme por elegibilidade métrica

[Entrada e hashes](entry.json), [diagnóstico](diagnosis.md) e
[walkthrough atualizado](../../../../memory-bank/bolts/008-arvores/implementation-walkthrough.md).
Apenas Implement, ainda pendente. Sem Test, fecho, commits ou push.

O [negativo exato](negative-01/result.json) reproduz Rig("R",false,4096,1),
parent A, métrica10, ANN G2/custo Long.MAX_VALUE-1000, PATH_CHANGED, T3s,
métrica2000 e regresso a10 sem novo ANN: esperado1/observado2 ACTIVATEs.
Log/XML/fontes preservados antes da correção; nenhuma asserção foi removida.

O delta de produção é uma guarda do watermark na recuperação de elegibilidade,
em RoutingService. Uma vista antes ausente já não dá crédito à prova bloqueada.
Primeira medição válida e geração validada nova posterior à falha conservam o
rearme limitado. ANN anterior à falha/duplicado continua bloqueado. Não se
alteram Route, RoutingTable, monitor, T/A/cadência, construtores ou físico.
[Patch de produção](RoutingService.java.patch), [patch de testes](RecoveryTest.java.patch)
e [35 métodos anteriores intactos](RecoveryTest-methods.json).

[11 testes R10 focados](focused-01/result.json) passaram.
[Regressão JDK21 completa](regression-01/result.json):1419/0/0/0,20 XML,
1379 históricos+40 RecoveryTest; destes,35 anteriores intactos e5 novos.
Os109 ficheiros novos CLI/subprocessos estão em regression-01/cli-deployments.
[Execuções](runs.json) separa o negativo dos sucessos. Nenhum JAR final novo.

A causa do negativo inicial TCP R17 continua não estabelecida. A correção R10
não o diagnostica e preserva toda a evidência anterior (246 ficheiros à entrada).
V-016 consolidado, manual humano antes de aprovar Test, revisão final Fillmore e
integração continuam pendentes. Sem gates externos adicionais.

V-ADD e git diff --check são verificação documental, distinta de Maven.
Status de âmbito e manifesto final registam hashes atuais e preservação anterior.
Os manifestos anteriores descrevem versões históricas; as suas referências aos
artefactos vivos continuam históricas, sem reescrever a evidência antiga.
