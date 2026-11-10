package pt.uminho.esr.ott.app;

import pt.uminho.esr.ott.bootstrap.BootstrapServer;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.*;
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
public final class OttApplication {
    public static final int MAX_LINE_CHARS = 4096;
    public static final int MAX_LOG_CHARS = 4096;
    public record Invocation(boolean help, Optional<NodeRole> role, Optional<Path> config) {
        public Invocation { Objects.requireNonNull(role); Objects.requireNonNull(config); }
    }
    interface ServiceFactory { RunningService open(NodeConfiguration configuration) throws Exception; }
    interface RunningService extends AutoCloseable {
        Map<String, Neighbor> neighbors();
        void requestShutdown(int reason, Duration remainingBudget);
        boolean awaitTermination(Duration remainingBudget);
        default Optional<Throwable> failure() { return Optional.empty(); }
        @Override void close();
    }
    private OttApplication() { }
    public static Invocation parse(String[] arguments) {
        Objects.requireNonNull(arguments);
        if (arguments.length == 1 && "--help".equals(arguments[0]))
            return new Invocation(true, Optional.empty(), Optional.empty());
        NodeRole role = null; Path config = null;
        for (int i = 0; i < arguments.length; i++) {
            String option = arguments[i];
            if ("--role".equals(option) && role == null && i + 1 < arguments.length) {
                try { role = NodeRole.valueOf(arguments[++i]); }
                catch (RuntimeException e) { throw new IllegalArgumentException("Invalid --role", e); }
            } else if ("--config".equals(option) && config == null && i + 1 < arguments.length) {
                String value = arguments[++i];
                if (value == null || value.isBlank() || value.startsWith("--")) throw new IllegalArgumentException("Invalid --config");
                config = Path.of(value);
            } else throw new IllegalArgumentException("Unknown, repeated or incomplete option: " + option);
        }
        if (role == null || config == null) throw new IllegalArgumentException("--role and --config required");
        return new Invocation(false, Optional.of(role), Optional.of(config));
    }
    public static int run(String[] arguments, InputStream input, PrintStream output, PrintStream error) {
        return execute(arguments, input, output, error, null, System::nanoTime, () -> {}, () -> {}).exitCode();
    }
    static int run(String[] arguments, InputStream input, PrintStream output, PrintStream error,
                   ServiceFactory services, LongSupplier clock) {
        return execute(arguments, input, output, error, Objects.requireNonNull(services), clock, () -> {}, () -> {}).exitCode();
    }
    /** Own interruptible descriptor channels rather than closing a locked PrintStream wrapper. */
    public static void main(String[] arguments) throws IOException {
        var in = new FileInputStream(FileDescriptor.in).getChannel();
        var out = new FileOutputStream(FileDescriptor.out).getChannel();
        var err = new FileOutputStream(FileDescriptor.err).getChannel();
        var result = execute(arguments, Channels.newInputStream(in),
                new PrintStream(Channels.newOutputStream(out), true, StandardCharsets.UTF_8),
                new PrintStream(Channels.newOutputStream(err), true, StandardCharsets.UTF_8), null,
                System::nanoTime, () -> closeChannel(in), () -> { closeChannel(out); closeChannel(err); });
        System.exit(result.exitCode());
    }
    private static void closeChannel(Channel channel) {
        try { channel.close(); } catch (IOException ignored) { /* Closing is followed by physical joins. */ }
    }
    private record InputEvent(String line, boolean eof) { }
    private record OutputEvent(String text, boolean error) { }
    record ExecutionResult(int exitCode, boolean inputTerminated, boolean outputTerminated, boolean serviceTerminated) { }
    static ExecutionResult execute(String[] arguments, InputStream input, PrintStream output, PrintStream error,
                               ServiceFactory factory, LongSupplier clock, Runnable closeInput, Runnable closeOutput) {
        final Invocation invocation;
        final NodeConfiguration config;
        try {
            invocation = parse(arguments);
            if (invocation.help()) { output.println("Usage: --role BOOTSTRAP|SERVER|RELAY|CLIENT --config <file>; commands: neighbors, quit"); return new ExecutionResult(0, true, true, true); }
            config = ConfigurationLoader.load(invocation.config().orElseThrow(), invocation.role().orElseThrow());
        } catch (ConfigurationException | IllegalArgumentException e) { error.println(bounded("CONFIG_ERROR " + e.getMessage())); return new ExecutionResult(2, true, true, true); }
        var console = new Console(input, output, error, config, clock);
        var hook = new Thread(() -> {
            console.stop(false);
            try { console.finished.await(config.runtime().control().shutdownTimeout().toNanos(), TimeUnit.NANOSECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "ott-shutdown-hook");
        RunningService service = null;
        boolean interrupted = false;
        boolean inputDone = true, outputDone = true, serviceDone = true;
        try {
            Runtime.getRuntime().addShutdownHook(hook);
            console.writer.start();
            service = factory == null ? open(config, console) : factory.open(config);
            console.log("STARTED node=" + config.nodeId() + " role=" + config.role() + " pid=" + ProcessHandle.current().pid(), false);
            console.reader.start();
            while (!console.stopped.get()) {
                var failure = service.failure();
                if (failure.isPresent()) { console.log("FATAL " + failure.orElseThrow(), true); console.stop(true); break; }
                var command = console.commands.poll(20, TimeUnit.MILLISECONDS);
                if (command == null) continue;
                if (command.eof()) { console.stop(false); break; }
                String line = command.line().strip();
                if (line.isEmpty()) continue;
                if (line.equals("quit")) console.stop(false);
                else if (line.equals("neighbors")) {
                    var neighbors = service.neighbors();
                    console.log("NEIGHBORS node=" + config.nodeId() + " role=" + config.role() + " count=" + neighbors.size(), false);
                    neighbors.values().stream().sorted(Comparator.comparing(Neighbor::nodeId)).forEach(n ->
                        console.log("NEIGHBOR node=" + n.nodeId() + " state=" + n.state() + " session=" + n.session(), false));
                } else if (line.equals("catalog") || line.equals("play") || line.startsWith("play "))
                    console.log("NOT_IMPLEMENTED " + line.split(" ", 2)[0], false);
                else console.log("COMMAND_ERROR unknown command", true);
            }
        } catch (InterruptedException e) { console.stop(true); interrupted = true; /* Restore after bounded cleanup. */ }
        catch (Exception e) { console.log("FATAL " + bounded(e.getMessage()), true); console.stop(true); }
        finally {
            console.stop(false);
            // Close/cancel the owned input before waiting on the network budget.
            console.reader.interrupt(); closeInput.run();
            if (service != null) {
                try {
                    service.requestShutdown(console.failed.get() ? 1 : 0, console.left());
                    serviceDone = service.awaitTermination(console.left());
                    if (!serviceDone) { console.failed.set(true); service.close(); }
                    if (service.failure().isPresent()) console.failed.set(true);
                } catch (RuntimeException e) {
                    serviceDone = false; console.failed.set(true); console.log("CLEANUP_ERROR " + e.getMessage(), true);
                    try { service.close(); } catch (RuntimeException ignored) { /* Preserve the first cleanup failure. */ }
                }
            }
            inputDone = join(console.reader, console.left());
            if (!inputDone) console.failed.set(true);
            // This record precedes the writer's final drain/join, so it cannot announce a final status.
            // The returned ExecutionResult / OS exit status is authoritative after all cleanup.
            console.log("SHUTDOWN node=" + config.nodeId() + " phase=closing-output", false);
            console.outputStopping.set(true);
            // Leave time for closing a blocked descriptor and joining its writer.
            outputDone = join(console.writer, console.left().dividedBy(2));
            if (!outputDone) {
                console.failed.set(true); console.writer.interrupt(); closeOutput.run();
                outputDone = join(console.writer, console.left());
                if (!outputDone) console.failed.set(true);
            }
            try { Runtime.getRuntime().removeShutdownHook(hook); }
            catch (IllegalStateException ignored) { /* JVM hook is already running. */ }
            console.finished.countDown();
        }
        if (interrupted) Thread.currentThread().interrupt();
        return new ExecutionResult(console.failed.get() ? 3 : 0, inputDone, outputDone, serviceDone);
    }
    private static RunningService open(NodeConfiguration config, Console console) throws IOException {
        if (config.role() == NodeRole.BOOTSTRAP) {
            var bootstrap = new BootstrapServer(config);
            console.log("READY node=" + config.nodeId() + " role=BOOTSTRAP instance=internal-not-exposed", false);
            return new RunningService() {
                private boolean closed; private RuntimeException failure;
                public Map<String, Neighbor> neighbors() { return Map.of(); }
                public synchronized void requestShutdown(int reason, Duration budget) {
                    if (closed) return; closed = true;
                    try { bootstrap.close(); } catch (RuntimeException e) { failure = e; }
                }
                public boolean awaitTermination(Duration budget) { if (failure != null) throw failure; return closed; }
                public void close() { requestShutdown(1, Duration.ZERO); }
            };
        }
        UUID instance = UUID.randomUUID();
        var node = new OverlayNode(config, instance, event -> console.log("EVENT node=" + config.nodeId()
                + " instance=" + instance + " " + event, event instanceof NeighborManager.Received r && r.message().type() == pt.uminho.esr.ott.protocol.MessageType.ERROR));
        node.start().whenComplete((v, error) -> {
            if (error == null) console.log("READY node=" + config.nodeId() + " instance=" + instance, false);
        });
        return new RunningService() {
            public Map<String, Neighbor> neighbors() { return node.diagnostics(); }
            public void requestShutdown(int reason, Duration budget) { node.requestShutdown(reason, budget); }
            public boolean awaitTermination(Duration budget) { return node.awaitTermination(budget); }
            public Optional<Throwable> failure() { return node.failure(); }
            public void close() { node.close(); }
        };
    }
    private static boolean join(Thread thread, Duration budget) {
        if (thread.getState() == Thread.State.NEW) return true;
        if (thread == Thread.currentThread()) return false;
        try { if (!budget.isZero()) thread.join(budget); return !thread.isAlive(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
    }
    private static String bounded(String text) {
        if (text == null) return "unspecified";
        String marker = "...[truncated]";
        return text.length() <= MAX_LOG_CHARS ? text : text.substring(0, MAX_LOG_CHARS - marker.length()) + marker;
    }
    private static final class Console {
        final ArrayBlockingQueue<InputEvent> commands;
        final ArrayBlockingQueue<OutputEvent> outputs;
        final AtomicBoolean stopped = new AtomicBoolean(), failed = new AtomicBoolean(), outputStopping = new AtomicBoolean();
        final CountDownLatch finished = new CountDownLatch(1);
        final Thread reader, writer;
        final LongSupplier clock; final long budget;
        volatile long stopAt;
        Console(InputStream input, PrintStream output, PrintStream error, NodeConfiguration config,
                LongSupplier clock) {
            this.clock = Objects.requireNonNull(clock); budget = config.runtime().control().shutdownTimeout().toNanos();
            commands = new ArrayBlockingQueue<>(config.runtime().overlay().eventsCapacity());
            outputs = new ArrayBlockingQueue<>(config.runtime().control().queueCapacity());
            reader = Thread.ofVirtual().name("ott-cli-input").unstarted(() -> read(input));
            writer = Thread.ofVirtual().name("ott-cli-output").unstarted(() -> write(output, error));
        }
        synchronized void stop(boolean fatal) {
            if (fatal) failed.set(true);
            if (stopped.compareAndSet(false, true)) stopAt = clock.getAsLong();
        }
        Duration left() { return Duration.ofNanos(Math.max(0, budget - (clock.getAsLong() - stopAt))); }
        void log(String text, boolean error) {
            if (!outputs.offer(new OutputEvent(bounded(text), error))) stop(true);
        }
        void command(InputEvent event) { if (!stopped.get() && !commands.offer(event)) { log("COMMAND_OVERFLOW", true); stop(true); } }
        void read(InputStream input) {
            try {
                var chars = new InputStreamReader(input, StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT).onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT));
                var line = new StringBuilder(); boolean overlong = false;
                while (!stopped.get()) {
                    int value = chars.read();
                    if (value < 0) { if (!overlong && !line.isEmpty()) command(new InputEvent(line.toString(), false)); command(new InputEvent("", true)); return; }
                    if (value == '\n') {
                        if (!overlong) command(new InputEvent(line.toString(), false));
                        line.setLength(0); overlong = false;
                    } else if (!overlong) {
                        if (line.length() == MAX_LINE_CHARS) { log("COMMAND_ERROR line exceeds 4096 characters", true); line.setLength(0); overlong = true; }
                        else line.append((char)value);
                    }
                }
            } catch (IOException | RuntimeException e) { if (!stopped.get()) { log("INPUT_ERROR " + e.getMessage(), true); stop(true); } }
        }
        void write(PrintStream output, PrintStream error) {
            try {
                while (!outputStopping.get() || !outputs.isEmpty()) {
                    var item = outputs.poll(20, TimeUnit.MILLISECONDS);
                    if (item == null) continue;
                    var target = item.error() ? error : output;
                    target.println(item.text());
                    if (target.checkError()) { stop(true); return; }
                }
            } catch (InterruptedException e) { if (!outputStopping.get()) stop(true); Thread.currentThread().interrupt(); }
            catch (RuntimeException e) { stop(true); }
        }
    }
}
