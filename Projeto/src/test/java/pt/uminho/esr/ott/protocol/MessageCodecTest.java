package pt.uminho.esr.ott.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import pt.uminho.esr.ott.overlay.NodeRole;
import pt.uminho.esr.ott.config.ConfigurationLoader;
import pt.uminho.esr.ott.protocol.ControlMessage.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;
import static org.junit.jupiter.api.Assertions.*;

class MessageCodecTest {
    private static final UUID REQ=UUID.fromString("00112233-4455-6677-8899-aabbccddeeff");
    private static final UUID INST=UUID.fromString("10213243-5465-7687-98a9-bacbdcedfe0f");
    private static final UUID STREAM=UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID EPOCH=UUID.fromString("deadbeef-0000-4000-8000-000000000001");
    private static final UUID SUB=UUID.fromString("fedcba98-7654-4321-8765-0123456789ab");
    private static final UUID TRACE=UUID.fromString("11223344-5566-4788-99aa-bbccddeeff00");
    private static final UUID NIL=new UUID(0,0);
    private static final Endpoint ENDPOINT=new Endpoint("S1",INST,"10.0.0.1",5000,5001,NodeRole.SERVER);
    private static final Endpoint CONFIGURED_ENDPOINT=new Endpoint("S1",NIL,"10.0.0.1",5000,5001,NodeRole.SERVER);

    // Valores interpretados do contrato, independentes do encoder e decoder.
    static Stream<Arguments> goldenVectors() {
        return Stream.of(
            Arguments.of("register",msg(MessageType.REGISTER,"S1",new EndpointPayload(ENDPOINT))),
            Arguments.of("register-ok",msg(MessageType.REGISTER_OK,"B",new NeighborList(List.of(CONFIGURED_ENDPOINT)))),
            Arguments.of("node-leave",msg(MessageType.NODE_LEAVE,"S1",new Leave(0))),
            Arguments.of("hello",msg(MessageType.HELLO,"S1",new EndpointPayload(ENDPOINT))),
            Arguments.of("hello-ok-min",msg(MessageType.HELLO_OK,"C",Empty.INSTANCE)),
            Arguments.of("probe-ping",msg(MessageType.PROBE_PING,"S1",new Probe(42))),
            Arguments.of("probe-pong",msg(MessageType.PROBE_PONG,"S1",new Probe(42))),
            Arguments.of("stream-announce",announcement("A")),
            Arguments.of("stream-withdraw",msg(MessageType.STREAM_WITHDRAW,"S1",new Withdrawal(STREAM,EPOCH,1))),
            Arguments.of("activate",msg(MessageType.ACTIVATE,"C1",activation())),
            Arguments.of("activate-ack",msg(MessageType.ACTIVATE_ACK,"S1",new ActivationAck(STREAM,EPOCH,SUB,1,6000))),
            Arguments.of("deactivate",msg(MessageType.DEACTIVATE,"C1",new Deactivation(STREAM,EPOCH,SUB,1))),
            Arguments.of("subscription-renew",msg(MessageType.SUBSCRIPTION_RENEW,"C1",activation())),
            Arguments.of("route-trace",msg(MessageType.ROUTE_TRACE,"C1",new Trace(STREAM,EPOCH,TRACE,32,List.of("C1")))),
            Arguments.of("trace-result",msg(MessageType.TRACE_RESULT,"S1",new TraceResult(STREAM,TRACE,List.of("C1","S1")))),
            Arguments.of("error-no-route",msg(MessageType.ERROR,"S1",new ErrorPayload(ErrorCode.NO_ROUTE,11,"no route"))),
            Arguments.of("stream-announce-utf8",announcement("🎬")));
    }

    @ParameterizedTest(name="golden {0}") @MethodSource("goldenVectors")
    void matchesIndependentFixturesBothDirections(String name,ControlMessage expected) throws Exception {
        byte[] bytes=fixture(name);
        assertArrayEquals(bytes,MessageCodec.encode(expected));
        assertEquals(expected,MessageCodec.decode(bytes));
        assertEquals(expected,MessageCodec.decode(MessageCodec.encode(expected)));
    }

    @ParameterizedTest(name="one-byte {0}") @MethodSource("goldenVectors")
    void readsOneByteAtATimeWithoutDependingOnAvailable(String name,ControlMessage expected) throws Exception {
        InputStream input=new FilterInputStream(new ByteArrayInputStream(fixture(name))) {
            @Override public int read(byte[] b,int off,int len) throws IOException {
                return super.read(b,off,Math.min(1,len));
            }
            @Override public int available(){return 0;}
        };
        assertEquals(expected,MessageCodec.read(input).orElseThrow());
        assertTrue(MessageCodec.read(input).isEmpty());
    }

