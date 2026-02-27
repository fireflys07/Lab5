package ru.itmo.anya.mark.model;

import java.time.Instant;

public class DilutionSeries {

    private static long nextId = 1L;

    private long id;                         // назначается программой
    private String name;                     // не пустое, <= 128 символов
    private DilutionSourceType sourceType;   // SAMPLE/SOLUTION
    private long sourceId;                   // > 0
    private String ownerUsername;            // не пустое, можно "SYSTEM"
    private Instant createdAt;               // ставится автоматически
    private Instant updatedAt;               // обновляется при изменениях

    public DilutionSeries(String name, DilutionSourceType sourceType, long sourceId, String ownerUsername) {
        this.id = generateNextId();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;

        setName(name);
        setSourceType(sourceType);
        setSourceId(sourceId);
        setOwnerUsername(ownerUsername);
    }

    private static long generateNextId() {
        return nextId++;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название серии не может быть пустым");
        }
        if (name.length() > 128) {
            throw new IllegalArgumentException("Название серии не может быть длиннее 128 символов");
        }
        this.name = name;
        touchUpdatedAt();
    }

    public DilutionSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(DilutionSourceType sourceType) {
        if (sourceType == null) {
            throw new IllegalArgumentException("Тип источника не может быть null");
        }
        this.sourceType = sourceType;
        touchUpdatedAt();
    }

    public long getSourceId() {
        return sourceId;
    }

    public void setSourceId(long sourceId) {
        if (sourceId <= 0) {
            throw new IllegalArgumentException("ID источника должен быть больше 0");
        }
        this.sourceId = sourceId;
        touchUpdatedAt();
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        if (ownerUsername == null || ownerUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя владельца серии не может быть пустым");
        }
        this.ownerUsername = ownerUsername;
        touchUpdatedAt();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}

