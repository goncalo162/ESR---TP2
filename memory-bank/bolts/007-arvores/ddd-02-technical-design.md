---
unit: "004-arvores"
bolt: "007-arvores"
stage: "design"
status: "complete"
created: "2026-11-17T17:11:33Z"
updated: "2026-11-17T17:30:07Z"
approved_at: "2026-11-17T17:30:07Z"
---
# IMP-011 — Design de ativação confirmada e procura agregada

✅ Model e Q-C-34/35 aprovados pelo utilizador em 2026-11-17T17:02:14Z.
✅ Design aprovado integralmente em 2026-11-17T17:30:07Z, incluindo ancoramento
dos leases, T/A, lineagens, capacidades e quotas. Apenas revisão ADR autorizada;
Implement continua não autorizado. Menções a propostas/aprovação pendente abaixo
conservam o texto do checkpoint anterior; os detalhes do Design estão aprovados.
M01–M14/V-014 NÃO EXECUTADOS. Sem alterações Java, commits ou push.

Retoma atual: ADR aprovada em2026-11-17T17:50:02Z; apenas Implement autorizado.
[Código/evidência incremental](implementation-walkthrough.md) nos quatro caminhos,
mais exceção Q-C-36 autorizada/aplicada somente no teste histórico. Focado
NeighborDown 1/0/0/0; duas regressões 1324/1/0/0 (D11a), isolado D11a 1/0/0/0.
Sem aceitação de Implement/etapa Test. NeighborDown investigado e negativos/
diagnóstico preservados. Texto abaixo histórico.

## 1. Fontes, arquitetura e fronteira

Fontes: [ADD](../../../add.md), IMP-011/ADR-005/018/024, secções 6–9/11;
[Model aprovado](ddd-01-domain-model.md); [Q-C-34/35](../../../docs/construction-duvidas.md);
[contrato técnico](../../../docs/07-API-AND-INTERFACES.md), tipos 11/12/17;
[Design de seleção](../021-rotas/ddd-02-technical-design.md),
[Design de publicidade](../006-rotas/ddd-02-technical-design.md) e
[Plan de trace](../022-rotas/implementation-plan.md).
Reutilizam-se ADR-004/014/015/016/017/020/021/026 e TCP v2/ADR-034/035.

Padrão: agregado volátil por stream no owner serial já existente. Mensagens
de rede entram por eventos, envios são assíncronos e snapshots são imutáveis.
Sem DB, HTTP/OpenAPI, novas bibliotecas, executor de estado, sockets ou wire.
Persistem apenas configuração e evidência; estado de árvores não é recuperado
de ficheiro. Em vez de migrações de DB há transições em memória no owner.

Não se leu, analisou ou alterou código-fonte nesta etapa. As APIs seguintes são
propostas baseadas em contratos documentados; a conferência das APIs reais fica
para a etapa que a permita, antes de Implement. Não se declara essa conferência
executada nem se resolve uma incompatibilidade por ampliar âmbito implicitamente.

Mantêm-se os quatro caminhos de IMP-011:

1 - **routing/Route.java**: preservar o snapshot e construtores de seleção;
acrescentar tipos aninhados imutáveis para confirmação/validade e destinos.
2 - **routing/Subscription.java**: valor de subscrição, identidade/revisão,
estado, path, validade e contexto de resposta; auxiliares neste ficheiro.
3 - **routing/RoutingService.java**: admissão/ativação, integração de eventos,
ledger de revisões, limites físicos, publicação e lifecycle existentes.
4 - **routing/ActivationTest.java**: M01–M14, subdivididos nesta etapa;
auxiliares de teste contidos no próprio ficheiro.

Os caminhos main/test são relativos a Projeto/src/main/java/pt/uminho/esr/ott
e Projeto/src/test/java/pt/uminho/esr/ott. Não alterar RoutingTable, RouteState,
StreamCatalog, OverlayNode, NeighborManager, NetworkMonitor, transporte, codec,
POM, configuração ou testes históricos por inferência. Não criar SubscriptionId,
Clock ou Repository em ficheiros adicionais. Cli/play/stop e acesso de consumidores
via OverlayNode continuam nas tarefas futuras; não usar reflexão para os antecipar.

