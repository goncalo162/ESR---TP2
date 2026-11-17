---
unit: "004-arvores"
bolt: "007-arvores"
stage: "adr"
status: "complete"
created: "2026-11-17T17:35:51Z"
updated: "2026-11-17T17:50:02Z"
approved_at: "2026-11-17T17:50:02Z"
---
# IMP-011 — revisão ADR e compatibilidade com as APIs reais

✅ Model/Q-C-34/35 e Design completo aprovados pelo utilizador.
Registo da aprovação de Design: 2026-11-17T17:30:07Z; inclui ancoramento dos
leases, prazo global T, até A tentativas, lineagens, capacidades e quotas.
✅ Revisão ADR aprovada em 2026-11-17T17:50:02Z. Apenas Implement autorizado nos
quatro caminhos, sem Test/fecho/commits/push/outro IMP. Investigar NeighborDown
antes de aceitar Implement; correção fora do âmbito exige apresentação prévia.
As menções a aprovação pendente abaixo são histórico do checkpoint ADR.

Retoma: [Implement nos quatro caminhos](implementation-walkthrough.md), mais exceção
Q-C-36 autorizada/aplicada apenas no teste histórico. Focado NeighborDown 1/0/0/0;
duas regressões 1324/1/0/0 (D11a), isolado D11a 1/0/0/0. Sem aceitação/etapa Test.
Investigação de NeighborDown consolidada, negativos/diagnóstico preservados.
As tabelas/linhas de fonte abaixo descrevem a base anterior à implementação.

## 1. Conclusão e alcance

A inspeção estática confirma pontos de integração suficientes nos quatro
caminhos previstos. Não se identificou uma incompatibilidade material que
obrigue a substituir uma ADR aceite, mudar wire ou pedir outro caminho Java.
Reutilizam-se ADR-005/018/024 com as restrições transversais abaixo; não se
criou uma ADR nova nem se alterou o estado das existentes.

Compatibilidade estática não é implementação, compilação ou prova funcional.
ACTIVATE/ACK ainda não são tratados pelo RoutingService atual e a projeção
forwarding ainda não existe. Os ajustes internos necessários estão discriminados
nas secções 3–7; pertencem a trabalho futuro dentro de RoutingService/Route,
sem enfraquecer o snapshot de seleção, o catálogo, o trace ou o lifecycle.

