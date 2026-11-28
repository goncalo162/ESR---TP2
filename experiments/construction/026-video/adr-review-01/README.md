# IMP-019 / 026-video — Revisão ADR com impedimento

Design aprovado; apenas revisão ADR. [Resultado A01–A08 e Q-C-50](../../../../memory-bank/bolts/026-video/adr-review.md).
As pontes de owner/procura/revogação e disponibilidade não existem na API
pública atual. Proposta de oito caminhos apresentada antes de código; os três
atuais continuam a ser o âmbito autorizado. Nenhuma API proposta é declarada
existente; nenhum teste de integração foi executado nesta conferência.

- [Estado de entrada/hashes](entry-state.json).
- [Inventário estático](project-api-inspection.json) e [excertos numerados](source-excerpts.md).
- [Assinaturas JDK21](jdk21-public-apis.txt) e [comando de inspeção](jdk21-command.json).
- [Bytecode de join(Duration)](jdk21-thread-join-duration.txt) e [proposta de âmbito](scope-proposal.json).
- [Verificação final](verification.json) e [V-ADD](v-add.md).

Qualificação anterior ao nó cabe no App; demanda precisa da ponte no owner real.
Retirada existente respeitada por anúncios periódicos, mas inacessível na
composição atual. Prazo restante adapta-se às APIs de lifecycle; eliminar
renovação no fallback do novo composto. Cleanup em FAILED continua incerto,
sem mudar VideoSource; jobs terminados não bastam para Termination.complete.
UNWIRED recusa explicitamente, sem alegação de envio/entrega UDP.

Não executados Maven, aplicações, rede, P01–P20/V-020-C ou leitura de vídeos.
javap só lê assinaturas locais do JDK21, sem executar o produtor/integração.
Os 1582 testes/JAR e conteúdos integrais de025 são evidência histórica.
Negativos/remoção da demo/R17 inicial sem causa estabelecida preservados.
16MiB apenas raster. Sem Implement, commits/push/fecho/outro IMP. Manual
humano aplicável/regressão JDK21/Denji/integração continuam obrigatórios;
TASK-020 só após A/B/C, sem novos gates externos.

Verificação documental: V-ADD0 erros e git diff --check0. Conferidos67 hashes
de fontes/testes/POM e11 ficheiros de evidência anteriores de Model/Design,
todos inalterados; os18 caminhos removidos da demo continuam ausentes.
HEAD/branch preservados, sem staging. Não confundir esta verificação estática
com P01–P20/V-020-C ou regressão de execução.
