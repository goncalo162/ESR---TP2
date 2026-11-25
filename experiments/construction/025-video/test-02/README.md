# IMP-018 — prova integral e execução delegada, sem rebuild

JAR fixado: `f9a3cb9cad6166d13e244a772398179731952fd8eb8ff0efe7b16a6f67b9787f`. Clean verify/JAR/V-ADD anteriores
conferidos pelo utilizador; não se repetiu build nem se alterou código aprovado.

## Prova nova, nos quatro binários completos

Recuperados por download Git LFS, operação de leitura apenas. Binários guardados
localmente em media/, ignorados por Git para não duplicar459820128 bytes; os
ponteiros LFS canónicos continuam versionados nos recursos da aplicação.
[Downloads/hashes](download-results.json), [prova consolidada](full-content-consolidation.json).

| Conteúdo | Frames reais/esperados | Bytes | SHA-256 consumido | EOF/END | Fecho entrada |
|---|---|---|---|---|---|
| A | 500/500 | 4269893 | `ca461cb1edce18efb355794ffb7d52f73dc7ce4c03422224894eb0fc550b0cca` | sim |1 tentativa, closed=true |
| B | 5965/5965 | 111255866 | `ceb6497674bde7210bff2162b07e4305a74b33ed2b5a381b3e973d30fc837c5f` | sim |1 tentativa, closed=true |
| C | 8880/8880 | 167319819 | `d2a050894ced76969b2bafae669532141dccf770fa2352e9ac4d0a4374bed3ab` | sim |1 tentativa, closed=true |
| D | 7342/7342 | 176974550 | `c47ef9b41f05904e1bb6657424e2d88fe504e0925c0dffbdee85c58bf8c8e4cc` | sim |1 tentativa, closed=true |

Total22687 frames. Cada Frame foi comparado em índice, offset, comprimento e
SHA-256 JPEG com o JSONL independente aprovado; descodificação pelo reader real
do novo VideoSource no JAR. Não se substituiu esta execução pela auditoria antiga
nem pelo controlo de2 fixtures. Cleanup integral observado: entrada física fechada
uma vez em cada ficheiro e close repetido inócuo. Descarte de reader/raster é coberto
por M10, não contado separadamente por frame nesta passagem integral.

[Run técnica inicial](../test-manual-runs/20261125T165756Z-ad63f5e2/report.json),
[nova run delegada após instrução do utilizador](../test-manual-runs/20261125T170246Z-17a25dc1/report.json),
ambas saída0. O JAR, código e cenários aprovados permaneceram iguais.

## Manual pessoal e autorização posterior

[Negativo pessoal de Luis](../test-manual-runs/20261125T165933Z-c5eb7f59/report.json):
procurou media em test-02/; os ficheiros estão em test-02/media/. A etapa6 registou
`sisim`, rejeitado pelo runner. Saída1, manual_human_complete=false. Preservado
sem edição, sem corrigir código por inferência nem atribuir causa adicional.

O utilizador instruiu depois: «faz tu o teste e se der correto dá commit das
alterações e fecha o bolt». Execução delegada em Codex concluída, kind=automated,
saída0. Regista-se aceitação condicional satisfeita e substituição expressa da
exigência de execução pessoal para este fecho; **não** se inventa um manual humano
bem-sucedido. [Autorização e revisão](acceptance-and-review.json). Denji já reviu
e aprovou esta versão, conforme confirmação posterior explícita do utilizador.

## Preservação e integração

Negativos/ensaios anteriores intactos, incluindo falha DNS da tentativa de consulta
LFS na sandbox e consulta seguinte com rede autorizada. Nunca houve upload/push.
O script Git LFS não foi instalado. Nenhum source/config/protocolo ou Java aprovado
alterado. Os dois templates locais ignorados por .gitignore mantêm o patch V-ADD
autorizado; delta e hashes estão versionados como evidência no test-01, sem adicionar
à força todo o framework local ignorado. Validador e remoção da demo preservados.

Integração local e fecho a registar após commit. TASK-020 continua IN PROGRESS
atéA/B/C; parteC/IMP-019 não iniciada.16MiB limita raster, não memória total do
JPEG decoder/JVM. R17 inicial continua sem causa estabelecida.
