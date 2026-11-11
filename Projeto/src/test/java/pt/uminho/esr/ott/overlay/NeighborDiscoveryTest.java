package pt.uminho.esr.ott.overlay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pt.uminho.esr.ott.config.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.Connection;
import static org.junit.jupiter.api.Assertions.*;

class NeighborDiscoveryTest {
    @TempDir Path temporary;
    private Path profile(String extra) throws Exception {
        var path=temporary.resolve("client.properties");
        Files.writeString(path,"node.id=C\nnode.role=CLIENT\nbind.host=127.0.0.1\nadvertised.host=127.0.0.1\ncontrol.port=4100\ndata.port=4200\nbootstrap.host=127.0.0.1\nbootstrap.port=4000\n"+extra); return path;
    }
    @Test void D01_redundantTrueAndAbsentAreValidOffline() throws Exception {
        assertDoesNotThrow(()->ConfigurationLoader.load(profile("extra.discovery.enabled=true\n"),NodeRole.CLIENT));
        assertDoesNotThrow(()->ConfigurationLoader.load(profile(""),NodeRole.CLIENT));
    }
    @Test void D01_falseIsRejectedInsteadOfDisablingDiscovery() throws Exception {
        var error=assertThrows(ConfigurationException.class,()->ConfigurationLoader.load(profile("extra.discovery.enabled=false\n"),NodeRole.CLIENT));
        assertTrue(error.problems().stream().anyMatch(p->p.key().equals("extra.discovery.enabled")));
    }
    @Test void D01_Q1AndFalseAreAggregatedBeforeResources() throws Exception {
        var error=assertThrows(ConfigurationException.class,()->ConfigurationLoader.load(profile("extra.discovery.enabled=false\ncontrol.queue.capacity=1\n"),NodeRole.CLIENT));
        assertTrue(error.problems().stream().anyMatch(p->p.key().equals("extra.discovery.enabled")));
        assertTrue(error.problems().stream().anyMatch(p->p.key().equals("control.queue.capacity")));
    }
    @Test void D01_overlayQ2NeedsNoSeparatePortOrEightConnections() throws Exception {
        var path=profile("control.queue.capacity=2\ncontrol.max.connections=1\nextra.discovery.enabled=true\n");
        Files.writeString(path,Files.readString(path).replace("data.port=4200","data.port=4100"));
        assertDoesNotThrow(()->ConfigurationLoader.load(path,NodeRole.CLIENT));
    }

