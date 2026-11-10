package pt.uminho.esr.ott.overlay;

import pt.uminho.esr.ott.protocol.ControlMessage;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Immutable configured edge and its optional, confirmed transport session. */
public record Neighbor(String nodeId, ControlMessage.Endpoint configured, State state,
                       Optional<Session> session, long retryDelayMillis) {
    private static final UUID ZERO = new UUID(0, 0);
    public enum State { DISCONNECTED, CONNECTING, HANDSHAKING, ACTIVE, CLOSED }
    public Neighbor {
        Objects.requireNonNull(nodeId); Objects.requireNonNull(configured);
        Objects.requireNonNull(state); Objects.requireNonNull(session);
        NeighborManager.validateEndpoint(configured, true);
        if (!nodeId.equals(configured.nodeId()) || !ZERO.equals(configured.instanceId())
                || retryDelayMillis < 0 || (state == State.ACTIVE) != session.isPresent()
                || session.isPresent() && !nodeId.equals(session.orElseThrow().nodeId()))
            throw new IllegalArgumentException("Inconsistent neighbor snapshot");
    }
    public record Session(String nodeId, UUID instanceId, UUID connectionId,
                          ControlMessage.Endpoint endpoint) {
        public Session {
            Objects.requireNonNull(nodeId); Objects.requireNonNull(instanceId);
            Objects.requireNonNull(connectionId); Objects.requireNonNull(endpoint);
            NeighborManager.validateEndpoint(endpoint, false);
            if (ZERO.equals(instanceId) || !nodeId.equals(endpoint.nodeId())
                    || !instanceId.equals(endpoint.instanceId()))
                throw new IllegalArgumentException("Inconsistent confirmed session");
        }
    }
}
