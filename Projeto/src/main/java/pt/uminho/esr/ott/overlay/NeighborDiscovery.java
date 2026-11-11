package pt.uminho.esr.ott.overlay;

import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.Connection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/** Bounded learning only. All mutable fields, except tickPending, belong to the node's serial owner. */
public final class NeighborDiscovery {
    static final long TICK=100_000_000L, QUERY_BUDGET=2_000_000_000L, REFRESH=5_000_000_000L,
            GLOBAL_INTERVAL=250_000_000L, INBOUND_INTERVAL=1_000_000_000L, COOLDOWN=30_000_000_000L;
    enum Issue { BACKPRESSURE, TIMEOUT, STALE, INVALID_CONTENT, RATE_LIMIT, REMOTE_ERROR, WRITE_FAILED,
        GENERATION_EXHAUSTED, CACHE_REJECTION }
    interface Host {
        boolean post(Runnable task);
        Map<String,Neighbor.Session> active();
        Connection.ExtensionEnqueue enqueue(Neighbor.Session session,ControlMessage message);
        void diagnostic(Issue issue);
    }
    private static final class Query {
        final UUID id=UUID.randomUUID(); final long started;
        Query(long started){this.started=started;}
    }
    private static final class Peer {
        final Neighbor.Session session; final UUID hello;
        final CandidateCache.SourceKey source;
        long generation, watermark, eligibleAt, incomingEligibleAt;
        Query query;
        Peer(Neighbor.Session session,UUID hello,long now) {
            this.session=session;this.hello=hello;eligibleAt=incomingEligibleAt=now;
            source=new CandidateCache.SourceKey(session.nodeId(),session.instanceId(),session.connectionId(),hello);
        }
    }
    private static final class Response {
        final Peer peer;final UUID request;final long observed;
        boolean logical=true;
        Response(Peer peer,UUID request,long observed){this.peer=peer;this.request=request;this.observed=observed;}
    }
    private final String localId;
    private final UUID instance;
    private final Host host;
    private final LongSupplier clock;
    private final CandidateCache cache;
    private final Map<String,Peer> peers=new LinkedHashMap<>();
    // A response remains here until the physical confirmation, even if logically expired.
    private final Set<Response> responses=new HashSet<>();
    private final EnumMap<Issue,Long> diagnosticAt=new EnumMap<>(Issue.class);
    private final EnumMap<Issue,Long> diagnosticCounts=new EnumMap<>(Issue.class);
    private final AtomicBoolean tickPending=new AtomicBoolean();
    private NeighborManager.Ticket tick;
    private boolean closed, globalSent;
    private long globalAt;
    private int cursor;

