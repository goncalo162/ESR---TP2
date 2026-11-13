package pt.uminho.esr.ott.routing;

/** Local microsecond cost; this value is never a control-wire ordinal or RTT estimator. */
public record RouteMetric(long costMicros, int hopCount) implements Comparable<RouteMetric> {
    public RouteMetric {
        if (costMicros < 0 || hopCount < 0 || hopCount > 31 || (costMicros == 0) != (hopCount == 0))
            throw new IllegalArgumentException("Invalid route metric");
    }

    public static RouteMetric accumulate(long advertisedCostMicros, long linkCostMicros, int hopCount) {
        if (advertisedCostMicros < 0 || linkCostMicros < 1 || hopCount < 1)
            throw new IllegalArgumentException("Invalid remote cost");
        return new RouteMetric(Math.addExact(advertisedCostMicros, linkCostMicros), hopCount);
    }

    @Override public int compareTo(RouteMetric other) {
        int cost = Long.compare(costMicros, other.costMicros);
        return cost != 0 ? cost : Integer.compare(hopCount, other.hopCount);
    }
}
