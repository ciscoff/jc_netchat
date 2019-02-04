package network;

import java.io.IOException;

public interface ChatUtilizer {
    String authenticationLoop() throws IOException;
    void conversationLoop() throws IOException;
    void commandProcessor(String s) throws IOException;
}
