package pt.uminho.esr.ott.server;

import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.config.StreamConfiguration;
import pt.uminho.esr.ott.overlay.OverlayNode;
import pt.uminho.esr.ott.routing.Route;
import pt.uminho.esr.ott.routing.RoutingService;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

/**
 * Bounded multi-stream local video producer with exact FPS pacing,
 * demand-gated emission, loop/EOF isolation, and global-budget shutdown.
 */
public final class StreamingServer implements AutoCloseable
{
    public static final int DEFAULT_WORKERS = 2;

    //* === Contracts & Value Objects === *

    public record SourcePlan(Path mediaBase, Path mediaFile, UUID streamId, String serverId,
            int fps, boolean loop, int maxFrameBytes)
    {
        public SourcePlan
        {
            Objects.requireNonNull(mediaBase, "mediaBase");
            Objects.requireNonNull(mediaFile, "mediaFile");
            Objects.requireNonNull(streamId, "streamId");
            Objects.requireNonNull(serverId, "serverId");
            if(fps < 1 || fps > 60)
                throw new IllegalArgumentException("fps must be in 1..60");
            if(maxFrameBytes < 1 || maxFrameBytes > 99999)
                throw new IllegalArgumentException("maxFrameBytes must be in 1..99999");
        }

        public static List<SourcePlan> fromConfiguration(NodeConfiguration config)
        {
            Objects.requireNonNull(config, "config");
            Path mediaBase = config.mediaBase().orElseThrow(() -> new IllegalStateException("mediaBase required"));
            int maxBytes = config.runtime().source().maxFrameBytes();
            var plans = new ArrayList<SourcePlan>(config.streams().size());
            for(StreamConfiguration s : config.streams())
            {
                plans.add(new SourcePlan(mediaBase, s.mediaFile(), s.streamId(),
                        s.serverId(), s.fps(), s.loop(), maxBytes));
            }
            return List.copyOf(plans);
        }
    }

    public interface SourceHandle extends AutoCloseable
    {
        Optional<VideoSource.Frame> nextFrame() throws VideoSource.SourceException;
        VideoSource.State state();
        @Override
        void close() throws VideoSource.SourceException;

        static SourceHandle open(SourcePlan plan) throws VideoSource.SourceException
        {
            Objects.requireNonNull(plan, "plan");
            VideoSource source = VideoSource.open(plan.mediaBase(), plan.mediaFile(), plan.maxFrameBytes());
            return new SourceHandle()
            {
                @Override
                public Optional<VideoSource.Frame> nextFrame() throws VideoSource.SourceException
                {
                    return source.nextFrame();
                }

                @Override
                public VideoSource.State state()
                {
                    return source.state();
                }

                @Override
                public void close() throws VideoSource.SourceException
                {
                    source.close();
                }
            };
        }
    }

    @FunctionalInterface
    public interface SourceOpener
    {
        SourceHandle open(SourcePlan plan) throws VideoSource.SourceException;
    }

    public enum FrameAdmission
    {
        ADMITTED, REFUSED, UNAVAILABLE
    }

    public record FrameEmission(UUID streamId, UUID serverEpoch, long frameSeq,
            long timestampNanos, int length, byte[] jpegBytes, Route.Confirmation confirmation)
    {
        public FrameEmission
        {
            Objects.requireNonNull(streamId, "streamId");
            Objects.requireNonNull(serverEpoch, "serverEpoch");
            Objects.requireNonNull(jpegBytes, "jpegBytes");
            Objects.requireNonNull(confirmation, "confirmation");
            if(frameSeq < 0 || timestampNanos < 0 || length != jpegBytes.length)
                throw new IllegalArgumentException("Invalid frame emission bounds");
        }
    }

    @FunctionalInterface
    public interface FramePort
    {
        FrameAdmission admit(FrameEmission emission);

        FramePort UNWIRED = emission -> FrameAdmission.UNAVAILABLE;
    }

    @FunctionalInterface
    public interface FatalPort
    {
        void onFatal(Throwable cause);
    }

    public enum CleanupOutcome
    {
        NOT_OPENED, CLOSED, CLEANUP_UNCERTAIN, CLOSE_FAILED
    }

    public record ShutdownBudget(long startNanos, long budgetNanos)
    {
        public ShutdownBudget
        {
            if(budgetNanos < 0)
                throw new IllegalArgumentException("Negative shutdown budget");
        }

