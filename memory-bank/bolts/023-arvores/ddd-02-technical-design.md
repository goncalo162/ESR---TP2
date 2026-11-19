---
unit: "004-arvores"
bolt: "023-arvores"
stage: "design"
created: "2026-11-18T16:04:20Z"
status: "complete"
---
# IMP-012 — Design de leases renováveis e poda agregada

Model aprovado pelo utilizador, com Q-C-40–42 reconfirmadas. **Design aprovado pelo utilizador; apenas revisão ADR autorizada.**
As concretizações deste documento foram aprovadas, incluindo matriz13/14, revisão
a podar, correspondência exata, cadência, prazos e limites físicos. As menções
a proposta/checkpoint pendente nas secções abaixo conservam o texto apresentado.
A aprovação de Design não é aprovação ADR nem autorização de Implement. Base operacional main/b273b258a4642f94f8614ae00f060a46318d77a5.
Sem alterações Java, execução Java/V-015, staging, commits ou push.

## 1. Fontes, arquitetura e allowlist exata

[Model aprovado](ddd-01-domain-model.md), [ADD](../../../add.md), IMP-012 e
ADR-005/024; contratos07/3–7, estratégia09, DoD14 e TASK-015. Reutilizar
ADR-014/016/017/018/020/021/026, TCP v2/ADR-034/035 e Q-C-34–42.
Não há nova biblioteca, DB, HTTP, transporte ou executor de estado. A composição
é o agregado volátil existente dentro de RoutingService no owner emprestado,
port assíncrono e scheduler comum. Projeção de leitura imutável, sem persistência.

Os únicos caminhos Java previstos para Implement são:

1 - `Projeto/src/main/java/pt/uminho/esr/ott/routing/Subscription.java`.
2 - `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`.
3 - `Projeto/src/test/java/pt/uminho/esr/ott/routing/SubscriptionLeaseTest.java`.

O terceiro é novo. Não alterar Route, RoutingTable, OverlayNode, NeighborManager,
NetworkMonitor, codec, transporte, configuração/POM ou testes históricos. Q-C-42:
sem CLI stop/play; consumidor usa SubscriptionTicket existente e mensagens de rede.
Preservar enums/códigos, records, cópias defensivas, TCP v2/19 tipos/código3
rejeitado, framing37..65536/UTF-8 estrito e UDP OTT1/64/1200/1136 intactos.

O confronto estático com as APIs reais foi realizado em Model. Nesta etapa usa-se
esse registo e os contratos aprovados, sem nova leitura/alteração de fontes.
Todos os tipos auxiliares propostos ficam aninhados nos ficheiros autorizados.

## 2. APIs e composição

| Porta/API | Contrato preservado ou adição interna proposta |
|---|---|
| requestSubscription(UUID streamId, UUID consumerToken) | Admissão local existente; token repetido não duplica procura; ticket consultável sem callback arbitrário no owner |
| retrySubscription(UUID, UUID) | Nova operação explícita após terminal; não equivale a timer de renovação nem recuperação automática |
| SubscriptionTicket.operationId/result/cancel | Assinaturas preservadas; cancel usa identidade da Local/operação; resultado SUCCESS antigo não representa a validade atual do ramo |
| cancelPendingChild(Session, UUID streamId, UUID subscriptionId, long revision) | Assinatura package-private preservada; cancelamento exato continua testável, incluindo M07; partilha guardas com DEACTIVATE, sem eliminar substituta |
| onNeighborEvent(NeighborManager.Event) | No owner, incluir13/14 e ERROR related13/14 antes de trace; manter11/12/ERROR11, anúncios e restantes eventos |
| onMetrics/setLocalAvailable | Preservar eventos e isolamento; perda de contexto/disponibilidade invalida só o que depende deles |
| snapshot() / Snapshot.forwarding | Mesmo formato público, mapas imutáveis e construtor compatível; publicação de autorização atual, sem mudar seleção/catálogo |
| Subscription / Subscription.Lease | Preservar componentes/construtor/State PENDING e ACTIVE; Lease usa início+duração, remainingNanos/remainingMillis existentes; validar limites/null/size antes de List.copyOf/instanciar novos valores, preservando validação e cópias defensivas |
| ControlPort.send(Session, ControlMessage) | Mesmo CompletionStage<SendResult>; ACCEPTED só enfileira, não prova admissão remota ou ACK |
| Scheduler/Ticket e requestClose/awaitTermination | Mesmo alarme/owner e primeiro budget de shutdown; reservas reais até callback e continuação acabarem |

