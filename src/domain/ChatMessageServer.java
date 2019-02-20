package domain;

public class ChatMessageServer extends Message {
    private final String message;
    private final String color;
    private final String from;
    private final String to;
    private final long time;

    public ChatMessageServer(MessageType type, String message, String color, String from, String to) {
        super.type = type;
        this.message = message;
        this.color = color;
        this.from = from;
        this.to = to;
        this.time = System.currentTimeMillis();
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
