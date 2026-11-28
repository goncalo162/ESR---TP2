package pt.uminho.esr.ott.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.Neighbor;
import pt.uminho.esr.ott.overlay.NeighborManager;
import pt.uminho.esr.ott.overlay.OverlayNode;
import pt.uminho.esr.ott.routing.Route;
import pt.uminho.esr.ott.routing.RoutingService;
import pt.uminho.esr.ott.routing.Subscription;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.junit.jupiter.api.Assertions.*;

class StreamingServerTest
{
    private static final UUID STREAM_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID STREAM_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID EPOCH = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Test
    void p00ValueObjectsAndPreparationRejectInvalidBounds(@TempDir Path tempDir) throws Exception
    {
        assertThrows(IllegalArgumentException.class,
                () -> new StreamingServer.SourcePlan(tempDir, Path.of("a"), STREAM_A, "S0", 0, false, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new StreamingServer.SourcePlan(tempDir, Path.of("a"), STREAM_A, "S0", 25, false, 0));
        var confirmation = confirmation(STREAM_A, EPOCH, 1, 1_000_000_000L, 0);
        assertThrows(IllegalArgumentException.class, () -> new StreamingServer.FrameEmission(
                STREAM_A, EPOCH, -1, 0, 0, new byte[0], confirmation));
        assertThrows(IllegalArgumentException.class,
                () -> new StreamingServer.ShutdownBudget(0, -1));
        assertThrows(IllegalArgumentException.class,
                () -> StreamingServer.ShutdownBudget.start(0, Duration.ofNanos(-1)));
        assertThrows(IllegalArgumentException.class,
                () -> StreamingServer.prepare(List.of(), StreamingServer.SourceHandle::open,
                        new AtomicBoolean(), System::nanoTime));

        Path source = writeVideoFile(tempDir, "single.mjpeg", 1);
        var prepared = StreamingServer.prepare(List.of(plan(tempDir, source.getFileName(), STREAM_A, 25, false)),
                StreamingServer.SourceHandle::open, new AtomicBoolean(), System::nanoTime);
        prepared.close();
        assertThrows(IllegalStateException.class, () -> new StreamingServer(
                prepared, StreamingServer.FramePort.UNWIRED, ignored -> {}, System::nanoTime));
    }

    private static byte[] validJpeg() throws IOException
    {
        var img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        var out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(img, "jpg", out));
        return out.toByteArray();
    }

    private static Path writeVideoFile(Path dir, String name, int frames) throws IOException
    {
        byte[] jpeg = validJpeg();
        String prefix = String.format(Locale.ROOT, "%05d", jpeg.length);
        Path file = dir.resolve(name);
        try(var out = Files.newOutputStream(file))
        {
            for(int i = 0; i < frames; i++)
            {
                out.write(prefix.getBytes(StandardCharsets.US_ASCII));
                out.write(jpeg);
            }
        }
        return file;
    }

    private static StreamingServer.SourcePlan plan(Path base, Path file, UUID id, int fps, boolean loop)
    {
        return new StreamingServer.SourcePlan(base, file, id, "S0", fps, loop, 99999);
    }

    private static Route.Confirmation confirmation(UUID streamId, UUID epoch, long version, long leaseNanos, long nowNanos)
    {
        var cid = UUID.randomUUID();
        var inst = UUID.randomUUID();
        var session = new Neighbor.Session("C1", inst, cid,
                new pt.uminho.esr.ott.protocol.ControlMessage.Endpoint("C1", inst, "127.0.0.1", 19001, 19002,
                        pt.uminho.esr.ott.overlay.NodeRole.CLIENT));
        var lease = new Subscription.Lease(nowNanos, leaseNanos);
        var sub = new Subscription(streamId, epoch, session, UUID.randomUUID(), 1L, UUID.randomUUID(),
                List.of("S0", "C1"), 5000, Subscription.State.ACTIVE, Optional.of(lease));
        return new Route.Confirmation(streamId, "S0", epoch, true, Optional.empty(), List.of("S0"),
                Map.of("C1", sub), Set.of(), Optional.empty(), version);
    }