Não é necessária nova API pública. Assinaturas internas conceptuais, a implementar
sem criar ficheiros novos: `receiveRenew(session, requestId, Activation, now)`,
`receiveDeactivate(session, requestId, Deactivation, now)`,
`effectiveDemand(tree, now)`, `renewalDue(tree, now)`,
`retireBranch(tree, generation, cause, now)` e `tryPrune(pruneIntent, now)`.
Métodos internos já existentes podem delegar nestes auxiliares; os nomes públicos
anteriores e a semântica de wants para ativação inicial são preservados.

A validação do construtor de Subscription pode ser reordenada internamente para
rejeitar listas fora do cap antes de copiar/iterar; sem trocar componentes,
construtor público, enum ou enfraquecer invariantes/testes existentes.
Subscription não recebe serviços, threads ou futures. Estado do ciclo vive no
ActivationEngine; Lease continua um valor, não um scheduler. Route.Confirmation
existente permite publicar novos leases/destinos sem alterar Route.java.

## 3. Estado lógico e representação limitada

Por Tree/stream/epoch:

- geração lógica do ramo, diferente de revisão wire; confirmação vigente opcional;
- uma operação upstream inicial OU uma renovação lógica pendente, nunca duas
  renovações correntes; referência ao grant anterior e à geração capturada;
- procura local/filhos, último início de ciclo e cadência associada ao grant;
- no máximo uma intenção de poda, usada quando não há procura; sem fila de podas.

Por Line existente: direção/stream/epoch/Session, mesmo subscriptionId, revisão
máxima admitida/submetida segundo a direção, requestId/fingerprint e espécie11/14
da operação canónica, terminal resumido. Identidade lógica da geração protege
callbacks; não se reutiliza UUID de pedido, ordena UUID ou guarda lista de todos
os pedidos. Uma nova Session/epoch retira o histórico lógico correspondente.

Por Child: concessão ACTIVE anterior opcional e no máximo uma operação de
renovação pendente. A concessão pode continuar válida enquanto essa operação
aguarda ACK local/admissão de resposta. Admitir revisão nova substitui contexto
lógico anterior, mas transfere o valor confirmado saudável, sem copiar mapas
ou libertar a reserva física do envio antigo. Line/Child/operação mantêm guardas
de identidade; contexto detached não pode publicar.

Terminais são específicos da operação. Falhar RENEW é diferente de cancelar
a geração inteira: um terminal de renovação impede ACK tardio dessa operação,
mas não apaga o grant anterior válido. Cancelar a geração remove também o grant
anterior e deixa watermark que impede replay. Preservar o efeito da guarda M07;
não a substituir por teste genérico !pending que rejeitaria ACKs legítimos.

Estados internos conceptuais: QUIESCENT, ACTIVATING, CONFIRMED, RENEWING,
RETIRING e EXPIRED. Não se acrescentam estes valores ao enum público Subscription.State.
Histórico terminal e reserva física não contam como procura nem destino.

| Evento | Alteração no mesmo turno do owner |
|---|---|
| Cancelar C1 havendo C2 | Retirar só C1; manter confirmação e cadência upstream |
| Última procura retirada | Retirar forwarding, invalidar operação lógica, capturar poda; sem renovar |
| Renovação elegível | Criar no máximo uma operação14 com correlação nova e prazo cortado pelo grant antigo |
| ACK12 válido + ACCEPTED corrente | Substituir concessão, publicar e recalcular cadência uma vez |
| Erro/timeout da renovação | Tornar operação terminal; preservar grant anterior ainda válido, sem alargar prazo |
| Expiração | Retirar autorização atual; não permitir14 como nova ativação |
| DEACTIVATE exato | Cancelar geração/child correspondente e a renovação pendente; recomputar procura |
| NeighborDown/epoch nova | Invalidar apenas o contexto correspondente, preservar outros; sem reconstrução automática |
| Shutdown | Encerrar admissão, retirar autorização, cancelar intenções; esperar físico fora do owner |

