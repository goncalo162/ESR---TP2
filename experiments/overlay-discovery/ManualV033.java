import pt.uminho.esr.ott.bootstrap.BootstrapServer;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.protocol.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Standalone manual API/CLI exercise against the verified JAR; no JUnit or production changes. */
public class ManualV033 {
    static final UUID ZERO=new UUID(0,0);
    static int checks;
    static Path evidence,jar;
    static void check(boolean ok,String label) {if(!ok)throw new AssertionError(label);checks++;System.out.println("CHECK_PASS "+label);}
    static String sha(Path p)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(p)));}
    static void waitFor(String label,long seconds,BooleanSupplier condition)throws Exception {
        long began=System.nanoTime(),last=began;
        while(!condition.getAsBoolean()) {
            if(System.nanoTime()-began>TimeUnit.SECONDS.toNanos(seconds))throw new AssertionError("Timeout: "+label);
            if(System.nanoTime()-last>TimeUnit.SECONDS.toNanos(10)){System.out.println("WAIT "+label);last=System.nanoTime();}
            Thread.sleep(50);
        }
        check(true,label);
    }
    static final class Graph implements AutoCloseable {
        final String label;
        final Map<String,Set<String>> edges;
        final Map<String,NodeConfiguration> configs=new LinkedHashMap<>();
        final Map<String,OverlayNode> nodes=new LinkedHashMap<>();
        final Set<String> stopped=new HashSet<>();
        final List<Integer> ports=new ArrayList<>();
        final Path dir;
        BootstrapServer bootstrap;
        Graph(String label,Map<String,Set<String>> edges)throws Exception {
            this.label=label;this.edges=edges;dir=Files.createDirectory(evidence.resolve(label));
            var reservations=new ArrayList<ServerSocket>();
            try {for(int i=0;i<edges.size()*2+1;i++){var s=new ServerSocket(0,1,InetAddress.getByName("127.0.0.1"));reservations.add(s);ports.add(s.getLocalPort());}}
            finally {for(var s:reservations)s.close();}
            var topology=new LinkedHashMap<String,TopologyConfiguration.Node>();int index=0;
            for(var entry:edges.entrySet()) {
                String id=entry.getKey();int tcp=ports.get(1+2*index),udp=ports.get(2+2*index);index++;
                var role=id.equals("S")?NodeRole.SERVER:id.equals("C")?NodeRole.CLIENT:NodeRole.RELAY;
                topology.put(id,new TopologyConfiguration.Node(id,"127.0.0.1",tcp,udp,role,entry.getValue()));
                var config=new NodeConfiguration(dir.resolve(id+".properties"),id,role,"127.0.0.1","127.0.0.1",tcp,udp,
                    Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1",ports.getFirst())),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),List.of());
                configs.put(id,config);
                Files.writeString(config.sourceFile(),"# Public API graph; SERVER stream catalog not exercised here\nnode.id="+id+"\nnode.role="+role+"\nbind.host=127.0.0.1\nadvertised.host=127.0.0.1\ncontrol.port="+tcp+"\ndata.port="+udp+"\nbootstrap.host=127.0.0.1\nbootstrap.port="+ports.getFirst()+"\n");
                nodes.put(id,new OverlayNode(config,UUID.randomUUID(),event->{}));
            }
            Files.writeString(dir.resolve("topology.txt"),topology.toString());
            var config=new NodeConfiguration(dir.resolve("bootstrap.properties"),"BOOT",NodeRole.BOOTSTRAP,"127.0.0.1","127.0.0.1",ports.getFirst(),0,
                Optional.empty(),Optional.of(dir.resolve("topology.txt")),Optional.empty(),Optional.empty(),Optional.of(new TopologyConfiguration(dir.resolve("topology.txt"),topology)),List.of());
            bootstrap=new BootstrapServer(config);
        }
        void start(Set<String> ids)throws Exception {
            for(var id:ids)nodes.get(id).start();
            for(var id:ids)nodes.get(id).start().toCompletableFuture().get(10,TimeUnit.SECONDS);
            waitFor(label+" admitted sessions require discovery",15,()->ids.stream().allMatch(id->{var expected=new HashSet<>(edges.get(id));expected.retainAll(ids);return nodes.get(id).snapshot().keySet().equals(expected)&&nodes.get(id).discoveryStatus().values().stream().filter(s->s.state()==NeighborManager.CapabilityState.ACTIVE_DISCOVERY).count()==expected.size();}));
        }
        List<CandidateCache.CandidateView> candidates(String id) {
            try{return nodes.get(id).candidates().toCompletableFuture().get(2,TimeUnit.SECONDS);}catch(Exception e){throw new RuntimeException(e);}
        }
        boolean learned(String id,String target){return candidates(id).stream().anyMatch(v->v.nodeId().equals(target)&&v.state()==CandidateCache.CandidateState.AVAILABLE);}
        void stopBootstrap(){bootstrap.close();bootstrap=null;check(true,label+" bootstrap terminated after initial lookup");}
        void lose(String id){nodes.get(id).close();stopped.add(id);check(true,label+" abrupt API close/EOF "+id+" completed");}
        void dump(String name)throws Exception {var text=new StringBuilder();for(var id:nodes.keySet())if(!stopped.contains(id))text.append(id).append(" sessions=").append(nodes.get(id).snapshot()).append(" candidates=").append(candidates(id)).append('\n');Files.writeString(dir.resolve(name+".txt"),text);System.out.println("EVIDENCE "+label+"/"+name+".txt");}
        public void close() {
            RuntimeException problem=null;
            for(var id:nodes.keySet())try{nodes.get(id).close();}catch(RuntimeException e){if(problem==null)problem=e;else problem.addSuppressed(e);}
            if(bootstrap!=null)try{bootstrap.close();}catch(RuntimeException e){if(problem==null)problem=e;else problem.addSuppressed(e);}
            if(problem!=null)throw problem;
            for(int port:ports)try(var s=new ServerSocket()){s.setReuseAddress(true);s.bind(new InetSocketAddress("127.0.0.1",port));}catch(IOException e){throw new AssertionError(label+" listener still bound: "+port,e);}
            check(true,label+" shutdown deadlines completed and TCP listener ports reusable");
        }
    }
    static void offline()throws Exception {
        Path p=evidence.resolve("invalid-client.properties");Files.writeString(p,"node.id=C\nnode.role=CLIENT\nbind.host=127.0.0.1\ncontrol.port=4100\ndata.port=4200\nbootstrap.host=127.0.0.1\nbootstrap.port=4000\ncontrol.queue.capacity=1\nextra.discovery.enabled=false\n");
        try {ConfigurationLoader.load(p,NodeRole.CLIENT);throw new AssertionError("Invalid configuration accepted");}
        catch(ConfigurationException e){check(e.problems().stream().anyMatch(x->x.key().equals("control.queue.capacity"))&&e.problems().stream().anyMatch(x->x.key().equals("extra.discovery.enabled")),"offline Q1/false errors aggregated before node construction");}
        var ping=new ControlMessage(MessageType.PROBE_PING,UUID.randomUUID(),"P",UUID.randomUUID(),new ControlMessage.Probe(1));
        var bytes=MessageCodec.encode(ping);check((bytes[4]&255)==2&&MessageCodec.read(new ByteArrayInputStream(bytes)).orElseThrow().equals(ping),"single operational codec v2 round trip");
        for(int version:List.of(1,0,3,255)){var bad=bytes.clone();bad[4]=(byte)version;try{MessageCodec.read(new ByteArrayInputStream(bad));throw new AssertionError("Version accepted "+version);}catch(pt.uminho.esr.ott.protocol.ProtocolException expected){check(true,"negative version "+version+" rejected without v1 decoder");}}
        var bad=bytes.clone();bad[5]=3;try{MessageCodec.read(new ByteArrayInputStream(bad));throw new AssertionError("Retired type accepted");}catch(pt.uminho.esr.ott.protocol.ProtocolException expected){check(true,"retired type 3 rejected");}
    }
    static void incompatiblePeer()throws Exception {
        try(var g=new Graph("negative-peer",Map.of("A",Set.of("Z"),"B",Set.of("Z"),"Z",Set.of("A","B")))) {
            g.start(Set.of("A","Z"));var healthy=g.nodes.get("Z").snapshot().get("A");g.stopBootstrap();var b=g.configs.get("B");var z=g.configs.get("Z");var instance=UUID.randomUUID();
            try(var socket=new Socket("127.0.0.1",z.controlPort())) {
                socket.setSoTimeout(5000);
                var hello=new ControlMessage(MessageType.HELLO,UUID.randomUUID(),"B",instance,new ControlMessage.Hello(new ControlMessage.Endpoint("B",instance,b.advertisedHost(),b.controlPort(),b.dataPort(),b.role()),1,0));
                var valid=new ControlMessage(MessageType.HELLO,hello.requestId(),"B",instance,new ControlMessage.Hello(new ControlMessage.Endpoint("B",instance,b.advertisedHost(),b.controlPort(),b.dataPort(),b.role()),1,1));
                var negative=MessageCodec.encode(valid);negative[negative.length-1]=0;
                socket.getOutputStream().write(negative);socket.getOutputStream().flush();var reply=MessageCodec.read(socket.getInputStream());
                check(reply.isEmpty()||reply.get().type()==MessageType.ERROR&&reply.get().requestId().equals(hello.requestId()),"incompatible capability rejected locally without HELLO_OK");
                check(reply.isEmpty()||socket.getInputStream().read()==-1,"invalid attempt terminates with EOF");
            }
            waitFor("explicit per-peer incompatible state",5,()->g.nodes.get("Z").discoveryStatus().get("B").lastConfirmedRejection().isPresent());
            check(healthy.equals(g.nodes.get("Z").snapshot().get("A"))&&g.nodes.get("Z").failure().isEmpty(),"healthy session connectionId and node survive rejected peer");
            check(g.candidates("Z")!=null,"candidate API still usable after rejection");g.dump("rejection");
        }
    }
    static void cli()throws Exception {
        try(var g=new Graph("cli",Map.of("C",Set.of("R"),"R",Set.of("C")))) {
            g.start(Set.of("R"));
            String javaBin=Path.of(System.getProperty("java.home"),"bin","java").toString();String main="pt.uminho.esr.ott.app.OttApplication";
            for(String mode:List.of("quit","eof","help")) {
                Path log=g.dir.resolve(mode+".log");var command=new ArrayList<>(List.of(javaBin,"-cp",jar.toString(),main));
                if(mode.equals("help"))command.add("--help");else command.addAll(List.of("--role","CLIENT","--config",g.configs.get("C").sourceFile().toString()));
                Process p=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
                try {if(!mode.equals("help"))waitFor("CLI "+mode+" READY before input",8,()->{try{return Files.readString(log).contains("READY");}catch(IOException e){throw new UncheckedIOException(e);}});if(!mode.equals("help"))waitFor("CLI "+mode+" persistent neighbor admitted",8,()->g.nodes.get("R").snapshot().containsKey("C"));if(mode.equals("quit")){p.getOutputStream().write("neighbors\nquit\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));p.getOutputStream().flush();}p.getOutputStream().close();if(!p.waitFor(12,TimeUnit.SECONDS))throw new AssertionError("CLI timeout "+mode);check(p.exitValue()==0,"CLI "+mode+" child JVM exit 0");String text=Files.readString(log);check(mode.equals("help")?text.contains("Usage:"):text.contains("READY"),"CLI "+mode+" expected output");if(mode.equals("quit"))check(text.contains("NEIGHBORS"),"CLI neighbors responded before quit");if(!mode.equals("help"))waitFor("CLI "+mode+" session removed after shutdown",5,()->g.nodes.get("R").snapshot().isEmpty());}
                finally {if(p.isAlive()){p.destroyForcibly();p.waitFor(5,TimeUnit.SECONDS);}}
            }
        }
    }
    public static void main(String[] args)throws Exception {
        if(args.length!=4)throw new IllegalArgumentException("operator expectedJarSha jar evidenceDirectory");
        jar=Path.of(args[2]).toAbsolutePath();evidence=Path.of(args[3]).toAbsolutePath();
        System.out.println("OPERATOR "+args[0]+" AT "+Instant.now()+" JAR "+jar+" SHA256 "+sha(jar));
        check(Runtime.version().feature()==21,"JDK 21");check(sha(jar).equals(args[1]),"verified JAR hash matches before execution");
        offline();incompatiblePeer();cli();
        var shallow=Map.of("S",Set.of("R1","R2"),"R1",Set.of("S","C"),"R2",Set.of("S","C"),"C",Set.of("R1","R2"));
        var deeper=Map.of("S",Set.of("R1"),"R1",Set.of("S","R2","R3"),"R2",Set.of("R1","C"),"R3",Set.of("R1","C"),"C",Set.of("R2","R3"));
        try(var a=new Graph("S-R1R2-C",shallow);var b=new Graph("S-R1-R2R3-C",deeper)) {
            a.start(a.nodes.keySet());b.start(b.nodes.keySet());a.stopBootstrap();b.stopBootstrap();
            waitFor("S/C learned via direct relays with bootstrap stopped",15,()->a.learned("C","S")&&a.learned("S","C"));
            waitFor("R1/C learned via R2/R3 with bootstrap stopped",15,()->b.learned("C","R1")&&b.learned("R1","C"));
            for(var g:List.of(a,b)){g.dump("learned");check(g.candidates("C").stream().flatMap(v->v.sources().stream()).allMatch(p->g.edges.get("C").contains(p.source().nodeId())&&p.generation()>0&&p.ttlNanos()==CandidateCache.TTL_NANOS),g.label+" direct provenance, positive generation and 60s local TTL");}
            var sr=b.nodes.get("S").snapshot().get("R1");a.lose("R1");a.lose("R2");b.lose("R2");b.lose("R3");
            waitFor("lost relay sessions removed in both graphs",5,()->a.nodes.get("C").snapshot().isEmpty()&&a.nodes.get("S").snapshot().isEmpty()&&b.nodes.get("C").snapshot().isEmpty()&&b.nodes.get("R1").snapshot().keySet().equals(Set.of("S")));
            check(a.learned("C","S")&&b.learned("C","R1"),"claims retained after both announcers terminate");check(sr.equals(b.nodes.get("S").snapshot().get("R1")),"healthy S-R1 connectionId preserved");
            a.dump("retained");b.dump("retained");
            check(!a.nodes.get("C").snapshot().containsKey("S")&&!b.nodes.get("C").snapshot().containsKey("R1"),"learning does not implement IMP-035 repair");
            waitFor("real 60-second TTL expires retained C claims in both graphs",65,()->a.candidates("C").isEmpty()&&b.candidates("C").isEmpty());
            a.dump("expired");b.dump("expired");check(sr.equals(b.nodes.get("S").snapshot().get("R1")),"healthy session and discovery remain live through expiry");
        }
        check(sha(jar).equals(args[1]),"JAR hash unchanged after execution");
        System.out.println("MANUAL_PASS operator="+args[0]+" checks="+checks+" at="+Instant.now());
    }
}
