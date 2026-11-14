---
unit: "003-rotas"
bolt: "006-rotas"
stage: "test"
status: "complete"
created: "2026-11-14T15:23:45Z"
---
# IMP-009 — Test/manual humano aprovados e fecho após integração

✅ Test e manual humano de Luis aprovados explicitamente; registo 2026-11-14T15:35:23Z.
Revisão final atribuída a Denji confirmada concluída pelo utilizador. Fecho e
commit para main autorizados. Integração posteriormente comprovada em main b7d74be5d9bad9fe7f598f29fa17188037647cf2;
D32 cumprido no fecho. Estados anteriores de pendência são histórico.
Sem pedido de push ou outro IMP. Gates externos adicionais: nenhum.

## 1. Versão e preservação

Base164ac0dc477c02bc7fde816c761d137ee18ea55d, Denji. [Entrada/arquivo de target](../../../experiments/construction/006-rotas/test-build-01/entry.json)
e [aprovação de Implement](../../../experiments/construction/006-rotas/implement-approval.json).
As237 entradas não geradas de Projeto/ mantêm hashes antes/depois de build e
manual. Nenhuma alteração Java/teste/POM/configuração/fixture nesta etapa.
As964 entradas anteriores de target foram arquivadas antes de clean; log/XML
novos e JAR ficam fora de target para preservar a evidência. Target gerado não
é fonte aprovada nem conteúdo para commit. Evidências anteriores preservadas.

## 2. Clean verify com JDK21

Comando real em Projeto/: `mvn -B -ntp clean verify`, JAVA_HOME/PATH para
/usr/lib/jvm/java-21-openjdk-amd64. Execução autorizada fora da sandbox por
necessidade de TCP loopback já comprovada, sem repetir falha conhecida.

- Início: 2026-11-14T15:09:08.688770+00:00; fim: 2026-11-14T15:09:27.059546+00:00.
- Exit0; **1256 testes,0 falhas,0 erros,0 skips**,16 XML Surefire.
- 39 RoutingServiceTest+6 OverlayNodeRoutingTest novos;1211 anteriores intactos.
- Provider real: org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.
- [Resultado/versão](../../../experiments/construction/006-rotas/test-build-01/result.json),
  [log](../../../experiments/construction/006-rotas/test-build-01/maven.log),
  [ambiente](../../../experiments/construction/006-rotas/test-build-01/environment.json),
  [Surefire](../../../experiments/construction/006-rotas/test-build-01/surefire/).

JAR **novo**,411736 bytes: [ott-streaming-1.0-SNAPSHOT.jar](../../../experiments/construction/006-rotas/test-build-01/ott-streaming-1.0-SNAPSHOT.jar).
SHA-256:

```text
1a1beb8b849ccf505e4266dda199fe439f93e2a54e2297058a2d43eb2265af93
```

[Auditoria](../../../experiments/construction/006-rotas/test-build-01/jar-audit.json):
228 classes, major65/Java21, sem classes de teste. Inclui RoutingService,
OverlayNode e StreamInfo/StreamCatalog novos. JAR copiado do build, diferente do
histórico IMP-008; é o artefacto fixo carregado no manual, não target mutável.
Não há JaCoCo/coverage/linter/CI novos (ADR-026).

## 3. V-012 e D01–D32

O [mapa do walkthrough](implementation-walkthrough.md) conserva os32 IDs e
liga D01–D31 aos métodos atuais. Os nomes/casos da regressão anterior e os dois
testes de correção constam dos XML novos, sem reutilizar contagens de XML antigos.
V-012 exercita losango/ciclo, segunda chegada melhor, quatro streams, retirada
por origem e estabilização por geração com publicidade periódica mantida.

D08 comprova relógio avançado8s entre dois envios no mesmo turno: só o primeiro
ANNOUNCE sai; custo/path/seq/sessão e TTL são revalidados antes da porta. D22
preserva metadados enquanto há candidato vivo, sem reapresentar custo/upstream
expirados com timestamp novo; projeção pura e snapshots anteriores imutáveis.

D29/D31 incluem testes determinísticos e inspeção estática dos limites, sem
alegar alocação de todos os máximos ou ensaio exaustivo de heap/performance.
Limitações de flooding em sobrecarga e espera por geração nova são do Design
aprovado, não uma garantia de disponibilidade contínua do catálogo.

| Obrigação D32 | Estado/evidência |
|---|---|
| V-012/D01–D31 e regressão atual | ✅clean verify1256/0/0/0,16 XML; mapa existente conferido |
| Fontes aprovadas/JAR novo | ✅237 hashes estáveis; major65 e JAR/hash acima |
| Manual técnico contra JAR | ✅Codex,53 CHECK_PASS/MANUAL_PASS/Java0, hashes/origem/JVM/recursos conferidos |
| Repetição humana antes de aprovar Test | ✅Luis, execução20261114T152915Z-8s1k6ruu,53 checks/MANUAL_PASS/Java0 |
| Aprovação de Test | ✅recebida explicitamente do utilizador, incluindo manual humano |
| Revisão final Denji e integração | ✅revisão atribuída confirmada concluída pelo utilizador; ✅integração local em mainb7d74be5d9bad9fe7f598f29fa17188037647cf2 |

D32 está **CUMPRIDO** após a integração comprovada e autorização de fecho.
Bolt/história/TASK-012 completos; unidade/fase continuam em progresso. As
pendências no relato histórico de preparação não são o estado atual.

