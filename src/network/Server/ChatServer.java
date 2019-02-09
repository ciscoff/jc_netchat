package network.Server;

import database.HistoryEntry;
import database.JdbcInteractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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

    /**
     * Сгенерить файлы истории чата по одному для каждого клиента.
     * Содержимое фалов одинаковое.
     * Имя файла в формате nickX.txt
     *
     * @param count Количество записей в истории
     */
    private void generateHistory(int count) {
        String[] nicknames = ji.getNicks();
        String[] coloredNick = new String[nicknames.length];

        System.out.println("nicknames " + nicknames.length);

        // Каждому нику присвоить цвет сообщений.
        for (int i = 0, j = 0; i < nicknames.length; i++, j++) {
            if (j == colors.length) j = 0;
            coloredNick[i] = nicknames[i] + SEPARATOR + colors[j];
            System.out.println(coloredNick[i]);
        }

        // Нагенерить массив сообщений
        String[] messages = new String[count];
        Random randomGreeting = new Random();
        Random randomNick = new Random();

        for (int i = 0; i < messages.length; i++) {
            messages[i] =
                    coloredNick[randomNick.nextInt(coloredNick.length)] +
                            SEPARATOR +
                            greetings[randomGreeting.nextInt(greetings.length)];
        }

        // Записать сообщения в файлы
        // Создать по одному файлу для каждого ника.
        for (int i = 0; i < nicknames.length; i++) {

                try (FileOutputStream fos = new FileOutputStream(DIR_LOCAL_HISTORY + nicknames[i] + ".txt")) {
                fos.write(String.join("\n", messages).getBytes());
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Запуск серверной части
     */
    public void start() {

        // Connect to DB
        ji = new JdbcInteractor();

        // Сгенерить историю чата
        generateHistory(100);

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
     * TODO: Этот костыль исправить !!!!!
     */
    boolean inHistory;


    /**
     * Отправить сообщение всем АВТОРИЗОВАННЫМ клиентам с проверкой черного списка отправителя и каждого получателя.
     * <p>
     * Все обычные соообщения отправляются в формате "nickFrom@@color@text_message"
     * Все служебные сообщения предваряются префиксом "/", их не помещаем в history.
     * В history сохраняем всё без учета blacklist'а
     */

    public synchronized void broadcastMessage(ClientHandler sender, String message) {
        inHistory = false;

        clients.entrySet().forEach((e) -> {

            // Отправить можно только авторизованному клиенту
            if (e.getKey().startsWith(SOCKET_PREFIX)) return;

            // Поместить сообщение в историю
            if (!inHistory) {
                // Так как сообщения отправляются в формате 'nick1@@#efe4b0;@@какой-то_текстс',
                // то их нужно укладывать в кавычки. Иначе ошибка.
                if (!message.startsWith(PROT_CMD_PREFIX)) {
                    ji.toHistory(sender.getNickname(), PROT_MSG_BROADCAST, "'" + message + "'");
                    inHistory = true;
                }
            }

            // Проверить черный список и отправить
            if (allowedToSend(sender, e.getValue())) {
                e.getValue().sendMessage(message);
            }
        });
    }

    // Отправить конкретному получателю
    public synchronized void sendTo(ClientHandler from, String nickTo, String message) {
        ClientHandler chTo = clients.get(nickTo);

        // Отправительнь существует ?
        if (chTo == null) return;

        // Не сохраняем служебные сообщения
        if (!message.startsWith(PROT_CMD_PREFIX)) {
            ji.toHistory(from.getNickname(), nickTo, "'" + message + "'");
        }

        // Проверка черного списка
        if (allowedToSend(from, chTo)) chTo.sendMessage(message);
    }

    // Отправить историю чата новому клиенту
    public synchronized void sendHistory(ClientHandler newClient, HistoryEntry[] history) {
        for (HistoryEntry entry : history) {

            // Отправляем сообщение если отправитель не из черного списка получателя
            System.out.println("sendHistory " + entry.getFrom());
            if (!newClient.getBlacklist().contains(entry.getFrom())) newClient.sendMessage(entry.getMsg());
        }
    }

    // Проверить, что оба клиента не блокируют друг друга
    private boolean allowedToSend(ClientHandler sender, ClientHandler recepient) {

        if ((sender.getBlacklist().contains(recepient.getNickname())) ||
                (recepient.getBlacklist().contains(sender.getNickname()))) return false;

        return true;
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
     * Старт сервера
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}