        public static ShutdownBudget start(long nowNanos, Duration budget)
        {
            Objects.requireNonNull(budget, "budget");
            if(budget.isNegative())
                throw new IllegalArgumentException("Negative shutdown budget");
            return new ShutdownBudget(nowNanos, budget.toNanos());
        }

        public long remainingNanos(long nowNanos)
        {
            long elapsed = nowNanos - startNanos;
            if(elapsed <= 0)
                return budgetNanos;
            return Math.max(0L, budgetNanos - elapsed);
        }
    }

    public record TerminationStatus(boolean complete, boolean coordinatorStopped,
            int workersAlive, Map<UUID, CleanupOutcome> cleanupOutcomes)
    {
        public TerminationStatus
        {
            cleanupOutcomes = Map.copyOf(cleanupOutcomes);
        }
    }

    public record Metrics(long attemptedFrames, long admittedFrames, long refusedFrames,
            long readFrames, long loops, long eofs, long sourceErrors)
    {
    }

    public static final class PreparationException extends IOException
    {
        private static final long serialVersionUID = 1L;
        private final Map<UUID, CleanupOutcome> cleanupOutcomes;

        public PreparationException(String message, Throwable cause, Map<UUID, CleanupOutcome> cleanupOutcomes)
        {
            super(message, cause);
            this.cleanupOutcomes = Map.copyOf(cleanupOutcomes);
        }

        public Map<UUID, CleanupOutcome> cleanupOutcomes()
        {
            return cleanupOutcomes;
        }
    }

    public static final class PreparedSources implements AutoCloseable
    {
        private final List<PreparedStream> streams;
        private final SourceOpener opener;
        private final AtomicBoolean consumed = new AtomicBoolean();

        private PreparedSources(List<PreparedStream> streams, SourceOpener opener)
        {
            this.streams = List.copyOf(streams);
            this.opener = opener;
        }

        public int size()
        {
            return streams.size();
        }

        @Override
        public void close()
        {
            if(!consumed.compareAndSet(false, true))
                return;
            for(PreparedStream ps : streams)
            {
                closeQuietly(ps.handle);
            }
        }

        private List<PreparedStream> consume()
        {
            if(!consumed.compareAndSet(false, true))
                throw new IllegalStateException("PreparedSources already consumed or closed");
            return streams;
        }
    }

    private record PreparedStream(SourcePlan plan, SourceHandle handle, VideoSource.Frame firstFrame)
    {
    }

    //* === Qualification / Preparation === *

    public static PreparedSources prepare(List<SourcePlan> plans, SourceOpener opener,
            AtomicBoolean cancelFlag, LongSupplier clock) throws PreparationException
    {
        return prepare(plans, opener, cancelFlag, () -> Long.MAX_VALUE, clock);
    }