    @ParameterizedTest @ValueSource(strings={"short-prefix","length-small","length-large","truncated-body",
        "version","type","retired-type","utf8","trailing-byte","negative-probe","role","path-count"})
    void rejectsMalformedTcpFixtures(String name) throws Exception {
        byte[] bytes=fixture("invalid-"+name);
        IOException e=assertThrows(IOException.class,()->MessageCodec.decode(bytes));
        if(name.equals("short-prefix")||name.equals("truncated-body")) assertInstanceOf(EOFException.class,e);
        else {
            ErrorCode code=name.equals("version")?ErrorCode.UNSUPPORTED_VERSION:
                (name.equals("length-large")||name.equals("path-count"))?ErrorCode.RESOURCE_LIMIT:ErrorCode.INVALID_MESSAGE;
            assertEquals(code,assertInstanceOf(ProtocolException.class,e).errorCode());
        }
    }

    @Test void consecutiveMessagesRemainSeparate() throws Exception {
        var pairs=goldenVectors().toList();var golden=new ByteArrayOutputStream();var written=new ByteArrayOutputStream();
        for(var p:pairs){golden.write(fixture((String)p.get()[0]));MessageCodec.write((ControlMessage)p.get()[1],written);}
        assertArrayEquals(golden.toByteArray(),written.toByteArray());
        var input=new ByteArrayInputStream(golden.toByteArray());
        for(var p:pairs) assertEquals(p.get()[1],MessageCodec.read(input).orElseThrow());
        assertTrue(MessageCodec.read(input).isEmpty());
        assertThrows(ProtocolException.class,()->MessageCodec.decode(golden.toByteArray()));
    }

    @Test void eofAtBoundaryDiffersFromEveryTruncatedFrame() throws Exception {
        assertTrue(MessageCodec.read(new ByteArrayInputStream(new byte[0])).isEmpty());
        assertThrows(EOFException.class,()->MessageCodec.decode(new byte[0]));
        byte[] frame=fixture("hello");
        for(int n=1;n<frame.length;n++){
            byte[] partial=Arrays.copyOf(frame,n);
            assertThrows(EOFException.class,()->MessageCodec.read(new ByteArrayInputStream(partial)),"length="+n);
        }
    }

    @ParameterizedTest @ValueSource(longs={65537L,2147483648L,4294967295L})
    void unsignedOversizedLengthRejectedBeforeBodyRead(long size) throws Exception {
        byte[] prefix={(byte)(size>>>24),(byte)(size>>>16),(byte)(size>>>8),(byte)size};
        var input=new ByteArrayInputStream(prefix){
            @Override public synchronized int read(byte[] b,int off,int len){fail("Corpo excessivo não deve ser lido");return -1;}
        };
        assertEquals(ErrorCode.RESOURCE_LIMIT,assertThrows(ProtocolException.class,()->MessageCodec.read(input)).errorCode());
    }

    @Test void fullLengthBoundsAndIncompleteFields() throws Exception {
        assertThrows(EOFException.class,()->MessageCodec.read(new ByteArrayInputStream(new byte[]{0,1,0,0})));
        byte[] frame=fixture("hello-ok-min");frame[22]=(byte)255;frame[23]=(byte)255;
        assertEquals(ErrorCode.RESOURCE_LIMIT,assertThrows(ProtocolException.class,()->MessageCodec.decode(frame)).errorCode());
        frame[22]=0;frame[23]=32;
        assertEquals(ErrorCode.INVALID_MESSAGE,assertThrows(ProtocolException.class,()->MessageCodec.decode(frame)).errorCode());
    }

    @Test void unsigned32LeasesAndUuidBitsRoundTrip() throws Exception {
        for(long lease:new long[]{0,2147483648L,4294967295L}){
            var m=msg(MessageType.ACTIVATE_ACK,"S1",new ActivationAck(STREAM,EPOCH,SUB,Long.MAX_VALUE,lease));
            byte[] b=MessageCodec.encode(m);assertEquals(m,MessageCodec.decode(b));
            long wire=((b[98]&255L)<<24)|((b[99]&255L)<<16)|((b[100]&255L)<<8)|(b[101]&255L);
            assertEquals(lease,wire);
        }
        var bits=new ControlMessage(MessageType.HELLO_OK,new UUID(Long.MIN_VALUE,-1),"C",NIL,Empty.INSTANCE);
        assertEquals(bits,MessageCodec.decode(MessageCodec.encode(bits)));
        var probe=msg(MessageType.PROBE_PONG,"S1",new Probe(Long.MAX_VALUE));
        assertEquals(probe,MessageCodec.decode(MessageCodec.encode(probe)));
    }