## 2. Responsabilidades e contratos de API propostos

1 - **Entrada local de aplicação**: operação de RoutingService para admitir
um consumidor identificado por (streamId, consumerToken), devolver ticket
consultável com resultado imutável e permitir cancelamento idempotente. Não
devolver future cujo callback arbitrário possa correr no owner. Reservar o
token/operação antes de offer; capturar o início monotónico antes da fila.
2 - **Entrada de controlo**: nos eventos recebidos já encaminhados ao serviço,
tratar ACTIVATE, ACTIVATE_ACK e ERROR com relatedType=11 e correlação própria.
Tipos 9/10/15/16/17 alheios e eventos de monitor mantêm o comportamento anterior.
DEACTIVATE/RENEW completos não são implementados por esta proposta.
3 - **Owner**: admissão, passos da ronda, confirmação, invalidação e publicação
executam no SerialOwner emprestado. Nenhuma espera em future/ACK/semáforo no owner.
4 - **Porta de envio**: reutilizar ControlPort.send(Session, ControlMessage)
e a CompletionStage<SendResult> documentada. ACCEPTED só enfileira; não equivale
a write/flush nem à confirmação remota da árvore.
5 - **Ticket local**: resultado consultável, identidade do consumidor/pedido,
cancelamento antes/depois da confirmação. Nova chamada com o mesmo token não
duplica procura nem reinicia uma ronda; uma repetição explícita após terminal
é uma operação nova identificada, com reserva e prazo novos, conservando o token
como uma só intenção. Não há repetição automática infinita por anúncio/tick.
Cancelamento lógico de filho é uma operação interna do owner, identificada por
Session/stream/subscriptionId/revision; poderá ser exercida pelo novo harness
sem implementar o handler completo de DEACTIVATE ou usar reflexão no serviço.
6 - **Leitura de confirmação**: nova projeção forwarding em Snapshot de
RoutingService, com tipo aninhado em Route.java. Preservar acesso a routes,
catalog, diagnostics e capturedAtNanos e manter overload compatível dos
construtores documentados de Snapshot. O mapa routes continua a representar
seleção: INACTIVE/UNAVAILABLE e campos de dados antigos não ganham autorização
implícita. Não reescrever a projeção do RoutingTable para fabricar ACTIVE.

Cada valor de confirmação contém streamId/serverId/epoch, path confirmado,
sessão upstream opcional, consumidores locais confirmados, filhos confirmados,
revisão da projeção e horizontes de validade relativos ao instante de captura.
Na origem o parent é ausente; a oferta local é a autoridade. PENDING nunca
é destino ativo. Projeção visibleAt(now) filtra estado expirado sem mutar o owner;
filho expira individualmente e parent expirado retira toda a autorização remota.
Snapshots antigos não são reutilizados como se fossem publicações novas.

## 3. Estado interno e transações

Por stream admitida: referência a binding/epoch já existentes, seleção consultada,
confirmação corrente opcional, uma ronda de ativação inicial, conjunto de tokens
locais, slots de filhos e uma versão monotónica da projeção. A fonte mantém
oferta/filhos/tokens, sem criar parent ou fazer pedido de rede a si mesma.

Por filho: Session completa, subscriptionId, maior revisão admitida, fingerprint
dos campos relevantes, requestId canónico, prazo, resultado lógico, eventual
confirmação e uma intenção de resposta. Enquanto existe uma confirmação saudável,
uma rejeição de pedido novo não a substitui. Uma resposta em voo é reserva física
distinta da intenção lógica. Não copiar a tabela inteira para cada contexto.

No owner, uma transação segue esta ordem:

1 - Validar sessão/envelope e tipo; conhecer a stream não basta para autorizar.
2 - Validar epoch corrente, IDs, path simples/origem/emissor/prefixo, lease,
revisão/correlação e a compatibilidade de Q-C-35, antes de criar procura.
3 - Reservar contextos/lineagem/filho/token necessários; falta de capacidade
recusa atomicamente e conserva mapas, histórico e confirmação saudáveis.
4 - Aplicar a alteração lógica, preparar intenções limitadas de envio e
publicar a projeção no mesmo turno. O ACK recebido de montante só confirma
após revalidar procura, contexto e validade; gerar ACK próprios dos filhos.
5 - Enviar fora de esperas bloqueantes, acompanhar SendResult e atualizar a
projeção quando houver confirmação local de admissão do ACK do filho.