O âmbito desta revisão é o [Design aprovado](ddd-02-technical-design.md) e
[IMP-011 do ADD](../../../add.md#imp-011). Os quatro caminhos são:

1 - Route.java: existente; conservar record/contrato de seleção e acrescentar
tipos aninhados de confirmação.
2 - Subscription.java: inexistente na entrada; é um novo alvo autorizado,
não uma API já disponível ou ficheiro vazio validado.
3 - RoutingService.java: existente; acrescentar o comportamento de ativação
sem alterar as portas públicas dos componentes emprestados.
4 - ActivationTest.java: inexistente na entrada; novo alvo de testes e
harness próprios, sem editar testes históricos.

Caminhos main/test relativos a Projeto/src/main/java/pt/uminho/esr/ott e
Projeto/src/test/java/pt/uminho/esr/ott. Dependências abaixo foram consultadas
apenas em leitura para conferir contratos, não autorizadas para alteração.
Não se investigou a falha de NeighborDown nem se analisou a sua asserção nesta etapa.

## 2. ADR e decisões aprovadas

| Decisão | Confronto com o Design e código real |
|---|---|
| ADR-005 | Procura por stream, confirmação separada da seleção e conjunto de filhos; cancelamento mínimo Q-C-34, renovação/poda completas em IMP-012 |
| ADR-018 | Estado em memória; Session completa/época/revisão validam as mensagens; sem restaurar subscrições após reconexão |
| ADR-024 | Quatro caminhos preservados, um IMP por bolt e alterações documentais rastreáveis; sem extensão implícita |
| ADR-004/017 | RoutingTable continua a selecionar por RTT/path; RoutingService usa o owner serial existente e publica projeções separadas |
| ADR-014/015/020 | CompletionStage/ACCEPTED não provam ACK; reservas físicas e prazo global de fecho preservados; recusa fatal do owner não vira backpressure recuperável |
| ADR-016 | T/A/lease já têm accessors e validação na configuração; nenhuma propriedade ou override novo |
| ADR-012/021/034/035 | Tipos/payloads existentes, controlo v2, validação contextual no serviço; sessões de reparação já admitidas usam o mesmo caminho |
| ADR-025/026 | Sem watchdog UDP, dependências, linter, CI ou limiar de cobertura novos; testes/CORE não são presumidos |

Q-C-34/35 e os detalhes completos do Design estão aprovados, incluindo a
política limitada de lineagens, quotas e prazo global T. A revisão não reabre
essas escolhas nem converte a sua aprovação em sucesso de V-014.

## 3. Matriz das APIs realmente disponíveis

| API/localização observada | Resultado da conferência |
|---|---|
| Route.java:11–46 | Record de seleção; activeUpstream/pendingUpstream presentes e downstreams não vazios são rejeitados; estado só INACTIVE/UNAVAILABLE |
| RoutingService.java:94–97 | Snapshot tem exatamente quatro componentes: capturedAtNanos, catalog, routes, diagnostics; copia routes |
| RoutingService.java:ControlPort/Factory/construtores | send(Session,ControlMessage) devolve CompletionStage<SendResult>; Factory e overload público de clock/Scheduler existem |
| RoutingService.java:266–292 | Eventos exigem owner; identidade/sessão corrente verificadas; 11/12 atualmente ignorados e todo ERROR passa pelo ramo de trace |
| NeighborManager.java:156–161/313–345 | SerialOwner público, capacity/offer/onStop; send valida a Session exata e enfileira comando, devolvendo estágio mínimo |
| NeighborManager.java:619–640 | Aplicação aceita tipos7–19; 11/12 e ERROR17 já são entregues como Received após handshake |
| ControlMessage.java:48–54 | Activation(streamId,epoch,subscriptionId,revision,leaseMs,expectedPath) e ActivationAck(...,grantedLeaseMs) já existem; durações wire são long |
| MessageCodec.java:186–188/287–293 | Parser/encoder para 11/12 existentes; revisão não negativa e u32 validados; positividade de lease/caminho contextual cabe ao handler |
| RuntimeConfiguration.java:66–93 | routing().activateTimeout(), activateAttempts(), subscription().lease()/renew() disponíveis |
| StreamCatalog.java:8–15/26–42 | Catálogo exige INACTIVE/UNAVAILABLE e tem projeção conservadora própria; não admite ACTIVE por acrescentar subscrição |
| RoutingService.java:501–560 | drive/schedule/wake/publish concentram o processamento e as publicações no mesmo serviço |
| RoutingService.java:801–847 | snapshot/requestClose/cleanup/awaitTermination e Scheduler físico já implementados; limite de dois jobs reais |
| OverlayNode.java:68–89/102–111/150–195 | Mesmo owner/manager alimentam routing; routing é encerrado e a sua terminação física aguardada dentro do orçamento existente |

As referências de linha correspondem à base 7164c9b; não são documentação de
Java novo já escrito. Inventário com hashes e dois caminhos ausentes em
[adr-source-before.json](../../../experiments/construction/007-arvores/adr-source-before.json).

## 4. Projeções: integração aditiva e preservação de contratos

**Route não pode passar a aceitar ACTIVE.** O seu construtor atual rejeita
autorizações de dados. RoutingTable.project (linhas174–179) publica explicitamente
os campos de autorização vazios. RoutingTableTest e RoutingServiceTest conferem
essa separação, e RouteTraceTest também exige que diagnóstico não crie procura.
Não alterar essas regras ou os testes para reutilizar o record como árvore ativa.

A extensão já aprovada usa um tipo aninhado em Route.java, aqui designado
Route.Confirmation, para path/época, upstream confirmado opcional, filhos,
consumidores locais e validade. A Session completa de Neighbor inclui endpoint
TCP/UDP; RoutingTable.SessionKey contém só nodeId/instanceId/connectionId.
Preservar a Session/endpoint confirmados no valor de forwarding e usar a conversão
key(Session) já existente ao comparar com o candidato de seleção; não resolver
endereços nem inventar endpoints a partir do nodeId na leitura do snapshot.

O Snapshot real tem quatro argumentos. A extensão compatível prevista é um
quinto componente Map<UUID,Route.Confirmation> forwarding, conservando o
construtor de quatro argumentos que delega com Map.of(), os quatro accessors
atuais e emptySnapshot(). Factory/construtores de RoutingService não mudam.
Fazer cópias defensivas do mapa e dos valores/conjuntos; não adicionar campos
de instância a um record fora dos seus componentes.

Adicionar um componente altera os metadados/equals/toString gerados do record;
não se alega uma compatibilidade irrestrita com reflexão/serialização externa.
Os consumidores locais inspecionados usam accessors e construtores documentados;
não se encontrou dependência de deconstruction/reflexão desse Snapshot. A prova
de compatibilidade de compilação e regressão continua por executar com o mesmo
JAR em todos os processos, como já exigido pelo projeto.

publish(t) deve compor seleção, catálogo e forwarding na mesma passagem do owner.
Rotas selecionadas e StreamCatalog.Entry conservam semântica/validação atuais.
Só confirmações válidas entram no novo mapa; nenhum ACK nem leitura do catálogo
preenche activeUpstream/downstreams do Route de seleção. O novo estado não é
limitado por routing.cache.capacity e não altera os horizontes Q-C-26/29/30.

Para M05, comparar identidade/path do parent, conjuntos e **horizontes originais
de concessão**. Um novo capturedAtNanos reduz o restante de validade; essa redução
natural não é remoção do ramo ou renovação. Não exigir igualdade byte a byte
de snapshots capturados em instantes diferentes. Não se muda o teste histórico:
esta precisão aplica-se aos novos oráculos de ActivationTest.

## 5. Dispatch e controlo: ajustes necessários dentro de RoutingService

1 - Integrar 11/12 na validação comum de versão, emissor e Session corrente
antes de processar conteúdo. NeighborManager já os encaminha; não é necessária
alteração de allowlist, handshake, codec, tipos ou observador de OverlayNode.
2 - Desmultiplexar ERROR: relatedType=11 para a tentativa de ativação com
requestId/Session guardados; relatedType=15 conserva o trace existente. ERROR
alheio não cancela uma ativação nem gera outro ERROR. Atualmente onNeighborEvent
encaminha todo ERROR para onTrace; acrescentar o novo ramo **antes desse retorno**.
3 - NeighborDiscovery.error só trata relatedType18/19; não é necessário alterá-lo
para 11. OverlayNode preserva a entrega ao observador externo no finally;
não roubar mensagens nem modificar a ordem de observadores fora do serviço.
4 - O codec aceita lease u32 zero como representação: rejeitar semanticamente
lease0/grant0 no serviço, antes de reter procura. Pedido acima do máximo local
pode negociar min, como aprovado, sem alterar o domínio u32 do codec. Conversão
ms→ns cabe em long para os limites wire; validar conta e usar idades locais.
5 - A seleção de candidato fornece path e SessionKey. O helper current(Session)
e key(Session) já permitem confirmar a instância/conexão correta. ACTIVATE não
deve chamar confirmEpoch para legitimar uma época indicada pelo próprio pedido.
6 - No ponto epoch(origin,replacement), invalidar ativação/históricos apenas
depois de confirmEpoch ser aceite; uma época recusada não pode retirar um ramo
saudável. remove/retire de Session invalidam o contexto exato; mudança de seleção
ou anúncio retirado não equivale a perda do lease confirmado, conforme Q-C-35.

Não há APIs públicas de consumo local em OverlayNode; requestTrace é uma
delegação específica do diagnóstico. Isto é compatível com o âmbito aprovado:
operação local vive em RoutingService, sem CLI play/stop ou wrapper no nó neste
IMP. Não pedir um quinto caminho nem usar reflexão para antecipar essa integração.

## 6. Owner, callbacks e lifecycle

O serviço real já usa requireOwner, owner.onStop, stopping, primeiro instante
de fecho e cleaned. A futura ativação deve entrar nesses mesmos pontos, sem
novo owner ou hook por subscrição. SerialOwner.onStop tem teto16; acrescentar
recursos ao cleanup existente evita usar um hook por consumidor.

**Envio não é síncrono no owner.** NeighborManager.send chama command/post,
que oferece novo evento, inclusive quando o chamador é o owner. O estágio pode
completar depois de um ACK recebido por outro caminho de eventos. Não substituir
o contrato por retorno direto de SendResult nem bloquear em toCompletableFuture().
A política de ACK antecipado guardado uma vez e revalidação final já aprovada
continua necessária. Falha de formato/porta, null ou exceção tem terminal explícito;
um callback antigo só liberta a sua própria reserva.

As reservas de publicidade Admission e TraceSend usam separação callbackDone /
continuationDone. Acrescentar reservas da ativação com a mesma garantia física,
mantendo B/J separados dos tetos anteriores. cleanup termina lógica/tickets,
esvazia projeção autorizada e marca continuação lógica; callbacks ainda vivos
continuam contados. awaitTermination atualmente espera admissions/traceSends:
é necessário incluir o conjunto físico de ativação dentro do mesmo while/budget.
Limpar só um mapa lógico e devolver true seria incompatível com ADR-014/020.

OverlayNode.requestShutdown já pede fecho de routing antes do manager e
awaitTermination chama routing.awaitTermination com orçamento restante antes
de encerrar o owner. Estender a espera interna do RoutingService não exige mudar
OverlayNode. Não multiplicar orçamento por operação, filho ou componente.
Não inferir de cleaned completo que I/O/callback terminou; são fases distintas.

Owner cheio continua fatal: Owner.offer falha/inicia stop e os hooks executam
cleanup. CommandSlots do manager têm E+1 e não são um novo mecanismo de retry
recuperável. Capacidades C/D/L/H/B/J não garantem que qualquer carga numa fila E
evita saturação. E pequeno pode falhar explicitamente, como já aprovado; não
alargar fila ou reduzir silenciosamente a publicidade/trace para esconder isso.

## 7. Alarme, quotas e justiça

Scheduler real oferece schedule(Runnable,long), requestClose(),
awaitTermination(Duration) e physicalJobs(). Ticket oferece só cancel().
Não existe await por Ticket; a espera física é do Scheduler. Timers mantém
até2 jobs físicos e cancel não elimina um job que já está a executar.

Os pontos futuros cabem todos em RoutingService.java:

1 - drive: processar conclusões/validades/intenções de ativação com o orçamento
aprovado de16 ações e cursores, sem reduzir o processamento de trace/flooding.
2 - schedule: incluir os próximos prazos de ronda/resposta, leases e janela de
quota quando houver resposta bloqueada por quota. Sem segundo scheduler/timer
por pedido e sem depender apenas de um anúncio futuro para acordar essa resposta.
3 - wake: reutilizar o wake coalescido existente. Trabalho bloqueado apenas por
reserva física aguarda callback; bloqueado por quota aguarda janela/prazo.
Não criar busy loop de wakes por intenções que continuam inelegíveis.
4 - publish/visibleAt: sweep com orçamento não pode autorizar lease expirado;
filtrar validade também na leitura pura, com o ancoramento aprovado. Rejeição
por path divergente não troca o parent nem altera os seus horizontes de concessão.

Um alarme pendente mais um job em execução/cancelamento usa o teto físico
existente de2; a extensão não promete capacidade para um terceiro job temporário.
A confirmação de runtime desta integração é M13/M14, ainda por executar.

## 8. Testabilidade e quatro caminhos

O construtor público do RoutingService com LongSupplier/Scheduler, a porta
ControlPort e SerialOwner permitem relógio, eventos e conclusões controlados
num harness próprio de ActivationTest, sem modificar os auxiliares históricos.
O overload package-private com RoutingTable.Limits também é acessível no mesmo
pacote. Criar configurações com RuntimeConfiguration existente, não propriedades
ou tipos fictícios. O enum RouteState já contém ACTIVATING/ACTIVE/SWITCHING;
o novo snapshot pode usá-lo sem editar RouteState.java ou mudar códigos wire.

Para eventual ensaio TCP no novo teste, as APIs públicas de NeighborManager com
owner emprestado e de RoutingService permitem composição explícita no harness.
Não usar o construtor de injeção de OverlayNode, que é package-private de outro
pacote, nem obter routing por reflexão. Esta revisão não demonstra um ensaio
TCP, CORE ou GUI, e não antecipa uma entrada pública de reprodução.

Os mesmos14 grupos M01–M14 do Design permanecem NÃO EXECUTADOS, incluindo
compatibilidade da projeção, erro de trace separado, ACK antes de ACCEPTED,
M05 com confirmação preservada e M14 com callback físico retido. A regressão
futura deve conservar os testes antigos e as suas asserções; nenhum prazo é
aumentado e nenhum teste é desativado para aceitar a implementação.

## 9. NeighborDown e limites desta revisão

Os negativos continuam em
[neighbor-down-baseline](../../../experiments/construction/007-arvores/neighbor-down-baseline/README.md),
com manifesto e hashes. A suite anterior teve1296/1falha/0erros; a repetição
isolada também falhou. A execução restrita com erros de sockets está separada.

**Investigação de causa continua pendente**, por instrução do utilizador.
A leitura de pontos de Session/lifecycle nesta revisão não estabelece a causa
da asserção falhada e não é apresentada como investigação concluída. Nenhuma
repetição de teste, correção, alteração de teste ou fundação foi feita.
Antes da aceitação de Implement/Test será necessária investigação consolidada;
qualquer correção fora do âmbito requer apresentação prévia de evidência/impacto.
Não se cria um gate externo adicional nem se declara a regressão verde.

## 10. Checkpoint ADR

Revisão preparada: reutilização das decisões existentes; nenhuma ADR nova,
substituição ou extensão de caminhos identificada como necessária pela análise
estática. As precisões de integração acima preservam o Design aprovado e não
alteram T/A, ancoramento, lineagens, capacidades, quotas ou Q-C-34/35.

Model e Design são as únicas etapas concluídas. current_stage=adr;
Implement/Test, Java, commits e push não estão autorizados. Os requisitos
globais, V-014, os14 cenários e revisão final/integração continuam pendentes.
Conferência documental e de hashes registada abaixo; nenhum resultado de
compilação/teste Java é presumido.

## 11. Verificação executada e preservação

Em 2026-11-17T17:43:38Z foram conferidos os253 ficheiros rastreados de Projeto/
e CORE/, excluindo target, contra o manifesto de entrada: zero diferenças.
Subscription.java/ActivationTest.java continuam ausentes; os três logs negativos
coincidem em dimensões/SHA-256 com o manifesto preservado. HEAD permanece
7164c9ba111268dff59d847982b517e6e41c7a98; sem diff em Projeto/CORE/scripts/.specsmd.
Resultado duradouro: experiments/construction/007-arvores/adr-preservation-check.json.

V-ADD documental em 2026-11-17T17:44:08Z: saída0, zero erros,4881 referências,
35 IMP/bolts/histórias,29 ADR e DAG sem ciclos. Log final em
experiments/construction/007-arvores/adr-validation.log. git diff --check passou.
A primeira validação acusou três chaves duplicadas na representação multilinha
de stages_completed; o log está preservado em adr-validation-01.log. Foi usada
a lista inline já existente noutros bolts, sem mudar nomes, datas, etapas ou
artefactos e sem alterar o validador ou Java.

Estados conferidos: Model/Design concluídos, current_stage=adr,
12complete/1in-progress/22planned, external_gates=[]. Não houve build/Maven,
teste Java, GUI/CORE, investigação nova de NeighborDown ou alteração de fonte.
M01–M14/V-014 permanecem por executar e revisão ADR por aceitar.

Retoma atual em 2026-11-17T21:49:25Z: Q-C-36/Q-C-37 explicitamente autorizadas e aplicadas,
sete caminhos no total; [Implement](implementation-walkthrough.md) para revisão.
D11b1/0/0/0, D11a1/0/0/0, handshake/lifecycle202/0/0/0, regressão1325/0/0/0;
Closed/prazo existente sem resposta e libertação de recursos verificados.
Nenhuma aceitação de Implement/Test/fecho, commits/push ou outro IMP. Negativos
anteriores preservados; as menções anteriores a proposta pendente são histórico.
