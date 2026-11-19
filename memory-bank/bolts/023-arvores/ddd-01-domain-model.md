---
unit: "004-arvores"
bolt: "023-arvores"
stage: "model"
created: "2026-11-18T15:48:41Z"
status: "complete"
---
# IMP-012 — Model de renovação e poda por procura

Model aprovado explicitamente pelo utilizador, incluindo Q-C-40–42. Apenas
Design autorizado; ADR/Implement/Test não iniciados. Texto de checkpoint no fim
conserva o estado histórico da apresentação anterior.
Base operacional main/b273b258a4642f94f8614ae00f060a46318d77a5, árvore/índice
limpos à entrada. Dependências IMP-011/007-arvores e IMP-010/022-rotas complete,
com TASK-014/TASK-013 DONE e fechos integrados na ancestralidade desta base.
Os1328 testes do IMP-011 são evidência histórica: nenhum teste Java executado aqui.

## 1. Fontes e fronteira

Enunciado → specs → intent → documentação → código; decisões no
[ADD](../../../add.md), secções2/5/6/8–12, especialmente IMP-012/ADR-005/024.
Lidos [specs](../../../specs.md), [intent](../../intents/001-ott-streaming/requirements.md),
[unidade](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md), história002,
bolt023, [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md), contratos07/3–7,
estratégia09, DoD14 e progresso15. Reutilizam-se ADR-014/016/017/018/020/021/026,
ADR-034/035 e Q-C-34–39; nenhuma ADR criada/substituída nesta etapa.

A leitura de código foi explicitamente pedida pelo utilizador e prevalece sobre
a proibição genérica de leitura do template Model da skill. Não se alterou Java.
Não há AGENTS.md aplicável encontrado na raiz/antecessores ou na pesquisa local.

Âmbito futuro Java, sem ampliar:

1 - routing/Subscription.java: valores de subscrição/lease, cópias defensivas.
2 - routing/RoutingService.java: agregado de procura, renovação/poda e lifecycle.
3 - routing/SubscriptionLeaseTest.java: novos oráculos V-015.

Os dois primeiros são existentes; o teste ainda não existe. Preservar assinaturas,
records/enums/códigos e os testes existentes. TASK-015 menciona NeighborManager,
mas o ADD e a autorização atual limitam aos três caminhos acima.
Q-C-42 aprovada: cancelamento pela SubscriptionTicket existente e DEACTIVATE de
rede; composição da CLI fica para trabalho posterior de cliente/aplicação.
Não se entrega CLI stop/play, emissão/receção de vídeo, GUI, CORE, recuperação
por alternativa ou troca de parent neste incremento.

## 2. Confronto com o código real

Subscription é um record imutável de stream/epoch/Session do filho/ID/revisão/
requestId/path/pedido/state/validity. Lease usa início+duração monotónicos,
remainingNanos com verificação de horizonte/wrap e remainingMillis com floor.
ACTIVE implica validade; não há conceito de renovação implementado nesse valor.

ActivationEngine já possui Tree, Line, Child, Round/Attempt, Local e ActSend.
Tree separa tokens locais, filhos e confirmação upstream. LineKey inclui direção,
stream, epoch e Session completa; Line retém ID/maior revisão/request canónico,
fingerprint e terminal. Pedido diferente na mesma revisão é STALE_REVISION.
Ticket local guarda operationId e resultado imutável: SUCCESS de uma operação
anterior não prova autorização atual, que se consulta em Snapshot.forwarding.

Handlers atuais aceitam11/12/ERROR11;13/14 não chegam ao engine, e ERROR14 cai no
ramo de trace sem tratamento de renovação. O codec já representa13 como
Deactivation e14 com payload Activation; ACK12 e ERROR17 existentes bastam.
NeighborManager.send aceita tipos de aplicação e exige Session atual/envelope;
não é necessária alteração ao manager/codec/transporte. OverlayNode entrega
Received ao routing e já aguarda o seu shutdown; sem nova porta para o core.

