package ru.itmo.anya.mark.storage;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractFileStorage<T> implements CollectionStorage<T> {

    protected void ensureParentDirectoryExists(Path path) throws Exception {
        Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }
}