# PHASE-04 — Revisão de fecho

Fecho reconciliado em **2026-11-22T23:10:04Z** por autorização explícita do utilizador, após a regressão atual. Coordenação de Fillmore e revisão dos três membros confirmadas pelo utilizador: «Sim, o fecho da fase já foi confirmado pela equipa». A presente data regista a confirmação, sem retrodatação.

- TASK-014/007-arvores: V-014, manual Francisco24, Waheed e integração 37fa863 ([fecho](../../memory-bank/bolts/007-arvores/closure-walkthrough.md)).
- TASK-015/023-arvores: V-015, manual Francisco30, Denji e integração 4d18d1d ([fecho](../../memory-bank/bolts/023-arvores/closure-walkthrough.md)).
- TASK-016/008-arvores: V-016/R01–R18, manual Luis sete etapas, Fillmore e integração 447ae4b ([fecho](../../memory-bank/bolts/008-arvores/closure-walkthrough.md)).
- TASK-017/024-arvores: V-017/S01–S18, manual/Test e Waheed confirmados; integração ce76726 ([fecho](../../memory-bank/bolts/024-arvores/closure-walkthrough.md)).
- TASK-018/009-arvores: V-018, duas runs 44/44, Waheed confirmado; integração 1ee8db6 ([fecho](../../memory-bank/bolts/009-arvores/closure-walkthrough.md)).

Regressão pós-integração: JDK21.0.13.1, `mvn -o -B -ntp test`, **1459 testes, zero falhas/erros/ignorados, 21 XML**, exit 0 em 2026-11-22T23:06:56Z. [Resultados e confirmações](../../experiments/construction/024-arvores/closure-reconciliation-01/README.md).

Os critérios da fase estão cumpridos no âmbito de controlo: procura agregada e poda, recuperação, troca confirmada com histerese e demonstração reproduzível. V-018 usa TCP real numa JVM com métricas controladas e corte de TRANSPORT; não demonstra SIGKILL de um processo SO. Sem vídeo, GUI, CORE ou UDP; `play` mantém NOT_IMPLEMENTED. A causa do negativo inicial R17 continua desconhecida e não é demonstrada por esta integração. G-ASSESSMENTS conserva a pendência de datas/conteúdo docente; não se declara avaliação ou submissão realizada. Nenhum requisito transversal é promovido a DONE por este fecho.

Unidade004 complete, PHASE-04 DONE; 17 bolts complete, 18 planned, nenhum ativo. PHASE-05 continua TODO: este fecho não inicia outro IMP. Sem alterações Java, commits ou push nesta reconciliação.
