# Homebrew — diagnóstico em leitura e proposta não executada

openssl@3 3.5.9 permanece instalado e executa. openssl@4 4.1.0 e libssh2
1.11.1_7 permanecem instalados. Os links genéricos bin/openssl,include/openssl,
libssl/libcrypto e pkg-config apontam para4;opt/openssl@3 continua a apontar para3.
[Versões](brew-versions.txt),[dependências dinâmicas](dynamic-dependencies.txt),
[inventário de links](openssl-links-observed.json),[comparação estática](openssl3-prefix-comparison.json).
A comparação é de caminhos físicos,não replica exclusões/colapsos de diretórios
Homebrew;existing_non_symlink pode ser ficheiro sob um diretório que é symlink.
Não interpretar os totais como número exato de links a repor.

A tentativa de dry-run falhou por tentativa de criar symlink ao expandir conflito
de diretório: [registo](dry-run-failure.txt). Não foi repetida com elevação.
O código local keg.rb resolve_any_conflicts invoca link_dir(dry_run:false) nesse
ramo. Por isso não se recomenda repetir esse dry-run de link neste estado.
Os links principais permanecem para4. Sem inventário completo prévio do sistema,
não se afirma prova de identidade de todos os caminhos anteriores à tentativa.
Nenhum comando de instalação,unlink/link real,uninstall ou alteração de pacote foi
executado nesta etapa de diagnóstico.

## Proposta para autorização separada

Objetivo:devolver os caminhos genéricos à versão3 instalada,sem apagar4/libssh2,
sem overwrite global e sem modificar PATH,Java ou a aplicação.
Antes da execução,guardar novamente links e recibos,conferir refs do repositório
e parar se os caminhos relevantes divergirem deste inventário. Os comandos abaixo
são uma proposta sequencial,não um script executado;parar em qualquer erro.

```sh
HOMEBREW_NO_AUTO_UPDATE=1 brew unlink openssl@4
HOMEBREW_NO_AUTO_UPDATE=1 brew link --force openssl@3
```

Não usar --overwrite. --force apenas permite ligar fórmula keg-only;conflito
residual exige nova inspeção,não remoção por inferência. Nenhum pacote será
removido. Se link3 falhar,reportar o estado parcial e conflito antes de prosseguir.
A versão3 instalada terá a topologia padrão atual do Homebrew;sem inventário
anterior completo não é possível prometer restauração exata dos6529 links.
Não alterar manualmente opt/openssl genérico sem verificar a política de aliases
resultante;opt/openssl@3 e opt/openssl@4 devem continuar válidos.

Verificação posterior proposta:readlink/realpath do binário,includes,bibliotecas
não versionadas e pkgconfig;executar /usr/local/bin/openssl version e o binário
explícito de4;verificar dependências de libssh2 com otool -L e as bibliotecas de4
existentes. Conferir inventário antes/depois e Git LFS version. Se autorizado,
um teste de carga local de libssh2 pode complementar otool;otool atual não prova
execução nem ausência de falhas em todos os consumidores.

## Impactos e limitações

Novas invocações por /usr/local/bin/openssl e compilações que usam includes,
-lssl/-lcrypto ou pkg-config genéricos passam de4 para3. ABI/API podem divergir;
software que passou a depender dos caminhos genéricos de4 pode ser afetado.
Não altera bibliotecas já carregadas em processos existentes.
libssh2 observado referencia explicitamente opt/openssl@4/lib/libssl.4 e
libcrypto.4:preservar esse keg e opt link;repor genéricos não deve trocar essas
referências (inferência fundamentada em otool,não teste funcional universal).
Git LFS instalado não lista OpenSSL em otool;nenhuma reinstalação é proposta.
Não avaliar/reverter outras alterações de pacotes nesta autorização.

[Semântica oficial de link/unlink](https://docs.brew.sh/Manpage.html):unlink retira
links do prefixo;link recria-os,--overwrite pode apagar conflitos e não faz parte
da proposta. Diagnóstico separado da aprovação técnica dos media.
