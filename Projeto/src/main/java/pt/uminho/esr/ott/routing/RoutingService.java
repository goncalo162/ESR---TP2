package pt.uminho.esr.ott.routing;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.LongSupplier;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.monitoring.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.streaming.StreamCatalog;
import pt.uminho.esr.ott.streaming.StreamInfo;
import pt.uminho.esr.ott.transport.Connection;

/** Bounded path-vector dissemination on the node's borrowed serial owner. */
public final class RoutingService implements AutoCloseable {
    public interface ControlPort { CompletionStage<Connection.SendResult> send(Neighbor.Session session, ControlMessage message); }
    public interface Observer { void onEvent(Event event); }
    public interface Factory { RoutingService create(NodeConfiguration config, UUID instanceId, NeighborManager.SerialOwner owner, ControlPort port, Observer observer); }
    public interface Ticket { void cancel(); }
    public interface Scheduler {
        Ticket schedule(Runnable task, long delayNanos);
        void requestClose(); boolean awaitTermination(Duration budget); int physicalJobs();
    }
    /** Approved local diagnostic contract; not a wire enum. */
    public enum TraceState { SUCCESS, ERROR, TIMEOUT, CANCELLED, CLOSED, RESOURCE_LIMIT, NOT_RUNNING }
    public static final int TRACE_INITIAL_TTL = 32;
    public static final Duration TRACE_TIMEOUT = Duration.ofMillis(3000);
    /** Polling never invokes caller continuations on the node owner. */
    public interface TraceTicket {
        UUID traceId();
        Optional<TraceOutcome> result();
        void cancel();
    }
    public record TraceOutcome(UUID streamId, UUID traceId, Optional<UUID> serverEpoch,
            TraceState state, Optional<ErrorCode> errorCode, String detail,
            List<String> visited, long elapsedNanos) {
        public TraceOutcome {
            Objects.requireNonNull(streamId); Objects.requireNonNull(traceId);
            Objects.requireNonNull(serverEpoch); Objects.requireNonNull(state);
            Objects.requireNonNull(errorCode); Objects.requireNonNull(detail);
            Objects.requireNonNull(visited);
            if (!tracePath(visited, true) || !traceText(detail, 256) || elapsedNanos < 0)
                throw new IllegalArgumentException("Trace outcome bounds");
            visited = List.copyOf(visited);
        }
    }
    /** Admission is bounded before publishing to the borrowed owner. No user continuations. */
    public TraceTicket requestTrace(UUID streamId) {
        Objects.requireNonNull(streamId);
        if (stopping.get() || view.diagnostics().phase() != Phase.RUNNING) return unavailableTrace(streamId);
        long began = clock.getAsLong();
        var ticket = new LocalTrace(streamId, UUID.randomUUID(), began);
        synchronized (resources) {
            if (stopping.get()) return unavailableTrace(streamId);
            if (traceReservations >= traceLimit || localTraces.size() >= 4) {
                ticket.outcome = traceValue(ticket, null, TraceState.RESOURCE_LIMIT, ErrorCode.RESOURCE_LIMIT,
                        "TRACE_CAPACITY", List.of(), began); return ticket;
            }
            traceReservations++; localTraces.add(ticket);
        }
        if (!owner.offer(() -> {
            try { admitTrace(ticket); }
            catch (RuntimeException failure) { note(Reason.CLOCK_ERROR,true); }
        })) {
            // The real owner rejects by initiating stop; its hook also resolves queued tickets.
            synchronized (resources) {
                if (localTraces.remove(ticket)) traceReservations--;
                if (ticket.outcome == null) ticket.outcome = traceValue(ticket, null, TraceState.CLOSED,
                        null, "OWNER_REJECTED", List.of(), began);
            }
        }
        return ticket;
    }
    public static TraceTicket unavailableTrace(UUID streamId) {
        Objects.requireNonNull(streamId);
        UUID id = UUID.randomUUID();
        var value = new TraceOutcome(streamId,id,Optional.empty(),TraceState.NOT_RUNNING,
                Optional.empty(),"NOT_RUNNING",List.of(),0);
        return new TraceTicket() {
            public UUID traceId() { return id; }
            public Optional<TraceOutcome> result() { return Optional.of(value); }
            public void cancel() { }
        };
    }
    public enum Phase { NEW, RUNNING, STOPPED }
    public enum Reason { NONE, INVALID_INPUT, METADATA_CONFLICT, CAPACITY, CLOSED, OUTPUT_OVERFLOW,
        INVALID_LOCAL_MESSAGE, OWNER_REJECTED, CLOCK_ERROR, SEQUENCE_EXHAUSTED, STOPPED }
    public record Event(Reason reason, boolean fatal) { public Event { Objects.requireNonNull(reason); } }
    public record Diagnostics(Phase phase, int sessions, int bindings, int references, int cacheEntries,
            int ledgers, int pendingCells, int pendingIntents, int quotas, int sendTickets, int timers,
            long failures, boolean countersSaturated, Reason lastReason) { }
    public record Snapshot(long capturedAtNanos, StreamCatalog.Snapshot catalog,
            Map<UUID,Route> routes, Diagnostics diagnostics) {
        public Snapshot { Objects.requireNonNull(catalog);Objects.requireNonNull(diagnostics);routes=Map.copyOf(routes); }
    }
    private static final long WINDOW=1_000_000_000L;
    private final NodeConfiguration config;
    private final UUID instance;
    private final NeighborManager.SerialOwner owner;
    private final ControlPort control;
    private final Observer observer;
    private final LongSupplier clock;
    private final Scheduler scheduler;
    private final RoutingTable root;
    private final RoutingTable.Limits limits;
    private final StreamCatalog catalog;
    private final int maxCache,maxPending,maxTickets,flood;
    private final long period,history,shutdownNanos;
    private final LinkedHashMap<UUID,Row> rows=new LinkedHashMap<>();
    private final List<UUID> streamIndex=new ArrayList<>();
    private final Map<String,Integer> originCounts=new HashMap<>();
    private final Map<String,UUID> epochs=new HashMap<>();
    private final LinkedHashMap<String,Neighbor.Session> sessions=new LinkedHashMap<>();
    private final List<Neighbor.Session> peerIndex=new ArrayList<>();
    private final Map<String,OptionalLong> costs=new HashMap<>();
    private final Map<Key,Ledger> ledgers=new HashMap<>();
    private final Map<Generation,CacheEntry> cache=new HashMap<>();
    private final Map<QuotaKey,ArrayDeque<Long>> quotas=new HashMap<>();
    private final ArrayDeque<QuotaKey> quotaSweep=new ArrayDeque<>();
    // These locks/flags cover physical work only; all routing state above belongs to owner.
    private final Object resources=new Object();
    private final Set<Admission> admissions=new HashSet<>();
    // Physical reservations/command cancellation may be touched off owner, routing contexts may not.
    private final Set<LocalTrace> localTraces = new HashSet<>();
    private final Set<TraceSend> traceSends = new HashSet<>();
    private final Map<UUID,TraceContext> traces = new HashMap<>();
    private final ArrayDeque<TraceContext> traceOrder = new ArrayDeque<>();
    private final int traceLimit;
    private int traceReservations, traceSweep;

