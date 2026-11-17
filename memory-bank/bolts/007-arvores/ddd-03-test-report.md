---
unit: "004-arvores"
bolt: "007-arvores"
stage: "test"
status: "in-progress"
---
# IMP-011 — Test técnico / V-014

Implement aprovado e Test autorizado pelo utilizador. **Test por aprovar.**
Manual humano de Francisco cumprido e conferido, cinco etapas/24 checks, sem
divergências; revisão final de Waheed confirmada pelo utilizador para a versão
atual, sem alterações pendentes. [Verificação e hashes da versão revista](../../../experiments/construction/007-arvores/integration-01/manual-and-review-verification.json).
Integração ainda não autorizada nem executada. Bolt/TASK/unidade/PHASE continuam
em progresso. Sem staging, commits, push ou fecho. As menções anteriores a
manual/revisão pendentes nos artefactos de execução são snapshots históricos.

## Construção e regressão atuais

Base efetiva main `9c5ad094e58313622a5a695e259cd91b15a71a72`. As alterações
aprovadas de Implement permanecem na árvore local, sem commit novo. JDK Temurin
21.0.13+9 / Maven3.9.9 / macOS. Executado em Projeto/ `mvn -B -ntp`, com diretórios
de evidência CLI/trace externos a target, `clean verify`.

[Log completo](../../../experiments/construction/007-arvores/test-02/clean-verify.log),
[comandos](../../../experiments/construction/007-arvores/test-02/commands.json) e
[totais por suite](../../../experiments/construction/007-arvores/test-02/result.json):
**1328 testes, 0 falhas, 0 erros, 0 ignorados, 18 suites, BUILD SUCCESS**.
31 métodos em ActivationTest, incluindo o diagnóstico TCP NeighborDown; os
30 restantes cobrem ativação/contratos. Os XML/TXT de todas as suites foram
copiados para test-02/surefire-reports antes de qualquer outro teste Maven.

A primeira execução restrita completou1328 testes com0 falhas e79 erros,
por sockets `Operation not permitted`. [Log e resultado](../../../experiments/construction/007-arvores/test-01/result.json)
e XML preservados em test-01. Repetição com sockets permitidos, sem mudar
código, testes ou prazos; esta é a construção positiva acima. Negativos históricos
NeighborDown/D11a/M07 e os seus diagnósticos não foram apagados/sobrescritos.

JAR congelado de486734 bytes:
[ott-streaming-1.0-SNAPSHOT.jar](../../../experiments/construction/007-arvores/test-02/ott-streaming-1.0-SNAPSHOT.jar).
SHA-256 `f923f5b766f4ddcc3871c6339710a4a0e87a609aa6cb5ce1e6620c73ccc55314`.
A cópia coincide com o JAR gerado, e todos os classfiles do JAR coincidem com
os classfiles de compilação. Fontes aprovadas inalteradas desde a entrada de Test:
[prova/hashmaps](../../../experiments/construction/007-arvores/test-02/preservation.json).
Não há nova dependência, POM, runtime, wire ou alteração Java nesta etapa.

## V-014 e os quatro oráculos normativos

| Oráculo | Prova atual e resultado |
|---|---|
| Dois consumidores, uma subscrição no tronco | m01; roteiro controlado etapa1 com dois relays; TCP real S-A-R-(C1,C2) conta exatamente1 ACTIVATE R→A e1 A→S, ACK/IDs próprios por consumidor |
| ACK atrasado/cancelado não ativa | m04/m07 (seis testes); negativo M07 preservado; após cancelamento, ACCEPTED tardio mantém só C2, lease exato, substituta intacta e reserva física até callback+continuação |
| Prefixo divergente PATH_CHANGED | m05, duas variantes; roteiro etapa4 compara ramo/lease exatos e zero ACTIVATE/DEACTIVATE adicional |
| Streams independentes | m10, fontes distintas; roteiro etapa3, complemento M10 e TCP real: ativação/cancelamento/expiração de outra stream preserva ramo/lease saudáveis |

