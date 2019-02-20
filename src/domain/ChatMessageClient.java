package domain;

import java.util.Optional;

public class ChatMessageClient extends Message {
    private final String message;
    private final String from;
    private final String to;
    private final long time;

    public ChatMessageClient(MessageType type, String message, String from, String to) {
        super.type = type;
        this.message = message;
        this.from = from;
        this.to = to;
        this.time = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public long getTime() {
        return time;
    }
}
