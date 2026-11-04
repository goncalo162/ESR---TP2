# IMP-003 — Design: descoberta inicial e escrita confirmada

Design aprovado explicitamente pelo utilizador em 2026-11-04T11:25:40Z (registo),
incluindo requestClose sem espera e preservação do comportamento de close().
Sem implementar Java ou executar V-006.
Model e as concretizações de [Q-C-04](../../../docs/construction-duvidas.md#q-c-04--coerência-da-instância-e-resposta-única-na-consulta-inicial)
foram aprovados explicitamente pelo utilizador, com registo em 2026-11-04T10:50:52Z.
[Revisão ADR aprovada](adr-review.md) em 2026-11-04T11:37:45Z; continuação autorizada
inicialmente em Implement, aprovado em 2026-11-04T12:18:01Z, com [Test para revisão](ddd-03-test-report.md).
As especificações abaixo são o desenho aprovado; a execução posterior está no walkthrough.

Base: Denji 4ac5808dacf6e97bfd3636d2788db363e926bc95, dependências IMP-001/002
concluídas/integradas. Reutilizar [IMP-003](../../../add.md#imp-003),
[Model M01–M18](ddd-01-domain-model.md), [contrato v1](../../../docs/07-API-AND-INTERFACES.md)
e [V-006](../../../docs/09-TESTING-STRATEGY.md#v-006--fornecer-localização-inicial-dos-vizinhos-configurados).
ADR-003 permanece substituída; aplicar 013/014/015/024 e invariantes transversais
010/012/016/017/018/020/021/026. Este documento não executa a etapa ADR.

## 1. APIs propostas e âmbito dos cinco ficheiros

As assinaturas abaixo são desenho, não código já disponível. Só a assinatura
sendAndFlush é imposta pelo ADD. Os outros nomes concretizam o mínimo necessário
para compor/testar este serviço sem criar ficheiros Java adicionais.

| Ficheiro | Superfície e alteração proposta |
|---|---|
| bootstrap/BootstrapServer.java | `public final class BootstrapServer implements AutoCloseable`; construtor `BootstrapServer(NodeConfiguration configuration) throws IOException`; `InetSocketAddress localAddress()` e `void close()` |
| transport/Connection.java | Preservar construtor interno e todos os métodos/enums públicos; acrescentar `public CompletableFuture<Void> sendAndFlush(ControlMessage message)`; substituir a entrada privada byte[] da fila por PendingWrite; acompanhar confirmações e frame em escrita |
| transport/Transport.java | Preservar construtores públicos, close/awaitTermination e Options de quatro argumentos; acrescentar requestClose para iniciar shutdown sem espera; mover fecho de conexões para fora de lifecycle e injetar escritor apenas por overload interno de teste |
| bootstrap/BootstrapServerTest.java | Fixtures/Channel/relógio/scheduler/eventos falsos em tipos aninhados; casos de grafo/identidade/host/consulta/prazo e TCP real |
| transport/TransportTest.java | Preservar os 18 testes existentes; acrescentar testes da fila comum, escrita/flush controlados, falhas, corridas, cancelamento e shutdown |

Caminhos de produção/teste são os cinco do IMP-003, relativos aos pacotes já
existentes em Projeto/src. Tipos auxiliares ficam aninhados nesses ficheiros;
não criar framework, dependência, propriedade, serviço ou ficheiro de produção.
Não alterar loader/records de configuração, codec, enums do wire, fixtures,
POM, OverlayNode, NeighborManager ou OttApplication.

BootstrapServer expõe o serviço embutível para a composição futura do IMP-005.
`localAddress()` permite comprovar o endpoint real nos testes. Não introduz CLI
ou processos overlay demonstrados. O construtor público usa bind.host/control.port
validados; resolução do endereço de bind próprio ocorre antes de arrancar o
executor serial e fora dos callbacks. Nunca resolver o host recebido num pedido.

O overload de teste, de acesso de pacote, aceita bind resolvido (porta efémera
0 permitida só nesse caminho), relógio/scheduler/eventos e uma TransportFactory.
Não relaxar portas do ficheiro de configuração. A factory recebe bind, Listener
e Options, devolvendo Transport; em testes negativos só conta invocações e falha
se houver tentativa de abrir rede antes da validação.

## 2. Validação antes do bind e comparação lexical de hosts

Antes de DNS de bind, socket ou worker: verificar configuração não nula/papel
BOOTSTRAP, identidade/bind/publicitação válidas, dataPort=0, topologia presente,
ausência de campos de perfil incompatíveis (contacto bootstrap, streams/media)
e Control válido. A presença dos outros seis grupos no RuntimeConfiguration
não é erro: o BOOTSTRAP só consome Control, como no IMP-002. O caminho normal
recebe a configuração de ConfigurationLoader.load. Como os records públicos
não validam todos os domínios, a fronteira do serviço verifica também os valores
que consome quando construídos diretamente: 1..64 nós, IDs/chaves coerentes,
papéis overlay, hosts/portas autorizados, endpoints sem colisões por transporte,
vizinhos existentes, sem auto-arestas e grafo simétrico, grau 1..63. O ID do
bootstrap não pertence ao grafo overlay e o seu endpoint TCP não colide com
outro endpoint TCP configurado. Não corrigir
arestas nem publicar um grafo parcial. Configuração direta inválida lança
IllegalArgumentException; carga do ficheiro mantém ConfigurationException agregada
no loader existente. Não abrir ficheiros/alterar o loader para simular a validação.

A comparação é **lexical após a normalização existente**, conforme docs/07 §1
e ConfigurationLoader.host. `loadDeployment` usa equals sobre strings que já
passaram por essa normalização; equals sobre o host bruto de rede seria insuficiente.
Esclarece a formulação abreviada do Model; não muda o contrato de configuração.

Helper interno `static String normalizeHost(String host)` em BootstrapServer:

1. Exigir ASCII sem espaços/controlos, 1..253 caracteres; validar autoridade
   de host com java.net.URI, sem InetAddress.getByName/getAllByName.
2. Aplicar Locale.ROOT para caixa; remover um par externo de colchetes de IPv6
   e um ponto terminal, como no loader. Não aplicar strip a um host de rede
   inválido para o tornar válido; o codec já rejeita espaços nesse campo.
3. Se a string for IPv4 numérica com pontos, exigir quatro componentes decimais
   no domínio 0..255 e escrever cada um com Integer.toString; não interpretar
   zeros iniciais como octal. Preservar as rejeições sintáticas do loader.
4. Rejeitar wildcard no endpoint publicitado: 0.0.0.0 e IPv6 todo zero, incluindo
   zone, pela regra lexical atual. Bind próprio tem o tratamento permitido existente.
5. Comparar os valores normalizados, role e ambas as portas configuradas.
   A resposta usa os valores configurados normalizados, nunca os dados recebidos.

Exemplos a testar: Node.Example. equivale a node.example; IPv4 com componentes
decimais normalizados equivale à sua forma sem zeros, quando a sintaxe é aceite
pelo loader; colchetes/caixa de um mesmo literal IPv6 seguem a regra acima.
localhost não equivale a 127.0.0.1; IPv6 comprimido não se equipara a uma expansão
diferente só porque representa o mesmo IP. Sem canonicalização física, aliases
DNS ou inferência pelo socket remoto. Mesmo IP/portas diferentes continua a
permitir processos distintos. Ports TCP de origem efémeros não são comparados
com controlPort publicitado.

Não expor o helper privado do loader nem alterar a sua API: a pequena rotina
lexical local é testada contra configurações temporárias carregadas pela API
existente. Detetar divergência real antes de intervir nas fundações.

## 3. Consulta, resposta única e erros

O core recebe eventos Open, Message, Closed, Written e Expired; guarda contextos
por Connection.id(), sem índice de membros por nodeId. Cada contexto contém
canal, instante inicial/deadline, estado AWAITING_REQUEST/WRITING_RESPONSE,
requestId/tipo após pedido e ticket de timeout. É estado efémero de operação,
não registry, cache de respostas ou disponibilidade.

REGISTER requer senderId=Endpoint.nodeId e senderInstance=Endpoint.instanceId
não zero (Q-C-04). O nodeId deve existir no grafo. Normalizar/comparar host,
controlPort, dataPort e role com o Node configurado. Pedidos de duas instâncias
válidas do mesmo ID coexistem e consultam a mesma função do grafo.

Produzir NeighborList com exatamente os adjacentes, na ordem preservada pelo
grafo carregado, sem filtrar offline. InstanceId de cada endpoint é UUID zero.
REGISTER_OK conserva requestId e usa nodeId/instanceId do bootstrap como emissor.
Gerar uma instanceId não zero do bootstrap por execução do serviço, fora da
configuração; teste injeta um UUID fixo no core. Sem lease, inscrição, ACK remoto
ou modificação de TopologyConfiguration.

| Situação já descodificada com segurança | Resultado contextual |
|---|---|
| Tipo diferente de REGISTER, incluindo ERROR/NODE_LEAVE/HELLO/RENEW | ERROR INVALID_MESSAGE, relatedType=type.code(), mesmo requestId |
| senderId/Endpoint.nodeId ou instâncias incoerentes | ERROR INVALID_MESSAGE |
| ID ausente do grafo ou host/portas/papel incompatíveis | ERROR UNAUTHORIZED_NEIGHBOR |
| REGISTER autorizado | REGISTER_OK |
| Envelope/frame inseguro ou rejeitado pelo codec | Fecho com razão local existente; sem ERROR inventado pelo handler |

Mensagens de erro fixas/curtas (≤256 bytes), sem eco de paths/stack traces.
Não usar DUPLICATE_ID para consultas simultâneas. Não alterar os 12 códigos.
Depois da primeira decisão de resposta, descartar mensagens **válidas e já
descodificadas** nessa conexão, sem segunda resposta e sem renovar prazo.
Um frame adicional malformado continua sujeito ao fecho normal do transporte;
não prometer conclusão da resposta se o próprio peer viola framing ou fecha cedo.

Ao escolher resposta: passar a WRITING_RESPONSE antes de chamar sendAndFlush;
ligar whenComplete que só publica Written(connectionId, contexto esperado,
resultado). Future já concluído pode executar a continuation imediatamente:
o contexto já tem de estar preparado. Processar Written só se o mapa ainda
referir o mesmo contexto; fechar canal/remover contexto/cancelar ticket.
Closed/Expired repetidos ou atrasados são idempotentes e não afetam outra consulta,
mesmo que esta pertença ao mesmo nodeId. Nunca usar join/get no handler.

## 4. Fila comum, frame em escrita e confirmação

Preservar ArrayBlockingQueue e a capacidade atual; mudar só o seu tipo de entrada
de byte[] para uma classe privada PendingWrite. Esta possui bytes codificados
privados, future opcional (nulo para send) e decisão local de término. O frame
não é exposto nem recebe bytes mutáveis do chamador: vem de MessageCodec.encode.

Connection guarda também `inFlight` (zero/um PendingWrite) e um conjunto privado
de confirmações admitidas ainda não retiradas pelo escritor. Só existem entradas
da fila ou do único frame retirado, nunca histórico ilimitado. Ambos os métodos
chamam o mesmo caminho de validação/admissão. O encode continua no chamador,
sem I/O; não deslocar o codec nem alterar a semântica de send.

| Operação | Admissão/resultado |
|---|---|
| send válido e espaço | Enfileirar PendingWrite sem future; devolver ACCEPTED |
| sendAndFlush válido e espaço | Registar PendingWrite/future sob o mesmo lock da admissão e enfileirar; devolver future |
| Mensagem inválida | send → INVALID_MESSAGE; sendAndFlush → failedFuture com ProtocolException de formato; não fechar ligação saudável |
| Conexão fechada | send → CLOSED; sendAndFlush → failedFuture IOException com razão curta |
| Fila cheia | Marcar fecho OUTPUT_OVERFLOW só nessa conexão; send → OVERFLOW; nova confirmação falha e as já admitidas terminam no escritor |

Não há fila separada de respostas confirmadas nem prioridade que ultrapasse
send. Ordem de frames = ordem das admissões sob o monitor, incluindo chamadas
concorrentes dos dois métodos. queuedMessages continua a contar só a fila,
sem incluir inFlight. Uma fila de capacidade N permite N frames em espera
e um retirado/em escrita; confirmações admitidas ≤N+1, teto 257. A serialização
concorrente já existente permanece fora desse limite de frames admitidos;
não alegar medição nova de memória.

Passos do escritor único:

1. Retirar PendingWrite com take, fora do monitor. Antes de publicar inFlight,
   o future já consta do conjunto de admitidos: fecho nessa janela não o perde.
2. Sob o monitor, revalidar fecho; se aberto, publicar inFlight. Fechado, não
   iniciar bytes novos e encaminhar término com erro para a fase de publicação.
3. Fora de todos os locks, invocar write(frame) e flush no driver de saída.
   Só depois de ambos regressarem normalmente considerar decisão de sucesso.
4. Sob o monitor, decidir sucesso se o fecho ainda não ganhou a corrida;
   caso contrário manter a falha. Retirar a entrada do conjunto e limpar inFlight.
5. Fora dos locks, o escritor publica complete(null) ou completeExceptionally.
   Só ele publica sucesso das escritas admitidas, na ordem dos frames.
6. No finally do escritor, antes de workerFinished/release, retirar todas as
   confirmações restantes e publicá-las com erro fora dos locks. Inclui fila,
   frame retirado antes de inFlight e frame cujo I/O foi interrompido.

Um fecho que ganha antes da decisão pós-flush pode produzir erro mesmo se alguns
bytes já chegaram ao peer; não se promete rollback. Se a decisão de sucesso do
escritor ganhou, um close posterior não a converte em erro. A publicação do
future pode ocorrer logo depois de libertar o monitor. O retorno de
Connection.close solicita fecho; awaitTermination com sucesso é que prova
escritor e confirmações admitidas terminados.

cancel(false/true) bem-sucedido é terminal para o observador. Não se instalam
callbacks para retirar frames ou fechar implicitamente a conexão: cancelamento
não promete impedir bytes. O frame cancelado continua limitado/ordenado e é
libertado pelo escritor ou fecho; complete posterior devolve false e não altera
a CancellationException. Não acumular referências canceladas fora da fila/frame.
O consumidor da consulta fecha o canal ao observar cancelamento/erro.

## 5. Locks, continuations e Transport.close

O monitor de Connection protege reason/admissão/conjunto/inFlight/decisões.
O lock interno de ArrayBlockingQueue é usado por operações curtas; take liberta-o
antes de adquirir o monitor. Não adquirir lifecycle de Transport nesse monitor.
Nenhum write/flush, callback, complete*, get/join ou espera de terminação ocorre
sob o monitor de Connection ou sob lifecycle.

Connection.close(CloseReason) deixa de ser um método inteiro synchronized:
num bloco curto marca reason uma vez, esvazia a fila e marca falha das
confirmações admitidas, incluindo a retirada pelo escritor. Fora do monitor,
fecha socket e interrompe o escritor. Não publica confirmações no chamador;
o escritor acorda/sai do I/O e termina-as antes de libertar a reserva. Assim,
callbacks de futures não são executados na thread externa que faz shutdown.

Transport.close atualmente chama Connection.close dentro de lifecycle. A nova
confirmação exige eliminar essa combinação: sob lifecycle marcar closed e
copiar sockets/conexões/workers; fora dele fechar listener/sockets/conexões e
interromper workers. Dials/accept que já tinham reservado um socket constam do
snapshot; attach/reserve continuam a verificar closed e a recusar novos anexos.
Cada tentativa também trata o próprio erro e termina o future de dial existente.
Não remover reservas antes de ambos os workers terminarem.

Acrescentar `public void requestClose()` ao Transport: a fase idempotente de
marcar closed e fechar/interromper recursos, sem await. close preserva a API
e comportamento anteriores, delegando nessa fase e depois aguardando apenas
se o chamador não é worker. É extensão de lifecycle necessária para o bootstrap
coordenar um único budget com core/timer, dentro do ficheiro autorizado; não
altera wire, política, opções ou a forma de chamar os construtores anteriores.

O teste de "chamado por worker" continua a incluir leitores **e escritores**
do próprio Transport. Uma continuation de confirmação pode chamar close:
inicia fecho e regressa, sem join circular. Chamador externo aguarda o deadline
global de Options.shutdownTimeout; callback deliberadamente preso continua
a produzir timeout explícito como na regressão existente, e pode depois terminar.

CompletableFuture.complete* pode executar continuations síncronas no escritor.
O contrato de callbacks rápidos aplica-se também a elas. Não usar commonPool,
executor de conclusões ilimitado, thread por future ou bloqueio à espera de outro
future. Uma continuation arbitrária que viole esse contrato não ganha garantia
de execução rápida; shutdown externo deve reportar workers não terminados.
Testar explicitamente continuation que chama send/close/Transport.close, sem
locks retidos. Não enfraquecer testes de callbacks existentes para ocultar corridas.

## 6. Core serial do bootstrap, prazos e saturação

Separar um Core aninhado de BootstrapServer, testável sem sockets, do adaptador
Transport.Listener. Um Channel aninhado oferece id(), isOpen(), sendAndFlush e
close(); o adaptador de produção delega diretamente em Connection. State/mapa
de contextos e tickets pertencem exclusivamente ao executor serial do Core.
Configuration/grafo/respostas são valores imutáveis. Core não cria Transport.

Injeções de pacote no próprio BootstrapServer.java:

| Tipo/entrada | Contrato |
|---|---|
| LongSupplier nanoTime | Tempo monotónico para deadline; produção System::nanoTime |
| Deadlines.schedule(Runnable, long delayNanos) → Ticket | Um ticket cancelável por consulta; produção scheduler próprio de um worker |
| SerialEvents.execute(Runnable), shutdown/await | Produção um worker e fila ArrayBlockingQueue de capacidade 4096; teste executor manual limitado |
| Core(configuration, bootstrapInstance, clock, deadlines, events) | Valida dados, deriva grafo e recebe eventos de Channel, sem rede |
| TransportFactory.open(bind, listener, options) | Só depois da validação; conta/abre o transporte na composição, não no core |

4096 é o teto de eventos existente no ADD; BOOTSTRAP não aceita override
node.events.capacity nem passa a consumir opções overlay. Schedule de produção
usa ScheduledThreadPoolExecutor(1) com removeOnCancelPolicy=true e sem tarefas
periódicas; apesar da implementação genérica de fila do JDK, a admissão privada
tem no máximo um ticket por contexto admitido, ≤control.max.connections≤64.
Não expor o scheduler para agendar trabalho arbitrário. Ticket removido em todos
os fechos, sem acumular timeouts cancelados. Filas de eventos e de TCP têm limites
e consequências distintas.

Capturar t0=nanoTime no callback connectionOpen, antes de enfileirar Open;
deadline=t0+Control.bootstrapQueryTimeout.toNanos(). O atraso na fila não
reinicia esse prazo. Ao processar Open, se já expirou, fechar sem agendar;
caso contrário agendar uma vez para o tempo restante. No processamento de cada
Message/Written/Expired verificar deadline por diferenças monotónicas, inclusive
quando o evento do timer chegou atrasado. Não usar horário UTC nem renovar o
ticket ao receber bytes parciais, mais pedidos ou concluir validação.

| Prazo | Origem/unidade | Efeito |
|---|---|---|
| Dial | Control.connectTimeout, default 2000 ms | Options do transporte; separado da duração da consulta |
| Consulta inteira após connectionOpen | Control.bootstrapQueryTimeout, default 3000 ms | Abrange espera de pedido, processamento e resposta/flush; expirar fecha canal e termina pendências |
| Shutdown | Control.shutdownTimeout, default 3000 ms | Budget global do serviço; sockets, core e scheduler devem terminar ou produzir erro explícito |

RuntimeConfiguration já verifica ms inteiros positivos até Integer.MAX_VALUE.
toNanos cabe em long (máximo 2147483647000000); calcular remaining=budget−elapsed,
sem converter diretamente nanoTime absoluto para tempo civil. Diferenças suportam
wrap do contador no horizonte destes prazos. Não acrescentar relação arbitrária
entre os três timeouts.

sendAndFlush não tem parâmetro de timeout nem cria um timer genérico por frame:
no bootstrap, o deadline da consulta é o prazo finito da operação e fecha o socket
para interromper a escrita. Fora desse contexto, o consumidor é responsável
pelo prazo/fecho; shutdown continua a terminar todas as confirmações admitidas.
Um mero future.orTimeout sem fechar a ligação não cumpre o oráculo de recursos.

Se fila TCP saturar, fechar somente aquela conexão com OUTPUT_OVERFLOW. Se a
fila serial do nó saturar, registar erro operacional explícito e solicitar paragem
do serviço; não usar CallerRunsPolicy nem fila alternativa ilimitada. Callbacks
nunca esperam por espaço. Uma flag atómica de paragem é sinal de lifecycle, não
mutação do mapa: após sinalizá-la, tentar publicar cleanup; se a fila está cheia,
o worker já tem trabalho e verifica a flag no início/fim de cada evento, entrando
em cleanup serial sem precisar de mais um slot. Eventos descartados deixam de
ser operações novas. Só o owner limpa contextos/tickets. Timeout/erro de um peer
normal não ativa este caminho de saturação global e não mata o listener.

Core, bridge e recursos de eventos/timer são preparados antes do bind. Se uma
falha pedir paragem durante o construtor do Transport (que já inicia accept),
a flag fica registada; assim que a referência do transporte é publicada,
o construtor fecha-o e desfaz recursos em vez de devolver serviço meio inicializado.
Não bloquear connectionOpen à espera de o construtor terminar.

close externo do bootstrap: fixar um único deadline de shutdown, solicitar paragem
serial, chamar Transport.requestClose para iniciar fecho de listener/conexões,
desligar scheduler, e aguardar transporte, core
e scheduler apenas com o tempo restante. Usar System.nanoTime para o budget real
de joins/awaits; o relógio falso rege os deadlines semânticos do Core, não congela
esperas de threads reais. Converter o restante por defeito para ms aceites por
Transport.awaitTermination; se já não resta 1 ms, reportar timeout, sem arredondar
para dar mais um intervalo. Nunca somar três deadlines independentes.
Se houver erro/timeout de uma espera, ainda solicitar fecho de todos os outros
recursos e reportar falha; não fingir terminação. Chamado por callback/owner,
só iniciar a paragem; um observador externo verifica terminação com prazo.
O Core identifica a sua thread owner, e as entradas de callback do bridge,
timer e whenComplete são marcadas num ThreadLocal de callback com finally de
limpeza; este sinal só decide não aguardar na própria callback, não possui o
mapa de consultas. requestClose permite iniciar o fecho sem joins nesses caminhos.

## 7. Escrita e tempo controláveis dentro dos ficheiros autorizados

Em Connection.java, tipos aninhados de acesso de pacote:

```java
interface WriteDriver {
    default void dequeued() throws IOException { } // ponto de controlo de corrida
    void write(byte[] frame) throws IOException;
    void flush() throws IOException;
}
interface WriteDriverFactory {
    WriteDriver open(Socket socket) throws IOException;
}
```

O overload interno de Connection recebe a factory; Transport tem um overload
de pacote correspondente para TransportTest. Construtores públicos existentes
delegam no driver normal, que obtém OutputStream no escritor e delega write/flush.
dequeued é no-op em produção e ocorre depois de take e antes de publicar inFlight,
fora dos locks; permite provar a janela que uma simples fila vazia esconderia.
Se open/dequeued/write/flush falhar, seguir o mesmo caminho de erro/finally.

TransportTest, no mesmo pacote, usa drivers aninhados com latches: bloquear antes
de escrever ou no flush, lançar IOException, observar a ordem dos frames e
controlar a janela take/fecho. Esperas do fake têm prazo de teste e respondem à
interrupção; finally liberta latches e fecha transportes, sem worker fictício preso.
Não alterar MessageCodec, usar reflexão para contornar privacidade ou depender
de saturar o buffer do kernel para provar o instante do flush.

BootstrapServerTest, no seu pacote, injeta Channel falso com future pendente,
ManualClock, ManualDeadlines e ManualSerialEvents, todos aninhados no ficheiro.
Avançar clock e drenar eventos explicitamente; inspecionar pedidos/fechos/tickets
por contadores limitados. O fake de escrita do Channel permite concluir sucesso,
erro ou cancelamento numa ordem controlada. Não precisa de construir/subclassificar
Connection ou Transport final de outro pacote, nem de tornar essas classes extensíveis.
O driver real de saída é testado em TransportTest; o deadline/estado em
BootstrapServerTest; testes TCP reais no bootstrap ligam as duas provas.

## 8. Oráculos M01–M18 e futura introdução de testes

Todos os testes abaixo são planeados. BootstrapServerTest abrevia-se BS e
TransportTest, TT; ambos são ficheiros autorizados, não suítes já executadas.

| Model | Teste proposto / injeção | Observação independente exigida |
|---|---|---|
| M01 | BS rejectsInvalidConfigurationBeforeOpeningTransport; factory com contador | Papel/grafo/portas/opções inválidos: zero aberturas e zero timers; incluir records construídos diretamente e assimetria |
| M02 | BS returnsExactlyConfiguredNeighbors; Core + grafo local | S1→S2/R1, S2→S1/R1, R1→S1/S2/C1, C1→R1; comparar endpoints/papéis/contagem e hash do grafo antes/depois |
| M03 | BS answersWhileAllNeighborsAreOffline; TCP real | Só bootstrap e peer de teste abertos; nenhum listener/dial/sonda nos endpoints dos vizinhos; mesma lista |
| M04 | BS repeatedConcurrentAndDifferentInstancesReturnSameGraph; fake e TCP | Muitos requestId/conexões/instâncias do mesmo ID; resultados iguais, sem DUPLICATE_ID; contextos/tickets regressam a zero |
| M05 | BS rejectsInconsistentIdentityAndInstance; parâmetros | IDs diferentes, ID desconhecido e instâncias diferentes/zero; erros esperados; dois IDs no mesmo IP continuam distinguíveis |
| M06 | BS comparesNormalizedHostsAndConfiguredPortsAndRole; parâmetros e loader offline | Aceitar equivalências lexicais documentadas; rejeitar host distinto, alias físico apenas, porta/papel distintos; sem DNS e consulta válida posterior funciona |
| M07 | BS rejectsAllOtherActiveMessageTypes; mensagens válidas de fixtures/construtores | Para os outros 15 tipos ativos, INVALID_MESSAGE/correlação sem efeitos; código 3/truncatura entram pelo codec e não inventam ERROR |
| M08 | BS preservesCorrelationAndZeroNeighborInstances | requestId e emissor corretos; todos os instanceId da lista zero; serializar/ler pelo codec sem lease, preservando fixtures independentes |
| M09 | BS writesCompleteSuccessAndErrorBeforeEof; socket real + codec e EOF | REGISTER_OK ou ERROR completo, seguido de EOF; teste de duas mensagens válidas concatenadas observa uma só resposta |
| M10 | BS queryHistoryDoesNotChangeTopologyOrScheduleRenewal; relógio manual | Depois do fecho, avançar muito o relógio: nenhum envio/renovação, contexto/ticket zero e grafo intacto |
| M11 | TT mixedSendAndConfirmedFramesKeepAdmissionOrder; driver controlado | Intercalar os métodos com barreiras e lanes concorrentes; frames íntegros, ordem por admissão/lane e SendResult anterior preservado |
| M12 | TT confirmationWaitsForWriteAndFlushAndReportsFailure; latches | write regressou mas flush bloqueado: future pendente; só depois de flush, sucesso. Invalid_message/closed/open/write/flush IOException terminam corretamente |
| M13 | BS totalDeadlineIsNotExtended; clock/scheduler/channel falsos + TCP parcial | Antes/no prazo, pedido tardio, bytes parciais e escrita pendente: canal fecha, future termina e outra consulta progride. Atraso na fila não renova t0 |
| M14 | TT overflowFailsQueuedAndInFlightConfirmations; driver bloqueado/capacidade 1 | Frame em escrita + um em fila; terceiro enche/fecha só essa conexão, todos os futures admitidos terminam e outro canal funciona |
| M15 | TT closeDuringDequeueAndCancellationAreTerminal; hook dequeued; BS staleEventsDoNotCloseAnotherQuery | Forçar janela take/close; cancel antes/durante I/O; nenhuma troca de erro/cancel em sucesso; Written/Expired/Closed antigos não atingem consulta nova do mesmo nodeId |
| M16 | TT shutdownTerminatesPendingConfirmationsAndAllowsContinuationClose; BS shutsDownCoreTimersAndTransport | Futures fila/retirado/em escrita terminais; continuation pode fechar transporte sem lock/join circular; awaitTermination real, porta reaberta, contagens/timers zero; callback preso tem timeout explícito |
| M17 | BS malformedEarlyCloseAndQueueSaturationAreIsolated; socket/fakes limitados | Prefixo/corpo/truncatura/EOF precoce só afetam peer; listener responde ao seguinte. Saturação distinta da fila serial causa erro/paragem observável e cleanup, sem fila ilimitada |
| M18 | BS controlOverridesReachConsumers; TT + regressão completa/JDK21 | Ver Options e deadline efetivos com overrides, relógio avança no valor configurado; 18 TT antigos preservados; clean verify com contagens/log/XML novos e 45 fixtures intactas |

Fixtures/expected bytes são entradas independentes; não gerar o esperado com
o encoder em teste. TCP real verifica framing/EOF/libertação; o fake verifica
o instante controlado de escrita/flush/timeout. Um frame pequeno em loopback
não comprova peer lento ou ausência de corrida.

Na etapa Implement, primeiro introduzir APIs/casos novos mínimos e registar
falhas reais do comportamento em falta, preservando os testes anteriores.
Depois executar incrementais autorizados por parte, com logs próprios. Test
consolida clean verify/JDK21, regressão TCP real, contagens Surefire atuais,
manual de consulta/erro/prazo/shutdown e comandos para repetição humana. Não
inventar agora falhas/contagens novas ou resultado de teste manual futuro.

## 9. Verificação e checkpoint

Mudanças internas futuras estritamente justificadas: entrada da fila e
acompanhamento de confirmação em Connection; bloco synchronized de close
reduzido às decisões; snapshot/fecho fora de lifecycle em Transport; overloads
de escrita para teste. Estas são exigidas por ADR-015/recursos e preservam
as APIs anteriores, limites e testes. Se Implement revelar necessidade de
alterar outro ficheiro de aplicação, parar e apresentar a lacuna antes de intervir.

Nenhuma divergência arquitetural material identificada neste desenho;
Q-C-04 aplica as aprovações de Model, e normalização lexical já estava no contrato.
Revisão formal das ADR agora autorizada e [preparada para aprovação](adr-review.md);
sem ADR nova ou revisão da base declarada aprovada por este documento.

V-006, regressão e manual deste incremento permanecem NÃO EXECUTADOS.
Os 824 testes referidos nos checkpoints anteriores pertencem ao IMP-002.
Não há gate externo adicional; critérios, revisão final atribuída a **Waheed**
e integração são exigidos antes de DONE. Sem contacto/aceitação presumidos,
commits/push, fecho ou outro IMP. Checkpoint Design aprovado; parar agora na
Test antes do fecho.
