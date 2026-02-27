package ru.itmo.anya.mark.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CollectionManager {

    private Map<Long, DilutionSeries> storage = new HashMap<>();

    public void add(DilutionSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Ошибка: нельзя добавить пустую серию разбавлений");
        }

        long id = series.getId();
        storage.put(id, series);
        System.out.println("Серия разбавлений успешно добавлена с ID: " + id);
    }

    public DilutionSeries getById(long id) {
        return storage.get(id);
    }

    public Collection<DilutionSeries> getAll() {
        return storage.values();
    }

    public void update(long id, DilutionSeries newData) {
        if (!storage.containsKey(id)) {
            System.out.println("Ошибка: серия с ID " + id + " не найдена");
            return;
        }

        DilutionSeries seriesToUpdate = storage.get(id);
        try {
            seriesToUpdate.setName(newData.getName());
            seriesToUpdate.setSourceType(newData.getSourceType());
            seriesToUpdate.setSourceId(newData.getSourceId());
            seriesToUpdate.setOwnerUsername(newData.getOwnerUsername());

            System.out.println("Серия с ID " + id + " успешно обновлена");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации при обновлении: " + e.getMessage());
        }
    }

    public void remove(long id) {
        if (storage.remove(id) != null) {
            System.out.println("Серия с ID " + id + " удалена");
        } else {
            System.out.println("Ошибка: ID " + id + " не существует");
        }
    }
}