    record Sent(Neighbor.Session peer,ControlMessage message,CompletableFuture<Void> physical) { }
    static final class Learning implements NeighborDiscovery.Host,AutoCloseable {
        final AtomicLong clock=new AtomicLong(1000);
        final NeighborManagerTest.FakeTimers timers=new NeighborManagerTest.FakeTimers(clock);
        final Map<String,ControlMessage.Endpoint> configured=new LinkedHashMap<>();
        final Map<String,Neighbor.Session> active=new LinkedHashMap<>();
        final Map<String,UUID> hello=new HashMap<>();
        final ArrayDeque<Runnable> owner=new ArrayDeque<>();
        final List<Sent> writes=new ArrayList<>();final List<NeighborDiscovery.Issue> issues=new ArrayList<>();
        final UUID instance=UUID.randomUUID();final NeighborDiscovery service;
        boolean hold,backpressure,closed;int physical,peak;
        Learning(String... ids) {
            int port=5000;
            for(String id:ids){configured.put(id,new ControlMessage.Endpoint(id,new UUID(0,0),"127.0.0.1",port,port+1,NodeRole.RELAY));port+=10;}
            timers.limit(128);service=new NeighborDiscovery(CandidateCacheTest.config(),instance,configured,clock::get,this);service.start(timers);
            for(String id:ids)up(id);
        }
        Neighbor.Session up(String id) {
            var ep=configured.get(id);var remote=UUID.randomUUID();
            var session=new Neighbor.Session(id,remote,UUID.randomUUID(),new ControlMessage.Endpoint(id,remote,ep.host(),ep.controlPort(),ep.dataPort(),ep.role()));
            active.put(id,session);hello.put(id,UUID.randomUUID());service.up(session,hello.get(id));return session;
        }
        void down(String id){service.down(active.remove(id));}
        public boolean post(Runnable task){if(closed)return false;owner.add(task);return true;}
        public Map<String,Neighbor.Session> active(){return Map.copyOf(active);}
        public Connection.ExtensionEnqueue enqueue(Neighbor.Session session,ControlMessage message) {
            if(backpressure||physical>=4)return Connection.refused(Connection.ExtensionStatus.BACKPRESSURED);
            try{MessageCodec.encode(message);}catch(Exception error){throw new AssertionError(error);}
            physical++;peak=Math.max(peak,physical);var written=new CompletableFuture<Void>();
            written.whenComplete((v,e)->physical--);writes.add(new Sent(session,message,written));
            if(!hold)written.complete(null);
            return new Connection.ExtensionEnqueue(Connection.ExtensionStatus.ACCEPTED,written.minimalCompletionStage());
        }
        public void diagnostic(NeighborDiscovery.Issue issue){issues.add(issue);}
        void pump(){int events=0;while(!owner.isEmpty()){assertTrue(++events<1000);owner.remove().run();}}
        void advance(long nanos){timers.advance(nanos);pump();}
        Sent query(){advance(NeighborDiscovery.TICK);return writes.stream().filter(w->w.message.type()==MessageType.DISCOVERY_QUERY).reduce((a,b)->b).orElseThrow();}
        ControlMessage reply(Sent query,long generation,List<ControlMessage.Endpoint> entries){return new ControlMessage(MessageType.DISCOVERY_SNAPSHOT,query.message.requestId(),query.peer.nodeId(),query.peer.instanceId(),new DiscoveryMessage.Snapshot(hello.get(query.peer.nodeId()),"C",instance,generation,entries));}
        void learn(Sent query,long generation,List<ControlMessage.Endpoint> entries){assertTrue(service.receive(query.peer,reply(query,generation,entries),clock.get()));pump();}
        ControlMessage request(String id){var peer=active.get(id);return new ControlMessage(MessageType.DISCOVERY_QUERY,UUID.randomUUID(),id,peer.instanceId(),new DiscoveryMessage.Query(hello.get(id),"C",instance));}
        public void close(){service.close();closed=true;for(var w:writes)w.physical.completeExceptionally(new IllegalStateException("test writer stopped"));owner.clear();timers.requestClose();assertEquals(0,physical);}
    }
    static ControlMessage.Endpoint candidate(String id,int port){return new ControlMessage.Endpoint(id,UUID.randomUUID(),"127.0.0.1",port,port+1,id.equals("S")?NodeRole.SERVER:NodeRole.RELAY);}
    @Test void D03D05_learnSourceViaDirectRelayRetainAfterDeathAndExpireFromQueryStart() {
        try(var h=new Learning("R1","R2")) {
            var query=h.query();var source=candidate("S",6000);h.advance(500_000_000L);h.learn(query,1,List.of(source));
            var view=h.service.snapshot().getFirst();assertEquals("S",view.nodeId());assertEquals(query.peer.connectionId(),view.sources().getFirst().source().connectionId());
            assertEquals(query.peer.nodeId(),view.sources().getFirst().source().nodeId());
            long began=view.sources().getFirst().queryStartedNanos();h.down("R1");h.down("R2");
            assertEquals(1,h.service.snapshot().size());h.clock.set(began+CandidateCache.TTL_NANOS-1);assertEquals(1,h.service.snapshot().size());
            h.clock.incrementAndGet();assertTrue(h.service.snapshot().isEmpty());assertTrue(h.active.isEmpty());
        }
    }
    @Test void D04_learnR1FromR2OrR3WithoutCreatingAnEdge() {
        try(var h=new Learning("R2","R3")) {
            h.learn(h.query(),1,List.of(candidate("R1",6100)));
            h.down("R2");h.down("R3");assertEquals("R1",h.service.snapshot().getFirst().nodeId());
            int writes=h.writes.size();h.advance(NeighborDiscovery.REFRESH);assertEquals(writes,h.writes.size());assertTrue(h.active.isEmpty());
        }
    }
    @Test void D06_newSessionStartsNewGenerationAndOldCallbacksCannotRenew() {
        try(var h=new Learning("R2")) {
            var old=h.query();var entry=candidate("S",6000);h.learn(old,9,List.of(entry));
            h.down("R2");var current=h.up("R2");h.advance(NeighborDiscovery.GLOBAL_INTERVAL);
            var query=h.writes.getLast();assertEquals(current,query.peer);h.learn(query,1,List.of(entry));
            var before=h.service.snapshot();assertFalse(h.service.receive(old.peer,h.reply(old,10,List.of(entry)),h.clock.get()));
            assertEquals(before,h.service.snapshot());assertEquals(2,before.getFirst().sources().size());
        }
    }
    @Test void D06_staleGenerationAndWrongQueryNeverRenewClaims() {
        try(var h=new Learning("R2")) {
            var first=h.query();var entry=candidate("S",6000);h.learn(first,2,List.of(entry));var before=h.service.snapshot();
            h.advance(NeighborDiscovery.REFRESH);var second=h.writes.getLast();h.learn(second,1,List.of(entry));
            assertEquals(before,h.service.snapshot());assertTrue(h.issues.contains(NeighborDiscovery.Issue.STALE));
            assertTrue(h.service.receive(first.peer,h.reply(first,3,List.of(entry)),h.clock.get()));assertEquals(before,h.service.snapshot());
        }
    }
    @Test void D07_rejectionIsAtomicAndBadRecipientIsTerminalToTheOwner() {
        try(var h=new Learning("R2")) {
            var query=h.query();var before=h.service.snapshot();
            var e=new MessageCodec.ExtraEnvelope(2,MessageType.DISCOVERY_SNAPSHOT,query.message.requestId(),query.peer.nodeId(),query.peer.instanceId(),h.hello.get(query.peer.nodeId()),"C",h.instance);
            assertTrue(h.service.rejected(query.peer,new MessageCodec.RejectedExtraContent(e,MessageCodec.ContentRejection.INVALID_MESSAGE)));
            assertEquals(before,h.service.snapshot());
            var bad=new MessageCodec.ExtraEnvelope(2,e.type(),e.queryId(),e.senderId(),e.senderInstance(),e.helloRequestId(),"OTHER",h.instance);
            assertFalse(h.service.rejected(query.peer,new MessageCodec.RejectedExtraContent(bad,MessageCodec.ContentRejection.INVALID_MESSAGE)));
            h.advance(NeighborDiscovery.COOLDOWN);h.learn(h.writes.getLast(),1,List.of(candidate("S",6000)));assertEquals(1,h.service.snapshot().size());
        }
    }
    @Test void D08_queriesHaveGlobalRateAndAtMostTwoLogicalOperations() {
        try(var h=new Learning("A","B","D","E")) {
            h.query();assertEquals(1,h.writes.size());h.advance(100_000_000L);assertEquals(1,h.writes.size());
            h.advance(200_000_000L);assertEquals(2,h.writes.size());
            h.advance(500_000_000L);assertEquals(2,h.writes.size());
            h.advance(1_300_000_000L);assertEquals(3,h.writes.size());assertNotEquals(h.writes.getFirst().peer,h.writes.getLast().peer);
        }
    }
    @Test void D08_timeoutDoesNotReleasePhysicalWritesAndNoRetryQueueIsRetained() {
        try(var h=new Learning("A","B","D","E","F")) {
            h.hold=true;h.query();h.advance(300_000_000L);assertEquals(2,h.physical);
            h.advance(NeighborDiscovery.QUERY_BUDGET);assertTrue(h.issues.contains(NeighborDiscovery.Issue.TIMEOUT));
            h.advance(300_000_000L);assertEquals(4,h.physical);
            for(int n=0;n<10;n++)h.advance(NeighborDiscovery.COOLDOWN);
            assertEquals(4,h.physical);assertEquals(4,h.writes.size());assertEquals(4,h.peak);
            h.writes.getFirst().physical.complete(null);h.pump();assertEquals(3,h.physical);
        }
    }
    @Test void D08_responsesAreRateLimitedFullDirectSnapshotsWithoutHearsay() {
        try(var h=new Learning("A","B")) {
            h.hold=true;var query=h.request("A");assertTrue(h.service.receive(h.active.get("A"),query,h.clock.get()));
            var response=h.writes.getFirst().message;var payload=(DiscoveryMessage.Snapshot)response.payload();
            assertEquals(Set.of("A","B"),payload.directNeighbors().stream().map(ControlMessage.Endpoint::nodeId).collect(java.util.stream.Collectors.toSet()));
            assertEquals(query.requestId(),response.requestId());assertEquals(1,payload.generation());
            assertTrue(h.service.receive(h.active.get("A"),h.request("A"),h.clock.get()));assertEquals(1,h.writes.size());
            h.clock.addAndGet(NeighborDiscovery.INBOUND_INTERVAL);assertTrue(h.service.receive(h.active.get("A"),h.request("A"),h.clock.get()));assertEquals(2,h.writes.size());
            assertTrue(h.service.receive(h.active.get("B"),h.request("B"),h.clock.get()));assertEquals(2,h.writes.size());
            assertTrue(h.issues.contains(NeighborDiscovery.Issue.BACKPRESSURE));
        }
    }
    @Test void D10_coalescesTickToOnePhysicalTimerAndOneOwnerEvent() {
        try(var h=new Learning("A")) {
            for(int n=0;n<20;n++)h.timers.advance(NeighborDiscovery.TICK);
            assertEquals(1,h.owner.size());assertEquals(1,h.timers.periodic.size());assertEquals(1,h.timers.peak);
            h.service.close();h.pump();assertTrue(h.writes.isEmpty());assertTrue(h.timers.periodic.isEmpty());
        }
    }
    @Test void D10_lateWriteFailureCannotTouchReplacementSession() {
        try(var h=new Learning("A")) {
            h.hold=true;var old=h.query();h.down("A");h.up("A");h.advance(NeighborDiscovery.GLOBAL_INTERVAL);
            var fresh=h.writes.getLast();old.physical.completeExceptionally(new java.io.IOException("old writer"));h.pump();
            h.learn(fresh,1,List.of(candidate("S",6000)));assertEquals(1,h.service.snapshot().size());
        }
    }
    @Test void D09_emptyExpiredOrUnlearnedCacheNeverCreatesConnections() {
        try(var h=new Learning("A")){assertTrue(h.service.snapshot().isEmpty());h.down("A");h.advance(100_000_000_000L);assertTrue(h.writes.isEmpty());assertTrue(h.service.snapshot().isEmpty());}
    }
    @Test void D08_learnedCandidatesAreNeverRepeatedAsDirectNeighbors() {
        try(var h=new Learning("A","B")) {
            h.learn(h.query(),1,List.of(candidate("S",6000)));assertEquals("S",h.service.snapshot().getFirst().nodeId());
            assertTrue(h.service.receive(h.active.get("A"),h.request("A"),h.clock.get()));
            var snapshot=(DiscoveryMessage.Snapshot)h.writes.getLast().message.payload();
            assertEquals(Set.of("A","B"),snapshot.directNeighbors().stream().map(ControlMessage.Endpoint::nodeId).collect(java.util.stream.Collectors.toSet()));
        }
    }
    @Test void D06_receiverGenerationAtLongMaxCannotWrapOrRenewOnReplay() {
        try(var h=new Learning("A")) {
            var entry=candidate("S",6000);h.learn(h.query(),Long.MAX_VALUE,List.of(entry));var before=h.service.snapshot();
            h.advance(NeighborDiscovery.REFRESH);h.learn(h.writes.getLast(),1,List.of(entry));assertEquals(before,h.service.snapshot());
        }
    }
    static final class Graph implements AutoCloseable {
        final Map<String,NeighborManager> nodes=new LinkedHashMap<>();
        final Map<String,NodeConfiguration> configs=new LinkedHashMap<>();
        final pt.uminho.esr.ott.bootstrap.BootstrapServer bootstrap;
        final Map<String,Set<String>> edges;
        Graph(boolean deeper)throws Exception {
            edges=deeper?Map.of("S",Set.of("R1"),"R1",Set.of("S","R2","R3"),"R2",Set.of("R1","C"),"R3",Set.of("R1","C"),"C",Set.of("R2","R3"))
                :Map.of("S",Set.of("R1","R2"),"R1",Set.of("S","C"),"R2",Set.of("S","C"),"C",Set.of("R1","R2"));
            var reserved=new ArrayList<java.net.ServerSocket>();var ports=new ArrayList<Integer>();
            try{for(int n=0;n<1+2*edges.size();n++){var socket=new java.net.ServerSocket(0,1,java.net.InetAddress.getLoopbackAddress());reserved.add(socket);ports.add(socket.getLocalPort());}}
            finally{for(var socket:reserved)socket.close();}
            var topology=new LinkedHashMap<String,TopologyConfiguration.Node>();int index=0;
            for(var edge:edges.entrySet()) {
                String id=edge.getKey();int control=ports.get(1+2*index),data=ports.get(2+2*index);index++;
                NodeRole role=id.equals("S")?NodeRole.SERVER:id.equals("C")?NodeRole.CLIENT:NodeRole.RELAY;
                topology.put(id,new TopologyConfiguration.Node(id,"127.0.0.1",control,data,role,edge.getValue()));
                var config=new NodeConfiguration(Path.of("graph-"+id),id,role,"127.0.0.1","127.0.0.1",control,data,
                    Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1",ports.getFirst())),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),List.of(),NeighborManagerTest.fastRuntime());
                configs.put(id,config);nodes.put(id,new NeighborManager(config,UUID.randomUUID(),event->{}));
            }
            var bootConfig=new NodeConfiguration(Path.of("graph-bootstrap"),"BOOT",NodeRole.BOOTSTRAP,"127.0.0.1","127.0.0.1",ports.getFirst(),0,
                Optional.empty(),Optional.of(Path.of("graph-topology")),Optional.empty(),Optional.empty(),Optional.of(new TopologyConfiguration(Path.of("graph-topology"),topology)),List.of(),NeighborManagerTest.fastRuntime());
            bootstrap=new pt.uminho.esr.ott.bootstrap.BootstrapServer(bootConfig);
        }
        void start()throws Exception {
            for(var node:nodes.values())node.start();
            for(var node:nodes.values())node.start().toCompletableFuture().get(3,TimeUnit.SECONDS);
            NeighborManagerTest.until(()->nodes.entrySet().stream().allMatch(e->e.getValue().snapshot().size()==edges.get(e.getKey()).size()));
        }
        List<CandidateCache.CandidateView> candidates(String id) {
            try{return nodes.get(id).candidates().toCompletableFuture().get(1,TimeUnit.SECONDS);}catch(Exception error){throw new AssertionError(error);}
        }
        boolean learned(String node,String target){return candidates(node).stream().anyMatch(v->v.nodeId().equals(target)&&v.state()==CandidateCache.CandidateState.AVAILABLE);}
        void lose(String id)throws Exception{nodes.get(id).requestClose();assertTrue(nodes.get(id).awaitTermination(java.time.Duration.ofSeconds(3)));}
        public void close(){for(var n:nodes.values())n.requestClose();try{for(var n:nodes.values())assertTrue(n.awaitTermination(java.time.Duration.ofSeconds(3)));}finally{bootstrap.close();}}
    }
    @Test @org.junit.jupiter.api.Timeout(20) void D03D05_realTcpLearnsSAndCWithoutBootstrapAndRetainsAfterRelayLoss()throws Exception {
        try(var g=new Graph(false)) {
            g.start();g.bootstrap.close();NeighborManagerTest.until(()->g.learned("C","S")&&g.learned("S","C"));
            var before=g.candidates("C");assertTrue(before.stream().flatMap(v->v.sources().stream()).allMatch(p->Set.of("R1","R2").contains(p.source().nodeId())));
            g.lose("R1");g.lose("R2");NeighborManagerTest.until(()->g.nodes.get("C").snapshot().isEmpty()&&g.nodes.get("S").snapshot().isEmpty());
            assertTrue(g.learned("C","S"));assertTrue(g.learned("S","C"));assertFalse(g.nodes.get("C").start().toCompletableFuture().isCompletedExceptionally());
            assertFalse(g.nodes.get("C").snapshot().containsKey("S"),"Learning must not perform IMP-035 repair");
        }
    }
    @Test @org.junit.jupiter.api.Timeout(20) void D04_realTcpLearnsR1AndRetainsHealthySR1SessionAfterR2R3Loss()throws Exception {
        try(var g=new Graph(true)) {
            g.start();var surviving=g.nodes.get("S").snapshot().get("R1");g.bootstrap.close();
            NeighborManagerTest.until(()->g.learned("C","R1")&&g.learned("R1","C"));g.lose("R2");g.lose("R3");
            NeighborManagerTest.until(()->g.nodes.get("C").snapshot().isEmpty()&&g.nodes.get("R1").snapshot().size()==1);
            assertEquals(surviving,g.nodes.get("S").snapshot().get("R1"));assertTrue(g.learned("C","R1"));assertTrue(g.learned("R1","C"));
            assertFalse(g.nodes.get("C").snapshot().containsKey("R1"),"No dynamic edge before IMP-035");
        }
    }

    @Test void D06_emitterAtMaximumGenerationDoesNotWrapOrCloseSession()throws Exception {
        try(var h=new Learning("A")) {
            // Controlled boundary fixture; no runtime configuration/property bypass.
            var peers=NeighborDiscovery.class.getDeclaredField("peers");peers.setAccessible(true);
            var state=((Map<?,?>)peers.get(h.service)).values().iterator().next();var generation=state.getClass().getDeclaredField("generation");generation.setAccessible(true);generation.setLong(state,Long.MAX_VALUE);
            assertTrue(h.service.receive(h.active.get("A"),h.request("A"),h.clock.get()));
            assertTrue(h.writes.isEmpty());assertTrue(h.issues.contains(NeighborDiscovery.Issue.GENERATION_EXHAUSTED));assertEquals(1,h.active.size());
        }
    }

}
