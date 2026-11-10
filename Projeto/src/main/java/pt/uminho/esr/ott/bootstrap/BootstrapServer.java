package pt.uminho.esr.ott.bootstrap;

import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.config.TopologyConfiguration;
import pt.uminho.esr.ott.overlay.NodeRole;
import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.protocol.ErrorCode;
import pt.uminho.esr.ott.protocol.MessageType;
import pt.uminho.esr.ott.transport.Connection;
import pt.uminho.esr.ott.transport.Transport;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/** Initial configured-neighbor lookup only: no membership, leases, liveness or routing. */
public final class BootstrapServer implements AutoCloseable 
{
    //* Atributos, Interfaces e Records

    private static final UUID ZERO = new UUID(0, 0);
    private static final int EVENTS_CAPACITY = 4096;
    private static final ThreadLocal<Boolean> CALLBACK = ThreadLocal.withInitial(() -> false);
    private final AtomicReference<Transport> transport = new AtomicReference<>();
    private final Core core;
    private final SerialEvents events;
    private final Deadlines deadlines;
    private final Duration shutdownTimeout;


    interface Channel 
    {
        UUID id();
        boolean isOpen();
        CompletableFuture<Void> sendAndFlush(ControlMessage message);
        void close();
    }

    interface Ticket 
    { 
        void cancel(); 
    }

    interface Deadlines 
    {
        Ticket schedule(Runnable task, long delayNanos);
        void close();
        boolean await(Duration timeout) throws InterruptedException;
    }

    interface SerialEvents 
    {
        void execute(Runnable task);
        boolean isOwnerThread();
        void close();
        boolean await(Duration timeout) throws InterruptedException;
    }

    interface TransportFactory 
    {
        Transport open(InetSocketAddress bind, Transport.Listener listener, Transport.Options options) throws IOException;
    }


    private record Plan(NodeConfiguration config, Map<String, TopologyConfiguration.Node> nodes,Transport.Options options) {}

    private record EndpointKey(String host, int port, boolean udp) {}

    private record TcpChannel(Connection connection) implements Channel 
    {
        @Override public UUID id() { return connection.id(); }
        @Override public boolean isOpen() { return connection.isOpen(); }
        @Override public CompletableFuture<Void> sendAndFlush(ControlMessage message) { return connection.sendAndFlush(message); }
        @Override public void close() { connection.close(); }
    }



    //* Construtores e Métodos Auxiliares de Inicialização

    public BootstrapServer(NodeConfiguration configuration) throws IOException 
    {
        this(validate(configuration), null, System::nanoTime, new RealDeadlines(), new EventLoop(), Transport::new);
    }

    BootstrapServer(NodeConfiguration configuration, InetSocketAddress bind) throws IOException 
    {
        this(validate(configuration), bind, System::nanoTime, new RealDeadlines(), new EventLoop(), Transport::new);
    }

    BootstrapServer(NodeConfiguration configuration, InetSocketAddress bind, LongSupplier clock,
                    Deadlines deadlines, SerialEvents events, TransportFactory factory) throws IOException 
    {
        this(validate(configuration), bind, clock, deadlines, events, factory);
    }


    private BootstrapServer(Plan plan, InetSocketAddress bind, LongSupplier clock,
                            Deadlines deadlines, SerialEvents events, TransportFactory factory) throws IOException 
    {
        Objects.requireNonNull(clock); 
        Objects.requireNonNull(factory);
        this.deadlines = Objects.requireNonNull(deadlines);
        this.events = Objects.requireNonNull(events);

        shutdownTimeout = plan.config.runtime().control().shutdownTimeout();
        core = new Core(plan, nonzeroInstance(), clock, deadlines, events);
        core.onStop = () -> { var tcp = transport.get(); if (tcp != null) tcp.requestClose(); };

        try{
            if(bind == null) 
                bind = new InetSocketAddress(InetAddress.getByName(plan.config.bindHost()), plan.config.controlPort());
            if(bind.isUnresolved() || (bind.getPort() != 0 && bind.getPort() != plan.config.controlPort()))
                throw new IllegalArgumentException("Resolved configured bind required (zero only for tests)");

            var listener = new Transport.Listener() 
            {
                @Override public void connectionOpen(Connection c) { callback(() -> core.open(new TcpChannel(c))); }
                @Override public void message(Connection c, ControlMessage m) { callback(() -> core.message(new TcpChannel(c), m)); }
                @Override public void connectionClosed(Connection c, Connection.CloseReason reason) { callback(() -> core.closed(new TcpChannel(c))); }
            };

            var tcp = factory.open(bind, listener, plan.options);
            transport.set(tcp);
            if(core.stopping.get()) 
            {
                tcp.requestClose(); 
                throw new IOException("Bootstrap stopped during startup");
            }

        }catch (IOException | RuntimeException e){
            core.requestStop(); deadlines.close();
            try 
            { 
                awaitStopped(shutdownTimeout); 
            }catch (RuntimeException cleanup) { 
                e.addSuppressed(cleanup); 
            }

            throw e;
        }
    }


