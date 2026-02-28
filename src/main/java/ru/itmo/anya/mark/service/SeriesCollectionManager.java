package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.model.DilutionSeries;
import java.util.*;

public class SeriesCollectionManager {
    private final Map<Long, DilutionSeries> storage = new LinkedHashMap<>();

    public void add(DilutionSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Ошибка: нельзя добавить пустую серию разбавлений");
        }

        if (storage.containsKey(series.getId())) {
            throw new IllegalArgumentException("Серия с ID " + series.getId() + " уже существует");
        }

        if (series.getOwnerUsername() == null || series.getOwnerUsername().trim().isEmpty()) {
            series.setOwnerUsername("SYSTEM");
        }

        storage.put(series.getId(), series);
        System.out.println("Серия разбавлений успешно добавлена с ID: " + series.getId());
    }

    public DilutionSeries getById ( long id){
            return storage.get(id);
    }

    public Collection<DilutionSeries> getAll () {
            return storage.values();
    }

    public void update ( long id, DilutionSeries newData){
        // Проверяем, существует ли серия
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

    public long getSeriesNextID() {
        return System.currentTimeMillis()+ storage.size();
    }

    public List<DilutionSeries> getSeries() {
        return new ArrayList<>(storage.values());
    }
}