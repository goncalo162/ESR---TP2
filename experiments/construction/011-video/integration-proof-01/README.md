# IMP-017 — integração interrompida antes do commit

Autorização vinculada ao manifesto
f807583c403dca458e0168b1046f6325719d43e2693e5f260bd18495ffa3e85c.
[Preflight](preflight.json):598 caminhos e sete acréscimos exclusivamente de
aprovação/plano;597 hashes de payload aprovados e quatro objetos locais intactos.
[Staging](staging.log),[paths staged](staged-paths.txt),[numstat](staged.numstat),
[stat](staged.stat),[verificação](staged-verification.json).

STOP:15 blobs staged não correspondem ao manifesto. Todas as diferenças são
exatamente CRLF→LF,confirmadas byte a byte. core.autocrlf=input é configuração
global preexistente;não foi alterada. [Diagnóstico](normalization-diagnosis.json),
[atributos](normalization-attributes.txt). Não houve alteração de bytes nos ficheiros
originais da working tree. Os quatro ponteiros LFS são canónicos,oid/size/SHA e
Git blob oid corretos. Os quatro vídeos/cache locais mantêm os hashes aprovados.

[git diff --cached --check](staged-check.log) terminou2 por whitespace de evidência
bruta/logs/headers. Saída completa preservada;sem editar originais,suprimir o check,
normalizar conteúdo ou mudar o validador por inferência.

Não foi criado commit nem executada a prova pós-commit. main continua
 e3d122a77257bf3c5c7489de06a34bad7af31fba. Os598 caminhos permanecem staged para
inspeção;esta pasta de prova é nova e não staged,nem faz parte da allowlist autorizada.
Sem reset/restauro/limpeza,uploads,push,fecho ou intervenção Homebrew.
LOCAL-04 e G-MEDIA-DISTRIBUTION abertos.

Proposta NÃO executada para nova autorização:refazer exclusivamente as15 entradas
de índice listadas no diagnóstico com core.autocrlf=false apenas na invocação,
usando git -c core.autocrlf=false --literal-pathspecs add --renormalize -- <15paths>.
Não alterar configuração global/local ou as quatro regras LFS;conferir novamente
todos os598 blobs contra o manifesto. Avaliar separadamente cada diagnóstico de
whitespace como bytes de evidência original,sem editar/enfraquecer evidências ou
validador. Só prosseguir ao commit após decisão explícita sobre a divergência.
