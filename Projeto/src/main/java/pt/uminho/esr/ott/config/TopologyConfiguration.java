package pt.uminho.esr.ott.config;

import pt.uminho.esr.ott.overlay.NodeRole;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Grafo permitido, independente dos nós que já estejam em execução. */
public record TopologyConfiguration(Path sourceFile, Map<String, Node> nodes) {
    public TopologyConfiguration {
        Objects.requireNonNull(sourceFile);
        nodes = Collections.unmodifiableMap(new LinkedHashMap<>(nodes));
    }

    public record Node(String nodeId, String host, int controlPort, int dataPort,
                       NodeRole role, Set<String> neighbors) {
        public Node {
            Objects.requireNonNull(nodeId);
            Objects.requireNonNull(host);
            Objects.requireNonNull(role);
            neighbors = Collections.unmodifiableSet(new LinkedHashSet<>(neighbors));
        }
    }
}
