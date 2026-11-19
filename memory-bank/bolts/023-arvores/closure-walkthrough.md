# IMP-012 / 023-arvores / TASK-015 — fecho documental autorizado

Bolt e história **complete**,implemented=true,TASK-015 **DONE** por autorização
explícita do utilizador após integração local comprovada. Unidade004-arvores e
PHASE-04 continuam **in-progress**,com TASK-016–018 e três histórias/bolts planned.
Nenhum outro IMP iniciado; sem push. Registos anteriores são snapshots históricos.

## Integração e aprovações cumpridas

Commit de integração em main **4d18d1dd8fb50d14ace02609a9133733c20cabfc**,
pai b273b258a4642f94f8614ae00f060a46318d77a5. Os290 caminhos/hashes/modos
coincidem com o manifesto aprovado. [Prova pós-commit](../../../experiments/construction/023-arvores/integration-proof-01/README.md),
[verificação do conteúdo](../../../experiments/construction/023-arvores/integration-proof-01/integration-proof.json)
e [V-ADD da cópia extraída:0 erros](../../../experiments/construction/023-arvores/integration-proof-01/v-add-extracted-commit.md).
Cópia extraída com6253 ficheiros,base fora da allowlist intacta,zero alterações
alheias/target no delta e index vazio após integrar.

Model/Design/ADR/Implement/Test aprovados explicitamente.
[Test/V-015 consolidado](ddd-03-test-report.md): clean verify **1379 testes,
0 falhas,0 erros,0 ignorados**, **19 XML** preservados,51 novos e6 M07 históricos.
[Log/XML/JAR](../../../experiments/construction/023-arvores/test-01/README.md).
JAR495568bytes,SHA-256
`e2a41113b8dbb4d3d68024f546bb570e7509fdd47a2d09366c4b96ef907a1188`.
264 classes do JAR e todos os hashes de fonte/artefactos conferidos antes/depois
da integração; não se repetem build ou manual num fecho sem mudança de código.

Francisco confirmou pessoalmente seis etapas sem divergências observadas:
[relatório humano original](../../../experiments/construction/023-arvores/test-manual-runs/20261118T175055Z-oiu494h1/report.json),
30 CHECK_PASS,CLEANUP_COMPLETE,MANUAL_PASS CHECKS=30,JAVA_EXIT0,passed=true.
[Conferência de fontes/JAR/auxiliares/origem/JVM](../../../experiments/construction/023-arvores/manual-human-verification-01/verification.json).
A confirmação humana vem do utilizador,não apenas da etiqueta human.
WRAPPER_EXIT não consta da saída/relatório fornecidos e não é inventado.

Revisão final Denji da implementação atual aprovada,sem alterações pendentes,
conforme confirmação explícita do utilizador. [Registo vinculado aos três hashes
Java e JAR](../../../experiments/construction/023-arvores/integration-01/review-confirmation.json).
Não houve contacto com pessoas ou aprovação presumida.

## Âmbito, preservação e limites

Fecho exclusivamente documental/evidência. Todos os233 ficheiros src,19 testes
históricos,745 ficheiros target existentes e282 artefactos de evidência anteriores
são preservados por hash; os caminhos target ficam excluídos do commit de fecho.
A prova posterior à integração acompanha o commit documental,sem ser reescrita.
Nenhuma mudança Java/POM/runtime/validador,wire TCPv2/19 tipos/código3 rejeitado,
framing/caps/UTF-8/UDP,owner único ou limites físicos. Q-C-40–42 preservadas:
mesmo subscriptionId/revisão crescente/request novo,prefixo divergente recusado
mantendo grant saudável e três ficheiros de produção/teste sem CLI stop/play.

V-015/L01–L24 provam controlo/procura/renovação/poda/validade/correlação e retenção
física. TCP loopback com dois relays/dois consumidores,sobrevivência além de um
lease,saída parcial/final,órfão com sessão viva e shutdown/portas testados.
Manual combina etapas controladas e TCP real numa JVM; não comprova processos
remotos,vídeo/GUI/CORE,UDP medido ou metas7s/FPS. Autorização de forwarding não
significa que vídeo está implementado. Nenhum requisito global é promovido a DONE.

Negativos de tipos/replay/callback/harness/sandbox e V-ADD preservados. Errata
20→19 XML documentada sem alterar originais. Avisos de whitespace nos logs/diffs
brutos/TXT negativos e diagnóstico Git são bytes de evidência aprovada; não são
normalizados nem ocultados,nem alteram o resultado do validador documental.

## Estado final e publicação

14 bolts/histórias complete,21 planned,nenhum em execução. Apenas o indicador
derivado blocks de008-arvores passa a false pelas dependências concluídas;
status=planned,started=null e stages_completed=[] preservados. Unidade004-arvores/
PHASE-04 em progresso,sem início de TASK-016–018 ou outro IMP. Controlo
renew/deactivate entregue não antecipa recuperação/adaptação/vídeo ou marco.

[Allowlist de fecho](../../../experiments/construction/023-arvores/closure-01/allowlist.txt),
[estado/preservação](../../../experiments/construction/023-arvores/closure-01/preservation-and-status.json),
[V-ADD do fecho](../../../experiments/construction/023-arvores/closure-01/v-add-final.md)
e manifesto/diff guardados em experiments/construction/023-arvores/closure-01.
Commit local exclusivamente documental/evidência autorizado; sem push/novo IMP.
