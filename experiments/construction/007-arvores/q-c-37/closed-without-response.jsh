import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.time.*;
import java.nio.file.*;
import java.net.*;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.transport.*;
Object field(Object x,String name) throws Exception {var f=x.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(x);}
void setField(Object x,String name,Object value) throws Exception {var f=x.getClass().getDeclaredField(name);f.setAccessible(true);f.set(x,value);}
Object call(Object x,String name,Class<?>[] types,Object... args) throws Exception {var m=x.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(x,args);}
void pump(Object f) throws Exception {call(f,"pump",new Class<?>[0]);}
void require(boolean condition,String detail) {if(!condition)throw new AssertionError(detail);}
void controlled(boolean closedEvent) throws Exception {
    var k=Class.forName("pt.uminho.esr.ott.overlay.NeighborManagerTest$Fixture");
    var ctor=k.getDeclaredConstructor(String.class);ctor.setAccessible(true);var f=ctor.newInstance("A");
    try {
        call(f,"ready",new Class<?>[]{String.class},"B");var raw=call(f,"outbound",new Class<?>[]{boolean.class},false);
        var id=(UUID)call(raw,"id",new Class<?>[0]);var manager=(NeighborManager)field(f,"manager");
        var contexts=(Map<?,?>)field(manager,"contexts");var context=contexts.get(id);
        var deadline=field(context,"deadline");var attempt=field(context,"attempt");
        var io=field(f,"io");var sink=field(io,"sink");var clock=(AtomicLong)field(f,"clock");
        var timers=field(f,"timers");var owner=field(f,"owner");
        var events=(List<NeighborManager.Event>)field(f,"events");events.clear();
        call(raw,"written",new Class<?>[0]);setField(raw,"open",false);pump(f);
        require(contexts.get(id)==context,"write callback must defer, not discard handshake");
        require(events.isEmpty(),"no fabricated terminal diagnostic from deferred callback");
        long began=clock.get();
        var config=(NodeConfiguration)field(field(manager,"plan"),"config");long budget=config.runtime().overlay().helloTimeout().toNanos();
        if(closedEvent)call(sink,"closed",new Class<?>[]{Class.forName("pt.uminho.esr.ott.overlay.NeighborManager$Channel"),Connection.CloseReason.class,long.class},raw,Connection.CloseReason.PEER_EOF,clock.get());
        else call(timers,"advance",new Class<?>[]{long.class},budget);
        pump(f);
        var diagnostics=events.stream().filter(e->e instanceof NeighborManager.Diagnostic).map(e->(NeighborManager.Diagnostic)e).toList();
        require(diagnostics.size()==1,"exactly one terminal diagnostic");
        var expected=closedEvent?NeighborManager.Reason.TRANSPORT:NeighborManager.Reason.TIMEOUT;
        require(diagnostics.getFirst().reason()==expected,"terminal path must match existing Closed/deadline");
        require(diagnostics.getFirst().connectionId().equals(Optional.of(id)),"same connection");
        require(diagnostics.getFirst().remoteMessage().isEmpty()&&diagnostics.getFirst().session().isEmpty(),"no fabricated reply/Session");
        require(contexts.isEmpty(),"context released");require((int)field(manager,"retained")==0,"retained frames released");
        require(field(context,"deadline")==null&&!(boolean)field(deadline,"live"),"old handshake timer canceled");
        require(!(boolean)field(attempt,"live"),"old attempt retired");
        require(((Map<?,?>)field(manager,"nativeDials")).isEmpty(),"native dial registry released");
        var peer=((Map<?,?>)field(manager,"peers")).get("B");
        require(field(peer,"context")==null&&field(peer,"attempt")==null&&field(peer,"session")==null,"peer references released");
        require(field(peer,"slot")!=null,"only existing bounded retry remains");
        require(manager.snapshot().isEmpty()&&!(boolean)field(owner,"stopped"),"no activation/global failure");
        System.out.println("CONTROLLED_OK path="+(closedEvent?"Closed":"existing deadline")+" connection="+id+" reason="+expected+" contexts=0 retained=0 nativeDials=0 oldAttemptLive=false oldDeadlineLive=false peerContext=null peerAttempt=null Session=empty retry=existing elapsedNanos="+(clock.get()-began)+" configuredHelloBudgetNanos="+budget);
    } finally {((AutoCloseable)f).close();}
}
void until(java.util.function.BooleanSupplier condition,Duration budget,String detail) throws Exception {long began=System.nanoTime();while(!condition.getAsBoolean()){require(System.nanoTime()-began<budget.toNanos(),detail);Thread.sleep(5);}}
void realTcp() throws Exception {
    System.setProperty("ott.evidence.directory",Path.of("../experiments/construction/007-arvores/q-c-37/no-response-deployments").toAbsolutePath().toString());
    var k=Class.forName("pt.uminho.esr.ott.app.OttApplicationTest$LiveDeployment");var ctor=k.getDeclaredConstructor();ctor.setAccessible(true);var d=ctor.newInstance();
    var profiles=(List<Path>)field(d,"profiles");int[] ports=(int[])field(d,"ports");
    var bootConfig=ConfigurationLoader.load(profiles.getFirst(),NodeRole.BOOTSTRAP);
    var config=ConfigurationLoader.load(profiles.get(1),NodeRole.SERVER);
    var events=new CopyOnWriteArrayList<NeighborManager.Event>();
    var manager=new NeighborManager(config,UUID.randomUUID(),e->{events.add(e);System.out.println("REAL_EVENT "+e);});
    try(var boot=new pt.uminho.esr.ott.bootstrap.BootstrapServer(bootConfig);var eofPeer=new ServerSocket(ports[2],8,InetAddress.getLoopbackAddress());var silentPeer=new ServerSocket(ports[3],8,InetAddress.getLoopbackAddress())) {
        eofPeer.setSoTimeout(4000);silentPeer.setSoTimeout(4000);
        manager.start().toCompletableFuture().get(3,TimeUnit.SECONDS);
        try(var eof=eofPeer.accept();var silent=silentPeer.accept()) {
            eof.setSoTimeout(2000);silent.setSoTimeout(2000);
            var helloEof=MessageCodec.read(eof.getInputStream()).orElseThrow();var helloSilent=MessageCodec.read(silent.getInputStream()).orElseThrow();
            require(helloEof.type()==MessageType.HELLO&&helloSilent.type()==MessageType.HELLO,"both peers receive HELLO");
            System.out.println("REAL_NO_REPLY peer=N1 request="+helloEof.requestId()+" socket="+eof.getRemoteSocketAddress()+" action=close-without-write");
            System.out.println("REAL_NO_REPLY peer=N2 request="+helloSilent.requestId()+" socket="+silent.getRemoteSocketAddress()+" action=remain-open-without-write helloBudget="+config.runtime().overlay().helloTimeout());
            // Close listeners after accept so existing retries cannot create another connected socket.
            eofPeer.close();silentPeer.close();eof.close();
            until(()->events.stream().anyMatch(e->e instanceof NeighborManager.Diagnostic x&&x.reason()==NeighborManager.Reason.TRANSPORT),config.runtime().overlay().helloTimeout(),"Closed must terminate negotiation");
            until(()->events.stream().anyMatch(e->e instanceof NeighborManager.Diagnostic x&&x.reason()==NeighborManager.Reason.TIMEOUT),config.runtime().overlay().helloTimeout(),"existing deadline must terminate silent negotiation");
            require(silent.getInputStream().read()==-1,"silent peer observes node's physical close at handshake deadline");
            var io=field(manager,"io");var tcp=(Transport)((AtomicReference<?>)field(io,"transport")).get();
            until(()->tcp.connectionCount()==0,config.runtime().control().shutdownTimeout(),"all native socket reservations released");
            require(manager.snapshot().isEmpty(),"no negative/silent peer became ACTIVE");
            manager.requestClose();require(manager.awaitTermination(config.runtime().control().shutdownTimeout()),"owner/timer/I/O/transport terminate within existing shutdown budget");
            require(tcp.connectionCount()==0,"no socket reservations after termination");
            System.out.println("REAL_TCP_OK Closed-and-existing-deadline; no response bytes; nativeConnectionCount=0; managerAwaitTermination=true; configuredHelloBudget="+config.runtime().overlay().helloTimeout()+" configuredShutdownBudget="+config.runtime().control().shutdownTimeout());
        }
    } finally {manager.requestClose();require(manager.awaitTermination(config.runtime().control().shutdownTimeout()),"final termination");((AutoCloseable)d).close();}
}
try {controlled(true);controlled(false);realTcp();System.out.println("NO_RESPONSE_PROBE_OK");}catch(Throwable e){e.printStackTrace();System.out.println("NO_RESPONSE_PROBE_FAILED");}
/exit