    private final AtomicBoolean stopping=new AtomicBoolean(),wakeQueued=new AtomicBoolean();
    private final AtomicReference<Long> firstStop=new AtomicReference<>();
    private final CompletableFuture<Void> cleaned=new CompletableFuture<>();
    private NeighborManager.Registration registration;
    private volatile Snapshot view;
    private Phase phase=Phase.NEW;
    private Ticket alarm;
    private long alarmAt,lastNow,failures;
    private boolean observed,driving,countersSaturated;
    private Reason lastReason=Reason.NONE;
    private int scanRemaining,streamCursor,peerCursor,pending;

    private static final class Row {
        final UUID id;final String origin;
        StreamInfo info;Route route;long version,sequence,nextAt;boolean local,offered;
        Row(UUID id,String origin){this.id=id;this.origin=origin;}
    }
    private record Key(UUID stream,Neighbor.Session session) { }
    private record QuotaKey(UUID stream,String nodeId) { }
    private record Generation(UUID stream,UUID epoch,long sequence) { }
    private static final class CacheEntry {
        final long began;long cost;int hops;
        CacheEntry(long t,Publication p){began=t;cost=p.cost;hops=p.path.size();}
        void improve(Publication p){if(p.cost<cost||p.cost==cost&&p.path.size()<hops){cost=p.cost;hops=p.path.size();}}
    }
    private record Publication(StreamInfo info,long sequence,long cost,List<String> path) {
        Publication { path=List.copyOf(path); }
        Generation generation(){return new Generation(info.streamId(),info.serverEpoch(),sequence);}
        boolean betterThan(Publication p){return cost<p.cost||cost==p.cost&&path.size()<p.path.size();}
    }
    private static final class Ledger {
        final Key key;UUID epoch;long maximum=-1,floor=-1,seenVersion=-1;
        Publication receipt,debt,want;boolean cell,review;
        Ledger(Key key){this.key=key;}
    }
    private final class Admission {
        final Key key;final Publication publication;final boolean withdrawal;
        Connection.SendResult result;Throwable error;boolean ready,callbackDone,continuationDone;
        Admission(Key key,Publication p,boolean w){this.key=key;publication=p;withdrawal=w;}
        void complete(Connection.SendResult value,Throwable failure){
            synchronized(resources){result=value;error=failure;ready=true;}
            try { if(!stopping.get())wake(); }
            finally {
                boolean freed;synchronized(resources){callbackDone=true;freed=release(this);}
                if(freed&&!stopping.get())wake();
            }
        }
    }

