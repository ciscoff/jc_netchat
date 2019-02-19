package domain;

import utils.AuthResult;

import java.util.Optional;

public class ChatAuthResponse extends Message {
    private final AuthResult response;
    private final int sessionId;
    private final long time;
    private final String nick;
    private final Optional<String> message;

    public ChatAuthResponse(AuthResult response, int sessionId, String nick, Optional<String> message) {
        super.type = MessageType.AUTH_RESPONSE;
        this.time = System.currentTimeMillis();
        this.response = response;
        this.sessionId = sessionId;
        this.nick = nick;
        this.message = message;
    }

    public AuthResult getResponse() {
        return response;
    }

    public int getSessionId() {
        return sessionId;
    }

    public long getTime() {
        return time;
    }

    public String getNick() {
        return nick;
    }

    public String getMessage() {
        return message.orElse("");
    }
}
