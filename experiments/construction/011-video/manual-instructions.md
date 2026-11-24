# IMP-017 / 011-video — Manual humano dos media finais

Estado: preparado,não executado. Um terminal e um navegador local,sete etapas
interativas. Não inicia a aplicação OTT,nós,servidor HTTP,CORE ou sockets.
Não usa JAR/JVM:é inspeção de dados com Python3 e imagens no navegador.

## Construção fixada

Quatro ficheiros finais em Projeto/src/main/resources/media/ e manifesto aprovado
em experiments/media/manifest.json.459820128 bytes de media,22687 JPEGs.
Hashes completos e inventários finais fixados em test-manual-tools/approved-media.json.
O runner verifica os hashes antes de abrir o guião e usa os JPEGs originais,
sem editar pixels,recortar ou acrescentar upscale. A preparação já aplicada aos
filmes está registada no manifesto. B provém de edição320×180,com texto pequeno;
a imagem640×360 não recupera detalhe original. Não presumir legibilidade.

## Comando completo — exemplo para Francisco

```sh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2

python3 -B experiments/construction/011-video/test-manual-tools/run-manual.py \
  --operator Francisco \
  --kind human
manual_exit=$?
printf 'SHELL_EXIT=%s\n' "$manual_exit"
```

O modo human exige stdin de terminal; não confirmar por pipe. O runner grava
cada tentativa em test-manual-runs/<RUN-ID-novo>/,sem substituir anteriores.
Depois dos checks de hash,mostra um comando `open` com o caminho de index.html.
Executar esse comando num segundo separador/terminal,ou abrir o ficheiro no
navegador. A página é local,sem rede,JavaScript ou ficheiros externos.
Voltar ao terminal original para responder às perguntas;manter o navegador aberto
para inspecionar as secções. Pode clicar em cada imagem para a ver à escala nativa.

## Sete etapas pessoais

1. Inspecionar as amostras A/B; confirmar se são identificáveis e distintas e
   descrever o conteúdo observado. A autoria audiovisual de A não deve ser inferida.
2. Inspecionar C/D; confirmar distinção e descrever ambos.
3. Comparar os quatro:confirmar quatro conteúdos diferentes,não apenas quatro
   aliases ou reencodificações da mesma obra. Associação A/B→S1,C/D→S2.
4. Inspecionar créditos B. Confirmar se vê credit scroll e classificar a legibilidade
   observada como legivel/parcial/ilegivel;registar notas obrigatórias.
5. Repetir para créditos C.
6. Repetir para créditos D.
7. Reconhecer explicitamente que a licença audiovisual de A está por confirmar,
   G-MEDIA-DISTRIBUTION continua aberto e LOCAL-03 não instalou Projeto/media.

As amostras de conteúdo cobrem início,10%,25%,50%,75%,90% e fim;os créditos mostram
os últimos150s calculados,em passos de5s. Inspeção por amostras não prova que todo
o texto ou todo o filme foi visualmente revisto. Se não conseguir confirmar
conteúdo/créditos,escolher nao,guardar o negativo e comunicar a divergência.
Não inventar legibilidade para obter PASS. As classificações parcial/ilegivel
ficam registadas mesmo que a inspeção tenha sido concluída;serão avaliadas pelo
utilizador/revisor. Nenhum resultado automático aprova redistribuição ou Test.

## Resultado e evidência

Se todas as etapas forem concluídas sem falha técnica e conteúdo/créditos forem
confirmados como observados,o runner mostra13 CHECK_PASS,7 OPERATOR_CONFIRM_STAGE,
MANUAL_PASS CHECKS=13,PASSED=true,WRAPPER_EXIT=0 e REPORT=<caminho>/report.json.
A legibilidade é campo separado:MANUAL_PASS significa guião de inspeção cumprido,
**não** legibilidade total de todos os créditos ou aprovação de redistribuição.
Conferir ainda SHELL_EXIT real no shell e comunicar esse valor separadamente.
Não existe JAVA_EXIT ou prova de terminação de JVM,pois este manual não lança Java.
NO_APP_PROCESSES_STARTED=true refere-se à aplicação OTT;fechar pessoalmente a
janela/separador do navegador no fim,não afirmar encerramento de browser pelo runner.

report.json guarda respostas/notas,classificações,hashes e caminhos das amostras;
manual.log guarda o diálogo integral;samples.json liga cada JPEG ao índice/offset
validado;index.html/samples preservam o material efetivamente observado.
Se houver Ctrl-C/erro,o relatório parcial não equivale a manual aprovado.
Enviar resultado,caminho de report.json,SHELL_EXIT e observações de legibilidade.
Test/revisão final/integração aguardam decisão humana;G-MEDIA-DISTRIBUTION permanece aberto.
