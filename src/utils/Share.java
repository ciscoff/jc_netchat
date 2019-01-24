package utils;

import java.io.Closeable;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Share {
    public static final int PORT = 9191;
    public static final String HOST = "127.0.0.1";

    public static final String REGEX_AUTH = "^/auth\\s.+";
    public static final String SEPARATOR = "@@";

    public static final String PROT_MSG_AUTH = "/auth";
    public static final String PROT_MSG_AUTH_OK = "/authok";
    public static final String PROT_MSG_AUTH_ERROR = "Login or password incorrect";
    public static final String PROT_MSG_AUTH_NICK_BUSSY = "User already joined";

    public static final String PROT_CMD_PREFIX = "/";
    public static final String PROT_MSG_END = "/end";
    public static final String PROT_MSG_TO = "/w";
    public static final String PROT_MSG_BYE = "Goodbye";
    public static final String PROT_MSG_SERVER_CLOSED = "/serverClosed";

    public static final int PROT_NICK_FROM = 0;
    public static final int PROT_COLOR = 1;
    public static final int PROT_MSG_BODY = 2;
    public static final int PROT_PARTS_NUM = 3;
    public static final int PROT_LOGIN = 1;
    public static final int PROT_PASS = 2;

    public static final int CLEANER_TIMEOUT = 5;
    public static final int IDLE_TIMEOUT = 30;



    public static final String URL_JDBC = "jdbc:sqlite:main.db";
    public static final String JDBC_CLASS_NAME = "org.sqlite.JDBC";

    public static final long FLAG_AUTHENTICATED = 0;


    /**
     * Цвета оформления сообщений
     */
    public static final String[] colors = {"#efe4b0;", "#d2d2d2;", "#99d9ea;", "#7092be;", "#ffaec9;", "#b5e61d;", "#ffc90e;"};
    public static final String systemColor = "#ffffff;";

    public static String currentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public static void closeResources(Closeable... obj){
        for (Closeable o: obj) {
            try {
                if(o != null) o.close();
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    public static long secToMsec(long sec) {
        return (sec * 1000);
    }

    public static long msecToSec(long msec) {
        return Math.abs(msec / 1000);
    }
}
