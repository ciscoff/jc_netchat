package database;

import java.io.ByteArrayInputStream;
import java.sql.*;

import static utils.Share.JDBC_CLASS_NAME;
import static utils.Share.URL_JDBC;

public class JdbcController {

    public Connection connection;
    public Statement statement;

    private JdbcController() {
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

    private static class JdbcControllerHolder {
        public static JdbcController instance = new JdbcController();
    }

    public static JdbcController getJdbc() {
        return JdbcControllerHolder.instance;
    }

    // Отключиться от базы
    public void disconnect() {
        try {
            connection.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    // Выполнить SELECT
    public synchronized ResultSet executeQuery(String sql) {
        ResultSet rs = null;

        try {
            rs = statement.executeQuery(sql);
        } catch (SQLException e) {e.printStackTrace();}

        return rs;
    }

    // Выполнить INSERT
    public synchronized void executeUpdate(String sql) {
        try {
            statement.executeUpdate(sql);
        } catch (SQLException e) {e.printStackTrace();}
    }

    public synchronized void updateHistory(String sql, String sender, String receiver, byte[] message) {

        PreparedStatement prepStatement = null;

        try {
            prepStatement = connection.prepareStatement(sql);

            ByteArrayInputStream bais = new ByteArrayInputStream(message);
            prepStatement.setString(1, sender);
            prepStatement.setString(2, receiver);
            prepStatement.setBinaryStream(3, bais, message.length);
            prepStatement.executeUpdate();

        } catch (SQLException e) {e.printStackTrace();}

    }

}
