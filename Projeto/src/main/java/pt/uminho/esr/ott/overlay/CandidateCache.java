package pt.uminho.esr.ott.overlay;

import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.protocol.ControlMessage;
import pt.uminho.esr.ott.protocol.DiscoveryCodec;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/** Bounded, owner-confined learned claims. Retiring a source never extends or deletes its TTL. */
public final class CandidateCache {
    public static final long TTL_NANOS = 60_000_000_000L;
    public static final int MAX_CANDIDATES=128, MAX_SOURCES=4;
    public enum CandidateState { AVAILABLE, CONFLICT }
    public enum Rejection { SELF, CONFIGURED, CONFLICT, CAPACITY, STALE, INVALID, EXPIRED }
    public record SourceKey(String nodeId, UUID instanceId, UUID connectionId, UUID helloRequestId) {
        public SourceKey {
            Objects.requireNonNull(nodeId); Objects.requireNonNull(instanceId); Objects.requireNonNull(connectionId); Objects.requireNonNull(helloRequestId);
            if(!nodeId.matches("[A-Za-z0-9_-]{1,32}")||instanceId.equals(new UUID(0,0))) throw new IllegalArgumentException("Source identity");
        }
    }
    public record CandidateEndpoint(String nodeId, String host, int controlPort, int dataPort, NodeRole role) {
        public CandidateEndpoint { host=DiscoveryCodec.normalizeHost(host); Objects.requireNonNull(nodeId); Objects.requireNonNull(role); }
    }
    public record Provenance(SourceKey source, long generation, CandidateEndpoint advertised, UUID observedInstance,
            long queryStartedNanos, long learnedNanos, long ttlNanos) { }
    public record CandidateView(String nodeId, CandidateState state, Optional<CandidateEndpoint> endpoint,
            List<Provenance> sources) {
        public CandidateView { Objects.requireNonNull(nodeId); Objects.requireNonNull(state); Objects.requireNonNull(endpoint); sources=List.copyOf(sources); }
    }
    private static final class Claim {
        final Provenance proof; boolean retired;
        Claim(Provenance proof) { this.proof=proof; }
    }
    private final String localId;
    private final Map<String,ControlMessage.Endpoint> configured;
    private final Set<String> protectedTcp=new HashSet<>(),protectedUdp=new HashSet<>();
    private final Map<String,LinkedHashMap<SourceKey,Claim>> candidates=new LinkedHashMap<>();
    private final Consumer<Rejection> diagnostic;
    public CandidateCache(NodeConfiguration config,Map<String,ControlMessage.Endpoint> configured,Consumer<Rejection> diagnostic) {
        localId=config.nodeId(); this.configured=Map.copyOf(configured); this.diagnostic=Objects.requireNonNull(diagnostic);
        protect(config.advertisedHost(),config.controlPort()); protectedUdp.add(tcp(config.advertisedHost(),config.dataPort()));
        config.bootstrap().ifPresent(b->protect(b.host(),b.port()));
        for(var endpoint:configured.values()) { protect(endpoint.host(),endpoint.controlPort()); protectedUdp.add(tcp(endpoint.host(),endpoint.dataPort())); }
    }
    private void protect(String host,int port) { protectedTcp.add(tcp(host,port)); }
    private static String tcp(String host,int port) { return DiscoveryCodec.normalizeHost(host)+"|"+port; }
    private static CandidateEndpoint endpoint(ControlMessage.Endpoint e) { return new CandidateEndpoint(e.nodeId(),e.host(),e.controlPort(),e.dataPort(),e.role()); }
    public void accept(SourceKey source,long generation,List<ControlMessage.Endpoint> entries,long queryStarted,long now) {
        Objects.requireNonNull(source);
        try { DiscoveryCodec.validateSnapshot(source.nodeId(),entries); }
        catch(IOException|IllegalArgumentException|NullPointerException error) { diagnostic.accept(Rejection.INVALID); return; }
        expire(now);
        if(generation<=0||now-queryStarted<0||now-queryStarted>=TTL_NANOS) { diagnostic.accept(Rejection.STALE); return; }
        for(var claims:candidates.values()) {
            var previous=claims.get(source);
            if(previous!=null&&(previous.retired||previous.proof.generation()>=generation)) { diagnostic.accept(Rejection.STALE); return; }
        }
        for(var e:entries) {
            if(e.nodeId().equals(localId)) { diagnostic.accept(Rejection.SELF); continue; }
            if(configured.containsKey(e.nodeId())) { diagnostic.accept(Rejection.CONFIGURED); continue; }
            if(protectedTcp.contains(tcp(e.host(),e.controlPort()))||protectedUdp.contains(tcp(e.host(),e.dataPort()))) { diagnostic.accept(Rejection.CONFLICT); continue; }
            var claims=candidates.get(e.nodeId());
            if(claims==null) {
                if(candidates.size()>=MAX_CANDIDATES) { diagnostic.accept(Rejection.CAPACITY); continue; }
                claims=new LinkedHashMap<>(); candidates.put(e.nodeId(),claims);
            }
            if(!claims.containsKey(source)&&claims.size()>=MAX_SOURCES) { diagnostic.accept(Rejection.CAPACITY); continue; }
            claims.put(source,new Claim(new Provenance(source,generation,endpoint(e),e.instanceId(),queryStarted,now,TTL_NANOS)));
        }
    }
    public void retire(SourceKey source) { for(var claims:candidates.values()) { var claim=claims.get(source); if(claim!=null) claim.retired=true; } }
    private void expire(long now) {
        var nodes=candidates.values().iterator();
        while(nodes.hasNext()) {
            var claims=nodes.next().values().iterator();
            while(claims.hasNext()) {
                var proof=claims.next().proof;
                if(now-proof.queryStartedNanos()>=proof.ttlNanos()) { claims.remove(); diagnostic.accept(Rejection.EXPIRED); }
            }
        }
        candidates.values().removeIf(Map::isEmpty);
    }
    void maintenance(long now) {expire(now);}
    public List<CandidateView> snapshot(long now) {
        expire(now); var result=new ArrayList<CandidateView>(candidates.size());
        for(var node:candidates.entrySet()) {
            var proofs=node.getValue().values().stream().map(c->c.proof).toList();
            var first=proofs.getFirst().advertised(); boolean conflict=proofs.stream().anyMatch(p->!p.advertised().equals(first));
            for(var other:candidates.entrySet()) if(!other.getKey().equals(node.getKey())) {
                for(var claim:other.getValue().values()) for(var proof:proofs)
                    if(collides(proof.advertised(),claim.proof.advertised())) conflict=true;
            }
            if(conflict) diagnostic.accept(Rejection.CONFLICT);
            result.add(new CandidateView(node.getKey(),conflict?CandidateState.CONFLICT:CandidateState.AVAILABLE,conflict?Optional.empty():Optional.of(first),proofs));
        }
        return List.copyOf(result);
    }
    private static boolean collides(CandidateEndpoint a,CandidateEndpoint b) {
        return a.host().equals(b.host())&&(a.controlPort()==b.controlPort()||a.dataPort()==b.dataPort());
    }
}
