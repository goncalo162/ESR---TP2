package pt.uminho.esr.ott.routing;

/** Local microsecond cost; this value is never a control-wire ordinal or RTT estimator. */
public record RouteMetric(long costMicros, int hopCount) implements Comparable<RouteMetric>
{

    //* === Construtor === *

    public RouteMetric
    {
        if(costMicros < 0 || hopCount < 0 || hopCount > 31 || (costMicros == 0) != (hopCount == 0))
            throw new IllegalArgumentException("Invalid route metric");
    }



    //* === Métodos Estáticos === *

    public static RouteMetric accumulate(long advertisedCostMicros, long linkCostMicros, int hopCount)
    {
        if(advertisedCostMicros < 0 || linkCostMicros < 1 || hopCount < 1)
            throw new IllegalArgumentException("Invalid remote cost");

        return new RouteMetric(Math.addExact(advertisedCostMicros, linkCostMicros), hopCount);
    }



    public static boolean improves(long current, long replacement, int percent)
    {
        if(current < 0 || replacement < 0 || percent < 0 || percent > 100)
            throw new IllegalArgumentException("Invalid improvement inputs");

        long minimum = (current / 100) * percent + ((current % 100) * percent + 99) / 100;
        return replacement < current && current - replacement >= minimum;
    }



    //* === Comparable Implementation === *

    @Override
    public int compareTo(RouteMetric other)
    {
        int cost = Long.compare(costMicros, other.costMicros);
        return cost != 0 ? cost : Integer.compare(hopCount, other.hopCount);
    }
}