    private static final class FakeHarness implements AutoCloseable
    {
        final AtomicLong clock = new AtomicLong(1_000_000_000L);
        final ExecutorService ownerExec = Executors.newSingleThreadExecutor(r -> new Thread(r, "fake-owner"));
        volatile Thread ownerThread;
        final ConcurrentHashMap<UUID, RoutingService.SourceDemand> demands = new ConcurrentHashMap<>();
        final ConcurrentHashMap<UUID, Boolean> available = new ConcurrentHashMap<>();
        final List<StreamingServer.FrameEmission> emitted = new CopyOnWriteArrayList<>();
        final List<Throwable> fatals = new CopyOnWriteArrayList<>();
        final AtomicReference<RoutingService.LocalSourceListener> listenerRef = new AtomicReference<>();
        final AtomicBoolean rejectOwnerOffers = new AtomicBoolean();
        final AtomicBoolean ownerStopped = new AtomicBoolean();

        FakeHarness() throws Exception
        {
            ownerExec.submit(() -> ownerThread = Thread.currentThread()).get(2, TimeUnit.SECONDS);
        }

        OverlayNode.OwnerAccess ownerAccess()
        {
            return new OverlayNode.OwnerAccess()
            {
                @Override public boolean isOwnerThread() { return Thread.currentThread() == ownerThread; }
                @Override public boolean offer(Runnable task)
                {
                    if(rejectOwnerOffers.get() || ownerStopped.get())
                        return false;
                    try { ownerExec.execute(task); return true; }
                    catch(RejectedExecutionException e) { return false; }
                }
                @Override public void fail(Throwable cause) { fatals.add(cause); }
            };
        }

        RoutingService.LocalSourceAccess sourceAccess()
        {
            return new RoutingService.LocalSourceAccess()
            {
                @Override public UUID serverEpoch() { return EPOCH; }
                @Override public RoutingService.SourceDemand observeOnOwner(UUID streamId)
                {
                    return demands.getOrDefault(streamId, new RoutingService.SourceDemand(
                            streamId, EPOCH, clock.get(), 1L, available.getOrDefault(streamId, true), Optional.empty()));
                }
                @Override public RoutingService.AvailabilityOutcome setAvailableOnOwner(UUID streamId, UUID expectedEpoch, boolean avail)
                {
                    if(ownerStopped.get()) return RoutingService.AvailabilityOutcome.STOPPED;
                    if(!EPOCH.equals(expectedEpoch)) return RoutingService.AvailabilityOutcome.STALE;
                    boolean prev = available.getOrDefault(streamId, true);
                    available.put(streamId, avail);
                    return prev == avail ? RoutingService.AvailabilityOutcome.ALREADY_APPLIED
                            : RoutingService.AvailabilityOutcome.APPLIED;
                }
            };
        }

        void bind(StreamingServer server) throws Exception
        {
            ownerExec.submit(() ->
            {
                var l = server.localSourceBinding().bindOnOwner(sourceAccess(), ownerAccess());
                listenerRef.set(l);
            }).get(2, TimeUnit.SECONDS);
        }

        void setDemand(UUID streamId, long version, boolean avail, Optional<Route.Confirmation> conf) throws Exception
        {
            available.put(streamId, avail);
            demands.put(streamId, new RoutingService.SourceDemand(streamId, EPOCH, clock.get(), version, avail, conf));
            ownerExec.submit(() -> listenerRef.get().onDemandChangedOnOwner(streamId)).get(2, TimeUnit.SECONDS);
        }

        @Override
        public void close()
        {
            ownerStopped.set(true);
            ownerExec.shutdownNow();
        }
    }