    @ParameterizedTest @ValueSource(longs={-1,4294967296L})
    void rejectsLeaseOutsideUnsigned32Domain(long lease){
        assertThrows(ProtocolException.class,()->MessageCodec.encode(msg(MessageType.ACTIVATE_ACK,"S1",new ActivationAck(STREAM,EPOCH,SUB,1,lease))));
    }

    @Test void strictUtf8AndCodePointLimits() throws Exception {
        var m=announcement("🎬".repeat(128));assertEquals(m,MessageCodec.decode(MessageCodec.encode(m)));
        for(String title:List.of("🎬".repeat(129),"a".repeat(129),"\uD800","a\0b",""))
            assertThrows(ProtocolException.class,()->MessageCodec.encode(announcement(title)));
        var modified=new ByteArrayOutputStream();new DataOutputStream(modified).writeUTF("🎬");
        assertArrayEquals(new byte[]{0,6},Arrays.copyOf(modified.toByteArray(),2));
        assertArrayEquals(new byte[]{0,4},Arrays.copyOfRange(fixture("stream-announce-utf8"),62,64));
    }

    @Test void validatesIdsAndMaximumNeighborCount() throws Exception {
        assertEquals(41,MessageCodec.encode(msg(MessageType.HELLO_OK,"C",Empty.INSTANCE)).length);
        for(String id:List.of("","á","bad/id","a".repeat(33)))
            assertThrows(ProtocolException.class,()->MessageCodec.encode(msg(MessageType.HELLO_OK,id,Empty.INSTANCE)));
        List<Endpoint> neighbors=IntStream.range(0,64).mapToObj(i->new Endpoint("N"+i,NIL,"10.0.0.1",5000,5001,NodeRole.RELAY)).toList();
        var m=msg(MessageType.REGISTER_OK,"B",new NeighborList(neighbors));assertEquals(m,MessageCodec.decode(MessageCodec.encode(m)));
        var excess=new ArrayList<>(neighbors);excess.add(ENDPOINT);
        assertEquals(ErrorCode.RESOURCE_LIMIT,assertThrows(ProtocolException.class,()->MessageCodec.encode(msg(MessageType.REGISTER_OK,"B",new NeighborList(excess)))).errorCode());
        byte[] frame=fixture("register-ok");frame[42]=65;
        assertEquals(ErrorCode.RESOURCE_LIMIT,assertThrows(ProtocolException.class,()->MessageCodec.decode(frame)).errorCode());
    }

    @Test void validatesPathsButDefersRoutingContext() throws Exception {
        var path=IntStream.range(0,32).mapToObj(i->"N"+i).toList();
        var m=msg(MessageType.TRACE_RESULT,"S1",new TraceResult(STREAM,TRACE,path));assertEquals(m,MessageCodec.decode(MessageCodec.encode(m)));
        var excess=new ArrayList<>(path);excess.add("N32");
        assertThrows(ProtocolException.class,()->MessageCodec.encode(msg(MessageType.TRACE_RESULT,"S1",new TraceResult(STREAM,TRACE,excess))));
        var context=msg(MessageType.TRACE_RESULT,"S1",new TraceResult(STREAM,TRACE,List.of("C1","C1")));
        assertEquals(context,MessageCodec.decode(MessageCodec.encode(context)));
    }

