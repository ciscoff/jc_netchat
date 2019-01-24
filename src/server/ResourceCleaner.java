package server;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import static utils.Share.*;

public class ResourceCleaner extends Thread {
    private ChatServer server;

    public ResourceCleaner(ChatServer server) {
        this.server = server;
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {

        while (true) {
            try {
                Thread.sleep(secToMsec(CLEANER_TIMEOUT));
            } catch (InterruptedException e) {e.printStackTrace();}

            Vector<ClientHandler> ps = server.getPretenders();
            System.out.println("ResourceCleaner: pretenders = " + ps.size());

            ps.forEach((c) -> c.closeIfIdle());
        }
    }

    public static void launch(ChatServer s) {
        new ResourceCleaner(s);
    }
}
