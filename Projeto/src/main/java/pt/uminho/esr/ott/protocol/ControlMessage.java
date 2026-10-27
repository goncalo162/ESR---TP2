package pt.uminho.esr.ott.protocol;

import pt.uminho.esr.ott.overlay.NodeRole;

import java.util.List;
import java.util.UUID;

/** Envelope e dados imutáveis; validação do formato é responsabilidade do codec. */
public record ControlMessage(int version, MessageType type, UUID requestId,
                             String senderId, UUID senderInstance, Payload payload) {
    public ControlMessage(MessageType type, UUID requestId, String senderId,
                          UUID senderInstance, Payload payload) {
        this(1, type, requestId, senderId, senderInstance, payload);
    }

    public sealed interface Payload permits EndpointPayload, RegistrationResult, Empty,
            Leave, Probe, Announcement, Withdrawal, Activation, ActivationAck,
            Deactivation, Trace, TraceResult, ErrorPayload { }

    public record Endpoint(String nodeId, UUID instanceId, String host, int controlPort,
                           int dataPort, NodeRole role) { }

    /** Metadados no fio; não é o catálogo dinâmico da TASK-012. */
    public record StreamInfo(UUID streamId, String serverId, String title, int codec,
                             int fps, UUID serverEpoch) { }

    public record EndpointPayload(Endpoint endpoint) implements Payload { }

    public record RegistrationResult(long leaseMs, List<Endpoint> neighbors) implements Payload {
        public RegistrationResult { neighbors = List.copyOf(neighbors); }
    }

    public enum Empty implements Payload { INSTANCE }
    public record Leave(int reason) implements Payload { }
    public record Probe(long probeId) implements Payload { }

    public record Announcement(StreamInfo stream, long announceSeq, long costMicros,
                               List<String> path) implements Payload {
        public Announcement { path = List.copyOf(path); }
    }

    public record Withdrawal(UUID streamId, UUID serverEpoch, long announceSeq) implements Payload { }

    public record Activation(UUID streamId, UUID serverEpoch, UUID subscriptionId,
                             long revision, long leaseMs, List<String> expectedPath) implements Payload {
        public Activation { expectedPath = List.copyOf(expectedPath); }
    }

    public record ActivationAck(UUID streamId, UUID serverEpoch, UUID subscriptionId,
                                long revision, long grantedLeaseMs) implements Payload { }

    public record Deactivation(UUID streamId, UUID serverEpoch, UUID subscriptionId,
                               long revision) implements Payload { }

    public record Trace(UUID streamId, UUID serverEpoch, UUID traceId, int ttl,
                        List<String> visited) implements Payload {
        public Trace { visited = List.copyOf(visited); }
    }

    public record TraceResult(UUID streamId, UUID traceId, List<String> visited) implements Payload {
        public TraceResult { visited = List.copyOf(visited); }
    }

    public record ErrorPayload(ErrorCode code, int relatedType, String detail) implements Payload { }
}
