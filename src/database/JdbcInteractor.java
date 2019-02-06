package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeSet;

public class JdbcInteractor {

    private JdbcController jc;

    public JdbcInteractor() {

        this.jc = JdbcController.getIdbc();
        clearHistory();
    }


    // Очистить историю
    public void clearHistory() {
        jc.executeUpdate("DELETE FROM history");
    }

    // Аутентификация по базе
    public synchronized String getNickByLoginPass(String login, String pass) {

        String result = null;
        ResultSet rs = jc.executeQuery(
                String.format("select nickname from users where login = '%s' and password = '%s'", login, pass));

        if (rs != null) {
            try {
                if (rs.next()) {
                    result = rs.getString("nickname");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // Прочитать blacklist пользователя
    public synchronized TreeSet<String> getBlackList(String nick) {
        ResultSet rs = jc.executeQuery(String.format(
                "SELECT nickname FROM users WHERE id IN " +
                        "(SELECT blocked_nick_id FROM blacklist WHERE nick_id = " +
                        "(SELECT id FROM users WHERE nickname ='%s'))", nick));

        TreeSet<String> result = new TreeSet<>();

        if (rs != null) {
            try {
                while (rs.next()) {
                    result.add(rs.getString("nickname"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    // Добавить в базу список заблокированных пользователей
    public synchronized void addToBlackList(String nick, TreeSet<String> blocked) {
        for (String b : blocked) {
            String query = String.format(
                    "INSERT INTO blacklist (nick_id, blocked_nick_id) VALUES n" +
                            "((SELECT A.id FROM users A where nickname = '%s'), n" +
                            "(SELECT B.id FROM users B WHERE B.nickname = '%s'))", nick, b);

            jc.executeUpdate(query);
        }
    }

    public synchronized void removeFromBlackList(String nick, TreeSet<String> unblocked) {
        for (String b : unblocked) {
            String query = String.format(
                    "DELETE FROM blacklist WHERE nick_id = " +
                            "(SELECT id FROM users WHERE nickname = '%s') " +
                            "AND blocked_nick_id = " +
                            "(SELECT id FROM users WHERE nickname = '%s')", nick, b);

            jc.executeUpdate(query);
        }
    }

    // Сохранить сообщение в базе
    public synchronized void toHistory(String sender, String receiver, String message) {

        String query = String.format(
                "INSERT INTO history (sender, receiver, message) VALUES " +
                        "((SELECT A.id FROM users A where nickname = '%s'), " +
                        "(SELECT B.id FROM users B WHERE B.nickname = '%s'), %s)", sender, receiver, message);
        jc.executeUpdate(query);
    }

    // Получить историю чата
    public synchronized HistoryEntry[] getHistory() {


        ResultSet rs = jc.executeQuery("SELECT sender, message FROM history");
        ArrayList<HistoryEntry> al = new ArrayList<>();

        if (rs != null) {
            try {
                while (rs.next()) {
                    al.add(new HistoryEntry(
                            rs.getString("sender"),
                            rs.getString("message")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return al.toArray(new HistoryEntry[al.size()]);
    }

    /**
     * Задача этого метода сделать выборку из истории только тех сообщений, которые валидны
     * относительно черного списка клиента nick и черных списков всех других отправителей.
     * То есть nick не должен получать от тех кого сам заблокировал и от тех, кто его заблокировал.
     * Получился адский SELECT ))
     * <p>
     * <p>
     * ;;; 1. Определяем id для nick1 ;;;
     * (select id from users where nickname = 'nick1')
     * <p>
     * ;;; 2. Получаем Из истории сообщения отправителей не входящих в блэклист nick1 ;;;
     * select sender, message from history
     * where sender not in (select blocked_nick_id from blacklist where nick_id = (select id from users where nickname = 'nick1'));
     * <p>
     * ;;; 3. В блэклисте оставляем только те записи, где nick1 заблокирован
     * (select * from blacklist where blocked_nick_id = (select id from users where nickname = 'nick1')
     * <p>
     * ;;; 4. склеиваем таблицы полученные на шагах 2. и 3. отбрасывая строки, где nick1 является заблокированным
     * left join ...
     * on ...
     * where ...
     */
    public synchronized HistoryEntry[] getHistory(String nick) {

        ArrayList<HistoryEntry> al = new ArrayList<>();

        String query = String.format(
                "select WL.sender, WL.message, BL.blocked_nick_id from " +
                        "  (select sender, message from history where sender not in (select blocked_nick_id from blacklist where nick_id = (select id from users where nickname = '%s'))) as WL " +
                        "left join (select * from blacklist where blocked_nick_id = (select id from users where nickname = '%s')) as BL " +
                        "on WL.sender = BL.nick_id " +
                        "where BL.blocked_nick_id is NULL;", nick, nick);

        // Проверка наличия записей в таблице черного списка
        ResultSet rs = jc.executeQuery("SELECT count(*) FROM blacklist");

        try {
            if (rs.getInt(1) == 0) return getHistory();

            System.out.println("count = " + rs.getInt(1));


            System.out.println(query);

            rs = jc.executeQuery(query);

            if (rs != null) {
                while (rs.next()) {
                    al.add(new HistoryEntry(
                            rs.getString("sender"),
                            rs.getString("message")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return al.toArray(new HistoryEntry[al.size()]);
    }

}