    NeighborDiscovery(NodeConfiguration configuration, UUID instance, Map<String,ControlMessage.Endpoint> configured,
            LongSupplier clock,Host host) {
        this.localId=configuration.nodeId();this.instance=Objects.requireNonNull(instance);
        this.clock=Objects.requireNonNull(clock);this.host=Objects.requireNonNull(host);
        cache=new CandidateCache(configuration,configured,rejection->diagnostic(Issue.CACHE_REJECTION));
    }
    void start(NeighborManager.Deadlines timers) {
        tick=timers.periodic(()->{
            if(tickPending.compareAndSet(false,true)) {
                if(!host.post(()->{tickPending.set(false);if(!closed)maintain();}))tickPending.set(false);
            }
        },TICK);
    }
    void up(Neighbor.Session session,UUID hello) {
        if(closed)return;
        var previous=peers.remove(session.nodeId());
        if(previous!=null)retire(previous);
        if(peers.size()>=63)throw new IllegalStateException("Discovery source cap");
        peers.put(session.nodeId(),new Peer(session,hello,clock.getAsLong()));
    }
    void down(Neighbor.Session session) {
        var p=peers.get(session.nodeId());
        if(p!=null && p.session.equals(session)){peers.remove(session.nodeId());retire(p);}
    }
    private void retire(Peer p) {cache.retire(p.source);p.query=null;for(var r:responses)if(r.peer==p)r.logical=false;}
    private boolean current(Peer p){return !closed && peers.get(p.session.nodeId())==p;}
    private Peer peer(Neighbor.Session session) {var p=peers.get(session.nodeId());return p!=null&&p.session.equals(session)?p:null;}
    private boolean identity(Peer p,String sender,UUID senderInstance,String recipient,UUID recipientInstance) {
        return p!=null && p.session.nodeId().equals(sender) && p.session.instanceId().equals(senderInstance)
                && localId.equals(recipient) && instance.equals(recipientInstance);
    }
    boolean receive(Neighbor.Session session,ControlMessage message,long observedAt) {
        var p=peer(session);
        if(!(message.payload() instanceof DiscoveryMessage data)
                || !identity(p,message.senderId(),message.senderInstance(),data.recipientId(),data.recipientInstance()))return false;
        // A prior HELLO token is stale correlation, not an incompatible identity.
        // Consume it without touching the current query or either direction's budget.
        if(!p.hello.equals(data.helloRequestId())){diagnostic(Issue.STALE);return true;}
        if(message.type()==MessageType.DISCOVERY_QUERY && data instanceof DiscoveryMessage.Query)respond(p,message,observedAt);
        else if(message.type()==MessageType.DISCOVERY_SNAPSHOT && data instanceof DiscoveryMessage.Snapshot snapshot)learn(p,message,snapshot);
        else return false;
        return true;
    }
    boolean rejected(Neighbor.Session session,MessageCodec.RejectedExtraContent rejection) {
        var p=peer(session);var e=rejection.envelope();
        if(!identity(p,e.senderId(),e.senderInstance(),e.recipientId(),e.recipientInstance()))return false;
        if(!p.hello.equals(e.helloRequestId())){diagnostic(Issue.STALE);return true;}
        diagnostic(Issue.INVALID_CONTENT);
        if(e.type()==MessageType.DISCOVERY_SNAPSHOT && p.query!=null && p.query.id.equals(e.queryId()))fail(p,Issue.INVALID_CONTENT);
        else if(e.type()==MessageType.DISCOVERY_QUERY)p.incomingEligibleAt=clock.getAsLong()+COOLDOWN;
        return true;
    }
    void error(Neighbor.Session session,ControlMessage message) {
        var p=peer(session);
        if(p==null || !(message.payload() instanceof ControlMessage.ErrorPayload error))return;
        if(error.relatedType()==18 && p.query!=null && p.query.id.equals(message.requestId()))fail(p,Issue.REMOTE_ERROR);
        if(error.relatedType()==19)for(var r:responses)if(r.peer==p&&r.request.equals(message.requestId())) {
            r.logical=false;p.incomingEligibleAt=clock.getAsLong()+COOLDOWN;diagnostic(Issue.REMOTE_ERROR);
        }
    }
    private void learn(Peer p,ControlMessage message,DiscoveryMessage.Snapshot snapshot) {
        long now=clock.getAsLong();var query=p.query;
        if(query==null || !query.id.equals(message.requestId())){diagnostic(Issue.STALE);return;}
        if(snapshot.generation()<=p.watermark){diagnostic(Issue.STALE);return;}
        if(now-query.started>=QUERY_BUDGET){fail(p,Issue.TIMEOUT);return;}
        try{DiscoveryCodec.validateSnapshot(message.senderId(),snapshot.directNeighbors());}
        catch(java.io.IOException|IllegalArgumentException error){fail(p,Issue.INVALID_CONTENT);return;}
        cache.accept(p.source,snapshot.generation(),snapshot.directNeighbors(),query.started,now);
        p.watermark=snapshot.generation();p.query=null;p.eligibleAt=now+REFRESH;
    }
    private void respond(Peer p,ControlMessage query,long observedAt) {
        long now=clock.getAsLong();
        if(now-observedAt>=QUERY_BUDGET){p.incomingEligibleAt=now+COOLDOWN;diagnostic(Issue.TIMEOUT);return;}
        if(now-p.incomingEligibleAt<0){diagnostic(Issue.RATE_LIMIT);return;}
        p.incomingEligibleAt=now+INBOUND_INTERVAL;
        if(responses.stream().filter(r->r.logical).count()>=2 || responses.size()>=4) {
            p.incomingEligibleAt=now+COOLDOWN;diagnostic(Issue.BACKPRESSURE);return;
        }
        if(p.generation==Long.MAX_VALUE){p.incomingEligibleAt=now+COOLDOWN;diagnostic(Issue.GENERATION_EXHAUSTED);return;}
        var endpoints=host.active().values().stream().map(Neighbor.Session::endpoint).toList();
        var payload=new DiscoveryMessage.Snapshot(p.hello,p.session.nodeId(),p.session.instanceId(),p.generation+1,endpoints);
        var response=new Response(p,query.requestId(),observedAt);
        var write=host.enqueue(p.session,new ControlMessage(MessageType.DISCOVERY_SNAPSHOT,query.requestId(),localId,instance,payload));
        if(write.status()!=Connection.ExtensionStatus.ACCEPTED){p.incomingEligibleAt=now+COOLDOWN;diagnostic(Issue.BACKPRESSURE);return;}
        p.generation++;responses.add(response);
        write.written().whenComplete((unused,error)->host.post(()->{
            responses.remove(response);
            if(!current(p))return;
            if(error!=null || clock.getAsLong()-observedAt>=QUERY_BUDGET) {
                p.incomingEligibleAt=clock.getAsLong()+COOLDOWN;diagnostic(error==null?Issue.TIMEOUT:Issue.WRITE_FAILED);
            }
        }));
    }
    private void maintain() {
        long now=clock.getAsLong();cache.maintenance(now);
        for(var p:peers.values())if(p.query!=null && now-p.query.started>=QUERY_BUDGET)fail(p,Issue.TIMEOUT);
        for(var r:responses)if(r.logical && now-r.observed>=QUERY_BUDGET) {
            r.logical=false;if(current(r.peer))r.peer.incomingEligibleAt=now+COOLDOWN;diagnostic(Issue.TIMEOUT);
        }
        if(globalSent && now-globalAt<GLOBAL_INTERVAL || peers.values().stream().filter(p->p.query!=null).count()>=2)return;
        var eligible=new ArrayList<>(peers.values());int count=eligible.size();
        for(int i=0;i<count;i++) {
            int index=(cursor+i)%count;var p=eligible.get(index);
            if(p.query!=null || now-p.eligibleAt<0)continue;
            var query=new Query(now);p.query=query;
            var message=new ControlMessage(MessageType.DISCOVERY_QUERY,query.id,localId,instance,
                    new DiscoveryMessage.Query(p.hello,p.session.nodeId(),p.session.instanceId()));
            var write=host.enqueue(p.session,message);
            cursor=(index+1)%count;
            if(write.status()!=Connection.ExtensionStatus.ACCEPTED){fail(p,Issue.BACKPRESSURE);return;}
            globalSent=true;globalAt=now;
            write.written().whenComplete((unused,error)->host.post(()->{
                if(current(p)&&p.query==query && error!=null)fail(p,Issue.WRITE_FAILED);
            }));
            return;
        }
    }
    private void fail(Peer p,Issue issue){p.query=null;p.eligibleAt=clock.getAsLong()+COOLDOWN;diagnostic(issue);}
    private void diagnostic(Issue issue) {
        diagnosticCounts.compute(issue,(key,count)->count==null?1:count==Long.MAX_VALUE?count:count+1);
        long now=clock.getAsLong();var previous=diagnosticAt.get(issue);
        if(previous==null || now-previous>=REFRESH){diagnosticAt.put(issue,now);host.diagnostic(issue);}
    }
    List<CandidateCache.CandidateView> snapshot(){return cache.snapshot(clock.getAsLong());}
    void close() {
        if(closed)return;closed=true;
        if(tick!=null){tick.cancel();tick=null;}
        for(var p:peers.values())retire(p);
        peers.clear();responses.clear();
    }
}
