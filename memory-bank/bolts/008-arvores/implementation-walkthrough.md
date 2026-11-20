---
unit: "004-arvores"
bolt: "008-arvores"
stage: "implement"
status: "complete"
created: "2026-11-20T12:24:47Z"
updated: "2026-11-20T15:44:31Z"
---
# IMP-013 — Implement para revisão

Revisão ADR aprovada; A01–A08 implementadas nos três caminhos do ADD.
**Implement aprovado pelo utilizador em 2026-11-20T15:59:41Z, incluindo R10.**
Apenas Test autorizado; manual humano antes de pedir aprovação Test.
Model/Design/ADR/Implement concluídos; current_stage=test, completed=null.
Registos de pendência abaixo são históricos. Verificações abaixo são incrementais de Implement,
não avanço à etapa Test ou aprovação de V-016 consolidado.

## 1. Resultado atual

Regressão incremental JDK21 atual após correção R10: **1419 testes, zero falhas,
zero erros, zero ignorados,20 XML**, terminada em2026-11-20T15:42:56Z, Maven37.994s.
Inclui1379 testes históricos e40 em RecoveryTest (35 anteriores intactos +5 novos). Dois dos novos usam TCP,
handshake/NeighborManager/reparação reais, com métricas controladas. Os testes
históricos mantêm fonte, asserções e prazos byte a byte.

[Resultado atual e hashes](../../../experiments/construction/008-arvores/implement-02-r10-metric/regression-01/result.json),
[log atual](../../../experiments/construction/008-arvores/implement-02-r10-metric/regression-01/maven.log),
[execuções desta correção](../../../experiments/construction/008-arvores/implement-02-r10-metric/runs.json).
[Execuções anteriores](../../../experiments/construction/008-arvores/implement-01/runs.json) permanecem preservadas.
Os20 XML e109 ficheiros novos de evidência CLI/subprocessos estão preservados
separadamente no diretório implement-02-r10-metric/regression-01. O JAR anterior não representa estas
classes novas: não se executou package/clean verify nem se produziu JAR final.

## 2. Ficheiros e adaptações A01–A08

Os únicos caminhos Java alterados/criados são:

1 - [RoutingService.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java).
2 - [Route.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/Route.java).
3 - [RecoveryTest.java](../../../Projeto/src/test/java/pt/uminho/esr/ott/routing/RecoveryTest.java), novo.

| Adaptação | Comportamento implementado |
|---|---|
| A01 | ANN observado só após validação/APPLIED; prova estrutural e geração, máximo observado/watermark, crédito coalescido. Callback ou recuperação de métrica isolada não reutiliza prova bloqueada; primeira elegibilidade e ANN novo posterior continuam admissíveis. A prova é consumida por cada tentativa, inclusive uma alternativa usada na mesma ronda |
| A02 | Procura usa intenção local admitida não cancelada, independentemente do resultado terminal do ticket. Recusas pré-admissão não criam intenção; cancelamento antigo não retira substituto |
| A03 | Mudança de época mantém Tree/intenções locais/cadência; retira autoridade, filhos e lineagens antigas. Nova época requer negociação nova, sem rollback ou troca de origem |
| A04 | ACTIVATE conserva ID da Line na mesma Session/época, revisão crescente e requestId novo. Correlação e barreiras de ACK/ACCEPTED/ERROR11/14/trace permanecem; poda conserva identidade capturada |
| A05 | Filho suspenso conserva só o lease/interesse original, sem forwarding ou ACK espontâneo. Prefixo novo exige pedido fresco; expiração/cancelamento/Session morta retiram interesse. ERROR14/PATH_CHANGED suspende retries inúteis, preservando o grant saudável até perda efetiva |
| A06 | Route mantém componentes/construtor de seleção; Recovery é valor aninhado imutável. Snapshot acrescenta recoveries e preserva chamadas4/5 argumentos, além do completo6; publicação no mesmo owner/instante |
| A07 | REQUEST/PRUNE upstream usam Row canónico da stream como contexto físico entre Trees/Sessions/épocas. Tetos partilhados existentes, prazo global T, até A emissões e cadência de rearme; não há pool/timer por recuperação |
| A08 | Cleanup retira novo estado lógico antes de concluir continuações canceladas. Reservas físicas permanecem até callbackDone && continuationDone; shutdown/await usam o orçamento global existente e incluem todos os sends |

