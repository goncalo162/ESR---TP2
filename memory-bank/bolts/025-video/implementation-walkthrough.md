---
unit: "005-video"
bolt: "025-video"
stage: "implement"
status: "complete"
created: "2026-11-25T16:26:32Z"
updated: "2026-11-25T16:28:55Z"
---
# IMP-018 — Leitor MJPEG, Implement para revisão

Implement aprovado explicitamente pelo utilizador; registo 2026-11-25T16:37:27Z.
Apenas Test autorizado. O checkpoint de Implement abaixo é histórico.
Model, Design e ADR concluídos. Implement **pendente de aceitação**; não se
avançou a Test, fecho ou outro IMP. Sem commits/push.

## Entrega e âmbito

Apenas dois caminhos de código:

- [VideoSource.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/server/VideoSource.java):
  leitor síncrono, single-caller, sem integração de streaming.
- [VideoSourceTest.java](../../../Projeto/src/test/java/pt/uminho/esr/ott/server/VideoSourceTest.java):
  fixtures independentes incorporadas, entradas/providers controlados e decoder
  real JDK21 em headless. Nenhum teste histórico alterado ou removido.

API pública: `open(Path,Path,int)`, `nextFrame()`, `state()`, `close()`;
`Frame`, `Diagnostic`, `SourceException`, `State`, `Stage`, `Reason` aninhados.
Não existia API anterior nesse placeholder vazio. Helpers e seams de teste
ficam aninhados, respeitando os dois caminhos aprovados e CODESTYLE.md.

Sem dependências, configuração, protocolo, threads, filas, pacing, loop,
emissão, player ou mudanças em StreamingServer. TASK-020/C continua futura.

## Comportamento implementado

Paths reais contidos na base, ficheiro regular, abertura READ/NOFOLLOW_LINKS;
sem fallback de symlinks. Não é isolamento atómico de diretórios-pai perante
troca hostil concorrente. Argumentos inválidos não transferem ownership.

Cada registo tem prefixo ASCII de cinco dígitos e exatamente L bytes, com
1≤L≤B≤99999 antes de reservar o corpo. Leituras curtas positivas progridem;
contagem zero falha imediatamente com READ_IO/NO_PROGRESS, sem retry, fallback
unitário ou available(). EOF limpo apenas na fronteira, com fecho bem-sucedido.
Índice e offsets usam aritmética verificada. Frame copia o payload na construção
e em cada getter; só é publicado após validação e cleanup transitório.

O scanner JPEG limita-se ao payload e verifica segmentos/SOF/SOS, stuffing,
restarts, scans progressivos e primeiro EOI estrutural no fim exato. APP/COM
são saltados por comprimento. Não substitui o decoder de entropia: ImageReader
JPEG real continua obrigatório, e qualquer warning impede entrega.

Guardas antes de decode: largura/altura≤4096, pixels≤4194304, destino canónico
BYTE_GRAY/3BYTE_BGR/INT_RGB, raster≤16777216 bytes. SOF é conferido antes da
factory; tamanho/tipo do reader são conferidos antes de alocar o destino.
ImageReadParam usa destino explícito, sem clipping/subsampling/offset ou
seleção parcial dos scans. **16 MiB limitam apenas o raster de destino**, não
memória total, metadados, buffers nativos, cópias do consumidor ou JVM.
Não há prazo/cancelamento de decoder bloqueado nem promessa de recuperar OOM.

## Ownership, primeiro erro e cleanup

Sessão possui a entrada após retorno da factory. O validador possui entrada
local do payload, MemoryCacheImageInputStream, reader, destino e, se o provider
violar a pós-condição, uma imagem extra distinta. Reader é registado antes de
setInput e consultas. Cache não fecha a sua entrada: ambos são libertados
explicitamente; não se altera ImageIO global nem se usam temporários de cache.

Coletor limitado a cinco identidades transitórias mais a entrada da sessão
(seis no total), com deduplicação por identidade e cleanup em ordem inversa.
Cada recurso é destacado antes da tentativa; uma falha não impede as seguintes.
Retorno==destino não duplica flush; retorno distinto é registado antes da
comparação e também recebe flush, mesmo havendo outra falha de cleanup.

A primeira falha checked é SourceException com causa e diagnóstico local;
RuntimeException/Error inesperados mantêm o mesmo objeto/tipo. Ausência de
provider é DECODER_UNAVAILABLE checked; retorno nulo/distinto de um reader
selecionado é IllegalStateException, conforme A05. Cleanup posterior, mesmo
Error, fica secundário; nunca promove ou substitui o primário. Cleanup como
primeira falha é CLOSE_IO se IOException, ou o objeto unchecked original.

Sem autosupressão, repetição da mesma causa/identidade ou crescimento em novas
chamadas: FAILED guarda e relança o mesmo terminal sem I/O; close em FAILED,
END ou CLOSED é inócuo. Assim try-with-resources não volta a lançar o primário
no close. Fecho que falha é tentativa observável, não libertação garantida.

## Verificações atuais de Implement

JDK21 fixado por JAVA_HOME/PATH, compilação release21, Maven offline; sem
instalar ferramentas. Comandos, ambiente, hashes e resultados separados em
[implement-01](../../../experiments/construction/025-video/implement-01/README.md).

- focused-01: negativo de compilação preservado; dois erros de inferência de
  tipo Class em assertThrows. Nenhum teste executado. Corrigidas apenas duas
  declarações no teste, sem alterar expectativas; snapshots/log/hashes guardados.
