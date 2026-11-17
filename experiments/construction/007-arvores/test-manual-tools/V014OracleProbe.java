package pt.uminho.esr.ott.routing;
import java.util.*;
import java.time.Duration;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.Connection;
import static pt.uminho.esr.ott.routing.ManualActivation.*;
/** Supplemental controlled Design boundaries; evidence only, no JUnit/project modifications. */
public final class V014OracleProbe {
    static int checks;
    static void check(boolean value,String label){if(!value)throw new AssertionError("CHECK_FAIL "+label);System.out.println("CHECK_PASS "+(++checks)+" "+label);}
    static void announce(Rig r,Neighbor.Session p,UUID stream){r.ad(p,stream,"S",EPOCH,1,100);}
    public static void main(String[] args){
        System.out.println("CODE_SOURCE="+RoutingService.class.getProtectionDomain().getCodeSource().getLocation());
        try(var s=new Rig("S",true,4096,3)){
            var c=peer("C1");s.up(c);s.act(()->s.router.setLocalAvailable(A,false));
            s.activate(c,A,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","C1"),6000);
            check(lastError(s)==ErrorCode.SOURCE_UNAVAILABLE&&s.forwarding(A)==null,"M03 unavailable source rejects A");
            var t=s.local(B,UUID.randomUUID());check(result(t).state()==RoutingService.SubscriptionState.SUCCESS,"M03 other local stream stays available");
        }
        try(var r=new Rig("R",false,4096,1)){
            var a=peer("A");r.route(a);var c1=peer("C1");var c2=peer("C2");r.up(c1);r.up(c2);var one=r.child(c1);var two=r.child(c2);
            r.error(r.latestRequest(),11,ErrorCode.SOURCE_UNAVAILABLE);
            check(r.messages(MessageType.ACTIVATE).size()==1,"M09 A=1 no retry");
            check(r.messages(MessageType.ERROR).stream().anyMatch(x->x.message().requestId().equals(one.requestId()))&&r.messages(MessageType.ERROR).stream().anyMatch(x->x.message().requestId().equals(two.requestId())),"M09 error per child's own requestId");
        }
        try(var r=new Rig()){
            var a=peer("A");r.route(a);r.ad(a,B,"S",EPOCH,1,100);var ta=r.local(A,UUID.randomUUID());var qa=r.latestRequest();var tb=r.local(B,UUID.randomUUID());var qb=r.latestRequest();r.ack(qa,1000);r.ack(qb,6000);
            var stable=r.forwarding(B);r.tick(1_000_000_000L);
            check(r.forwarding(A)==null&&stable.equals(r.forwarding(B)),"M10 A lease expiry preserves B exactly");
        }
        try(var r=new Rig()){
            var a=peer("A");r.route(a);var local=r.local(A,UUID.randomUUID());var req=r.latestRequest();r.tick(999_999L);r.ack(req,1);
            check(result(local).state()==RoutingService.SubscriptionState.SUCCESS&&r.forwarding(A).upstreamLease().orElseThrow().remainingNanos(r.clock.get())==1,"M12 ACK one nanosecond before t0+G activates for 1ns");
            r.tick(1);check(r.forwarding(A)==null,"M12 exact grant deadline removes authorization");
        }
        try(var r=new Rig()){
            var a=peer("A");r.route(a);var t=r.local(A,UUID.randomUUID());r.ack(r.latestRequest(),6000);
            var old=a;var fresh=new Neighbor.Session(a.nodeId(),a.instanceId(),UUID.randomUUID(),a.endpoint());r.up(fresh);r.metric(fresh,10);r.ad(fresh,2,100);
            var retry=r.router.retrySubscription(A,t.result().orElseThrow().consumerToken());r.pump();r.ack(r.latestRequest(),6000);var stable=r.forwarding(A);
            r.act(()->r.router.onNeighborEvent(new NeighborManager.NeighborDown(old,NeighborManager.Reason.TRANSPORT)));
            check(stable!=null&&stable.equals(r.forwarding(A)),"M11 old Down cannot invalidate replacement Session");
        }
        try(var r=new Rig()){
            var a=peer("A");r.route(a);for(int i=0;i<4;i++)r.local(A,UUID.randomUUID());var denied=r.local(A,UUID.randomUUID());
            check(r.load().locals()==4&&result(denied).state()==RoutingService.SubscriptionState.RESOURCE_LIMIT,"M13 four local tokens per stream; fifth refused");
        }
        try(var r=new Rig("R",false,2,3)){
            var a=peer("A");r.route(a);r.ad(a,B,"S",EPOCH,1,100);r.local(A,UUID.randomUUID());r.local(B,UUID.randomUUID());
            var denied=r.local(A,UUID.randomUUID());check(r.load().locals()==2&&result(denied).state()==RoutingService.SubscriptionState.RESOURCE_LIMIT,"M13 global local limit 2 across streams");
        }
        try(var s=new Rig("S",true,2,3)){
            for(String name:List.of("C1","C2")){var c=peer(name);s.up(c);s.activate(c,A,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S",name),6000);}
            var old=s.forwarding(A);var c=peer("C3");s.up(c);s.activate(c,A,EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S","C3"),6000);
            check(lastError(s)==ErrorCode.RESOURCE_LIMIT&&old.equals(s.forwarding(A))&&s.load().children()==2,"M13 child/history capacity refusal preserves two active leases");
        }
        try(var r=new Rig("R",false,2,3)){
            var a=peer("A");r.up(a);r.metric(a,10);for(int i=0;i<3;i++)announce(r,a,new UUID(17,i));
            var one=r.local(new UUID(17,0),UUID.randomUUID());var two=r.local(new UUID(17,1),UUID.randomUUID());var denied=r.local(new UUID(17,2),UUID.randomUUID());
            check(r.load().trees()==2&&result(denied).state()==RoutingService.SubscriptionState.RESOURCE_LIMIT,"M13 bounded two trees, third operation refused");
        }
        try(var r=new Rig()){
            var a=peer("A");r.up(a);r.metric(a,10);r.hold=m->m.type()==MessageType.ACTIVATE;
            for(int i=0;i<5;i++){var stream=new UUID(18,i);announce(r,a,stream);r.local(stream,UUID.randomUUID());}
            check(r.load().sends()==4&&r.messages(MessageType.ACTIVATE).size()==4,"M13 per-Session physical send limit 4, fifth intent waits");
            var first=r.messages(MessageType.ACTIVATE).getFirst();first.result().complete(Connection.SendResult.ACCEPTED);r.pump();
            check(r.load().sends()==4&&r.messages(MessageType.ACTIVATE).size()==5,"M13 fifth send admitted only after physical release");
            check(r.timers.physicalJobs()<=2,"M14 alarm physical jobs bounded under pending demand");
        }
        try(var s=new Rig("S",true,2,3)){
            s.hold=m->m.type()==MessageType.ERROR;
            for(int i=0;i<3;i++){var c=peer("C"+i);s.up(c);s.activate(c,new UUID(99,i),EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S",c.nodeId()),6000);}
            check(s.load().sends()==2&&s.load().unbound()==2&&s.messages(MessageType.ERROR).size()==2,"M13 J/B global cap 2, third unbound error dropped");
            var first=s.messages(MessageType.ERROR).getFirst();var c=first.peer();s.activate(c,new UUID(99,8),EPOCH,UUID.randomUUID(),1,UUID.randomUUID(),List.of("S",c.nodeId()),6000);
            check(s.messages(MessageType.ERROR).size()==2,"M13 no unbounded error backlog");
            first.result().complete(Connection.SendResult.ACCEPTED);s.pump();check(s.load().unbound()==1,"M13 J slot freed after callback/continuation");
        }
        System.out.println("CLEANUP_COMPLETE");System.out.println("ORACLES_PASS CHECKS="+checks);
    }
}