O parent só publica o filho como ACTIVE após ACCEPTED do seu ACK, se o contexto
continuar corrente e válido; até lá fica PENDING. Isto é a concessão local do
parent, não uma prova de que o filho já recebeu o ACK. Um ACK upstream que chega
antes do callback ACCEPTED do pedido pode ser guardado uma única vez; só é
aplicado quando ambos os factos forem válidos e o prazo ainda não tiver vencido.
Callback antigo nunca confirma revisão/sessão substituta.

## 4. Lease concedido e validade remanescente — concretização de Q-C-34

Todas as contas usam nanos monotónicos, com conversão/soma verificadas.
Nenhuma conta subtrai relógios de dois nós. Duração wire é u32 em ms; a
conversão de um restante em nanos para ms usa floor, nunca ceil ou clamp para 1.
Os limites absolutos nas equações são notação matemática: armazenar pares
(início,duração) e comparar idades locais dentro do horizonte suportado, para
não depender de uma soma absoluta que pode transbordar junto ao wrap do relógio.
Converter durações com operação verificada; relógio regressivo/horizonte inválido
gera falha explícita, não tempo adicional ou concessão negativa.

**Receção do ACK a montante.** O início t0 da tentativa é capturado antes da
fila local, antes de enviar o ACTIVATE. Para grant G recebido, usar:

```text
limiteConfirmado = t0 + G * 1_000_000
restanteConfirmado(agora) = max(0, limiteConfirmado - agora)
```

Só aceitar ACK ainda dentro do prazo da operação e com limiteConfirmado > agora.
G deve ser positivo e não exceder o lease pedido nessa tentativa. Ancorar G na
receção do ACK prolongaria a concessão pelo tempo passado em filas/rede; isso é
proibido. Uma nova tentativa tem t0 e requestId próprios, sem reiniciar o prazo
global da ronda. ACK repetido não recalcula nem amplia limiteConfirmado.

**Concessão a um filho.** No instante d de preparar cada resposta, revalidar
procura, sessão/epoch, prefixo selecionado/confirmado e validade. Definir:

```text
na fonte: limiteRamo = d + min(pedidoMs, maximoLocalMs) * 1_000_000
no relay: limiteRamo = limiteConfirmadoAtual
se já houver concessão ao mesmo filho/revisão: limitar também ao seu limite antigo
Gfilho = min(pedidoMs, maximoLocalMs, floor((limiteRamo - d) / 1_000_000))
limiteLocalFilho = min(limiteRamo, d + Gfilho * 1_000_000)
```

Gfilho deve caber no wire e ser >=1 ms. Na fonte, um duplicado usa a concessão
anterior e não executa novamente a expressão que criaria um lease novo. Uma
revisão superior no mesmo ramo já confirmado também fica limitada ao restante
existente: ACTIVATE não funciona como renovação implícita de IMP-012.
Sem pelo menos 1 ms concedível, responder NO_ROUTE/detail GRANT_TOO_SHORT ao
novo pedido, preservando confirmação e consumidores ainda válidos. Se a própria
tentativa perdeu a validade antes de receber ACK, terminar TIMEOUT/GRANT_EXPIRED.

Uma intenção de ACK que aguarda quota/reserva não contém um grant congelado:
recalculá-lo imediatamente antes de ControlPort.send, incluindo o limite antigo
do filho quando houver. Depois de aceite no port, não se alteram os bytes.
O prazo de resposta original também não é renovado por essa espera.

O filho recetor aplica o mesmo ancoramento no início do seu pedido. Como esse
início precede a preparação da concessão pelo parent, uma resposta atrasada não
cria validade adicional por atraso de transporte. Se chegar tarde demais, é
ignorada como expirada. Relógios monotónicos com a sua taxa normal são a base
local; isto não exige sincronização de épocas dos relógios, nem mede atraso
unidirecional, nem garante entrega do ACK ou serviço em rede arbitrariamente lenta.

