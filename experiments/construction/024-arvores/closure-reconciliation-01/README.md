# Reconciliação de fecho — 024/009 e PHASE-04

Registo **2026-11-22T23:10:04Z**. 024/IMP-014 e 009/IMP-015 reconciliados como complete/DONE; unidade004 complete e PHASE-04 DONE. [Aceitações e resultados estruturados](acceptance-and-results.json).

## Confirmações humanas

O utilizador confirmou conjuntamente a conclusão do manual de V-017, a aprovação de Test e a aprovação final de Waheed de 024 e 009 («Tudo concluído — indicar operador e evidência»), e reiterou: «O manual foi concluido. POdes marcar como feito. Corre apenas depois a suite de testes e fecha o que está por fechar». O operador e uma run/log específica do manual não foram indicados; não são inferidos. A confirmação é preservada neste registo, sem inventar um relatório humano.

O utilizador confirmou a coordenação de Fillmore e revisão dos três membros: «Sim, o fecho da fase já foi confirmado pela equipa». As confirmações são registadas agora e não retrodatadas.

## Verificação atual

- JDK21.0.13.1: `mvn -o -B -ntp test` → exit 0, **1459/0/0/0, 21 XML**, terminado em 2026-11-22T23:06:56Z. [Log](maven-test-host.log), [XML preservados](host-surefire-reports/). Os 34 métodos de V-017/S01–S18 e os seis de compatibilidade estão presentes e PASS nos XML atuais.
- A primeira tentativa foi bloqueada pela sandbox nos sockets locais: [log](maven-test.log), [XML](sandbox-failure-reports/). Preservada separadamente; a repetição fora da sandbox serve para remover a restrição de ambiente, sem alterar código ou testes.
- Clean verify anterior: 1459/0/0/0, 21 XML; JAR congelado **dc192971c9ba3a7f66df0e20b3851b4c176c8f4d34158267610b4d7eebab920f**, 517927 bytes. [Evidência original](../test-01/clean-verify-result.json). Não foi gerado um novo JAR nesta reconciliação.
- V-018: runs [aceite](../../../milestone-2/runs/20261122T164428Z-f6n15e/result.json) e [independente](../../../milestone-2/runs/20261122T165306Z-2hy8eu/result.json), ambas PASS 44/44, com o mesmo hash do JAR. Foram conferidas, sem repetir o manual ou a demonstração.
- Integrações comprovadas em main: [PR #5](https://github.com/goncalo162/ESR---TP2/pull/5) → ce76726 (2026-11-22T16:00:42Z); [PR #6](https://github.com/goncalo162/ESR---TP2/pull/6) → 1ee8db6 (2026-11-22T17:50:36Z). HEAD/main: 1ee8db6be28c9c9f091cb8fd90cbfb6c9bda5bc4. Ambos os heads são antepassados de main; fontes Projeto/src sem diferenças entre 15c74b1 e HEAD. Os PRs não têm reviews registadas; a aprovação de Waheed vem da confirmação explícita do utilizador, não do nome da branch.

## Histórico e limites

As datas declaradas de DONE de 009 (17:12:18Z) e 024 (17:47:01Z) têm ordem incompatível com a dependência documental. Não se afirma que a condição estivesse satisfeita às 17:12:18Z: as condições e integrações são verificadas e o fecho ratificado agora. As datas originais e [documentos anteriores](documents-before.json) são preservados.

Os campos `pending`/`false` nos JSON originais descrevem o instante da execução; esses ficheiros não são reescritos para representar aprovações posteriores. [Baseline de hashes](baseline.json) e relatórios Surefire anteriores preservados em prior-surefire-reports/.

Negativos de S13/S05-S06/R10/R17 e restantes evidências preservados. **O negativo inicial R17 continua sem causa estabelecida; a integração com métricas controladas não demonstra a sua causa.** Sem vídeo/GUI/CORE/UDP; morte do relay em V-018 corresponde a corte TRANSPORT numa JVM, não SIGKILL de processo SO. G-ASSESSMENTS mantém informação docente pendente (datas/conteúdo), sem inventar avaliação/submissão.

Não há alteração Java, correção nova, commit, push ou início de outro IMP. [Revisão de fecho da fase](../../../../docs/phases/PHASE-04-CLOSURE-REVIEW.md).

## Verificação documental final

`git diff --check` PASS. Fontes, testes, CORE e todas as evidências preexistentes sem alterações; hashes conferidos contra a baseline. [Resultado estruturado](verification.json). Os dois ficheiros de listas Maven gerados pela suite foram guardados separadamente e repostos nos bytes da baseline limpa.

V-ADD: sete inconsistências preexistentes confirmadas na [leitura de controlo dos documentos anteriores](v-add-before.md), quatro corrigidas no âmbito documental do fecho (critério literal de 024, flags derivadas blocks de 009/010 e ligação relativa no construction-log). [Resultado atual](v-add.md): **três erros preexistentes**, dois fences abertos e um destino de exemplo ausente em .specsmd. Nenhum erro novo; não se declara V-ADD PASS nem se alteram os templates fora do âmbito. A flag blocks=false de 010 apenas reflete as dependências concluídas: o bolt permanece planned, sem etapa iniciada.

O JAR preexistente em Projeto/target tem hash diferente do JAR aceite e manteve os bytes da baseline. Não foi produzido/reclassificado como entrega: a aceitação e o manual dizem respeito ao JAR congelado em test-01, dc192971…ab920f.
