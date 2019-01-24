package server;

import java.util.Hashtable;
import java.util.Set;

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

            System.out.println("in ResourceCleaner");

            Hashtable<String, ClientHandler> clients = server.getClients();
            Set<String> keys = clients.keySet();

            System.out.println("ResourceCleaner: keay = " + keys.size());

            for(String key : keys) {
                ClientHandler ch = clients.get(key);
                if(ch != null) ch.closeIfIdle();
            }
        }
    }

    public static void launch(ChatServer s) {
        new ResourceCleaner(s);
    }
}
