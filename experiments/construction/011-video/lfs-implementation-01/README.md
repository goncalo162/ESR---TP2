# IMP-017 — Git LFS instalado/configurado e validação local executada

Estratégia LFS/exceção .gitattributes aprovadas. Git LFS3.8.1 nativo instalado,
configuração apenas local (--local --skip-smudge),sem --force ou alterações Git globais.
[Instalação oficial/checksum](native-install.json),[versão](native-version.txt),
[configuração/hooks](configuration-proof.json),[atributos](attributes-check.txt).
13 ficheiros de hooks anteriores preservados;novos post-checkout/post-commit/
post-merge/pre-push de LFS,sem sobrepor hooks próprios. Filtros globais já existiam
antes da execução e mantêm o mesmo hash. .gitattributes tem exatamente4 regras.

## Tentativa Homebrew e efeitos adicionais reais

[Log integral](brew-install.log),[interrupção](brew-stop.json),
[efeitos/inventário](brew-side-effects.json),[versões observadas](brew-side-effects-inventory.txt).
Homebrew sem bottle para o host planeou13 dependências e2 upgrades. Antes de
interrompido,instalou openssl@4 e libssh2 e desligou6529 links de openssl@3/3.5.9.
Lz4/zstd ficaram nas versões anteriores;build cmake interrompido. Não foi feita
reversão/religação/desinstalação automática,para não alterar pacotes do sistema
por inferência. Esta pendência de instalação está explícita,não é apagada por a
alternativa nativa ter funcionado. O binário final não é uma fórmula Homebrew
concluída. Configuração Git global,Java/perfis/POM/dados permanecem intactos.

## Validação local,sem index/commits/rede LFS

[Resultado](materialization.json),[script executado](validate-local.py),
[working tree materializado](materialized-tree-check.json),
[ZIP temporário com bytes reais](materialized-zip-check.json),[env LFS](git-lfs-env.txt).
Para A/B/C/D:clean direto produziu ponteiro canónico,cache .git/lfs/objects foi
conferido por SHA256 antes de smudge;smudge materializou ficheiro real com o hash
aprovado. Fallback de URL só file:// vazio,autodetect/searchall false,sem HTTP.
Quatro objetos continuam no cache local;saídas temporárias e ZIP só de dados foram
removidos. Não houve staging,commit,reescrita,upload,push ou billing/settings alterados.

A.pointer..D.pointer e logs clean/smudge preservam a prova. O script é evidência
da execução;não sobrescrever estes logs numa repetição,criar nova execução/diretório.
HEAD/index não contêm ainda estes ponteiros;git lfs ls-files/fsck de HEAD não devem
ser usados para fingir prova de referências ainda não cometidas. Aqui a prova é
hash direto dos quatro objetos e materialização real,independente do index.

Futuro Codigo.zip:[empacotador/verificador](../lfs-proposal-01/README.md),que exige
commit revisto com os ponteiros e substitui-os por vídeos completos. Não executado
como entrega;não existe Codigo.zip final. Test/G-MEDIA técnicos anteriores intactos,
LOCAL-04/G-MEDIA-DISTRIBUTION abertos,sem legibilidade/atribuição/redistribuição aprovadas.
LOCAL-03:cópia runtime posterior,não instalada. Revisão final/integração pendentes.
Antes de publicação,o proprietário tem de confirmar saldo/quota e orçamento;
nenhum custo zero presumido,nenhuma mensagem a pessoas ou upload de dados efetuados.
