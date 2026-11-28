package pt.uminho.esr.ott.app;

import pt.uminho.esr.ott.bootstrap.BootstrapServer;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.routing.*;
import pt.uminho.esr.ott.server.StreamingServer;

import java.io.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.LongSupplier;

/** Validated CLI composition. Blocking console work never runs on the node owner. */
public final class OttApplication
{

    //* === Atributos e Variáveis === *

    public static final int MAX_LINE_CHARS = 4096;
    public static final int MAX_LOG_CHARS = 4096;



    //* === Invocation Record === *

    public record Invocation(boolean help, Optional<NodeRole> role, Optional<Path> config)
    {

        //* === Construtor === *

        public Invocation
        {
            Objects.requireNonNull(role);
            Objects.requireNonNull(config);
        }
    }



    //* === Interfaces === *

    interface ServerNodeFactory
    {
        RunningService open(NodeConfiguration configuration, UUID instance,
                StreamingServer.PreparedSources prepared, ConsoleAccess console) throws IOException;
    }

    interface ConsoleAccess
    {
        void log(String text, boolean error);
        void stop(boolean fatal);
        Duration left();
        AtomicBoolean prepareCancelled();
    }

    interface ServiceFactory
    {
        RunningService open(NodeConfiguration configuration) throws Exception;
    }

    interface RunningService extends AutoCloseable
    {
        Map<String, Neighbor> neighbors();

        default pt.uminho.esr.ott.routing.RoutingService.Snapshot routingSnapshot()
        {
            return pt.uminho.esr.ott.routing.RoutingService.emptySnapshot();
        }

        default Optional<pt.uminho.esr.ott.monitoring.NetworkMonitor.Snapshot> monitoringSnapshot()
        {
            return Optional.empty();
        }

        default pt.uminho.esr.ott.routing.RoutingService.TraceTicket requestTrace(UUID streamId)
        {
            Objects.requireNonNull(streamId);
            return RoutingService.unavailableTrace(streamId);
        }

        default Optional<OverlayRecovery.RecoveryStatus> recoveryStatus()
        {
            return Optional.empty();
        }

        void requestShutdown(int reason, Duration remainingBudget);
        boolean awaitTermination(Duration remainingBudget);

        default Optional<Throwable> failure()
        {
            return Optional.empty();
        }

        @Override
        void close();
    }



    //* === Construtor === *

    private OttApplication()
    {
    }



    //* === Parse e Run === *

    public static Invocation parse(String[] arguments)
    {
        Objects.requireNonNull(arguments);

        if(arguments.length == 1 && "--help".equals(arguments[0]))
            return new Invocation(true, Optional.empty(), Optional.empty());

        NodeRole role = null;
        Path config = null;

        for(int i = 0; i < arguments.length; i++)
        {
            String option = arguments[i];

            if("--role".equals(option) && role == null && i + 1 < arguments.length)
            {
                try
                {
                    role = NodeRole.valueOf(arguments[++i]);
                }
                catch(RuntimeException e)
                {
                    throw new IllegalArgumentException("Invalid --role", e);
                }
            }
            else if("--config".equals(option) && config == null && i + 1 < arguments.length)
            {
                String value = arguments[++i];

                if(value == null || value.isBlank() || value.startsWith("--"))
                    throw new IllegalArgumentException("Invalid --config");

                config = Path.of(value);
            }
            else
                throw new IllegalArgumentException("Unknown, repeated or incomplete option: " + option);
        }

        if(role == null || config == null)
            throw new IllegalArgumentException("--role and --config required");

        return new Invocation(false, Optional.of(role), Optional.of(config));
    }

    public static int run(String[] arguments, InputStream input, PrintStream output, PrintStream error)
    {
        return execute(arguments, input, output, error, null, System::nanoTime, () ->
        {
        }, () ->
        {
        }).exitCode();
    }

    static int run(String[] arguments, InputStream input, PrintStream output, PrintStream error,
                   ServiceFactory services, LongSupplier clock)
    {
        return execute(arguments, input, output, error, Objects.requireNonNull(services), clock, () ->
        {
        }, () ->
        {
        }).exitCode();
    }

