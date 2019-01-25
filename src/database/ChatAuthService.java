package database;

import java.sql.*;

import static utils.Share.*;

public class ChatAuthService {

    public static Connection connection;
    public static Statement statement;

    public static void connect() {
        try {
            Class.forName(JDBC_CLASS_NAME);
            connection = DriverManager.getConnection(URL_JDBC);
            statement = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    // Запрос в БД
    public static String getNickByLoginPass(String login, String pass) {

        String result = null;

        try {
            ResultSet rs = statement.executeQuery(String.format(
                    "select nickname from users\n" +
                    "where login = '%s' and password = '%s'",
                    login, pass));

            if(rs.next()) {
                result = rs.getString("nickname");
            }

        } catch (SQLException e) {e.printStackTrace();}

        return result;
    }
}
