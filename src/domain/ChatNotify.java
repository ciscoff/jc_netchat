package domain;

public class ChatNotify extends Message {
    private final String message;
    private final long time;
    private final NotifyType ntype;

    public ChatNotify(NotifyType ntype, String message) {
        super.type = MessageType.NOTIFY;
        this.ntype = ntype;
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
