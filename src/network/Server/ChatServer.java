package network.Server;

import database.ChatAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static utils.Share.*;

public class ChatServer implements Cleaner {
    private HashMap<String, ClientHandler> clients = new HashMap<>();
    private int colorIdx = 0;

    @Override
    public void scheduledCleaning() {
        Iterator it = clients.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, ClientHandler> pair = (Map.Entry) it.next();

            if (pair.getKey().startsWith(SOCKET_PREFIX) && pair.getValue().inIdleState()) {
                System.out.println("Client " + pair.getKey() + " in idle state and will be closed");
                it.remove();
            }
        }
    }

    public void start() {

        // Connect to DB
        ChatAuthService.connect();

        // Start resource cleaner
        new ResourceCleaner(this);

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        boolean active = true;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server started on port " + PORT);

            while (active) {
                clientSocket = serverSocket.accept();

                // И поместить в список кандидатов
                subscribe(new ClientHandler(this, clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResources(serverSocket);
            ChatAuthService.disconnect();
        }
    }

    // Отправить сообщение всем АВТОРИЗОВАННЫМ клиентам
    public synchronized void broadcastMessage(String message) {
        Set<String> keys = clients.keySet();
        for (String key : keys) {
            if(!key.startsWith(SOCKET_PREFIX)) {
                ClientHandler ch = clients.get(key);
                if (ch != null) ch.sendMessage(message);
            }
        }
    }

    // Отправить конкретному получателю
    public synchronized void sendTo(String nick, String message) {
        ClientHandler ch = clients.get(nick);
        if (ch != null) ch.sendMessage(message);
    }

    public synchronized boolean isNickBusy(String nickname) {
        return clients.get(nickname) != null;
    }

    public synchronized void subscribe(ClientHandler ch) {
        String nick = ch.getNickname();
        if (nick != null) {
            clients.remove(ch.getConnectId());
            clients.put(nick, ch);
        } else {
            clients.put(ch.getConnectId(), ch);
        }
    }

    public synchronized void unsubscribe(ClientHandler ch) {
        String nick = ch.getNickname();
        clients.remove((nick != null) ? nick : ch.getConnectId());
    }

    // Выделить авторизованному клиенту цвет фона сообщений
    public String assignColor() {
        if (colorIdx == colors.length) colorIdx = 0;
        return colors[colorIdx++];
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }

}