cancelPendingChild verifica Session/stream/ID/revisão exata no owner; cancelamento
local usa identidade do ticket/Local para não remover substituta. stepTree/stepChild
filtram expiração, mas não enviam DEACTIVATE nem renovam. wants(Tree) só cobre
operações locais pendentes/filhos pending, pelo que não pode ser reutilizado como
predicado de procura contínua sem adaptação dentro do engine.

M07: completed rejeita filho detached, identidade substituída e line.terminal antes
de instalar ACK. O finally marca continuationDone; release exige callbackDone e
continuationDone. Esta separação física/lógica é invariante a preservar também
para renovação e poda. Expirar/cancelar/retirar árvore não liberta envio em voo.

O scheduler comum integra nextDelay/drive com cursores, até2 jobs físicos/1 wake;
awaitTermination inclui sends de ativação, anúncios/trace e o scheduler, limitado
pelo primeiro início do shutdown e budget restante. Não se cria executor/timer
por filho, nem shutdown independente de renovação/poda.

## 3. Entidades e valores

1 - **Árvore de procura por stream/epoch:** raiz do agregado, com confirmação
upstream única (ausente na fonte), intenções locais e filhos da mesma stream.
A oferta local da fonte é catálogo/disponibilidade; não equivale a procura.
2 - **Intenção local:** (streamId, consumerToken, identidade da operação).
Permanece até cancelamento explícito; terminal de admissão, intenção e autorização
são coisas distintas. Cancelar ticket antigo não remove intenção substituta.
3 - **Lineagem de ramo:** direção/stream/epoch/Session, subscriptionId fixado,
revisão máxima admitida e watermark terminal. Não ordenar UUIDs, nem esquecer
cancelamento para aceitar replay. Histórico protegido retido no contexto vigente.
4 - **Concessão confirmada:** revisão/Session/path e Lease imutável que autoriza
um ramo agora. Enquanto renovação está pendente, a concessão anterior pode
continuar válida; não se prolonga por enviar ou repetir mensagens.
5 - **Operação de renovação:** espécie local RENEW, correlação própria e prazo
finito, mantendo referência à concessão que tenta substituir. Q-C-40: mesmo
subscriptionId, revisão estritamente crescente e requestId novo. Há no máximo
uma renovação lógica corrente por ramo upstream, partilhada pela procura agregada.
6 - **Intenção de poda:** identidade capturada do ramo antigo a retirar, nunca
uma consulta tardia ao ramo atual. DEACTIVATE não tem ACK de sucesso no wire.
A intenção tem vida/recursos limitados; não é autorização para continuar forwarding.
7 - **Envio físico:** reserva independente de contextos lógicos, associada a
Session/identidade da operação e a callback/continuação. Sobrevive ao cancelamento
lógico até ambos terminarem, dentro dos tetos existentes.

Não existe DB/repositório persistente. Os mapas do RoutingService constituem o
repositório volátil do agregado; snapshots imutáveis são projeções de leitura,
sem devolver entidades mutáveis nem criar ficheiros/repositories Java adicionais.

## 4. Procura, estados e invariantes

Procura efetiva de uma stream = intenções locais vigentes + filhos com concessão
não expirada e contexto vigente. Pending de admissão é interesse transitório
limitado pelo prazo da sua operação; não é destino autorizado. História terminal,
ACK/callback antigo, oferta de catálogo, anúncio/trace e simples tick não são procura.

Estados conceptuais, sem acrescentar enums Java nesta etapa: sem procura;
ativação pendente; confirmado; confirmado com renovação pendente; retirado/expirado.
A remoção lógica pode coexistir com trabalho físico antigo ainda reservado.

- Sair C1 com C2 vigente retira apenas C1; mantém o tronco confirmado e a sua
  renovação agregada, sem ACTIVATE/DEACTIVATE upstream por cada consumidor final.
