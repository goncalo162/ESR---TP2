import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.io.*;
import pt.uminho.esr.ott.overlay.*;
import pt.uminho.esr.ott.protocol.*;
import pt.uminho.esr.ott.transport.*;
Object field(Object x,String name) throws Exception { var f=x.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(x); }
void setField(Object x,String name,Object value) throws Exception { var f=x.getClass().getDeclaredField(name);f.setAccessible(true);f.set(x,value); }
Object call(Object x,String name,Class<?>[] types,Object... args) throws Exception { var m=x.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(x,args); }
void pump(Object f) throws Exception { call(f,"pump",new Class<?>[0]); }
void probe(String order) throws Exception {
    var k=Class.forName("pt.uminho.esr.ott.overlay.NeighborManagerTest$Fixture");
    var ctor=k.getDeclaredConstructor(String.class);ctor.setAccessible(true);var f=ctor.newInstance("A");
    try {
        call(f,"ready",new Class<?>[]{String.class},"B");
        var raw=call(f,"outbound",new Class<?>[]{boolean.class},false);
        var channelType=Class.forName("pt.uminho.esr.ott.overlay.NeighborManager$Channel");
        var connectionId=(UUID)call(raw,"id",new Class<?>[0]);
        var writes=(List<ControlMessage>)field(raw,"writes");var hello=writes.getLast();
        var negative=new ControlMessage(MessageType.HELLO_OK,hello.requestId(),"B",UUID.randomUUID(),new ControlMessage.HelloOk(3));
        var bytes=MessageCodec.encode(negative);java.nio.ByteBuffer.wrap(bytes).putInt(bytes.length-4,0);
        pt.uminho.esr.ott.protocol.ProtocolException error=null;
        try {MessageCodec.readFrame(new ByteArrayInputStream(bytes));throw new AssertionError("negative decoded as valid");}
        catch(pt.uminho.esr.ott.protocol.ProtocolException expected){error=expected;}
        var remote=((MessageCodec.HandshakeFailure)error.getCause()).message();
        var io=field(f,"io");var sink=field(io,"sink");
        var clock=(AtomicLong)field(f,"clock");var manager=(NeighborManager)field(f,"manager");
        var completions=(List<CompletableFuture<Void>>)field(raw,"confirmations");
        var events=(List<NeighborManager.Event>)field(f,"events");events.clear();
        System.out.println("CASE="+order+" connection="+connectionId+" HELLO.request="+hello.requestId()+" HELLO.senderInstance="+hello.senderInstance()+" HELLO_OK.request="+remote.requestId()+" HELLO_OK.senderInstance="+remote.senderInstance()+" selected="+((ControlMessage.HelloOk)remote.payload()).selectedMask()+" preSession="+manager.snapshot());
        if(order.equals("WRITER_QUEUED_THEN_READER_CLOSE")) {
            completions.getLast().complete(null);
            call(sink,"protocolError",new Class<?>[]{channelType,pt.uminho.esr.ott.protocol.ProtocolException.class,long.class},raw,error,clock.get());
            setField(raw,"open",false);
            call(sink,"closed",new Class<?>[]{channelType,Connection.CloseReason.class,long.class},raw,Connection.CloseReason.PROTOCOL_ERROR,clock.get());
        } else if(order.equals("READER_QUEUED_BEFORE_WRITER")) {
            call(sink,"protocolError",new Class<?>[]{channelType,pt.uminho.esr.ott.protocol.ProtocolException.class,long.class},raw,error,clock.get());
            setField(raw,"open",false);
            completions.getLast().complete(null);
            call(sink,"closed",new Class<?>[]{channelType,Connection.CloseReason.class,long.class},raw,Connection.CloseReason.PROTOCOL_ERROR,clock.get());
        } else {
            completions.getLast().complete(null);pump(f);
            call(sink,"protocolError",new Class<?>[]{channelType,pt.uminho.esr.ott.protocol.ProtocolException.class,long.class},raw,error,clock.get());
            setField(raw,"open",false);
            call(sink,"closed",new Class<?>[]{channelType,Connection.CloseReason.class,long.class},raw,Connection.CloseReason.PROTOCOL_ERROR,clock.get());
        }
        pump(f);
        var diagnostics=events.stream().filter(e->e instanceof NeighborManager.Diagnostic).map(e->(NeighborManager.Diagnostic)e).toList();
        for(var d:diagnostics)System.out.println("RESULT="+order+" reason="+d.reason()+" connection="+d.connectionId()+" Session="+d.session()+" remote="+d.remoteMessage());
        System.out.println("STATUS="+order+" "+manager.discoveryStatus()+" active="+manager.snapshot());
        var expected=order.equals("WRITER_QUEUED_THEN_READER_CLOSE")?NeighborManager.Reason.TRANSPORT:NeighborManager.Reason.REQUIRED_DISCOVERY_MISSING;
        if(diagnostics.size()!=1||diagnostics.getFirst().reason()!=expected)throw new AssertionError("unexpected result "+diagnostics);
        if(!manager.snapshot().isEmpty())throw new AssertionError("negative activated");
    } finally {((AutoCloseable)f).close();}
}
try {probe("WRITER_QUEUED_THEN_READER_CLOSE");probe("READER_QUEUED_BEFORE_WRITER");probe("OWNER_PROCESSED_WRITE_BEFORE_READER");System.out.println("PROBE_OK 3 controlled orders; unchanged compiled fixture and production; no clock advance");}catch(Throwable e){e.printStackTrace();System.out.println("PROBE_FAILED");}
/exit