## 4. Manual reproduzível e observação corrigida

[Roteiro humano e comando](../../../experiments/construction/006-rotas/manual-instructions.md)
e [manifesto congelado](../../../experiments/construction/006-rotas/test-manual-manifest.json).
Sete etapas,53 verificações: grafos com entradas controladas, TTL/visibleAt e
metadados, depois cinco OverlayNode normais/dois servidores/quatro streams com
TCP real. Bootstrap parado sem trocar as sessões físicas, fonteS1 encerrada sem
eliminar streams deS2 e shutdown limitado/portas reutilizáveis. Helper usa apenas
APIs públicas do JAR; não é nova CLI de produto, GUI, CORE ou vídeo.

Ensaio final: 2026-11-14T15:19:24.620724+00:00–2026-11-14T15:19:30.032592+00:00, Codex/kind=agent.
[Report](../../../experiments/construction/006-rotas/test-manual-runs/20261114T151924Z-_00ejxkk/report.json),
[log](../../../experiments/construction/006-rotas/test-manual-runs/20261114T151924Z-_00ejxkk/manual.log):
53 CHECK_PASS,MANUAL_PASS,Java0,passed=true, origem do JAR e hashes corretos,
JVM terminada/no_resources_pending=true. Não substitui execução humana.

[Primeiro ensaio falhado](../../../experiments/construction/006-rotas/test-manual-runs/20261114T151721Z-4t1kaj5v/report.json)
preservado:45 checks/Java1, falha46 após seq nova com catálogo transitório parcial.
O auxiliar esperava só a seq e lia outro snapshot para o catálogo; a
[correção de observação](../../../experiments/construction/006-rotas/test-manual-observation-fix.json)
passou a esperar conjuntamente quatro streams/seq nova e a capturar essa vista.
Mesmas53 asserções e prazo, sem mudar fonte/JAR. Na saída deS1 também se usa
snapshot capturado. Não inferir daí disponibilidade contínua, PCAP sem tráfego
ou ausência de TIME_WAIT; só os estados/intervalos observados.

## 5. Checkpoint

✅ Model/Design/ADR/Implement concluídos. ⏳ Test aguarda repetição humana antes de
aprovação. O comando do roteiro cria um diretório novo e report.json com operador/
tipo declarados; confirmação humana será registada apenas depois de recebida.
Não atribuir este ensaio Codex a Luis. Revisão final Denji e integração continuam
obrigatórias. Sem autorização de fecho, commits, push ou início de outro IMP.


## 6. Verificações documentais e diff

[V-ADD](../../../experiments/construction/006-rotas/test-build-01/v-add.md): zero erros/78 fixtures;
`git diff --check` sem erros. [Resumo e preservação de hashes](../../../experiments/construction/006-rotas/test-build-01/test-summary.json).
[Diff documental de Test](../../../experiments/construction/006-rotas/test-build-01/documents.diff);
[fonte acumulada contra a base](../../../experiments/construction/006-rotas/test-build-01/cumulative-source.diff):
+1080/−8 nos seis caminhos aprovados. Nenhuma linha Java alterada nesta etapa.
[Listas target geradas](../../../experiments/construction/006-rotas/test-build-01/target-generated.numstat)
separadas da fonte. Não há staging/commit/push ou integração por este relatório.

## Aprovação humana e preparação de integração

Registo 2026-11-14T15:35:23Z. Luis executou o manual em2026-11-14T15:29:15.896098+00:00–
2026-11-14T15:29:30.657284+00:00;53 CHECK_PASS,MANUAL_PASS,Java0,passed=true,
hashes/origem corretos,JVM terminada/no_resources_pending=true.
[Report original](../../../experiments/construction/006-rotas/test-manual-runs/20261114T152915Z-8s1k6ruu/report.json),
SHA256f3b3d2102fe887f42cda588663ade110c2b97f6fc436ddb9c90d5c0188671005.
[Log humano](../../../experiments/construction/006-rotas/test-manual-runs/20261114T152915Z-8s1k6ruu/manual.log)
separado do ensaio Codex. JAR411736bytes/SHA256
1a1beb8b849ccf505e4266dda199fe439f93e2a54e2297058a2d43eb2265af93.
[Aceitação](../../../experiments/construction/006-rotas/closure/test-acceptance.json)
e [revisão final confirmada](../../../experiments/construction/006-rotas/closure/final-review-confirmation.json).
A revisão atribuída a Denji foi confirmada pelo utilizador, sem alegar contacto
independente. Integração local em main será comprovada antes de DONE; este
registo não inventa hash ou integração futura. Não repetir build/manual sem
alteração das fontes aprovadas. Sem push ou início de outro IMP.

## Integração e fecho efetivos

Registo 2026-11-14T15:43:38Z. Main contém a versão revista b7d74be5d9bad9fe7f598f29fa17188037647cf2.
[Prova de integração](../../../experiments/construction/006-rotas/closure/integration-proof.json)
e [V-ADD do commit](../../../experiments/construction/006-rotas/closure/integration-commit-v-add.log):
268 caminhos iguais à allowlist,267 hashes do pacote e237 de Projeto/ conferidos;
JAR/report humano iguais ao Test. Revisão final Denji confirmada pelo utilizador.
D32 cumprido; [fecho](closure-walkthrough.md) autorizado. Sem push solicitado ou
novo IMP. Nenhum build/manual repetido sem alteração de fonte.