Código novo e métodos reescritos seguem
[CODESTYLE](../../../docs/style/CODESTYLE.md); auxiliares ficam aninhados conforme
o Design e os três caminhos autorizados. Não se iniciou formatação global de
fundações ou testes históricos. APIs/wire/codec/configuração/POM/Subscription/
RoutingTable/OverlayNode/manager/monitor permaneceram inalterados.

## 3. Justificação das remoções e substituições

Nenhum ficheiro, teste histórico, asserção ou prazo histórico foi retirado.
O diff substitui corpos densos dos métodos de admissão, transição/erro, perda de
Session/época e fecho, e formata os contextos alterados segundo CODESTYLE.
As linhas removidas não representam redução dos oráculos.

- O predicado que fazia procura depender de outcome pendente/confirmedLocals
  foi substituído por intenção admitida viva: o anterior perdia recuperação
  após SUCCESS/erro e perda da confirmação.
- A remoção integral de Tree na mudança de época foi substituída por retirada
  de autoridade/contextos remotos e conservação da intenção local/cadência.
  Filhos mortos, cancelados ou expirados continuam a ser removidos; não se
  conserva procura fantasma.
- A remoção imediata do interesse de filho por perda upstream foi substituída
  por suspensão limitada ao seu deadline original. O destino ativo é retirado
  imediatamente e não é reinstalado por cópia na árvore nova.
- A chave física upstream Line foi substituída por Row estável para que trocar
  geração não esconda sends antigos perante2/contexto. Não se remove ActSend
  por cancelamento/timeout lógico; o release físico mantém as duas conclusões.
- A instrumentação temporária R13 saiu do teste depois de recolhido o diagnóstico;
  a versão instrumentada/log/XML está preservada. Nenhuma asserção saiu.
- A fixture TCP nova passou a controlar métricas e esperar prefixo coerente
  antes da admissão. Mantém SUCCESS, TCP/reparação reais e os mesmos prazos;
  não altera a integração histórica com NetworkMonitor real.

Os dois ficheiros rastreados em target/maven-status alteraram-se como output
gerado de compilação; ficam separados de código/âmbito e não são propostas de
alteração de fundações. Não houve staging, commit ou push.

## 4. Oráculos correntes R01–R18

Todos os40 testes de RecoveryTest passaram na regressão atual. Esta matriz identifica a
cobertura automatizada corrente; o relatório consolidado V-016 e a aceitação da
etapa Test continuam pendentes. Não se declara vídeo/GUI/CORE ou manual humano.

