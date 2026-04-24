package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.model.User;
import ru.itmo.anya.mark.storage.UserStorage;

import java.nio.file.Path;

public class AuthService {

    private final UserStorage userStorage;
    private String currentUserLogin;

    public AuthService(UserStorage userStorage, Path usersFilePath) {
        this.userStorage = userStorage;
        try {
            userStorage.load(usersFilePath);
        } catch (Exception e) {
            // Файл не существует или пустой — это нормально
        }
    }

    public boolean register(String login, String password) {
        if (login == null || login.trim().isEmpty()) return false;
        if (password == null || password.length() < 4) return false;

        User user = new User(login, password);
        return userStorage.addUser(user);
    }

    public boolean login(String login, String password) {
        boolean success = userStorage.findByLogin(login)
                .filter(user -> user.checkPassword(password))
                .isPresent();

        if (success) {
            currentUserLogin = login;
        }
        return success;
    }

    public void logout() {
        currentUserLogin = null;
    }

    public boolean isAuthenticated() {
        return currentUserLogin != null;
    }

    public String getCurrentUser() {
        return currentUserLogin;
    }

    public void saveUsers(Path path) throws Exception {
        userStorage.save(userStorage.load(path), path);
    }

    public void reloadUsers(Path path) throws Exception {
        userStorage.load(path);
    }
}