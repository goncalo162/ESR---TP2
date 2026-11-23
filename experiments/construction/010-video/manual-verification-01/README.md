# IMP-016 — Manual humano Francisco24 conferido

[Relatório original](../test-manual-runs/20261123T153332Z-e5356b3d/report.json),
[log pessoal](../test-manual-runs/20261123T153332Z-e5356b3d/manual.log),
[conferência/hashes/proveniência](verification.json).
Originais preservados; nenhuma execução repetida pelo agente.

24 CHECK_PASS numerados1..24,8 confirmações1..8,PROBE_PASS CHECKS=24,
CLEANUP_COMPLETE,MANUAL_PASS CHECKS=24,JAVA_EXIT=0,PASSED=true,WRAPPER_EXIT=0.
SHELL_EXIT=0 confirmado explicitamente pelo utilizador; não consta do log/JSON.
Francisco confirmou execução pessoal sem divergências observadas.

Hash do JAR/config/probe/runner,código e fixtures conferidos. JAR preservado:
588bd8e5f85c880666beab1a2033c8f100f3bd37e79d2d83ed4d15ec322ac3a2.
A origem StreamPacket é impressa; CHECK_PASS1 do probe fixado valida que tanto
StreamPacket como MessageCodec vieram desse JAR. Conteúdo das classes coincide
com jar-info.json da construção clean verify1537/0/0/0,22 XML.

JAVA_EXIT provém do wait real no runner; cleanup verifica a JVM terminada e a
pasta temporária já não existe. Não se alega inventário atual de processos ou
execução de streaming/GUI/CORE. Manual é de codec puro.

Test reapresentado por aprovar. Revisão final Denji e integração pendentes.
Ramo imp-016-udp,IDs locais,demo/main preservados,target/ excluído.
Sem staging/commits/push/fecho. Sem gates externos adicionais.
