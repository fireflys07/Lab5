package ru.itmo.anya.mark.model;

import java.time.Instant;
import java.util.Objects;

public final class DilutionSeries {
    // Уникальный номер серии разбавлений. Программа назначает сама.
    private final long id;
    // Название серии (например "Nitrate 1:10 series"). Нельзя пустое. До 128 символов.
    private String name;
    // Тип источника (SAMPLE или SOLUTION).
    private DilutionSourceType sourceType;
    // ID источника (sampleId или solutionId, в зависимости от sourceType).
    private long sourceId;
    // Кто создал серию (логин). На ранних этапах можно "SYSTEM".
    private String ownerUsername;
    // Когда создана. Программа ставит автоматически.
    private final Instant createdAt;
    // Когда обновляли. Программа обновляет автоматически.
    private Instant updatedAt;

    public DilutionSeries(long id, String name, DilutionSourceType sourceType, long sourceId, String ownerUsername, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.setName(name);
        this.setSourceType(sourceType);
        this.setSourceId(sourceId);
        this.setOwnerUsername(ownerUsername);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public DilutionSeries(long id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DilutionSourceType getSourceType() {
        return sourceType;
    }

    public long getSourceId() {
        return sourceId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setName(String name) {
        if (name != null && !name.isEmpty() && name.length() < 128) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Название серии не может быть пустым или длиннее 128 символов");
        }
    }

    public void setSourceType(DilutionSourceType sourceType) {
        if (sourceType != null) {
            this.sourceType = sourceType;
        } else {
            throw new IllegalArgumentException("Тип источника не может быть null");
        }
    }

    public void setSourceId(long sourceId) {
        if (sourceId > 0) {
            this.sourceId = sourceId;
        } else {
            throw new IllegalArgumentException("ID источника должен быть больше 0");
        }
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public void setUpdatedAt(Instant updatedAt) {
        if (ownerUsername != null || !ownerUsername.trim().isEmpty()) {
            this.updatedAt = updatedAt;
        } else {
            throw new IllegalArgumentException("Имя владельца серии не может быть пустым");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DilutionSeries that = (DilutionSeries) o;
        return id == that.id && sourceId == that.sourceId && Objects.equals(name, that.name) && sourceType == that.sourceType && Objects.equals(ownerUsername, that.ownerUsername) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, sourceType, sourceId, ownerUsername, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "DilutionSeries{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sourceType=" + sourceType +
                ", sourceId=" + sourceId +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
