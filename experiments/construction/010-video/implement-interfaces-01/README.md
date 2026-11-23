# IMP-016 — Primeiro passo Implement

Plan aprovado; apenas tipos e testes vermelhos autorizados. Comportamentos pendentes.

JDK/ambiente: [environment.txt](environment.txt).
Comandos em Projeto/, com JDK21 e Maven offline:

- mvn -o -B -ntp -Dtest=StreamPacketTest test — exit1,76/74/0/0.
- mvn -o -B -ntp -Dtest=MessageCodecTest,DiscoveryCodecTest test — exit0,120/0/0/0.

[Negativo](red-01.log), [XML](red-01.xml), [resultado](red-01-result.json),
[causas das74 falhas de asserção](expected-failures.json).
Dois verdes apenas referências/caps; não provam codec.
[Históricos](historical-codecs.log), [resultado](historical-codecs-result.json),
[MessageCodec XML](historical-MessageCodecTest.xml), [Discovery XML](historical-DiscoveryCodecTest.xml).
Não é regressão integral, clean verify, JAR, manual ou V-019 aprovado.

[Preservação/hash](preservation.json), [Java diff](java.diff),
[Java numstat](java.numstat), [target separado](target.numstat).
[V-ADD](v-add.md); validador inalterado. IDs locais; LOCAL-02 resolvida no Plan.
Main e HEAD preservados, demo/artefactos e negativos anteriores intactos.
Sem staging/commits/push/Test. Fecho exige V-019,regressão,manual humano,
revisão final Denji e integração; nenhum gate externo adicional.
