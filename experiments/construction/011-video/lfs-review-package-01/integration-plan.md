# Plano atualizado — LFS configurado,sem staging/commits

1. Finalizar revisão humana do pacote corrente,com os quatro hashes de media e
   manifesto preservados,atributos LFS e distinção entre ponteiros/bytes reais.
   Não apresentar aprovação anterior de Test como revisão final atual.
2. Após revisão confirmada,registar revisor/âmbito/hashes e obter autorização
   específica de staging/commit local e ramo. Neste momento main permanece e3d122a,
   sem criar branches/commits. Revalidar refs/estado e preservar trabalho alheio.
3. Staging futuro apenas da allowlist final atualizada,incluindo .gitattributes.
   Para não-media:conferir hash do blob normal. Para os quatro media:conferir texto
   canónico do ponteiro,hash SHA256 do ponteiro/Git blob e oid/size do conteúdo real
   contra o manifesto. Conferir cache e working tree reais pelo SHA aprovado.
   Não usar o hash do vídeo para comparar diretamente ao texto staged do ponteiro.
4. Commit local somente quando autorizado,depois de conferir staged diff/caminhos.
   Verificar commit extraído:git archive contém ponteiros,que têm de ser conferidos
   e materializados com objetos locais sem rede antes de verificar ficheiros reais
   e V-ADD. Após ponteiros existirem em HEAD,git lfs fsck --objects HEAD pode ser
   usado adicionalmente;agora o index/HEAD não contêm ainda os quatro media.
5. A integração local tem de ser comprovada no ref autorizado,sem force,migrate ou
   reescrita de histórico. Código/dados sem alteração não exigem reexecutar media/
   manual por inferência. Se algum hash real mudar,parar e avaliar nova validação.
6. **Antes de qualquer upload/push/publicação**,pedir ao proprietário confirmação:
   «Qual o saldo LFS disponível para0,428GiB de storage e as descargas previstas,
   e qual o orçamento máximo autorizado para excedentes? Confirma a publicação
   desta versão e os respetivos direitos/condições?»
   Sem confirmação,não publicar. Não assumir custo zero ou ajustar billing/budget
   pelo agente. O hook pre-push pode enviar os objetos antes do Git;autorizar só
   commits locais não autoriza uploads. Sem git lfs push --all por inferência.
7. G-MEDIA-DISTRIBUTION/LOCAL-04 ficam abertos para TASK-030;aceitação técnica de
   B ilegível não aprova redistribuição. Nenhuma publicação é autorizada por este
   plano. LOCAL-03:cópia/hash runtime posterior,não instalada.
8. Futuro Codigo.zip:usar build-codigo.py com SHA completo revisto e checkout
   materializado. Script exige quatro ponteiros oid/size aprovados,exporta código
   e substitui ponteiros por vídeos completos,verificando os cinco SHA-256 dentro
   do ZIP. Teste positivo atual foi só um ZIP temporário de dados,não entrega final.
   Codigo.zip final depende de execução/autorização posterior,sem nomes PL/grupo inventados.
9. Bolt/história/pai continuam abertos até revisão final e integração. Fecho
   documental apenas com autorização posterior;parte A não fecha TASK-020 inteira.

Efeitos Homebrew da primeira tentativa registados separadamente. Não executar
uninstall/relink/limpeza por inferência;manter prova e pedir decisão específica
se for necessário intervir nesses pacotes do sistema.
