# IMP-017 / 011-video — Pacote para revisão final,sem fecho

Test e G-MEDIA aprovados **apenas no âmbito técnico** pelo utilizador:
22687 JPEGs,16 casos de fronteira,quatro conteúdos distintos e manual Francisco13.
[Confirmação vinculada aos hashes](technical-approval.json).
Revisão final ainda pendente,não confirmada;integração pendente,bolt/história abertos.
TASK-020 pai permanece em progresso;esta é só parte A.

## Resumo da versão validada tecnicamente

Quatro ficheiros finais no perfil cinco dígitos ASCII+L bytes JPEG,sem separadores.
A/S1:movie.Mjpeg docente,preservado byte a byte pelo hash histórico,sem reencodar.
B/S1:Big Buck Bunny;C/S2:Sintel;D/S2:Tears of Steel,filmes completos/créditos retidos,
preparação10fps/640×360/q5/yuvj420p,sem trim. FPS de reprodução não medido.
Limite99999 do perfil separado do cap UDP2MiB,sem mudança de wire/caps.
[Manifesto aprovado intacto](../../../media/manifest.json),
[Test/oráculos](../../../../memory-bank/bolts/011-video/test-walkthrough.md),
[evidência técnica](../test-01/README.md),
[manual e tentativa parcial preservados](../manual-verification-01/README.md).

| Alias/origem | Frames | Bytes | SHA-256 |
|---|---:|---:|---|
| A/S1 |500|4269893|ca461cb1edce18efb355794ffb7d52f73dc7ce4c03422224894eb0fc550b0cca|
| B/S1 |5965|111255866|ceb6497674bde7210bff2162b07e4305a74b33ed2b5a381b3e973d30fc837c5f|
| C/S2 |8880|167319819|d2a050894ced76969b2bafae669532141dccf770fa2352e9ac4d0a4374bed3ab|
| D/S2 |7342|176974550|c47ef9b41f05904e1bb6657424e2d88fe504e0925c0dffbdee85c58bf8c8e4cc|

459820128 bytes de media;documentação/evidência acrescem,volume exato no inventário.
Nenhuma nova auditoria/conversão/manual foi repetida ou resultado Java alegado.

## Limitação aceite,preservada e ainda aberta para distribuição

B:créditos presentes mas ilegíveis,nota pessoal «nao dava para ler as letras».
C/D legíveis nas amostras,sem generalizar a todo o texto. Aceitação técnica de B
**não** aprova atribuição adequada,legibilidade ou redistribuição.
LOCAL-04 e G-MEDIA-DISTRIBUTION mantêm-se abertos para TASK-030;A continua sem
licença audiovisual específica conhecida. Nenhuma proposta de correção aplicada.
Originais completos e parcial20261124T152418Z-403a6f63 intactos.
LOCAL-03 opção1:media apenas em resources/media;Projeto/media não instalado;
cópia/hash futura na composição,não anunciar perfis atuais prontos.

## Revisão e conteúdo proposto

[Allowlist exata por caminho](allowlist.txt),[grupos/volumes](allowlist-groups.json),
[manifesto SHA-256](manifest.json),[preservação/exclusões](preservation.json).
[Diff código/documentação/manifesto](content.diff),[numstat](content.numstat).
Binários indicados pelo Git como binary;hashes/tamanhos no manifesto substituem
um despejo de centenas de MB no diff. Execuções/fotogramas de evidência têm
inventário de caminhos/hash,sem modificar originais para compactar o pacote.
O manifesto exclui apenas o seu próprio hash;allowlist inclui o manifesto,
e o hash deste é apresentado separadamente no checkpoint. Este é pacote candidato
para revisão,**não** autorização de staging/commit nem revisão final presumida.

[Plano de integração](integration-plan.md),[restrição de volume/publicação](volume-constraint.json),
[estado local](branch-state.json),[V-ADD](v-add.md).
Origin configurado GitHub;B/C/D excedem100MiB por ficheiro,limite do Git normal
no GitHub. Isto não reabre Test/G-MEDIA nem escolhe solução;publicação remota não
é prometida. Dados/armazenamento permanecem inalterados,sem LFS/externo por inferência.

Sem Java/perfis/POM/fixtures/demo alterados,staging,commits,push ou fecho.
IDs locais preservados,sem reserva Q-C global. Media/fontes técnicas temporárias
fora da allowlist não são dependências runtime;cache de downloads/ferramentas
fica fora do pacote Git e pode ser reobtido pelas URLs/hashes documentadas.

Artefactos históricos preservam o estado à data da execução,incluindo campos que
então diziam pendente. A aprovação corrente consta de technical-approval.json e
dos documentos vivos;originais não são reescritos para mudar o passado.
