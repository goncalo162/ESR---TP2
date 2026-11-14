package pt.uminho.esr.ott.overlay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.BooleanSupplier;
import pt.uminho.esr.ott.bootstrap.BootstrapServer;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.monitoring.NetworkMonitor;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.routing.*;
import pt.uminho.esr.ott.transport.Connection;

@Timeout(25)
class OverlayNodeRoutingTest {
    static final UUID STREAM=new UUID(10,1), OTHER=new UUID(10,2);
    static void until(BooleanSupplier predicate)throws Exception{
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(7);
        while(!predicate.getAsBoolean()){if(System.nanoTime()-deadline>=0)fail("condition deadline");Thread.sleep(5);}
    }
    static NodeConfiguration source(NodeConfiguration c){
        var streams=List.of(new StreamConfiguration(Path.of("streams"),STREAM,c.nodeId(),"Live A",Path.of("not-read.mjpeg"),25,false),new StreamConfiguration(Path.of("streams"),OTHER,c.nodeId(),"Live B",Path.of("not-read2.mjpeg"),30,false));
        return new NodeConfiguration(c.sourceFile(),c.nodeId(),NodeRole.SERVER,c.bindHost(),c.advertisedHost(),c.controlPort(),c.dataPort(),c.bootstrap(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),streams,c.runtime());
    }
    static final class Rig implements AutoCloseable {
        final NeighborManagerTest.Deployment ports=new NeighborManagerTest.Deployment();
        final List<OverlayNode> nodes=new ArrayList<>();final List<RoutingService> routers=new ArrayList<>();
        final List<NeighborManager.SerialOwner> owners=new ArrayList<>();
        final List<RoutingService.ControlPort> controls=new ArrayList<>();final List<UUID> instances=new ArrayList<>();
        final List<List<NeighborManager.Event>> events=new ArrayList<>();
        final AtomicReference<NeighborManager.SerialOwner> creatingOwner=new AtomicReference<>();
        final BootstrapServer bootstrap;
        Rig()throws Exception{
            ports.bootstrap.close();var bc=ports.bootstrapConfig;var topology=new LinkedHashMap<String,TopologyConfiguration.Node>();
            bc.topology().orElseThrow().nodes().forEach((id,n)->topology.put(id,new TopologyConfiguration.Node(id,n.host(),n.controlPort(),n.dataPort(),id.equals("N0")?NodeRole.SERVER:n.role(),n.neighbors())));
            bootstrap=new BootstrapServer(new NodeConfiguration(bc.sourceFile(),bc.nodeId(),bc.role(),bc.bindHost(),bc.advertisedHost(),bc.controlPort(),0,bc.bootstrap(),bc.topologyFile(),bc.streamsFile(),bc.mediaBase(),Optional.of(new TopologyConfiguration(Path.of("graph"),topology)),bc.streams(),bc.runtime()));
            for(int i=0;i<3;i++){
                var c=i==0?source(ports.configs.get(i)):ports.configs.get(i);var log=new CopyOnWriteArrayList<NeighborManager.Event>();events.add(log);
                nodes.add(new OverlayNode(c,UUID.randomUUID(),log::add,(cfg,id,owner,port,observer)->{
                    creatingOwner.set(owner);owners.add(owner);return new NetworkMonitor(cfg,id,owner,port,observer);
                },(cfg,id,owner,port,observer)->{
                    controls.add(port);instances.add(id);assertSame(creatingOwner.get(),owner);assertEquals(cfg.runtime().overlay().eventsCapacity(),owner.capacity());
                    var r=new RoutingService(cfg,id,owner,(session,message)->{assertTrue(owner.isOwnerThread());return port.send(session,message);},observer);routers.add(r);return r;
                }));
            }
        }
        void start()throws Exception{
            assertNotNull(bootstrap.localAddress());
            for(var n:nodes)n.start().toCompletableFuture().get(3,TimeUnit.SECONDS);
            until(()->nodes.stream().allMatch(n->n.snapshot().size()==2));
            until(()->nodes.stream().allMatch(n->n.routingSnapshot().catalog().entries().size()==2));
            until(()->nodes.get(1).routingSnapshot().routes().get(STREAM).selectedCandidate().isPresent());
        }
        public void close(){
            for(var n:nodes)n.requestShutdown(0,Duration.ofSeconds(3));
            for(var n:nodes)assertTrue(n.awaitTermination(Duration.ofSeconds(3)));
            bootstrap.close();ports.close();
        }
    }
    @Test void d26RealTcpSharesOwnerAndCodecAndPropagatesWithdrawalWithoutBootstrap()throws Exception{
        try(var rig=new Rig()){
            rig.start();assertEquals(3,rig.routers.size());var relay=rig.nodes.get(1);var route=relay.routingSnapshot().routes().get(STREAM);
            assertFalse(route.candidates().isEmpty());assertTrue(route.selectedMetric().orElseThrow().costMicros()>0);assertTrue(route.activeUpstream().isEmpty());
            var wire=rig.events.get(1).stream().filter(e->e instanceof NeighborManager.Received r&&r.message().type()==MessageType.STREAM_ANNOUNCE).map(e->((NeighborManager.Received)e).message()).toList();
            assertFalse(wire.isEmpty());for(var m:wire){assertEquals(2,m.version());assertEquals(m,MessageCodec.decode(MessageCodec.encode(m)));}
            rig.bootstrap.close();var changed=new CompletableFuture<Void>();assertTrue(rig.owners.get(0).offer(()->{rig.routers.get(0).setLocalAvailable(STREAM,false);changed.complete(null);}));changed.get(1,TimeUnit.SECONDS);
            until(()->!relay.routingSnapshot().catalog().entries().containsKey(STREAM));assertTrue(relay.routingSnapshot().catalog().entries().containsKey(OTHER));
            assertTrue(rig.events.get(1).stream().anyMatch(e->e instanceof NeighborManager.Received r&&r.message().type()==MessageType.STREAM_WITHDRAW));
            assertTrue(rig.nodes.stream().allMatch(n->n.failure().isEmpty()));
        }
    }
    @Test void d27OriginalObserverGetsSameErrorExactlyOnceAndRoutingDoesNotReply()throws Exception{
        try(var rig=new Rig()){
            rig.start();var source=rig.nodes.get(0);var destination=source.snapshot().get("N1");UUID request=UUID.randomUUID();
            var error=new ControlMessage(MessageType.ERROR,request,"N0",rig.instances.get(0),new ControlMessage.ErrorPayload(ErrorCode.NO_ROUTE,9,"real error"));
            assertEquals(Connection.SendResult.ACCEPTED,rig.controls.get(0).send(destination,error).toCompletableFuture().get(1,TimeUnit.SECONDS));
            until(()->rig.events.get(1).stream().anyMatch(e->e instanceof NeighborManager.Received received&&received.message().requestId().equals(request)));
            var received=rig.events.get(1).stream().filter(e->e instanceof NeighborManager.Received r&&r.message().requestId().equals(request)).map(e->((NeighborManager.Received)e).message()).toList();
            assertEquals(List.of(error),received);
            var event=new NeighborManager.Diagnostic(NeighborManager.Phase.READY,NeighborManager.Reason.STALE,Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty());
            var method=OverlayNode.class.getDeclaredMethod("onNeighborEvent",NeighborManager.Event.class);method.setAccessible(true);var done=new CompletableFuture<Void>();
            assertTrue(rig.owners.get(0).offer(()->{try{method.invoke(source,event);done.complete(null);}catch(Exception e){done.completeExceptionally(e);}}));done.get(1,TimeUnit.SECONDS);
            assertEquals(1,rig.events.get(0).stream().filter(e->e==event).count());
            assertTrue(source.routingSnapshot().routes().values().stream().allMatch(r->r.activeUpstream().isEmpty()));
            assertTrue(rig.nodes.stream().allMatch(n->n.failure().isEmpty()));
        }
    }
    @Test void d24RoutingFactoryFailureRollsBackPreviousComponents()throws Exception{
        var owner=new AtomicReference<NeighborManager.SerialOwner>();var node=new OverlayNode(NeighborManagerTest.config("A",19001),UUID.randomUUID(),e->{},
            (c,id,o,p,v)->{owner.set(o);return new NetworkMonitor(c,id,o,p,v);},(c,id,o,p,v)->{throw new IllegalStateException("routing factory fixture");});
        assertThrows(ExecutionException.class,()->node.start().toCompletableFuture().get(1,TimeUnit.SECONDS));assertTrue(node.failure().isPresent());assertTrue(node.awaitTermination(Duration.ofSeconds(2)));assertTrue(owner.get().awaitTermination(Duration.ZERO));node.close();
    }
    @Test void d27OldConstructorAndSnapshotsBeforeStartAndAfterClose(){
        var node=new OverlayNode(NeighborManagerTest.config("A",19001),UUID.randomUUID(),e->{});assertTrue(node.routingSnapshot().routes().isEmpty());node.close();assertTrue(node.routingSnapshot().catalog().entries().isEmpty());
    }
    @Test void d24SharedDeadlineIncludesRoutingPhysicalResourcesAndGracefulLeave()throws Exception{
        try(var rig=new Rig()){
            rig.start();var node=rig.nodes.get(0);var result=node.requestShutdown(0,Duration.ofSeconds(2));assertSame(result,node.requestShutdown(1,Duration.ofSeconds(30)));
            var report=result.toCompletableFuture().get(2,TimeUnit.SECONDS);assertEquals(2,report.attempts().size());assertTrue(report.attempts().stream().allMatch(a->a.outcome()==NeighborManager.LeaveOutcome.LOCAL_WRITE_CONFIRMED));
            assertTrue(node.awaitTermination(Duration.ofSeconds(2)));assertTrue(rig.routers.get(0).awaitTermination(Duration.ZERO));assertEquals(RoutingService.Phase.STOPPED,node.routingSnapshot().diagnostics().phase());
        }
    }
    @Test void d31InvalidSourceMetadataRejectedOfflineBeforeOwnerOrHelper(){
        var c=source(NeighborManagerTest.config("S",19001));var bad=new StreamConfiguration(Path.of("s"),STREAM,"S","bad\ud800",Path.of("missing"),25,false);
        var malformed=new NodeConfiguration(c.sourceFile(),c.nodeId(),c.role(),c.bindHost(),c.advertisedHost(),c.controlPort(),c.dataPort(),c.bootstrap(),c.topologyFile(),c.streamsFile(),c.mediaBase(),c.topology(),List.of(bad),c.runtime());
        var called=new AtomicBoolean();assertThrows(IllegalArgumentException.class,()->new OverlayNode(malformed,UUID.randomUUID(),e->{},(cfg,id,o,p,v)->{called.set(true);return new NetworkMonitor(cfg,id,o,p,v);}));assertFalse(called.get());
    }
}
