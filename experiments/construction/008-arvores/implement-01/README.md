# IMP-013 — Implement para revisão

[Entrada/autorização e hashes anteriores](entry.json).
[Walkthrough](../../../../memory-bank/bolts/008-arvores/implementation-walkthrough.md).
[Execuções separadas](runs.json), [regressão final JDK21](regression-02/result.json)
e [log final](regression-02/maven.log).1414 testes/0 falhas/0 erros/0 ignorados,
20 XML;35 testes novos,1379 históricos byte preservados.109 novos ficheiros de
CLI/subprocessos em regression-02/cli-deployments. Duas integrações TCP reais,
com métricas controladas e fecho/portas verificados. Sem JAR final novo.

Os negativos de focused-01/02/02-tcp/04, diagnostic-r13-01 e
negative-consumed-alternative-01 foram preservados com logs/XML/diagnóstico.
A causa do ERROR inicial de R17 TCP não foi estabelecida; a fixture atual controla
métricas/prefixo sem retirar asserções ou alterar prazos. Não confundir sandbox
sem permissão de sockets (focused-02) com a execução TCP fora dela (02-tcp).
consumed-alternative-fixed-01 prova a correção de R10 antes da regressão final.

Código só nos três caminhos do ADD; CODESTYLE foi lido e registado por hash.
Targets gerados ficam separados da proposta; não foram feitos commits/push.
Model/Design/ADR aprovados, current_stage=implement, Implement por aceitar;
V-016 consolidado/Test, regressão da etapa Test, manual humano antes de aprovar
Test, revisão final Fillmore e integração continuam obrigatórios. Sem gates novos.

Manifesto final e status-and-scope.json conferem código, testes históricos e
27 ficheiros de evidência Model/Design/ADR anteriores. Os seus manifestos são
históricos, não obrigações de conservar o artefacto vivo sem nota de aprovação.
V-ADD/git diff --check são verificações documentais, separadas dos resultados Java.
