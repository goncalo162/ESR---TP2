package pt.uminho.esr.ott.routing;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import pt.uminho.esr.ott.routing.RoutingTable.SessionKey;

/** Immutable remote candidate. Path includes the local receiver as its last ID. */
public record RouteCandidate(UUID streamId, String serverId, UUID serverEpoch,
        SessionKey upstream, List<String> path, long announceSeq, long advertisedCostMicros,
        Optional<RouteMetric> metric, long generationStartedAtNanos, long candidateTtlNanos) {
    public RouteCandidate {
        Objects.requireNonNull(streamId); RoutingTable.nodeId(serverId);
        Objects.requireNonNull(serverEpoch); Objects.requireNonNull(upstream);
        Objects.requireNonNull(path); Objects.requireNonNull(metric);
        if (path.size() < 2 || path.size() > 32 || announceSeq < 0 || advertisedCostMicros < 0
                || candidateTtlNanos < 1 || candidateTtlNanos > Integer.MAX_VALUE * 1_000_000L)
            throw new IllegalArgumentException("Invalid candidate bounds");
        var ids = new HashSet<String>();
        for (String id : path) {
            RoutingTable.nodeId(id);
            if (!ids.add(id)) throw new IllegalArgumentException("Repeated path ID");
        }
        if (!path.getFirst().equals(serverId) || !path.get(path.size() - 2).equals(upstream.nodeId())
                || (advertisedCostMicros == 0) != (path.size() == 2)
                || metric.isPresent() && (metric.orElseThrow().hopCount() != path.size() - 1
                    || metric.orElseThrow().costMicros() <= advertisedCostMicros))
            throw new IllegalArgumentException("Inconsistent candidate");
        path = List.copyOf(path);
    }
}
