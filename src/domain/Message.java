package domain;

import java.io.Serializable;

public abstract class Message implements Serializable {
    public MessageType type;
}
