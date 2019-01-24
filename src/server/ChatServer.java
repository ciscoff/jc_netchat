package server;

import authorization.AuthService;
import authorization.BaseAuthService;
import authorization.ChatAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static utils.Share.*;
import static utils.Share.currentTime;

public class ChatServer {
    private Hashtable<String, ClientHandler> clients =  new Hashtable<>();

    public void start(){

        ChatAuthService.connect();

        new ResourceCleaner(this);

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        boolean active = true;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server started on port " + PORT);
            int colorIdx = 0;

            while(active) {
                clientSocket = serverSocket.accept();

                // Выделить новому клиенту цвет фона сообщений
                if(colorIdx == colors.length) colorIdx = 0;
                new ClientHandler(this, clientSocket, colors[colorIdx++]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResources(serverSocket);
            ChatAuthService.disconnect();
        }
    }

    // Отправить сообщение всем клиентам
    public synchronized void broadcastMessage(String message) {
        Set<String> keys = clients.keySet();
        for(String key : keys) {
            ClientHandler ch = clients.get(key);
            if(ch != null) ch.sendMessage(message);
        }
    }

    // Отправить конкретному получателю
    public synchronized void sendTo(String nick, String message) {
        ClientHandler ch = clients.get(nick);
        if(ch != null) ch.sendMessage(message);
    }

    public synchronized boolean isNickBusy(String nickname) {
        return clients.get(nickname) != null;
    }
    public synchronized void subscribe(ClientHandler ch) {
        clients.put(ch.getNickname(), ch);
    }
    public synchronized void unsubscribe(ClientHandler ch) {
        clients.remove(ch.getNickname());
    }
    public synchronized Hashtable<String, ClientHandler> getClients() { return clients; }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}