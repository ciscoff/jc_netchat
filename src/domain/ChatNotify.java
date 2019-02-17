package domain;

public class ChatNotify extends Message {
    private final String message;

    public ChatNotify(String message) {
        super.type = MessageType.NOTIFY;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
