package pt.uminho.esr.ott.routing;

import java.util.*;
import java.util.function.LongSupplier;
import java.util.function.Function;

/** In-memory planning domain. All methods must run on the node's existing serial owner. */
public final class RoutingTable {
    public record SessionKey(String nodeId, UUID instanceId, UUID connectionId) {
        public SessionKey {
            RoutingTable.nodeId(nodeId); Objects.requireNonNull(instanceId); Objects.requireNonNull(connectionId);
            if (instanceId.equals(new UUID(0, 0))) throw new IllegalArgumentException("Nonzero instance required");
        }
    }
    public record Advertisement(UUID streamId, String serverId, UUID serverEpoch, SessionKey session,
            long announceSeq, long advertisedCostMicros, List<String> advertisedPath) {
        public Advertisement {
            Objects.requireNonNull(streamId); nodeId(serverId); Objects.requireNonNull(serverEpoch);
            Objects.requireNonNull(session); Objects.requireNonNull(advertisedPath);
            if (announceSeq < 0 || advertisedCostMicros < 0 || advertisedPath.isEmpty() || advertisedPath.size() > 31)
                throw new IllegalArgumentException("Invalid advertisement bounds");
            for (String id : advertisedPath) nodeId(id);
            advertisedPath = List.copyOf(advertisedPath);
        }
    }
    public record Limits(int origins, int streamsPerOrigin, int neighbors, int retiredEpochsPerOrigin) {
        public Limits {
            if (origins < 1 || origins > 64 || streamsPerOrigin < 1 || streamsPerOrigin > 256
                    || neighbors < 1 || neighbors > 63 || retiredEpochsPerOrigin < 1 || retiredEpochsPerOrigin > 8)
                throw new IllegalArgumentException("Invalid table limits");
            Math.multiplyExact(Math.multiplyExact(origins, streamsPerOrigin), neighbors);
        }
        public static Limits forNode(int maxConnections) {
            if (maxConnections < 1 || maxConnections > 64) throw new IllegalArgumentException("Invalid connection limit");
            return new Limits(64, 256, Math.min(maxConnections, 63), 8);
        }
        public int streamCapacity() { return Math.multiplyExact(origins, streamsPerOrigin); }
        public int slotCapacity() { return Math.multiplyExact(streamCapacity(), neighbors); }
    }
    public enum Status { APPLIED, IGNORED, REJECTED }
    public enum Reason {
        NONE, DUPLICATE, STALE_SEQUENCE, GENERATION_EXPIRED, WITHDRAWN_GENERATION,
        STALE_SESSION, STALE_EPOCH, RETIRED_EPOCH, UNKNOWN_STREAM, ORIGIN_CONFLICT,
        INVALID_INPUT, CAPACITY_EXCEEDED, REVISION_EXHAUSTED
    }
    public record Outcome(Status status, Reason reason, Set<UUID> changedStreams) {
        public Outcome {
            Objects.requireNonNull(status); Objects.requireNonNull(reason); Objects.requireNonNull(changedStreams);
            if (changedStreams.size() > 16_384) throw new IllegalArgumentException("Too many changed streams");
            changedStreams = Set.copyOf(changedStreams);
        }
    }
    static void nodeId(String value) {
        Objects.requireNonNull(value);
        if (!value.matches("[A-Za-z0-9_-]{1,32}")) throw new IllegalArgumentException("Invalid node ID");
    }
    static long nextRevision(long current) {
        if (current < 0) throw new IllegalArgumentException("Negative revision");
        return Math.incrementExact(current);
    }

    private record Origin(Optional<UUID> epoch, Map<UUID, Long> retired, int streams) {}
    private record Link(SessionKey session, OptionalLong cost) {}
    private record Slot(long sequence, long began, boolean withdrawn, Advertisement advertisement) {}
    private record Row(UUID streamId, String serverId, Map<String, Slot> slots, boolean localOffer, Route view) {}
    private record State(Map<String, Origin> origins, Map<String, Link> links, Map<UUID, Row> rows) {}
    private record Decision(Status status, Reason reason) {}
    private final String local;
    private final Limits limits;
    private final long candidateTtl, historyTtl;
    private final LongSupplier clock;
    private State state = new State(Map.of(), Map.of(), Map.of());
    private boolean observed;
    private long lastObserved;

