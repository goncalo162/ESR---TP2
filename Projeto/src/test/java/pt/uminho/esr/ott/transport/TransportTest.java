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

    @Test void failingOpenAndClosedCallbacksDoNotRetainConnectionSlots() throws Exception {
        var events = new Events() {
            @Override public void connectionOpen(Connection c) {
                super.connectionOpen(c);
                throw new IllegalStateException("open callback failed");
            }
            @Override public void connectionClosed(Connection c, Connection.CloseReason reason) {
                super.connectionClosed(c, reason);
                throw new IllegalStateException("closed callback failed");
            }
        };
        var options = new Transport.Options(2, 1, WAIT, WAIT);
        try (var receiver = new Transport(ephemeral(), events, options);
             var sender = new Transport(ephemeral(), new Events())) {
            for (int i = 0; i < 2; i++) {
                sender.connect(receiver.localAddress()).get(3, TimeUnit.SECONDS);
                Connection c = take(events.opened);
                assertEquals(new Closed(c.id(), Connection.CloseReason.CALLBACK_ERROR), take(events.closed));
                eventually(() -> receiver.connectionCount() == 0);
            }
            terminated(receiver);
        }
    }

    @Test void blockedCallbackMakesShutdownTimeoutExplicitAndCanThenTerminate() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CompletableFuture<Void>();
        var events = new Events() {
            @Override public void message(Connection c, ControlMessage message) {
                entered.countDown();
                release.join(); // Deliberately violates the non-blocking callback contract.
            }
        };
        var options = new Transport.Options(2, 2, WAIT, Duration.ofMillis(100));
        try (var receiver = new Transport(ephemeral(), events, options);
             var sender = new Transport(ephemeral(), new Events())) {
            try {
                Connection c = sender.connect(receiver.localAddress()).get(3, TimeUnit.SECONDS);
                take(events.opened);
                assertEquals(Connection.SendResult.ACCEPTED, c.send(ping(1)));
                assertTrue(entered.await(3, TimeUnit.SECONDS));
                IllegalStateException error = assertThrows(IllegalStateException.class, receiver::close);
                assertTrue(error.getMessage().contains("did not terminate"));
                assertFalse(receiver.awaitTermination(Duration.ofMillis(10)));
            } finally {
                release.complete(null);
            }
            terminated(receiver);
            assertEquals(0, receiver.connectionCount());
            assertEquals(Connection.CloseReason.SHUTDOWN, take(events.closed).reason());
            assertTrue(events.closed.isEmpty());
        }
    }

    @Test void simultaneousShutdownFromTwoCallbacksDoesNotJoinEachOther() throws Exception {
        var arrived = new CountDownLatch(2);
        var finished = new CountDownLatch(2);
        var release = new CompletableFuture<Void>();
        final Transport[] holder = new Transport[1];
        var events = new Events() {
            @Override public void message(Connection c, ControlMessage message) {
                arrived.countDown();
                release.join();
                holder[0].close();
                finished.countDown();
            }
        };
        try (var receiver = new Transport(ephemeral(), events);
             var left = new Transport(ephemeral(), new Events());
             var right = new Transport(ephemeral(), new Events())) {
            holder[0] = receiver;
            try {
                Connection a = left.connect(receiver.localAddress()).get(3, TimeUnit.SECONDS);
                Connection b = right.connect(receiver.localAddress()).get(3, TimeUnit.SECONDS);
                take(events.opened); take(events.opened);
                assertEquals(Connection.SendResult.ACCEPTED, a.send(ping(1)));
                assertEquals(Connection.SendResult.ACCEPTED, b.send(ping(2)));
                assertTrue(arrived.await(3, TimeUnit.SECONDS));
                release.complete(null);
                assertTrue(finished.await(3, TimeUnit.SECONDS));
                assertTrue(receiver.awaitTermination(WAIT));
                assertEquals(0, receiver.connectionCount());
                assertNotEquals(take(events.closed).connectionId(), take(events.closed).connectionId());
                assertTrue(events.closed.isEmpty());
            } finally {
                release.complete(null);
            }
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

    // IMP-003: start with the actual missing APIs, without modifying earlier tests.
    private static CompletableFuture<Void> confirmed(Connection c, ControlMessage message) throws Exception {
        return c.sendAndFlush(message);
    }

    @Test void confirmedResponseCanBeReadCompletelyBeforeLocalClose() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events); var peer = new Socket()) {
            peer.connect(transport.localAddress()); peer.setSoTimeout(3000);
            Connection c = take(events.opened);
            confirmed(c, ping(801)).get(3, TimeUnit.SECONDS);
            c.close();
            assertEquals(ping(801), pt.uminho.esr.ott.protocol.MessageCodec.read(peer.getInputStream()).orElseThrow());
            assertTrue(pt.uminho.esr.ott.protocol.MessageCodec.read(peer.getInputStream()).isEmpty());
            terminated(transport);
        }
    }

    @Test void mixedPlainAndConfirmedFramesUseTheSameWriterOrder() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events); var peer = new Socket()) {
            peer.connect(transport.localAddress()); peer.setSoTimeout(3000);
            Connection c = take(events.opened);
            assertEquals(Connection.SendResult.ACCEPTED, c.send(ping(810)));
            var middle = confirmed(c, ping(811));
            assertEquals(Connection.SendResult.ACCEPTED, c.send(ping(812)));
            var last = confirmed(c, ping(813));
            last.get(3, TimeUnit.SECONDS); assertTrue(middle.isDone());
            for (int i = 810; i <= 813; i++)
                assertEquals(ping(i), pt.uminho.esr.ott.protocol.MessageCodec.read(peer.getInputStream()).orElseThrow());
            terminated(transport);
        }
    }

    @Test void requestCloseIsAdditiveAndTerminatesWithoutWaitingInTheCall() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events)) {
            transport.requestClose();
            assertTrue(transport.awaitTermination(WAIT));
            assertEquals(0, transport.connectionCount());
        }
    }
    private static final class ControlledWriter implements Connection.WriteDriver {
        final String pause;
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch released = new CountDownLatch(1);
        final BlockingQueue<byte[]> frames = new java.util.concurrent.ArrayBlockingQueue<>(8);
        final boolean fail;
        ControlledWriter(String pause, boolean fail) { this.pause = pause; this.fail = fail; }
        private void step(String point) throws java.io.IOException {
            if (!pause.equals(point)) return;
            entered.countDown();
            try {
                if (!released.await(3, TimeUnit.SECONDS)) throw new java.io.IOException("Test driver deadline");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new java.io.IOException("Test driver interrupted", e);
            }
            if (fail) throw new java.io.IOException("Injected " + point + " failure");
        }
        @Override public void dequeued() throws java.io.IOException { step("dequeued"); }
        @Override public void write(byte[] frame) throws java.io.IOException { frames.add(frame.clone()); step("write"); }
        @Override public void flush() throws java.io.IOException { step("flush"); }
    }

    @Test void confirmationRemainsPendingUntilFlushAndContinuationHoldsNoLocks() throws Exception {
        var driver = new ControlledWriter("flush", false); var events = new Events();
        try (var transport = new Transport(ephemeral(), events, Transport.Options.defaults(), s -> driver);
             var peer = new Socket()) {
            peer.connect(transport.localAddress()); Connection c = take(events.opened);
            var result = confirmed(c, ping(901));
            assertTrue(driver.entered.await(3, TimeUnit.SECONDS));
            assertFalse(result.isDone()); assertEquals(0, c.queuedMessages());
            assertEquals(ping(901), pt.uminho.esr.ott.protocol.MessageCodec.decode(take(driver.frames)));
            var callback = result.thenRun(() -> {
                assertFalse(Thread.holdsLock(c));
                var observed = new CompletableFuture<Void>();
                Thread.ofVirtual().start(() -> {
                    synchronized (c) { transport.connectionCount(); }
                    observed.complete(null);
                });
                try { observed.get(1, TimeUnit.SECONDS); }
                catch (Exception e) { throw new AssertionError("Continuation held an incompatible lock", e); }
                transport.close(); // Current writer must not join itself or the reader.
            });
            driver.released.countDown(); callback.get(3, TimeUnit.SECONDS);
            assertTrue(transport.awaitTermination(WAIT)); assertEquals(0, transport.connectionCount());
        } finally { driver.released.countDown(); }
    }

    @Test void closeBetweenTakeAndInFlightPublicationDoesNotLoseConfirmation() throws Exception {
        var driver = new ControlledWriter("dequeued", false); var events = new Events();
        try (var transport = new Transport(ephemeral(), events, Transport.Options.defaults(), s -> driver);
             var peer = new Socket()) {
            peer.connect(transport.localAddress()); Connection c = take(events.opened);
            var result = confirmed(c, ping(902));
            assertTrue(driver.entered.await(3, TimeUnit.SECONDS));
            assertEquals(0, c.queuedMessages()); assertFalse(result.isDone());
            c.close();
            assertThrows(java.util.concurrent.ExecutionException.class, () -> result.get(3, TimeUnit.SECONDS));
            terminated(transport); assertEquals(0, transport.connectionCount()); assertTrue(driver.frames.isEmpty());
        } finally { driver.released.countDown(); }
    }

    @Test void overflowFailsQueuedAndInFlightConfirmationsButPreservesOtherPeer() throws Exception {
        var driver = new ControlledWriter("write", false); var events = new Events();
        var first = new java.util.concurrent.atomic.AtomicBoolean(true);
        var options = new Transport.Options(1, 4, WAIT, WAIT);
        try (var transport = new Transport(ephemeral(), events, options,
                socket -> first.getAndSet(false) ? driver : Connection.SOCKET_WRITER.open(socket));
             var peer = new Socket(); var healthy = new Socket()) {
            peer.connect(transport.localAddress()); Connection c = take(events.opened);
            var active = confirmed(c, ping(910)); assertTrue(driver.entered.await(3, TimeUnit.SECONDS));
            var queued = confirmed(c, ping(911)); assertEquals(1, c.queuedMessages());
            var rejected = confirmed(c, ping(912));
            for (var f : java.util.List.of(active, queued, rejected))
                assertThrows(java.util.concurrent.ExecutionException.class, () -> f.get(3, TimeUnit.SECONDS));
            assertEquals(Connection.CloseReason.OUTPUT_OVERFLOW, take(events.closed).reason());
            healthy.connect(transport.localAddress()); healthy.setSoTimeout(3000);
            Connection good = take(events.opened); confirmed(good, ping(913)).get(3, TimeUnit.SECONDS);
            assertEquals(ping(913), pt.uminho.esr.ott.protocol.MessageCodec.read(healthy.getInputStream()).orElseThrow());
            terminated(transport); assertEquals(0, transport.connectionCount());
        } finally { driver.released.countDown(); }
    }

    @Test void cancellationBeforeAndDuringWritingRemainsTerminalAfterClose() throws Exception {
        var driver = new ControlledWriter("write", false); var events = new Events();
        try (var transport = new Transport(ephemeral(), events, Transport.Options.defaults(), s -> driver);
             var peer = new Socket()) {
            peer.connect(transport.localAddress()); Connection c = take(events.opened);
            var active = confirmed(c, ping(920)); assertTrue(driver.entered.await(3, TimeUnit.SECONDS));
            var queued = confirmed(c, ping(921));
            assertTrue(active.cancel(true)); assertTrue(queued.cancel(false)); c.close();
            driver.released.countDown(); terminated(transport);
            assertTrue(active.isCancelled()); assertTrue(queued.isCancelled());
            assertThrows(java.util.concurrent.CancellationException.class, active::join);
            assertThrows(java.util.concurrent.CancellationException.class, queued::join);
            assertEquals(0, c.queuedMessages()); assertEquals(0, transport.connectionCount());
        } finally { driver.released.countDown(); }
    }

    @Test void invalidConfirmedMessagePreservesHealthyConnectionAndClosedSendFails() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events); var peer = new Socket()) {
            peer.connect(transport.localAddress()); peer.setSoTimeout(3000); Connection c = take(events.opened);
            var bad = confirmed(c, new ControlMessage(2, MessageType.PROBE_PING, UUID.randomUUID(), "N",
                    UUID.randomUUID(), new ControlMessage.Probe(1)));
            var error = assertThrows(java.util.concurrent.ExecutionException.class, () -> bad.get(3, TimeUnit.SECONDS));
            assertInstanceOf(pt.uminho.esr.ott.protocol.ProtocolException.class, error.getCause()); assertTrue(c.isOpen());
            confirmed(c, ping(930)).get(3, TimeUnit.SECONDS);
            assertEquals(ping(930), pt.uminho.esr.ott.protocol.MessageCodec.read(peer.getInputStream()).orElseThrow());
            c.close();
            assertThrows(java.util.concurrent.ExecutionException.class, () -> confirmed(c, ping(931)).get(3, TimeUnit.SECONDS));
            terminated(transport);
        }
    }

    @Test void controlledWriteAndFlushErrorsTerminateAllAdmittedFutures() throws Exception {
        for (String point : java.util.List.of("write", "flush")) {
            var driver = new ControlledWriter(point, true); var events = new Events();
            try (var transport = new Transport(ephemeral(), events, Transport.Options.defaults(), s -> driver);
                 var peer = new Socket()) {
                peer.connect(transport.localAddress()); Connection c = take(events.opened);
                var active = confirmed(c, ping(940)); assertTrue(driver.entered.await(3, TimeUnit.SECONDS));
                var queued = confirmed(c, ping(941)); driver.released.countDown();
                for (var f : java.util.List.of(active, queued)) {
                    var ex = assertThrows(java.util.concurrent.ExecutionException.class, () -> f.get(3, TimeUnit.SECONDS));
                    assertInstanceOf(java.io.IOException.class, ex.getCause());
                }
                terminated(transport); assertEquals(0, transport.connectionCount());
            } finally { driver.released.countDown(); }
        }
    }

    @Test void requestCloseDoesNotAwaitBlockedWriterAndReservationRemainsUntilBothWorkersEnd() throws Exception {
        var entered = new CountDownLatch(1); var release = new CompletableFuture<Void>(); var events = new Events();
        Connection.WriteDriver blocked = new Connection.WriteDriver() {
            @Override public void write(byte[] bytes) { entered.countDown(); release.join(); }
            @Override public void flush() { }
        };
        try (var transport = new Transport(ephemeral(), events, Transport.Options.defaults(), s -> blocked);
             var peer = new Socket()) {
            try {
                peer.connect(transport.localAddress()); Connection c = take(events.opened);
                var active = confirmed(c, ping(950)); assertTrue(entered.await(3, TimeUnit.SECONDS));
                var queued = confirmed(c, ping(951));
                transport.requestClose();
                assertFalse(transport.awaitTermination(Duration.ofMillis(10)));
                assertEquals(1, transport.connectionCount()); // Reader exit cannot release a writer still running.
                release.complete(null);
                assertTrue(transport.awaitTermination(WAIT)); assertEquals(0, transport.connectionCount());
                assertTrue(active.isCompletedExceptionally()); assertTrue(queued.isCompletedExceptionally());
            } finally { release.complete(null); }
        }
    }

    @Test void writerFactoryFailureIsLocalAndCannotLeaveWorkersOrReservations() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events, Transport.Options.defaults(), socket -> {
            throw new java.io.IOException("Injected output factory failure");
        }); var peer = new Socket()) {
            peer.connect(transport.localAddress()); Connection c = take(events.opened);
            var f = confirmed(c, ping(960));
            assertThrows(java.util.concurrent.ExecutionException.class, () -> f.get(3, TimeUnit.SECONDS));
            terminated(transport); assertEquals(0, transport.connectionCount());
        }
    }
    @Test void concurrentPlainAndConfirmedSendersPreserveFramesAndPerLaneOrder() throws Exception {
        var events = new Events();
        try (var transport = new Transport(ephemeral(), events); var peer = new Socket();
             var jobs = Executors.newVirtualThreadPerTaskExecutor()) {
            peer.connect(transport.localAddress()); peer.setSoTimeout(3000); Connection c = take(events.opened);
            var start = new CountDownLatch(1); var tasks = new ArrayList<java.util.concurrent.Future<?>>();
            for (int lane = 0; lane < 4; lane++) {
                final int sender = lane;
                tasks.add(jobs.submit(() -> {
                    start.await(); CompletableFuture<Void> last = null;
                    for (int i = 0; i < 16; i++) {
                        var message = ping(sender * 100 + i);
                        if (i % 2 == 0) assertEquals(Connection.SendResult.ACCEPTED, c.send(message));
                        else last = c.sendAndFlush(message);
                    }
                    last.get(3, TimeUnit.SECONDS); return null;
                }));
            }
            start.countDown(); for (var task : tasks) task.get(3, TimeUnit.SECONDS);
            int[] previous = {-1, -1, -1, -1}; var seen = new HashSet<Long>();
            for (int i = 0; i < 64; i++) {
                var m = pt.uminho.esr.ott.protocol.MessageCodec.read(peer.getInputStream()).orElseThrow();
                long seq = ((ControlMessage.Probe) m.payload()).probeId(); int lane = (int) seq / 100;
                assertTrue(seen.add(seq)); assertEquals(previous[lane] + 1, seq % 100); previous[lane]++;
            }
            assertArrayEquals(new int[]{15, 15, 15, 15}, previous); terminated(transport);
        }
    }

    @Test void blockedFutureContinuationProducesExplicitShutdownTimeoutThenReleasesAllPendingWrites() throws Exception {
        var driver = new ControlledWriter("flush", false); var events = new Events();
        var callbackEntered = new CountDownLatch(1); var release = new CompletableFuture<Void>();
        var options = new Transport.Options(2, 2, WAIT, Duration.ofMillis(100));
        try (var transport = new Transport(ephemeral(), events, options, socket -> driver); var peer = new Socket()) {
            try {
                peer.connect(transport.localAddress()); Connection c = take(events.opened);
                var active = c.sendAndFlush(ping(970)); assertTrue(driver.entered.await(3, TimeUnit.SECONDS));
                var continuation = active.thenRun(() -> { callbackEntered.countDown(); release.join(); });
                driver.released.countDown(); assertTrue(callbackEntered.await(3, TimeUnit.SECONDS));
                var queued = c.sendAndFlush(ping(971));
                assertThrows(IllegalStateException.class, transport::close);
                assertFalse(transport.awaitTermination(Duration.ofMillis(10))); assertEquals(1, transport.connectionCount());
                release.complete(null); continuation.get(3, TimeUnit.SECONDS);
                assertTrue(transport.awaitTermination(WAIT)); assertEquals(0, transport.connectionCount());
                assertTrue(queued.isCompletedExceptionally());
            } finally { release.complete(null); driver.released.countDown(); }
        }
    }
}
