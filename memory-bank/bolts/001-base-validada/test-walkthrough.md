# IMP-001 — Checkpoint 4: validação executada

Registo preparado em 2026-11-03T21:32:04Z, após aprovação explícita do
checkpoint 3, registada em 2026-11-03T21:22:32Z. O utilizador aprovou o checkpoint 4
e os resultados apresentados, com registo em 2026-11-03T21:40:56Z, autorizando
apenas o checkpoint 5. Test concluído; o [fecho](closure-walkthrough.md) foi depois
aprovado e aplicado em 2026-11-03T21:56:48Z, após integração efetiva em main.
Os resultados abaixo são da execução do
checkpoint 4; não foi necessário repetir Maven ou o teste manual para editar documentação.

## Resultado e ambiente

V-001–005/V-ADD executados no âmbito da base herdada, sem alterar POM, fontes,
configuração, fixtures ou testes existentes. A regressão nova terminou com
**149 testes, zero falhas, zero erros e zero skips**. O teste manual proposto
terminou com **22 verificações aprovadas**. Nenhuma lacuna observada exige
alterar fundações/testes ou substituir uma ADR.

Ramo Denji; HEAD 4fe6791178828e72ea48c71d46146f9c8cd4c6f9. Linux, Maven 3.6.3,
JDK 21.0.12.1 em /usr/lib/jvm/java-21-openjdk-amd64. JAVA_HOME foi selecionado
para os comandos Maven; o JDK por omissão do ambiente não foi alterado.
Os metadados dos comandos registam início/fim reais e códigos de saída.

| Oráculo | Resultado atual | Evidência |
|---|---|---|
| V-001 | POM efetivo: release 21, Surefire 3.5.4, failIfNoTests=true; provider JUnitPlatformProvider e engine Jupiter 5.13.4 observados; clean verify aprovado | [POM](../../../experiments/construction/001-base-validada/effective-pom.xml), [resumo Surefire](../../../experiments/construction/001-base-validada/surefire-summary.json) |
| V-002 | 45 fixtures conferidas por tamanho/SHA-256/binário/hex; revisão independente de offsets e identidades; proveniência do recurso consultada na evidência histórica | [Auditoria](../../../experiments/construction/001-base-validada/fixture-audit.json), [revisão histórica TASK-002](../../../experiments/task-002-contract-review.md) |
| V-003 | ConfigurationLoaderTest: 65 testes aprovados; descoberta real e casos do mapa do checkpoint 2 | [XML de configuração](../../../experiments/construction/001-base-validada/surefire-local-tcp/TEST-pt.uminho.esr.ott.config.ConfigurationLoaderTest.xml) |
| V-004 | MessageCodecTest: 66 testes aprovados; golden bytes, 16 tipos e rejeições existentes preservados | [XML do codec](../../../experiments/construction/001-base-validada/surefire-local-tcp/TEST-pt.uminho.esr.ott.protocol.MessageCodecTest.xml) |
| V-005 | TransportTest: 18 testes aprovados com TCP loopback real; concorrência, reconexão, saturação e shutdown existentes | [XML de transporte](../../../experiments/construction/001-base-validada/surefire-local-tcp/TEST-pt.uminho.esr.ott.transport.TransportTest.xml) |
| V-ADD | Validador documental/snapshot aprovado: zero erros | [Relatório documental](../../../experiments/construction/001-base-validada/planning-validation.md) |
| Manual | Configuração/codec/TCP/shutdown: 22 CHECK_PASS e MANUAL_PASS | [Log](../../../experiments/construction/001-base-validada/manual-smoke-default-engine.log) |

## Comandos e regressão nova

A partir de Projeto/, com JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64:

```sh
mvn -B -ntp org.apache.maven.plugins:maven-help-plugin:3.5.1:effective-pom -Doutput=/home/luis/ESR/ESR---TP2/experiments/construction/001-base-validada/effective-pom.xml
mvn -B -ntp clean verify
```

O help plugin já estava em cache; foi usado para inspeção, sem mudar o POM.
O POM efetivo contém somente junit-jupiter 5.13.4 com scope test como dependência
direta; nenhuma dependência runtime nova foi introduzida. O provider aparece
no log Maven; o engine e Java 21 constam do classpath/propriedades dos XML novos.

