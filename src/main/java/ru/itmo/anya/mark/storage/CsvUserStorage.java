package ru.itmo.anya.mark.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.itmo.anya.mark.model.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// Вспомогательный класс для CSV сериализации
class UserCsvRecord {
    @JsonProperty("login")
    public String login;

    @JsonProperty("passwordHash")
    public String passwordHash;

    public UserCsvRecord() {}

    public UserCsvRecord(String login, String passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
    }
}

public class CsvUserStorage implements UserStorage {

    private final CsvMapper csvMapper;
    private final Map<String, User> users = new HashMap<>();

    public CsvUserStorage() {
        this.csvMapper = new CsvMapper();
        this.csvMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void save(Iterable<User> usersList, Path path) throws Exception {
        Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        List<UserCsvRecord> records = new ArrayList<>();
        for (User u : usersList) {
            records.add(new UserCsvRecord(u.getLogin(), u.getPasswordHash()));
        }

        CsvSchema schema = csvMapper.schemaFor(UserCsvRecord.class).withHeader();
        csvMapper.writer(schema).writeValue(path.toFile(), records);
    }

    @Override
    public Iterable<User> load(Path path) throws Exception {
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }

        CsvSchema schema = csvMapper.schemaFor(UserCsvRecord.class).withHeader();
        List<UserCsvRecord> records = csvMapper.readerFor(UserCsvRecord.class)
                .with(schema)
                .<UserCsvRecord>readValues(path.toFile())
                .readAll();

        users.clear();
        List<User> result = new ArrayList<>();
        for (UserCsvRecord r : records) {
            User u = new User(r.login, r.passwordHash, true);
            users.put(u.getLogin(), u);
            result.add(u);
        }
        return result;
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(login));
    }

    @Override
    public boolean addUser(User user) {
        if (users.containsKey(user.getLogin())) {
            return false;
        }
        users.put(user.getLogin(), user);
        return true;
    }
}