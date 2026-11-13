---
unit: "003-rotas"
bolt: "021-rotas"
stage: "test"
status: "complete"
created: "2026-11-13T17:42:46Z"
---
# IMP-008 — Test técnico executado; repetição humana pendente

✅ Test e manual humano de Luis aprovados explicitamente pelo utilizador.
Registo 2026-11-13T17:59:03Z. Cinco etapas concluídas; revisão final por Luis confirmada,
substituindo Waheed. D22 cumprido após integração em main 3b7aa745cac62b71ac8ce7e036656bf7ab21692f;
bolt/história complete e TASK-011 DONE no fecho autorizado. Unidade/PHASE-03
continuam em progresso. Outro IMP não iniciado.

## 1. Versão, autorização e preservação

Base HEAD/main/Denji cc952c5f558930f2c67e7dea8fe92612cd8cc705. Os seis hashes
aprovados em Implement conferidos antes/depois do build e do manual; nenhum
fonte Java ou teste de Projeto alterado nesta etapa.
[Autorização](../../../experiments/construction/021-rotas/implement-approval.json)
e [entrada](../../../experiments/construction/021-rotas/test-entry.json).

Havia alterações documentais preexistentes de EXTRA-02 em specs/docs/bolt014 e
em documentos partilhados; preservadas e separadas do diff deste checkpoint.
A prévia árvore target (708 ficheiros) foi arquivada com hashes antes de clean;
resultados históricos de Implement não foram sobrescritos. Target novo fica em
inventário separado e não é conteúdo aprovado para commit.

## 2. Clean verify real com JDK21

Comando em Projeto: `mvn -B -ntp clean verify`, JAVA_HOME/PATH apontados para
/usr/lib/jvm/java-21-openjdk-amd64. Execução fora da sandbox com autorização para
TCP local, pois o bloqueio de sockets já foi comprovado em Implement. Não foi
criada uma falha de sandbox nova para repetir essa limitação conhecida.

- Início:2026-11-13T17:33:26.358693+00:00; fim:2026-11-13T17:33:45.053337+00:00.
- Saída0, **1211 testes/0falhas/0erros/0ignorados**,14 XML Surefire.
- 35 testes RoutingTableTest+1176 anteriores, com os mesmos fontes aprovados.
- Provider real: org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.
- [Resultado e comandos](../../../experiments/construction/021-rotas/test-build-01/result.json),
  [log](../../../experiments/construction/021-rotas/test-build-01/maven.log),
  [ambiente](../../../experiments/construction/021-rotas/test-build-01/environment.json),
  [Surefire](../../../experiments/construction/021-rotas/test-build-01/surefire/).

JAR **novo**: [ott-streaming-1.0-SNAPSHOT.jar](../../../experiments/construction/021-rotas/test-build-01/ott-streaming-1.0-SNAPSHOT.jar),
364704 bytes. SHA-256:

```text
366210cf8c7656ad2948ae1c842796632085aaa4f09afcedcd00a49db0a0ecee
```

A cópia de evidência está fora de target e é o JAR usado pelo manual. Auditoria
confirma os cinco tipos principais de routing com class major65 (Java21) e sem
RoutingTableTest empacotado. Não é o JAR anterior nem uma alegação de integração.
[Auditoria](../../../experiments/construction/021-rotas/test-build-01/jar-audit.json).

## 3. V-011 e D01–D22

Os35 nomes/mapeamento de D01–D21 preservados em
[implement-oracles.json](../../../experiments/construction/021-rotas/implement-oracles.json)
foram conferidos nos XML novos, não contados por presença de relatório antigo.
Custos e desempate usam esperados literais; quatro streams isoladas, alternativas,
paths, seq/TTL, epoch, saturação, sessão, overflow e snapshots exercitados. D20
confere leitura única do relógio, fronteiras, ticks negativos/wrap e regressão.
D21 testa limite de revisão no helper e rejeições transacionais na API; não se
alegam bilhões de revisões nem alocação máxima de um milhão de slots.

| Obrigação D22 | Estado/evidência real |
|---|---|
| Mapa V-011/D01–D21 e regressão nova | ✅35 testes de domínio+1176 anteriores;14 XML, clean verify0 |
| JAR identificado e fontes aprovados | ✅Hash acima e conferência antes/depois; sem Java alterado |
| Manual técnico offline | ✅Codex,43 CHECK_PASS/MANUAL_PASS/Java0, mesmo JAR |
| Repetição humana antes de aprovar Test | ✅Luis,43 checks/MANUAL_PASS/Java0, relatório original preservado |
| Aprovação de Test | ✅Recebida explicitamente do utilizador |
| Revisão humana final e integração | ✅Revisão final Luis confirmada, substituindo Waheed; ✅integração em main3b7aa74 comprovada |

D22 está **CUMPRIDO** após integração comprovada. V-011/Test, manual humano e
revisão final aceites; bolt/história fechados, sem promover requisito global/fase.
Sem gates externos adicionais.