Prova técnica V-014 executada e consolidada neste relatório. Aceitação de Test
continua dependente de execução humana e decisão do utilizador; não se infere
conformidade de streaming, renovação/poda ou recuperação futuras.

## Matriz M01–M14 do Design

Nomes abaixo são métodos de ActivationTest nos XML atuais. Os14 grupos não
são14 testes; as verificações dos auxiliares também não entram no total JUnit.

| Grupo | Métodos/evidência e observação |
|---|---|
| M01 — tronco | m01TwoChildrenBeforeAndAfterAckShareOneUpstream; roteiro etapa1 completa origem e cadeia de dois relays, ACK próprios e adesão tardia; TCP real confirma ambos os troncos |
| M02 — locais | m02LocalTokensAreSetsAndCancellationPreservesTheOtherConsumer; m07CancelledRequestDoesNotReactivateAndOldCancellationCannotRemoveReplacement; token repetido/retry explícito, cancelamento último e isolamento |
| M03 — fonte | m03SourceConfirmsDirectlyAndDuplicateNeverRenews; complemento recusa SOURCE_UNAVAILABLE de A e conserva B; nenhum parent/envio para si |
| M04 — admissão/ACK | m04AcceptedIsNotAckAndAnEarlyAckWaitsForAcceptance; M09 sem ACK termina; M07/M14 retêm callback físico; PENDING fora de forwarding |
| M05 — divergência | m05DivergentSelectionRejectsBothPrefixesWithoutChangingConfirmedBranch e m05WithdrawnSelectionStillPreservesALiveConfirmedLease; confirmação/lease iguais e sem troca/poda |
| M06 — correlação | m06WrongAckFieldsAndTraceErrorCannotConfirmOrCancelActivation varia6 campos, Session incluída; ERROR15 não termina ativação11; validadores de protocolo/caps na regressão |
| M07 — cancelamento | Seis métodos m07, incluindo3 novos: cancelamento/ACK tardio, revisão substituta e callback bloqueado; negativos Maven preservados, estado saudável e reservas físicas conferidos |
| M08 — histórico | m08DuplicatesRevisionsAndLineageAreBoundedAndIdempotent; revisões0/1/MAX, pedido divergente, troca de ID, watermark/replay e histórico limitado; MAX admitida e cancelamento antigo ignorado |
| M09 — T/A | m09AttemptsHaveOneGlobalDeadlineAndErrorsAreCorrelated; m09QueueAndFirstTimeoutConsumeTheEntireRound; complemento A=1 e erros pelos requestId próprios de C1/C2; A=3, fila/T, nenhum retry por tick após terminal |
| M10 — streams | m10StreamsAndOriginsHaveIndependentConfirmations; complemento expiração A mantém B exata; roteiro e TCP com ativação/cancelamento de B preservando A |
| M11 — contexto | m11ReplacementSessionAndEpochCannotInheritAnAck; complemento Down de Session antiga mantém substituta; reset de epoch/histórico sem herança |
| M12 — leases | Três métodos m12, floor/wrap/excedido/exato; roteiro concede6000→5500→4500 na cadeia,5000→4000 em duplicado; complemento ACK em t0+G−1 autoriza1ns, t0+G retira;999999ns não concede1ms |
| M13 — físicos | Quatro métodos m13, H protegido e recuperação por perda de Session, quota fixa16/janela, L/PENDING e B; complemento C/L/D/H em E=2/4, L4/stream, B4/Session com quinta intenção à espera, J/B2 e descarte sem backlog; lógica expirada não recicla físico |
| M14 — lifecycle | Cinco métodos m14, offer rejeitado, owner saturado, callback/continuação vivos, shutdown/imutabilidade; complementos conferem jobs<=2; TCP real awaitTermination de todos os componentes e portas reutilizáveis |

