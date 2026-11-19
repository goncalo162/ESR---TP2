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
class SubscriptionLeaseTest {
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


    static Sent exactlyOne(Rig r,MessageType type){
        var values=r.messages(type);
        assertEquals(1,values.size(),"Expected exactly one "+type+"; missing IMP-012 behavior is not a timeout of this harness");
        return values.getFirst();
    }
    static ControlMessage renew(Rig r,Neighbor.Session child,ControlMessage original,long revision,UUID request){
        var p=(ControlMessage.Activation)original.payload();
        var m=new ControlMessage(MessageType.SUBSCRIPTION_RENEW,request,child.nodeId(),child.instanceId(),
                new ControlMessage.Activation(p.streamId(),p.serverEpoch(),p.subscriptionId(),revision,p.leaseMs(),p.expectedPath()));
        r.receive(child,m);return m;
    }
    static void deactivate(Rig r,Neighbor.Session child,ControlMessage original,long revision){
        var p=(ControlMessage.Activation)original.payload();
        r.receive(child,new ControlMessage(MessageType.DEACTIVATE,UUID.randomUUID(),child.nodeId(),child.instanceId(),
                new ControlMessage.Deactivation(p.streamId(),p.serverEpoch(),p.subscriptionId(),revision)));
    }
    static Sent reply(Rig r,MessageType type,UUID request){
        var values=r.messages(type).stream().filter(s->s.message.requestId().equals(request)).toList();
        assertEquals(1,values.size(),"Missing correlated "+type+" for request "+request);
        return values.getFirst();
    }
    static void errorReply(Rig r,ControlMessage request,int related,ErrorCode code){
        var p=(ControlMessage.ErrorPayload)reply(r,MessageType.ERROR,request.requestId()).message.payload();
        assertEquals(related,p.relatedType());assertEquals(code,p.code());
    }
    static ControlMessage sourceChild(Rig s,Neighbor.Session child,UUID stream,long lease){
        return s.activate(child,stream,s.instance,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S",child.nodeId()),lease);
    }

    @Test void l01DeactivateOneChildPreservesSiblingAndCommonTrunk(){try(var r=new Rig()){
        var a=peer("A");var c1=peer("C1");var c2=peer("C2");r.route(a);r.up(c1);r.up(c2);
        var one=r.child(c1);r.child(c2);r.ack(r.latestRequest(),6000);
        var healthy=r.forwarding(A).downstreams().get("C2");var upstream=r.forwarding(A).upstreamLease();
        deactivate(r,c1,one,1);
        assertEquals(Set.of("C2"),r.forwarding(A).downstreams().keySet(),"DEACTIVATE must remove only its exact child");
        assertEquals(healthy,r.forwarding(A).downstreams().get("C2"));assertEquals(upstream,r.forwarding(A).upstreamLease());
        assertEquals(1,r.messages(MessageType.ACTIVATE).size());assertTrue(r.messages(MessageType.DEACTIVATE).isEmpty());
    }}
    @Test void l02LastCancellationRemovesAuthorizationBeforeHeldPruneCompletes(){try(var r=new Rig()){
        var a=peer("A");r.route(a);var local=r.local(A,UUID.randomUUID());var original=r.latestRequest();r.ack(original,6000);
        r.hold=m->m.type()==MessageType.DEACTIVATE;local.cancel();r.pump();
        assertNull(r.forwarding(A),"Logical removal must not wait for physical pruning");
        var sent=exactlyOne(r,MessageType.DEACTIVATE);var old=(ControlMessage.Activation)original.message.payload();
        var p=(ControlMessage.Deactivation)sent.message.payload();
        assertEquals(original.peer,sent.peer);assertEquals(old.streamId(),p.streamId());assertEquals(old.serverEpoch(),p.serverEpoch());
        assertEquals(old.subscriptionId(),p.subscriptionId());assertEquals(old.revision(),p.revision());
        assertFalse(sent.result.isDone());assertEquals(1,r.load().sends());
        r.router.requestClose();r.owner.drain();assertFalse(r.router.awaitTermination(Duration.ZERO));
        sent.result.complete(Connection.SendResult.ACCEPTED);r.pump();assertTrue(r.router.awaitTermination(Duration.ofMillis(10)));
    }}
    @Test void l03MixedLocalAndChildDemandPrunesOnlyAfterBothLeave(){try(var r=new Rig()){
        var a=peer("A");var c=peer("C1");r.route(a);r.up(c);var local=r.local(A,UUID.randomUUID());var child=r.child(c);r.ack(r.latestRequest(),6000);
        local.cancel();r.pump();assertEquals(Set.of("C1"),r.forwarding(A).downstreams().keySet());assertTrue(r.messages(MessageType.DEACTIVATE).isEmpty());
        deactivate(r,c,child,1);assertNull(r.forwarding(A),"The last child must remove the remaining aggregate demand");
        exactlyOne(r,MessageType.DEACTIVATE);
    }}
    @Test void l04OrphanChildExpiresAndPrunesItsCommonTrunk(){try(var r=new Rig()){
        var a=peer("A");var c=peer("C1");r.route(a);r.up(c);
        r.activate(c,A,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","A","R","C1"),1000);r.ack(r.latestRequest(),6000);
        r.tick(999_999_999L);assertNotNull(r.forwarding(A));r.tick(1);assertNull(r.forwarding(A));
        assertEquals(a,exactlyOne(r,MessageType.DEACTIVATE).peer,"Orphan expiry must propagate the loss of final demand");
    }}
    @Test void l04SourceExpiryAlreadyExcludesOrphanWithoutNewNetworkInput(){try(var s=new Rig("S",true,4096,3)){
        var c=peer("C1");s.up(c);sourceChild(s,c,A,1000);var old=s.forwarding(A);
        s.tick(999_999_999L);assertNotNull(s.forwarding(A));s.tick(1);
        assertNull(s.forwarding(A));assertTrue(old.visibleAt(s.clock.get()).isEmpty());assertTrue(s.messages(MessageType.DEACTIVATE).isEmpty());
    }}
    @Test void l05PeriodicRenewalKeepsIdAndAdvancesRevisionAndRequest(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());var first=r.latestRequest();r.ack(first,6000);
        r.tick(2_000_000_000L);var sent=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);
        var before=(ControlMessage.Activation)first.message.payload();var after=(ControlMessage.Activation)sent.message.payload();
        assertEquals(before.subscriptionId(),after.subscriptionId());assertTrue(after.revision()>before.revision());
        assertNotEquals(first.message.requestId(),sent.message.requestId());assertEquals(before.expectedPath(),after.expectedPath());
        assertEquals(first.peer,sent.peer);assertEquals(1,r.messages(MessageType.ACTIVATE).size());
    }}
    @Test void l06ShortGrantSchedulesAtOneThirdAndStillExpiresWithoutAck(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),300);
        r.tick(99_999_999L);assertTrue(r.messages(MessageType.SUBSCRIPTION_RENEW).isEmpty());r.tick(1);
        exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);r.tick(200_000_000L);
        assertNull(r.forwarding(A),"Renewal waiting for ACK cannot prolong the old grant");
    }}
    @Test void l07EarlyRenewAckWaitsForAcceptanceAndUsesRequestAnchor(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),3000);
        var before=r.forwarding(A).upstreamLease();r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;r.tick(1_000_000_000L);
        var sent=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);r.tick(100_000_000L);r.ack(sent,2500);
        assertEquals(before,r.forwarding(A).upstreamLease(),"ACK alone cannot replace the grant while admission is unconfirmed");
        sent.result.complete(Connection.SendResult.ACCEPTED);r.pump();
        assertEquals(2_400_000_000L,r.forwarding(A).upstreamLease().orElseThrow().remainingNanos(r.clock.get()));
    }}
    @Test void l08NewRenewalExtendsButExactDuplicateNeverRenewsAgain(){try(var s=new Rig("S",true,4096,3)){
        var c=peer("C1");s.up(c);var first=sourceChild(s,c,A,6000);s.tick(1_000_000_000L);UUID request=UUID.randomUUID();
        var renewal=renew(s,c,first,2,request);reply(s,MessageType.ACTIVATE_ACK,request);
        var grant=s.forwarding(A).downstreams().get("C1");assertEquals(2,grant.revision());
        assertEquals(6_000_000_000L,grant.validity().orElseThrow().remainingNanos(s.clock.get()),"A fresh type14 may extend the old remaining 5s");
        s.tick(500_000_000L);s.receive(c,renewal);assertEquals(grant,s.forwarding(A).downstreams().get("C1"));
        assertEquals(5500,((ControlMessage.ActivationAck)s.messages(MessageType.ACTIVATE_ACK).getLast().message.payload()).grantedLeaseMs());
        assertEquals(1,s.load().histories());assertEquals(1,s.load().children());
    }}
    @Test void l10OldDeactivateCannotRemoveRenewedRevision(){try(var s=new Rig("S",true,4096,3)){
        var c=peer("C1");s.up(c);var first=sourceChild(s,c,A,6000);UUID request=UUID.randomUUID();renew(s,c,first,2,request);
        reply(s,MessageType.ACTIVATE_ACK,request);var stable=s.forwarding(A).downstreams().get("C1");assertEquals(2,stable.revision());
        deactivate(s,c,first,1);assertEquals(stable,s.forwarding(A).downstreams().get("C1"));
        deactivate(s,c,first,2);assertNull(s.forwarding(A),"Only the admitted current revision may be removed");
    }}
    @Test void l11PruneCapturesSubmittedRenewalRevisionAndLateAckCannotChangeReplacement(){try(var r=new Rig()){
        var a=peer("A");r.route(a);UUID token=UUID.randomUUID();var old=r.local(A,token);r.ack(r.latestRequest(),6000);
        r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;r.tick(2_000_000_000L);
        var pending=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);var renewal=(ControlMessage.Activation)pending.message.payload();
        old.cancel();r.pump();assertNull(r.forwarding(A));
        var prune=(ControlMessage.Deactivation)exactlyOne(r,MessageType.DEACTIVATE).message.payload();
        assertEquals(renewal.subscriptionId(),prune.subscriptionId());assertEquals(renewal.revision(),prune.revision());
        assertEquals(1,r.load().sends(),"Only the submitted renewal is still physically held");
        var fresh=r.router.retrySubscription(A,token);r.pump();var replacement=r.latestRequest();
        assertTrue(((ControlMessage.Activation)replacement.message.payload()).revision()>renewal.revision());
        r.ack(replacement,6000);var stable=r.forwarding(A);assertEquals(RoutingService.SubscriptionState.SUCCESS,result(fresh).state());
        r.ack(pending,6000);pending.result.complete(Connection.SendResult.ACCEPTED);r.pump();old.cancel();r.pump();
        assertEquals(stable,r.forwarding(A));assertEquals(Set.of(token),r.forwarding(A).localConsumers());assertEquals(0,r.load().sends());
    }}
    @Test void l11CancelledRenewAckCannotRestoreChildOrChangeSiblingLease(){try(var s=new Rig("S",true,4096,3)){
        var c1=peer("C1");var c2=peer("C2");s.up(c1);s.up(c2);var first=sourceChild(s,c1,A,6000);sourceChild(s,c2,A,6000);
        var healthy=s.forwarding(A).downstreams().get("C2");UUID request=UUID.randomUUID();s.hold=m->m.type()==MessageType.ACTIVATE_ACK&&m.requestId().equals(request);
        renew(s,c1,first,2,request);var held=reply(s,MessageType.ACTIVATE_ACK,request);assertFalse(held.result.isDone());
        deactivate(s,c1,first,2);assertEquals(Set.of("C2"),s.forwarding(A).downstreams().keySet());assertEquals(1,s.load().sends());
        held.result.complete(Connection.SendResult.ACCEPTED);s.pump();assertEquals(Set.of("C2"),s.forwarding(A).downstreams().keySet());
        assertEquals(healthy,s.forwarding(A).downstreams().get("C2"));assertEquals(0,s.load().sends());
    }}
    @Test void l12CancelledRenewAckRetainsPhysicalSlotUntilCallbackAndContinuationEnd() throws Exception {try(var s=new Rig("S",true,4096,3)){
        var c=peer("C1");s.up(c);var first=sourceChild(s,c,A,6000);UUID request=UUID.randomUUID();s.hold=m->m.type()==MessageType.ACTIVATE_ACK&&m.requestId().equals(request);
        renew(s,c,first,2,request);var held=reply(s,MessageType.ACTIVATE_ACK,request);deactivate(s,c,first,2);assertNull(s.forwarding(A));
        s.owner.hold=t->t.getName().equals("renew-ack-callback-held");var callback=new Thread(()->held.result.complete(Connection.SendResult.ACCEPTED),"renew-ack-callback-held");callback.start();
        try{assertTrue(s.owner.offered.await(5,TimeUnit.SECONDS));s.act(()->s.router.cancelPendingChild(c,A,((ControlMessage.Activation)first.payload()).subscriptionId(),2));
            assertEquals(1,s.load().sends(),"A processed continuation does not finish its blocked callback");assertNull(s.forwarding(A));
        }finally{s.owner.hold=t->false;s.owner.resume.countDown();callback.join(5000);}
        assertFalse(callback.isAlive());s.pump();assertEquals(0,s.load().sends());assertNull(s.forwarding(A));
    }}
    @Test void l13ExpiredRenewalIsExplicitlyRejectedWithoutActivating(){try(var s=new Rig("S",true,4096,3)){
        var c=peer("C1");s.up(c);var first=sourceChild(s,c,A,100);s.tick(100_000_000L);assertNull(s.forwarding(A));
        var request=renew(s,c,first,2,UUID.randomUUID());errorReply(s,request,14,ErrorCode.NO_ROUTE);assertNull(s.forwarding(A));
    }}
    @Test void l14DivergentRenewalPreservesOldGrantAndRejectsExplicitly(){try(var r=new Rig()){
        var a=peer("A");var b=peer("B");var c=peer("C1");r.route(a);r.up(b);r.metric(b,10);r.ad(b,1,500);r.up(c);var child=r.child(c);r.ack(r.latestRequest(),6000);
        var stable=r.forwarding(A);r.ad(b,2,1);var request=renew(r,c,child,2,UUID.randomUUID());
        errorReply(r,request,14,ErrorCode.PATH_CHANGED);assertEquals(stable,r.forwarding(A));
        assertEquals(1,r.messages(MessageType.ACTIVATE).size());assertTrue(r.messages(MessageType.DEACTIVATE).isEmpty());
    }}
    @Test void l15OldSessionDeactivateCannotAffectReplacementConnection(){try(var s=new Rig("S",true,4096,3)){
        var old=peer("C1");s.up(old);var first=sourceChild(s,old,A,6000);
        var fresh=new Neighbor.Session(old.nodeId(),old.instanceId(),UUID.randomUUID(),old.endpoint());s.up(fresh);sourceChild(s,fresh,A,6000);var stable=s.forwarding(A);
        deactivate(s,old,first,1);s.act(()->s.router.onNeighborEvent(new NeighborManager.NeighborDown(old,NeighborManager.Reason.TRANSPORT)));
        assertEquals(stable,s.forwarding(A));assertEquals(fresh,s.forwarding(A).downstreams().get("C1").child());
    }}
    @Test void l16DeactivateOneStreamPreservesOtherStreamExactly(){try(var s=new Rig("S",true,4096,3)){
        var c=peer("C1");s.up(c);var one=sourceChild(s,c,A,6000);sourceChild(s,c,B,6000);var healthy=s.forwarding(B);
        deactivate(s,c,one,1);assertFalse(s.router.snapshot().forwarding().containsKey(A),"DEACTIVATE must be scoped to the selected stream");
        assertEquals(healthy,s.forwarding(B));
    }}
    @Test void l18RenewErrorAndTraceErrorDoNotInvalidateHealthyGrants(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.ad(a,B,"S",EPOCH,1,100);r.local(A,UUID.randomUUID());var qa=r.latestRequest();r.local(B,UUID.randomUUID());var qb=r.latestRequest();r.ack(qa,6000);r.ack(qb,6000);
        var beforeA=r.forwarding(A);var beforeB=r.forwarding(B);r.tick(2_000_000_000L);var sent=r.messages(MessageType.SUBSCRIPTION_RENEW);assertEquals(2,sent.size(),"Both independent streams need a renewal");
        var ra=sent.stream().filter(x->((ControlMessage.Activation)x.message.payload()).streamId().equals(A)).findFirst().orElseThrow();
        var rb=sent.stream().filter(x->((ControlMessage.Activation)x.message.payload()).streamId().equals(B)).findFirst().orElseThrow();
        r.error(ra,14,ErrorCode.PATH_CHANGED);r.error(rb,15,ErrorCode.NO_ROUTE);assertEquals(beforeA,r.forwarding(A));assertEquals(beforeB,r.forwarding(B));
        r.ack(rb,6000);assertEquals(beforeA,r.forwarding(A));assertEquals(6_000_000_000L,r.forwarding(B).upstreamLease().orElseThrow().remainingNanos(r.clock.get()));
    }}
    @Test void l19RenewRepliesShareExistingFixedWindowQuota(){try(var s=new Rig("S",true,4096,3)){
        var c=peer("C1");s.up(c);var first=sourceChild(s,c,A,6000);
        for(long revision=2;revision<=17;revision++)renew(s,c,first,revision,UUID.randomUUID());
        assertEquals(16,s.messages(MessageType.ACTIVATE_ACK).size(),"Initial ACK plus renewals must use the same 16-reply window");
        assertTrue(s.load().responseIntents()<=1);assertEquals(1,s.load().histories());s.tick(1_000_000_000L);
        assertEquals(17,s.messages(MessageType.ACTIVATE_ACK).size());
    }}
    @Test void l20LateTimerAdmitsOneCurrentRenewalWithoutCatchup(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;
        r.tick(4_000_000_000L);var sent=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);r.tick(100_000_000L);assertEquals(1,r.messages(MessageType.SUBSCRIPTION_RENEW).size());
        r.ack(sent,6000);sent.result.complete(Connection.SendResult.ACCEPTED);r.pump();assertEquals(1,r.messages(MessageType.SUBSCRIPTION_RENEW).size());
    }}
    @Test void l22ShutdownWaitsForPhysicalRenewalCompletionWithinOriginalBudget(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;r.tick(2_000_000_000L);
        var sent=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);r.router.requestClose();r.owner.drain();assertTrue(r.router.snapshot().forwarding().isEmpty());
        assertFalse(r.router.awaitTermination(Duration.ZERO));sent.result.complete(Connection.SendResult.CLOSED);r.pump();
        assertTrue(r.router.awaitTermination(Duration.ofMillis(10)));assertEquals(0,r.load().sends());
    }}
    @Test void l23OversizedSubscriptionPathIsRejectedBeforeCopyOrIteration(){
        var c=peer("C1");var accesses=new AtomicInteger();
        var oversized=new AbstractList<String>(){public int size(){return 33;}public String get(int index){accesses.incrementAndGet();throw new AssertionError("Path copied before cap validation");}};
        assertThrows(IllegalArgumentException.class,()->new Subscription(A,EPOCH,c,UUID.randomUUID(),1,UUID.randomUUID(),oversized,6000,Subscription.State.PENDING,Optional.empty()));
        assertEquals(0,accesses.get());
    }
    @Test void l05ThreeCyclesUseOneLineAndStrictlyIncreasingRevisions(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());var first=r.latestRequest();r.ack(first,6000);
        var initial=(ControlMessage.Activation)first.message.payload();long revision=initial.revision();var requests=new HashSet<UUID>();requests.add(first.message.requestId());
        for(int cycle=1;cycle<=3;cycle++){
            r.tick(2_000_000_000L);assertEquals(cycle,r.messages(MessageType.SUBSCRIPTION_RENEW).size());
            var renewal=r.messages(MessageType.SUBSCRIPTION_RENEW).getLast();var p=(ControlMessage.Activation)renewal.message.payload();
            assertEquals(initial.subscriptionId(),p.subscriptionId());assertTrue(p.revision()>revision);revision=p.revision();assertTrue(requests.add(renewal.message.requestId()));
            r.ack(renewal,6000);assertEquals(6_000_000_000L,r.forwarding(A).upstreamLease().orElseThrow().remainingNanos(r.clock.get()));
            assertEquals(1,r.load().histories());assertEquals(0,r.load().sends());
        }
    }}
    @Test void l06OneMillisecondGrantUsesSubMillisecondCadenceAndExactExpiry(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),1);
        r.tick(333_332);assertTrue(r.messages(MessageType.SUBSCRIPTION_RENEW).isEmpty());r.tick(1);assertEquals(1,r.messages(MessageType.SUBSCRIPTION_RENEW).size());
        r.tick(666_667);assertNull(r.forwarding(A));assertEquals(1,r.messages(MessageType.SUBSCRIPTION_RENEW).size());
    }}
    @Test void l07OversizedAndExactDeadlineAckDoNotReplaceGrant(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);var previous=r.forwarding(A).upstreamLease();
        r.tick(2_000_000_000L);var renew=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);r.ack(renew,6001);assertEquals(previous,r.forwarding(A).upstreamLease());
        r.tick(3_000_000_000L);r.ack(renew,6000);assertEquals(previous,r.forwarding(A).upstreamLease());
    }}
    @Test void l08PendingDuplicateAndWrongKindCannotReanchor(){try(var r=new Rig("S",true,4096,3)){
        var c=peer("C1");r.up(c);var initial=sourceChild(r,c,A,6000);r.hold=m->m.type()==MessageType.ACTIVATE_ACK;
        var renewal=renew(r,c,initial,2,UUID.randomUUID());r.receive(c,renewal);assertEquals(2,r.messages(MessageType.ACTIVATE_ACK).size());
        var old=r.forwarding(A).downstreams().get("C1");
        r.receive(c,new ControlMessage(MessageType.ACTIVATE,renewal.requestId(),c.nodeId(),c.instanceId(),renewal.payload()));
        assertEquals(ErrorCode.INVALID_MESSAGE,lastError(r));assertEquals(old,r.forwarding(A).downstreams().get("C1"));
        r.messages(MessageType.ACTIVATE_ACK).getLast().result.complete(Connection.SendResult.ACCEPTED);r.pump();
        assertEquals(2,r.forwarding(A).downstreams().get("C1").revision());
    }}
    @Test void l09LatePreviousAckCannotReplaceNewRenewal(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);
        r.tick(2_000_000_000L);var first=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);r.error(first,14,ErrorCode.NO_ROUTE);
        r.tick(2_000_000_000L);var second=r.messages(MessageType.SUBSCRIPTION_RENEW).getLast();assertNotEquals(first.message.requestId(),second.message.requestId());
        r.ack(second,6000);var current=r.forwarding(A);r.ack(first,6000);assertEquals(current,r.forwarding(A));
    }}
    @Test void l09MaximumRevisionNeverWrapsAndEqualNewRequestIsRejected(){try(var r=new Rig("S",true,4096,3)){
        var c=peer("C1");r.up(c);var m=r.activate(c,A,r.instance,UUID.randomUUID(),Long.MAX_VALUE-1,UUID.randomUUID(),List.of("S","C1"),6000);
        var last=renew(r,c,m,Long.MAX_VALUE,UUID.randomUUID());var active=r.forwarding(A);
        var equal=renew(r,c,m,Long.MAX_VALUE,UUID.randomUUID());errorReply(r,equal,14,ErrorCode.STALE_REVISION);assertEquals(active,r.forwarding(A));
        var wrapped=renew(r,c,m,Long.MIN_VALUE,UUID.randomUUID());errorReply(r,wrapped,14,ErrorCode.INVALID_MESSAGE);assertEquals(active,r.forwarding(A));
        r.receive(c,last);assertEquals(active,r.forwarding(A));
    }}
    @Test void l10OldPruneCannotCancelNewActivationOnSameSession(){try(var r=new Rig("S",true,4096,3)){
        var c=peer("C1");r.up(c);var first=sourceChild(r,c,A,6000);var p=(ControlMessage.Activation)first.payload();
        renew(r,c,first,2,UUID.randomUUID());deactivate(r,c,first,2);
        r.activate(c,A,r.instance,p.subscriptionId(),3,UUID.randomUUID(),p.expectedPath(),6000);var replacement=r.forwarding(A);
        deactivate(r,c,first,2);assertEquals(replacement,r.forwarding(A));assertEquals(3,r.forwarding(A).downstreams().get("C1").revision());
    }}
    @Test void l11UnknownHigherPruneUsesLeaseFallbackWithoutApproximateCancellation(){try(var r=new Rig("S",true,4096,3)){
        var c=peer("C1");r.up(c);var first=sourceChild(r,c,A,300);var old=r.forwarding(A);
        deactivate(r,c,first,2);assertEquals(old,r.forwarding(A));r.tick(299_999_999);assertNotNull(r.forwarding(A));r.tick(1);assertNull(r.forwarding(A));
    }}
    @Test void l13RenewBeforeInitialAckAndAfterCancelCannotActivate(){try(var r=new Rig("S",true,4096,3)){
        var c=peer("C1");r.up(c);r.hold=m->m.type()==MessageType.ACTIVATE_ACK;var initial=sourceChild(r,c,A,6000);
        var early=renew(r,c,initial,2,UUID.randomUUID());errorReply(r,early,14,ErrorCode.NO_ROUTE);assertNull(r.forwarding(A));
        r.messages(MessageType.ACTIVATE_ACK).getFirst().result.complete(Connection.SendResult.ACCEPTED);r.pump();assertNotNull(r.forwarding(A));
        deactivate(r,c,initial,1);var late=renew(r,c,initial,2,UUID.randomUUID());errorReply(r,late,14,ErrorCode.NO_ROUTE);assertNull(r.forwarding(A));
    }}
    @Test void l17LocalSendFailurePreservesGrantAndAcceptedAloneDoesNotExtend(){
        for(var failure:List.of(Connection.SendResult.INVALID_MESSAGE,Connection.SendResult.ACCEPTED))try(var r=new Rig()){
            var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);var old=r.forwarding(A).upstreamLease();
            r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;r.tick(2_000_000_000L);exactlyOne(r,MessageType.SUBSCRIPTION_RENEW).result.complete(failure);r.pump();
            assertEquals(old,r.forwarding(A).upstreamLease());r.tick(4_000_000_000L);assertNull(r.forwarding(A));
        }
        try(var r=new Rig()){
            var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);var old=r.forwarding(A).upstreamLease();
            r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;r.tick(2_000_000_000L);exactlyOne(r,MessageType.SUBSCRIPTION_RENEW).result.completeExceptionally(new IllegalStateException("local send"));r.pump();assertEquals(old,r.forwarding(A).upstreamLease());
        }
    }
    @Test void l17PhysicalFailureInvalidatesOnlyDependentSession(){
        for(var failure:List.of(Connection.SendResult.CLOSED,Connection.SendResult.OVERFLOW))try(var r=new Rig()){
            var a=peer("A");var b=peer("B");r.route(a);r.up(b);r.metric(b,10);r.ad(b,B,"T",id("T-epoch"),1,100);
            r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);r.local(B,UUID.randomUUID());r.ack(r.latestRequest(),6000);var other=r.forwarding(B);
            r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;r.tick(2_000_000_000L);
            r.messages(MessageType.SUBSCRIPTION_RENEW).stream().filter(s->s.peer.equals(a)).findFirst().orElseThrow().result.complete(failure);r.pump();
            assertNull(r.forwarding(A));assertEquals(other,r.forwarding(B));
        }
    }
    @Test void l18WrongAckFieldsAndErrorKindsAreIsolated(){try(var r=new Rig()){
        var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);var old=r.forwarding(A);
        r.tick(2_000_000_000L);var renew=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);var p=(ControlMessage.Activation)renew.message.payload();
        for(int type:List.of(11,13,15))r.error(renew,type,ErrorCode.PATH_CHANGED);
        r.receive(a,new ControlMessage(MessageType.ACTIVATE_ACK,renew.message.requestId(),a.nodeId(),a.instanceId(),new ControlMessage.ActivationAck(A,EPOCH,p.subscriptionId(),p.revision()+1,6000)));
        assertEquals(old,r.forwarding(A));assertTrue(r.messages(MessageType.ERROR).isEmpty());r.ack(renew,6000);assertNotEquals(old.upstreamLease(),r.forwarding(A).upstreamLease());
    }}
    @Test void l19RetiredGenerationsShareStablePhysicalContext(){try(var r=new Rig()){
        var a=peer("A");r.route(a);UUID token=UUID.randomUUID();var ticket=r.local(A,token);r.ack(r.latestRequest(),6000);
        r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW||m.type()==MessageType.DEACTIVATE;r.tick(2_000_000_000L);
        var renewal=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);ticket.cancel();r.pump();var prune=exactlyOne(r,MessageType.DEACTIVATE);assertEquals(2,r.load().sends());
        var replacement=r.router.retrySubscription(A,token);r.pump();assertTrue(replacement.result().isEmpty());assertEquals(1,r.messages(MessageType.ACTIVATE).size());
        renewal.result.complete(Connection.SendResult.ACCEPTED);r.pump();assertEquals(2,r.messages(MessageType.ACTIVATE).size());r.ack(r.latestRequest(),6000);
        var healthy=r.forwarding(A);prune.result.complete(Connection.SendResult.ACCEPTED);r.pump();assertEquals(healthy,r.forwarding(A));assertEquals(1,r.load().histories());
    }}
    @Test void l20MonotonicWrapPreservesRenewalCadence(){try(var r=new Rig()){
        r.clock.set(Long.MAX_VALUE-1_000_000_000L);var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);
        r.tick(2_000_000_000L);var renewal=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);r.ack(renewal,6000);assertEquals(6_000_000_000L,r.forwarding(A).upstreamLease().orElseThrow().remainingNanos(r.clock.get()));
    }}
    @Test void l21SourceCatalogSurvivesLastConsumerAndOtherStream(){try(var r=new Rig("S",true,4096,3)){
        assertTrue(r.router.snapshot().forwarding().isEmpty());var catalog=r.router.snapshot().catalog();var c=peer("C1");r.up(c);
        var first=sourceChild(r,c,A,6000);sourceChild(r,c,B,6000);var other=r.forwarding(B);deactivate(r,c,first,1);
        assertNull(r.forwarding(A));assertEquals(other,r.forwarding(B));assertEquals(catalog,r.router.snapshot().catalog());assertTrue(r.messages(MessageType.DEACTIVATE).isEmpty());
    }}
    @Test void l22ShutdownAfterPruneDoesNotSendMoreAndRetainsPhysicalReservation(){try(var r=new Rig()){
        var a=peer("A");r.route(a);var local=r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);r.hold=m->m.type()==MessageType.DEACTIVATE;
        local.cancel();r.pump();var prune=exactlyOne(r,MessageType.DEACTIVATE);r.router.requestClose();r.owner.drain();var count=r.sent.size();
        assertFalse(r.router.awaitTermination(Duration.ZERO));r.tick(8_000_000_000L);assertEquals(count,r.sent.size());prune.result.complete(Connection.SendResult.ACCEPTED);
        assertTrue(r.router.awaitTermination(Duration.ofMillis(10)));assertTrue(r.router.snapshot().forwarding().isEmpty());
    }}
    /** Real managers and monitors share their node's owner; only the bootstrap and transport open sockets. */
    static final class TcpNode implements AutoCloseable {
        final NeighborManager.SerialOwner owner;final NeighborManager manager;final NetworkMonitor monitor;final RoutingService router;
        final List<ControlMessage> received=new CopyOnWriteArrayList<>();final List<Throwable> failures=new CopyOnWriteArrayList<>();
        TcpNode(NodeConfiguration cfg){
            owner=NeighborManager.createSerialOwner(cfg.runtime().overlay().eventsCapacity());var routing=new AtomicReference<RoutingService>();var monitoring=new AtomicReference<NetworkMonitor>();
            UUID instance=UUID.randomUUID();
            manager=new NeighborManager(cfg,instance,owner,event->{
                if(event instanceof NeighborManager.Received e)received.add(e.message());
                var m=monitoring.get();if(m!=null)m.onEvent(event);var r=routing.get();if(r!=null)r.onNeighborEvent(event);
            });
            router=new RoutingService(cfg,instance,owner,manager::send,e->{if(e.fatal())failures.add(new IllegalStateException(e.toString()));});routing.set(router);
            monitor=new NetworkMonitor(cfg,instance,owner,new NetworkMonitor.ControlPort(){
                public CompletionStage<Connection.SendResult> send(Neighbor.Session s,ControlMessage m){return manager.send(s,m);}
                public CompletionStage<Boolean> disconnect(Neighbor.Session s){return manager.disconnect(s);}
            },e->{router.onMetrics(e);if(e instanceof NetworkMonitor.LocalFailure f&&f.fatal())failures.add(new IllegalStateException(e.toString()));});monitoring.set(monitor);
        }
        void start() throws Exception {assertTrue(owner.offer(()->{monitor.start();router.start();}));manager.start().toCompletableFuture().get(5,TimeUnit.SECONDS);}
        Route.Confirmation forwarding(){return router.snapshot().forwarding().get(A);}
        public void close(){
            long began=System.nanoTime(),budget=TimeUnit.SECONDS.toNanos(5);router.requestClose();monitor.requestClose();manager.requestClose();
            assertTrue(router.awaitTermination(Duration.ofNanos(Math.max(0,budget-(System.nanoTime()-began)))));
            assertTrue(monitor.awaitTermination(Duration.ofNanos(Math.max(0,budget-(System.nanoTime()-began)))));
            assertTrue(manager.awaitTermination(Duration.ofNanos(Math.max(0,budget-(System.nanoTime()-began)))));
            owner.requestClose();assertTrue(owner.awaitTermination(Duration.ofNanos(Math.max(0,budget-(System.nanoTime()-began)))));assertTrue(failures.isEmpty(),failures.toString());
        }
    }
    static void tcpAwait(BooleanSupplier condition) throws Exception {
        long began=System.nanoTime();while(!condition.getAsBoolean()){
            if(System.nanoTime()-began>TimeUnit.SECONDS.toNanos(10))fail("Lease TCP condition deadline");Thread.sleep(2);
        }
    }
    @Test @Timeout(45) void l24TcpTwoRelaysAggregateRenewPruneExpireAndReleasePorts() throws Exception {
        var reserved=new ArrayList<ServerSocket>();var ports=new ArrayList<Integer>();
        try{for(int i=0;i<11;i++){var s=new ServerSocket(0,1,InetAddress.getLoopbackAddress());reserved.add(s);ports.add(s.getLocalPort());}}
        finally{for(var s:reserved)s.close();}
        var names=List.of("S","R1","R2","C1","C2");
        var peers=List.of(Set.of("R1"),Set.of("S","R2"),Set.of("R1","C1","C2"),Set.of("R2"),Set.of("R2"));
        var topology=new LinkedHashMap<String,TopologyConfiguration.Node>();
        for(int i=0;i<5;i++)topology.put(names.get(i),new TopologyConfiguration.Node(names.get(i),"127.0.0.1",ports.get(i+1),ports.get(i+6),i==0?NodeRole.SERVER:NodeRole.RELAY,peers.get(i)));
        var bootConfig=new NodeConfiguration(Path.of("bootstrap"),"BOOT",NodeRole.BOOTSTRAP,"127.0.0.1","127.0.0.1",ports.getFirst(),0,
                Optional.empty(),Optional.of(Path.of("topology")),Optional.empty(),Optional.empty(),Optional.of(new TopologyConfiguration(Path.of("topology"),topology)),List.of());
        var nodes=new ArrayList<TcpNode>();
        try(var boot=new BootstrapServer(bootConfig)){
            var d=RuntimeConfiguration.defaults();var dr=d.routing();
            var runtime=new RuntimeConfiguration(d.control(),d.overlay(),d.monitoring(),
                    new RuntimeConfiguration.Routing(Duration.ofMillis(200),Duration.ofSeconds(3),dr.cacheCapacity(),dr.cacheTtl(),dr.floodMaxPerSecond(),Duration.ofSeconds(1),dr.activateAttempts(),dr.switchImprovementPercent(),dr.switchObservations(),dr.switchMinDwell()),
                    new RuntimeConfiguration.Subscription(Duration.ofSeconds(3),Duration.ofMillis(200)),d.video(),d.source());
            for(int i=0;i<5;i++){
                String name=names.get(i);var cfg=new NodeConfiguration(Path.of(name),name,i==0?NodeRole.SERVER:NodeRole.RELAY,"127.0.0.1","127.0.0.1",ports.get(i+1),ports.get(i+6),
                        Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1",ports.getFirst())),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),
                        i==0?config("S",true,4096,3).streams():List.of(),runtime);
                var n=new TcpNode(cfg);nodes.add(n);n.start();
            }
            var source=nodes.get(0);var trunk=nodes.get(1);var fork=nodes.get(2);var c1=nodes.get(3);var c2=nodes.get(4);
            tcpAwait(()->c1.router.snapshot().routes().containsKey(A)&&c1.router.snapshot().routes().get(A).knownUpstream().isPresent()
                    &&c2.router.snapshot().routes().containsKey(A)&&c2.router.snapshot().routes().get(A).knownUpstream().isPresent());
            UUID token1=UUID.randomUUID(),token2=UUID.randomUUID();var one=c1.router.requestSubscription(A,token1);var two=c2.router.requestSubscription(A,token2);
            tcpAwait(()->one.result().isPresent()&&two.result().isPresent());assertEquals(RoutingService.SubscriptionState.SUCCESS,result(one).state());assertEquals(RoutingService.SubscriptionState.SUCCESS,result(two).state());
            tcpAwait(()->fork.forwarding()!=null&&fork.forwarding().downstreams().size()==2);
            long start=System.nanoTime();while(System.nanoTime()-start<TimeUnit.MILLISECONDS.toNanos(3300)){
                assertNotNull(c1.forwarding());assertNotNull(c2.forwarding());assertEquals(Set.of("R1"),source.forwarding().downstreams().keySet());Thread.sleep(10);
            }
            assertTrue(source.received.stream().anyMatch(m->m.type()==MessageType.SUBSCRIPTION_RENEW));
            one.cancel();tcpAwait(()->c1.forwarding()==null&&fork.forwarding()!=null&&fork.forwarding().downstreams().keySet().equals(Set.of("C2")));
            assertNotNull(c2.forwarding());assertEquals(Set.of("R2"),trunk.forwarding().downstreams().keySet());
            two.cancel();tcpAwait(()->nodes.stream().allMatch(n->n.forwarding()==null));
            assertTrue(source.received.stream().anyMatch(m->m.type()==MessageType.DEACTIVATE));
            var replacement=c2.router.retrySubscription(A,token2);tcpAwait(()->replacement.result().isPresent());assertEquals(RoutingService.SubscriptionState.SUCCESS,result(replacement).state());
            c2.router.requestClose();assertTrue(c2.router.awaitTermination(Duration.ofSeconds(1)));assertFalse(c2.manager.snapshot().isEmpty(),"Orphan experiment keeps TCP session alive");
            tcpAwait(()->source.forwarding()==null&&trunk.forwarding()==null&&fork.forwarding()==null);
            assertTrue(source.router.snapshot().catalog().entries().containsKey(A));
        }finally{for(int i=nodes.size()-1;i>=0;i--)nodes.get(i).close();}
        for(int i=0;i<6;i++)try(var socket=new ServerSocket()){socket.setReuseAddress(true);socket.bind(new InetSocketAddress("127.0.0.1",ports.get(i)));}
    }
    @Test void l01SharedTrunkRenewsOncePerCycleAfterPartialDeparture(){try(var r=new Rig()){
        var a=peer("A");var c1=peer("C1");var c2=peer("C2");r.route(a);r.up(c1);r.up(c2);var first=r.child(c1);var second=r.child(c2);r.ack(r.latestRequest(),6000);
        deactivate(r,c1,first,1);deactivate(r,c1,first,1);
        for(int i=1;i<=2;i++){
            r.tick(2_000_000_000L);assertEquals(i,r.messages(MessageType.SUBSCRIPTION_RENEW).size());r.ack(r.messages(MessageType.SUBSCRIPTION_RENEW).getLast(),6000);
            renew(r,c2,second,i+1,UUID.randomUUID());assertEquals(Set.of("C2"),r.forwarding(A).downstreams().keySet());assertTrue(r.messages(MessageType.DEACTIVATE).isEmpty());
        }
        assertEquals(1,r.messages(MessageType.ACTIVATE).size());
    }}
    @Test void l03ChildLeavesBeforeLocalConsumer(){try(var r=new Rig()){
        var a=peer("A");var child=peer("C1");r.route(a);r.up(child);var m=r.child(child);var token=UUID.randomUUID();var local=r.local(A,token);r.ack(r.latestRequest(),6000);
        deactivate(r,child,m,1);assertEquals(Set.of(token),r.forwarding(A).localConsumers());assertTrue(r.forwarding(A).downstreams().isEmpty());assertTrue(r.messages(MessageType.DEACTIVATE).isEmpty());
        local.cancel();r.pump();assertNull(r.forwarding(A));exactlyOne(r,MessageType.DEACTIVATE);
    }}
    @Test void l08TerminalReplayIsBoundedAndFreshRenewCanKeepOldHealthyGrant(){try(var r=new Rig("S",true,4096,3)){
        var c=peer("C1");r.up(c);var first=sourceChild(r,c,A,6000);var old=r.forwarding(A).downstreams().get("C1");
        r.hold=m->m.type()==MessageType.ACTIVATE_ACK;var failed=renew(r,c,first,2,UUID.randomUUID());var ack=r.messages(MessageType.ACTIVATE_ACK).getLast();
        ack.result.complete(Connection.SendResult.INVALID_MESSAGE);r.pump();assertEquals(old,r.forwarding(A).downstreams().get("C1"));
        r.receive(c,failed);assertEquals(old,r.forwarding(A).downstreams().get("C1"));assertEquals(2,r.messages(MessageType.ACTIVATE_ACK).size());
        r.hold=m->false;renew(r,c,first,3,UUID.randomUUID());assertEquals(3,r.forwarding(A).downstreams().get("C1").revision());
    }}
    @Test void l11UnsentPruneExpiresAndNewDemandInvalidatesOldIntent(){
        for(boolean replace:List.of(false,true))try(var r=new Rig()){
            var a=peer("A");r.route(a);UUID token=UUID.randomUUID();var local=r.local(A,token);r.ack(r.latestRequest(),6000);
            r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;r.tick(2_000_000_000L);var first=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);r.error(first,14,ErrorCode.NO_ROUTE);
            r.tick(2_000_000_000L);assertEquals(2,r.load().sends());local.cancel();r.pump();assertTrue(r.messages(MessageType.DEACTIVATE).isEmpty());assertNull(r.forwarding(A));
            if(replace){r.router.retrySubscription(A,token);r.pump();}else r.tick(3_000_000_000L);
            for(var s:r.messages(MessageType.SUBSCRIPTION_RENEW))s.result.complete(Connection.SendResult.ACCEPTED);r.pump();
            assertTrue(r.messages(MessageType.DEACTIVATE).isEmpty());if(replace){r.ack(r.latestRequest(),6000);assertNotNull(r.forwarding(A));}else assertNull(r.forwarding(A));
        }
    }
    @Test void l14SamePathMetricChangeAllowsRenewal(){try(var r=new Rig()){
        var a=peer("A");var child=peer("C1");r.route(a);r.up(child);var m=r.child(child);r.ack(r.latestRequest(),6000);r.metric(a,11);
        var renewal=renew(r,child,m,2,UUID.randomUUID());reply(r,MessageType.ACTIVATE_ACK,renewal.requestId());assertEquals(2,r.forwarding(A).downstreams().get("C1").revision());
    }}
    @Test void l15OldRenewCallbackAndAckCannotInvalidateNewInstance(){try(var r=new Rig()){
        var old=peer("A");r.route(old);UUID token=UUID.randomUUID();r.local(A,token);r.ack(r.latestRequest(),6000);r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;r.tick(2_000_000_000L);var stale=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);
        var endpoint=new ControlMessage.Endpoint("A",UUID.randomUUID(),"127.0.0.1",19001,19101,NodeRole.RELAY);var fresh=new Neighbor.Session("A",endpoint.instanceId(),UUID.randomUUID(),endpoint);
        r.up(fresh);r.metric(fresh,10);r.ad(fresh,2,100);r.router.retrySubscription(A,token);r.pump();r.ack(r.latestRequest(),6000);var healthy=r.forwarding(A);assertNotNull(healthy);
        r.ack(stale,6000);stale.result.complete(Connection.SendResult.CLOSED);r.pump();assertEquals(healthy,r.forwarding(A));
    }}
    @Test void l19SmallSharedHistoryAndLocalCapsPreserveHealthyGrant(){try(var r=new Rig("R",false,2,3)){
        var a=peer("A");var child=peer("C1");r.route(a);r.up(child);var token=UUID.randomUUID();r.local(A,token);r.ack(r.latestRequest(),6000);r.child(child);var healthy=r.forwarding(A);
        var other=peer("C2");r.up(other);r.child(other);assertEquals(ErrorCode.RESOURCE_LIMIT,lastError(r));assertEquals(healthy,r.forwarding(A));assertEquals(2,r.load().histories());
        r.tick(2_000_000_000L);r.ack(exactlyOne(r,MessageType.SUBSCRIPTION_RENEW),6000);assertEquals(2,r.load().histories());assertEquals(Set.of("C1"),r.forwarding(A).downstreams().keySet());
    }}
    @Test void l23SubscriptionCopiesValidPathAndRetainsPublicShape(){
        var c=peer("C1");var path=new ArrayList<>(List.of("S","C1"));var lease=new Subscription.Lease(0,1_000_000L);
        var value=new Subscription(A,EPOCH,c,UUID.randomUUID(),1,UUID.randomUUID(),path,1,Subscription.State.ACTIVE,Optional.of(lease));path.set(0,"other");
        assertEquals(List.of("S","C1"),value.expectedPath());assertThrows(UnsupportedOperationException.class,()->value.expectedPath().clear());
        assertEquals(0,lease.remainingMillis(1));assertEquals(999_999,lease.remainingNanos(1));assertEquals(2,Subscription.State.values().length);
    }
    @Test void l07SubMillisecondParentRemainderIsNeverRoundedUp(){try(var r=new Rig()){
        var a=peer("A");var child=peer("C1");r.route(a);r.up(child);var initial=r.child(child);r.ack(r.latestRequest(),6000);
        var old=r.forwarding(A).downstreams().get("C1");r.tick(5_999_000_001L);
        var renewal=renew(r,child,initial,2,UUID.randomUUID());errorReply(r,renewal,14,ErrorCode.NO_ROUTE);
        assertEquals(old,r.forwarding(A).downstreams().get("C1"));r.tick(999_999);assertNull(r.forwarding(A));
    }}
    @Test void l14BothDivergentPrefixesPreserveConfirmedGrant(){try(var r=new Rig()){
        var a=peer("A");var b=peer("B");var child=peer("C1");r.route(a);r.up(child);var initial=r.child(child);r.ack(r.latestRequest(),6000);var old=r.forwarding(A);
        r.up(b);r.metric(b,1);r.ad(b,2,1);r.tick(2_000_000_000L);r.ad(b,3,1);r.ad(b,4,1);
        assertEquals("B",r.router.snapshot().routes().get(A).knownUpstream().orElseThrow().nodeId());
        var same=renew(r,child,initial,2,UUID.randomUUID());errorReply(r,same,14,ErrorCode.PATH_CHANGED);
        var p=(ControlMessage.Activation)initial.payload();var changed=new ControlMessage(MessageType.SUBSCRIPTION_RENEW,UUID.randomUUID(),child.nodeId(),child.instanceId(),
                new ControlMessage.Activation(A,EPOCH,p.subscriptionId(),2,6000,List.of("S","B","R","C1")));
        r.receive(child,changed);errorReply(r,changed,14,ErrorCode.PATH_CHANGED);assertEquals(old,r.forwarding(A));
    }}
    @Test void l19RenewalsShareFourPendingPerSessionAndNoParallelPool(){try(var r=new Rig()){
        var a=peer("A");var child=peer("C1");r.up(a);r.metric(a,10);r.up(child);var originals=new ArrayList<ControlMessage>();
        for(int i=0;i<5;i++){
            UUID stream=new UUID(33,i);r.ad(a,stream,"S",EPOCH,1,100);
            originals.add(r.activate(child,stream,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","A","R","C1"),6000));r.ack(r.latestRequest(),6000);
        }
        var fifth=r.forwarding(((ControlMessage.Activation)originals.get(4).payload()).streamId());r.hold=m->m.type()==MessageType.ACTIVATE_ACK;
        for(var original:originals)renew(r,child,original,2,UUID.randomUUID());
        assertEquals(4,r.load().pendingChildren());assertEquals(4,r.load().sends());assertEquals(10,r.load().histories());assertEquals(fifth,r.forwarding(fifth.streamId()));
        assertEquals(9,r.messages(MessageType.ACTIVATE_ACK).size());
    }}
    @Test void l19UnboundErrorsShareGlobalAndPerSessionPhysicalCaps(){try(var r=new Rig("S",true,2,3)){
        var a=peer("C1");var b=peer("C2");r.up(a);r.up(b);r.hold=m->m.type()==MessageType.ERROR;
        for(var peer:List.of(a,a,b,b,a)){
            var original=new ControlMessage(MessageType.ACTIVATE,UUID.randomUUID(),peer.nodeId(),peer.instanceId(),
                    new ControlMessage.Activation(A,r.instance,UUID.randomUUID(),1,6000,List.of("S",peer.nodeId())));
            renew(r,peer,original,2,UUID.randomUUID());
        }
        assertEquals(2,r.load().sends());assertEquals(2,r.load().unbound());assertEquals(0,r.load().histories());assertNull(r.forwarding(A));
        r.router.requestClose();r.owner.drain();assertFalse(r.router.awaitTermination(Duration.ZERO));
        for(var error:r.messages(MessageType.ERROR))error.result.complete(Connection.SendResult.ACCEPTED);
        assertTrue(r.router.awaitTermination(Duration.ofMillis(10)));
    }}
}
