# Revisão de contratos e recursos — TASK-002 / V-002

> Atualização posterior: esta evidência descreve a execução original da TASK-002. A correção explícita do bootstrap (ADR-013) alterou intencionalmente a fixture REGISTER_OK e reclassificou a antiga renovação como caso inválido, mantendo os bytes desta última. Usar o [contrato e evidência atuais](bootstrap-discovery-correction.md); não reintroduzir leases de registo ou renovação com base no histórico.

Data: 26-10-2026. Estado: **V-002 executada; TASK-002 concluída**. A revisão fixa dados de referência e resolve ambiguidades dos formatos, sem implementar codecs, leitura de vídeo, rede ou reprodução.

## Partida e autorização

TASK-001 estava DONE, com [baseline](build-baseline.md), POM configurado e artefacto presente. O repositório continua na revisão 04d02fef9f4646266de0fca428ca4d680597ba36, com alterações locais da TASK-001 por preservar; não foram revertidas ou misturadas com fontes novas. Não há instruções AGENTS.md locais adicionais encontradas. Foi recolhido um snapshot de 62 ficheiros existentes em Projeto/src, CORE, POM, .gitignore e baseline para conferir integridade no fecho.

O utilizador respondeu **«Sem indicações adicionais conhecidas»** à pergunta sobre áudio/texto, RTP/RTSP ou outros protocolos impostos. Isto sustenta manter H-01/H-10 explícitas, sem inventar resposta do docente ou enviar mensagens externas.

## Revisão contra o enunciado

Foi novamente extraído o texto integral do [PDF original](../ESR-TP2-26-27.pdf) e revistos os pontos mínimos, etapas sugeridas, avaliação e entrega.

| Fonte | Cobertura no plano | Conclusão |
|---|---|---|
| p. 1, objetivos e underlay | RF-015, RNF-001/002/004, RC-001 | Vídeo com objetivo de tempo real, eficiência e CORE principal; sem SLA numérico imposto |
| p. 2, mínimo 1 | RF-001 | Processo manual numa máquina IP |
| p. 2, mínimo 2 | RF-002 | Nós entram/saem dinamicamente |
| p. 2, mínimo 3 | RF-003 | TCP/UDP com X vizinhos; X=3 é exemplo, não constante obrigatória |
| p. 2, mínimo 4 | RF-004 | Contacto conhecido fornece vizinhos |
| p. 2, mínimo 5 | RF-005 | Tabela por fluxo com custo/origem/destinos/estado |
| p. 2, mínimo 6 | RF-006/007 | Alternativas e clientes simultâneos |
| p. 2, mínimo 7 | RF-008/009 | Monitorização e atualização perante nós e condições |
| p. 3, mínimo 8 | RF-010 | Reenvio conforme tabela |
| p. 3, mínimo 9 | RF-012/013 | Dois servidores, cada um com pelo menos dois vídeos distintos |
| p. 3, mínimo 10 | RF-014 | Métricas de rede para construir rotas |
| p. 2, exemplo O1–O2 | RF-011 | Evitar cópias redundantes no tronco partilhado |
| p. 3–4, estratégias | RP-001–004/006/008 e ADR | Estratégias escolhidas, sem transformar Java, MJPEG, UDP ou RTP em obrigação docente |
| p. 4, pesos e subitens | RNF-005, RC-007 | Especificação, qualidade, testes/crítica, duas avaliações e defesa |
| p. 5, cenários | RP-005 e RC-001 | Cenários 4/6/complexo adotados; variant(es) explicitadas |
| p. 5, relatório/entrega | RC-002–006, RP-007 | Artigo até 12 páginas, LNCS recomendado, PDF e ZIP/RAR com nomes PLx/Gy e upload do grupo |

A menção simplificada a uma fonte/fluxo não elimina o mínimo explícito multi-servidor/multi-stream. Nenhuma obrigação foi retirada ou marcada cumprida por este trabalho.

## Decisões de contrato fixadas