    /** Own interruptible descriptor channels rather than closing a locked PrintStream wrapper. */
    public static void main(String[] arguments) throws IOException
    {
        var in = new FileInputStream(FileDescriptor.in).getChannel();
        var out = new FileOutputStream(FileDescriptor.out).getChannel();
        var err = new FileOutputStream(FileDescriptor.err).getChannel();

        var result = execute(arguments, Channels.newInputStream(in),
                new PrintStream(Channels.newOutputStream(out), true, StandardCharsets.UTF_8),
                new PrintStream(Channels.newOutputStream(err), true, StandardCharsets.UTF_8), null,
                System::nanoTime, () -> closeChannel(in), () ->
                {
                    closeChannel(out);
                    closeChannel(err);
                });

        System.exit(result.exitCode());
    }

    private static void closeChannel(Channel channel)
    {
        try
        {
            channel.close();
        }
        catch(IOException ignored)
        {
            // Closing is followed by physical joins.
        }
    }



    //* === Event Records === *

    private record InputEvent(String line, boolean eof)
    {
    }

    private record OutputEvent(String text, boolean error)
    {
    }

    record ExecutionResult(int exitCode, boolean inputTerminated, boolean outputTerminated, boolean serviceTerminated)
    {
    }



    //* === Execute === *

    static ExecutionResult execute(String[] arguments, InputStream input, PrintStream output, PrintStream error,
                               ServiceFactory factory, LongSupplier clock, Runnable closeInput, Runnable closeOutput)
    {
        return execute(arguments, input, output, error, factory, null, null, clock, closeInput, closeOutput);
    }

