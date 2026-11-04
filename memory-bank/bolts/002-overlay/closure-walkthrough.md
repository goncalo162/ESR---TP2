# IMP-003 — Fecho preparado para revisão final

Preparado em 2026-11-04T14:10:25Z. Test aprovado explicitamente pelo utilizador,
com registo em 2026-11-04T14:05:14Z, incluindo os 856 testes e o manual pessoal
de Luis. Autorizados commits e push de Denji. Base do incremento:
`4ac5808dacf6e97bfd3636d2788db363e926bc95`.

## Estado e pendências

As cinco etapas Model → Design → ADR → Implement → Test estão concluídas.
Os seis critérios técnicos têm evidência em V-006/M01–M18 e foram assinalados
no bolt, história e TASK-006. O bolt e a história continuam **in-progress**;
TASK-006 continua **IN PROGRESS**, completed=null e sem implemented=true.
A unidade permanece em construção; 003-overlay e os restantes incrementos
planeados não foram iniciados.

Para fechar, falta a aceitação da versão final pelo revisor atribuído **Waheed**
e a integração da versão revista em main, conforme
[TASK-006](../../../docs/tasks/PHASE-02/TASK-006.md) e
[Definition of Done](../../../docs/14-DEFINITION-OF-DONE.md).
A aprovação de Test por Luis não regista uma aceitação em nome de Waheed.
O mecanismo bolt-complete e a cascata de estados ficam por aplicar depois
dessas condições. Publicar Denji preserva este estado para revisão.

## Resultados aprovados e proveniência

[Relatório Test](ddd-03-test-report.md) e
[mapa V-006/M01–M18](../../../experiments/construction/002-overlay/test-case-map.json).

| Execução | Instantes UTC em 2026-11-04 | Resultado | Evidência |
|---|---|---|---|
| clean verify, JDK 21.0.12.1, TCP real | 12:18:05Z–12:18:10Z | 856 testes, zero falhas/erros/skips, saída 0 | [Log Maven](../../../experiments/construction/002-overlay/test-clean-verify.log), quatro XML/txt Surefire novos |
| Manual do agente Codex | 12:22:51Z–12:22:53Z | 34 CHECK_PASS, MANUAL_PASS, saída Java 0 | [Log do agente](../../../experiments/construction/002-overlay/test-manual.log) |
| Manual pessoal de Luis | 13:58:36.658445362Z–13:58:38.039499140Z | 34 CHECK_PASS, MANUAL_PASS, JAVA_EXIT=0 | [Log de Luis](../../../experiments/construction/002-overlay/test-manual-luis.log), [metadados](../../../experiments/construction/002-overlay/test-manual-luis.json) |

Os 856 testes são 740 configuração +66 codec +31 transporte +19 bootstrap.
Os dois manuais usam o mesmo JAR, SHA-256
`2c3c063ac58692d1fd813d7e7564b00af7068906d156cc99126827847d2a9ccc`.
O log pessoal identifica Luis e verifica esse hash antes de abrir rede.

O original `/tmp/ott-imp003-manual.sivs66.log` permanece intacto; a cópia tem os
mesmos 2248 bytes e SHA-256
`6d7cc4eb76bc0487128c685f8143be1497255a9b7048aa655424a3a9781bcaa9`.
As durações pessoais (501 ms/503 ms/5 ms) e as do agente (504 ms/503 ms/4 ms)
permanecem separadas, sem as transformar em metas de desempenho.
[Comandos para repetir](../../../experiments/construction/002-overlay/manual-bootstrap.md)
preservados como artefacto do checkpoint anterior; a indicação de pendência
nesse retrato foi superada pela aprovação aqui registada.

## Diff e preservação

O incremento inteiro altera somente os cinco ficheiros Java autorizados:

| Ficheiro relativo a Projeto/src | Acrescentadas | Removidas |
|---|---:|---:|
| main/java/pt/uminho/esr/ott/bootstrap/BootstrapServer.java | 368 | 11 |
| main/java/pt/uminho/esr/ott/transport/Connection.java | 124 | 10 |
| main/java/pt/uminho/esr/ott/transport/Transport.java | 27 | 14 |
| test/java/pt/uminho/esr/ott/bootstrap/BootstrapServerTest.java | 452 | 0 |
| test/java/pt/uminho/esr/ott/transport/TransportTest.java | 255 | 0 |
| **Total** | **1226** | **35** |

As 35 remoções estão justificadas no
[walkthrough de implementação](implementation-walkthrough.md) e no
[inventário por linha](../../../experiments/construction/002-overlay/implement-removals.json).
Os 18 testes anteriores de transporte foram preservados. Outros 150 ficheiros
de Projeto/ mantêm os hashes da base; todos os 155 ficheiros fora de target
mantêm os hashes da execução Test aprovada. Código, POM, fixtures e bytes v1
não foram alterados durante esta preparação.

Os diffs `closure-preparation-documents.diff` e
`closure-preparation-cumulative.diff` distinguem esta preparação documental
das etapas anteriores e mostram o resultado contra a base. Os logs, XML e
diffs anteriores permanecem históricos; não se reescrevem resultados ou
pendências que eram verdadeiros nos respetivos checkpoints.

As listas Maven em Projeto/target/ ficam num diff separado e fora dos commits.
Os atributos locais da pasta de evidências preservam whitespace de logs,
XML/txt Surefire e patches brutos, seguindo o padrão dos bolts anteriores;
código, scripts, metadados e Markdown mantêm a verificação normal.

## Verificação desta preparação

V-ADD e git diff --check têm resultados atuais em
[validação documental](../../../experiments/construction/002-overlay/closure-preparation-validation.md)
e [preservação e diff final](../../../experiments/construction/002-overlay/closure-preparation-results.json).
Não foi executado novo build nem manual nesta preparação; os resultados Java
são os aprovados e preservados acima, sobre fontes com hashes inalterados.
Os metadados anteriores a esta aprovação conservam a sua data e estado;
o registo atual prevalece para a aprovação Test e o estado de preparação.

TCP loopback/API embutível não demonstram CLI/HELLO, JVMs overlay independentes,
vídeo, GUI, CORE ou recuperação. Flush local não é ACK remoto. Nenhum desses
resultados, revisão de Waheed, integração em main ou fecho foi presumido.
