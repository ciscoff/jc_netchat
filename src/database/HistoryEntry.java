package database;

public class HistoryEntry {
    private String from;
    private String to;
    private String msg;

    public HistoryEntry(String from, String msg) {
        this.from = from;
        this.msg = msg;
    }

    public String getFrom() {
        return from;
    }

    public String getMsg() {
        return msg;
    }
}
