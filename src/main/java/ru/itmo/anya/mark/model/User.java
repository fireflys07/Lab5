package ru.itmo.anya.mark.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String login;
    private final String passwordHash;

    public User(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Пароль должен быть не менее 4 символов");
        }
        this.login = login.trim();
        this.passwordHash = hashPassword(password);
    }

    // Для десериализации из CSV
    public User(String login, String passwordHash, boolean isHashed) {
        this.login = login;
        this.passwordHash = isHashed ? passwordHash : hashPassword(passwordHash);
    }

    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }

    public boolean checkPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "User{login='" + login + "'}";
    }
}