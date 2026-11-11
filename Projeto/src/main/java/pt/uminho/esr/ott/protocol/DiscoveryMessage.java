package pt.uminho.esr.ott.protocol;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Payloads of the single control-v2 envelope; no transport/framing of their own. */
public sealed interface DiscoveryMessage extends ControlMessage.Payload
        permits DiscoveryMessage.Query, DiscoveryMessage.Snapshot {
    UUID helloRequestId(); String recipientId(); UUID recipientInstance();
    record Query(UUID helloRequestId, String recipientId, UUID recipientInstance) implements DiscoveryMessage {
        public Query { Objects.requireNonNull(helloRequestId); Objects.requireNonNull(recipientId); Objects.requireNonNull(recipientInstance); }
    }
    record Snapshot(UUID helloRequestId, String recipientId, UUID recipientInstance,
                    long generation, List<ControlMessage.Endpoint> directNeighbors) implements DiscoveryMessage {
        public Snapshot {
            Objects.requireNonNull(helloRequestId); Objects.requireNonNull(recipientId); Objects.requireNonNull(recipientInstance);
            Objects.requireNonNull(directNeighbors);
            if (directNeighbors.size() > 63) throw new IllegalArgumentException("Adjacency cap 63");
            directNeighbors = List.copyOf(directNeighbors);
        }
    }
}
