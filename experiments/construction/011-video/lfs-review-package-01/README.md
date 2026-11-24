# IMP-017 / 011-video — Pacote atualizado com Git LFS,para revisão

Estratégia LFS/exceção .gitattributes aprovadas e aplicadas,sem staging/commits ou
integração. Revisão final ainda pendente,não presumida. Test/G-MEDIA técnicos já
aprovados,media/manifesto byte-idênticos;manual não repetido. Parte A não fecha TASK-020.

[Resumo da execução](execution-summary.json),
[instalação/configuração/validação e efeitos adicionais](../lfs-implementation-01/README.md),
[representação dos quatro media](media-representation.json),
[preservação](preservation.json),[estado de refs](refs.json).

## Duas camadas de hashes,não confundir

Working tree:459820128 bytes reais nas quatro paths de media,com os SHA-256 aprovados.
Index Git continua inalterado:nenhum media staged. Quando for autorizado o staging,
cada path deverá conter um ponteiro LFS canónico,cujo oid sha256 e size correspondem
ao vídeo aprovado;hash do texto do ponteiro não é hash do vídeo.
O manifesto fornece hashes/tamanhos reais e hashes/tamanhos esperados dos blobs Git.
Os quatro objetos reais já existem em .git/lfs/objects e passaram clean→cache→smudge,
com materialização e verificação dos hashes sem servidor LFS. Cache/config/hooks
locais não são caminhos de commit nem dados incluídos na allowlist.

[Allowlist exata](allowlist.txt),[grupos e tamanhos](groups.json),
[manifesto de duas camadas](manifest.json),[diff documental/atributos](content.diff),
[numstat](content.numstat). Binários continuam intactos;ponteiros de prova/oid/size
estão no inventário e resultados. .gitattributes é a exceção de âmbito explícita.
O manifesto exclui apenas o seu próprio hash,fornecido separadamente no checkpoint.
Snapshots antigos mantêm o estado da respetiva execução;este pacote substitui-os
como candidato corrente,sem alterar os originais. Nova allowlist substitui a de523
para eventual staging futuro,que requer autorização própria.

## Instalação,efeitos e preservação

Git LFS3.8.1 instalado pelo binário oficial Intel/checksum,configurado localmente
--skip-smudge,sem --force.13 hooks anteriores preservados,4 hooks LFS novos.
Configuração Git global preexistente não mudou. A tentativa Homebrew inicial
teve efeitos reais:openssl@4 e libssh2 instalados,links de openssl@3 desligados,
cmake interrompido;inventário/logs explícitos,sem reversão automática de pacotes.
Esta ocorrência não fica ocultada pelo sucesso da instalação alternativa e precisa
de decisão separada se for desejada limpeza/religação. Não é alteração dos dados
ou do POM da aplicação.

## Codigo.zip,publicação e pendências

ZIP temporário de validação conteve quatro vídeos completos+manifesto e passou
verificação dos hashes;foi removido. Não foi criado Codigo.zip completo nem entregue.
[Empacotador/verificador preparado](../lfs-proposal-01/README.md) exige o commit
revisto com os quatro ponteiros e copia os bytes reais para o ZIP,validando-os
antes de aceitar o arquivo final. git archive cru/ZIP GitHub não são prova de vídeos reais.

[Plano atualizado](integration-plan.md). Nenhum objeto enviado ao servidor LFS,
nenhum push ou alteração de billing/settings. **Antes de qualquer publicação**,
perguntar ao proprietário saldo de quota e orçamento permitido e obter confirmação;
não assumir custo zero nem permitir que o hook pre-push seja tratado como operação
sem upload. Confirmação ainda pendente,não enviada a terceiros pelo agente.
LOCAL-04:B com créditos presentes mas ilegíveis,sem atribuição adequada/legibilidade/
redistribuição aprovadas. G-MEDIA-DISTRIBUTION continua aberto para TASK-030.
LOCAL-03 não instalou Projeto/media. Revisão final,integração e fecho pendentes.
Sem alterações Java/perfis/POM/fixtures/demo,staging,commits,uploads,push ou histórico.
[V-ADD](v-add.md).
