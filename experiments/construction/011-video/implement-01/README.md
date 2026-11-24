# IMP-017 / 011-video — Checkpoint Implement por aceitar

Plan/LOCAL-03 opção1/seleção/parâmetros aprovados. Só Implement,sem avanço Test.
[Walkthrough](../../../../memory-bank/bolts/011-video/implementation-walkthrough.md),
[manifesto real dos quatro conteúdos](../../../media/manifest.json).

## Resultados reais

A:500 frames/4269893 bytes,hash histórico ca461cb1edce18efb355794ffb7d52f73dc7ce4c03422224894eb0fc550b0cca.
B:5965 frames/111255866 bytes;C:8880/167319819;D:7342/176974550.
Total22687 JPEGs/459820128 bytes de ficheiros preparados.
JPEG min/max:A4970/13967,B1602/41769,C1602/66091,D1938/56322.
Todos≤99999;perfil cinco dígitos distinto do cap UDP2MiB.

[Auditorias headless completas](full-audit-results.json),inventários A/B/C/D-frames.jsonl,
[negativos/fronteiras16/16](negative-results.json),[distinção/amostras visuais](distinctness.json).
Todos os registos foram varridos e descodificados com ImageIO/JDK21,sem avisos.
Teste de JPEG válido com99999 mantém pixels do frame de referência;não altera assets.
São verificações incrementais de dados em Implement,não aprovação/consolidação Test.
Identificação visual pelo agente não substitui manual/revisão humana.

## Proveniência e reprodução

[Downloads](download-summary.json),[A sem reencodar](A-preservation.json),
[B/C/D parâmetros/comandos](preparation-results.json),[ferramenta](tool.json),
[FFmpeg versão/build](ffmpeg-version.txt). Fontes/cache preservados em entry.json;
originais não são adicionados a Projeto/src ou ao runtime. ZIP docente não executado.
Scripts auxiliares:obtain-sources.py,prepare-media.py,run-audits.py,audit-negatives.py.
Auditor independente:AuditMjpeg.java.txt,compilado apenas no cache temporário;
nenhum ficheiro Java da aplicação/testes alterado. Comandos de JVM em A/B/C/D-audit-command.json.
A fonte do auditor aplica parsing/limites de JPEG e dimensões antes de decode,
sem usar VideoSource que continua placeholder;um frame/imagem de cada vez.
FFmpeg offline foi obtido com aprovação própria,em pasta temporária,sem instalação
sistema/Python/POM. SHA do wheel verificado com PyPI;ver tool.json.
[Falha inicial de certificado urllib](acquisition-initial-error.json) preservada;
recuperação via curl com TLS ativo,sem --insecure. Não repetir o script inicial
sobre ficheiros já obtidos;ele recusa sobrescrita e a falha inicial não é ocultada.
Fontes/ferramentas temporárias podem precisar de reobtenção por URL/hash noutra sessão.

## Gates e limites

G-MEDIA:evidência técnica completa,aguarda aceitação do checkpoint Implement.
G-MEDIA-DISTRIBUTION aberto:licença audiovisual de A não conhecida,nem atribuída
por inferência;B/C/D declarações/condições oficiais e créditos registados.
Filmes B/C/D completos sem trim,créditos presentes;10fps de preparação/configuração,
não FPS de reprodução medido. B tem créditos pequenos na edição original320×180,
limitação preservada e revisão humana final pendente. Não declarar redistribuição aprovada.
LOCAL-03 opção1:cópia/hashes futura para Projeto/media,sem alteração dos perfis
ou afirmação de que já encontram resources/media. Parte A não fecha TASK-020.

[Preservação](preservation.json),[V-ADD](v-add.md),[diff](cumulative.diff),
[numstat](cumulative.numstat),[manifesto do checkpoint](manifest.json).
HEAD e3d122a/main,IDs locais,demo/incrementos fechados preservados. Sem Java/perfis/
POM/fixtures alterados,staging/commits/push ou avanço Test.1537 testes Java anteriores
são históricos,não nova regressão. Manual/revisão humana/Test/integração pendentes.
