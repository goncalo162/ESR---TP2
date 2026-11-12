package pt.uminho.esr.ott.overlay;

import pt.uminho.esr.ott.config.NodeConfiguration;
import pt.uminho.esr.ott.protocol.ControlMessage;
import java.util.*;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;

/** Owner-confined selection/retry policy. Sockets, dials and physical accounting remain in NeighborManager. */
public final class OverlayRecovery {
    static final long TOTAL_BUDGET=10_000_000_000L;
    public enum Phase { WAITING, ISOLATED, REQUESTING, ACTIVE, STOPPING }
    public enum RecoveryReason { NONE, NO_CANDIDATE, BACKOFF, RESOURCE_LIMIT, CONNECTING,
        EXPIRED, CONFLICT, TRANSPORT, TIMEOUT, REJECTED, SATISFIED, SHUTDOWN, EXHAUSTED }
    public record RecoveryStatus(Phase phase,RecoveryReason reason,int activeCount,Optional<String> candidate,
            long retryDelayMillis) {
        public RecoveryStatus {Objects.requireNonNull(phase);Objects.requireNonNull(reason);Objects.requireNonNull(candidate);}
    }
    public record RepairAttempt(UUID requestId,UUID instanceId,long needEpoch,CandidateCache.SourceKey source,
            String targetId,long generation,long queryStartedNanos,long ttlNanos,long selectedAtNanos) {
        public RepairAttempt {Objects.requireNonNull(requestId);Objects.requireNonNull(instanceId);Objects.requireNonNull(source);Objects.requireNonNull(targetId);}
    }
    interface RecoveryPorts {
        Map<String,Neighbor.Session> active();
        List<CandidateCache.CandidateView> candidates(long now);
        boolean beginRepair(RepairAttempt attempt,ControlMessage.Endpoint target,long remainingNanos);
        void cancelRepair(RepairAttempt attempt);
        void publish(RecoveryStatus status);
        default long retryDelay(long baseMillis){return baseMillis;}
    }
    private record Retry(long began,long baseMillis,long delayMillis) {}
    private final NodeConfiguration configuration;
    private final UUID instance;
    private final LongSupplier clock;
    private final Supplier<UUID> ids;
    private final RecoveryPorts ports;
    private final Map<String,Retry> retries=new LinkedHashMap<>();
    private RepairAttempt current;
    private CandidateCache.CandidateEndpoint selectedEndpoint;
    private long epoch;
    private int cursor;
    private boolean isolated,closed;
    private RecoveryStatus status=new RecoveryStatus(Phase.WAITING,RecoveryReason.NONE,0,Optional.empty(),0);