- Sair o último consumidor retira imediatamente a autorização local/publicada,
  cancela a renovação lógica e prepara DEACTIVATE do ramo correspondente. Se
  faltar quota/espaço/conexão, não se mantém forwarding nem se espera sem prazo;
  a expiração do lease remoto é a salvaguarda de limpeza.
- Na fonte, sem procura não há Confirmation utilizável nem parent para podar.
  A oferta de catálogo não desaparece por falta de consumidores; suspender bytes
  da fonte pertence ao futuro componente de vídeo.
- Perder child remove apenas esse child. Perder upstream invalida autorização
  dependente; outra Session, stream ou origem não é atingida. Reconexão exige
  ativação/identidade novas; não herda lease nem terminal da conexão antiga.
- Expiração órfã verifica o relógio mesmo sem tráfego novo. Aos limites exatos,
  confirmação/filho expirados não aparecem na projeção utilizável nem em
  Confirmation.visibleAt. Sweep físico pode ocorrer depois sem autorizar dados.
- DEACTIVATE e callbacks antigos nunca removem o ramo de revisão/Session/operação
  substituta. Não usar comparação apenas por nodeId, stream ou subscriptionId.
- Renovação após expiração/cancelamento não é ACTIVATE implícito; retorno a
  autorização exige admissão explícita válida. Recuperação automática fica em
  IMP-013/014. Intenção pode continuar indisponível sem renovação fantasma.

## 5. Renovação e validade

Fonte concede no máximo min(pedido, máximo local). Relay limita também ao
restante da confirmação upstream corrente na emissão do ACK. Renovar um filho
não renova por si a confirmação a montante e não cria referência por cliente.
Se uma renovação upstream válida trouxer nova concessão, o agregado poderá usá-la
para respostas seguintes; não se alteram bytes de ACK já entregues ao port.

A nova concessão remota é ancorada no início local da operação de renovação,
antes da fila, e não na chegada do ACK. Atrasos de fila/send/ACK consomem validade.
Duplicado da mesma revisão/request/fingerprint devolve restante da concessão
original ou o terminal; nunca cria novo horizonte. Um ciclo novo aprovado usa
revisão maior. Cancelamento/expiração prevalecem sobre ACK12 tardio, mesmo ACCEPTED.

Regra de negociação já proposta no ADD6.1: período efetivo
min(subscription.renew, grantedLease/3), com precisão monotónica positiva e sem
arredondar para zero. Deve ser concretizada/verificada em Design, incluindo
configurações heterogéneas, grant curto, ACK que chega depois do instante elegível,
uma única pendência e ausência de bursts/catch-up. Não se altera configuração
nem se apresenta essa regra como imposição do PDF.

O prazo lógico de renovação não pode autorizar para além da concessão vigente:
falhar renovação não apaga antecipadamente grant saudável nem prolonga o velho.
O Design fixará o corte por prazo restante, o instante da próxima renovação e a
política de falhas locais usando os parâmetros/limites existentes.

Q-C-41 aprovada: RENEW exige prefixo selecionado e confirmado compatíveis e epoch/
Session correntes. Divergência devolve PATH_CHANGED, preserva o grant atual até ao
seu prazo e não troca parent nem prolonga o ramo. Uma métrica diferente com o
mesmo path/epoch/Session não é divergência.

## 6. Cancelamento e concorrência

Eventos serializados no owner: CancelLocal, ReceivedDeactivate, RenewalDue,
ReceivedRenew, AckReceived, LeaseExpired, NeighborDown, EpochChanged, SendCompleted,
OwnerStopped. São conceitos de domínio, sem classes/eventos de rede novos.

Para cada evento, primeiro validar contexto/limites, depois alterar apenas o
agregado/correlação correntes e publicar, preparando envios sem espera bloqueante.
ACK de RENEW só pode instalar a concessão correlacionada vigente; ERROR11/13/14
não pode terminar trace15 ou outro pedido. ERROR sobre ERROR não é emitido.

