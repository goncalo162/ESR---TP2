# IMP-017-LOCAL-05 — Proposta Git LFS,por aprovar,não aplicada

Só proposta/documentação e verificação de leitura. Nenhuma instalação,configuração,
staging,commit,upload,push ou migração. .gitattributes continua ausente.
O pacote anterior523 caminhos fica preservado como snapshot;se esta proposta for
aprovada,allowlist/manifesto/plano precisam de revisão para incluir a exceção de
âmbito .gitattributes e artefactos adicionais. Revisão final/integração continuam pendentes.

## Ferramenta e estado real

Git LFS **não instalado no PATH**:git lfs version falha;[saída](tool-availability.txt).
Mac Intel x86_64,macOS14.6.1,Git2.39.5;Homebrew disponível em /usr/local/bin/brew.
Não há .gitattributes nem media já versionados na história local consultada,
nem configuração LFS local/hook pre-push existente. Assim,propor primeiro tracking
sem git lfs migrate,reescrita de histórico ou force. Reconfirmar estado antes de aplicar.
Instalação futura sugerida:Homebrew,com aprovação própria;verificar versão mantida
atual do publicador e compatibilidade. O [publicador](https://git-lfs.com/) apresenta
3.8.1 como versão atual na consulta,não se afirma instalação/teste desse binário.
Ferramenta de desenvolvimento/distribuição,não dependência runtime/POM da aplicação.

## Quotas e custos públicos versus conta real

[Billing oficial](https://docs.github.com/en/billing/concepts/product-billing/git-lfs):
Free/Pro/Free organizações incluem10GiB de storage e10GiB de downloads por ciclo;
Team/Enterprise Cloud250GiB de cada. Uso é contabilizado no proprietário do
repositório,downloads incluídos;novas versões inteiras somam storage,uploads não
consomem quota de download. [Calculadora oficial](https://github.com/pricing/calculator):
excedente USD0,07/GiB-mês de storage e USD0,0875/GiB descarregado.
Valores públicos,sem impostos/câmbio ou promessa sobre a conta concreta.

[Estimativa reproduzível](quota-estimate.json):459820128 bytes =0,428240865GiB.
Uma versão dos quatro media consome cerca de0,428GiB de storage;cada descarga
completa sem cache cerca de0,428GiB;três colegas1,284722596GiB.
Se **não houvesse qualquer outro uso** de uma quota10GiB,caberiam23 descargas
completas nesse ciclo. Se todo este volume fosse excedente faturável:aprox.
USD0,030 por mês de storage e USD0,0375 por descarga integral. Não são faturas.

Plano,saldo usado/disponível,budget e forma de pagamento reais do proprietário
**não acessíveis nesta sessão**. Não afirmar custo zero garantido. Antes de qualquer
upload,o proprietário deve conferir Billing/usage e orçamento permitido;nenhuma
configuração de billing é alterada pelo agente. Uma conta sem saldo pode devolver
só ponteiros ou bloquear operações. A calculadora mantém redação histórica sobre
transição;usa-se a documentação atual de billing para o regime e a calculadora
para estimar tarifas,com confirmação final na conta antes de incorrer em custos.
Todos os ficheiros são menores que o mínimo2GB/ficheiro de LFS publicado para
Free/Pro:[limites](https://docs.github.com/en/repositories/working-with-files/managing-large-files/about-git-large-file-storage).

## Alteração exata proposta — apenas quatro regras

Criar .gitattributes com estas quatro linhas,sem wildcard global,sem .lfsconfig
ou endpoint externo:

```gitattributes
/Projeto/src/main/resources/media/a.Mjpeg filter=lfs diff=lfs merge=lfs -text
/Projeto/src/main/resources/media/b.Mjpeg filter=lfs diff=lfs merge=lfs -text
/Projeto/src/main/resources/media/c.Mjpeg filter=lfs diff=lfs merge=lfs -text
/Projeto/src/main/resources/media/d.Mjpeg filter=lfs diff=lfs merge=lfs -text
```

[Patch proposto,não aplicado](gitattributes-proposed.patch). -text impede conversão
de fins de linha nos binários. Git guardará ponteiros;os quatro bytes completos
continuam no working tree e nos objetos LFS. SHA-256 e tamanhos do conteúdo real
não mudam. No index,verificar oid sha256 e size do ponteiro contra os hashes
aprovados,não comparar o hash do texto do ponteiro ao hash do vídeo.

## Sequência futura de aplicação — só após aprovação explícita

1. Reconfirmar refs/estado,cinco hashes aprovados,quota/budget e permissões do
   proprietário;preservar a pendência de direitos,não autorizar uploads por este plano.
2. Instalar ferramenta com aprovação própria;por exemplo,macOS:
   `brew install git-lfs`. Confirmar `git lfs version`. Nenhum comando executado agora.
3. Inspecionar hooks existentes. Configuração futura apenas neste clone:
   `git lfs install --local --skip-smudge`.
   [Semântica oficial](https://raw.githubusercontent.com/git-lfs/git-lfs/main/docs/man/git-lfs-install.adoc).
   Instala filtros locais e hook pre-push;se houver conflito de hooks,parar e
   rever,nunca --force por inferência. Não alterar configuração global.
4. Aplicar só o patch aprovado e reconferir `git check-attr filter diff merge text --`
   com os quatro caminhos. Esperado lfs/lfs/lfs/unset. Verificar hashes reais de novo.
5. Apenas com autorização de staging/commit e allowlist atualizada,fazer staging
   explícito de .gitattributes,quatro media e restantes caminhos aprovados.
   Conferir ponteiros de cada blob do index,oid/size aprovados e objetos locais;
   exemplos de inspeção: `git show :Projeto/src/main/resources/media/a.Mjpeg`.
   Não usar migrate,add global,renormalização de todo o repositório ou reescrita.
6. Após commit local autorizado,verificar ponteiros do commit e executar
   `git lfs fsck --objects HEAD`;materializar cópia do commit com objetos locais,
   verificar os ficheiros reais/manifesto e V-ADD. git archive cru contém ponteiros;
   não usar o seu conteúdo como se fossem vídeos nem criar prova falsa de extração.
7. Upload/push **exigem outra autorização**. O hook pre-push pode carregar objetos
   LFS antes do Git;push não é operação apenas de ponteiros nem é implicitamente
   autorizado pelo commit. Não executar git lfs push --all ou uploads neste plano.
   Sem aprovação de redistribuição,G-MEDIA-DISTRIBUTION continua aberto para TASK-030.

## Colegas — obter os ficheiros completos,depois de publicado com autorização

É necessário ter Git LFS instalado. Comandos abaixo são instruções futuras,não
executadas nesta sessão. Só depois de main conter a revisão publicada comunicada;
confirmar o hash de HEAD com o responsável. Para clone novo:

```sh
GIT_LFS_SKIP_SMUDGE=1 git clone https://github.com/goncalo162/ESR---TP2.git ESR---TP2-media
cd ESR---TP2-media
git lfs install --local --skip-smudge
git rev-parse HEAD
git lfs pull --include="Projeto/src/main/resources/media/a.Mjpeg,Projeto/src/main/resources/media/b.Mjpeg,Projeto/src/main/resources/media/c.Mjpeg,Projeto/src/main/resources/media/d.Mjpeg" --exclude="" origin
git lfs fsck --objects HEAD
python3 -B experiments/construction/011-video/lfs-proposal-01/verify-media.py --root .
```

Para clone existente:primeiro git status --short;se não estiver limpo,parar e
preservar trabalho. No ramo correto,após combinar a versão revista:

```sh
git lfs install --local --skip-smudge
GIT_LFS_SKIP_SMUDGE=1 git pull --ff-only
git rev-parse HEAD
git lfs pull --include="Projeto/src/main/resources/media/a.Mjpeg,Projeto/src/main/resources/media/b.Mjpeg,Projeto/src/main/resources/media/c.Mjpeg,Projeto/src/main/resources/media/d.Mjpeg" --exclude="" origin
git lfs fsck --objects HEAD
python3 -B experiments/construction/011-video/lfs-proposal-01/verify-media.py --root .
```

[git lfs pull](https://raw.githubusercontent.com/git-lfs/git-lfs/main/docs/man/git-lfs-pull.adoc)
materializa a revisão corrente;--exclude vazio evita exclusões de fetch herdadas.
--skip-smudge exige repetir pull explícito ao mudar para uma revisão com novos
objetos. Só checkout de ponteiros não basta. Falha de quota/rede/hash:parar,não
substituir por ficheiros vazios. Não alterar .gitattributes para contornar falhas.

## Futuro Codigo.zip — vídeos reais obrigatórios

[Arquivos GitHub](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/managing-repository-settings/managing-git-lfs-objects-in-archives-of-your-repository)
podem conter só ponteiros por omissão. Não depender de Download ZIP nem alterar
settings do repositório por inferência. Não aprovar um ZIP de git archive cru.

Proposta concreta [build-codigo.py](build-codigo.py):somente no futuro,com SHA
completo revisto e checkout materializado,exige .gitattributes exato,quatro ponteiros
oid/size aprovados e manifesto idêntico. Exporta código do commit,substitui os
quatro ponteiros pelos vídeos reais aprovados,exclui Projeto/target e verifica os
cinco SHA-256 dentro do ZIP antes de publicar o nome final. Recusa sobrescrita.
Não foi executado;não existe ainda Codigo.zip final validado ou entrega autorizada.
PL/grupo/nome final não são inventados;Codigo.zip é nome técnico provisório.

Comandos futuros,depois da autorização de empacotamento e decisões de direitos:

```sh
python3 -B experiments/construction/011-video/lfs-proposal-01/verify-media.py --root .
# Substituir o valor pelo SHA completo realmente revisto,sem inventar um commit.
REVISAO_APROVADA=SUBSTITUIR_PELO_SHA_COMPLETO_REVISTO
python3 -B experiments/construction/011-video/lfs-proposal-01/build-codigo.py \
  --repo . --commit "$REVISAO_APROVADA" --output /private/tmp/Codigo.zip
python3 -B experiments/construction/011-video/lfs-proposal-01/verify-media.py \
  --zip /private/tmp/Codigo.zip
```

[Verificador](verify-media.py) fixa tamanhos e SHA-256 dos quatro media e manifesto;
recusa ponteiros,entradas ausentes/duplicadas,symlinks,tamanhos ou hashes divergentes.
ZIPs com prefixo de pasta usam --prefix Codigo/ se esse for o prefixo real.
[Hashes locais conferidos agora](approved-hashes-check.json),
[testes negativos do verificador](zip-verifier-check.json):ponteiro/falta rejeitados.
Só fixtures ZIP minúsculas temporárias foram criadas/apagadas;não se afirma teste
positivo de um futuro ZIP ainda inexistente. Scripts apenas propostos,sintaxe conferida.
A garantia é fail-closed:o Codigo.zip só será considerado pronto quando os quatro
corpos reais e manifesto passarem esta verificação após criar o arquivo.

## Pendências preservadas

LOCAL-04:B com créditos presentes mas ilegíveis,aceite só no Test técnico.
Não aprova atribuição adequada,legibilidade ou redistribuição;G-MEDIA-DISTRIBUTION
aberto para TASK-030,A sem licença audiovisual específica conhecida. Test/G-MEDIA
técnicos não são autorização de upload. LOCAL-03 não instalou Projeto/media;
perfis atuais não são anunciados como prontos. Revisão final/integração/fecho
continuam pendentes;TASK-020 inteira aberta. Sem alterar os conteúdos aprovados.

[Preservação/verificação do estado](preservation.json),[fontes oficiais consultadas](sources.json).
[V-ADD da proposta](v-add.md). Nenhum ficheiro da proposta foi acrescentado ao index.
