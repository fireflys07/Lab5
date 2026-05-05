package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;
import ru.itmo.anya.mark.storage.StepRepository;
import java.util.*;
import java.util.stream.Collectors;

public class DilutionStepManager {
    private final Map<Long, DilutionStep> storage = new LinkedHashMap<>();
    private final StepRepository repository;

    public DilutionStepManager(StepRepository repository) {
        this.repository = repository;
    }

    // Загрузка из БД при старте
    public void loadFromDatabase() {
        storage.clear();
        List<DilutionStep> allFromDb = repository.findAll();
        for (DilutionStep s : allFromDb) {
            storage.put(s.getId(), s);
        }
        System.out.println("Шаги загружены из БД: " + storage.size());
    }

    public DilutionStep add(DilutionStep step) {
        if (step == null) throw new IllegalArgumentException("Пустой шаг");

        long generatedId = repository.save(step);
        if (generatedId == -1) throw new RuntimeException("Ошибка сохранения шага");

        DilutionStep savedStep = repository.getById(generatedId);
        storage.put(savedStep.getId(), savedStep);
        return savedStep;
    }

    public DilutionStep getById(long id) {
        return storage.get(id);
    }

    public List<DilutionStep> getStepsBySeriesId(long seriesId) {
        return storage.values().stream()
                .filter(step -> step.getSeriesId() == seriesId)
                .collect(Collectors.toList());
    }

    public void update(long id, DilutionStep newData) {
        if (!storage.containsKey(id)) return;
        DilutionStep existing = storage.get(id);
        existing.setStepNumber(newData.getStepNumber());
        existing.setFactor(newData.getFactor());
        existing.setFinalQuantity(newData.getFinalQuantity());
        existing.setFinalUnit(newData.getFinalUnit());
        repository.save(existing);
    }

    public void remove(long id) {
        storage.remove(id);
        repository.delete(id);
    }

    public Collection<DilutionStep> getAll() {
        return storage.values();
    }

    public List<DilutionStep> getSteps() {
        return new ArrayList<>(storage.values());
    }

    public void clear() { storage.clear(); }
}