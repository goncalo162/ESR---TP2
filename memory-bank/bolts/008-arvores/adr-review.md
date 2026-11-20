---
unit: "004-arvores"
bolt: "008-arvores"
stage: "adr"
status: "complete"
created: "2026-11-20T11:45:38Z"
---
# IMP-013 — revisão ADR aprovada

Revisão ADR aprovada explicitamente pelo utilizador em 2026-11-20T11:55:33Z. Apenas
Implement autorizado nos três caminhos, aplicando A01–A08. Model/Design/ADR
concluídos; current_stage=implement, completed=null. Registos de pendência
abaixo são históricos; não autorizam Test ou fecho.

## 1. Conclusão e âmbito

O [Design aprovado](ddd-02-technical-design.md) é realizável pelas APIs atuais
nos três caminhos do ADD. Não foi identificado impedimento que exija ampliar
âmbito, substituir ADR aceite ou alterar fundações. Nenhuma ADR nova é proposta
ou criada; nenhuma decisão aceite muda de estado. A conclusão é estática, não
prova execução, compatibilidade binária ou recuperação já implementada.

Os três caminhos de Implement continuam exclusivamente:

1 - `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`.
2 - `Projeto/src/main/java/pt/uminho/esr/ott/routing/Route.java`.
3 - `Projeto/src/test/java/pt/uminho/esr/ott/routing/RecoveryTest.java`.

RecoveryTest ainda não existe. Dependências foram consultadas apenas em leitura;
não se alteram RoutingTable, Subscription, manager, monitor, OverlayNode, CLI,
codec/transporte/configuração/POM ou testes históricos. Qualquer necessidade
concreta posterior fora destes caminhos terá de ser apresentada antes de código.

## 2. Decisões reutilizadas

| Decisão aceite | Conformidade e condição a preservar |
|---|---|
| ADR-005 | Um upstream confirmado por stream, procura agregada e autorização apenas após ACK/barreira; interesses suspensos não são forwarding |
| ADR-020 | Intenção local admitida sobrevive a indisponibilidade; conexão morta não transmite subscrição à nova Session; operação finita e erro isolado |
| ADR-024 | Um IMP, três caminhos, evidência e checkpoints; diferença entre código atual e entrega prevista não autoriza corrigir fundações |
| ADR-025 | Sem watchdog ou rearme por silêncio UDP; continuidade de vídeo não é demonstrada por recuperação TCP |
| ADR-004/017/018 | Seleção separada de confirmação, mutação no owner serial, estado volátil e correlação por contexto completo |
| ADR-014/015/021 | ACCEPTED só enfileira; reservas/quotas antes do port, sem ACK implícito nem libertação física por timeout lógico |
| ADR-016/026 | T/A e configuração existentes, Java21 e verificação pertinente; sem dependência, ferramenta de qualidade ou limiar novo |
| ADR-019/033/034/035 | Handshake/reparação existentes são entradas; TCPv2/19 tipos/3 rejeitado preservados, sem implementar reparação no routing |
| Q-C-35/40/41 | Grant saudável sobrevive a seleção divergente/ERROR14; revisão crescente, poda exata e sem ressuscitar lease expirado |
| Q-C-43/44 | Intenção/interesse limitado, novidade F/G, crédito coalescido, watermark e rondas/cadência limitadas, incluindo RESOURCE_LIMIT temporário |

Q-C-43/44 já têm decisão humana; esta revisão confronta a sua concretização com
as APIs. Não introduz novo gate externo nem presume aprovação de Implement.

## 3. Geração, watermark e momento da observação

**API real:** RouteCandidate expõe streamId/serverId/serverEpoch/upstream/path,
announceSeq, metric, generationStartedAtNanos e candidateTtlNanos. RoutingTable
projeta esses valores dos slots validados em project (linhas358–413). O path já
inclui o nó local. SessionKey contém nodeId/instanceId/connectionId; a Session
completa, incluindo endpoint, obtém-se do mapa sessions do RoutingService,
validando igualdade da chave e current(Session), nunca apenas nodeId.