## 4. Admissão e correlação de RENEW

Validar envelope v2, Session completa corrente, stream/epoch, IDs, revisão,
lease1..u32 e path2..32 simples antes de criar contexto ou copiar listas. Um RENEW
só é admitido quando existe concessão do mesmo child/ID/Session ainda não expirada
e confirmação upstream saudável/oferta local disponível. Tipo14 não cria ramo
nem reabre terminal cancelado. RENEW que chega antes de o ACK de ACTIVATE ter
admissão local pode ser recusado NO_ROUTE sem invalidar a ativação pendente.

Q-C-41: prefixo selecionado=confirmado e Session/epoch compatíveis. Caso contrário,
ERROR14/PATH_CHANGED sem mutar grant/parent/destinos. Recusa antes da admissão não
consome watermark nem remove estado saudável. Saturação também recusa atomicamente.

Q-C-40: requestId novo, revision maior, mesmo subscriptionId da Line. O ledger
inclui a espécie11/14 no fingerprint: mesmos campos/request não autorizam trocar
ACTIVATE por RENEW. Revisão menor é STALE_REVISION; igual e pedido diferente é
REVISION_BOUND; igual com campos/espécie diferentes é INVALID_MESSAGE. Duplicado
exato pendente coalesce; após sucesso devolve apenas o restante original, dentro
da quota/deadline; após terminal devolve terminal limitado, sem reaplicar concessão.

ACK12 é desmultiplexado por espécie local e tuplo completo
(Session, stream, epoch, subscriptionId, revision, requestId, geração/operação).
Guardar no máximo um ACK antecipado enquanto o envio do pedido aguarda ACCEPTED;
só confirmar quando ambos forem válidos. ACK duplicado não reancora lease.
ERROR11/14 só resolve a operação correlacionada de mesmo tipo; ERROR13 é diagnóstico
de poda correspondente, sem cancelar ativação/renovação de uma geração posterior.
Trace15 e outros ERROR/eventos mantêm o dispatcher anterior, sem ERROR sobre ERROR.

## 5. Corrida RENEW / DEACTIVATE e revisão a podar

Cada ramo upstream conserva `lastSubmittedRevision` e a revisão confirmada,
ambas ligadas à mesma geração lógica e Session. Uma revisão reservada localmente
mas ainda não entregue ao ControlPort não é submetida. Antes de invocar o port,
marcar a submissão no contexto capturado, permitindo callback síncrono sem perder
correlação. Isso não significa que o parent a admitiu.

Ao retirar a última procura, capturar uma única intenção imutável:
Session, stream, epoch, subscriptionId e **maior revisão submetida daquela geração**;
na ausência de renovação submetida, usar a revisão do ACTIVATE/ramo retirado.
Invalidar a operação lógica antes de preparar qualquer envio. Não consultar uma
Tree/Line substituta ao completar callback ou tentar enviar a poda. DEACTIVATE
usa requestId próprio e não espera ACK de sucesso (não existe no wire).

No recetor, DEACTIVATE só altera Line de Session/stream/epoch/ID exatos cuja
revisão máxima admitida coincide com a revisão recebida. Nesse caso retira a
renovação e a concessão anterior transferida dentro dessa geração, conserva
terminal e recomputa procura. Repetição é no-op idempotente; revisão menor,
maior desconhecida ou outro ID não altera ramo e pode receber ERROR13/
STALE_REVISION limitado por J/B/quota, sem criar histórico novo para o erro.
Não usar >= para cancelar uma subscrição atual nem aceitar UUID arbitrário.

