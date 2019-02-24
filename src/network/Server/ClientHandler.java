package network.Server;

import database.JdbcInteractor;
import domain.*;
import network.ChatUtilizer;
import utils.AuthResult;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static utils.Share.*;

public class ClientHandler implements ChatUtilizer {
    private ChatServer server;
    private JdbcInteractor ji;
    private Socket socket;
    private String nickname;
    private String connectId;   // /127.0.0.1@@55191
    private String color;
    private TreeSet<String> blacklist;
    private long startTime;

    private DataInputStream is = null;
    private DataOutputStream os = null;

    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;

    public ClientHandler(ChatServer server, Socket socket, JdbcInteractor ji) {

        try {
            this.color = systemColor;
            this.server = server;
            this.socket = socket;
            this.ji = ji;
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.startTime = System.currentTimeMillis();
            this.connectId = getConnectId();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Цикл аутентификации
                        if ((ClientHandler.this.nickname = authenticationLoop()) != null) {
                            p(ClientHandler.this.nickname);
                            ClientHandler.this.color = server.assignColor();
                            server.subscribe(ClientHandler.this);
                            //blacklist = ji.getBlackList(nickname);

                            // Отправить новому клиенту историю чата
                            //server.sendHistory(ClientHandler.this, ji.getHistory(ClientHandler.this.nickname));

                            // Известить всех о новом клиенте
                            server.broadcastMessage(ClientHandler.this,
                                    new ChatMessageServer(MessageType.BROADCAST_SERVER, nickname + " joined to chat",
                                            color, nickname, null));
                            conversationLoop();
                        }
                    } catch (IOException e) {
                        System.out.println("Client " + nickname + " disconnected");
                    } catch (ClassNotFoundException e) {
                        System.out.println("Incorrect message type");
                    }
                    finally {
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
    public String authenticationLoop() throws IOException, ClassNotFoundException {
        String nick = null;
        Message message;

        while ((message = (Message)ois.readObject()) != null) {

            switch(message.type){
                case AUTH_REQUEST:
                    ChatAuthRequest request = (ChatAuthRequest)message;
                    nick = ji.getNickByLoginPass(request.getLogin(), request.getPassword());
                    p("authenticationLoop: " + nick + ":" + request.getLogin() + ":" + request.getPassword());
                    if (nick != null) {
                        if (server.nickNotBussy(nick)) {
                            // Сообщить клиенту об успешной аутентификации и отправить nickname
                            sendMessage(new ChatAuthResponse(AuthResult.AUTH_OK, getSessionId(), nick, optional()));
                            // Выход из цикла и метода
                            return nick;
                        } else {
                            sendMessage(new ChatAuthResponse(AuthResult.NICK_BUSSY, 0, null, PROT_MSG_AUTH_NICK_BUSSY));
                        }
                    } else {
                        sendMessage(new ChatAuthResponse(AuthResult.AUTH_ERROR, 0, nick, PROT_MSG_AUTH_ERROR));
                    }
                    break;
            }
        }
        return nick;
    }

//    public String authenticationLoop() throws IOException {
//        String message = null;
//        String nick = null;
//
//        while ((message = is.readUTF()) != null) {
//            if (message.matches(REGEX_AUTH) /* /auth login password */) {
//                String[] parts = message.split("\\s", 3);
//                nick = ji.getNickByLoginPass(parts[PROT_LOGIN], parts[PROT_PASSWORD]);
//                if (nick != null) {
//                    if (!server.isNickBusy(nick)) {
//                        // Сообщить об успешной аутентификации и отправить nickname
//                        sendMessage(PROT_MSG_AUTH_OK + SEPARATOR + nick);
//                        break;
//                    } else {
//                        sendMessage(PROT_MSG_AUTH_NICK_BUSSY);
//                    }
//                } else {
//                    sendMessage(PROT_MSG_AUTH_ERROR);
//                }
//            }
//        }
//        return nick;
//    }

    // Цикл обработки сообщений
    @Override
    public void conversationLoop() throws IOException {
        Message message = null;

        try {
            while ((message = (Message) ois.readObject()) != null) {

                switch (message.type) {
                    case COMMAND:
                    case NOTIFY:
                        commandProcessor(message);
                        break;
                    case BROADCAST_CLIENT:
                        server.broadcastMessage(this, mapMessage((ChatMessageClient) message));
                        break;
                    case UNICAST_CLIENT:
                        server.sendTo(this, mapMessage((ChatMessageClient) message));
                        break;

                }

//                if (message.startsWith(PROT_CMD_PREFIX)) {
//                    commandProcessor(message);
//                } else {
//                    server.broadcastMessage(this, addMetaData(message));
//                    System.out.print("[" + currentTime() + ": " + nickname + "]: " + message + System.lineSeparator());
//                }
            }
        } catch (ClassNotFoundException e) {e.printStackTrace();}
    }

    // Обработка комманд
    @Override
    public void commandProcessor(Message message) throws IOException {

//        if (command.startsWith(PROT_MSG_TO) /* /w@@nick@@message */) {
//            String[] parts = command.split(SEPARATOR, 3);
//            server.sendTo(this, parts[1], addMetaData(parts[2]));
//            sendMessage(addMetaData(parts[2]));
//        } else if (command.equals(PROT_MSG_END /* /end */)) {
//            server.broadcastMessage(this, addMetaData(" has left the chat"));
//            /**
//             * Нужно добавить код отключения коннекта
//             */
//        } else if (command.startsWith(PROT_MSG_BLOCK)) {  /* "/block nickBl1 nickBl2 ..." */
//            List<String> ll = new LinkedList<>(Arrays.asList(command.split("\\s")));
//            ll.remove(0);                           /* remove "/block" */
//            // Нельзя добавлять свой ник в черный список
//            if(ll.contains(nickname)) ((LinkedList<String>) ll).remove(nickname);
//            // Обновить кеш blacklist
//            for(String s : ll){ blacklist.add(s);}
//            // Обновить sqlite blacklist
//            ji.addToBlackList(nickname, new TreeSet<>(ll));
//        } else if (command.startsWith(PROT_MSG_SHOW_BL)) {   /* /showbl */
//            ji.getBlackList(nickname).forEach((s) -> System.out.println(s));
//        } else if(command.startsWith(PROT_MSG_UNBLOCK)) {
//            List<String> ll = new LinkedList<>(Arrays.asList(command.split("\\s")));
//            ll.remove(0);                           /* remove "/block" */
//            // Обновить кеш blacklist
//            for(String s : ll){ blacklist.remove(s);}
//            // Обновить sqlite blacklist
//            ji.removeFromBlackList(nickname, new TreeSet<>(ll));
//        }
    }

    // Закрыть сокет при простое
    public boolean inIdleState() {
        boolean b = false;
        if (nickname == null) {
            if (msecToSec(System.currentTimeMillis() - startTime) > IDLE_TIMEOUT) {
                sendMessage(new ChatNotify(NotifyType.IDLE, "Server Connection Timeout"));
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
    public void sendMessage(Message message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            os.writeUTF(message);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Получить blacklist
    public TreeSet<String> getBlacklist() {
        return blacklist;
    }

    // Уникальный ID
    public String getConnectId() {
        return socket.getInetAddress() + SEPARATOR + socket.getPort();
    }

    // Сгенерить ID сессии
    private int getSessionId() {
        return this.hashCode();
    }

    /**
     *  Смапить клиентское сообщение в формат серверного сообщения
     */
    private ChatMessageServer mapMessage(ChatMessageClient cmc) {

        ChatMessageServer cms = new ChatMessageServer(
                cmc.type == MessageType.UNICAST_CLIENT ? MessageType.UNICAST_SERVER : MessageType.BROADCAST_SERVER,
                cmc.getMessage(),
                color,
                nickname,
                cmc.getTo()
        );

        return cms;
    }
}
