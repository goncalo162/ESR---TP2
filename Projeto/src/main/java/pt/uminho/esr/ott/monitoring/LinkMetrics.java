package pt.uminho.esr.ott.monitoring;

import pt.uminho.esr.ott.overlay.Neighbor;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/** Immutable local measurement contract; never encoded on the control wire. */
public record LinkMetrics(Neighbor.Session session, State state, OptionalDouble ewmaMicros,
        OptionalLong costMicros, long sampleCount, boolean sampleCountSaturated,
        OptionalLong lastSampleAtNanos, int missedCount) {
    public enum State { UNMEASURED, MEASURED, SUSPECT }
    public LinkMetrics {
        Objects.requireNonNull(session); Objects.requireNonNull(state);
        Objects.requireNonNull(ewmaMicros); Objects.requireNonNull(costMicros);
        Objects.requireNonNull(lastSampleAtNanos);
        if (sampleCount < 0 || missedCount < 0 || sampleCountSaturated && sampleCount != Long.MAX_VALUE)
            throw new IllegalArgumentException("Invalid measurement counters");
        boolean measured = sampleCount > 0;
        if (measured != ewmaMicros.isPresent() || measured != costMicros.isPresent()
                || measured != lastSampleAtNanos.isPresent()
                || measured && (state == State.UNMEASURED || !Double.isFinite(ewmaMicros.orElseThrow())
                    || ewmaMicros.orElseThrow() < 1 || ewmaMicros.orElseThrow() >= 0x1p63
                    || costMicros.orElseThrow() != (long) Math.ceil(ewmaMicros.orElseThrow()))
                || !measured && state != State.UNMEASURED
                || measured && missedCount > 0 && state != State.SUSPECT
                || state == State.SUSPECT && missedCount == 0)
            throw new IllegalArgumentException("Inconsistent measurement snapshot");
    }
    public OptionalLong sampleAgeNanos(long now) {
        if (lastSampleAtNanos.isEmpty()) return OptionalLong.empty();
        long elapsed = now - lastSampleAtNanos.orElseThrow();
        if (elapsed < 0) throw new IllegalArgumentException("Monotonic clock horizon exceeded");
        return OptionalLong.of(elapsed);
    }
}