| Ordem / corrida | Resultado proposto |
|---|---|
| Grant r, renovação r+1 só reservada, cancelamento | Não submeter14; poda captura r; ACK antigo não reativa |
|14 r+1 submetido, cancelamento antes de ACCEPTED | Poda captura r+1; sem nova confirmação local, B do envio continua reservado |
| Parent admitiu14 r+1 e conserva grant r durante ACK | DEACTIVATE r+1 cancela ambos os estados dessa geração; ACCEPTED/ACK tardios ignorados |
| Parent recusou/não recebeu14 r+1, só conhece r | DEACTIVATE r+1 não cancela por aproximação; ramo r termina pelo lease, sem nova renovação |
| Nova ativação explícita r+2, depois poda r+1 | Revisão antiga não remove r+2; callback da geração retirada não altera a substituta |
| Cancelamento r chega depois de RENEW r+1 admitido | Não remove a revisão nova, mesmo enquanto grant antigo ainda é o valor publicado |
| Nova Session com mesmo nodeId/instanceId | ConnectionId diferente torna mensagens/callbacks/poda antigos obsoletos |
| ACK chega antes do callback ACCEPTED, depois ocorre cancelamento | ACK retido é descartado logicamente; finally/contabilização físicos mantidos |

O caso de parent conhecer apenas r é deliberadamente best effort, conforme Model:
não há prova de admissão remota no SendResult. Não enviar uma segunda poda de
fallback, inventar ACK, baixar revisão ou manter forwarding para aguardar entrega.
A garantia de limpeza remota é o lease finito efetivamente concedido. Bytes14 já
aceites antes de cancelar podem chegar depois e originar um grant remoto antes
da poda; não se promete limpeza pelo instante do grant que o requerente conhecia
no cancelamento. Não há novos envios após a retirada, e a limpeza segue o último
grant remoto válido e finito; não se deduz uma meta7s sem medição.
Em TCP saudável, pedidos aceites
na mesma Session preservam FIFO; isso não prova que o pedido foi aceite pela regra
de domínio nem autoriza remover revisão diferente.

A intenção de poda tem uma tentativa de envio: se B/quota de trabalho estiverem
ocupados, aguarda dentro do prazo local T=activateTimeout capturado no cancelamento,
sem reiniciar o prazo. Ao chamar o port é consumida; ACCEPTED não prova aplicação
remota. INVALID_MESSAGE/exceção termina essa tentativa; CLOSED/OVERFLOW da Session
corrente seguem a política existente de perda dessa conexão. Sem retry de rede
ilimitado, fila de reenvios ou socket/future adicional para poda.

Nova procura explícita antes de poda ainda não submetida invalida essa intenção
antiga, sem retargeting: o ramo anterior limpa pelo lease se necessário. A nova
ativação usa revisão maior da mesma Line; não cria outra intenção de poda para
cada consumidor. Poda já submetida mantém referência/recursos até acabar, mas
não modifica a nova geração. Long.MAX_VALUE termina por STALE_REVISION/
REVISION_EXHAUSTED sem wrap; só novo contexto de Session permite outra lineagem.

## 6. Leases, cadência e prazos concretos

Usar pares início/duração e diferenças monotónicas verificadas, não somas de
instantes que transbordem. G é grant wire em ms, convertido com multiplyExact;
P = min(config.renew.toNanos(), floor(G*1000000/3)). Como G>=1ms, P>=333333ns;
nenhum ceil/clamp artificial para1ms. Um relógio fora do horizonte suportado
mantém o tratamento de erro operacional existente, sem oferecer mais tempo.

Concessão remota confirmada por ACK da renovação começa no início local t0 dessa
operação, capturado antes da fila. Restante novo=max(0,G*nanos-age(now,t0)).
Não reancorar na receção/ACCEPTED; grant positivo mas já expirado não confirma.
Grant >pedido/máximo é inválido e não substitui o anterior.

Por concessão confirmada, guardar duração G e P constantes. **Não recalcular
P como restante/3 a cada wake**, que criaria aproximação infinita ao deadline.
Primeira elegibilidade: idade desde início da operação que confirmou o grant>=P.
Após tentativa falhada: elegibilidade seguinte a P desde o último início real de
ciclo, enquanto houver grant anterior saudável e procura. Ciclo novo usa revisão/
requestId novos; não retransmite o mesmo pedido automaticamente. Cada ciclo14
tem uma tentativa de envio, sem gastar/contornar as A tentativas da ronda inicial11.
Depois de um erro, outro ciclo periódico só é elegível dentro do grant antigo
ainda vivo; expirado esse grant não se reinicia ACTIVATE por timer.

