package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.model.User;
import ru.itmo.anya.mark.storage.UserRepository;

public class AuthService {
    private final UserRepository userRepository;
    private String currentUserLogin;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(String login, String password) throws Exception {
        try {
            // Проверяем, существует ли пользователь
            if (userRepository.findByLogin(login).isPresent()) {
                return false; // Действительно существует
            }
            // Создаём нового
            User user = new User(login, password);
            return userRepository.save(user);
        } catch (Exception e) {
            // Пробрасываем ошибку БД
            throw new Exception("Ошибка при регистрации: " + e.getMessage(), e);
        }
    }

    public boolean login(String login, String password) throws Exception {
        try {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && user.checkPassword(password)) {
                currentUserLogin = login;
                return true;
            }
            return false; // Неверный пароль
        } catch (Exception e) {
            // Пробрасываем ошибку БД
            throw new Exception("Ошибка при входе: " + e.getMessage(), e);
        }
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