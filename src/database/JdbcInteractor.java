package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeSet;

public class JdbcInteractor {

    private JdbcController jc;

    public JdbcInteractor() {
        this.jc = JdbcController.getIdbc();
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
                    "INSERT INTO blacklist (nick_id, blocked_nick_id) VALUES\n" +
                            "((SELECT A.id FROM users A where nickname = '%s'),\n" +
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
}
