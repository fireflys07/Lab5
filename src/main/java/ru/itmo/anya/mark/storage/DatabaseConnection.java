package ru.itmo.anya.mark.storage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private final String url, username, password;

    private DatabaseConnection() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) throw new RuntimeException("Не найден database.properties");
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения конфига: " + e.getMessage(), e);
        }
        this.url = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println(" БД подключена: " + conn.getMetaData().getURL());
        } catch (SQLException e) {
            System.err.println(" Ошибка подключения к БД: " + e.getMessage());
        }
    }
}