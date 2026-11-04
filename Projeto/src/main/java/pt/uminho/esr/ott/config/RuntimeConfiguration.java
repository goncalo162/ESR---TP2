package pt.uminho.esr.ott.config;

import java.time.Duration;
import java.util.Objects;

/** Opções operacionais imutáveis e validadas, sem rede ou estado de execução. */
public record RuntimeConfiguration(
        Control control, Overlay overlay, Monitoring monitoring, Routing routing,
        Subscription subscription, Video video, Source source) {

    private static final Duration MIN_DURATION = Duration.ofMillis(1);
    private static final Duration MAX_DURATION = Duration.ofMillis(Integer.MAX_VALUE);
    private static final RuntimeConfiguration DEFAULTS = new RuntimeConfiguration(
                new Control(Duration.ofMillis(2000), Duration.ofMillis(3000), 64, 256, Duration.ofMillis(3000)),
                new Overlay(Duration.ofMillis(3000), Duration.ofMillis(500), Duration.ofMillis(5000), 10, 4096),
                new Monitoring(Duration.ofMillis(1000), Duration.ofMillis(1000), 3, .2),
                new Routing(Duration.ofMillis(2000), Duration.ofMillis(8000), 4096, Duration.ofMillis(16000), 10,
                        Duration.ofMillis(3000), 3, 20, 3, Duration.ofMillis(5000)),
                new Subscription(Duration.ofMillis(6000), Duration.ofMillis(2000)),
                new Video(256, 8, Duration.ofMillis(250)), new Source(99999));

    public RuntimeConfiguration {
        Objects.requireNonNull(control, "control");
        Objects.requireNonNull(overlay, "overlay");
        Objects.requireNonNull(monitoring, "monitoring");
        Objects.requireNonNull(routing, "routing");
        Objects.requireNonNull(subscription, "subscription");
        Objects.requireNonNull(video, "video");
        Objects.requireNonNull(source, "source");
    }

    public static RuntimeConfiguration defaults() { return DEFAULTS; }

    public record Control(Duration connectTimeout, Duration shutdownTimeout, int maxConnections,
                          int queueCapacity, Duration bootstrapQueryTimeout) {
        public Control {
            duration(connectTimeout, "connectTimeout");
            duration(shutdownTimeout, "shutdownTimeout");
            range(maxConnections, 1, 64, "maxConnections");
            range(queueCapacity, 1, 256, "queueCapacity");
            duration(bootstrapQueryTimeout, "bootstrapQueryTimeout");
        }
    }
    public record Overlay(Duration helloTimeout, Duration retryInitial, Duration retryMax,
                          int retryJitterPercent, int eventsCapacity) {
        public Overlay {
            duration(helloTimeout, "helloTimeout");
            duration(retryInitial, "retryInitial");
            duration(retryMax, "retryMax");
            range(retryJitterPercent, 0, 100, "retryJitterPercent");
            range(eventsCapacity, 1, 4096, "eventsCapacity");
            if (retryInitial.compareTo(retryMax) > 0)
                throw new IllegalArgumentException("retryInitial must not exceed retryMax");
        }
    }
    public record Monitoring(Duration probeInterval, Duration probeTimeout, int probeFailures,
                             double ewmaAlpha) {
        public Monitoring {
            duration(probeInterval, "probeInterval");
            duration(probeTimeout, "probeTimeout");
            range(probeFailures, 1, Integer.MAX_VALUE, "probeFailures");
            if (!Double.isFinite(ewmaAlpha) || ewmaAlpha <= 0 || ewmaAlpha > 1)
                throw new IllegalArgumentException("ewmaAlpha must be finite and in (0,1]");
        }
    }
    public record Routing(Duration announceInterval, Duration candidateTtl, int cacheCapacity,
                          Duration cacheTtl, int floodMaxPerSecond, Duration activateTimeout,
                          int activateAttempts, int switchImprovementPercent, int switchObservations,
                          Duration switchMinDwell) {
        public Routing {
            duration(announceInterval, "announceInterval");
            duration(candidateTtl, "candidateTtl");
            range(cacheCapacity, 1, 4096, "cacheCapacity");
            duration(cacheTtl, "cacheTtl");
            range(floodMaxPerSecond, 1, 10, "floodMaxPerSecond");
            duration(activateTimeout, "activateTimeout");
            range(activateAttempts, 1, Integer.MAX_VALUE, "activateAttempts");
            range(switchImprovementPercent, 0, 100, "switchImprovementPercent");
            range(switchObservations, 1, Integer.MAX_VALUE, "switchObservations");
            duration(switchMinDwell, "switchMinDwell");
            if (candidateTtl.compareTo(announceInterval) <= 0)
                throw new IllegalArgumentException("candidateTtl must exceed announceInterval");
            if (cacheTtl.compareTo(candidateTtl) < 0)
                throw new IllegalArgumentException("cacheTtl must not be less than candidateTtl");
        }
    }
    public record Subscription(Duration lease, Duration renew) {
        public Subscription {
            duration(lease, "lease");
            duration(renew, "renew");
            if (renew.compareTo(lease) >= 0)
                throw new IllegalArgumentException("renew must be less than lease");
        }
    }
    public record Video(int egressCapacity, int reassemblyMaxFrames, Duration reassemblyDeadline) {
        public Video {
            range(egressCapacity, 1, 256, "egressCapacity");
            range(reassemblyMaxFrames, 1, 8, "reassemblyMaxFrames");
            duration(reassemblyDeadline, "reassemblyDeadline");
        }
    }
    public record Source(int maxFrameBytes) {
        public Source { range(maxFrameBytes, 1, 99999, "maxFrameBytes"); }
    }

    private static void range(int value, int min, int max, String name) {
        if (value < min || value > max)
            throw new IllegalArgumentException(name + " must be in " + min + ".." + max);
    }

    private static void duration(Duration value, String name) {
        Objects.requireNonNull(value, name);
        // Compare before converting: an arbitrary Duration can overflow toMillis/toNanos.
        if (value.compareTo(MIN_DURATION) < 0 || value.compareTo(MAX_DURATION) > 0
                || value.getNano() % 1_000_000 != 0)
            throw new IllegalArgumentException(name + " must fit positive integer milliseconds");
    }
}