## 4. Manual offline reproduzível

[Roteiro/comando para Luis](../../../experiments/construction/021-rotas/manual-instructions.md),
[manifesto](../../../experiments/construction/021-rotas/test-manual-manifest.json).
Auxiliares fora de Projeto (evidência autorizada, não composição do produto):
ManualRouting.java e run-manual.py, congelados por hash. Compilam exclusivamente
contra o JAR fixo, sem JUnit, rede, sockets ou relógio civil para expiração.
O modo humano pede Enter em oito etapas e cria diretório novo com operador e logs.

Execução técnica real em
[test-manual-runs/20261113T173843Z-92dfcjfm](../../../experiments/construction/021-rotas/test-manual-runs/20261113T173843Z-92dfcjfm/report.json):
operator_declared=Codex/kind=agent,43 CHECK_PASS, MANUAL_PASS, compile_exit0,
JAVA_EXIT0, passed=true, frozen_hashes_match=true, code_source_matches_jar=true e
java_process_terminated=true. É só a JVM do ensaio que foi verificada terminada;
não se presume o estado de processos alheios. Não há identidade humana inventada.

Execução humana real de Luis:
[report.json](../../../experiments/construction/021-rotas/test-manual-runs/20261113T174838Z-y23kpe_b/report.json)
e [manual.log](../../../experiments/construction/021-rotas/test-manual-runs/20261113T174838Z-y23kpe_b/manual.log).
Início2026-11-13T17:48:38.560557+00:00/fim17:48:51.830308+00:00;43 CHECK_PASS,
MANUAL_PASS,compile0,Java0,passed=true,hashes/origem JAR corretos,JVM terminada.
Operador confirmado pelo utilizador; o original conserva o campo do logger que
não verifica identidade independentemente, sem o falsificar. Relatório SHA256
47b712bb518ddc2fb5f975f8af218fd9b76987cfd174d5a3c238bcc5ea11a9d1.
[Test aceite](../../../experiments/construction/021-rotas/test-acceptance.json) e
[revisão final Luis](../../../experiments/construction/021-rotas/luis-final-review-confirmation.json).
Não é cópia/renomeação do ensaio Codex, que permanece separado.

## 5. Validação documental e preservação

[V-ADD desta etapa](../../../experiments/construction/021-rotas/test-v-add.md):
saída0, zero erros e78 fixtures verificadas; não executa CORE nem substitui a
repetição humana. git diff --check passou; fontes e evidências anteriores
conferidos por SHA256. Nenhum limite ou contrato operacional mudou nesta etapa.

## 6. Alcance, diff e checkpoint

Não há alteração da produção ou dos testes aprovados em Implement. Diff de
Projeto/src contra a base permanece **+851/−0 em seis caminhos**; delta de fonte
neste Test é zero. Documentação/auxiliares/evidência deste checkpoint são separados
do estado preexistente e do target recompilado. Nenhum teste existente enfraquecido.
O enunciado/bytes/UDP/TCP/caps e a separação seleção/autorização mantêm-se.

Este é um domínio offline: não demonstra catálogo de rede, routing integrado,
ativação, vídeo, GUI, CORE, sincronização horária entre máquinas ou entrega. Custos
e tempo do manual são controlados; não são medições de rede. Sem dependências
runtime novas, JaCoCo, limiar de cobertura, CI ou linter (ADR-026).

1 - ✅ Manual humano/Test/revisão final Luis aceites.
2 - ✅ Integração e prova concluídas; fecho documental autorizado aplicado.

Fecho/commits/integração/push main autorizados pelo utilizador. Este relatório
não os presume executados. Registos de pendências anteriores são históricos;
cinco etapas concluídas, bolt ainda aberto até integração. Nenhum outro IMP iniciado.


## IMP-008 / 021-rotas / TASK-011 — fecho após integração comprovada

Registo 2026-11-13T18:09:37Z. Versão revista integrada em main: 3b7aa745cac62b71ac8ce7e036656bf7ab21692f;192 caminhos/hashes
iguais ao pacote, V-ADD0/78 numa cópia extraída do commit. Fontes/JAR e relatório
humano Luis conferidos no commit, sem overlay do diretório de trabalho.
Test1211/0/0/0,JDK21/14XML; manual humano Luis43 CHECK_PASS/MANUAL_PASS/Java0/
passed=true/JVM terminada aceites. Revisão final Luis confirmada pelo utilizador,
substituindo Waheed. D22 cumprido; fecho explicitamente autorizado.
Bolt/história complete, implemented=true, TASK-011 DONE. Dez bolts complete,
25 planned, nenhum em execução. Unidade003-rotas/PHASE-03 continuam em progresso;
006-rotas apenas desimpedido, sem início ou autorização para outro IMP.
Código/JAR/logs originais preservados, sem repetição de build/manual. EXTRA-02
preexistente e target gerado fora dos commits. Publicação main autorizada,
resultado só será declarado após push efetivo; nenhum force push.
