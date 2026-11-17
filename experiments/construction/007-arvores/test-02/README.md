# IMP-011 — Test técnico, aprovação pendente

- [clean verify](clean-verify.log): 1328 testes, 0 falhas, 0 erros, 0 ignorados;
  [totais por suite e JAR/hash](result.json), XML/TXT em surefire-reports.
- [Preservação](preservation.json), [classes do JAR](jar-classes.json).
- [TCP real S-A-R-(C1,C2)](controlled-agent/tcp-run-01.log): 19 checks,
  agregação, isolamento e fecho físico/portas; [auxiliar](../test-manual-tools/TcpActivationProbe.java).
- [Oráculos adicionais](controlled-agent/oracles-run-01.log): 18 checks;
  [auxiliar](../test-manual-tools/V014OracleProbe.java).
- [Resultados suplementares](supplements.json); contagens separadas de JUnit.
- [Manual humano reproduzível](../manual-instructions.md), ainda NÃO executado.

A primeira execução restrita em test-01 falhou com 79 erros de sockets
Operation not permitted. Logs/XML preservados antes da repetição autorizada
com sockets locais permitidos. Nenhuma alteração de código ou prazo.

O primeiro ensaio controlado sem wrapper passou24 checks mas a codificação da
saída perdeu acentos; log preservado. O wrapper fixa UTF-8 e foi ensaiado
separadamente como kind=agent. Não é aprovação ou execução humana.

Não houve alterações em Projeto/src desde a aprovação de Implement. JAR copiado
para esta pasta; target não pertence à entrega. Sem staging/commit/push/fecho.

V-ADD: [execução após documentação Test, zero erros](v-add-initial.md).
[Relatório de Test](../../../../memory-bank/bolts/007-arvores/ddd-03-test-report.md).
