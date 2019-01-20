package server;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static utils.Share.*;

public class ClientHandler {
    private ChatServer server;
    private Socket socket;
    private DataInputStream is = null;
    private DataOutputStream os = null;
    private boolean active = true;

    public ClientHandler(ChatServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.is = new DataInputStream(socket.getInputStream());
            this.os = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String message;
                    try {
                        while ((message = is.readUTF()) != null){
                            if(message.equals("/end")) {
                                sendMessage("Goodbye");
                                active = false;
                                break;
                            }
                            server.broadcastMessage(message);
                        }

                    } catch (IOException e) {
                        System.out.println("Client " + clientId(socket) + " disconnected");
                    } finally {
                        closeResources(is, os, socket);
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isActive() {
        return active;
    }

    public void sendMessage(String message){
        try {
            os.writeUTF(message);
            os.flush();
        } catch (IOException e) {e.printStackTrace();}
    }
}