| Grupo | Testes RecoveryTest / complemento histórico executado |
|---|---|
| R01 | r01DiamondLosesAuthorityBeforeConfirmingTheAlternative |
| R02 | r02PartitionIsUnavailableWithoutLosingAdmittedIntentOrRetryingOnTicks |
| R03 | r03ReturnedSessionAutomaticallyRecoversTheSameConsumer |
| R04 | r04RetiredSessionAckErrorAndPhysicalCallbackCannotRetireReplacement; M06/M07/L15 |
| R05 | r05FailureAndEpochChangeAreIsolatedFromTheOtherOrigin |
| R06 | r06SamePathNeedsANewValidatedGenerationAfterTheFailure; invalid ANN/watermark e erro permanente |
| R07 | r07CancellationWinsAgainstCreditAndAnOldTicketCannotCancelItsReplacement; M07 |
| R08 | r08ADeadChildSessionDoesNotLeaveRecoverableDemand; expiração de interesse suspenso |
| R09 | suspensão/pedido fresco/revisão maior, expiração e r09TcpTwoRelaysAutomaticallyRenegotiateAChangedPrefixWithoutHumanRetry |
| R10 | A/T, rajada, ANN anterior à falha, segunda adesão, crédito expirado, wrap monotónico, alternativa já consumida e overflow/restauro de métrica sem reutilizar prova bloqueada |
| R11 | ACK antecipado/barreira, deadline exato, lease excessivo e ancoramento; M12 cobre floor/sub-ms |
| R12 | r12NewEpochRetainsIntentButRequiresFreshAuthority |
| R13 | capacidade libertada sem retry, ANN posterior, nova saturação bloqueia prova outra vez; M13/L19 complementam caps/quotas históricos |
| R14 | Sessions/épocas partilham reserva, callback retido após continuação e liberação só no fim; M07/L12/L19 |
| R15 | shutdown com callback retido e com crédito à espera de orçamento; await(0)/sem submissões novas |
| R16 | r16NoUdpSilenceEventOrMetricRefreshCreatesRecovery; controlo sem evento UDP, sem injeção real de falha UDP/CORE |
| R17 | r17TcpSequentialRelayFailuresConsumeExistingSourceClientRepairWithoutBootstrap |
| R18 | seleção divergente/recusa14 preserva grant; ANN posterior permite14 legítimo; recuperação nova retoma renovação |

O teste de projeção cobre construtores4/5, imutabilidade e Route de seleção.
Testes controlados reutilizam Rig/owner/clock/port/scheduler históricos em leitura.
Na integração TCP, o clock é real e as métricas são fixas; após falhas, só os
pedidos automáticos dos nós reconstroem autoridade. O bootstrap é fechado antes
das duas falhas sequenciais em R17. As portas de controlo voltam a aceitar bind
depois do fecho e todos os owners/schedulers/managers terminam com budget limitado.

## 5. Execuções e negativos preservados

| Execução | Resultado testes/falhas/erros/ignorados | Diagnóstico/ação |
|---|---|---|
| compile-01 | compilação passou, sem testes | JDK21 offline |
| focused-01 |23/1/1/0 | expiração sem nova passagem de Tree e snapshot/prazo não publicados na admissão cheia |
| diagnostic-r13-01 |1/0/1/0 | confirma capturedAt=0/recoveries vazio apesar de Tree/Local/envios existentes; admit chamava drive interno |
| focused-02 |105/1/2/0 | prazo de fila reaberto indevidamente; duas recusas de sockets pela sandbox |
| focused-02-tcp |105/1/0/0 | fora da sandbox confirma apenas o desvio de prazo de fila |
| focused-03 |107/0/0/0 | primeiras correções e duas integrações TCP passaram |
| focused-04 |116/2/0/0 | fixture R18 chegava ao TTL exato do ANN; ticket inicial TCP R17 terminou ERROR sem razão recolhida |
| regression-01 |1413/0/0/0 | fixture R18 recebe ANN válido; TCP usa métricas fixas/prefixo coerente |
| negative-consumed-alternative-01 |1/1/0/0 | reproduz quarta ativação sem ANN novo após alternativa B já consumida |
| consumed-alternative-fixed-01 |1/0/0/0 | consumo da prova por tentativa corrige o negativo, sem alterar teste |
| regression-02 |1414/0/0/0 | resultado anterior preservado,20 XML e hashes à data |
| implement-02/negative-01 |1/1/0/0 | reprodução exata do utilizador: métrica10→2000→10, esperado1/observado2 ACTIVATEs |
| implement-02/focused-01 |11/0/0/0 | todos os R10, incluindo os cinco casos novos |
| implement-02/regression-01 |1419/0/0/0 | resultado atual JDK21,20 XML,40 RecoveryTest e1379 históricos |

