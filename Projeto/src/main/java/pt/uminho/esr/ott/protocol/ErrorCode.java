package pt.uminho.esr.ott.protocol;

public enum ErrorCode {
    INVALID_MESSAGE(1), UNSUPPORTED_VERSION(2), UNAUTHORIZED_NEIGHBOR(3), DUPLICATE_ID(4),
    UNKNOWN_STREAM(5), NO_ROUTE(6), PATH_CHANGED(7), LOOP_DETECTED(8), STALE_REVISION(9),
    TIMEOUT(10), RESOURCE_LIMIT(11), SOURCE_UNAVAILABLE(12);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ErrorCode fromCode(int code) throws ProtocolException {
        for (ErrorCode error : values()) if (error.code == code) return error;
        throw new ProtocolException(INVALID_MESSAGE, "Código de erro desconhecido: " + code);
    }
}