    OverlayRecovery(NodeConfiguration configuration,UUID instance,LongSupplier clock,Supplier<UUID> ids,RecoveryPorts ports) {
        this.configuration=Objects.requireNonNull(configuration);this.instance=Objects.requireNonNull(instance);
        this.clock=Objects.requireNonNull(clock);this.ids=Objects.requireNonNull(ids);this.ports=Objects.requireNonNull(ports);
    }
    void onNeighborUp(Neighbor.Session session){maintain(clock.getAsLong());}
    void onNeighborDown(Neighbor.Session session){if(closed)return;publish(ports.active().isEmpty()?Phase.ISOLATED:Phase.ACTIVE,RecoveryReason.NONE,Optional.empty(),0);}
    boolean isCurrent(RepairAttempt attempt){return !closed && current==attempt;}
    private boolean expired(RepairAttempt a,long now) {
        return now-a.selectedAtNanos()>=TOTAL_BUDGET || now-a.queryStartedNanos()<0 || now-a.queryStartedNanos()>=a.ttlNanos();
    }
    boolean eligible(RepairAttempt attempt,long now) {
        if(!isCurrent(attempt)||!ports.active().isEmpty()||expired(attempt,now))return false;
        return ports.candidates(now).stream().anyMatch(c->c.nodeId().equals(attempt.targetId())
            && c.state()==CandidateCache.CandidateState.AVAILABLE && c.endpoint().filter(selectedEndpoint::equals).isPresent());
    }
    void onAttemptTerminal(RepairAttempt attempt,RecoveryReason reason) {
        if(!isCurrent(attempt))return;
        current=null;selectedEndpoint=null;
        long max=configuration.runtime().overlay().retryMax().toMillis();var previous=retries.get(attempt.targetId());
        long base=previous==null?configuration.runtime().overlay().retryInitial().toMillis():previous.baseMillis()>max/2?max:Math.min(max,previous.baseMillis()*2);
        long delay=Math.max(1,Math.min(max,ports.retryDelay(base)));
        retries.put(attempt.targetId(),new Retry(clock.getAsLong(),base,delay));
        publish(Phase.ISOLATED,reason,Optional.of(attempt.targetId()),delay);
    }
    void maintain(long now) {
        if(closed)return;
        if(!ports.active().isEmpty()) {
            var old=current;current=null;selectedEndpoint=null;isolated=false;
            if(old!=null)ports.cancelRepair(old);
            publish(Phase.ACTIVE,RecoveryReason.SATISFIED,Optional.empty(),0);return;
        }
        if(!isolated) {
            if(epoch==Long.MAX_VALUE){publish(Phase.ISOLATED,RecoveryReason.EXHAUSTED,Optional.empty(),0);return;}
            epoch++;isolated=true;
        }
        if(current!=null) {
            if(eligible(current,now))return;
            var old=current;var reason=now-old.selectedAtNanos()>=TOTAL_BUDGET?RecoveryReason.TIMEOUT:expired(old,now)?RecoveryReason.EXPIRED:RecoveryReason.CONFLICT;
            onAttemptTerminal(old,reason);ports.cancelRepair(old);return;
        }
        var candidates=new ArrayList<>(ports.candidates(now));
        if(candidates.size()>CandidateCache.MAX_CANDIDATES)throw new IllegalArgumentException("Candidate snapshot cap");
        candidates.sort(Comparator.comparing(CandidateCache.CandidateView::nodeId));
        var present=new HashSet<String>();for(var c:candidates)present.add(c.nodeId());retries.keySet().retainAll(present);
        long nearest=Long.MAX_VALUE;int size=candidates.size();
        for(int n=0;n<size;n++) {
            int index=(cursor+n)%size;var c=candidates.get(index);
            if(c.nodeId().equals(configuration.nodeId())||c.state()!=CandidateCache.CandidateState.AVAILABLE||c.endpoint().isEmpty())continue;
            var retry=retries.get(c.nodeId());
            if(retry!=null) {
                long left=TimeUnit.MILLISECONDS.toNanos(retry.delayMillis())-(now-retry.began());
                if(left>0){nearest=Math.min(nearest,left);continue;}
            }
            var proof=c.sources().stream().filter(p->p.ttlNanos()>0 && now-p.queryStartedNanos()>=0 && now-p.queryStartedNanos()<p.ttlNanos())
                .sorted(Comparator.comparing((CandidateCache.Provenance p)->p.source().nodeId())
                    .thenComparing(p->p.source().connectionId().toString())).findFirst();
            if(proof.isEmpty())continue;
            var id=Objects.requireNonNull(ids.get());if(id.equals(new UUID(0,0))){publish(Phase.ISOLATED,RecoveryReason.EXHAUSTED,Optional.empty(),0);return;}
            var p=proof.get();selectedEndpoint=c.endpoint().orElseThrow();
            current=new RepairAttempt(id,instance,epoch,p.source(),c.nodeId(),p.generation(),p.queryStartedNanos(),p.ttlNanos(),now);
            var endpoint=new ControlMessage.Endpoint(c.nodeId(),new UUID(0,0),selectedEndpoint.host(),selectedEndpoint.controlPort(),selectedEndpoint.dataPort(),selectedEndpoint.role());
            cursor=(index+1)%size;publish(Phase.REQUESTING,RecoveryReason.CONNECTING,Optional.of(c.nodeId()),0);
            var attempt=current;long remaining=Math.min(TOTAL_BUDGET,p.ttlNanos()-(now-p.queryStartedNanos()));
            if(!ports.beginRepair(attempt,endpoint,remaining))onAttemptTerminal(attempt,RecoveryReason.RESOURCE_LIMIT);
            return;
        }
        boolean failure= switch(status.reason()) {
            case RESOURCE_LIMIT,EXPIRED,CONFLICT,TRANSPORT,TIMEOUT,REJECTED -> true;
            default -> false;
        };
        // Keep the terminal reason visible while reporting the remaining retry delay.
        publish(Phase.ISOLATED,nearest==Long.MAX_VALUE?RecoveryReason.NO_CANDIDATE:failure?status.reason():RecoveryReason.BACKOFF,
            nearest!=Long.MAX_VALUE&&failure?status.candidate():Optional.empty(),
            nearest==Long.MAX_VALUE?0:Math.max(1,TimeUnit.NANOSECONDS.toMillis(nearest)));
    }
    void requestClose(long remainingNanos) {
        if(closed)return;closed=true;var old=current;current=null;selectedEndpoint=null;
        if(old!=null)ports.cancelRepair(old);retries.clear();publish(Phase.STOPPING,RecoveryReason.SHUTDOWN,Optional.empty(),0);
    }
    RecoveryStatus snapshot(){return status;}
    private void publish(Phase phase,RecoveryReason reason,Optional<String> candidate,long retry) {
        var next=new RecoveryStatus(phase,reason,ports.active().size(),candidate,retry);
        if(!next.equals(status)){status=next;ports.publish(next);}
    }
}
