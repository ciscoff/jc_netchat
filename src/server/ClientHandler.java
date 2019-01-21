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
    private String nickname;
    private String color;

    public ClientHandler(ChatServer server, Socket socket, String color) {

        try {
            this.color = color;
            this.server = server;
            this.socket = socket;
            this.is = new DataInputStream(socket.getInputStream());
            this.os = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String message;
                    try {

                        // Цикл аутентификации
                        while((message = is.readUTF()) != null) {
                            if(message.matches(REGEX_AUTH)) {
                                String [] parts = message.split ( "\\s" );
                                String nick = server.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                                if(nick != null) {
                                    if(!server.isNickBusy(nick)){
                                        nickname = nick;
                                        sendMessage(PROT_MSG_AUTH_OK + SEPARATOR + nick);
                                        server.subscribe(ClientHandler.this);
                                        server.broadcastMessage(nick + SEPARATOR + color + SEPARATOR + nick + " joined to chat");
                                        break;
                                    }
                                }
                                sendMessage(PROT_MSG_AUTH_ERROR);
                            }
                        }

                        // Цикл обмена сообщениями
                        while ((message = is.readUTF()) != null){
                            if(message.equals(PROT_MSG_END)) {
                                sendMessage("Goodbye");
                                active = false;
                                break;
                            }
                            server.broadcastMessage(color+message);
                            System.out.print("[" + currentTime() + ": " + nickname + "]: " + message + System.lineSeparator());
                        }

                    } catch (IOException e) {
                        System.out.println("Client " + nickname + " disconnected");
                        server.unsubscribe(ClientHandler.this);
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

    public String getNickname() {
        return nickname;
    }

    public void sendMessage(String message){
        try {
            os.writeUTF(message);
            os.flush();
        } catch (IOException e) {e.printStackTrace();}
    }
}