Exemplo controlado: confirmação de R termina em 10 000 ms; em d=8500 ms o filho
pede6000 e máximo local6000: Gfilho=1500, nunca6000. Se a resposta só puder ser
preparada em d=9200, Gfilho=800. Com restante999999 ns, não emitir grant1 ms.
No relógio do filho, início2000 e G=1500 implica limite3500; receber em2600 dá
900 ms restantes, não1500. Receber em3500 não ativa. Os números são oráculos
com relógios controlados, não uma medição real ou relógio global.

Sem RENEW neste IMP, confirmações expiram. Não prometer streaming duradouro.
O scheduler e a projeção excluem expirados; a política de poda remota continua
em IMP-012, mesmo quando cancelar localmente já impede um ACK de reativar dados.

## 5. Ronda, tentativas e idempotência

Proposta concreta: T=routing.activate.timeout.ms, A=routing.activate.attempts.
Default T=3000 ms/A=3; respeitar os valores configurados sem prealocar A objetos.
Há um **prazo global T para a ronda**, desde a primeira admissão antes da fila;
cada tentativa tem também prazo <=T, cortado pelo restante da ronda. Esta é
uma concretização adicional de Design: A é teto de tentativas, não promessa
de A esperas completas. Uma tentativa que esgota T esgota a ronda; erros
antecipados podem permitir as seguintes dentro do mesmo prazo global.

Admissão local entra no próprio RoutingService antes de offer. Para um evento
remoto já entregue no owner, usar o instante de entrada no handler, sem outra
fila interna. Não inventar timestamp de chegada ao socket ausente do contrato:
o extremo requerente aplica o seu prazo desde antes do envio e rejeita o ACK
que chegou depois dele. A proposta não exige campos wire ou callbacks novos
no NeighborManager para medir uma chegada física remota anterior ao handler.

Uma tentativa de ACTIVATE emitida ou admitida para emissão consome uma unidade
de A; não devolver a unidade por CLOSED/OVERFLOW/cancelamento. Erro de formato,
quota ou reserva antes de emitir não gasta tentativa, mas o prazo continua a
correr. No máximo uma tentativa lógica corrente por stream, sem fanout concorrente.
Nova tentativa usa requestId novo e revisão crescente da própria subscrição;
o prefixo é recalculado da seleção atual e sujeito a Q-C-35. Sem forçar um
candidato cujo prefixo o nó não pode confirmar; alternativas não justificam
ignorar seleção/prefixo nem antecipar failover de IMP-013.

Adesão posterior não prolonga a ronda. Cada filho PENDING tem o menor entre
o seu prazo desde admissão e o prazo remanescente da ronda. Após terminal,
retirar provisórios e resolver tickets/filhos uma única vez. Intenção local
pode continuar indisponível, mas ticks/anúncios/duplicados não iniciam rondas
infinitas: só uma nova operação explícita admitida pode abrir outra neste IMP.

## 6. Históricos e revisões limitados

Proposta de Design para tornar idempotência finita sem ordenar UUID:

