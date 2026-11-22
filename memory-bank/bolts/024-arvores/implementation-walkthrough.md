---
unit: "004-arvores"
bolt: "024-arvores"
stage: "implement"
status: "in-progress"
created: "2026-11-21T15:03:15Z"
updated: "2026-11-21T16:01:32Z"
---

# IMP-014 — Implement concluído

**Implement aprovado.** Model/Q-C-45/46, Design e revisão ADR com
Q-C-47-B aprovados. ADD, Design e contratos foram actualizados para cinco
caminhos. Implementação concluída e testada. [Evidência 01](../../../experiments/construction/024-arvores/implement-01/README.md) e
[Evidência 02 (stale metrics)](../../../experiments/construction/024-arvores/implement-02-stale-metrics/README.md).

Regressão incremental final JDK21: **1459 testes, 0 falhas, 0 erros, 0 skips**, 21 XML:
1419 testes históricos preservados e 40 novos. Os resultados são verificação
de Implement; consolidados para Test e V-017. Executado clean verify com sucesso.

## 1. Comportamento implementado

A árvore compara o custo actual do caminho confirmado com a alternativa actual,
usando a geração validada do anúncio e proveniência de LinkMetrics para contar
observações distintas. Melhoria relativa exacta não usa double nem produtos
que possam transbordar; defaults20%/3/5s preservados. A permanência é ancorada
na instalação da primeira confirmação e na mudança efectiva de Session/época/path,
sem ser deslocada por renovação do mesmo ramo.

Uma troca usa a Round existente e mantém o grant antigo enquanto válido. Antes
do envio e do ACK são revalidados procura, contexto, custos/O/D, prazo e alvo.
A confirmação nova é publicada antes de emitir SUCCESS e antes da poda exacta
da revisão antiga capturada. Filhos com prefixo incompatível ficam suspensos
apenas até ao seu lease original; não se transfere autoridade por cópia ou ACK
espontâneo. Uma11 fresca pode conceder o novo prefixo, limitada pelo remanescente
a montante. Recuperação automática e renovação anteriores permanecem activas.

Falha efectiva ignora histerese/backoff de qualidade; uma tentativa útil pode
assumir a recuperação com o mesmo início/limite/tentativas/reservas. Terminal de
qualidade conserva o parent saudável e bloqueia a prova consumida. Métrica que
fica inválida e volta, callback, duplicado ou timer não a desbloqueiam; ANN
posterior útil e nova qualificação permitem tentativa limitada após T/cadência.

## 2. Cinco caminhos e adaptações A01–A08

| Caminho | Alteração e compatibilidade |
|---|---|
| routing/RoutingService.java | Tokens de medição limitados por Sessions correntes; cursor por Tree; mesma Round para qualidade/recuperação; captura antiga separada; confirmação/publicação/poda; eventos imutáveis e construtor Event2 preservado |
| routing/RouteMetric.java | Função pura improves(C,N,P) com decomposição inteira; componentes/construtor/compareTo/accumulate preservados |
| overlay/OverlayNode.java | Factory nomeada withRoutingObserver e sink explícito; construtores3/4/5 e factories anteriores mantidos; tratamento fatal no finally |
| app/OttApplication.java | Sink ligado exclusivamente a console.log; formato compacto com identidades/custos/motivo/duração antes dos paths, dentro do limite existente |
| routing/RouteSwitchTest.java | Trinta testes novos, relógio/owner/transporte controlados, integração TCP real e saída real da aplicação; sem alteração de testes históricos |

Paths de produção relativos a Projeto/src/main/java/pt/uminho/esr/ott; teste em
Projeto/src/test/java/pt/uminho/esr/ott. Nenhum outro fonte/teste/fundação alterado.
[Estilo consultado](../../../docs/style/CODESTYLE.md); helpers internos conforme
Design/âmbito aprovado, sem nova classe auxiliar externa ou refactor global.

- **A01:** M=(Session,count,saturação,instante), retido no máximo por Session;
  Updated de timeout/mesma prova não conta. G novo é determinado comparando a
  vista anterior com o ANN aplicado; APPLIED com G igual não é observação nova.
- **A02:** mínimo=ceil(P×C/100) por q=C/100,r=C%100; custo confirmado procura
  candidato fresco do path/Session exactos, independentemente da selecção.
- **A03:** mesma Round/budget/issued. RENEW admissível continua prioritário,
  nunca paralelo à11 de qualidade. Sem alteração da recusa por selecção divergente.
- **A04:** captura Line/Session/revisão efectivamente submetida antes de B; enviar
  B não sobrescreve a identidade de A usada pela poda/cancelamento.
- **A05:** publicação imutável antes de SUCCESS/13. Prune distingue ramo retirado
  de última procura; procura em B não elimina a poda de A. Não há fila de podas
  ou cancelamento aproximado de revisões; lease remoto é fallback.
- **A06:** retirar grants child incompatíveis antes de publicar novo prefixo;
  suspensão guarda o Lease original no slot existente, sem prazo adicional.
