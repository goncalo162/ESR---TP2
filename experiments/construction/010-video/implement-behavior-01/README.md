# IMP-016 — Checkpoint Implement

Implement por aceitar, Test não autorizado. Apenas StreamPacket.java e respetivo teste.

## Execuções reais

JDK21 (JAVA_HOME=/Users/maria/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home).
Comandos na raiz do repositório:

- mvn -o -B -ntp -f Projeto/pom.xml -Dtest=StreamPacketTest test: primeiro verde76/0/0/0, exit0. [Log](unit-01.log), [XML](unit-01-TEST-pt.uminho.esr.ott.streaming.StreamPacketTest.xml).
- mvn -o -B -ntp -f Projeto/pom.xml test: sandbox, exit1 por sockets bloqueados. [Log negativo](regression-01.log), [resultado](regression-01-result.json), XML em regression-01-xml/.
- Mesmo comando fora da sandbox: exit0, **1537/0/0/0,22 XML**, incluindo78 casos U01–U08. [Log](regression-02.log), [resultado/inventário](regression-02-result.json), XML em regression-02-xml/.

Versão final não mudou após regressão: [hashes](code-under-test.json).
Maven test, não clean verify; sem JAR final/manual/revisão humana presumidos.
Os logs/XML anteriores permanecem intactos, incluindo o negativo74 asserções do
primeiro checkpoint. Nenhum teste histórico alterado ou enfraquecido.

## Oráculos U01–U08 / V-019

- U01:9 casos, golden encode/decode e integridade das16 referências.
- U02:12 casos negativos independentes.
- U03:4 casos offsets/bits UUID/caps/contadores.
- U04:11 casos fronteiras/recombinação, máximo1847 fragmentos e último96.
- U05:1 caso1137, dois datagramas golden independentes.
- U06:26 casos metadados hostis/invariantes/nulos/payload exato.
- U07:11 casos regiões/overflow/cap, buffer inteiro versus região exata.
- U08:4 casos cópias defensivas e lista imutável.

[Revisão de validação antes de alocar](review.md), [preservação](preservation.json),
[diff Java](java.diff), [numstat Java](java.numstat),
[diff acumulado](cumulative.diff), [numstat acumulado](cumulative.numstat).
[V-ADD](v-add.md). target/ gerado separado em target.numstat; não é entrega.
Manifesto do checkpoint em manifest.json; não constitui autorização de integração.

Main/HEAD b6de848 preservados, ramo imp-016-udp, IDs locais e demo intacta.
Sem staging/commits/push ou avanço Test. Pendentes: aprovação Implement,
Test/clean verify/JAR,manual humano,revisão final Denji e integração.
Sem gates externos adicionais, sockets de streaming ou alegação de vídeo entregue.