    public static PreparedSources prepare(List<SourcePlan> plans, SourceOpener opener,
            AtomicBoolean cancelFlag, LongSupplier remainingNanos, LongSupplier clock) throws PreparationException
    {
        Objects.requireNonNull(plans, "plans");
        Objects.requireNonNull(opener, "opener");
        Objects.requireNonNull(cancelFlag, "cancelFlag");
        Objects.requireNonNull(remainingNanos, "remainingNanos");
        Objects.requireNonNull(clock, "clock");
        if(plans.isEmpty())
            throw new IllegalArgumentException("At least one source plan is required");

        var opened = new ConcurrentHashMap<UUID, PreparedStream>();
        var cleanupOutcomes = new ConcurrentHashMap<UUID, CleanupOutcome>();
        for(SourcePlan p : plans)
            cleanupOutcomes.put(p.streamId(), CleanupOutcome.NOT_OPENED);

        var firstFailure = new AtomicReference<Throwable>();
        var nextIndex = new AtomicInteger();
        int workerCount = Math.min(DEFAULT_WORKERS, plans.size());
        var workers = new ArrayList<Thread>(workerCount);

        for(int w = 0; w < workerCount; w++)
        {
            Thread t = new Thread(() ->
            {
                while(!cancelFlag.get() && firstFailure.get() == null)
                {
                    int idx = nextIndex.getAndIncrement();
                    if(idx >= plans.size())
                        return;
                    SourcePlan plan = plans.get(idx);
                    SourceHandle handle = null;
                    try
                    {
                        handle = opener.open(plan);
                        if(cancelFlag.get())
                        {
                            cleanupOutcomes.put(plan.streamId(), closeHandle(handle));
                            return;
                        }
                        Optional<VideoSource.Frame> frame = handle.nextFrame();
                        if(frame.isEmpty())
                            throw new IllegalStateException("Empty source stream: " + plan.streamId());
                        if(cancelFlag.get())
                        {
                            cleanupOutcomes.put(plan.streamId(), closeHandle(handle));
                            return;
                        }
                        opened.put(plan.streamId(), new PreparedStream(plan, handle, frame.orElseThrow()));
                    }
                    catch(Throwable tFailure)
                    {
                        firstFailure.compareAndSet(null, tFailure);
                        cancelFlag.set(true); // BUG-A fix: signal other blocked workers immediately
                        if(handle != null)
                            cleanupOutcomes.put(plan.streamId(), closeHandle(handle));
                        else
                            cleanupOutcomes.put(plan.streamId(), CleanupOutcome.NOT_OPENED);
                        return;
                    }
                }
            }, "ott-prepare-worker-" + w);
            t.setDaemon(true);
            t.start();
            workers.add(t);
        }

        for(Thread t : workers)
        {
            while(t.isAlive())
            {
                if(cancelFlag.get())
                {
                    long remMs = remainingNanos.getAsLong() / 1_000_000L;
                    if(remMs <= 0L)
                    {
                        t.interrupt();
                        break;
                    }
                    try
                    {
                        t.join(remMs);
                    }
                    catch(InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                        cancelFlag.set(true);
                        firstFailure.compareAndSet(null, e);
                    }
                    if(t.isAlive() && remainingNanos.getAsLong() / 1_000_000L <= 0L)
                    {
                        t.interrupt();
                        break;
                    }
                }
                else
                {
                    try
                    {
                        t.join(10L);
                    }
                    catch(InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                        cancelFlag.set(true);
                        firstFailure.compareAndSet(null, e);
                    }
                }
            }
        }

        boolean anyAlive = workers.stream().anyMatch(Thread::isAlive);
        Throwable error = firstFailure.get();
        if(anyAlive || error != null || cancelFlag.get() || opened.size() != plans.size())
        {
            for(PreparedStream ps : opened.values())
                cleanupOutcomes.put(ps.plan().streamId(), closeHandle(ps.handle()));
            for(SourcePlan p : plans)
            {
                if(cleanupOutcomes.get(p.streamId()) == CleanupOutcome.NOT_OPENED && anyAlive)
                    cleanupOutcomes.put(p.streamId(), CleanupOutcome.CLEANUP_UNCERTAIN);
            }
            Throwable cause = error != null ? error : new CancellationException("Preparation cancelled or timed out");
            if(cause instanceof Error err)
                throw err;
            throw new PreparationException("Source preparation failed", cause, cleanupOutcomes);
        }

        var ordered = new ArrayList<PreparedStream>(plans.size());
        for(SourcePlan p : plans)
            ordered.add(opened.get(p.streamId()));
        return new PreparedSources(ordered, opener);
    }

    //* === Runtime State === *

    private final Map<UUID, StreamCell> cells = new LinkedHashMap<>();
    private final List<StreamCell> orderedCells;
    private final SourceOpener opener;
    private final FramePort framePort;
    private final FatalPort fatalPort;
    private final LongSupplier clock;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition wakeCondition = lock.newCondition();

    private final AtomicBoolean stopping = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicBoolean fatalSignaled = new AtomicBoolean();
    private final AtomicReference<Throwable> primaryFailure = new AtomicReference<>();
    private final AtomicReference<ShutdownBudget> shutdownBudget = new AtomicReference<>();

    private volatile RoutingService.LocalSourceAccess localSourceAccess;
    private volatile OverlayNode.OwnerAccess ownerAccess;
    private volatile UUID bindingId;
    private volatile UUID serverEpoch;

    private final Thread coordinatorThread;
    private final Thread[] workerThreads;
    private final ArrayDeque<ReadJob> workerQueue = new ArrayDeque<>();
    private int rrCursor;

    private final AtomicLong attemptedFrames = new AtomicLong();
    private final AtomicLong admittedFrames = new AtomicLong();
    private final AtomicLong refusedFrames = new AtomicLong();
    private final AtomicLong readFrames = new AtomicLong();
    private final AtomicLong loops = new AtomicLong();
    private final AtomicLong eofs = new AtomicLong();
    private final AtomicLong sourceErrors = new AtomicLong();

