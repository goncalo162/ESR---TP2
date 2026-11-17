package pt.uminho.esr.ott.streaming;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import pt.uminho.esr.ott.protocol.ControlMessage;

/** Immutable source metadata; titles are content, never stream identities. */
public record StreamInfo(UUID streamId, String serverId, String title, int codec, int fps, UUID serverEpoch)
{

    //* === Construtor === *

    public StreamInfo
    {
        Objects.requireNonNull(streamId);
        Objects.requireNonNull(serverId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(serverEpoch);

        if(!serverId.matches("[A-Za-z0-9_-]{1,32}") || codec != 1 || fps < 1 || fps > 60
                || title.isEmpty() || title.codePointCount(0, title.length()) > 128 || title.indexOf(0) >= 0)
            throw new IllegalArgumentException("Invalid stream metadata");

        try
        {
            if(StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(title)).remaining() > 512)
                throw new IllegalArgumentException("Title exceeds UTF-8 limit");
        }
        catch(CharacterCodingException e)
        {
            throw new IllegalArgumentException("Invalid Unicode title", e);
        }
    }



    //* === Métodos Públicos === *

    public static StreamInfo fromWire(ControlMessage.StreamInfo info)
    {
        Objects.requireNonNull(info);
        return new StreamInfo(info.streamId(), info.serverId(), info.title(), info.codec(), info.fps(), info.serverEpoch());
    }

    public ControlMessage.StreamInfo toWire()
    {
        return new ControlMessage.StreamInfo(streamId, serverId, title, codec, fps, serverEpoch);
    }

    public boolean sameMetadata(StreamInfo other)
    {
        return title.equals(other.title) && codec == other.codec && fps == other.fps;
    }
}
