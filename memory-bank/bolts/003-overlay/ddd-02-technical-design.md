# IMP-004 — Design: descoberta e sessões de vizinhança

Desenho preparado em 2026-11-10T15:00:14Z, para revisão da etapa 2, após aprovação explícita do
[Model](ddd-01-domain-model.md) e de
[Q-C-05](../../../docs/construction-duvidas.md#q-c-05--receção-de-error-no-imp-004).
Design aprovado explicitamente, incluindo Q-C-06, com registo em 2026-11-10T15:13:50Z.
Apenas a etapa ADR está agora autorizada; este documento não é código ou execução V-007.
Fonte: ADD 5/6/8/9/11, IMP-004, contrato 07 e TASK-007.

## 1. Fronteira e APIs reais

Base Denji/main `26aaa4ae6775e9c78e1d7658472ccaca0b376427`. Preservar os 155
ficheiros de Projeto/. A implementação futura limita-se a Neighbor.java,
NeighborManager.java e NeighborManagerTest.java, atualmente vazios. Tipos auxiliares,
adaptadores, enums e fakes ficam aninhados nesses ficheiros, sem dependência nova.

Transport fornece um listener comum, connect assíncrono com endereço **resolvido**,
Connection.id/isOpen/send/sendAndFlush/close, requestClose e awaitTermination.
attach inicia os workers antes de completar o future do dial: Open/Message/Closed
podem anteceder DialResult. Não existe indicador público inbound/outbound.
O desenho abaixo resolve essa ausência sem alterar Transport/Connection.

BootstrapServer já implementa o servidor REGISTER. O cliente inicial pertence
ao manager, conforme TASK-007. A composição CLI/processos fica no IMP-005;
monitoring/routing/UDP reais não são implementados aqui. MessageType, ErrorCode,
ControlMessage e codecs permanecem intocados; HELLO_OK conserva payload vazio.

## 2. Valores e assinaturas previstas

Assinaturas abaixo são o contrato a introduzir em Implement, não APIs disponíveis.
Imports triviais omitidos. Enums de estado são locais, nunca codificados por ordinal.

```java
// Neighbor.java
public record Neighbor(String nodeId, ControlMessage.Endpoint configured,
        State state, Optional<Session> session, long retryDelayMillis) {
    public enum State { DISCONNECTED, CONNECTING, HANDSHAKING, ACTIVE, CLOSED }
    public record Session(String nodeId, UUID instanceId, UUID connectionId,
                          ControlMessage.Endpoint endpoint) { }
}

// NeighborManager.java
public final class NeighborManager implements AutoCloseable {
    public NeighborManager(NodeConfiguration config, UUID instanceId, Observer observer);
    public NeighborManager(NodeConfiguration config, UUID instanceId,
                           SerialOwner borrowedOwner, Observer observer);
    public CompletionStage<Void> start();
    public Map<String, Neighbor.Session> snapshot();
    public Map<String, Neighbor> diagnostics();
    public CompletionStage<Connection.SendResult> send(
            Neighbor.Session expected, ControlMessage message);
    public CompletionStage<Boolean> disconnect(Neighbor.Session expected);
    public void requestClose();
    public boolean awaitTermination(Duration timeout);
    @Override public void close();

    public interface Observer { void onEvent(Event event); }
    public sealed interface Event permits NeighborUp, NeighborDown, Received, Diagnostic { }
    public record NeighborUp(Neighbor.Session session) implements Event { }
    public record NeighborDown(Neighbor.Session session, Reason reason) implements Event { }
    public record Received(Neighbor.Session session, ControlMessage message) implements Event { }
    public record Diagnostic(Phase phase, Reason reason, Optional<UUID> attemptId,
            Optional<UUID> connectionId, Optional<Neighbor.Session> session,
            Optional<ControlMessage> remoteMessage) implements Event { }
    public enum Phase { NOT_STARTED, DISCOVERING, READY, FAILED, CLOSED }
}
```

Reason é enum local tipado para rejeição remota, falha de contexto, transporte,
prazo, saturação e encerramento; não acrescenta códigos ERROR na rede.
DiscoveryRejected é uma exceção local que conserva ControlMessage original,
attemptId/connectionId e origem remota; falha local de contexto tem outro tipo/
motivo, mesmo que retenha a mensagem que a desencadeou. Não fabricar ERROR remoto.

Records validam não nulos/coerência. configured tem instanceId zero; Session tem
instanceId não zero e Endpoint coerente com nodeId/instanceId. Não mudar configured
ao aprender a instância. session está presente só em ACTIVE. Mapas/listas são
cópias imutáveis; snapshots ACTIVE e diagnósticos são publicados por referência
atómica, sem expor Channel, Connection, futures ou coleções mutáveis.

start é idempotente e devolve o mesmo resultado de descoberta: READY significa
lista instalada, não todos os vizinhos online. Devolver uma CompletionStage sem
permissão de cancelar o future nativo de um dial. send executa a validação da
sessão no dono serial: vínculo retirado devolve CLOSED; envelope local deve ter
nodeId/instanceId locais e tipo de aplicação permitido. Usa Connection.send e
mantém ACCEPTED=enfileiramento; não é um novo ACK ou garantia de entrega.

send admite os tipos de aplicação 7–16 e ERROR(17) explicitamente produzido pelo
consumidor segundo o protocolo; não envia REGISTER/HELLO por essa API nem responde
automaticamente a mensagens. Um ERROR recebido nunca desencadeia ERROR de resposta,
também por contrato do consumidor. relatedType bruto de ERROR recebido permanece
intacto, independentemente do tipo permitido para uma emissão local.
Para emissão local de ERROR com relatedType=17, devolver INVALID_MESSAGE; é guarda
de contexto do manager, sem alterar o codec que representa esse octeto recebido.

disconnect é uma operação local de lifecycle para diagnóstico/monitoring: no owner,
confirma o trio da sessão, retira a autorização e fecha o canal corrente; devolve false para uma sessão
retirada. Não apaga a aresta nem escolhe novo vizinho. A perda TCP real gera Down
e reconexão pelo iniciador, com Closed posterior idempotente. Permite ao operador do manual induzir essa perda sem
expor Connection ou antecipar CLI/sondas; não se apresenta como falha de rede CORE.

Observer é obrigatório, rápido e chamado no dono serial, fora de locks de I/O.
Recebe Up antes dos Received retidos e Down antes de Up da sessão sucessora.
Exceção do consumidor é falha operacional local observável. Não criar uma fila
ilimitada de notificações nem consumidor por omissão que descarte ERROR.

## 3. Executor serial que pode ser partilhado

```java
public interface Registration { void remove(); }
public interface SerialOwner extends AutoCloseable {
    int capacity();
    boolean isOwnerThread();
    boolean offer(Runnable event);
    Registration onStop(Runnable cleanup);
    void fail(Throwable cause);
    void requestClose();
    boolean awaitTermination(Duration timeout);
    @Override void close();
}
public static SerialOwner createSerialOwner(int capacity); // aninhado no manager
```

O construtor por omissão guarda um Plan validado; não inicia threads/sockets.
start cria o owner próprio. O segundo construtor recebe o executor do futuro nó,
com capacity exatamente igual a runtime.overlay.eventsCapacity. O manager não
cria outro dono e não encerra o owner emprestado numa paragem normal. O chamador
encerra esse owner depois dos componentes; um fail por saturação é fatal ao nó.

Implementação própria: um worker, ArrayBlockingQueue de N eventos, flag terminal
fora da fila e hooks limitados a 16 registos por owner; este manager regista um.
O futuro nó pode usar o mesmo owner para monitoring/routing sem threads a alterar
vizinhos diretamente. onStop só regista lifecycle; os hooks executam no worker,
fora do lock de registo. Registration é retirada depois do cleanup do componente.
O limite de hooks é local à ferramenta de execução, não um cap de protocolo.

offer nunca espera; se falhar, fail grava a primeira causa terminal e acorda/
interrompe o worker. O finally do loop executa os cleanups no próprio owner, sem
inserir Stop na fila cheia. O ponto de captura solicita também fecho dos recursos
I/O por handles thread-safe; não modifica os mapas de domínio fora do owner.
Os eventos enfileirados deixam de poder criar trabalho após a flag de paragem.

Callback de transporte, conclusão de future ou timer só captura dados e oferece
evento. Nenhum DNS, join, get de future incompleto ou put bloqueante no owner.
Se um Observer bloquear e impedir o finally, o fecho de I/O é solicitado por fora
e o shutdown termina em erro explícito de prazo, sem alegar worker terminado.

## 4. Núcleo, portas e relógio

O construtor de testes package-private recebe Ports/clock/jitter, todos tipos
aninhados. As três classes autorizadas continuam suficientes.

```java
interface Channel {
    UUID id(); boolean isOpen();
    Connection.SendResult send(ControlMessage message);
    CompletableFuture<Void> sendAndFlush(ControlMessage message);
    void close();
}
interface Ticket { void cancel(); }
interface Deadlines {
    void limit(int maxPhysicalTickets);
    Ticket schedule(Runnable task, long delayNanos);
    void requestClose(); boolean awaitTermination(Duration timeout);
}
interface Jitter { long choose(long minInclusive, long maxInclusive); }
interface Resolver {
    CompletableFuture<InetAddress> resolve(UUID jobId, String configuredHost);
    void retire(UUID jobId);
}
interface Sink {
    void opened(Channel channel, long observedAt);
    void message(Channel channel, ControlMessage message, long observedAt);
    void closed(Channel channel, Connection.CloseReason reason, long observedAt);
}
interface Network {
    CompletableFuture<InetSocketAddress> open(InetSocketAddress resolvedBind,
                                            Transport.Options options, Sink sink);
    CompletableFuture<Channel> connect(InetSocketAddress resolvedRemote);
    int reservations();
    void requestClose(); boolean awaitTermination(Duration timeout);
}
interface IoServices {
    Resolver resolver(); Network network();
    void requestClose(); boolean awaitTermination(Duration timeout);
}
interface Ports {
    SerialOwner owner(int eventsCapacity);
    IoServices io(int maxPhysicalJobs);
    Deadlines deadlines(int maxPhysicalTickets);
}
NeighborManager(NodeConfiguration config, UUID instanceId, SerialOwner borrowedOwner,
                Observer observer, LongSupplier clock, Jitter jitter, Ports ports);
```

LongSupplier nanoClock serve deadlines; clock/Jitter/Deadlines/Network/Resolver/
SerialOwner/Observer são controláveis em NeighborManagerTest. Fakes não tentam
construir/subclassificar Connection final nem aceder aos hooks privados de outro
pacote. O wrapper real adapta Channel a Connection pública. Fake await consome
o relógio controlado e regista o budget restante; TCP real confirma a fronteira.

Core possui Phase, immutable desired, Peer por ID, Context por connectionId,
NativeDial por token ainda não concluído, tickets por chave e contadores de
retenção. AttemptId UUID local nunca vai no fio. Todos os eventos verificam
geração/contexto/estado antes de afetar Peer; sessão confirmada compara também
instanceId. Não manter tombstones/cache de conexões retiradas sem limite.

Ports cria os serviços só após validação/start; IoServices partilha o mesmo worker
entre Resolver e Network.open/bind. resolve/open/connect devolvem resultados
nativos apenas ao terminarem esses trabalhos, sem expor o seu cancelamento na API
pública. retire remove job ainda em fila ou pede interrupção do que corre, mas o
future de um resolver em execução só termina quando o job realmente termina.
Sink preserva a ordem do leitor e captura observedAt antes da oferta ao owner.
IoServices.await verifica o worker local; Network.await verifica separadamente
Transport. O teste pode envolver Network/Sink sem alterar o transporte existente.

## 5. Configuração e arranque

Validar perfil overlay, contacto bootstrap, nodeId/instanceId não zero, hosts/
portas/papel e runtime antes de criar recursos. A normalização lexical de host
fica em helper interno equivalente à do loader/bootstrap, sem chamar o método
package-private do outro pacote e sem DNS do valor recebido. String.compareTo
sobre os IDs ASCII válidos define iniciador, sem locale/ordem numérica de sufixo.

Plan contém Endpoint local imutável e Transport.Options obtido de Control:
queueCapacity, maxConnections, connectTimeout, shutdownTimeout. Durações têm
milissegundos inteiros 1..Integer.MAX_VALUE; validar antes de toNanos/toMillis.
Não criar propriedade nem elevar hard cap. Listas recebidas validam grau ≤63 e
grau ≤C, own ID/duplicados/instâncias zero/papéis/colisões antes da instalação.

start inicia DISCOVERING e, no worker de I/O limitado, resolve bind configurado
e abre o Transport. Resolução/bind não bloqueiam o owner. Um bind tardio após Stop
é fechado ao obter o handle, antes de o publicar como pronto. O handle de I/O
é registado para fecho de emergência; esse registo não é estado de Neighbor.
Depois resolve/contacta bootstrap, envia REGISTER com requestId novo e espera
REGISTER_OK ou ERROR correlacionado. Consulta válida instala a lista uma vez,
fecha o canal inicial e torna READY. Entrada HELLO antes de READY não é autorizada.

Q-C-05 aprovada: ERROR corrente de REGISTER com code 10/11 termina a tentativa e
agenda retry inicial; restantes codes levam a FAILED com rejeição integral.
Erro de contexto no canal da consulta corrente também fecha localmente e vai a FAILED.
Um canal desconhecido aceite durante DISCOVERING não é essa consulta: isolá-lo
sem terminar a tentativa legítima do bootstrap, conforme Q-C-05/ADR-020.
Erros de transporte/prazo anteriores ao sucesso têm retry com backoff. FAILED
solicita cleanup de arranque; não criar loops automáticos de erro permanente.
Emitir Diagnostic(FAILED, causa original) e completar start excecionalmente antes
de limpar recursos; cleanup não mascara essa causa com o EOF que ele próprio
provoca. Stop explícito posterior pode concluir CLOSED, preservando o diagnóstico.
Depois de READY não há novos contactos bootstrap, incluindo retries de peers.

## 6. DNS e trabalho físico limitado

Um único worker de I/O por manager executa jobs de resolução e o bind inicial,
com fila ArrayBlockingQueue de capacidade 65: no máximo D+2 hosts configurados
(D≤63, mais bind e bootstrap). Sem threads novas por mensagem ou retry.
O registo global limita também jobs físicos totais (em fila + a correr) a 65,
incluindo o job de bind; reservar antes de submeter, sem exceder esse total.
Resolver usa InetAddress.getByName apenas sobre esses hosts; não recebe host
do frame HELLO. Um resultado é um endereço para a porta configurada, não uma
prova de identidade. A comparação de endpoint continua lexical e independente.

Há no máximo um job físico não concluído por host normalizado. Vários destinos
no mesmo host podem esperar pelo mesmo resultado; waiters são tokens correntes,
limitados aos D peers e uma operação de descoberta. Sem cache histórica infinita:
retirar job após conclusão e entrega ao owner; uma tentativa posterior pode
resolver de novo, conservando os limites. Não persistir IP ou lista após processo.

Cada espera de resolução/bind tem budget de control.connectTimeout, capturado
antes de enfileirar o job; inclui a fila. Esta concretização foi aprovada
explicitamente em [Q-C-06](../../../docs/construction-duvidas.md#q-c-06--prazo-próprio-de-resolução-e-bind-no-imp-004),
sem nova propriedade ou alteração do timeout TCP no transporte.
Depois de resolver, o dial TCP conserva o seu connectTimeout. Os budgets são
fases explícitas; não se apresenta o seu somatório como uma meta do enunciado.

Timeout/cancelamento retira o waiter lógico. Job ainda em fila pode ser removido
antes de executar; job a correr conserva a reserva até terminar fisicamente.
Não submeter outro para o mesmo host enquanto estiver bloqueado. Retry entretanto
elegível adia/reaproveita o job corrente dentro do seu próprio prazo; não aumenta
o número de workers/jobs. Resultado tardio carrega jobId e attemptId e não revive
uma tentativa retirada. Fila cheia é falha explícita de recurso, sem fallback
para executor comum, thread extra ou execução DNS no owner.

cancel/shutdownNow não prova que getByName terminou: uma resolução do sistema
pode ignorar interrupção. Await verifica a terminação real do worker. Se não
terminar no budget global, close lança erro com o recurso pendente identificado;
não afirmar shutdown bem-sucedido, thread removida ou cancelamento físico pelo
isDone de um future. Os testes incluem resolver bloqueável que só termina quando
libertado. Esta limitação de erro explícito já está no Model aprovado/ADR-020.

## 7. Correlação do dial sem alterar Transport

Antes de executar connect, o owner regista NativeDial(token, target, finalidade,
geração) no conjunto de dials **fisicamente não concluídos**. Os callbacks usam
connectionId e handle; a conclusão do future produz DialResult(token, channel/erro).
Não inferir direção por IP, porta efémera ou pelo facto de Open ter chegado primeiro.

Se DialResult chega primeiro, cria/associa Context OUTBOUND ao token/alvo. Open
posterior usa o mesmo ID sem reiniciar o relógio. Se Open/Message chegam primeiro,
Context começa UNCLASSIFIED e guarda a barreira dos tokens nativos ainda não
concluídos existentes quando o owner processa esse Open. O conjunto pode ser um
sobraconjunto conservador, mas inclui qualquer dial que possa ser dono do canal.
São os únicos dials que
podem ser donos do canal; dials iniciados depois desse Open não entram na barreira.

Um Context UNCLASSIFIED só pode tornar-se INBOUND quando a barreira ficar vazia
e nenhum DialResult o tiver associado. HELLO/HELLO_OK/ERROR que chegarem antes
ficam na retenção limitada da secção 8. Um peer malicioso não consegue confirmar
um HELLO inbound num socket que um DialResult ainda pode identificar como outbound.
Direção e alvo são validados antes de Up, mesmo que HELLO pareça plausível.

Ao concluir um dial, processar primeiro a associação/fecho do seu channel, depois
retirar o token das barreiras dos restantes Contexts e reavaliá-los. Uma conclusão
de erro sem channel retira só o seu token. Context fechado, channel.isOpen=false,
tentativa retirada ou prazo vencido não podem originar Up. Open tardio de um
channel já fechado não o recria como saudável. Fecho anterior a DialResult não
exige guardar um tombstone: isOpen e geração da tentativa cercam a conclusão.

Para preservar esta prova, o manager **não cancela o CompletableFuture nativo
de Transport.connect durante uma retirada individual**: cancelá-lo poderia perder
o handle de uma attach concorrente, mantendo Open sem correlação. Retirar o token
logicamente, manter a sua conclusão registada e fechar qualquer channel tardio.
O socket conserva o timeout TCP; não iniciar outro dial desse peer até o anterior
terminar nativamente. Isto não altera o contrato público de Transport.cancel.
Em shutdown, Transport.requestClose fecha imediatamente todos os sockets/dials,
sem depender dessa espera lógica, e depois await verifica os workers reais.

Assim, mesmo um token lógico retirado continua na barreira até DialResult nativo,
limitado pelas reservas C. Não usar Future.isCancelled como prova de que um socket
nunca existiu. Saturação ao publicar DialResult pede fecho imediato do handle e
paragem explícita do owner, evitando um recurso órfão fora da fila.

O custo dessa barreira é latência: um inbound pode esperar por outros dials em
curso. O deadline de HELLO nunca é renovado por essa espera; se expirar, fechar
localmente e permitir retry. O desenho troca essa latência limitada por uma
classificação correta com a API atual. D14 tem de provar esse caso, não o esconder.

## 8. Retenção e memória: limites concretos

Definir C=control.maxConnections, Q=control.queueCapacity e N=eventsCapacity.
Para Context provisório ou handshake confirmado no fio ainda à espera de evento
local, usar **B=min(4,Q) frames por canal** e **R=min(N,C×B) frames no total**.
São limites internos derivados, sem novas propriedades/caps wire. Defaults:
C=64, Q=256, N=4096 → B=4/R=256. Contar antes de inserir; retirar a cobrança ao
processar/libertar cada frame. Context removido drena a sua cobrança uma vez.

Retenção mantém ControlMessage imutável e ordem de chegada por conexão; não faz
nova serialização/cópia de payload nem acumula bytes de socket fora do codec.
Cada frame respeita o corpo TCP ≤65536; R limita quantidade, não é uma medição de
heap. A fila de eventos N e as filas nativas de saída Q são recursos separados.
Limites muito pequenos podem provocar rejeição explícita sob concorrência;
não há promessa de convergência durante saturação deliberada.

Overflow de retenção fecha apenas o Context ofensor, emite diagnóstico local
RESOURCE_LIMIT e retira candidato/sessão se lhe pertencer; não apaga outro peer.
Não enviar ERROR de resposta a um ERROR que causou o overflow. Se um envio de
handshake/erro já está pendente, fechar localmente em vez de admitir outra resposta
para contornar o limite de uma confirmação por Context. A fila serial cheia é
um erro global do dono, tratado pela secção 3, sem publicação adicional nessa fila.

| Recurso | Limite efetivo |
|---|---|
| Lista desejada / peers | D≤63, instalação rejeitada se D>C |
| Reservas Transport | C≤64, incluindo dials e workers ainda a terminar |
| Contexts de canais | ≤C, incluindo anónimos e canais em fecho ainda registados |
| Dials nativos não concluídos | ≤C, no máximo um por peer; descoberta ≤1 |
| Tokens de barreira | ≤C por Context; ≤C²≤4096 referências no total |
| Retenção | ≤B≤4 por Context; ≤R≤256 frames no total |
| Eventos | N≤4096; nenhuma fila paralela ilimitada de notificações |
| Agenda | ≤D tickets de fase/retry, ≤C deadlines de Context, ≤1 fase inicial |
| I/O local / DNS | 1 worker, fila 65, jobs por host ≤D+2≤65; waiters ≤D+1 |
| Confirmações de escrita | ≤1 operação handshake/erro corrente por Context |

Uma reserva TCP só é libertada pelo transporte após os dois workers terminarem.
Não decrementar artificialmente C ao retirar uma tentativa de domínio. Context
retirado não precisa de história ilimitada; fechos tardios são comparados pela
sessão/geração, não usados para limpar o Neighbor corrente por nodeId apenas.

## 9. Handshake, unicidade e ERROR aprovado

Só o menor ID agenda dial de peer. Após correlacionar o outbound, enviar HELLO
com Endpoint local e requestId novo; guardar helloWritten=false/helloOk=false.
HELLO_OK válido marca helloOk e fixa peerInstance não zero; Written válido marca
helloWritten. Só ambas as condições e channel ainda aberto permitem ACTIVE.

Inbound classificado exige HELLO, senderId/Endpoint.nodeId e senderInstance/
Endpoint.instanceId coerentes, allowlist/endpoints/papel/direção corretos.
Reservar candidato por ID antes de enfileirar HELLO_OK; ACTIVE só após Written
da resposta. Não substituir candidato/vínculo saudável por UUID mais recente ou
primeiro socket. Duplicado recebe DUPLICATE_ID se for seguro/possível dentro do
prazo; o vínculo existente permanece. Rejeição não aprende novo endpoint.

Depois de validar a identidade e iniciar a barreira de confirmação, frames de
aplicação 7–16 e ERROR(17) da janela legítima aguardam em B/R. Up publica Session/
snapshot e só depois drena Received em FIFO. Falha/prazo limpa retidos com
diagnóstico, sem entrega à instância seguinte. Antes de uma janela legítima,
mensagem incompatível falha o contexto; ERROR nunca gera outro ERROR.

Q-C-05 aprovada é aplicada por operação, não apenas por code:

| Estado/operação | Condição e ação |
|---|---|
| REGISTER pendente | ERROR com token/connectionId/requestId/prazo corretos e relatedType=1: rejeição remota integral; 10/11 retry, restantes FAILED |
| HELLO enviado pendente | ERROR do alvo/instância esperados, mesmo pedido/contexto e relatedType=5: retirar só candidato; retry pelo menor ID |
| HELLO_OK enfileirado ainda pendente | ERROR do HELLO validado, mesmo requestId/contexto, relatedType=6: rejeitar candidato; Written tardio não reativa |
| ERROR inesperado antes de ACTIVE, fora da janela legítima | Diagnóstico de contexto e close local só desse canal; descoberta FAILED ou peer DISCONNECTED; nunca responder ERROR |
| ACTIVE | Received(Session, mensagem original), sem filtro de code/relatedType/requestId próprio; consumidor futuro correlaciona, sem Down só pelo payload |
| Evento antigo/retirado/fora do prazo | Diagnóstico/contador limitado; não mudar sessão nova ou causa terminal anterior |

Preservar inclusive relatedType bruto 0/3/17/255 quando válido no formato. O
manager não traduz requestId para pedidos de filhos: essa tarefa é de routing.
Uma falha local não é convertida em ErrorPayload remoto. NODE_LEAVE corrente
retira a sessão e mantém a aresta desejada; nunca é dirigido ao bootstrap.

## 10. Prazos, retry e cancelamento

Capturar clock no callback antes da fila. Context usa o instante mais antigo
conhecido de Open ou BindResult; outro evento não reinicia o prazo. Sem comparar
nanoTime por Math.min: calcular tempo decorrido/subtração modular, válido para
budgets <2^63 ns, incluindo wrap. Evento depois de expirado não confirma sessão.
O ticket provisório usa helloTimeout; ao identificar bootstrap, aplicar
bootstrapQueryTimeout desde a abertura observada, sem apagar o tempo em fila.

Há um ticket de fase/retry por peer: DNS, dial lógico ou espera de retry, nunca
vários tickets sobrepostos desse Peer. Context de Open tem o seu deadline HELLO,
mesmo antes de BindResult. Consulta/arranque usa um ticket global de fase.
ScheduledThreadPoolExecutor com um worker e remove-on-cancel; registo de tickets
limitado a D+C+1 e geração por chave. Callback do timer só oferece um evento.
Ticket cancelado que já começou pode publicar evento velho, cercado pela geração;
não manter uma lista histórica de todas as futures agendadas.

O adaptador reserva uma permissão física antes de schedule e só a liberta depois
de retirar um job que não começou ou do finally do callback em execução. Um
future cancelado/isDone não basta para libertar essa permissão. O máximo físico
provisionado é 63+C+1; chamar limit(C+1) antes de qualquer agendamento e elevar
para limit(D+C+1) ao instalar a lista. A admissão testa o contador físico contra
esse limite ativo, incluindo callbacks retirados que ainda terminam; não basta
contar as chaves lógicas atuais. Se uma substituição não couber porque
um callback cancelado ainda termina, rejeitar a operação com diagnóstico de
recurso e cleanup da parte afetada; não submeter além do limite nem esquecer o
prazo. D17 inclui esse interleaving e comprova limite físico, não só tamanho do mapa.

Função exata de retry, em milissegundos long, sem overflow:

```text
base := retryInitial na primeira falha; nas seguintes min(retryMax, 2*base)
         (testar base > retryMax/2 antes de multiplicar)
spread := floor(base * retryJitterPercent / 100)
j := Jitter.choose(-spread, +spread), inteiro uniforme no adaptador real
delay := max(1, min(retryMax, base+j))
```

base≤Integer.MAX_VALUE, percent≤100; operações intermédias usam long. Jitter fake
fora do intervalo é erro explícito, não um valor ocultamente corrigido. Reset de
base após ACTIVE. Quando retry vence, se ainda há dial nativo/resolução física
incompatível, adiar com novo backoff; não duplicar trabalho do mesmo peer/host.
Stop cancela elegibilidade/tickets. Nenhum retry pode voltar ao bootstrap em READY.

## 11. Shutdown e ownership

requestClose é não bloqueante: primeira flag vence, rejeita novos comandos,
solicita Transport.requestClose e fecho da agenda/I/O worker; oferece Stop ou usa
o fail/hook fora da fila se não houver espaço. Estado de Peer e snapshots só
mudam no owner; snapshot() retorna vazio quando a flag de fecho já está ativa,
evitando apresentar autorização operacional antiga enquanto o owner termina.

O cleanup no owner retira candidatos, cobra/liberta retidos, emite Down uma vez,
fecha todos os Channels, cancela waiters/tickets e completa comandos pendentes
com causa local. Conclusões tardias fecham os handles obtidos, sem publicar Up.
Handles físicos de I/O estão registados separadamente para rollback/fecho, mesmo
se bind ou resolução completarem depois da paragem lógica.

close num callback/owner só solicita paragem. close externo mede **um** budget
global shutdownTimeout, desde o primeiro pedido de fecho: await de cleanup,
Transport, agenda, I/O e owner próprio recebe sempre o tempo restante, fora de
locks. Arredondar restante para baixo para APIs que exigem ms; <1 ms é prazo
esgotado, não nova espera de 1 ms por recurso. Repetir close não renova o budget;
se tudo já terminou, devolve normalmente. awaitTermination(timeout) permite
observação posterior explícita, sem mudar o resultado de timeout anterior.

Owner emprestado fica ativo para o chamador; aguardar só cleanup deste manager
e os recursos que ele criou. Teste de partilha encerra o owner no fim da fixture.
Worker DNS/Observer bloqueado após o prazo implica erro explícito de shutdown;
libertar depois e verificar terminação não reescreve o resultado anterior.

## 12. D01–D22: prova do desenho contra o Model

Todos os testes e resultados funcionais estão por executar. Os IDs abaixo
orientam os casos futuros em NeighborManagerTest, sem alegar contagem JUnit.

| Design/Model | Casos concretos a introduzir em Implement |
|---|---|
| D01/M01 | Construtores inválidos falham antes das factories; observer/owner/config/runtime coerentes, sem rede |
| D02/M02 | REGISTER_OK/ERROR/EOF por conexão/geração/requestId; codes 10/11 versus FAILED; rejeição integral observável, zero ERROR de resposta |
| D03/M03 | Cópias defensivas, grau 63/C, próprios/duplicados/colisões/papéis; zero instalação/dial parcial |
| D04/M04 | Contador de consultas fica constante em READY; bootstrap parado e reconexão; processo novo necessita descoberta |
| D05/M05 | Seis ordens de arranque de três nós e arranque simultâneo; só menor ID disca, uma sessão por par |
| D06/M06 | HELLO com host lexical/papel/porta/ID/instância/direção errados; peer saudável preservado, sem DNS do payload |
| D07/M07 | Negativas HELLO/HELLO_OK e ERROR inesperado/antigo, incluindo os suboráculos aprovados de Q-C-05; nenhum Up indevido |
| D08/M08 | Flush bloqueado/HELLO_OK antes de Written; Up antes de drain; buffers em B/R-1, B/R e B/R+1; falha liberta cobrança |
| D09/M09 | Dois candidatos/ID vivo/UUID distinta; preservar sessão saudável, sem escolher direção por primeiro socket |
| D10/M10 | Instância nova após fecho; callbacks antigos de Close/Message/ERROR/Written/timeout não a removem |
| D11/M11 | Received íntegro com ERROR ACTIVE e relatedType bruto; consumidor fake correlaciona NO_ROUTE/PATH_CHANGED; sem pedido próprio do manager, sem Down/ERROR de resposta |
| D12/M12 | Queue delay, prazo exato, wrap de nanoTime e mensagem duplicada não renovam; budgets de cada fase explícitos |
| D13/M13 | Base/jitter 0/100, overflow evitado, clamp 1..max, reset após ACTIVE; um ticket por peer e seed registada |
| D14/M14 | Todas as intercalações com DialResult permitidas, preservando Open→Message→Closed do leitor; barreira de outros dials; HELLO forjado antes de Bind não confirma inbound; prazo inclui a espera |
| D15/M15 | Retirada lógica antes da conclusão nativa, sucesso tardio fechado; DNS a ignorar interrupção mantém reserva, sem duplicar job/worker |
| D16/M16 | NODE_LEAVE corrente/duplicado/antigo, Down único, desejo conservado e reconexão pelo iniciador |
| D17/M17 | C/D/tickets/barreiras/B/R/jobs/waiters nos limites e overflow; reservas só acabam fisicamente; sem mapas de IDs arbitrários |
| D18/M18 | owner.offer=false, consumidor que lança erro e agenda/I/O cheios: falha observável, cleanup sem slot adicional |
| D19/M19 | Stop em bind/DNS/bootstrap/dial/handshake/ACTIVE; budget restante em awaits, timeout real de worker preso, porta reutilizável e close idempotente |
| D20/M20 | Overrides capturados em Transport.Options/hello/retry/eventos; dois componentes fake no mesmo SerialOwner, manager não o encerra ao sair; nenhuma mutação fora do owner |
| D21/M21 | TCP real/BootstrapServer público, três nós, peer desconhecido e nova instanceId; capturar e fechar Connection de teste via wrapper do Listener para simular falha com managers vivos e bootstrap parado |
| D22/M22 | clean verify/JDK21 atual, XML/log/JAR/hash; manual embutível com comandos e operador real, revisão humana e integração antes de DONE |

Não é necessário expor Connection ao utilizador para D21: factory package-private
de testes envolve Transport.Listener e conserva handles apenas na fixture.
Os testes TCP usam perfis completos e portas previamente reservadas/libertadas;
falha de bind é resultado real, sem ignorar o teste ou alterar a validação de porta.
Manual posterior terá de usar uma forma pública/reproduzível de provocar a falha
sem afirmar prova CORE/JVMs completas: após descoberta e ACTIVE nos três managers,
parar BootstrapServer, guardar Session corrente e chamar disconnect dessa sessão.
Observar Down, novo connectionId/mesmas instâncias, Up e ausência de consulta ao
bootstrap encerrado. Reutilizar a Session antiga em disconnect não fecha a nova.
Reinício com nova instanceId é uma prova distinta: repor bootstrap para a nova
descoberta, como exige CP-02. Preparar/executar o roteiro em Test, sobre API final.

## 13. Verificação documental e checkpoint

Este desenho não exige mudar os contratos públicos existentes: adaptadores/
interfaces novas residem nos três ficheiros autorizados. Reter um dial nativo
até correlação tem custo e testes explícitos; não se introduz um indicador de
direção no wire ou se altera a semântica de cancelamento de Transport.

Model/Q-C-05 e Design/Q-C-06 estão aprovados. Design foi concluído em 2026-11-10T15:13:50Z;
a revisão formal de ADR-012/013/018/019/021/024 e das decisões transversais é
a etapa ADR agora autorizada, ainda sujeita a revisão antes de Implement/Test. Se os oráculos revelarem que uma decisão aceite é inviável
ou que é necessário alterar fundações, aplicar ADD 11 antes de intervir.

Nenhuma execução de build/V-007/regressão/manual, nova dependência, Java, commit,
push ou outro IMP. O resultado histórico de 856 testes não foi reexecutado aqui.
Revisor final atribuído Denji e integração em main continuam exigidos antes de
DONE; sem gates externos adicionais nem revisão humana presumida.

Aprovação de Q-C-06 registada sem alterar as linhas dos oráculos D01–D22.
A precisão sobre o canal da consulta em DISCOVERING explicita o isolamento de
falhas já exigido; não amplia a política de FAILED a canais de terceiros.