1. Corpo TCP mínimo de 37 bytes, máximo 65536; o prefixo uint32 big-endian de quatro bytes fica fora desse comprimento. Ficheiro mínimo completo: 41 bytes.
2. Tipos 1–17, ordem de campos, UUID binário, comprimentos de strings em bytes e UTF-8 estrito confirmados. senderId/serverId/nodeId usam [A-Za-z0-9_-]{1,32}; o título admite UTF-8 e tem limites de bytes e caracteres separados.
3. Endpoint.instanceId zero só representa um vizinho ainda desconhecido numa resposta bootstrap. HELLO e identidade do envelope são validados em contexto na TASK-007; fixtures de formato não simulam autorização.
4. ERROR.relatedType é um octeto bruto que pode indicar tipo desconhecido; o enum do envelope continua limitado a 1–17. ERROR.code=1–12 e NODE_LEAVE.reason=0/1.
5. Cabeçalho UDP de 64 bytes, máximo de aplicação 1200 e payload máximo 1136. Campos/offsets e a fronteira de frame 1137 conferidos. São pacotes próprios, sem alegação de RTP.
6. Corrigido invariante do modelo: serverEpoch é da instância da origem e pode ser partilhado por A/B da mesma origem; rotas, subscrições, sequências e caches são separados por StreamKey. Epoch isoladamente não é chave de deduplicação.
7. Ficheiro docente confirmado: cinco bytes decimais ASCII + L bytes JPEG, repetidos até EOF entre registos. O máximo desse perfil é 99999, distinto do máximo de 2 MiB do protocolo UDP. FPS é configuração externa, não metadado do ficheiro.

Estas clarificações constam de [06](../docs/06-DATA-MODEL.md), [07](../docs/07-API-AND-INTERFACES.md) e ADR-009 em [12](../docs/12-RISKS-AND-DECISIONS.md). Não modificam as obrigações do PDF.

## Fixtures e revisão independente

Artefactos em [Projeto/src/test/resources/protocol](../Projeto/src/test/resources/protocol/README.md): 45 pares .bin/.hex e manifest.json com resultados, campos interpretados, tamanhos e hashes. São 29 casos TCP (18 aceites/11 rejeitados) e 16 UDP (quatro aceites/12 rejeitados). Os 17 tipos TCP estão representados, com uma variante adicional de título UTF-8 suplementar (🎬, U+1F3AC): quatro bytes em UTF-8 comum versus seis em modified UTF-8, permitindo detetar uso indevido de writeUTF.

Os bytes vieram de literais hexadecimais e comprimentos calculados manualmente. Uma segunda inspeção temporária usou leitura direta de offsets, aritmética, UUID e struct do Python, sem usar um codec do projeto. Conferiu equivalência .bin/.hex, comprimentos e SHA-256 dos 45 casos; envelope, identidades, amostras de payload, limites, motivos das 23 rejeições, os quatro cabeçalhos UDP e reconstrução dos payloads de um frame sintético de 1137 bytes. Não foi usado o futuro encoder para criar os expected bytes.

Resultados: 22 casos aceites quanto ao formato e 23 rejeitados quanto à condição documentada, sem discrepâncias. Estes são resultados de inspeção das fixtures, não execução de decoder, sockets, JUnit ou streaming. Os payloads UDP sintéticos não são JPEG. O README das fixtures anota offsets de HELLO_OK, HELLO, ANNOUNCE, ACTIVATE, ERROR e UDP.

## Recurso docente realmente obtido

