package pt.uminho.esr.ott.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pt.uminho.esr.ott.overlay.NodeRole;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationLoaderTest {
    private static final String A = "00000000-0000-4000-8000-000000000001";
    private static final String B = "00000000-0000-4000-8000-000000000002";
    private static final List<String> PROFILES = List.of(
            "bootstrap", "server-1", "server-2", "relay", "client");
    @TempDir Path temp;

    @Test
    void loadsFiveRealProfilesAndFourDistinctOwnedStreams() throws Exception {
        var nodes = ConfigurationLoader.loadDeployment(examplePaths(exampleDirectory()));
        assertEquals(5, nodes.size());
        assertEquals(2, nodes.stream().filter(n -> n.role() == NodeRole.SERVER).count());
        var streams = nodes.stream().flatMap(n -> n.streams().stream()).toList();
        assertEquals(4, streams.size());
        assertEquals(4, streams.stream().map(StreamConfiguration::streamId).distinct().count());
        assertEquals(Set.of("S1", "S2"), streams.stream().map(StreamConfiguration::serverId).collect(Collectors.toSet()));
        for (var n : nodes) {
            if (n.role() == NodeRole.SERVER) {
                assertEquals(2, n.streams().size());
                assertTrue(n.streams().stream().allMatch(s -> s.serverId().equals(n.nodeId())));
            }
        }
        var root = nodes.stream().filter(n -> n.role() == NodeRole.BOOTSTRAP).findFirst().orElseThrow();
        assertEquals(0, root.dataPort());
        assertTrue(root.bootstrap().isEmpty());
        assertEquals(Set.of("S2", "R1"), root.topology().orElseThrow().nodes().get("S1").neighbors());
        assertThrows(UnsupportedOperationException.class, () -> nodes.clear());
    }

    @Test
    void collectionsAreImmutable() throws Exception {
        var bootstrap = ConfigurationLoader.load(exampleDirectory().resolve("bootstrap.properties"), NodeRole.BOOTSTRAP);
        var topology = bootstrap.topology().orElseThrow();
        assertThrows(UnsupportedOperationException.class, () -> topology.nodes().clear());
        assertThrows(UnsupportedOperationException.class, () -> topology.nodes().get("S1").neighbors().clear());
        var server = ConfigurationLoader.load(exampleDirectory().resolve("server-1.properties"), NodeRole.SERVER);
        assertThrows(UnsupportedOperationException.class, () -> server.streams().clear());
    }

    @Test
    void resolvesPathsAgainstConfigurationNotWorkingDirectory() throws Exception {
        Path nested = temp.resolve("pasta com espaços/conf");
        Files.createDirectories(nested);
        write(nested.resolve("catalog.properties"), catalog());
        Path file = write(nested.resolve("server.properties"), profile(NodeRole.SERVER, "S1", 5100, 5101)
                + "streams.file=catalog.properties\nmedia.base=../media\n");
        var result = ConfigurationLoader.load(file, NodeRole.SERVER);
        assertEquals(nested.resolve("catalog.properties").toAbsolutePath(), result.streamsFile().orElseThrow());
        assertEquals(nested.getParent().resolve("media/a.Mjpeg").toAbsolutePath(), result.streams().getFirst().mediaFile());
        assertFalse(Files.exists(result.streams().getFirst().mediaFile()));
    }

    @Test
    void acceptsOneCharacterIdAndUnresolvedHostnameWithoutNetwork() throws Exception {
        Path p = client(profile(NodeRole.CLIENT, "C", 5400, 5401)
                .replace("advertised.host=127.0.0.1", "advertised.host=unit-test.invalid"));
        var n = ConfigurationLoader.load(p, NodeRole.CLIENT);
        assertEquals("C", n.nodeId());
        assertEquals("unit-test.invalid", n.advertisedHost());
    }

    @Test
    void aggregatesMissingFieldsWithFileAndProperty() throws Exception {
        Path p = write(temp.resolve("empty.properties"), "node.role=CLIENT\n");
        var failure = failure(p, NodeRole.CLIENT);
        assertTrue(failure.problems().size() >= 6);
        hasKey(failure, "node.id");
        hasKey(failure, "control.port");
        assertTrue(failure.problems().stream().allMatch(e -> e.file().equals(p.toAbsolutePath())));
        assertThrows(UnsupportedOperationException.class, () -> failure.problems().clear());
    }

    @Test
    void missingFileIsAnExplicitConfigurationProblem() {
        var failure = failure(temp.resolve("missing.properties"), NodeRole.CLIENT);
        hasKey(failure, "<file>");
        assertTrue(failure.getMessage().contains("missing.properties"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"node.id=C2", "node.\\u0069d=C2"})
    void rejectsDuplicateDecodedProperties(String duplicate) throws Exception {
        var failure = failure(client(profile(NodeRole.CLIENT, "C1", 5400, 5401) + duplicate + "\n"), NodeRole.CLIENT);
        hasKey(failure, "node.id");
        assertTrue(failure.getMessage().contains("duplicada"));
    }

    @Test
    void rejectsUnknownAndRoleInapplicableKeys() throws Exception {
        var failure = failure(client(profile(NodeRole.CLIENT, "C1", 5400, 5401)
                + "control.prt=5000\nstreams.file=unexpected.properties\n"), NodeRole.CLIENT);
        hasKey(failure, "control.prt");
        hasKey(failure, "streams.file");
    }

    @Test
    void rejectsExpectedRoleMismatch() throws Exception {
        hasKey(failure(client(profile(NodeRole.CLIENT, "C1", 5400, 5401)), NodeRole.RELAY), "node.role");
    }

    @ParameterizedTest
    @ValueSource(strings = {"server", "OTHER", ""})
    void rejectsInvalidRoles(String role) throws Exception {
        hasKey(failure(client(profile(NodeRole.CLIENT, "C1", 5400, 5401)
                .replace("node.role=CLIENT", "node.role=" + role)), NodeRole.CLIENT), "node.role");
    }

    @ParameterizedTest
    @ValueSource(strings = {"bad/id", "á", "12345678901234567890123456789012345"})
    void rejectsInvalidIds(String id) throws Exception {
        hasKey(failure(client(profile(NodeRole.CLIENT, id, 5400, 5401)), NodeRole.CLIENT), "node.id");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "65536", "999999999999999999999", "abc", ""})
    void rejectsInvalidPorts(String port) throws Exception {
        String data = profile(NodeRole.CLIENT, "C1", 5400, 5401).replace("control.port=5400", "control.port=" + port);
        hasKey(failure(client(data), NodeRole.CLIENT), "control.port");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0.0.0", "::", "[::]", "::%eth0", "0:0:0:0:0:0:0:0", "bad host", "256.1.2.3", "127.0.0.1:5000"})
    void rejectsUnusableAdvertisedHosts(String host) throws Exception {
        hasKey(failure(client(profile(NodeRole.CLIENT, "C1", 5400, 5401)
                .replace("advertised.host=127.0.0.1", "advertised.host=" + host)), NodeRole.CLIENT), "advertised.host");
    }

    @Test
    void permitsWildcardBindAndLiteralIpv6WithoutLookup() throws Exception {
        var p = client(profile(NodeRole.CLIENT, "C1", 5400, 5401)
                .replace("bind.host=127.0.0.1", "bind.host=::")
                .replace("advertised.host=127.0.0.1", "advertised.host=::1"));
        var c = ConfigurationLoader.load(p, NodeRole.CLIENT);
        assertEquals("::", c.bindHost());
        assertEquals("::1", c.advertisedHost());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "R1,,C1", "R1,R1", "R1,C1,"})
    void rejectsMalformedNodeLists(String list) throws Exception {
        hasKey(failure(bootstrap(topology().replace("node.ids=R1,C1", "node.ids=" + list)), NodeRole.BOOTSTRAP), "node.ids");
    }

    @Test
    void rejectsSelfUnknownAndAsymmetricNeighbors() throws Exception {
        hasKey(failure(bootstrap(topology().replace("node.R1.neighbors=C1", "node.R1.neighbors=R1")), NodeRole.BOOTSTRAP), "node.R1.neighbors");
        hasKey(failure(bootstrap(topology().replace("node.R1.neighbors=C1", "node.R1.neighbors=X")), NodeRole.BOOTSTRAP), "node.R1.neighbors");
        String triangle = topology().replace("node.ids=R1,C1", "node.ids=R1,C1,R2")
                .replace("node.R1.neighbors=C1", "node.R1.neighbors=R2")
                + "node.R2.host=127.0.0.1\nnode.R2.controlPort=5500\nnode.R2.dataPort=5501\nnode.R2.role=RELAY\nnode.R2.neighbors=R1\n";
        hasKey(failure(bootstrap(triangle), NodeRole.BOOTSTRAP), "node.C1.neighbors");
    }

    @Test
    void rejectsDuplicateNeighborAndUnknownTopologyKey() throws Exception {
        var failure = failure(bootstrap(topology().replace("node.R1.neighbors=C1", "node.R1.neighbors=C1,C1")
                + "node.R1.hots=127.0.0.1\n"), NodeRole.BOOTSTRAP);
        hasKey(failure, "node.R1.neighbors");
        hasKey(failure, "node.R1.hots");
    }

    @Test
    void rejectsDuplicateEndpointsWithinSameTransport() throws Exception {
        var failure = failure(bootstrap(topology().replace("node.C1.controlPort=5400", "node.C1.controlPort=5300")), NodeRole.BOOTSTRAP);
        hasKey(failure, "node.C1.controlPort");
    }

    @Test
    void allowsSameNumericPortForTcpAndUdp() throws Exception {
        var p = bootstrap(topology().replace("node.R1.dataPort=5301", "node.R1.dataPort=5400"));
        assertEquals(5400, ConfigurationLoader.load(p, NodeRole.BOOTSTRAP).topology().orElseThrow().nodes().get("R1").dataPort());
    }

    @Test
    void rejectsBootstrapAsForwardingNode() throws Exception {
        hasKey(failure(bootstrap(topology().replace("node.R1.role=RELAY", "node.R1.role=BOOTSTRAP")), NodeRole.BOOTSTRAP), "node.R1.role");
    }

    @Test
    void missingTopologyFileDoesNotReturnPartialNode() throws Exception {
        Path p = write(temp.resolve("bootstrap.properties"), profile(NodeRole.BOOTSTRAP, "B", 5002, 0) + "topology.file=missing.properties\n");
        var error = failure(p, NodeRole.BOOTSTRAP);
        hasKey(error, "<file>");
        assertEquals(temp.resolve("missing.properties"), error.problems().getFirst().file());
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-uuid", "1-1-1-1-1", "00000000-0000-4000-8000-00000000000Z"})
    void rejectsNonCanonicalUuid(String id) throws Exception {
        hasKey(failure(server(catalog().replace(A, id)), NodeRole.SERVER), "stream.ids");
    }

    @Test
    void rejectsRepeatedUuid() throws Exception {
        hasKey(failure(server(catalog().replace("stream.ids=" + A + "," + B, "stream.ids=" + A + "," + A)), NodeRole.SERVER), "stream.ids");
    }

    @Test
    void rejectsServerWithOnlyOneStream() throws Exception {
        hasKey(failure(server(catalog().replace("stream.ids=" + A + "," + B, "stream.ids=" + A)), NodeRole.SERVER), "stream.ids");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "61", "-1", "abc"})
    void rejectsInvalidFps(String fps) throws Exception {
        hasKey(failure(server(catalog().replace(".fps=10", ".fps=" + fps)), NodeRole.SERVER), "stream." + A + ".fps");
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE", "1", "yes"})
    void rejectsNonBooleanLoop(String value) throws Exception {
        hasKey(failure(server(catalog().replace(".loop=true", ".loop=" + value)), NodeRole.SERVER), "stream." + A + ".loop");
    }

    @Test
    void enforcesTitleUnicodeLimitsAndUtf8() throws Exception {
        String value = "🎬".repeat(128);
        var valid = ConfigurationLoader.load(server(catalog().replace("title=A", "title=" + value)), NodeRole.SERVER);
        assertEquals(value, valid.streams().getFirst().title());
        hasKey(failure(server(catalog().replace("title=A", "title=" + value + "🎬")), NodeRole.SERVER), "stream." + A + ".title");
        hasKey(failure(server(catalog().replace("title=A", "title=")), NodeRole.SERVER), "stream." + A + ".title");
        hasKey(failure(server(catalog().replace("title=A", "title=\\uD800")), NodeRole.SERVER), "stream." + A + ".title");
    }

    @Test
    void rejectsAliasMediaAndEscapeOutsideBase() throws Exception {
        hasKey(failure(server(catalog().replace("file=b.Mjpeg", "file=./a.Mjpeg")), NodeRole.SERVER), "stream." + B + ".file");
        hasKey(failure(server(catalog().replace("file=a.Mjpeg", "file=../outside.Mjpeg")), NodeRole.SERVER), "stream." + A + ".file");
        hasKey(failure(server(catalog().replace("file=a.Mjpeg", "file=.")), NodeRole.SERVER), "stream." + A + ".file");
        hasKey(failure(server(catalog().replace("file=a.Mjpeg", "file=\\u0000")), NodeRole.SERVER), "stream." + A + ".file");
    }

    @Test
    void rejectsMissingFieldsAndUnknownCatalogKeys() throws Exception {
        var e = failure(server(catalog().replace("stream." + A + ".title=A\n", "") + "stream." + A + ".ftps=20\n"), NodeRole.SERVER);
        hasKey(e, "stream." + A + ".title");
        hasKey(e, "stream." + A + ".ftps");
    }

    @Test
    void rejectsInvalidUtf8AndMalformedUnicodeEscapes() throws Exception {
        Path p = temp.resolve("client.properties");
        Files.write(p, new byte[]{(byte)0xC3, 0x28});
        hasKey(failure(p, NodeRole.CLIENT), "<file>");
        write(p, "node.id=\\uZZZZ\n");
        hasKey(failure(p, NodeRole.CLIENT), "<file>");
    }

    @Test
    void acceptsPropertyContinuationAndUtf8Titles() throws Exception {
        String c = catalog().replace("stream.ids=" + A + "," + B, "stream.ids=" + A + ",\\\n  " + B)
                .replace("title=A", "title=Olá 🎬");
        assertEquals("Olá 🎬", ConfigurationLoader.load(server(c), NodeRole.SERVER).streams().getFirst().title());
    }

    @Test
    void deploymentRejectsUuidOwnedByTwoServers() throws Exception {
        Path dir = copyExamples();
        Path p = dir.resolve("streams-s2.properties");
        write(p, Files.readString(p).replace("00000000-0000-4000-8000-000000000003", A));
        var e = assertThrows(ConfigurationException.class, () -> ConfigurationLoader.loadDeployment(examplePaths(dir)));
        hasKey(e, "stream.ids");
        assertTrue(e.getMessage().contains("S1"));
    }

    @Test
    void deploymentRejectsRoleAndEndpointsDifferentFromTopology() throws Exception {
        Path dir = copyExamples();
        Path p = dir.resolve("client.properties");
        write(p, Files.readString(p).replace("node.role=CLIENT", "node.role=RELAY")
                .replace("control.port=5400", "control.port=5402")
                .replace("data.port=5401", "data.port=5403")
                .replace("advertised.host=127.0.0.1", "advertised.host=127.0.0.2"));
        var e = assertThrows(ConfigurationException.class, () -> ConfigurationLoader.loadDeployment(examplePaths(dir)));
        for (String key : List.of("node.role", "control.port", "data.port", "advertised.host")) hasKey(e, key);
    }

    @Test
    void deploymentRejectsWrongBootstrapContact() throws Exception {
        Path dir = copyExamples();Path p = dir.resolve("client.properties");
        write(p, Files.readString(p).replace("bootstrap.host=127.0.0.1", "bootstrap.host=127.0.0.2")
                .replace("bootstrap.port=5002", "bootstrap.port=5003"));
        var e = assertThrows(ConfigurationException.class, () -> ConfigurationLoader.loadDeployment(examplePaths(dir)));
        hasKey(e, "bootstrap.host");hasKey(e, "bootstrap.port");
    }

    @Test
    void deploymentRejectsMissingAndDuplicateProfiles() throws Exception {
        var paths = examplePaths(exampleDirectory());
        var e = assertThrows(ConfigurationException.class, () -> ConfigurationLoader.loadDeployment(paths.subList(0, 4)));
        hasKey(e, "node.ids");
        var duplicate = new java.util.ArrayList<>(paths);duplicate.add(paths.getLast());
        hasKey(assertThrows(ConfigurationException.class, () -> ConfigurationLoader.loadDeployment(duplicate)), "node.id");
        hasKey(assertThrows(ConfigurationException.class, () -> ConfigurationLoader.loadDeployment(List.of())), "deployment");
    }

    @Test
    void deploymentDetectsCollisionWithBootstrapPort() throws Exception {
        Path dir=copyExamples();Path p=dir.resolve("client.properties");
        write(p, Files.readString(p).replace("control.port=5400", "control.port=5002"));
        assertTrue(assertThrows(ConfigurationException.class, () -> ConfigurationLoader.loadDeployment(examplePaths(dir)))
                .getMessage().contains("já atribuído"));
    }

    @Test
    void rejectsOversizedNodeAndStreamLists() throws Exception {
        String ids = java.util.stream.IntStream.range(0, 65).mapToObj(i -> "N" + i)
                .collect(Collectors.joining(","));
        var e = failure(bootstrap(topology().replace("node.ids=R1,C1", "node.ids=" + ids)), NodeRole.BOOTSTRAP);
        assertTrue(e.problems().stream().anyMatch(p -> p.key().equals("node.ids") && p.message().contains("excede")));
        String uuids = java.util.stream.IntStream.range(0, 257)
                .mapToObj(i -> String.format("00000000-0000-4000-8000-%012d", i))
                .collect(Collectors.joining(","));
        e = failure(server("stream.ids=" + uuids + "\n"), NodeRole.SERVER);
        assertTrue(e.problems().stream().anyMatch(p -> p.key().equals("stream.ids") && p.message().contains("excede")));
    }

    @Test
    void deploymentRejectsNodeAbsentFromAllowedGraph() throws Exception {
        Path dir = copyExamples();
        Path p = dir.resolve("client.properties");
        write(p, Files.readString(p).replace("node.id=C1", "node.id=C2"));
        var e = assertThrows(ConfigurationException.class,
                () -> ConfigurationLoader.loadDeployment(examplePaths(dir)));
        hasKey(e, "node.id");
        hasKey(e, "node.ids");
    }

    private Path client(String data) throws IOException {
        return write(temp.resolve("client.properties"), data);
    }
    private Path server(String data) throws IOException {
        write(temp.resolve("catalog.properties"), data);
        return write(temp.resolve("server.properties"), profile(NodeRole.SERVER, "S1", 5100, 5101)
                + "streams.file=catalog.properties\nmedia.base=media\n");
    }
    private Path bootstrap(String data) throws IOException {
        write(temp.resolve("topology.properties"), data);
        return write(temp.resolve("bootstrap.properties"), profile(NodeRole.BOOTSTRAP, "B", 5002, 0)
                + "topology.file=topology.properties\n");
    }
    private static Path write(Path p, String text) throws IOException {
        Files.writeString(p, text, StandardCharsets.UTF_8);return p;
    }
    private static ConfigurationException failure(Path p, NodeRole role) {
        return assertThrows(ConfigurationException.class, () -> ConfigurationLoader.load(p, role));
    }
    private static void hasKey(ConfigurationException e, String key) {
        assertTrue(e.problems().stream().anyMatch(p -> p.key().equals(key)), e::getMessage);
    }
    private static Path exampleDirectory() throws Exception {
        return Path.of(ConfigurationLoaderTest.class.getResource("/config/bootstrap.properties").toURI()).getParent();
    }
    private static List<Path> examplePaths(Path dir) {
        return PROFILES.stream().map(name -> dir.resolve(name + ".properties")).toList();
    }
    private Path copyExamples() throws Exception {
        try (var files = Files.list(exampleDirectory())) {
            for (Path p : files.filter(p -> p.toString().endsWith(".properties")).toList()) {
                Files.copy(p, temp.resolve(p.getFileName()));
            }
        }
        return temp;
    }
    private static String profile(NodeRole role, String id, int tcp, int udp) {
        String common = "node.id=" + id + "\nnode.role=" + role + "\nbind.host=127.0.0.1\nadvertised.host=127.0.0.1\ncontrol.port=" + tcp + "\n";
        return role == NodeRole.BOOTSTRAP ? common : common + "data.port=" + udp + "\nbootstrap.host=127.0.0.1\nbootstrap.port=5002\n";
    }
    private static String catalog() {
        return "stream.ids=" + A + "," + B + "\n" + stream(A, "A", "a.Mjpeg") + stream(B, "B", "b.Mjpeg");
    }
    private static String stream(String id, String title, String file) {
        String p = "stream." + id + ".";
        return p + "title=" + title + "\n" + p + "file=" + file + "\n" + p + "fps=10\n" + p + "loop=true\n";
    }
    private static String topology() {
        return """
                node.ids=R1,C1
                node.R1.host=127.0.0.1
                node.R1.controlPort=5300
                node.R1.dataPort=5301
                node.R1.role=RELAY
                node.R1.neighbors=C1
                node.C1.host=127.0.0.1
                node.C1.controlPort=5400
                node.C1.dataPort=5401
                node.C1.role=CLIENT
                node.C1.neighbors=R1
                """;
    }
    // IMP-002 / V-032: D01-D17. Existing foundation tests above remain unchanged.
    private record RuntimeCase(String group, String component, String key, String defaultValue,
                               String override, int min, int max,
                               java.util.function.Function<RuntimeConfiguration, Object> read) {
        Object expected(String value) {
            if (key.endsWith(".ms")) return Duration.ofMillis(Long.parseLong(value));
            return key.equals("monitor.ewma.alpha") ? Double.valueOf(value) : (Object) Integer.valueOf(value);
        }
        @Override public String toString() { return key; }
    }

    private static java.util.stream.Stream<RuntimeCase> runtimeCases() {
        int max = Integer.MAX_VALUE;
        return java.util.stream.Stream.of(
            new RuntimeCase("control", "connectTimeout", "control.connect.timeout.ms", "2000", "1111", 1, max, r -> r.control().connectTimeout()),
            new RuntimeCase("control", "shutdownTimeout", "control.shutdown.timeout.ms", "3000", "2222", 1, max, r -> r.control().shutdownTimeout()),
            new RuntimeCase("control", "maxConnections", "control.max.connections", "64", "13", 1, 64, r -> r.control().maxConnections()),
            new RuntimeCase("control", "queueCapacity", "control.queue.capacity", "256", "7", 1, 256, r -> r.control().queueCapacity()),
            new RuntimeCase("control", "bootstrapQueryTimeout", "bootstrap.query.timeout.ms", "3000", "1234", 1, max, r -> r.control().bootstrapQueryTimeout()),
            new RuntimeCase("overlay", "helloTimeout", "overlay.hello.timeout.ms", "3000", "1234", 1, max, r -> r.overlay().helloTimeout()),
            new RuntimeCase("overlay", "retryInitial", "overlay.retry.initial.ms", "500", "750", 1, max, r -> r.overlay().retryInitial()),
            new RuntimeCase("overlay", "retryMax", "overlay.retry.max.ms", "5000", "6000", 1, max, r -> r.overlay().retryMax()),
            new RuntimeCase("overlay", "retryJitterPercent", "overlay.retry.jitter.percent", "10", "25", 0, 100, r -> r.overlay().retryJitterPercent()),
            new RuntimeCase("overlay", "eventsCapacity", "node.events.capacity", "4096", "99", 1, 4096, r -> r.overlay().eventsCapacity()),
            new RuntimeCase("monitoring", "probeInterval", "monitor.probe.interval.ms", "1000", "1500", 1, max, r -> r.monitoring().probeInterval()),
            new RuntimeCase("monitoring", "probeTimeout", "monitor.probe.timeout.ms", "1000", "1750", 1, max, r -> r.monitoring().probeTimeout()),
            new RuntimeCase("monitoring", "probeFailures", "monitor.probe.failures", "3", "5", 1, max, r -> r.monitoring().probeFailures()),
            new RuntimeCase("monitoring", "ewmaAlpha", "monitor.ewma.alpha", "0.2", "0.35", 0, 1, r -> r.monitoring().ewmaAlpha()),
            new RuntimeCase("routing", "announceInterval", "routing.announce.interval.ms", "2000", "1000", 1, max, r -> r.routing().announceInterval()),
            new RuntimeCase("routing", "candidateTtl", "routing.candidate.ttl.ms", "8000", "9000", 1, max, r -> r.routing().candidateTtl()),
            new RuntimeCase("routing", "cacheCapacity", "routing.cache.capacity", "4096", "123", 1, 4096, r -> r.routing().cacheCapacity()),
            new RuntimeCase("routing", "cacheTtl", "routing.cache.ttl.ms", "16000", "18000", 1, max, r -> r.routing().cacheTtl()),
            new RuntimeCase("routing", "floodMaxPerSecond", "routing.flood.max.per.sec", "10", "7", 1, 10, r -> r.routing().floodMaxPerSecond()),
            new RuntimeCase("routing", "activateTimeout", "routing.activate.timeout.ms", "3000", "1200", 1, max, r -> r.routing().activateTimeout()),
            new RuntimeCase("routing", "activateAttempts", "routing.activate.attempts", "3", "4", 1, max, r -> r.routing().activateAttempts()),
            new RuntimeCase("routing", "switchImprovementPercent", "routing.switch.improvement.percent", "20", "35", 0, 100, r -> r.routing().switchImprovementPercent()),
            new RuntimeCase("routing", "switchObservations", "routing.switch.observations", "3", "5", 1, max, r -> r.routing().switchObservations()),
            new RuntimeCase("routing", "switchMinDwell", "routing.switch.min.dwell.ms", "5000", "6500", 1, max, r -> r.routing().switchMinDwell()),
            new RuntimeCase("subscription", "lease", "subscription.lease.ms", "6000", "9000", 1, max, r -> r.subscription().lease()),
            new RuntimeCase("subscription", "renew", "subscription.renew.ms", "2000", "1500", 1, max, r -> r.subscription().renew()),
            new RuntimeCase("video", "egressCapacity", "video.egress.capacity", "256", "123", 1, 256, r -> r.video().egressCapacity()),
            new RuntimeCase("video", "reassemblyMaxFrames", "video.reassembly.max.frames", "8", "4", 1, 8, r -> r.video().reassemblyMaxFrames()),
            new RuntimeCase("video", "reassemblyDeadline", "video.reassembly.deadline.ms", "250", "333", 1, max, r -> r.video().reassemblyDeadline()),
            new RuntimeCase("source", "maxFrameBytes", "source.max.frame.bytes", "99999", "50000", 1, 99999, r -> r.source().maxFrameBytes()));
    }

    @Test
    void runtimeDefaultsPreserveFiveRealProfiles() throws Exception { // D01
        for (var node : ConfigurationLoader.loadDeployment(examplePaths(exampleDirectory()))) {
            assertEquals(RuntimeConfiguration.defaults(), node.runtime());
            for (var c : runtimeCases().toList()) assertEquals(c.expected(c.defaultValue()), c.read().apply(node.runtime()), c.key());
        }
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> runtimeAllowedRoles() {
        return runtimeCases().flatMap(c -> java.util.Arrays.stream(NodeRole.values())
                .filter(role -> c.group().equals("control") || role != NodeRole.BOOTSTRAP
                        && (!c.group().equals("source") || role == NodeRole.SERVER))
                .map(role -> org.junit.jupiter.params.provider.Arguments.of(c, role)));
    }

    @ParameterizedTest(name = "{0} / {1}")
    @org.junit.jupiter.params.provider.MethodSource("runtimeAllowedRoles")
    void runtimeOverridesReachExactAccessorForEveryAllowedRole(RuntimeCase c, NodeRole role) throws Exception { // D02,D06
        var runtime = runtimeLoad(role, c.key() + "=" + c.override() + "\n").runtime();
        for (var other : runtimeCases().toList()) {
            String expected = other.key().equals(c.key()) ? c.override() : other.defaultValue();
            assertEquals(other.expected(expected), other.read().apply(runtime), other.key());
        }
    }

    private record RuntimeBoundary(String properties, String relationError) { }
    private static RuntimeBoundary runtimeBoundary(RuntimeCase c, int value) {
        var values = new java.util.LinkedHashMap<String, String>();
        values.put(c.key(), Integer.toString(value));
        String error = null;
        switch (c.key()) {
            case "overlay.retry.initial.ms" -> values.put("overlay.retry.max.ms", Integer.toString(Math.max(5000, value)));
            case "overlay.retry.max.ms" -> values.put("overlay.retry.initial.ms", Integer.toString(Math.min(500, value)));
            case "routing.announce.interval.ms" -> {
                if (value == Integer.MAX_VALUE) error = "routing.candidate.ttl.ms";
                else {
                    values.put("routing.candidate.ttl.ms", Integer.toString(value + 1));
                    values.put("routing.cache.ttl.ms", Integer.toString(Math.max(16000, value + 1)));
                }
            }
            case "routing.candidate.ttl.ms" -> {
                if (value == 1) error = c.key();
                else {
                    values.put("routing.announce.interval.ms", Integer.toString(Math.min(2000, value - 1)));
                    values.put("routing.cache.ttl.ms", Integer.toString(Math.max(16000, value)));
                }
            }
            case "routing.cache.ttl.ms" -> {
                if (value == 1) {
                    values.put("routing.announce.interval.ms", "1");
                    values.put("routing.candidate.ttl.ms", "2"); error = c.key();
                }
            }
            case "subscription.lease.ms" -> {
                if (value == 1) error = "subscription.renew.ms";
                else values.put("subscription.renew.ms", Integer.toString(Math.min(2000, value - 1)));
            }
            case "subscription.renew.ms" -> {
                if (value == Integer.MAX_VALUE) error = c.key();
                else values.put("subscription.lease.ms", Integer.toString(Math.max(6000, value + 1)));
            }
            default -> { }
        }
        return new RuntimeBoundary(values.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue() + "\n")
                .collect(Collectors.joining()), error);
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> runtimeBoundaries() {
        return runtimeCases().filter(c -> !c.key().equals("monitor.ewma.alpha"))
                .flatMap(c -> java.util.stream.Stream.of(c.min(), c.max())
                        .map(v -> org.junit.jupiter.params.provider.Arguments.of(c, v)));
    }

    @ParameterizedTest(name = "{0} / {1}")
    @org.junit.jupiter.params.provider.MethodSource("runtimeBoundaries")
    void runtimeScalarBoundariesRespectCrossRelations(RuntimeCase c, int value) throws Exception { // D03,D08
        if(c.key().equals("control.queue.capacity") && value==1) {
            hasKey(failure(runtimeProfile(NodeRole.SERVER,c.key()+"=1\n"),NodeRole.SERVER),c.key());
            assertEquals(1,new RuntimeConfiguration.Control(java.time.Duration.ofMillis(1),java.time.Duration.ofMillis(1),1,1,java.time.Duration.ofMillis(1)).queueCapacity());
            assertEquals(1,runtimeLoad(NodeRole.BOOTSTRAP,"control.queue.capacity=1\n").runtime().control().queueCapacity());
            assertEquals(2,runtimeLoad(NodeRole.SERVER,"control.queue.capacity=2\n").runtime().control().queueCapacity());
            return;
        }
        var boundary = runtimeBoundary(c, value);
        if (boundary.relationError() != null) hasKey(failure(runtimeProfile(NodeRole.SERVER, boundary.properties()), NodeRole.SERVER), boundary.relationError());
        else {
            Object actual = c.read().apply(runtimeLoad(NodeRole.SERVER, boundary.properties()).runtime());
            assertEquals(c.expected(Integer.toString(value)), actual);
            if (actual instanceof Duration d) {
                assertEquals((long) value * 1_000_000L, d.toNanos());
                assertEquals(value, Math.toIntExact(d.toMillis()));
            }
        }
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> runtimeBadIntegers() {
        return runtimeCases().filter(c -> !c.key().equals("monitor.ewma.alpha")).flatMap(c -> {
            var values = new java.util.LinkedHashSet<>(List.of("", " ", "-1", "+1", "1.5", "1ms", "1_000", "2,000", "2147483648"));
            values.add(Integer.toString(c.min() - 1)); values.add(Long.toString((long) c.max() + 1));
            return values.stream().map(v -> org.junit.jupiter.params.provider.Arguments.of(c, v));
        });
    }

    @ParameterizedTest(name = "{0} / [{1}]")
    @org.junit.jupiter.params.provider.MethodSource("runtimeBadIntegers")
    void runtimeRejectsInvalidIntegerTokensAndCaps(RuntimeCase c, String value) throws Exception { // D04,D05
        Path file = runtimeProfile(NodeRole.SERVER, c.key() + "=" + value + "\n");
        var error = failure(file, NodeRole.SERVER); hasKey(error, c.key());
        assertTrue(error.problems().stream().allMatch(problem -> problem.file().equals(file.toAbsolutePath())));
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> runtimeGoodAlpha() {
        return java.util.stream.Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("1", 1d),
            org.junit.jupiter.params.provider.Arguments.of("1.0", 1d),
            org.junit.jupiter.params.provider.Arguments.of("0.2", .2d),
            org.junit.jupiter.params.provider.Arguments.of(".2", .2d),
            org.junit.jupiter.params.provider.Arguments.of("000.500", .5d),
            org.junit.jupiter.params.provider.Arguments.of(" 0.25 ", .25d),
            org.junit.jupiter.params.provider.Arguments.of("0." + "0".repeat(323) + "49", Double.MIN_VALUE));
    }
    @ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("runtimeGoodAlpha")
    void runtimeAcceptsApprovedAlphaGrammar(String token, double expected) throws Exception { // D03,D04,Q-C-03
        assertEquals(expected, runtimeLoad(NodeRole.CLIENT, "monitor.ewma.alpha=" + token + "\n").runtime().monitoring().ewmaAlpha());
    }

    private static java.util.stream.Stream<String> runtimeBadAlpha() {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of("", " ", "0", "0.0", "-0.2", "+0.2", "1.01", "NaN", "Infinity", "-Infinity",
                "0,2", "2e-1", "0x1.0p-1", "0.2d", "0.2f", "1.", "0.2ms", "0.2_0"),
                java.util.stream.Stream.of("0." + "0".repeat(399) + "1", "9".repeat(400) + ".0"));
    }
    @ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("runtimeBadAlpha")
    void runtimeRejectsAlphaGrammarDomainAndUnderflow(String token) throws Exception { // D04
        hasKey(failure(runtimeProfile(NodeRole.CLIENT, "monitor.ewma.alpha=" + token + "\n"), NodeRole.CLIENT), "monitor.ewma.alpha");
    }

    @Test
    void runtimeAcceptsWhitespaceAndLeadingZeroIntegers() throws Exception { // D04
        var runtime = runtimeLoad(NodeRole.SERVER,
                "control.queue.capacity= 0007  \noverlay.hello.timeout.ms= 0009  \nsource.max.frame.bytes= 0001234  \n").runtime();
        assertEquals(7, runtime.control().queueCapacity());
        assertEquals(Duration.ofMillis(9), runtime.overlay().helloTimeout());
        assertEquals(1234, runtime.source().maxFrameBytes());
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> runtimeForbiddenRoles() {
        return runtimeCases().filter(c -> !c.group().equals("control")).flatMap(c -> {
            var roles = c.group().equals("source") ? List.of(NodeRole.BOOTSTRAP, NodeRole.RELAY, NodeRole.CLIENT) : List.of(NodeRole.BOOTSTRAP);
            return roles.stream().flatMap(role -> java.util.stream.Stream.of(c.defaultValue(), c.override())
                    .map(value -> org.junit.jupiter.params.provider.Arguments.of(c, role, value)));
        });
    }
    @ParameterizedTest(name = "{0} / {1} / {2}")
    @org.junit.jupiter.params.provider.MethodSource("runtimeForbiddenRoles")
    void runtimeRejectsInapplicableKeysEvenAtDefault(RuntimeCase c, NodeRole role, String value) throws Exception { // D06
        hasKey(failure(runtimeProfile(role, c.key() + "=" + value + "\n"), role), c.key());
    }

    @Test
    void runtimeRejectsUnknownDuplicatesAndInvalidRoleWithoutFallback() throws Exception { // D07
        hasKey(failure(runtimeProfile(NodeRole.CLIENT, "monitor.ewma.alpa=0.2\n"), NodeRole.CLIENT), "monitor.ewma.alpa");
        for (String second : List.of("control.queue.capacity=9\n", "control.queue.capacit\\u0079=9\n")) {
            var error = failure(runtimeProfile(NodeRole.CLIENT, "control.queue.capacity=7\n" + second), NodeRole.CLIENT);
            assertTrue(error.problems().stream().anyMatch(e -> e.key().equals("control.queue.capacity") && e.message().contains("duplicada")));
        }
        Path badRole = runtimeProfile(NodeRole.CLIENT, "monitor.ewma.alpha=0.3\n");
        write(badRole, Files.readString(badRole).replace("node.role=CLIENT", "node.role=INVALID"));
        hasKey(failure(badRole, NodeRole.CLIENT), "node.role");
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> runtimeRelationCases() {
        return java.util.stream.Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("overlay.retry.initial.ms=5000\noverlay.retry.max.ms=5000\n", ""),
            org.junit.jupiter.params.provider.Arguments.of("overlay.retry.initial.ms=5001\noverlay.retry.max.ms=5000\n", "overlay.retry.max.ms"),
            org.junit.jupiter.params.provider.Arguments.of("routing.announce.interval.ms=2000\nrouting.candidate.ttl.ms=2001\n", ""),
            org.junit.jupiter.params.provider.Arguments.of("routing.announce.interval.ms=2000\nrouting.candidate.ttl.ms=2000\n", "routing.candidate.ttl.ms"),
            org.junit.jupiter.params.provider.Arguments.of("routing.cache.ttl.ms=8000\n", ""),
            org.junit.jupiter.params.provider.Arguments.of("routing.cache.ttl.ms=7999\n", "routing.cache.ttl.ms"),
            org.junit.jupiter.params.provider.Arguments.of("subscription.renew.ms=5999\n", ""),
            org.junit.jupiter.params.provider.Arguments.of("subscription.renew.ms=6000\n", "subscription.renew.ms"));
    }
    @ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("runtimeRelationCases")
    void runtimeEnforcesFourCrossRelations(String properties, String errorKey) throws Exception { // D08
        if (errorKey.isEmpty()) assertNotNull(runtimeLoad(NodeRole.CLIENT, properties));
        else hasKey(failure(runtimeProfile(NodeRole.CLIENT, properties), NodeRole.CLIENT), errorKey);
    }

    @Test
    void runtimeValidatesOverridesTogetherWithDefaults() throws Exception { // D09
        String[][] pairs = {
            {"overlay.retry.initial.ms=6000\n", "overlay.retry.max.ms=6000\n", "overlay.retry.max.ms"},
            {"routing.announce.interval.ms=8000\n", "routing.candidate.ttl.ms=8001\n", "routing.candidate.ttl.ms"},
            {"routing.candidate.ttl.ms=20000\n", "routing.cache.ttl.ms=20000\n", "routing.cache.ttl.ms"},
            {"subscription.lease.ms=1000\n", "subscription.renew.ms=999\n", "subscription.renew.ms"}};
        for (var pair : pairs) {
            hasKey(failure(runtimeProfile(NodeRole.CLIENT, pair[0]), NodeRole.CLIENT), pair[2]);
            assertNotNull(runtimeLoad(NodeRole.CLIENT, pair[0] + pair[1]));
        }
    }

    @Test
    void runtimeAggregatesIndependentLegacyAndRuntimeProblems() throws Exception { // D10
        Path file = runtimeProfile(NodeRole.CLIENT, "control.queue.capacity=0\nmonitor.ewma.alpha=NaN\nvideo.reassembly.max.frames=9\nrouting.candidate.ttl.ms=1\n");
        write(file, Files.readString(file).replace("control.port=5400", "control.port=0"));
        var error = failure(file, NodeRole.CLIENT);
        for (var key : List.of("control.port", "control.queue.capacity", "monitor.ewma.alpha", "video.reassembly.max.frames", "routing.candidate.ttl.ms")) hasKey(error, key);
        assertTrue(error.problems().stream().allMatch(problem -> problem.file().equals(file.toAbsolutePath())));
        assertThrows(UnsupportedOperationException.class, () -> error.problems().clear());
    }
    @Test
    void runtimeDoesNotInventRelationErrorsFromInvalidOperands() throws Exception { // D11
        var error = failure(runtimeProfile(NodeRole.CLIENT, "overlay.retry.initial.ms=invalid\noverlay.retry.max.ms=200\nsubscription.lease.ms=1\n"), NodeRole.CLIENT);
        hasKey(error, "overlay.retry.initial.ms"); hasKey(error, "subscription.renew.ms");
        assertFalse(error.problems().stream().anyMatch(problem -> problem.key().equals("overlay.retry.max.ms")));
    }

    private static Object rebuildRecord(Object original, String component, Object replacement) throws Exception {
        var fields = original.getClass().getRecordComponents();
        var types = new Class<?>[fields.length]; var values = new Object[fields.length];
        boolean found = false;
        for (int i = 0; i < fields.length; i++) {
            types[i] = fields[i].getType(); values[i] = fields[i].getAccessor().invoke(original);
            if (fields[i].getName().equals(component)) { values[i] = replacement; found = true; }
        }
        assertTrue(found, component);
        try { return original.getClass().getConstructor(types).newInstance(values); }
        catch (java.lang.reflect.InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException cause) throw cause;
            throw ex;
        }
    }
    private static Object runtimeGroup(RuntimeConfiguration runtime, String group) throws Exception {
        return RuntimeConfiguration.class.getMethod(group).invoke(runtime);
    }
    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> runtimeBadDirectValues() {
        return runtimeCases().flatMap(c -> {
            List<Object> values;
            if (c.key().endsWith(".ms")) values = List.of(Duration.ZERO, Duration.ofMillis(-1), Duration.ofNanos(1),
                    Duration.ofMillis(1).plusNanos(1), Duration.ofSeconds(Long.MAX_VALUE), Duration.ofMillis((long) Integer.MAX_VALUE + 1));
            else if (c.key().equals("monitor.ewma.alpha")) values = List.of(0d, -.1d, 1.1d, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
            else values = c.max() == Integer.MAX_VALUE ? List.of(c.min() - 1) : List.of(c.min() - 1, c.max() + 1);
            return values.stream().map(value -> org.junit.jupiter.params.provider.Arguments.of(c, value));
        });
    }
    @ParameterizedTest(name = "{0} / {1}")
    @org.junit.jupiter.params.provider.MethodSource("runtimeBadDirectValues")
    void runtimeConstructorsRejectInvalidValues(RuntimeCase c, Object value) throws Exception { // D12
        Object group = runtimeGroup(RuntimeConfiguration.defaults(), c.group());
        assertThrows(IllegalArgumentException.class, () -> rebuildRecord(group, c.component(), value));
    }
    @Test
    void runtimeConstructorsRejectNullsAndInvalidRelations() throws Exception { // D12
        var r = RuntimeConfiguration.defaults();
        for (var name : List.of("control", "overlay", "monitoring", "routing", "subscription", "video", "source"))
            assertThrows(NullPointerException.class, () -> rebuildRecord(r, name, null));
        for (var c : runtimeCases().filter(x -> x.key().endsWith(".ms")).toList())
            assertThrows(NullPointerException.class, () -> rebuildRecord(runtimeGroup(r, c.group()), c.component(), null));
        assertThrows(IllegalArgumentException.class, () -> rebuildRecord(r.overlay(), "retryMax", Duration.ofMillis(499)));
        assertThrows(IllegalArgumentException.class, () -> rebuildRecord(r.routing(), "candidateTtl", Duration.ofMillis(2000)));
        assertThrows(IllegalArgumentException.class, () -> rebuildRecord(r.routing(), "cacheTtl", Duration.ofMillis(7999)));
        assertThrows(IllegalArgumentException.class, () -> rebuildRecord(r.subscription(), "renew", Duration.ofMillis(6000)));
    }

    @Test
    void runtimePreservesOldConstructorAndCanonicalOverrides() throws Exception { // D13
        var n = runtimeLoad(NodeRole.SERVER, "");
        var old = new NodeConfiguration(n.sourceFile(), n.nodeId(), n.role(), n.bindHost(), n.advertisedHost(), n.controlPort(), n.dataPort(),
                n.bootstrap(), n.topologyFile(), n.streamsFile(), n.mediaBase(), n.topology(), n.streams());
        assertEquals(n, old); assertEquals(RuntimeConfiguration.defaults(), old.runtime());
        var input = new java.util.ArrayList<>(n.streams());
        var changed = runtimeLoad(NodeRole.SERVER, "control.queue.capacity=7\n").runtime();
        var canonical = new NodeConfiguration(n.sourceFile(), n.nodeId(), n.role(), n.bindHost(), n.advertisedHost(), n.controlPort(), n.dataPort(),
                n.bootstrap(), n.topologyFile(), n.streamsFile(), n.mediaBase(), n.topology(), input, changed);
        input.clear(); assertEquals(2, canonical.streams().size()); assertEquals(changed, canonical.runtime()); assertNotEquals(old, canonical);
        assertThrows(UnsupportedOperationException.class, () -> canonical.streams().clear());
        assertThrows(NullPointerException.class, () -> new NodeConfiguration(n.sourceFile(), n.nodeId(), n.role(), n.bindHost(), n.advertisedHost(), n.controlPort(), n.dataPort(),
                n.bootstrap(), n.topologyFile(), n.streamsFile(), n.mediaBase(), n.topology(), n.streams(), null));
    }
    @Test
    void runtimeOverridesReachTransportOptionsWithoutOpeningSockets() throws Exception { // D14
        var control = runtimeLoad(NodeRole.CLIENT, "control.queue.capacity=7\ncontrol.max.connections=13\ncontrol.connect.timeout.ms=1111\ncontrol.shutdown.timeout.ms=2222\n").runtime().control();
        var consumed = new pt.uminho.esr.ott.transport.Transport.Options(control.queueCapacity(), control.maxConnections(), control.connectTimeout(), control.shutdownTimeout());
        assertEquals(7, consumed.queueCapacity()); assertEquals(13, consumed.maxConnections());
        assertEquals(Duration.ofMillis(1111), consumed.connectTimeout()); assertEquals(Duration.ofMillis(2222), consumed.shutdownTimeout());
    }
    @Test
    void runtimeDeploymentIsAtomicAndOptionsRemainPerNode() throws Exception { // D15
        Path dir = copyExamples();
        Files.writeString(dir.resolve("server-1.properties"), "control.queue.capacity=7\n", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        Files.writeString(dir.resolve("client.properties"), "control.queue.capacity=8\n", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        var nodes = ConfigurationLoader.loadDeployment(examplePaths(dir));
        assertEquals(7, nodes.stream().filter(n -> n.nodeId().equals("S1")).findFirst().orElseThrow().runtime().control().queueCapacity());
        assertEquals(8, nodes.stream().filter(n -> n.role() == NodeRole.CLIENT).findFirst().orElseThrow().runtime().control().queueCapacity());
        Files.writeString(dir.resolve("client.properties"), "monitor.ewma.alpha=NaN\n", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        var error = assertThrows(ConfigurationException.class, () -> ConfigurationLoader.loadDeployment(examplePaths(dir)));
        hasKey(error, "monitor.ewma.alpha");
        assertEquals(256, RuntimeConfiguration.defaults().control().queueCapacity());
        assertEquals(RuntimeConfiguration.defaults(), ConfigurationLoader.load(exampleDirectory().resolve("client.properties"), NodeRole.CLIENT).runtime());
    }
    @Test
    void runtimePropertiesStayOutOfTopologyAndCatalog() throws Exception { // D15
        hasKey(failure(bootstrap(topology() + "control.queue.capacity=7\n"), NodeRole.BOOTSTRAP), "control.queue.capacity");
        Path server = runtimeProfile(NodeRole.SERVER, "");
        Files.writeString(temp.resolve("runtime-catalog.properties"), "control.queue.capacity=7\n", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        hasKey(failure(server, NodeRole.SERVER), "control.queue.capacity");
    }
    @Test
    void runtimeDefaultsDoNotReplaceRequiredConfiguration() throws Exception { // D16
        for (String key : List.of("node.id", "node.role", "bind.host", "advertised.host", "control.port", "data.port", "bootstrap.host", "bootstrap.port")) {
            Path file = runtimeProfile(NodeRole.CLIENT, "control.queue.capacity=7\n");
            write(file, Files.readString(file).replaceAll("(?m)^" + java.util.regex.Pattern.quote(key) + "=.*\\R", ""));
            hasKey(failure(file, NodeRole.CLIENT), key);
        }
        Path bootstrap = runtimeProfile(NodeRole.BOOTSTRAP, "control.queue.capacity=7\n");
        write(bootstrap, Files.readString(bootstrap).replaceAll("(?m)^topology\\.file=.*\\R", "")); hasKey(failure(bootstrap, NodeRole.BOOTSTRAP), "topology.file");
        for (String key : List.of("streams.file", "media.base")) {
            Path file = runtimeProfile(NodeRole.SERVER, "control.queue.capacity=7\n");
            write(file, Files.readString(file).replaceAll("(?m)^" + java.util.regex.Pattern.quote(key) + "=.*\\R", "")); hasKey(failure(file, NodeRole.SERVER), key);
        }
    }

    private NodeConfiguration runtimeLoad(NodeRole role, String properties) throws Exception {
        return ConfigurationLoader.load(runtimeProfile(role, properties), role);
    }
    private Path runtimeProfile(NodeRole role, String properties) throws IOException {
        String body = profile(role, role == NodeRole.BOOTSTRAP ? "B" : role == NodeRole.SERVER ? "S1" : "C1",
                role == NodeRole.BOOTSTRAP ? 5002 : 5400, role == NodeRole.BOOTSTRAP ? 0 : 5401);
        if (role == NodeRole.BOOTSTRAP) {
            write(temp.resolve("runtime-topology.properties"), topology()); body += "topology.file=runtime-topology.properties\n";
        } else if (role == NodeRole.SERVER) {
            write(temp.resolve("runtime-catalog.properties"), catalog()); body += "streams.file=runtime-catalog.properties\nmedia.base=media\n";
        }
        return write(temp.resolve("runtime-" + role + ".properties"), body + properties);
    }
    @Test void D01JointPreflightRejectsDegreeBeyondPhysicalConnectionCapacity() throws Exception {
        Path dir=copyExamples();Files.writeString(dir.resolve("relay.properties"),"control.max.connections=1\n",StandardCharsets.UTF_8,java.nio.file.StandardOpenOption.APPEND);
        var error=assertThrows(ConfigurationException.class,()->ConfigurationLoader.loadDeployment(examplePaths(dir)));
        hasKey(error,"control.max.connections");
    }

}