    private record ReadPermit(UUID streamId, UUID bindingId, UUID epoch, long demandVersion,
            long expiresAtNanos, AtomicBoolean revoked)
    {
        boolean isValid(long nowNanos)
        {
            return !revoked.get() && (expiresAtNanos == Long.MAX_VALUE || nowNanos < expiresAtNanos);
        }
    }

    private record ReadJob(StreamCell cell, ReadPermit permit, boolean loopReopen, long opToken)
    {
    }

    private static final class StreamCell
    {
        final SourcePlan plan;
        SourceHandle handle;
        VideoSource.Frame bufferedFrame;
        long frameSeq;
        long nextEmitNanos = Long.MAX_VALUE;
        long anchorNanos;
        long anchorTick;
        boolean paused = true;
        boolean eof;
        boolean failed;
        boolean localAvailable = true;
        boolean inFlight;
        long opToken;
        RoutingService.SourceDemand lastDemand;
        ReadPermit activePermit;
        CleanupOutcome cleanupOutcome = CleanupOutcome.NOT_OPENED;

        StreamCell(PreparedStream ps)
        {
            this.plan = ps.plan();
            this.handle = ps.handle();
            this.bufferedFrame = ps.firstFrame();
        }
    }

    public StreamingServer(PreparedSources prepared, FramePort framePort, FatalPort fatalPort, LongSupplier clock)
    {
        Objects.requireNonNull(prepared, "prepared");
        this.framePort = Objects.requireNonNull(framePort, "framePort");
        this.fatalPort = Objects.requireNonNull(fatalPort, "fatalPort");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.opener = prepared.opener;

        List<PreparedStream> streams = prepared.consume();
        var list = new ArrayList<StreamCell>(streams.size());
        for(PreparedStream ps : streams)
        {
            var cell = new StreamCell(ps);
            cells.put(ps.plan().streamId(), cell);
            list.add(cell);
        }
        this.orderedCells = List.copyOf(list);

        this.coordinatorThread = new Thread(this::coordinatorLoop, "ott-streaming-coordinator");
        this.coordinatorThread.setDaemon(true);
        int workerCount = Math.min(DEFAULT_WORKERS, orderedCells.size());
        this.workerThreads = new Thread[workerCount];
        for(int i = 0; i < workerCount; i++)
        {
            this.workerThreads[i] = new Thread(this::workerLoop, "ott-streaming-worker-" + i);
            this.workerThreads[i].setDaemon(true);
        }
    }

    public OverlayNode.LocalSourceBinding localSourceBinding()
    {
        return (access, owner) ->
        {
            this.localSourceAccess = Objects.requireNonNull(access, "access");
            this.ownerAccess = Objects.requireNonNull(owner, "owner");
            this.bindingId = UUID.randomUUID();
            this.serverEpoch = access.serverEpoch();

            startThreads();

            // Observe initial demand for all configured streams on the owner thread.
            for(StreamCell cell : orderedCells)
            {
                try
                {
                    RoutingService.SourceDemand demand = access.observeOnOwner(cell.plan.streamId());
                    onDemandSnapshotOnOwner(cell, demand);
                }
                catch(RuntimeException | Error e)
                {
                    recordFatal(e);
                }
            }

            return new RoutingService.LocalSourceListener()
            {
                @Override
                public void onDemandChangedOnOwner(UUID streamId)
                {
                    StreamCell cell = cells.get(streamId);
                    if(cell == null)
                        return;
                    try
                    {
                        RoutingService.SourceDemand demand = access.observeOnOwner(streamId);
                        onDemandSnapshotOnOwner(cell, demand);
                    }
                    catch(RuntimeException | Error e)
                    {
                        recordFatal(e);
                    }
                }

                @Override
                public void onStoppedOnOwner()
                {
                    initiateStop(null);
                }
            };
        };
    }

    private synchronized void startThreads()
    {
        if(stopping.get() || closed.get())
            return;
        if(coordinatorThread.getState() == Thread.State.NEW)
            coordinatorThread.start();
        for(Thread w : workerThreads)
        {
            if(w.getState() == Thread.State.NEW)
                w.start();
        }
    }

