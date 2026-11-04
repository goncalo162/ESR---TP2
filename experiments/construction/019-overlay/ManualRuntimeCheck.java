import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.NodeRole;
import pt.uminho.esr.ott.transport.Transport;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

/** Operational manual checklist; source-file launch only, outside Maven/JUnit. */
public class ManualRuntimeCheck {
    private static int checks;
    private static final List<String> PROFILES = List.of("bootstrap", "server-1", "server-2", "relay", "client");
    private static final String DEFAULTS = """
            control.connect.timeout.ms=2000
            control.shutdown.timeout.ms=3000
            control.max.connections=64
            control.queue.capacity=256
            bootstrap.query.timeout.ms=3000
            overlay.hello.timeout.ms=3000
            overlay.retry.initial.ms=500
            overlay.retry.max.ms=5000
            overlay.retry.jitter.percent=10
            node.events.capacity=4096
            monitor.probe.interval.ms=1000
            monitor.probe.timeout.ms=1000
            monitor.probe.failures=3
            monitor.ewma.alpha=0.2
            routing.announce.interval.ms=2000
            routing.candidate.ttl.ms=8000
            routing.cache.capacity=4096
            routing.cache.ttl.ms=16000
            routing.flood.max.per.sec=10
            routing.activate.timeout.ms=3000
            routing.activate.attempts=3
            routing.switch.improvement.percent=20
            routing.switch.observations=3
            routing.switch.min.dwell.ms=5000
            subscription.lease.ms=6000
            subscription.renew.ms=2000
            video.egress.capacity=256
            video.reassembly.max.frames=8
            video.reassembly.deadline.ms=250
            source.max.frame.bytes=99999
            """;
    private static final String OVERRIDES = """
            control.connect.timeout.ms=1111
            control.shutdown.timeout.ms=2222
            control.max.connections=13
            control.queue.capacity=7
            bootstrap.query.timeout.ms=1234
            overlay.hello.timeout.ms=1234
            overlay.retry.initial.ms=750
            overlay.retry.max.ms=6000
            overlay.retry.jitter.percent=25
            node.events.capacity=99
            monitor.probe.interval.ms=1500
            monitor.probe.timeout.ms=1750
            monitor.probe.failures=5
            monitor.ewma.alpha=0.35
            routing.announce.interval.ms=1000
            routing.candidate.ttl.ms=9000
            routing.cache.capacity=123
            routing.cache.ttl.ms=18000
            routing.flood.max.per.sec=7
            routing.activate.timeout.ms=1200
            routing.activate.attempts=4
            routing.switch.improvement.percent=35
            routing.switch.observations=5
            routing.switch.min.dwell.ms=6500
            subscription.lease.ms=9000
            subscription.renew.ms=1500
            video.egress.capacity=123
            video.reassembly.max.frames=4
            video.reassembly.deadline.ms=333
            source.max.frame.bytes=50000
            """;

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
        System.out.println("CHECK_PASS " + label);
    }
    private static Map<String, String> literals(String text) {
        var values = new TreeMap<String, String>();
        text.lines().forEach(line -> { var parts = line.split("=", 2); values.put(parts[0], parts[1]); });
        return values;
    }
    private static Map<String, String> values(RuntimeConfiguration r) {
        var v = new TreeMap<String, String>();
        v.put("control.connect.timeout.ms", Long.toString(r.control().connectTimeout().toMillis()));
        v.put("control.shutdown.timeout.ms", Long.toString(r.control().shutdownTimeout().toMillis()));
        v.put("control.max.connections", Integer.toString(r.control().maxConnections()));
        v.put("control.queue.capacity", Integer.toString(r.control().queueCapacity()));
        v.put("bootstrap.query.timeout.ms", Long.toString(r.control().bootstrapQueryTimeout().toMillis()));
        v.put("overlay.hello.timeout.ms", Long.toString(r.overlay().helloTimeout().toMillis()));
        v.put("overlay.retry.initial.ms", Long.toString(r.overlay().retryInitial().toMillis()));
        v.put("overlay.retry.max.ms", Long.toString(r.overlay().retryMax().toMillis()));
        v.put("overlay.retry.jitter.percent", Integer.toString(r.overlay().retryJitterPercent()));
        v.put("node.events.capacity", Integer.toString(r.overlay().eventsCapacity()));
        v.put("monitor.probe.interval.ms", Long.toString(r.monitoring().probeInterval().toMillis()));
        v.put("monitor.probe.timeout.ms", Long.toString(r.monitoring().probeTimeout().toMillis()));
        v.put("monitor.probe.failures", Integer.toString(r.monitoring().probeFailures()));
        v.put("monitor.ewma.alpha", Double.toString(r.monitoring().ewmaAlpha()));
        v.put("routing.announce.interval.ms", Long.toString(r.routing().announceInterval().toMillis()));
        v.put("routing.candidate.ttl.ms", Long.toString(r.routing().candidateTtl().toMillis()));
        v.put("routing.cache.capacity", Integer.toString(r.routing().cacheCapacity()));
        v.put("routing.cache.ttl.ms", Long.toString(r.routing().cacheTtl().toMillis()));
        v.put("routing.flood.max.per.sec", Integer.toString(r.routing().floodMaxPerSecond()));
        v.put("routing.activate.timeout.ms", Long.toString(r.routing().activateTimeout().toMillis()));
        v.put("routing.activate.attempts", Integer.toString(r.routing().activateAttempts()));
        v.put("routing.switch.improvement.percent", Integer.toString(r.routing().switchImprovementPercent()));
        v.put("routing.switch.observations", Integer.toString(r.routing().switchObservations()));
        v.put("routing.switch.min.dwell.ms", Long.toString(r.routing().switchMinDwell().toMillis()));
        v.put("subscription.lease.ms", Long.toString(r.subscription().lease().toMillis()));
        v.put("subscription.renew.ms", Long.toString(r.subscription().renew().toMillis()));
        v.put("video.egress.capacity", Integer.toString(r.video().egressCapacity()));
        v.put("video.reassembly.max.frames", Integer.toString(r.video().reassemblyMaxFrames()));
        v.put("video.reassembly.deadline.ms", Long.toString(r.video().reassemblyDeadline().toMillis()));
        v.put("source.max.frame.bytes", Integer.toString(r.source().maxFrameBytes()));
        return v;
    }
    private static Map<String, String> hashes(Path dir) throws Exception {
        var result = new TreeMap<String, String>();
        try (var files = Files.list(dir)) {
            for (Path file : files.filter(p -> p.getFileName().toString().endsWith(".properties")).toList())
                result.put(file.getFileName().toString(), HexFormat.of().formatHex(
                        MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))));
        }
        return result;
    }
    private static boolean rejectsKey(Path path, NodeRole role, String key) throws Exception {
        try { ConfigurationLoader.load(path, role); return false; }
        catch (ConfigurationException expected) { return expected.problems().stream().anyMatch(p -> p.key().equals(key)); }
    }
    private static void run(Path profiles, String operator) throws Exception {
        System.out.println("MANUAL_START " + Instant.now() + " operator=" + operator + " Java=" + System.getProperty("java.version"));
        var before = hashes(profiles);
        var expectedDefaults = literals(DEFAULTS);
        var nodes = ConfigurationLoader.loadDeployment(PROFILES.stream().map(p -> profiles.resolve(p + ".properties")).toList());
        check(nodes.size() == 5, "defaults_five_profiles");
        check(nodes.stream().map(NodeConfiguration::role).collect(java.util.stream.Collectors.toSet()).equals(Set.of(NodeRole.values()))
                && nodes.stream().filter(n -> n.role() == NodeRole.SERVER).count() == 2, "defaults_four_roles_two_servers");
        check(nodes.stream().flatMap(n -> n.streams().stream()).map(StreamConfiguration::streamId).distinct().count() == 4,
                "defaults_four_distinct_streams");
        for (var node : nodes) check(values(node.runtime()).equals(expectedDefaults), "defaults_30_values_" + node.nodeId());
        expectedDefaults.forEach((key, value) -> System.out.println("DEFAULT " + key + "=" + value));

        Path temporary = Files.createTempDirectory("ott-imp002-manual-");
        System.out.println("TEMP_CONFIG_DIR " + temporary);
        try {
            for (String filename : before.keySet()) Files.copy(profiles.resolve(filename), temporary.resolve(filename));
            Path server = temporary.resolve("server-1.properties");
            Files.writeString(server, OVERRIDES, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            var loaded = ConfigurationLoader.load(server, NodeRole.SERVER);
            check(values(loaded.runtime()).equals(literals(OVERRIDES)), "overrides_all_30_values");
            values(loaded.runtime()).forEach((key, value) -> System.out.println("OVERRIDE " + key + "=" + value));
            var control = loaded.runtime().control();
            // Only the immutable record is created. No Transport, listener, bind or dial.
            var consumed = new Transport.Options(control.queueCapacity(), control.maxConnections(), control.connectTimeout(), control.shutdownTimeout());
            check(consumed.queueCapacity() == 7, "consumer_queue_7");
            check(consumed.maxConnections() == 13, "consumer_connections_13");
            check(consumed.connectTimeout().equals(Duration.ofMillis(1111)), "consumer_connect_1111_ms");
            check(consumed.shutdownTimeout().equals(Duration.ofMillis(2222)), "consumer_shutdown_2222_ms");
            System.out.println("CONSUMER " + consumed);
            var legacy = new NodeConfiguration(loaded.sourceFile(), loaded.nodeId(), loaded.role(), loaded.bindHost(), loaded.advertisedHost(),
                    loaded.controlPort(), loaded.dataPort(), loaded.bootstrap(), loaded.topologyFile(), loaded.streamsFile(), loaded.mediaBase(), loaded.topology(), loaded.streams());
            check(values(legacy.runtime()).equals(expectedDefaults) && legacy.nodeId().equals(loaded.nodeId()), "legacy_constructor_defaults");
            boolean immutable = false;
            try { legacy.streams().clear(); } catch (UnsupportedOperationException expected) { immutable = true; }
            check(immutable, "legacy_collections_immutable");

            String badText = Files.readString(profiles.resolve("client.properties")).replace("control.port=5400", "control.port=0")
                    + "control.queue.capacity=0\nmonitor.ewma.alpha=NaN\nrouting.candidate.ttl.ms=1\nvideo.reassembly.max.frames=9\n";
            Path invalid = temporary.resolve("invalid-client.properties");
            Files.writeString(invalid, badText, StandardCharsets.UTF_8);
            ConfigurationException failure = null;
            NodeConfiguration partial = null;
            try { partial = ConfigurationLoader.load(invalid, NodeRole.CLIENT); }
            catch (ConfigurationException expected) { failure = expected; }
            check(failure != null && partial == null, "invalid_no_partial_configuration");
            var problems = Objects.requireNonNull(failure).problems();
            var keys = problems.stream().map(ConfigurationProblem::key).collect(java.util.stream.Collectors.toSet());
            check(keys.containsAll(Set.of("control.port", "control.queue.capacity", "monitor.ewma.alpha", "routing.candidate.ttl.ms", "video.reassembly.max.frames")),
                    "invalid_five_independent_keys_aggregated");
            check(problems.stream().allMatch(p -> p.file().equals(invalid.toAbsolutePath())), "invalid_diagnostics_have_source");
            problems.forEach(p -> System.out.println("EXPECTED_PROBLEM " + p));
            boolean problemsImmutable = false;
            try { problems.clear(); } catch (UnsupportedOperationException expected) { problemsImmutable = true; }
            check(problemsImmutable, "invalid_problem_list_immutable");
            check(Files.readString(invalid).equals(badText), "invalid_input_not_modified");
            check(values(ConfigurationLoader.load(profiles.resolve("client.properties"), NodeRole.CLIENT).runtime()).equals(expectedDefaults), "defaults_not_mutated_after_overrides_or_failure");
            Path alpha = temporary.resolve("invalid-alpha.properties");
            Files.writeString(alpha, Files.readString(profiles.resolve("client.properties")) + "monitor.ewma.alpha=2e-1\n");
            check(rejectsKey(alpha, NodeRole.CLIENT, "monitor.ewma.alpha"), "approved_alpha_grammar_rejects_exponent");
            Path bootstrap = temporary.resolve("invalid-bootstrap.properties");
            Files.writeString(bootstrap, Files.readString(profiles.resolve("bootstrap.properties")) + "overlay.hello.timeout.ms=3000\n");
            check(rejectsKey(bootstrap, NodeRole.BOOTSTRAP, "overlay.hello.timeout.ms"), "bootstrap_rejects_overlay_override_equal_to_default");
            check(hashes(profiles).equals(before), "original_profiles_preserved");
        } finally {
            try (var files = Files.walk(temporary)) {
                for (var file : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
            }
        }
        check(!Files.exists(temporary), "temporary_files_removed");
        System.out.println("MANUAL_PASS " + Instant.now() + " checks=" + checks + " operator=" + operator);
    }
    public static void main(String[] args) {
        try {
            if (args.length != 2) throw new IllegalArgumentException("Usage: ManualRuntimeCheck.java <profiles-directory> <operator>");
            run(Path.of(args[0]).toAbsolutePath().normalize(), args[1]);
        } catch (Throwable error) {
            System.out.println("MANUAL_FAIL " + error);
            error.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