O watermark interno Slot.sequence é privado. Não existe getter público para
ele e não é necessário acrescentá-lo. announce (RoutingTable632–709) devolve
Outcome com Status/Reason; rejeita contexto/path inválido, ignora sequência menor,
duplicado, geração retirada ou expirada. Mesma sequência pode ser APPLIED por
melhoria de custo/path, preservando began: **APPLIED não significa G novo**.
expire conserva a sequência mesmo quando retira o anúncio; o candidato deixa
de aparecer, sem permitir reancorar a mesma geração por replay.

**A01 — adaptação prevista em RoutingService:** observar o anúncio apenas depois
de root.announce e applied(outcome), no handler announce atual (linhas354–383).
Ler G/F da vista validada e cruzar Session/época atuais. Registar o maior G
observado no cursor/Line limitado, inclusive durante pedido pendente e quando
uma amostra ainda não permite seleção. Na falha bloquear esse máximo; não usar
apenas G da tentativa inicial, pois um ANN entretanto observado não é novidade
posterior à falha. O cursor do Tree conserva a prova corrente mesmo se H impede
criar uma Line; não criar mapa paralelo de históricos sem teto.

Receção validada significa aqui observação/admissão no owner do RoutingService:
onNeighborEvent exige owner e announce executa nesse turno. Received transporta
Session e mensagem, sem timestamp remoto ou de leitura do socket. Capturar o
tempo monotónico local dessa validação, além de G; não deduzir idade a partir de
requestId, UUID, tempo de callback ou publicação de snapshot. A política não
prova quando a fonte remota gerou os bytes nem promete ordenar relógios remotos.

Para F igual depois de falha reavaliável, só o ANN validado observado depois da
falha, com G superior ao bloqueado/consumido, concede crédito. Repetição da mesma
geração não atualiza a prova temporal. Métrica/refresh sozinhos não são geração
nova; primeira elegibilidade causada por evento validado segue o Design, sem
transformar cada alteração de RTT num novo incidente. A capacidade libertada
continua incapaz de criar crédito. A próxima janela só pode consumir crédito já
existente e ainda válido; a falha de admissão bloqueia-o mesmo sem envio.

Isto realiza R06/R10/R13 sem acesso a Slot privado, alteração de RoutingTable,
timestamp wire ou modificação do evento do manager. Esses oráculos não correram.

## 4. Intenção admitida e resultado terminal

**API real:** requestSubscription/retrySubscription devolvem SubscriptionTicket
(operationId/result/cancel). Local já separa cancelled e outcome, e outcome()
só escreve quando outcome==null (RoutingService916–922,985–1008). localTickets,
Tree.locals e localOrder conservam objetos após vários terminais, mas demand()
atual só conta operação pendente ou token em confirmedLocals. Perda do grant
limpa confirmedLocals; logo, conservar o objeto atual não preserva por si só a
procura recuperável. startRound também só serve os disparos atuais de admissão.

**A02 — adaptação prevista:** distinguir intenção efetivamente admitida do estado
do ticket. A intenção não cancelada conta para recuperação mesmo após SUCCESS,
ERROR ou TIMEOUT; resultado terminal antigo permanece imutável. Recuperação
interna não chama retrySubscription, não cria consumerToken/ticket falso e não
duplica L. Não converter recusas pré-admissão (UNKNOWN_STREAM, ausência de época,
capacidade L/C ou owner rejeitado) em intenções persistentes. Cancelamento só
retira a intenção cuja operação continua corrente; retry explícito substituto
preserva a proteção contra cancelamento antigo.

**A03 — mudança de época:** epochChanged atual (1439–1446) termina operações e
remove Tree, filhos e Lines. Deve retirar a autoridade/lineagens antigas e
reassociar apenas intenções locais já admitidas à época corrente da mesma origem,
sem perder a reserva L, o cancelamento corrente ou a cadência por stream. Não
copiar filhos da época retirada nem inferir ordenação de UUID. Transferência
interna cabe em RoutingService; não requer API nova em Subscription/OverlayNode.
R02/R03/R05/R07/R08/R12 terão de demonstrar estas propriedades posteriormente.

## 5. ACTIVATE, revisões e renegociação descendente