- **A07:** REQUEST/PRUNE usam Row canónica e ACK/ERROR Line estável. ActSend,
  limites B/Session/contexto, callbackDone/continuationDone e primeiro shutdown
  continuam partilhados. Cancelamento/timeout não libertam reserva física.
- **A08/Q-C-47-B:** SwitchEvent copia paths≤32, limita motivo≤256 e contém
  stream/origem/época/operação, Sessions, revisões, custos, fase e duração.
  Só STARTED e um terminal SUCCESS/FAILURE por operação, incluindo cancel/close.
  Nenhum log por amostra, logger/I/O no owner, dependência, thread ou fila nova.

Snapshot mantém exactamente seis componentes e construtores4/5/6. Route,
Confirmation/Recovery, Subscription, monitor, manager, transporte, wire e
configuração permanecem intactos. Não foi implementada a alternativa Q-C-47-A.
Eventos normais de troca não incrementam o contador de falhas de routing.

## 3. Verificação dos oráculos

Os18 grupos do Design têm verificações técnicas na implementação; continuam
sujeitos à consolidação V-017 na etapa Test, ainda não autorizada.

| Grupos | Verificação actual |
|---|---|
| S01–S04 | Ruído interrompe consecutividade; fronteira20%/D exacto; P0/100; fonte0 e MAX sem overflow |
| S05–S06 | Mesma medição/ANN/tick não conta; amostras iguais distintas e saturadas; invalidação reinicia sequência |
| S07–S08 | Renovação não desloca D; selecção B não substitui custo confirmado A; ausência de custo não revoga grant |
| S09–S10 | Publicação antes de sucesso/poda, revisão antiga14 capturada; ACK precoce aguarda ACCEPTED; cancelamento impede commit |
| S11–S12 | Falha conserva A, um terminal, sem retry espontâneo por métrica; ANN posterior e O novo rearmam sob T/cadência |
| S13 | Perda efectiva antes de D, deadline global original durante troca, três tentativas partilhadas e crédito de incidente consumido; perda após expiração distingue incidente fresco |
| S14–S16 | Alvo obsoleto não confirma; um de dois consumidores pode sair; filhos incompatíveis suspensos e nova11 recebe prefixo correcto |
| S17 | Envio físico retido atravessa cancelamento/shutdown; await(0)=false até callback; guardas M07 e regressão de lifecycle preservadas |
| S18 | TCP real S–(A,B)–R com métricas controladas até writer real da aplicação; streams/origens isoladas; ERROR14/15 e Session errada não completam11 |
| Q-C-47-B | Payload/cópias/limites, Snapshot/Event e construtores/factories, fila de saída cheia sem I/O no owner, fatal preservado, escaping e contexto máximo sem truncar custos/motivo/duração |

O harness de aplicação usa reflexão apenas no novo teste para alcançar a
composição/Console privados existentes; não foram acrescentados hooks públicos
de teste, comandos play ou novas filas de produção. O writer é o ott-cli-output
existente. Peers de TCP controlam métricas e respondem a sondas, mantendo o
protocolo real; isso não representa medições reais de ruído/latência.

Não há nova prova de vídeo, GUI, CORE ou recuperação por silêncio exclusivo UDP;
ADR-025 e essa limitação mantêm-se. A regressão inclui os testes anteriores de
rearme, leases/renegociação e callback/continuação, sem retirar asserções/prazos.

## 4. Negativos funcionais reproduzidos e correcções

### 4.1 Negativo determinístico de transferência (S13)

Após a primeira regressão verde1445, uma auditoria adicional produziu
`s13TransferredLossCannotGiveTheSameExhaustedRoundAnotherCredit`:
qualificar B, iniciar aos5s, observar G2 ainda pendente, perder A, negar três
tentativas B e avançar T sem ANN novo. Esperado4 ACTIVATE totais (A+3B);
observado5. [Negativo duradouro](../../../experiments/construction/024-arvores/implement-01/transfer-negative-01.log)
e XML preservados, assim como a versão de RoutingService anterior à correcção.

Causa sustentada: loseConfirmation promovia a ronda existente, mas deixava
lossCredit=true. Ao terminar essa ronda, o mesmo incidente reabria outra na
janela seguinte apesar do watermark bloqueado. A correcção consome o crédito
somente se a ronda ainda tem prazo e uma prova elegível. Se a troca já expirou
quando a perda ocorre, o novo incidente conserva crédito; não o absorver numa
tentativa morta. Dois testes cobrem os lados dessa fronteira, sem aumentar T/A.
Verificação pertinente4/0/0/0 e regressão final1449/0/0/0. Esta causa pertence
ao negativo novo de S13; **não é uma explicação de R17 histórico**.

### 4.2 Negativo de medição ultrapassada e validação de proveniência (S05/S06)

Na auditoria das regras de proveniência de métricas em S05/S06, reproduziu-se no
rig de `RouteSwitchTest`:
1. `longGrant()`, ficando em D=4 s.
2. Ligar B, medir custo 10000 (count=1, sampledAt=4 s) e anunciar G1/custo 790.
3. Entregar três medições novas de B com custo 10, contagens 2/3/4.
4. Reentregar exactamente a medição antiga: custo 10000, contagem 1, instante 4 s.
5. Avançar 1 s até D=5 s.

