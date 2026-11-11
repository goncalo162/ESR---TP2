package pt.uminho.esr.ott.config;

import pt.uminho.esr.ott.overlay.NodeRole;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuração validada, sem identidade de execução, sockets ou estado de rede.
 * @param dataPort zero apenas no BOOTSTRAP, que não tem transporte UDP
 */
public record NodeConfiguration(
        Path sourceFile, String nodeId, NodeRole role, String bindHost,
        String advertisedHost, int controlPort, int dataPort,
        Optional<BootstrapContact> bootstrap, Optional<Path> topologyFile,
        Optional<Path> streamsFile, Optional<Path> mediaBase,
        Optional<TopologyConfiguration> topology, List<StreamConfiguration> streams,
        RuntimeConfiguration runtime) {

    public NodeConfiguration {
        Objects.requireNonNull(sourceFile);
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(role);
        Objects.requireNonNull(bindHost);
        Objects.requireNonNull(advertisedHost);
        Objects.requireNonNull(bootstrap);
        Objects.requireNonNull(topologyFile);
        Objects.requireNonNull(streamsFile);
        Objects.requireNonNull(mediaBase);
        Objects.requireNonNull(topology);
        Objects.requireNonNull(runtime);
        if (role != NodeRole.BOOTSTRAP && runtime.control().queueCapacity() < 2)
            throw new IllegalArgumentException("Overlay discovery requires control.queue.capacity>=2");
        streams = List.copyOf(streams);
    }

    /** Compatibilidade com o construtor anterior à parametrização operacional. */
    public NodeConfiguration(
            Path sourceFile, String nodeId, NodeRole role, String bindHost,
            String advertisedHost, int controlPort, int dataPort,
            Optional<BootstrapContact> bootstrap, Optional<Path> topologyFile,
            Optional<Path> streamsFile, Optional<Path> mediaBase,
            Optional<TopologyConfiguration> topology, List<StreamConfiguration> streams) {
        this(sourceFile, nodeId, role, bindHost, advertisedHost, controlPort, dataPort,
                bootstrap, topologyFile, streamsFile, mediaBase, topology, streams,
                RuntimeConfiguration.defaults());
    }

    /** Intrinsic overlay capability, never an operational switch. */
    public boolean discoveryRequired() { return role != NodeRole.BOOTSTRAP; }

    /** Contacto conhecido para obter a vizinhança, sem resolução de DNS na carga. */
    public record BootstrapContact(String host, int port) {
        public BootstrapContact {
            Objects.requireNonNull(host);
        }
    }
}
