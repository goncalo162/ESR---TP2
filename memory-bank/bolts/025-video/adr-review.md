---
unit: "005-video"
bolt: "025-video"
stage: "adr"
status: "complete"
created: "2026-11-25T16:00:22Z"
updated: "2026-11-25T16:10:17Z"
---
# IMP-018 — Revisão ADR sobre fontes e APIs reais

**Revisão e A05/A06 aprovados pelo utilizador;registo 2026-11-25T16:10:17Z.**
Estado atual: apenas Implement nos dois caminhos autorizado, sem avanço aTest/fecho.
[Implement para revisão](implementation-walkthrough.md). O texto da conferência
abaixo é histórico, anterior à autorização de código e às verificações atuais.

**Snapshot da conferência ADR:** Model,
Design e guardas aprovados; apenas esta revisão autorizada. Nenhum Java,
teste, configuração, protocolo, demo, template, commit ou push alterado.
M01–M14/V-020-B continuam por executar; V-ADD mantém seis erros pendentes.

## 1. Conclusão e decisões a rever

Não foi identificado impedimento de API nem necessidade de ampliar os dois
caminhos. Reutilizar ADR-006/009/021/024; não propor uma nova ADR arquitetural.
As concretizações A01–A08 abaixo devem integrar a aprovação desta revisão
antes de qualquer Implement.

Há duas precisões explícitas em relação ao texto genérico do Design:

- **A05:** distinguir ausência de um provider de um `ImageReader` que viola
  o seu contrato. Com destino explícito, retorno nulo/distinto, sem erro
  checked de formato, é falha inesperada do provider. Continua rejeitado,
  mas não é rotulado automaticamente como corrupção do JPEG.
- **A06:** se um provider defeituoso devolver outra imagem, essa imagem
  também precisa de cleanup. O teto conservador do coletor passa de5 para
  **6 recursos/suprimidas**, apenas nesse ramo anormal; não altera os limites
  de pixels/raster ou o âmbito. O primeiro erro, seja checked ou inesperado,
  continua primário. Esta precisão é apresentada agora, antes de código.

Não se declara o scanner correto ou os quatro vídeos lidos pelo futuro
VideoSource: ainda não há essa implementação. A aprovação desta revisão
não satisfaz testes, regressão, manual humano, revisão final ou integração.

## 2. Base real e método da conferência

[Inventário com hashes](../../../experiments/construction/025-video/adr-review-01/project-api-inspection.json),
[assinaturas JDK21](../../../experiments/construction/025-video/adr-review-01/jdk21-public-apis.txt)
e [APIs auxiliares](../../../experiments/construction/025-video/adr-review-01/jdk21-support-apis.txt).

- `server/VideoSource.java` existe e tem0 bytes. `VideoSourceTest.java` não
  existe; a busca nos fontes Java não encontrou consumidores atuais de
  VideoSource. Não há construtor/factory anterior a quebrar nesse placeholder.
- `StreamConfiguration.mediaFile()` é Path; `NodeConfiguration.mediaBase()`
  é Optional<Path>; `runtime().source().maxFrameBytes()` já existe e é limitado
  a1..99999. O loader verifica pertença lexical à base, mas não efetua a
  admissão real do ficheiro/symlinks prevista para abertura da fonte.
- POM tem release21 e não tem dependência runtime nova a acrescentar. O JDK
  instalado21.0.13.1 inclui java.desktop e o JPEGImageReader. As assinaturas
  e bytecode foram inspecionados com `javap`; não se executou um leitor, teste
  ou compilação. src.zip local está indisponível; não se instalou software.
- A configuração continua a fornecer caminhos e limite. Não são necessários
  getters, chaves, mensagens ou mudanças de configuração para esta API.

APIs públicas próprias aprovadas em Design continuam futuras:
`open(mediaBase,file,maxFrameBytes)`, `nextFrame()`, `state()`, `close()` e
tipos aninhados. A correspondência estática com os dados existentes não é
uma integração executada com StreamingServer/IMP-019.

## 3. A01 — Paths, admissão e compatibilidade

Conferidos Path.toRealPath, Files.readAttributes/newInputStream e
LinkOption.NOFOLLOW_LINKS. A enum LinkOption é também OpenOption; a inspeção
do provider Unix deste JDK confirma tratamento dessa opção, sem abrir um
ficheiro durante a revisão. [Evidência local](../../../experiments/construction/025-video/adr-review-01/jdk21-unix-open-options.txt).

Sequência a manter no único ficheiro VideoSource.java:

