package domain;

public class ChatBroadcast extends Message{
    private final String message;
    private final String color;
    private final String from;

    public ChatBroadcast(String message, String color, String from, String to) {
        super.type = MessageType.BROADCAST;
        this.message = message;
        this.color = color;
        this.from = from;
    }
}