- **Esperado:** B qualificado preserva a selecção e emite o segundo ACTIVATE.
- **Observado no negativo:** A selecção regressava a A e permanecia apenas o ACTIVATE inicial (`expected: <2> but was: <1>`).
- **Controlo sem repetição (`s05StaleMeasurementControlWithoutReplayKeepsQualification`):** Passava (2 ACTIVATE).
- **Negativo com repetição (`s05SupersededMeasurementCannotReplaceCurrentCostOrQualification`):** Falhava (1 ACTIVATE). [Negativo e controlo preservados](../../../experiments/construction/024-arvores/implement-02-stale-metrics/negative-and-control-01.log).

**Causa raiz:** Em `RoutingService.onMetrics`, a contagem repetida não incrementava
observações distintas (`distinct=false`), mas o custo antigo de 10000 substituía
directamente o custo actual no grafo (`root.setLinkCost`) e despoletava
`activation.kick()` e `drive()`, desqualificando B e revertendo a selecção para A.

**Correcção autorizada:** Validação de proveniência em `supersededMeasurement(metrics, previous)`
antes de substituir custos, projecções ou qualificação. Rejeita amostras com contagem
inferior, sem timestamp, com timestamp decrescente, des-saturação inválida ou replay
após invalidação. Preservados o tratamento legítimo de SUSPECT actual (conserva custo e
qualificação sem contar nova observação), invalidação sem ressurreição por replay antigo,
amostras novas, saturação temporal estrita e Session substituta (descarta marcas antigas).
Verificação pertinente 63/0/0/0 e regressão completa JDK21 **1459/0/0/0**.

## 5. Execuções preservadas e justificação de alterações

| Evidência | Resultado / alcance |
|---|---|
| implement-01/compile.log | Compilação release21 passou |
| implement-01/focused-01.log | Nenhum teste executado: erro de caminho ao criar o novo ficheiro; Maven rejeitou padrão sem testes |
| implement-01/focused-02.log/xml |20/0/0/0, determinísticos JDK21 |
| implement-01/focused-03.log/xml |22/0/1/0; sandbox recusou criação de socket |
| implement-01/focused-04.log/xml |22/1/0/0; peer do teste sem resposta PONG impedia a medição inicial; runtime fora da sandbox era25, não prova JDK21 |
| implement-01/focused-05.log/xml |22/0/0/0 JDK21 explícito, após acrescentar echo ao peer autorizado |
| implement-01/focused-06.log/xml |25/0/3/0; topologia auxiliar de grau0 rejeitada pela validação existente |
| implement-01/focused-07.log/xml |25/0/0/0; topologia R–X válida, mantendo validação/assertivas/prazos |
| implement-01/regression-01.log e XML/source |1445/0/0/0 JDK21, versão anterior ao teste adicional de transferência |
| implement-01/transfer-negative-01.log/xml |1/1/0/0, falha funcional determinística4 vs5 |
| implement-01/transfer-fixed-01.log/xml |4/0/0/0 JDK21, transferência/fronteira/isolações/correlações |
| implement-01/regression-02.log e XML/source |1449/0/0/0 JDK21.0.13.1, 21 XML, 30 testes novos |
| implement-02/negative-and-control-01.log/xml | 2 testes: 1 falha funcional reproduzida (s05Superseded) e 1 controlo sem replay com sucesso |
| implement-02/reproduction-fixed-01.log/xml | 2/0/0/0 após validação de proveniência antes de substituir custos |
| implement-02/targeted-01.log e XML | 63/0/0/0 testes pertinentes (S05/S06, negativos R10 históricos, RoutingServiceTest) |
| implement-02/regression-01.log | **1459/0/0/0 JDK21.0.13.1**, 21 suites, 40 testes novos no total (30 de imp-01 + 10 de imp-02) |

Não há remoção de teste/asserção, relaxamento de prazo, alteração de fundação ou
correcção fora do âmbito. Guardas anteriores que impediam11 com grant saudável
foram especializadas apenas para qualidade qualificada; a recuperação preserva
a guarda original. A escrita prematura do contexto de A e a eliminação de poda
com procura foram restringidas conforme A04/A05; não foram apagados os contratos
de cancelamento ou igualdade exacta de revisão. As falhas dos novos fixtures
foram corrigidas no próprio RouteSwitchTest, sem mudar BootstrapServer/monitor.

## 6. Limites e checkpoint

**R17 inicial de IMP-013 continua sem causa estabelecida.** A integração com
métricas controladas não demonstra a causa desse negativo. Logs, negativos,
sucessos, manual/JAR/hashes anteriores preservados. Nenhum gate externo novo.

Implement está pronto para revisão, não aprovado. Só Model/Design/ADR concluídos;
current_stage=implement, história planned/não concluída. Test formal/V-017
consolidado, clean verify/JAR de entrega, regressão aprovada, manual humano
antes de aprovar Test, revisão final **Waheed** e integração continuam pendentes.
Sem fecho, commit/push ou outro IMP. Não se solicita aprovação de Test nesta etapa.
