package pt.uminho.esr.ott.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pt.uminho.esr.ott.overlay.NodeRole;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Migrated OTD1 oracles: sole v2 MessageCodec, frozen independent vectors. */
class DiscoveryCodecTest {
    static final UUID Q=new UUID(1,2),H=new UUID(3,4),A=new UUID(5,6),B=new UUID(7,8);
    static ControlMessage query(){return new ControlMessage(MessageType.DISCOVERY_QUERY,Q,"A",A,new DiscoveryMessage.Query(H,"B",B));}
    static ControlMessage snapshot(long generation,List<ControlMessage.Endpoint> entries){return new ControlMessage(MessageType.DISCOVERY_SNAPSHOT,Q,"B",B,new DiscoveryMessage.Snapshot(H,"A",A,generation,entries));}
    static ControlMessage.Endpoint endpoint(String id){return new ControlMessage.Endpoint(id,A,"127.0.0.1",4100,4200,NodeRole.RELAY);}
    static byte[] fixture(String name) throws Exception {try(var in=DiscoveryCodecTest.class.getResourceAsStream("/protocol/v2/tcp-"+name+".bin")){assertNotNull(in,name);return in.readAllBytes();}}
    @Test void D07_snapshotDefensiveCopyAndExplicitRole() throws Exception {
        var entries=new ArrayList<>(List.of(endpoint("C")));var m=snapshot(1,entries);entries.clear();
        var payload=(DiscoveryMessage.Snapshot)m.payload();assertEquals(1,payload.directNeighbors().size());
        assertThrows(UnsupportedOperationException.class,()->payload.directNeighbors().clear());
        byte[] wire=MessageCodec.encode(m);assertEquals(2,wire[4]);assertEquals(19,wire[5]);assertEquals(2,wire[wire.length-1]);
        assertEquals(m,MessageCodec.decode(wire));
    }
    @Test void D07_maximumSnapshotHas19674BodyBytes() throws Exception {
        var entries=new ArrayList<ControlMessage.Endpoint>();
        for(int i=0;i<63;i++)entries.add(new ControlMessage.Endpoint(("N"+i).concat("x".repeat(32-("N"+i).length())),A,
            "a".repeat(63)+"."+"b".repeat(63)+"."+"c".repeat(63)+"."+"d".repeat(61),65535,65534,NodeRole.CLIENT));
        var m=new ControlMessage(MessageType.DISCOVERY_SNAPSHOT,Q,"A".repeat(32),A,new DiscoveryMessage.Snapshot(H,"B".repeat(32),B,1,entries));
        byte[] wire=MessageCodec.encode(m);assertEquals(19678,wire.length);assertEquals(19674,ByteBuffer.wrap(wire).getInt());assertEquals(m,MessageCodec.decode(wire));
    }
    @ParameterizedTest @ValueSource(strings={"snapshot-generation-zero","snapshot-duplicate","snapshot-invalid-role","snapshot-invalid-count","snapshot-trailing"})
    void D07_completeInvalidContentHasTypedRejection(String name) throws Exception {
        byte[] wire=fixture(name);var decoded=MessageCodec.readFrame(new ByteArrayInputStream(wire)).orElseThrow();
        var rejected=assertInstanceOf(MessageCodec.RejectedExtraContent.class,decoded);assertEquals(MessageType.DISCOVERY_SNAPSHOT,rejected.envelope().type());
        assertEquals("S1",rejected.envelope().senderId());assertEquals("C",rejected.envelope().recipientId());
        assertThrows(ProtocolException.class,()->MessageCodec.decode(wire));
    }
    @ParameterizedTest @ValueSource(strings={"snapshot-truncated-endpoint","snapshot-field-length","discovery-over-cap","invalid-length-unsigned","invalid-identity-utf8",
            "invalid-capability-not-required","invalid-capability-missing","invalid-capability-reserved","invalid-selection","invalid-version-0","invalid-version-1","invalid-version-3","invalid-version-255"})
    void D07_terminalNegativesNeverBecomeContentRejections(String name) throws Exception {
        assertThrows(ProtocolException.class,()->MessageCodec.readFrame(new ByteArrayInputStream(fixture(name))));
    }
    @Test void D07_invalidContentThenNormalV2ThenValidSnapshotKeepBoundaries() throws Exception {
        var stream=new ByteArrayOutputStream();stream.write(fixture("snapshot-duplicate"));stream.write(fixture("probe-ping"));stream.write(fixture("discovery-snapshot"));
        var input=new ByteArrayInputStream(stream.toByteArray());assertInstanceOf(MessageCodec.RejectedExtraContent.class,MessageCodec.readFrame(input).orElseThrow());
        assertEquals(MessageType.PROBE_PING,assertInstanceOf(MessageCodec.Decoded.class,MessageCodec.readFrame(input).orElseThrow()).message().type());
        assertEquals(MessageType.DISCOVERY_SNAPSHOT,assertInstanceOf(MessageCodec.Decoded.class,MessageCodec.readFrame(input).orElseThrow()).message().type());assertTrue(MessageCodec.readFrame(input).isEmpty());
    }
    @Test void D07_utf8CandidateRecoverableButLaterTruncationTerminal() throws Exception {
        byte[] wire=MessageCodec.encode(snapshot(1,List.of(endpoint("C"))));// Fixed one-byte identity layout, independently calculated below.
        // One candidate starts after envelope(37)+token(16)+recipient(3)+instance(16)+generation/count(10).
        int start=4+37+16+3+16+10;wire[start+2]=(byte)0x80;
        assertInstanceOf(MessageCodec.RejectedExtraContent.class,MessageCodec.readFrame(new ByteArrayInputStream(wire)).orElseThrow());
        for(int end=1;end<wire.length;end++){byte[] cut=Arrays.copyOf(wire,end);assertThrows(EOFException.class,()->MessageCodec.readFrame(new ByteArrayInputStream(cut)));}
        byte[] full=Arrays.copyOf(wire,wire.length-1);ByteBuffer.wrap(full).putInt(full.length-4);
        assertThrows(ProtocolException.class,()->MessageCodec.readFrame(new ByteArrayInputStream(full)));
    }
    @Test void D07_invalidIdentityIsTerminalEvenWithBadPayload() throws Exception {
        byte[] wire=fixture("snapshot-duplicate");wire[24]=(byte)0x80;assertThrows(ProtocolException.class,()->MessageCodec.readFrame(new ByteArrayInputStream(wire)));
    }
    @Test void D07_rejectDuplicatesSenderZeroAndBadGenerationOnOutput() {
        for(var entries:List.of(List.of(endpoint("C"),endpoint("C")),List.of(endpoint("B")),List.of(new ControlMessage.Endpoint("C",new UUID(0,0),"127.0.0.1",1,2,NodeRole.RELAY))))
            assertThrows(IOException.class,()->MessageCodec.encode(snapshot(1,entries)));
        assertThrows(IOException.class,()->MessageCodec.encode(snapshot(0,List.of())));assertThrows(IOException.class,()->MessageCodec.encode(snapshot(-1,List.of())));
    }
    @Test void D01e_v1IsNegativeNotAnotherCodec() throws Exception {
        byte[] wire=MessageCodec.encode(query());wire[4]=1;assertEquals(ErrorCode.UNSUPPORTED_VERSION,assertThrows(ProtocolException.class,()->MessageCodec.decode(wire)).errorCode());
    }
    @Test void D07_discoveryCapIsAppliedBeforeBodyReadOrAllocation() {
        byte[] prefixAndType=ByteBuffer.allocate(6).putInt(32769).put((byte)2).put((byte)18).array();
        var input=new ByteArrayInputStream(prefixAndType){
            @Override public synchronized int read(byte[] b,int off,int len){fail("Oversized discovery body must not be read");return -1;}
        };
        assertEquals(ErrorCode.RESOURCE_LIMIT,assertThrows(ProtocolException.class,()->MessageCodec.readFrame(input)).errorCode());
    }

}