Se o alarme ou ACK chega depois da elegibilidade, admitir no máximo um ciclo agora,
registar o seu início real=agora e não recuperar períodos perdidos. Se esse ACK
permitir outro ciclo imediatamente elegível, só haverá uma operação corrente e
um início novo=agora; um novo grant confirmado nessa operação terá elegibilidade
futura positiva. Não criar lista de ticks vencidos nem vários14 no mesmo instante
para compensar atraso. Recursos indisponíveis mantêm uma só intenção/contexto.

Prazo do ciclo upstream: min(T, restante do grant anterior na admissão). No child,
prazo de resposta: min(T, restante do grant anterior do próprio child); espera por
B/quota/fila/ACK/callback consome esse mesmo prazo. Um ACK ou ACCEPTED só instala
se o ciclo ainda é corrente, há procura, ambos os grants/contextos relevantes
continuam válidos e o deadline não venceu (comparação >= termina exatamente).
Ao prazo, tornar operação terminal; manter apenas o grant anterior ainda válido.
Se este expira, retirar autorização e cessar renovação, sem ACTIVATE por tick.

Na fonte, RENEW válido cria grant novo<=min(pedido,máximo local), podendo exceder
restante do child antigo. No relay, limita também ao restante upstream na emissão.
Só uma renovação14 explicitamente admitida pode estender lease; ACTIVATE11 e replay
exato continuam sem renovação implícita, preservando os testes de IMP-011.
Revalidar/floor novamente imediatamente antes de ControlPort.send. Depois não
alterar bytes. Se resta<1ms concedível, NO_ROUTE/GRANT_TOO_SHORT, sem arredondar
para cima nem remover antecipadamente a concessão anterior ainda válida.

Receber RENEW de child não força um pedido upstream por child: responder a partir
do grant corrente, sujeito a T/lease/recursos. A renovação upstream agregada tem
cadência própria. Pode haver espera pelo trabalho já agendado, mas nunca outro
ciclo/fanout a montante ou bloqueio do owner para obter um ACK melhor.

Exemplos de oráculo, não medições: grant3000ms e renew2000ms implica P1000ms;
grant300ms implica P100ms; grant1ms implica P333333ns. Atraso que deixa1ns de
lease continua a valer só1ns; o novo pedido não ganha T completo. Grants muito
curtos/carga que ultrapasse16 respostas/Session/janela não têm continuidade
prometida: recusa/expiração segura e diagnóstico limitado, sem alterar caps.

## 7. Procura, expiração órfã e fonte

Separar intenção vigente, pedido inicial pendente e destinos autorizados.
Renovar upstream só com intenções locais vigentes associadas à geração ou filhos
ACTIVE de Session atual com lease>0; pending inicial é tratado pelo fluxo de
ativação existente, não licença para renovar grants de uma geração cancelada.
Históricos/erros, catálogo, trace e envios físicos sozinhos não são procura.

Sair um de dois consumidores retira só o seu destino/token, sem mudar confirmação,
lease ou referência dos outros. Ao sair o último, retirar confirmação utilizável
e parar novas renovações no mesmo turno do owner que processa o cancelamento.
Não se promete mutação síncrona antes de esse evento ser processado. Consultas de
forwarding usam a publicação atual; snapshots antigos não se tratam como leitura
nova após cancelamento. visibleAt continua a excluir expiração sem mutar o owner.

Agendar no alarme comum os próximos prazos de grants e operações, mesmo sem rede
nova. O sweep limitado retira Child expirado e conserva terminal enquanto o contexto
for corrente. Recalcular procura pode disparar a poda do tronco; o prazo final de
limpeza remota inclui lease já concedido, não se apresenta como ACK de DEACTIVATE.
Metas experimentais de poda<=7s não foram executadas nem garantidas aqui.

