# IMP-017 / 011-video — entrega para revisão final pendente

A preparação local Git LFS e a validação dos quatro media foram aceites pelo
utilizador. O manual Francisco13 já foi cumprido. **Revisão final não concluída**:
aguarda confirmação explícita do utilizador;integração também pendente.

## Versão vinculada

O pacote de578 caminhos foi preservado integralmente. Manifesto SHA-256:
`0e3ad7da6d2aa7bd6196e251a02fdc2532a79d4c27c708280924d4ec05c3a719`.
[Manifesto vinculante anterior](../lfs-review-package-01/manifest.json),
[conferência atual dos577 hashes de payload](approved-package-check.json),
[resumo e diffs](../lfs-review-package-01/README.md),
[ponteiros versus vídeos reais](../lfs-review-package-01/media-representation.json).
Nenhum desses artefactos foi reescrito. Este suplemento regista a aceitação da
preparação LFS e acrescenta diagnóstico/proposta Homebrew,sem alterar media ou Java.

[Allowlist final de591 caminhos](allowlist.txt) e [inventário suplementar](manifest.json)
acrescentam estes artefactos ao pacote anterior;preservam o vínculo ao hash aprovado.
Não substituem os hashes originais. Não constituem autorização de staging.
[Plano de integração preservado](../lfs-review-package-01/integration-plan.md).
Não se repetiram build,auditoria JPEG ou manual:os hashes aprovados continuam iguais.

## Reconciliação exata:578 +13 =591 caminhos

Nenhum caminho anterior foi removido. Os13 acréscimos estão todos nesta pasta;
não acrescentam media,Java,configuração de aplicação ou uma intervenção de sistema.

| Caminho relativo a review-handoff-01/ | Finalidade |
| --- | --- |
| README.md | Entrega para revisão,estado pendente e explicação dos13 acréscimos. |
| allowlist.txt | Lista final exata:578 caminhos anteriores +13 deste suplemento. |
| approved-package-check.json | Conferência do manifesto preservado e dos577 hashes de payload,refs e index. |
| manifest.json | Inventário suplementar das591 paths e hashes por representação. |
| v-add.md | Resultado documental do suplemento,não novos testes Java/media. |
| brew-versions.txt | Versões instaladas observadas por comando em leitura. |
| dry-run-failure.txt | Erro preservado da tentativa dry-run;não repetida com elevação. |
| dynamic-dependencies.txt | Saída otool das dependências de libssh2,Git LFS e OpenSSL3. |
| homebrew-proposal.md | Proposta não executada,impactos e limites da eventual reposição. |
| openssl-links-observed.json | Inventário em leitura dos links OpenSSL observados. |
| openssl3-prefix-comparison.json | Comparação estática de caminhos,não uma simulação integral de brew link. |
| openssl3-version.txt | Resultado do binário explícito OpenSSL3. |
| openssl4-version.txt | Resultado do binário explícito OpenSSL4. |

O **manifesto preservado** pertence ao pacote anterior de578 paths e conserva o
SHA-256 indicado acima. O **inventário suplementar**,embora o nome do ficheiro
seja manifest.json,é novo:contém bound_approved_manifest com o vínculo ao original
e entries para as591 paths da allowlist final. Em cada entrada,worktree_sha256 e
worktree_bytes vinculam o ficheiro real;git_content distingue blobs ordinários
dos quatro ponteiros LFS esperados e respetivos oid/size dos vídeos. O manifesto
original é agora uma entrada ordinária e tem o seu hash expressamente conferido.

O inventário suplementar inclui o hash da allowlist final e o de cada payload.
A única exceção é o próprio inventário (self_hash_excluded),cujo SHA-256 é
apresentado fora dele no checkpoint,evitando autorreferência. Os hashes do
README e dos restantes12 acréscimos encontram-se nesse inventário;para o próprio
manifest.json usa-se o hash externo. Assim,a lista final fica vinculada a cada
ficheiro e ao manifesto preservado,sem substituir a evidência anterior.

Os oito ficheiros Homebrew são diagnóstico/proposta arquivados para rastreabilidade.
A sua presença na allowlist não autoriza intervenção,não integra uma religação
no bolt e não cria um gate de fecho do IMP-017. Qualquer intervenção permanece
separada,pendente de autorização específica. O erro dry-run original é imutável.

## Âmbito da revisão solicitada

Conferir os quatro media e manifesto/proveniência;oráculos V-020-A e negativos;
manual completo e tentativa parcial preservada;atributos exatos,ponteiros oid/size,
materialização local e empacotador que coloca vídeos reais no futuro Codigo.zip.
Conferir exclusão de target/,demo/,caches/config/hooks locais e alterações alheias.
Depois da confirmação da revisão final,registar revisor/âmbito/hashes e pedir
somente a autorização de integração aplicável. Sem presumir revisão de Denji.

LOCAL-04:créditos B presentes mas ilegíveis («nao dava para ler as letras»).
Não há aprovação de atribuição adequada,legibilidade ou redistribuição.
G-MEDIA-DISTRIBUTION continua aberto para TASK-030. LOCAL-03 não instalou
Projeto/media. Antes de upload/push/publicação,pedir ao proprietário confirmação
de quota restante e orçamento autorizado;não assumir custo zero. Não contactámos
terceiros nem alterámos billing/settings. O hook pre-push pode enviar objetos LFS.
TASK-020 inteira permanece aberta;parte A não fecha o pai.

## Sistema local

[Diagnóstico Homebrew e proposta concreta não executada](homebrew-proposal.md).
A intervenção futura nos links exige autorização separada. Não desinstalámos,
religámos ou alterámos pacotes nesta etapa. A tentativa dry-run e a sua limitação
estão registadas explicitamente. Evidência anterior de instalação é preservada.

[V-ADD documental desta entrega](v-add.md). Sem staging,commits,uploads,push,
fecho,alterações de conteúdos,Java/perfis/POM/fixtures/demo ou histórico.
