---
unit: "005-video"
bolt: "025-video"
stage: "test"
status: "complete"
created: "2026-11-25T16:49:09Z"
updated: "2026-11-25T17:05:33Z"
---
# IMP-018 — Test aceite por condição expressa; revisão Denji confirmada

Estado atual, 2026-11-25T17:05:33Z: quatro conteúdos completos verificados com o JAR fixado,
500/5965/8880/7342 frames, hashes/EOF/cleanup e comparação de cada Frame aprovados.
[Prova integral](../../../experiments/construction/025-video/test-02/full-content-consolidation.json).
Depois do negativo pessoal preservado, o utilizador delegou a repetição em Codex
e autorizou commit/fecho se correto. Run delegada saída0; condição satisfeita.
Não se declara manual humano concluído. Denji reviu/aprovou esta versão, confirmado
pelo utilizador. [Registo](../../../experiments/construction/025-video/test-02/acceptance-and-review.json).
Nenhum build repetido, código/JAR inalterados. Integração local por concluir antes
do fecho; sem push ou IMP-019. TASK-020 só fecha apósA/B/C.

**Relatório do checkpoint anterior, histórico (quatro binários então ausentes):**

# IMP-018 — Test técnico executado; conteúdos completos e manual pendentes

Implement aprovado pelo utilizador; apenas Test autorizado. **Não se pede
aprovação de Test**: falta verificar os quatro conteúdos completos com o novo
VideoSource e executar o manual humano aplicável. Não há fecho, commit/push
ou IMP-019. Revisão final Denji e integração continuam obrigatórias;
TASK-020 permanece IN PROGRESS e só fecha com A/B/C.

## Build limpo e novo artefacto

JDK21.0.13.1+2, Maven offline, release21; sockets TCP locais reais autorizados.
Comando em Projeto/:

```sh
mvn -o -B -ntp -DargLine=-Djava.awt.headless=true clean verify
```

**1582 testes, zero falhas/erros/skips,23 XML, saída0,43.624s.**
1537 históricos +45 do VideoSource; mesma contagem da regressão de Implement.
Nenhuma remoção ou alteração de código/testes/asserções aprovada foi aplicada.
[Comando/ambiente/hashes](../../../experiments/construction/025-video/test-01/clean-verify/result.json),
[log](../../../experiments/construction/025-video/test-01/clean-verify/maven.log).

[JAR novo preservado](../../../experiments/construction/025-video/test-01/clean-verify/ott-streaming-1.0-SNAPSHOT.jar), 543438 bytes.
SHA-256: `f9a3cb9cad6166d13e244a772398179731952fd8eb8ff0efe7b16a6f67b9787f`.
[SHA-256 em ficheiro](../../../experiments/construction/025-video/test-01/clean-verify/ott-streaming-1.0-SNAPSHOT.jar.sha256).
As12 classes VideoSource do JAR têm major65; não contém VideoSourceTest.
Os recursos media empacotados são os ponteiros atuais, não vídeos completos.
[Inspeção do JAR](../../../experiments/construction/025-video/test-01/jar-inspection.json).

## Consolidação M01–M14 / V-020-B

[Consolidação estruturada](../../../experiments/construction/025-video/test-01/consolidation.json)
associa os45 métodos JUnit aos14 grupos. Resultados desta tabela vêm do novo
clean verify, não da contagem antiga. Sem percentagem de cobertura inventada
ou novo gate de ferramenta.1032 cortes de corpo,8 prefixos parciais e50 casos
não decimais ficam agrupados nos métodos, não são testes adicionais à contagem.

| Grupo | Métodos | Resultado observado |
|---|---|---|
| M01 | 1 | PASS — oráculos locais do Model/Design |
| M02 | 1 | PASS — oráculos locais do Model/Design |
| M03 | 1 | PASS — oráculos locais do Model/Design |
| M04 | 1 | PASS — oráculos locais do Model/Design |
| M05 | 1 | PASS — oráculos locais do Model/Design |
| M06 | 2 | PASS — oráculos locais do Model/Design |
| M07 | 1 | PASS — oráculos locais do Model/Design |
| M08 | 7 | PASS — oráculos locais do Model/Design |
| M09 | 10 | PASS — oráculos locais do Model/Design |
| M10 | 8 | PASS — oráculos locais do Model/Design |
| M11 | 1 | PASS — oráculos locais do Model/Design |
| M12 | 2 | PASS — oráculos locais do Model/Design |
| M13 | 6 | Guardas/tipos/raster/decoder e fixtures PASS; quatro conteúdos integrais PENDENTES |
| M14 | 3 | PASS — oráculos locais do Model/Design |

