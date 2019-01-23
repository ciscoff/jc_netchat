package server;

import authorization.ChatAuthService;
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
    private String nickname;
    private String color;

    private DataInputStream is = null;
    private DataOutputStream os = null;

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

                            if(message.matches(REGEX_AUTH) /* /auth */) {
                                String [] parts = message.split( "\\s" );
                                String nick = ChatAuthService.getNickByLoginPass(parts[1], parts[2]);
                                if(nick != null) {
                                    if(!server.isNickBusy(nick)){
                                        nickname = nick;
                                        sendMessage(PROT_MSG_AUTH_OK + SEPARATOR + nick);
                                        server.subscribe(ClientHandler.this);
                                        server.broadcastMessage(addMetaData(" joined to chat"));
                                        break;
                                    } else { sendMessage(PROT_MSG_AUTH_NICK_BUSSY); }
                                } else { sendMessage(PROT_MSG_AUTH_ERROR); }
                            }
                        }

                        // Цикл обмена сообщениями
                        while ((message = is.readUTF()) != null){

                            if(message.startsWith(PROT_MSG_TO) /* /w nick message */) {
                                String[] parts = message.split(SEPARATOR);
                                server.sendTo(parts[1], addMetaData(parts[2]));
                                sendMessage(addMetaData(parts[2]));
                            } else if(message.equals(PROT_MSG_END /* /end */ )) {
                                server.broadcastMessage(addMetaData(" has left the chat"));
                                break;
                            } else {
                                server.broadcastMessage(addMetaData(message));
                                System.out.print("[" + currentTime() + ": " + nickname + "]: " + message + System.lineSeparator());
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Client " + nickname + " disconnected");
                    } finally {
                        server.unsubscribe(ClientHandler.this);
                        closeResources(is, os, socket);
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String addMetaData(String message) {
        return nickname + SEPARATOR + color + SEPARATOR + message;
    }

    public void sendMessage(String message){
        try {
            os.writeUTF(message);
            os.flush();
        } catch (IOException e) {e.printStackTrace();}
    }
}