[Complementos de oráculos](../../../experiments/construction/007-arvores/test-02/controlled-agent/oracles-run-01.log):
18 checks, exit0, cleanup completo. São harnesses externos ao produto,
compilados contra o JAR, sem alterar testes históricos. A limitação é explícita:
não se mede memória JVM máxima nem se esgotam todas as combinações de caps.
O teste de fronteira reduz E para observar saturação física sem alocações máximas;
o exemplo numérico da cadeia usa6000/5500/4500 em vez de10000/8500/1500 do Design.
Os tetos estáticos16 ações/turno,63 filhos/stream,2N lineagens/stream e dois jobs
mantêm-se; a regressão dos componentes emprestados e os ensaios de owner/lifecycle
não são um benchmark de justiça sob tráfego de produção arbitrário.

## Composição real e shutdown

[Prova TCP real](../../../experiments/construction/007-arvores/test-02/controlled-agent/tcp-run-01.log):
5 OverlayNode e bootstrap no mesmo processo Java, sockets loopback reais,
source S, relays A/R e consumidores via API em C1/C2 (papel RELAY no harness,
não CLI de reprodução). Usa a factory de routing existente, sem reflexão,
novo transporte ou alteração a OverlayNode. Monitor/probes e anúncios reais
preparam sessões/métricas/rotas. As duas streams são metadados, não vídeo.

19 checks, exit0: uma ativação por tronco, descoberta sem procura, path confirmado,
isolamento de B e cancelamento local de C1 preservam C2. Todos os componentes dos
cinco nós e routing terminaram; seis portas TCP reutilizáveis. O close de bootstrap
terminou sem exceção. Shutdown real não foi medido como meta de desempenho;
os testes controlados verificam o budget e reservas durante callbacks pendentes.
Q-C-34: o parent pode conservar o ramo remoto cancelado até ao lease; não se
afirma DEACTIVATE/RENEW/poda completa, vídeo ou execução CORE/GUI.

## Preservação e exceções

Q-C-36/37 mantidas byte a byte; Q-C-38 exclusivamente a lambda do leitor;
Q-C-39 exclusivamente whitespace das seis linhas autorizadas. Nenhuma Java mudou
nesta etapa. O snapshot de entrada e os hashes de evidência anterior demonstram
preservação dos negativos. target foi recriado por clean verify e é saída de
execução, separada do pacote; logs/XML/JAR foram copiados para experiments.
Não se alterou o validador, ADR, caps, UTF-8/framing,19 tipos ou código3 rejeitado.
UDP OTT1 e fixtures são conferidos por V-ADD, sem transmissão de vídeo.

## Manual humano e checkpoint

[Manual reproduzível](../../../experiments/construction/007-arvores/manual-instructions.md),
manifesto e wrapper congelados; cinco etapas e24 checks. O ensaio Codex foi
registado separadamente como agente.

Francisco executou pessoalmente e confirmou ausência de divergências no
[relatório humano](../../../experiments/construction/007-arvores/test-manual-runs/20261117T233223Z-q2zk5pu4/report.json).
Conferidos log/checks, SHA-256 de fontes/auxiliares/manifesto/JAR, origem da classe
no JAR aprovado e terminação da JVM recolhida pelo wrapper com exit0.
[Verificação persistida](../../../experiments/construction/007-arvores/integration-01/manual-and-review-verification.json).
Não se afirma identidade humana comprovada pelo software: é a confirmação
explícita do utilizador que estabelece a execução pessoal de Francisco.

O utilizador confirmou a revisão final de Waheed da implementação e evidência
atuais, sem alterações pendentes. A confirmação é vinculada aos hashes dos oito
caminhos Java autorizados, a todas as fontes aprovadas e ao JAR congelado.
Não houve contacto com o revisor, nova revisão presumida ou alteração Java.

V-ADD passou com zero erros sobre o pacote de Test; a simulação do pacote final
de integração tem prova própria em integration-01. Sem cobertura/JaCoCo/linter/CI
por ADR-026, sem metas/FPS/latências inventados.

**Test reapresentado para aprovação.** [Plano de integração](../../../experiments/construction/007-arvores/integration-01/plan.md)
ainda requer autorização explícita antes de qualquer staging/commit. Não há
integração ou fecho concluídos; sem push, DONE ou novo IMP.