Diagnósticos sustentados em
[focused-01](../../../experiments/construction/008-arvores/implement-01/focused-01/diagnosis.md),
[prazo de fila](../../../experiments/construction/008-arvores/implement-01/focused-02-tcp/diagnosis.md),
[fixtures](../../../experiments/construction/008-arvores/implement-01/focused-04/diagnosis.md) e
[alternativa consumida](../../../experiments/construction/008-arvores/implement-01/negative-consumed-alternative-01/diagnosis.md).
Não se atribui causa ao ERROR inicial de R17: faltou correlação nessa execução.
A mudança da fixture torna as entradas de seleção controladas, conserva o
negativo e não comprova a causa histórica. Não houve repetições sem alteração
ou diagnóstico apenas para obter verde, aumento de prazos ou supressão de testes.

## 6. Estado para aceitação

Reapresentar Implement; manter sem aprovação automática e sem avançar a Test.
Regressão JDK21 incremental passou, mas aceitação/consolidação de V-016,
regressão da etapa Test, manual humano antes de aprovar Test, revisão final
Fillmore e integração continuam obrigatórios. Sem gates externos adicionais.
Recuperação de falha exclusiva UDP continua limitação deliberada ADR-025.

Nenhum outro IMP iniciado, nenhum fecho/commit/push. Os resultados verdes não
substituem estes checkpoints nem autorizam ampliar âmbito. Fonte e evidência
históricas, incluindo os negativos de NeighborDown/D11a, permanecem preservadas.


## 7. Correção R10 — métrica recuperada não reutiliza prova bloqueada

Registo 2026-11-20T15:44:31Z. Implement permanece pendente. O teste novo reproduziu exatamente
Rig("R",false,4096,1), parent A, métrica10, ANN G2/custo Long.MAX_VALUE-1000,
ACTIVATE/PATH_CHANGED, T3s, métrica2000 (soma não representável) e regresso a10
sem ANN novo. A primeira execução falha na contagem: esperado1/observado2.
[Negativo preservado](../../../experiments/construction/008-arvores/implement-02-r10-metric/negative-01/result.json),
[log](../../../experiments/construction/008-arvores/implement-02-r10-metric/negative-01/maven.log),
[diagnóstico sustentado](../../../experiments/construction/008-arvores/implement-02-r10-metric/diagnosis.md).

A perda de representabilidade retirava a seleção elegível, mantendo candidato,
G e TTL. Isso fazia view=null; a reposição da métrica acionava previous==null e
dava crédito sem consultar blocked. A guarda agora verifica o watermark nessa
transição: a mesma prova bloqueada não fornece novidade. Prova de ramo distinto,
primeira medição válida de prova ainda não usada e geração validada acima do
watermark mantêm os caminhos legítimos. O watermark da falha inclui os ANN já
observados, portanto G recebido antes da falha continua bloqueado.

Mudança de produção limitada a reconcileRecovery em RoutingService; Route e
todos os outros componentes permanecem byte a byte iguais à entrada desta
correção. RecoveryTest recebeu cinco testes novos, sem modificar os35 anteriores:
reprodução exata; primeira medição válida; ANN novo após a falha/restauro; ANN
maior recebido antes da falha; ANN posterior recebido sem métrica e depois
primeira observação elegível. Não se retiraram asserções, prazos, APIs,
construtores ou reservas físicas, nem se reiniciaram T/A/cadência.

A única substituição de produção é o crédito incondicional por ausência da vista
anterior, trocado pela guarda do watermark. Não há remoção de funcionalidades,
testes ou evidência. [Delta face à entrada](../../../experiments/construction/008-arvores/implement-02-r10-metric/RoutingService.java.patch)
e [conferência dos métodos anteriores](../../../experiments/construction/008-arvores/implement-02-r10-metric/RecoveryTest-methods.json).
Os11 R10 focados e a regressão incremental1419/0/0/0 passaram com JDK21.
246 ficheiros de evidência anteriores foram preservados para conferência por hash.

O negativo inicial TCP R17 continua sem causa estabelecida. Esta correção R10
não o diagnostica nem autoriza eliminá-lo. Sem aceitação de Implement ou avanço
a Test, fecho, commits ou push. V-016 consolidado, manual humano antes de aprovar
Test, revisão Fillmore e integração continuam pendentes, sem gates externos novos.
