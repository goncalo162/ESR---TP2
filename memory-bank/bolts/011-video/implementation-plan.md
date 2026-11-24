---
unit: "005-video"
bolt: "011-video"
stage: "plan"
status: "complete"
---
# IMP-017 — Plan: preparar quatro conteúdos distintos

Plan aprovado pelo utilizador,incluindo seleção/parâmetros e LOCAL-03 opção1.
Implement autorizado para obtenção/preparação dos quatro conteúdos e manifesto real;
Implement aprovado e Test autorizado posteriormente. Sem alterações Java/perfis/POM/fixtures/demo,staging/commits/push.
Base e HEAD e3d122a77257bf3c5c7489de06a34bad7af31fba,ramo main,estado inicial limpo.
IDs apenas IMP-017-LOCAL; nenhum Q-C global reservado. DEMO-001 é apoio separado.

## 1. Fontes,dependências e fronteira

[ADD](../../../add.md),secções2/5/6/8–12/IMP-017; [specs](../../../specs.md),
[intent](../../intents/001-ott-streaming/requirements.md),
[unidade](../../intents/001-ott-streaming/units/005-video/unit-brief.md),
[história](../../intents/001-ott-streaming/units/005-video/stories/002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao.md),
[TASK-020 parte A](../../../docs/tasks/PHASE-05/TASK-020.md),
[contrato7/perfis](../../../docs/07-API-AND-INTERFACES.md),
[testes](../../../docs/09-TESTING-STRATEGY.md),[DoD](../../../docs/14-DEFINITION-OF-DONE.md),
[revisão histórica V-002](../../../experiments/task-002-contract-review.md).

IMP-015/009-arvores e IMP-016/010-video complete; histórias implemented=true,
TASK-018/019 DONE,integrados na base. IMP-016 tem integração72c9f92 e fecho f283a98.
Os1537 testes daquele incremento são históricos,não resultados desta etapa.
Unidade004/PHASE-04 complete; unidade005/PHASE-05 em progresso.

Reutilizar ADR-006/009/021/024; preservar ADR-026,sem nova ADR/dependência runtime.
IMP-017 entrega dados/proveniência,**não** VideoSource,StreamingServer,pacing,
procura,UDP,Swing ou leitor da parte B. TASK-020 composta só fecha após A/B/C,
critérios do pai,revisão e integração. VideoSource.java/StreamingServer.java reais
continuam placeholders de0 bytes; não os preencher neste bolt.

## 2. Inspeção real e G-MEDIA

[Inventário local](../../../experiments/construction/011-video/plan-01/local-availability.json):
media/ e experiments/media/manifest.json ausentes. Busca no repositório,incluindo
ficheiros ignorados,não encontrou vídeos; só três PNG documentais do enunciado,
que não são conteúdos substitutos. /private/tmp/esr-task002* ausente. FFmpeg e
ffprobe não encontrados no PATH; JDK21/java.desktop disponível no ambiente.
Não se presume ferramenta offline instalada por existir o plano.

[Consultas atuais](../../../experiments/construction/011-video/plan-01/remote-availability.json):
HEAD200 para A/B/C/D,zero bytes de corpo de media descarregados. Não confirma
integridade,formato,hash,JPEGs ou licença do objeto remoto. GET200 apenas nas três
páginas textuais oficiais de condições. Falha DNS da sandbox/web reader402
registadas; consulta autorizada fora da sandbox teve êxito.

**G-MEDIA aberto:** quatro ficheiros compatíveis ainda não preparados/validados.
A disponibilidade remota permite planear obtenção futura,mas não fecha o gate.
Não pedir ao utilizador ficheiros que continuam acessíveis remotamente sem primeiro
usar esta evidência. Se uma fonte falhar posteriormente,preservar negativo e
manter o gate aberto; continuar trabalho independente autorizado,sem fingir media.
G-MEDIA-DISTRIBUTION pertence à TASK-030: pendência de redistribuição explicitada,
não confundida com teste de formato ou licença automaticamente atribuída a A.

