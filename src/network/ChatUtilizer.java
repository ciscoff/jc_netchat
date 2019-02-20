package network;

import domain.Message;

import java.io.IOException;

public interface ChatUtilizer {
    String authenticationLoop() throws IOException, ClassNotFoundException;
    void conversationLoop() throws IOException;
    void commandProcessor(Message message) throws IOException;
}
