import pt.uminho.esr.ott.bootstrap.BootstrapServer;
import pt.uminho.esr.ott.config.*;
import pt.uminho.esr.ott.overlay.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Observation-only manual helper. Real public JAR APIs, default clocks/transports and one JVM per node. */
public final class ManualV034Node {
    static final ArrayBlockingQueue<String> output=new ArrayBlockingQueue<>(4096);
    static final AtomicBoolean logging=new AtomicBoolean(true);
    static final AtomicInteger dropped=new AtomicInteger();
    static String node,token,jarHash;
    static UUID instance;
    static String json(Object value) {
        if(value==null)return "null";
        if(value instanceof Optional<?> optional)return json(optional.orElse(null));
        if(value instanceof Number||value instanceof Boolean)return value.toString();
        if(value instanceof Map<?,?> map) {
            var pairs=new ArrayList<String>();map.forEach((k,v)->pairs.add(json(k.toString())+":"+json(v)));return "{"+String.join(",",pairs)+"}";
        }
        if(value instanceof Collection<?> items)return "["+String.join(",",items.stream().map(ManualV034Node::json).toList())+"]";
        if(value.getClass().isRecord()) {
            var fields=new LinkedHashMap<String,Object>();
            try{for(var field:value.getClass().getRecordComponents())fields.put(field.getName(),field.getAccessor().invoke(value));}
            catch(ReflectiveOperationException error){throw new IllegalStateException(error);}
            return json(fields);
        }
        var s=value.toString();var escaped=new StringBuilder("\"");
        for(char c:s.toCharArray())switch(c){case '"'->escaped.append("\\\"");case '\\'->escaped.append("\\\\");case '\n'->escaped.append("\\n");case '\r'->escaped.append("\\r");case '\t'->escaped.append("\\t");default->{if(c<32)escaped.append(String.format("\\u%04x",(int)c));else escaped.append(c);}}
        return escaped.append('"').toString();
    }
    static void emit(String kind,Object payload) {
        var line=new LinkedHashMap<String,Object>();line.put("kind",kind);line.put("node",node);line.put("pid",ProcessHandle.current().pid());line.put("instance",instance);line.put("token",token);line.put("nano",System.nanoTime());line.put("payload",payload);
        if(!output.offer(json(line)))dropped.incrementAndGet();
    }
    static String hash(Path path)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));}
    public static void main(String[] args)throws Exception {
        if(args.length>0&&args[0].equals("validate")) {
            var configurations=ConfigurationLoader.loadDeployment(Arrays.stream(args).skip(1).map(Path::of).toList());
            System.out.println(json(Map.of("kind","offline-valid","nodes",configurations.stream().map(NodeConfiguration::nodeId).toList())));return;
        }
        if(args.length!=4)throw new IllegalArgumentException("ROLE CONFIG RUN_TOKEN JAR_SHA256");
        var role=NodeRole.valueOf(args[0]);var config=ConfigurationLoader.load(Path.of(args[1]),role);
        node=config.nodeId();token=args[2];jarHash=args[3];instance=UUID.randomUUID();
        Path actualJar=Path.of(OverlayNode.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if(!hash(actualJar).equals(jarHash))throw new IllegalStateException("JAR hash mismatch before sockets");
        Thread writer=Thread.ofPlatform().name("manual-v034-output").start(()->{
            while(logging.get()||!output.isEmpty())try{String line=output.poll(100,TimeUnit.MILLISECONDS);if(line!=null){System.out.println(line);System.out.flush();}}
            catch(InterruptedException error){Thread.currentThread().interrupt();return;}
        });
        int exit=0;OverlayNode overlay=null;BootstrapServer bootstrap=null;
        try {
            emit("started",Map.of("jar",actualJar.toString(),"jarSha256",jarHash,"role",role,"config",config.sourceFile()));
            if(role==NodeRole.BOOTSTRAP)bootstrap=new BootstrapServer(config);
            else {
                overlay=new OverlayNode(config,instance,event->emit("event",Map.of("type",event.getClass().getSimpleName(),"data",event)));
                overlay.start().toCompletableFuture().get(12,TimeUnit.SECONDS);
            }
            emit("ready",Map.of("jarSha256",jarHash));
            var input=new java.io.BufferedReader(new java.io.InputStreamReader(System.in,StandardCharsets.UTF_8));
            for(String line;(line=input.readLine())!=null;) {
                if(line.equals("quit"))break;
                if(!line.startsWith("state "))throw new IllegalArgumentException("manual helper accepts state TOKEN or quit");
                var state=new LinkedHashMap<String,Object>();state.put("request",line.substring(6));state.put("dropped",dropped.get());
                if(overlay!=null) {
                    state.put("sessions",new TreeMap<>(overlay.snapshot()));state.put("candidates",overlay.candidates().toCompletableFuture().get(2,TimeUnit.SECONDS));
                    state.put("recovery",overlay.recoveryStatus());state.put("failure",overlay.failure().map(Object::toString));
                }
                emit("state",state);
            }
            if(overlay!=null) {
                var report=overlay.requestShutdown(0,Duration.ofSeconds(3)).toCompletableFuture().get(3,TimeUnit.SECONDS);
                if(!overlay.awaitTermination(Duration.ofSeconds(3)))throw new IllegalStateException("Overlay shutdown timeout");
                emit("shutdown",report);overlay=null;
            }
            if(bootstrap!=null){bootstrap.close();bootstrap=null;}
            emit("ended",Map.of("dropped",dropped.get()));
            if(dropped.get()!=0)exit=2;
        }catch(Throwable error){exit=1;emit("failure",Map.of("error",error.toString()));error.printStackTrace(System.err);}
        finally {
            if(overlay!=null)try{overlay.close();}catch(Throwable error){exit=1;error.printStackTrace(System.err);}
            if(bootstrap!=null)try{bootstrap.close();}catch(Throwable error){exit=1;error.printStackTrace(System.err);}
            logging.set(false);writer.join(2000);if(writer.isAlive())exit=3;
        }
        System.exit(exit);
    }
}