    private void onDemandSnapshotOnOwner(StreamCell cell, RoutingService.SourceDemand demand)
    {
        long now = clock.getAsLong();
        lock.lock();
        try
        {
            cell.lastDemand = demand;
            cell.localAvailable = demand.localAvailable();

            boolean hasDemand = !stopping.get() && !cell.eof && !cell.failed
                    && demand.localAvailable() && demand.confirmation().isPresent();

            if(!hasDemand)
            {
                if(cell.activePermit != null)
                    cell.activePermit.revoked().set(true);
                cell.activePermit = null;
                cell.paused = true;
                cell.nextEmitNanos = Long.MAX_VALUE;
            }
            else
            {
                long horizon = computePermitHorizonNanos(demand.confirmation().orElseThrow(), now);
                if(horizon <= 0L)
                {
                    if(cell.activePermit != null)
                        cell.activePermit.revoked().set(true);
                    cell.activePermit = null;
                    cell.paused = true;
                    cell.nextEmitNanos = Long.MAX_VALUE;
                }
                else
                {
                    long expiresAt = horizon == Long.MAX_VALUE ? Long.MAX_VALUE : now + horizon;
                    if(cell.activePermit != null)
                        cell.activePermit.revoked().set(true);
                    cell.activePermit = new ReadPermit(cell.plan.streamId(), bindingId, serverEpoch,
                            demand.version(), expiresAt, new AtomicBoolean());
                    if(cell.paused)
                    {
                        cell.paused = false;
                        cell.anchorNanos = now;
                        cell.anchorTick = 0L;
                        cell.nextEmitNanos = now;
                    }
                }
            }
            wakeCondition.signalAll();
        }
        finally
        {
            lock.unlock();
        }
    }

    private static long computePermitHorizonNanos(Route.Confirmation confirmation, long nowNanos)
    {
        if(!confirmation.localConsumers().isEmpty())
            return Long.MAX_VALUE;
        long maxRemaining = 0L;
        for(var sub : confirmation.downstreams().values())
        {
            if(sub.validity().isPresent())
            {
                long rem = sub.validity().orElseThrow().remainingNanos(nowNanos);
                if(rem > maxRemaining)
                    maxRemaining = rem;
            }
        }
        return maxRemaining;
    }

    //* === Coordinator & Worker Loops === *

    private void coordinatorLoop()
    {
        try
        {
            while(true)
            {
                long nextWake = Long.MAX_VALUE;
                lock.lock();
                try
                {
                    if(stopping.get())
                        return;

                    long now = clock.getAsLong();
                    int n = orderedCells.size();
                    for(int i = 0; i < n; i++)
                    {
                        int idx = (rrCursor + i) % n;
                        StreamCell cell = orderedCells.get(idx);

                        if(cell.eof || cell.failed || cell.paused || cell.activePermit == null)
                            continue;
                        if(!cell.activePermit.isValid(now))
                        {
                            cell.paused = true;
                            cell.nextEmitNanos = Long.MAX_VALUE;
                            continue;
                        }

                        if(now >= cell.nextEmitNanos)
                        {
                            if(cell.bufferedFrame != null)
                            {
                                VideoSource.Frame frame = cell.bufferedFrame;
                                cell.bufferedFrame = null;
                                advanceSchedule(cell, now);
                                rrCursor = (idx + 1) % n;
                                offerEmitOnOwner(cell, frame, cell.activePermit);
                            }
                            else if(!cell.inFlight)
                            {
                                cell.inFlight = true;
                                long token = ++cell.opToken;
                                advanceSchedule(cell, now);
                                rrCursor = (idx + 1) % n;
                                workerQueue.addLast(new ReadJob(cell, cell.activePermit, false, token));
                                wakeCondition.signalAll();
                            }
                        }

                        if(cell.nextEmitNanos < nextWake)
                            nextWake = cell.nextEmitNanos;
                    }

                    if(stopping.get())
                        return;

                    if(nextWake == Long.MAX_VALUE)
                    {
                        wakeCondition.await();
                    }
                    else
                    {
                        long delay = nextWake - clock.getAsLong();
                        if(delay > 0)
                            wakeCondition.awaitNanos(delay);
                    }
                }
                finally
                {
                    lock.unlock();
                }
            }
        }
        catch(InterruptedException e)
        {
            if(!stopping.get())
                Thread.currentThread().interrupt();
        }
        catch(Throwable t)
        {
            recordFatal(t);
        }
    }

    private static long scaleNanos(long count, int fps)
    {
        return Math.multiplyExact(count, 1_000_000_000L) / fps;
    }

