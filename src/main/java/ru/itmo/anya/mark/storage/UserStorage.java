package ru.itmo.anya.mark.storage;

import ru.itmo.anya.mark.model.User;
import java.nio.file.Path;
import java.util.Optional;

public interface UserStorage {
    void save(Iterable<User> users, Path path) throws Exception;
    Iterable<User> load(Path path) throws Exception;
    Optional<User> findByLogin(String login);
    boolean addUser(User user);
}