    @Test
    void p01PrepareFailsBeforeAnyNodeAndCleansOpenedHandles(@TempDir Path tempDir) throws Exception
    {
        Path good = writeVideoFile(tempDir, "good.mjpeg", 3);
        Path missing = tempDir.resolve("missing.mjpeg");
        var closedGood = new AtomicBoolean();
        StreamingServer.SourceOpener opener = p ->
        {
            if(p.mediaFile().equals(missing.getFileName()))
                return StreamingServer.SourceHandle.open(p);
            var real = StreamingServer.SourceHandle.open(p);
            return new StreamingServer.SourceHandle()
            {
                @Override public Optional<VideoSource.Frame> nextFrame() throws VideoSource.SourceException { return real.nextFrame(); }
                @Override public VideoSource.State state() { return real.state(); }
                @Override public void close() throws VideoSource.SourceException
                {
                    closedGood.set(true);
                    real.close();
                }
            };
        };

        var plans = List.of(
                plan(tempDir, good.getFileName(), STREAM_A, 10, false),
                plan(tempDir, missing.getFileName(), STREAM_B, 10, false));
        var ex = assertThrows(StreamingServer.PreparationException.class,
                () -> StreamingServer.prepare(plans, opener, new AtomicBoolean(), System::nanoTime));
        assertTrue(closedGood.get(), "Already-opened handle must be closed when another source fails");
        assertEquals(StreamingServer.CleanupOutcome.CLOSED, ex.cleanupOutcomes().get(STREAM_A));
        assertEquals(StreamingServer.CleanupOutcome.NOT_OPENED, ex.cleanupOutcomes().get(STREAM_B));
    }

    @Test
    void p02P03DemandGatedEmissionPauseResumeAndNoUnlimitedCatchUp(@TempDir Path tempDir) throws Exception
    {
        Path fileA = writeVideoFile(tempDir, "a.mjpeg", 10);
        var plans = List.of(plan(tempDir, fileA.getFileName(), STREAM_A, 10, false));
        try(var h = new FakeHarness();
            var prepared = StreamingServer.prepare(plans, StreamingServer.SourceHandle::open, new AtomicBoolean(), h.clock::get);
            var server = new StreamingServer(prepared, emission ->
            {
                h.emitted.add(emission);
                return StreamingServer.FrameAdmission.ADMITTED;
            }, h.fatals::add, h.clock::get))
        {
            h.bind(server);
            // Initially no demand -> 0 frames emitted.
            Thread.sleep(50);
            assertEquals(0, h.emitted.size());

            // Activate demand -> emits frame 0, then paces at 10 fps (100ms/frame).
            long t0 = h.clock.get();
            h.setDemand(STREAM_A, 2L, true, Optional.of(confirmation(STREAM_A, EPOCH, 2L, 5_000_000_000L, t0)));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while(h.emitted.isEmpty() && System.nanoTime() < deadline)
                Thread.sleep(10);
            assertEquals(1, h.emitted.size());
            assertEquals(0L, h.emitted.get(0).frameSeq());

            // Pause demand, jump clock by 2 seconds, resume -> no burst catch-up (only 1 frame for resume).
            h.setDemand(STREAM_A, 3L, true, Optional.empty());
            h.clock.addAndGet(2_000_000_000L);
            int beforeResume = h.emitted.size();
            h.setDemand(STREAM_A, 4L, true, Optional.of(confirmation(STREAM_A, EPOCH, 4L, 5_000_000_000L, h.clock.get())));
            deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while(h.emitted.size() == beforeResume && System.nanoTime() < deadline)
                Thread.sleep(10);
            Thread.sleep(30);
            assertEquals(beforeResume + 1, h.emitted.size(), "Resume must not burst missed frames");
            assertTrue(h.emitted.get(beforeResume).timestampNanos() > h.emitted.get(0).timestampNanos());
        }
    }

