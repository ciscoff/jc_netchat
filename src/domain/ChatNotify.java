package domain;

public class ChatNotify extends Message {
    private final String message;
    private final long time;

    public ChatNotify(String message) {
        super.type = MessageType.NOTIFY;
        this.message = message;
        this.time = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }
}