    private static void advanceSchedule(StreamCell cell, long nowNanos)
    {
        int fps = cell.plan.fps();
        long elapsed = Math.max(0L, nowNanos - cell.anchorNanos);
        long wholeSeconds = elapsed / 1_000_000_000L;
        long remNanos = elapsed % 1_000_000_000L;
        long currentTick = Math.addExact(Math.multiplyExact(wholeSeconds, fps), (remNanos * fps) / 1_000_000_000L);
        long nextTick = Math.max(Math.addExact(cell.anchorTick, 1L), Math.addExact(currentTick, 1L));
        cell.anchorTick = nextTick;
        cell.nextEmitNanos = Math.addExact(cell.anchorNanos, scaleNanos(nextTick, fps));
    }

    private void workerLoop()
    {
        try
        {
            while(true)
            {
                ReadJob job;
                lock.lock();
                try
                {
                    while(workerQueue.isEmpty() && !stopping.get())
                        wakeCondition.await();
                    if(stopping.get())
                        return;
                    job = workerQueue.removeFirst();
                }
                finally
                {
                    lock.unlock();
                }

                executeReadJob(job);
            }
        }
        catch(InterruptedException e)
        {
            if(!stopping.get())
                Thread.currentThread().interrupt();
        }
        catch(Throwable t)
        {
            recordFatal(t);
        }
    }

    private void executeReadJob(ReadJob job)
    {
        StreamCell cell = job.cell();
        ReadPermit permit = job.permit();
        long now = clock.getAsLong();
        if(stopping.get() || !permit.isValid(now))
        {
            lock.lock();
            try
            {
                if(cell.opToken == job.opToken())
                    cell.inFlight = false;
                wakeCondition.signalAll();
            }
            finally
            {
                lock.unlock();
            }
            return;
        }

        VideoSource.Frame readFrame = null;
        boolean isEof = false;
        Throwable failure = null;

        try
        {
            Optional<VideoSource.Frame> opt = cell.handle.nextFrame();
            if(opt.isPresent())
            {
                readFrame = opt.get();
                readFrames.incrementAndGet();
            }
            else if(cell.plan.loop() && cell.frameSeq > 0)
            {
                cell.cleanupOutcome = closeHandle(cell.handle);
                cell.handle = opener.open(cell.plan);
                Optional<VideoSource.Frame> first = cell.handle.nextFrame();
                if(first.isPresent())
                {
                    readFrame = first.get();
                    readFrames.incrementAndGet();
                    loops.incrementAndGet();
                }
                else
                {
                    isEof = true;
                }
            }
            else
            {
                isEof = true;
            }
        }
        catch(Throwable t)
        {
            failure = t;
        }

        if(isEof || failure != null)
        {
            CleanupOutcome outcome = closeHandle(cell.handle);
            lock.lock();
            try
            {
                cell.cleanupOutcome = outcome;
                if(cell.opToken == job.opToken())
                    cell.inFlight = false;
                if(isEof)
                {
                    cell.eof = true;
                    eofs.incrementAndGet();
                }
                else
                {
                    cell.failed = true;
                    sourceErrors.incrementAndGet();
                }
                if(cell.activePermit != null)
                    cell.activePermit.revoked().set(true);
                cell.activePermit = null;
                cell.paused = true;
                wakeCondition.signalAll();
            }
            finally
            {
                lock.unlock();
            }

            if(failure instanceof Error err)
                recordFatal(err);
            else
                withdrawStreamOnOwner(cell.plan.streamId(), permit.epoch());
            return;
        }

        lock.lock();
        try
        {
            if(cell.opToken == job.opToken())
                cell.inFlight = false;
            if(stopping.get() || !permit.isValid(clock.getAsLong()))
            {
                if(!stopping.get())
                    cell.bufferedFrame = readFrame;
                wakeCondition.signalAll();
                return;
            }
        }
        finally
        {
            lock.unlock();
        }

        offerEmitOnOwner(cell, readFrame, permit);
    }

