package pt.uminho.esr.ott.routing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.monitoring.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.streaming.*;
import pt.uminho.esr.ott.transport.Connection;

@Timeout(15)
class RoutingServiceTest {
    static final UUID STREAM=new UUID(1,1), OTHER=new UUID(1,2), EPOCH=new UUID(2,1);
    static final UUID LOCAL=new UUID(3,1);
    static Neighbor.Session session(String id) {
        UUID instance=UUID.randomUUID();
        return new Neighbor.Session(id,instance,UUID.randomUUID(),new ControlMessage.Endpoint(id,instance,"127.0.0.1",19001,19101,NodeRole.RELAY));
    }
    static NodeConfiguration config(String id, boolean source, int cache, int events, int flood) {
        var r=RuntimeConfiguration.defaults();
        var runtime=new RuntimeConfiguration(r.control(),new RuntimeConfiguration.Overlay(r.overlay().helloTimeout(),r.overlay().retryInitial(),r.overlay().retryMax(),10,events),r.monitoring(),
            new RuntimeConfiguration.Routing(Duration.ofMillis(2000),Duration.ofMillis(8000),cache,Duration.ofMillis(16000),flood,r.routing().activateTimeout(),3,20,3,r.routing().switchMinDwell()),r.subscription(),r.video(),r.source());
        UUID first=id.equals("S")?STREAM:UUID.nameUUIDFromBytes((id+"0").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        UUID second=id.equals("S")?OTHER:UUID.nameUUIDFromBytes((id+"1").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var streams=source?List.of(new StreamConfiguration(Path.of("s"),first,id,"Video A",Path.of("absent.mjpeg"),25,false),new StreamConfiguration(Path.of("s"),second,id,"Video B",Path.of("absent2"),25,false)):List.<StreamConfiguration>of();
        return new NodeConfiguration(Path.of("n"),id,source?NodeRole.SERVER:NodeRole.RELAY,"127.0.0.1","127.0.0.1",19001,19101,Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1",19000)),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),streams,runtime);
    }
    static final class Owner implements NeighborManager.SerialOwner {
        final ArrayDeque<Runnable> queue=new ArrayDeque<>(); final List<Runnable> hooks=new ArrayList<>();
        final int capacity; boolean inside,stopped,reject,inline; Throwable failure;
        Owner(int capacity){this.capacity=capacity;}
        public int capacity(){return capacity;} public boolean isOwnerThread(){return inside;}
        public boolean offer(Runnable r){if(stopped)return false;if(reject||queue.size()==capacity){fail(new IllegalStateException("full"));return false;}queue.add(r);if(inline)drain();return true;}
        public NeighborManager.Registration onStop(Runnable r){hooks.add(r);return ()->hooks.remove(r);}
        public void fail(Throwable t){failure=t;requestClose();}
        public void requestClose(){if(stopped)return;stopped=true;queue.clear();run(()->{for(var h:List.copyOf(hooks))h.run();});}
        public boolean awaitTermination(Duration d){return stopped;} public void close(){requestClose();}
        void run(Runnable r){boolean old=inside;inside=true;try{r.run();}finally{inside=old;}}
        void drain(){int n=0;while(!queue.isEmpty()){assertTrue(n++<10000,"owner busy loop");run(queue.remove());}}
    }
    static final class Timers implements RoutingService.Scheduler {
        final AtomicLong clock; final List<Job> jobs=new ArrayList<>(); boolean stopped,pin;
        final class Job implements RoutingService.Ticket { final Runnable r;final long at;boolean cancel;
            Job(Runnable r,long d){this.r=r;at=clock.get()+d;} public void cancel(){cancel=true;if(!pin)jobs.remove(this);} }
        Timers(AtomicLong c){clock=c;}
        public RoutingService.Ticket schedule(Runnable r,long d){if(stopped||jobs.size()>=2)throw new RejectedExecutionException();var j=new Job(r,d);jobs.add(j);return j;}
        public void requestClose(){stopped=true;for(var j:List.copyOf(jobs))j.cancel();}
        public boolean awaitTermination(Duration d){return jobs.isEmpty();} public int physicalJobs(){return jobs.size();}
        boolean fire(){for(var j:List.copyOf(jobs))if(!j.cancel&&clock.get()-j.at>=0){jobs.remove(j);j.r.run();return true;}return false;}
    }
    record Sent(Neighbor.Session peer,ControlMessage message,CompletableFuture<Connection.SendResult> result) { }
    static final class Rig implements AutoCloseable {
        final AtomicLong clock=new AtomicLong(); final Owner owner;final Timers timer=new Timers(clock);
        final List<Sent> sent=new ArrayList<>();final List<RoutingService.Event> events=new ArrayList<>(); final RoutingService router;
        BiConsumer<Neighbor.Session,ControlMessage> delivery;
        String overflowPeer; boolean hold; Connection.SendResult next=Connection.SendResult.ACCEPTED;
        Rig(){this("C",false,4096,4096,10,new RoutingTable.Limits(64,256,63,8));}
        Rig(String id,boolean source,int cache,int events,int flood,RoutingTable.Limits limits){
            owner=new Owner(events);router=new RoutingService(config(id,source,cache,events,flood),LOCAL,owner,(p,m)->{
                assertTrue(owner.isOwnerThread());var f=new CompletableFuture<Connection.SendResult>();sent.add(new Sent(p,m,f));if(!hold)f.complete(p.nodeId().equals(overflowPeer)?Connection.SendResult.OVERFLOW:next);if(delivery!=null)delivery.accept(p,m);return f;
            },this.events::add,clock::get,timer,limits);
            owner.run(router::start);owner.drain();
        }
        void act(Runnable r){owner.run(r);owner.drain();}
        void up(Neighbor.Session s){act(()->router.onNeighborEvent(new NeighborManager.NeighborUp(s)));}
        void metric(Neighbor.Session s,long cost){act(()->router.onMetrics(new NetworkMonitor.Updated(new LinkMetrics(s,LinkMetrics.State.MEASURED,OptionalDouble.of(cost),OptionalLong.of(cost),1,false,OptionalLong.of(clock.get()),0))));}
        void ad(Neighbor.Session s,UUID stream,UUID epoch,long seq,long cost,List<String> path,String title,int fps){
            act(()->router.onNeighborEvent(new NeighborManager.Received(s,new ControlMessage(MessageType.STREAM_ANNOUNCE,UUID.randomUUID(),s.nodeId(),s.instanceId(),new ControlMessage.Announcement(new ControlMessage.StreamInfo(stream,path.getFirst(),title,1,fps,epoch),seq,cost,path)))));
        }
        void ad(Neighbor.Session s,long seq,long cost){ad(s,STREAM,EPOCH,seq,cost,List.of("S",s.nodeId()),"Video A",25);}
        void down(Neighbor.Session s){act(()->router.onNeighborEvent(new NeighborManager.NeighborDown(s,NeighborManager.Reason.TRANSPORT)));}
        void tick(long ms){clock.addAndGet(ms*1_000_000);int n=0;while(timer.fire()){assertTrue(n++<10000,"timer busy loop");owner.drain();}owner.drain();}
        Route route(){return router.snapshot().routes().get(STREAM);}
        List<ControlMessage> to(Neighbor.Session s){return sent.stream().filter(x->x.peer().equals(s)).map(Sent::message).toList();}
        public void close(){router.requestClose();owner.drain();for(var s:sent)if(!s.result().isDone())s.result().complete(Connection.SendResult.CLOSED);owner.drain();assertTrue(router.awaitTermination(Duration.ofMillis(50)));}
    }
    @Test void d01SourcePublishesWithoutConsumersAndFreshGenerationWithoutCatchup(){
        try(var r=new Rig("S",true,4096,4096,10,new RoutingTable.Limits(64,256,63,8))){
            var p=session("P");r.up(p);assertEquals(2,r.to(p).size());assertEquals(2,r.router.snapshot().catalog().entries().size());
            var a=(ControlMessage.Announcement)r.to(p).getFirst().payload();assertEquals(0,a.announceSeq());assertEquals(LOCAL,a.stream().serverEpoch());assertEquals(List.of("S"),a.path());
            r.tick(20000);assertEquals(4,r.to(p).size());assertTrue(r.to(p).subList(2,4).stream().allMatch(m->((ControlMessage.Announcement)m.payload()).announceSeq()==1));
            r.act(()->r.router.setLocalAvailable(STREAM,false));assertTrue(r.to(p).stream().anyMatch(m->m.type()==MessageType.STREAM_WITHDRAW));
            r.act(()->r.router.setLocalAvailable(STREAM,true));assertTrue(r.to(p).stream().filter(m->m.payload() instanceof ControlMessage.Announcement a2&&a2.stream().streamId().equals(STREAM)).map(m->((ControlMessage.Announcement)m.payload()).announceSeq()).max(Long::compare).orElseThrow()>1);
        }
    }
    @Test void d02D03DiamondBetterArrivalAndAlternativeDespiteCacheHit(){try(var r=new Rig()){
        var a=session("A");var b=session("B");var d=session("D");for(var s:List.of(a,b,d))r.up(s);r.metric(a,40);r.metric(b,30);
        r.ad(a,1,100);assertEquals(140,r.route().selectedMetric().orElseThrow().costMicros());r.ad(b,1,100);assertEquals(130,r.route().selectedMetric().orElseThrow().costMicros());assertEquals(2,r.route().candidates().size());
        assertEquals(2,r.to(d).size());r.ad(a,1,90);assertEquals(2,r.route().candidates().size());
    }}
    @Test void d06UnmeasuredAndSuspectPreserveTtl(){try(var r=new Rig()){
        var a=session("A");r.up(a);r.ad(a,1,100);assertEquals(RouteState.UNAVAILABLE,r.route().state());assertEquals(1,r.router.snapshot().catalog().entries().size());
        long began=r.route().candidates().getFirst().generationStartedAtNanos();r.tick(1000);r.metric(a,40);assertEquals(140,r.route().selectedMetric().orElseThrow().costMicros());assertEquals(began,r.route().selectedCandidate().orElseThrow().generationStartedAtNanos());
        r.act(()->r.router.onMetrics(new NetworkMonitor.Updated(new LinkMetrics(a,LinkMetrics.State.SUSPECT,OptionalDouble.of(40),OptionalLong.of(40),1,false,OptionalLong.of(0),1))));assertEquals(140,r.route().selectedMetric().orElseThrow().costMicros());
    }}
    @Test void d07ConflictingMetadataNeverConsumesSequenceOrHarmsHealthyCandidate(){try(var r=new Rig()){
        var a=session("A");var b=session("B");r.up(a);r.up(b);r.metric(a,10);r.metric(b,10);r.ad(a,1,100);var before=r.route();
        r.ad(b,STREAM,EPOCH,100,50,List.of("S","B"),"wrong",30);assertEquals(before,r.route());assertEquals("Video A",r.router.snapshot().catalog().entries().get(STREAM).info().title());
        r.ad(b,100,50);assertEquals(2,r.route().candidates().size());assertEquals(60,r.route().selectedMetric().orElseThrow().costMicros());
    }}
    @Test void d08ReferenceSurvivesAllExpiryAndCanChangeOnlyOnAcceptedEpoch(){try(var r=new Rig()){
        var a=session("A");r.up(a);r.metric(a,10);r.ad(a,1,100);r.tick(17000);assertTrue(r.router.snapshot().catalog().entries().isEmpty());assertEquals(1,r.router.snapshot().diagnostics().references());
        r.ad(a,STREAM,EPOCH,100,100,List.of("S","A"),"wrong",30);assertTrue(r.router.snapshot().catalog().entries().isEmpty());r.ad(a,100,100);assertEquals("Video A",r.router.snapshot().catalog().entries().get(STREAM).info().title());
        r.ad(a,STREAM,new UUID(2,2),0,100,List.of("S","A"),"New",30);assertEquals("New",r.router.snapshot().catalog().entries().get(STREAM).info().title());r.ad(a,101,100);assertEquals("New",r.router.snapshot().catalog().entries().get(STREAM).info().title());
    }}
    @Test void d09WithdrawalIsSenderScopedAndSourceSeparation(){try(var r=new Rig()){
        var a=session("A");var b=session("B");r.up(a);r.up(b);r.metric(a,10);r.metric(b,20);r.ad(a,7,100);r.ad(b,7,100);
        r.act(()->r.router.onNeighborEvent(new NeighborManager.Received(a,new ControlMessage(MessageType.STREAM_WITHDRAW,UUID.randomUUID(),a.nodeId(),a.instanceId(),new ControlMessage.Withdrawal(STREAM,EPOCH,7)))));
        assertEquals(1,r.route().candidates().size());assertEquals("B",r.route().knownUpstream().orElseThrow().nodeId());
    }}
    @Test void d11OldSessionCannotMutateNewSession(){try(var r=new Rig()){
        var old=session("A");r.up(old);r.metric(old,10);r.ad(old,1,100);var fresh=session("A");r.up(fresh);r.metric(fresh,20);r.ad(fresh,1,100);var before=r.route();r.down(old);r.metric(old,1);r.ad(old,100,1);assertEquals(before,r.route());
    }}
    @Test void d12SlidingWindowPersistsAcrossReconnectAndCountsAttempts(){try(var r=new Rig("S",true,4096,4096,1,new RoutingTable.Limits(64,256,63,8))){
        var p=session("P");r.up(p);assertEquals(2,r.to(p).size());r.down(p);var fresh=session("P");r.up(fresh);assertEquals(0,r.to(fresh).size());r.tick(999);assertEquals(0,r.to(fresh).size());r.tick(1);assertEquals(2,r.to(fresh).size());
    }}
    @Test void d13FullFloodCacheKeepsAcceptedCandidates(){try(var r=new Rig("C",false,1,4096,10,new RoutingTable.Limits(64,256,63,8))){
        var a=session("A");var d=session("D");r.up(a);r.up(d);r.metric(a,10);r.ad(a,1,100);r.ad(a,2,90);assertEquals(2,r.route().selectedCandidate().orElseThrow().announceSeq());assertEquals(1,r.router.snapshot().diagnostics().cacheEntries());assertEquals(1,r.to(d).size());
    }}
    @Test void d15BlockedPhysicalTicketSurvivesRetirementAndShutdown(){
        var r=new Rig("S",true,4096,1,10,new RoutingTable.Limits(64,256,63,8));r.hold=true;var p=session("P");r.up(p);assertEquals(1,r.sent.size());r.down(p);assertEquals(1,r.router.snapshot().diagnostics().sendTickets());
        r.router.requestClose();r.owner.drain();assertFalse(r.router.awaitTermination(Duration.ZERO));r.sent.getFirst().result().complete(Connection.SendResult.ACCEPTED);r.owner.drain();assertTrue(r.router.awaitTermination(Duration.ofMillis(50)));r.close();
    }
    @Test void d16WorseSameGenerationWithdrawsAndWaitsForNewGeneration(){try(var r=new Rig()){
        var a=session("A");var b=session("B");var d=session("D");for(var s:List.of(a,b,d))r.up(s);r.metric(a,10);r.metric(b,50);r.ad(a,7,100);r.ad(b,7,100);r.down(a);
        assertEquals(MessageType.STREAM_WITHDRAW,r.to(d).getLast().type());r.ad(b,7,90);assertEquals(MessageType.STREAM_WITHDRAW,r.to(d).getLast().type());r.ad(b,8,90);assertEquals(8,((ControlMessage.Announcement)r.to(d).getLast().payload()).announceSeq());
    }}
    @Test void d19WithdrawalCannotBeCoalescedAwayByNewGeneration(){try(var r=new Rig("C",false,4096,4096,1,new RoutingTable.Limits(64,256,63,8))){
        var a=session("A");var b=session("B");var d=session("D");for(var s:List.of(a,b,d))r.up(s);r.metric(a,10);r.metric(b,50);r.ad(a,7,100);r.ad(b,7,100);r.down(a);r.ad(b,8,100);assertEquals(1,r.to(d).size());
        r.tick(1000);assertEquals(MessageType.STREAM_WITHDRAW,r.to(d).getLast().type());r.tick(1000);assertEquals(8,((ControlMessage.Announcement)r.to(d).getLast().payload()).announceSeq());
    }}
    @Test void d20LateAcceptedOldEpochIsAccountedWithoutRestoringDomain(){try(var r=new Rig()){
        var a=session("A");var d=session("D");r.up(a);r.up(d);r.metric(a,10);r.hold=true;r.ad(a,7,100);assertEquals(1,r.to(d).size());
        var first=r.sent.stream().filter(s->s.peer().equals(d)).findFirst().orElseThrow();var nextEpoch=new UUID(2,2);r.ad(a,STREAM,nextEpoch,0,100,List.of("S","A"),"New",25);
        first.result().complete(Connection.SendResult.ACCEPTED);r.owner.drain();assertEquals(nextEpoch,r.route().serverEpoch().orElseThrow());assertEquals(MessageType.STREAM_WITHDRAW,r.to(d).getLast().type());
        r.hold=false;for(var s:List.copyOf(r.sent))s.result().complete(Connection.SendResult.ACCEPTED);r.owner.drain();assertEquals(nextEpoch,((ControlMessage.Announcement)r.to(d).getLast().payload()).stream().serverEpoch());
    }}
    @Test void d22ImmutableSnapshotsNeverAuthorizeData(){try(var r=new Rig()){
        var a=session("A");r.up(a);r.metric(a,10);r.ad(a,1,100);var s=r.router.snapshot();assertThrows(UnsupportedOperationException.class,()->s.routes().clear());assertThrows(UnsupportedOperationException.class,()->s.catalog().entries().clear());assertTrue(r.route().activeUpstream().isEmpty());assertTrue(r.route().downstreams().isEmpty());
        assertTrue(s.catalog().visibleAt(s.capturedAtNanos()+8_000_000_000L).entries().isEmpty());assertEquals(1,s.catalog().entries().size());
    }}
    @Test void d24StopBeforeStartAndOwnerCleanupWithoutWake(){var r=new Rig("S",true,4096,4096,10,new RoutingTable.Limits(64,256,63,8));r.owner.reject=true;r.tick(2000);assertTrue(r.owner.stopped);assertEquals(RoutingService.Phase.STOPPED,r.router.snapshot().diagnostics().phase());assertTrue(r.router.awaitTermination(Duration.ofMillis(50)));r.close();}
    @Test void d28BindingSaturationDoesNotEvictReference(){try(var r=new Rig("C",false,4096,4096,10,new RoutingTable.Limits(1,1,2,1))){
        var a=session("A");r.up(a);r.metric(a,10);r.ad(a,1,100);r.ad(a,OTHER,EPOCH,1,100,List.of("S","A"),"Other",25);assertEquals(1,r.router.snapshot().routes().size());assertEquals(1,r.router.snapshot().diagnostics().references());assertEquals("Video A",r.router.snapshot().catalog().entries().get(STREAM).info().title());
    }}
    @Test void d31MetadataUnicodeAndCatalogBoundaries(){
        var s=new StreamInfo(STREAM,"S","😀".repeat(128),1,60,EPOCH);assertEquals(s,StreamInfo.fromWire(s.toWire()));
        assertThrows(IllegalArgumentException.class,()->new StreamInfo(STREAM,"S","\ud800",1,25,EPOCH));assertThrows(IllegalArgumentException.class,()->new StreamInfo(STREAM,"S","a\0b",1,25,EPOCH));assertThrows(IllegalArgumentException.class,()->new StreamInfo(STREAM,"S","x",2,25,EPOCH));
    }
    @Test void d04CyclicDiamondConvergesAndFixedGenerationStopsFlooding(){
        try(var net=new Network()){
            net.add("S",true);net.add("A",false);net.add("B",false);net.add("C",false);
            net.edge("S","A",40);net.edge("S","B",30);net.edge("A","C",100);net.edge("B","C",100);net.edge("A","B",5);
            net.pump();net.tick(2000);net.tick(2000);
            assertEquals(130,net.nodes.get("C").route().selectedMetric().orElseThrow().costMicros());
            assertEquals(2,net.nodes.get("C").router.snapshot().catalog().entries().size());
            int before=net.messages;net.pump();assertEquals(before,net.messages);assertTrue(net.messages<500);
            assertTrue(net.nodes.values().stream().flatMap(r->r.router.snapshot().routes().values().stream()).flatMap(r->r.candidates().stream()).allMatch(c->new HashSet<>(c.path()).size()==c.path().size()));
        }
    }
    @Test void d05TwoOriginsFourStreamsAndWithdrawalIndependence(){
        try(var net=new Network()){
            net.add("S",true);net.add("T",true);net.add("A",false);net.add("C",false);
            net.edge("S","A",10);net.edge("T","A",20);net.edge("A","C",30);net.pump();net.tick(2000);
            var c=net.nodes.get("C");assertEquals(4,c.router.snapshot().catalog().entries().size());
            var source=net.nodes.get("S");source.act(()->{source.router.setLocalAvailable(STREAM,false);source.router.setLocalAvailable(OTHER,false);});net.pump();
            assertEquals(2,c.router.snapshot().catalog().entries().size());assertTrue(c.router.snapshot().catalog().entries().values().stream().allMatch(e->e.info().serverId().equals("T")));
        }
    }
    static final class Network implements AutoCloseable {
        final Map<String,Rig> nodes=new LinkedHashMap<>();final Map<String,Map<String,Neighbor.Session>> sessions=new HashMap<>();
        final ArrayDeque<Runnable> wire=new ArrayDeque<>();int messages;
        void add(String id,boolean source){var r=new Rig(id,source,4096,4096,10,new RoutingTable.Limits(64,256,63,8));nodes.put(id,r);sessions.put(id,new HashMap<>());
            r.delivery=(p,m)->{messages++;wire.add(()->{var target=nodes.get(p.nodeId());target.act(()->target.router.onNeighborEvent(new NeighborManager.Received(sessions.get(p.nodeId()).get(id),m)));});};}
        static Neighbor.Session peer(String id){return new Neighbor.Session(id,LOCAL,UUID.randomUUID(),new ControlMessage.Endpoint(id,LOCAL,"127.0.0.1",19001,19101,NodeRole.RELAY));}
        void edge(String a,String b,long cost){var ab=peer(b);var ba=peer(a);sessions.get(a).put(b,ab);sessions.get(b).put(a,ba);nodes.get(a).up(ab);nodes.get(b).up(ba);nodes.get(a).metric(ab,cost);nodes.get(b).metric(ba,cost);}
        void pump(){int n=0;while(!wire.isEmpty()){assertTrue(n++<10000,"network flood");wire.remove().run();}}
        void tick(long ms){for(var r:nodes.values())r.tick(ms);pump();}
        public void close(){nodes.values().forEach(Rig::close);}
    }
    @Test void d07InvalidBeforeFirstReferenceCannotPoisonMetadataOrBinding(){try(var r=new Rig()){
        var a=session("A");r.up(a);r.ad(a,STREAM,EPOCH,1,100,List.of("S","A","C"),"Wrong",25);
        assertEquals(0,r.router.snapshot().diagnostics().bindings());r.ad(a,1,100);assertEquals("Video A",r.router.snapshot().catalog().entries().get(STREAM).info().title());
        var before=r.route();r.ad(a,STREAM,EPOCH,2,90,List.of("S","A"),"Video A",30);assertEquals(before,r.route());r.ad(a,2,90);assertEquals(2,r.route().candidates().getFirst().announceSeq());
    }}
    @Test void d08D10EpochChangeIsSourceAtomicAndNinthRetirementRefused(){try(var r=new Rig()){
        var a=session("A");r.up(a);r.metric(a,10);r.ad(a,1,100);r.ad(a,OTHER,EPOCH,1,100,List.of("S","A"),"Other",25);assertEquals(2,r.router.snapshot().catalog().entries().size());
        for(int i=2;i<=9;i++)r.ad(a,STREAM,new UUID(2,i),0,100,List.of("S","A"),"Epoch "+i,25);
        var before=r.route();assertEquals(1,r.router.snapshot().catalog().entries().size());r.ad(a,STREAM,new UUID(2,10),0,100,List.of("S","A"),"Invalid ninth switch",25);assertEquals(before,r.route());assertEquals("Epoch 9",r.router.snapshot().catalog().entries().get(STREAM).info().title());
    }}
    @Test void d14LocalPendingSaturationPreservesWithdrawalDebtAndHealthySessions(){try(var r=new Rig("C",false,4096,1,1,new RoutingTable.Limits(64,256,3,8))){
        var a=session("A");var d=session("D");var e=session("E");for(var p:List.of(a,d,e))r.up(p);r.metric(a,10);r.ad(a,7,100);
        assertEquals(1,r.to(d).size());assertEquals(1,r.to(e).size());r.down(a);
        assertEquals(1,r.router.snapshot().diagnostics().pendingCells());assertTrue(r.router.snapshot().diagnostics().pendingIntents()<=2);assertEquals(2,r.router.snapshot().diagnostics().sessions());
        r.tick(1000);assertEquals(MessageType.STREAM_WITHDRAW,r.to(d).getLast().type());assertEquals(MessageType.STREAM_WITHDRAW,r.to(e).getLast().type());assertEquals(2,r.router.snapshot().diagnostics().sessions());assertFalse(r.owner.stopped);
    }}
    @Test void d15OutputOverflowRetiresOnlyAffectedSessionAndNeverRetriesIt(){try(var r=new Rig()){
        var a=session("A");var d=session("D");var e=session("E");for(var p:List.of(a,d,e))r.up(p);r.metric(a,10);r.overflowPeer="D";r.ad(a,1,100);
        assertEquals(2,r.router.snapshot().diagnostics().sessions());assertEquals("A",r.router.snapshot().routes().get(STREAM).knownUpstream().orElseThrow().nodeId());int count=r.sent.size();r.next=Connection.SendResult.ACCEPTED;r.tick(1000);assertEquals(count,r.sent.size());assertFalse(r.owner.stopped);
    }}
    @Test void d17EqualCostPathChangeWithdrawsInsteadOfClaimingImprovement(){try(var r=new Rig()){
        var a=session("A");var b=session("B");var d=session("D");for(var p:List.of(a,b,d))r.up(p);r.metric(a,10);r.metric(b,10);r.ad(b,7,100);r.ad(a,7,100);
        assertEquals("A",r.route().knownUpstream().orElseThrow().nodeId());assertEquals(MessageType.STREAM_WITHDRAW,r.to(d).getLast().type());r.ad(a,8,100);assertEquals(8,((ControlMessage.Announcement)r.to(d).getLast().payload()).announceSeq());
    }}
    @Test void d18SelectedGenerationCannotBorrowOrRegressSequence(){try(var r=new Rig()){
        var a=session("A");var b=session("B");var d=session("D");for(var p:List.of(a,b,d))r.up(p);r.metric(a,10);r.metric(b,10);r.ad(a,7,100);r.ad(b,8,200);
        assertEquals(7,((ControlMessage.Announcement)r.to(d).getLast().payload()).announceSeq());
        r.down(a);assertEquals(8,((ControlMessage.Announcement)r.to(d).getLast().payload()).announceSeq());
        var fresh=session("A");r.up(fresh);r.metric(fresh,10);r.ad(fresh,7,50);assertEquals(MessageType.STREAM_WITHDRAW,r.to(d).getLast().type());r.ad(fresh,8,40);assertEquals(MessageType.STREAM_WITHDRAW,r.to(d).getLast().type());r.ad(fresh,9,40);assertEquals(9,((ControlMessage.Announcement)r.to(d).getLast().payload()).announceSeq());
    }}
    @Test void d21LateRevisionAndOldConnectionDoNotConfirmNewIntent(){try(var r=new Rig()){
        var a=session("A");var d=session("D");r.up(a);r.up(d);r.metric(a,10);r.hold=true;r.ad(a,7,100);var first=r.sent.getFirst();r.ad(a,7,90);assertEquals(1,r.to(d).size());
        first.result().complete(Connection.SendResult.ACCEPTED);r.owner.drain();assertEquals(2,r.to(d).size());assertEquals(100,((ControlMessage.Announcement)r.to(d).getLast().payload()).costMicros());
        var old=r.sent.getLast();r.down(d);var fresh=session("D");r.up(fresh);int before=r.to(fresh).size();old.result().complete(Connection.SendResult.ACCEPTED);r.owner.drain();assertEquals(before,r.to(fresh).size());assertTrue(r.to(fresh).stream().noneMatch(m->m.type()==MessageType.STREAM_WITHDRAW));
    }}
    @Test void d23NewRepairPeerGetsCurrentGenerationWithoutBootstrapPort(){try(var r=new Rig()){
        var a=session("A");r.up(a);r.metric(a,10);r.ad(a,7,100);var newlyAdmitted=session("Z");r.up(newlyAdmitted);assertEquals(1,r.to(newlyAdmitted).size());assertEquals(7,((ControlMessage.Announcement)r.to(newlyAdmitted).getFirst().payload()).announceSeq());
    }}
    @Test void d25UnrelatedEventsAndErrorNeverMutateRoutingOrReply(){try(var r=new Rig()){
        var a=session("A");r.up(a);var before=r.router.snapshot();r.act(()->r.router.onNeighborEvent(new NeighborManager.Received(a,new ControlMessage(MessageType.ERROR,UUID.randomUUID(),a.nodeId(),a.instanceId(),new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE,9,"legitimate")))));assertEquals(before,r.router.snapshot());assertTrue(r.sent.isEmpty());
    }}
    @Test void d29RunningTimerAndCallbackMustPhysicallyFinishAfterCleanup(){
        var r=new Rig("S",true,4096,4096,10,new RoutingTable.Limits(64,256,63,8));r.timer.pin=true;r.router.requestClose();r.owner.drain();assertFalse(r.router.awaitTermination(Duration.ZERO));assertTrue(r.timer.physicalJobs()<=2);r.timer.pin=false;r.timer.jobs.clear();assertTrue(r.router.awaitTermination(Duration.ofMillis(50)));r.close();
    }
    @Test void d30SourceSequenceExhaustionIsExplicitWithoutWrapping()throws Exception{
        try(var r=new Rig("S",true,4096,4096,10,new RoutingTable.Limits(64,256,63,8))){
            var field=RoutingService.class.getDeclaredField("rows");field.setAccessible(true);var row=((Map<?,?>)field.get(r.router)).get(STREAM);var seq=row.getClass().getDeclaredField("sequence");seq.setAccessible(true);seq.setLong(row,Long.MAX_VALUE);
            r.tick(2000);assertEquals(RoutingService.Phase.STOPPED,r.router.snapshot().diagnostics().phase());assertTrue(r.events.stream().anyMatch(e->e.fatal()&&e.reason()==RoutingService.Reason.SEQUENCE_EXHAUSTED));
        }
    }

    @Test void d28NewStreamAtCapacityCannotInvalidateHealthyEpoch(){try(var r=new Rig("C",false,4096,4096,10,new RoutingTable.Limits(1,1,2,8))){
        var a=session("A");r.up(a);r.metric(a,10);r.ad(a,1,100);var before=r.route();
        r.ad(a,OTHER,new UUID(2,2),0,100,List.of("S","A"),"New stream",25);
        assertEquals(before,r.route());assertEquals("Video A",r.router.snapshot().catalog().entries().get(STREAM).info().title());assertEquals(1,r.router.snapshot().diagnostics().references());
    }}
    @Test void d29RejectedCompletionWakeCleansByHookBeforePhysicalRelease(){
        var r=new Rig("S",true,4096,1,10,new RoutingTable.Limits(64,256,63,8));r.hold=true;var p=session("P");r.up(p);
        var physicalAtHook=new java.util.concurrent.atomic.AtomicInteger(-1);r.owner.onStop(()->physicalAtHook.set(r.router.snapshot().diagnostics().sendTickets()));r.owner.reject=true;
        r.sent.getFirst().result().complete(Connection.SendResult.ACCEPTED);
        assertTrue(r.owner.stopped);assertEquals(1,physicalAtHook.get());assertTrue(r.owner.queue.isEmpty());assertTrue(r.router.awaitTermination(Duration.ofMillis(50)));r.close();
    }
    @Test void d24CloseBeforeStartLeavesBorrowedOwnerUsable(){
        var clock=new AtomicLong();var owner=new Owner(10);var timer=new Timers(clock);var r=new RoutingService(config("C",false,10,10,10),LOCAL,owner,(p,m)->{throw new AssertionError("send before start");},e->{},clock::get,timer);
        r.requestClose();owner.drain();assertTrue(r.awaitTermination(Duration.ZERO));assertFalse(owner.stopped);assertTrue(r.snapshot().routes().isEmpty());owner.close();
    }
    @Test void d12ClosedAttemptConsumesQuotaWithoutRefund(){try(var r=new Rig("C",false,4096,4096,1,new RoutingTable.Limits(64,256,63,8))){
        var a=session("A");var d=session("D");r.up(a);r.up(d);r.metric(a,10);r.next=Connection.SendResult.CLOSED;r.ad(a,1,100);r.next=Connection.SendResult.ACCEPTED;var fresh=session("D");r.up(fresh);assertTrue(r.to(fresh).isEmpty());r.tick(1000);assertEquals(1,r.to(fresh).size());
    }}
    @Test void d07IdentityAndCostOverflowCannotCreateBinding(){try(var r=new Rig()){
        var a=session("A");r.up(a);r.metric(a,10);r.ad(a,1,Long.MAX_VALUE);assertEquals(0,r.router.snapshot().diagnostics().bindings());
        r.act(()->r.router.onNeighborEvent(new NeighborManager.Received(a,new ControlMessage(MessageType.STREAM_ANNOUNCE,UUID.randomUUID(),"wrong",a.instanceId(),new ControlMessage.Announcement(new ControlMessage.StreamInfo(STREAM,"S","Wrong",1,25,EPOCH),1,100,List.of("S","A"))))));assertEquals(0,r.router.snapshot().diagnostics().bindings());
    }}

    @Test void d15PhysicalReleaseAfterConcurrentOwnerContinuationResumesPendingWork(){try(var r=new Rig("S",true,4096,1,10,new RoutingTable.Limits(64,256,63,8))){
        r.hold=true;r.up(session("P"));assertEquals(1,r.sent.size());r.owner.inline=true;
        // The owner consumes the result before the producer callback's finally returns.
        r.sent.getFirst().result().complete(Connection.SendResult.ACCEPTED);assertEquals(2,r.sent.size());
    }}

    @Test void d24ClockFailureDuringConstructionCannotLeaveCleanupHook(){
        var clock=new AtomicLong();var owner=new Owner(10);var timers=new Timers(clock);
        try{
            assertThrows(IllegalStateException.class,()->new RoutingService(config("C",false,10,10,10),LOCAL,owner,(p,m)->CompletableFuture.completedFuture(Connection.SendResult.ACCEPTED),e->{},()->{throw new IllegalStateException("clock fixture");},timers));
            assertTrue(owner.hooks.isEmpty(),"failed constructor left owner hook");assertEquals(0,timers.physicalJobs());
        }finally{owner.hooks.clear();timers.requestClose();owner.close();}
    }

    @Test void d08AnnounceRevalidatesExpiryBetweenTwoSendsInOneTurn(){try(var r=new Rig()){
        var a=session("A");var d=session("D");var e=session("E");for(var p:List.of(a,d,e))r.up(p);r.metric(a,10);r.hold=true;
        var announcements=new java.util.concurrent.atomic.AtomicInteger();
        r.delivery=(peer,message)->{if(message.type()==MessageType.STREAM_ANNOUNCE&&announcements.getAndIncrement()==0)r.clock.addAndGet(8_000_000_000L);};
        r.ad(a,1,100);
        assertEquals(1,announcements.get(),"second send in the same owner turn used an expired candidate");
        assertEquals(8_000_000_000L,r.router.snapshot().capturedAtNanos());assertTrue(r.router.snapshot().catalog().entries().isEmpty());
        assertEquals(1,r.router.snapshot().diagnostics().sendTickets());assertEquals(1,r.router.snapshot().diagnostics().quotas());
        assertTrue(r.route().selectedCandidate().isEmpty());assertEquals(3,r.router.snapshot().diagnostics().sessions());
    }}
    @Test void d22VisibleAtNeverRelabelsExpiredSelectionAsCurrentWhileAlternativeLives(){try(var r=new Rig()){
        var a=session("A");var b=session("B");r.up(a);r.up(b);r.metric(a,10);r.metric(b,20);r.ad(a,1,100);r.tick(1000);r.ad(b,1,200);
        var old=r.router.snapshot();var entry=old.catalog().entries().get(STREAM);assertEquals("A",entry.knownUpstream().orElseThrow().nodeId());assertEquals(110,entry.metric().orElseThrow().costMicros());
        assertEquals(old.catalog(),old.catalog().visibleAt(old.capturedAtNanos()));
        // A expires at 8s; B remains alive until 9s. The pure catalog lacks B's route details.
        var later=old.catalog().visibleAt(8_000_000_000L);var visible=later.entries().get(STREAM);
        assertNotNull(visible);assertEquals("Video A",visible.info().title());assertEquals(1_000_000_000L,visible.remainingNanos().orElseThrow());
        assertTrue(visible.metric().isEmpty(),"snapshot dated now still exposes expired selected cost");assertTrue(visible.knownUpstream().isEmpty());assertEquals(RouteState.UNAVAILABLE,visible.state());
        assertEquals(500_000_000L,later.visibleAt(8_500_000_000L).entries().get(STREAM).remainingNanos().orElseThrow());assertTrue(later.visibleAt(9_000_000_000L).entries().isEmpty());
        assertSame(old,r.router.snapshot());assertEquals(110,entry.metric().orElseThrow().costMicros());assertEquals("A",old.routes().get(STREAM).knownUpstream().orElseThrow().nodeId());assertEquals(1_000_000_000L,r.clock.get());
    }}

}