| Suite | Testes | Falhas | Erros | Skips |
|---|---:|---:|---:|---:|
| ConfigurationLoaderTest | 65 | 0 | 0 | 0 |
| MessageCodecTest | 66 | 0 | 0 | 0 |
| TransportTest | 18 | 0 | 0 | 0 |
| Total atual | 149 | 0 | 0 | 0 |

A execução aprovada ocorreu em 2026-11-03T21:24:54Z–21:24:58Z; código 0,
BUILD SUCCESS. [Log completo novo](../../../experiments/construction/001-base-validada/clean-verify-local-tcp.log)
e [comando/ambiente](../../../experiments/construction/001-base-validada/clean-verify-local-tcp-command.json).
Os XML/txt dessa execução foram copiados para surefire-local-tcp antes de
qualquer outra operação de build. O resumo regista hashes dos XML/POM/JAR.
O total 149 desta tabela foi contado nos relatórios atuais, independentemente
do total histórico coincidir. Classes de teste futuras vazias não contam.

A primeira tentativa, em 2026-11-03T21:23:56Z–21:23:59Z, terminou com código 1:
149 testes, zero falhas, 18 erros de transporte por SocketException/Operação
não permitida na sandbox. O [log inicial](../../../experiments/construction/001-base-validada/clean-verify.log)
e os relatórios em surefire-sandbox foram preservados. O mesmo clean verify
foi repetido fora da sandbox após autorização da execução com TCP local.
Não foi necessário corrigir código, enfraquecer testes ou trocar TCP por mocks.

## Fixtures e coerência documental

A [auditoria independente](../../../experiments/construction/001-base-validada/fixture-audit.py)
usa apenas Python standard library e leitura direta, sem recorrer ao codec
Java para construir os bytes esperados. Resultado em 2026-11-03T21:27:23Z:
17 TCP aceites, 12 TCP rejeitados, 4 UDP aceites e 12 UDP rejeitados; 45 casos.
Confere tamanhos, SHA-256 e equivalência .bin/.hex, envelope e códigos ativos
1, 2, 4–17, sete amostras de offsets HELLO_OK/HELLO/REGISTER_OK/ANNOUNCE/
ANNOUNCE UTF-8/ACTIVATE/ERROR, instância zero na resposta bootstrap e ausência
de lease. As observações dos casos inválidos e os campos UDP constam do JSON.
Confere cabeçalho UDP 64, datagrama até 1200, payload até 1136 e reconstrução
do payload sintético de 1137 bytes. Isto não é execução de um codec UDP/vídeo.

A primeira versão do auxiliar assumia path.count no offset 100 em todos os
ANNOUNCE. A fixture inválida usa senderId N32, com três bytes, e o campo está
em 101. O auxiliar foi corrigido para derivar offsets das strings/envelope;
fixtures e código Java permaneceram intactos. O JSON regista esta correção.

V-002 consulta a evidência de acesso/hash do recurso docente registada na
revisão TASK-002. Não foi repetido o download, não se afirma disponibilidade
atual dos URLs nem preparação/reprodução dos quatro conteúdos. A correspondência
dos requisitos e identidades está no mapa aprovado e na validação V-ADD.

Comando documental, a partir da raiz:

```sh
python3 scripts/validate-planning.py --check-snapshot --report experiments/construction/001-base-validada/planning-validation.md
```

São conferidos 40 requisitos/constraints/intents, 35 aliases, 26 ADR, matriz
40/40, 7 unidades, 33 histórias/IMP/bolts, 32 TASK pai, DAG/gates, 45 fixtures
e 220 hashes de fontes. Os três XML CORE são analisados, sem executar CORE.
O relatório contém a hora e a contagem de referências da última execução.
O texto genérico do validador sobre 149 históricos refere-se ao alcance desse
script; os resultados Java atuais estão nos relatórios Surefire acima.

## Teste manual executado

