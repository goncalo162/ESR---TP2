# IMP-004 — Test preparado para checkpoint

**Resultado técnico:** `mvn -B -ntp clean verify` passou com **903 testes**, zero
falhas, erros ou ignorados. O agente executou o manual interativo sobre o JAR
novo: **49 CHECK_PASS**, MANUAL_PASS, CLEANUP_COMPLETE e saída Java **0**.
**V-ADD passou após a correção autorizada de Q-C-08, agora resolvida.** Test foi aprovado explicitamente pelo utilizador;
bolt/TASK/história/unidade continuam em progresso até revisão final e integração. Não houve fecho/commit/push.

## Autorização, versão e preservação

O utilizador aprovou Implement e autorizou apenas Test, com registo no bolt em
2026-11-10T16:17:06Z. Autoria/operador desta execução: agente Codex. Ramo real
`main`; HEAD `06dc037` contém a base documental e a alteração externa anterior.
A versão executada inclui as alterações locais aprovadas, identificadas pelos
hashes; o commit HEAD isolado não identifica esse conteúdo.

A [auditoria de Test](../../../experiments/construction/003-overlay/test-preservation.json)
confirma **155 ficheiros de Projeto/ e 67 ficheiros de ferramentas intactos**
contra os hashes congelados na entrada. `Projeto/target/` é saída gerada e não
entra nessa contagem. Não foram acrescentados testes ou alterado código aprovado
nesta etapa. ManualNeighborSmoke.java é driver de evidência fora de Projeto/;
não faz parte do JAR, da CLI ou do código runtime do produto.

Diferença de fontes em Test contra o checkpoint Implement: **+0/−0**.
Acumulado contra `26aaa4ae6775e9c78e1d7658472ccaca0b376427`:

| Fonte | + | − | Âmbito |
|---|---:|---:|---|
| Neighbor.java | 33 | 0 | IMP-004 |
| NeighborManager.java | 953 | 0 | IMP-004 |
| NeighborManagerTest.java | 759 | 0 | IMP-004 |
| BootstrapServer.java | 482 | 142 | legibilidade externa + reposição autorizada em Implement |

