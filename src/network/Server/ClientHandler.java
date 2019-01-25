package network.Server;

import database.ChatAuthService;
import network.ChatUtilizer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static utils.Share.*;

public class ClientHandler implements ChatUtilizer {
    private ChatServer server;
    private Socket socket;
    private String nickname;
    private String connectId;   // /127.0.0.1@@55191
    private String color;
    private long startTime; /* if == 0 than authenticated*/

    private DataInputStream is = null;
    private DataOutputStream os = null;

    public ClientHandler(ChatServer server, Socket socket) {

        try {
            this.color = systemColor;
            this.server = server;
            this.socket = socket;
            this.is = new DataInputStream(socket.getInputStream());
            this.os = new DataOutputStream(socket.getOutputStream());
            this.startTime = System.currentTimeMillis();
            this.connectId = getConnectId();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Цикл аутентификации
                        if((ClientHandler.this.nickname = authenticationLoop()) != null) {
                            ClientHandler.this.color = server.assignColor();
                            server.subscribe(ClientHandler.this);
                            server.broadcastMessage(addMetaData(" joined to chat"));
                            conversationLoop();
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

    // Цикл аутентификации
    @Override
    public String authenticationLoop() throws IOException {
        String message = null;
        String nick = null;

        while((message = is.readUTF()) != null) {
            if(message.matches(REGEX_AUTH) /* /auth login password */) {
                String [] parts = message.split( "\\s", 3);
                nick = ChatAuthService.getNickByLoginPass(parts[PROT_LOGIN], parts[PROT_PASSWORD]);
                if(nick != null) {
                    if(!server.isNickBusy(nick)){
                        sendMessage(PROT_MSG_AUTH_OK + SEPARATOR + nick); // /authok@@nick
                        break;
                    } else { sendMessage(PROT_MSG_AUTH_NICK_BUSSY); }
                } else { sendMessage(PROT_MSG_AUTH_ERROR); }
            }
        }
        return nick;
    }

    // Цикл обработки сообщений
    @Override
    public void conversationLoop() throws IOException {

        String message = null;

        while ((message = is.readUTF()) != null){

            if(message.startsWith(PROT_CMD_PREFIX)) {
                commandProcessor(message);
            } else {
                server.broadcastMessage(addMetaData(message));
                System.out.print("[" + currentTime() + ": " + nickname + "]: " + message + System.lineSeparator());
            }
        }
    }

    // Обработка комманд
    @Override
    public void commandProcessor(String command) throws IOException {
        if(command.startsWith(PROT_MSG_TO) /* /w nick message */) {
            String[] parts = command.split(SEPARATOR, 3);
            server.sendTo(parts[1], addMetaData(parts[2]));
            sendMessage(addMetaData(parts[2]));
        } else if(command.equals(PROT_MSG_END /* /end */ )) {
            server.broadcastMessage(addMetaData(" has left the chat"));
            /**
             * Нужно добавить код отключения коннекта
             */
        }
    }

    // Закрыть сокет при простое
    public boolean inIdleState() {
        boolean b = false;
        if(nickname == null) {
            if(msecToSec(System.currentTimeMillis() - startTime) > IDLE_TIMEOUT) {
                sendMessage(PROT_MSG_IDLE + SEPARATOR + "Server Connection Timeout");
                closeResources(is, os, socket);
                b = true;
            }
        }
        return b;
    }

    // Get nickname
    public String getNickname() {
        return nickname;
    }

    // Добавить служебный заголовок к сообщению
    public String addMetaData(String message) {
        return nickname + SEPARATOR + color + SEPARATOR + message;
    }

    // Отправить сообщение в сокет
    public void sendMessage(String message){
        try {
            os.writeUTF(message);
            os.flush();
        } catch (IOException e) {e.printStackTrace();}
    }

    // Уникальный ID
    public String getConnectId() {
        return socket.getInetAddress() + SEPARATOR + socket.getPort();
    }
}
