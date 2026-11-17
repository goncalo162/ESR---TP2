package pt.uminho.esr.ott.overlay;

import java.net.*;
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

/** Evidence-only integration against the frozen JAR via existing package factory; no reflection. */
public final class TcpActivationProbe {
    static final UUID A=new UUID(1,1),B=new UUID(1,2);
    static int checks;
    static void check(boolean ok,String label){if(!ok)throw new AssertionError("CHECK_FAIL "+label);System.out.println("CHECK_PASS "+(++checks)+" "+label);}
    static void until(BooleanSupplier p,String label)throws Exception{
        long start=System.nanoTime();while(!p.getAsBoolean()){
            if(System.nanoTime()-start>TimeUnit.SECONDS.toNanos(5))throw new AssertionError("Deadline: "+label);Thread.sleep(2);
        }
    }
    static boolean success(RoutingService.SubscriptionTicket t){return t.result().map(v->v.state()==RoutingService.SubscriptionState.SUCCESS).orElse(false);}
    public static void main(String[] args)throws Exception {
        System.out.println("CODE_SOURCE="+RoutingService.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println("REAL_LOOPBACK_TCP; S-A-R-(C1,C2); NO_VIDEO; NO_CORE; AGENT_TECHNICAL_PROBE");
        var names=List.of("S","A","R","C1","C2");var reserved=new ArrayList<ServerSocket>();var ports=new ArrayList<Integer>();
        try{for(int i=0;i<11;i++){var s=new ServerSocket(0,1,InetAddress.getByName("127.0.0.1"));reserved.add(s);ports.add(s.getLocalPort());}}
        finally{for(var s:reserved)s.close();}
        System.out.println("PORTS="+ports);
        var adjacency=Map.of("S",Set.of("A"),"A",Set.of("S","R"),"R",Set.of("A","C1","C2"),"C1",Set.of("R"),"C2",Set.of("R"));
        var topology=new LinkedHashMap<String,TopologyConfiguration.Node>();
        for(int i=0;i<5;i++){String n=names.get(i);topology.put(n,new TopologyConfiguration.Node(n,"127.0.0.1",ports.get(1+i),ports.get(6+i),i==0?NodeRole.SERVER:NodeRole.RELAY,adjacency.get(n)));}
        var bootCfg=new NodeConfiguration(Path.of("probe-bootstrap"),"BOOT",NodeRole.BOOTSTRAP,"127.0.0.1","127.0.0.1",ports.getFirst(),0,
                Optional.empty(),Optional.of(Path.of("probe-topology")),Optional.empty(),Optional.empty(),Optional.of(new TopologyConfiguration(Path.of("probe-topology"),topology)),List.of());
        var nodes=new LinkedHashMap<String,OverlayNode>();var routers=new ConcurrentHashMap<String,RoutingService>();
        var sends=new ConcurrentHashMap<String,AtomicInteger>();var owners=new ConcurrentHashMap<String,NeighborManager.SerialOwner>();
        var boot=new BootstrapServer(bootCfg);
        try{
            for(int i=0;i<5;i++){
                String name=names.get(i);boolean source=i==0;
                var streams=source?List.of(new StreamConfiguration(Path.of("probe-streams"),A,"S","A",Path.of("a.Mjpeg"),10,false),new StreamConfiguration(Path.of("probe-streams"),B,"S","B",Path.of("b.Mjpeg"),10,false)):List.<StreamConfiguration>of();
                var cfg=new NodeConfiguration(Path.of("probe-"+name),name,source?NodeRole.SERVER:NodeRole.RELAY,"127.0.0.1","127.0.0.1",ports.get(1+i),ports.get(6+i),
                        Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1",ports.getFirst())),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),streams);
                var node=new OverlayNode(cfg,UUID.randomUUID(),e->{},NetworkMonitor::new,(c,id,owner,port,observer)->{
                    owners.put(c.nodeId(),owner);
                    var r=new RoutingService(c,id,owner,(session,msg)->{
                        if(msg.type()==MessageType.ACTIVATE){var a=(ControlMessage.Activation)msg.payload();sends.computeIfAbsent(c.nodeId()+"->"+session.nodeId()+":"+a.streamId(),k->new AtomicInteger()).incrementAndGet();}
                        return port.send(session,msg);
                    },observer);routers.put(c.nodeId(),r);return r;
                });nodes.put(name,node);
            }
            for(var n:nodes.values())n.start().toCompletableFuture().get(5,TimeUnit.SECONDS);
            until(()->names.stream().allMatch(n->nodes.get(n).snapshot().size()==adjacency.get(n).size()),"neighbors");
            until(()->List.of("C1","C2").stream().allMatch(n->nodes.get(n).routingSnapshot().routes().containsKey(A)&&nodes.get(n).routingSnapshot().routes().get(A).selectedCandidate().isPresent()),"measured selected routes");
            check(nodes.values().stream().allMatch(n->n.routingSnapshot().forwarding().isEmpty()),"discovery/announcements/probes do not create demand");
            var one=routers.get("C1").requestSubscription(A,UUID.randomUUID());var two=routers.get("C2").requestSubscription(A,UUID.randomUUID());
            until(()->success(one)&&success(two),"both subscribers confirmed");
            until(()->{var f=nodes.get("R").routingSnapshot().forwarding().get(A);return f!=null&&f.downstreams().keySet().equals(Set.of("C1","C2"));},"R children published");
            check(nodes.get("S").routingSnapshot().forwarding().get(A).downstreams().keySet().equals(Set.of("A")),"source has one child A");
            check(nodes.get("A").routingSnapshot().forwarding().get(A).downstreams().keySet().equals(Set.of("R")),"A has one child R");
            for(String edge:List.of("C1->R","C2->R","R->A","A->S"))check(sends.get(edge+":"+A).get()==1,"exactly one ACTIVATE "+edge+" for A");
            check(nodes.get("C1").routingSnapshot().forwarding().get(A).path().equals(List.of("S","A","R","C1")),"two-relay confirmed path");
            var preserved=nodes.get("R").routingSnapshot().forwarding().get(A);
            var second=routers.get("C1").requestSubscription(B,UUID.randomUUID());until(()->success(second),"second stream");
            check(preserved.equals(nodes.get("R").routingSnapshot().forwarding().get(A)),"B activation preserves exact A branch/leases");
            second.cancel();until(()->!nodes.get("C1").routingSnapshot().forwarding().containsKey(B),"cancel B local");
            check(preserved.equals(nodes.get("R").routingSnapshot().forwarding().get(A)),"B cancellation preserves A");
            one.cancel();until(()->!nodes.get("C1").routingSnapshot().forwarding().containsKey(A),"cancel C1 local");
            check(success(two)&&nodes.get("C2").routingSnapshot().forwarding().containsKey(A),"cancel C1 preserves C2 local confirmation");
            System.out.println("Q_C_34: parent may retain cancelled remote branch until lease; no pruning/RENEW claim");
            System.out.println("ACTIVATE_COUNTS="+sends);
            for(var entry:nodes.entrySet())System.out.println("SNAPSHOT "+entry.getKey()+" "+entry.getValue().routingSnapshot().forwarding());
        }finally{
            RuntimeException failure=null;
            var reverse=new ArrayList<>(nodes.values());Collections.reverse(reverse);
            for(var n:reverse)try{n.close();}catch(RuntimeException e){if(failure==null)failure=e;else failure.addSuppressed(e);}
            try{boot.close();}catch(RuntimeException e){if(failure==null)failure=e;else failure.addSuppressed(e);}
            if(failure!=null)throw failure;
        }
        check(nodes.values().stream().allMatch(n->n.awaitTermination(Duration.ZERO)),"all node components physically terminated");
        check(nodes.values().stream().allMatch(n->n.failure().isEmpty()),"no node fatal failures");
        for(var entry:routers.entrySet()){
            var v=new CompletableFuture<Integer>();var o=owners.get(entry.getKey());
            // The owner is already terminated: public lifecycle is the authoritative physical check.
            check(entry.getValue().awaitTermination(Duration.ZERO),"routing termination "+entry.getKey());
        }
        for(int i=0;i<6;i++)try(var socket=new ServerSocket()){socket.setReuseAddress(true);socket.bind(new InetSocketAddress("127.0.0.1",ports.get(i)));}
        check(true,"all six control/bootstrap TCP ports reusable after cleanup");
        System.out.println("CLEANUP_COMPLETE");System.out.println("PROBE_PASS CHECKS="+checks);
    }
}
