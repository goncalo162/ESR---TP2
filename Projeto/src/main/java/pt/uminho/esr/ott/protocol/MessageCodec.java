package pt.uminho.esr.ott.protocol;

import pt.uminho.esr.ott.overlay.NodeRole;
import pt.uminho.esr.ott.protocol.ControlMessage.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Codec de controlo v1; não abre/fecha sockets nem executa operações de overlay. */
public final class MessageCodec {
    public static final int VERSION = 1;
    public static final int MIN_BODY_BYTES = 37;
    public static final int MAX_BODY_BYTES = 65536;
    public static final int MAX_NEIGHBORS = 64;
    public static final int MAX_PATH_NODES = 32;
    private static final UUID UNKNOWN_INSTANCE = new UUID(0, 0);

    private MessageCodec() { }

    /** Valida antes de escrever no destino; o proprietário faz flush/close e sincronização. */
    public static void write(ControlMessage message, OutputStream destination) throws IOException {
        Objects.requireNonNull(destination, "destination").write(encode(message));
    }

    public static byte[] encode(ControlMessage message) throws IOException {
        validate(message);
        var buffer = new BoundedBuffer();
        var out = new DataOutputStream(buffer);
        out.writeByte(message.version());
        out.writeByte(message.type().code());
        writeUuid(out, message.requestId());
        writeString(out, message.senderId());
        writeUuid(out, message.senderInstance());
        writePayload(out, message);
        byte[] body = buffer.bytes();
        var frame = new ByteArrayOutputStream(body.length + 4);
        var framed = new DataOutputStream(frame);
        framed.writeInt(body.length);
        framed.write(body);
        return frame.toByteArray();
    }

    /** Um array representa exatamente um frame, sem bytes posteriores. */
    public static ControlMessage decode(byte[] frame) throws IOException {
        require(frame != null, "Frame nulo");
        limit(frame.length, MAX_BODY_BYTES + 4, "frame");
        var input = new ByteArrayInputStream(frame);
        ControlMessage message = read(input).orElseThrow(() -> new EOFException("Frame ausente"));
        require(input.read() == -1, "Bytes após o frame único");
        return message;
    }

    /** Optional.empty apenas para EOF limpo, antes do primeiro byte do prefixo. */
    public static Optional<ControlMessage> read(InputStream source) throws IOException {
        Objects.requireNonNull(source, "source");
        int first = source.read();
        if (first == -1) return Optional.empty();
        var input = new DataInputStream(source);
        long length = ((long) first << 24) | ((long) input.readUnsignedByte() << 16)
                | ((long) input.readUnsignedByte() << 8) | input.readUnsignedByte();
        require(length >= MIN_BODY_BYTES, "Corpo inferior a 37 bytes");
        if (length > MAX_BODY_BYTES) throw resource("Corpo superior a 65536 bytes");
        byte[] body = new byte[(int) length];
        input.readFully(body);
        var fields = new DataInputStream(new ByteArrayInputStream(body));
        try {
            int version = fields.readUnsignedByte();
            checkVersion(version);
            MessageType type = MessageType.fromCode(fields.readUnsignedByte());
            UUID requestId = readUuid(fields);
            String senderId = readString(fields, 32);
            UUID senderInstance = readUuid(fields);
            var message = new ControlMessage(version, type, requestId, senderId,
                    senderInstance, readPayload(fields, type));
            require(fields.read() == -1, "Bytes inesperados no fim do payload");
            validate(message);
            return Optional.of(message);
        } catch (EOFException ex) {
            throw new ProtocolException(ErrorCode.INVALID_MESSAGE, "Campos incompletos no corpo recebido", ex);
        }
    }

