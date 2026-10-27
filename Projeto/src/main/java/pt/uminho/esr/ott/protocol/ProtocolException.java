package pt.uminho.esr.ott.protocol;

import java.io.IOException;
import java.util.Objects;

/** Erro de formato; não envia mensagens nem decide a política de ligação. */
public final class ProtocolException extends IOException {
    private final ErrorCode errorCode;

    public ProtocolException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode);
    }

    public ProtocolException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode);
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
