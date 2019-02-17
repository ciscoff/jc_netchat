package domain;

public class ChatCommand extends Message {

    private String command;
    private String[] params;

    public ChatCommand(String command) {
        super.type = MessageType.COMMAND;
        this.command = command;
        this.params = null;
    }

    public ChatCommand(String command, String[] params) {
        super.type = MessageType.REQUEST;
        this.command = command;
        this.params = params;
    }

}