M01–M12/M14 e a parte local de M13 exercitam bytes/offsets, limites, erros,
EOF, imutabilidade, isolamento, cleanup e aritmética. M13 não fica totalmente
aceite pela dimensão equivalente das fixtures. **V-020-B permanece pendente**
da prova integral exigida e da aceitação humana aplicável.

## Quatro conteúdos: prova não executada

[Disponibilidade atual](../../../experiments/construction/025-video/test-01/media-availability.json):
A/B/C/D são ponteiros LFS de132/134 bytes; os objetos não estão no cache local,
sem lfs.storage alternativo configurado. Não houve download/fetch/materialização.
Bytes completos esperados:459820128; frames:22687 (500/5965/8880/7342).
Não existem novos hashes consumidos, contagens, EOF ou cleanup da leitura integral.
Os hashes declarados nos ponteiros e os22687 registos históricos não substituem
esses resultados pelo novo leitor. Os requisitos mantêm-se pendentes.

O verificador preparado compara hash/bytes aprovados, lê todos os frames com
o VideoSource do JAR, confere cada payload/índice/offset com o JSONL aprovado,
regista hash consumido/EOF/END e fecho único da entrada real. O reader JPEG é
o da produção, sem provider simulado. O controlo com2 fixtures prova apenas
o funcionamento do verificador; nunca é apresentado como A/B/C/D completo.

## Manual preparado e ensaio técnico

[Guião de sete etapas](../../../experiments/construction/025-video/manual-instructions.md)
e [runner](../../../experiments/construction/025-video/test-manual-tools/run-checks.py).
Repetição humana ainda não executada. kind=human exige terminal, operador e
respostas; rehearsals nunca constituem confirmação humana.

- Negativo de ensaio20261125T164445Z-4f90b945: classpath do lançador JShell
  local não encontrava SourceException. JAR contém a classe, conferido por ZIP
  e javap. Mesmo script/JAR passou num controlo que apenas acrescentou o JAR
  ao classpath da JVM do JShell. Log/comando/snapshots/controlo preservados;
  corrigido só o lançador de verificação, sem alterar Java/testes aprovados.
- [Ensaio atual](../../../experiments/construction/025-video/test-manual-runs/20261125T164607Z-a961bb8c/report.json): checks locais e
  controlo do verificador passaram; saída2, pending_full_media, nenhuma
  confirmação humana. A/B/C/D explicitamente pendentes por LFS_POINTER.

16MiB limitam apenas raster destino, não consumo total do decoder/JVM.
Não se mediu playback/FPS nem streaming ponta a ponta, que não pertencem ao bolt.
O negativo inicial R17 continua sem causa estabelecida; integração com métricas
controladas não demonstra a causa. Negativos de Implement e demais históricos
permanecem intactos; não houve repetição até verde sem diagnóstico.

## Seis problemas V-ADD resolvidos por patch autorizado

O utilizador autorizou exclusivamente o
[patch apresentado](../../../experiments/construction/025-video/test-01/v-add-proposal/resolution.patch).
Aplicado byte a byte conforme proposta em3 paths; [prova](../../../experiments/construction/025-video/test-01/v-add-proposal/application.json).

| Pendência | Resolução aplicada |
|---|---|
| VADD-01 | Fence exterior de quatro backticks e fecho no exemplo Bolt Sequence de inception/bolt-plan.md |
| VADD-02 | Fence exterior de quatro backticks e fecho no exemplo de histórico de operations/deploy.md |
| VADD-03–06 | Quatro links indisponíveis da demo convertidos em paths históricos marcados removidos, sem apagar os resultados/negativos |

[V-ADD novo](../../../experiments/construction/025-video/test-01/v-add-after-patch.md):
**saída0, zero erros**. O validador não foi modificado/excluído/enfraquecido;
18 paths removidos da demo continuam ausentes. Não se restaurou a demo nem
se declarou o protótipo aprovado. Relatórios anteriores com seis erros intactos.

## Checkpoint

Test em curso, não aprovado; completed=null e Test fora de stages_completed.
Faltam os quatro binários completos com prova do novo leitor e a repetição
humana do manual. V-ADD resolvido não dispensa essas obrigações. Depois da
aceitação de Test continuam revisão final Denji e integração, sem novos gates
externos. Sem fecho/commits/push/IMP-019; TASK-020 só fecha após A/B/C.
