import pt.uminho.esr.ott.bootstrap.BootstrapServer;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.NodeRole;
import pt.uminho.esr.ott.protocol.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

/** Manual source-file launcher against the built JAR, outside production and JUnit. */
public class ManualBootstrapCheck {
    private static int checks;
    private static final UUID ZERO = new UUID(0, 0);
    private static final int QUERY_MS = 500;
    private record Exchange(ControlMessage reply, boolean eof) { }
    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++; System.out.println("CHECK_PASS " + label);
    }
    private static String sha(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }
    private static UUID instance() {
        UUID value; do { value = UUID.randomUUID(); } while (ZERO.equals(value)); return value;
    }
    private static ControlMessage register(NodeConfiguration config, String id, UUID execution, boolean wrongPort) {
        var node = config.topology().orElseThrow().nodes().get(id);
        int tcp = wrongPort ? node.controlPort() % 65535 + 1 : node.controlPort();
        return new ControlMessage(MessageType.REGISTER, UUID.randomUUID(), id, execution,
                new ControlMessage.EndpointPayload(new ControlMessage.Endpoint(id, execution,
                        node.host(), tcp, node.dataPort(), node.role())));
    }
    private static Socket peer(InetSocketAddress address) throws IOException {
        var socket = new Socket();
        try { socket.connect(address, 3000); socket.setSoTimeout(5000); return socket; }
        catch (IOException e) { socket.close(); throw e; }
    }
    private static Exchange exchange(InetSocketAddress address, ControlMessage... requests) throws Exception {
        try (var socket = peer(address)) {
            for (var request : requests) MessageCodec.write(request, socket.getOutputStream());
            socket.getOutputStream().flush();
            var reply = MessageCodec.read(socket.getInputStream()).orElseThrow(() -> new AssertionError("EOF before complete response"));
            boolean eof = MessageCodec.read(socket.getInputStream()).isEmpty();
            return new Exchange(reply, eof);
        }
    }
    private static boolean closed(Socket socket) throws IOException {
        try { return socket.getInputStream().read() == -1; }
        catch (SocketException reset) {
            System.out.println("OBSERVED peer_close=" + reset.getClass().getSimpleName()); return true;
        }
    }
    private static void deadline(InetSocketAddress address, boolean partial) throws Exception {
        String kind = partial ? "partial" : "silent";
        try (var socket = peer(address)) {
            long started = System.nanoTime();
            if (partial) { socket.getOutputStream().write(new byte[]{0, 0}); socket.getOutputStream().flush(); }
            socket.setSoTimeout(QUERY_MS / 2);
            boolean waiting = false;
            try { socket.getInputStream().read(); }
            catch (SocketTimeoutException expected) { waiting = true; }
            check(waiting, kind + "_before_deadline_still_open");
            socket.setSoTimeout(5000);
            check(closed(socket), kind + "_closed_at_query_deadline");
            System.out.println("OBSERVED " + kind + "_elapsed_ms=" + Duration.ofNanos(System.nanoTime() - started).toMillis()
                    + " configured_query_ms=" + QUERY_MS + " read_guard_ms=5000");
        }
    }
    private static void run(Path temporary) throws Exception {
        String[] ids = {"S1", "S2", "R1", "C1"};
        NodeRole[] roles = {NodeRole.SERVER, NodeRole.SERVER, NodeRole.RELAY, NodeRole.CLIENT};
        String[] neighbors = {"S2,R1", "S1,R1", "S1,S2,C1", "R1"};
        var reservations = new ArrayList<ServerSocket>(); var ports = new ArrayList<Integer>();
        try {
            for (int i = 0; i < 5; i++) {
                var socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
                reservations.add(socket); ports.add(socket.getLocalPort());
            }
        } finally { for (var socket : reservations) socket.close(); }
        StringBuilder topology = new StringBuilder("node.ids=S1,S2,R1,C1\n");
        for (int i = 0; i < ids.length; i++) {
            topology.append("node.%s.host=127.0.0.1\nnode.%s.controlPort=%d\nnode.%s.dataPort=%d\nnode.%s.role=%s\nnode.%s.neighbors=%s\n"
                    .formatted(ids[i], ids[i], ports.get(i + 1), ids[i], 61001 + i, ids[i], roles[i], ids[i], neighbors[i]));
        }
        Path graph = temporary.resolve("topology.properties"), profile = temporary.resolve("bootstrap.properties");
        Files.writeString(graph, topology);
        Files.writeString(profile, """
                node.id=B
                node.role=BOOTSTRAP
                bind.host=127.0.0.1
                advertised.host=127.0.0.1
                control.port=%d
                topology.file=topology.properties
                bootstrap.query.timeout.ms=500
                control.shutdown.timeout.ms=3000
                control.max.connections=8
                control.queue.capacity=4
                """.formatted(ports.getFirst()));
        var hashes = Map.of(profile, sha(profile), graph, sha(graph));
        System.out.println("TEMP_CONFIG_DIR " + temporary);
        hashes.forEach((file, hash) -> System.out.println("CONFIG_SHA256 " + file.getFileName() + "=" + hash));
        var config = ConfigurationLoader.load(profile, NodeRole.BOOTSTRAP);
        check(config.role() == NodeRole.BOOTSTRAP && config.topology().orElseThrow().nodes().size() == 4, "valid_bootstrap_profile_and_four_nodes");
        check(config.runtime().control().bootstrapQueryTimeout().toMillis() == QUERY_MS
                && config.runtime().control().maxConnections() == 8 && config.runtime().control().queueCapacity() == 4, "validated_control_overrides");
        int refused = 0;
        for (var node : config.topology().orElseThrow().nodes().values()) try (var probe = new Socket()) {
            try { probe.connect(new InetSocketAddress(node.host(), node.controlPort()), 500); }
            catch (ConnectException expected) { refused++; }
        }
        check(refused == 4, "all_four_neighbor_tcp_ports_refuse_connections");
        var before = config.topology().orElseThrow();
        try (var service = new BootstrapServer(config)) {
            var address = service.localAddress();
            check(address.getPort() == config.controlPort(), "bootstrap_bound_to_configured_port");
            var execution = instance(); var request = register(config, "R1", execution, false);
            var valid = exchange(address, request); var reply = valid.reply();
            check(reply.type() == MessageType.REGISTER_OK, "valid_reply_register_ok");
            check(reply.requestId().equals(request.requestId()), "valid_request_id_correlated");
            check(reply.senderId().equals("B") && !ZERO.equals(reply.senderInstance()), "valid_bootstrap_sender_and_execution");
            var list = ((ControlMessage.NeighborList) reply.payload()).neighbors();
            check(list.stream().map(ControlMessage.Endpoint::nodeId).toList().equals(List.of("S1", "S2", "C1")), "exact_configured_neighbor_ids");
            check(list.stream().allMatch(ep -> {
                var expected = before.nodes().get(ep.nodeId());
                return ep.host().equals(expected.host()) && ep.controlPort() == expected.controlPort()
                        && ep.dataPort() == expected.dataPort() && ep.role() == expected.role();
            }), "neighbor_endpoints_and_roles_from_configuration");
            check(list.stream().allMatch(ep -> ZERO.equals(ep.instanceId())), "neighbor_instances_are_zero");
            check(valid.eof(), "complete_valid_response_before_eof");
            var nextExecution = instance();
            var repeated = exchange(address, register(config, "R1", nextExecution, false));
            check(!nextExecution.equals(execution) && repeated.reply().payload().equals(reply.payload()), "new_instance_same_id_same_neighbor_list");
            check(repeated.reply().senderInstance().equals(reply.senderInstance()), "bootstrap_instance_stable_across_queries");
            check(repeated.eof(), "complete_repeated_response_before_eof");
            var bad = register(config, "R1", instance(), true); var rejected = exchange(address, bad);
            check(rejected.reply().type() == MessageType.ERROR
                    && ((ControlMessage.ErrorPayload) rejected.reply().payload()).code() == ErrorCode.UNAUTHORIZED_NEIGHBOR,
                    "wrong_endpoint_rejected");
            check(rejected.reply().requestId().equals(bad.requestId())
                    && ((ControlMessage.ErrorPayload) rejected.reply().payload()).relatedType() == MessageType.REGISTER.code(), "error_request_id_and_related_type");
            check(rejected.eof(), "complete_error_response_before_eof");
            var ping = new ControlMessage(MessageType.PROBE_PING, UUID.randomUUID(), "R1", instance(), new ControlMessage.Probe(1));
            var alien = exchange(address, ping);
            check(alien.reply().type() == MessageType.ERROR && alien.reply().requestId().equals(ping.requestId())
                    && ((ControlMessage.ErrorPayload) alien.reply().payload()).code() == ErrorCode.INVALID_MESSAGE, "ping_rejected_as_invalid_operation");
            check(alien.eof(), "complete_invalid_operation_error_before_eof");
            var first = register(config, "S1", instance(), false);
            var doubleRequest = exchange(address, first, register(config, "S2", instance(), false));
            check(doubleRequest.reply().type() == MessageType.REGISTER_OK && doubleRequest.reply().requestId().equals(first.requestId())
                    && doubleRequest.eof(), "one_response_for_two_requests_on_one_connection");
            deadline(address, false); deadline(address, true);
            var afterTimeout = exchange(address, register(config, "R1", instance(), false));
            check(afterTimeout.reply().payload().equals(reply.payload()) && afterTimeout.eof(), "valid_query_after_peer_timeouts");
            check(before.equals(config.topology().orElseThrow()) && hashes.get(profile).equals(sha(profile))
                    && hashes.get(graph).equals(sha(graph)), "topology_and_configuration_files_unchanged");
            try (var pending = peer(address)) {
                pending.setSoTimeout(QUERY_MS / 2); boolean waiting = false;
                try { pending.getInputStream().read(); } catch (SocketTimeoutException expected) { waiting = true; }
                check(waiting, "pending_query_open_before_shutdown");
                long started = System.nanoTime(); service.close();
                check(true, "shutdown_returns_after_resource_termination");
                System.out.println("OBSERVED shutdown_elapsed_ms=" + Duration.ofNanos(System.nanoTime() - started).toMillis()
                        + " configured_shutdown_ms=" + config.runtime().control().shutdownTimeout().toMillis());
                pending.setSoTimeout(5000); check(closed(pending), "shutdown_closes_pending_query");
            }
            service.close(); check(true, "shutdown_is_idempotent");
            try (var rebound = new ServerSocket()) {
                rebound.setReuseAddress(true); rebound.bind(address); check(rebound.isBound(), "listener_port_reusable_after_shutdown");
            }
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: ManualBootstrapCheck.java OPERATOR EXPECTED_JAR_SHA256");
        System.out.println("MANUAL_START " + Instant.now() + " operator=" + args[0] + " Java=" + System.getProperty("java.version"));
        check(Runtime.version().feature() == 21, "jdk_21");
        Path jar = Path.of(BootstrapServer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        check(Files.isRegularFile(jar) && sha(jar).equals(args[1]), "loaded_jar_matches_expected_sha256");
        System.out.println("JAR_SHA256 " + sha(jar));
        Path temporary = Files.createTempDirectory("ott-imp003-manual-");
        try { run(temporary); }
        catch (Exception | Error failure) { System.out.println("MANUAL_FAIL " + failure); throw failure; }
        finally {
            try (var files = Files.walk(temporary)) {
                for (var file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
            }
        }
        check(!Files.exists(temporary), "temporary_configuration_removed");
        System.out.println("MANUAL_PASS checks=" + checks + " operator=" + args[0] + " finished=" + Instant.now());
    }
}