Renovar/cancelar compete com revisão maior, novo token e reconexão: a referência
à geração/Child/Line da operação é capturada, e reaplicação tardia é no-op sobre
substituta. Preservar a guarda terminal M07 e o finally de conclusão física.

O Design deve explicitar, antes de Implement, a corrida entre nova revisão RENEW
emitida/admitida e DEACTIVATE: concessão anterior ainda ativa, revisão conhecida
no requerente e revisão efetivamente admitida no parent, envio que falha antes
ou depois de admissão, e ACK antecipado/tardio. Não se assume que ACCEPTED prova
admissão remota. Na incerteza de entrega, a poda remota é best effort e termina
pelo lease, sem cancelar por ID genérico ou iniciar retries ilimitados.

## 7. Limites e shutdown

E=capacidade do owner existente; preservar os tetos de IMP-011: C/D=min(E,256),
L=min(E,64) com4 tokens/stream, H=min(E,1024), B=min(E,64), J=min(E,16),
4 PENDING/Session,4 envios físicos/Session e2/contexto; resposta16/janela fixa1s,
N=min(control.maxConnections,63), path<=32. Novas intenções/renovações/podas
partilham estes orçamentos; não acrescentam filas/reservas paralelas ilimitadas.
O Design terá de demonstrar como a representação cabe nesses recursos antes
de alocar/copiar. Redução de memória lógica não antecipa libertação física.

Alarme comum: até2 jobs físicos/1 wake, trabalho por turno limitado e justo com
anúncio/trace. Sem timer por pedido, executor de estado extra ou novo transporte.
Saturação recusa localmente preservando ramos saudáveis; owner.offer(false) mantém
a política existente de fecho por saturação, sem bloquear callbacks.

requestClose impede novas renovações/admissões, cancela intenções e retira a
projeção. Poda de rede é best effort, sem inventar handshake/ACK de DEACTIVATE
nem fase de drenagem do manager. awaitTermination inclui callbacks/continuações
e alarme no budget global restante; não reinicia prazo por filho/stream/operação.

## 8. Oráculos V-015 planeados — nenhum executado

São grupos de oráculos, não contagem de testes nem garantia de resultado futuro.
SubscriptionLeaseTest usará relógio/port/scheduler controlados e o owner existente
injetável, com asserts de mensagens e de estado físico/lógico separados.