A fonte sem procura publica zero forwarding, mantendo anúncio/oferta e sem parent
para podar. Não lê/fecha media neste IMP. Intenção local sobrevivente a indisponibilidade
pode manter ticket SUCCESS histórico, mas autorização vem do snapshot atual;
reconstrução de ramo por falha/troca pertence a IMP-013/014 ou nova operação explícita.

## 8. Limites físicos e partilha

Preservar parâmetros e limites de IMP-011; nada é aumentado para acomodar o recurso.

| Recurso | Teto partilhado / representação proposta |
|---|---|
| Trees C | min(E,256), incluindo Tree sem procura com intenção de poda ainda viva |
| Children D | min(E,256), até N/stream; renovação substitui contexto dentro do slot, conservando grant anterior por valor |
| Pendentes por Session | Até4, partilhados entre ACTIVATE e RENEW de children; substituição do próprio contexto não cria outro slot |
| Tokens L | min(E,64), até4/stream; mesma API e reserva antes de offer |
| Históricos H | min(E,1024), até2N/stream por direções/sessões; mesmo ID/watermark, espécie11/14 no fingerprint, sem lista de revisões |
| Envios B | min(E,64), até4/Session e2/contexto, somando11/12/13/14/ERROR de ativação/renovação/poda |
| Erros sem slot J | min(E,16), até1/Session; partilha B, descarte limitado sem backlog |
| Resposta | ACK/ERROR até16/janela fixa1s por Session; pedido RENEW/DEACTIVATE não é resposta nem ganha pool paralelo |
| Intenções | Uma renovação por Tree, uma resposta por Child, uma poda por Tree em retirada; dentro de C/D, sem nova fila |
| Trabalho | Mesmo passe<=16 ações:4 conclusões+4 trees+6 children+2 locais; cursores cedem com trace/anúncios |
| Scheduler | Mesmo alarme,<=2 jobs físicos e1 wake; nenhum timer por stream/operação |

N=min(control.maxConnections,63). Validar Session/IDs/path/capacidades antes de
copiar/alocar. Novos valores só após reserva; rejeição não expulsa confirmação,
watermark vivo ou substituta. Saturação de contexto não gera uma lista de intenções.

Todos os envios upstream de uma stream (ACTIVATE/RENEW/DEACTIVATE), incluindo os
retirados ainda físicos, usam a referência canónica estável do seu contexto, sem
escapar a2/contexto pela criação de um objeto/generation novo. ACK/ERROR de child
usam Line estável; não se usa UUID recém-criado como chave de quota física.
Sessão nova tem identidade própria, mas antigos envios continuam a ocupar B global.

Reserva antes do port; callback publica resultado/ready e apenas agenda trabalho.
Continuação valida geração/identidades antes de mutar e marca continuationDone no
finally mesmo no caminho ignorado. release só quando callbackDone && continuationDone.
Não interpretar future.cancel(), fila vazia, limpeza de mapa ou ACCEPTED como
terminação física. Após retirar contexto lógico, o envio mantém apenas identidade/
valores necessários, sem cópia de tabela ou retenção desnecessária de coleções.

Se acaba o prazo de poda, libertar a intenção lógica/C elegível; B de envio já
submetido permanece. O H protegido só é retirado por Session/epoch, não por precisar
de capacidade. Uma Tree nova para a mesma stream não herda confirmação/cancelamento
da antiga; revisão da Line continua crescente e callbacks continuam isolados.

CLOSED/OVERFLOW da **Session corrente** mantém a remoção dessa conexão, afetando
naturalmente as streams que dependem dela; isso não é uma falha lógica isolada de
renovação. Resultado de Session antiga nunca remove a nova. INVALID_MESSAGE ou
exceção sem falha física termina só a operação corrente e preserva grant válido.
owner.offer(false) mantém o hook existente de shutdown, sem esperar bloqueante.

## 9. Shutdown global

