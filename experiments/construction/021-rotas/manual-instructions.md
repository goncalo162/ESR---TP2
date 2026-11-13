# IMP-008 / V-011 — manual offline para repetição humana

Preparado em 2026-11-13T17:42:46Z. **Execução humana pendente**. Test e D22 continuam por
concluir. Este roteiro usa só as APIs públicas do novo JAR: não é CLI do produto,
não abre sockets, não reproduz vídeo e não demonstra CORE. Tempo e custos são
entradas controladas; os segundos das etapas são simulados, não esperas reais.

## Artefacto fixo e preparação

- JAR: [ott-streaming-1.0-SNAPSHOT.jar](test-build-01/ott-streaming-1.0-SNAPSHOT.jar), 364704 bytes.
- SHA-256: `366210cf8c7656ad2948ae1c842796632085aaa4f09afcedcd00a49db0a0ecee`.
- JDK21 com java/javac; caminho local confirmado: `/usr/lib/jvm/java-21-openjdk-amd64`.
- Manifesto: [test-manual-manifest.json](test-manual-manifest.json). O wrapper
  confere JAR, seis fontes aprovados e os próprios auxiliares antes/depois.
  Não usa os classfiles mutáveis de Projeto/target nem JUnit.

A partir da raiz do repositório:

```bash
python3 -B experiments/construction/021-rotas/test-manual-tools/run-manual.py --operator Luis --kind human --java-home /usr/lib/jvm/java-21-openjdk-amd64
```

O comando cria e apresenta um diretório **novo** em test-manual-runs/. Não reutiliza
ou sobrescreve o ensaio do agente. Compila ManualRouting.java contra o JAR fixo,
guarda compile.log e usa o mesmo JAR como origem real de RoutingTable em runtime.
Prima Enter antes de cada uma das oito etapas. Leia os resultados e compare com
o ESPERADO; Ctrl+C interrompe e guarda falha/saída, sem aprovação automática.
EOF antes de uma confirmação de etapa não conta como execução completa.

## Resultados a conferir

| Etapa | Resultado esperado |
|---|---|
| 1 — Quatro streams | S1/A=130 porR2 (alternativa140 porR1), S1/B=240, S2/C=340, S2/D=450; snapshots imutáveis e sem autorização de dados |
| 2 — Empates e TTL | NodeId desempata; depois menos saltos vence. Melhoria aos7s mantém início0; aos8s expira e volta à alternativa130. Aos9s mesma geração é recusada pelo watermark |
| 3 — Retirada e sessão | Retirada deR2 preservaR1=140; geração retirada não regressa. Conexão nova na mesma instância começa sem RTT; custo novo produz11; callbacks antigos não alteram snapshot |
| 4 — Época | Mudança explícita deS1 invalida juntas A/B; snapshots deS2 ficam iguais; época retirada e anúncio antigo não revertem |
| 5 — Histórico | Oito épocas protegidas; nona retirada recusa sem apagar A=6/B=7; aos24999ms ainda recusa, aos25000ms permite por expiração dos históricos de9s |
| 6 — Capacidades | Limits pequenos saturam origens/streams/sessões sem evicção; atualizar slot existente continua possível |
| 7 — Fonte local | Custo0 sem parent/dados; época nova retira ambas as ofertas locais |
| 8 — Overflow | Soma até Long.MAX_VALUE aceite; anúncio com overflow não substitui saudável; métrica impossível conserva anúncio inelegível e recuperação preserva início |

Condições finais esperadas: **43 CHECK_PASS**, `CHECK_COUNT=43`, `MANUAL_PASS`,
`JAVA_EXIT=0` e `PASSED=true`. Não aceitar sucesso parcial ou erro de hash.

O diretório novo contém entry.json, compile.log, manual.log, java-pid.json,
classes auxiliares e **report.json** com operador/tipo declarados, comandos,
hashes, origem de classe, saída e terminação da JVM criada. O wrapper não
comprova a identidade da pessoa: a confirmação humana é registada só depois
por resposta do utilizador, associada a esse relatório e ao JAR validado.

Quando terminares, envia o caminho do report.json e confirma o resultado e
qualquer divergência observada. Não existe ainda um caminho de execução humana
para registar: só será criado quando executares o comando.

## Ensaio técnico do agente — separado

[Relatório Codex](test-manual-runs/20261113T173843Z-92dfcjfm/report.json) e
[log](test-manual-runs/20261113T173843Z-92dfcjfm/manual.log):43 CHECK_PASS, MANUAL_PASS,
JAVA_EXIT0, passed=true; hashes e origem do JAR conferidos e JVM terminada.
kind_declared=agent. Não é a execução humana nem aprovação de Test.

Fecho continua dependente de V-011/consolidação, manual humano antes de aprovar
Test, revisão final e integração. Não foi autorizado commit, push ou outro IMP.


## Execução humana posteriormente recebida e aprovada

Registo 2026-11-13T17:59:03Z. Luis executou o roteiro em
[test-manual-runs/20261113T174838Z-y23kpe_b/report.json](test-manual-runs/20261113T174838Z-y23kpe_b/report.json):
43 CHECK_PASS,MANUAL_PASS,Java0,passed=true,hashes corretos e JVM terminada.
O utilizador confirmou essa execução e aprovou Test. Registo inicial de manual
pendente acima é histórico; o relatório original foi preservado byte a byte.
