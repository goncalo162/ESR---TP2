package pt.uminho.esr.ott.protocol;

import pt.uminho.esr.ott.overlay.NodeRole;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/** Payload validation/lexical helpers only. MessageCodec is the sole wire codec. */
public final class DiscoveryCodec {
    public static final int MAX_BODY_BYTES=32768;
    private static final UUID ZERO=new UUID(0,0);
    private DiscoveryCodec() { }
    public static void validateSnapshot(String sender, List<ControlMessage.Endpoint> entries) throws IOException {
        require(entries!=null && entries.size()<=63,"Discovery adjacency cap"); var ids=new HashSet<String>();
        for(var endpoint:entries) {
            validateEndpoint(endpoint);
            require(!endpoint.nodeId().equals(sender) && ids.add(endpoint.nodeId()),"Discovery repeated/sender adjacency");
        }
    }
    public static void validateEndpoint(ControlMessage.Endpoint endpoint) throws IOException {
        require(endpoint!=null,"Missing endpoint"); id(endpoint.nodeId());
        require(endpoint.instanceId()!=null && !ZERO.equals(endpoint.instanceId()),"Observed instance required");
        try { normalizeHost(endpoint.host()); } catch(IllegalArgumentException error) { throw invalid("Invalid host",error); }
        require(endpoint.controlPort()>0 && endpoint.controlPort()<=65535 && endpoint.dataPort()>0 && endpoint.dataPort()<=65535,"Discovery port");
        require(endpoint.role()!=null && endpoint.role()!=NodeRole.BOOTSTRAP,"Discovery overlay role");
    }
    /** Same lexical normalization as the accepted neighbor contract; never resolves DNS. */
    public static String normalizeHost(String host) {
        try {
            if(host==null||host.isEmpty()||host.length()>253||!host.chars().allMatch(c->c>32&&c<127)) throw new IllegalArgumentException("Invalid host");
            new URI("ott",null,host,-1,null,null,null).parseServerAuthority();
            String value=host.toLowerCase(Locale.ROOT);
            if(value.startsWith("[")&&value.endsWith("]")) value=value.substring(1,value.length()-1);
            if(value.endsWith(".")) value=value.substring(0,value.length()-1);
            if(value.matches("[0-9.]+")&&value.contains(".")) {
                var parts=value.split("\\.",-1); if(parts.length!=4) throw new IllegalArgumentException("Invalid IPv4");
                for(int n=0;n<4;n++) { int digit=Integer.parseInt(parts[n]); if(digit>255) throw new IllegalArgumentException("Invalid IPv4"); parts[n]=Integer.toString(digit); }
                value=String.join(".",parts);
            }
            String address=value.split("%",2)[0];
            if(address.equals("0.0.0.0")||address.contains(":")&&address.replace(":","").matches("0*")) throw new IllegalArgumentException("Wildcard host");
            return value;
        } catch(URISyntaxException error) { throw new IllegalArgumentException("Invalid host",error); }
    }

    private static void id(String id) throws IOException { require(id!=null && id.matches("[A-Za-z0-9_-]{1,32}"),"Invalid nodeId"); }
    private static void require(boolean value,String message) throws IOException { if(!value) throw invalid(message); }
    private static ProtocolException invalid(String message) { return new ProtocolException(ErrorCode.INVALID_MESSAGE,message); }
    private static ProtocolException invalid(String message,Throwable cause) { return new ProtocolException(ErrorCode.INVALID_MESSAGE,message,cause); }
}
