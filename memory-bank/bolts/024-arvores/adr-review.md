---
unit: "004-arvores"
bolt: "024-arvores"
stage: "adr"
status: "complete"
created: "2026-11-21T14:46:03Z"
updated: "2026-11-21T15:03:15Z"
---

# IMP-014 — revisão ADR das APIs reais

**Revisão preparada para decisão humana; não aprovada.** Model/Q-C-45/46 e
Design aprovados; autorizada apenas esta revisão. Não há Implement ou execução
de V-017/S01–S18, Maven, commit, push ou início de outro IMP.

## Aprovação e resolução

Q-C-47-B e revisão ADR aprovadas explicitamente pelo utilizador; registo2026-11-21T15:03:15Z.
Apenas Implement autorizado em cinco caminhos, já registados no ADD/Design/
contratos antes de código. Proposta/impedimento abaixo preservados como histórico;
a alternativa A não foi escolhida. Nenhuma aprovação Test/fecho/commit/push.

## 1. Conclusão e âmbito

As APIs reais fornecem proveniência das medições/anúncios, candidatos com custo
actual e contexto suficiente para a troca limitada. O núcleo é viável com
adaptações internas nos três caminhos do ADD. Essas adaptações **ainda não
existem** e não são provadas por esta leitura estática.

**Há impedimento na promessa de observabilidade através dos mecanismos
existentes sem alteração de contrato.** Event não transporta os dados e a
composição descarta eventos não fatais. [Q-C-47](../../../docs/construction-duvidas.md)
apresenta uma proposta que conserva os três caminhos, com alteração explícita
do contrato de observabilidade, e a alternativa necessária para logs automáticos.
Nenhuma alternativa é assumida como aprovada; não alterar código ou o Design
aprovado para ocultar a diferença.

Âmbito futuro autorizado continua exclusivamente:

- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`;
- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RouteMetric.java`;
- `Projeto/src/test/java/pt/uminho/esr/ott/routing/RouteSwitchTest.java`.

Outras fontes foram consultadas apenas para verificar contratos/dependências.
Não foi ampliado o âmbito de escrita. NetworkMonitor/LinkMetrics/Route/
RoutingTable/Subscription/NeighborManager/OverlayNode/OttApplication e testes
históricos permanecem intactos. RouteSwitchTest ainda é ficheiro futuro. CODESTYLE.md foi consultado; futuras
adaptações devem respeitar o estilo sem reformatar código fora do incremento.
O [Design aprovado](ddd-02-technical-design.md) original e os seus hashes foram
preservados antes da transição em
[adr-review-01](../../../experiments/construction/024-arvores/adr-review-01/README.md).

## 2. Decisões arquitecturais reutilizadas

| Decisão | Aplicação e limite |
|---|---|
| ADR-005 / Q-C-45/46 | Uma confirmação por stream, histerese somente por qualidade, grant antigo apenas enquanto válido, troca confirmada e poda exacta |
| ADR-016 | RuntimeConfiguration.Routing já fornece switchImprovementPercent/switchObservations/switchMinDwell; não alterar loader/defaults/wire |
| ADR-017 | Estado no owner serial, publicação imutável, nenhuma I/O bloqueante/novo executor para produzir observabilidade |
| ADR-014/020 | ACCEPTED não é ACK; erros correlacionados, limites lógicos/físicos distintos e primeiro prazo global de shutdown |
| ADR-018 / Q-C-40–44 | Contexto Session/época/lineagem, intenção admitida e bloqueios/créditos conservados; R10 não regrede |
| ADR-024 | Impedimento ou mudança de contrato/âmbito é apresentado antes de código; Q-C-47 não altera ADD por si |
| ADR-025/026 | Sem watchdog UDP, ferramentas/percentagem/gate externo novo |

Não se cria ADR numerada nova por inferência. Q-C-47 é proposta de resolução,
não uma decisão aceite nem aprovação do incremento. A revisão humana decidirá
o contrato antes de qualquer autorização de Implement.

## 3. Proveniência, comparação e relógio — A01/A02

**APIs verificadas:**

