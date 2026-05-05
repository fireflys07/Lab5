package ru.itmo.anya.mark.storage;

import ru.itmo.anya.mark.model.User;
import java.sql.*;
import java.util.*;

public class UserRepository {

    public boolean save(User user) {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?) " +
                "ON CONFLICT (login) DO UPDATE SET password_hash = EXCLUDED.password_hash";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPasswordHash());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка сохранения пользователя: " + e.getMessage());
            return false;
        }
    }

    public Optional<User> findByLogin(String login) {
        String sql = "SELECT login, password_hash FROM users WHERE login = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User(rs.getString("login"), rs.getString("password_hash"), true);
                    return Optional.of(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка поиска пользователя: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT login, password_hash FROM users ORDER BY login";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getString("login"), rs.getString("password_hash"), true));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки пользователей: " + e.getMessage());
        }
        return users;
    }

    public boolean existsByLogin(String login) {
        String sql = "SELECT COUNT(*) FROM users WHERE login = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}