## 3. Seleção proposta: quatro obras distintas,duas por origem

Usar UUID/associações já presentes em streams-s1.properties/streams-s2.properties;
nenhuma mudança de configuração autorizada aqui. FPS10 abaixo é configuração
existente/proposta de preparação,não FPS medido nem obrigação do PDF.

| Alias/origem/UUID | Conteúdo/proveniência | Saída proposta | FPS de configuração |
|---|---|---|---:|
| A/S1/00000000-0000-4000-8000-000000000001 | movie.Mjpeg,arquivo docente ProgEx,entrada ProgEx/Java/movie.Mjpeg | a.Mjpeg |10|
| B/S1/00000000-0000-4000-8000-000000000002 | Big Buck Bunny,Blender Foundation/Peach | b.Mjpeg |10|
| C/S2/00000000-0000-4000-8000-000000000003 | Sintel,Blender Foundation/Durian | c.Mjpeg |10|
| D/S2/00000000-0000-4000-8000-000000000004 | Tears of Steel,Blender Institute/Mango | d.Mjpeg |10|

Fontes atuais para obtenção futura,sem obtenção de media nesta etapa:

- A:[ProgEx.zip](https://www.di.uminho.pt/~flavio/ESR/ProgEx.zip).
- B:[arquivo MP4](https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4.zip).
- C:[arquivo MKV](https://download.blender.org/durian/movies/Sintel.2010.720p.mkv.zip).
- D:[MOV](https://download.blender.org/demo/movies/ToS/tears_of_steel_720p.mov).

A tem referências **históricas** da V-002: ZIP SHA-256
fbee4b0a654cbc98b7350e41fbb6bddb1a043d5ac31caea7d2667a47b689be0d;
movie.Mjpeg SHA-256 ca461cb1edce18efb355794ffb7d52f73dc7ce4c03422224894eb0fc550b0cca,
500 registos,384×288 na primeira imagem inspecionada. Reobtenção futura tem de
comparar os bytes; hash diferente exige registar versão/impacto e revisão,
sem substituir o expected histórico. Todas as cópias idênticas do ZIP contam só A.
A autoria/licença audiovisual específica de A não foi indicada no ZIP histórico;
não inventar autorização de redistribuição. Obter/confirmar condições ou discutir
substituição explícita antes da entrega; nenhuma substituição aplicada neste Plan.

[B:condições oficiais](https://peach.blender.org/about/),
[C:condições oficiais](https://durian.blender.org/sharing/),
[D:condições oficiais](https://mango.blender.org/about/) declaram CC BY3.0 e exigem
atribuição; há exclusões referidas nos sites,como marcas/logótipos. Preservar
créditos completos das obras e registar atribuição/links/transformações; não
atribuir a mesma licença ao ZIP docente. Para C/D,não remover o credit scroll.
O plano usa filmes completos B/C/D,incluindo créditos,em vez de excertos que
percam essa condição. A suficiência dos créditos da versão efetivamente preparada
será verificada; a preparação não resolve por si só G-MEDIA-DISTRIBUTION.
Páginas/headers datados e hashes da inspeção preservados em plan-01.

## 4. Allowlist de implementação e preparação proposta

Somente após autorização Implement:

1 - Projeto/src/main/resources/media/a.Mjpeg.
2 - Projeto/src/main/resources/media/b.Mjpeg.
3 - Projeto/src/main/resources/media/c.Mjpeg.
4 - Projeto/src/main/resources/media/d.Mjpeg.
5 - experiments/media/manifest.json.

Documentação/evidência do bolt é suporte de Construction. Não criar já media/,
manifesto de recursos fictícios ou conteúdos sintéticos no lugar das quatro obras.
Não alterar perfis,.java,POM,fixtures ou demo/. Ferramentas de auditoria offline
poderão ser temporárias,com comandos/fontes/versões preservados como evidência;
não são leitor de produção nem nova dependência runtime da aplicação.

[PRESSUPOSTO proposto] A:preservar integralmente movie.Mjpeg se o hash histórico
coincidir,sem reencodar. B/C/D:preservar toda a duração/créditos; preparar offline
com FPS10,aspect ratio conservado em canvas640×360,padding e SAR1,sem áudio no
perfil JPEG. Configurar JPEG q5/yuvj420p como ponto de partida,registando versão e
comando exatos. Não se garante à partida que os JPEG cabem em99999. Se excederem,
parar a versão candidata e documentar parâmetros ajustados/revalidar tudo; nunca
truncar JPEG,renomear MP4/MKV/MOV ou ampliar o prefixo silenciosamente.
Formato de saída de ffmpeg MJPEG sem prefixos **não** serve como ficheiro final:
empacotar cada JPEG com cinco dígitos ASCII de L,sem newline nem delimitador extra.
Ferramenta offline/versão e parâmetros finais dependem de disponibilização futura,
sem instalar ou executar conversão nesta etapa. Não há hash ou duração preparados.

## 5. Contrato de manifesto proposto

Manifesto deve separar valores históricos,solicitados e medidos; valores ainda
não obtidos ficam null/com estado pending,nunca hashes ou métricas plausíveis.
Campos por entrada:

- alias,streamId,serverId,title;destinationPath e runtimePathStatus (LOCAL-03).
- source URL,publicador/autoria,container/entrada ZIP,versão,data real de obtenção,
  SHA-256 do download e da fonte extraída;expectedHistoricalHash separado.
- licença declarada/URL,página consultada/data/hash,atribuição,créditos preservados,
  transformações,restrições/incertezas e redistributionStatus/G-MEDIA-DISTRIBUTION.
- ferramenta/versão,comando,segmento completo,scale/pad/SAR/pixel format/qualidade,
  fps solicitado e de configuração;fps original apenas se inspecionado.
- saída SHA-256,tamanho,registos/dimensões,min/max JPEG;FPS de reprodução medido
  fica ausente/pending,pois ficheiro sem timestamps não o prova.
- inventário por registo:index,offset do prefixo,prefixo,L,hash JPEG,dimensões,
  resultado de decode e digest de pixels se usado;resumos de duplicação/comparação.
- audit command,JDK21/headless,exit/logs/limites,estado V-020-A e G-MEDIA;
  timestamp só da execução real. Ficheiros/relatórios referidos devem existir.

Não alterar streams-s1/s2 para afirmar associação entregue: manifest liga A/B a
S1 e C/D a S2; execução real dessas origens pertence à integração posterior.

## 6. V-020-A — oráculos planeados,não executados

1 - Inventário:quatro caminhos/UUID/títulos e associação2+2;ficheiros legíveis e
    não vazios;origem/hash/condições/preparação/FPS registados sem placeholders de sucesso.
2 - Varredura completa de **todos** os registos:EOF limpo só antes do próximo
    prefixo;ler cinco bytes exatos,ASCII0..9;validar1≤L≤99999 antes de reservar
    payload;ler exatamente L;próximo prefixo logo a seguir. Proibir bytes sobrantes.
3 - Auditor independente do futuro VideoSource:offsets/aritmética/hash prefixos
    e SOI/EOI;ImageIO/JDK21 com java.awt.headless=true em cada JPEG,sem GUI.
    Ler dimensões via ImageReader antes de alocar imagem e comparar aos limites
    declarados por ativo (A:reconfirmar384×288;B/C/D:canvas proposto640×360).
    Descodificar um frame de cada vez,imagem não nula/dimensões consistentes,
    dispose/close dos readers/streams;sem carregar o filme inteiro em memória.
4 - Fronteiras/negativos em buffers temporários:prefixo de1..4 bytes,truncatura
    de payload,não decimal,sinal/espaço/newline,L00000,99999 permitido **quanto
    ao framing** apenas se JPEG válido,100000 não representável no perfil,
    JPEG inválido/EOI ausente/lixo final,EOF entre registos versus truncatura.
    Limite de formato não promete que qualquer JPEG sintético de99999 seja válido.
    Não modificar recursos reais para fabricar negativos;guardar evidência temporária.
5 - Limites físicos:payload de auditor≤99999,um registo/imagem de cada vez;
    dimensões validadas antes do decode. Os testes UDP de2MiB/1847 pertencem ao
    codec fechado;não alargar o perfil cinco-dígitos para caber nesses testes.
6 - Distinção:comparar hashes dos quatro ficheiros/fontes/sequências de JPEG;
    cópias byte-idênticas falham. Hash diferente não prova conteúdo distinto:
    combinar proveniência das quatro obras e inspeção de frames representativos
    (início/meio/fim/ação/créditos),pixels/sequências para detetar mera reencodificação
    ou corte da mesma obra. Não aceitar quatro aliases/UUID do mesmo vídeo.
    Repetição de frames dentro de uma obra não é por si um erro (cenas estáticas).
7 - Conferir créditos/identificação visual em revisão humana,sem declarar reprodução
    Swing/CORE;headless comprova decode, não apresentação ou metas de FPS.
8 - Recalcular manifesto/hashes e ligar logs/inventário completo. Só fechar G-MEDIA
    após quatro conteúdos preparados,distintos e integralmente validados. Test/revisão
    Denji/integração/DoD necessários ao fecho;parte A não fecha TASK-020 nem a fase.

Sem executar Java,JUnit,Maven,ffmpeg,decode de media,V-020-A ou manual neste Plan.
A única validação executável prevista aqui é V-ADD/documentos/preservação.

## 7. Dúvidas locais e decisões pendentes

- IMP-017-LOCAL-01 — disponibilidade:ausência local confirmada,HEAD200 dos quatro
  endpoints não é media pronta. G-MEDIA aberto;obtenção/preparação só após aprovação
  Implement. Se acesso falhar,reusar cache legitimamente disponível ou pedir fonte
  equivalente/autorização após identificar a falha,nunca substituir silenciosamente.
- IMP-017-LOCAL-02 — condições/versão: A sem licença audiovisual específica conhecida;
  B/C/D declarações oficiais e créditos a preservar. G-MEDIA-DISTRIBUTION continua
  na TASK-030,com condições da versão efetivamente usada por verificar. Não inventar
  permissões,nem impor novo gate global de Plan por isso.
- IMP-017-LOCAL-03 — composição de paths:ConfigurationLoader resolve media.base
  relativo ao ficheiro de perfil. Os dois server-*.properties atuais usam
  ../../../../media,que aponta para Projeto/media;allowlist pede resources/media.
  **Nenhuma configuração/Java alterada.** Opções para decisão:
  1. Recomendada:manter os cinco caminhos de outputs deste IMP;documentar instalação
     por cópia com hash em Projeto/media na composição/validação posterior. Não
     anunciar disponibilidade automática dos perfis atuais neste IMP de preparação.
  2. Autorizar futuramente exceção mínima de dois perfis server-1/2.properties,
     mudando media.base para ../media,e respetiva validação;exige scope explícito,
     fora deste Plan autorizado. Não aplicar por inferência.
- [PRESSUPOSTO menor] FPS10 é reaproveitado da configuração;canvas/qualidade propostos
  são parâmetros offline revistos pela validação,não regras do enunciado ou wire.

## 8. Checkpoint

Apresentar Plan,estado G-MEDIA,dúvidas/diffs/preservação e parar para aprovação.
Plan não marcado complete,stages_completed=[];Implement/Test não autorizados.
Sem media preparado/download/conversão,sem alterações Java/staging/commits/push.
Preservar demo e incrementos fechados,sem reabrir TASK-001–005 ou IMP-016.

## Aprovação e execução posterior

Utilizador aprovou Plan e LOCAL-03 opção1;manter cinco outputs,com cópia/hashes para
Projeto/media só em composição posterior. Configurações não alteradas,nem perfis
anunciados prontos. Implement executado;ver implementation-walkthrough e manifesto.
Este Plan mantém inspeções/propostas originais como histórico do checkpoint.