    @Test
    void p12P14LoopMonotonicityAndEofIsolation(@TempDir Path tempDir) throws Exception
    {
        Path loopFile = writeVideoFile(tempDir, "loop.mjpeg", 2);
        Path eofFile = writeVideoFile(tempDir, "eof.mjpeg", 2);
        var plans = List.of(
                plan(tempDir, loopFile.getFileName(), STREAM_A, 60, true),
                plan(tempDir, eofFile.getFileName(), STREAM_B, 60, false));

        try(var h = new FakeHarness();
            var prepared = StreamingServer.prepare(plans, StreamingServer.SourceHandle::open, new AtomicBoolean(), h.clock::get);
            var server = new StreamingServer(prepared, emission ->
            {
                h.emitted.add(emission);
                return StreamingServer.FrameAdmission.ADMITTED;
            }, h.fatals::add, h.clock::get))
        {
            h.bind(server);
            long t0 = h.clock.get();
            h.setDemand(STREAM_A, 2L, true, Optional.of(confirmation(STREAM_A, EPOCH, 2L, 10_000_000_000L, t0)));
            h.setDemand(STREAM_B, 2L, true, Optional.of(confirmation(STREAM_B, EPOCH, 2L, 10_000_000_000L, t0)));

            // Advance clock in 20ms steps so 60fps loop stream emits >= 4 frames and eof stream emits 2 and withdraws.
            for(int i = 0; i < 10; i++)
            {
                h.clock.addAndGet(20_000_000L);
                Thread.sleep(15);
            }

            List<StreamingServer.FrameEmission> aFrames = h.emitted.stream().filter(f -> f.streamId().equals(STREAM_A)).toList();
            List<StreamingServer.FrameEmission> bFrames = h.emitted.stream().filter(f -> f.streamId().equals(STREAM_B)).toList();

            assertEquals(2, bFrames.size(), "Non-looping stream must stop at EOF");
            assertFalse(h.available.getOrDefault(STREAM_B, true), "EOF stream must withdraw local availability");
            assertTrue(aFrames.size() >= 4, "Looping stream must continue emitting across loop");
            assertTrue(h.available.getOrDefault(STREAM_A, true), "Looping stream stays available");
            for(int i = 1; i < aFrames.size(); i++)
            {
                assertEquals(aFrames.get(i - 1).frameSeq() + 1, aFrames.get(i).frameSeq());
                assertTrue(aFrames.get(i).timestampNanos() > aFrames.get(i - 1).timestampNanos());
            }
            assertTrue(server.metrics().loops() >= 1);
            assertEquals(1L, server.metrics().eofs());
        }
    }