    private static Payload readPayload(DataInputStream in, MessageType type) throws IOException {
        return switch (type) {
            case REGISTER, HELLO -> new EndpointPayload(readEndpoint(in));
            case REGISTER_OK -> {
                long lease = readU32(in);
                int count = readCount(in, MAX_NEIGHBORS);
                var neighbors = new ArrayList<Endpoint>(count);
                for (int i = 0; i < count; i++) neighbors.add(readEndpoint(in));
                yield new RegistrationResult(lease, neighbors);
            }
            case REGISTER_RENEW, HELLO_OK -> Empty.INSTANCE;
            case NODE_LEAVE -> new Leave(in.readUnsignedByte());
            case PROBE_PING, PROBE_PONG -> new Probe(in.readLong());
            case STREAM_ANNOUNCE -> new Announcement(readStream(in), in.readLong(), in.readLong(), readNodes(in));
            case STREAM_WITHDRAW -> new Withdrawal(readUuid(in), readUuid(in), in.readLong());
            case ACTIVATE, SUBSCRIPTION_RENEW -> new Activation(readUuid(in), readUuid(in), readUuid(in),
                    in.readLong(), readU32(in), readNodes(in));
            case ACTIVATE_ACK -> new ActivationAck(readUuid(in), readUuid(in), readUuid(in), in.readLong(), readU32(in));
            case DEACTIVATE -> new Deactivation(readUuid(in), readUuid(in), readUuid(in), in.readLong());
            case ROUTE_TRACE -> new Trace(readUuid(in), readUuid(in), readUuid(in), in.readUnsignedByte(), readNodes(in));
            case TRACE_RESULT -> new TraceResult(readUuid(in), readUuid(in), readNodes(in));
            case ERROR -> new ErrorPayload(ErrorCode.fromCode(in.readUnsignedShort()), in.readUnsignedByte(), readString(in, 256));
        };
    }

    private static void writePayload(DataOutputStream out, ControlMessage m) throws IOException {
        switch (m.type()) {
            case REGISTER, HELLO -> writeEndpoint(out, payload(m, EndpointPayload.class).endpoint());
            case REGISTER_OK -> {
                var p = payload(m, RegistrationResult.class);
                out.writeInt((int) p.leaseMs());
                out.writeShort(p.neighbors().size());
                for (var endpoint : p.neighbors()) writeEndpoint(out, endpoint);
            }
            case REGISTER_RENEW, HELLO_OK -> { }
            case NODE_LEAVE -> out.writeByte(payload(m, Leave.class).reason());
            case PROBE_PING, PROBE_PONG -> out.writeLong(payload(m, Probe.class).probeId());
            case STREAM_ANNOUNCE -> {
                var p = payload(m, Announcement.class);
                writeStream(out, p.stream());
                out.writeLong(p.announceSeq());
                out.writeLong(p.costMicros());
                writeNodes(out, p.path());
            }
            case STREAM_WITHDRAW -> {
                var p = payload(m, Withdrawal.class);
                writeUuid(out, p.streamId());writeUuid(out, p.serverEpoch());out.writeLong(p.announceSeq());
            }
            case ACTIVATE, SUBSCRIPTION_RENEW -> {
                var p = payload(m, Activation.class);
                writeUuid(out, p.streamId());writeUuid(out, p.serverEpoch());writeUuid(out, p.subscriptionId());
                out.writeLong(p.revision());out.writeInt((int) p.leaseMs());writeNodes(out, p.expectedPath());
            }
            case ACTIVATE_ACK -> {
                var p = payload(m, ActivationAck.class);
                writeUuid(out, p.streamId());writeUuid(out, p.serverEpoch());writeUuid(out, p.subscriptionId());
                out.writeLong(p.revision());out.writeInt((int) p.grantedLeaseMs());
            }
            case DEACTIVATE -> {
                var p = payload(m, Deactivation.class);
                writeUuid(out, p.streamId());writeUuid(out, p.serverEpoch());writeUuid(out, p.subscriptionId());out.writeLong(p.revision());
            }
            case ROUTE_TRACE -> {
                var p = payload(m, Trace.class);
                writeUuid(out, p.streamId());writeUuid(out, p.serverEpoch());writeUuid(out, p.traceId());
                out.writeByte(p.ttl());writeNodes(out, p.visited());
            }
            case TRACE_RESULT -> {
                var p = payload(m, TraceResult.class);
                writeUuid(out, p.streamId());writeUuid(out, p.traceId());writeNodes(out, p.visited());
            }
            case ERROR -> {
                var p = payload(m, ErrorPayload.class);
                out.writeShort(p.code().code());out.writeByte(p.relatedType());writeString(out, p.detail());
            }
        }
    }