1. Validar argumentos/limite antes de I/O. Argumentos rejeitados não
   transferem posse de entrada fornecida pelo chamador.
2. Canonicalizar a base e o alvo por caminho real; exigir alvo dentro da
   base, distinto dela. `file` relativo resolve-se na base; absoluto só se
   continuar contido. Escape por symlink falha antes de abrir o corpo.
3. Usar `readAttributes(realFile,BasicFileAttributes.class,NOFOLLOW_LINKS)`
   e exigir ficheiro regular. É preferível ao booleano `isRegularFile`,
   porque a IOException concreta de acesso deve continuar como causa.
4. Abrir em READ+NOFOLLOW_LINKS. Sem fallback que siga links se o provider
   recusar a opção. Factory possui a entrada até transferir à sessão; erro
   entre abertura e entrega exige tentativa de fecho com primário preservado.

Resolver caminho real e abrir não constituem sandbox atómica contra troca
hostil dos diretórios-pai; permanece a fronteira de laboratório do Design.
Não se introduz SecureDirectoryStream/novo serviço por inferência nem se
afirma proteção que não foi implementada. [Files/JDK21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/Files.html),
[LinkOption/JDK21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/LinkOption.html).

## 4. A02 — Posse entre factory, sessão e decoder

`fromOwnedInput` é package-private; não altera configuração nem cria outra
origem pública. O chamador conserva a entrada até retorno com sucesso; a
sessão fica responsável depois. Na factory de Path, a própria factory é
dona desde newInputStream; se não entregar sessão, não deixa a entrada órfã.

Precisar a ReaderFactory aninhada do Design:

- O ImageInputStream é **emprestado**, continua a pertencer ao validador.
  A factory não o fecha nem transfere a propriedade do payload.
- A factory cria/seleciona apenas o reader. Produção obtém um provider JPEG
  pelo mecanismo público `ImageIO.getImageReadersByFormatName("JPEG")`;
  nenhum provider disponível corresponde a DECODER_UNAVAILABLE checked.
- Antes de devolver um reader, a factory possui qualquer instância que
  criou; se falhar antes de a entregar, deve libertá-la. No retorno, o
  validador regista a posse **antes** de setInput/listeners/consultas.
- `setInput(payloadStream,true,true)` é feito pelo validador. O cache por
  frame permite as operações internas, sem pesquisa de várias imagens ou
  reutilização de reader entre frames. Não se muta o registry ou useCache global.

