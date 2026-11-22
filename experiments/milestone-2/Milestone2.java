import pt.uminho.esr.ott.bootstrap.BootstrapServer;
import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.config.RuntimeConfiguration;
import pt.uminho.esr.ott.config.StreamConfiguration;
import pt.uminho.esr.ott.config.TopologyConfiguration;
import pt.uminho.esr.ott.monitoring.LinkMetrics;
import pt.uminho.esr.ott.monitoring.NetworkMonitor;
import pt.uminho.esr.ott.overlay.Neighbor;
import pt.uminho.esr.ott.overlay.NeighborManager;
import pt.uminho.esr.ott.overlay.NodeRole;
import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.protocol.MessageType;
import pt.uminho.esr.ott.routing.Route;
import pt.uminho.esr.ott.routing.RoutingService;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/**
 * IMP-015 / V-018 — segundo marco: árvores de distribuição e adaptação.
 * Auxiliar de demonstração (não é código de produção): nós reais em TCP
 * loopback (BootstrapServer + NeighborManager + RoutingService, API pública
 * do JAR), métricas de custo controladas via onMetrics no fio do owner.
 * Escreve CHECK_PASS/CHECK_FAIL em stdout e artefactos em <runDir>.
 */
public final class Milestone2
{
    static final UUID STREAM_A = UUID.fromString("00000000-0000-4000-8000-00000000000a");
    static final UUID STREAM_B = UUID.fromString("00000000-0000-4000-8000-00000000000b");

    static Path runDir;
    static int passed, failed;
    static String scenario = "-";


    public static void main(String[] args) throws Exception
    {
        runDir = Path.of(args[0]);
        Files.createDirectories(runDir);
        System.out.println("RUN_ID " + runDir.getFileName());
        System.out.println("JAVA " + System.getProperty("java.version") + " " + System.getProperty("java.vendor"));

        run("C1", Milestone2::scenarioIsolation);
        run("C2", Milestone2::scenarioAggregated);
        run("C3", Milestone2::scenarioFailover);
        run("C4", Milestone2::scenarioRttChange);

        System.out.println("SUMMARY passed=" + passed + " failed=" + failed);
        var result = "{\n  \"run_id\": \"" + runDir.getFileName() + "\",\n  \"passed\": " + passed + ",\n  \"failed\": " + failed + ",\n  \"verdict\": \"" + (failed == 0 ? "PASS" : "FAIL") + "\"\n}\n";
        Files.writeString(runDir.resolve("result.json"), result);
        System.exit(failed == 0 ? 0 : 1);
    }


    //* === Harness === *

    interface ScenarioBody { void run() throws Exception; }

    static void run(String name, ScenarioBody body)
    {
        scenario = name;
        int before = failed;
        System.out.println("SCENARIO_BEGIN " + name);
        try
        {
            body.run();
        }
        catch(Throwable t)
        {
            check(name, "scenario-completed", false, t.toString());
        }
        System.out.println("SCENARIO_END " + name + " failedDelta=" + (failed - before));
    }

    static void check(String sc, String name, boolean cond, String detail)
    {
        if(cond)
        {
            passed++;
            System.out.println("CHECK_PASS scenario=" + sc + " check=" + name + (detail.isEmpty() ? "" : " detail=" + detail));
        }
        else
        {
            failed++;
            System.out.println("CHECK_FAIL scenario=" + sc + " check=" + name + " detail=" + detail);
        }
    }

    static boolean awaitFor(BooleanSupplier cond, long ms)
    {
        long began = System.nanoTime();
        while(!cond.getAsBoolean())
        {
            if(System.nanoTime() - began > ms * 1_000_000L)
                return false;
            try { Thread.sleep(5); } catch(InterruptedException e) { Thread.currentThread().interrupt(); return false; }
        }
        return true;
    }

    static void act(NeighborManager.SerialOwner owner, Runnable action) throws Exception
    {
        var done = new CompletableFuture<Void>();
        if(!owner.offer(() -> {
            try { action.run(); done.complete(null); }
            catch(Throwable t) { done.completeExceptionally(t); }
        }))
            throw new IllegalStateException("owner rejected action");
        done.get(5, TimeUnit.SECONDS);
    }