| ID | Preparação e resultado verificável esperado |
|---|---|
| L01 | S-R-(C1,C2), ACK válidos; sair C1 mantém C2 e uma referência upstream, sem DEACTIVATE do tronco |
| L02 | Sair o último interessado retira forwarding antes de concluir o envio de poda; um DEACTIVATE por ramo retirado, sem parent na fonte |
| L03 | Consumidor local e child mistos; retirar um não elimina o outro, nem a renovação agregada |
| L04 | Relógio falso ultrapassa lease órfão sem novo evento de rede; limpeza termina, terminal não reativa |
| L05 | Renovações periódicas com procura mantêm uma referência no tronco e concessões dentro de cada horizonte |
| L06 | Máximos heterogéneos: fonte pedido6000/máximo3000 concede3000; período efetivo<=1000ms; relay limita ao restante upstream |
| L07 | Dois relays, filas/ACK atrasados e grant curto; floor não arredonda999999ns para1ms, nenhum nó concede para além da sua confirmação |
| L08 | Mesmo ID/revisão/request/payload repetido não prolonga lease nem duplica resposta/contexto; campos conflitantes recusados |
| L09 | Q-C-40: renovar com revisão maior/request novo; ACK da revisão anterior e Long.MAX_VALUE não contaminam operação atual nem fazem wrap |
| L10 | Cancelar ticket antigo e DEACTIVATE antigo deixam token/ramo substituto com lease/path intactos |
| L11 | Cancelar durante renovação/ACK held, depois ACCEPTED: não reinstala autorização; outro child e lease mantidos |
| L12 | Callback bloqueado e continuação já processada: B/J/contexto não se reutilizam até ambos terminarem; repetir ordem inversa |
| L13 | Renovação recebida depois do prazo/cancelamento recusada; não ativa via14 nem recria procura |
| L14 | Q-C-41: seleção divergente devolve PATH_CHANGED, mantém grant original até expirar, zero troca de parent |
| L15 | Session antiga/nova, mesma instanceId/nova connectionId e nova instanceId; evento/callback velho não remove a nova |
| L16 | Mesmo epoch com duas streams e duas origens; poda/erro/expiração/renovação de uma preserva as outras |
| L17 | Falha de envio CLOSED/OVERFLOW/INVALID_MESSAGE/exceção; nenhuma concessão nova por ACCEPTED isolado, cleanup limitado e erros localizados |
| L18 | ERROR related11/13/14 e ACK12 com correlação errada não afetam trace15, ativação ou renovação alheias |
| L19 | C/D/L/H/B/J/quota no limite e limite+1, sem evicção de watermark vivo nem backlog de podas/erros; perda de contexto liberta só o lógico elegível |
| L20 | Uma renovação pendente impede bursts; scheduler atrasado/relógio no wrap, nenhum busy loop ou timer/stream |
| L21 | Fonte mantém oferta de catálogo mas não ramo autorizado sem procura; não se conta vídeo emitido |
| L22 | Shutdown durante ACK/RENEW/DEACTIVATE: primeiro deadline preservado, zero autorização e terminação física demonstrada, não presumida por fila vazia |
| L23 | Compatibilidade dos records/snapshots e APIs de ticket; seleções, catálogo e trace mantêm semântica, cópias defensivas |
| L24 | Integração posterior TCP real com S-R-(C1,C2) e cadeia de dois relays, sessões reais, remoção parcial/final e terminação; separado de prova de vídeo/GUI/CORE |

Integração posterior pode compor manager/monitor/routing no teste com o owner/port
existentes e consumidores via API, sem mudar OverlayNode/CLI. Só após autorização
da etapa apropriada executar JDK21/regressão/clean verify e preservar logs/XML/JAR.
Manual humano reprodutível, revisão final Denji atribuída na TASK-015 e integração
continuam requisitos de fecho aplicáveis, sem presumir aprovação ou contacto.

## 9. Decisões, pressupostos e checkpoint

Q-C-40/41/42 foram respondidas explicitamente pelo utilizador nesta etapa:
revisão crescente, recusa de RENEW divergente com grant preservado, três ficheiros
sem CLI. [Registo](../../../docs/construction-duvidas.md) e concretizações no ADD/
contratos. Não são decisões da retoma cancelada de outro IMP nem novas ADR.

[PRESSUPOSTO] Manter identificadores aninhados e coleções privadas nos ficheiros
previstos, sem novas classes públicas de infraestrutura: respeita a allowlist e
as portas já existentes; os tipos concretos ficam para Design.

Antes de Implement, Design detalhará a corrida de cancelamento Q-C-40, periodicidade
com grants curtos, budget por operação, idempotência tipo11/tipo14 e contabilização
física. Nenhuma política não aprovada é introduzida por código neste Model.

Parar neste checkpoint: Model por aprovar, stages_completed vazio e
current_stage=model. Sem Design/ADR/Implement/Test, Java/testes Java, staging,
commits/push ou início de outro IMP. Evidência documental nova em
experiments/construction/023-arvores/model-01, com diff/numstat contra a base e
prova de preservação de fontes/fechos anteriores. Não se atribuem resultados
históricos1328 ao IMP-012.

## Aprovação de Model

Registo 2026-11-18T15:58:39Z: utilizador aprovou Model e reconfirmou Q-C-40–42.
Apenas Design autorizado, com os três ficheiros, owner/limites/shutdown existentes.
Sem Java/testes Java/staging/commits/push; revisão final Denji e integração
continuam requisitos futuros de fecho.
