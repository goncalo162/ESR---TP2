# IMP-012 — interfaces/tipos e testes vermelhos

[Walkthrough](../../../../memory-bank/bolts/023-arvores/implementation-walkthrough.md).
Base main/b273b258, revisão ADR aprovada; somente primeiro passo de Implement
autorizado, comportamentos/Test ainda não autorizados.

- [Entrada](entry.json), [preservação](preservation.json).
- [Negativo inicial:21 testes/19 falhas](red-01/result.json).
- [Negativo atual:22 testes/20 falhas](red-02/result.json),
  [log Maven](red-02/maven.log), XML/TXT no mesmo diretório.
- [Históricos:30/0/0/0, incluindo6 M07](historical-activation-01/result.json).

Compilação passou, sem erros/ignorados; falhas são asserções dos comportamentos
13/14/cadência/poda ausentes e cap antes de cópia. Duas salvaguardas antigas
passaram. Só tipos imutáveis internos adicionados a main, sem ligar o engine.
22 testes novos com relógio/owner/scheduler/port falsos, sem TCP/sockets.

Não se conta o teste vermelho como V-015 aprovado nem se afirma que todos os
asserts posteriores de segurança física foram alcançados. Regressão integral,
integração real, clean verify/JAR e manual humano pendentes, tal como revisão
final Denji/integração. Os1328 de IMP-011 são históricos.
Sem Test/staging/commits/push; target separado; negativos anteriores preservados.

[Diff Java dos três ficheiros](java.diff), [numstat Java](java.numstat),
[resumo](diff-summary.json), [diff cumulativo documental+Java](cumulative.diff),
[numstat cumulativo](cumulative.numstat). Target separado em target.numstat.
[V-ADD documental](v-add-initial.md); relatório final guardado na mesma pasta.
