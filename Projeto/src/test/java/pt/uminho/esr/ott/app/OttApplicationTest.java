package pt.uminho.esr.ott.app;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.overlay.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/** Initial CLI/composition oracles; full V-008 remains to be built and executed. */
@Timeout(15)
class OttApplicationTest {
    @TempDir Path temporary;
    static final class FakeService implements OttApplication.RunningService {
        final List<Duration> budgets = new ArrayList<>();
        int stops, closes, snapshots; boolean terminated = true; int reason = -1;
        public Map<String, Neighbor> neighbors() { snapshots++; return Map.of(); }
        public void requestShutdown(int reason, Duration budget) { stops++; this.reason = reason; budgets.add(budget); }
        public boolean awaitTermination(Duration budget) { budgets.add(budget); return terminated; }
        public void close() { closes++; }
    }
    static final class Harness {
        final AtomicLong clock = new AtomicLong(1000);
        final AtomicInteger opens = new AtomicInteger();
        final List<NodeConfiguration> configs = new ArrayList<>();
        final FakeService service = new FakeService();
        final ByteArrayOutputStream output = new ByteArrayOutputStream(), error = new ByteArrayOutputStream();
        boolean failOpen;
        int run(String input, String... arguments) {
            return assertDoesNotThrow(() -> OttApplication.run(arguments,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8), new PrintStream(error, true, StandardCharsets.UTF_8),
                config -> { opens.incrementAndGet(); configs.add(config); if (failOpen) throw new IOException("controlled resource failure"); return service; }, clock::get));
        }
        String text() { return output.toString(StandardCharsets.UTF_8) + error.toString(StandardCharsets.UTF_8); }
    }
    @Test void m01HelpHasNoServiceOrConfigLoad() {
        var h = new Harness(); assertEquals(0, h.run("", "--help"));
        assertEquals(0, h.opens.get()); assertTrue(h.text().contains("--role"));
    }
    @TestFactory Stream<DynamicTest> m01GrammarRejectsAmbiguousOrIncompleteArguments() {
        List<String[]> invalid = List.of(new String[0], new String[]{"--help", "--role", "RELAY"},
            new String[]{"--role", "relay", "--config", "x"}, new String[]{"--role=RELAY", "--config", "x"},
            new String[]{"--role", "RELAY", "--role", "CLIENT", "--config", "x"},
            new String[]{"--role", "RELAY", "--config", "x", "--config", "y"},
            new String[]{"--role", "RELAY", "--config"}, new String[]{"--role", "RELAY"},
            new String[]{"--config", "x"}, new String[]{"--unknown"},
            new String[]{"--role", "UNKNOWN", "--config", "x"}, new String[]{"RELAY", "x"});
        return invalid.stream().map(args -> DynamicTest.dynamicTest(Arrays.toString(args), () -> {
            assertThrows(IllegalArgumentException.class, () -> OttApplication.parse(args));
            var h = new Harness(); assertEquals(2, h.run("", args)); assertEquals(0, h.opens.get());
        }));
    }
    @TestFactory Stream<DynamicTest> m02GrammarAcceptsAllRolesInEitherOrder() {
        return Arrays.stream(NodeRole.values()).map(role -> DynamicTest.dynamicTest(role.name(), () -> {
            var invocation = assertDoesNotThrow(() -> OttApplication.parse(new String[]{"--role", role.name(), "--config", "node.properties"}));
            var reversed = assertDoesNotThrow(() -> OttApplication.parse(new String[]{"--config", "node.properties", "--role", role.name()}));
            assertEquals(invocation, reversed); assertFalse(invocation.help());
            assertEquals(Optional.of(role), invocation.role()); assertEquals(Optional.of(Path.of("node.properties")), invocation.config());
        }));
    }
    @Test void m01MissingConfigurationCannotOpenAService() {
        var h = new Harness(); assertEquals(2, h.run("quit\n", "--role", "RELAY", "--config", temporary.resolve("missing.properties").toString()));
        assertEquals(0, h.opens.get()); assertFalse(h.text().isBlank());
    }
    @Test void m01InvalidConfigurationCannotOpenAService() throws IOException {
        Path file = temporary.resolve("bad.properties"); Files.writeString(file, "node.id=bad\ncontrol.port=999999\n");
        var h = new Harness(); assertEquals(2, h.run("", "--role", "RELAY", "--config", file.toString())); assertEquals(0, h.opens.get());
    }
    @Test void m04NeighborsAndQuitUseValidatedProfileAndCloseOnce() {
        var h = new Harness(); assertEquals(0, h.run("neighbors\nquit\n", "--role", "RELAY", "--config", "src/main/resources/config/relay.properties"));
        assertEquals(1, h.opens.get()); assertEquals(NodeRole.RELAY, h.configs.getFirst().role());
        assertEquals(1, h.service.snapshots); assertEquals(1, h.service.stops); assertEquals(0, h.service.reason);
        assertTrue(h.service.budgets.stream().allMatch(d -> !d.isNegative() && d.compareTo(h.configs.getFirst().runtime().control().shutdownTimeout()) <= 0));
    }
    @Test void m10EofStopsNormallyWithoutAQuitLine() {
        var h = new Harness(); assertEquals(0, h.run("", "--role", "RELAY", "--config", "src/main/resources/config/relay.properties"));
        assertEquals(1, h.service.stops); assertEquals(0, h.service.reason);
    }
    @Test void m09ResourceFailureReturnsThreeInsteadOfHanging() {
        var h = new Harness(); h.failOpen = true;
        assertEquals(3, h.run("", "--role", "RELAY", "--config", "src/main/resources/config/relay.properties"));
        assertEquals(1, h.opens.get()); assertTrue(h.text().contains("controlled resource failure"));
    }
    @Test void m12FailedPhysicalTerminationReturnsThree() {
        var h = new Harness(); h.service.terminated = false;
        assertEquals(3, h.run("quit\n", "--role", "RELAY", "--config", "src/main/resources/config/relay.properties"));
        assertEquals(1, h.service.stops);
    }
    @Test void m16FutureCommandsExplainUnavailabilityAndKeepCliUsable() {
        var h = new Harness(); assertEquals(0, h.run("catalog\nplay example\nneighbors\nquit\n", "--role", "RELAY", "--config", "src/main/resources/config/relay.properties"));
        assertEquals(1, h.service.snapshots); assertEquals(1, h.service.stops);
        assertFalse(h.text().isBlank());
    }
    @Test void m14OverlongLineIsReportedAndDoesNotBecomeACommand() {
        var h = new Harness(); assertEquals(0, h.run("x".repeat(4097) + "\nneighbors\nquit\n", "--role", "RELAY", "--config", "src/main/resources/config/relay.properties"));
        assertEquals(1, h.service.snapshots); assertFalse(h.error.toString(StandardCharsets.UTF_8).isBlank());
    }
    @Test void m15NodeCannotReviveAfterCloseBeforeStart() {
        // Configuration is loaded only by the existing validated loader; no service starts here.
        var config = assertDoesNotThrow(() -> pt.uminho.esr.ott.config.ConfigurationLoader.load(
                Path.of("src/main/resources/config/relay.properties"), NodeRole.RELAY));
        var node = new OverlayNode(config, UUID.randomUUID(), event -> {});
        assertDoesNotThrow(node::close);
        var started = assertDoesNotThrow(node::start);
        assertTrue(started.toCompletableFuture().isCompletedExceptionally());
        assertTrue(assertDoesNotThrow(() -> node.awaitTermination(Duration.ofSeconds(1))));
    }
    record ChildResult(int exit, String output) { }
    ChildResult child(String input, String... arguments) throws Exception {
        var command = new ArrayList<String>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString()); command.add("-cp");
        command.add(System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
        command.add(OttApplication.class.getName()); command.addAll(List.of(arguments));
        Path log = temporary.resolve(UUID.randomUUID() + ".log");
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            try (var stream = process.getOutputStream()) { stream.write(input.getBytes(StandardCharsets.UTF_8)); }
            assertTrue(process.waitFor(5, TimeUnit.SECONDS), "CLI JVM must terminate within the test deadline");
            return new ChildResult(process.exitValue(), Files.readString(log));
        } finally {
            if (process.isAlive()) { process.destroyForcibly(); assertTrue(process.waitFor(5, TimeUnit.SECONDS), "child cleanup"); }
        }
    }
    @Test void v008RealJvmHelpReturnsZero() throws Exception {
        var child = child("", "--help"); assertEquals(0, child.exit(), child.output()); assertTrue(child.output().contains("--config"));
    }
    @Test void v008RealJvmInvalidConfigurationReturnsTwo() throws Exception {
        var child = child("", "--role", "RELAY", "--config", temporary.resolve("absent.properties").toString());
        assertEquals(2, child.exit(), child.output());
    }
    static final class LiveChild implements AutoCloseable {
        final Process process; final Path log; final BufferedWriter input;
        LiveChild(Path profile, NodeRole role, Path log, boolean pipeOutput) throws IOException {
            this.log = log;
            var command = List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp",
                System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                OttApplication.class.getName(), "--role", role.name(), "--config", profile.toString());
            var builder = new ProcessBuilder(command).redirectErrorStream(true);
            if (!pipeOutput) builder.redirectOutput(log.toFile());
            process = builder.start(); input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        }
        void send(String line) throws IOException { input.write(line); input.newLine(); input.flush(); }
        String text() { try { return Files.exists(log) ? Files.readString(log) : ""; } catch (IOException e) { throw new UncheckedIOException(e); } }
        void await(java.util.function.Predicate<String> predicate) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(6);
            while (!predicate.test(text())) {
                assertTrue(process.isAlive(), "child stopped unexpectedly: " + text());
                assertTrue(System.nanoTime() < deadline, "child log condition deadline: " + text());
                Thread.sleep(5);
            }
        }
        int exit() throws Exception {
            assertTrue(process.waitFor(5, TimeUnit.SECONDS), "process deadline: " + text());
            int result = process.exitValue();
            Files.writeString(log.resolveSibling(log.getFileName() + ".exit"), Integer.toString(result));
            return result;
        }
        public void close() throws Exception {
            if (process.isAlive()) process.destroy();
            if (!process.waitFor(4, TimeUnit.SECONDS)) process.destroyForcibly();
            assertTrue(process.waitFor(2, TimeUnit.SECONDS), "all child JVMs reaped");
            try { input.close(); } catch (IOException expected) { /* A dead child may already have closed its stdin pipe. */ }
        }
    }
    static final class LiveDeployment implements AutoCloseable {
        final Path directory; final int[] ports = new int[7];
        final List<LiveChild> children = new ArrayList<>();
        final List<Path> profiles = new ArrayList<>();
        LiveDeployment() throws Exception {
            directory = Path.of(System.getProperty("ott.evidence.directory", "target/imp005-evidence"))
                .toAbsolutePath().resolve("cli-" + UUID.randomUUID()); Files.createDirectories(directory);
            var reservations = new ArrayList<java.net.ServerSocket>();
            try {
                for (int n = 0; n < ports.length; n++) {
                    var socket = new java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress());
                    reservations.add(socket); ports[n] = socket.getLocalPort();
                }
                StringBuilder graph = new StringBuilder("node.ids=N0,N1,N2\n");
                for (int n = 0; n < 3; n++) graph.append("node.N" + n + ".host=127.0.0.1\nnode.N" + n + ".controlPort=" + ports[n + 1]
                    + "\nnode.N" + n + ".dataPort=" + ports[n + 4] + "\nnode.N" + n + ".role=" + role(n)
                    + "\nnode.N" + n + ".neighbors=" + (n == 0 ? "N1,N2" : n == 1 ? "N0,N2" : "N0,N1") + "\n");
                Files.writeString(directory.resolve("topology.properties"), graph.toString());
                Path boot = directory.resolve("bootstrap.properties");
                Files.writeString(boot, "node.id=BOOT\nnode.role=BOOTSTRAP\nbind.host=127.0.0.1\nadvertised.host=127.0.0.1\ncontrol.port="
                    + ports[0] + "\ntopology.file=topology.properties\ncontrol.shutdown.timeout.ms=1500\n"); profiles.add(boot);
                for (int n = 0; n < 3; n++) {
                    Path file = directory.resolve("N" + n + ".properties");
                    String media = n == 0 ? "streams.file=" + Path.of("src/main/resources/config/streams-s1.properties").toAbsolutePath()
                        + "\nmedia.base=" + directory + "\n" : "";
                    Files.writeString(file, "node.id=N" + n + "\nnode.role=" + role(n) + "\nbind.host=127.0.0.1\nadvertised.host=127.0.0.1\ncontrol.port="
                        + ports[n + 1] + "\ndata.port=" + ports[n + 4] + "\nbootstrap.host=127.0.0.1\nbootstrap.port=" + ports[0]
                        + "\ncontrol.connect.timeout.ms=300\nbootstrap.query.timeout.ms=500\noverlay.retry.initial.ms=20\noverlay.retry.max.ms=100\noverlay.retry.jitter.percent=0\ncontrol.shutdown.timeout.ms=1500\n" + media);
                    profiles.add(file);
                }
            } finally { for (var socket : reservations) socket.close(); }
        }
        static NodeRole role(int n) { return n == 0 ? NodeRole.SERVER : n == 1 ? NodeRole.RELAY : NodeRole.CLIENT; }
        LiveChild start(int index) throws Exception {
            var child = new LiveChild(profiles.get(index), index == 0 ? NodeRole.BOOTSTRAP : role(index - 1),
                directory.resolve("process-" + index + "-" + children.size() + ".log"), false);
            children.add(child); return child;
        }
        public void close() throws Exception {
            Exception failure = null;
            for (var child : children) try { child.close(); } catch (Exception e) { failure = e; }
            if (failure != null) throw failure;
        }
    }
    @Test @Timeout(40) void v008FourRolesConvergeAndPeerLeaveDoesNotRestartSurvivors() throws Exception {
        try (var d = new LiveDeployment()) {
            var bootstrap = d.start(0); bootstrap.await(t -> t.contains("READY"));
            var a = d.start(1); var b = d.start(2); var c = d.start(3);
            for (var child : List.of(a,b,c)) child.await(t -> t.lines().filter(l -> l.contains("NeighborUp[")).count() == 2);
            for (var child : List.of(a,b,c)) { child.send("neighbors"); child.await(t -> t.lines().filter(l -> l.contains("state=ACTIVE")).count() >= 2); }
            bootstrap.send("quit"); assertEquals(0, bootstrap.exit());
            long aPid = a.process.pid(), bPid = b.process.pid();
            c.send("quit"); assertEquals(0, c.exit());
            a.await(t -> t.contains("reason=LEAVE")); b.await(t -> t.contains("reason=LEAVE"));
            a.send("neighbors"); b.send("neighbors");
            assertTrue(a.process.isAlive()); assertTrue(b.process.isAlive()); assertEquals(aPid, a.process.pid()); assertEquals(bPid, b.process.pid());
            var bootAgain = d.start(0); bootAgain.await(t -> t.contains("READY"));
            var restart = d.start(3); restart.await(t -> t.lines().filter(l -> l.contains("NeighborUp[")).count() == 2);
            a.await(t -> t.lines().filter(l -> l.contains("NeighborUp[")).count() == 3);
            restart.send("quit"); assertEquals(0, restart.exit());
            a.send("quit"); b.send("quit"); assertEquals(0, a.exit()); assertEquals(0, b.exit());
            bootAgain.send("quit"); assertEquals(0, bootAgain.exit());
            for (int port : d.ports) try (var socket = new java.net.ServerSocket(port, 1, java.net.InetAddress.getLoopbackAddress())) { assertTrue(socket.isBound()); }
        }
    }
    @Test @Timeout(20) void m04NeighborsWorksDuringDiscoveryAndQuitClosesBlockedStdin() throws Exception {
        try (var d = new LiveDeployment()) {
            var child = d.start(2); child.await(t -> t.contains("STARTED"));
            child.send("neighbors"); child.await(t -> t.contains("NEIGHBORS"));
            child.send("quit"); // Deliberately keep the input pipe open while the reader waits for another line.
            assertEquals(0, child.exit());
        }
    }
    @Test @Timeout(20) void m10RealSigtermWhileStdinIsBlockedTerminatesWithinBudget() throws Exception {
        try (var d = new LiveDeployment()) {
            var child = d.start(2); child.await(t -> t.contains("STARTED"));
            child.process.destroy(); child.exit(); // Record actual OS status; a signal is not promised to return zero.
            assertFalse(child.process.isAlive());
        }
    }
    @Test @Timeout(20) void m09RealBindFailureRollsBackAndReturnsThree() throws Exception {
        try (var d = new LiveDeployment(); var occupied = new java.net.ServerSocket(d.ports[2], 1, java.net.InetAddress.getLoopbackAddress())) {
            var child = d.start(2); assertEquals(3, child.exit());
            assertFalse(child.text().contains("READY node=N1"));
        }
    }
    @Test @Timeout(20) void m13ClosedStdoutTerminatesInsteadOfBlockingOwner() throws Exception {
        try (var d = new LiveDeployment(); var child = new LiveChild(d.profiles.get(2), NodeRole.RELAY, d.directory.resolve("closed-pipe.log"), true)) {
            child.process.getInputStream().close(); assertEquals(3, child.exit());
        }
    }

    @Test void m13InputFailureStopsAndCleansService() {
        var h = new Harness(); var input = new InputStream() { public int read() throws IOException { throw new IOException("controlled input failure"); } };
        int result = OttApplication.run(new String[]{"--role", "RELAY", "--config", "src/main/resources/config/relay.properties"}, input,
            new PrintStream(h.output), new PrintStream(h.error), c -> h.service, h.clock::get);
        assertEquals(3, result); assertEquals(1, h.service.stops); assertEquals(1, h.service.reason);
    }
    @Test void m14FatalLogIsBoundedAndTruncationIsVisible() {
        var h = new Harness();
        int result = OttApplication.run(new String[]{"--role", "RELAY", "--config", "src/main/resources/config/relay.properties"}, new ByteArrayInputStream(new byte[0]),
            new PrintStream(h.output), new PrintStream(h.error), c -> { throw new IOException("x".repeat(10000)); }, h.clock::get);
        assertEquals(3, result); assertTrue(h.text().contains("[truncated]"));
        assertTrue(h.text().lines().allMatch(line -> line.length() <= 4096));
    }
    @Test @Timeout(20) void m13BlockedStdoutIsClosedAndWriterTerminates() throws Exception {
        try (var d = new LiveDeployment(); var child = new LiveChild(d.profiles.get(2), NodeRole.RELAY, d.directory.resolve("blocked-pipe.log"), true)) {
            var feeder = Thread.ofVirtual().start(() -> {
                try { for (int n = 0; n < 20000; n++) child.send("neighbors"); }
                catch (IOException expected) { /* Child exits after queue/backpressure failure. */ }
            });
            try { assertEquals(3, child.exit()); }
            finally {
                if (child.process.isAlive()) child.process.destroyForcibly();
                feeder.join(Duration.ofSeconds(3)); assertFalse(feeder.isAlive(), "stdin feeder physically terminated");
            }
        }
    }
    @Test @Timeout(30) void m07AbruptPeerDeathLeavesSurvivorsRunningAndRestartReconnects() throws Exception {
        try (var d = new LiveDeployment()) {
            var bootstrap = d.start(0); bootstrap.await(t -> t.contains("READY"));
            var a = d.start(1); var b = d.start(2); var peer = d.start(3);
            for (var child : List.of(a,b,peer)) child.await(t -> t.lines().filter(l -> l.contains("NeighborUp[")).count() == 2);
            peer.process.destroyForcibly(); peer.exit();
            a.await(t -> t.contains("NeighborDown[") && t.contains("reason=TRANSPORT"));
            assertTrue(a.process.isAlive()); assertTrue(b.process.isAlive());
            var restarted = d.start(3); restarted.await(t -> t.lines().filter(l -> l.contains("NeighborUp[")).count() == 2);
            a.await(t -> t.lines().filter(l -> l.contains("NeighborUp[")).count() == 3);
            for (var child : List.of(a,b,restarted,bootstrap)) child.send("quit");
            for (var child : List.of(a,b,restarted,bootstrap)) assertEquals(0, child.exit());
        }
    }
    @Test @Timeout(15) void m01InvalidProfileInRealJvmDoesNotBindItsConfiguredPort() throws Exception {
        try (var d = new LiveDeployment()) {
            Files.writeString(d.profiles.get(2), "\nunknown.option=1\n", StandardOpenOption.APPEND);
            var child = d.start(2); assertEquals(2, child.exit());
            try (var free = new java.net.ServerSocket(d.ports[2], 1, java.net.InetAddress.getLoopbackAddress())) { assertTrue(free.isBound()); }
        }
    }

    @Test @Timeout(20) void m06AndM08ActiveErrorIsObservedAndReconnectNeedsNoBootstrap() throws Exception {
        try (var d = new LiveDeployment(); var peerServer = new java.net.ServerSocket(d.ports[3], 8, java.net.InetAddress.getLoopbackAddress())) {
            peerServer.setSoTimeout(4000);
            var boot = d.start(0); boot.await(t -> t.contains("READY")); var child = d.start(2);
            UUID remote = UUID.randomUUID();
            try (var socket = peerServer.accept()) {
                socket.setSoTimeout(2500);
                var hello = pt.uminho.esr.ott.protocol.MessageCodec.read(socket.getInputStream()).orElseThrow();
                assertEquals(pt.uminho.esr.ott.protocol.MessageType.HELLO, hello.type());
                pt.uminho.esr.ott.protocol.MessageCodec.write(new pt.uminho.esr.ott.protocol.ControlMessage(
                    pt.uminho.esr.ott.protocol.MessageType.HELLO_OK, hello.requestId(), "N2", remote,
                    new pt.uminho.esr.ott.protocol.ControlMessage.HelloOk(3)), socket.getOutputStream()); socket.getOutputStream().flush();
                child.await(t -> t.contains("NeighborUp[")); boot.send("quit"); assertEquals(0, boot.exit());
                pt.uminho.esr.ott.protocol.MessageCodec.write(new pt.uminho.esr.ott.protocol.ControlMessage(
                    pt.uminho.esr.ott.protocol.MessageType.ERROR, UUID.randomUUID(), "N2", remote,
                    new pt.uminho.esr.ott.protocol.ControlMessage.ErrorPayload(pt.uminho.esr.ott.protocol.ErrorCode.TIMEOUT, 7, "controlled-active-error")), socket.getOutputStream()); socket.getOutputStream().flush();
                child.await(t -> t.contains("controlled-active-error") && t.contains("Received["));
                assertTrue(child.process.isAlive());
            }
            try (var second = peerServer.accept()) {
                second.setSoTimeout(2500);
                var hello = pt.uminho.esr.ott.protocol.MessageCodec.read(second.getInputStream()).orElseThrow();
                UUID newRemote = UUID.randomUUID();
                pt.uminho.esr.ott.protocol.MessageCodec.write(new pt.uminho.esr.ott.protocol.ControlMessage(
                    pt.uminho.esr.ott.protocol.MessageType.HELLO_OK, hello.requestId(), "N2", newRemote,
                    new pt.uminho.esr.ott.protocol.ControlMessage.HelloOk(3)), second.getOutputStream()); second.getOutputStream().flush();
                child.await(t -> t.lines().filter(l -> l.contains("NeighborUp[")).count() == 2);
                assertTrue(child.text().contains(newRemote.toString())); child.send("quit");
                var leave = pt.uminho.esr.ott.protocol.MessageCodec.read(second.getInputStream()).orElseThrow();
                assertEquals(pt.uminho.esr.ott.protocol.MessageType.NODE_LEAVE, leave.type());
                assertEquals(new pt.uminho.esr.ott.protocol.ControlMessage.Leave(0), leave.payload());
                assertEquals(0, child.exit());
            }
        }
    }
    @Test void m10InterruptCleansBeforeRestoringInterruptStatus() throws Exception {
        var reading = new java.util.concurrent.CountDownLatch(1);
        var input = new InputStream() {
            public int read() throws IOException {
                reading.countDown();
                try { new java.util.concurrent.CountDownLatch(1).await(); return -1; }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException(e); }
            }
        };
        var h = new Harness(); var outcome = new AtomicInteger(-1); var restored = new AtomicBoolean();
        var coordinator = Thread.ofVirtual().start(() -> {
            outcome.set(OttApplication.run(new String[]{"--role", "RELAY", "--config", "src/main/resources/config/relay.properties"}, input,
                new PrintStream(h.output), new PrintStream(h.error), c -> h.service, System::nanoTime));
            restored.set(Thread.currentThread().isInterrupted());
        });
        try {
            assertTrue(reading.await(2, TimeUnit.SECONDS)); coordinator.interrupt(); coordinator.join(Duration.ofSeconds(3));
            assertFalse(coordinator.isAlive()); assertEquals(3, outcome.get()); assertTrue(restored.get());
            assertEquals(1, h.service.stops); assertEquals(1, h.service.reason);
        } finally { coordinator.interrupt(); coordinator.join(Duration.ofSeconds(3)); }
    }
    @Test @Timeout(15) void m03NodeUsesOneSerialObserverAndPhysicallyTerminatesBorrowedOwner() throws Exception {
        try (var d = new LiveDeployment(); var boot = new pt.uminho.esr.ott.bootstrap.BootstrapServer(
                pt.uminho.esr.ott.config.ConfigurationLoader.load(d.profiles.getFirst(), NodeRole.BOOTSTRAP))) {
            var threads = java.util.concurrent.ConcurrentHashMap.<Thread>newKeySet();
            var observed = new java.util.concurrent.CountDownLatch(1);
            var config = pt.uminho.esr.ott.config.ConfigurationLoader.load(d.profiles.get(2), NodeRole.RELAY);
            try (var node = new OverlayNode(config, UUID.randomUUID(), event -> { threads.add(Thread.currentThread()); observed.countDown(); })) {
                node.start().toCompletableFuture().get(3, TimeUnit.SECONDS);
                assertEquals(2, node.diagnostics().size());
                assertTrue(observed.await(3, TimeUnit.SECONDS), "observe a real serial event before testing its owner");
                node.requestShutdown(0, Duration.ofMillis(1500)).toCompletableFuture().get(1, TimeUnit.SECONDS);
                assertTrue(node.awaitTermination(Duration.ofMillis(1500)));
                assertEquals(1, threads.size()); assertTrue(threads.stream().noneMatch(Thread::isAlive));
                assertTrue(node.snapshot().isEmpty());
            }
        }
    }

    public static final class IoTerminationProbe {
        public static void main(String[] args) throws Exception {
            var in = new FileInputStream(FileDescriptor.in).getChannel();
            var out = new FileOutputStream(FileDescriptor.out).getChannel();
            var result = OttApplication.execute(new String[]{"--role", "RELAY", "--config", args[0]},
                java.nio.channels.Channels.newInputStream(in),
                new PrintStream(java.nio.channels.Channels.newOutputStream(out), true, StandardCharsets.UTF_8), System.err,
                c -> new FakeService(), System::nanoTime,
                () -> { try { in.close(); } catch (IOException e) { throw new UncheckedIOException(e); } },
                () -> { try { out.close(); } catch (IOException e) { throw new UncheckedIOException(e); } });
            Files.writeString(Path.of(args[1]), result.toString());
            System.exit(result.exitCode());
        }
    }
    @Test @Timeout(20) void m13PhysicalIoJoinsAreProvedBeforeJvmExitWithBlockedOutput() throws Exception {
        try (var d = new LiveDeployment()) {
            Path result = d.directory.resolve("physical-joins.txt");
            var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp",
                System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                IoTerminationProbe.class.getName(), d.profiles.get(2).toString(), result.toString())
                .redirectError(d.directory.resolve("physical-joins.err").toFile()).start();
            var feeder = Thread.ofVirtual().start(() -> {
                try (var input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                    for (int n = 0; n < 20000; n++) { input.write("neighbors\n"); input.flush(); }
                } catch (IOException expected) { /* Child stopped after bounded queue saturation. */ }
            });
            try {
                assertTrue(process.waitFor(6, TimeUnit.SECONDS)); assertEquals(3, process.exitValue());
                String actual = Files.readString(result);
                assertTrue(actual.contains("inputTerminated=true"), actual);
                assertTrue(actual.contains("outputTerminated=true"), actual);
                assertTrue(actual.contains("serviceTerminated=true"), actual);
            } finally {
                if (process.isAlive()) process.destroyForcibly();
                assertTrue(process.waitFor(3, TimeUnit.SECONDS)); feeder.join(Duration.ofSeconds(3)); assertFalse(feeder.isAlive());
            }
        }
    }

    @Test void m12ServiceWaitReceivesOnlyTheRemainingGlobalBudget() {
        var clock = new AtomicLong(1000); var budgets = new ArrayList<Duration>();
        var service = new OttApplication.RunningService() {
            public Map<String, Neighbor> neighbors() { return Map.of(); }
            public void requestShutdown(int reason, Duration budget) { budgets.add(budget); clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(1000)); }
            public boolean awaitTermination(Duration budget) { budgets.add(budget); clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(500)); return true; }
            public void close() { fail("No second complete timeout or duplicate close"); }
        };
        var output = new ByteArrayOutputStream();
        var result = OttApplication.execute(new String[]{"--role", "RELAY", "--config", "src/main/resources/config/relay.properties"},
            new ByteArrayInputStream("quit\n".getBytes(StandardCharsets.UTF_8)), new PrintStream(output), new PrintStream(output),
            c -> service, clock::get, () -> {}, () -> {});
        assertEquals(0, result.exitCode()); assertTrue(result.inputTerminated()); assertTrue(result.outputTerminated()); assertTrue(result.serviceTerminated());
        assertEquals(List.of(Duration.ofMillis(3000), Duration.ofMillis(2000)), budgets);
    }

    @Test @Timeout(15) void m15ClosingFromSerialCallbackNeverWaitsForItsOwnOwner() throws Exception {
        try (var d = new LiveDeployment(); var boot = new pt.uminho.esr.ott.bootstrap.BootstrapServer(
                pt.uminho.esr.ott.config.ConfigurationLoader.load(d.profiles.getFirst(), NodeRole.BOOTSTRAP))) {
            var reference = new AtomicReference<OverlayNode>(); var once = new AtomicBoolean();
            var returned = new java.util.concurrent.CountDownLatch(1); var callbackFailure = new AtomicReference<Throwable>();
            var config = pt.uminho.esr.ott.config.ConfigurationLoader.load(d.profiles.get(2), NodeRole.RELAY);
            try (var node = new OverlayNode(config, UUID.randomUUID(), event -> {
                if (once.compareAndSet(false, true)) {
                    try { reference.get().close(); } catch (Throwable e) { callbackFailure.set(e); }
                    finally { returned.countDown(); }
                }
            })) {
                reference.set(node); node.start();
                assertTrue(returned.await(3, TimeUnit.SECONDS)); assertNull(callbackFailure.get());
                assertTrue(node.awaitTermination(Duration.ofMillis(1500))); assertTrue(node.snapshot().isEmpty());
                node.start();
                try (var free = new java.net.ServerSocket(config.controlPort(), 1, java.net.InetAddress.getLoopbackAddress())) { assertTrue(free.isBound()); }
            }
        }
    }

    @Test void m13CleanupWriterFailureCannotPublishPrematureFinalExit() throws Exception {
        cleanupWriter(false);
    }
    @Test void m13CleanupWriterBlockClosesWithinRemainingGlobalBudget() throws Exception {
        cleanupWriter(true);
    }
    private void cleanupWriter(boolean blocked) throws Exception {
        var clock = new AtomicLong(1000);
        var closed = new AtomicBoolean(); var afterClose = new AtomicInteger(); var closes = new AtomicInteger();
        var release = new java.util.concurrent.CountDownLatch(1);
        var progress = new java.util.concurrent.CopyOnWriteArrayList<String>();
        var serviceBudgets = new ArrayList<Duration>();
        var service = new OttApplication.RunningService() {
            public Map<String, Neighbor> neighbors() { return Map.of(); }
            public void requestShutdown(int reason, Duration budget) { assertEquals(0, reason); serviceBudgets.add(budget); }
            public boolean awaitTermination(Duration budget) {
                serviceBudgets.add(budget); clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(1000)); return true;
            }
            public void close() { fail("service already physically terminated"); }
        };
        var output = new PrintStream(new ByteArrayOutputStream()) {
            @Override public void println(String line) {
                if (closed.get()) afterClose.incrementAndGet();
                progress.add(line);
                if (line.startsWith("STARTED")) return;
                // This is the cleanup record, after a successful service shutdown.
                if (!blocked) { setError(); return; }
                boolean done = false;
                while (!done) try { release.await(); done = true; }
                catch (InterruptedException expected) { /* A blocked writer needs its owned output closed. */ }
            }
        };
        try {
            var result = OttApplication.execute(new String[]{"--role", "RELAY", "--config", "src/main/resources/config/relay.properties"},
                new ByteArrayInputStream("quit\n".getBytes(StandardCharsets.UTF_8)), output, output, c -> service, clock::get,
                () -> {}, () -> {
                    closes.incrementAndGet(); closed.set(true);
                    // Account for the half of the remaining 2000 ms spent draining the writer.
                    clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(1000)); release.countDown();
                });
            assertEquals(3, result.exitCode()); assertTrue(result.serviceTerminated());
            assertTrue(result.inputTerminated()); assertTrue(result.outputTerminated());
            assertEquals(List.of(Duration.ofMillis(3000), Duration.ofMillis(3000)), serviceBudgets);
            assertEquals(blocked ? 1 : 0, closes.get()); assertEquals(0, afterClose.get());
            assertEquals(TimeUnit.MILLISECONDS.toNanos(blocked ? 2000 : 1000), clock.get() - 1000);
            assertTrue(progress.stream().noneMatch(line -> line.contains("exit=") || line.startsWith("STOPPED")), progress.toString());
            assertTrue(progress.stream().anyMatch(line -> line.contains("phase=closing-output")), progress.toString());
        } finally { release.countDown(); }
    }

    @Test @Timeout(25) void D11aIncompatibleInputRejectsOnlyPeerWhileCliAndDiscoveryRemainUsable() throws Exception {
        try(var d=new LiveDeployment();var rawPeer=new java.net.ServerSocket(d.ports[3],8,java.net.InetAddress.getLoopbackAddress())) {
            rawPeer.setSoTimeout(4000);var boot=d.start(0);boot.await(t->t.contains("READY"));var a=d.start(1);var b=d.start(2);
            var rejected=new HashSet<String>();long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(6);
            while(rejected.size()<2) {
                assertTrue(System.nanoTime()<deadline,"Both configured peers must exercise the negative handshake");
                try(var socket=rawPeer.accept()) {
                    socket.setSoTimeout(2000);var hello=pt.uminho.esr.ott.protocol.MessageCodec.read(socket.getInputStream()).orElseThrow();
                    assertEquals(2,hello.version());assertEquals(pt.uminho.esr.ott.protocol.MessageType.HELLO,hello.type());
                    var reply=new pt.uminho.esr.ott.protocol.ControlMessage(pt.uminho.esr.ott.protocol.MessageType.HELLO_OK,
                        hello.requestId(),"N2",UUID.randomUUID(),new pt.uminho.esr.ott.protocol.ControlMessage.HelloOk(3));
                    byte[] negative=pt.uminho.esr.ott.protocol.MessageCodec.encode(reply);java.nio.ByteBuffer.wrap(negative).putInt(negative.length-4,0);
                    socket.getOutputStream().write(negative);socket.getOutputStream().flush();rejected.add(hello.senderId());
                }
            }
            assertEquals(Set.of("N0","N1"),rejected);
            for(var child:List.of(a,b)) {
                child.await(t->t.contains("REQUIRED_DISCOVERY_MISSING")&&t.contains("NeighborUp[")&&t.contains("DISCOVERY_CACHE_REJECTION"));
                long replies=child.text().lines().filter(l->l.contains("NEIGHBORS ")).count();child.send("neighbors");
                child.await(t->t.lines().filter(l->l.contains("NEIGHBORS ")).count()>replies&&t.contains("state=ACTIVE"));
                assertEquals(1,child.text().lines().filter(l->l.contains("NeighborUp[")).count());
                child.send("recovery");child.await(t->t.contains("RECOVERY node=")&&t.contains("phase=ACTIVE"));
                assertFalse(child.text().contains("COMMAND_ERROR unknown command"));
                assertFalse(child.text().contains("FATAL"));assertTrue(child.process.isAlive());
            }
            var help=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java").toString(),"-cp",System.getProperty("java.class.path"),OttApplication.class.getName(),"--help")
                    .redirectErrorStream(true).start();
            try {assertTrue(help.waitFor(3,TimeUnit.SECONDS));assertEquals(0,help.exitValue());assertTrue(new String(help.getInputStream().readAllBytes(),StandardCharsets.UTF_8).contains("Usage:"));}
            finally {if(help.isAlive()){help.destroyForcibly();assertTrue(help.waitFor(3,TimeUnit.SECONDS));}}
            boot.send("quit");assertEquals(0,boot.exit());
            for(var child:List.of(a,b)){long replies=child.text().lines().filter(l->l.contains("NEIGHBORS ")).count();child.send("neighbors");child.await(t->t.lines().filter(l->l.contains("NEIGHBORS ")).count()>replies);}
            a.send("quit");b.send("quit");assertEquals(0,a.exit());assertEquals(0,b.exit());
        }
    }

    @Test void D18_recoveryCommandIsBoundedAndNotAnUnknownCommand() {
        var h=new Harness();assertEquals(0,h.run("recovery\nquit\n","--role","RELAY","--config","src/main/resources/config/relay.properties"));
        assertTrue(h.text().contains("RECOVERY node="));assertFalse(h.text().contains("unknown command"));
        assertTrue(h.text().lines().filter(line->line.contains("RECOVERY ")).allMatch(line->line.length()<=OttApplication.MAX_LOG_CHARS));
    }
}