    private static UUID nonzeroInstance() 
    {
        UUID value;
        do { value = UUID.randomUUID(); } while (ZERO.equals(value));
        return value;
    }


    private static void callback(Runnable action) 
    {
        boolean previous = CALLBACK.get(); 
        CALLBACK.set(true);
        try 
        { 
            action.run();

        }finally{ 
            if (previous) 
                CALLBACK.set(true); 
            else 
                CALLBACK.remove(); 
        }
    }


    public InetSocketAddress localAddress() 
    {
        return transport.get().localAddress(); 
    }


    @Override public void close() 
    {
        long started = System.nanoTime();
        core.requestStop();

        var tcp = transport.get(); 
        if (tcp != null) 
            tcp.requestClose();
        deadlines.close();

        if (CALLBACK.get() || events.isOwnerThread()) 
            return;

        awaitStopped(Duration.ofNanos(Math.max(0, shutdownTimeout.toNanos() - (System.nanoTime() - started))));
        if (core.failure != null) 
            throw new IllegalStateException("Bootstrap control processing failed", core.failure);
    }


    private void awaitStopped(Duration budget) 
    {
        long started = System.nanoTime();
        try{
            var tcp = transport.get();
            if(tcp != null && !tcp.awaitTermination(remaining(budget, started)))
                throw new IllegalStateException("Bootstrap TCP workers did not terminate");
            if(!events.await(remaining(budget, started)) || !deadlines.await(remaining(budget, started)))
                throw new IllegalStateException("Bootstrap control workers did not terminate");

        }catch (InterruptedException e){
            Thread.currentThread().interrupt(); throw new IllegalStateException("Bootstrap shutdown interrupted", e);
        }
    }


    private static Duration remaining(Duration budget, long started) 
    {
        long millis = TimeUnit.NANOSECONDS.toMillis(budget.toNanos() - (System.nanoTime() - started));
        if(millis < 1) 
            throw new IllegalStateException("Bootstrap shutdown deadline exceeded");
        return Duration.ofMillis(millis);
    }


    static String normalizeHost(String host) 
    { 
        return normalizeHost(host, false); 
    }


    private static String normalizeHost(String host, boolean allowWildcard) 
    {
        try {
            if(host == null || host.isEmpty() || host.length() > 253 || !host.chars().allMatch(c -> c > 32 && c < 127))
                throw new IllegalArgumentException();
            new URI("ott", null, host, -1, null, null, null).parseServerAuthority();

            String result = host.toLowerCase(Locale.ROOT);
            if(result.startsWith("[") && result.endsWith("]")) 
                result = result.substring(1, result.length() - 1);
            if(result.endsWith(".")) 
                result = result.substring(0, result.length() - 1);

            if(result.matches("[0-9.]+") && result.contains(".")) 
            {
                String[] parts = result.split("\\.", -1);
                if(parts.length != 4) 
                    throw new IllegalArgumentException();

                for(int i = 0; i < parts.length; i++) 
                {
                    int n = Integer.parseInt(parts[i]);
                    if(n > 255) 
                        throw new IllegalArgumentException();

                    parts[i] = Integer.toString(n);
                }
                result = String.join(".", parts);
            }

            String address = result.split("%", 2)[0];
            boolean wildcard = address.equals("0.0.0.0") || (address.contains(":") && address.replace(":", "").matches("0*"));

            if(wildcard && !allowWildcard) 
                throw new IllegalArgumentException();
            return result;
        }catch (URISyntaxException | IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid configured or advertised host", e);
        }
    }

    private static void require(boolean condition, String detail) 
    {
        if(!condition) 
            throw new IllegalArgumentException(detail);
    }

