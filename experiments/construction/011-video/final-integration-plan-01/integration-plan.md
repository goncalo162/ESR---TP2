# Plano final — requer autorização do utilizador

1. Revalidar main/HEAD e3d122a77257bf3c5c7489de06a34bad7af31fba,index inicialmente
   vazio e estado real. Comparar todos os hashes da allowlist com o inventário final,
   incluindo os dois manifestos revistos e os quatro vídeos/cache LFS. Se refs,
   caminhos ou conteúdo divergirem,parar e identificar;sem checkout/reset/merge.
2. Após autorização,staging explícito apenas dos598 caminhos de allowlist.txt.
   Usar lista lida e argumentos literais Git por subprocess,sem glob nem git add .:
   ['git','--literal-pathspecs','add','--',*paths]. Não incluir target/,demo/,
   .git/config,hooks,cache LFS,downloads temporários ou trabalho alheio.
3. Conferir git diff --cached --name-only -z e comparar conjunto EXATO com allowlist;
   conferir staged diff --stat/--numstat e git diff --cached --check. Para cada blob
   ordinário,ler git show :path em bytes e comparar SHA-256/tamanho ao inventário.
   Para cada media,o índice tem de conter exclusivamente o ponteiro canónico LFS:
   version https://git-lfs.github.com/spec/v1,oid sha256 e size aprovados. Conferir
   hash do ponteiro e blob oid Git contra git_content/media-representation;conferir
   separadamente SHA/tamanho do vídeo real e objeto local. Quatro ponteiros somam534
   bytes,representam459820128 bytes reais. Não confundir estas duas camadas.
4. Se tudo coincidir,commit local proposto em main:
   feat(IMP-017/TASK-020-A): integrar quatro fontes MJPEG com Git LFS
   Sem bypass de hooks por inferência,nem upload ou push. Se um hook inesperado
   exigir ação fora de âmbito,parar. Não executar migrate,rebase ou alterar histórico.
5. Verificar commit efetivo:parent/base,ref main,lista exata de paths e hashes dos
   blobs de commit,incluindo quatro ponteiros. Extrair uma cópia temporária desse
   commit com git archive e extração segura. O arquivo cru conterá ponteiros.
6. Materializar os quatro media EXCLUSIVAMENTE a partir do cache LFS local existente:
   para cada ponteiro validado,usar .git/lfs/objects/<oid2>/<oid2>/<oid>,validar SHA e
   size ANTES de copiar os bytes completos para a path extraída. Sem fetch/pull,
   sem acesso remoto;objeto ausente ou divergente implica parar,nunca descarregar.
   Executar verify-media.py --root <copia> para verificar vídeos reais e manifesto.
   Conferir novamente todos os hashes ordinários extraídos e os hashes reais de media.
   A cópia integral materializada é a prova;ponteiros sozinhos não bastam.
7. Na cópia extraída/materializada,executar python3 -B scripts/validate-planning.py
   --report <log novo fora da cópia congelada>. Exigir0 erros,guardar saída e resultado
   real,refs,hashes,lista de paths e comprovativo de materialização. Esta prova só pode
   existir APÓS o commit;será não cometida até autorização documental posterior.
8. Apresentar hash e prova de integração antes de qualquer DONE/fecho. Não repetir
   build/manual se media/código permanecerem iguais. Não fechar TASK-020 inteira.
9. Publicação não faz parte desta autorização futura de integração local. Antes de
   qualquer upload/push,confirmar com proprietário quota restante e orçamento e os
   gates de distribuição. LOCAL-04/G-MEDIA-DISTRIBUTION continuam abertos. Hooks
   pre-push podem enviar objetos;nenhum comando de push está autorizado.
10. Futuro Codigo.zip deve materializar vídeos reais e validar os hashes aprovados,
    usando o empacotador preparado após autorização própria;git archive cru não serve.
    Intervenção Homebrew permanece separada,não executada,sem gate adicional do bolt.
