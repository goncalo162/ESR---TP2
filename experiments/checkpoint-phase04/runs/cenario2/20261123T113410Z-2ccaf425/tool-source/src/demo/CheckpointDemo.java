package demo;

import pt.uminho.esr.ott.protocol.ControlMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/** Checkpoint controller. Business decisions remain entirely in the accepted JAR. */
public final class CheckpointDemo
{
    //* === Atributos e Variáveis === *
    private static final String NODE_MAIN = "pt.uminho.esr.ott.overlay.DemoNode";
    private static final String APP_MAIN = "pt.uminho.esr.ott.app.OttApplication";
    private static final UUID STREAM_A = UUID.fromString("00000000-0000-4000-8000-00000000000a");
    private static final UUID STREAM_B = UUID.fromString("00000000-0000-4000-8000-00000000000b");
    private static final UUID TOKEN_A = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID TOKEN_B = UUID.fromString("00000000-0000-4000-8000-000000000002");
    private static final UUID TOKEN_QUALITY = UUID.fromString("00000000-0000-4000-8000-000000000003");
    private final Path root;
    private final Path jar;
    private final Path run;
    private final Properties scenario = new Properties();
    private final Map<String, Properties> profiles = new LinkedHashMap<>();
    private final Map<String, Child> children = new java.util.concurrent.ConcurrentSkipListMap<>();
    private final List<Process> owned = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<DelayProxy> proxies = new ArrayList<>();
    private final Map<String, Integer> delays = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> views = new LinkedHashMap<>();
    private final List<Map<String, Object>> checks = new ArrayList<>();
    private final Map<String, Long> messageCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> launches = new LinkedHashMap<>();
    private final String name;
    private final int portBase;
    private Process bootstrap;
    private BufferedWriter bootInput;
    private BufferedWriter wireLog;
    private int stage;
    private final Object lifecycle = new Object();
    private volatile boolean cleaned;
    private boolean wireClosed;



    //* === Construtores === *

    private CheckpointDemo(String[] args) throws Exception
    {
        root = Path.of(args[0]);
        jar = Path.of(args[1]).toAbsolutePath();
        name = args[2];
        if(!List.of("cenario1", "cenario2", "cenario3").contains(name))
            throw new IllegalArgumentException("Use cenario1|cenario2|cenario3 [--auto] [--port-base N]");
        int foundPort = 0;
        for(int i = 3; i < args.length; i++)
        {
            if(args[i].equals("--port-base"))
                foundPort = Integer.parseInt(args[++i]);
            else if(!args[i].equals("--auto"))
                throw new IllegalArgumentException("Unknown option " + args[i]);
        }
        if(foundPort < 0 || foundPort > 65000)
            throw new IllegalArgumentException("Port base must be 0..65000");
        portBase = foundPort;
        String id = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(Instant.now())
                + "-" + UUID.randomUUID().toString().substring(0, 8);
        run = root.resolve("experiments/checkpoint-phase04/runs/" + name + "/" + id);
        Files.createDirectories(run.resolve("config"));
        Files.createDirectories(run.resolve("logs"));
        Files.createDirectories(run.resolve("snapshots"));
        try(var input = Files.newInputStream(root.resolve("demo/config/" + name + ".properties")))
        {
            scenario.load(input);
        }
        for(String key : scenario.stringPropertyNames())
        {
            if(key.startsWith("delay."))
            {
                String[] edge = key.substring(6).split("-");
                delays.put(DelayProxy.edge(edge[0], edge[1]), Integer.parseInt(scenario.getProperty(key)));
            }
        }
        wireLog = Files.newBufferedWriter(run.resolve("wire.jsonl"));
        writeManifest(args);
        System.out.println("RUN " + run);
        System.out.println("DASHBOARD " + run.resolve("dashboard.html").toUri());
    }



    //* === Métodos Públicos === *

    public static void main(String[] args) throws Exception
    {
        var demo = new CheckpointDemo(args);
        Thread hook = new Thread(demo::cleanup, "demo-controller-cleanup");
        Runtime.getRuntime().addShutdownHook(hook);
        boolean success = false;
        try
        {
            demo.start();
            if(Arrays.asList(args).contains("--auto"))
            {
                while(demo.stage < 6)
                {
                    demo.next();
                }
            }else{
                demo.interact();
            }
            success = demo.stage == 6;
        }catch(Throwable failure){
            demo.checks.add(Map.of("check", "run-completed", "passed", false, "detail", failure.toString()));
            System.err.println("DEMO_FAIL " + failure);
            failure.printStackTrace(System.err);
        }finally{
            demo.cleanup();
            Runtime.getRuntime().removeShutdownHook(hook);
            success &= demo.checks.stream().allMatch(c -> Boolean.TRUE.equals(c.get("passed")));
            success = demo.finish(success);
        }
        System.exit(success ? 0 : 1);
    }



