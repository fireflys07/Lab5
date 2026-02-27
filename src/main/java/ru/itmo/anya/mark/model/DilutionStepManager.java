package ru.itmo.anya.mark.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DilutionStepManager {

    private final Map<Long, DilutionStep> storage = new HashMap<>();

    public void add(DilutionStep step) {
        if (step == null) {
            throw new IllegalArgumentException("Ошибка: нельзя добавить пустой шаг разбавления");
        }

        long id = step.getId();
        storage.put(id, step);
        System.out.println("Шаг разбавления успешно добавлен с ID: " + id);
    }

    public DilutionStep getById(long id) {
        return storage.get(id);
    }

    public Collection<DilutionStep> getAll() {
        return storage.values();
    }

    public void update(long id,
                       long seriesId,
                       int stepNumber,
                       double factor,
                       double finalQuantity,
                       FinalQuantityUnit finalUnit) {
        if (!storage.containsKey(id)) {
            System.out.println("Ошибка: шаг с ID " + id + " не найден");
            return;
        }

        DilutionStep stepToUpdate = storage.get(id);
        try {
            stepToUpdate.setSeriesId(seriesId);
            stepToUpdate.setStepNumber(stepNumber);
            stepToUpdate.setFactor(factor);
            stepToUpdate.setFinalQuantity(finalQuantity);
            stepToUpdate.setFinalUnit(finalUnit);

            System.out.println("Шаг с ID " + id + " успешно обновлён");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации при обновлении шага: " + e.getMessage());
        }
    }

    public void remove(long id) {
        if (storage.remove(id) != null) {
            System.out.println("Шаг с ID " + id + " удалён");
        } else {
            System.out.println("Ошибка: ID " + id + " не существует");
        }
    }
}

