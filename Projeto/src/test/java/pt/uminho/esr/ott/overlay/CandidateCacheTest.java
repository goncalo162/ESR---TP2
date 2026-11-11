package pt.uminho.esr.ott.overlay;

import org.junit.jupiter.api.Test;
import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.protocol.ControlMessage;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CandidateCacheTest {
    static NodeConfiguration config() {
        return new NodeConfiguration(Path.of("C.properties"),"C",NodeRole.CLIENT,"127.0.0.1","127.0.0.1",4100,4200,
                Optional.of(new NodeConfiguration.BootstrapContact("127.0.0.1",4000)),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),List.of());
    }
    static ControlMessage.Endpoint ep(String id,int port) { return new ControlMessage.Endpoint(id,UUID.randomUUID(),"127.0.0.1",port,port+1,NodeRole.RELAY); }
    static CandidateCache.SourceKey source(String id) { return new CandidateCache.SourceKey(id,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID()); }
    static CandidateCache cache() { return new CandidateCache(config(),Map.of(),r -> { }); }
    @Test void D05_retainsAfterAnnouncerDeathAndExpiresAtExactLocalDeadline() {
        var cache=cache(); var source=source("R2"); cache.accept(source,1,List.of(ep("R1",5000)),0,2_000_000_000L);
        cache.retire(source); assertEquals("R1",cache.snapshot(59_999_999_999L).getFirst().nodeId());
        assertTrue(cache.snapshot(60_000_000_000L).isEmpty());
    }
    @Test void D06_oldGenerationAndRetiredCallbackCannotRenewAndClockWrapIsSafe() {
        var cache=cache(); var source=source("R2"); var entry=ep("R1",5000); long start=Long.MAX_VALUE-10_000_000_000L;
        cache.accept(source,3,List.of(entry),start,start); cache.accept(source,2,List.of(entry),start+20_000_000_000L,start+20_000_000_000L);
        cache.retire(source); cache.accept(source,4,List.of(entry),start+40_000_000_000L,start+40_000_000_000L);
        assertTrue(cache.snapshot(start+60_000_000_000L).isEmpty());
    }
    @Test void D06_freshSourcesHaveIndependentExpiryAndSnapshotsAreImmutable() {
        var cache=cache(); var a=source("R2"); var b=source("R3"); var ep=ep("R1",5000);
        cache.accept(a,1,List.of(ep),0,0); cache.accept(b,1,List.of(ep),20_000_000_000L,20_000_000_000L);
        var view=cache.snapshot(60_000_000_000L); assertEquals(1,view.getFirst().sources().size());
        assertThrows(UnsupportedOperationException.class,view::clear);
        assertThrows(UnsupportedOperationException.class,view.getFirst().sources()::clear);
        assertTrue(cache.snapshot(80_000_000_000L).isEmpty());
    }
    @Test void D06_caps128CandidatesAndFourSourcesWithoutEvictingValidClaims() {
        var cache=cache(); int index=0;
        for (int batch=0;batch<3;batch++) {
            var list=new ArrayList<ControlMessage.Endpoint>();
            for (int n=0;n<50;n++) list.add(ep("N"+index++,5000+index*2));
            cache.accept(source("R"+batch),1,list,0,0);
        }
        assertEquals(128,cache.snapshot(0).size()); var first=cache.snapshot(0).getFirst().endpoint().orElseThrow();
        for (int n=0;n<8;n++) cache.accept(source("X"+n),1,List.of(ep(first.nodeId(),first.controlPort())),0,0);
        assertEquals(4,cache.snapshot(0).getFirst().sources().size());
    }
    @Test void D07_conflictQuarantinesUntilDivergentClaimExpires() {
        var cache=cache(); var a=source("R2"); var b=source("R3");
        cache.accept(a,1,List.of(ep("R1",5000)),0,0); cache.accept(b,1,List.of(ep("R1",6000)),10_000_000_000L,10_000_000_000L);
        assertEquals(CandidateCache.CandidateState.CONFLICT,cache.snapshot(10_000_000_000L).getFirst().state());
        var view=cache.snapshot(60_000_000_000L).getFirst(); assertEquals(CandidateCache.CandidateState.AVAILABLE,view.state());
        assertEquals(6000,view.endpoint().orElseThrow().controlPort());
    }
    @Test void D07_selfConfiguredCollisionAndOmissionDoNotChangeAuthoritativeState() {
        var configured=ep("R2",5100); var cache=new CandidateCache(config(),Map.of("R2",configured),r->{ }); var source=source("R3");
        cache.accept(source,1,List.of(ep("C",5200),ep("R2",5300),ep("X",4100),ep("Y",5100),ep("R1",5400)),0,0);
        assertEquals(List.of("R1"),cache.snapshot(0).stream().map(CandidateCache.CandidateView::nodeId).toList());
        cache.accept(source,2,List.of(),10_000_000_000L,10_000_000_000L);
        assertTrue(cache.snapshot(60_000_000_000L).isEmpty());
    }
    @Test void D07_snapshotStructurallyInvalidIsAtomicAndTcpCollisionsConflictAcrossIds() {
        var cache=cache(); var source=source("R2"); var one=ep("R1",5000);
        cache.accept(source,1,List.of(one,one),0,0); assertTrue(cache.snapshot(0).isEmpty());
        cache.accept(source,2,List.of(one,ep("X",5000)),0,0);
        assertTrue(cache.snapshot(0).stream().allMatch(v->v.state()==CandidateCache.CandidateState.CONFLICT));
    }
    @Test void D09_noLearningAndExpiredQueriesLeaveNoCandidate() {
        var cache=cache(); assertTrue(cache.snapshot(0).isEmpty());
        cache.accept(source("R2"),1,List.of(ep("R1",5000)),0,60_000_000_000L); assertTrue(cache.snapshot(60_000_000_000L).isEmpty());
    }
    @Test void D07_transportNamespacesRemainSeparateAndActualEndpointCollisionsAreFiltered() {
        var cache=cache();var source=source("R2");
        var tcpOnUdp=new ControlMessage.Endpoint("X",UUID.randomUUID(),"127.0.0.1",4200,6100,NodeRole.RELAY);
        var udpOnTcp=new ControlMessage.Endpoint("Y",UUID.randomUUID(),"127.0.0.1",6200,4100,NodeRole.RELAY);
        var badTcp=new ControlMessage.Endpoint("T",UUID.randomUUID(),"127.0.0.1",4100,6300,NodeRole.RELAY);
        var badUdp=new ControlMessage.Endpoint("U",UUID.randomUUID(),"127.0.0.1",6400,4200,NodeRole.RELAY);
        cache.accept(source,1,List.of(tcpOnUdp,udpOnTcp,badTcp,badUdp),0,0);
        assertEquals(Set.of("X","Y"),cache.snapshot(0).stream().map(CandidateCache.CandidateView::nodeId).collect(java.util.stream.Collectors.toSet()));
        assertTrue(cache.snapshot(0).stream().allMatch(v->v.state()==CandidateCache.CandidateState.AVAILABLE));
    }

}