    private static void validate(ControlMessage m) throws ProtocolException {
        require(m != null, "Mensagem nula");
        checkVersion(m.version());
        require(m.type() != null, "Tipo ausente");
        uuid(m.requestId());nodeId(m.senderId());uuid(m.senderInstance());
        switch (m.type()) {
            case REGISTER, HELLO -> endpoint(payload(m, EndpointPayload.class).endpoint(), false);
            case REGISTER_OK -> {
                var p = payload(m, RegistrationResult.class);
                u32(p.leaseMs());limit(p.neighbors().size(), MAX_NEIGHBORS, "vizinhos");
                for (var e : p.neighbors()) endpoint(e, true);
            }
            case REGISTER_RENEW, HELLO_OK -> payload(m, Empty.class);
            case NODE_LEAVE -> range(payload(m, Leave.class).reason(), 0, 1, "reason");
            case PROBE_PING, PROBE_PONG -> nonnegative(payload(m, Probe.class).probeId(), "probeId");
            case STREAM_ANNOUNCE -> {
                var p = payload(m, Announcement.class);var s = p.stream();
                require(s != null, "StreamInfo ausente");
                uuid(s.streamId());nodeId(s.serverId());text(s.title(), 1, 512);
                limit(s.title().codePointCount(0, s.title().length()), 128, "pontos Unicode do título");
                range(s.codec(), 1, 1, "codec MJPEG");range(s.fps(), 1, 60, "fps");uuid(s.serverEpoch());
                nonnegative(p.announceSeq(), "announceSeq");nonnegative(p.costMicros(), "costMicros");nodes(p.path());
            }
            case STREAM_WITHDRAW -> {
                var p = payload(m, Withdrawal.class);uuid(p.streamId());uuid(p.serverEpoch());nonnegative(p.announceSeq(), "announceSeq");
            }
            case ACTIVATE, SUBSCRIPTION_RENEW -> {
                var p = payload(m, Activation.class);uuid(p.streamId());uuid(p.serverEpoch());uuid(p.subscriptionId());
                nonnegative(p.revision(), "revision");u32(p.leaseMs());nodes(p.expectedPath());
            }
            case ACTIVATE_ACK -> {
                var p = payload(m, ActivationAck.class);uuid(p.streamId());uuid(p.serverEpoch());uuid(p.subscriptionId());
                nonnegative(p.revision(), "revision");u32(p.grantedLeaseMs());
            }
            case DEACTIVATE -> {
                var p = payload(m, Deactivation.class);uuid(p.streamId());uuid(p.serverEpoch());uuid(p.subscriptionId());nonnegative(p.revision(), "revision");
            }
            case ROUTE_TRACE -> {
                var p = payload(m, Trace.class);uuid(p.streamId());uuid(p.serverEpoch());uuid(p.traceId());range(p.ttl(), 0, 255, "ttl");nodes(p.visited());
            }
            case TRACE_RESULT -> {
                var p = payload(m, TraceResult.class);uuid(p.streamId());uuid(p.traceId());nodes(p.visited());
            }
            case ERROR -> {
                var p = payload(m, ErrorPayload.class);require(p.code() != null, "Código de erro ausente");
                range(p.relatedType(), 0, 255, "relatedType");text(p.detail(), 0, 256);
            }
        }
    }

    private static <T extends Payload> T payload(ControlMessage m, Class<T> expected) throws ProtocolException {
        require(expected.isInstance(m.payload()), "Payload incompatível com " + m.type());
        return expected.cast(m.payload());
    }

    private static Endpoint readEndpoint(DataInputStream in) throws IOException {
        return new Endpoint(readString(in, 32), readUuid(in), readString(in, 253),
                in.readUnsignedShort(), in.readUnsignedShort(), readRole(in.readUnsignedByte()));
    }

    private static void writeEndpoint(DataOutputStream out, Endpoint e) throws IOException {
        writeString(out, e.nodeId());writeUuid(out, e.instanceId());writeString(out, e.host());
        out.writeShort(e.controlPort());out.writeShort(e.dataPort());
        out.writeByte(switch (e.role()) {
            case SERVER -> 1;case RELAY -> 2;case CLIENT -> 3;
            case BOOTSTRAP -> throw invalid("BOOTSTRAP não é papel de Endpoint overlay");
        });
    }

    private static NodeRole readRole(int code) throws ProtocolException {
        return switch (code) {
            case 1 -> NodeRole.SERVER;case 2 -> NodeRole.RELAY;case 3 -> NodeRole.CLIENT;
            default -> throw invalid("Papel de Endpoint desconhecido");
        };
    }

    private static void endpoint(Endpoint e, boolean allowUnknown) throws ProtocolException {
        require(e != null, "Endpoint ausente");
        nodeId(e.nodeId());uuid(e.instanceId());text(e.host(), 1, 253);
        require(e.host().chars().allMatch(c -> c > 32 && c < 127), "Host deve ser ASCII sem espaços");
        require(allowUnknown || !UNKNOWN_INSTANCE.equals(e.instanceId()), "instanceId desconhecido fora de REGISTER_OK");
        range(e.controlPort(), 1, 65535, "controlPort");range(e.dataPort(), 1, 65535, "dataPort");
        require(e.role() != null && e.role() != NodeRole.BOOTSTRAP, "Papel de Endpoint inválido");
    }