Após o build, o roteiro aprovado foi executado pelo agente em JShell/JDK 21,
com TCP loopback real, callbacks que publicam em filas limitadas e esperas
com prazo. [Roteiro executável](../../../experiments/construction/001-base-validada/manual-smoke.jsh)
e [metadados](../../../experiments/construction/001-base-validada/manual-smoke-default-engine-command.json).

```sh
/usr/lib/jvm/java-21-openjdk-amd64/bin/jshell --class-path target/classes /home/luis/ESR/ESR---TP2/experiments/construction/001-base-validada/manual-smoke.jsh
```

Execução 2026-11-03T21:29:42Z–21:29:44Z, código 0, 22 CHECK_PASS:

- Cinco perfis reais, duas origens, quatro UUID de streams distintos e vizinhos
  S1=S2,R1, validados antes de abrir sockets.
- Fixture HELLO independente de 77 bytes: campos/requestId/endpoint e bytes
  reencodificados iguais; 16 tipos ativos e código 3 rejeitado.
- Duas ligações abertas; PROBE_PING e PROBE_PONG efetivamente recebidos com
  requestId comum e probeId 42, além da aceitação de envio.
- Eventos de fecho únicos, terminação dos dois transportes, zero reservas,
  nenhuma fila de callbacks saturada e reutilização da porta de escuta num
  terceiro transporte também terminado.

A tentativa anterior com --execution local falhou antes dos checks ao resolver
a classe Connection; saída e comando preservados em manual-smoke.log e
manual-smoke-command.json. O engine padrão executou o mesmo roteiro com êxito.
Trata-se de execução operacional assistida das APIs, não de revisão humana
independente, handshake overlay, cálculo de RTT, CLI, GUI, vídeo ou CORE.

### Execução pelo utilizador — Luis

Luis executou o roteiro e forneceu /tmp/ott-imp001-manual.ijH5h9.log, cuja
preservação e atribuição autorizou explicitamente no pedido de fecho/integração.
O [log preservado](../../../experiments/construction/001-base-validada/manual-smoke-luis.log)
regista início em 2026-11-03T21:42:26.035183806Z e MANUAL_PASS em
2026-11-03T21:42:26.165152629Z, com 22 CHECK_PASS e nenhum MANUAL_FAIL.
Os [metadados](../../../experiments/construction/001-base-validada/manual-smoke-luis.json)
guardam o SHA-256, origem, hora real de preservação e operador Luis. O ficheiro
original em /tmp foi mantido; a cópia é byte a byte igual. Não se inventa código
de saída ou comando que o log não regista. Esta prova distingue a execução pelo
utilizador da execução anterior pelo agente e conserva os mesmos limites GUI/CORE.

## Preservação, diff e paragem

POM, código, recursos e testes não foram editados. Dos 163 ficheiros rastreados
de Projeto/ da baseline, 162 mantêm os hashes; a única diferença é o ficheiro
gerado target/maven-status/maven-compiler-plugin/compile/default-compile/createdFiles.lst.
O clean verify reordenou entradas e acrescentou seis classes de transporte já
existentes nas fontes; não se trata de uma alteração das fundações.
Este resultado gerado fica visível no diff, sem reposição artificial da lista
antiga. Evidência de comparação em checkpoint-4-preservation.json.

As alterações de Inception continuam separadas na baseline/pre-existing.diff.
O diff documental incremental checkpoint-4.diff compara com o checkpoint 3
aprovado; bolt-through-checkpoint-4.diff compara com o arranque do bolt.
O diff gerado de Projeto/ é guardado separadamente em project-generated.diff;
os dois auxiliares novos de auditoria/manual são evidência fora de Projeto/.
Nos checkpoints 1–4 não foram feitos commits/push nem criados branches/deploys/uploads.
Os commits e integração posteriores foram explicitamente autorizados e estão no
relatório final do checkpoint 5.

O ponto de paragem do checkpoint 4 foi respeitado e os resultados foram depois
aprovados explicitamente pelo utilizador. Test e os critérios técnicos do bolt
estão concluídos. O utilizador aprovou também o fecho; a versão revista foi
integrada em main e a cascata aplicada em 2026-11-03T21:56:48Z. Bolt, história
e unidade complete, com provas reais no relatório do checkpoint 5.
