---
stage: closure
bolt: "025-video"
completed: "2026-11-25T17:11:00Z"
---
# IMP-018 / 025-video — concluído; TASK-020 apenas A/B

Fecho autorizado pelo utilizador após delegar a repetição do teste em Codex e
condicionar commit/fecho a resultado correto. Nova run kind=automated saída0,
Test aceite por essa condição. Denji reviu e aprovou esta versão, confirmado
explicitamente pelo utilizador. Integração local real em main: `e89664387b6235505b53f6de344392e5945ac89f`.
[Prova da integração](../../../experiments/construction/025-video/integration-01/integration-proof.json).
Bolt/história complete, implemented=true. TASK-020 pai e PHASE-05/unidade005
mantêm IN PROGRESS; parteC/IMP-019 não iniciada. Não houve push.

## Entrega aceite

- Leitor síncrono pull5ASCII+LJPEG, paths admitidos, erros localizados,
  Frame imutável, EOF e ownership/cleanup. Só VideoSource.java/VideoSourceTest.java.
- Model/Design/ADR/Implement/Test aprovados; A05/A06 e guardas preservados.
- Clean verify JDK21 conferred:1582/0/0/0,23 XML;45 testes M01–M14 novos.
  Não se repetiu build após a conferência do utilizador.
- JAR fixado f9a3cb9cad6166d13e244a772398179731952fd8eb8ff0efe7b16a6f67b9787f.
- Quatro binários completos recuperados do Git LFS, hashes aprovados;459820128
  bytes.500/5965/8880/7342 frames, total22687, efetivamente lidos/descodificados
  pelo novo VideoSource. Cada índice/offset/comprimento/SHA-256 JPEG comparado
  com o JSONL independente aprovado. EOF/END e fecho físico da entrada uma vez
  emcada ficheiro, inclusive após close repetido. Não se reutilizou a auditoria
  anterior como resultado novo. Reader/raster são cobertos por M10; não se
  contaram esses descartes separadamente por frame na passagem integral.
- Seis problemas V-ADD corrigidos pelo patch documental exclusivo autorizado;
  validador inalterado e zero erros. Os18 paths da demo continuam removidos.

[Test](ddd-03-test-report.md), [prova integral](../../../experiments/construction/025-video/test-02/full-content-consolidation.json),
[nova run delegada](../../../experiments/construction/025-video/test-manual-runs/20261125T170246Z-17a25dc1/report.json),
[aceitação/revisão](../../../experiments/construction/025-video/test-02/acceptance-and-review.json).

## Exceção pessoal, evidência e limites

A run humana de Luis20261125T165933Z-c5eb7f59 permanece negativa, inalterada:
path sem /media, etapa6 recebeu sisim. Não ficou concluída e não foi convertida
em sucesso. O utilizador substituiu depois essa exigência pessoal, **neste
fecho apenas**, por execução delegada em Codex e autorizou commit/fecho se
correto. Não se declara manual humano concluído nem se dispensa o manual de
outros bolts por inferência. Condição satisfeita pela nova run automática.

Preservados os negativos de compilação, ensaios parciais, logs e bytes brutos
Maven/patch. Git diff --cached --check apresentou whitespace apenas nesses
artefactos brutos; código/documentos/dados estruturados passaram separadamente,
sem reescrever evidência ou relaxar validador. [Registo](../../../experiments/construction/025-video/commit-01/whitespace-decision.json).
Binários completos ficam em test-02/media localmente, ignorados por Git; os
ponteiros LFS canónicos já estão versionados. Não se duplicam460MB no commit,
nem se altera configuração/runtime/recursos da aplicação. O patch exato dos
templates locais ignorados fica versionado como evidência; não se força a
entrada do framework inteiro no repositório.

16MiB limitam apenas raster destino, não a memória total do decoder/JVM.
Não se entregam pacing, loop, emissão, player ou streaming integrado neste bolt.
LOCAL-03/cópia runtime e LOCAL-04/G-MEDIA-DISTRIBUTION mantêm o âmbito anterior;
não se aprova redistribuição/licença por este download técnico de dados já aprovados.
R17 inicial continua sem causa estabelecida; métricas controladas não a demonstram.
TASK-020 só fecha apósA/B/C. Sem outro IMP ou gate externo adicional.

Reconciliação derivada:026-video continua planned, started=null, current_stage=null,
stages_completed=[]; apenas blocks=false por009/025 estarem concluídos.
O primeiro V-ADD de fecho detetou esse flag desatualizado; negativo guardado,
metadado corrigido sem iniciar IMP-019 ou alterar o validador.
