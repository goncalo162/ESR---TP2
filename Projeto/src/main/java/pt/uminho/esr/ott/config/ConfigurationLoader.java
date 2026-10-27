package pt.uminho.esr.ott.config;

import pt.uminho.esr.ott.overlay.NodeRole;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/** Leitura UTF-8 estrita, sem DNS, sockets, criação de diretórios ou leitura de vídeo. */
public final class ConfigurationLoader {
    private static final Set<String> COMMON_KEYS = Set.of(
            "node.id", "node.role", "bind.host", "advertised.host", "control.port");
    private static final String UUID_PATTERN =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private ConfigurationLoader() { }

    public static NodeConfiguration load(Path path, NodeRole expectedRole)
            throws ConfigurationException {
        Objects.requireNonNull(expectedRole, "expectedRole");
        var errors = new ArrayList<ConfigurationProblem>();
        NodeConfiguration result = node(path.toAbsolutePath().normalize(), expectedRole, errors);
        failIfAny(errors);
        return result;
    }

    /** Valida conjuntamente todos os perfis de uma configuração de teste. */
    public static List<NodeConfiguration> loadDeployment(Collection<Path> paths)
            throws ConfigurationException {
        Objects.requireNonNull(paths, "paths");
        var errors = new ArrayList<ConfigurationProblem>();
        var nodes = new LinkedHashMap<String, NodeConfiguration>();
        for (Path path : paths) {
            NodeConfiguration n = node(path.toAbsolutePath().normalize(), null, errors);
            if (n != null && nodes.putIfAbsent(n.nodeId(), n) != null) {
                error(errors, n.sourceFile(), "node.id", "identificador repetido na configuração conjunta");
            }
        }
        List<NodeConfiguration> bootstraps = nodes.values().stream()
                .filter(n -> n.role() == NodeRole.BOOTSTRAP).toList();
        if (bootstraps.size() != 1) {
            error(errors, Path.of(".").toAbsolutePath().normalize(), "deployment",
                    "é necessário exatamente um perfil BOOTSTRAP");
        } else {
            validateDeployment(nodes, bootstraps.getFirst(), errors);
        }
        failIfAny(errors);
        return List.copyOf(nodes.values());
    }

    private static NodeConfiguration node(Path file, NodeRole expected,
                                           List<ConfigurationProblem> errors) {
        int start = errors.size();
        Properties p = read(file, errors);
        if (p == null) return null;
        String id = nodeId(p, file, "node.id", errors);
        NodeRole role = role(p, file, "node.role", true, errors);
        if (role != null && expected != null && role != expected) {
            error(errors, file, "node.role", "papel diferente do esperado: " + expected);
        }
        String bind = host(p, file, "bind.host", true, errors);
        String advertised = host(p, file, "advertised.host", false, errors);
        int control = integer(p, file, "control.port", 1, 65535, errors);
        int data = 0;
        NodeConfiguration.BootstrapContact bootstrap = null;
        Path topologyFile = null, streamsFile = null, mediaBase = null;
        TopologyConfiguration topology = null;
        List<StreamConfiguration> streams = List.of();
        var allowed = new HashSet<>(COMMON_KEYS);
        if (role == NodeRole.BOOTSTRAP) {
            allowed.add("topology.file");
            topologyFile = path(p, file, "topology.file", errors);
            if (topologyFile != null) topology = topology(topologyFile, errors);
        } else if (role != null) {
            allowed.addAll(Set.of("data.port", "bootstrap.host", "bootstrap.port"));
            data = integer(p, file, "data.port", 1, 65535, errors);
            String bootstrapHost = host(p, file, "bootstrap.host", false, errors);
            int bootstrapPort = integer(p, file, "bootstrap.port", 1, 65535, errors);
            if (bootstrapHost != null) {
                bootstrap = new NodeConfiguration.BootstrapContact(bootstrapHost, bootstrapPort);
            }
            if (role == NodeRole.SERVER) {
                allowed.addAll(Set.of("streams.file", "media.base"));
                streamsFile = path(p, file, "streams.file", errors);
                mediaBase = path(p, file, "media.base", errors);
                if (streamsFile != null && mediaBase != null && id != null) {
                    streams = streams(streamsFile, mediaBase, id, errors);
                }
            }
        }
        unknownKeys(p, allowed, file, errors);
        if (errors.size() != start) return null;
        return new NodeConfiguration(file, id, role, bind, advertised, control, data,
                Optional.ofNullable(bootstrap), Optional.ofNullable(topologyFile),
                Optional.ofNullable(streamsFile), Optional.ofNullable(mediaBase),
                Optional.ofNullable(topology), streams);
    }

