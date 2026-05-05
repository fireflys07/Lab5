package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.storage.SeriesRepository;
import java.util.*;

public class SeriesCollectionManager {
    private final Map<Long, DilutionSeries> storage = new LinkedHashMap<>();
    private final SeriesRepository repository; // Используем репозиторий

    public SeriesCollectionManager(SeriesRepository repository) {
        this.repository = repository;
    }

    // Метод загрузки из БД в память (вызывается при старте)
    public void loadFromDatabase() {
        storage.clear();
        List<DilutionSeries> allFromDb = repository.findAll();
        for (DilutionSeries s : allFromDb) {
            storage.put(s.getId(), s);
        }
        System.out.println("Серии загружены из БД: " + storage.size());
    }

    public DilutionSeries add(DilutionSeries series) {
        if (series == null) throw new IllegalArgumentException("Ошибка: пустая серия");

        // Сохраняем в БД и получаем сгенерированный ID
        long generatedId = repository.save(series);

        if (generatedId == -1) {
            throw new RuntimeException("Не удалось сохранить серию в БД");
        }

        // Создаем новый объект с настоящим ID из БД (т.к. id в модели final)
        // Но чтобы не усложнять, мы просто обновим объект в мапе, если ID совпал,
        // или положим новый.
        // В данном случае repository.save уже вернул ID, нам нужно обновить объект в памяти.
        // Проще всего перезапросить из БД или обновить текущий.

        DilutionSeries savedSeries = repository.getById(generatedId);
        storage.put(savedSeries.getId(), savedSeries);

        return savedSeries;
    }

    public DilutionSeries getById(long id) {
        return storage.get(id);
    }

    public Collection<DilutionSeries> getAll() {
        return storage.values();
    }

    public void update(long id, DilutionSeries newData) {
        if (!storage.containsKey(id)) return;

        DilutionSeries existing = storage.get(id);
        existing.setName(newData.getName());
        existing.setSourceType(newData.getSourceType());
        existing.setSourceId(newData.getSourceId());
        // owner менять нельзя обычно, но если нужно: existing.setOwnerUsername(...)
        existing.setUpdatedAt(java.time.Instant.now());

        repository.save(existing); // Сохраняем изменения в БД
    }

    public void remove(long id) {
        storage.remove(id);
        repository.delete(id); // Удаляем из БД (каскадно удалит и шаги)
    }

    public List<DilutionSeries> getSeries() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }
}