    public RoutingTable(String localNodeId, Limits limits, long candidateTtlMillis,
            long historyTtlMillis, LongSupplier nanoTime) {
        nodeId(localNodeId); this.local = localNodeId;
        this.limits = Objects.requireNonNull(limits); this.clock = Objects.requireNonNull(nanoTime);
        if (candidateTtlMillis < 1 || historyTtlMillis < candidateTtlMillis || historyTtlMillis > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid history horizons");
        candidateTtl = Math.multiplyExact(candidateTtlMillis, 1_000_000L);
        historyTtl = Math.multiplyExact(historyTtlMillis, 1_000_000L);
    }

    private long now() {
        long value = clock.getAsLong();
        if (observed && value - lastObserved < 0) throw new IllegalStateException("Monotonic clock regressed");
        lastObserved = value; observed = true; return value;
    }
    private static long age(long now, long began) {
        long age = now - began;
        if (age < 0) throw new IllegalStateException("Monotonic horizon exceeded");
        return age;
    }
    private static Decision apply() { return new Decision(Status.APPLIED, Reason.NONE); }
    private static Decision ignore(Reason reason) { return new Decision(Status.IGNORED, reason); }
    private static Decision reject(Reason reason) { return new Decision(Status.REJECTED, reason); }
    private static Outcome unchanged(Decision decision) { return new Outcome(decision.status(), decision.reason(), Set.of()); }

    /** A private draft: failed validation/revision checks never publish a partially changed source. */
    private final class Change {
        final long time;
        final Map<String, Origin> origins = new LinkedHashMap<>(state.origins());
        final Map<String, Link> links = new LinkedHashMap<>(state.links());
        final Map<UUID, Row> rows = new LinkedHashMap<>(state.rows());
        final Set<UUID> touched = new LinkedHashSet<>();
        Change(long time) { this.time = time; }
        void row(Row row) { rows.put(row.streamId(), row); touched.add(row.streamId()); }
        void slots(Row row, Map<String, Slot> slots) {
            row(new Row(row.streamId(), row.serverId(), Map.copyOf(slots), row.localOffer(), row.view()));
        }
        boolean current(SessionKey session) {
            var link = links.get(session.nodeId()); return link != null && link.session().equals(session);
        }
        void removeSlots(String neighbor) {
            for (var row : List.copyOf(rows.values())) {
                if (row.slots().containsKey(neighbor)) {
                    var slots = new LinkedHashMap<>(row.slots()); slots.remove(neighbor); slots(row, slots);
                }
            }
        }
        Outcome commit() {
            var changed = new LinkedHashSet<UUID>();
            for (UUID id : touched) {
                var row = rows.get(id); var previous = state.rows().get(id);
                var old = previous == null ? null : previous.view();
                long revision = old == null ? 0 : old.revision();
                Route view = project(row, origins, links, time, revision);
                if (!view.equals(old)) {
                    if (old != null) {
                        try { revision = nextRevision(revision); }
                        catch (ArithmeticException exhausted) { return unchanged(reject(Reason.REVISION_EXHAUSTED)); }
                        view = project(row, origins, links, time, revision);
                    }
                    changed.add(id);
                }
                rows.put(id, new Row(id, row.serverId(), row.slots(), row.localOffer(), view));
            }
            State published = new State(Map.copyOf(origins), Map.copyOf(links), Map.copyOf(rows));
            Outcome outcome = new Outcome(Status.APPLIED, Reason.NONE, changed);
            state = published;
            return outcome;
        }
    }

    private Outcome change(Function<Change, Decision> operation) {
        var draft = new Change(now()); var decision = operation.apply(draft);
        return decision.status() == Status.APPLIED ? draft.commit() : unchanged(decision);
    }

    private Route project(Row row, Map<String, Origin> origins, Map<String, Link> links, long time, long revision) {
        var epoch = origins.get(row.serverId()).epoch();
        var candidates = new ArrayList<RouteCandidate>();
        for (Slot slot : row.slots().values()) {
            var ad = slot.advertisement();
            if (ad == null || slot.withdrawn() || age(time, slot.began()) >= candidateTtl) continue;
            var link = links.get(ad.session().nodeId());
            if (link == null || !link.session().equals(ad.session()) || !epoch.equals(Optional.of(ad.serverEpoch()))) continue;
            var path = new ArrayList<>(ad.advertisedPath()); path.add(local);
            Optional<RouteMetric> metric = Optional.empty();
            if (link.cost().isPresent()) {
                try { metric = Optional.of(RouteMetric.accumulate(ad.advertisedCostMicros(), link.cost().orElseThrow(), path.size() - 1)); }
                catch (ArithmeticException overflow) { /* Keep provenance and original TTL, but do not rank an impossible sum. */ }
            }
            candidates.add(new RouteCandidate(row.streamId(), row.serverId(), ad.serverEpoch(), ad.session(), path,
                    ad.announceSeq(), ad.advertisedCostMicros(), metric, slot.began(), candidateTtl));
        }
        candidates.sort((a, b) -> {
            int order = Boolean.compare(b.metric().isPresent(), a.metric().isPresent());
            if (order == 0 && a.metric().isPresent()) order = a.metric().orElseThrow().compareTo(b.metric().orElseThrow());
            return order != 0 ? order : a.upstream().nodeId().compareTo(b.upstream().nodeId());
        });
        Optional<RouteCandidate> selected = row.localOffer() ? Optional.empty()
                : candidates.stream().filter(c -> c.metric().isPresent()).findFirst();
        Optional<RouteMetric> metric = row.localOffer() ? Optional.of(new RouteMetric(0, 0)) : selected.flatMap(RouteCandidate::metric);
        return new Route(row.streamId(), row.serverId(), epoch, row.localOffer(), selected, metric,
                selected.map(RouteCandidate::upstream), Optional.empty(), Optional.empty(), Set.of(), candidates,
                metric.isPresent() ? RouteState.INACTIVE : RouteState.UNAVAILABLE, revision);
    }

    public Outcome bindStream(UUID streamId, String expectedServerId) {
        Objects.requireNonNull(streamId); nodeId(expectedServerId);
        return change(d -> {
            var old = d.rows.get(streamId);
            if (old != null) return old.serverId().equals(expectedServerId) ? ignore(Reason.DUPLICATE) : reject(Reason.ORIGIN_CONFLICT);
            var origin = d.origins.get(expectedServerId);
            if (origin == null) {
                if (d.origins.size() == limits.origins()) return reject(Reason.CAPACITY_EXCEEDED);
                origin = new Origin(Optional.empty(), Map.of(), 0);
            }
            if (origin.streams() == limits.streamsPerOrigin()) return reject(Reason.CAPACITY_EXCEEDED);
            d.origins.put(expectedServerId, new Origin(origin.epoch(), origin.retired(), origin.streams() + 1));
            d.row(new Row(streamId, expectedServerId, Map.of(), false, null)); return apply();
        });
    }
    public Outcome replaceSession(Optional<SessionKey> expected, SessionKey replacement) {
        Objects.requireNonNull(expected); Objects.requireNonNull(replacement);
        return change(d -> {
            if (replacement.nodeId().equals(local) || expected.isPresent() && !expected.orElseThrow().nodeId().equals(replacement.nodeId()))
                return reject(Reason.INVALID_INPUT);
            var old = d.links.get(replacement.nodeId());
            if (old != null && old.session().equals(replacement)) return ignore(Reason.DUPLICATE);
            if (!expected.equals(Optional.ofNullable(old).map(Link::session))) return ignore(Reason.STALE_SESSION);
            if (old == null && d.links.size() == limits.neighbors()) return reject(Reason.CAPACITY_EXCEEDED);
            d.removeSlots(replacement.nodeId()); d.links.put(replacement.nodeId(), new Link(replacement, OptionalLong.empty()));
            return apply();
        });
    }
    public Outcome removeSession(SessionKey session) {
        Objects.requireNonNull(session);
        return change(d -> {
            if (!d.current(session)) return ignore(Reason.STALE_SESSION);
            d.removeSlots(session.nodeId()); d.links.remove(session.nodeId()); return apply();
        });
    }
    public Outcome setLinkCost(SessionKey session, OptionalLong costMicros) {
        Objects.requireNonNull(session); Objects.requireNonNull(costMicros);
        if (costMicros.isPresent() && costMicros.orElseThrow() < 1) throw new IllegalArgumentException("Positive measured cost required");
        return change(d -> {
            if (!d.current(session)) return ignore(Reason.STALE_SESSION);
            d.links.put(session.nodeId(), new Link(session, costMicros));
            for (var row : d.rows.values()) if (row.slots().containsKey(session.nodeId())) d.touched.add(row.streamId());
            return apply();
        });
    }
    public Outcome confirmEpoch(String serverId, Optional<UUID> expectedCurrent, UUID replacement) {
        nodeId(serverId); Objects.requireNonNull(expectedCurrent); Objects.requireNonNull(replacement);
        return change(d -> {
            var origin = d.origins.get(serverId);
            if (origin == null) return reject(Reason.UNKNOWN_STREAM);
            if (origin.epoch().equals(Optional.of(replacement))) return ignore(Reason.DUPLICATE);
            if (!origin.epoch().equals(expectedCurrent)) return ignore(Reason.STALE_EPOCH);
            var retired = new LinkedHashMap<UUID, Long>();
            origin.retired().forEach((epoch, began) -> { if (age(d.time, began) < historyTtl) retired.put(epoch, began); });
            if (retired.containsKey(replacement)) return reject(Reason.RETIRED_EPOCH);
            if (origin.epoch().isPresent()) {
                if (retired.size() == limits.retiredEpochsPerOrigin()) return reject(Reason.CAPACITY_EXCEEDED);
                retired.put(origin.epoch().orElseThrow(), d.time);
            }
            d.origins.put(serverId, new Origin(Optional.of(replacement), Map.copyOf(retired), origin.streams()));
            for (var row : List.copyOf(d.rows.values()))
                if (row.serverId().equals(serverId)) d.row(new Row(row.streamId(), serverId, Map.of(), false, row.view()));
            return apply();
        });
    }
    private Decision context(Change d, UUID stream, UUID epoch, SessionKey session) {
        var row = d.rows.get(stream);
        if (row == null) return reject(Reason.UNKNOWN_STREAM);
        if (!d.current(session)) return ignore(Reason.STALE_SESSION);
        if (!d.origins.get(row.serverId()).epoch().equals(Optional.of(epoch))) return ignore(Reason.STALE_EPOCH);
        return apply();
    }
    public Outcome offerLocal(UUID streamId, UUID epoch) { return localOffer(streamId, epoch, true); }
    public Outcome removeLocalOffer(UUID streamId, UUID epoch) { return localOffer(streamId, epoch, false); }
    private Outcome localOffer(UUID streamId, UUID epoch, boolean present) {
        Objects.requireNonNull(streamId); Objects.requireNonNull(epoch);
        return change(d -> {
            var row = d.rows.get(streamId);
            if (row == null) return reject(Reason.UNKNOWN_STREAM);
            if (!row.serverId().equals(local)) return reject(Reason.INVALID_INPUT);
            if (!d.origins.get(local).epoch().equals(Optional.of(epoch))) return ignore(Reason.STALE_EPOCH);
            if (row.localOffer() == present) return ignore(Reason.DUPLICATE);
            d.row(new Row(streamId, local, row.slots(), present, row.view())); return apply();
        });
    }
    private Slot currentSlot(Change draft, Row row, String neighbor) {
        var slot = row.slots().get(neighbor);
        return slot != null && age(draft.time, slot.began()) < historyTtl ? slot : null;
    }
    private Decision store(Change draft, Row row, String neighbor, Slot slot) {
        if (!row.slots().containsKey(neighbor) && row.slots().size() == limits.neighbors())
            return reject(Reason.CAPACITY_EXCEEDED);
        var slots = new LinkedHashMap<>(row.slots()); slots.put(neighbor, slot); draft.slots(row, slots); return apply();
    }
    public Outcome announce(Advertisement advertisement) {
        var ad = Objects.requireNonNull(advertisement);
        return change(d -> {
            var row = d.rows.get(ad.streamId());
            if (row == null) return reject(Reason.UNKNOWN_STREAM);
            if (!row.serverId().equals(ad.serverId())) return reject(Reason.ORIGIN_CONFLICT);
            var context = context(d, ad.streamId(), ad.serverEpoch(), ad.session());
            if (context.status() != Status.APPLIED) return context;
            var path = ad.advertisedPath();
            if (!path.getFirst().equals(ad.serverId()) || !path.getLast().equals(ad.session().nodeId())
                    || path.contains(local) || new HashSet<>(path).size() != path.size()
                    || (ad.advertisedCostMicros() == 0) != (path.size() == 1))
                return reject(Reason.INVALID_INPUT);
            var old = currentSlot(d, row, ad.session().nodeId());
            long began = d.time;
            if (old != null) {
                int order = Long.compare(ad.announceSeq(), old.sequence());
                if (order < 0) return ignore(Reason.STALE_SEQUENCE);
                if (order == 0) {
                    if (old.withdrawn()) return ignore(Reason.WITHDRAWN_GENERATION);
                    if (old.advertisement() == null || age(d.time, old.began()) >= candidateTtl)
                        return ignore(Reason.GENERATION_EXPIRED);
                    var previous = old.advertisement();
                    int improvement = Long.compare(ad.advertisedCostMicros(), previous.advertisedCostMicros());
                    if (improvement == 0) improvement = Integer.compare(path.size(), previous.advertisedPath().size());
                    if (improvement >= 0) return ignore(Reason.DUPLICATE);
                    began = old.began();
                }
            }
            var link = d.links.get(ad.session().nodeId());
            if (link.cost().isPresent()) {
                try { RouteMetric.accumulate(ad.advertisedCostMicros(), link.cost().orElseThrow(), path.size()); }
                catch (ArithmeticException overflow) { return reject(Reason.INVALID_INPUT); }
            }
            return store(d, row, ad.session().nodeId(), new Slot(ad.announceSeq(), began, false, ad));
        });
    }
    public Outcome withdraw(UUID streamId, UUID epoch, SessionKey session, long sequence) {
        Objects.requireNonNull(streamId); Objects.requireNonNull(epoch); Objects.requireNonNull(session);
        if (sequence < 0) throw new IllegalArgumentException("Negative sequence");
        return change(d -> {
            var context = context(d, streamId, epoch, session);
            if (context.status() != Status.APPLIED) return context;
            var row = d.rows.get(streamId); var old = currentSlot(d, row, session.nodeId());
            long began = d.time;
            if (old != null) {
                if (sequence < old.sequence()) return ignore(Reason.STALE_SEQUENCE);
                if (sequence == old.sequence()) {
                    if (old.withdrawn()) return ignore(Reason.DUPLICATE);
                    began = old.began();
                }
            }
            return store(d, row, session.nodeId(), new Slot(sequence, began, true, null));
        });
    }
    public Outcome expire() {
        return change(d -> {
            for (var entry : List.copyOf(d.origins.entrySet())) {
                var origin = entry.getValue(); var retired = new LinkedHashMap<UUID, Long>();
                origin.retired().forEach((epoch, began) -> { if (age(d.time, began) < historyTtl) retired.put(epoch, began); });
                if (retired.size() != origin.retired().size())
                    d.origins.put(entry.getKey(), new Origin(origin.epoch(), Map.copyOf(retired), origin.streams()));
            }
            for (var row : List.copyOf(d.rows.values())) {
                var slots = new LinkedHashMap<String, Slot>(); boolean cleaned = false;
                for (var entry : row.slots().entrySet()) {
                    var slot = entry.getValue(); long elapsed = age(d.time, slot.began());
                    if (elapsed >= historyTtl) { cleaned = true; continue; }
                    if (elapsed >= candidateTtl && slot.advertisement() != null) {
                        slot = new Slot(slot.sequence(), slot.began(), slot.withdrawn(), null); cleaned = true;
                    }
                    slots.put(entry.getKey(), slot);
                }
                if (cleaned) d.slots(row, slots);
            }
            return apply();
        });
    }
    public Optional<Route> snapshot(UUID streamId) {
        Objects.requireNonNull(streamId); var draft = new Change(now());
        if (!draft.rows.containsKey(streamId)) return Optional.empty();
        draft.touched.add(streamId);
        if (draft.commit().status() == Status.REJECTED) throw new IllegalStateException("Route revision exhausted");
        return Optional.of(state.rows().get(streamId).view());
    }
}