**API real:** LineKey=(direção,stream,epoch,Session completa), Line guarda UUID,
revisão, request, espécie/fingerprint e terminal. incoming (1075–1130) permite
ACTIVATE11 de revisão superior na mesma Line, recusa outro subscriptionId,
revisão inferior e duplicado conflitante. RENEW14 exige grant anterior vivo.
stepTree cria revisão com ++line.revision e bloqueia MAX_VALUE (1222–1238).
send marca submitted/issued/submittedRevision antes de chamar ControlPort
(1335), incluindo callback síncrono. PruneRequest captura Identity/revisão.

**A04 — adaptação prevista:** reativar com o ID da Line existente na mesma
Session/época e revisão superior a todas as emitidas, incluindo14 em voo;
requestId novo. Não reduzir contador, trocar UUID para contornar watermark ou
usar14 para recriar autoridade expirada. Nova Session/época tem contexto próprio.
Podas antigas continuam ligadas à identidade capturada; ACK/ERROR antigos
precisam de validar também a ronda/geração corrente antes de instalar ou retirar.
O dispatch de ERROR11/13/14 já precede trace, e a correlação11/14 já distingue
espécie/request/Session (338–344,1065–1071); preservar essas barreiras e M07.

**A05 — interesses filhos:** stepChild atual pode apagar active/detachar quando
perde upstream (1245–1278). Conservar no slot D apenas o interesse permitido
por Q-C-43 até ao deadline original, separando-o da autoridade publicável.
Child.began/budget mede o pedido pendente; Subscription.validity mede o grant.
São prazos distintos: filho já confirmado não deve expirar pelo velho prazo T
do seu pedido nem ganhar T novo por suspensão. Pending conserva o seu deadline;
ativo suspenso conserva apenas o restante do lease original. Ao expirar, retirar.

No filho, ANN novo pode marcar rebind, mas seleção/ERROR14 não retiram grant
saudável. Suprimir renovação inútil sobre a mesma prova; ao perder autoridade,
fazer11 automático limitado pela intenção e crédito atuais. Recuperação do relay
não reinstala filhos por cópia nem envia12 espontâneo. Novo pedido compatível
pode conceder grant novo após ACK upstream. Sem filhos válidos nem intenção
local, relay não conserva procura fantasma; pedidos frescos dos descendentes
reconstroem os saltos. Tudo cabe nos contextos internos existentes.

Subscription.Lease já ancora duração e calcula restante por subtração; ACK filho
é limitado/revalidado pelo upstream na fronteira do port (1267–1277,1314–1332).
Manter essa barreira, floor em ms e validação de prazo; não editar Subscription.
R04/R09/R11/R14/R18 e os históricos M07/L09–L19 serão verificação futura.

## 6. Construtores e projeções

| API real | Compatibilidade exigida pela entrega futura |
|---|---|
| Route, record de13 componentes | Preservar componentes/construtor e invariantes INACTIVE/UNAVAILABLE de seleção; Recovery será valor aninhado, não componente de Route |
| Route.Confirmation e visibleAt(long) | Continuar a representar só autoridade confirmada; leitor filtra lease expirado, sem reabrir operação nem estender grant |
| Snapshot de5 componentes (140–147) | Novo sexto recoveries imutável; manter construtor público4 existente e acrescentar overload5 com a assinatura canónica atual, ambos encaminhando recoveries vazio |
| emptySnapshot()/publish()/snapshot() | Default vazio e publicação pelo owner, capturando catálogo/rotas/forwarding/recoveries no mesmo instante; sem mutação por leitor |
| OverlayNode.routingSnapshot() e consumidor CLI | Continuam a usar a mesma classe/accessors; catálogo/routes mantêm seleção; esta entrega não cria comando play ou modifica apresentação CLI |

**A06 — adaptação prevista:** copiar defensivamente recoveries e os seus valores,
limitar por C, separar WAITING/ACTIVATING/UNAVAILABLE/CONFIRMED do estado legado.
Um snapshot antigo é observação ao tempo capturedAt, não garantia de lease vivo
para sempre; visibleAt continua a ser o filtro de confirmação. Metadados de
recuperação não autorizam forwarding. Referências reais aos construtores foram
pesquisadas; ActivationTest361–362 verifica4 argumentos/mapa imutável. RecoveryTest
deverá acrescentar a cobertura de4/5/6 argumentos sem mudar esse teste histórico.

