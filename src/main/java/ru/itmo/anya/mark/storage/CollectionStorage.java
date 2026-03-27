package ru.itmo.anya.mark.storage;

import java.nio.file.Path;
import java.util.List;

public interface CollectionStorage<T> {
    void save(List<T> items, Path path) throws Exception;

    List<T> load(Path path) throws Exception;
}