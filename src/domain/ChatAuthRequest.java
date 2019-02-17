package domain;

public class ChatAuthRequest extends Message{
    private final long time;
    private final String login;
    private final String password;

    public ChatAuthRequest(long time, String login, String password) {
        super.type = MessageType.AUTH_REQUEST;
        this.time = System.currentTimeMillis();
        this.login = login;
        this.password = password;
    }
}