    static ExecutionResult execute(String[] arguments, InputStream input, PrintStream output, PrintStream error,
                               ServiceFactory factory, StreamingServer.SourceOpener sourceOpener,
                               ServerNodeFactory serverNodeFactory, LongSupplier clock,
                               Runnable closeInput, Runnable closeOutput)
    {
        final Invocation invocation;
        final NodeConfiguration config;

        try
        {
            invocation = parse(arguments);

            if(invocation.help())
            {
                output.println("Usage: --role BOOTSTRAP|SERVER|RELAY|CLIENT --config <file>; commands: neighbors, recovery, routes, catalog, metrics, trace UUID, quit");
                return new ExecutionResult(0, true, true, true);
            }

            config = ConfigurationLoader.load(invocation.config().orElseThrow(), invocation.role().orElseThrow());
        }
        catch(ConfigurationException | IllegalArgumentException e)
        {
            error.println(bounded("CONFIG_ERROR " + e.getMessage()));
            return new ExecutionResult(2, true, true, true);
        }

        var console = new Console(input, output, error, config, clock);

        var hook = new Thread(() ->
        {
            console.stop(false);

            try
            {
                console.finished.await(config.runtime().control().shutdownTimeout().toNanos(), TimeUnit.NANOSECONDS);
            }
            catch(InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }, "ott-shutdown-hook");

        RunningService service = null;
        var diagnostic = new DiagnosticConsole(console, clock);
        boolean interrupted = false;
        boolean inputDone = true;
        boolean outputDone = true;
        boolean serviceDone = true;

        try
        {
            Runtime.getRuntime().addShutdownHook(hook);
            console.writer.start();
            service = factory == null ? open(config, console, sourceOpener, serverNodeFactory, clock) : factory.open(config);
            console.log("STARTED node=" + config.nodeId() + " role=" + config.role() + " pid=" + ProcessHandle.current().pid(), false);
            console.reader.start();

            while(!console.stopped.get())
            {
                diagnostic.poll();

                var failure = service.failure();

                if(failure.isPresent())
                {
                    console.log("FATAL " + failure.orElseThrow(), true);
                    console.stop(true);
                    break;
                }

                var command = console.commands.poll(20, TimeUnit.MILLISECONDS);

                if(command == null)
                    continue;

                if(command.eof())
                {
                    console.stop(false);
                    break;
                }

                String line = command.line().strip();

                if(line.isEmpty())
                    continue;

                if(line.equals("quit"))
                    console.stop(false);
                else if(line.equals("neighbors"))
                {
                    var neighbors = service.neighbors();
                    console.log("NEIGHBORS node=" + config.nodeId() + " role=" + config.role() + " count=" + neighbors.size(), false);

                    neighbors.values().stream().sorted(Comparator.comparing(Neighbor::nodeId)).forEach(n ->
                            console.log("NEIGHBOR node=" + n.nodeId() + " state=" + n.state() + " session=" + n.session(), false));
                }
                else if(line.equals("recovery"))
                {
                    var status = service.recoveryStatus();
                    console.log("RECOVERY node=" + config.nodeId() + " " + status.map(Object::toString).orElse("NOT_APPLICABLE"), false);
                }
                else if(line.equals("trace") || line.startsWith("trace "))
                    diagnostic.trace(line, service);
                else if(line.equals("routes") || line.equals("catalog") || line.equals("metrics"))
                    diagnostic.list(line, service);
                else if(line.equals("play") || line.startsWith("play "))
                    console.log("NOT_IMPLEMENTED " + line.split(" ", 2)[0], false);
                else
                    console.log("COMMAND_ERROR unknown command", true);
            }
        }
        catch(InterruptedException e)
        {
            console.stop(true);
            interrupted = true; // Restore after bounded cleanup.
        }
        catch(Exception e)
        {
            console.log("FATAL " + bounded(e.getMessage()), true);
            console.stop(true);
        }
        finally
        {
            console.stop(false);
            diagnostic.cancel();
            // Close/cancel the owned input before waiting on the network budget.
            console.reader.interrupt();
            closeInput.run();

            if(service != null)
            {
                try
                {
                    service.requestShutdown(console.failed.get() ? 1 : 0, console.left());
                    serviceDone = service.awaitTermination(console.left());

                    if(!serviceDone)
                    {
                        console.failed.set(true);
                        service.close();
                    }

                    if(service.failure().isPresent())
                        console.failed.set(true);
                }
                catch(RuntimeException e)
                {
                    serviceDone = false;
                    console.failed.set(true);
                    console.log("CLEANUP_ERROR " + e.getMessage(), true);

                    try
                    {
                        service.close();
                    }
                    catch(RuntimeException ignored)
                    {
                        // Preserve the first cleanup failure.
                    }
                }
            }

            diagnostic.poll();
            inputDone = join(console.reader, console.left());

            if(!inputDone)
                console.failed.set(true);

            // This record precedes the writer's final drain/join, so it cannot announce a final status.
            // The returned ExecutionResult / OS exit status is authoritative after all cleanup.
            console.log("SHUTDOWN node=" + config.nodeId() + " phase=closing-output", false);
            console.outputStopping.set(true);

            // Leave time for closing a blocked descriptor and joining its writer.
            outputDone = join(console.writer, console.left().dividedBy(2));

            if(!outputDone)
            {
                console.failed.set(true);
                console.writer.interrupt();
                closeOutput.run();
                outputDone = join(console.writer, console.left());

                if(!outputDone)
                    console.failed.set(true);
            }

            try
            {
                Runtime.getRuntime().removeShutdownHook(hook);
            }
            catch(IllegalStateException ignored)
            {
                // JVM hook is already running.
            }

            console.finished.countDown();
        }

        if(interrupted)
            Thread.currentThread().interrupt();

        return new ExecutionResult(console.failed.get() ? 3 : 0, inputDone, outputDone, serviceDone);
    }



    //* === Service Opening === *

    private static RunningService open(NodeConfiguration config, Console console) throws IOException
    {
        return open(config, console, null, null, System::nanoTime);
    }

    private static RunningService open(NodeConfiguration config, Console console,
            StreamingServer.SourceOpener sourceOpener, ServerNodeFactory serverNodeFactory,
            LongSupplier clock) throws IOException
    {
        if(config.role() == NodeRole.BOOTSTRAP)
        {
            var bootstrap = new BootstrapServer(config);
            console.log("READY node=" + config.nodeId() + " role=BOOTSTRAP instance=internal-not-exposed", false);

            return new RunningService()
            {
                private boolean closed;
                private RuntimeException failure;

                public Map<String, Neighbor> neighbors()
                {
                    return Map.of();
                }

                public synchronized void requestShutdown(int reason, Duration budget)
                {
                    if(closed)
                        return;

                    closed = true;

                    try
                    {
                        bootstrap.close();
                    }
                    catch(RuntimeException e)
                    {
                        failure = e;
                    }
                }

                public boolean awaitTermination(Duration budget)
                {
                    if(failure != null)
                        throw failure;

                    return closed;
                }

                public void close()
                {
                    requestShutdown(1, Duration.ZERO);
                }
            };
        }

        UUID instance = UUID.randomUUID();
        if(config.role() == NodeRole.SERVER)
        {
            var plans = StreamingServer.SourcePlan.fromConfiguration(config);
            StreamingServer.SourceOpener opener = sourceOpener != null ? sourceOpener : StreamingServer.SourceHandle::open;
            var prepared = StreamingServer.prepare(plans, opener, console.prepareCancelled(),
                    () -> console.stopped.get() ? console.left().toNanos() : Long.MAX_VALUE, clock);

            if(serverNodeFactory != null)
            {
                try
                {
                    return serverNodeFactory.open(config, instance, prepared, console);
                }
                catch(RuntimeException | Error | IOException e)
                {
                    prepared.close();
                    throw e;
                }
            }

            var fatalRef = new AtomicReference<Throwable>();
            var serverRef = new AtomicReference<StreamingServer>();
            var nodeRef = new AtomicReference<OverlayNode>();
            var server = new StreamingServer(prepared, StreamingServer.FramePort.UNWIRED, cause ->
            {
                fatalRef.compareAndSet(null, cause);
                console.stop(true);
            }, clock);
            serverRef.set(server);

            try
            {
                var node = OverlayNode.withLocalSources(config, instance,
                        neighborObserver(config, console, instance, nodeRef),
                        event -> event.routeSwitch().ifPresent(change ->
                                console.log(switchLog(config.nodeId(), instance, change), false)),
                        server.localSourceBinding());
                nodeRef.set(node);
                node.start().whenComplete((v, error) ->
                {
                    if(error == null)
                        console.log("READY node=" + config.nodeId() + " instance=" + instance, false);
                    else
                    {
                        fatalRef.compareAndSet(null, error);
                        console.stop(true);
                    }
                });

                var sharedBudget = new AtomicReference<StreamingServer.ShutdownBudget>();
                return new RunningService()
                {
                    public Map<String, Neighbor> neighbors()
                    {
                        return node.diagnostics();
                    }

                    public RoutingService.Snapshot routingSnapshot()
                    {
                        return node.routingSnapshot();
                    }

                    public Optional<pt.uminho.esr.ott.monitoring.NetworkMonitor.Snapshot> monitoringSnapshot()
                    {
                        return Optional.of(node.monitoringSnapshot());
                    }

                    public RoutingService.TraceTicket requestTrace(UUID streamId)
                    {
                        return node.requestTrace(streamId);
                    }

                    public Optional<OverlayRecovery.RecoveryStatus> recoveryStatus()
                    {
                        return Optional.of(node.recoveryStatus());
                    }

                    public void requestShutdown(int reason, Duration budget)
                    {
                        var sb = sharedBudget.updateAndGet(existing -> existing != null ? existing
                                : console.stopped.get()
                                        ? console.shutdownBudget()
                                        : StreamingServer.ShutdownBudget.start(clock.getAsLong(), budget));
                        server.requestShutdown(sb);
                        node.requestShutdown(reason, Duration.ofNanos(sb.remainingNanos(clock.getAsLong())));
                    }

                    public boolean awaitTermination(Duration budget)
                    {
                        var sb = sharedBudget.updateAndGet(existing -> existing != null ? existing
                                : console.stopped.get()
                                        ? console.shutdownBudget()
                                        : StreamingServer.ShutdownBudget.start(clock.getAsLong(), budget));
                        Duration remaining = Duration.ofNanos(sb.remainingNanos(clock.getAsLong()));
                        boolean nodeDone = node.awaitTermination(remaining);
                        var serverDone = server.awaitTermination(sb);
                        return nodeDone && serverDone.complete();
                    }

                    public Optional<Throwable> failure()
                    {
                        return Optional.ofNullable(fatalRef.get())
                                .or(node::failure)
                                .or(server::failure);
                    }

                    public void close()
                    {
                        var sb = sharedBudget.updateAndGet(existing -> existing != null ? existing
                                : console.stopped.get()
                                        ? console.shutdownBudget()
                                        : StreamingServer.ShutdownBudget.start(clock.getAsLong(),
                                                config.runtime().control().shutdownTimeout()));
                        server.requestShutdown(sb);
                        long remNanos = sb.remainingNanos(clock.getAsLong());
                        try
                        {
                            if(remNanos > 0L)
                                node.close();
                            else
                            {
                                node.requestShutdown(1, Duration.ZERO);
                                node.awaitTermination(Duration.ZERO);
                            }
                        }
                        finally
                        {
                            server.awaitTermination(sb);
                        }
                    }
                };
            }
            catch(RuntimeException | Error e)
            {
                server.requestShutdown(StreamingServer.ShutdownBudget.start(clock.getAsLong(), Duration.ZERO));
                server.close();
                throw e;
            }
        }

        var reference = new AtomicReference<OverlayNode>();

        var node = OverlayNode.withRoutingObserver(config, instance, neighborObserver(config, console, instance, reference),
                event -> event.routeSwitch().ifPresent(change ->
                        console.log(switchLog(config.nodeId(), instance, change), false)));

        reference.set(node);

        node.start().whenComplete((v, error) ->
        {
            if(error == null)
                console.log("READY node=" + config.nodeId() + " instance=" + instance, false);
        });

        return new RunningService()
        {
            public Map<String, Neighbor> neighbors()
            {
                return node.diagnostics();
            }

            public RoutingService.Snapshot routingSnapshot()
            {
                return node.routingSnapshot();
            }

            public Optional<pt.uminho.esr.ott.monitoring.NetworkMonitor.Snapshot> monitoringSnapshot()
            {
                return Optional.of(node.monitoringSnapshot());
            }

            public RoutingService.TraceTicket requestTrace(UUID streamId)
            {
                return node.requestTrace(streamId);
            }

            public Optional<OverlayRecovery.RecoveryStatus> recoveryStatus()
            {
                return Optional.of(node.recoveryStatus());
            }

            public void requestShutdown(int reason, Duration budget)
            {
                node.requestShutdown(reason, budget);
            }

            public boolean awaitTermination(Duration budget)
            {
                return node.awaitTermination(budget);
            }

            public Optional<Throwable> failure()
            {
                return node.failure();
            }

            public void close()
            {
                node.close();
            }
        };
    }



    private static NeighborManager.Observer neighborObserver(NodeConfiguration config, Console console,
            UUID instance, AtomicReference<OverlayNode> reference)
    {
        return event ->
        {
            console.log("EVENT node=" + config.nodeId() + " instance=" + instance + " " + event,
                    event instanceof NeighborManager.Received r && r.message().type() == pt.uminho.esr.ott.protocol.MessageType.ERROR);

            if(event instanceof NeighborManager.Received received)
            {
                var m = received.message();
                UUID stream = m.payload() instanceof pt.uminho.esr.ott.protocol.ControlMessage.Trace t ? t.streamId()
                        : m.payload() instanceof pt.uminho.esr.ott.protocol.ControlMessage.TraceResult t ? t.streamId() : null;
                var n = reference.get();

                if(stream != null && n != null)
                {
                    var snapshot = n.routingSnapshot();
                    var route = snapshot.routes().get(stream);
                    console.log("TRACE_HOP node=" + config.nodeId() + " instance=" + instance + " stream=" + stream
                            + " request=" + m.requestId() + " direction=" + (m.type() == pt.uminho.esr.ott.protocol.MessageType.ROUTE_TRACE ? "request" : "response")
                            + " session=" + received.session() + " capturedAt=" + snapshot.capturedAtNanos() + " costMicros="
                            + (route == null ? "UNAVAILABLE" : route.selectedMetric().map(x -> Long.toString(x.costMicros())).orElse("UNAVAILABLE")), false);
                }
            }
        };
    }

    private static String switchLog(String node, UUID instance, RoutingService.SwitchEvent change)
    {
        var before = change.previous();
        var after = change.replacement();
        return "ROUTE_SWITCH node=" + node + " instance=" + instance + " stream=" + change.streamId()
                + " origin=" + change.serverId() + " epoch=" + change.serverEpoch() + " operation=" + change.operationId()
                + " phase=" + change.phase() + " previous=" + before.nodeId() + "/" + before.instanceId() + "/" + before.connectionId()
                + " replacement=" + after.nodeId() + "/" + after.instanceId() + "/" + after.connectionId()
                + " previousRevision=" + change.previousRevision() + " replacementRevision=" + change.replacementRevision()
                + " previousCostMicros=" + change.previousCostMicros() + " replacementCostMicros=" + change.replacementCostMicros()
                + " reason=" + change.reason() + " elapsedNanos=" + change.elapsedNanos()
                + " previousPath=" + change.previousPath() + " replacementPath=" + change.replacementPath();
    }



    //* === DiagnosticConsole === *

    /** CLI-owned tickets/cursor. Never runs on the node owner or waits for a network future. */
    private static final class DiagnosticConsole
    {

        //* === Atributos e Variáveis === *

        final Console console;
        final LongSupplier clock;
        final LinkedHashMap<UUID, RoutingService.TraceTicket> tickets = new LinkedHashMap<>();
        Listing listing;



        //* === Construtor === *

        DiagnosticConsole(Console console, LongSupplier clock)
        {
            this.console = console;
            this.clock = clock;
        }



        //* === Métodos === *

        void trace(String command, RunningService service)
        {
            String[] parts = command.split(" ", -1);
            final UUID stream;

            try
            {
                if(parts.length != 2 || parts[1].length() != 36)
                    throw new IllegalArgumentException();

                stream = UUID.fromString(parts[1]);

                if(!stream.toString().equalsIgnoreCase(parts[1]))
                    throw new IllegalArgumentException();
            }
            catch(IllegalArgumentException e)
            {
                console.log("COMMAND_ERROR trace requires one canonical UUID", true);
                return;
            }

            if(tickets.size() == 4)
            {
                console.log("TRACE_ERROR stream=" + stream + " reason=CLI_BUSY", false);
                return;
            }

            var ticket = service.requestTrace(stream);
            tickets.put(ticket.traceId(), ticket);
            console.log("TRACE_STARTED stream=" + stream + " trace=" + ticket.traceId(), false);
        }

        void list(String kind, RunningService service)
        {
            if(listing != null)
            {
                console.log("COMMAND_ERROR diagnostic listing BUSY", true);
                return;
            }

            listing = new Listing(kind, service, clock);
        }

        void poll()
        {
            var it = tickets.values().iterator();

            while(it.hasNext() && console.outputs.remainingCapacity() > 0)
            {
                var result = it.next().result();

                if(result.isPresent())
                {
                    var r = result.orElseThrow();
                    console.log((r.state() == RoutingService.TraceState.SUCCESS ? "TRACE_RESULT" : "TRACE_ERROR")
                            + " stream=" + r.streamId() + " trace=" + r.traceId() + " state=" + r.state() + " code=" + r.errorCode()
                            + " reason=" + escape(r.detail()) + " visited=" + r.visited() + " elapsedNanos=" + r.elapsedNanos(), false);
                    it.remove();
                }
            }

            for(int i = 0; i < 16 && listing != null && console.outputs.remainingCapacity() > 0; i++)
            {
                console.log(listing.next(), false);

                if(listing.done)
                    listing = null;
            }
        }

        void cancel()
        {
            listing = null;

            for(var ticket : tickets.values())
                ticket.cancel();
        }
    }



    //* === Utility Methods === *

    private static String escape(String value)
    {
        StringBuilder text = new StringBuilder();

        for(int i = 0; i < value.length() && text.length() < MAX_LOG_CHARS - 8; i++)
        {
            char c = value.charAt(i);

            if(c == '\n')
                text.append("\\n");
            else if(c == '\r')
                text.append("\\r");
            else if(c == '\t')
                text.append("\\t");
            else if(c == '\\')
                text.append("\\\\");
            else if(Character.isISOControl(c))
                text.append('?');
            else
                text.append(c);
        }

        return text.toString();
    }

    private static final class Listing
    {

        //* === Atributos e Variáveis === *

        final String kind;
        final long at;
        final long captured;
        final Iterator<? extends Map.Entry<?, ?>> entries;
        Iterator<RouteCandidate> candidates = Collections.emptyIterator();
        UUID routeId;
        boolean header = true;
        boolean done;



        //* === Construtor === *

        Listing(String kind, RunningService service, LongSupplier clock)
        {
            this.kind = kind;

            if(kind.equals("metrics"))
            {
                var snapshot = service.monitoringSnapshot();
                at = clock.getAsLong();
                captured = snapshot.map(x -> x.publishedAtNanos()).orElse(at);
                entries = snapshot.map(x -> x.links().entrySet().iterator()).orElseGet(() -> Collections.emptyIterator());
            }
            else
            {
                var snapshot = service.routingSnapshot();
                at = clock.getAsLong();
                captured = snapshot.capturedAtNanos();
                // One reference to immutable data; no eager formatting/sort of a whole table.
                entries = kind.equals("routes") ? snapshot.routes().entrySet().iterator() : snapshot.catalog().visibleAt(at).entries().entrySet().iterator();
            }
        }



        //* === Método next === *

        String next()
        {
            String prefix = kind.toUpperCase(Locale.ROOT);

            if(header)
            {
                header = false;
                return prefix + " capturedAt=" + captured + " ageNanos=" + Math.max(0, at - captured) + " count=snapshot";
            }

            if(candidates.hasNext())
            {
                var c = candidates.next();
                long remaining = c.candidateTtlNanos() - (at - c.generationStartedAtNanos());

                return "ROUTE_CANDIDATE stream=" + routeId + " session=" + c.upstream() + " path=" + c.path()
                        + " costMicros=" + c.metric().map(m -> Long.toString(m.costMicros())).orElse("UNMEASURED")
                        + " remainingNanos=" + Math.max(0, remaining) + " expired=" + (remaining <= 0);
            }

            if(!entries.hasNext())
            {
                done = true;
                return prefix + "_END";
            }

            var entry = entries.next();

            if(entry.getValue() instanceof Route route)
            {
                routeId = route.streamId();
                candidates = route.candidates().iterator();

                return "ROUTE stream=" + routeId + " origin=" + route.serverId() + " epoch=" + route.serverEpoch()
                        + " selected=" + route.knownUpstream() + " state=" + route.state() + " revision=" + route.revision()
                        + " snapshotOnly=true costMicros=" + route.selectedMetric().map(m -> Long.toString(m.costMicros())).orElse("UNAVAILABLE");
            }

            if(entry.getValue() instanceof pt.uminho.esr.ott.streaming.StreamCatalog.Entry stream)
                return "CATALOG_STREAM stream=" + stream.info().streamId() + " origin=" + stream.info().serverId()
                        + " title=" + escape(stream.info().title()) + " epoch=" + stream.info().serverEpoch() + " state=" + stream.state()
                        + " remainingNanos=" + stream.remainingNanos() + " local=" + stream.localOffer();

            var link = (pt.uminho.esr.ott.monitoring.LinkMetrics) entry.getValue();
            return "METRIC node=" + entry.getKey() + " session=" + link.session() + " state=" + link.state()
                    + " ewmaMicros=" + link.ewmaMicros() + " costMicros=" + link.costMicros() + " sampleAgeNanos=" + link.sampleAgeNanos(at);
        }
    }

    private static boolean join(Thread thread, Duration budget)
    {
        if(thread.getState() == Thread.State.NEW)
            return true;

        if(thread == Thread.currentThread())
            return false;

        try
        {
            if(!budget.isZero())
                thread.join(budget);

            return !thread.isAlive();
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static String bounded(String text)
    {
        if(text == null)
            return "unspecified";

        String marker = "...[truncated]";
        return text.length() <= MAX_LOG_CHARS ? text : text.substring(0, MAX_LOG_CHARS - marker.length()) + marker;
    }

    /** Bound before copying; one event stays one physical log line, including hostile titles. */
    private static String oneLine(String value)
    {
        if(value == null)
            return "unspecified";

        var text = new StringBuilder();
        int i = 0;

        while(i < value.length() && text.length() <= MAX_LOG_CHARS)
        {
            char c = value.charAt(i++);

            if(c == '\n')
                text.append("\\n");
            else if(c == '\r')
                text.append("\\r");
            else if(c == '\t')
                text.append("\\t");
            else if(Character.isISOControl(c) || c == '\u2028' || c == '\u2029')
                text.append('?');
            else
                text.append(c);
        }

        if(i < value.length())
            text.append("...[truncated]");

        return bounded(text.toString());
    }



    //* === Console === *

    private static final class Console implements ConsoleAccess
    {

        //* === Atributos e Variáveis === *

        final ArrayBlockingQueue<InputEvent> commands;
        final ArrayBlockingQueue<OutputEvent> outputs;
        final AtomicBoolean stopped = new AtomicBoolean();
        final AtomicBoolean failed = new AtomicBoolean();
        final AtomicBoolean prepareCancelled = new AtomicBoolean();
        final AtomicBoolean outputStopping = new AtomicBoolean();
        final CountDownLatch finished = new CountDownLatch(1);
        final Thread reader;
        final Thread writer;
        final LongSupplier clock;
        final long budget;
        volatile long stopAt;



        //* === Construtor === *

        Console(InputStream input, PrintStream output, PrintStream error, NodeConfiguration config,
                LongSupplier clock)
        {
            this.clock = Objects.requireNonNull(clock);
            budget = config.runtime().control().shutdownTimeout().toNanos();
            commands = new ArrayBlockingQueue<>(config.runtime().overlay().eventsCapacity());
            outputs = new ArrayBlockingQueue<>(config.runtime().control().queueCapacity());
            reader = Thread.ofVirtual().name("ott-cli-input").unstarted(() -> read(input));
            writer = Thread.ofVirtual().name("ott-cli-output").unstarted(() -> write(output, error));
        }



        //* === Métodos === *

        @Override
        public synchronized void stop(boolean fatal)
        {
            prepareCancelled.set(true);
            if(fatal)
                failed.set(true);

            if(stopped.compareAndSet(false, true))
                stopAt = clock.getAsLong();
        }

        @Override
        public AtomicBoolean prepareCancelled()
        {
            return prepareCancelled;
        }

        @Override
        public Duration left()
        {
            return Duration.ofNanos(Math.max(0, budget - (clock.getAsLong() - stopAt)));
        }

        StreamingServer.ShutdownBudget shutdownBudget()
        {
            return new StreamingServer.ShutdownBudget(stopAt, budget);
        }

        @Override
        public void log(String text, boolean error)
        {
            if(!outputs.offer(new OutputEvent(oneLine(text), error)))
                stop(true);
        }

        void command(InputEvent event)
        {
            if(!stopped.get() && !commands.offer(event))
            {
                log("COMMAND_OVERFLOW", true);
                stop(true);
            }
        }

        void read(InputStream input)
        {
            try
            {
                var chars = new InputStreamReader(input, StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT).onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT));
                var line = new StringBuilder();
                boolean overlong = false;

                while(!stopped.get())
                {
                    int value = chars.read();

                    if(value < 0)
                    {
                        if(!overlong && !line.isEmpty())
                            command(new InputEvent(line.toString(), false));

                        command(new InputEvent("", true));
                        return;
                    }

                    if(value == '\n')
                    {
                        if(!overlong)
                            command(new InputEvent(line.toString(), false));

                        line.setLength(0);
                        overlong = false;
                    }
                    else if(!overlong)
                    {
                        if(line.length() == MAX_LINE_CHARS)
                        {
                            log("COMMAND_ERROR line exceeds 4096 characters", true);
                            line.setLength(0);
                            overlong = true;
                        }
                        else
                            line.append((char) value);
                    }
                }
            }
            catch(IOException | RuntimeException e)
            {
                if(!stopped.get())
                {
                    log("INPUT_ERROR " + e.getMessage(), true);
                    stop(true);
                }
            }
        }

        void write(PrintStream output, PrintStream error)
        {
            try
            {
                while(!outputStopping.get() || !outputs.isEmpty())
                {
                    var item = outputs.poll(20, TimeUnit.MILLISECONDS);

                    if(item == null)
                        continue;

                    var target = item.error() ? error : output;
                    target.println(item.text());

                    if(target.checkError())
                    {
                        stop(true);
                        return;
                    }
                }
            }
            catch(InterruptedException e)
            {
                if(!outputStopping.get())
                    stop(true);

                Thread.currentThread().interrupt();
            }
            catch(RuntimeException e)
            {
                stop(true);
            }
        }
    }
}