    private static TopologyConfiguration topology(Path file, List<ConfigurationProblem> errors) {
        int start = errors.size();
        Properties p = read(file, errors);
        if (p == null) return null;
        List<String> ids = csv(p, file, "node.ids", 64, errors);
        var allowed = new HashSet<>(Set.of("node.ids"));
        var nodes = new LinkedHashMap<String, TopologyConfiguration.Node>();
        var endpoints = new EndpointOwners();
        for (String id : ids) {
            int before = errors.size();
            validNodeId(id, file, "node.ids", errors);
            String prefix = "node." + id + ".";
            for (String suffix : List.of("host", "controlPort", "dataPort", "role", "neighbors")) {
                allowed.add(prefix + suffix);
            }
            String h = host(p, file, prefix + "host", false, errors);
            int tcp = integer(p, file, prefix + "controlPort", 1, 65535, errors);
            int udp = integer(p, file, prefix + "dataPort", 1, 65535, errors);
            NodeRole role = role(p, file, prefix + "role", false, errors);
            List<String> neighbors = csv(p, file, prefix + "neighbors", 63, errors);
            for (String neighbor : neighbors) validNodeId(neighbor, file, prefix + "neighbors", errors);
            if (errors.size() == before) {
                nodes.put(id, new TopologyConfiguration.Node(id, h, tcp, udp, role,
                        new LinkedHashSet<>(neighbors)));
                endpoints.add(h, tcp, "TCP", file, prefix + "controlPort", id, errors);
                endpoints.add(h, udp, "UDP", file, prefix + "dataPort", id, errors);
            }
        }
        unknownKeys(p, allowed, file, errors);
        for (var n : nodes.values()) {
            String key = "node." + n.nodeId() + ".neighbors";
            for (String neighbor : n.neighbors()) {
                if (neighbor.equals(n.nodeId())) {
                    error(errors, file, key, "auto-vizinhança não permitida");
                } else if (!ids.contains(neighbor)) {
                    error(errors, file, key, "vizinho não declarado: " + neighbor);
                } else if (nodes.containsKey(neighbor)
                        && !nodes.get(neighbor).neighbors().contains(n.nodeId())) {
                    error(errors, file, key, "vizinhança assimétrica com " + neighbor);
                }
            }
        }
        return errors.size() == start ? new TopologyConfiguration(file, nodes) : null;
    }

    private static List<StreamConfiguration> streams(Path file, Path mediaBase, String server,
                                                      List<ConfigurationProblem> errors) {
        Properties p = read(file, errors);
        if (p == null) return List.of();
        List<String> ids = csv(p, file, "stream.ids", 256, errors);
        if (ids.size() < 2) error(errors, file, "stream.ids", "cada servidor deve configurar pelo menos dois fluxos");
        var allowed = new HashSet<>(Set.of("stream.ids"));
        var parsedIds = new HashSet<UUID>();
        var mediaFiles = new HashSet<Path>();
        var result = new ArrayList<StreamConfiguration>();
        for (String rawId : ids) {
            int before = errors.size();
            UUID id = null;
            if (!rawId.matches(UUID_PATTERN)) {
                error(errors, file, "stream.ids", "UUID deve usar representação canónica de 36 caracteres");
            } else {
                id = UUID.fromString(rawId);
                if (!parsedIds.add(id)) error(errors, file, "stream.ids", "UUID repetido");
            }
            String prefix = "stream." + rawId + ".";
            for (String suffix : List.of("title", "file", "fps", "loop")) allowed.add(prefix + suffix);
            String title = required(p, file, prefix + "title", errors);
            if (title != null && (title.indexOf('\0') >= 0
                    || !StandardCharsets.UTF_8.newEncoder().canEncode(title)
                    || title.codePointCount(0, title.length()) > 128
                    || title.getBytes(StandardCharsets.UTF_8).length > 512)) {
                error(errors, file, prefix + "title", "título deve ter 1–128 pontos Unicode, até 512 bytes UTF-8 e sem NUL");
            }
            String media = required(p, file, prefix + "file", errors);
            Path mediaFile = resolve(mediaBase, media, file, prefix + "file", errors);
            if (mediaFile != null) {
                if (!mediaFile.startsWith(mediaBase) || mediaFile.equals(mediaBase)) {
                    error(errors, file, prefix + "file", "ficheiro deve estar dentro de media.base");
                }
                if (!mediaFiles.add(mediaFile)) error(errors, file, prefix + "file", "dois fluxos não podem ser aliases do mesmo ficheiro");
            }
            int fps = integer(p, file, prefix + "fps", 1, 60, errors);
            String loop = required(p, file, prefix + "loop", errors);
            if (loop != null && !loop.equals("true") && !loop.equals("false")) {
                error(errors, file, prefix + "loop", "booleano deve ser true ou false");
            }
            if (errors.size() == before) result.add(new StreamConfiguration(file, id, server,
                    title, mediaFile, fps, Boolean.parseBoolean(loop)));
        }
        unknownKeys(p, allowed, file, errors);
        return List.copyOf(result);
    }