    private static void nodeId(String id) 
    { 
        require(id != null && id.matches("[A-Za-z0-9_-]{1,32}"), "Invalid node ID"); 
    }

    private static void port(int port) 
    { 
        require(port >= 1 && port <= 65535, "Invalid endpoint port"); 
    }

    private static Plan validate(NodeConfiguration config) 
    {
        require(config != null && config.role() == NodeRole.BOOTSTRAP, "BOOTSTRAP configuration required");
        nodeId(config.nodeId()); 
        port(config.controlPort()); 

        normalizeHost(config.bindHost(), true);
        String advertised = normalizeHost(config.advertisedHost());

        require(config.dataPort() == 0 && config.bootstrap().isEmpty() && config.streamsFile().isEmpty()
                && config.mediaBase().isEmpty() && config.streams().isEmpty(), "Incompatible bootstrap profile");

        require(config.topology().isPresent() && config.topologyFile().isPresent(), "Topology required");

        var original = config.topology().orElseThrow().nodes();
        require(!original.isEmpty() && original.size() <= 64 && !original.containsKey(config.nodeId()), "Invalid topology size or bootstrap ID");
        
        var nodes = new LinkedHashMap<String, TopologyConfiguration.Node>();

        var endpoints = new HashSet<EndpointKey>(); endpoints.add(new EndpointKey(advertised, config.controlPort(), false));

        original.forEach((id, n) -> 
        {
            nodeId(id); 
            require(n != null && id.equals(n.nodeId()), "Topology ID mismatch");
            require(n.role() != null && n.role() != NodeRole.BOOTSTRAP, "Invalid overlay role");

            port(n.controlPort()); port(n.dataPort()); String host = normalizeHost(n.host());
            require(endpoints.add(new EndpointKey(host, n.controlPort(), false))
                    && endpoints.add(new EndpointKey(host, n.dataPort(), true)), "Endpoint collision");

            require(!n.neighbors().isEmpty() && n.neighbors().size() <= 63, "Invalid degree");

            for(String neighbor : n.neighbors())
            {
                nodeId(neighbor);
            }

            nodes.put(id, new TopologyConfiguration.Node(id, host, n.controlPort(), n.dataPort(), n.role(), n.neighbors()));
        });

        for(var n : nodes.values())
        {
            for (String id : n.neighbors())
            {
                require(!id.equals(n.nodeId()) && nodes.containsKey(id) && nodes.get(id).neighbors()
                                                                .contains(n.nodeId()), "Invalid or asymmetric edge");
            }
        }
        
        var c = config.runtime().control();
        return new Plan(config, Collections.unmodifiableMap(nodes),
                new Transport.Options(c.queueCapacity(), c.maxConnections(), c.connectTimeout(), c.shutdownTimeout()));
    }

    //* Validação e Ciclo de Vida

    static final class Core 
    {
        //* Atributos e Subclasses

        private final Plan plan;
        private final UUID instance;
        private final LongSupplier clock;
        private final Deadlines deadlines;
        private final SerialEvents events;

        final AtomicBoolean stopping = new AtomicBoolean();
        volatile RuntimeException failure;
        volatile Runnable onStop = () -> { };

        private final Map<UUID, Context> contexts = new LinkedHashMap<>();
        private boolean cleaned;


        /** Estado por ligação: canal, instante de chegada, ticket de prazo e flag de resposta. */
        private static final class Context 
        {
            final Channel channel;
            final long started;
            Ticket ticket;
            boolean responding;

            Context(Channel channel, long started) 
            { 
                this.channel = channel; 
                this.started = started; 
            }
        }


        //* Construtores

        Core(NodeConfiguration config, UUID instance, LongSupplier clock, Deadlines deadlines, SerialEvents events) 
        {
            this(validate(config), instance, clock, deadlines, events);
        }

        private Core(Plan plan, UUID instance, LongSupplier clock, Deadlines deadlines, SerialEvents events) 
        {
            this.plan = plan;
            this.instance = Objects.requireNonNull(instance);
            require(!ZERO.equals(instance), "Bootstrap execution instance required");

            this.clock = Objects.requireNonNull(clock);
            this.deadlines = Objects.requireNonNull(deadlines);
            this.events = Objects.requireNonNull(events);
        }


        //* Prazos (Deadlines)