- [LinkMetrics (linha10)](../../../Projeto/src/main/java/pt/uminho/esr/ott/monitoring/LinkMetrics.java)
  expõe Session, sampleCount, sampleCountSaturated, lastSampleAtNanos e custo
  opcional. O construtor liga presença de amostra/custo/instante a sampleCount>0.
- [NetworkMonitor.sample (linha464)](../../../Projeto/src/main/java/pt/uminho/esr/ott/monitoring/NetworkMonitor.java)
  incrementa/satura contagem, fixa sampleAt e emite Updated. expire, na linha483,
  também emite Updated ao incrementar missedCount **sem uma medição nova**.
- [RoutingService.onMetrics (linha408)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
  recebe o Updated completo no owner, mas hoje retém só OptionalLong em costs.
  O tap de [OverlayNode (linha120)](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java)
  já entrega o evento inteiro; não é necessária alteração do monitor/composição
  para obter a proveniência.

**A01 — adaptação interna proposta:** capturar M antes de descartar os campos
no handler, comparar token da Session corrente e marcar uma única entrada
pertinente por Tree. Uma Updated de timeout/SUSPECT com o mesmo token não conta.
Ao saturar, só instante monotónico que avança prova outra observação; igualdade
é conservadoramente coalescida. Valores de custo iguais com contagem distinta
continuam observações distintas. Manter no máximo N tokens no estado de sessões
e um cursor por C; retirar token no mesmo retire/epoch/cleanup aplicável.
Não contar row.version, capturedAt ou ticks; não alterar LinkMetrics.

[announce (linha375)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
valida metadados/path/representabilidade e só chama observeAnnouncement depois
de root.announce APPLIED. A vista RouteCandidate contém G, advertisedCostMicros,
metric, generationStartedAtNanos e candidateTtlNanos. APPLIED pode ocorrer com
G igual; é preciso comparar G com o máximo observado **antes** de o actualizar,
sem reutilizar o retorno booleano como sinónimo de geração nova. Preservar
observedSequence/blockedSequence e o máximo observado durante tentativa, não
apenas G da11 inicial. Acrescentar campos de qualidade no contexto C/H existente;
não limpar bloqueios/créditos da recuperação ao qualificar/falhar uma melhoria.

**Custo confirmado:**
[RoutingTable.project (linha354)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingTable.java)
constrói todos os candidatos vivos, incluindo os não seleccionados, com custo
anunciado mais link actual. Overflow conserva candidato com metric vazio;
anúncios expirados/retirados e Sessions antigas não aparecem. Procurar em
row.route.candidates o candidato com stream/origem/época, SessionKey completa
e path **iguais à confirmação**. Conferir Session completa/current(Session),
TTL e metric presente ao mesmo now usado para o alvo. Não usar selectedMetric
para representar o custo de A quando a selecção já é B. Se A deixou de ter
custo/path demonstrável, não trocar por qualidade nem inventar perda do lease.

**A02 — cálculo e âncora:**
[RouteMetric (linha4)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RouteMetric.java)
é record(costMicros,hopCount), acumula com addExact e impõe zero somente à fonte.
Acrescentar função pura de melhoria sem mudar esse construtor/compareTo:
q=C/100,r=C%100, mínimo=q×P+(r×P+99)/100; exigir N<C e C−N≥mínimo. Nenhum produto
P×C em long/double. P100/N0 é fronteira matemática; não fabricar candidato remoto
de custo0 que o construtor proíbe. Parametrização existe em
[RuntimeConfiguration (linha117)](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/RuntimeConfiguration.java).

Confirmed hoje não guarda instante de instalação; renewBegan é início da
operação e muda em14. Acrescentar âncora de confirmação distinta na Tree:
primeiro commit e mudança efectiva Session/época/path; não usar renewBegan,
revisão, now() de leitura ou candidate.generationStartedAtNanos para D.
O relógio existente usa subtracção no horizonte monotónico, incluindo wrap;
D e backoff são estado servido por nextDelay/schedule existentes.

## 4. Operação única, renovação e recuperação — A03

[Tree/Round/Attempt (linha947)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
fornecem uma round por Tree, began final, budget, issued e uma attempt.
roundElapsed usa began/budget; send consome issued só após reserva e validação
na fronteira do port. RENEW tem limite de uma emissão e budget=min(T,grant antigo).
startRound actualmente recusa Tree saudável: **não existe troca por qualidade**.

**A03 — adaptação proposta:** distinguir internamente propósito da mesma Round
(activação/recuperação, renovação, qualidade), sem enum wire ou segundo motor.
Admissão de qualidade usa a mesma Tree/Round/cadência, mas permite grant saudável
somente após O/D/revalidação. Não remover globalmente a guarda healthy que protege
a recuperação ou transformar requestSubscription duplicado em ronda nova.

Enquanto14 lógica está pendente,11 espera o prazo/conclusão original. A reserva
física14 pode continuar ocupada depois do terminal lógico; B não é libertado
para abrir a troca. Depois de admitir qualidade, não iniciar14 paralela em A.
Uma14 já recusada não ganha retries A por callback. Qualidade e recuperação
partilham literalmente began/budget/issued da tentativa em curso; na perda de A,
promover a ronda útil mantendo essas identidades, não chamar startRound de novo.

**Limite de compatibilidade preservado:** stepTree só renova quando
compatible(t,t.confirmed.path). Esse método exige selecção=confirmado; não
forçar14 para o path A enquanto a selecção é B nem relaxar Q-C-41. A prioridade
de renovação descrita no Design vale para14 admissível pelo contrato anterior.
Se A expira durante dwell/observação, isso é perda efectiva: retirar autoridade
e recuperar sem P/O/D/backoff de qualidade, ainda sujeito a capacidade e cadência
da recuperação. Não prometer manter um grant saudável indefinidamente.

loseConfirmation hoje cancela a ronda só se renew=true; uma11 útil já pode
continuar. reconcileRecovery não abre outra enquanto t.round existe. Conservar
essa propriedade e dar prioridade à perda efectiva sobre a revalidação de
melhoria. A falha de qualidade deve usar terminal próprio/cooldown/prova no
cursor existente: não lançar erro de recuperação global que faça falhar filhos
saudáveis via t.failure. Preservar os negativos R10 e os testes de14 divergente.

## 5. Captura, confirmação/publicação e poda — A04/A05

Na [fronteira do port (linha1896)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java),
o código actual escreve t.upstream e submittedRevision para **qualquer** REQUEST,
antes de control.send. Durante uma qualidade futura B, isso perderia a identidade
de controlo de A mesmo conservando t.confirmed=A. Line.revision também inclui
revisões reservadas antes de submissão, não só as conhecidas pelo transporte.

**A04 — adaptação proposta:** capturar de A Session/stream/época/Line.id,
maior11/14 realmente submetida e geração/grant antes de preparar/submeter B.
Manter separados ramo confirmado capturado e attempt corrente, sem procurar
Line substituta num callback. Não usar Line.revision como prova de submissão,
nem a revisão de B para a13 de A. Reservas de B não alteram retroactivamente
esse valor; rollback de qualidade não reinstala uma versão velha sobre estado
que entretanto perdeu autoridade.

[receive/confirm (linha1459)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
já cruzam Session/request/época/ID/revisão, ACK positivo e ACCEPTED, e ancoram lease
em a.began. Faltam as guardas de qualidade: procura ainda admitida, O/D, custo
comparável e alvo actual no commit. Uma flag cancelada pode preceder o sweep:
revalidar procura explicitamente, não esperar stepTree após o ACK para desfazer
uma confirmação indevida. Falha real muda o modo da ronda e deixa de exigir
melhoria, sem estender deadline.

Confirmar prepara primeiro valores/grants compatíveis, altera estado no owner
e publica um único Snapshot imutável. [drive (linha588)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
publica antes e depois de activation.drive; se13 for enviada dentro do passe
antes da segunda publicação, a ordem exigida pelo Design ficaria invertida.
**A05:** publicar a confirmação B antes de disponibilizar a intenção13 ao sender
ou adiar o envio para passe posterior à publicação. O port pode ser síncrono e
reentrante: não presumir que o callback só chega depois de drive terminar.

[retireBranch/stepTree (linha1640)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
actualmente usam13 para a última procura, e `wanted && t.prune!=null` apaga essa
intenção. A poda de A após troca com procura em B precisa de distinguir contexto
retirado de cancelamento da árvore, dentro do mesmo slot de poda, sem fila ou
retargeting. Não basta preencher t.prune com a captura antiga e reutilizar o
sweep inalterado. Enquanto esse slot está ocupado, não admitir outra troca
planeada que exija segundo slot; falha efectiva nunca espera a poda, podendo
usar o fallback de lease finito do ramo retirado sem fila nova.

O receptor13 já exige igualdade exacta Line.id/revision. Preservar a semântica:
r+1 submetida sem admissão remota pode ser ignorada por parent que só conhece r.
Não enviar poda de fallback aproximada; lease finito limpa remoto. Na mesma
Line, revisão nova maior fica protegida da13 antiga. Falha de13 não restaura A.
Nenhuma modificação de Subscription, codec ou NeighborManager é necessária
para estas adaptações no RoutingService; ainda têm de ser provadas por S09/10/15.

## 6. Filhos, construtores e projecções — A06

[forwarding (linha1982)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
actualmente inclui children.active quando o lease continua válido. Depois de
mudar A→B sem suspender os filhos antigos, o construtor de
[Route.Confirmation (linha114)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/Route.java)
rejeitaria expectedPath sem o novo prefixo ou child que pertence ao próprio path.

**A06 — adaptação proposta:** antes do commit/publicação, mover grants de prefixo
incompatível para Child.suspended com o Lease original, sem novo prazo, e retirar
a autoridade. Conserva-se intenção no slot D; ACKs antigos/pending não confirmam
sob B. O mecanismo de loseConfirmation/childInterest/stepChild já dá suspensão
limitada e recuperação automática; não chamar loseConfirmation integral para
produzir uma falsa perda/segundo orçamento durante uma troca bem sucedida.

O filho mantém grant saudável só até ao deadline anterior, recusa de14 não
revoga por inferência, e no fim recupera via11 fresca sem novo play. ID/revisão
da Line incluem14 submetida; MAX não faz wrap. Floor de remanescente a montante
é revalidado no próprio send, sem bytes imutáveis reescritos após submissão.

Snapshot real tem seis componentes, overloads5/4 e cópias defensivas. Route
continua record de13 componentes só de selecção; Route.Confirmation e Recovery
separados. Preservar todos os construtores/accessors e a captura única da view.
Não promover Route.state para ACTIVE ou usar Recovery.detail para esconder
um contrato novo de qualidade. Qualquer extensão de observabilidade fica
explicitamente sujeita a Q-C-47, não é uma modificação já aceite.

## 7. Reservas físicas e lifecycle — A07

[send (linha1845)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
usa Set<ActSend> sob resources: B=min(E,64), até4/Session e2 por identidade de
contexto. REQUEST/PRUNE usam Row canónica; ACK/ERROR usam Line estável. Isso
já impede que uma Round/generation nova obtenha outro conjunto de reservas.
Conservar a referência, incluindo envios aposentados que só terminam fisicamente
mais tarde. Não substituir quota por Attempt/UUID novo.

ActSend.complete só marca callbackDone no finally; completed marca continuationDone
também no finally. release exige ambos. Cleanup assume a continuação lógica no
owner depois de limpar o estado, mas conserva callback activo. Timeout, cancel,
future terminal ou mapa vazio não autorizam release antecipado.

[awaitTermination (linha907)](../../../Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java)
já espera admissions, traceSends e activation.sends, além do scheduler e cleaned,
no menor budget restante do primeiro shutdown. Não é preciso alterar OverlayNode
para incluir a troca **se** continuar a usar ActSend. Preservar nextDelay/alarme
único, quotas e passe4+4+6+2; D/backoff usam esse mecanismo. No shutdown, limpar
tokens/cursor sem lançar13 nova, e conservar a espera física existente.

Diagnostics.sendTickets actual conta admissions de anúncios, não activation.sends;
não o apresentar como contagem global nem usá-lo como oráculo único de B.
activationLoad package-private e testes de conclusão física continuam disponíveis.
Verificação estática não substitui S17 nem regressão JDK21.

## 8. Impedimento de observabilidade — A08 / Q-C-47

**Factos conferidos:**

1. RoutingService.Event é `(Reason, boolean fatal)`; Observer só onEvent(Event).
   Diagnostics contém contadores genéricos/lastReason, sem stream/custos/operação.
2. note actual incrementa failures e pode encerrar o nó; não é canal para contar
   trocas normais nem permite inventar uma falha para gerar log.
3. OverlayNode, linhas135–140, só trata event.fatal(); não reencaminha eventos
   normais para o observador da aplicação.
4. A aplicação regista NeighborManager.Event, uma hierarquia sealed separada,
   e o comando `recovery` imprime OverlayRecovery.RecoveryStatus de descoberta,
   **não** Route.Recovery. Não resolve a lacuna codificar texto em Recovery.detail.

Portanto não se pode afirmar que os mecanismos existentes registam custos,
motivos e duração das trocas de qualidade. JDK System.Logger/println no owner
introduziria I/O potencialmente bloqueante; executor/logger novo ou mensagem
wire artificial também violariam o Design. Nenhuma dessas alternativas foi feita.

**Proposta principal Q-C-47-A, mantendo três caminhos:** diagnóstico imutável
consultável num mapa `Snapshot.switches`, com tipo aninhado em RoutingService,
slot≤C e último estado/contadores saturados por Tree. Acrescentar sétimo componente
e overload6 compatível, preservando4/5/6 e todas as projecções anteriores. Incluir
identidades/custos/P/O/D/operação/revisões/razão/duração e validade as-of, sem
autorizar forwarding, histórico de amostras, callback ou novo executor. Isto
**altera** a decisão de não estender Snapshot e substitui a promessa de log
automático por observabilidade consultável via API/harness humano. CLI actual
não passaria a imprimi-la por magia. Precisa de aceitação explícita desse contrato
e ajustamento documental antes de Implement; não enfraquecer aceitação por inferência.

**Alternativa Q-C-47-B, se logs automáticos forem mantidos:** preservar Snapshot
e estender RoutingService.Event com payload de troca e construtor2 compatível;
adicionar um sink explícito de routing em OverlayNode e ligá-lo ao console.log
limitado da aplicação. Exigiria **dois caminhos adicionais de produção**
(`overlay/OverlayNode.java`, `app/OttApplication.java`) e contrato local novo,
sem alterar eventos/wire de NeighborManager. Uma factory nomeada pode preservar
construtores existentes sem overload ambíguo. Não está autorizada e não integra
a allowlist. Não propor dependência/logger/thread nova como substituto silencioso.

Detalhes de campos, ordenação/limites e oráculos das duas propostas constam
de Q-C-47. Nenhum patch Java foi escrito/aplicado; não escolher A/B pelo utilizador.

## 9. Evidência, limites e checkpoint

A01–A07 são adaptações internas compatíveis com os contratos anteriores e
restritas ao âmbito futuro; A08 requer decisão Q-C-47. Nenhuma foi implementada.
As fontes consultadas e hashes estão inventariados separadamente da documentação.
V-ADD e diff check são verificações documentais/âmbito, não V-017.

V-017/S01–S18 continuam por executar; preservar testes históricos/asserções/
prazos. Eventual Implement só após decisão do impedimento e autorização explícita.
Fecho requer regressão JDK21, manual humano **antes de aprovar Test**, revisão
final **Waheed** e integração; não há gate externo adicional.

**R17 inicial de IMP-013 permanece sem causa estabelecida.** A integração com
métricas controladas não demonstra a causa do negativo histórico. Logs, negativos,
sucessos, manual/JAR/hashes anteriores estão preservados, sem nova investigação
ou alegação de causa por esta revisão.

Parar na revisão ADR. Só Model/Design concluídos; `current_stage=adr`, história
planned/não implementada. Sem aprovação ADR/Implement/Test, commit/push ou fecho.
