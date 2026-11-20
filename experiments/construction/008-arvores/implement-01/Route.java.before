package pt.uminho.esr.ott.routing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import pt.uminho.esr.ott.routing.RoutingTable.SessionKey;
import pt.uminho.esr.ott.overlay.Neighbor;
import java.util.Map;
import java.util.LinkedHashMap;

/** Planning snapshot; selection alone never authorizes data transmission. */
public record Route(UUID streamId, String serverId, Optional<UUID> serverEpoch,
        boolean localSource, Optional<RouteCandidate> selectedCandidate,
        Optional<RouteMetric> selectedMetric, Optional<SessionKey> knownUpstream,
        Optional<SessionKey> activeUpstream, Optional<SessionKey> pendingUpstream,
        Set<String> downstreams, List<RouteCandidate> candidates, RouteState state, long revision)
{

    //* === Construtor === *

    public Route
    {
        Objects.requireNonNull(streamId);
        RoutingTable.nodeId(serverId);
        Objects.requireNonNull(serverEpoch);
        Objects.requireNonNull(selectedCandidate);
        Objects.requireNonNull(selectedMetric);
        Objects.requireNonNull(knownUpstream);
        Objects.requireNonNull(activeUpstream);
        Objects.requireNonNull(pendingUpstream);
        Objects.requireNonNull(downstreams);
        Objects.requireNonNull(candidates);
        Objects.requireNonNull(state);

        if(revision < 0 || candidates.size() > 63 || activeUpstream.isPresent()
                || pendingUpstream.isPresent() || !downstreams.isEmpty())
            throw new IllegalArgumentException("No data authorization in a planning route");

        candidates = List.copyOf(candidates);
        downstreams = Set.copyOf(downstreams);

        var neighbors = new java.util.HashSet<String>();

        for(var c : candidates)
        {
            if(!streamId.equals(c.streamId()) || !serverId.equals(c.serverId())
                    || !serverEpoch.equals(Optional.of(c.serverEpoch())) || !neighbors.add(c.upstream().nodeId()))
                throw new IllegalArgumentException("Inconsistent route candidates");
        }

        if(localSource)
        {
            if(serverEpoch.isEmpty() || selectedCandidate.isPresent() || knownUpstream.isPresent()
                    || !selectedMetric.equals(Optional.of(new RouteMetric(0, 0))))
                throw new IllegalArgumentException("Inconsistent local source");
        }
        else if(selectedCandidate.isPresent())
        {
            var selected = selectedCandidate.orElseThrow();

            if(!candidates.contains(selected) || selected.metric().isEmpty()
                    || !selectedMetric.equals(selected.metric())
                    || !knownUpstream.equals(Optional.of(selected.upstream())))
                throw new IllegalArgumentException("Inconsistent selection");
        }
        else if(selectedMetric.isPresent() || knownUpstream.isPresent())
            throw new IllegalArgumentException("Metric without a selected route");

        if(state != (selectedMetric.isPresent() ? RouteState.INACTIVE : RouteState.UNAVAILABLE))
            throw new IllegalArgumentException("Planning state does not match availability");
    }

    /** Data authorization is deliberately separate from the planning Route. */
    public record Confirmation(UUID streamId, String serverId, UUID serverEpoch,
            boolean localSource, Optional<Neighbor.Session> upstream, List<String> path,
            Map<String, Subscription> downstreams, Set<UUID> localConsumers,
            Optional<Subscription.Lease> upstreamLease, long revision) {
        public Confirmation {
            Objects.requireNonNull(streamId); RoutingTable.nodeId(serverId);
            Objects.requireNonNull(serverEpoch); Objects.requireNonNull(upstream);
            Objects.requireNonNull(upstreamLease);
            path = List.copyOf(path); downstreams = Map.copyOf(downstreams);
            localConsumers = Set.copyOf(localConsumers);
            if (revision < 0 || path.isEmpty() || path.size() > 32
                    || !path.getFirst().equals(serverId) || Set.copyOf(path).size() != path.size()
                    || localSource != upstream.isEmpty() || localSource != upstreamLease.isEmpty()
                    || localSource && path.size() != 1 || !localSource && path.size() < 2
                    || downstreams.size() > 63 || localConsumers.size() > 4)
                throw new IllegalArgumentException("Inconsistent forwarding confirmation");
            for (String id : path) RoutingTable.nodeId(id);
            if (upstream.isPresent() && !path.get(path.size()-2).equals(upstream.orElseThrow().nodeId()))
                throw new IllegalArgumentException("Upstream does not match confirmed path");
            for (var e : downstreams.entrySet()) {
                var child = e.getValue();
                if (!e.getKey().equals(child.child().nodeId()) || !streamId.equals(child.streamId())
                        || !serverEpoch.equals(child.serverEpoch()) || child.state() != Subscription.State.ACTIVE
                        || path.contains(e.getKey()) || child.expectedPath().size() != path.size()+1
                        || !child.expectedPath().subList(0,path.size()).equals(path))
                    throw new IllegalArgumentException("Inconsistent forwarding child");
            }
        }
        /** Ages immutable grants; never renews them or mutates the owner's state. */
        public Optional<Confirmation> visibleAt(long now) {
            if (upstreamLease.isPresent() && upstreamLease.orElseThrow().remainingNanos(now) == 0)
                return Optional.empty();
            var children = new LinkedHashMap<String,Subscription>();
            downstreams.forEach((id,s) -> {
                if (s.validity().orElseThrow().remainingNanos(now)>0) children.put(id,s);
            });
            if (children.isEmpty() && localConsumers.isEmpty()) return Optional.empty();
            return Optional.of(new Confirmation(streamId,serverId,serverEpoch,localSource,upstream,path,
                    children,localConsumers,upstreamLease,revision));
        }
    }
}
