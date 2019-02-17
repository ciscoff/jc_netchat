package domain;

import utils.AuthResult;

public class ChatAuthResponse extends Message {
    private final AuthResult response;
    private final int sessionId;
    private final long time;
    private final String nick;

    public ChatAuthResponse(AuthResult response, int sessionId, String nick) {
        super.type = MessageType.AUTH_RESPONSE;
        this.time = System.currentTimeMillis();
        this.response = response;
        this.sessionId = sessionId;
        this.nick = nick;
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
}