1 - Uma **lineagem** por (direção, stream, epoch corrente, Session completa).
Na primeira admissão fixa subscriptionId; revisões seguintes nessa lineagem
usam o mesmo ID e crescem. Trocar ID na mesma lineagem devolve STALE_REVISION
sem remover a confirmação. Após nova Session/época, renegociar identidade.
2 - Reter somente o maior número de revisão admitida, requestId canónico,
fingerprint, resultado terminal resumido e eventual confirmação corrente.
Não criar listas de todos os requestId/revisões/UUID retirados. A confirmação
anterior, se ainda saudável, é um valor separado do pedido novo pendente.
3 - Mesma revisão/requestId e mesmos campos é repetição exata: coalescer se
PENDING ou repetir o resultado corrente com validade remanescente. Mesmo tuplo
com campos diferentes é INVALID_MESSAGE; revisão menor é STALE_REVISION.
Mesma revisão com requestId diferente é STALE_REVISION/detail REVISION_BOUND;
não criar aliases ilimitados. Uma tentativa nova usa revisão maior/requestId novo.
4 - Um terminal mantém watermark enquanto Session/epoch forem correntes:
ACTIVATE cancelado/timed-out não pode ser recriado por replay igual. Não usar
cacheTtl como dispensa dessa proteção. Sem espaço para novo histórico protegido,
recusar RESOURCE_LIMIT antes de mutar; não evictar uma lineagem viva.
5 - Ao perder Session ou mudar a época por mecanismo de routing já aprovado,
libertar o histórico lógico correspondente. Mensagem antiga deixa de passar
pela validação de sessão/época. Não aceitar uma epoch só porque chegou ACTIVATE.
Reservas físicas antigas sobrevivem até os seus callbacks terminarem.
6 - Históricos de épocas/origens Q-C-26 não são duplicados ou ampliados. Revisão
Long.MAX_VALUE não incrementa com wrap: terminal STALE_REVISION/REVISION_EXHAUSTED;
nova Session/execução é necessária para reiniciar a lineagem.

Consequência explícita: churn de streams numa conexão muito longa pode consumir
todos os slots de histórico. A resposta é recusa limitada, não memória infinita
ou esquecimento inseguro. A política de lineagem/retenção é detalhe novo proposto
neste Design, ainda não aprovado pela aprovação de princípio de Q-C-34/35.

## 7. Limites de admissão, respostas e trabalho físico

E=capacidade do owner existente; N=min(control.maxConnections,63).
Os seguintes são tetos locais propostos para ativação, sem alterar wire/hard caps
do protocolo ou reduzir os limites já aprovados de anúncios/trace:

| Recurso | Teto proposto | Libertação / saturação |
|---|---:|---|
| Árvores com procura ou trabalho lógico retido C | min(E,256) | Fim da procura/trabalho; recusar nova stream antes de alocar |
| Filhos admitidos D, ACTIVE+PENDING | min(E,256), até N/stream | Remoção lógica; preservar watermark separado |
| Filhos PENDING por Session | 4 | Falha/ACK/cancelamento; não expulsar filhos ativos |
| Tokens locais L | min(E,64), até4/stream | Cancelamento explícito; duplicado não reserva de novo |
| Lineagens H, pais+filhos+terminais | min(E,1024), até2N/stream | Só fim da Session/epoch; sem evicção protegida |
| Envios físicos de ativação B | min(E,64), até4/Session | Até callback e continuação físicos acabarem |
| Envios físicos por contexto | 2 | Inclui tentativa antiga ainda em callback |
| Intenções de resposta | 1/filho, dentro de D | Coalescer mesma correlação; deadline original |
| Respostas de erro sem slot admitido J | min(E,16), até1/Session | Sem backlog; só se houver J+B e quota |
| ACK/ERROR enviados por Session | 16/janela local de1s | Contador limitado; nova janela não renova prazo |
| Ações de ativação por turno | 16 | Cursores para justiça, com scheduler existente |
| Alarme/wake | Scheduler existente, <=2 jobs físicos e1 wake | Inclui jobs em execução/cancelamento; sem timer/pedido |

Janela de resposta é fixa desde a primeira emissão, com relógio monotónico;
até16 por janela, podendo haver até32 em intervalo móvel que cruza uma fronteira.
Não se alega um limite deslizante estrito de16/s. Só avançar a janela pelo
tempo decorrido, sem guardar uma lista de instantes ou criar um timer por sessão.
Cancelamento/prazo não dependem de ter quota de envio disponível.

Um pedido sem slot recebe ERROR apenas se J/B/quota permitirem; caso contrário,
descarte com diagnóstico limitado. Um pedido admitido conserva no máximo uma
intenção até ao seu deadline; não criar uma fila de erros quando o prazo acaba.
Nesse caso, o resultado local é terminal e o outro extremo terminará pelo seu
próprio prazo. Não prometer que todos os erros são entregues a um peer lento.