    //* === Configuração === *

    private List<String> csv(String key)
    {
        return Arrays.asList(scenario.getProperty(key).split(","));
    }


    private String source()
    {
        return csv("servers").getFirst();
    }


    private String client(int index)
    {
        return csv("clients").get(index);
    }


    private Path profile(String node)
    {
        return run.resolve("config/" + node + ".properties");
    }


    private void configure() throws Exception
    {
        var graph = new LinkedHashMap<String, List<String>>();
        for(String node : csv("nodes"))
        {
            graph.put(node, new ArrayList<>());
        }
        for(String edge : csv("edges"))
        {
            String[] pair = edge.split("-");
            graph.get(pair[0]).add(pair[1]);
            graph.get(pair[1]).add(pair[0]);
        }
        var topology = new Properties();
        topology.setProperty("node.ids", scenario.getProperty("nodes"));
        int bootPort = freePort("127.0.0.200", 0);
        var boot = new Properties();
        boot.setProperty("node.id", "BOOT");
        boot.setProperty("node.role", "BOOTSTRAP");
        boot.setProperty("bind.host", "127.0.0.200");
        boot.setProperty("advertised.host", "127.0.0.200");
        boot.setProperty("control.port", Integer.toString(bootPort));
        boot.setProperty("topology.file", "topology.properties");
        profiles.put("BOOT", boot);
        int index = 1;
        for(String node : csv("nodes"))
        {
            String host = "127.0.0." + index;
            String bind = "127.0.1." + index;
            String role = csv("servers").contains(node) ? "SERVER" : csv("clients").contains(node) ? "CLIENT" : "RELAY";
            int port = freePort(bind, index);
            var p = new Properties();
            p.setProperty("node.id", node);
            p.setProperty("node.role", role);
            p.setProperty("bind.host", bind);
            p.setProperty("advertised.host", host);
            p.setProperty("control.port", Integer.toString(port));
            p.setProperty("data.port", Integer.toString(port + 1));
            p.setProperty("bootstrap.host", "127.0.0.200");
            p.setProperty("bootstrap.port", Integer.toString(bootPort));
            p.setProperty("monitor.probe.interval.ms", "400");
            p.setProperty("monitor.probe.timeout.ms", "1800");
            p.setProperty("monitor.probe.failures", "3");
            p.setProperty("monitor.ewma.alpha", "1.0");
            p.setProperty("routing.announce.interval.ms", "1000");
            p.setProperty("routing.switch.improvement.percent", "20");
            p.setProperty("routing.switch.observations", "3");
            p.setProperty("routing.switch.min.dwell.ms", "5000");
            p.setProperty("subscription.lease.ms", "6000");
            p.setProperty("subscription.renew.ms", "1500");
            if(role.equals("SERVER"))
            {
                p.setProperty("streams.file", "streams.properties");
                p.setProperty("media.base", "media-not-used");
            }
            profiles.put(node, p);
            String prefix = "node." + node + ".";
            topology.setProperty(prefix + "host", host);
            topology.setProperty(prefix + "controlPort", Integer.toString(port));
            topology.setProperty(prefix + "dataPort", Integer.toString(port + 1));
            topology.setProperty(prefix + "role", role);
            topology.setProperty(prefix + "neighbors", String.join(",", graph.get(node)));
            index++;
        }
        var streams = new Properties();
        streams.setProperty("stream.ids", STREAM_A + "," + STREAM_B);
        for(UUID stream : List.of(STREAM_A, STREAM_B))
        {
            String prefix = "stream." + stream + ".";
            streams.setProperty(prefix + "title", "CONTROL-ONLY-" + stream);
            streams.setProperty(prefix + "file", stream + ".Mjpeg");
            streams.setProperty(prefix + "fps", "10");
            streams.setProperty(prefix + "loop", "false");
        }
        store(run.resolve("config/topology.properties"), topology);
        store(run.resolve("config/streams.properties"), streams);
        for(var item : profiles.entrySet())
        {
            store(profile(item.getKey()), item.getValue());
        }
    }


