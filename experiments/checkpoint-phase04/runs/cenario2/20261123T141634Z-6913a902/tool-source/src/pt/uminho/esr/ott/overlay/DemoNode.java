package pt.uminho.esr.ott.overlay;

import demo.Json;
import pt.uminho.esr.ott.config.ConfigurationLoader;
import pt.uminho.esr.ott.monitoring.NetworkMonitor;
import pt.uminho.esr.ott.routing.RoutingService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Demo-only adapter in a separate JVM, using the existing composition factory. */
public final class DemoNode
{
    //* === Atributos e Variáveis === *
    private final ArrayDeque<Map<String, Object>> events = new ArrayDeque<>();
    private final Map<UUID, RoutingService.SubscriptionTicket> tickets = new LinkedHashMap<>();
    private final AtomicReference<RoutingService> routing = new AtomicReference<>();
    private final OverlayNode node;
    private final UUID instance = UUID.randomUUID();
    private long sequence;
    private long dropped;



    //* === Construtores === *

    private DemoNode(Path profile, NodeRole role) throws Exception
    {
        var config = ConfigurationLoader.load(profile, role);
        node = new OverlayNode(config, instance, event ->
                {
                    if(!(event instanceof NeighborManager.Received))
                        record("neighbor", Map.of("type", event.getClass().getSimpleName(), "event", event));
                }, NetworkMonitor::new,
                (cfg, id, owner, port, observer) ->
                {
                    var service = new RoutingService(cfg, id, owner, port, event ->
                    {
                        observer.onEvent(event);
                        event.routeSwitch().ifPresent(change ->
                        {
                            var current = routing.get().snapshot().forwarding().get(change.streamId());
                            record("switch", Map.of("change", change,
                                    "publishedRevision", current == null ? -1L : current.revision(),
                                    "publishedUpstream", current == null || current.upstream().isEmpty() ? ""
                                            : current.upstream().orElseThrow().nodeId(),
                                    "publishedPath", current == null ? List.<String>of() : current.path()));
                        });
                        if(event.fatal())
                            record("fatal", event);
                    });
                    routing.set(service);
                    return service;
                });
        node.start().toCompletableFuture().get(15, TimeUnit.SECONDS);
    }



    //* === Métodos Públicos === *

    public static void main(String[] arguments) throws Exception
    {
        if(arguments[0].equals("validate"))
        {
            var paths = new ArrayList<Path>();
            for(int i = 1; i < arguments.length; i++)
            {
                paths.add(Path.of(arguments[i]));
            }
            System.out.println(Json.write(Map.of("validated", ConfigurationLoader.loadDeployment(paths).size())));
            return;
        }
        var demo = new DemoNode(Path.of(arguments[0]), NodeRole.valueOf(arguments[1]));
        Runtime.getRuntime().addShutdownHook(new Thread(demo::close, "demo-node-cleanup"));
        System.out.println(Json.write(Map.of("ready", true, "pid", ProcessHandle.current().pid(), "instance", demo.instance)));
        try(var input = new BufferedReader(new InputStreamReader(System.in)))
        {
            String line;
            while((line = input.readLine()) != null)
            {
                if(line.length() > 4096)
                    throw new IllegalArgumentException("Demo command too long");
                if(line.equals("quit"))
                    break;
                try
                {
                    System.out.println(Json.write(demo.command(line)));
                }catch(Exception e){
                    System.out.println(Json.write(Map.of("error", e.toString())));
                }
            }
        }finally{
            demo.close();
        }
    }



    //* === Lógica === *

    private Object command(String line) throws Exception
    {
        String[] args = line.strip().split(" +");
        switch(args[0])
        {
            case "subscribe":
            {
                UUID token = UUID.fromString(args[2]);
                if(tickets.size() >= 16 && !tickets.containsKey(token))
                    throw new IllegalStateException("Demo ticket capacity");
                tickets.computeIfAbsent(token, key -> routing.get().requestSubscription(UUID.fromString(args[1]), key));
                return Map.of("admittedToken", token);
            }
            case "cancel":
            {
                var ticket = tickets.remove(UUID.fromString(args[1]));
                if(ticket != null)
                    ticket.cancel();
                return Map.of("cancelled", args[1]);
            }
            case "trace":
            {
                var ticket = node.requestTrace(UUID.fromString(args[1]));
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
                while(ticket.result().isEmpty() && System.nanoTime() < deadline)
                {
                    Thread.sleep(10);
                }
                return Map.of("trace", ticket.result());
            }
            case "play": return Map.of("play", "NOT_IMPLEMENTED");
            case "snapshot", "neighbors", "recovery", "routes", "catalog", "metrics": return snapshot();
            default: throw new IllegalArgumentException("Unknown demo command: " + line);
        }
    }


    private Map<String, Object> snapshot() throws Exception
    {
        var view = new LinkedHashMap<String, Object>();
        view.put("pid", ProcessHandle.current().pid());
        view.put("instance", instance);
        view.put("neighbors", node.diagnostics());
        view.put("discovery", node.discoveryStatus());
        view.put("candidates", node.candidates().toCompletableFuture().get(2, TimeUnit.SECONDS));
        view.put("recovery", node.recoveryStatus());
        view.put("routing", node.routingSnapshot());
        view.put("metrics", node.monitoringSnapshot());
        view.put("failure", node.failure().map(Object::toString));
        var outcomes = new LinkedHashMap<UUID, Object>();
        tickets.forEach((token, ticket) -> outcomes.put(token, ticket.result()));
        view.put("tickets", outcomes);
        synchronized(events)
        {
            view.put("events", new ArrayList<>(events));
            view.put("eventsDropped", dropped);
        }
        return view;
    }


    private void record(String kind, Object value)
    {
        synchronized(events)
        {
            if(events.size() == 512)
            {
                events.removeFirst();
                dropped++;
            }
            events.addLast(Map.of("sequence", ++sequence, "atNanos", System.nanoTime(), "kind", kind, "value", value));
        }
    }


    private void close()
    {
        node.requestShutdown(0, Duration.ofSeconds(4));
        if(!node.awaitTermination(Duration.ofSeconds(5)))
            System.err.println("DEMO_CLEANUP_TIMEOUT pid=" + ProcessHandle.current().pid());
    }
}