    private static void validateDeployment(Map<String, NodeConfiguration> nodes,
                                          NodeConfiguration bootstrap,
                                          List<ConfigurationProblem> errors) {
        TopologyConfiguration topology = bootstrap.topology().orElseThrow();
        var endpoints = new EndpointOwners();
        endpoints.add(bootstrap.advertisedHost(), bootstrap.controlPort(), "TCP",
                bootstrap.sourceFile(), "control.port", bootstrap.nodeId(), errors);
        var streams = new LinkedHashMap<UUID, StreamConfiguration>();
        for (var n : nodes.values()) {
            if (n.role() == NodeRole.BOOTSTRAP) continue;
            var allowed = topology.nodes().get(n.nodeId());
            if (allowed == null) {
                error(errors, n.sourceFile(), "node.id", "nó não está declarado na topologia");
            } else {
                if (n.role() != allowed.role()) error(errors, n.sourceFile(), "node.role", "papel difere da topologia");
                if (!n.advertisedHost().equals(allowed.host())) error(errors, n.sourceFile(), "advertised.host", "host difere da topologia");
                if (n.controlPort() != allowed.controlPort()) error(errors, n.sourceFile(), "control.port", "porta difere da topologia");
                if (n.dataPort() != allowed.dataPort()) error(errors, n.sourceFile(), "data.port", "porta difere da topologia");
            }
            var contact = n.bootstrap().orElseThrow();
            if (!contact.host().equals(bootstrap.advertisedHost())) {
                error(errors, n.sourceFile(), "bootstrap.host", "host difere do perfil BOOTSTRAP");
            }
            if (contact.port() != bootstrap.controlPort()) {
                error(errors, n.sourceFile(), "bootstrap.port", "porta difere do perfil BOOTSTRAP");
            }
            endpoints.add(n.advertisedHost(), n.controlPort(), "TCP", n.sourceFile(), "control.port", n.nodeId(), errors);
            endpoints.add(n.advertisedHost(), n.dataPort(), "UDP", n.sourceFile(), "data.port", n.nodeId(), errors);
            for (var stream : n.streams()) {
                StreamConfiguration previous = streams.putIfAbsent(stream.streamId(), stream);
                if (previous != null) error(errors, stream.sourceFile(), "stream.ids",
                        "UUID já pertence ao servidor " + previous.serverId() + " em " + previous.sourceFile());
            }
        }
        for (String id : topology.nodes().keySet()) {
            if (!nodes.containsKey(id) || nodes.get(id).role() == NodeRole.BOOTSTRAP) {
                error(errors, topology.sourceFile(), "node.ids", "falta perfil de execução para " + id);
            }
        }
    }