Cada envio reserva antes de chamar o port. Resultado lógico, cancelamento de
future ou ACCEPTED não provam terminação de callback. B/J/contexto só são
reutilizados após callback/continuação reais acabarem, inclusive em fecho.
Retries podem ficar sem espaço porque uma tentativa antiga ainda ocupa reserva;
esperar dentro do prazo, sem acumular mais tickets físicos.

E pequeno pode impedir certos grafos/cargas: erro de capacidade explícito, não
exceção oculta nem aumento dos tetos. Estruturas são alocadas por necessidade.
Memória é O(C + D×32 IDs + L + H×32 IDs + B×frame limitado + J), com todos os
contadores acima finitos; nenhum máximo de bytes JVM medido é alegado.

Recusa local por C/D/L/H/B/J/quota é localizada. owner.offer(false) por fila
cheia continua a iniciar o fecho existente de Q-C-31; não aguardar um wake que
essa execução já não admite. OVERFLOW é terminal só nessa conexão; não tentar
reenviar nela, nem fechar outras streams/sessões saudáveis por um pedido inválido.

## 8. Seleção divergente — concretização de Q-C-35

Adesão nova em R exige a mesma epoch e prefixo [S,...,R] na seleção elegível
e na confirmação utilizada. Conferir ainda a Session upstream exata e a validade
da confirmação. O path do filho termina em R,filho e não contém repetições.

Se a seleção passa de [S,A,R] para [S,B,R], mantendo uma confirmação saudável
via A, um novo filho com qualquer desses prefixos é recusado PATH_CHANGED:
um não coincide com a seleção, o outro não coincide com a confirmação.
Não emitir ACTIVATE/DEACTIVATE para trocar parent nem modificar a confirmação
de A, os leases dos filhos existentes ou os tokens locais confirmados.

Repetição exata de uma subscrição já admitida é tratada pelo ledger: não é uma
nova adesão. Pode obter o mesmo resultado com restante antigo, sem abrir ramo ou
alargar lease. Uma revisão superior é um pedido novo e tem de passar Q-C-35.
Cancelamento lógico continua permitido: a divergência não prende um consumidor.

Se o caminho selecionado desaparece por TTL/WITHDRAW mas a confirmação continua
válida e a Session/epoch/oferta não falharam, não usar a expiração do anúncio para
apagar automaticamente a confirmação. Recusar adesão nova por PATH_CHANGED;
preservar a confirmação até ao próprio horizonte ou invalidação contextual.
Sem confirmação e sem seleção, o resultado inicial é NO_ROUTE. Alterar apenas
custo/seq sem mudar prefixo/epoch/Session não é divergência e não renova leases.

Na tentativa inicial, se a seleção muda antes de chegar ACK, descartar essa
confirmação para a tentativa antiga, limpar provisórios e só repetir dentro
de A/T com o prefixo selecionado atual. Não há parent antigo saudável a trocar.
Após ACTIVE, seleção nova não substitui o ramo confirmado; IMP-013/014 farão isso.

## 9. Eventos, falhas e shutdown

NeighborDown da Session corrente invalida provisórios e confirmação que nela
dependem; evento antigo não retira a nova Session. Época substituída invalida só
as streams da respetiva origem. setLocalAvailable(false) invalida confirmação
da oferta local correspondente. Nada disto implementa uma recuperação automática.

Erros: UNKNOWN_STREAM sem binding; NO_ROUTE sem caminho/grant útil;
PATH_CHANGED para prefixo/epoch incompatível; LOOP_DETECTED para ciclo;
STALE_REVISION para revisão/lineagem obsoleta; TIMEOUT para prazo ou grant
recebido já expirado; RESOURCE_LIMIT para admissão; SOURCE_UNAVAILABLE para
oferta local indisponível. Envelope inseguro/ERROR alheio não gera ERROR sobre ERROR.
Respostas usam o requestId e subscriptionId/revision do filho, nunca os do pedido
agregado a montante. Detalhe curto, sem paths de media ou stack traces na rede.

Integração de timers no alarme comum: considerar próximo prazo de ronda,
resposta e validade da confirmação, processar até16 ações e ceder; a leitura
visibleAt impede que sweep atrasado autorize uma confirmação expirada. Não
criar sweep síncrono de16384 streams por tick ou alarme independente por pedido.

