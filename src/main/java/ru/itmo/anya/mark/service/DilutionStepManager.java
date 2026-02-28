package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;

import java.io.*;
import java.util.*;

public class DilutionStepManager {

    private final Map<Long, DilutionStep> storage = new LinkedHashMap<>();
    private long currentId = 1;
    private static final String SAVE_FILE = "steps.dat";

    // Сохранить все данные в файл
    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(storage);
            oos.writeLong(currentId);
            System.out.println("Данные сохранены в файл " + SAVE_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении: " + e.getMessage());
        }
    }

    // Загрузить данные из файла
    @SuppressWarnings("unchecked")
    public void loadFromFile() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            System.out.println("Файл сохранения не найден, создаем новую коллекцию");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(SAVE_FILE))) {
            storage.clear();
            storage.putAll((Map<Long, DilutionStep>) ois.readObject());
            currentId = ois.readLong();
            System.out.println("Данные загружены из файла. Всего шагов: " + storage.size());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при загрузке: " + e.getMessage());
        }
    }

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

    public long getStepsNextID() {
        return System.currentTimeMillis() + storage.size();
    }

    public List<DilutionStep> getSteps() {
        return new ArrayList<>(storage.values());
    }

    public void addAndSave(DilutionStep step) {
        // Сначала добавляем в коллекцию
        add(step);

        // Потом сохраняем все данные в файл
        saveToFile();

        System.out.println("✅ Шаг добавлен и данные сохранены в файл");
    }
}

