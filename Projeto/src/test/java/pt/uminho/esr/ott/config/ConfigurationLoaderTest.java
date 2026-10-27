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
}