URL indicado na p. 5: [ProgEx.zip](https://www.di.uminho.pt/~flavio/ESR/ProgEx.zip). Download em /tmp/esr-task002-ProgEx.zip; não foi copiado para a distribuição ou para fontes do projeto. HTTP 200, application/zip, 10403290 bytes, Last-Modified de 02-10-2024. SHA-256:

~~~text
fbee4b0a654cbc98b7350e41fbb6bddb1a043d5ac31caea7d2667a47b689be0d
~~~

O ZIP contém 31 ficheiros e não apresentou erros na verificação CRC. Não há entrada README, LICENSE, COPYING ou NOTICE. O PDF atribui os exemplos ao livro e à adaptação docente; isto não identifica por si só a autoria/licença de todas as imagens do vídeo. A [página oficial do livro](https://gaia.cs.umass.edu/kurose_ross/programming.php) relaciona o exercício RTSP/RTP e os seus autores; não implica que o nosso pacote seja RTP ou uma licença de redistribuição do recurso audiovisual.

Foram inspecionados como texto VideoStream.java, VideoStream.py e o período do exemplo Servidor.java. Nenhum programa, .pyc ou script descarregado foi executado ou copiado para Projeto/src. A leitura do recurso foi análise pontual em /tmp, não implementação do componente VideoSource.

### Identidade e estrutura de movie.Mjpeg

As três entradas são byte a byte idênticas: ProgEx/Java/movie.Mjpeg, ProgEx/Python/movie.Mjpeg e ProgEx/Python/originais/movie.Mjpeg. Cada uma tem 4269893 bytes e SHA-256:

~~~text
ca461cb1edce18efb355794ffb7d52f73dc7ce4c03422224894eb0fc550b0cca
~~~

Uma varredura por comprimentos encontrou 500 registos, consumindo o ficheiro exatamente até EOF. Todos os prefixos têm cinco dígitos decimais; todos os payloads têm comprimento completo e delimitadores JPEG SOI/EOI. Tamanho mínimo=4970 bytes, máximo=13967; os 500 hashes de frames são distintos. Não se afirma descodificação de todos os JPEG: a validação de ImageIO e casos de ficheiro corrompido pertence à TASK-020.

| Registo | Offset do prefixo | Prefixo ASCII | Comprimento JPEG | SHA-256 do JPEG |
|---:|---:|---|---:|---|
| 0 | 0 | 06014 | 6014 | 3677f32f0b028fa579e4f291350af3f2413aab0f9dc7c888701f188394239c57 |
| 1 | 6019 | 05940 | 5940 | 213b0b8591ba54482796e35c48cc134e00001799a2409354c50a9b546023b5e6 |

O primeiro JPEG foi extraído em /tmp e inspecionado visualmente como imagem de 384×288. Não houve reprodução de vídeo. No exemplo Java o período é 100 ms, equivalente a 10 FPS, sem que isso se torne requisito obrigatório. Os comentários do exemplo sobre bits não alteram o que os bytes mostram: o prefixo ocupa cinco **bytes**, não cinco bits.

## Quatro conteúdos identificados

Os nomes a/b/c/d e UUID de [07](../docs/07-API-AND-INTERFACES.md) continuam a definir os perfis propostos. Só A tem ficheiro MJPEG inspecionado nesta tarefa. B/C/D foram identificados em publicadores oficiais, com índices e HEAD HTTP 200; não foram descarregados, convertidos, reproduzidos nem lhes foi atribuído hash fictício.

| Origem/alias | Conteúdo identificado e autoria/proveniência | Evidência de acesso | Estado para TASK-020 |
|---|---|---|---|
| S1/A | movie.Mjpeg do ZIP docente; autoria audiovisual/licença específica não indicada no ZIP | GET 200, bytes/hash e 500 frames verificados | Disponível para referência do exercício; confirmar redistribuição antes de incluir o vídeo na entrega |
| S1/B | Big Buck Bunny, Blender Foundation / Peach | [Arquivo MP4](https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4.zip): HEAD 200, application/zip, 64657225 bytes | Descarregar, extrair, converter para o perfil de cinco dígitos, calcular hash e validar conteúdo/FPS |
| S2/C | Sintel, Blender Foundation / Durian | [Arquivo MKV](https://download.blender.org/durian/movies/Sintel.2010.720p.mkv.zip): HEAD 200, application/zip, 681285470 bytes | Mesma preparação, sem confundir MKV com MJPEG docente |
| S2/D | Tears of Steel, Blender Institute / Mango | [MOV](https://download.blender.org/demo/movies/ToS/tears_of_steel_720p.mov): HEAD 200, video/quicktime, 372178639 bytes | Mesma preparação; fonte do conteúdo é distinta de C |

As páginas oficiais declaram CC BY 3.0 para o material relevante dos filmes: [Big Buck Bunny](https://peach.blender.org/about/), [Sintel](https://durian.blender.org/sharing/) e [Tears of Steel](https://mango.blender.org/about/). Registar os créditos e condições da versão efetivamente usada quando preparar os ficheiros; os websites distinguem alguns elementos, como logótipos e música autónoma. Esta tabela regista as declarações dos publicadores, sem atribuir uma licença por inferência ao ZIP docente.

O mínimo de dois conteúdos distintos por origem é satisfeito pela **seleção planeada** A/B e C/D, não por media já preparado. As cópias iguais do ZIP contam como um só vídeo. Não trocar simplesmente extensão de MP4/MKV/MOV para .Mjpeg nem usar dois aliases do mesmo ficheiro. A preparação/transformação do conteúdo na TASK-020 deve produzir JPEGs identificáveis, respeitar 99999 bytes por frame e registar ferramenta, parâmetros, créditos e hashes; FFmpeg pode ser ferramenta offline, não dependência runtime obrigatória do serviço.

## Comandos e resultados reais

- pdftotext -layout ESR-TP2-26-27.pdf /tmp/esr-task002-enunciado.txt: código 0; revisão das cinco páginas e ligação RF/RNF/RC/RP acima.
- curl --fail --location --connect-timeout 10 --max-time 30 --dump-header /tmp/esr-task002-ProgEx.headers --output /tmp/esr-task002-ProgEx.zip https://www.di.uminho.pt/~flavio/ESR/ProgEx.zip: primeira tentativa no sandbox falhou com código 6 (DNS); repetição autorizada fora do sandbox terminou com código 0 e HTTP 200.
- Inspeção temporária com zipfile/hashlib: CRC sem erro, inventário de ficheiros, leitura dos exemplos como texto e hashes das três cópias de movie.Mjpeg.
- Inspeção temporária por prefixos e offsets do MJPEG: 500 registos completos, sem bytes sobrantes; não houve execução do leitor do exemplo.
- curl de três índices em download.blender.org, com connect-timeout 10/max-time 20, para /tmp: código 0. Os índices atuais referem ficheiros ZIP em alguns casos; não se assumiu o URL antigo de MP4 direto.
- curl --head dos três URLs de conteúdos da tabela, com limites de tempo e saídas /tmp: código 0, HTTP 200 nos três. HEAD confirma metadados/disponibilidade naquele instante, não integridade ou formato interno dos ficheiros que não foram descarregados.
- python3 /tmp/esr-task002-create-fixtures.py: criou os testemunhos literais, sem código da aplicação.
- python3 /tmp/esr-task002-validate-fixtures.py: 45 pares íntegros, cobertura dos 17 tipos e fronteiras conferidas, zero discrepâncias. Os scripts são temporários e não integram o projeto.

Não foram executados mvn test, build, CORE, codec da aplicação ou streaming nesta tarefa, pois houve apenas novos dados de teste e clarificações documentais. V-001 continua com a sua evidência histórica; V-004/V-019 irão validar implementação contra estas fixtures.

## Gates e passagem de contexto

- H-01/H-10: utilizador sem indicações adicionais conhecidas; hipóteses do plano mantidas, sem confirmação formal docente.
- H-02: acesso e formato do recurso docente confirmados nesta execução. O ficheiro em /tmp é transitório; voltar ao URL/hash se já não existir numa sessão futura.
- TASK-020: preparar B/C/D em MJPEG compatível, validar ImageIO e os quatro conteúdos; não confundir identificação/HEAD com ficheiros prontos.
- TASK-030: confirmar créditos e condições de redistribuição, sobretudo do recurso A, antes de empacotar. Se não houver autorização, preparar uma substituição documentada e discutir alternativa quando exigido pelo docente; não inventar permissão.
- RNF-005 permanece IN PROGRESS, com especificação/fixtures parcialmente cobertas; implementação, testes de comportamento e apreciação crítica dependem das outras tarefas.
- Sem bloqueio atual da TASK-002. Os gates futuros não impedem TASK-003, próxima tarefa ainda TODO e não iniciada.

## Verificação de fecho

Foram conferidas 1412 referências relativas sem ligações/âncoras quebradas e a integridade dos 62 ficheiros existentes protegidos no snapshot inicial. Fontes Java, configurações, testes anteriores, POM, .gitignore, CORE e baseline de TASK-001 mantêm os hashes de partida. Os 45 pares binário/hex e respetivo manifesto foram revalidados após a variante Unicode suplementar, sem discrepâncias. git diff --check não encontrou erros de whitespace. TASK-001/002 estão DONE; TASK-003–031 continuam TODO. Apenas RNF-005 e RP-006 estão IN PROGRESS na matriz; nenhum requisito foi marcado DONE por esta revisão.
