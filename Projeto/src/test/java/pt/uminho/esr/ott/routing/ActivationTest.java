package pt.uminho.esr.ott.routing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import java.net.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import pt.uminho.esr.ott.bootstrap.BootstrapServer;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.monitoring.*;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.Connection;

@Timeout(20)
class ActivationTest {
    static final UUID A=new UUID(1,1),B=new UUID(1,2),C=new UUID(1,3);
    static UUID id(String name){return UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    static final UUID EPOCH=id("S-instance");
    static Neighbor.Session peer(String name){UUID instance=id(name+"-instance");return new Neighbor.Session(name,instance,UUID.randomUUID(),
            new ControlMessage.Endpoint(name,instance,"127.0.0.1",19001,19101,NodeRole.RELAY));}
    static NodeConfiguration config(String name,boolean source,int capacity,int attempts){
        var d=RuntimeConfiguration.defaults();
        var runtime=new RuntimeConfiguration(d.control(),new RuntimeConfiguration.Overlay(d.overlay().helloTimeout(),d.overlay().retryInitial(),
                d.overlay().retryMax(),d.overlay().retryJitterPercent(),capacity),d.monitoring(),
                new RuntimeConfiguration.Routing(d.routing().announceInterval(),d.routing().candidateTtl(),4096,d.routing().cacheTtl(),10,
                        Duration.ofMillis(3000),attempts,20,3,d.routing().switchMinDwell()),d.subscription(),d.video(),d.source());
        var streams=source?List.of(new StreamConfiguration(Path.of("catalog"),A,name,"A",Path.of("a.Mjpeg"),10,false),
                new StreamConfiguration(Path.of("catalog"),B,name,"B",Path.of("b.Mjpeg"),10,false)):List.<StreamConfiguration>of();
        return new NodeConfiguration(Path.of("node"),name,source?NodeRole.SERVER:NodeRole.RELAY,"127.0.0.1","127.0.0.1",19001,19101,
                Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1",19000)),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),streams,runtime);
    }
    static final class Owner implements NeighborManager.SerialOwner {
        final ArrayDeque<Runnable> queue=new ArrayDeque<>();final List<Runnable> hooks=new ArrayList<>();final int capacity;
        volatile Thread running;boolean stopped;volatile Predicate<Thread> hold=t->false;
        final CountDownLatch offered=new CountDownLatch(1),resume=new CountDownLatch(1);
        Owner(int capacity){this.capacity=capacity;}
        public int capacity(){return capacity;}public boolean isOwnerThread(){return running==Thread.currentThread();}
        public boolean offer(Runnable r){
            if(hold.test(Thread.currentThread())){offered.countDown();try{assertTrue(resume.await(5,TimeUnit.SECONDS));}catch(InterruptedException e){throw new AssertionError(e);}}
            synchronized(queue){if(stopped)return false;if(queue.size()>=capacity){fail(new IllegalStateException("full"));return false;}queue.add(r);return true;}
        }
        public NeighborManager.Registration onStop(Runnable r){hooks.add(r);return ()->hooks.remove(r);}
        public void fail(Throwable t){requestClose();}
        public void requestClose(){if(stopped)return;stopped=true;synchronized(queue){queue.clear();}run(()->{for(var h:List.copyOf(hooks))h.run();});}
        public boolean awaitTermination(Duration d){return stopped;}public void close(){requestClose();}
        void run(Runnable r){var old=running;running=Thread.currentThread();try{r.run();}finally{running=old;}}
        boolean drain(){boolean did=false;for(int n=0;n<10000;n++){
            Runnable r;synchronized(queue){r=queue.poll();}if(r==null)return did;did=true;run(r);
        }throw new AssertionError("Unbounded owner wake loop");}
    }
    static final class Timers implements RoutingService.Scheduler {
        final AtomicLong clock;final List<Job> jobs=new ArrayList<>();boolean stopped;
        final class Job implements RoutingService.Ticket {final Runnable r;final long began,delay;
            Job(Runnable r,long delay){this.r=r;this.delay=delay;began=clock.get();}public void cancel(){jobs.remove(this);}}
        Timers(AtomicLong clock){this.clock=clock;}
        public RoutingService.Ticket schedule(Runnable r,long delay){if(stopped||jobs.size()>=2)throw new RejectedExecutionException();var j=new Job(r,delay);jobs.add(j);return j;}
        public void requestClose(){stopped=true;jobs.clear();}public boolean awaitTermination(Duration d){return jobs.isEmpty();}public int physicalJobs(){return jobs.size();}
        boolean fire(){for(var j:List.copyOf(jobs))if(clock.get()-j.began>=j.delay){jobs.remove(j);j.r.run();return true;}return false;}
    }
    record Sent(Neighbor.Session peer,ControlMessage message,CompletableFuture<Connection.SendResult> result) { }
    static final class Rig implements AutoCloseable {
        final String name;final UUID instance;final AtomicLong clock=new AtomicLong();final Owner owner;final Timers timers=new Timers(clock);
        final List<Sent> sent=new ArrayList<>();final RoutingService router;Predicate<ControlMessage> hold=m->false;
        Rig(){this("R",false,4096,3);}Rig(String name,boolean source,int capacity,int attempts){
            this.name=name;instance=id(name+"-instance");owner=new Owner(capacity);
            router=new RoutingService(config(name,source,capacity,attempts),instance,owner,(p,m)->{
                assertTrue(owner.isOwnerThread());var result=new CompletableFuture<Connection.SendResult>();sent.add(new Sent(p,m,result));
                if(!hold.test(m))result.complete(Connection.SendResult.ACCEPTED);return result;
            },e->{assertFalse(e.fatal(),e.toString());},clock::get,timers);
            act(router::start);
        }
        void act(Runnable r){owner.run(r);pump();}
        void pump(){for(int i=0;i<10000;i++){boolean did=owner.drain();boolean timer=timers.fire();if(!did&&!timer)return;}fail("Timer/owner busy loop");}
        void tick(long nanos){clock.addAndGet(nanos);pump();}
        void up(Neighbor.Session p){act(()->router.onNeighborEvent(new NeighborManager.NeighborUp(p)));}
        void metric(Neighbor.Session p,long cost){act(()->router.onMetrics(new NetworkMonitor.Updated(new LinkMetrics(p,LinkMetrics.State.MEASURED,
                OptionalDouble.of(cost),OptionalLong.of(cost),1,false,OptionalLong.of(clock.get()),0))));}
        void receive(Neighbor.Session p,ControlMessage m){act(()->router.onNeighborEvent(new NeighborManager.Received(p,m)));}
        void ad(Neighbor.Session p,long seq,long cost){ad(p,A,"S",EPOCH,seq,cost);}
        void ad(Neighbor.Session p,UUID stream,String source,UUID epoch,long seq,long cost){
            receive(p,new ControlMessage(MessageType.STREAM_ANNOUNCE,UUID.randomUUID(),p.nodeId(),p.instanceId(),
                    new ControlMessage.Announcement(new ControlMessage.StreamInfo(stream,source,"Video",1,10,epoch),seq,cost,List.of(source,p.nodeId()))));
        }
        void route(Neighbor.Session p){up(p);metric(p,10);ad(p,1,100);}
        ControlMessage activate(Neighbor.Session p,UUID stream,UUID epoch,UUID sub,long rev,UUID request,List<String> path,long lease){
            var m=new ControlMessage(MessageType.ACTIVATE,request,p.nodeId(),p.instanceId(),new ControlMessage.Activation(stream,epoch,sub,rev,lease,path));receive(p,m);return m;
        }
        ControlMessage child(Neighbor.Session p){return activate(p,A,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","A",name,p.nodeId()),6000);}
        List<Sent> messages(MessageType type){return sent.stream().filter(s->s.message.type()==type).toList();}
        Sent latestRequest(){return messages(MessageType.ACTIVATE).getLast();}
        void ack(Sent request,long grant){var p=(ControlMessage.Activation)request.message.payload();
            receive(request.peer,new ControlMessage(MessageType.ACTIVATE_ACK,request.message.requestId(),request.peer.nodeId(),request.peer.instanceId(),
                    new ControlMessage.ActivationAck(p.streamId(),p.serverEpoch(),p.subscriptionId(),p.revision(),grant)));}
        void error(Sent request,int related,ErrorCode code){receive(request.peer,new ControlMessage(MessageType.ERROR,request.message.requestId(),
                request.peer.nodeId(),request.peer.instanceId(),new ControlMessage.ErrorPayload(code,related,"fixture")));}
        RoutingService.SubscriptionTicket local(UUID stream,UUID token){var t=router.requestSubscription(stream,token);pump();return t;}
        Route.Confirmation forwarding(UUID stream){return router.snapshot().forwarding().get(stream);}
        RoutingService.ActivationLoad load(){var result=new AtomicReference<RoutingService.ActivationLoad>();owner.run(()->result.set(router.activationLoad()));return result.get();}
        public void close(){router.requestClose();owner.drain();for(var s:sent)if(!s.result.isDone())s.result.complete(Connection.SendResult.CLOSED);owner.drain();
            assertTrue(router.awaitTermination(Duration.ofSeconds(1)));owner.close();}
    }
    static ErrorCode lastError(Rig r){return ((ControlMessage.ErrorPayload)r.messages(MessageType.ERROR).getLast().message.payload()).code();}
    static RoutingService.SubscriptionOutcome result(RoutingService.SubscriptionTicket t){return t.result().orElseThrow();}

    @Test void m01TwoChildrenBeforeAndAfterAckShareOneUpstream(){try(var r=new Rig()){
        var a=peer("A");var c1=peer("C1");var c2=peer("C2");r.route(a);r.up(c1);r.up(c2);
        var one=r.child(c1);var two=r.child(c2);assertEquals(1,r.messages(MessageType.ACTIVATE).size());assertNull(r.forwarding(A));
        r.ack(r.latestRequest(),6000);var f=r.forwarding(A);assertEquals(Set.of("C1","C2"),f.downstreams().keySet());assertEquals(a,f.upstream().orElseThrow());
        for(var request:List.of(one,two))assertTrue(r.messages(MessageType.ACTIVATE_ACK).stream().anyMatch(s->s.message.requestId().equals(request.requestId())
                &&((ControlMessage.ActivationAck)s.message.payload()).subscriptionId().equals(((ControlMessage.Activation)request.payload()).subscriptionId())));
        var c3=peer("C3");r.up(c3);r.child(c3);assertEquals(1,r.messages(MessageType.ACTIVATE).size());assertEquals(3,r.forwarding(A).downstreams().size());
        assertTrue(r.router.snapshot().routes().get(A).activeUpstream().isEmpty());assertEquals(RouteState.INACTIVE,r.router.snapshot().catalog().entries().get(A).state());
    }}
    @Test void m02LocalTokensAreSetsAndCancellationPreservesTheOtherConsumer(){try(var r=new Rig()){
        var a=peer("A");r.route(a);UUID t1=UUID.randomUUID(),t2=UUID.randomUUID();var one=r.local(A,t1);
        assertSame(one,r.router.requestSubscription(A,t1));var two=r.local(A,t2);assertEquals(1,r.messages(MessageType.ACTIVATE).size());
        r.ack(r.latestRequest(),6000);assertEquals(RoutingService.SubscriptionState.SUCCESS,result(one).state());assertEquals(2,r.forwarding(A).localConsumers().size());
        one.cancel();r.pump();assertEquals(Set.of(t2),r.forwarding(A).localConsumers());two.cancel();r.pump();assertNull(r.forwarding(A));assertEquals(0,r.load().locals());
    }}
    @Test void m03SourceConfirmsDirectlyAndDuplicateNeverRenews(){try(var r=new Rig("S",true,4096,3)){
        var c=peer("C1");r.up(c);UUID sub=UUID.randomUUID(),request=UUID.randomUUID();
        var m=r.activate(c,A,r.instance,sub,0,request,List.of("S","C1"),6000);var old=r.forwarding(A).downstreams().get("C1").validity().orElseThrow();
        assertTrue(r.messages(MessageType.ACTIVATE).isEmpty());assertTrue(r.forwarding(A).upstream().isEmpty());
        r.tick(1_000_000_000L);r.receive(c,m);var grant=(ControlMessage.ActivationAck)r.messages(MessageType.ACTIVATE_ACK).getLast().message.payload();
        assertEquals(5000,grant.grantedLeaseMs());assertEquals(old.remainingNanos(r.clock.get()),r.forwarding(A).downstreams().get("C1").validity().orElseThrow().remainingNanos(r.clock.get()));
        r.act(()->r.router.setLocalAvailable(B,false));assertNotNull(r.forwarding(A));
    }}
    @Test void m04AcceptedIsNotAckAndAnEarlyAckWaitsForAcceptance(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.hold=m->m.type()==MessageType.ACTIVATE;var ticket=r.local(A,UUID.randomUUID());var request=r.latestRequest();
        assertTrue(ticket.result().isEmpty());assertNull(r.forwarding(A));r.ack(request,6000);assertTrue(ticket.result().isEmpty());
        request.result.complete(Connection.SendResult.ACCEPTED);r.pump();assertEquals(RoutingService.SubscriptionState.SUCCESS,result(ticket).state());
    }}
    @Test void m05DivergentSelectionRejectsBothPrefixesWithoutChangingConfirmedBranch(){try(var r=new Rig()){
        var a=peer("A");var b=peer("B");var c=peer("C1");r.route(a);r.up(b);r.metric(b,10);r.ad(b,1,500);r.up(c);r.child(c);r.ack(r.latestRequest(),6000);
        var original=r.forwarding(A);r.ad(b,2,1);assertEquals("B",r.router.snapshot().routes().get(A).knownUpstream().orElseThrow().nodeId());
        for(var prefix:List.of(List.of("S","A","R","D"),List.of("S","B","R","D"))){var d=peer("D");r.up(d);
            r.activate(d,A,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),prefix,6000);assertEquals(ErrorCode.PATH_CHANGED,lastError(r));assertEquals(original,r.forwarding(A));}
        assertEquals(1,r.messages(MessageType.ACTIVATE).size());assertTrue(r.messages(MessageType.DEACTIVATE).isEmpty());
    }}
    @Test void m05WithdrawnSelectionStillPreservesALiveConfirmedLease(){try(var r=new Rig()){
        var a=peer("A");r.route(a);var ticket=r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);var f=r.forwarding(A);
        r.receive(a,new ControlMessage(MessageType.STREAM_WITHDRAW,UUID.randomUUID(),a.nodeId(),a.instanceId(),new ControlMessage.Withdrawal(A,EPOCH,2)));
        var d=peer("D");r.up(d);r.child(d);assertEquals(ErrorCode.PATH_CHANGED,lastError(r));assertEquals(f,r.forwarding(A));assertEquals(RoutingService.SubscriptionState.SUCCESS,result(ticket).state());
    }}
    @Test void m06WrongAckFieldsAndTraceErrorCannotConfirmOrCancelActivation(){try(var r=new Rig()){
        var a=peer("A");var other=peer("OTHER");r.route(a);r.up(other);var ticket=r.local(A,UUID.randomUUID());var request=r.latestRequest();var p=(ControlMessage.Activation)request.message.payload();
        for(int i=0;i<6;i++){
            var from=i==5?other:a;var ack=new ControlMessage.ActivationAck(i==0?B:A,i==1?UUID.randomUUID():EPOCH,
                    i==2?UUID.randomUUID():p.subscriptionId(),i==3?p.revision()+1:p.revision(),6000);
            r.receive(from,new ControlMessage(MessageType.ACTIVATE_ACK,i==4?UUID.randomUUID():request.message.requestId(),from.nodeId(),from.instanceId(),ack));
            assertTrue(ticket.result().isEmpty());assertNull(r.forwarding(A));
        }
        r.error(request,15,ErrorCode.NO_ROUTE);assertTrue(ticket.result().isEmpty());r.ack(request,6000);assertEquals(RoutingService.SubscriptionState.SUCCESS,result(ticket).state());
    }}
    @Test void m07CancelledRequestDoesNotReactivateAndOldCancellationCannotRemoveReplacement(){try(var r=new Rig()){
        var a=peer("A");r.route(a);UUID token=UUID.randomUUID();var old=r.local(A,token);var request=r.latestRequest();old.cancel();r.pump();r.ack(request,6000);assertNull(r.forwarding(A));
        var fresh=r.local(A,token);var next=r.latestRequest();assertNotEquals(request.message.requestId(),next.message.requestId());r.ack(request,6000);assertTrue(fresh.result().isEmpty());
        r.ack(next,6000);old.cancel();r.pump();assertEquals(Set.of(token),r.forwarding(A).localConsumers());
    }}
    @Test void m07CancellingOnePendingChildDoesNotCancelTheOther(){try(var r=new Rig()){
        var a=peer("A");var c=peer("C1");var d=peer("C2");r.route(a);r.up(c);r.up(d);var one=r.child(c);r.child(d);var p=(ControlMessage.Activation)one.payload();
        r.act(()->r.router.cancelPendingChild(c,A,p.subscriptionId(),p.revision()));r.ack(r.latestRequest(),6000);assertEquals(Set.of("C2"),r.forwarding(A).downstreams().keySet());
    }}
    @Test void m07CancelThenImmediateRejectedRetryCannotRestoreTheOldConsumer(){try(var r=new Rig()){
        var a=peer("A");var b=peer("B");r.route(a);r.up(b);r.metric(b,10);r.ad(b,1,500);UUID token=UUID.randomUUID();
        var old=r.local(A,token);r.ack(r.latestRequest(),6000);UUID other=UUID.randomUUID();r.local(A,other);r.ad(b,2,1);
        old.cancel();var fresh=r.router.retrySubscription(A,token);r.pump();
        assertEquals(RoutingService.SubscriptionState.ERROR,result(fresh).state());assertEquals(Optional.of(ErrorCode.PATH_CHANGED),result(fresh).errorCode());
        assertEquals(Set.of(other),r.forwarding(A).localConsumers());
    }}
    @Test void m08DuplicatesRevisionsAndLineageAreBoundedAndIdempotent(){try(var r=new Rig("S",true,4096,3)){
        var c=peer("C1");r.up(c);UUID sub=UUID.randomUUID(),request=UUID.randomUUID();var m=r.activate(c,A,r.instance,sub,1,request,List.of("S","C1"),6000);
        for(int i=0;i<30;i++)r.receive(c,m);assertEquals(1,r.load().children());assertEquals(1,r.load().histories());
        r.tick(1_000_000_000L);
        r.activate(c,A,r.instance,sub,0,UUID.randomUUID(),List.of("S","C1"),6000);assertEquals(ErrorCode.STALE_REVISION,lastError(r));
        r.activate(c,A,r.instance,sub,1,UUID.randomUUID(),List.of("S","C1"),6000);assertEquals(ErrorCode.STALE_REVISION,lastError(r));
        r.activate(c,A,r.instance,UUID.randomUUID(),2,UUID.randomUUID(),List.of("S","C1"),6000);assertEquals(ErrorCode.STALE_REVISION,lastError(r));
        r.activate(c,A,r.instance,sub,1,request,List.of("S","C1"),5000);assertEquals(ErrorCode.INVALID_MESSAGE,lastError(r));
        r.act(()->r.router.cancelPendingChild(c,A,sub,1));r.receive(c,m);assertNull(r.forwarding(A));assertEquals(1,r.load().histories());
        r.activate(c,A,r.instance,sub,Long.MAX_VALUE,UUID.randomUUID(),List.of("S","C1"),6000);assertNotNull(r.forwarding(A));
        r.act(()->r.router.cancelPendingChild(c,A,sub,1));assertNotNull(r.forwarding(A));
    }}
    @Test void m09AttemptsHaveOneGlobalDeadlineAndErrorsAreCorrelated(){try(var r=new Rig("R",false,4096,3)){
        var a=peer("A");r.route(a);var ticket=r.local(A,UUID.randomUUID());
        for(int i=0;i<3;i++)r.error(r.latestRequest(),11,ErrorCode.NO_ROUTE);
        assertEquals(3,r.messages(MessageType.ACTIVATE).size());assertEquals(RoutingService.SubscriptionState.ERROR,result(ticket).state());
        r.tick(5_000_000_000L);assertEquals(3,r.messages(MessageType.ACTIVATE).size());
    }}
    @Test void m09QueueAndFirstTimeoutConsumeTheEntireRound(){try(var r=new Rig()){
        var a=peer("A");r.route(a);var ticket=r.router.requestSubscription(A,UUID.randomUUID());r.clock.set(3_000_000_000L);r.pump();
        assertEquals(RoutingService.SubscriptionState.TIMEOUT,result(ticket).state());assertTrue(r.messages(MessageType.ACTIVATE).isEmpty());
        var fresh=r.local(A,UUID.randomUUID());r.tick(3_000_000_000L);assertEquals(RoutingService.SubscriptionState.TIMEOUT,result(fresh).state());assertEquals(1,r.messages(MessageType.ACTIVATE).size());
    }}
    @Test void m10StreamsAndOriginsHaveIndependentConfirmations(){try(var r=new Rig("R",false,4096,1)){
        var a=peer("A");r.route(a);r.ad(a,B,"S",EPOCH,1,100);var z=peer("Z");r.up(z);r.metric(z,10);UUID e=id("T-instance");r.ad(z,C,"T",e,1,100);
        var ta=r.local(A,UUID.randomUUID());var ar=r.latestRequest();var tb=r.local(B,UUID.randomUUID());var br=r.latestRequest();var tc=r.local(C,UUID.randomUUID());var cr=r.latestRequest();
        r.ack(ar,6000);r.error(br,11,ErrorCode.SOURCE_UNAVAILABLE);r.ack(cr,6000);
        assertEquals(RoutingService.SubscriptionState.SUCCESS,result(ta).state());assertEquals(RoutingService.SubscriptionState.ERROR,result(tb).state());
        assertEquals(RoutingService.SubscriptionState.SUCCESS,result(tc).state());assertEquals(Set.of(A,C),r.router.snapshot().forwarding().keySet());
    }}
    @Test void m11ReplacementSessionAndEpochCannotInheritAnAck(){try(var r=new Rig()){
        var a=peer("A");r.route(a);UUID token=UUID.randomUUID();var ticket=r.local(A,token);var old=r.latestRequest();
        var fresh=new Neighbor.Session(a.nodeId(),a.instanceId(),UUID.randomUUID(),a.endpoint());r.up(fresh);r.metric(fresh,10);r.ad(fresh,2,100);r.ack(old,6000);assertNull(r.forwarding(A));
        var retry=r.router.retrySubscription(A,token);r.pump();r.ack(r.latestRequest(),6000);assertEquals(RoutingService.SubscriptionState.SUCCESS,result(retry).state());
        UUID epoch2=UUID.randomUUID();r.ad(fresh,A,"S",epoch2,0,100);assertNull(r.forwarding(A));assertTrue(r.load().histories()==0);
        retry.cancel();r.pump();assertEquals(0,r.load().locals());
    }}
    @Test void m12GrantUsesRequestAnchorAndParentRemainingTimeWithFloor(){try(var r=new Rig()){
        var a=peer("A");var c=peer("C1");r.route(a);var local=r.local(A,UUID.randomUUID());var request=r.latestRequest();
        r.tick(1_000_000_000L);r.ack(request,6000);assertEquals(5_000_000_000L,r.forwarding(A).upstreamLease().orElseThrow().remainingNanos(r.clock.get()));
        r.up(c);var m=r.child(c);assertEquals(5000,((ControlMessage.ActivationAck)r.messages(MessageType.ACTIVATE_ACK).getLast().message.payload()).grantedLeaseMs());
        r.tick(1_000_000_000L);r.receive(c,m);assertEquals(4000,((ControlMessage.ActivationAck)r.messages(MessageType.ACTIVATE_ACK).getLast().message.payload()).grantedLeaseMs());
        var old=r.router.snapshot().forwarding().get(A);r.tick(4_000_000_000L);assertTrue(old.visibleAt(r.clock.get()).isEmpty());assertNull(r.forwarding(A));
    }}
    @Test void m12SubMillisecondRemainderAndExpiredOrOversizedAckNeverActivate(){try(var r=new Rig()){
        var a=peer("A");r.route(a);var t=r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6001);assertTrue(t.result().isEmpty());r.ack(r.latestRequest(),6000);
        r.tick(6_000_000_000L-999_999);var c=peer("C1");r.up(c);r.child(c);assertEquals(ErrorCode.NO_ROUTE,lastError(r));
        assertTrue(r.messages(MessageType.ACTIVATE_ACK).isEmpty());
    }try(var r=new Rig()){
        var a=peer("A");r.route(a);var t=r.local(A,UUID.randomUUID());var req=r.latestRequest();r.tick(1_000_000L);r.ack(req,1);
        assertNull(r.forwarding(A));assertTrue(t.result().isEmpty()||result(t).state()!=RoutingService.SubscriptionState.SUCCESS);
    }}
    @Test void m12LeaseArithmeticSupportsNanoTimeWrapAndNeverRoundsUp(){
        var l=new Subscription.Lease(Long.MAX_VALUE-10,30);assertEquals(10,l.remainingNanos(Long.MIN_VALUE+9));assertEquals(0,l.remainingMillis(Long.MIN_VALUE+9));
        assertEquals(0,l.remainingNanos(Long.MIN_VALUE+19));assertThrows(IllegalArgumentException.class,()->new Subscription.Lease(10,1).remainingNanos(9));
    }
    @Test void m13ProtectedHistoryCannotBeEvictedToAdmitNewChildren(){try(var r=new Rig("S",true,4,3)){
        var c=peer("C1");var d=peer("C2");var e=peer("C3");r.up(c);r.up(d);r.up(e);
        for(var stream:List.of(A,B))for(var child:List.of(c,d)){
            var m=r.activate(child,stream,r.instance,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S",child.nodeId()),6000);
            if(!stream.equals(B)||!child.equals(d)){var p=(ControlMessage.Activation)m.payload();r.act(()->r.router.cancelPendingChild(child,stream,p.subscriptionId(),1));}
        }
        assertEquals(4,r.load().histories());var healthy=r.forwarding(B);
        r.activate(e,A,r.instance,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","C3"),6000);assertEquals(ErrorCode.RESOURCE_LIMIT,lastError(r));assertEquals(healthy,r.forwarding(B));
        r.act(()->r.router.onNeighborEvent(new NeighborManager.NeighborDown(c,NeighborManager.Reason.TRANSPORT)));
        r.activate(e,A,r.instance,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","C3"),6000);assertNotNull(r.forwarding(A));assertTrue(r.load().histories()<=4);
    }}
    @Test void m13LogicalExpiryDoesNotReleasePhysicalAckReservations(){try(var r=new Rig("S",true,2,3)){
        var c=peer("C1");var d=peer("C2");r.up(c);r.up(d);r.hold=m->m.type()==MessageType.ACTIVATE_ACK;
        r.activate(c,A,r.instance,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","C1"),6000);
        r.activate(d,A,r.instance,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","C2"),6000);
        assertEquals(2,r.load().sends());r.tick(3_000_000_000L);assertEquals(2,r.load().sends());assertNull(r.forwarding(A));
        r.router.requestClose();r.owner.drain();assertFalse(r.router.awaitTermination(Duration.ZERO));
        for(var s:r.messages(MessageType.ACTIVATE_ACK))s.result.complete(Connection.SendResult.ACCEPTED);
        assertTrue(r.router.awaitTermination(Duration.ofMillis(10)));
    }}
    @Test void m13ReplyQuotaIsFixedWindowAndDoesNotExtendGrants(){try(var r=new Rig("S",true,4096,3)){
        var c=peer("C1");r.up(c);UUID sub=UUID.randomUUID(),request=UUID.randomUUID();var m=r.activate(c,A,r.instance,sub,1,request,List.of("S","C1"),6000);
        for(int i=0;i<40;i++)r.receive(c,m);assertEquals(16,r.messages(MessageType.ACTIVATE_ACK).size());assertTrue(r.load().responseIntents()<=1);
        r.tick(1_000_000_000L);assertEquals(17,r.messages(MessageType.ACTIVATE_ACK).size());
        assertEquals(5000,((ControlMessage.ActivationAck)r.messages(MessageType.ACTIVATE_ACK).getLast().message.payload()).grantedLeaseMs());
    }}
    @Test void m13LocalCapacityAndFourPendingStreamsPerChildAreEnforced(){try(var r=new Rig("R",false,2,3)){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.local(A,UUID.randomUUID());
        var denied=r.local(A,UUID.randomUUID());assertEquals(RoutingService.SubscriptionState.RESOURCE_LIMIT,result(denied).state());assertEquals(2,r.load().locals());
    }try(var r=new Rig()){
        var a=peer("A");var c=peer("C1");r.up(a);r.metric(a,10);r.up(c);r.hold=m->m.type()==MessageType.ACTIVATE;
        for(int i=0;i<5;i++){var stream=new UUID(88,i);r.ad(a,stream,"S",EPOCH,1,100);
            r.activate(c,stream,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","A","R","C1"),6000);}
        assertEquals(4,r.load().pendingChildren());assertEquals(4,r.load().trees());assertEquals(ErrorCode.RESOURCE_LIMIT,lastError(r));
    }}
    @Test void m14CallbackStillRunningRetainsItsReservationAndReleaseWakesBlockedRetry() throws Exception {try(var r=new Rig()){
        var a=peer("A");r.route(a);r.hold=m->m.type()==MessageType.ACTIVATE;var ticket=r.local(A,UUID.randomUUID());var first=r.latestRequest();
        r.owner.hold=t->t.getName().equals("activation-callback-held");
        var callback=new Thread(()->first.result.complete(Connection.SendResult.ACCEPTED),"activation-callback-held");callback.start();
        try{
            assertTrue(r.owner.offered.await(5,TimeUnit.SECONDS));r.error(first,11,ErrorCode.NO_ROUTE);var second=r.latestRequest();
            assertEquals(2,r.load().sends());r.error(second,11,ErrorCode.NO_ROUTE);assertEquals(2,r.messages(MessageType.ACTIVATE).size());
            assertTrue(ticket.result().isEmpty());
        }finally{r.owner.hold=t->false;r.owner.resume.countDown();callback.join(5000);}
        assertFalse(callback.isAlive());r.pump();assertEquals(3,r.messages(MessageType.ACTIVATE).size());assertEquals(2,r.load().sends());
    }}
    @Test void m14OwnerRejectionCompletesReservedLocalTicketViaExistingHook(){try(var r=new Rig("R",false,2,3)){
        var a=peer("A");r.route(a);assertTrue(r.owner.offer(()->{}));assertTrue(r.owner.offer(()->{}));
        var ticket=r.router.requestSubscription(A,UUID.randomUUID());assertEquals(RoutingService.SubscriptionState.CLOSED,result(ticket).state());assertEquals(0,r.load().locals());
    }}
    @Test void m14OwnerSaturationDuringCallbackCleansLogicAndReleasesPhysicalTicket(){try(var r=new Rig("R",false,2,3)){
        var a=peer("A");r.route(a);r.hold=m->m.type()==MessageType.ACTIVATE;var ticket=r.local(A,UUID.randomUUID());var pending=r.latestRequest();
        assertTrue(r.owner.offer(()->{}));assertTrue(r.owner.offer(()->{}));pending.result.complete(Connection.SendResult.ACCEPTED);
        assertEquals(RoutingService.SubscriptionState.CLOSED,result(ticket).state());assertEquals(0,r.load().sends());
        assertTrue(r.router.awaitTermination(Duration.ofMillis(10)));assertTrue(r.router.snapshot().forwarding().isEmpty());
    }}
    @Test void m14ShutdownWaitsForRequestCallbackWithoutResettingBudget(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.hold=m->m.type()==MessageType.ACTIVATE;var ticket=r.local(A,UUID.randomUUID());var req=r.latestRequest();
        r.router.requestClose();r.owner.drain();assertEquals(RoutingService.SubscriptionState.CLOSED,result(ticket).state());assertFalse(r.router.awaitTermination(Duration.ZERO));
        req.result.complete(Connection.SendResult.ACCEPTED);assertTrue(r.router.awaitTermination(Duration.ofMillis(10)));assertTrue(r.router.snapshot().forwarding().isEmpty());
    }}
    @Test void m14SnapshotConstructorAndViewsRemainImmutable(){try(var r=new Rig()){
        var s=r.router.snapshot();var old=new RoutingService.Snapshot(s.capturedAtNanos(),s.catalog(),s.routes(),s.diagnostics());assertTrue(old.forwarding().isEmpty());
        assertThrows(UnsupportedOperationException.class,()->s.forwarding().clear());var a=peer("A");r.route(a);var t=r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);
        assertThrows(UnsupportedOperationException.class,()->r.forwarding(A).localConsumers().clear());assertTrue(r.router.snapshot().routes().get(A).downstreams().isEmpty());
    }}

    /** Controlled observation of the original test's missing happens-before; no old test is modified. */
    @Test void neighborDownSnapshotCanPrecedeExternalObserverDelivery() throws Exception {
        var sockets=new ArrayList<ServerSocket>();var ports=new ArrayList<Integer>();
        try{for(int i=0;i<5;i++){var s=new ServerSocket(0,1,InetAddress.getLoopbackAddress());sockets.add(s);ports.add(s.getLocalPort());}}
        finally{for(var s:sockets)s.close();}
        var topology=new LinkedHashMap<String,TopologyConfiguration.Node>();
        for(int i=0;i<2;i++){String name="N"+i;topology.put(name,new TopologyConfiguration.Node(name,"127.0.0.1",ports.get(i+1),ports.get(i+3),NodeRole.RELAY,Set.of("N"+(1-i))));}
        var bootConfig=new NodeConfiguration(Path.of("bootstrap"),"BOOT",NodeRole.BOOTSTRAP,"127.0.0.1","127.0.0.1",ports.getFirst(),0,
                Optional.empty(),Optional.of(Path.of("topology")),Optional.empty(),Optional.empty(),Optional.of(new TopologyConfiguration(Path.of("topology"),topology)),List.of());
        var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var observed=new CopyOnWriteArrayList<NeighborManager.Event>();
        var nodes=new ArrayList<OverlayNode>();
        try(var boot=new BootstrapServer(bootConfig)){
            for(int i=0;i<2;i++){
                final int index=i;String name="N"+i;
                var cfg=new NodeConfiguration(Path.of(name),name,NodeRole.RELAY,"127.0.0.1","127.0.0.1",ports.get(i+1),ports.get(i+3),
                        Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1",ports.getFirst())),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),List.of());
                nodes.add(new OverlayNode(cfg,UUID.randomUUID(),event->{
                    if(index==0&&event instanceof NeighborManager.NeighborDown){entered.countDown();
                        try{if(!release.await(5,TimeUnit.SECONDS))throw new IllegalStateException("diagnostic barrier deadline");}
                        catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}
                    if(index==0)observed.add(event);
                }));
            }
            for(var n:nodes)n.start();await(()->nodes.stream().allMatch(n->n.snapshot().size()==1));
            var old=nodes.getFirst().snapshot().get("N1");nodes.get(1).close();assertTrue(entered.await(5,TimeUnit.SECONDS));
            assertFalse(nodes.getFirst().monitoringSnapshot().links().containsKey("N1"));
            assertFalse(observed.stream().anyMatch(e->e instanceof NeighborManager.NeighborDown d&&d.session().equals(old)));
            release.countDown();await(()->observed.stream().anyMatch(e->e instanceof NeighborManager.NeighborDown d&&d.session().equals(old)));
            assertTrue(nodes.getFirst().failure().isEmpty());
        }finally{release.countDown();for(var n:nodes)n.close();}
    }
    static void await(BooleanSupplier condition) throws Exception {long began=System.nanoTime();while(!condition.getAsBoolean()){
        if(System.nanoTime()-began>TimeUnit.SECONDS.toNanos(5))fail("diagnostic TCP condition deadline");Thread.sleep(2);}}
}