requestClose bloqueia novas admissões/renovações e cancela intenções de poda ainda
não submetidas. Não criar fase adicional de drenagem, novo envio depois de stopping,
ACK13 fictício ou prazo T extra para cada ramo. O manager já tem lifecycle/LEAVE/EOF;
quando a notificação não chega, lease remoto limita o órfão. DEACTIVATE submetido
antes do fecho pode completar fisicamente, mas não volta a publicar estado.

Cleanup retira destinos/grants e termina operações/tickets ainda pendentes como
CLOSED, preservando resultados terminais já publicados. Cancela alarme/jobs por
hooks existentes; continuations que já foram limpas marcam a sua conclusão, mas
callbacks ainda em execução conservam B/J. Cleanup só substitui a continuação
lógica após executar a sua remoção no owner, onde não existe outra continuação
de domínio simultânea; não basta cancelar um future ou antecipar a flag.
Não usar timers para libertar contadores
que ainda representam threads/callbacks reais.

awaitTermination fora do owner usa o menor entre budget do chamador e restante
do **primeiro** shutdown, já incluindo scheduler e envios de todos os subsistemas.
Sem reiniciar orçamento por callback/stream/chamadas repetidas. Com callback retido,
await(0)=false; após callback+continuação e jobs terminados, true apenas se dentro
do orçamento. Se excede, falha explícita; não aumentar prazos/tests históricos.

## 10. Mapa L01–L24 / V-015 — por executar

Cada linha especifica grupos de asserts, não um total de métodos JUnit.
Os helpers ficam em SubscriptionLeaseTest; relógio, port, owner e scheduler falsos
controlam explicitamente a ordem. Não se executaram Java, benchmarks ou medições.

| ID | Exercício concreto / critério de aprovação futuro |
|---|---|
| L01 | S-R-(C1,C2) confirmado; cancelar C1, repetir stop e ticks por dois períodos; C2/lease/path exatos preservados, nenhuma13 no tronco, uma14 agregada por ciclo |
| L02 | Cancelar último local/child de ramo confirmado; snapshot vazio antes do future13 terminar; exatamente uma13 com revisão capturada, zero13 na fonte; B só liberto fisicamente |
| L03 | Local+child, retirar em ambas as ordens; último provoca poda, primeiro preserva outro e não cria14 por consumidor |
| L04 | Desaparecer child sem LEAVE/13 e avançar relógio a G−1/G; autorização só antes, sweep termina sem evento de rede, ramo órfão remoto limitado pelo lease |
| L05 | Três ciclos com ACK válidos S-R-C; ID estável, revisões crescentes/requests novos, referência única e grants conservadores |
| L06 | pedido6000/máximo3000, renew2000: grant3000/P1000; grant300/P100; grant1/P333333ns sem arredondamento para zero; quota não é aumentada |
| L07 | Dois relays, fila/send/ACK com atrasos; grant novo ancorado em t0; restante999999ns nunca vira1ms; ACK limite exato/oversized não confirma |
| L08 | Repetir14 exato pending/sucesso/terminal; um contexto/intenção, restante antigo; mesmo revision com request/fields/espécie diferentes rejeitado sem renovar |
| L09 | r+1/r+2 e request novo; ACK r+1 não substitui r+2; MAX_VALUE termina sem wrap nem UUID para contornar watermark |
| L10 |13 r depois de14 r+1 e13 r+1 depois de novo11 r+2; substituta e leases iguais. Mesmo token com ticket novo imune ao cancel velho |
| L11 | Matriz secção5: reservar/submeter/admitir14 antes/depois de cancel; ACK e ACCEPTED tardios, parent só conhece r, poda r+1 ignorada e expiração segura; outro child intacto |
| L12 | Callback bloqueado com continuação concluída e ordem inversa; B/J/2por contexto retidos, M07 finalmente executa, release único e wake após libertação |
| L13 |14 em concessão expirada/cancelada e antes de ACK11 admitido; não cria ramo nem revive terminal. Falha14 preserva grant anterior enquanto válido |
| L14 | Seleção B/confirmado A;14 por ambos os prefixos recusado PATH_CHANGED, parent/destinos/lease originais iguais até prazo; custo só diferente com mesmo path permite |
| L15 | Novo connectionId na mesma instância e nova instanceId; Down/ACK/13/callback antigo não altera Session nova, sem herança de grant |
| L16 | Duas streams da mesma epoch e outra origem; cancelar/renovar/expirar/errar uma preserva confirmação/procura das outras, exceto perda física da Session partilhada |
| L17 | CLOSED/OVERFLOW correntes retiram só essa Session; INVALID_MESSAGE/exceção termina operação sem conceder mais tempo; ACCEPTED sozinho não confirma |
| L18 | Intercalar ERROR11/13/14/15 e ACK12 de campos errados; cada correlação/tipo isolado, nenhum ERROR17 em resposta a ERROR17 |
| L19 | E pequeno e fronteiras C/D/L/H/B/J/quota; nova geração não escapa2/contexto, pending14/13 não cria pool/fila paralelos, estado saudável preservado; H protegido sem evicção |
| L20 | Alarme atrasado até depois de vários períodos, ACK tardio e falha; no máximo um ciclo atual, início real avança e P constante, sem remaining/3 em loop; wrap monotónico testado |
| L21 | Fonte oferta ativa sem consumidor: catálogo preservado/forwarding vazio/sem13 para si; cancelar A não fecha oferta B; nenhuma medição de bytes de vídeo presumida |
| L22 | Shutdown antes/depois de submissão14/13, callback preso e job físico; nenhuma submissão após stopping, deadline único, await(0)=false até terminar, sem prolongar orçamento |
| L23 | Overloads de Snapshot, Subscription/Lease/State e ticket preservados; cópias defensivas; validadores antes de novas cópias; seleção/catálogo/trace sem procura implícita |
| L24 | Integração TCP real posterior com source/relay/dois consumidores e dois relays; saída parcial/final, renovação além de um lease, expiração órfã e shutdown/portas; consumidor via API, sem alteração CLI/OverlayNode |

