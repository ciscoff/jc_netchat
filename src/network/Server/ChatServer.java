package network.Server;

import database.JdbcInteractor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static utils.Share.*;

public class ChatServer implements Cleaner {
    private HashMap<String, ClientHandler> clients = new HashMap<>();
    private JdbcInteractor ji = null;
    private int colorIdx = 0;

    @Override
    public void scheduledCleaning() {
        Iterator it = clients.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, ClientHandler> pair = (Map.Entry) it.next();

            if (pair.getKey().startsWith(SOCKET_PREFIX) && pair.getValue().inIdleState()) {
                it.remove();
            }
        }
    }

    public void start() {

        // Connect to DB
        ji = new JdbcInteractor();

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
                subscribe(new ClientHandler(this, clientSocket, ji));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResources(serverSocket);
        }
    }

    // Отправить сообщение всем АВТОРИЗОВАННЫМ клиентам с проверкой
    // черного списка отправителя и каждого получателя.
    public synchronized void broadcastMessage(ClientHandler sender, String message) {
        clients.entrySet().forEach((e) -> {
            if ((!e.getKey().startsWith(SOCKET_PREFIX)) && (allowedToSend(sender, e.getValue()))) {
                e.getValue().sendMessage(message);
            }
        });
    }

    // Проверить, что оба клиента не блокируют друг друга
    private boolean allowedToSend(ClientHandler sender, ClientHandler recepient) {

        if ((sender.getBlacklist().contains(recepient.getNickname())) ||
                (recepient.getBlacklist().contains(sender.getNickname()))) return false;

        return true;
    }

    // Отправить конкретному получателю
    public synchronized void sendTo(ClientHandler from, String nickTo, String message) {
        ClientHandler chTo = clients.get(nickTo);

        if((chTo != null) && allowedToSend(from, chTo)){
            chTo.sendMessage(message);
        }
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

    /**
     *
     * Старт сервера
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}