    static List<Integer> ports(int count) throws Exception
    {
        var sockets = new ArrayList<ServerSocket>();
        var out = new ArrayList<Integer>();
        try
        {
            for(int i = 0; i < count; i++)
            {
                var socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                sockets.add(socket);
                out.add(socket.getLocalPort());
            }
        }
        finally
        {
            for(var s : sockets) s.close();
        }
        return out;
    }

    static NodeConfiguration config(String name, NodeRole role, int port, int data, int bootPort, List<StreamConfiguration> streams)
    {
        var d = RuntimeConfiguration.defaults();
        var runtime = new RuntimeConfiguration(d.control(), d.overlay(), d.monitoring(),
                new RuntimeConfiguration.Routing(Duration.ofMillis(200), d.routing().candidateTtl(), d.routing().cacheCapacity(),
                        d.routing().cacheTtl(), d.routing().floodMaxPerSecond(), d.routing().activateTimeout(),
                        d.routing().activateAttempts(), 20, 3, d.routing().switchMinDwell()),
                d.subscription(), d.video(), d.source());
        return new NodeConfiguration(Path.of(name + ".properties"), name, role, "127.0.0.1", "127.0.0.1", port, data,
                role == NodeRole.BOOTSTRAP ? Optional.empty() : Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1", bootPort)),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), streams, runtime);
    }

    static NodeConfiguration bootConfig(int port, Map<String, Set<String>> graph, Map<String, int[]> nodePorts, Map<String, NodeRole> roles)
    {
        var topology = new LinkedHashMap<String, TopologyConfiguration.Node>();
        graph.forEach((n, neighbors) -> {
            var p = nodePorts.get(n);
            topology.put(n, new TopologyConfiguration.Node(n, "127.0.0.1", p[0], p[1], roles.get(n), neighbors));
        });
        return new NodeConfiguration(Path.of("bootstrap.properties"), "BOOT", NodeRole.BOOTSTRAP, "127.0.0.1", "127.0.0.1", port, 0,
                Optional.empty(), Optional.of(Path.of("topology.properties")), Optional.empty(), Optional.empty(),
                Optional.of(new TopologyConfiguration(Path.of("topology.properties"), topology)), List.of(), RuntimeConfiguration.defaults());
    }

    static StreamConfiguration stream(UUID id, String server, String title)
    {
        return new StreamConfiguration(Path.of("catalog.properties"), id, server, title, Path.of(title + ".Mjpeg"), 10, false);
    }

    static String streamOf(ControlMessage m)
    {
        var p = m.payload();
        if(p instanceof ControlMessage.Activation a) return a.streamId().toString();
        if(p instanceof ControlMessage.ActivationAck a) return a.streamId().toString();
        if(p instanceof ControlMessage.Deactivation d) return d.streamId().toString();
        if(p instanceof ControlMessage.Announcement a) return a.stream().streamId().toString();
        if(p instanceof ControlMessage.Withdrawal w) return w.streamId().toString();
        if(p instanceof ControlMessage.Trace t) return t.streamId().toString();
        if(p instanceof ControlMessage.TraceResult t) return t.streamId().toString();
        return "-";
    }

    static final class Node implements AutoCloseable
    {
        final String id;
        final NeighborManager.SerialOwner owner;
        final NeighborManager manager;
        final RoutingService router;
        final List<String> tap = new CopyOnWriteArrayList<>();
        final List<String> events = new CopyOnWriteArrayList<>();
        final List<RoutingService.SwitchEvent> switches = new CopyOnWriteArrayList<>();
        final AtomicInteger metricCount = new AtomicInteger(1);

        Node(NodeConfiguration cfg, long baseCost) throws Exception
        {
            id = cfg.nodeId();
            UUID instance = UUID.randomUUID();
            owner = NeighborManager.createSerialOwner(4096);
            AtomicReference<RoutingService> ref = new AtomicReference<>();
            manager = new NeighborManager(cfg, instance, owner, event -> onManagerEvent(event, instance, baseCost, ref));
            router = new RoutingService(cfg, instance, owner, (session, message) -> {
                tap.add("type=" + message.type() + " stream=" + streamOf(message) + " to=" + session.nodeId() + " request=" + message.requestId());
                return manager.send(session, message);
            }, event -> {
                if(event.fatal())
                    events.add("FATAL reason=" + event.reason());
                event.routeSwitch().ifPresent(switches::add);
            });
            ref.set(router);
            act(owner, router::start);
            manager.start().toCompletableFuture().get(15, TimeUnit.SECONDS);
        }

        void onManagerEvent(NeighborManager.Event event, UUID instance, long baseCost, AtomicReference<RoutingService> ref)
        {
            if(event instanceof NeighborManager.Received r && r.message().type() == MessageType.PROBE_PING)
                manager.send(r.session(), new ControlMessage(MessageType.PROBE_PONG, r.message().requestId(), id, instance, r.message().payload()));
            RoutingService routing = ref.get();
            if(routing != null)
            {
                routing.onNeighborEvent(event);
                if(event instanceof NeighborManager.NeighborUp up)
                    routing.onMetrics(new NetworkMonitor.Updated(new LinkMetrics(up.session(), LinkMetrics.State.MEASURED,
                            OptionalDouble.of(baseCost), OptionalLong.of(baseCost), metricCount.getAndIncrement(),
                            false, OptionalLong.of(System.nanoTime()), 0)));
            }
            if(event instanceof NeighborManager.NeighborUp || event instanceof NeighborManager.NeighborDown)
                events.add(event.toString());
        }

        void setCost(String neighborId, long cost, long count) throws Exception
        {
            var session = manager.snapshot().get(neighborId);
            if(session == null)
                throw new IllegalStateException(id + ": no active session to " + neighborId);
            act(owner, () -> router.onMetrics(new NetworkMonitor.Updated(new LinkMetrics(session, LinkMetrics.State.MEASURED,
                    OptionalDouble.of(cost), OptionalLong.of(cost), count, false, OptionalLong.of(System.nanoTime()), 0))));
        }

        RoutingService.SubscriptionTicket subscribe(UUID stream)
        {
            return router.requestSubscription(stream, UUID.randomUUID());
        }

        boolean forwardingHas(UUID stream)
        {
            return router.snapshot().forwarding().containsKey(stream);
        }

        String forwardingUpstream(UUID stream)
        {
            var f = router.snapshot().forwarding().get(stream);
            return f == null || f.upstream().isEmpty() ? "NONE" : f.upstream().orElseThrow().nodeId();
        }

        long countTap(MessageType type, UUID stream, String to)
        {
            String needle = "type=" + type + " stream=" + stream + " to=" + to + " request=";
            return tap.stream().filter(l -> l.contains(needle)).count();
        }

        public void close()
        {
            try { router.requestClose(); } catch(Throwable ignored) { }
            try { manager.requestClose(); } catch(Throwable ignored) { }
            try { router.awaitTermination(Duration.ofSeconds(5)); } catch(Throwable ignored) { }
            try { manager.awaitTermination(Duration.ofSeconds(5)); } catch(Throwable ignored) { }
            try { owner.requestClose(); } catch(Throwable ignored) { }
            try { owner.awaitTermination(Duration.ofSeconds(5)); } catch(Throwable ignored) { }
            try { owner.close(); } catch(Throwable ignored) { }
        }
    }

    static final class Rig implements AutoCloseable
    {
        final BootstrapServer boot;
        final Map<String, Node> nodes = new LinkedHashMap<>();

        Rig(Map<String, Set<String>> graph, Map<String, NodeRole> roles, Map<String, List<StreamConfiguration>> streams, int portCount) throws Exception
        {
            var ports = ports(1 + portCount * 2);
            int bootPort = ports.get(0);
            var nodePorts = new LinkedHashMap<String, int[]>();
            int i = 1;
            for(var n : graph.keySet())
            {
                nodePorts.put(n, new int[]{ports.get(i), ports.get(i + 1)});
                i += 2;
            }
            boot = new BootstrapServer(bootConfig(bootPort, graph, nodePorts, roles));
            for(var n : graph.keySet())
            {
                var p = nodePorts.get(n);
                var cfg = config(n, roles.get(n), p[0], p[1], bootPort, streams.getOrDefault(n, List.of()));
                nodes.put(n, new Node(cfg, 10));
            }
        }

        void converge(Map<String, Integer> expectedNeighbors) throws Exception
        {
            for(var e : expectedNeighbors.entrySet())
            {
                var n = nodes.get(e.getKey());
                if(!awaitFor(() -> {
                    var diag = n.manager.diagnostics();
                    var snap = n.manager.snapshot();
                    return diag.size() == e.getValue() && snap.size() == e.getValue()
                            && diag.values().stream().allMatch(x -> x.state() == Neighbor.State.ACTIVE && x.session().isPresent());
                }, 25000))
                    throw new RuntimeException("convergence timeout for " + e.getKey());
            }
            for(var n : nodes.values())
                if(!awaitFor(() -> n.router.snapshot().diagnostics().phase() == RoutingService.Phase.RUNNING, 10000))
                    throw new RuntimeException("router not RUNNING on " + n.id);
        }

        boolean routesContain(String node, UUID stream)
        {
            return nodes.get(node).router.snapshot().routes().containsKey(stream);
        }

        public void close()
        {
            for(var n : nodes.values()) n.close();
            try { boot.close(); } catch(Throwable ignored) { }
        }
    }

    static boolean ticketSuccess(RoutingService.SubscriptionTicket t, long ms)
    {
        return awaitFor(() -> t.result().isPresent()
                && t.result().orElseThrow().state() == RoutingService.SubscriptionState.SUCCESS, ms);
    }

    static void dump(String sc, Rig rig) throws Exception
    {
        try(var msgs = new PrintWriter(runDir.resolve("messages-" + sc + ".jsonl").toFile()))
        {
            for(var n : rig.nodes.values())
                for(var l : n.tap)
                    msgs.println("{\"node\":\"" + n.id + "\",\"line\":\"" + l + "\"}");
        }
        try(var out = new PrintWriter(runDir.resolve("snapshots-" + sc + ".txt").toFile()))
        {
            for(var n : rig.nodes.values())
            {
                var s = n.router.snapshot();
                out.println("NODE " + n.id + " phase=" + s.diagnostics().phase());
                for(var r : s.routes().values())
                    out.println("  ROUTE stream=" + r.streamId() + " origin=" + r.serverId()
                            + " selected=" + r.knownUpstream().map(k -> k.nodeId()).orElse("NONE")
                            + " state=" + r.state() + " revision=" + r.revision()
                            + " cost=" + r.selectedMetric().map(m -> Long.toString(m.costMicros())).orElse("UNAVAILABLE"));
                for(var f : s.forwarding().values())
                    out.println("  FORWARD stream=" + f.streamId() + " origin=" + f.serverId()
                            + " upstream=" + f.upstream().map(x -> x.nodeId()).orElse("NONE")
                            + " path=" + f.path() + " downstreams=" + f.downstreams().keySet()
                            + " localConsumers=" + f.localConsumers() + " revision=" + f.revision());
                for(var e : n.events)
                    out.println("  EVENT " + e);
                for(var sw : n.switches)
                    out.println("  SWITCH " + sw);
            }
        }
    }


    //* === C1: isolamento entre fluxos (2 servidores, 4 consumidores) === *

    static void scenarioIsolation() throws Exception
    {
        var graph = new LinkedHashMap<String, Set<String>>();
        graph.put("S1", Set.of("R"));
        graph.put("S2", Set.of("R"));
        graph.put("R", Set.of("S1", "S2", "C1", "C2", "C3", "C4"));
        graph.put("C1", Set.of("R"));
        graph.put("C2", Set.of("R"));
        graph.put("C3", Set.of("R"));
        graph.put("C4", Set.of("R"));
        var roles = Map.of("S1", NodeRole.SERVER, "S2", NodeRole.SERVER, "R", NodeRole.RELAY,
                "C1", NodeRole.CLIENT, "C2", NodeRole.CLIENT, "C3", NodeRole.CLIENT, "C4", NodeRole.CLIENT);
        var streams = Map.of("S1", List.of(stream(STREAM_A, "S1", "A")), "S2", List.of(stream(STREAM_B, "S2", "B")));
        try(var rig = new Rig(graph, roles, streams, graph.size()))
        {
            rig.converge(Map.of("S1", 1, "S2", 1, "R", 6, "C1", 1, "C2", 1, "C3", 1, "C4", 1));
            check("C1", "converged", true, "todos os nodos ACTIVE");
            check("C1", "routes-flooded", awaitFor(() -> rig.routesContain("R", STREAM_A) && rig.routesContain("R", STREAM_B)
                    && rig.routesContain("C1", STREAM_A) && rig.routesContain("C2", STREAM_B), 20000), "catalogo com A e B em R e consumidores");

            var c1 = rig.nodes.get("C1"); var c2 = rig.nodes.get("C2"); var c3 = rig.nodes.get("C3"); var c4 = rig.nodes.get("C4");
            var r = rig.nodes.get("R");
            var t1 = c1.subscribe(STREAM_A);
            var t3 = c3.subscribe(STREAM_A);
            var t2 = c2.subscribe(STREAM_B);
            var t4 = c4.subscribe(STREAM_B);
            check("C1", "subscribe-A-C1", ticketSuccess(t1, 15000), "ticket C1 stream A");
            check("C1", "subscribe-A-C3", ticketSuccess(t3, 15000), "ticket C3 stream A");
            check("C1", "subscribe-B-C2", ticketSuccess(t2, 15000), "ticket C2 stream B");
            check("C1", "subscribe-B-C4", ticketSuccess(t4, 15000), "ticket C4 stream B");

            check("C1", "isolation-C1-only-A", c1.router.snapshot().forwarding().keySet().equals(Set.of(STREAM_A)),
                    "forwarding C1=" + c1.router.snapshot().forwarding().keySet());
            check("C1", "isolation-C2-only-B", c2.router.snapshot().forwarding().keySet().equals(Set.of(STREAM_B)),
                    "forwarding C2=" + c2.router.snapshot().forwarding().keySet());
            check("C1", "relay-has-both-streams", r.forwardingHas(STREAM_A) && r.forwardingHas(STREAM_B),
                    "forwarding R A=" + r.forwardingHas(STREAM_A) + " B=" + r.forwardingHas(STREAM_B));

            t1.cancel();
            Thread.sleep(3000);
            check("C1", "cancel-C1-keeps-C3-on-A", c3.forwardingHas(STREAM_A), "C3 forwarding A apos cancel de C1");
            check("C1", "cancel-C1-keeps-B-consumers", c2.forwardingHas(STREAM_B) && c4.forwardingHas(STREAM_B),
                    "C2/C4 forwarding B preservado");
            check("C1", "relay-keeps-A-for-C3", r.forwardingHas(STREAM_A), "R mantem A enquanto C3 activo");
            dump("C1", rig);

            var s1 = rig.nodes.get("S1");
            s1.close(); // origem S1 termina: A fica indisponivel, B preservado
            boolean aDropped = awaitFor(() -> !c3.forwardingHas(STREAM_A), 25000);
            check("C1", "origin-S1-down-drops-A", aDropped, "C3 deixou de ter forwarding A");
            check("C1", "origin-S1-down-preserves-B", c2.forwardingHas(STREAM_B) && c4.forwardingHas(STREAM_B),
                    "C2/C4 continuam com B");
            dump("C1-final", rig);
        }
    }


    //* === C2: procura agregada (1 servidor, 2 consumidores no mesmo stream) === *

    static void scenarioAggregated() throws Exception
    {
        var graph = new LinkedHashMap<String, Set<String>>();
        graph.put("S", Set.of("R"));
        graph.put("R", Set.of("S", "C1", "C2"));
        graph.put("C1", Set.of("R"));
        graph.put("C2", Set.of("R"));
        var roles = Map.of("S", NodeRole.SERVER, "R", NodeRole.RELAY, "C1", NodeRole.CLIENT, "C2", NodeRole.CLIENT);
        var streams = Map.of("S", List.of(stream(STREAM_A, "S", "A")));
        try(var rig = new Rig(graph, roles, streams, graph.size()))
        {
            rig.converge(Map.of("S", 1, "R", 3, "C1", 1, "C2", 1));
            check("C2", "routes-flooded", awaitFor(() -> rig.routesContain("R", STREAM_A)
                    && rig.routesContain("C1", STREAM_A) && rig.routesContain("C2", STREAM_A), 20000), "rota A em R/C1/C2");

            var c1 = rig.nodes.get("C1"); var c2 = rig.nodes.get("C2"); var r = rig.nodes.get("R"); var s = rig.nodes.get("S");
            var t1 = c1.subscribe(STREAM_A);
            var t2 = c2.subscribe(STREAM_A);
            check("C2", "both-consumers-success", ticketSuccess(t1, 15000) && ticketSuccess(t2, 15000), "C1 e C2 SUCCESS no mesmo stream");

            long activates = r.countTap(MessageType.ACTIVATE, STREAM_A, "S");
            check("C2", "single-activate-on-trunk", activates == 1, "ACTIVATE R->S=" + activates + " (esperado 1, RF-011)");
            var fwdS = s.router.snapshot().forwarding().get(STREAM_A);
            check("C2", "server-single-downstream", fwdS != null && fwdS.downstreams().size() == 1,
                    "downstreams em S=" + (fwdS == null ? "null" : fwdS.downstreams().keySet()));
            var fwdR = r.router.snapshot().forwarding().get(STREAM_A);
            check("C2", "relay-two-downstream-consumers", fwdR != null && fwdR.downstreams().size() == 2
                    && fwdR.downstreams().containsKey("C1") && fwdR.downstreams().containsKey("C2"),
                    "downstreams em R=" + (fwdR == null ? "null" : fwdR.downstreams().keySet()));
            check("C2", "relay-single-upstream", fwdR != null && fwdR.upstream().map(u -> u.nodeId().equals("S")).orElse(false),
                    "upstream em R=" + (fwdR == null ? "null" : fwdR.upstream().map(u -> u.nodeId()).orElse("NONE")));
            var fwdC1 = c1.router.snapshot().forwarding().get(STREAM_A);
            check("C2", "consumer-local-token", fwdC1 != null && fwdC1.localConsumers().size() == 1,
                    "localConsumers em C1=" + (fwdC1 == null ? "null" : fwdC1.localConsumers()));
            dump("C2", rig);

            t1.cancel();
            Thread.sleep(3000);
            check("C2", "one-leaves-other-keeps", c2.forwardingHas(STREAM_A) && r.forwardingHas(STREAM_A),
                    "C2 e R mantem A apos C1 sair");
            check("C2", "no-new-activate-after-leave", r.countTap(MessageType.ACTIVATE, STREAM_A, "S") == 1,
                    "ACTIVATE R->S continua 1");

            t2.cancel();
            boolean pruned = awaitFor(() -> !r.forwardingHas(STREAM_A), 15000);
            check("C2", "last-leave-prunes", pruned, "R removeu forwarding A apos ultimo consumidor");
            check("C2", "deactivate-sent-upstream", r.countTap(MessageType.DEACTIVATE, STREAM_A, "S") >= 1,
                    "DEACTIVATE R->S=" + r.countTap(MessageType.DEACTIVATE, STREAM_A, "S"));
            check("C2", "consumer-forwarding-removed", !c2.forwardingHas(STREAM_A), "C2 sem forwarding A");
            dump("C2-final", rig);
        }
    }


    //* === C3: falha e recuperação (morte de relay, switch para alternativa) === *

    static void scenarioFailover() throws Exception
    {
        // 3a: losango S—R1/R2—C; morte abrupta de R1 (disconnect TRANSPORT, sem NODE_LEAVE)
        var graph = new LinkedHashMap<String, Set<String>>();
        graph.put("S", Set.of("R1", "R2"));
        graph.put("R1", Set.of("S", "C"));
        graph.put("R2", Set.of("S", "C"));
        graph.put("C", Set.of("R1", "R2"));
        var roles = Map.of("S", NodeRole.SERVER, "R1", NodeRole.RELAY, "R2", NodeRole.RELAY, "C", NodeRole.CLIENT);
        var streams = Map.of("S", List.of(stream(STREAM_A, "S", "A")));
        try(var rig = new Rig(graph, roles, streams, graph.size()))
        {
            rig.converge(Map.of("S", 2, "R1", 2, "R2", 2, "C", 2));
            var c = rig.nodes.get("C");
            check("C3", "route-known", awaitFor(() -> rig.routesContain("C", STREAM_A), 20000), "rota A em C");
            c.setCost("R1", 10, 100);
            c.setCost("R2", 1000, 100);
            var t = c.subscribe(STREAM_A);
            check("C3", "subscribe-success", ticketSuccess(t, 15000), "C subscreveu A");
            check("C3", "confirmed-via-R1", awaitFor(() -> c.forwardingHas(STREAM_A) && c.forwardingUpstream(STREAM_A).equals("R1"), 15000),
                    "upstream=" + c.forwardingUpstream(STREAM_A));

            // morte abrupta de R1: corte TRANSPORT nos dois lados, sem NODE_LEAVE
            var s = rig.nodes.get("S");
            var sToR1 = s.manager.snapshot().get("R1");
            var cToR1 = c.manager.snapshot().get("R1");
            s.manager.disconnect(sToR1).toCompletableFuture().get(5, TimeUnit.SECONDS);
            c.manager.disconnect(cToR1).toCompletableFuture().get(5, TimeUnit.SECONDS);
            rig.nodes.get("R1").close();

            boolean switched = awaitFor(() -> c.forwardingHas(STREAM_A) && c.forwardingUpstream(STREAM_A).equals("R2"), 25000);
            check("C3", "failover-to-R2", switched, "upstream apos morte de R1=" + c.forwardingUpstream(STREAM_A));
            check("C3", "neighbordown-observed", c.events.stream().anyMatch(e -> e.contains("NeighborDown")), "evento NeighborDown em C");
            check("C3", "subscription-still-success", t.result().isPresent()
                    && t.result().orElseThrow().state() == RoutingService.SubscriptionState.SUCCESS, "ticket permanece SUCCESS");
            dump("C3", rig);
        }

        // 3b: cadeia S—R1—C sem alternativa; morte de R1 -> sem caminho, explícito
        var graph2 = new LinkedHashMap<String, Set<String>>();
        graph2.put("S", Set.of("R1"));
        graph2.put("R1", Set.of("S", "C"));
        graph2.put("C", Set.of("R1"));
        try(var rig = new Rig(graph2, roles, streams, graph2.size()))
        {
            rig.converge(Map.of("S", 1, "R1", 2, "C", 1));
            var c = rig.nodes.get("C");
            var t = c.subscribe(STREAM_A);
            check("C3", "chain-subscribe-success", ticketSuccess(t, 15000), "C subscreveu A em cadeia");
            check("C3", "chain-via-R1", c.forwardingUpstream(STREAM_A).equals("R1"), "upstream=" + c.forwardingUpstream(STREAM_A));

            var s = rig.nodes.get("S");
            var sToR1 = s.manager.snapshot().get("R1");
            var cToR1 = c.manager.snapshot().get("R1");
            s.manager.disconnect(sToR1).toCompletableFuture().get(5, TimeUnit.SECONDS);
            c.manager.disconnect(cToR1).toCompletableFuture().get(5, TimeUnit.SECONDS);
            rig.nodes.get("R1").close();

            boolean unavailable = awaitFor(() -> !c.forwardingHas(STREAM_A), 25000);
            check("C3", "no-alternative-unavailable", unavailable, "C sem forwarding A sem alternativa");
            check("C3", "router-still-running", c.router.snapshot().diagnostics().phase() == RoutingService.Phase.RUNNING,
                    "router C continua RUNNING (falha explicita, nao fatal)");
            dump("C3b", rig);
        }
    }


    //* === C4: alteração de RTT (mudança de custo sustentada, comuta para melhor path) === *

    static void scenarioRttChange() throws Exception
    {
        var graph = new LinkedHashMap<String, Set<String>>();
        graph.put("S", Set.of("A", "B"));
        graph.put("A", Set.of("S", "R"));
        graph.put("B", Set.of("S", "R"));
        graph.put("R", Set.of("A", "B"));
        var roles = Map.of("S", NodeRole.SERVER, "A", NodeRole.RELAY, "B", NodeRole.RELAY, "R", NodeRole.CLIENT);
        var streams = Map.of("S", List.of(stream(STREAM_A, "S", "A")));
        try(var rig = new Rig(graph, roles, streams, graph.size()))
        {
            rig.converge(Map.of("S", 2, "A", 2, "B", 2, "R", 2));
            var r = rig.nodes.get("R");
            check("C4", "route-known", awaitFor(() -> rig.routesContain("R", STREAM_A), 20000), "rota A em R");
            r.setCost("A", 10, 100);
            r.setCost("B", 1000, 100);
            check("C4", "selected-via-A", awaitFor(() -> {
                var route = r.router.snapshot().routes().get(STREAM_A);
                return route != null && route.knownUpstream().map(k -> k.nodeId().equals("A")).orElse(false);
            }, 15000), "candidato seleccionado A");

            var t = r.subscribe(STREAM_A);
            check("C4", "subscribe-success", ticketSuccess(t, 15000), "R subscreveu A");
            check("C4", "confirmed-via-A", awaitFor(() -> r.forwardingHas(STREAM_A) && r.forwardingUpstream(STREAM_A).equals("A"), 15000),
                    "upstream=" + r.forwardingUpstream(STREAM_A));

            // melhoria sustentada em B (custo 1000 -> 1, varias amostras novas)
            r.setCost("B", 1, 101);
            r.setCost("B", 1, 102);
            r.setCost("B", 1, 103);
            boolean switched = awaitFor(() -> r.forwardingHas(STREAM_A) && r.forwardingUpstream(STREAM_A).equals("B"), 20000);
            check("C4", "switched-to-better-path", switched, "upstream apos melhoria=" + r.forwardingUpstream(STREAM_A));
            long successSwitches = r.switches.stream().filter(sw -> sw.phase() == RoutingService.SwitchPhase.SUCCESS).count();
            check("C4", "switch-event-confirmed", successSwitches == 1,
                    "eventos SUCCESS=" + successSwitches + " switches=" + r.switches);
            System.out.println("SWITCH_DETAIL scenario=C4 " + r.switches);

            // ruido sem melhoria sustentada: 2 amostras em A com custo pior (5 -> total 15 > 11 de B)
            r.setCost("A", 5, 200);
            r.setCost("A", 5, 201);
            Thread.sleep(8000); // > switchMinDwell (5s)
            long afterNoise = r.switches.stream().filter(sw -> sw.phase() == RoutingService.SwitchPhase.SUCCESS).count();
            check("C4", "noise-no-oscillation", afterNoise == 1 && r.forwardingUpstream(STREAM_A).equals("B"),
                    "SUCCESS=" + afterNoise + " upstream=" + r.forwardingUpstream(STREAM_A));
            check("C4", "noise-no-reactivation", r.countTap(MessageType.ACTIVATE, STREAM_A, "A") == 1,
                    "ACTIVATE R->A=" + r.countTap(MessageType.ACTIVATE, STREAM_A, "A") + " (esperado 1, sem reativacao por ruido)");
            dump("C4", rig);
        }
    }
}