requestClose impede novas admissões, termina tickets/provisórios, remove a
autorização publicada, cancela intenções e usa os hooks existentes para cleanup.
awaitTermination inclui B/J/callbacks e jobs físicos dentro do budget global
restante, sem um prazo adicional por stream/filho. Sem joins no owner, novo
executor ou alteração da ordem de fecho de OverlayNode por inferência.

## 10. V-014 — os mesmos14 cenários, concretizados e NÃO EXECUTADOS

Não se acrescenta uma contagem fictícia de testes: M01–M14 são grupos de
oráculos planeados. As variantes abaixo cabem nesses grupos; nenhuma foi executada.

1 - **M01 / tronco**: C1/C2 antes e depois do ACK, source+relay e relay em
cascata; contar uma referência upstream, IDs próprios e ACK por filho admitido.
2 - **M02 / locais**: tokens repetidos/distintos, último cancelamento e nova
operação explícita; uma referência, sem ronda nova por mera leitura/tick.
3 - **M03 / fonte**: oferta ativa/ausente, duplicado após1000ms não renova grant,
outra stream da mesma fonte intacta; nenhum parent/envio a si própria.
4 - **M04 / transporte**: ACCEPTED/flush sem ACK não confirma; ACK antes do
callback de envio, callback em execução e deadline exato; PENDING fora de forwarding.
5 - **M05 / divergência**: [S,A,R] confirmado, [S,B,R] selecionado; pedidos com
ambos os prefixos devolvem PATH_CHANGED. Comparar antes/depois Session/path do
parent, versão e validade da confirmação, destinos, tokens e leases: inalterados.
Contar zero envios ACTIVATE/DEACTIVATE causados pelas recusas. Acrescentar seleção
ausente, ciclo/prefixo inválido, custo alterado com path igual e duplicado exato
admitido antes da divergência. Isolar os eventos de anúncio normais dos envios
de ativação: não exigir que o protocolo de publicidade pare.
6 - **M06 / correlação**: variar isoladamente request/stream/epoch/ID/revisão/
Session; ACK inválido não confirma nem afeta resposta de filho/trace alheio.
7 - **M07 / cancelamento**: duas ordens ACK/cancelamento, filho retirado com
outro interessado, último token removido, callback tardio e novo pedido; sem
reativação. Sem afirmar poda remota completa neste IMP.
8 - **M08 / histórico**: revisão menor, igual exata, campos diferentes, novo
requestId com revisão igual, novo subscriptionId na mesma lineagem, revisão
maior, terminal replay e Long.MAX_VALUE; sem novo lease ou crescimento por duplicado.
9 - **M09 / A e T**: A=1/3 e T controlado, erro antecipado permite retry,
primeiro timeout esgota ronda, fila consome prazo, adesão tardia não o renova,
A erros rápidos não abrem quarta tentativa; ERROR usa requestId de cada filho.
10 - **M10 / isolamento**: duas streams da mesma epoch e de origens diferentes;
erro/lease/cancelamento de uma conserva seleção, confirmação e procura das outras.
11 - **M11 / contexto**: Session antiga/nova, nova epoch, reparação já admitida
pela overlay, limpeza de H apenas quando o contexto deixa de ser corrente;
nenhuma herança de confirmação e nenhum reset por UUID arbitrário no ACTIVATE.
12 - **M12 / validade**: exemplo10000/8500/1500 e atraso até9200/800; limite
remoto menor que pedido/máximo local; restante999999ns; floor exato; ACK recebido
em t0+G−1/t0+G; atraso de fila/send/ACK, grant excedido, grant0 e duplicado sem
renovação. Fonte, relay e cadeia de dois relays; filhos nunca publicados para
além do limite da confirmação local do parent.
13 - **M13 / tetos**: C/D/L/H/B/J e quotas no limite/limite+1, até4PENDING por
Session, linhas terminais protegidas sem evicção,1intenção/filho e2sends/contexto;
devolver capacidade só após término físico. Saturar H e manter outras streams
saudáveis; perda de Session/epoch permite libertar slot. Janela de respostas
cruzada não é indevidamente testada como limite móvel de16/s.
14 - **M14 / lifecycle e regressão**: relógio/scheduler/port controlados,
<=2jobs,1wake,16ações/turno, justiça com flooding/trace, overflow local e owner
fatal preservados, shutdown durante callback e ausência de Java0 presumido.
Executar regressão pertinente só na etapa autorizada e incluir a investigação
de NeighborDown descrita abaixo, sem alterar teste histórico por inferência.

