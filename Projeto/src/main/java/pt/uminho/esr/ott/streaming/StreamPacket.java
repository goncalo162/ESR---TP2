package pt.uminho.esr.ott.streaming;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import pt.uminho.esr.ott.protocol.ErrorCode;
import pt.uminho.esr.ott.protocol.ProtocolException;

/** Immutable fragment of an opaque frame, encoded as one UDP OTT1 v1 datagram. */
public record StreamPacket(UUID streamId, UUID sessionId, long frameSeq,
        long mediaTimeUs, long frameLength, int fragmentIndex,
        int fragmentCount, byte[] payload) {

    public static final int MAGIC = 0x4f545431;
    public static final int VERSION = 1;
    public static final int HEADER_BYTES = 64;
    public static final int MAX_DATAGRAM_BYTES = 1200;
    public static final int MAX_PAYLOAD_BYTES = 1136;
    public static final int MAX_FRAME_BYTES = 2_097_152;
    public static final int MAX_FRAGMENTS = 1847;

    public StreamPacket {
        validateIdentity(streamId, sessionId, frameSeq, mediaTimeUs);
        if (payload == null) throw new IllegalArgumentException("Missing payload");
        validateFragment(frameLength, fragmentIndex, fragmentCount, payload.length);
        payload = payload.clone();
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }

    private static void validateIdentity(UUID streamId, UUID sessionId, long seq, long time) {
        if (streamId == null || sessionId == null || seq < 0 || time < 0)
            throw new IllegalArgumentException("Invalid identity or counter");
    }

    private static void validateFragment(long length, int index, int count, int payloadSize) {
        // Validate bounds before arithmetic, narrowing or any payload-sized allocation.
        if (length < 1 || length > MAX_FRAME_BYTES)
            throw new IllegalArgumentException("Frame length outside UDP cap");
        int expectedCount = 1 + (int) ((length - 1) / MAX_PAYLOAD_BYTES);
        if (count < 1 || count > MAX_FRAGMENTS || count != expectedCount || index < 0 || index >= count)
            throw new IllegalArgumentException("Invalid fragment index/count");
        long offset = (long) index * MAX_PAYLOAD_BYTES;
        int expectedPayload = (int) Math.min(MAX_PAYLOAD_BYTES, length - offset);
        if (payloadSize != expectedPayload)
            throw new IllegalArgumentException("Payload length does not match fragment");
    }

    /** Produces exactly the application datagram, without transport framing. */
    public static byte[] encode(StreamPacket packet) throws ProtocolException {
        if (packet == null) throw invalid("Missing packet");
        // The validated immutable value owns its payload; avoid an accessor copy.
        ByteBuffer output = ByteBuffer.allocate(HEADER_BYTES + packet.payload.length).order(ByteOrder.BIG_ENDIAN);
        output.putInt(MAGIC).put((byte) VERSION).put((byte) 0).putShort((short) HEADER_BYTES);
        output.putLong(packet.streamId.getMostSignificantBits()).putLong(packet.streamId.getLeastSignificantBits());
        output.putLong(packet.sessionId.getMostSignificantBits()).putLong(packet.sessionId.getLeastSignificantBits());
        output.putLong(packet.frameSeq).putLong(packet.mediaTimeUs).putInt((int) packet.frameLength);
        output.putShort((short) packet.fragmentIndex).putShort((short) packet.fragmentCount).put(packet.payload);
        return output.array();
    }

    public static StreamPacket decode(byte[] datagram) throws ProtocolException {
        if (datagram == null) throw invalid("Missing datagram");
        return decode(datagram, 0, datagram.length);
    }

    /** Parses only the declared region; surrounding receive-buffer bytes are ignored. */
    public static StreamPacket decode(byte[] buffer, int offset, int length) throws ProtocolException {
        if (buffer == null || offset < 0 || length < HEADER_BYTES + 1 || length > MAX_DATAGRAM_BYTES
                || offset > buffer.length || length > buffer.length - offset)
            throw invalid("Invalid datagram region/length");
        ByteBuffer input = ByteBuffer.wrap(buffer, offset, length).slice().order(ByteOrder.BIG_ENDIAN);
        if (input.getInt() != MAGIC) throw invalid("Invalid OTT1 magic");
        if (Byte.toUnsignedInt(input.get()) != VERSION)
            throw new ProtocolException(ErrorCode.UNSUPPORTED_VERSION, "Unsupported UDP version");
        if (input.get() != 0 || Short.toUnsignedInt(input.getShort()) != HEADER_BYTES)
            throw invalid("Invalid flags/header length");
        UUID stream = new UUID(input.getLong(), input.getLong());
        UUID session = new UUID(input.getLong(), input.getLong());
        long seq = input.getLong();
        long time = input.getLong();
        long frameLength = Integer.toUnsignedLong(input.getInt());
        int index = Short.toUnsignedInt(input.getShort());
        int count = Short.toUnsignedInt(input.getShort());
        try {
            validateIdentity(stream, session, seq, time);
            validateFragment(frameLength, index, count, length - HEADER_BYTES);
        } catch (IllegalArgumentException ex) {
            throw new ProtocolException(ErrorCode.INVALID_MESSAGE, ex.getMessage(), ex);
        }
        // No untrusted frame-sized allocation: only the fully validated fragment is copied.
        byte[] payload = new byte[length - HEADER_BYTES];
        input.get(payload);
        return new StreamPacket(stream, session, seq, time, frameLength, index, count, payload);
    }

    /** Splits a bounded frame without retaining its mutable input array. */
    public static List<StreamPacket> fragment(UUID streamId, UUID sessionId,
            long frameSeq, long mediaTimeUs, byte[] frame) {
        validateIdentity(streamId, sessionId, frameSeq, mediaTimeUs);
        if (frame == null || frame.length < 1 || frame.length > MAX_FRAME_BYTES)
            throw new IllegalArgumentException("Frame outside UDP cap");
        int count = 1 + (frame.length - 1) / MAX_PAYLOAD_BYTES;
        List<StreamPacket> packets = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            int offset = index * MAX_PAYLOAD_BYTES;
            int end = Math.min(frame.length, offset + MAX_PAYLOAD_BYTES);
            packets.add(new StreamPacket(streamId, sessionId, frameSeq, mediaTimeUs,
                    frame.length, index, count, Arrays.copyOfRange(frame, offset, end)));
        }
        return List.copyOf(packets);
    }

    private static ProtocolException invalid(String message) {
        return new ProtocolException(ErrorCode.INVALID_MESSAGE, message);
    }
}
