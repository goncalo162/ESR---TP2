package pt.uminho.esr.ott.streaming;

import java.util.*;
import pt.uminho.esr.ott.routing.*;

/** Owner-published projections. Expiry of a view never mutates the routing domain. */
public final class StreamCatalog {
    public record Entry(StreamInfo info, RouteState state, Optional<RouteMetric> metric,
            Optional<RoutingTable.SessionKey> knownUpstream, boolean localOffer, OptionalLong remainingNanos) {
        public Entry {
            Objects.requireNonNull(info); Objects.requireNonNull(state); Objects.requireNonNull(metric);
            Objects.requireNonNull(knownUpstream); Objects.requireNonNull(remainingNanos);
            if (localOffer == remainingNanos.isPresent() || remainingNanos.isPresent() && remainingNanos.orElseThrow() < 1
                    || state != (metric.isPresent() ? RouteState.INACTIVE : RouteState.UNAVAILABLE))
                throw new IllegalArgumentException("Inconsistent catalog entry");
        }
    }
    public record Snapshot(long capturedAtNanos, Map<UUID, Entry> entries) {
        public Snapshot {
            Objects.requireNonNull(entries);
            if (entries.size() > 16_384) throw new IllegalArgumentException("Catalog capacity");
            for (var entry : entries.entrySet())
                if (!entry.getKey().equals(entry.getValue().info().streamId())) throw new IllegalArgumentException("Catalog key");
            entries = Map.copyOf(entries);
        }
        public Snapshot visibleAt(long now) {
            long age = now - capturedAtNanos;
            if (age < 0) throw new IllegalArgumentException("Monotonic horizon");
            var visible = new LinkedHashMap<UUID, Entry>();
            entries.forEach((id, e) -> {
                if (e.localOffer()) visible.put(id, e);
                else if (e.remainingNanos().orElseThrow() > age) {
                    // Remaining metadata validity is the maximum over all candidates, not
                    // the selected route's lifetime. This projection has no candidate set
                    // with which to reselect: never relabel an older cost/upstream as current.
                    visible.put(id, new Entry(e.info(), age == 0 ? e.state() : RouteState.UNAVAILABLE,
                            age == 0 ? e.metric() : Optional.empty(),
                            age == 0 ? e.knownUpstream() : Optional.empty(), false,
                            OptionalLong.of(e.remainingNanos().orElseThrow() - age)));
                }
            });
            return new Snapshot(now, visible);
        }
    }
    private final int capacity;
    private volatile Snapshot view = new Snapshot(0, Map.of());
    public StreamCatalog(int capacity) {
        if (capacity < 1 || capacity > 16_384) throw new IllegalArgumentException("Catalog capacity");
        this.capacity = capacity;
    }
    /** Only the composing RoutingService calls this mutation, on its serial owner. */
    public void replace(Snapshot snapshot) {
        Objects.requireNonNull(snapshot);
        if (snapshot.entries().size() > capacity) throw new IllegalArgumentException("Catalog capacity");
        view = snapshot;
    }
    public Snapshot snapshot() { return view; }
}
