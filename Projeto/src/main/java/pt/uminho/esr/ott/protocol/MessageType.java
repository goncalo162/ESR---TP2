package pt.uminho.esr.ott.protocol;

/** Identificadores v1 explícitos; nunca usar ordinal() no formato de rede. */
public enum MessageType {
    REGISTER(1), REGISTER_OK(2), REGISTER_RENEW(3), NODE_LEAVE(4),
    HELLO(5), HELLO_OK(6), PROBE_PING(7), PROBE_PONG(8),
    STREAM_ANNOUNCE(9), STREAM_WITHDRAW(10), ACTIVATE(11), ACTIVATE_ACK(12),
    DEACTIVATE(13), SUBSCRIPTION_RENEW(14), ROUTE_TRACE(15), TRACE_RESULT(16), ERROR(17);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static MessageType fromCode(int code) throws ProtocolException {
        for (MessageType type : values()) if (type.code == code) return type;
        throw new ProtocolException(ErrorCode.INVALID_MESSAGE, "Tipo de mensagem desconhecido: " + code);
    }
}
