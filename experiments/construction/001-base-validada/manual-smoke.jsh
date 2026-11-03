import java.nio.file.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.*;

void check(boolean ok, String label) {
    if (!ok) throw new AssertionError(label);
    System.out.println("CHECK_PASS " + label);
}

record ManualClosed(UUID id, Connection.CloseReason reason) {}

class ManualEvents implements Transport.Listener {
    final ArrayBlockingQueue<Connection> opened = new ArrayBlockingQueue<>(8);
    final ArrayBlockingQueue<ControlMessage> messages = new ArrayBlockingQueue<>(8);
    final ArrayBlockingQueue<ManualClosed> closed = new ArrayBlockingQueue<>(8);
    final AtomicBoolean overflow = new AtomicBoolean();
    public void connectionOpen(Connection c) { if (!opened.offer(c)) overflow.set(true); }
    public void message(Connection c, ControlMessage m) { if (!messages.offer(m)) overflow.set(true); }
    public void connectionClosed(Connection c, Connection.CloseReason reason) {
        if (!closed.offer(new ManualClosed(c.id(), reason))) overflow.set(true);
    }
}

void manualSmoke() throws Exception {
    System.out.println("MANUAL_START " + Instant.now() + " Java=" + System.getProperty("java.version"));
    Path configs = Path.of("src/main/resources/config");
    var paths = List.of("bootstrap", "server-1", "server-2", "relay", "client").stream().map(n -> configs.resolve(n + ".properties")).toList();
    var nodes = ConfigurationLoader.loadDeployment(paths);
    var streams = nodes.stream().flatMap(n -> n.streams().stream()).toList();
    check(nodes.size() == 5, "config_five_profiles");
    check(nodes.stream().filter(n -> n.role() == pt.uminho.esr.ott.overlay.NodeRole.SERVER).count() == 2, "config_two_origins");
    check(streams.size() == 4 && streams.stream().map(StreamConfiguration::streamId).distinct().count() == 4, "config_four_distinct_stream_ids");
    var bootstrap = nodes.stream().filter(n -> n.role() == pt.uminho.esr.ott.overlay.NodeRole.BOOTSTRAP).findFirst().orElseThrow();
    check(bootstrap.topology().orElseThrow().nodes().get("S1").neighbors().equals(Set.of("S2", "R1")), "config_expected_neighbors");
    System.out.println("CONFIG_PASS nodes=5 origins=2 streams=4 neighbors_S1=S2,R1");

    byte[] golden = Files.readAllBytes(Path.of("src/test/resources/protocol/tcp-hello.bin"));
    ControlMessage decoded = MessageCodec.decode(golden);
    var endpoint = ((ControlMessage.EndpointPayload) decoded.payload()).endpoint();
    check(decoded.type() == MessageType.HELLO && decoded.senderId().equals("S1"), "fixture_type_and_sender");
    check(decoded.requestId().equals(UUID.fromString("00112233-4455-6677-8899-aabbccddeeff")), "fixture_request_id");
    check(endpoint.host().equals("10.0.0.1") && endpoint.controlPort() == 5000 && endpoint.dataPort() == 5001, "fixture_endpoint");
    check(Arrays.equals(golden, MessageCodec.encode(decoded)), "fixture_independent_bytes_roundtrip");
    check(MessageType.values().length == 16, "sixteen_active_types");
    boolean retiredRejected = false;
    try { MessageCodec.decode(Files.readAllBytes(Path.of("src/test/resources/protocol/tcp-invalid-retired-type.bin"))); }
    catch (pt.uminho.esr.ott.protocol.ProtocolException e) { retiredRejected = e.errorCode() == ErrorCode.INVALID_MESSAGE; }
    check(retiredRejected, "retired_type_3_rejected");
    System.out.println("CODEC_PASS hello_bytes=" + golden.length + " active_types=16 retired_3=rejected");

    var a = new ManualEvents();
    var b = new ManualEvents();
    var bind = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
    InetSocketAddress reusable;
    try (var left = new Transport(bind, a); var right = new Transport(bind, b)) {
        reusable = left.localAddress();
        System.out.println("TCP_LISTEN left=" + left.localAddress() + " right=" + right.localAddress());
        Connection outgoing = left.connect(right.localAddress()).get(3, TimeUnit.SECONDS);
        Connection openedLeft = a.opened.poll(3, TimeUnit.SECONDS);
        Connection incoming = b.opened.poll(3, TimeUnit.SECONDS);
        check(openedLeft != null && incoming != null && outgoing.id().equals(openedLeft.id()), "tcp_both_connections_open");
        UUID request = UUID.randomUUID();
        var ping = new ControlMessage(MessageType.PROBE_PING, request, "L", UUID.randomUUID(), new ControlMessage.Probe(42));
        check(outgoing.send(ping) == Connection.SendResult.ACCEPTED, "tcp_ping_queued");
        check(ping.equals(b.messages.poll(3, TimeUnit.SECONDS)), "tcp_ping_actually_received");
        var pong = new ControlMessage(MessageType.PROBE_PONG, request, "R", UUID.randomUUID(), new ControlMessage.Probe(42));
        check(incoming.send(pong) == Connection.SendResult.ACCEPTED, "tcp_pong_queued");
        check(pong.equals(a.messages.poll(3, TimeUnit.SECONDS)), "tcp_pong_actually_received");
        outgoing.close();
        var closedLeft = a.closed.poll(3, TimeUnit.SECONDS);
        var closedRight = b.closed.poll(3, TimeUnit.SECONDS);
        check(closedLeft != null && closedLeft.id().equals(outgoing.id()), "tcp_local_close_event");
        check(closedRight != null && closedRight.id().equals(incoming.id()), "tcp_peer_close_event");
        left.close(); right.close();
        check(left.awaitTermination(Duration.ofSeconds(3)) && right.awaitTermination(Duration.ofSeconds(3)), "shutdown_both_transports_terminated");
        check(left.connectionCount() == 0 && right.connectionCount() == 0, "shutdown_no_connection_reservations");
        check(a.closed.isEmpty() && b.closed.isEmpty() && !a.overflow.get() && !b.overflow.get(), "shutdown_unique_events_and_no_queue_overflow");
        System.out.println("TCP_PASS request=" + request + " probeId=42 actual_ping_and_pong=true");
    }
    try (var rebound = new Transport(reusable, new ManualEvents())) {
        check(rebound.localAddress().equals(reusable), "shutdown_listening_port_reused");
        rebound.close();
        check(rebound.awaitTermination(Duration.ofSeconds(3)), "shutdown_rebound_transport_terminated");
    }
    System.out.println("MANUAL_PASS " + Instant.now());
}

try { manualSmoke(); } catch (Throwable failure) { System.out.println("MANUAL_FAIL " + failure); failure.printStackTrace(System.out); }
/exit
