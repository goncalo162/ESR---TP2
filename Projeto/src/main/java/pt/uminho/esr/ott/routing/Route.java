package pt.uminho.esr.ott.routing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import pt.uminho.esr.ott.routing.RoutingTable.SessionKey;

/** Planning snapshot; selection alone never authorizes data transmission. */
public record Route(UUID streamId, String serverId, Optional<UUID> serverEpoch,
        boolean localSource, Optional<RouteCandidate> selectedCandidate,
        Optional<RouteMetric> selectedMetric, Optional<SessionKey> knownUpstream,
        Optional<SessionKey> activeUpstream, Optional<SessionKey> pendingUpstream,
        Set<String> downstreams, List<RouteCandidate> candidates, RouteState state, long revision) {
    public Route {
        Objects.requireNonNull(streamId); RoutingTable.nodeId(serverId);
        Objects.requireNonNull(serverEpoch); Objects.requireNonNull(selectedCandidate);
        Objects.requireNonNull(selectedMetric); Objects.requireNonNull(knownUpstream);
        Objects.requireNonNull(activeUpstream); Objects.requireNonNull(pendingUpstream);
        Objects.requireNonNull(downstreams); Objects.requireNonNull(candidates); Objects.requireNonNull(state);
        if (revision < 0 || candidates.size() > 63 || activeUpstream.isPresent()
                || pendingUpstream.isPresent() || !downstreams.isEmpty())
            throw new IllegalArgumentException("No data authorization in a planning route");
        candidates = List.copyOf(candidates); downstreams = Set.copyOf(downstreams);
        var neighbors = new java.util.HashSet<String>();
        for (var c : candidates) {
            if (!streamId.equals(c.streamId()) || !serverId.equals(c.serverId())
                    || !serverEpoch.equals(Optional.of(c.serverEpoch())) || !neighbors.add(c.upstream().nodeId()))
                throw new IllegalArgumentException("Inconsistent route candidates");
        }
        if (localSource) {
            if (serverEpoch.isEmpty() || selectedCandidate.isPresent() || knownUpstream.isPresent()
                    || !selectedMetric.equals(Optional.of(new RouteMetric(0, 0))))
                throw new IllegalArgumentException("Inconsistent local source");
        } else if (selectedCandidate.isPresent()) {
            var selected = selectedCandidate.orElseThrow();
            if (!candidates.contains(selected) || selected.metric().isEmpty()
                    || !selectedMetric.equals(selected.metric())
                    || !knownUpstream.equals(Optional.of(selected.upstream())))
                throw new IllegalArgumentException("Inconsistent selection");
        } else if (selectedMetric.isPresent() || knownUpstream.isPresent()) {
            throw new IllegalArgumentException("Metric without a selected route");
        }
        if (state != (selectedMetric.isPresent() ? RouteState.INACTIVE : RouteState.UNAVAILABLE))
            throw new IllegalArgumentException("Planning state does not match availability");
    }
}
