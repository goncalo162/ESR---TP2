package pt.uminho.esr.ott.transport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.protocol.MessageType;
import pt.uminho.esr.ott.overlay.NodeRole;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class TransportTest {
    private static final InetAddress LOOPBACK = InetAddress.getLoopbackAddress();
    private static final Duration WAIT = Duration.ofSeconds(3);
    private static InetSocketAddress ephemeral() { return new InetSocketAddress(LOOPBACK, 0); }
    private static ControlMessage ping(int sequence) {
        return new ControlMessage(MessageType.PROBE_PING, new UUID(0, sequence), "N",
                new UUID(1, 2), new ControlMessage.Probe(sequence));
    }
    // Current ADR-013 contract: no lease, no active registration in bootstrap.
    private static ControlMessage largeMessage() {
        var endpoints = new ArrayList<ControlMessage.Endpoint>();
        for (int i = 0; i < 64; i++) endpoints.add(new ControlMessage.Endpoint("N" + i,
                new UUID(0, 0), "a".repeat(253), 5000, 5001, NodeRole.RELAY));
        return new ControlMessage(MessageType.REGISTER_OK, UUID.randomUUID(), "N",
                UUID.randomUUID(), new ControlMessage.NeighborList(endpoints));
    }
    private record Received(UUID connectionId, ControlMessage message) { }
    private record Closed(UUID connectionId, Connection.CloseReason reason) { }
    private static class Events implements Transport.Listener {
        final BlockingQueue<Connection> opened = new LinkedBlockingQueue<>();
        final BlockingQueue<Received> messages = new LinkedBlockingQueue<>();
        final BlockingQueue<Closed> closed = new LinkedBlockingQueue<>();
        @Override public void connectionOpen(Connection c) { opened.add(c); }
        @Override public void message(Connection c, ControlMessage m) { messages.add(new Received(c.id(), m)); }
        @Override public void connectionClosed(Connection c, Connection.CloseReason r) { closed.add(new Closed(c.id(), r)); }
    }
    private static <T> T take(BlockingQueue<T> queue) throws InterruptedException {
        T item = queue.poll(WAIT.toMillis(), TimeUnit.MILLISECONDS);
        assertNotNull(item, "Expected TCP event before deadline");
        return item;
    }
    private static void eventually(BooleanSupplier condition) {
        long deadline = System.nanoTime() + WAIT.toNanos();
        while (!condition.getAsBoolean() && System.nanoTime() < deadline)
            java.util.concurrent.locks.LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        assertTrue(condition.getAsBoolean(), "Condition not reached before deadline");
    }
    private static void terminated(Transport transport) {
        transport.close();
        assertTrue(transport.awaitTermination(WAIT));
    }

    @Test void concurrentSendersPreserveFullDuplexFramesAndPerSenderOrder() throws Exception {
        var a = new Events(); var b = new Events();
        try (var left = new Transport(ephemeral(), a); var right = new Transport(ephemeral(), b)) {
            Connection outbound = left.connect(right.localAddress()).get(3, TimeUnit.SECONDS);
            assertEquals(outbound.id(), take(a.opened).id());
            Connection inbound = take(b.opened);
            var start = new CountDownLatch(1);
            try (var senders = Executors.newVirtualThreadPerTaskExecutor()) {
                var jobs = new ArrayList<java.util.concurrent.Future<?>>();
                for (int sender = 0; sender < 4; sender++) {
                    final int lane = sender;
                    jobs.add(senders.submit(() -> {
                        start.await();
                        for (int i = 0; i < 40; i++)
                            assertEquals(Connection.SendResult.ACCEPTED,
                                    (lane < 2 ? outbound : inbound).send(ping(lane * 1000 + i)));
                        return null;
                    }));
                }
                start.countDown();
                for (var job : jobs) job.get(3, TimeUnit.SECONDS);
            }
            for (int side = 0; side < 2; side++) {
                Events receiver = side == 0 ? b : a;
                UUID id = side == 0 ? inbound.id() : outbound.id();
                var seen = new HashSet<ControlMessage>();
                int[] previous = {-1, -1, -1, -1};
                for (int i = 0; i < 80; i++) {
                    Received received = take(receiver.messages);
                    assertEquals(id, received.connectionId());
                    int sequence = (int) ((ControlMessage.Probe) received.message().payload()).probeId();
                    assertEquals(ping(sequence), received.message());
                    int lane = sequence / 1000;
                    assertEquals(side, lane / 2);
                    assertEquals(previous[lane] + 1, sequence % 1000);
                    previous[lane]++;
                    assertTrue(seen.add(received.message()));
                }
            }
            terminated(left); terminated(right);
            assertEquals(outbound.id(), take(a.closed).connectionId());
            assertEquals(inbound.id(), take(b.closed).connectionId());
            assertTrue(a.closed.isEmpty()); assertTrue(b.closed.isEmpty());
        }
    }

    @Test void readsIndependentFixtureByteByByteThenConcatenatedFrames() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events); var raw = new Socket()) {
            raw.connect(transport.localAddress()); Connection c = take(events.opened);
            byte[] frame;
            try (var resource = getClass().getResourceAsStream("/protocol/tcp-probe-ping.bin")) {
                assertNotNull(resource); frame = resource.readAllBytes();
            }
            var output = raw.getOutputStream();
            for (byte value : frame) { output.write(value & 255); output.flush(); }
            ControlMessage expected = take(events.messages).message();
            assertEquals(MessageType.PROBE_PING, expected.type());
            output.write(frame); output.write(frame); output.flush();
            assertEquals(new Received(c.id(), expected), take(events.messages));
            assertEquals(new Received(c.id(), expected), take(events.messages));
        }
    }

    @Test void truncatedPeerClosesOnceAndListenerAcceptsReplacement() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events)) {
            UUID old;
            try (var raw = new Socket()) {
                raw.connect(transport.localAddress()); old = take(events.opened).id();
                raw.getOutputStream().write(new byte[]{0, 0, 0});
            }
            assertEquals(new Closed(old, Connection.CloseReason.TRUNCATED_FRAME), take(events.closed));
            try (var raw = new Socket()) {
                raw.connect(transport.localAddress()); Connection next = take(events.opened);
                assertNotEquals(old, next.id()); raw.shutdownOutput();
                assertEquals(new Closed(next.id(), Connection.CloseReason.PEER_EOF), take(events.closed));
            }
            terminated(transport); assertTrue(events.closed.isEmpty());
        }
    }

    @Test void truncatedBodyAndOversizedPrefixAreIsolated() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events)) {
            for (int i = 0; i < 2; i++) {
                try (var raw = new Socket()) {
                    raw.connect(transport.localAddress()); Connection c = take(events.opened);
                    raw.getOutputStream().write(i == 0 ? new byte[]{0,0,0,37,1} : new byte[]{0,1,0,1});
                    raw.shutdownOutput();
                    assertEquals(new Closed(c.id(), i == 0 ? Connection.CloseReason.TRUNCATED_FRAME
                            : Connection.CloseReason.PROTOCOL_ERROR), take(events.closed));
                    assertTrue(events.messages.isEmpty());
                }
            }
        }
    }

    @Test void occupiedPortAndRefusedDialDoNotLeaveWorkers() throws Exception {
        var events = new Events(); InetSocketAddress released;
        try (var transport = new Transport(ephemeral(), events)) {
            assertThrows(IOException.class, () -> new Transport(transport.localAddress(), new Events()));
            try (var socket = new ServerSocket(0, 1, LOOPBACK)) {
                released = (InetSocketAddress) socket.getLocalSocketAddress();
            }
            assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> transport.connect(released).get(3, TimeUnit.SECONDS));
            assertTrue(events.opened.isEmpty()); assertTrue(events.closed.isEmpty());
            terminated(transport);
            assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> transport.connect(released).get(3, TimeUnit.SECONDS));
        }
    }

    @Test void boundedConnectionAdmissionAndCancellation() throws Exception {
        var options = new Transport.Options(2, 1, Duration.ofMillis(200), WAIT);
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events, options);
             var remote = new Transport(ephemeral(), new Events())) {
            Connection first = transport.connect(remote.localAddress()).get(3, TimeUnit.SECONDS);
            take(events.opened);
            assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> transport.connect(remote.localAddress()).get(3, TimeUnit.SECONDS));
            first.close(); take(events.closed);
            eventually(() -> transport.connectionCount() == 0);
            var pending = transport.connect(remote.localAddress()); pending.cancel(true);
            terminated(transport);
        }
    }

    @Test void slowPeerOverflowClosesOnlySaturatedConnection() throws Exception {
        var options = new Transport.Options(1, 4, Duration.ofSeconds(2), WAIT);
        var events = new Events(); var healthyEvents = new Events();
        try (var transport = new Transport(ephemeral(), events, options);
             var healthy = new Transport(ephemeral(), healthyEvents); var stalled = new ServerSocket()) {
            stalled.setReceiveBufferSize(1024); stalled.bind(ephemeral());
            Connection slow = transport.connect((InetSocketAddress) stalled.getLocalSocketAddress()).get(3, TimeUnit.SECONDS);
            try (var peer = stalled.accept()) {
                take(events.opened);
                Connection good = transport.connect(healthy.localAddress()).get(3, TimeUnit.SECONDS);
                take(events.opened); take(healthyEvents.opened);
                var large = largeMessage();
                Connection.SendResult result = Connection.SendResult.ACCEPTED;
                for (int i = 0; i < 20000 && result == Connection.SendResult.ACCEPTED; i++) {
                    result = slow.send(large); assertTrue(slow.queuedMessages() <= 1);
                }
                assertEquals(Connection.SendResult.OVERFLOW, result);
                assertEquals(new Closed(slow.id(), Connection.CloseReason.OUTPUT_OVERFLOW), take(events.closed));
                assertEquals(Connection.SendResult.CLOSED, slow.send(ping(0)));
                assertEquals(Connection.SendResult.ACCEPTED, good.send(ping(7)));
                assertEquals(ping(7), take(healthyEvents.messages).message());
                terminated(transport); assertTrue(events.closed.size() <= 1);
            }
        }
    }

    @Test void shutdownReleasesPartialReaderIdleWriterAndAcceptPort() throws Exception {
        var events = new Events(); var transport = new Transport(ephemeral(), events);
        var address = transport.localAddress();
        try (transport; var raw = new Socket()) {
            raw.connect(address); Connection c = take(events.opened);
            raw.getOutputStream().write(new byte[]{0,0,0,37,1}); terminated(transport);
            assertEquals(new Closed(c.id(), Connection.CloseReason.SHUTDOWN), take(events.closed));
            assertTrue(events.closed.isEmpty());
            try (var rebound = new Transport(address, new Events())) { assertEquals(address, rebound.localAddress()); }
        }
    }

    @Test void simultaneousLocalCloseEmitsOneEventAndReconnectGetsNewId() throws Exception {
        var a = new Events(); var b = new Events();
        try (var left = new Transport(ephemeral(), a); var right = new Transport(ephemeral(), b)) {
            Connection first = left.connect(right.localAddress()).get(3, TimeUnit.SECONDS);
            take(a.opened); Connection other = take(b.opened);
            try (var tasks = Executors.newVirtualThreadPerTaskExecutor()) {
                var f = tasks.submit(() -> { first.close(); });
                var g = tasks.submit(() -> { other.close(); }); f.get(); g.get();
            }
            take(a.closed); take(b.closed);
            Connection replacement = left.connect(right.localAddress()).get(3, TimeUnit.SECONDS);
            assertNotEquals(first.id(), replacement.id()); take(a.opened); take(b.opened);
            assertEquals(Connection.SendResult.ACCEPTED, replacement.send(ping(9)));
            assertEquals(ping(9), take(b.messages).message());
            terminated(left); terminated(right); take(a.closed); take(b.closed);
            assertTrue(a.closed.isEmpty()); assertTrue(b.closed.isEmpty());
        }
    }

    @Test void invalidOutboundMessageDoesNotCloseHealthyConnection() throws Exception {
        var events = new Events(); var received = new Events();
        try (var left = new Transport(ephemeral(), events); var right = new Transport(ephemeral(), received)) {
            Connection c = left.connect(right.localAddress()).get(3, TimeUnit.SECONDS);
            take(events.opened); take(received.opened);
            assertEquals(Connection.SendResult.INVALID_MESSAGE, c.send(null)); assertTrue(c.isOpen());
            assertEquals(Connection.SendResult.ACCEPTED, c.send(ping(1)));
            assertEquals(ping(1), take(received.messages).message());
        }
    }

    @Test void failingCallbackIsLocalizedAndShutdownWorksFromCallback() throws Exception {
        var events = new Events() {
            @Override public void message(Connection c, ControlMessage m) { throw new IllegalStateException("consumer failed"); }
        };
        try (var left = new Transport(ephemeral(), new Events()); var right = new Transport(ephemeral(), events)) {
            Connection c = left.connect(right.localAddress()).get(3, TimeUnit.SECONDS);
            Connection receiver = take(events.opened); c.send(ping(1));
            assertEquals(new Closed(receiver.id(), Connection.CloseReason.CALLBACK_ERROR), take(events.closed));
        }
        var finished = new CompletableFuture<Void>(); final Transport[] holder = new Transport[1];
        var stop = new Events() {
            @Override public void message(Connection c, ControlMessage m) { holder[0].close(); finished.complete(null); }
        };
        try (var right = new Transport(ephemeral(), stop); var left = new Transport(ephemeral(), new Events())) {
            holder[0] = right; left.connect(right.localAddress()).get(3, TimeUnit.SECONDS).send(ping(2));
            finished.get(3, TimeUnit.SECONDS); assertTrue(right.awaitTermination(WAIT));
        }
    }

    @Test void inboundAdmissionRejectsExcessPeerWithoutKillingListener() throws Exception {
        var options = new Transport.Options(2, 1, WAIT, WAIT); var events = new Events();
        try (var transport = new Transport(ephemeral(), events, options); var first = new Socket()) {
            first.connect(transport.localAddress()); Connection admitted = take(events.opened);
            try (var excess = new Socket()) {
                excess.connect(transport.localAddress()); excess.setSoTimeout(3000);
                assertEquals(-1, excess.getInputStream().read());
                assertEquals(1, transport.connectionCount()); assertTrue(events.opened.isEmpty());
            }
            admitted.close(); take(events.closed); eventually(() -> transport.connectionCount() == 0);
            try (var next = new Socket()) {
                next.connect(transport.localAddress()); assertNotEquals(admitted.id(), take(events.opened).id());
            }
        }
    }

    @Test void shutdownWithQueuedWritesToNonReadingPeerReclaimsBothWorkers() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events); var stalled = new ServerSocket()) {
            stalled.setReceiveBufferSize(1024); stalled.bind(ephemeral());
            Connection c = transport.connect((InetSocketAddress) stalled.getLocalSocketAddress()).get(3, TimeUnit.SECONDS);
            try (var peer = stalled.accept()) {
                take(events.opened); var message = largeMessage();
                for (int i = 0; i < 200; i++) assertEquals(Connection.SendResult.ACCEPTED, c.send(message));
                assertTrue(c.queuedMessages() > 0); terminated(transport);
                assertEquals(0, transport.connectionCount()); assertEquals(0, c.queuedMessages());
                assertEquals(new Closed(c.id(), Connection.CloseReason.SHUTDOWN), take(events.closed));
            }
        }
    }

    @Test void shutdownRacingOutboundDialsCompletesAllFuturesAndReleasesReservations() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events); var remote = new Transport(ephemeral(), new Events())) {
            var dials = new ArrayList<CompletableFuture<Connection>>();
            for (int i = 0; i < 32; i++) {
                var dial = transport.connect(remote.localAddress()); dials.add(dial);
                if (i % 2 == 0) dial.cancel(true);
            }
            terminated(transport); assertEquals(0, transport.connectionCount());
            for (var dial : dials) {
                assertTrue(dial.isDone()); if (!dial.isCompletedExceptionally()) assertFalse(dial.get().isOpen());
            }
            assertEquals(events.opened.size(), events.closed.size());
            assertEquals(events.opened.stream().map(Connection::id).collect(java.util.stream.Collectors.toSet()),
                    events.closed.stream().map(Closed::connectionId).collect(java.util.stream.Collectors.toSet()));
        }
    }

    @Test void validatesLimitsAndRejectsUnresolvedEndpoints() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new Transport.Options(0, 1, WAIT, WAIT));
        assertThrows(IllegalArgumentException.class, () -> new Transport.Options(257, 1, WAIT, WAIT));
        assertThrows(IllegalArgumentException.class, () -> new Transport.Options(1, 65, WAIT, WAIT));
        assertThrows(IllegalArgumentException.class, () -> new Transport.Options(1, 1, Duration.ZERO, WAIT));
        try (var transport = new Transport(ephemeral(), new Events())) {
            assertThrows(IllegalArgumentException.class,
                    () -> transport.connect(InetSocketAddress.createUnresolved("example.invalid", 5000)));
        }
    }
}
