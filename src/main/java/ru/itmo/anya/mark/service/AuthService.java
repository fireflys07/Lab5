package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.model.User;
import ru.itmo.anya.mark.storage.UserRepository;

public class AuthService {
    private final UserRepository userRepository;
    private String currentUserLogin;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Регистрация
    public boolean register(String login, String password) {
        if (userRepository.findByLogin(login).isPresent()) {
            return false; // Уже существует
        }
        User user = new User(login, password);
        return userRepository.save(user);
    }

    // Вход
    public boolean login(String login, String password) {
        User user = userRepository.findByLogin(login).orElse(null);
        if (user != null && user.checkPassword(password)) {
            currentUserLogin = login;
            return true;
        }
        return false;
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
}