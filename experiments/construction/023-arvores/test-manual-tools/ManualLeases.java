package pt.uminho.esr.ott.routing;

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

public final class ManualLeases {
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

    static int checks;static boolean interactive;
    static final java.io.BufferedReader input=new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
    static void fail(String detail){throw new AssertionError(detail);}
    static void fail(Throwable detail){throw new AssertionError(detail);}
    static void assertTrue(boolean value){assertTrue(value,"Expected true");}
    static void assertTrue(boolean value,String detail){if(!value)fail(detail);}
    static void assertFalse(boolean value){assertTrue(!value,"Expected false");}
    static void assertFalse(boolean value,String detail){assertTrue(!value,detail);}
    static void assertNotNull(Object value){assertTrue(value!=null,"Expected non-null");}
    static void assertEquals(Object expected,Object actual){assertEquals(expected,actual,"Equality");}
    static void assertEquals(Object expected,Object actual,String detail){assertTrue(Objects.equals(expected,actual),detail+": expected="+expected+" actual="+actual);}
    static void check(boolean value,String name){if(!value)fail("CHECK_FAIL "+name);System.out.println("CHECK_PASS "+(++checks)+" "+name);}
    static void step(String title,String expected)throws Exception{
        System.out.println("\nSTEP "+title+"\nESPERADO: "+expected);
        if(interactive){System.out.println("Prima Enter para executar; EOF interrompe sem aprovação.");if(input.readLine()==null)throw new java.io.EOFException("Manual interrupted");}
    }
    static void show(String label,Rig r){System.out.println(label+" t="+r.clock.get()+" forwarding="+r.forwarding(A)+" physical="+r.load());}
    static void renewCycles()throws Exception{
        step("1 / renovação agregada", "C1/C2 partilham um tronco. Três ciclos, mesmo subscriptionId, revisões e requestId novos. Ainda autorizado em t=6s.");
        try(var r=new Rig()){
            var a=peer("A");var c1=peer("C1");var c2=peer("C2");r.route(a);r.up(c1);r.up(c2);var one=r.child(c1);var two=r.child(c2);r.ack(r.latestRequest(),6000);
            var original=(ControlMessage.Activation)r.latestRequest().message.payload();long revision=original.revision();var requests=new HashSet<UUID>();
            check(r.messages(MessageType.ACTIVATE).size()==1&&r.forwarding(A).downstreams().size()==2,"one trunk for two children");
            for(int i=1;i<=3;i++){
                r.tick(2_000_000_000L);var request=r.messages(MessageType.SUBSCRIPTION_RENEW).getLast();var p=(ControlMessage.Activation)request.message.payload();
                check(r.messages(MessageType.SUBSCRIPTION_RENEW).size()==i&&p.subscriptionId().equals(original.subscriptionId())&&p.revision()>revision&&requests.add(request.message.requestId()),"cycle "+i+" stable ID/new revision/new request/no fanout");
                revision=p.revision();r.ack(request,6000);renew(r,c1,one,i+1,UUID.randomUUID());renew(r,c2,two,i+1,UUID.randomUUID());show("CYCLE "+i,r);
            }
            check(r.clock.get()==6_000_000_000L&&r.forwarding(A).downstreams().keySet().equals(Set.of("C1","C2")),"demand authorized beyond original expiry");
        }
    }
    static void partialFinalAndLateChild()throws Exception{
        step("2 / saída parcial/final e callback cancelado", "C1 nunca reaparece após ACCEPTED tardio. C2 e lease intactos. Último sai sem apagar catálogo da fonte.");
        try(var r=new Rig("S",true,4096,3)){
            var c1=peer("C1");var c2=peer("C2");r.up(c1);r.up(c2);var one=sourceChild(r,c1,A,6000);var two=sourceChild(r,c2,A,6000);var healthy=r.forwarding(A).downstreams().get("C2");
            UUID request=UUID.randomUUID();r.hold=m->m.type()==MessageType.ACTIVATE_ACK&&m.requestId().equals(request);renew(r,c1,one,2,request);var ack=reply(r,MessageType.ACTIVATE_ACK,request);
            deactivate(r,c1,one,2);show("AFTER_CANCEL_C1",r);
            check(r.forwarding(A).downstreams().keySet().equals(Set.of("C2"))&&r.forwarding(A).downstreams().get("C2").equals(healthy),"partial cancellation preserves sibling and exact lease");
            check(r.load().sends()==1,"cancelled response remains physically reserved");ack.result.complete(Connection.SendResult.ACCEPTED);r.pump();show("AFTER_LATE_ACCEPTED",r);
            check(r.forwarding(A).downstreams().keySet().equals(Set.of("C2"))&&r.forwarding(A).downstreams().get("C2").equals(healthy)&&r.load().sends()==0,"late acceptance cannot restore C1; physical completion releases B");
            deactivate(r,c2,two,1);check(r.forwarding(A)==null&&r.router.snapshot().catalog().entries().containsKey(A)&&r.messages(MessageType.DEACTIVATE).isEmpty(),"source last departure preserves offer; no parent prune");
        }
    }
    static void replacement()throws Exception{
        step("3 / poda e substituta", "13 captura a maior revisão14 submetida. Forwarding sai antes da conclusão física. ACK/callback/ticket antigos não alteram a substituta.");
        try(var r=new Rig()){
            var a=peer("A");r.route(a);UUID token=UUID.randomUUID();var old=r.local(A,token);r.ack(r.latestRequest(),6000);
            r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW;r.tick(2_000_000_000L);var pending=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);var p=(ControlMessage.Activation)pending.message.payload();old.cancel();r.pump();
            var prune=(ControlMessage.Deactivation)exactlyOne(r,MessageType.DEACTIVATE).message.payload();show("RETIRED",r);
            check(r.forwarding(A)==null&&prune.revision()==p.revision()&&prune.subscriptionId().equals(p.subscriptionId()),"prune uses highest submitted revision; zero authorization immediately");
            check(r.load().sends()==1,"retired renewal still physical");var fresh=r.router.retrySubscription(A,token);r.pump();r.ack(r.latestRequest(),6000);var stable=r.forwarding(A);
            check(result(fresh).state()==RoutingService.SubscriptionState.SUCCESS&&stable.localConsumers().contains(token),"explicit replacement confirmed");
            r.ack(pending,6000);pending.result.complete(Connection.SendResult.ACCEPTED);r.pump();old.cancel();r.pump();show("REPLACEMENT_AFTER_OLD_CALLBACK",r);
            check(stable.equals(r.forwarding(A))&&r.load().sends()==0,"old ACK/ACCEPTED/cancel leave replacement unchanged");
        }
    }
    static void orphanAndShortLease()throws Exception{
        step("4 / órfão e grant curto", "Órfão autorizado em G-1 e ausente em G sem rede nova. Grant1ms dispara em333333ns e expira em1ms sem ACK.");
        try(var r=new Rig("S",true,4096,3)){
            var c=peer("C1");r.up(c);sourceChild(r,c,A,300);r.tick(299_999_999);check(r.forwarding(A)!=null,"orphan valid at G-1");r.tick(1);show("ORPHAN_EXPIRED",r);
            check(r.forwarding(A)==null,"orphan excluded exactly at G without new network event");
        }
        try(var r=new Rig()){
            var a=peer("A");r.route(a);r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),1);r.tick(333_332);check(r.messages(MessageType.SUBSCRIPTION_RENEW).isEmpty(),"no early short-grant cycle");
            r.tick(1);check(r.messages(MessageType.SUBSCRIPTION_RENEW).size()==1,"one renewal at floor(1ms/3)");r.tick(666_667);
            check(r.forwarding(A)==null&&r.messages(MessageType.SUBSCRIPTION_RENEW).size()==1,"expiry without ACK; no catchup burst");
        }
    }
    static void physicalLimitsAndShutdown()throws Exception{
        step("5 / limites físicos e shutdown", "Duas gerações ocupam2/Line. Terceira aguarda sem pool novo. Shutdown não envia mais; await(0)=false até acabar callbacks.");
        try(var r=new Rig()){
            var a=peer("A");r.route(a);UUID token=UUID.randomUUID();var old=r.local(A,token);r.ack(r.latestRequest(),6000);r.hold=m->m.type()==MessageType.SUBSCRIPTION_RENEW||m.type()==MessageType.DEACTIVATE;
            r.tick(2_000_000_000L);var renewal=exactlyOne(r,MessageType.SUBSCRIPTION_RENEW);old.cancel();r.pump();var prune=exactlyOne(r,MessageType.DEACTIVATE);
            check(r.load().sends()==2,"renew/prune share exactly two physical slots per Line");r.router.retrySubscription(A,token);r.pump();
            check(r.messages(MessageType.ACTIVATE).size()==1,"new generation cannot evade retired slots");int count=r.sent.size();r.router.requestClose();r.owner.drain();
            check(!r.router.awaitTermination(Duration.ZERO)&&r.router.snapshot().forwarding().isEmpty(),"logical cleanup does not claim physical termination");r.tick(8_000_000_000L);
            check(r.sent.size()==count,"zero submissions after shutdown");renewal.result.complete(Connection.SendResult.ACCEPTED);prune.result.complete(Connection.SendResult.ACCEPTED);r.pump();
            check(r.router.awaitTermination(Duration.ofMillis(10))&&r.load().sends()==0&&r.timers.physicalJobs()==0,"all physical slots and timer jobs terminate within original budget");
        }
    }
    public static void main(String[] args)throws Exception{
        if(args.length!=1||!Set.of("--interactive","--automatic").contains(args[0]))throw new IllegalArgumentException("--interactive or --automatic required");
        interactive=args[0].equals("--interactive");System.out.println("CODE_SOURCE="+RoutingService.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println("STEPS1_5_CONTROLLED_OWNER_CLOCK_PORT; STEP6_REAL_TCP_LOOPBACK; NO_VIDEO_GUI_CORE");
        renewCycles();partialFinalAndLateChild();replacement();orphanAndShortLease();physicalLimitsAndShutdown();
        step("6 / TCP real S-R1-R2-(C1,C2)","Lease3000ms/renew200ms. Mantém dois consumidores por3300ms, sai um, sai último, reativa e expira órfão com TCP vivo. Fecha owners/timers/sockets e confirma6 portas TCP disponíveis.");
        tcpIntegration();System.out.println("CLEANUP_COMPLETE");System.out.println("MANUAL_PASS CHECKS="+checks);
    }
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
    static void tcpIntegration() throws Exception {
        var reserved=new ArrayList<ServerSocket>();var ports=new ArrayList<Integer>();
        try{for(int i=0;i<11;i++){var s=new ServerSocket(0,1,InetAddress.getLoopbackAddress());reserved.add(s);ports.add(s.getLocalPort());}}
        finally{for(var s:reserved)s.close();}
        System.out.println("TCP_CONTROL_PORTS="+ports.subList(0,6)+" DATA_ENDPOINT_PORTS="+ports.subList(6,11));
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
            check(source.received.stream().anyMatch(m->m.type()==MessageType.SUBSCRIPTION_RENEW),"TCP renewals observed after 3300ms with 3000ms grant");
            one.cancel();tcpAwait(()->c1.forwarding()==null&&fork.forwarding()!=null&&fork.forwarding().downstreams().keySet().equals(Set.of("C2")));
            assertNotNull(c2.forwarding());assertEquals(Set.of("R2"),trunk.forwarding().downstreams().keySet());
            check(c2.forwarding()!=null&&fork.forwarding().downstreams().keySet().equals(Set.of("C2")),"TCP partial departure preserves C2 and trunk");
            two.cancel();tcpAwait(()->nodes.stream().allMatch(n->n.forwarding()==null));
            assertTrue(source.received.stream().anyMatch(m->m.type()==MessageType.DEACTIVATE));
            check(nodes.stream().allMatch(n->n.forwarding()==null),"TCP final departure prunes all authorizations");
            var replacement=c2.router.retrySubscription(A,token2);tcpAwait(()->replacement.result().isPresent());assertEquals(RoutingService.SubscriptionState.SUCCESS,result(replacement).state());
            c2.router.requestClose();assertTrue(c2.router.awaitTermination(Duration.ofSeconds(1)));assertFalse(c2.manager.snapshot().isEmpty(),"Orphan experiment keeps TCP session alive");
            check(!c2.manager.snapshot().isEmpty(),"TCP orphan keeps live session while routing is stopped");
            tcpAwait(()->source.forwarding()==null&&trunk.forwarding()==null&&fork.forwarding()==null);
            check(source.forwarding()==null&&trunk.forwarding()==null&&fork.forwarding()==null,"TCP orphan expires through two relays");
            assertTrue(source.router.snapshot().catalog().entries().containsKey(A));
        }finally{for(int i=nodes.size()-1;i>=0;i--)nodes.get(i).close();}
        check(nodes.stream().allMatch(n->n.failures.isEmpty()),"TCP routing/monitor/manager/owner physical shutdown complete");
        for(int i=0;i<6;i++)try(var socket=new ServerSocket()){socket.setReuseAddress(true);socket.bind(new InetSocketAddress("127.0.0.1",ports.get(i)));}
            check(true,"six TCP control ports rebound after shutdown");
    }
}