    public RoutingService(NodeConfiguration c,UUID id,NeighborManager.SerialOwner o,ControlPort p,Observer v){
        this(c,id,o,p,v,System::nanoTime,null,RoutingTable.Limits.forNode(c.runtime().control().maxConnections()));
    }
    public RoutingService(NodeConfiguration c,UUID id,NeighborManager.SerialOwner o,ControlPort p,Observer v,
            LongSupplier clock,Scheduler scheduler){
        this(c,id,o,p,v,clock,scheduler,RoutingTable.Limits.forNode(c.runtime().control().maxConnections()));
    }
    RoutingService(NodeConfiguration c,UUID id,NeighborManager.SerialOwner o,ControlPort p,Observer v,
            LongSupplier clock,Scheduler scheduler,RoutingTable.Limits limits){
        config=Objects.requireNonNull(c);instance=Objects.requireNonNull(id);owner=Objects.requireNonNull(o);
        control=Objects.requireNonNull(p);observer=Objects.requireNonNull(v);this.clock=Objects.requireNonNull(clock);
        this.limits=Objects.requireNonNull(limits);
        // Reuse full offline endpoint/role validation without opening a socket/starting a manager.
        new NeighborManager(c,id,o,e->{});
        if(id.equals(new UUID(0,0)))throw new IllegalArgumentException("Nonzero instance required");
        validateConfiguration(c,id);
        if(c.streams().size()>limits.streamsPerOrigin())throw new IllegalArgumentException("Source capacity");
        var r=c.runtime().routing();period=r.announceInterval().toNanos();history=r.cacheTtl().toNanos();
        traceLimit=Math.min(o.capacity(),64);
        maxCache=r.cacheCapacity();maxPending=Math.min(maxCache,o.capacity());maxTickets=Math.min(o.capacity(),2*limits.neighbors());
        flood=r.floodMaxPerSecond();shutdownNanos=c.runtime().control().shutdownTimeout().toNanos();
        root=new RoutingTable(c.nodeId(),limits,r.candidateTtl().toMillis(),r.cacheTtl().toMillis(),clock);
        catalog=new StreamCatalog(limits.streamCapacity());
        long initialTime=clock.getAsLong(); // fail before allocating a helper/registering cleanup
        this.scheduler=scheduler==null?new Timers():scheduler;
        try { registration=owner.onStop(this::ownerStopped);publish(initialTime); }
        catch(RuntimeException e){
            if(registration!=null)registration.remove();
            this.scheduler.requestClose();throw e;
        }
    }
    /** Validates new source metadata without creating an owner, helper or socket. */
    public static void validateConfiguration(NodeConfiguration c,UUID id){
        Objects.requireNonNull(c);Objects.requireNonNull(id);
        if(id.equals(new UUID(0,0)))throw new IllegalArgumentException("Nonzero instance required");
        var ids=new HashSet<UUID>();
        for(var stream:c.streams()){
            if(c.role()!=NodeRole.SERVER||!stream.serverId().equals(c.nodeId())||!ids.add(stream.streamId())||ids.size()>256)
                throw new IllegalArgumentException("Invalid configured source");
            new StreamInfo(stream.streamId(),stream.serverId(),stream.title(),1,stream.fps(),id);
        }
    }
    public static Snapshot emptySnapshot(){
        return new Snapshot(0,new StreamCatalog.Snapshot(0,Map.of()),Map.of(),new Diagnostics(Phase.NEW,0,0,0,0,0,0,0,0,0,0,0,false,Reason.NONE));
    }
    private void requireOwner(){if(!owner.isOwnerThread())throw new IllegalStateException("Routing requires node owner");}
    private long now(){long t=clock.getAsLong();if(observed&&t-lastNow<0)throw new IllegalStateException("Clock regressed");observed=true;lastNow=t;return t;}
    private static RoutingTable.SessionKey key(Neighbor.Session s){return new RoutingTable.SessionKey(s.nodeId(),s.instanceId(),s.connectionId());}
    private boolean current(Neighbor.Session s){return s.equals(sessions.get(s.nodeId()));}
    private boolean applied(RoutingTable.Outcome o){
        if(o.status()==RoutingTable.Status.REJECTED){note(o.reason()==RoutingTable.Reason.CAPACITY_EXCEEDED?Reason.CAPACITY:Reason.INVALID_INPUT,false);return false;}
        for(UUID id:o.changedStreams())refresh(rows.get(id));
        return o.status()==RoutingTable.Status.APPLIED;
    }
    private void refresh(Row row){if(row==null)return;var route=root.snapshot(row.id).orElseThrow();if(!route.equals(row.route)){row.route=route;row.version=Math.incrementExact(row.version);dirty();}}
    private void dirty(){scanRemaining=Math.multiplyExact(rows.size(),sessions.size());}
    private Row bind(UUID id,String origin){
        var old=rows.get(id);if(old!=null)return old.origin.equals(origin)?old:null;
        if(rows.size()==limits.streamCapacity()||!originCounts.containsKey(origin)&&originCounts.size()==limits.origins()
                ||originCounts.getOrDefault(origin,0)==limits.streamsPerOrigin()){note(Reason.CAPACITY,false);return null;}
        var outcome=root.bindStream(id,origin);if(outcome.status()!=RoutingTable.Status.APPLIED){note(Reason.CAPACITY,false);return null;}
        var row=new Row(id,origin);rows.put(id,row);streamIndex.add(id);originCounts.merge(origin,1,Integer::sum);refresh(row);dirty();return row;
    }
    private boolean epoch(String origin,UUID replacement){
        UUID old=epochs.get(origin);if(replacement.equals(old))return true;
        var outcome=root.confirmEpoch(origin,Optional.ofNullable(old),replacement);
        if(outcome.status()!=RoutingTable.Status.APPLIED){note(outcome.reason()==RoutingTable.Reason.CAPACITY_EXCEEDED?Reason.CAPACITY:Reason.INVALID_INPUT,false);return false;}
        epochs.put(origin,replacement);
        for(var row:rows.values())if(row.origin.equals(origin)){row.info=null;refresh(row);}
        dirty();return true;
    }
    public void start(){
        requireOwner();if(phase!=Phase.NEW||stopping.get())return;phase=Phase.RUNNING;
        try{
            long t=now();
            for(var s:config.streams()){
                var row=bind(s.streamId(),s.serverId());if(row==null)throw new IllegalStateException("Configured source capacity");
                if(!epoch(s.serverId(),instance))throw new IllegalStateException("Configured source epoch");
                row.info=new StreamInfo(s.streamId(),s.serverId(),s.title(),1,s.fps(),instance);row.local=true;row.offered=true;row.nextAt=t+period;
                applied(root.offerLocal(row.id,instance));
            }
            drive();
        }catch(RuntimeException e){note(Reason.INVALID_LOCAL_MESSAGE,true);}
    }
    public void onNeighborEvent(NeighborManager.Event event){
        requireOwner();Objects.requireNonNull(event);if(phase!=Phase.RUNNING||stopping.get())return;
        try{
            traceSweep=traceOrder.size();
            if(event instanceof NeighborManager.NeighborUp e){
                var s=e.session();var old=sessions.get(s.nodeId());
                if(!s.equals(old)){
                    if(old==null&&sessions.size()==limits.neighbors()){note(Reason.CAPACITY,false);return;}
                    if(!applied(root.replaceSession(Optional.ofNullable(old).map(RoutingService::key),key(s))))return;
                    if(old!=null)retire(old);sessions.put(s.nodeId(),s);costs.put(s.nodeId(),OptionalLong.empty());peersChanged();
                }
            }else if(event instanceof NeighborManager.NeighborDown e){if(current(e.session()))remove(e.session());}
            else if(event instanceof NeighborManager.Received e){
                var m=e.message();if(!current(e.session()))return;
                if (m.type()==MessageType.ROUTE_TRACE || m.type()==MessageType.TRACE_RESULT || m.type()==MessageType.ERROR) {
                    if (m.version()==2 && m.requestId()!=null && e.session().nodeId().equals(m.senderId())
                            && e.session().instanceId().equals(m.senderInstance())) onTrace(e.session(),m);
                    drive(); return;
                }
                if(m.type()!=MessageType.STREAM_ANNOUNCE&&m.type()!=MessageType.STREAM_WITHDRAW)return;
                if(m.version()!=2||m.requestId()==null||!e.session().nodeId().equals(m.senderId())||!e.session().instanceId().equals(m.senderInstance())){note(Reason.INVALID_INPUT,false);return;}
                if(m.type()==MessageType.STREAM_ANNOUNCE&&m.payload() instanceof ControlMessage.Announcement a)announce(e.session(),a);
                else if(m.type()==MessageType.STREAM_WITHDRAW&&m.payload() instanceof ControlMessage.Withdrawal w){
                    if(w.streamId()==null||w.serverEpoch()==null||w.announceSeq()<0){note(Reason.INVALID_INPUT,false);return;}
                    applied(root.withdraw(w.streamId(),w.serverEpoch(),key(e.session()),w.announceSeq()));
                }else{note(Reason.INVALID_INPUT,false);return;}
            }else return;
            drive();
        }catch(IllegalArgumentException|NullPointerException e){note(Reason.INVALID_INPUT,false);}
        catch(RuntimeException e){note(Reason.CLOCK_ERROR,true);}
    }
    private void announce(Neighbor.Session session,ControlMessage.Announcement a){
        var info=StreamInfo.fromWire(a.stream());var path=a.path();
        if(a.announceSeq()<0||a.costMicros()<0||path.isEmpty()||path.size()>31||!path.getFirst().equals(info.serverId())
                ||!path.getLast().equals(session.nodeId())||path.contains(config.nodeId())||new HashSet<>(path).size()!=path.size()
                ||path.stream().anyMatch(s->s==null||!s.matches("[A-Za-z0-9_-]{1,32}"))||(a.costMicros()==0)!=(path.size()==1)){
            note(Reason.INVALID_INPUT,false);return;
        }
        var cost=costs.get(session.nodeId());if(cost!=null&&cost.isPresent())try{Math.addExact(a.costMicros(),cost.orElseThrow());}catch(ArithmeticException e){note(Reason.INVALID_INPUT,false);return;}
        var existing=rows.get(info.streamId());
        if(existing!=null&&(!existing.origin.equals(info.serverId())||existing.info!=null&&existing.info.serverEpoch().equals(info.serverEpoch())&&!existing.info.sameMetadata(info))){note(Reason.METADATA_CONFLICT,false);return;}
        // Validate capacity before a source-wide epoch transition: rejection must preserve healthy rows.
        if(existing==null&&(rows.size()==limits.streamCapacity()
                ||!originCounts.containsKey(info.serverId())&&originCounts.size()==limits.origins()
                ||originCounts.getOrDefault(info.serverId(),0)==limits.streamsPerOrigin())){
            note(Reason.CAPACITY,false);return;
        }
        // A rejected epoch must not consume a new binding for an already known source.
        if(originCounts.containsKey(info.serverId())&&!epoch(info.serverId(),info.serverEpoch()))return;
        var row=existing==null?bind(info.streamId(),info.serverId()):existing;if(row==null)return;
        if(!epoch(info.serverId(),info.serverEpoch()))return;
        var outcome=root.announce(new RoutingTable.Advertisement(info.streamId(),info.serverId(),info.serverEpoch(),key(session),a.announceSeq(),a.costMicros(),path));
        if(applied(outcome)){
            if(row.info==null)row.info=info;
            // Catalog must be published with the newly established canonical metadata.
            dirty();
        }
    }
    public void onMetrics(NetworkMonitor.Event event){
        requireOwner();Objects.requireNonNull(event);if(phase!=Phase.RUNNING||stopping.get())return;
        try{
            Neighbor.Session session;OptionalLong cost;
            if(event instanceof NetworkMonitor.Updated e){session=e.metrics().session();cost=e.metrics().costMicros();}
            else if(event instanceof NetworkMonitor.Invalidated e){session=e.session();cost=OptionalLong.empty();}
            else return;
            if(!current(session))return;traceSweep=traceOrder.size();costs.put(session.nodeId(),cost);applied(root.setLinkCost(key(session),cost));drive();
        }catch(RuntimeException e){note(Reason.CLOCK_ERROR,true);}
    }
    public void setLocalAvailable(UUID id,boolean available){
        requireOwner();if(stopping.get())return;var row=rows.get(Objects.requireNonNull(id));
        if(row==null||!row.local)throw new IllegalArgumentException("Configured local stream required");
        if(row.offered==available)return;row.offered=available;
        try{
            if(available){row.sequence=Math.incrementExact(row.sequence);row.nextAt=now()+period;applied(root.offerLocal(id,instance));}
            else applied(root.removeLocalOffer(id,instance));
            row.version=Math.incrementExact(row.version);dirty();drive();
        }catch(ArithmeticException e){note(Reason.SEQUENCE_EXHAUSTED,true);}
    }
    private void peersChanged(){peerIndex.clear();peerIndex.addAll(sessions.values());peerCursor=0;dirty();}
    private void retire(Neighbor.Session s){
        var iterator=ledgers.entrySet().iterator();while(iterator.hasNext()){
            var entry=iterator.next();if(entry.getKey().session.equals(s)){if(entry.getValue().cell)pending--;iterator.remove();}
        }
    }
    private void remove(Neighbor.Session s){
        if(!current(s))return;applied(root.removeSession(key(s)));sessions.remove(s.nodeId());costs.remove(s.nodeId());retire(s);peersChanged();
    }
    private Publication selected(Row row,Neighbor.Session recipient){
        if(row.info==null||row.route==null)return null;
        if(row.offered)return new Publication(row.info,row.sequence,0,List.of(config.nodeId()));
        var candidate=row.route.selectedCandidate();if(candidate.isEmpty())return null;
        var c=candidate.orElseThrow();if(c.path().contains(recipient.nodeId()))return null;
        return new Publication(row.info,c.announceSeq(),c.metric().orElseThrow().costMicros(),c.path());
    }
    private void reconsider(Ledger ledger,Row row){
        var epoch=row.route.serverEpoch().orElse(null);
        if(!Objects.equals(epoch,ledger.epoch)){ledger.epoch=epoch;ledger.maximum=-1;ledger.floor=-1;}
        Publication desired=selected(row,ledger.key.session);
        var old=ledger.receipt;
        if(old!=null){
            boolean invalid=desired==null||!old.info.serverEpoch().equals(desired.info.serverEpoch())
                ||desired.sequence<ledger.maximum
                ||desired.sequence==old.sequence&&!desired.equals(old)&&!desired.betterThan(old);
            if(invalid&&ledger.debt==null)ledger.debt=old;
        }
        if(desired!=null){
            if(desired.info.serverEpoch().equals(ledger.epoch)&&(desired.sequence<=ledger.floor||desired.sequence<ledger.maximum))desired=null;
            else if(ledger.debt!=null&&desired.info.serverEpoch().equals(ledger.debt.info.serverEpoch())&&desired.sequence<=ledger.debt.sequence)desired=null;
            else if(ledger.debt==null&&desired.equals(old))desired=null;
        }
        ledger.want=null;
        if(ledger.debt==null&&desired==null){freeCell(ledger);return;}
        if(!ledger.cell){if(pending==maxPending){note(Reason.CAPACITY,false);return;}ledger.cell=true;pending++;}
        if(desired!=null){
            var generation=desired.generation();var cached=cache.get(generation);
            if(cached==null){if(cache.size()==maxCache){note(Reason.CAPACITY,false);if(ledger.debt==null)freeCell(ledger);return;}cached=new CacheEntry(lastNow,desired);cache.put(generation,cached);}
            else cached.improve(desired);
            ledger.want=desired;
        }
    }
    private void freeCell(Ledger ledger){if(ledger.cell){pending--;ledger.cell=false;}ledger.want=null;}
    private boolean busy(Neighbor.Session session){synchronized(resources){return admissions.stream().anyMatch(t->t.key.session.equals(session)&&!t.continuationDone);}}
    private boolean release(Admission ticket){
        if(ticket.callbackDone&&ticket.continuationDone&&admissions.remove(ticket)){resources.notifyAll();return true;}
        return false;
    }
    private void completions(){
        List<Admission> ready;
        synchronized(resources){ready=admissions.stream().filter(t->t.ready&&!t.continuationDone).toList();}
        for(var ticket:ready){
            try{
                if(stopping.get()||!current(ticket.key.session))continue;
                var ledger=ledgers.get(ticket.key);if(ledger==null)continue;
                if(ticket.error!=null){note(Reason.INVALID_LOCAL_MESSAGE,true);continue;}
                if(ticket.result==Connection.SendResult.ACCEPTED){
                    if(ticket.withdrawal){
                        if(ticket.publication.equals(ledger.debt))ledger.debt=null;
                        if(ticket.publication.equals(ledger.receipt))ledger.receipt=null;
                        if(ticket.publication.info.serverEpoch().equals(ledger.epoch))ledger.floor=Math.max(ledger.floor,ticket.publication.sequence);
                    }else{
                        ledger.receipt=ticket.publication;
                        if(ticket.publication.info.serverEpoch().equals(ledger.epoch))ledger.maximum=Math.max(ledger.maximum,ticket.publication.sequence);
                    }
                    ledger.review=true;dirty();
                }else if(ticket.result==Connection.SendResult.CLOSED||ticket.result==Connection.SendResult.OVERFLOW){
                    note(ticket.result==Connection.SendResult.CLOSED?Reason.CLOSED:Reason.OUTPUT_OVERFLOW,false);remove(ticket.key.session);
                }else note(Reason.INVALID_LOCAL_MESSAGE,true);
            }finally{
                boolean freed;synchronized(resources){ticket.continuationDone=true;freed=release(ticket);}
                if(freed&&!stopping.get())wake();
            }
        }
    }
    private void purgeQuota(ArrayDeque<Long> q,long t){while(!q.isEmpty()&&t-q.getFirst()>=WINDOW)q.removeFirst();}
    private int sweepQuotas(long t){int n=Math.min(16,quotaSweep.size());for(int i=0;i<n;i++){
        var key=quotaSweep.removeFirst();var q=quotas.get(key);purgeQuota(q,t);
        if(q.isEmpty()&&!sessions.containsKey(key.nodeId))quotas.remove(key);else quotaSweep.addLast(key);
    }return n;}
    private boolean quota(Ledger l,long t){
        var key=new QuotaKey(l.key.stream,l.key.session.nodeId());var q=quotas.get(key);
        if(q==null){if(quotas.size()==limits.slotCapacity()){note(Reason.CAPACITY,false);return false;}q=new ArrayDeque<>();quotas.put(key,q);quotaSweep.addLast(key);}
        purgeQuota(q,t);if(q.size()==flood)return false;q.addLast(t);return true;
    }
    private boolean currentAnnouncement(Ledger ledger,Publication publication,long at){
        if(stopping.get()||!current(ledger.key.session))return false;
        var row=rows.get(ledger.key.stream);
        if(row==null||!publication.equals(selected(row,ledger.key.session)))return false;
        if(row.offered)return true;
        var candidate=row.route.selectedCandidate().orElseThrow();
        long age=at-candidate.generationStartedAtNanos();
        return age>=0&&age<candidate.candidateTtlNanos();
    }
    private boolean attempt(Ledger l){
        if(stopping.get()||!current(l.key.session)||!l.cell||busy(l.key.session))return false;
        if(l.debt==null&&l.want!=null){
            // Previous sends in this same turn may have consumed the remaining TTL.
            // Refresh selection/expiry without renewing a generation or manufacturing seq.
            long at=now();applied(root.expire());
            var row=rows.get(l.key.stream);
            if(l.seenVersion!=row.version||l.review){
                l.seenVersion=row.version;l.review=false;reconsider(l,row);
            }
            publish(at);
        }
        Publication p=l.debt!=null?l.debt:l.want;boolean withdrawal=l.debt!=null;
        if(stopping.get()||p==null)return false;
        ControlMessage message=new ControlMessage(withdrawal?MessageType.STREAM_WITHDRAW:MessageType.STREAM_ANNOUNCE,UUID.randomUUID(),config.nodeId(),instance,
            withdrawal?new ControlMessage.Withdrawal(p.info.streamId(),p.info.serverEpoch(),p.sequence):new ControlMessage.Announcement(p.info.toWire(),p.sequence,p.cost,p.path));
        // Check at the port boundary, after message preparation; do not reuse turn time.
        long attemptAt=now();
        if(!withdrawal&&!currentAnnouncement(l,p,attemptAt)){
            applied(root.expire());var row=rows.get(l.key.stream);
            l.seenVersion=row.version;l.review=false;reconsider(l,row);publish(attemptAt);
            return false; // no physical ticket or rate token was consumed by this stale intent
        }
        Admission ticket;
        synchronized(resources){if(admissions.size()==maxTickets)return false;ticket=new Admission(l.key,p,withdrawal);admissions.add(ticket);}
        if(!quota(l,attemptAt)){discardUnsent(ticket);return false;}
        try {
            // Physical/quota reservation can take time too. This is the last clock
            // check before the port call; an already charged attempt is not refunded.
            long dispatchAt=now();
            if(!withdrawal&&!currentAnnouncement(l,p,dispatchAt)){
                discardUnsent(ticket);applied(root.expire());
                var row=rows.get(l.key.stream);l.seenVersion=row.version;l.review=false;
                reconsider(l,row);publish(dispatchAt);return false;
            }
        } catch(RuntimeException e){discardUnsent(ticket);throw e;}
        if(!withdrawal){l.want=null;if(l.debt==null)freeCell(l);}
        try{Objects.requireNonNull(control.send(l.key.session,message)).whenComplete(ticket::complete);}
        catch(RuntimeException e){ticket.complete(null,e);}
        return true;
    }
    private void discardUnsent(Admission ticket){
        // No port call/future/callback exists yet, so this reservation has no
        // outstanding physical work. In-flight admissions still use release().
        synchronized(resources){admissions.remove(ticket);resources.notifyAll();}
    }
    private void scan(int budget){
        int sends=0;
        while(scanRemaining>0&&budget-->0&&!stopping.get()){
            if(streamIndex.isEmpty()||peerIndex.isEmpty()){scanRemaining=0;break;}
            if(streamCursor>=streamIndex.size())streamCursor=0;
            if(peerCursor>=peerIndex.size())peerCursor=0;
            var row=rows.get(streamIndex.get(streamCursor));var peer=peerIndex.get(peerCursor);
            if(++peerCursor==peerIndex.size()){peerCursor=0;streamCursor=(streamCursor+1)%streamIndex.size();}
            scanRemaining--;
            var key=new Key(row.id,peer);var ledger=ledgers.get(key);
            if(ledger==null){if(selected(row,peer)==null)continue;ledger=new Ledger(key);ledgers.put(key,ledger);}
            if(ledger.seenVersion!=row.version||ledger.review||ledger.debt!=null&&!ledger.cell){
                ledger.seenVersion=row.version;ledger.review=false;reconsider(ledger,row);
            }
            if(sends<16&&attempt(ledger))sends++;
        }
    }
    private void drive(){
        requireOwner();if(driving||stopping.get()||phase!=Phase.RUNNING)return;driving=true;
        try{
            long t=now();completions();if(stopping.get())return;
            applied(root.expire());cache.entrySet().removeIf(e->t-e.getValue().began>=history);
            for(var row:rows.values())if(row.offered&&t-row.nextAt>=0){
                try{row.sequence=Math.incrementExact(row.sequence);row.version=Math.incrementExact(row.version);}
                catch(ArithmeticException e){note(Reason.SEQUENCE_EXHAUSTED,true);return;}
                row.nextAt=t+period;dirty();
            }
            // All owner projections precede the resulting send attempts.
            publish(t); traceDrive(); int swept=sweepQuotas(t);scan(64-swept);
            // A slow/control-port callback may also advance time during this turn.
            long finished=now();applied(root.expire());publish(finished);
            if(scanRemaining>0)wake();
            schedule(finished);
        }catch(RuntimeException e){note(Reason.CLOCK_ERROR,true);}
        finally{driving=false;}
    }
    private void schedule(long t){
        if(stopping.get())return;
        long delay=Long.MAX_VALUE;
        for (var context : traces.values()) delay=Math.min(delay,Math.max(1,TRACE_TIMEOUT.toNanos()-(t-context.began)));
        for(var row:rows.values()){
            if(row.offered)delay=Math.min(delay,Math.max(1,row.nextAt-t));
            if(row.route!=null)for(var c:row.route.candidates())delay=Math.min(delay,Math.max(1,c.candidateTtlNanos()-(t-c.generationStartedAtNanos())));
        }
        for(var c:cache.values())delay=Math.min(delay,Math.max(1,history-(t-c.began)));
        // One finite alarm also expires quota histories, including disconnected node IDs.
        for(var q:quotas.values())if(!q.isEmpty())delay=Math.min(delay,Math.max(1,WINDOW-(t-q.getFirst())));
        if(delay==Long.MAX_VALUE){if(alarm!=null)alarm.cancel();alarm=null;return;}
        long at=t+delay;if(alarm!=null&&alarmAt==at)return;
        if(alarm!=null)alarm.cancel();alarm=null;
        alarmAt=at;alarm=scheduler.schedule(()->{wake();},delay);
    }
    private void wake(){
        if(stopping.get()||!wakeQueued.compareAndSet(false,true))return;
        if(!owner.offer(()->{wakeQueued.set(false);if(stopping.get())return;
            // A timer wake starts another bounded fair pass, never an unbounded retry loop.
            if (traceSweep==0) traceSweep=traceOrder.size();
            if(scanRemaining==0)dirty();drive();
        })){
            wakeQueued.set(false);stopping.set(true);firstStop.compareAndSet(null,System.nanoTime());scheduler.requestClose();
            // The real owner's rejected offer initiates stop. Its hook owns logical cleanup.
        }
    }
    private void publish(long t){
        var routes=new LinkedHashMap<UUID,Route>();var entries=new LinkedHashMap<UUID,StreamCatalog.Entry>();int refs=0,intents=0;
        for(var row:rows.values()){
            if(row.route!=null)routes.put(row.id,row.route);if(row.info==null)continue;refs++;
            if(row.offered)entries.put(row.id,new StreamCatalog.Entry(row.info,row.route.state(),row.route.selectedMetric(),row.route.knownUpstream(),true,OptionalLong.empty()));
            else{
                long remaining=0;for(var c:row.route.candidates())remaining=Math.max(remaining,c.candidateTtlNanos()-(t-c.generationStartedAtNanos()));
                if(remaining>0)entries.put(row.id,new StreamCatalog.Entry(row.info,row.route.state(),row.route.selectedMetric(),row.route.knownUpstream(),false,OptionalLong.of(remaining)));
            }
        }
        for(var l:ledgers.values())if(l.cell){if(l.debt!=null)intents++;if(l.want!=null)intents++;}
        var projection=new StreamCatalog.Snapshot(t,entries);catalog.replace(projection);
        int physical;synchronized(resources){physical=admissions.size();}
        view=new Snapshot(t,projection,routes,new Diagnostics(phase,sessions.size(),rows.size(),refs,cache.size(),ledgers.size(),pending,intents,quotas.size(),physical,scheduler.physicalJobs(),failures,countersSaturated,lastReason));
    }
    private static boolean traceText(String text,int maximum) {
        if (text==null || text.length()>maximum) return false;
        int bytes=0;
        for (int i=0;i<text.length();i++) {
            char c=text.charAt(i); if(c==0)return false;
            if (Character.isHighSurrogate(c)) {
                if(++i==text.length() || !Character.isLowSurrogate(text.charAt(i)))return false;
                bytes+=4;
            } else if(Character.isLowSurrogate(c))return false;
            else bytes+=c<128?1:c<2048?2:3;
            if(bytes>maximum)return false;
        } return true;
    }
    private static boolean tracePath(List<String> path,boolean empty) { return tracePath(path,empty,true); }
    private static boolean tracePath(List<String> path,boolean empty,boolean unique) {
        if(path==null || path.size()>32 || !empty && path.isEmpty())return false;
        for(int i=0;i<path.size();i++) {
            String id=path.get(i);
            if(id==null || id.length()<1 || id.length()>32)return false;
            for(int j=0;j<id.length();j++) {
                char c=id.charAt(j); if(!(c>='A'&&c<='Z'||c>='a'&&c<='z'||c>='0'&&c<='9'||c=='_'||c=='-'))return false;
            }
            if(unique)for(int j=0;j<i;j++)if(id.equals(path.get(j)))return false;
        } return true;
    }
    private final class LocalTrace implements TraceTicket {
        final UUID stream,id; final long began;
        final AtomicBoolean cancelled=new AtomicBoolean(); volatile TraceOutcome outcome;
        LocalTrace(UUID stream,UUID id,long began){this.stream=stream;this.id=id;this.began=began;}
        public UUID traceId(){return id;}
        public Optional<TraceOutcome> result(){return Optional.ofNullable(outcome);}
        public void cancel(){if(outcome==null && cancelled.compareAndSet(false,true))wake();}
    }
    private static final class TraceContext {
        final UUID id,stream; final long began; final Neighbor.Session incoming;
        final LocalTrace local; final List<String> prefix;
        UUID epoch; String origin; Neighbor.Session upstream;
        boolean terminal; ControlMessage response;
        TraceContext(UUID id,UUID stream,long began,Neighbor.Session incoming,LocalTrace local,List<String> prefix) {
            this.id=id;this.stream=stream;this.began=began;this.incoming=incoming;this.local=local;this.prefix=prefix;
        }
    }
    private TraceOutcome traceValue(LocalTrace ticket,UUID epoch,TraceState state,ErrorCode code,
            String detail,List<String> path,long at) {
        return new TraceOutcome(ticket.stream,ticket.id,Optional.ofNullable(epoch),state,
                Optional.ofNullable(code),detail,path,Math.max(0,at-ticket.began));
    }
    private void admitTrace(LocalTrace ticket) {
        requireOwner();
        if(stopping.get())return; // onStop/cleanup owns every reserved local ticket
        var c=new TraceContext(ticket.id,ticket.stream,ticket.began,null,ticket,List.of(config.nodeId()));
        traces.put(c.id,c);traceOrder.add(c);traceSweep=traceOrder.size();
        long at=now();
        if(ticket.cancelled.get())finishTrace(c,TraceState.CANCELLED,null,"CANCELLED",c.prefix,at);
        else if(at-c.began>=TRACE_TIMEOUT.toNanos())finishTrace(c,TraceState.TIMEOUT,ErrorCode.TIMEOUT,"TIMEOUT",c.prefix,at);
        else forwardTrace(c,TRACE_INITIAL_TTL-1);
        drive();
    }
    private void finishTrace(TraceContext c,TraceState state,ErrorCode code,String detail,List<String> path,long at) {
        if(c.terminal)return;c.terminal=true;
        if(c.local!=null)c.local.outcome=traceValue(c.local,c.epoch,state,code,detail,path,at);
        else if(c.incoming!=null && current(c.incoming) && at-c.began<TRACE_TIMEOUT.toNanos()) {
            c.response=new ControlMessage(state==TraceState.SUCCESS?MessageType.TRACE_RESULT:MessageType.ERROR,
                    c.id,config.nodeId(),instance,state==TraceState.SUCCESS?
                    new ControlMessage.TraceResult(c.stream,c.id,path):
                    new ControlMessage.ErrorPayload(code==null?ErrorCode.NO_ROUTE:code,15,detail));
        }
        traceSweep=traceOrder.size();
    }
    private void forwardTrace(TraceContext c,int remaining) {
        long at=now();applied(root.expire());
        if(at-c.began>=TRACE_TIMEOUT.toNanos()){finishTrace(c,TraceState.TIMEOUT,ErrorCode.TIMEOUT,"TIMEOUT",c.prefix,at);return;}
        var row=rows.get(c.stream);
        if(row==null){finishTrace(c,TraceState.ERROR,ErrorCode.UNKNOWN_STREAM,"UNKNOWN_STREAM",c.prefix,at);return;}
        refresh(row);var route=row.route;
        if(c.epoch!=null && !route.serverEpoch().equals(Optional.of(c.epoch))) {
            finishTrace(c,TraceState.ERROR,ErrorCode.PATH_CHANGED,"PATH_CHANGED",c.prefix,at);return;
        }
        c.epoch=route.serverEpoch().orElse(null);c.origin=route.serverId();
        if(route.localSource()){finishTrace(c,TraceState.SUCCESS,null,"",c.prefix,at);return;}
        if(route.selectedCandidate().isEmpty()){finishTrace(c,TraceState.ERROR,ErrorCode.NO_ROUTE,"NO_ROUTE",c.prefix,at);return;}
        if(remaining==0){finishTrace(c,TraceState.ERROR,ErrorCode.TIMEOUT,"TTL_EXHAUSTED",c.prefix,at);return;}
        var selected=route.selectedCandidate().orElseThrow();
        var session=sessions.get(selected.upstream().nodeId());
        if(session==null || !key(session).equals(selected.upstream())){finishTrace(c,TraceState.ERROR,ErrorCode.NO_ROUTE,"NO_ROUTE",c.prefix,at);return;}
        if(c.prefix.contains(session.nodeId())){finishTrace(c,TraceState.ERROR,ErrorCode.LOOP_DETECTED,"LOOP_DETECTED",c.prefix,at);return;}
        c.upstream=session;
        var request=new ControlMessage(MessageType.ROUTE_TRACE,c.id,config.nodeId(),instance,
                new ControlMessage.Trace(c.stream,c.epoch,c.id,remaining,c.prefix));
        if(!sendTrace(c,session,request,true)) {
            long failedAt=now();
            if(failedAt-c.began>=TRACE_TIMEOUT.toNanos())finishTrace(c,TraceState.TIMEOUT,ErrorCode.TIMEOUT,"TIMEOUT",c.prefix,failedAt);
            else finishTrace(c,TraceState.RESOURCE_LIMIT,ErrorCode.RESOURCE_LIMIT,"TRACE_SEND_CAPACITY",c.prefix,failedAt);
        }
    }
    private void onTrace(Neighbor.Session session,ControlMessage message) {
        requireOwner();long at=now();
        if(message.type()==MessageType.ROUTE_TRACE && message.payload() instanceof ControlMessage.Trace p) {
            // Bound before copying, allocating a context, or responding from unsafe content.
            if(p.streamId()==null || p.serverEpoch()==null || p.traceId()==null || !p.traceId().equals(message.requestId())
                    || p.ttl()<0 || p.ttl()>255 || !tracePath(p.visited(),false,false)
                    || !p.visited().getLast().equals(session.nodeId()))return;
            var duplicate=traces.get(p.traceId());
            if(duplicate!=null)return; // includes protected terminal contexts; never refresh deadline
            if(p.ttl()==0){traceError(session,p.traceId(),ErrorCode.TIMEOUT,"TTL_EXHAUSTED");return;}
            if(!tracePath(p.visited(),false) || p.visited().contains(config.nodeId())){traceError(session,p.traceId(),ErrorCode.LOOP_DETECTED,"LOOP_DETECTED");return;}
            if(p.visited().size()==32){traceError(session,p.traceId(),ErrorCode.RESOURCE_LIMIT,"TRACE_PATH_LIMIT");return;}
            synchronized(resources) {
                long peer=traces.values().stream().filter(c->session.equals(c.incoming)).count();
                if(traceReservations>=traceLimit || peer>=4){traceError(session,p.traceId(),ErrorCode.RESOURCE_LIMIT,"TRACE_CAPACITY");return;}
                traceReservations++;
            }
            var prefix=new ArrayList<String>(p.visited().size()+1);prefix.addAll(p.visited());prefix.add(config.nodeId());
            var c=new TraceContext(p.traceId(),p.streamId(),at,session,null,List.copyOf(prefix));c.epoch=p.serverEpoch();
            traces.put(c.id,c);traceOrder.add(c);traceSweep=traceOrder.size();forwardTrace(c,p.ttl()-1);
        } else {
            var c=traces.get(message.requestId());
            if(c==null || c.terminal || c.upstream==null || !c.upstream.equals(session))return;
            if(at-c.began>=TRACE_TIMEOUT.toNanos()){finishTrace(c,TraceState.TIMEOUT,ErrorCode.TIMEOUT,"TIMEOUT",c.prefix,at);return;}
            if(message.payload() instanceof ControlMessage.TraceResult p && message.type()==MessageType.TRACE_RESULT) {
                if(!c.id.equals(p.traceId()) || !c.stream.equals(p.streamId()) || !tracePath(p.visited(),false)
                        || p.visited().size()<=c.prefix.size() || !p.visited().subList(0,c.prefix.size()).equals(c.prefix)
                        || !p.visited().get(c.prefix.size()).equals(c.upstream.nodeId())
                        || !p.visited().getLast().equals(c.origin))return;
                var row=rows.get(c.stream);
                if(row==null || !row.route.serverEpoch().equals(Optional.ofNullable(c.epoch)))
                    finishTrace(c,TraceState.ERROR,ErrorCode.PATH_CHANGED,"PATH_CHANGED",c.prefix,at);
                else finishTrace(c,TraceState.SUCCESS,null,"",p.visited(),at);
            } else if(message.type()==MessageType.ERROR && message.payload() instanceof ControlMessage.ErrorPayload p
                    && p.relatedType()==15 && p.code()!=null && traceText(p.detail(),256))
                finishTrace(c,p.code()==ErrorCode.TIMEOUT?TraceState.TIMEOUT:TraceState.ERROR,p.code(),p.detail(),c.prefix,at);
        }
    }
    private final class TraceSend {
        final TraceContext context; final Neighbor.Session session; final boolean request;
        Connection.SendResult result;Throwable error;boolean ready,callbackDone,continuationDone;
        TraceSend(TraceContext c,Neighbor.Session s,boolean request){context=c;session=s;this.request=request;}
        void complete(Connection.SendResult value,Throwable error) {
            synchronized(resources){this.result=value;this.error=error;ready=true;}
            try{if(!stopping.get())wake();}
            finally{boolean freed;synchronized(resources){callbackDone=true;freed=releaseTraceSend(this);}if(freed&&!stopping.get())wake();}
        }
    }
    private boolean releaseTraceSend(TraceSend send) {
        if(send.callbackDone && send.continuationDone && traceSends.remove(send)){resources.notifyAll();return true;}return false;
    }
    private boolean sendTrace(TraceContext c,Neighbor.Session session,ControlMessage message,boolean request) {
        requireOwner();
        if(stopping.get() || !current(session) || c!=null && now()-c.began>=TRACE_TIMEOUT.toNanos())return false;
        final TraceSend send;
        synchronized(resources) {
            if(traceSends.size()>=traceLimit || c!=null && traceSends.stream().filter(t->t.context==c).count()>=2)return false;
            send=new TraceSend(c,session,request);traceSends.add(send);
        }
        boolean submitted=false;
        try {
            // Reservation/validation can consume time; failures before the port own no callback.
            long at=now();
            boolean valid=!stopping.get() && current(session) && (c==null || at-c.began<TRACE_TIMEOUT.toNanos());
            if(valid && request) {
                applied(root.expire());var row=rows.get(c.stream);
                valid=row!=null && row.route.serverEpoch().equals(Optional.ofNullable(c.epoch))
                        && row.route.knownUpstream().equals(Optional.of(key(session)));
                if(!valid)finishTrace(c,TraceState.ERROR,ErrorCode.PATH_CHANGED,"PATH_CHANGED",c.prefix,at);
            }
            if(!valid)return false;
            submitted=true;
            try{Objects.requireNonNull(control.send(session,message)).whenComplete(send::complete);}
            catch(RuntimeException failure){send.complete(null,failure);}
            return true;
        } finally {
            if(!submitted)synchronized(resources){traceSends.remove(send);resources.notifyAll();}
        }
    }
    private void traceError(Neighbor.Session s,UUID id,ErrorCode error,String detail) {
        sendTrace(null,s,new ControlMessage(MessageType.ERROR,id,config.nodeId(),instance,
                new ControlMessage.ErrorPayload(error,15,detail)),false);
    }
    private void traceDrive() {
        requireOwner();
        List<TraceSend> completed;
        synchronized(resources){completed=traceSends.stream().filter(t->t.ready&&!t.continuationDone).limit(16).toList();}
        for(var send:completed) {
            try {
                var c=send.context;
                if(c!=null && !c.terminal && send.request && traces.get(c.id)==c
                        && (send.error!=null || send.result!=Connection.SendResult.ACCEPTED))
                {
                    long at=now();
                    if(at-c.began>=TRACE_TIMEOUT.toNanos())finishTrace(c,TraceState.TIMEOUT,ErrorCode.TIMEOUT,"TIMEOUT",c.prefix,at);
                    else finishTrace(c,TraceState.ERROR,ErrorCode.NO_ROUTE,"SEND_"+(send.result==null?"FAILED":send.result),c.prefix,at);
                }
            }finally{synchronized(resources){send.continuationDone=true;releaseTraceSend(send);}}
        }
        synchronized(resources){if(traceSends.stream().anyMatch(t->t.ready&&!t.continuationDone))wake();}
        int count=Math.min(16-completed.size(),traceSweep);
        while(count-->0 && !traceOrder.isEmpty()) {
            var c=traceOrder.removeFirst();traceSweep--;
            long at=now();
            if(!c.terminal) {
                if(c.local!=null && c.local.cancelled.get())finishTrace(c,TraceState.CANCELLED,null,"CANCELLED",c.prefix,at);
                else if(at-c.began>=TRACE_TIMEOUT.toNanos())finishTrace(c,TraceState.TIMEOUT,ErrorCode.TIMEOUT,"TIMEOUT",c.prefix,at);
                else if(c.upstream!=null && !current(c.upstream))finishTrace(c,TraceState.ERROR,ErrorCode.NO_ROUTE,"SESSION_CLOSED",c.prefix,at);
                else if(c.incoming!=null && !current(c.incoming))finishTrace(c,TraceState.CLOSED,null,"SESSION_CLOSED",c.prefix,at);
                else if(c.epoch!=null && rows.containsKey(c.stream) && !rows.get(c.stream).route.serverEpoch().equals(Optional.of(c.epoch)))
                    finishTrace(c,TraceState.ERROR,ErrorCode.PATH_CHANGED,"PATH_CHANGED",c.prefix,at);
            }
            if(c.response!=null) {
                if(!current(c.incoming) || at-c.began>=TRACE_TIMEOUT.toNanos())c.response=null;
                else if(sendTrace(c,c.incoming,c.response,false))c.response=null;
            }
            if(at-c.began>=TRACE_TIMEOUT.toNanos()) {
                traces.remove(c.id);
                synchronized(resources){traceReservations--;if(c.local!=null)localTraces.remove(c.local);}
            }else traceOrder.addLast(c);
        }
        if(traceSweep>0)wake();
    }
    record TraceLoad(int contexts,int reservations,int locals,int sends,int responseIntents,int timers) { }
    TraceLoad traceLoad() {
        requireOwner();
        synchronized(resources) { return new TraceLoad(traces.size(),traceReservations,localTraces.size(),traceSends.size(),
                (int)traces.values().stream().filter(c->c.response!=null).count(),scheduler.physicalJobs()); }
    }
    private void closeTraces() {
        long at=clock.getAsLong();
        for(var c:traces.values())if(!c.terminal)finishTrace(c,TraceState.CLOSED,null,"CLOSED",c.prefix,at);
        traces.clear();traceOrder.clear();traceSweep=0;
        synchronized(resources) {
            for(var ticket:localTraces)if(ticket.outcome==null)ticket.outcome=traceValue(ticket,null,TraceState.CLOSED,null,"CLOSED",List.of(),at);
            localTraces.clear();traceReservations=0;
            for(var send:List.copyOf(traceSends)){send.continuationDone=true;releaseTraceSend(send);}
        }
    }
    private void note(Reason reason,boolean fatal){
        lastReason=reason;if(failures==Long.MAX_VALUE)countersSaturated=true;else failures++;
        if(fatal)requestClose();
        try{observer.onEvent(new Event(reason,fatal));}catch(RuntimeException e){requestClose();}
    }
    public Snapshot snapshot(){return view;}
    private void ownerStopped(){firstStop.compareAndSet(null,System.nanoTime());stopping.set(true);scheduler.requestClose();lastReason=Reason.OWNER_REJECTED;cleanup();}
    public CompletionStage<Void> requestClose(){
        firstStop.compareAndSet(null,System.nanoTime());if(stopping.compareAndSet(false,true)){
            scheduler.requestClose();if(owner.isOwnerThread())cleanup();else owner.offer(this::cleanup);
        }return cleaned.minimalCompletionStage();
    }
    private void cleanup(){
        requireOwner();if(cleaned.isDone())return;stopping.set(true);scheduler.requestClose();if(alarm!=null)alarm.cancel();alarm=null;
        closeTraces();
        rows.clear();streamIndex.clear();originCounts.clear();epochs.clear();sessions.clear();peerIndex.clear();costs.clear();ledgers.clear();cache.clear();quotas.clear();quotaSweep.clear();pending=0;scanRemaining=0;phase=Phase.STOPPED;
        synchronized(resources){for(var a:List.copyOf(admissions)){a.continuationDone=true;release(a);}}
        publish(clock.getAsLong());cleaned.complete(null);
        if(registration!=null){registration.remove();registration=null;}
    }
    private static long nanos(Duration d){Objects.requireNonNull(d);if(d.isNegative())throw new IllegalArgumentException("Negative budget");try{return d.toNanos();}catch(ArithmeticException e){throw new IllegalArgumentException("Budget overflow",e);}}
    private static long left(long budget,long began){return Math.max(0,budget-(System.nanoTime()-began));}
    public boolean awaitTermination(Duration timeout){
        long budget=nanos(timeout),began=System.nanoTime();if(owner.isOwnerThread()||!stopping.get())return false;
        budget=Math.min(budget,left(shutdownNanos,firstStop.get()));
        try{cleaned.get(left(budget,began),TimeUnit.NANOSECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();return false;}catch(ExecutionException|TimeoutException e){return false;}
        if(!scheduler.awaitTermination(Duration.ofNanos(left(budget,began))))return false;
        synchronized(resources){while(!admissions.isEmpty() || !traceSends.isEmpty()){
            long rest=left(budget,began);if(rest==0)return false;
            try{TimeUnit.NANOSECONDS.timedWait(resources,rest);}catch(InterruptedException e){Thread.currentThread().interrupt();return false;}
        }}return true;
    }
    public void close(){requestClose();if(!owner.isOwnerThread()&&!awaitTermination(Duration.ofNanos(left(shutdownNanos,firstStop.get()))))throw new IllegalStateException("Routing physical shutdown deadline");}

    /** One timer worker, at most two physical jobs. Cancellation never releases a running job. */
    private static final class Timers implements Scheduler {
        private final ScheduledThreadPoolExecutor executor=new ScheduledThreadPoolExecutor(1,Thread.ofPlatform().daemon(true).name("ott-routing-timer").factory());
        private final Set<Job> jobs=new HashSet<>();private boolean stopped;
        Timers(){executor.setRemoveOnCancelPolicy(true);}
        private final class Job implements Ticket,Runnable {
            final Runnable task;final AtomicInteger state=new AtomicInteger();ScheduledFuture<?> future;
            Job(Runnable task){this.task=task;}
            public void run(){if(!state.compareAndSet(0,1))return;try{task.run();}finally{state.set(2);synchronized(Timers.this){jobs.remove(this);}}}
            public void cancel(){if(state.compareAndSet(0,3)){synchronized(Timers.this){if(future!=null)future.cancel(false);jobs.remove(this);}}}
        }
        public synchronized Ticket schedule(Runnable task,long delay){
            if(delay<0||stopped||jobs.size()>=2)throw new RejectedExecutionException("Routing timer capacity");
            var job=new Job(task);jobs.add(job);try{job.future=executor.schedule(job,delay,TimeUnit.NANOSECONDS);}catch(RuntimeException e){jobs.remove(job);throw e;}return job;
        }
        public synchronized void requestClose(){if(stopped)return;stopped=true;for(var job:List.copyOf(jobs))job.cancel();executor.shutdown();}
        public boolean awaitTermination(Duration budget){try{return executor.awaitTermination(nanos(budget),TimeUnit.NANOSECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();return false;}}
        public synchronized int physicalJobs(){return jobs.size();}
    }
}
