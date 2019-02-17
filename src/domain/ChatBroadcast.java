package domain;

public class ChatBroadcast extends Message{
    private final String message;
    private final String color;
    private final String from;
    private final long time;

    public ChatBroadcast(String message, String color, String from, String to) {
        super.type = MessageType.BROADCAST;
        this.message = message;
        this.color = color;
        this.from = from;
        this.time = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public String getColor() {
        return color;
    }

    public String getFrom() {
        return from;
    }

    public long getTime() {
        return time;
    }
}
