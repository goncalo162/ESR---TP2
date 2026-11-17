package pt.uminho.esr.ott.routing;

import java.util.*;
import pt.uminho.esr.ott.overlay.Neighbor;

/** Immutable view of one admitted child, not the aggregate upstream identity. */
public record Subscription(UUID streamId, UUID serverEpoch, Neighbor.Session child,
        UUID subscriptionId, long revision, UUID requestId, List<String> expectedPath,
        long requestedLeaseMs, State state, Optional<Lease> validity) {
    public enum State { PENDING, ACTIVE }

    /** A duration anchored before the request queue; safe across nanoTime wrap. */
    public record Lease(long beganNanos, long durationNanos) {
        public Lease {
            if (durationNanos < 1 || durationNanos > 0xffff_ffffL * 1_000_000L)
                throw new IllegalArgumentException("Invalid lease duration");
        }
        public long remainingNanos(long now) {
            long age=now-beganNanos;
            if(age<0) throw new IllegalArgumentException("Monotonic lease horizon");
            return age>=durationNanos ? 0 : durationNanos-age;
        }
        public long remainingMillis(long now) { return remainingNanos(now)/1_000_000L; }
    }

    public Subscription {
        Objects.requireNonNull(streamId); Objects.requireNonNull(serverEpoch);
        Objects.requireNonNull(child); Objects.requireNonNull(subscriptionId);
        Objects.requireNonNull(requestId); Objects.requireNonNull(state); Objects.requireNonNull(validity);
        expectedPath=List.copyOf(expectedPath);
        if(revision<0 || requestedLeaseMs<1 || requestedLeaseMs>0xffff_ffffL
                || expectedPath.size()<2 || expectedPath.size()>32
                || !expectedPath.getLast().equals(child.nodeId())
                || new HashSet<>(expectedPath).size()!=expectedPath.size()
                || (state==State.ACTIVE)!=validity.isPresent())
            throw new IllegalArgumentException("Invalid subscription");
        for(String id:expectedPath) RoutingTable.nodeId(id);
    }
}
