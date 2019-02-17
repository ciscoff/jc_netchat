package domain;

public class ChatMessage extends Message {
    private final String message;
    private final String color;
    private final String from;
    private final String to;

    public ChatMessage(String message, String color, String from, String to) {
        super.type = MessageType.UNICAST;
        this.message = message;
        this.color = color;
        this.from = from;
        this.to = to;
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
}