A equivalência estática da correção externa e os seus limites continuam no
[walkthrough Implement](implementation-walkthrough.md#correção-externa--q-c-07).
Nenhuma correção adicional ao bootstrap foi feita em Test.

## Build e regressão

Ambiente efetivo: Temurin **21.0.13**, Maven **3.9.9**, macOS arm64; UTF-8.
[Java](../../../experiments/construction/003-overlay/test-java-version.log) e
[Maven](../../../experiments/construction/003-overlay/test-maven-version.log).
Run ID: `20261110T161706Z`. Comando em Projeto/:

```sh
mvn -B -ntp clean verify
```

[Log novo completo](../../../experiments/construction/003-overlay/test-clean-verify-20261110T161706Z.log),
[XML preservados](../../../experiments/construction/003-overlay/test-artifacts-20261110T161706Z/surefire-reports/)
e [totais extraídos dos XML](../../../experiments/construction/003-overlay/test-build-results.json).
Execução fora da sandbox autorizada para sockets loopback, já bloqueados pela
sandbox em Implement. Não se ignorou ou substituiu nenhum teste TCP.

| Suite | Testes | Falhas | Erros | Ignorados |
|---|---:|---:|---:|---:|
| ConfigurationLoaderTest | 740 | 0 | 0 | 0 |
| MessageCodecTest | 66 | 0 | 0 | 0 |
| TransportTest | 31 | 0 | 0 | 0 |
| BootstrapServerTest | 19 | 0 | 0 | 0 |
| NeighborManagerTest | 47 | 0 | 0 | 0 |
| **Total atual** | **903** | **0** | **0** | **0** |

Os 149/856/903 de etapas anteriores são histórico, não a origem deste resultado.
O total acima provém desta execução clean. A mensagem stderr de saturação de
bootstrap vem do teste negativo existente; a suite termina com zero falhas.
Não houve JaCoCo, CI, linter, limiar percentual ou dependência nova.

## JAR e proveniência

O build criou `Projeto/target/ott-streaming-1.0-SNAPSHOT.jar`. Uma cópia do mesmo
conteúdo está [preservada](../../../experiments/construction/003-overlay/test-artifacts-20261110T161706Z/ott-streaming-1.0-SNAPSHOT.jar).
SHA-256 do original, da cópia e do JAR carregado no manual:

```text
1d56888b8d3db1ca8d12d45621e14783c45bb7f0b7c8b4a0d0945deb77644d17
```

O driver confirmou a origem de NeighborManager no JAR do classpath, sem classes
JUnit ou de target/test-classes. O JAR não é apresentado como release, integração
ou entrega. O hash identifica esta construção local, não uma promessa de builds
binariamente idênticos em futuras execuções.

## V-007 — consolidação técnica

| Critério | Execução real |
|---|---|
| Três nós convergem sem duplicados em ordens diferentes | JUnit: seis permutações e arranque simultâneo; manual: N2/N0/N1, grau dois por nó, IDs esperados e conexões únicas |
| Reinício com instanceId nova e isolamento de eventos antigos | Fakes verificam callbacks da ligação anterior; TCP real e manual confirmam nova instância; disconnect/send com Session retirada não afetam a nova |
| Vizinho desconhecido é rejeitado | JUnit e manual TCP recebem UNAUTHORIZED_NEIGHBOR e EOF; manual verifica também DUPLICATE_ID sem substituir vínculo saudável |
| Reconexão direta sem novas consultas após descoberta | Fake conta dials; TCP JUnit e manual encerram bootstrap e reconectam. Manual confirma Connection refused no bootstrap e connectionId novo com mesma instanceId |
| Endpoints publicados apenas após handshake | Fakes bloqueiam flush/HELLO_OK e verificam ausência de snapshot/Up prematuros; manual observa snapshot vazio antes de start e sessões confirmadas com endpoints |

O reinício usa nova descoberta: no manual, N1 novo não obtém a lista enquanto
bootstrap está parado na janela observada; bootstrap é reposto para esse processo.
Não se afirma arranque de processo novo sem bootstrap. As falhas são induzidas
pela API pública disconnect/close e por sockets locais, sem afirmar falha CORE.

## D01–D22 — evidência e limites

O [índice dos casos realmente executados](../../../experiments/construction/003-overlay/test-oracle-results.json)
foi extraído dos XML, preservando nomes e resultados dos 47 casos de vizinhança.
O Design e as linhas dos oráculos aprovados permanecem intactos; a tabela abaixo
consolida a prova sem os redefinir.

| Oráculo | Casos/evidência executada |
|---|---|
| D01 | d01*: instância zero/ID, owner e endpoint inválidos antes do arranque; manual carrega perfis e verifica ausência de bind nos construtores |
| D02 | d02*: ERROR permanente/transitório 10/11, pedido errado, canal estranho isolado, REGISTER_OK completo antes do EOF |
| D03 | d03*: coerência dos records, lista defensiva, instalação atómica, grau 63/64, cap efetivo e colisões |
| D04 | d10/d21: contador fake e reconexão com bootstrap encerrado; manual distingue processo com lista conservada de processo novo |
| D05 | d05/d21: menor ID e seis ordens + simultâneo; manual confirma grafo completo |
| D06 | d06: host/porta/papel/ID/instância/direção incompatíveis sem DNS do payload; raw HELLO manual rejeitado sem afetar grafo |
| D07 | d07 e d08Inbound*: negativas HELLO_OK/ERROR inesperado, correlação HELLO/HELLO_OK, flush tardio sem Up |
| D08 | d08/d17: duas condições de confirmação, FIFO após Up, retenção até B e limite global R, overflow e libertação |
| D09 | d09: duplicado vivo preserva sessão; manual TCP rejeita segunda instância do ID vivo |
| D10 | d10/d07/d21: contexto/geração antigos não reativam vínculo; manual Session antiga false/CLOSED após reconexão/reinício |
| D11 | d11*: todos os códigos ERROR e relatedType 0/3/17/255 preservados, identidade/envio validados e Received antes de Down no EOF; manual NO_ROUTE/255 íntegro |
| D12 | d12/d14: prazo exato, espera em fila, wrap monotónico, resolução/bind tardios e barreira que não renova prazo |
| D13 | d13: jitter 100% nos extremos, saturação em Integer.MAX_VALUE, clamp 1..max e reset após ACTIVE; manual jitter zero registado |
| D14 | d14/d21: Open antes/depois de resultado, HELLO forjado, barreira de outro dial e deadline; TCP real exercita callbacks concorrentes |
| D15 | d15: dial retirado não cancelado, handle tardio fechado; resolver bloqueável ignora interrupção e conserva reserva até terminar |
| D16 | d16: NODE_LEAVE corrente/repetido produz um Down e mantém aresta desejada |
| D17 | d17/d15: B/R e overflow, timer cancelado em execução mantém reserva, 65 jobs físicos e rejeição do seguinte |
| D18 | d18/d17/d15: fila serial cheia, consumidor que lança exceção e limites físicos de agenda/I/O; cleanup não exige novo slot |
| D19 | d19/d21/d15: fecho em DNS/bind, deadline restante, timeout explícito, owner externo e close idempotente; manual awaits/portas reutilizadas |
| D20 | d20: runtime efetivo/defaults/caps menores, owner emprestado continua utilizável por outro componente |
| D21 | d21*: integração TCP real com BootstrapServer, três nós, desconhecido, desconexão e nova instância; manual no JAR |
| D22 | clean verify/JDK21/XML/JAR/hash e manual do agente executados; aprovação Test/revisão humana/integração ainda pendentes |

A tabela corresponde aos casos críticos existentes, não a uma enumeração exaustiva
de intercalações possíveis. Algumas condições relacionadas partilham o mesmo
caso JUnit; não se infla a contagem por estímulo ou por linha D. Os fakes controlam
clock/agenda/transporte/jitter; não se apresentam como TCP real. Jitter fake usa
zero/extremos e o manual usa 0%; não foi inventada seed para fontes que não usam
um gerador pseudoaleatório reproduzível. Não há benchmark de desempenho, FPS,
vídeo, GUI, CORE ou demonstração da CLI final.

## Manual executado pelo agente

[Roteiro e comandos para repetição pessoal](../../../experiments/construction/003-overlay/manual-instructions.md).
[Driver](../../../experiments/construction/003-overlay/ManualNeighborSmoke.java),
[log original](../../../experiments/construction/003-overlay/manual-codex-20261110T161706Z/manual.log)
e [resultado/proveniência](../../../experiments/construction/003-overlay/test-manual-result.json).

Operador Codex; quatro prompts avançados individualmente via PTY. Intervalo
registado no log: 2026-11-10T16:21:29.316793Z–2026-11-10T16:22:54.328646Z.
49 CHECK_PASS, zero CHECK_FAIL, MANUAL_PASS CHECKS=49, CLEANUP_COMPLETE; processo
Java terminou com código 0. Esse intervalo inclui pausas do operador, não mede
convergência ou recuperação. As duas observações negativas de 200 ms são janelas
do roteiro, não provas de ausência eterna ou metas de latência.

O teardown confirmou os awaits e reutilização das portas TCP antes do sucesso.
Os perfis, portas, UUID e hashes reais estão no diretório do manual. Não há log
pessoal humano nesta continuação; não se atribui a execução do agente a Luis,
Denji ou outro membro. Uma repetição humana deve usar diretório/log próprios.

## Q-C-08 — histórico da falha antes da correção autorizada

Antes da correção autorizada, a execução de `python3 scripts/validate-planning.py` falhava:
[log de Test](../../../experiments/construction/003-overlay/test-planning-validation.log).
Há três erros em exemplos de ferramentas preexistentes:

- fence não fechado em `.specsmd/aidlc/skills/inception/bolt-plan.md`;
- fence não fechado em `.specsmd/aidlc/skills/operations/deploy.md`;
- ligação de exemplo `bolts/001-auth-service/adr-001-jwt-auth.md` inexistente no
  template ddd-construction-bolt.

Não se alteraram os ficheiros nem o alcance do validador. A DAG e contagens
estruturais que ele imprime não transformam a saída 1 em sucesso. Q-C-08 permanece
por resolver; o resultado Maven/manual é separado. O fecho não pode alegar
validação global concluída enquanto esta pendência não tiver tratamento explícito.

## Checkpoint Test

Test preparado, **aguardando validação do utilizador**. Metadados: current_stage=test,
Model/Design/ADR/Implement em stages_completed, sem Test concluído, completed=null.
Revisão final humana atribuída a Denji e integração continuam exigidas antes de
DONE. Sem gates externos adicionais, sem nova ADR, sem commits/push/fecho.

## Checkpoint Test mantido aberto — 2026-11-10T16:29:38Z

O utilizador confirmou que conferiu a evidência técnica, mas manteve Test
por aprovar até executar o manual humano. Não há execução humana registada
por esta confirmação. Checkpoint aberto, current_stage=test, completed=null;
Test não entra em stages_completed. Q-C-08 continua por resolver e V-ADD
continua reprovado. Sem gates externos adicionais; critérios/validações,
revisão humana final e integração continuam exigidos antes do fecho.
Não fechar o bolt nem fazer commits/push.

## Manual humano aprovado — Test ainda aberto

Registo de conferência em 2026-11-10T16:38:44Z.
Manual humano executado e aprovado por Francisco, confirmado pelo utilizador.
Conferência do log original: 49 CHECK_PASS distintos, zero CHECK_FAIL,
MANUAL_PASS CHECKS=49 e CLEANUP_COMPLETE; hash do JAR coincide com o clean verify
e os cinco hashes de configuração coincidem com os ficheiros da execução.
JAVA_EXIT=0 é confirmação explícita do utilizador; esse marcador não consta
do log original do driver, que foi preservado sem edição.
Test continua por aprovar até tratar V-ADD. Q-C-08 continua aberta; correção
mínima preparada e ensaiada apenas em cópia isolada, não aplicada às ferramentas.
Sem fecho/commits/push; revisão humana final e integração continuam pendentes.

[Log Francisco](../../../experiments/construction/003-overlay/manual-Francisco-execucao-01/manual.log) · [Conferência](../../../experiments/construction/003-overlay/manual-Francisco-evidence.json) · [Proposta Q-C-08](../../../experiments/construction/003-overlay/q-c-08-proposal.md).

## Checkpoint Test atualizado — V-ADD passou

Atualizado em 2026-11-10T16:41:30Z. Aplicado exclusivamente o patch Q-C-08 aprovado:
[diff](../../../experiments/construction/003-overlay/q-c-08-applied.diff),
[numstat +5/−3](../../../experiments/construction/003-overlay/q-c-08-applied.diff.numstat).
[Execução real V-ADD](../../../experiments/construction/003-overlay/q-c-08-v-add-applied.log):
saída 0, zero erros. Q-C-08 resolvida, validador e alcance preservados.
As 155 fontes/configurações/testes de Projeto/ continuam intactas desde Implement;
as únicas exceções ao congelamento de ferramentas são as três correções
documentais agora autorizadas. O restante conteúdo local foi preservado por hash.
Clean verify permanece o já executado de 903 testes/zero falhas/erros/skips,
com JAR/hash e XML preservados; não houve nova execução Java nesta correção
exclusivamente documental. Manual humano Francisco cumprido e aprovado: 49 checks,
MANUAL_PASS/CLEANUP_COMPLETE, Java 0 confirmado pelo utilizador.
**Test aguarda aprovação explícita.** Revisão humana final e integração pendentes.
current_stage=test; Test fora de stages_completed; completed=null.
Sem fecho, commits/push ou outro IMP. O histórico anterior de V-ADD reprovado
e manual humano pendente não descreve este estado atual.

## Aprovação de Test e limites da autorização

Test aprovado explicitamente pelo utilizador; registo em 2026-11-10T16:44:29Z.
A aprovação inclui clean verify/903 testes, manual humano de Francisco e V-ADD.
Q-C-08 resolvida. As anteriores esperas por aprovação de Test são históricas.
Autorizada preparação do resumo/diff para revisão humana final; sem commits
ou push. O bolt continua in-progress/completed=null, com as cinco etapas
concluídas e current_stage=null; revisão final e integração ainda pendentes.
[Resumo final para revisão](final-review-summary.md).

## Revisão final Denji aprovada — integração por autorizar

O utilizador confirmou explicitamente que Denji realizou e aprovou a revisão
humana final desta versão, sem alterações pendentes. Confirmação registada em
2026-11-10T17:00:44Z; data de execução da revisão não fornecida e não inventada.
Código permanece idêntico ao aprovado em Test; manual humano de Francisco
já cumprido, sem repetição. Q-C-08 resolvida, Test/V-ADD aprovados.
Falta integração autorizada/comprovada para poder tratar DONE. Sem staging,
commits/push, integração ou fecho nesta autorização.

[Registo da confirmação](../../../experiments/construction/003-overlay/denji-final-review-confirmation.json).
