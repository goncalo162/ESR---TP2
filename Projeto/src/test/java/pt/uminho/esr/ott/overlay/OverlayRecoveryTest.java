package pt.uminho.esr.ott.overlay;

import org.junit.jupiter.api.Test;
import pt.uminho.esr.ott.protocol.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static pt.uminho.esr.ott.overlay.NeighborManagerTest.*;

class OverlayRecoveryTest {
    static FakeChannel repair(Fixture f,String id,UUID instance,int port,boolean flush)throws Exception {
        var c=new FakeChannel();c.flush=flush;f.io.sink.opened(c,f.clock.get());f.pump();
        f.message(c,new ControlMessage(MessageType.fromCode(20),UUID.randomUUID(),id,instance,
            new ControlMessage.Hello(endpoint(id,instance,port),3,3)));return c;
    }
    @Test void D05_greaterIsolatedPeerCompletesOnSameConnectionWithoutReturnDial()throws Exception {
        try(var f=new Fixture("A")) {
            f.ready("B");f.io.dials.getLast().future().completeExceptionally(new java.io.IOException("configured peer offline"));f.pump();var c=repair(f,"Z",UUID.randomUUID(),18000,true);
            assertTrue(c.open);assertEquals(1,f.ups());assertEquals(0,f.downs());
            assertEquals(c.id(),f.manager.snapshot().get("Z").connectionId());
            assertEquals(MessageType.HELLO_OK,c.last().type());assertEquals(2,f.io.dials.size());
        }
    }
    @Test void D05_healthyDestinationPreservesExistingSessionAndDoesNotRequireReciprocalCache()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var healthy=f.inbound("R",UUID.randomUUID(),true);var before=f.manager.snapshot().get("R");
            var c=repair(f,"ZZ",UUID.randomUUID(),18000,true);
            assertTrue(c.open);assertEquals(2,f.ups());assertEquals(0,f.downs());
            assertEquals(before,f.manager.snapshot().get("R"));assertTrue(healthy.open);
            assertEquals(c.id(),f.manager.snapshot().get("ZZ").connectionId());assertEquals(1,f.io.dials.size());
        }
    }

    static void learn(Fixture f,FakeChannel relay,UUID remote,String target)throws Exception {
        f.timers.advance(NeighborDiscovery.TICK);f.pump();var q=relay.extraWrites.getLast();
        assertEquals(MessageType.DISCOVERY_QUERY,q.type());var payload=(DiscoveryMessage.Query)q.payload();
        f.message(relay,new ControlMessage(MessageType.DISCOVERY_SNAPSHOT,q.requestId(),"R",remote,
            new DiscoveryMessage.Snapshot(payload.helloRequestId(),"Z",f.instance,1,List.of(endpoint(target,UUID.randomUUID(),18000)))));
    }
    static void lose(Fixture f,FakeChannel channel) {
        channel.close();f.io.sink.closed(channel,pt.uminho.esr.ott.transport.Connection.CloseReason.PEER_EOF,f.clock.get());f.pump();
    }
    static FakeChannel outgoing(Fixture f,boolean flush) {
        var c=new FakeChannel();c.flush=flush;f.io.sink.opened(c,f.clock.get());f.io.dials.getLast().future().complete(c);f.pump();return c;
    }
    @Test void D05_isolatedGreaterSelectsLearnedCandidateAndKeepsSameTcp()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);
            learn(f,relay,remote,"A");assertEquals(1,f.io.dials.size());lose(f,relay);
            f.timers.advance(NeighborDiscovery.TICK);f.pump();assertEquals(2,f.io.dials.size());
            var c=outgoing(f,true);assertEquals(20,c.last().type().code());var id=c.last().requestId();
            f.message(c,new ControlMessage(MessageType.HELLO_OK,id,"A",UUID.randomUUID(),new ControlMessage.HelloOk(3)));
            assertEquals(c.id(),f.manager.snapshot().get("A").connectionId());assertEquals(2,f.io.dials.size());
            assertEquals(OverlayRecovery.Phase.ACTIVE,f.manager.recoveryStatus().phase());assertEquals(1,f.downs());
        }
    }
    @Test void D09_smallerInboundWinsPendingOutgoingAndLateLoserCannotRemoveWinner()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);learn(f,relay,remote,"A");lose(f,relay);
            f.timers.advance(NeighborDiscovery.TICK);f.pump();var losing=outgoing(f,false);var oldRequest=losing.last().requestId();
            var winning=repair(f,"A",UUID.randomUUID(),18000,true);var winner=f.manager.snapshot().get("A");
            assertNotNull(winner);assertEquals(winning.id(),winner.connectionId());assertFalse(losing.open);
            losing.written();f.pump();f.message(losing,new ControlMessage(MessageType.HELLO_OK,oldRequest,"A",UUID.randomUUID(),new ControlMessage.HelloOk(3)));
            f.io.sink.closed(losing,pt.uminho.esr.ott.transport.Connection.CloseReason.PEER_EOF,f.clock.get());f.pump();
            assertEquals(winner,f.manager.snapshot().get("A"));assertEquals(1,f.downs());assertEquals(2,f.ups());
        }
    }
    @Test void D09_smallerOutgoingWinsPendingGreaterInboundIncludingOpenedBeforeDialResult()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);learn(f,relay,remote,"ZZ");lose(f,relay);
            f.timers.advance(NeighborDiscovery.TICK);f.pump();
            var loser=repair(f,"ZZ",UUID.randomUUID(),18000,true);assertTrue(loser.open,"UNCLASSIFIED retains frame until dial result");
            var winner=outgoing(f,true);assertEquals(20,winner.last().type().code());assertFalse(loser.open);
            f.message(winner,new ControlMessage(MessageType.HELLO_OK,winner.last().requestId(),"ZZ",UUID.randomUUID(),new ControlMessage.HelloOk(3)));
            assertEquals(winner.id(),f.manager.snapshot().get("ZZ").connectionId());assertEquals(1,f.downs());
        }
    }
    @Test void D10_activeGreaterInitiatorIsNotReplacedBySmallerLateAttemptOrNewInstance()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var first=repair(f,"ZZ",UUID.randomUUID(),18000,true);var active=f.manager.snapshot().get("ZZ");
            var late=repair(f,"ZZ",UUID.randomUUID(),18000,true);
            assertFalse(late.open);assertTrue(first.open);assertEquals(active,f.manager.snapshot().get("ZZ"));assertEquals(1,f.ups());assertEquals(0,f.downs());
        }
    }
    @Test void D11_sameOriginDuplicatePendingDoesNotReplaceCurrentRequest()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var instance=UUID.randomUUID();var chosen=repair(f,"A",instance,18000,false);
            var duplicate=repair(f,"A",instance,18000,true);assertFalse(duplicate.open);assertEquals(0,f.ups());
            chosen.written();f.pump();assertEquals(chosen.id(),f.manager.snapshot().get("A").connectionId());assertEquals(0,f.downs());
        }
    }
    @Test void D08_badEndpointIdentityCapabilitiesAndConfiguredHelloDoNotAdmit()throws Exception {
        for(int kind=0;kind<6;kind++)try(var f=new Fixture("Z")) {
            f.ready("R");var instance=UUID.randomUUID();var c=new FakeChannel();f.io.sink.opened(c,f.clock.get());f.pump();
            var ep=endpoint(kind==0?"OTHER":"A",kind==1?UUID.randomUUID():instance,kind==2?19001:18000);
            var type=kind==5?MessageType.HELLO:MessageType.fromCode(20);
            f.message(c,new ControlMessage(type,kind==3?ZERO:UUID.randomUUID(),"A",instance,new ControlMessage.Hello(ep,kind==4?1:3,kind==4?1:3)));
            assertFalse(c.open);assertEquals(0,f.ups());assertTrue(f.manager.snapshot().isEmpty());
        }
    }
    @Test void D18_repairErrorUsesRelatedType20WithoutErrorReply()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);learn(f,relay,remote,"A");lose(f,relay);
            f.timers.advance(NeighborDiscovery.TICK);f.pump();var c=outgoing(f,true);
            var error=new ControlMessage(MessageType.ERROR,c.last().requestId(),"A",UUID.randomUUID(),new ControlMessage.ErrorPayload(ErrorCode.RESOURCE_LIMIT,20,"busy"));
            f.message(c,error);assertFalse(c.open);assertEquals(1,c.writes.size());assertTrue(f.manager.snapshot().isEmpty());
            assertEquals(OverlayRecovery.RecoveryReason.REJECTED,f.manager.recoveryStatus().reason());
        }
    }
    static final class Policy implements OverlayRecovery.RecoveryPorts {
        final java.util.concurrent.atomic.AtomicLong clock=new java.util.concurrent.atomic.AtomicLong(1);
        final Map<String,Neighbor.Session> active=new LinkedHashMap<>();final List<CandidateCache.CandidateView> candidates=new ArrayList<>();
        final List<OverlayRecovery.RepairAttempt> launched=new ArrayList<>(),cancelled=new ArrayList<>();
        final OverlayRecovery service;boolean admit=true;long serial;
        Policy(){service=new OverlayRecovery(config("C",19001),UUID.randomUUID(),clock::get,()->new UUID(1,++serial),this);}
        CandidateCache.CandidateView candidate(String id,long began) {
            var source=new CandidateCache.SourceKey("R",UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());
            var endpoint=new CandidateCache.CandidateEndpoint(id,"127.0.0.1",18000+serialAsInt(id),18100+serialAsInt(id),NodeRole.RELAY);
            return new CandidateCache.CandidateView(id,CandidateCache.CandidateState.AVAILABLE,Optional.of(endpoint),List.of(new CandidateCache.Provenance(source,1,endpoint,UUID.randomUUID(),began,began,CandidateCache.TTL_NANOS)));
        }
        static int serialAsInt(String id){return Math.floorMod(id.hashCode(),50);}
        public Map<String,Neighbor.Session> active(){return Map.copyOf(active);}
        public List<CandidateCache.CandidateView> candidates(long now){return List.copyOf(candidates);}
        public boolean beginRepair(OverlayRecovery.RepairAttempt a,ControlMessage.Endpoint ep,long remaining){assertTrue(remaining>0&&remaining<=OverlayRecovery.TOTAL_BUDGET);launched.add(a);return admit;}
        public void cancelRepair(OverlayRecovery.RepairAttempt a){cancelled.add(a);}
        public void publish(OverlayRecovery.RecoveryStatus status){}
        void tick(long elapsed){clock.addAndGet(elapsed);service.maintain(clock.get());}
    }
    @Test void D02_onlyIsolationTriggersAndOneOperationIsLogical() {
        var p=new Policy();p.candidates.add(p.candidate("A",0));var instance=UUID.randomUUID();p.active.put("R",new Neighbor.Session("R",instance,UUID.randomUUID(),endpoint("R",instance,19002)));
        p.tick(0);assertTrue(p.launched.isEmpty());p.active.clear();p.tick(0);p.tick(1);assertEquals(1,p.launched.size());
    }
    @Test void D07_emptyExpiredConflictAndSelfNeverDial() {
        var p=new Policy();p.tick(0);assertTrue(p.launched.isEmpty());
        var c=p.candidate("A",0);p.candidates.add(new CandidateCache.CandidateView("A",CandidateCache.CandidateState.CONFLICT,Optional.empty(),c.sources()));p.tick(0);assertTrue(p.launched.isEmpty());
        p.candidates.clear();p.candidates.add(p.candidate("C",0));p.tick(0);assertTrue(p.launched.isEmpty());
        p.candidates.clear();p.candidates.add(c);p.tick(CandidateCache.TTL_NANOS);assertTrue(p.launched.isEmpty());
    }
    @Test void D15_originalClaimDeadlineDoesNotExtendAfterCacheRefresh() {
        var p=new Policy();p.candidates.add(p.candidate("A",-59_000_000_000L));p.tick(0);assertEquals(1,p.launched.size());
        p.tick(500_000_000L);p.candidates.set(0,p.candidate("A",p.clock.get()));p.tick(500_000_000L);
        assertEquals(1,p.cancelled.size());assertEquals(OverlayRecovery.RecoveryReason.EXPIRED,p.service.snapshot().reason());
    }
    @Test void D15_totalBudgetAndOldCallbackCannotCancelFreshAttempt() {
        var p=new Policy();p.candidates.add(p.candidate("A",0));p.tick(0);var old=p.launched.getFirst();
        p.tick(OverlayRecovery.TOTAL_BUDGET);assertEquals(1,p.cancelled.size());assertEquals(OverlayRecovery.RecoveryReason.TIMEOUT,p.service.snapshot().reason());
        p.tick(500_000_000L);var fresh=p.launched.getLast();assertNotSame(old,fresh);p.service.onAttemptTerminal(old,OverlayRecovery.RecoveryReason.TRANSPORT);
        assertTrue(p.service.isCurrent(fresh));
    }
    @Test void D14_backoffIsBoundedAndOtherCandidateCanProgress() {
        var p=new Policy();p.admit=false;p.candidates.add(p.candidate("A",0));p.candidates.add(p.candidate("B",0));
        p.tick(0);p.tick(1);assertEquals(List.of("A","B"),p.launched.stream().map(OverlayRecovery.RepairAttempt::targetId).toList());
        p.tick(1);assertEquals(2,p.launched.size());p.tick(500_000_000L);assertEquals(3,p.launched.size());
        assertTrue(p.service.snapshot().retryDelayMillis()<=5000);
    }
    @Test void D19_shutdownCancelsLogicalAttemptAndForbidsNewAdmissions() {
        var p=new Policy();p.candidates.add(p.candidate("A",0));p.tick(0);var a=p.launched.getFirst();p.service.requestClose(1);p.tick(99_000_000_000L);
        assertEquals(List.of(a),p.cancelled);assertEquals(1,p.launched.size());assertEquals(OverlayRecovery.Phase.STOPPING,p.service.snapshot().phase());
        p.service.onAttemptTerminal(a,OverlayRecovery.RecoveryReason.TRANSPORT);assertEquals(OverlayRecovery.Phase.STOPPING,p.service.snapshot().phase());
    }

    @Test @org.junit.jupiter.api.Timeout(20) void D03_realTcpLearnsBeforeFailureAndFormsSCOnSameSurvivors()throws Exception {
        try(var g=new NeighborDiscoveryTest.Graph(false)) {
            g.start();g.bootstrap.close();until(()->g.learned("C","S")&&g.learned("S","C"));
            g.lose("R1");until(()->g.nodes.get("C").snapshot().keySet().equals(Set.of("R2"))&&g.nodes.get("S").snapshot().keySet().equals(Set.of("R2")));
            assertFalse(g.nodes.get("C").snapshot().containsKey("S"));g.lose("R2");
            until(()->g.nodes.get("C").snapshot().keySet().equals(Set.of("S"))&&g.nodes.get("S").snapshot().keySet().equals(Set.of("C")));
            assertFalse(g.nodes.get("C").start().toCompletableFuture().isCompletedExceptionally());assertFalse(g.nodes.get("S").start().toCompletableFuture().isCompletedExceptionally());
        }
    }
    @Test @org.junit.jupiter.api.Timeout(20) void D04_realTcpFormsR1CAndPreservesHealthySR1()throws Exception {
        try(var g=new NeighborDiscoveryTest.Graph(true)) {
            g.start();g.bootstrap.close();var healthy=g.nodes.get("S").snapshot().get("R1");
            until(()->g.learned("C","R1")&&g.learned("R1","C"));g.lose("R2");
            until(()->g.nodes.get("C").snapshot().keySet().equals(Set.of("R3")));assertFalse(g.nodes.get("C").snapshot().containsKey("R1"));
            g.lose("R3");until(()->g.nodes.get("C").snapshot().keySet().equals(Set.of("R1"))&&g.nodes.get("R1").snapshot().keySet().equals(Set.of("S","C")));
            assertEquals(healthy,g.nodes.get("S").snapshot().get("R1"));
        }
    }

    @Test void D13_repairDnsDeadlineUsesExistingTickNotAnAdditionalPhysicalTimer()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);learn(f,relay,remote,"A");lose(f,relay);
            f.io.holdDns=true;f.timers.limit(1);f.timers.advance(NeighborDiscovery.TICK);f.pump();
            assertFalse(f.owner.stopped);assertEquals(1,f.timers.periodic.size());assertTrue(f.timers.tasks.isEmpty());
            f.timers.advance(java.util.concurrent.TimeUnit.SECONDS.toNanos(2));f.pump();
            assertFalse(f.owner.stopped);assertEquals(OverlayRecovery.RecoveryReason.TIMEOUT,f.manager.recoveryStatus().reason());
        }
    }

    @Test void D13_atMostTwoPendingAdmissionsWithoutLosingHealthyState()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var first=repair(f,"A",UUID.randomUUID(),18000,false);var second=repair(f,"B",UUID.randomUUID(),18010,false);
            var excess=repair(f,"D",UUID.randomUUID(),18020,true);assertFalse(excess.open);assertTrue(first.open);assertTrue(second.open);
            assertEquals(0,f.ups());assertFalse(f.owner.stopped);assertEquals(3,f.manager.diagnostics().size());
            first.written();f.pump();second.written();f.pump();assertEquals(Set.of("A","B"),f.manager.snapshot().keySet());assertEquals(0,f.downs());
        }
    }
    @Test void D13_dynamicTableStopsAtSixteenAndPreservesAllActiveSessions()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var connections=new ArrayList<FakeChannel>();
            for(int i=0;i<16;i++)connections.add(repair(f,"N"+i,UUID.randomUUID(),18000+i*10,true));
            var before=f.manager.snapshot();assertEquals(16,before.size());var excess=repair(f,"N16",UUID.randomUUID(),18300,true);
            assertFalse(excess.open);assertEquals(before,f.manager.snapshot());assertTrue(connections.stream().allMatch(c->c.open));
            assertEquals(0,f.downs());assertFalse(f.owner.stopped);
        }
    }
    @Test void D15_lateNativeDialCannotWriteRepairHelloEvenWithoutTimerDispatch()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);learn(f,relay,remote,"A");lose(f,relay);
            f.timers.advance(NeighborDiscovery.TICK);f.pump();f.clock.addAndGet(java.util.concurrent.TimeUnit.SECONDS.toNanos(2));
            var late=outgoing(f,true);assertFalse(late.open);assertTrue(late.writes.isEmpty());assertTrue(f.manager.snapshot().isEmpty());
            assertEquals(OverlayRecovery.RecoveryReason.TIMEOUT,f.manager.recoveryStatus().reason());
        }
    }
    @Test void D12_wrongErrorRelatedTypeCannotRejectCurrentRepairAsLegitimateRemoteError()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);learn(f,relay,remote,"A");lose(f,relay);
            f.timers.advance(NeighborDiscovery.TICK);f.pump();var c=outgoing(f,true);
            f.message(c,new ControlMessage(MessageType.ERROR,c.last().requestId(),"A",UUID.randomUUID(),new ControlMessage.ErrorPayload(ErrorCode.RESOURCE_LIMIT,5,"wrong context")));
            assertFalse(c.open);assertEquals(1,c.writes.size());assertNotEquals(OverlayRecovery.RecoveryReason.REJECTED,f.manager.recoveryStatus().reason());
        }
    }
    static List<CandidateCache.CandidateView> candidates(Fixture f) {
        var stage=f.manager.candidates();f.pump();return stage.toCompletableFuture().join();
    }
    @Test void D16_staleDiscoveryOnRepairedSessionPreservesQueryCacheAndConnection()throws Exception {
        for(String kind:List.of("token-snapshot","token-query","rejected-snapshot","rejected-query","generation","query"))try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var c=repair(f,"A",remote,18000,true);var session=f.manager.snapshot().get("A");
            f.timers.advance(NeighborDiscovery.TICK);f.pump();var first=c.extraWrites.getLast();
            var hello=((DiscoveryMessage.Query)first.payload()).helloRequestId();var candidate=endpoint("S",UUID.randomUUID(),18100);
            f.message(c,new ControlMessage(MessageType.DISCOVERY_SNAPSHOT,first.requestId(),"A",remote,
                new DiscoveryMessage.Snapshot(hello,"Z",f.instance,4,List.of(candidate))));var before=candidates(f);
            f.timers.advance(NeighborDiscovery.REFRESH);f.pump();var query=c.extraWrites.getLast();long began=f.clock.get();int writes=c.extraWrites.size();
            assertNotEquals(first.requestId(),query.requestId());var token=kind.equals("generation")||kind.equals("query")?hello:UUID.randomUUID();
            if(kind.startsWith("rejected")) {
                var type=kind.endsWith("query")?MessageType.DISCOVERY_QUERY:MessageType.DISCOVERY_SNAPSHOT;
                var envelope=new MessageCodec.ExtraEnvelope(2,type,query.requestId(),"A",remote,token,"Z",f.instance);
                f.io.sink.rejected(c,new MessageCodec.RejectedExtraContent(envelope,MessageCodec.ContentRejection.INVALID_MESSAGE),f.clock.get());f.pump();
            } else if(kind.equals("token-query")) {
                f.message(c,new ControlMessage(MessageType.DISCOVERY_QUERY,query.requestId(),"A",remote,new DiscoveryMessage.Query(token,"Z",f.instance)));
            } else f.message(c,new ControlMessage(MessageType.DISCOVERY_SNAPSHOT,kind.equals("query")?first.requestId():query.requestId(),"A",remote,
                new DiscoveryMessage.Snapshot(token,"Z",f.instance,kind.equals("generation")?3:Long.MAX_VALUE,List.of(endpoint("OTHER",UUID.randomUUID(),18200)))));
            assertTrue(c.open,kind);assertEquals(session,f.manager.snapshot().get("A"));assertEquals(0,f.downs());
            assertEquals(before,candidates(f),kind);assertEquals(writes,c.extraWrites.size(),kind);
            f.message(c,new ControlMessage(MessageType.DISCOVERY_SNAPSHOT,query.requestId(),"A",remote,
                new DiscoveryMessage.Snapshot(hello,"Z",f.instance,5,List.of(candidate))));
            var proof=candidates(f).getFirst().sources().getFirst();assertEquals(5,proof.generation(),kind);assertEquals(began,proof.queryStartedNanos());
            assertEquals(session.connectionId(),proof.source().connectionId());assertEquals(hello,proof.source().helloRequestId());
            assertEquals(session,f.manager.snapshot().get("A"));assertEquals(0,f.downs());
        }
    }
    @Test void D17_onlyConfirmedRepairedAdjacencyIsAdvertised()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);var hello=relay.last().requestId();
            var pending=repair(f,"A",UUID.randomUUID(),18000,false);assertFalse(f.manager.snapshot().containsKey("A"));
            f.message(relay,new ControlMessage(MessageType.DISCOVERY_QUERY,UUID.randomUUID(),"R",remote,new DiscoveryMessage.Query(hello,"Z",f.instance)));
            var before=(DiscoveryMessage.Snapshot)relay.extraWrites.getLast().payload();assertEquals(List.of("R"),before.directNeighbors().stream().map(ControlMessage.Endpoint::nodeId).toList());
            pending.written();f.pump();assertTrue(f.manager.snapshot().containsKey("A"));f.timers.advance(NeighborDiscovery.INBOUND_INTERVAL);f.pump();
            f.message(relay,new ControlMessage(MessageType.DISCOVERY_QUERY,UUID.randomUUID(),"R",remote,new DiscoveryMessage.Query(hello,"Z",f.instance)));
            var after=(DiscoveryMessage.Snapshot)relay.extraWrites.getLast().payload();assertEquals(Set.of("R","A"),new HashSet<>(after.directNeighbors().stream().map(ControlMessage.Endpoint::nodeId).toList()));
        }
    }
    @Test void D07_selectionRecordsPinnedProvenanceOnce()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);learn(f,relay,remote,"A");var proof=candidates(f).getFirst().sources().getFirst();lose(f,relay);
            f.timers.advance(NeighborDiscovery.TICK);f.pump();var events=f.events.stream().filter(NeighborManager.RepairStarted.class::isInstance).map(NeighborManager.RepairStarted.class::cast).toList();
            assertEquals(1,events.size());var a=events.getFirst().attempt();assertEquals(proof.source(),a.source());assertEquals(proof.generation(),a.generation());
            assertEquals(proof.queryStartedNanos(),a.queryStartedNanos());assertEquals(proof.ttlNanos(),a.ttlNanos());assertEquals(f.instance,a.instanceId());assertEquals("A",a.targetId());
            assertNotEquals(ZERO,a.requestId());assertTrue(events.getFirst().toString().length()<1024);
        }
    }
    @Test void D15_expiredOriginalClaimCannotEnqueueHelloWithoutTickDispatch()throws Exception {
        try(var f=new Fixture("Z")) {
            f.ready("R");var remote=UUID.randomUUID();var relay=f.inbound("R",remote,true);learn(f,relay,remote,"A");
            long began=candidates(f).getFirst().sources().getFirst().queryStartedNanos();
            f.clock.set(began+CandidateCache.TTL_NANOS-150_000_000L);lose(f,relay);
            f.timers.advance(NeighborDiscovery.TICK);f.pump();assertEquals(2,f.io.dials.size());
            f.clock.addAndGet(60_000_000L);var c=outgoing(f,true);
            assertTrue(c.writes.isEmpty(),"Expired provenance must not authorize an enqueued REPAIR_HELLO");
            assertFalse(c.open);assertEquals(0,f.manager.snapshot().size());assertEquals(1,f.downs());
        }
    }
    /** Configure 62 confirmed peers plus an optional 63rd configured attempt. No time advances. */
    static void degreeBoundary(Fixture f,String configuredPending) {
        var bootstrap=f.discovery();var entries=new ArrayList<ControlMessage.Endpoint>();
        for(int n=0;n<62;n++)entries.add(endpoint(String.format("P%02d",n),ZERO,20000+n*2));
        if(configuredPending!=null)entries.add(endpoint(configuredPending,ZERO,21000));
        f.message(bootstrap,new ControlMessage(MessageType.REGISTER_OK,bootstrap.last().requestId(),"BOOT",UUID.randomUUID(),new ControlMessage.NeighborList(entries)));
        assertEquals(64,f.io.options.maxConnections());
    }
    static Map<String,Neighbor.Session> confirmSixtyTwo(Fixture f) {
        for(int n=0;n<62;n++)configuredPending(f,String.format("P%02d",n),20000+n*2,true);
        assertEquals(62,f.manager.snapshot().size());assertEquals(62,f.ups());return f.manager.snapshot();
    }
    static FakeChannel configuredPending(Fixture f,String id,int port,boolean flush) {
        var c=new FakeChannel();c.flush=flush;var remote=UUID.randomUUID();f.io.sink.opened(c,f.clock.get());f.pump();
        f.message(c,new ControlMessage(MessageType.HELLO,UUID.randomUUID(),id,remote,new ControlMessage.Hello(endpoint(id,remote,port),3,3)));return c;
    }
    static void assertDegreeWinner(Fixture f,Map<String,Neighbor.Session> healthy,String winner,String loser,FakeChannel losing) {
        assertEquals(63,f.manager.snapshot().size());assertEquals(63,f.ups());assertEquals(0,f.downs());assertFalse(losing.open);
        assertTrue(f.manager.snapshot().containsKey(winner));assertFalse(f.manager.snapshot().containsKey(loser));
        healthy.forEach((id,session)->assertEquals(session,f.manager.snapshot().get(id),"Healthy session must survive confirmation saturation"));
        assertTrue(f.events.stream().filter(NeighborManager.NeighborUp.class::isInstance).map(NeighborManager.NeighborUp.class::cast).noneMatch(e->e.session().nodeId().equals(loser)));
        assertTrue(f.events.stream().filter(NeighborManager.Diagnostic.class::isInstance).map(NeighborManager.Diagnostic.class::cast)
            .anyMatch(e->e.reason()==NeighborManager.Reason.RESOURCE_LIMIT&&e.connectionId().filter(losing.id()::equals).isPresent()));
        assertFalse(f.owner.stopped);
    }
    @Test void D13_degreeConfirmationTwoRepairsCannotBothPassSixtyThree()throws Exception {
        for(boolean firstWins:List.of(false,true))try(var f=new Fixture("Z")) {
            degreeBoundary(f,null);var healthy=confirmSixtyTwo(f);
            var a=repair(f,"A",UUID.randomUUID(),22000,false);var b=repair(f,"B",UUID.randomUUID(),22200,false);
            assertTrue(a.open);assertTrue(b.open);assertEquals(62,f.manager.snapshot().size());
            var winner=firstWins?a:b;var loser=firstWins?b:a;winner.written();f.pump();assertEquals(63,f.manager.snapshot().size());
            loser.written();f.pump();assertDegreeWinner(f,healthy,firstWins?"A":"B",firstWins?"B":"A",loser);
        }
    }
    @Test void D13_degreeConfiguredInboundAndRepairShareConfirmationLimit()throws Exception {
        for(boolean configuredWins:List.of(false,true))try(var f=new Fixture("Z")) {
            degreeBoundary(f,"P62");var healthy=confirmSixtyTwo(f);
            var normal=configuredPending(f,"P62",21000,false);var extra=repair(f,"A",UUID.randomUUID(),22000,false);
            assertTrue(normal.open);assertTrue(extra.open);assertEquals(62,f.manager.snapshot().size());
            var winner=configuredWins?normal:extra;var loser=configuredWins?extra:normal;winner.written();f.pump();assertEquals(63,f.manager.snapshot().size());
            loser.written();f.pump();assertDegreeWinner(f,healthy,configuredWins?"P62":"A",configuredWins?"A":"P62",loser);
        }
    }
    @Test void D13_degreeConfiguredOutboundAckAndFlushInterleaveWithRepair()throws Exception {
        for(int order=0;order<4;order++)try(var f=new Fixture("Z")) {
            degreeBoundary(f,"ZZ");var normal=outgoing(f,false);assertEquals(MessageType.HELLO,normal.last().type());var healthy=confirmSixtyTwo(f);
            var extra=repair(f,"A",UUID.randomUUID(),22000,false);var ack=new ControlMessage(MessageType.HELLO_OK,normal.last().requestId(),"ZZ",UUID.randomUUID(),new ControlMessage.HelloOk(3));
            // An ACK alone or a local flush alone never confirms an outbound session.
            if(order%2==0)f.message(normal,ack);else {normal.written();f.pump();}
            assertEquals(62,f.manager.snapshot().size());assertEquals(62,f.ups());
            boolean configuredWins=order<2;
            if(configuredWins) {
                if(order%2==0){normal.written();f.pump();}else f.message(normal,ack);
                assertEquals(63,f.manager.snapshot().size());extra.written();f.pump();
            }else {
                extra.written();f.pump();assertEquals(63,f.manager.snapshot().size());
                if(order%2==0){normal.written();f.pump();}else f.message(normal,ack);
            }
            assertDegreeWinner(f,healthy,configuredWins?"ZZ":"A",configuredWins?"A":"ZZ",configuredWins?extra:normal);
        }
    }
}
