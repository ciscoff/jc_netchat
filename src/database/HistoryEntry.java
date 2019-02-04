package database;

public class HistoryEntry {
    private String from;
    private String to;
    private String msg;

    public HistoryEntry(String from, String to, String msg) {
        this.from = from;
        this.to = to;
        this.msg = msg;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getMsg() {
        return msg;
    }
}