A integração real do teste comporá manager/monitor/routing usando owner/port
existentes, sem nova infraestrutura de produção. Distinguir teste fake do TCP
real e de manual humano. Nomes concretos dos métodos surgem na etapa autorizada;
preservar ActivationTest e restantes testes históricos integralmente.

## 11. Riscos, observação e checkpoint

Poda é best effort: maior revisão submetida pode não ter sido admitida remotamente;
a salvaguarda é o lease, não cancelamento aproximado. Prefixo divergente pode levar
a expiração/indisponibilidade sem troca automática, como Q-C-41 aprovou. Grants
curtos e quotas/capacidades pequenas podem impedir continuidade; provar segurança
e diagnóstico, sem esconder recusa, falha física ou inventar tempos/FPS.

Registar resultados limitados/razões nos diagnósticos locais existentes e ticket/
snapshots; sem stack trace/path de media no wire, log ilimitado ou novas dependências.
Valores de configuração não mudam política/wire/hard caps. Se uma ADR aceite se
mostrar inviável, aplicar ADD11, parar a parte afetada e pedir decisão; não se
identificou essa inviabilidade ao preparar este Design.

Propostas adicionais deste Design (corridas, revisão da poda, cadência/prazos,
intenção única e partilha física) aguardam aprovação integral do documento;
Q-C-40–42 mantêm o estatuto de decisões já aprovadas. Não se cria outra ADR por
inferência, nem se apresenta o desenho como implementação ou resultado de teste.

Parar no checkpoint Design, antes de ADR/Implement. Apenas Model em stages_completed;
Design em progresso, sem Java/testes Java/staging/commits/push ou outro IMP.
Fecho futuro exige V-015/L01–L24, regressão atual/clean verify, manual humano,
revisão final Denji e integração. Nenhum gate externo adicional.

## Aprovação de Design

Registo 2026-11-18T16:12:04Z: aprovação explícita do utilizador ao Design, incluindo a
matriz RENEW/DEACTIVATE, maior revisão submetida, match exato remoto/lease fallback,
P constante por grant, prazos remanescentes, pendência única e limites físicos.
Apenas revisão ADR autorizada, sem Java/testes Java/staging/commits/push.