    @Test void endpointInstanceAndRolesFollowWireContract() throws Exception {
        var unknown=new Endpoint("N",NIL,"10.0.0.2",65535,65535,NodeRole.CLIENT);
        var response=msg(MessageType.REGISTER_OK,"B",new NeighborList(List.of(unknown)));
        assertEquals(response,MessageCodec.decode(MessageCodec.encode(response)));
        assertThrows(ProtocolException.class,()->MessageCodec.encode(msg(MessageType.HELLO,"N",new EndpointPayload(unknown))));
        byte[] zero=fixture("hello");Arrays.fill(zero,46,62,(byte)0);assertThrows(ProtocolException.class,()->MessageCodec.decode(zero));
        for(NodeRole role:List.of(NodeRole.SERVER,NodeRole.RELAY,NodeRole.CLIENT)){
            int code=switch(role){case SERVER->1;case RELAY->2;case CLIENT->3;default->throw new AssertionError();};
            var m=msg(MessageType.HELLO,"S1",new EndpointPayload(new Endpoint("S1",INST,"10.0.0.1",5000,5001,role)));
            byte[] b=MessageCodec.encode(m);assertEquals(code,b[76]);assertEquals(m,MessageCodec.decode(b));
        }
        for(Endpoint invalid:List.of(new Endpoint("S1",INST,"h",0,5001,NodeRole.SERVER),
                new Endpoint("S1",INST,"h",5000,65536,NodeRole.SERVER),new Endpoint("S1",INST,"bad host",5000,5001,NodeRole.SERVER),
                new Endpoint("S1",INST,"h",5000,5001,NodeRole.BOOTSTRAP)))
            assertThrows(ProtocolException.class,()->MessageCodec.encode(msg(MessageType.HELLO,"S1",new EndpointPayload(invalid))));
    }

    @Test void invalidModelsDoNotEmitPartialFrames(){
        var output=new ByteArrayOutputStream();
        assertThrows(ProtocolException.class,()->MessageCodec.write(msg(MessageType.PROBE_PING,"S1",Empty.INSTANCE),output));
        assertEquals(0,output.size());assertThrows(ProtocolException.class,()->MessageCodec.encode(null));
        assertThrows(ProtocolException.class,()->MessageCodec.encode(new ControlMessage(2,MessageType.HELLO_OK,REQ,"C",INST,Empty.INSTANCE)));
        assertThrows(ProtocolException.class,()->MessageCodec.encode(new ControlMessage(MessageType.HELLO_OK,null,"C",INST,Empty.INSTANCE)));
    }

    @Test void callerOwnsStreamLifetimeAndFlush() throws Exception {
        class Output extends ByteArrayOutputStream {
            boolean closed,flushed;
            @Override public void close(){closed=true;}
            @Override public void flush(){flushed=true;}
        }
        var output=new Output();MessageCodec.write(msg(MessageType.HELLO_OK,"C",Empty.INSTANCE),output);
        assertFalse(output.closed);assertFalse(output.flushed);
        class Input extends ByteArrayInputStream {
            boolean closed;
            Input(byte[] b){super(b);}
            @Override public void close(){closed=true;}
        }
        var input=new Input(output.toByteArray());MessageCodec.read(input);assertFalse(input.closed);
    }

    @Test void errorCodesAndUnknownRelatedTypeRoundTrip() throws Exception {
        for(ErrorCode code:ErrorCode.values()){
            var m=msg(MessageType.ERROR,"S1",new ErrorPayload(code,255,""));assertEquals(m,MessageCodec.decode(MessageCodec.encode(m)));
        }
        byte[] b=fixture("error-no-route");b[43]=0;assertThrows(ProtocolException.class,()->MessageCodec.decode(b));
        assertThrows(ProtocolException.class,()->MessageCodec.encode(msg(MessageType.ERROR,"S1",new ErrorPayload(ErrorCode.INVALID_MESSAGE,256,"bad"))));
        assertThrows(ProtocolException.class,()->MessageCodec.encode(msg(MessageType.ERROR,"S1",new ErrorPayload(ErrorCode.INVALID_MESSAGE,1,"a".repeat(257)))));
    }

    @Test void payloadListsAreDefensivelyCopied(){
        var list=new ArrayList<>(List.of("S1"));var a=new Announcement(new StreamInfo(STREAM,"S1","A",1,10,EPOCH),1,0,list);
        list.clear();assertEquals(List.of("S1"),a.path());assertThrows(UnsupportedOperationException.class,()->a.path().clear());
        var neighbors=new ArrayList<>(List.of(CONFIGURED_ENDPOINT));var r=new NeighborList(neighbors);
        neighbors.clear();assertEquals(1,r.neighbors().size());assertThrows(UnsupportedOperationException.class,()->r.neighbors().clear());
    }

    @Test void allMessageNumbersHaveIndependentVectors(){
        assertEquals(IntStream.rangeClosed(1,17).filter(code->code!=3).boxed().toList(),
                goldenVectors().limit(16).map(a->((ControlMessage)a.get()[1]).type().code()).toList());
        assertEquals(16,MessageType.values().length);
    }