    private void offerEmitOnOwner(StreamCell cell, VideoSource.Frame frame, ReadPermit permit)
    {
        OverlayNode.OwnerAccess owner = this.ownerAccess;
        RoutingService.LocalSourceAccess access = this.localSourceAccess;
        if(owner == null || access == null || stopping.get())
            return;

        boolean offered = owner.offer(() ->
        {
            if(stopping.get())
                return;
            try
            {
                RoutingService.SourceDemand current = access.observeOnOwner(cell.plan.streamId());
                if(!current.localAvailable() || current.confirmation().isEmpty()
                        || !permit.epoch().equals(current.serverEpoch()))
                {
                    lock.lock();
                    try
                    {
                        if(!stopping.get() && !cell.eof && !cell.failed && cell.bufferedFrame == null)
                            cell.bufferedFrame = frame;
                        wakeCondition.signalAll();
                    }
                    finally
                    {
                        lock.unlock();
                    }
                    return;
                }

                long seq;
                long ts;
                lock.lock();
                try
                {
                    if(stopping.get() || cell.eof || cell.failed)
                        return;
                    seq = cell.frameSeq++;
                    ts = scaleNanos(seq, cell.plan.fps());
                }
                finally
                {
                    lock.unlock();
                }

                byte[] bytes = frame.bytes();
                var emission = new FrameEmission(cell.plan.streamId(), current.serverEpoch(),
                        seq, ts, bytes.length, bytes, current.confirmation().orElseThrow());
                attemptedFrames.incrementAndGet();
                FrameAdmission admission = framePort.admit(emission);
                if(admission == FrameAdmission.ADMITTED)
                    admittedFrames.incrementAndGet();
                else
                    refusedFrames.incrementAndGet();
            }
            catch(Throwable t)
            {
                recordFatal(t);
            }
        });

        if(!offered)
            recordFatal(new RejectedExecutionException("Owner rejected frame emission"));
    }

    private void withdrawStreamOnOwner(UUID streamId, UUID expectedEpoch)
    {
        OverlayNode.OwnerAccess owner = this.ownerAccess;
        RoutingService.LocalSourceAccess access = this.localSourceAccess;
        if(owner == null || access == null || stopping.get())
            return;

        boolean offered = owner.offer(() ->
        {
            if(stopping.get())
                return;
            try
            {
                RoutingService.AvailabilityOutcome outcome = access.setAvailableOnOwner(streamId, expectedEpoch, false);
                if(outcome == RoutingService.AvailabilityOutcome.STALE)
                    recordFatal(new IllegalStateException("Stale epoch withdrawing local stream " + streamId));
            }
            catch(Throwable t)
            {
                recordFatal(t);
            }
        });
        if(!offered)
            recordFatal(new RejectedExecutionException("Owner rejected availability withdrawal"));
    }

    private void recordFatal(Throwable cause)
    {
        if(cause == null)
            return;
        primaryFailure.compareAndSet(null, cause);
        if(fatalSignaled.compareAndSet(false, true))
        {
            try
            {
                fatalPort.onFatal(cause);
            }
            catch(Throwable ignored)
            {
                // Preserve primary failure.
            }
            OverlayNode.OwnerAccess owner = this.ownerAccess;
            if(owner != null)
            {
                try
                {
                    owner.fail(cause);
                }
                catch(Throwable ignored)
                {
                    // Owner may already be stopped.
                }
            }
        }
        initiateStop(null);
    }

    //* === Shutdown & Termination === *

    public void requestShutdown(ShutdownBudget budget)
    {
        Objects.requireNonNull(budget, "budget");
        initiateStop(budget);
    }

    private void initiateStop(ShutdownBudget budget)
    {
        if(budget != null)
            shutdownBudget.compareAndSet(null, budget);
        if(stopping.compareAndSet(false, true))
        {
            lock.lock();
            try
            {
                for(StreamCell cell : orderedCells)
                {
                    if(cell.activePermit != null)
                        cell.activePermit.revoked().set(true);
                    cell.activePermit = null;
                    cell.paused = true;
                    cell.bufferedFrame = null;
                }
                // BUG-C fix: collect queued (not-yet-dispatched) jobs before clearing,
                // then reset their inFlight flags so awaitTermination does not mistake
                // them for handles held by live workers.
                var queuedTokens = new java.util.HashMap<StreamCell, Long>(workerQueue.size() * 2 + 1);
                for(ReadJob job : workerQueue)
                    queuedTokens.put(job.cell(), job.opToken());
                workerQueue.clear();
                for(var entry : queuedTokens.entrySet())
                {
                    StreamCell qc = entry.getKey();
                    if(qc.opToken == entry.getValue())
                        qc.inFlight = false;
                }
                wakeCondition.signalAll();
            }
            finally
            {
                lock.unlock();
            }
            coordinatorThread.interrupt();
            for(Thread w : workerThreads)
                w.interrupt();
        }
    }

