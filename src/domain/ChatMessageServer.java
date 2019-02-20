package domain;

public class ChatMessageServer extends Message {
    private final String message;
    private final String color;
    private final String from;
    private final String to;
    private final long time;

    public ChatMessageServer(MessageType type, String message, String color, String from, String to) {
        super.type = type;
        this.to = type == MessageType.UNICAST_SERVER ? to : null;
        this.time = System.currentTimeMillis();
        this.message = message;
        this.color = color;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }
    public String getColor() {
        return color;
    }
    public String getTo() {
        return to;
    }
    public String getFrom() {
        return from;
    }
    public long getTime() {
        return time;
    }
}