        /** Orçamento temporal máximo que uma ligação pode ficar aberta à espera de REGISTER. */
        private long budget() 
        { 
            return plan.config.runtime().control().bootstrapQueryTimeout().toNanos(); 
        }

        /** Verifica se o contexto já consumiu todo o seu orçamento temporal. */
        private boolean expired(Context context) 
        { 
            return clock.getAsLong() - context.started >= budget(); 
        }


        //* Processamento de Eventos

        /** Registo de uma nova ligação; agenda o fecho automático se não chegar REGISTER a tempo. */
        void open(Channel channel) 
        {
            long started = clock.getAsLong();

            if(!post(() -> 
            {
                var context = new Context(channel, started);

                // Rejeita ligações inválidas, fora de prazo, acima do limite ou duplicadas.
                if(!channel.isOpen() || expired(context) 
                        || contexts.size() >= plan.options.maxConnections() 
                        || contexts.containsKey(channel.id())) 
                { 
                    channel.close(); 
                    return; 
                }

                contexts.put(channel.id(), context);

                long remaining = budget() - (clock.getAsLong() - started);
                if(remaining <= 0) 
                { 
                    finish(context); 
                    return; 
                }

                // Agenda o prazo: fecha a ligação se expirar sem resposta.
                context.ticket = deadlines.schedule(() -> post(() -> 
                {
                    if(contexts.get(channel.id()) == context && expired(context)) 
                        finish(context);
                }), remaining);

            })) channel.close();
        }

        /** Processa uma mensagem recebida; só REGISTER é válido e gera REGISTER_OK com a lista de vizinhos. */
        void message(Channel channel, ControlMessage message) 
        {
            post(() -> 
            {
                var context = contexts.get(channel.id());
                if(context == null) 
                    return;

                if(expired(context) || !channel.isOpen()) 
                { 
                    finish(context); 
                    return; 
                }

                // Ignora mensagens adicionais enquanto a resposta anterior ainda não terminou.
                if(context.responding) 
                    return;

                ControlMessage.Payload response;
                MessageType type;

                // Valida o tipo da mensagem.
                if(message.type() != MessageType.REGISTER) 
                {
                    response = error(ErrorCode.INVALID_MESSAGE, message, "Only REGISTER is accepted"); 
                    type = MessageType.ERROR;
                }
                // Valida a coerência da identidade do remetente com o endpoint anunciado.
                else if(!(message.payload() instanceof ControlMessage.EndpointPayload payload)
                        || payload.endpoint() == null
                        || !Objects.equals(message.senderId(), payload.endpoint().nodeId())
                        || payload.endpoint().instanceId() == null || ZERO.equals(payload.endpoint().instanceId())
                        || !Objects.equals(message.senderInstance(), payload.endpoint().instanceId())) 
                {
                    response = error(ErrorCode.INVALID_MESSAGE, message, "Inconsistent sender identity"); 
                    type = MessageType.ERROR;
                }
                // Valida se o nó e o endpoint pertencem à topologia configurada.
                else if(!authorized(message.senderId(), payload.endpoint())) 
                {
                    response = error(ErrorCode.UNAUTHORIZED_NEIGHBOR, message, "Node or endpoint is not configured"); 
                    type = MessageType.ERROR;
                }
                // Pedido válido: devolve a lista de vizinhos configurada para o remetente.
                else 
                {
                    var neighbors = new ArrayList<ControlMessage.Endpoint>();

                    for(String id : plan.nodes.get(message.senderId()).neighbors()) 
                    {
                        var node = plan.nodes.get(id);
                        neighbors.add(new ControlMessage.Endpoint(id, ZERO, node.host(), node.controlPort(), node.dataPort(), node.role()));
                    }

                    response = new ControlMessage.NeighborList(neighbors); 
                    type = MessageType.REGISTER_OK;
                }

                // Envia a resposta e fecha o contexto quando o envio termina.
                context.responding = true;
                var reply = new ControlMessage(type, message.requestId(), plan.config.nodeId(), instance, response);

                channel.sendAndFlush(reply).whenComplete((ignored, error) -> callback(() -> post(() -> 
                {
                    if(contexts.get(channel.id()) == context) 
                        finish(context);
                })));
            });
        }

        /** Fecho de ligação reportado pelo transporte. */
        void closed(Channel channel) 
        {
            post(() -> 
            { 
                var context = contexts.get(channel.id()); 
                if(context != null) 
                    finish(context); 
            });
        }


