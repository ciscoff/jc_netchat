package domain;

public class ChatCommand extends Message {

    private String[] params;
    private CommandType ctype;

    public ChatCommand(CommandType ctype) {
        super.type = MessageType.COMMAND;
        this.ctype = ctype;
        this.params = null;
    }

    public ChatCommand(CommandType ctype, String[] params) {
        super.type = MessageType.COMMAND;
        this.ctype = ctype;
        this.params = params;
    }

}
