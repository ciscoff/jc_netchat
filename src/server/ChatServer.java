package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static utils.Share.*;
import static utils.Share.currentTime;

public class ChatServer {
    Hashtable<String, ClientHandler> clients =  new Hashtable<>();

    public void start(){

        ServerSocket serverSocket = null;
        Socket client = null;
        boolean active = true;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server started on port " + PORT);
            int colorIdx = 0;

            while(active) {
                client = serverSocket.accept();
                System.out.println(currentTime() + ": Client " + clientId(client) + " connected");

                if(colorIdx == colors.length) colorIdx = 0;
                clients.put(clientId(client), new ClientHandler(this, client, colors[colorIdx++]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Отправить сообщение всем клиентам
    public void broadcastMessage(String message) {
        Set<String> keys = clients.keySet();
        for(String key : keys) {
            ClientHandler ch = clients.get(key);
            if(ch.isActive()) ch.sendMessage(message);
        }
    }

    // Удалить клиента из хеш-таблицы
    public void removeClient(String id) {
        clients.remove(id);
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