## 11. NeighborDown — evidência preservada e investigação pendente

[Evidência duradoura](../../../experiments/construction/007-arvores/neighbor-down-baseline/README.md):
três logs originais copiados byte a byte, SHA-256 e proveniência em manifest.json.
Suite autorizada1296/1falha/0erros/0ignorados e repetição isolada1/1falha na
mesma asserção; primeira execução restrita78erros de sockets distinguida.
Não houve repetição nova de testes na preservação ou no Design.

Causa não estabelecida. Quando uma etapa autorizar inspeção de código, investigar
identidade/ordem dos eventos e a observação de NeighborDown com o teste/prazo
originais, guardar novos artefactos separados e comparar com o contrato aprovado.
Não atribuir a causa a uma corrida por inferência do log.
Antes da aceitação de Implement/Test é necessária a consolidação dessa
investigação/regressão. Correção em fundação ou teste histórico fora dos quatro
caminhos não está autorizada e exige evidência e decisão específica antes de
alterar. Esta pendência técnica já existe; não se cria um gate externo adicional,
não se reabre uma fase DONE e não se apaga a execução negativa.

## 12. Checkpoint e matérias a conferir depois

Design concretiza Q-C-34/35 aceites; a escolha de ancoramento de lease,
prazo global T, lineagem, capacidades e quotas é proposta de Design ainda
por aprovar. A revisão posterior deve conferir APIs/compatibilidade dos quatro
caminhos, extensão aditiva de Snapshot e integração do alarme/lifecycle.
Necessidade de outro caminho será apresentada com evidência, sem código oculto.

Mantêm-se TCPv2/19tipos, código3 rejeitado, formatos/códigos existentes, Q-C-26–33,
bootstrap só inicial, catálogos/trace/seleção sem efeitos de procura, sem UDP
ou vídeo implementados. Não criar ADR nova nem promover estado de ADR aceite
por preparar este Design. Os14 cenários, V-014, regressão nova, revisão final
atribuída a Waheed e integração continuam pendentes.

Parar após verificação documental deste artefacto. Sem ADR/Implement/Test,
alterações Java, commits, push, deploy ou publicação nesta autorização.

## 13. Verificação documental executada

Em 2026-11-17T17:18:44Z, python3 scripts/validate-planning.py terminou com saída0,
zero erros,4870 referências locais/âncoras,35 IMP/bolts/histórias e DAG sem ciclos.
Log preservado em experiments/construction/007-arvores/design-validation.log.
git diff --check passou. Verificação de hashes/dimensões e comparação byte a byte
das três cópias com os originais passou; os logs não estão ignorados pelo Git.
Estado conferido:12 bolts complete,1 in-progress,22 planned; apenas Model em
stages_completed, current_stage=design e external_gates=[]. Projeto/CORE sem diff
e HEAD7164c9ba111268dff59d847982b517e6e41c7a98 preservado, sem commit ou push.
Não foram executados Java, M01–M14/V-014, GUI ou CORE. A verificação documental
não aprova este Design, não investiga a causa de NeighborDown e não substitui
a investigação/regressão exigidas antes da aceitação de Implement/Test.

Retoma atual em 2026-11-17T21:49:25Z: Q-C-36/Q-C-37 explicitamente autorizadas e aplicadas,
sete caminhos no total; [Implement](implementation-walkthrough.md) para revisão.
D11b1/0/0/0, D11a1/0/0/0, handshake/lifecycle202/0/0/0, regressão1325/0/0/0;
Closed/prazo existente sem resposta e libertação de recursos verificados.
Nenhuma aceitação de Implement/Test/fecho, commits/push ou outro IMP. Negativos
anteriores preservados; as menções anteriores a proposta pendente são histórico.