Preservar construtores assegura compatibilidade de chamadas Java após recompilar;
acrescentar componente altera a forma do record (equals/toString/reflexão e
descritor canónico). Não se promete compatibilidade binária ou de serialização
para clientes externos não presentes. Nenhum contrato wire usa este snapshot.

## 7. Capacidade, contexto físico e lifecycle

Os tetos C/D/L/H/B/J já estão em ActivationEngine898–900; envios/quotas/passes
e scheduler partilhados existem em1280–1416/1463–1478. A contagem de tentativa
já ocorre na fronteira do port; falta o cursor de crédito/cadência e a política
automática, não novos parâmetros ou pools. T/A vêm de RuntimeConfiguration.

**A07 — adaptação prevista:** o contexto físico upstream atual é a Line passada
em send(a.line,...) e na poda. Trocar parent/época/Tree pode criar outra Line e
ocultar reservas antigas perante2/contexto. Usar contexto canónico da stream
partilhado por REQUEST/PRUNE de todas as gerações, por exemplo o Row estável
já retido em rows; ACK/ERROR filho mantêm a sua Line/contexto e limites partilhados.
Não usar novo Attempt/Round/requestId como chave física. Não retirar ActSend
para liberar slot por timeout lógico, cancelamento ou limpeza de histórico.

Timers já têm um worker, até2 jobs e um wake coalescido; acrescentar os instantes
de fim de janela/crédito a nextDelay/schedule. Timer/libertação apenas reavaliam
ronda vigente ou crédito válido; não criam retry espontâneo terminal. Preservar
16 ações por passe e todos os limites partilhados, incluindo envios retirados.

**API real de fecho:** requestClose/ownerStopped chamam cleanup; cleanup chama
activation.close. awaitTermination espera scheduler, admissions, traceSends e
activation.sends usando o menor budget restante desde firstStop (866–894).
ActSend.complete e completed mantêm callbackDone/continuationDone e release só
remove quando ambos terminaram (965–975,1343–1373). close pode terminar a parte
lógica cancelada, mas não declara callback ainda em execução terminado.
OverlayNode já chama requestClose e awaitTermination do routing no seu shutdown
global (353–358,449–455); não é necessária alteração de integração neste ficheiro.

**A08 — adaptação prevista:** incluir os novos cursors/interesses/rebinds na
limpeza lógica de ActivationEngine, sem criar reservas fora de sends. Depois de
stopping não emitir11/13/14 nem rearmar por ANN/callback/timer. Await(0) continua
false enquanto houver físico; nenhum budget adicional por ramo/recuperação.
CLOSED/OVERFLOW corrente retira só essa Session; evento antigo não retira a
substituta. R13–R15 testarão estas condições; não foram executados agora.

## 8. Limites da conclusão e checkpoint

A01–A08 são adaptações concretas previstas dentro do âmbito aprovado, ainda não
aplicadas. Não são correções descobertas que autorizem editar outros componentes.
O teste novo deverá usar clock/owner/port/scheduler controlados e integração
pertinente, sem alterar ou enfraquecer históricos, asserções ou prazos.

Leitura estática e hashes em
[adr-01](../../../experiments/construction/008-arvores/adr-01/README.md).
V-ADD e git diff --check são verificações documentais separadas. V-016/R01–R18,
regressão JDK21, manual humano, revisão final Fillmore e integração continuam
pendentes;1379 testes anteriores são histórico. Sem gate externo adicional.

Revisão ADR apresentada para aprovação. Sem Implement, Java alterado, Maven,
etapa Test, fecho, staging, commits, push ou início de outro IMP. Uma eventual
inviabilidade futura segue ADD11: apresentar prova/impacto/patch proposto antes
de alterar código ou âmbito. Não existe impedimento de ampliação identificado
nesta inspeção estática.