    private static Properties read(Path file, List<ConfigurationProblem> errors) {
        Properties p = new Properties() {
            @Override
            public synchronized Object put(Object key, Object value) {
                if (containsKey(key)) {
                    error(errors, file, key.toString(), "chave duplicada");
                    return get(key);
                }
                return super.put(key, value);
            }
        };
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(reader);
            return p;
        } catch (IOException | IllegalArgumentException ex) {
            error(errors, file, "<file>", "não foi possível ler propriedades UTF-8: " + ex.getMessage());
            return null;
        }
    }

    private static String required(Properties p, Path file, String key,
                                   List<ConfigurationProblem> errors) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) {
            error(errors, file, key, "propriedade obrigatória em falta ou vazia");
            return null;
        }
        return value.strip();
    }

    private static String nodeId(Properties p, Path file, String key, List<ConfigurationProblem> errors) {
        String value = required(p, file, key, errors);
        if (value != null) validNodeId(value, file, key, errors);
        return value;
    }

    private static void validNodeId(String id, Path file, String key, List<ConfigurationProblem> errors) {
        if (!id.matches("[A-Za-z0-9_-]{1,32}")) error(errors, file, key, "nodeId deve respeitar [A-Za-z0-9_-]{1,32}");
    }

    private static NodeRole role(Properties p, Path file, String key, boolean allowBootstrap,
                                 List<ConfigurationProblem> errors) {
        String value = required(p, file, key, errors);
        if (value == null) return null;
        try {
            NodeRole role = NodeRole.valueOf(value);
            if (!allowBootstrap && role == NodeRole.BOOTSTRAP) throw new IllegalArgumentException();
            return role;
        } catch (IllegalArgumentException ex) {
            error(errors, file, key, "papel inválido");
            return null;
        }
    }

    private static int integer(Properties p, Path file, String key, int min, int max,
                               List<ConfigurationProblem> errors) {
        String value = required(p, file, key, errors);
        if (value == null) return 0;
        try {
            if (!value.matches("[0-9]+")) throw new NumberFormatException();
            int result = Integer.parseInt(value);
            if (result < min || result > max) throw new NumberFormatException();
            return result;
        } catch (NumberFormatException ex) {
            error(errors, file, key, "inteiro decimal esperado no intervalo " + min + ".." + max);
            return 0;
        }
    }

    private static String host(Properties p, Path file, String key, boolean allowWildcard,
                               List<ConfigurationProblem> errors) {
        String value = required(p, file, key, errors);
        if (value == null) return null;
        try {
            if (value.length() > 253 || !value.chars().allMatch(c -> c > 32 && c < 127)) {
                throw new URISyntaxException(value, "host ASCII inválido");
            }
            new URI("ott", null, value, -1, null, null, null).parseServerAuthority();
            String result = value.toLowerCase(Locale.ROOT);
            if (result.startsWith("[") && result.endsWith("]")) result = result.substring(1, result.length() - 1);
            if (result.endsWith(".")) result = result.substring(0, result.length() - 1);
            if (result.matches("[0-9.]+") && result.contains(".")) {
                String[] parts = result.split("\\.", -1);
                if (parts.length != 4) throw new IllegalArgumentException();
                for (int i = 0; i < parts.length; i++) {
                    int n = Integer.parseInt(parts[i]);
                    if (n > 255) throw new IllegalArgumentException();
                    parts[i] = Integer.toString(n);
                }
                result = String.join(".", parts);
            }
            String address = result.split("%", 2)[0];
            boolean wildcard = address.equals("0.0.0.0")
                    || (address.contains(":") && address.replace(":", "").matches("0*"));
            if (wildcard && !allowWildcard) throw new IllegalArgumentException();
            return result;
        } catch (URISyntaxException | IllegalArgumentException ex) {
            error(errors, file, key, "host inválido" + (allowWildcard ? "" : " ou wildcard não publicitável"));
            return null;
        }
    }

    private static Path path(Properties p, Path file, String key, List<ConfigurationProblem> errors) {
        return resolve(file.getParent(), required(p, file, key, errors), file, key, errors);
    }

    private static Path resolve(Path base, String value, Path file, String key,
                                List<ConfigurationProblem> errors) {
        if (value == null) return null;
        try {
            return base.resolve(value).toAbsolutePath().normalize();
        } catch (InvalidPathException ex) {
            error(errors, file, key, "caminho inválido");
            return null;
        }
    }

    private static List<String> csv(Properties p, Path file, String key, int max,
                                    List<ConfigurationProblem> errors) {
        String value = required(p, file, key, errors);
        if (value == null) return List.of();
        String[] tokens = value.split(",", -1);
        if (tokens.length > max) error(errors, file, key, "lista excede " + max + " elementos");
        var seen = new LinkedHashSet<String>();
        for (String token : tokens) {
            String item = token.strip();
            if (item.isEmpty()) error(errors, file, key, "elemento vazio na lista");
            else if (!seen.add(item)) error(errors, file, key, "elemento repetido na lista: " + item);
        }
        return List.copyOf(seen);
    }

    private static void unknownKeys(Properties p, Set<String> allowed, Path file,
                                    List<ConfigurationProblem> errors) {
        p.stringPropertyNames().stream().filter(k -> !allowed.contains(k)).sorted()
                .forEach(k -> error(errors, file, k, "propriedade desconhecida ou não aplicável ao papel"));
    }

    private static void error(List<ConfigurationProblem> errors, Path file, String key, String message) {
        errors.add(new ConfigurationProblem(file, key, message));
    }

    private static void failIfAny(List<ConfigurationProblem> errors) throws ConfigurationException {
        if (!errors.isEmpty()) throw new ConfigurationException(errors);
    }

    private static final class EndpointOwners {
        private final Map<String, String> owners = new LinkedHashMap<>();

        void add(String host, int port, String transport, Path file, String key, String node,
                 List<ConfigurationProblem> errors) {
            String endpoint = transport + ":" + host + ":" + port;
            String previous = owners.putIfAbsent(endpoint, node);
            if (previous != null) error(errors, file, key, "endpoint " + endpoint + " já atribuído a " + previous);
        }
    }
}