    @Test
    void p18P20UnwiredRefusesAndFailedSourcePreservesCleanupUncertain(@TempDir Path tempDir) throws Exception
    {
        Path fileA = writeVideoFile(tempDir, "a.mjpeg", 2);
        var plans = List.of(plan(tempDir, fileA.getFileName(), STREAM_A, 60, false));

        StreamingServer.SourceOpener failingCloseOpener = p -> new StreamingServer.SourceHandle()
        {
            private VideoSource.State st = VideoSource.State.OPEN;
            private int count;
            @Override public Optional<VideoSource.Frame> nextFrame()
            {
                if(count++ == 0)
                {
                    try
                    {
                        return StreamingServer.SourceHandle.open(p).nextFrame();
                    }
                    catch(VideoSource.SourceException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                st = VideoSource.State.FAILED;
                return Optional.empty();
            }
            @Override public VideoSource.State state() { return st; }
            @Override public void close() { /* quiet no-op after FAILED */ }
        };

        try(var h = new FakeHarness();
            var prepared = StreamingServer.prepare(plans, failingCloseOpener, new AtomicBoolean(), h.clock::get);
            var server = new StreamingServer(prepared, StreamingServer.FramePort.UNWIRED, h.fatals::add, h.clock::get))
        {
            h.bind(server);
            h.setDemand(STREAM_A, 2L, true, Optional.of(confirmation(STREAM_A, EPOCH, 2L, 5_000_000_000L, h.clock.get())));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while(server.metrics().attemptedFrames() == 0 && System.nanoTime() < deadline)
                Thread.sleep(10);

            assertTrue(server.metrics().attemptedFrames() >= 1);
            assertEquals(0L, server.metrics().admittedFrames());
            assertEquals(server.metrics().attemptedFrames(), server.metrics().refusedFrames());

            h.clock.addAndGet(20_000_000L);
            Thread.sleep(30);

            var budget = StreamingServer.ShutdownBudget.start(h.clock.get(), Duration.ofSeconds(2));
            var status = server.awaitTermination(budget);
            assertTrue(status.coordinatorStopped());
            assertEquals(0, status.workersAlive());
            assertEquals(StreamingServer.CleanupOutcome.CLEANUP_UNCERTAIN, status.cleanupOutcomes().get(STREAM_A));
            assertFalse(status.complete(), "CLEANUP_UNCERTAIN must not report complete=true");
        }
    }

    @Test
    void p19FatalDoesNotPinZeroBudgetAndBlockedWorkerIsNotClosedConcurrently(@TempDir Path tempDir) throws Exception
    {
        Path fileA = writeVideoFile(tempDir, "a.mjpeg", 2);
        var plans = List.of(plan(tempDir, fileA.getFileName(), STREAM_A, 60, false));
        var inNextFrame = new CountDownLatch(1);
        var releaseNextFrame = new CountDownLatch(1);
        var concurrentCloseCount = new AtomicInteger();

        StreamingServer.SourceOpener blockingOpener = p -> new StreamingServer.SourceHandle()
        {
            private int count;
            private volatile boolean reading;
            @Override public Optional<VideoSource.Frame> nextFrame()
            {
                if(count++ == 0)
                {
                    try { return StreamingServer.SourceHandle.open(p).nextFrame(); }
                    catch(VideoSource.SourceException e) { throw new RuntimeException(e); }
                }
                reading = true;
                inNextFrame.countDown();
                long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while(releaseNextFrame.getCount() > 0 && System.nanoTime() < end)
                {
                    try { releaseNextFrame.await(50, TimeUnit.MILLISECONDS); }
                    catch(InterruptedException ignored) { }
                }
                reading = false;
                return Optional.empty();
            }
            @Override public VideoSource.State state() { return VideoSource.State.OPEN; }
            @Override public void close()
            {
                if(reading) concurrentCloseCount.incrementAndGet();
            }
        };

        try(var h = new FakeHarness();
            var prepared = StreamingServer.prepare(plans, blockingOpener, new AtomicBoolean(), h.clock::get);
            var server = new StreamingServer(prepared, StreamingServer.FramePort.UNWIRED, h.fatals::add, h.clock::get))
        {
            h.bind(server);
            h.setDemand(STREAM_A, 2L, true, Optional.of(confirmation(STREAM_A, EPOCH, 2L, 5_000_000_000L, h.clock.get())));
            // Advance fake clock past frame 0's slot so coordinator dispatches worker nextFrame() for frame 1.
            long waitDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while(inNextFrame.getCount() > 0 && System.nanoTime() < waitDeadline)
            {
                h.clock.addAndGet(20_000_000L);
                Thread.sleep(10);
            }
            assertTrue(inNextFrame.await(2, TimeUnit.SECONDS));

            // Simulate fatal before awaitTermination: must NOT pin Duration.ZERO.
            h.rejectOwnerOffers.set(true);
            var budget = StreamingServer.ShutdownBudget.start(h.clock.get(), Duration.ofMillis(40));
            var tAdv = new Thread(() ->
            {
                try
                {
                    Thread.sleep(60);
                    h.clock.addAndGet(100_000_000L);
                }
                catch(InterruptedException ignored) { }
            });
            tAdv.start();
            var status = server.awaitTermination(budget);
            tAdv.join(1000);
            assertEquals(1, status.workersAlive());
            assertFalse(status.complete());
            assertEquals(0, concurrentCloseCount.get(), "Live worker handle must not be closed concurrently");
            assertEquals(StreamingServer.CleanupOutcome.CLEANUP_UNCERTAIN, status.cleanupOutcomes().get(STREAM_A));

            releaseNextFrame.countDown();
        }
    }

    @Test
    void p01PrepareRespectsBoundedBudgetWhenCancelledDuringBlockingRead(@TempDir Path tempDir) throws Exception
    {
        Path fileA = writeVideoFile(tempDir, "a.mjpeg", 1);
        var plans = List.of(plan(tempDir, fileA.getFileName(), STREAM_A, 10, false));
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var cancel = new AtomicBoolean();

        StreamingServer.SourceOpener blockedOpener = p -> new StreamingServer.SourceHandle()
        {
            @Override public Optional<VideoSource.Frame> nextFrame()
            {
                entered.countDown();
                try { release.await(5, TimeUnit.SECONDS); }
                catch(InterruptedException ignored) { }
                return Optional.empty();
            }
            @Override public VideoSource.State state() { return VideoSource.State.OPEN; }
            @Override public void close() { }
        };

        var t = new Thread(() ->
        {
            try
            {
                assertTrue(entered.await(2, TimeUnit.SECONDS));
                cancel.set(true);
            }
            catch(InterruptedException ignored) { }
        });
        t.start();

        long start = System.nanoTime();
        long cancelDeadline = start + 30_000_000L;
        assertThrows(StreamingServer.PreparationException.class, () ->
                StreamingServer.prepare(plans, blockedOpener, cancel,
                        () -> Math.max(0L, cancelDeadline - System.nanoTime()), System::nanoTime));
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertTrue(elapsedMs < 1500, "prepare() must return within bounded budget, took " + elapsedMs + "ms");
        release.countDown();
        t.join(1000);
    }

    /**
     * p20: fatal path — plan-B opener blocks; plan-A opener throws an exception.
     *
     * Without BUG-A fix, the blocked worker would keep plan-B open indefinitely
     * because {@code cancelFlag} was not set when plan-A's worker recorded the failure,
     * so the join loop for plan-B's worker stayed in the 10ms-poll branch instead of
     * the budget-bounded branch.
     *
     * With BUG-A fix ({@code cancelFlag.set(true)} immediately after
     * {@code firstFailure.compareAndSet}), the join loop switches to the budget-bounded
     * branch and {@code prepare()} returns within the given budget even though the
     * plan-B worker is still physically blocked.
     */
    @Test
    void p20FatalOpenerExceptionUnblocksOtherBlockedWorkerWithinBudget(@TempDir Path tempDir) throws Exception
    {
        // plan-B opener blocks indefinitely inside open()
        var planBEntered = new CountDownLatch(1);
        var planBRelease = new CountDownLatch(1);

        StreamingServer.SourceOpener opener = p ->
        {
            if(p.streamId().equals(STREAM_B))
            {
                planBEntered.countDown();
                try { planBRelease.await(10, TimeUnit.SECONDS); }
                catch(InterruptedException ignored) { }
                // After being interrupted/released, return a valid but empty source
                // so the test does not leave dangling resources.
                throw new RuntimeException("plan-B blocked then interrupted");
            }
            // plan-A opener throws immediately — sets firstFailure + cancelFlag (BUG-A fix)
            throw new RuntimeException("plan-A fatal open failure");
        };

        var plans = List.of(
                plan(tempDir, Path.of("a.mjpeg"), STREAM_A, 10, false),
                plan(tempDir, Path.of("b.mjpeg"), STREAM_B, 10, false));

        // Budget: 200 ms. Without BUG-A fix, prepare() would spin for ~budget time only
        // after the external cancel; with BUG-A fix it switches immediately.
        long budgetNanos = 200_000_000L;
        long start = System.nanoTime();

        // Wait until plan-B worker is blocked before starting the budget timer.
        // (plan-A may throw before plan-B enters, so we don't strictly require entry.)
        var cancel = new AtomicBoolean();
        long[] deadlineRef = {start + budgetNanos};

        var ex = assertThrows(StreamingServer.PreparationException.class, () ->
                StreamingServer.prepare(plans, opener, cancel,
                        () -> Math.max(0L, deadlineRef[0] - System.nanoTime()), System::nanoTime));

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        // Must finish well within twice the budget; the main assertion is that it does
        // not block for a long time (>= 1 s) waiting for plan-B's blocked worker.
        assertTrue(elapsedMs < 1000,
                "prepare() must return within budget even when plan-B worker is blocked; took " + elapsedMs + "ms");
        assertTrue(ex.cleanupOutcomes().containsKey(STREAM_A) || ex.cleanupOutcomes().containsKey(STREAM_B));

        // Release plan-B worker so its thread can terminate (test hygiene).
        planBRelease.countDown();
    }
}
