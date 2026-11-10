import pt.uminho.esr.ott.bootstrap.BootstrapServer;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.Connection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Operator smoke driver: public API of the verified JAR, real TCP, no JUnit or product changes. */
public class ManualNeighborSmoke {
    static PrintWriter log;
    static int checks;
    static boolean interactive;
    static BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    static void line(String value) {
        String text = Instant.now() + " " + value;
        System.out.println(text); log.println(text); log.flush();
    }
    static void check(String name, boolean ok) {
        if (!ok) { line("CHECK_FAIL " + name); throw new AssertionError(name); }
        checks++; line("CHECK_PASS " + name);
    }
    static void pause(String name) throws IOException {
        line("OPERATOR_STEP " + name);
        if (interactive) {
            line("PRESS_ENTER");
            if (input.readLine() == null) throw new EOFException("Operator input ended before checkpoint");
            line("OPERATOR_CONTINUE " + name);
        }
    }
    static void until(String label, BooleanSupplier condition) throws Exception {
        long start = System.nanoTime();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() - start >= TimeUnit.SECONDS.toNanos(8)) throw new AssertionError("Timed out: " + label);
            Thread.sleep(10);
        }
    }
    static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    static class Events implements NeighborManager.Observer {
        final ArrayBlockingQueue<NeighborManager.Event> values = new ArrayBlockingQueue<>(256);
        final String node;
        int printed;
        Events(String node) { this.node = node; }
        public void onEvent(NeighborManager.Event event) {
            if (!values.offer(event)) throw new IllegalStateException("Manual event collector exceeded 256");
        }
        boolean received(UUID request) {
            return values.stream().anyMatch(e -> e instanceof NeighborManager.Received r && r.message().requestId().equals(request));
        }
        NeighborManager.Received message(UUID request) {
            return values.stream().filter(e -> e instanceof NeighborManager.Received r && r.message().requestId().equals(request))
                    .map(e -> (NeighborManager.Received)e).findFirst().orElseThrow();
        }
        boolean down(Neighbor.Session session) {
            return values.stream().anyMatch(e -> e instanceof NeighborManager.NeighborDown d && d.session().equals(session));
        }
        void printNew() {
            var snapshot = List.copyOf(values);
            for (int i = printed; i < snapshot.size(); i++) line("EVENT " + node + " " + snapshot.get(i));
            printed = snapshot.size();
        }
    }
    static void profiles(Path out, int[] ports) throws IOException {
        String common = "bind.host=127.0.0.1\nadvertised.host=127.0.0.1\n"
                + "control.connect.timeout.ms=1000\ncontrol.shutdown.timeout.ms=2000\n"
                + "control.max.connections=8\ncontrol.queue.capacity=16\nbootstrap.query.timeout.ms=1500\n";
        String overlay = "overlay.hello.timeout.ms=1500\noverlay.retry.initial.ms=100\n"
                + "overlay.retry.max.ms=500\noverlay.retry.jitter.percent=0\nnode.events.capacity=256\n";
        Files.writeString(out.resolve("bootstrap.properties"), "node.id=BOOT\nnode.role=BOOTSTRAP\ncontrol.port=" + ports[0]
                + "\ntopology.file=topology.properties\n" + common);
        var topology = new StringBuilder("node.ids=N0,N1,N2\n");
        for (int i = 0; i < 3; i++) {
            String id = "N" + i;
            String neighbors = String.join(",", List.of("N0", "N1", "N2").stream().filter(n -> !n.equals(id)).toList());
            topology.append("node.").append(id).append(".host=127.0.0.1\nnode.").append(id).append(".controlPort=").append(ports[i+1])
                    .append("\nnode.").append(id).append(".dataPort=").append(ports[i+4]).append("\nnode.").append(id).append(".role=RELAY\nnode.")
                    .append(id).append(".neighbors=").append(neighbors).append('\n');
            Files.writeString(out.resolve(id + ".properties"), "node.id=" + id + "\nnode.role=RELAY\ncontrol.port=" + ports[i+1]
                    + "\ndata.port=" + ports[i+4] + "\nbootstrap.host=127.0.0.1\nbootstrap.port=" + ports[0] + "\n" + common + overlay);
        }
        Files.writeString(out.resolve("topology.properties"), topology);
    }
    static boolean graph(List<NeighborManager> managers) {
        for (int i = 0; i < 3; i++) {
            var snapshot = managers.get(i).snapshot();
            var expected = new HashSet<>(List.of("N0", "N1", "N2")); expected.remove("N" + i);
            if (!snapshot.keySet().equals(expected)) return false;
        }
        return true;
    }
    static void rawHello(NodeConfiguration target, ControlMessage.Endpoint endpoint, ErrorCode expected) throws Exception {
        try (var socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", target.controlPort()), 1500); socket.setSoTimeout(2000);
            UUID request = UUID.randomUUID();
            MessageCodec.write(new ControlMessage(MessageType.HELLO, request, endpoint.nodeId(), endpoint.instanceId(),
                    new ControlMessage.EndpointPayload(endpoint)), socket.getOutputStream()); socket.getOutputStream().flush();
            var reply = MessageCodec.read(socket.getInputStream()).orElseThrow();
            check("raw_hello_reply_" + expected, reply.type() == MessageType.ERROR && reply.requestId().equals(request)
                    && ((ControlMessage.ErrorPayload)reply.payload()).code() == expected);
            check("raw_hello_eof_" + expected, MessageCodec.read(socket.getInputStream()).isEmpty());
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length < 3) throw new IllegalArgumentException("JAR OUTPUT_DIR OPERATOR [--interactive]");
        Path jar = Path.of(args[0]).toRealPath(); Path out = Path.of(args[1]).toAbsolutePath();
        String operator = args[2]; interactive = args.length == 4 && args[3].equals("--interactive");
        Files.createDirectories(out);
        if (Files.exists(out.resolve("manual.log"))) throw new IllegalArgumentException("Use a new output directory; never overwrite manual evidence");
        log = new PrintWriter(Files.newBufferedWriter(out.resolve("manual.log"), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW));
        var managers = new ArrayList<NeighborManager>(); var events = new ArrayList<Events>();
        BootstrapServer bootstrap = null;
        try {
            line("OPERATOR=" + operator + " MODE=" + (interactive ? "interactive" : "scripted"));
            line("SCOPE=embedded services, real loopback TCP; no GUI/CORE/CLI/video claim");
            line("JAR=" + jar + " SHA256=" + hash(jar)); line("JAVA=" + System.getProperty("java.version"));
            check("java21", Runtime.version().feature() == 21);
            check("operator_present", !operator.isBlank());
            check("classes_loaded_from_verified_jar", Path.of(NeighborManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath().equals(jar));
            int[] ports = new int[7]; var reservations = new ArrayList<ServerSocket>();
            try {
                for (int i = 0; i < ports.length; i++) {
                    var socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")); reservations.add(socket); ports[i] = socket.getLocalPort();
                }
            } finally { for (var socket : reservations) socket.close(); }
            profiles(out, ports); line("PORTS=" + Arrays.toString(ports) + " JITTER_PERCENT=0 SEED=not_used");
            var paths = List.of(out.resolve("bootstrap.properties"), out.resolve("N0.properties"), out.resolve("N1.properties"), out.resolve("N2.properties"));
            var loaded = ConfigurationLoader.loadDeployment(paths);
            check("deployment_four_valid_profiles", loaded.size() == 4);
            var configs = loaded.stream().filter(c -> c.role() != NodeRole.BOOTSTRAP).toList();
            var bootConfig = loaded.stream().filter(c -> c.role() == NodeRole.BOOTSTRAP).findFirst().orElseThrow();
            for (var path : paths) line("CONFIG_SHA256 " + path.getFileName() + " " + hash(path));
            line("CONFIG_SHA256 topology.properties " + hash(out.resolve("topology.properties")));
            var instances = new ArrayList<UUID>();
            for (var config : configs) {
                var collector = new Events(config.nodeId()); events.add(collector); UUID instance = UUID.randomUUID(); instances.add(instance);
                managers.add(new NeighborManager(config, instance, collector)); line("INSTANCE " + config.nodeId() + "=" + instance);
            }
            check("no_active_before_start", managers.stream().allMatch(m -> m.snapshot().isEmpty()));
            for (var config : configs) try (var socket = new ServerSocket(config.controlPort(), 1, InetAddress.getByName("127.0.0.1"))) {
                check("constructor_did_not_bind_" + config.nodeId(), socket.isBound());
            }
            pause("1_start_bootstrap_and_nodes_N2_N0_N1");
            bootstrap = new BootstrapServer(bootConfig);
            for (int i : new int[]{2,0,1}) managers.get(i).start().toCompletableFuture().get(5, TimeUnit.SECONDS);
            until("three-node graph", () -> graph(managers));
            check("configured_graph_converged", graph(managers));
            for (int i = 0; i < 3; i++) {
                var snapshot = managers.get(i).snapshot();
                check("degree_and_unique_connections_N" + i, snapshot.size() == 2 && snapshot.values().stream().map(Neighbor.Session::connectionId).distinct().count() == 2);
                check("nonzero_instances_and_authorized_udp_N" + i, snapshot.values().stream().allMatch(s -> !s.instanceId().equals(new UUID(0,0))
                        && s.endpoint().instanceId().equals(s.instanceId()) && s.endpoint().dataPort() > 0));
                check("configured_instances_stay_zero_N" + i, managers.get(i).diagnostics().values().stream().allMatch(n -> n.configured().instanceId().equals(new UUID(0,0))));
                line("SNAPSHOT N" + i + " " + snapshot); events.get(i).printNew();
            }
            var a = managers.get(0); var n1 = managers.get(1);
            var initial = a.snapshot().get("N1"); var n1Session = n1.snapshot().get("N0");
            var message = new ControlMessage(MessageType.ERROR, UUID.randomUUID(), "N0", instances.get(0), new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE, 255, "manual-no-route"));
            check("error_send_accepted_means_queued", a.send(initial, message).toCompletableFuture().get(2, TimeUnit.SECONDS) == Connection.SendResult.ACCEPTED);
            until("ERROR delivery to N1", () -> events.get(1).received(message.requestId()));
            var received = events.get(1).message(message.requestId());
            check("active_error_delivered_intact", received.message().equals(message) && received.session().equals(n1Session));
            Thread.sleep(200); // Explicit bounded observation window, not a latency target.
            check("no_error_reply_in_200ms_window", !events.get(0).received(message.requestId()));
            check("error_did_not_remove_session", a.snapshot().get("N1").equals(initial) && n1.snapshot().get("N0").equals(n1Session));
            events.get(1).printNew();
            rawHello(configs.get(2), new ControlMessage.Endpoint("UNKNOWN", UUID.randomUUID(), "127.0.0.1", 18002, 18003, NodeRole.RELAY), ErrorCode.UNAUTHORIZED_NEIGHBOR);
            var n0 = configs.get(0);
            rawHello(configs.get(2), new ControlMessage.Endpoint("N0", UUID.randomUUID(), n0.advertisedHost(), n0.controlPort(), n0.dataPort(), n0.role()), ErrorCode.DUPLICATE_ID);
            check("negative_handshakes_preserve_graph", graph(managers));
            pause("2_close_bootstrap_and_disconnect_current_N0_N1_session");
            bootstrap.close(); bootstrap = null;
            boolean refused = false;
            try (var probe = new Socket()) { probe.connect(new InetSocketAddress("127.0.0.1", ports[0]), 1000); }
            catch (ConnectException expected) { refused = true; }
            check("bootstrap_closed_and_connection_refused", refused);
            check("disconnect_current_session_true", a.disconnect(initial).toCompletableFuture().get(2, TimeUnit.SECONDS));
            until("direct reconnect without bootstrap", () -> a.snapshot().containsKey("N1") && !a.snapshot().get("N1").connectionId().equals(initial.connectionId()) && graph(managers));
            var reconnected = a.snapshot().get("N1");
            check("new_connection_same_instance_without_bootstrap", reconnected.instanceId().equals(initial.instanceId()) && !reconnected.connectionId().equals(initial.connectionId()));
            check("down_for_old_session_observed", events.get(0).down(initial));
            check("stale_disconnect_is_false", !a.disconnect(initial).toCompletableFuture().get(2, TimeUnit.SECONDS));
            check("stale_send_is_closed", a.send(initial, message).toCompletableFuture().get(2, TimeUnit.SECONDS) == Connection.SendResult.CLOSED);
            check("stale_commands_preserve_new_session", reconnected.equals(a.snapshot().get("N1")));
            for (var collector : events) collector.printNew();
            pause("3_restart_N1_with_new_instance_restore_bootstrap_for_its_discovery");
            n1.close(); until("old N1 removed", () -> !a.snapshot().containsKey("N1") && !managers.get(2).snapshot().containsKey("N1"));
            check("surviving_N0_N2_edge_preserved", a.snapshot().containsKey("N2") && managers.get(2).snapshot().containsKey("N0"));
            UUID freshInstance = UUID.randomUUID(); line("RESTART_INSTANCE N1=" + freshInstance);
            var freshEvents = new Events("N1-restarted"); events.add(freshEvents);
            var fresh = new NeighborManager(configs.get(1), freshInstance, freshEvents); managers.set(1, fresh);
            var discovery = fresh.start(); Thread.sleep(200);
            check("new_process_has_no_discovery_without_bootstrap_in_200ms_window", !discovery.toCompletableFuture().isDone() && fresh.snapshot().isEmpty());
            bootstrap = new BootstrapServer(bootConfig); discovery.toCompletableFuture().get(5, TimeUnit.SECONDS);
            until("restart converges", () -> graph(managers) && a.snapshot().get("N1").instanceId().equals(freshInstance));
            check("restart_confirms_new_instance", !freshInstance.equals(reconnected.instanceId()) && a.snapshot().get("N1").instanceId().equals(freshInstance));
            check("old_instance_disconnect_is_false", !a.disconnect(reconnected).toCompletableFuture().get(2, TimeUnit.SECONDS));
            check("restart_graph_still_complete", graph(managers));
            for (var collector : events) collector.printNew();
            pause("4_shutdown_and_verify_awaits_and_tcp_port_reuse");
            for (var manager : managers) manager.requestClose();
            for (int i = 0; i < managers.size(); i++) {
                check("all_manager_resources_terminated_N" + i, managers.get(i).awaitTermination(Duration.ofSeconds(3)));
                check("snapshot_empty_after_close_N" + i, managers.get(i).snapshot().isEmpty());
                managers.get(i).close();
            }
            bootstrap.close(); bootstrap = null;
            for (int i = 0; i < 4; i++) try (var socket = new ServerSocket(ports[i], 1, InetAddress.getByName("127.0.0.1"))) {
                check("tcp_port_reusable_" + i, socket.isBound());
            }
            line("MANUAL_PASS CHECKS=" + checks);
        } catch (Throwable error) {
            line("MANUAL_FAIL " + error); error.printStackTrace(log); log.flush(); throw error;
        } finally {
            try {
                for (var manager : managers) manager.requestClose();
                for (var manager : managers) if (!manager.awaitTermination(Duration.ofSeconds(3))) throw new IllegalStateException("Manual cleanup: manager resources pending");
            } finally {
                if (bootstrap != null) bootstrap.close();
                line("CLEANUP_COMPLETE"); log.close();
            }
        }
    }
}