    private static StreamInfo readStream(DataInputStream in) throws IOException {
        return new StreamInfo(readUuid(in), readString(in, 32), readString(in, 512),
                in.readUnsignedByte(), in.readUnsignedShort(), readUuid(in));
    }

    private static void writeStream(DataOutputStream out, StreamInfo s) throws IOException {
        writeUuid(out, s.streamId());writeString(out, s.serverId());writeString(out, s.title());
        out.writeByte(s.codec());out.writeShort(s.fps());writeUuid(out, s.serverEpoch());
    }

    private static UUID readUuid(DataInputStream in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }

    private static void writeUuid(DataOutputStream out, UUID uuid) throws IOException {
        out.writeLong(uuid.getMostSignificantBits());out.writeLong(uuid.getLeastSignificantBits());
    }

    private static long readU32(DataInputStream in) throws IOException {
        return Integer.toUnsignedLong(in.readInt());
    }

    private static int readCount(DataInputStream in, int max) throws IOException {
        int count = in.readUnsignedShort();limit(count, max, "elementos da lista");return count;
    }

    private static List<String> readNodes(DataInputStream in) throws IOException {
        int count = readCount(in, MAX_PATH_NODES);
        var result = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) result.add(readString(in, 32));
        return result;
    }

    private static void writeNodes(DataOutputStream out, List<String> nodes) throws IOException {
        out.writeShort(nodes.size());for (String node : nodes) writeString(out, node);
    }

    private static void nodes(List<String> nodes) throws ProtocolException {
        limit(nodes.size(), MAX_PATH_NODES, "IDs do caminho");for (String id : nodes) nodeId(id);
    }

    private static String readString(DataInputStream in, int maxBytes) throws IOException {
        int length = in.readUnsignedShort();limit(length, maxBytes, "bytes da string");
        byte[] bytes = new byte[length];in.readFully(bytes);
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException ex) {
            throw new ProtocolException(ErrorCode.INVALID_MESSAGE, "UTF-8 inválido", ex);
        }
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);out.writeShort(bytes.length);out.write(bytes);
    }

    private static void text(String value, int minBytes, int maxBytes) throws ProtocolException {
        require(value != null, "String ausente");limit(value.length(), maxBytes, "string");
        require(value.indexOf('\0') < 0 && StandardCharsets.UTF_8.newEncoder().canEncode(value), "String não é UTF-8 válido sem NUL");
        int bytes = value.getBytes(StandardCharsets.UTF_8).length;
        require(bytes >= minBytes, "String vazia");limit(bytes, maxBytes, "bytes UTF-8");
    }

    private static void nodeId(String value) throws ProtocolException {
        text(value, 1, 32);require(value.matches("[A-Za-z0-9_-]{1,32}"), "nodeId inválido");
    }

    private static void uuid(UUID uuid) throws ProtocolException {
        require(uuid != null, "UUID ausente");
    }

    private static void nonnegative(long value, String field) throws ProtocolException {
        range(value, 0, Long.MAX_VALUE, field);
    }

    private static void u32(long value) throws ProtocolException {
        range(value, 0, 0xffffffffL, "u32");
    }

    private static void range(long value, long min, long max, String field) throws ProtocolException {
        require(value >= min && value <= max, "Valor fora do domínio: " + field);
    }

    private static void limit(int value, int max, String field) throws ProtocolException {
        if (value > max) throw resource("Limite excedido: " + field);
    }

    private static void checkVersion(int version) throws ProtocolException {
        if (version != VERSION) throw new ProtocolException(ErrorCode.UNSUPPORTED_VERSION, "Versão de protocolo não suportada");
    }

    private static void require(boolean condition, String message) throws ProtocolException {
        if (!condition) throw invalid(message);
    }

    private static ProtocolException invalid(String message) {
        return new ProtocolException(ErrorCode.INVALID_MESSAGE, message);
    }

    private static ProtocolException resource(String message) {
        return new ProtocolException(ErrorCode.RESOURCE_LIMIT, message);
    }

    private static final class BoundedBuffer extends OutputStream {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        @Override public void write(int value) throws IOException {
            check(1);buffer.write(value);
        }
        @Override public void write(byte[] bytes, int offset, int length) throws IOException {
            Objects.checkFromIndexSize(offset, length, bytes.length);check(length);buffer.write(bytes, offset, length);
        }
        private void check(int count) throws ProtocolException {
            if (count > MAX_BODY_BYTES - buffer.size()) throw resource("Corpo codificado superior ao máximo");
        }
        byte[] bytes() { return buffer.toByteArray(); }
    }
}
