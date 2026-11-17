package pt.uminho.esr.ott.routing;

import java.net.*;
import java.io.*;
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

public final class ManualActivation {
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


    // Evidence-only harness. Owner, clock and port are controlled, not sockets.
    static int checks;static boolean interactive;
    static final BufferedReader input=new BufferedReader(new InputStreamReader(System.in));
    static void fail(String detail){throw new AssertionError(detail);}
    static void assertTrue(boolean v){if(!v)fail("Expected true");}
    static void assertFalse(boolean v,String detail){if(v)fail(detail);}
    static void assertFalse(boolean v){assertTrue(!v);}
    static void check(boolean v,String name){if(!v)fail("CHECK_FAIL "+name);System.out.println("CHECK_PASS "+(++checks)+" "+name);}
    static void step(String name,String expected)throws Exception{
        System.out.println("\nSTEP "+name+"\nESPERADO: "+expected);
        if(interactive){System.out.println("Prima Enter para executar; EOF interrompe sem sucesso.");if(input.readLine()==null)throw new EOFException("Manual interrupted");}
    }
    static void show(String label,Rig r,UUID stream){var f=r.forwarding(stream);System.out.println(label+" forwarding="+f+" load="+r.load());}
    static Sent ackFor(Rig r,UUID request){return r.messages(MessageType.ACTIVATE_ACK).stream().filter(s->s.message.requestId().equals(request)).findFirst().orElseThrow();}
    static void cascade()throws Exception{
        step("1 / tronco S-A-R com C1/C2", "Uma ACTIVATE R->A e uma A->S; C1/C2 recebem ACK próprios. Grants não aumentam ao descer.");
        try(var s=new Rig("S",true,4096,3);var a=new Rig("A",false,4096,3);var r=new Rig()){
            var pa=peer("A");var pr=peer("R");var ps=peer("S");var c1=peer("C1");var c2=peer("C2");
            s.up(pa);a.up(ps);a.metric(ps,10);
            a.receive(ps,new ControlMessage(MessageType.STREAM_ANNOUNCE,UUID.randomUUID(),"S",ps.instanceId(),
                    new ControlMessage.Announcement(new ControlMessage.StreamInfo(A,"S","A",1,10,EPOCH),1,0,List.of("S"))));
            a.up(pr);r.route(pa);r.up(c1);r.up(c2);var one=r.child(c1);var two=r.child(c2);
            check(r.forwarding(A)==null&&r.messages(MessageType.ACTIVATE).size()==1,"C1/C2 pending share one R->A request");
            var ra=r.latestRequest();a.receive(pr,ra.message);var as=a.latestRequest();
            check(a.forwarding(A)==null&&a.messages(MessageType.ACTIVATE).size()==1,"A pending, one A->S request");
            s.receive(pa,as.message);var sa=ackFor(s,as.message.requestId());a.tick(500_000_000L);a.receive(ps,sa.message);
            var ar=ackFor(a,ra.message.requestId());r.tick(1_000_000_000L);r.receive(pa,ar.message);
            check(r.forwarding(A).downstreams().keySet().equals(Set.of("C1","C2")),"R confirms exactly C1/C2");
            check(a.forwarding(A).downstreams().keySet().equals(Set.of("R"))&&s.forwarding(A).downstreams().keySet().equals(Set.of("A")),"one confirmed child per trunk");
            for(var m:List.of(one,two))check(ackFor(r,m.requestId()).message.requestId().equals(m.requestId()),"child has its own correlated ACK");
            long sg=((ControlMessage.ActivationAck)sa.message.payload()).grantedLeaseMs(),ag=((ControlMessage.ActivationAck)ar.message.payload()).grantedLeaseMs();
            long rg=((ControlMessage.ActivationAck)ackFor(r,one.requestId()).message.payload()).grantedLeaseMs();
            check(sg==6000&&ag==5500&&rg==4500,"two relays reduce grants 6000->5500->4500 with controlled delay");
            var c3=peer("C3");r.up(c3);r.child(c3);check(r.messages(MessageType.ACTIVATE).size()==1&&a.messages(MessageType.ACTIVATE).size()==1,"late child reuses both trunks");
            show("SOURCE",s,A);show("RELAY_A",a,A);show("RELAY_R",r,A);
        }
    }
    static void cancellation()throws Exception{
        step("2 / M07 cancelamento antes de ACCEPTED", "C2 mantém o lease; C1 não reaparece, reserva 1 até callback/continuação e depois 0; replay STALE_REVISION.");
        try(var s=new Rig("S",true,4096,3)){
            var c1=peer("C1");var c2=peer("C2");s.up(c1);s.up(c2);
            s.activate(c2,A,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","C2"),6000);var old=s.forwarding(A).downstreams().get("C2");
            UUID sub=UUID.randomUUID(),req=UUID.randomUUID();s.hold=m->m.type()==MessageType.ACTIVATE_ACK&&m.requestId().equals(req);
            var request=s.activate(c1,A,EPOCH,sub,1,req,List.of("S","C1"),6000);var ack=ackFor(s,req);show("BEFORE_CANCEL",s,A);
            s.act(()->s.router.cancelPendingChild(c1,A,sub,1));show("AFTER_CANCEL",s,A);
            check(s.load().sends()==1&&s.forwarding(A).downstreams().keySet().equals(Set.of("C2")),"logical cancellation retains physical send");
            ack.result.complete(Connection.SendResult.ACCEPTED);s.pump();show("AFTER_LATE_ACCEPTED",s,A);
            check(s.load().sends()==0&&s.forwarding(A).downstreams().keySet().equals(Set.of("C2")),"late ACCEPTED never authorizes C1; send freed");
            check(old.equals(s.forwarding(A).downstreams().get("C2")),"C2 exact subscription and lease unchanged");
            s.receive(c1,request);check(lastError(s)==ErrorCode.STALE_REVISION,"terminal watermark rejects replay");
        }
    }
    static void isolation()throws Exception{
        step("3 / streams e validade", "Erro/cancelamento em A não altera B; ACK duplicado não renova; restante <1ms não concede; snapshot antigo expira.");
        try(var r=new Rig()){
            var a=peer("A");var c=peer("C1");r.route(a);r.ad(a,B,"S",EPOCH,1,100);r.up(c);
            var ta=r.local(A,UUID.randomUUID());var qa=r.latestRequest();var tb=r.local(B,UUID.randomUUID());var qb=r.latestRequest();r.ack(qa,6000);r.ack(qb,6000);
            var stable=r.forwarding(B);ta.cancel();r.pump();check(r.forwarding(A)==null&&stable.equals(r.forwarding(B)),"cancel A preserves B confirmation");
            r.tick(1_000_000_000L);var request=r.activate(c,B,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","A","R","C1"),6000);
            check(((ControlMessage.ActivationAck)ackFor(r,request.requestId()).message.payload()).grantedLeaseMs()==5000,"child bounded by parent remaining 5000ms");
            r.tick(1_000_000_000L);r.receive(c,request);
            check(((ControlMessage.ActivationAck)r.messages(MessageType.ACTIVATE_ACK).getLast().message.payload()).grantedLeaseMs()==4000,"duplicate grant ages, not renews");
            r.tick(3_999_000_001L);var d=peer("D");r.up(d);r.activate(d,B,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","A","R","D"),6000);
            check(lastError(r)==ErrorCode.NO_ROUTE,"999999ns cannot become a 1ms grant");
            r.tick(999_999L);check(r.forwarding(B)==null&&stable.visibleAt(r.clock.get()).isEmpty(),"live and old snapshots cannot authorize expired B");
        }
    }
    static void divergence()throws Exception{
        step("4 / seleção divergente", "Prefixos via A e B recusados PATH_CHANGED, confirmação de A e lease iguais; zero ACTIVATE/DEACTIVATE adicional.");
        try(var r=new Rig()){
            var a=peer("A");var b=peer("B");var c=peer("C1");r.route(a);r.up(b);r.metric(b,10);r.ad(b,1,500);r.up(c);r.child(c);r.ack(r.latestRequest(),6000);var old=r.forwarding(A);r.ad(b,2,1);
            for(String next:List.of("A","B")){var d=peer("D"+next);r.up(d);r.activate(d,A,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S",next,"R",d.nodeId()),6000);check(lastError(r)==ErrorCode.PATH_CHANGED&&old.equals(r.forwarding(A)),"refusal via "+next+" preserves confirmed branch");}
            check(r.messages(MessageType.ACTIVATE).size()==1&&r.messages(MessageType.DEACTIVATE).isEmpty(),"no activation or pruning caused by refusal");
        }
    }
    static void limitsAndClose()throws Exception{
        step("5 / capacidade e shutdown físico", "E=2 retém dois ACKs físicos após expiração; await(0) false até conclusão; depois true, sem forwarding/timers.");
        try(var s=new Rig("S",true,2,3)){
            s.hold=m->m.type()==MessageType.ACTIVATE_ACK;
            for(String name:List.of("C1","C2")){var c=peer(name);s.up(c);s.activate(c,A,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S",name),6000);}
            check(s.load().sends()==2&&s.forwarding(A)==null,"two held physical ACKs, no authorized children");s.tick(3_000_000_000L);
            check(s.load().sends()==2,"timeout does not recycle physical reservations");s.router.requestClose();s.owner.drain();
            check(!s.router.awaitTermination(Duration.ZERO),"shutdown cannot report physical completion while sends held");
            for(var ack:s.messages(MessageType.ACTIVATE_ACK))ack.result.complete(Connection.SendResult.ACCEPTED);
            check(s.router.awaitTermination(Duration.ofMillis(10))&&s.load().sends()==0&&s.router.snapshot().forwarding().isEmpty()&&s.timers.physicalJobs()==0,"shutdown ends only after physical completion, zero sends/timers/forwarding");
        }
    }
    public static void main(String[] args)throws Exception{
        if(args.length!=1||!Set.of("--interactive","--automatic").contains(args[0]))throw new IllegalArgumentException("--interactive or --automatic required");
        interactive=args[0].equals("--interactive");System.out.println("CODE_SOURCE="+RoutingService.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println("CONTROLLED_CLOCK_OWNER_PORT; NO_NETWORK_MEASUREMENTS; NO_VIDEO; NO_CORE");
        cascade();cancellation();isolation();divergence();limitsAndClose();
        System.out.println("CLEANUP_COMPLETE");System.out.println("MANUAL_PASS CHECKS="+checks);
    }
}