        //* Autorização

        /** Confirma que o remetente e o endpoint anunciado correspondem exatamente à topologia configurada. */
        private boolean authorized(String sender, ControlMessage.Endpoint endpoint) 
        {
            var node = plan.nodes.get(sender);

            if(node == null || node.role() != endpoint.role() 
                    || node.controlPort() != endpoint.controlPort() 
                    || node.dataPort() != endpoint.dataPort()) 
                return false;

            try 
            { 
                return node.host().equals(normalizeHost(endpoint.host())); 
            }
            catch (IllegalArgumentException e) 
            { 
                return false; 
            }
        }


        //* Respostas de Erro

        /** Constrói a payload de erro que responde a um pedido inválido. */
        private static ControlMessage.ErrorPayload error(ErrorCode code, ControlMessage request, String detail) 
        {
            return new ControlMessage.ErrorPayload(code, request.type().code(), detail);
        }


        //* Limpeza e Encerramento

        /** Remove o contexto do mapa, cancela o prazo pendente e fecha o canal. */
        private void finish(Context context) 
        {
            if(!contexts.remove(context.channel.id(), context)) 
                return;

            if(context.ticket != null) 
                context.ticket.cancel();

            context.channel.close();
        }

        /** Submete um evento no loop serial; devolve false (e fecha o canal) se o servidor já estiver a parar. */
        private boolean post(Runnable event) 
        {
            if(stopping.get()) 
                return false;

            try 
            {
                events.execute(() -> 
                {
                    if(stopping.get()) 
                    { 
                        cleanup(); 
                        return; 
                    }

                    try 
                    { 
                        event.run(); 
                    }
                    catch (RuntimeException e) 
                    { 
                        fail(e); 
                    }
                    finally 
                    { 
                        if(stopping.get()) 
                            cleanup(); 
                    }
                });

                return true;

            }catch (RejectedExecutionException e) 
            {
                fail(e); 
                return false;
            }
        }

        /** Registra a falha crítica e inicia o encerramento do serviço. */
        private void fail(RuntimeException cause) 
        {
            failure = cause;
            System.err.println("Bootstrap control processing failed; stopping bounded service");
            requestStop();
        }

        /** Marca o servidor como parando e dispara o onStop; idempotente. */
        void requestStop() 
        {
            if(!stopping.compareAndSet(false, true)) 
                return;

            onStop.run();

            try 
            { 
                events.execute(this::cleanup); 
            }
            catch (RejectedExecutionException ignored) 
            {
                // Fila cheia: já existe um evento que observa stopping e limpa.
                // Loop fechado: a limpeza já foi executada.
            }
        }

        /** Fecha todos os contextos ativos e encerra os executores; executa uma única vez. */
        private void cleanup() 
        {
            if(cleaned) 
                return;

            cleaned = true;

            for(var context : new ArrayList<>(contexts.values())) 
                finish(context);

            deadlines.close(); 
            events.close();
        }


        //* Observabilidade (Testes)

        int contextCount() 
        { 
            return contexts.size(); 
        }
    }

    private static final class EventLoop implements SerialEvents {
        private volatile Thread owner;
        private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(EVENTS_CAPACITY), r -> Thread.ofVirtual().name("ott-bootstrap-events").unstarted(() -> {
                    owner = Thread.currentThread(); r.run();
                }), new ThreadPoolExecutor.AbortPolicy());
        @Override public void execute(Runnable task) { executor.execute(task); }
        @Override public boolean isOwnerThread() { return owner == Thread.currentThread(); }
        @Override public void close() { executor.shutdownNow(); }
        @Override public boolean await(Duration timeout) throws InterruptedException { return executor.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS); }
    }
    private static final class RealDeadlines implements Deadlines {
        private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                Thread.ofVirtual().name("ott-bootstrap-deadline").factory());
        RealDeadlines() { executor.setRemoveOnCancelPolicy(true); executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false); }
        @Override public Ticket schedule(Runnable task, long delayNanos) {
            var future = executor.schedule(() -> callback(task), delayNanos, TimeUnit.NANOSECONDS);
            return () -> future.cancel(false);
        }
        @Override public void close() { executor.shutdownNow(); }
        @Override public boolean await(Duration timeout) throws InterruptedException { return executor.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS); }
    }
}
