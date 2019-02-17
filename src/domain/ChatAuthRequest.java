package domain;

public class ChatAuthRequest extends Message{
    private final long time;
    private final String login;
    private final String password;

    public ChatAuthRequest(String login, String password) {
        super.type = MessageType.AUTH_REQUEST;
        this.time = System.currentTimeMillis();
        this.login = login;
        this.password = password;
    }

    public long getTime() {
        return time;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