    private int freePort(String host, int index) throws Exception
    {
        try(var socket = new ServerSocket())
        {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(host, portBase == 0 ? 0 : portBase + index * 2));
            return socket.getLocalPort();
        }
    }


    private void store(Path path, Properties properties) throws Exception
    {
        try(var writer = Files.newBufferedWriter(path))
        {
            properties.store(writer, "Demo control-only; no media read, UDP socket or player");
        }
    }


    private List<String> javaCommand(String main)
    {
        return new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-cp",
                jar + System.getProperty("path.separator") + root.resolve("demo/.classes"), main));
    }


    private void start() throws Exception
    {
        configure();
        var validate = javaCommand(NODE_MAIN);
        validate.add("validate");
        for(String node : profiles.keySet())
        {
            validate.add(profile(node).toString());
        }
        Process process = spawn(new ProcessBuilder(validate).redirectErrorStream(true).redirectOutput(run.resolve("validation.log").toFile()));
        require(process.waitFor(15, TimeUnit.SECONDS) && process.exitValue() == 0, "configuration-valid", "ConfigurationLoader.loadDeployment");
        for(String node : csv("nodes"))
        {
            var p = profiles.get(node);
            proxies.add(new DelayProxy(node, p.getProperty("advertised.host"), p.getProperty("bind.host"),
                    Integer.parseInt(p.getProperty("control.port")), edge -> delays.getOrDefault(edge, 2), this::wire));
        }
        startBootstrap();
        for(String node : csv("nodes"))
        {
            launch(node);
        }
        await("all-configured-edges-ACTIVE", () -> configuredActive(), 25);
        await("real-RTT-and-catalog", () -> csv("nodes").stream().allMatch(this::routesReady)
                && csv("nodes").stream().allMatch(n -> !map(map(views.get(n).get("metrics")).get("links")).isEmpty()), 20);
        require(messageCounts.keySet().stream().anyMatch(x -> x.startsWith("HELLO|"))
                && messageCounts.keySet().stream().anyMatch(x -> x.startsWith("DISCOVERY_QUERY|"))
                && messageCounts.keySet().stream().anyMatch(x -> x.startsWith("DISCOVERY_SNAPSHOT|")), "TCP-v2-discovery-observed", "Raw decoded frames in wire.jsonl");
        save("00-stable");
        System.out.println("READY. next=guião; snapshot; raw NODE COMMAND; delay A-B MS; kill NODE; restart NODE; quit");
    }


    private void startBootstrap() throws Exception
    {
        var args = javaCommand(APP_MAIN);
        args.addAll(List.of("--role", "BOOTSTRAP", "--config", profile("BOOT").toString()));
        bootstrap = spawn(new ProcessBuilder(args).redirectErrorStream(true)
                .redirectOutput(run.resolve("logs/BOOT-" + launches.merge("BOOT", 1L, Long::sum) + ".log").toFile()));
        bootInput = new BufferedWriter(new OutputStreamWriter(bootstrap.getOutputStream(), StandardCharsets.UTF_8));
        Thread.sleep(600);
        require(bootstrap.isAlive(), "bootstrap-running", "pid=" + bootstrap.pid());
    }


    private void launch(String node) throws Exception
    {
        long generation = launches.merge(node, 1L, Long::sum);
        children.put(node, new Child(node, generation));
    }


    private boolean configuredActive()
    {
        for(String node : csv("nodes"))
        {
            Map<String, Object> neighbors = map(views.get(node).get("neighbors"));
            for(String edge : csv("edges"))
            {
                String[] pair = edge.split("-");
                String peer = pair[0].equals(node) ? pair[1] : pair[1].equals(node) ? pair[0] : null;
                if(peer != null && !"ACTIVE".equals(map(neighbors.get(peer)).get("state")))
                    return false;
            }
        }
        return true;
    }



    //* === Guião === *

    private void next() throws Exception
    {
        switch(stage)
        {
            case 0: activate(); break;
            case 1: renew(); break;
            case 2: quality(); break;
            case 3: failure(); break;
            case 4: restart(); break;
            case 5: prune(); break;
            default: System.out.println("Guião concluído; quit para limpar."); return;
        }
        stage++;
        save("0" + stage + "-step");
    }


    private void activate() throws Exception
    {
        cliCheck();
        await("routes-ready-before-subscription", () -> csv("nodes").stream().allMatch(this::routesReady), 20);
        for(String node : csv("clients"))
        {
            children.get(node).request("subscribe " + STREAM_A + " " + TOKEN_A);
        }
        children.get(client(1)).request("subscribe " + STREAM_B + " " + TOKEN_B);
        String qualityNode = scenario.getProperty("quality.node");
        if(!qualityNode.isBlank())
            children.get(qualityNode).request("subscribe " + STREAM_A + " " + TOKEN_QUALITY);
        await("clients-ACK-SUCCESS", () -> csv("clients").stream().allMatch(n -> ticket(n, TOKEN_A))
                && ticket(client(1), TOKEN_B), 15);
        Map<String, Object> sourceTree = forward(source(), STREAM_A);
        require(sourceTree != null && map(sourceTree.get("downstreams")).size() == 1,
                "one-shared-trunk", "V-018/C2: source has one confirmed downstream for multiple clients");
        String trunk = map(sourceTree.get("downstreams")).keySet().iterator().next();
        require(messageCounts.getOrDefault("ACTIVATE|" + trunk + "|" + STREAM_A, 0L) == 1,
                "one-initial-ACTIVATE-on-shared-trunk", "V-018/C2: exactly one initial ACTIVATE for stream A from " + trunk);
        require(forward(client(0), STREAM_B) == null && forward(client(1), STREAM_B) != null,
                "two-stream-intent-isolation", "V-018/C1: C1 only A; C2 A+B; catalog selection does not authorize forwarding");
        awaitTrace(client(0), "trace-before.json", "trace-before-SUCCESS", 15);
    }


    /**
     * Positive multi-hop trace is a deadline-based observation after ACK: the
     * terminal answer of any individual attempt (including NO_ROUTE while
     * announcements settle) is preserved in the evidence file, but the oracle
     * requires one real SUCCESS within the window, not a single lucky attempt.
     */
    private void awaitTrace(String node, String evidenceFile, String label, int seconds) throws Exception
    {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        Object trace = null;
        do
        {
            refresh();
            trace = children.get(node).request("trace " + STREAM_A);
            Files.writeString(run.resolve(evidenceFile), Json.write(trace));
            if("SUCCESS".equals(map(map(trace).get("trace")).get("state")))
            {
                require(true, label, trace.toString());
                return;
            }
            Thread.sleep(200);
        }while(System.nanoTime() < deadline);
        save("FAILED-" + label);
        require(false, label, String.valueOf(trace));
    }


    private void renew() throws Exception
    {
        long before = count("SUBSCRIPTION_RENEW", source());
        hold(7);
        require(ticket(client(0), TOKEN_A) && ticket(client(1), TOKEN_A) && ticket(client(1), TOKEN_B),
                "lease-renewed-past-original-6s", "All admitted intents remain confirmed after original grant duration");
        require(count("SUBSCRIPTION_RENEW", source()) > before, "renewal-on-real-wire", "SUBSCRIPTION_RENEW (type 14) and ACTIVATE_ACK (type 12) in wire.jsonl");
    }


    private void quality() throws Exception
    {
        String node = scenario.getProperty("quality.node");
        if(node.isBlank())
        {
            require(true, "quality-mapped-to-cenario2", "Scenario 1 has one configured path; RTT switch demonstrated in scenario 2 and V-017/S01-S18");
            return;
        }
        String edge = scenario.getProperty("quality.edge");
        String[] pair = edge.split("-");
        String key = DelayProxy.edge(pair[0], pair[1]);
        String previous = pair[0].equals(node) ? pair[1] : pair[0];
        String replacement = scenario.getProperty("quality.alternative");
        await("quality-baseline-confirmed", () -> previous.equals(upstream(node, STREAM_A)), 20);
        long successes = switchCount(node);
        int originalDelay = delays.getOrDefault(key, 2);
        setDelay(key, 120);
        holdMillis(150);
        setDelay(key, originalDelay);
        hold(2);
        require(switchCount(node) == successes && previous.equals(upstream(node, STREAM_A)),
                "transient-delay-no-oscillation", "150ms pulse; policy P=20%, K=3 distinct real samples, D=5s unchanged");
        setDelay(key, 120);
        await("real-RTT-confirmed-switch", () -> replacement.equals(upstream(node, STREAM_A)) && switchCount(node) > successes, 25);
        var events = list(views.get(node).get("events"));
        boolean atomic = events.stream().map(Json::object).filter(e -> e.get("kind").equals("switch"))
                .map(e -> map(e.get("value"))).anyMatch(e ->
                {
                    var change = map(e.get("change"));
                    return "SUCCESS".equals(change.get("phase")) && number(e.get("publishedRevision")) == number(change.get("replacementRevision"));
                });
        require(atomic, "success-after-confirmation-publication", "Observer captured replacementRevision already published at SUCCESS");
        long stable = switchCount(node);
        hold(3);
        require(switchCount(node) == stable && replacement.equals(upstream(node, STREAM_A)),
                "stable-after-quality-switch", "No additional successful switch in measured stability window");
    }


    private void failure() throws Exception
    {
        String victim = scenario.getProperty("kill");
        String observer = scenario.getProperty("observer");
        long pid = children.get(observer).process.pid();
        Object beforeCandidates = views.get(observer).get("candidates");
        require(!list(beforeCandidates).isEmpty(), "candidate-cache-before-failure", beforeCandidates.toString());
        stopBootstrap();
        children.get(victim).kill();
        await("NeighborDown-after-real-JVM-kill", () -> list(views.get(observer).get("events")).stream()
                .anyMatch(e -> e.toString().contains("NeighborDown") && e.toString().contains(victim)), 12);
        await("automatic-tree-recovery-with-bootstrap-stopped", () -> ticket(observer, TOKEN_A)
                && forward(observer, STREAM_A) != null && !list(forward(observer, STREAM_A).get("path")).contains(victim), 30);
        require(!bootstrap.isAlive() && children.get(observer).process.pid() == pid,
                "same-client-intent-and-no-bootstrap", "No new subscription, no client restart; bootstrap process is stopped");
        awaitTrace(observer, "trace-after.json", "trace-after-SUCCESS", 15);
        save("failure-no-bootstrap");
    }


    private void restart() throws Exception
    {
        startBootstrap();
        launch(scenario.getProperty("kill"));
        await("restarted-node-configured-edges-ACTIVE", this::configuredActive, 25);
        require(true, "restart-initial-bootstrap-distinguished", "Only the new JVM needs initial discovery; survivor repair was observed while BOOT was stopped");
    }


    private void prune() throws Exception
    {
        for(String node : csv("clients"))
        {
            if(!node.equals(client(1)))
                children.get(node).request("cancel " + TOKEN_A);
        }
        String qualityNode = scenario.getProperty("quality.node");
        if(!qualityNode.isBlank())
            children.get(qualityNode).request("cancel " + TOKEN_QUALITY);
        hold(1);
        require(ticket(client(1), TOKEN_A) && ticket(client(1), TOKEN_B), "one-client-exits-other-preserved", "V-018/C2 cancel A in C1 preserves C2 A+B");
        children.get(client(1)).request("cancel " + TOKEN_A);
        await("last-A-consumer-prunes-A-only", () -> forward(source(), STREAM_A) == null && forward(source(), STREAM_B) != null, 15);
        children.get(client(1)).kill();
        await("abandoned-B-demand-removed-after-client-death", () -> forward(source(), STREAM_B) == null, 15);
        require(messageCounts.keySet().stream().anyMatch(k -> k.startsWith("DEACTIVATE|")), "prune-on-real-wire", "DEACTIVATE after last explicit A consumer; B removed after real client death");
    }


    private void cliCheck() throws Exception
    {
        String node = client(1);
        children.get(node).close();
        var args = javaCommand(APP_MAIN);
        args.addAll(List.of("--role", "CLIENT", "--config", profile(node).toString()));
        Path log = run.resolve("logs/production-cli.log");
        Process cli = spawn(new ProcessBuilder(args).redirectErrorStream(true).redirectOutput(log.toFile()));
        try(var input = new BufferedWriter(new OutputStreamWriter(cli.getOutputStream(), StandardCharsets.UTF_8)))
        {
            long readyDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
            while(true)
            {
                input.write("neighbors\nrecovery\nroutes\ncatalog\nmetrics\n");
                input.flush();
                Thread.sleep(500);
                String observed = Files.readString(log);
                boolean selected = observed.lines().anyMatch(line -> line.startsWith("ROUTE stream=" + STREAM_A)
                        && !line.contains("costMicros=UNAVAILABLE"));
                if(selected && observed.contains("CATALOG_STREAM") && observed.contains("METRIC "))
                    break;
                if(System.nanoTime() >= readyDeadline)
                    throw new IllegalStateException("CLI route/catalog/metrics readiness not observed");
            }
            input.write("trace " + STREAM_A + "\nplay " + STREAM_A + "\n");
            input.flush();
            long traceDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
            while(!Files.readString(log).contains("TRACE_RESULT") && !Files.readString(log).contains("TRACE_ERROR")
                    && System.nanoTime() < traceDeadline)
            {
                Thread.sleep(100);
            }
            input.write("quit\n");
            input.flush();
            require(cli.waitFor(8, TimeUnit.SECONDS) && cli.exitValue() == 0, "production-CLI-exit0", "OttApplication.main on the same JAR and profile");
        }
        String output = Files.readString(log);
        require(output.contains("state=ACTIVE") && output.contains("CATALOG_STREAM") && output.contains("METRIC ")
                && output.contains("TRACE_STARTED") && (output.contains("TRACE_RESULT") || output.contains("TRACE_ERROR"))
                && output.contains("NOT_IMPLEMENTED play"),
                "production-CLI-diagnostics-and-play", "See logs/production-cli.log; play deliberately NOT_IMPLEMENTED");
        require(true, "production-CLI-trace-terminal-recorded", output.contains("TRACE_RESULT")
                ? "SUCCESS; multi-hop positive additionally required after ACK" : "TRACE_ERROR preserved; positive multi-hop trace is separately required after ACK, not inferred from this command");
        launch(node);
        await("CLI-check-returned-to-tool-adapter", this::configuredActive, 15);
    }



    //* === Observação e Evidência === *

    private void refresh() throws Exception
    {
        for(var item : children.entrySet())
        {
            if(item.getValue().process.isAlive())
            {
                Map<String, Object> view = map(item.getValue().request("snapshot"));
                requireNoFatal(item.getKey(), view);
                views.put(item.getKey(), view);
            }
        }
        var state = new LinkedHashMap<String, Object>();
        state.put("at", Instant.now().toString());
        state.put("scenario", name);
        state.put("stage", stage);
        state.put("nodes", views);
        state.put("alive", children.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> e.getValue().process.isAlive())));
        state.put("edges", csv("edges"));
        state.put("delays", delays);
        state.put("checks", checks);
        atomic(run.resolve("latest.json"), Json.write(state));
        String html = Files.readString(root.resolve("demo/dashboard.html"));
        html = html.replace("/*SNAPSHOT*/null", Json.write(state).replace("<", "\\u003c"));
        atomic(run.resolve("dashboard.html"), html);
    }


    private void requireNoFatal(String node, Map<String, Object> view)
    {
        if(view.get("failure") != null || list(view.get("events")).stream().map(Json::object).anyMatch(e -> "fatal".equals(e.get("kind"))))
            throw new IllegalStateException("Production fatal in " + node + ": " + view.get("failure"));
    }


    private void await(String label, BooleanSupplier condition, int seconds) throws Exception
    {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        do
        {
            refresh();
            if(condition.getAsBoolean())
            {
                require(true, label, "Observed actual snapshots");
                return;
            }
            Thread.sleep(200);
        }while(System.nanoTime() < deadline);
        save("FAILED-" + label);
        require(false, label, "Condition not observed within " + seconds + "s");
    }


    private void hold(int seconds) throws Exception
    {
        holdMillis(seconds * 1000);
    }


    private void holdMillis(int millis) throws Exception
    {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis);
        while(System.nanoTime() < deadline)
        {
            refresh();
            Thread.sleep(Math.min(100, millis));
        }
    }


    private void require(boolean condition, String label, String detail)
    {
        checks.add(Map.of("at", Instant.now().toString(), "check", label, "passed", condition, "detail", detail));
        System.out.println((condition ? "CHECK_PASS " : "CHECK_FAIL ") + label + " " + detail);
        if(!condition)
            throw new IllegalStateException(label);
    }


    private void save(String label) throws Exception
    {
        refresh();
        Files.copy(run.resolve("latest.json"), run.resolve("snapshots/" + label + ".json"), StandardCopyOption.REPLACE_EXISTING);
    }


    private void setDelay(String edge, int millis) throws Exception
    {
        if(millis < 0 || millis > 250)
            throw new IllegalArgumentException("Demo delay range 0..250ms per direction");
        delays.put(edge, millis);
        Files.writeString(run.resolve("actions.jsonl"), Json.write(Map.of("at", Instant.now().toString(), "action", "delay", "edge", edge, "millisecondsPerDirection", millis)) + "\n",
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        System.out.println("DELAY " + edge + " " + millis + "ms/direction");
    }


    private synchronized void wire(ControlMessage message)
    {
        if(wireClosed)
            return;
        String stream = "-";
        String destination = "-";
        Object payload = message.payload();
        if(payload instanceof ControlMessage.Activation x)
            stream = x.streamId().toString();
        if(payload instanceof ControlMessage.ActivationAck x)
            stream = x.streamId().toString();
        if(payload instanceof ControlMessage.Deactivation x)
            stream = x.streamId().toString();
        messageCounts.merge(message.type() + "|" + message.senderId() + "|" + stream, 1L, Long::sum);
        try
        {
            wireLog.write(Json.write(Map.of("at", Instant.now().toString(), "version", message.version(), "type", message.type(),
                    "sender", message.senderId(), "instance", message.senderInstance(), "requestId", message.requestId(), "payload", payload)) + "\n");
            wireLog.flush();
        }catch(Exception e){
            throw new IllegalStateException("Cannot preserve wire evidence", e);
        }
    }


    private long count(String type, String sourceNode)
    {
        return messageCounts.entrySet().stream().filter(e -> e.getKey().startsWith(type + "|") && !e.getKey().startsWith(type + "|" + sourceNode + "|"))
                .mapToLong(Map.Entry::getValue).sum();
    }


    private long switchCount(String node)
    {
        return list(views.get(node).get("events")).stream().map(Json::object)
                .filter(e -> "switch".equals(e.get("kind")))
                .filter(e -> "SUCCESS".equals(map(map(e.get("value")).get("change")).get("phase"))).count();
    }


    private Map<String, Object> route(String node, UUID stream)
    {
        return optionalMap(map(map(views.get(node).get("routing")).get("routes")).get(stream.toString()));
    }


    private Map<String, Object> forward(String node, UUID stream)
    {
        return optionalMap(map(map(views.get(node).get("routing")).get("forwarding")).get(stream.toString()));
    }


    private String upstream(String node, UUID stream)
    {
        var tree = forward(node, stream);
        return tree == null ? "NONE" : String.valueOf(map(tree.get("upstream")).get("nodeId"));
    }


    private boolean ticket(String node, UUID token)
    {
        var outcome = map(map(views.get(node).get("tickets")).get(token.toString()));
        if(outcome.get("streamId") == null)
            return false;
        var tree = forward(node, UUID.fromString(outcome.get("streamId").toString()));
        return tree != null && list(tree.get("localConsumers")).contains(token.toString());
    }


    private boolean routesReady(String node)
    {
        var catalog = map(map(map(views.get(node).get("routing")).get("catalog")).get("entries"));
        for(UUID stream : List.of(STREAM_A, STREAM_B))
        {
            var row = route(node, stream);
            if(row == null || row.get("selectedMetric") == null || !catalog.containsKey(stream.toString()))
                return false;
        }
        return true;
    }


    private static Map<String, Object> map(Object object)
    {
        return object == null ? Map.of() : Json.object(object);
    }


    private static Map<String, Object> optionalMap(Object object)
    {
        return object == null ? null : Json.object(object);
    }


    @SuppressWarnings("unchecked")
    private static List<Object> list(Object object)
    {
        return object == null ? List.of() : (List<Object>)object;
    }


    private static long number(Object value)
    {
        return ((Number)value).longValue();
    }


    private void atomic(Path path, String text) throws Exception
    {
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temp, text);
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }


    private void writeManifest(String[] args) throws Exception
    {
        var git = new ProcessBuilder("git", "rev-parse", "HEAD").directory(root.toFile()).start();
        String head = new String(git.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();
        git.waitFor();
        var toolHashes = new LinkedHashMap<String, String>();
        try(var files = Files.walk(root.resolve("demo")))
        {
            for(Path file : files.filter(Files::isRegularFile).filter(f -> !f.toString().contains(".classes")).toList())
            {
                String relative = root.resolve("demo").relativize(file).toString();
                Path copy = run.resolve("tool-source/" + relative);
                Files.createDirectories(copy.getParent());
                Files.copy(file, copy);
                toolHashes.put(relative, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))));
            }
        }
        Files.writeString(run.resolve("tool-sha256.json"), Json.write(toolHashes));
        Files.writeString(run.resolve("manifest.json"), Json.write(Map.of("createdAt", Instant.now().toString(), "head", head,
                "jar", jar.toString(), "jarSha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(jar))),
                "java", System.getProperty("java.version"), "arguments", Arrays.asList(args), "operator", "agent-run; not human acceptance",
                "mode", "one real JVM per overlay node; real TCP and NetworkMonitor; no metric injection")));
    }



    //* === Interface === *

    private void interact() throws Exception
    {
        var commands = new ArrayBlockingQueue<String>(32);
        Thread.startVirtualThread(() ->
        {
            try(var input = new BufferedReader(new InputStreamReader(System.in)))
            {
                String line;
                while((line = input.readLine()) != null)
                {
                    if(line.length() <= 4096)
                        commands.put(line);
                }
                commands.put("quit");
            }catch(Exception e){
                commands.offer("quit");
            }
        });
        while(true)
        {
            refresh();
            String command = commands.poll(500, TimeUnit.MILLISECONDS);
            if(command == null)
                continue;
            String[] words = command.strip().split(" +", 3);
            switch(words[0])
            {
                case "next": next(); break;
                case "snapshot": save("manual-" + System.currentTimeMillis()); break;
                case "raw": System.out.println(Json.write(children.get(words[1]).request(words[2]))); break;
                case "delay":
                {
                    String[] edge = words[1].split("-");
                    setDelay(DelayProxy.edge(edge[0], edge[1]), Integer.parseInt(words[2]));
                    break;
                }
                case "kill": children.get(words[1]).kill(); break;
                case "restart": launch(words[1]); break;
                case "quit": return;
                default: System.out.println("next | snapshot | raw NODE COMMAND | delay A-B MS | kill NODE | restart NODE | quit");
            }
        }
    }



    //* === Lifecycle === *

    private void stopBootstrap() throws Exception
    {
        bootInput.write("quit\n");
        bootInput.flush();
        require(bootstrap.waitFor(6, TimeUnit.SECONDS) && bootstrap.exitValue() == 0, "bootstrap-stopped-before-failure", "No process can answer bootstrap queries during recovery");
    }


    private void cleanup()
    {
        synchronized(lifecycle)
        {
            if(cleaned)
                return;
            cleaned = true;
        }
        for(Child child : children.values())
        {
            child.close();
        }
        for(Process process : owned)
        {
            if(process.isAlive())
            {
                process.destroy();
                try
                {
                    if(!process.waitFor(6, TimeUnit.SECONDS))
                        process.destroyForcibly().waitFor(3, TimeUnit.SECONDS);
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }
        }
        for(var proxy : proxies)
        {
            proxy.close();
        }
        try
        {
            synchronized(this)
            {
                wireClosed = true;
                wireLog.close();
            }
        }catch(Exception e){
            checks.add(Map.of("check", "wire-log-close", "passed", false, "detail", e.toString()));
        }
    }


    private Process spawn(ProcessBuilder builder) throws Exception
    {
        synchronized(lifecycle)
        {
            if(cleaned)
                throw new IllegalStateException("Demo is closing");
            Process process = builder.start();
            owned.add(process);
            return process;
        }
    }


    private boolean finish(boolean success) throws Exception
    {
        var processes = new ArrayList<Object>();
        for(Process process : owned)
        {
            processes.add(Map.of("pid", process.pid(), "alive", process.isAlive(), "exit", process.isAlive() ? -999 : process.exitValue()));
            success &= !process.isAlive();
        }
        var ports = new ArrayList<Object>();
        for(var p : profiles.values())
        {
            for(String host : List.of(p.getProperty("bind.host"), p.getProperty("advertised.host")))
            {
                try(var socket = new ServerSocket())
                {
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(host, Integer.parseInt(p.getProperty("control.port"))));
                    ports.add(Map.of("host", host, "port", p.getProperty("control.port"), "reusable", true));
                }catch(Exception e){
                    ports.add(Map.of("host", host, "port", p.getProperty("control.port"), "reusable", false));
                    success = false;
                }
            }
        }
        var result = new LinkedHashMap<String, Object>();
        result.put("finishedAt", Instant.now().toString());
        result.put("verdict", success ? "PASS" : "FAIL");
        result.put("checks", checks);
        result.put("processes", processes);
        result.put("ports", ports);
        result.put("outsideScope", List.of("UDP", "MJPEG", "video", "Swing player", "CORE execution", "article", "external submission"));
        Files.writeString(run.resolve("result.json"), Json.write(result));
        System.out.println("RESULT " + result.get("verdict") + " " + run.resolve("result.json"));
        return success;
    }


    private final class Child
    {
        //* === Atributos e Variáveis === *
        final String id;
        final Process process;
        final BufferedWriter input;
        final ArrayBlockingQueue<String> output = new ArrayBlockingQueue<>(32);



        //* === Construtores === *

        Child(String id, long generation) throws Exception
        {
            this.id = id;
            var args = javaCommand(NODE_MAIN);
            args.addAll(List.of(profile(id).toString(), profiles.get(id).getProperty("node.role")));
            process = spawn(new ProcessBuilder(args).redirectError(run.resolve("logs/" + id + "-" + generation + ".err").toFile()));
            input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            Thread.startVirtualThread(() ->
            {
                try(var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                    var log = Files.newBufferedWriter(run.resolve("logs/" + id + "-" + generation + ".jsonl")))
                {
                    String line;
                    while((line = reader.readLine()) != null)
                    {
                        log.write(line + "\n");
                        log.flush();
                        output.put(line);
                    }
                }catch(Exception e){
                    output.offer(Json.write(Map.of("error", e.toString())));
                }
            });
            String ready = output.poll(20, TimeUnit.SECONDS);
            if(ready == null || !Boolean.TRUE.equals(map(Json.read(ready)).get("ready")))
                throw new IllegalStateException("Node failed to start: " + id + " " + ready);
        }



        //* === Lógica === *

        synchronized Object request(String command) throws Exception
        {
            if(!process.isAlive())
                throw new IllegalStateException("Node not alive: " + id);
            input.write(command + "\n");
            input.flush();
            String response = output.poll(8, TimeUnit.SECONDS);
            if(response == null)
                throw new IllegalStateException("Snapshot timeout: " + id);
            Object parsed = Json.read(response);
            if(map(parsed).containsKey("error"))
                throw new IllegalStateException(id + " " + response);
            return parsed;
        }


        void kill() throws Exception
        {
            long pid = process.pid();
            process.destroyForcibly();
            require(process.waitFor(5, TimeUnit.SECONDS), "SIGKILL-" + id, "Owned JVM pid=" + pid + " exit=" + process.exitValue());
        }


        void close()
        {
            if(!process.isAlive())
                return;
            try
            {
                input.write("quit\n");
                input.flush();
                if(!process.waitFor(7, TimeUnit.SECONDS))
                    process.destroyForcibly().waitFor(3, TimeUnit.SECONDS);
            }catch(Exception e){
                process.destroyForcibly();
            }
        }
    }
}