- focused-02: 42 testes, zero falhas/erros/skips, JDK21/headless.
- focused-03: 45 testes, zero falhas/erros/skips; inclui consultas checked,
  limites de cleanup e JPEGs independentes nas dimensões dos conteúdos aprovados.
- regression-01: **1582 testes, zero falhas/erros/skips, 23 XML**, Maven offline
  com JDK21 e TCP local real; 41.942 s. São1537 históricos +45 novos.
  Regressão incremental `mvn test`, não clean verify ou aceitação de Test.

Os casos negativos esperados permanecem em testes, juntamente com os controlos
positivos. Não foram apagados históricos, negativos anteriores ou a limitação R17.

## M01–M14 / V-020-B: correspondência aos oráculos

| Grupo | Oráculo executado nos testes novos |
|---|---|
| M01 | Dois JPEGs literais independentes, SHA-256, ordem/offsets/bytes exatos |
| M02 | Leituras bulk limitadas a1/2/3 bytes preservam os frames |
| M03 | Vazio e EOF na fronteira; repetição sem I/O e fecho único |
| M04 | Oito prefixos truncados: cortes1..4 no primeiro/segundo registo |
| M05 | Dez bytes não decimais em cada posição; sem leitura posterior ao erro |
| M06 | Zero, B−1/B/B+1, excesso antes do corpo/decoder, JPEG real com99999 bytes |
| M07 | 1032 cortes:684 no primeiro corpo RGB e348 no segundo GRAY; primeiro frame preservado |
| M08 | JPEG inválido/concatenado/sufixo/EOI precoce/segmentos truncados, progressive/COM reais; stuffing/restarts/multiscans com decoder controlado; warning, provider ausente, read checked, retorno nulo/distinto |
| M09 | Paths/abertura, leitura em prefixo/corpo, consultas/read do provider, checked/Runtime/Error, cleanup secundário, causa/identidade e try-with-resources |
| M10 | Fecho em cada estado, antes/depois de frame, todos os recursos distintos mesmo em erro, entrada/cache/reader/imagem extra; seis recursos e secundários limitados |
| M11 | Mutação do buffer retido e de bytes() não afeta Frame nem sessão |
| M12 | Sessões intercaladas e duas aberturas do mesmo path; erro/fecho/posição isolados |
| M13 | SOF excedido antes de factory; dimensões do decoder antes de read; fronteiras4096/4MP/16MiB, tipos canónicos; decoder real headless em384×288 e640×360 |
| M14 | Zero progresso em prefixo/corpo, contagens inválidas sem fallback; helpers de soma/índice emLong.MAX_VALUE sem ficheiro gigante |

M13 com headers sintéticos e reader controlado demonstra guardas/ordem, não
validade de entropia nem leitura dos vídeos aprovados. Progressive/COM e as
fixtures positivas usam decoder JPEG real, sem gerar expected pelo VideoSource.
45 métodos JUnit agrupam os casos;1032 cortes não são1032 métodos separados.

## Limitação dos quatro conteúdos e evidência histórica

Os quatro paths aprovados desta checkout contêm apenas ponteiros LFS de
132/134 bytes; OIDs coincidem com os hashes aprovados da auditoria de IMP-017.
[Disponibilidade atual](../../../experiments/construction/025-video/implement-01/media-availability.json).
Não houve fetch/materialização, instalação, cópia/conversão ou alteração dos
recursos. A leitura dos22687 JPEGs pelo novo leitor **não foi executada**.

Os limites aprovados continuam compatíveis aritmeticamente com a auditoria
anterior: A384×288, B/C/D640×360, maiorJPEG66091≤99999. Fixtures sintéticas
independentes nessas dimensões passam no novo leitor real; não substituem a
verificação dos quatro conteúdos integrais antes da aceitação pertinente.

Fixtures pequenas foram geradas uma vez com Pillow9.0.1 já instalado e ficaram
como literais Base64 no único teste. Pillow não é dependência do projeto nem
é invocado por Maven/JUnit. Proveniência, dimensões e hashes preservados em
[fixture-provenance.json](../../../experiments/construction/025-video/implement-01/fixture-provenance.json).
A auditoria de IMP-017 e os seus16 negativos são históricos intactos; não são
reciclados como resultado do novo leitor. A causa do negativo inicial R17
continua desconhecida; integração com métricas controladas não a demonstra.

## Pendências e checkpoint

Implement aguarda revisão do utilizador. Não há relatório Test nem aprovação
presumida de V-020-B consolidado. O V-ADD voltou a encontrar exatamente os mesmos seis problemas, sem erros
adicionais; continuam pendentes antes do fecho, sem validação aprovada ou
restauro da demo. `git diff --check` passou; a conferência de257 ficheiros
protegidos encontrou apenas a alteração e o novo teste autorizados. As fontes
atuais coincidem com os hashes do foco e da regressão guardados.
Continuam testes/regressãoJDK21 de aceitação, manual humano aplicável, revisão
final Denji e integração. Sem gates externos adicionais; TASK-020 só fecha
com A/B/C. Sem Test, fecho, commits, push ou início de IMP-019.

## Aprovação de Implement e seguimento — 2026-11-25T16:50:39Z

Implement aprovado explicitamente; código/testes preservados. A etapa atual
é [Test](ddd-03-test-report.md), sem aprovação/fecho. O checkpoint de Implement
acima é histórico. Clean verify novo1582/0/0/0, JAR/SHA-256 guardados; V-ADD
resolvido por exceção documental autorizada; conteúdos integrais/manual pendentes.
