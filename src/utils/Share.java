package utils;

import java.io.Closeable;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Share {
    public static final int PORT = 9191;
    public static final String HOST = "127.0.0.1";

    /**
     * Цвета оформления сообщений
     */

    public static final String[] colors = {"#efe4b0;", "#d2d2d2;", "#99d9ea;", "#7092be;", "#ffaec9;", "#b5e61d;", "#ffc90e;"};


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

    public static String clientId(Socket socket) {
        return socket.getInetAddress().toString().substring(1) + ":" + socket.getPort();
    }

}