    public TerminationStatus awaitTermination(ShutdownBudget budget)
    {
        Objects.requireNonNull(budget, "budget");
        initiateStop(budget);
        ShutdownBudget activeBudget = shutdownBudget.get();
        if(activeBudget == null)
        {
            shutdownBudget.compareAndSet(null, budget);
            activeBudget = shutdownBudget.get();
        }

        boolean coordStopped = joinExact(coordinatorThread, activeBudget, clock);
        int alive = 0;
        for(Thread w : workerThreads)
        {
            if(!joinExact(w, activeBudget, clock))
                alive++;
        }

        // Physical handle cleanup does not depend on the owner thread,
        // but never closes a handle concurrently owned by an active worker in nextFrame().
        var outcomes = new LinkedHashMap<UUID, CleanupOutcome>();
        for(StreamCell cell : orderedCells)
        {
            boolean stillOwnedByLiveWorker;
            lock.lock();
            try
            {
                stillOwnedByLiveWorker = alive > 0 && cell.inFlight;
            }
            finally
            {
                lock.unlock();
            }

            if(stillOwnedByLiveWorker)
            {
                if(cell.cleanupOutcome == CleanupOutcome.NOT_OPENED || cell.cleanupOutcome == CleanupOutcome.CLOSED)
                    cell.cleanupOutcome = CleanupOutcome.CLEANUP_UNCERTAIN;
            }
            else if(cell.cleanupOutcome == CleanupOutcome.NOT_OPENED || cell.cleanupOutcome == CleanupOutcome.CLOSED)
            {
                if(cell.handle != null && cell.handle.state() != VideoSource.State.CLOSED)
                    cell.cleanupOutcome = closeHandle(cell.handle);
                else if(cell.cleanupOutcome == CleanupOutcome.NOT_OPENED && cell.handle != null)
                    cell.cleanupOutcome = CleanupOutcome.CLOSED;
            }
            outcomes.put(cell.plan.streamId(), cell.cleanupOutcome);
        }

        boolean complete = coordStopped && alive == 0
                && outcomes.values().stream().allMatch(o -> o == CleanupOutcome.CLOSED);
        return new TerminationStatus(complete, coordStopped, alive, outcomes);
    }

    @Override
    public void close()
    {
        if(!closed.compareAndSet(false, true))
            return;
        ShutdownBudget budget = shutdownBudget.get();
        if(budget == null)
            budget = ShutdownBudget.start(clock.getAsLong(), Duration.ofSeconds(1));
        awaitTermination(budget);
    }

    public Optional<Throwable> failure()
    {
        return Optional.ofNullable(primaryFailure.get());
    }

    public Metrics metrics()
    {
        return new Metrics(attemptedFrames.get(), admittedFrames.get(), refusedFrames.get(),
                readFrames.get(), loops.get(), eofs.get(), sourceErrors.get());
    }

    private static boolean joinExact(Thread thread, ShutdownBudget budget, LongSupplier clock)
    {
        if(thread.getState() == Thread.State.NEW)
            return true;
        if(thread == Thread.currentThread())
            return false;
        long remNanos = budget.remainingNanos(clock.getAsLong());
        long millis = remNanos / 1_000_000L;
        if(millis > 0L)
        {
            try
            {
                thread.join(millis);
            }
            catch(InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return !thread.isAlive();
            }
        }
        return !thread.isAlive();
    }

    private static CleanupOutcome closeHandle(SourceHandle handle)
    {
        if(handle == null)
            return CleanupOutcome.NOT_OPENED;
        boolean wasFailed = handle.state() == VideoSource.State.FAILED;
        try
        {
            handle.close();
            if(wasFailed || handle.state() == VideoSource.State.FAILED)
                return CleanupOutcome.CLEANUP_UNCERTAIN;
            return handle.state() == VideoSource.State.CLOSED ? CleanupOutcome.CLOSED : CleanupOutcome.CLEANUP_UNCERTAIN;
        }
        catch(Throwable ignored)
        {
            return CleanupOutcome.CLOSE_FAILED;
        }
    }

    private static void closeQuietly(SourceHandle handle)
    {
        if(handle == null)
            return;
        try
        {
            handle.close();
        }
        catch(Throwable ignored)
        {
            // Best-effort cleanup on preparation cancellation.
        }
    }
}
