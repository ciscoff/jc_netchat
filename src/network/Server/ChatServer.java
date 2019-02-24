package network.Server;

import database.HistoryEntry;
import database.JdbcInteractor;
import domain.ChatMessageClient;
import domain.ChatMessageServer;
import domain.Message;
import domain.MessageType;

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

        // Если клиент находится в состоянии простоя, то удаляем его их HashMap.
        // Удаление выполняем через Iterator.
        while (it.hasNext()) {
            Map.Entry<String, ClientHandler> pair = (Map.Entry) it.next();

            if (pair.getKey().startsWith(SOCKET_PREFIX) && pair.getValue().inIdleState()) {
                it.remove();
            }
        }
    }

    /**
     * Старт потока сервера
     */
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

    /**
     *  TODO: Этот костыль исправить !!!!!
     */
    boolean inHistory;


    /**
     * Отправить сообщение всем АВТОРИЗОВАННЫМ клиентам с проверкой черного списка отправителя и каждого получателя.
     *
     * Все обычные соообщения отправляются в формате "nickFrom@@color@text_message"
     * Все служебные сообщения предваряются префиксом "/", их не помещаем в history.
     * В history сохраняем всё без учета blacklist'а
     */

    public synchronized void broadcastMessage(ClientHandler sender, ChatMessageServer message) {
        inHistory = false;

        clients.entrySet().forEach((e) -> {

            // Отправить можно только авторизованному клиенту
            if(e.getKey().startsWith(SOCKET_PREFIX)) {
                p(e.getKey());
                return;
            }

            // Поместить сообщение в историю
//            if(!inHistory) {
//                // Так как сообщения отправляются в формате 'nick1@@#efe4b0;@@какой-то_текстс',
//                // то их нужно укладывать в кавычки. Иначе ошибка.
//                if(!message.startsWith(PROT_CMD_PREFIX)) {
//                    ji.toHistory(sender.getNickname(), PROT_MSG_BROADCAST, "'" + message + "'");
//                    inHistory = true;
//                }
//            }

            e.getValue().sendMessage(message);

            // Проверить черный список и отправить
//            if (allowedToSend(sender, e.getValue())) {
//                e.getValue().sendMessage(message);
//            }
        });
    }

    // Отправить конкретному получателю
//    public synchronized void sendTo(ClientHandler from, String nickTo, String message) {
//        ClientHandler chTo = clients.get(nickTo);
//
//        // Получатель существует ?
//        if(chTo == null) return;
//
//        // Не сохраняем служебные сообщения
//        if(!message.startsWith(PROT_CMD_PREFIX)) {
//            ji.toHistory(from.getNickname(), nickTo, "'" + message + "'");
//        }
//
//        // Проверка черного списка
//        if(allowedToSend(from, chTo)) chTo.sendMessage(message);
//    }

    // Отправить конкретному получателю
    public synchronized void sendTo(ClientHandler from, Message message) {

        ChatMessageServer cms = (ChatMessageServer)message;
        ClientHandler chTo = clients.get(cms.getTo());

        // Получатель в чате ?
        if(chTo == null) {
            p("Client '" + cms.getTo() + "' not exists");
            return;
        }

        // В историю не должны попадать служебные сообщения
//        if(message.type == MessageType.BROADCAST_CLIENT || message.type == MessageType.UNICAST_CLIENT) {
//            ji.toHistory(from.getNickname(), nickTo, "'" + message + "'");
//        }

        // Проверка черного списка
//        if(allowedToSend(from, chTo)) chTo.sendMessage(message);

        // Отправить получателю и отправителю
        chTo.sendMessage(message);
        from.sendMessage(message);
    }


    // Отправить историю чата новому клиенту
    public synchronized void sendHistory(ClientHandler newClient, HistoryEntry[] history){
        for(HistoryEntry entry: history) {

            // Отправляем сообщение если отправитель не из черного списка получателя
            System.out.println("sendHistory " + entry.getFrom());
            if(!newClient.getBlacklist().contains(entry.getFrom())) newClient.sendMessage(entry.getMsg());
        }
    }

    // Проверить, что оба клиента не блокируют друг друга
    private boolean allowedToSend(ClientHandler sender, ClientHandler recepient) {

        if ((sender.getBlacklist().contains(recepient.getNickname())) ||
                (recepient.getBlacklist().contains(sender.getNickname()))) return false;

        return true;
    }

    public synchronized boolean nickNotBussy(String nickname) {
        return clients.get(nickname) == null;
    }

    public synchronized void subscribe(ClientHandler ch) {
        String nick = ch.getNickname();
        if (nick == null) { // Клиент ещё не аутентифицирован и не имеет nick'а
            clients.put(ch.getConnectId(), ch);
        } else {            // Клиент аутентифицирован и получил nick
            clients.remove(ch.getConnectId());
            clients.put(nick, ch);
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