    @Test void bootstrapReplyContainsOnlyConfiguredNeighbors() throws Exception {
        byte[] bytes=fixture("register-ok");
        // Envelope termina em 41; segue imediatamente count u16=1, sem lease.
        assertEquals(78,bytes.length);assertArrayEquals(new byte[]{0,1},Arrays.copyOfRange(bytes,41,43));
        assertEquals(List.of(CONFIGURED_ENDPOINT),((NeighborList)MessageCodec.decode(bytes).payload()).neighbors());
        assertThrows(ProtocolException.class,()->MessageCodec.encode(msg(MessageType.REGISTER_OK,"B",new NeighborList(List.of(ENDPOINT)))));
        byte[] liveInstance=bytes.clone();liveInstance[47]=1;
        assertThrows(ProtocolException.class,()->MessageCodec.decode(liveInstance));
        // Layout antigo tinha lease=15000 antes da lista: não é aceite no contrato corrigido.
        var legacy=new ByteArrayOutputStream();legacy.write(bytes,4,37);
        legacy.write(new byte[]{0,0,0x3a,(byte)0x98});legacy.write(bytes,41,bytes.length-41);
        var framed=new ByteArrayOutputStream();new DataOutputStream(framed).writeInt(legacy.size());legacy.writeTo(framed);
        assertThrows(ProtocolException.class,()->MessageCodec.decode(framed.toByteArray()));
        assertEquals(ErrorCode.INVALID_MESSAGE,assertThrows(ProtocolException.class,()->MessageType.fromCode(3)).errorCode());
    }

    @Test void phaseOneConfigurationCanProduceAndRecoverControlFrames() throws Exception {
        Path directory=Path.of(MessageCodecTest.class.getResource("/config/bootstrap.properties").toURI()).getParent();
        var paths=List.of("bootstrap","server-1","server-2","relay","client").stream()
                .map(name->directory.resolve(name+".properties")).toList();
        var configuration=ConfigurationLoader.loadDeployment(paths);
        int hellos=0,announcements=0,responses=0;
        for(var node:configuration){
            UUID instance=UUID.nameUUIDFromBytes(("test-instance-"+node.nodeId()).getBytes(StandardCharsets.UTF_8));
            if(node.role()==NodeRole.BOOTSTRAP){
                var endpoints=node.topology().orElseThrow().nodes().values().stream().map(n->
                        new Endpoint(n.nodeId(),NIL,n.host(),n.controlPort(),n.dataPort(),n.role())).toList();
                var message=new ControlMessage(MessageType.REGISTER_OK,REQ,node.nodeId(),instance,new NeighborList(endpoints));
                assertEquals(message,MessageCodec.read(new ByteArrayInputStream(MessageCodec.encode(message))).orElseThrow());
                responses++;
            }else{
                var endpoint=new Endpoint(node.nodeId(),instance,node.advertisedHost(),node.controlPort(),node.dataPort(),node.role());
                var message=new ControlMessage(MessageType.HELLO,REQ,node.nodeId(),instance,new EndpointPayload(endpoint));
                assertEquals(message,MessageCodec.decode(MessageCodec.encode(message)));hellos++;
            }
            for(var stream:node.streams()){
                var descriptor=new StreamInfo(stream.streamId(),stream.serverId(),stream.title(),1,stream.fps(),instance);
                var message=new ControlMessage(MessageType.STREAM_ANNOUNCE,REQ,node.nodeId(),instance,
                        new Announcement(descriptor,1,0,List.of(node.nodeId())));
                var decoded=MessageCodec.decode(MessageCodec.encode(message));
                assertEquals(message,decoded);
                assertEquals(node.nodeId(),((Announcement)decoded.payload()).stream().serverId());
                announcements++;
            }
        }
        assertEquals(4,hellos);assertEquals(4,announcements);assertEquals(1,responses);
    }

    private static ControlMessage msg(MessageType type,String sender,Payload payload){return new ControlMessage(type,REQ,sender,INST,payload);}
    private static ControlMessage announcement(String title){return msg(MessageType.STREAM_ANNOUNCE,"S1",new Announcement(new StreamInfo(STREAM,"S1",title,1,10,EPOCH),1,0,List.of("S1")));}
    private static Activation activation(){return new Activation(STREAM,EPOCH,SUB,1,6000,List.of("S1","C1"));}
    private static byte[] fixture(String name) throws IOException {
        try(InputStream input=MessageCodecTest.class.getResourceAsStream("/protocol/tcp-"+name+".bin")){
            assertNotNull(input,"Fixture em falta: "+name);return input.readAllBytes();
        }
    }
}
