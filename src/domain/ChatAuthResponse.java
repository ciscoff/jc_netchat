package domain;

import utils.AuthResult;

public class ChatAuthResponse extends Message {
    private final AuthResult response;
    private final int sessionId;
    private final long time;

    public ChatAuthResponse(long time, AuthResult response, int sessionId) {
        super.type = MessageType.AUTH_RESPONSE;
        this.time = System.currentTimeMillis();
        this.response = response;
        this.sessionId = 0;
    }
}
