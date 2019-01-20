package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static utils.Share.*;
import static utils.Share.currentTime;

public class ChatServer {

    Hashtable<String, ClientHandler> clients =  new Hashtable<>();
//        Vector<ClientHandler> clients = new Vector<>();

    public void start(){

        ServerSocket serverSocket = null;
        Socket client = null;
        boolean active = true;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server started on port " + PORT);

            while(active) {
                client = serverSocket.accept();
                System.out.println(currentTime() + ": Client " + clientId(client) + " connected" );

                clients.put(clientId(client), new ClientHandler(this, client));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        Set<String> keys = clients.keySet();
        for(String key : keys) {
            ClientHandler ch = clients.get(key);
            if(ch.isActive()) ch.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
