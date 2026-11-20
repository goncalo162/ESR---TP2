# IMP-013 — Test técnico; repetição humana pendente

[Entrada/aprovação de Implement](entry.json), [clean verify JDK21](clean-verify-result.json)
e [log integral](clean-verify.log):1419/0/0/0,20 XML,40 RecoveryTest+1379 históricos.
[JAR congelado](ott-streaming-1.0-SNAPSHOT.jar),504997bytes,SHA256
81779be388778fd7dfacba5957e74ccffe5da45e86a403ea1ec5626f52c8f49d.
[267 classes](jar-classes-sha256.json) iguais ao clean build.
[Consolidação técnica R01–R18](v016-oracles.json), sem percentagem/gate adicional.

Target anterior está preservado integralmente em target-before-clean.tar.gz.
Clean e ensaio não alteraram fontes aprovadas, históricos ou evidências anteriores.
Os oito JARs em test-libs e as fixtures em frozen-test-classes são artefactos
existentes de teste congelados; não há nova dependência no produto.

[Manual humano](../manual-instructions.md), [manifesto](../test-manual-manifest.json)
e [ensaio Codex](manual-agent-verification.json):7 etapas,28 checks. kind=agent,
JAVA_EXIT0/wrapper0/hashes/origem/JVM conferidos. Não é repetição humana.
Aguardar execução pessoal antes de pedir aprovação de Test.

[Relatório Test](../../../../memory-bank/bolts/008-arvores/ddd-03-test-report.md).
R17 inicial permanece sem causa estabelecida; o sucesso com métricas controladas
não demonstra a causa da falha. Negativos anteriores preservados, nova correção
exige reprodução/âmbito apresentados antes de alterar o código.

Só Model/Design/ADR/Implement concluídos, current_stage=test, Test por aprovar,
completed=null. Manual humano/revisão Fillmore/integração pendentes; sem fecho,
commits/push/outro IMP ou gates externos novos. Relatórios antigos que apontam
para ficheiros vivos registam hashes históricos; as evidências não são reescritas.
