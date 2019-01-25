package database;

import java.sql.*;

import static utils.Share.JDBC_CLASS_NAME;
import static utils.Share.URL_JDBC;

public class JdbcController {

    public Connection connection;
    public Statement statement;

    private JdbcController() {}

    private static class JdbcControllerHolder {
        public static JdbcController instance = new JdbcController();
    }

    public static JdbcController getIdbc() {
        return JdbcControllerHolder.instance;
    }

    public void connect() {
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

    public void disconnect() {
        try {
            connection.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    public synchronized ResultSet executeQuery(String sql) {
        ResultSet rs = null;

        try {
            rs = statement.executeQuery(sql);
        } catch (SQLException e) {e.printStackTrace();}

        return rs;
    }
}
