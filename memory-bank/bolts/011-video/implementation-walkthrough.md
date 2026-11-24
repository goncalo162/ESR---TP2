---
unit: "005-video"
bolt: "011-video"
stage: "implement"
status: "complete"
---
# IMP-017 — Implement: quatro conteúdos e manifesto real

## Resumo e estrutura

Plan aprovado,LOCAL-03 opção1 e parâmetros autorizados. Quatro obras preparadas,
duas por origem,nos cinco outputs previstos. Implement aprovado pelo utilizador;Test autorizado posteriormente.

- [x] media/a.Mjpeg:movie.Mjpeg docente,4269893 bytes/500 frames,hash histórico preservado;sem reencodar.
- [x] media/b.Mjpeg:Big Buck Bunny,111255866 bytes/5965 frames,filme completo/resampling10fps.
- [x] media/c.Mjpeg:Sintel,167319819 bytes/8880 frames,filme completo/resampling10fps.
- [x] media/d.Mjpeg:Tears of Steel,176974550 bytes/7342 frames,filme completo/resampling10fps.
- [x] experiments/media/manifest.json:proveniência,condições,parâmetros,hashes,associações e auditorias reais.

Os caminhos media/ acima são relativos a Projeto/src/main/resources/.
Não houve alteração de código de produção/testes Java,perfis,POM,fixtures ou demo.
Ferramenta FFmpeg isolada em pasta temporária,não instalação runtime/sistema.
O auditor é independente e temporário;fonte textual preservada como evidência,
sem implementar VideoSource ou criar classes sob Projeto/src.

## Decisões,proveniência e limites

A/ZIP e membro coincidem com hashes históricos; nenhuma divergência aceite.
B/C/D completos e créditos presentes,aspect ratio preservado em640×360,SAR1,
JPEG q5/yuvj420p,FPS10 do filtro/configuração. Sem início/fim aparado;resampling
não preserva cada frame original. Sem áudio neste perfil de vídeo.
Framing cinco dígitos ASCII+JPEG,sem separadores. Nenhum JPEG acima de99999;
cap UDP2MiB não altera este perfil. Não houve truncagem ou ajuste de qualidade.
Fontes originais/ferramenta mantidas no cache temporário e hashes de proveniência
registados. Fontes/URLs e comandos exatos no manifesto/evidência.

G-MEDIA:evidência técnica cumprida para revisão de Implement. Não é aprovação
Test/manual/revisão final humana. G-MEDIA-DISTRIBUTION aberto:A sem licença
audiovisual específica conhecida;B/C/D páginas oficiais declaram CC BY3.0,
créditos/transformações registados,versão final ainda sujeita a revisão na TASK-030.
Texto dos créditos é pequeno na edição320×180 original de B;upscale não cria
detalhe perdido. Não se afirma aprovação humana de legibilidade ou redistribuição.

LOCAL-03 opção1 mantida:outputs em resources/media;perfis continuam a resolver
Projeto/media. Cópia futura deve comparar hashes. Não instalada a pasta runtime
nem anunciados os perfis atuais como disponíveis. Parte A não fecha TASK-020.

## Resultados reais e evidência

22687/22687 JPEGs descodificados com ImageIO/JDK21 headless,sem avisos. Inventário
por registo com offset/prefixo/tamanho/hash JPEG/hash pixels/dimensões/decoder.
16/16 casos independentes de fronteira/erro do auditor passaram,incluindo JPEG
válido de99999 bytes e EOF limpo/truncaturas;recursos reais não foram alterados.
Hashes de ficheiros e sequências de pixels distintos em seis pares. Amostras
visuais de conteúdo e créditos inspecionadas pelo agente;não substitui manual/revisão humana.
[Manifesto](../../../experiments/media/manifest.json),
[evidência/hashes/comandos](../../../experiments/construction/011-video/implement-01/README.md).

Falha inicial de obtenção da ferramenta por urllib/certificado registada;
recuperação via curl sem desativar TLS,com SHA do wheel verificado no PyPI.
Todos os downloads de media terminaram com exit0;sem executar programas do ZIP docente.
V-ADD documental executado;sem Maven ou regressão Java nova por não haver mudança
Java.1537 testes anteriores são históricos,não resultado deste incremento de dados.
Sem desvio funcional do Plan ou dependências runtime novas.

## Pendências

Implement aprovado;Test com auditoria técnica consolidada no test-walkthrough.
Manual humano Francisco13/7 etapas cumprido e conferido,com créditos B ilegíveis
e C/D legíveis nas amostras. Test/G-MEDIA técnicos aprovados com limitação B aceite;LOCAL-04/distribuição abertos.
Revisão final e integração pendentes. Unidade/fase/pai em progresso. IDS locais,demo e incrementos
fechados preservados. Sem staging,commits,push ou fecho.