O mecanismo de seleção existe no [ImageIO/JDK21](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/imageio/ImageIO.html#getImageReadersByFormatName(java.lang.String)).
Não se usa ImageIO.read por conveniência: a política de seleção e recursos
precisa de ser explícita e não pode depender de defaults de cache em disco.

## 5. A03 — Comprimentos, progresso e erros observados

Os limites existentes de configuração são compatíveis com B=1..99999.
O primeiro byte do prefixo é observado antes da reserva aritmética para
outro registo; EOF numa fronteira continua limpo. Prefixo5, corpoL, índices e
offsets usam a aritmética verificada do Design, sem import de política de UDP.

READ_IO/NO_PROGRESS após a primeira contagem bulk0, sem retry/fallback.
Uma leitura curta positiva é progresso; não se infere EOF de available.
Não são necessárias APIs ou threads novas para isso. A validação deve
considerar a primeira IOException **observada pela chamada**, preservando
a sua causa, sem tentar reconstruir erros que uma implementação de entrada
possa ter absorvido internamente.

| Primeiro acontecimento | Classificação proposta |
|---|---|
| Argumento inválido antes da admissão | NPE/IllegalArgumentException; sem posse ou I/O |
| Falha real de path/abertura | SourceException OPEN_IO com causaIOException |
| Prefixo/corpo/estrutura JPEG inválidos, limite excedido | SourceException com a categoria local do Design |
| IOException ao consultar/ler JPEG | SourceException INVALID_JPEG com causa; sem alteração dos bytes |
| Falha checked ao criar provider | DECODER_UNAVAILABLE com causa |
| RuntimeException/Error inesperada | Conservar objeto/tipo, cleanup e relançar; nunca converter em EOF/INVALID_JPEG |

Uma leitura posterior emFAILED volta a lançar o primeiro terminal; não
reativa recurso, não acrescenta suprimidas e não tenta resync.

## 6. A04 — Scanner JPEG: conferência de estrutura, não teste executado

Não existe scanner emVideoSource para auditar: o ficheiro está vazio. A
revisão confere a proposta com o formato JPEG/T.81, não declara runtimeverde.

O scanner fica interno no mesmo ficheiro e não substitui ImageIO. Deve
distinguir segmento com comprimento de dados de scan; não procurar EOI com
uma busca bruta no array. Guardas: SOI inicial, SOF distinto deDHT/DAC,
comprimentos contidos emL, dimensões positivas/limitadas, scans e stuffing,
primeiro EOI estrutural no fim exato. APP/COM são saltados por comprimento,
permitindo marcadores aparentes nos seus dados. Scans progressivos não
equivalem a outra imagem. [T.81, anexoB eF](https://www.w3.org/Graphics/JPEG/itu-t81.pdf).

Planear os controlos deM08/M13:

- APP/COM com FF D9 interno e JPEG integral no limite99999.
- JPEG concatenado, EOI precoce+lixo+EOI final e segmentos truncados.
- Byte stuffing/restarts e JPEG progressivo íntegro; não confundir os
  códigosDHT/DAC com SOF nem ler dimensões de offsets sem validar o segmento.
- Dimensões zero/diferidas são rejeitadas pela política local já aprovada;
  variantes não suportadas não são apresentadas como decode universal.
- O cursor deve avançar ou falhar em cada passo; cada índice/comprimento
  é limitado porL. Não acumular segmentos ou procurar dados noutro ficheiro.

Nenhum destes casos foi executado. A aceitação futura dos quatro conteúdos
tem de verificar também este scanner, não apenas citar o decoder da auditoria.

## 7. A05 — ImageReader, ImageReadParam e destino limitado

As consultas de tamanho/tipos, setInput, listener, read e dispose existem
nas classes reais do JDK21. Dimensões do SOF são verificadas **antes** de criar
o reader; getWidth(0)/getHeight(0) voltam a conferir guardas e concordância
antes de construir o destino. Sem getNumImages(true), thumbnails ou decode
parcial que esconda corrupção no resto da imagem.

Selecionar um ImageTypeSpecifier oferecido por getImageTypes(0) cuja forma
canónica seja BYTE_GRAY,3BYTE_BGR ouINT_RGB. Criar esse tipo/dimensões,
verificar o orçamento e fornecer o BufferedImage emsetDestination, com
parâmetros sem clipping/subsampling. O destino deve corresponder ao modelo
de cor/amostras admitido pelo reader; não se cria um raster arbitrário só
porque o seu número de bytes cabe. [ImageReadParam/JDK21](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/imageio/ImageReadParam.html#setDestination(java.awt.image.BufferedImage)).

**Precisão de contrato para aprovação:** ImageIO.read pode devolvernull
quando não encontra provider. Aqui a API usada é ImageReader.read(0,param),
com reader selecionado e destino não nulo. Retorno null ou de outra imagem
sem exceção de formato é violação inesperada dessa pós-condição: lançar
IllegalStateException local, não criar SourceException de corrupção de dados.
Nenhum frame é entregue. A rejeição de imagem ausente do Model mantém-se;
apenas a classificação fica correta para a API concreta.
[ImageReader/JDK21](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/imageio/ImageReader.html#read(int,javax.imageio.ImageReadParam)).

Um provider sem tipos, tipo null ou formato que viole a factory JPEG também
é falha inesperada. Tipos válidos mas fora da allowlist/orçamento produzem
IMAGE_LIMIT checked; warning emitido pelo decoder continua a rejeitar o
frame como INVALID_JPEG, sem interpretar o texto localizado como causa provada.
RuntimeException/Error lançado pelo provider é preservado sem reclassificação.

O buffer destino pertence ao validador desde a sua criação; o retorno, se
distinto por defeito do provider, é registado antes da comparação para também
ser limpo. Nenhuma destas imagens é o resultado público do leitor.

## 8. A06 — Cleanup completo e primário, incluindo falha inesperada

| Ponto de saída | Recursos já adquiridos que exigem tentativa de cleanup |
|---|---|
| Falha antes de admissão | Nenhum recurso do chamador |
| Falha na construção após newInputStream | Entrada aberta pela factory |
| Prefixo/corpo/scanner falham | Entrada da sessão; arrays ficam sem retenção |
| ReaderFactory falha | Entrada do payload/cache/sessão; a factory limpa reader não entregue |
| setInput/listener/consultas/tipo/destino falham | Reader já registado, cache e ambas as entradas |
| Decode/warning/pós-condição falham | Destino, imagem retornada distinta se existir, reader, cache e entradas |
| Frame válido | Recursos transitórios; entrada da sessão permanece aberta |
| EOF | Recursos transitórios já limpos e uma tentativa de fechar entrada da sessão |
| close explícito | Apenas recursos ainda possuídos; nenhuma nova tentativa emEND/FAILED/CLOSED |

MemoryCacheImageInputStream.close liberta o cache mas não fecha a entrada
subjacente; ImageReader.dispose é uma operação distinta de close. Ambos
estão explicitamente no lifecycle; não são substituídos porGC/finalize.
[MemoryCacheImageInputStream/JDK21](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/imageio/stream/MemoryCacheImageInputStream.html#close()).

**O tipo do primeiro erro rege o resultado final.** Se o primeiro erro é
IOException de leitura/formato, o primário é a SourceException com essa causa.
Se é RuntimeException/Error inesperada, o primário é esse mesmo objeto.
Um cleanup posterior, mesmo inesperado, é secundário e não promove/substitui
o primário. Se cleanup é a primeira falha: IOException→CLOSE_IO; Runtime/Error
continua unchecked, sem embrulho. Assim se precisa a linha genérica "cleanup
é a primeira falha" do Design, sem mascarar erros de infraestrutura.

Todos os recursos são destacados antes da sua tentativa; um erro não impede
as restantes tentativas. Fechos emFAILED não relançam o primário já notificado.
Isso evita que try-with-resources tente suprimir a exceção em si própria.
O coletor evita identidades repetidas/iguais ao primário ou causa, sem crescer
com chamadas repetidas. [Throwable/JDK21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Throwable.html#addSuppressed(java.lang.Throwable)).

**Precisão de contagem para aprovação:** os5 recursos do fluxo conforme do
Design tornam-se no máximo6 se um provider devolver uma imagem extra distinta.
Guardar até6 identidades de cleanup/secundários; cada imagem distinta é
libertada uma vez, sem duplicar quando retorno==destino. Não é quota global,
fila nova ou aumento do raster autorizado. Não se promete libertação de um
recurso cujo próprio cleanup falhe; o erro tem de permanecer observável.

## 9. A07 — Guardas aprovadas, alcance da memória e dados

Permanecem4096 por dimensão,4194304 pixels e16777216 bytes de raster destino;
cálculos long/checked antes de alocar. O limite comprimido B continua o da
configuração1..99999. Memória de ImageIO, metadados, buffers nativos, objetos
Java e cópias do consumidor **não** ficam abrangidos por um teto total16MiB.
Não se promete timeout/cancelamento de um provider bloqueado nem recuperar
de OutOfMemoryError por fingir que o JPEG era inválido.

Os registos já aprovados cobrem A384×288 eB/C/D640×360,22687 frames. Os caps
dimensionais são compatíveis por aritmética sobre esses registos; B=99999
comporta o maiorJPEG66091. Este facto não comprova scanner/destino/código
novos. [Evidência do Design](../../../experiments/construction/025-video/design-01/approved-audit-dimensions.json).

## 10. A08 — Âmbito, oráculos e checkpoint

Tipos/portos/helper/scanner/cleanup podem permanecer aninhados emVideoSource;
spies e JPEGs pequenos independentes ficam emVideoSourceTest. ImageReader é
subclassificável para testes; não requer Mockito/dependências ou registro
global alterado. M08/M09/M10/M13 devem cobrir as precisões A05/A06, sem retirar
os restantes casos M01–M14 nem asserções/prazos históricos.

Sem necessidade identificada de modificar ConfigLoader, runtime/configuração,
protocolo, StreamingServer ou outro caminho. Nenhuma API do emissor, pacing,
loop, despacho no owner ou transmissão é integrada neste bolt.

Esta revisão está pronta para validação humana, não marcada concluída.
Até aprovação: apenas Model/Design emstages_completed,current_stage=adr,
história planned,sem Implement/testes/Maven/commits/push ou outro IMP.

Os [seis problemas V-ADD](../../../experiments/construction/025-video/design-01/v-add-pendencias.md)
permanecem por resolver antes do fecho,preservando a remoção da demo;V-ADD
não aprovada. Mantêm-se regressãoJDK21,manual humano aplicável,Denji e
integração;sem gates externos adicionais. TASK-020 só fecha apósA/B/C.
LOCAL-03/04/G-MEDIA-DISTRIBUTION e R17 não são resolvidos por esta conferência.

Aprovação posterior: revisão incluindoA05/A06 concluída,apenas Implement
autorizado. Obrigações de testes/regressão e seis pendênciasV-ADD preservadas.
