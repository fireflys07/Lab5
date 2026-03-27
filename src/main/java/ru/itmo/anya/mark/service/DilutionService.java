package ru.itmo.anya.mark.service;


import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;
import ru.itmo.anya.mark.storage.CollectionStorage;
import ru.itmo.anya.mark.storage.CsvCollectionStorage;
import ru.itmo.anya.mark.storage.FileValidator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DilutionService {

    private final SeriesCollectionManager seriesManager;
    private final DilutionStepManager stepManager;

    private final CollectionStorage<DilutionSeries> seriesStorage;
    private final CollectionStorage<DilutionStep> stepStorage;

    public DilutionService(SeriesCollectionManager seriesManager, DilutionStepManager stepManager) {
        this.seriesManager = seriesManager;
        this.stepManager = stepManager;

        this.seriesStorage = new CsvCollectionStorage<>(DilutionSeries.class);
        this.stepStorage = new CsvCollectionStorage<>(DilutionStep.class);
        FileValidator fileValidator = new FileValidator();
    }
    // 7) dil_calc – возвращает список концентраций по шагам
    public List<Double> calculateConcentrations(long seriesId, double startConc) {
        // Получаем серию
        DilutionSeries series = seriesManager.getById(seriesId);
        if (series == null) {
            throw new IllegalArgumentException("Series not found: " + seriesId);
        }

        // Получаем шаги серии
        List<DilutionStep> steps = stepManager.getStepsBySeriesId(seriesId);
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Series has no dilution steps");
        }

        // Сортируем шаги по номеру
        steps.sort((s1, s2) -> Integer.compare(s1.getStepNumber(), s2.getStepNumber()));

        // Рассчитываем концентрации
        List<Double> results = new ArrayList<>();
        double currentConc = startConc;

        for (DilutionStep step : steps) {
            currentConc = currentConc / step.getFactor();
            results.add(currentConc);
        }

        return results;
    }
    // 8) dil_step_update
    public void updateStepFactor(long stepId, double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Factor must be positive");
        }

        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }

        step.setFactor(factor);
    }

    public void updateStepFinalQuantity(long stepId, double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }

        step.setFinalQuantity(quantity);
    }
    // 9) dil_step_delete
    public DilutionStep getStepById(long stepId) {
        return stepManager.getById(stepId);
    }

    public void deleteStep(long stepId) {
        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }

        stepManager.remove(stepId);
        System.out.println("Шаг с ID " + stepId + " удалён из хранилища");
    }

    // 10) dil_export
    public String exportSeries(long seriesId) {
        DilutionSeries series = seriesManager.getById(seriesId);
        if (series == null) {
            throw new IllegalArgumentException("Серия с ID " + seriesId + " не найдена");
        }

        List<DilutionStep> steps = stepManager.getStepsBySeriesId(seriesId);

        StringBuilder sb = new StringBuilder();
        sb.append("=== Dilution Series #").append(seriesId).append(" ===\n");
        sb.append("Name: ").append(series.getName()).append("\n");
        sb.append("Owner: ").append(series.getOwnerUsername()).append("\n");

        if (series.getSourceType() != null) {
            sb.append("Source: ").append(series.getSourceType())
                    .append(" #").append(series.getSourceId()).append("\n");
        }

        sb.append("\nSteps:\n");
        sb.append(String.format("%-6s %-6s %-8s %-10s %s\n",
                "ID", "Step", "Factor", "Quantity", "Unit"));

        for (DilutionStep step : steps) {
            sb.append(String.format("%-6d %-6d %-8.2f %-10.2f %s\n",
                    step.getId(),
                    step.getStepNumber(),
                    step.getFactor(),
                    step.getFinalQuantity(),
                    step.getFinalUnit()));
        }

        return sb.toString();
    }

    // 1) dil_series_create – только валидация
    public DilutionSeries createSeries(String name, DilutionSourceType sourceType, long sourceId, String ownerUsername) {
        if (name == null || name.trim().isEmpty() || name.length() > 128) {
            throw new IllegalArgumentException("name: пустое или слишком длинное");
        }
        if (sourceType == null) {
            throw new IllegalArgumentException("sourceType: null");
        }
        if (sourceId <= 0) {
            throw new IllegalArgumentException("sourceId: должен быть > 0");
        }
        if (ownerUsername == null || ownerUsername.trim().isEmpty()) {
            ownerUsername = "SYSTEM";
        }

        long id = seriesManager.getSeriesNextID();
        DilutionSeries series = new DilutionSeries(id, Instant.now());
        series.setName(name);
        series.setSourceType(sourceType);
        series.setSourceId(sourceId);
        series.setOwnerUsername(ownerUsername);

        seriesManager.add(series);
        return series;
    }

    // 2) dil_series_list
    public List<DilutionSeries> listSeries() {
        return new ArrayList<>(seriesManager.getSeries());
    }

    // 3) dil_series_show
    public DilutionSeries getSeries(long seriesId) {
        DilutionSeries s = seriesManager.getById(seriesId);
        if (s == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        return s;
    }

    // 4) dil_step_add
    public DilutionStep addStep(long seriesId, int stepNumber, double factor, double finalQuantity, FinalQuantityUnit unit) {
        if (seriesManager.getById(seriesId) == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        if (stepNumber <= 0) {
            throw new IllegalArgumentException("stepNumber: должен быть > 0");
        }
        if (factor <= 0) {
            throw new IllegalArgumentException("factor: должен быть > 0");
        }
        if (finalQuantity <= 0) {
            throw new IllegalArgumentException("finalQuantity: должен быть > 0");
        }
        if (unit == null) {
            throw new IllegalArgumentException("не может быть: null");
        }

        long stepId = stepManager.getStepsNextID();
        DilutionStep step = new DilutionStep(stepId, seriesId, stepNumber, factor, finalQuantity, unit, Instant.now());
        stepManager.add(step);
        return step;
    }

    // 5) dil_step_list
    public List<DilutionStep> listSteps(long seriesId) {
        if (seriesManager.getById(seriesId) == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        List<DilutionStep> result = new ArrayList<>();
        for (DilutionStep st : stepManager.getSteps()) {
            if (st.getSeriesId() == seriesId) {
                result.add(st);
            }
        }

        return result;
    }

    // 6) dil_link_set
    public void linkSource(long seriesId, DilutionSourceType type, long sourceId) {
        DilutionSeries s = seriesManager.getById(seriesId);
        if (s == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        if (type == null) {
            throw new IllegalArgumentException("sourceType: неизвестный тип");
        }
        if (sourceId <= 0) {
            throw new IllegalArgumentException("sourceId: должен быть > 0");
        }
        s.setSourceType(type);
        s.setSourceId(sourceId);
    }

    public void saveToCsv(String basePath) throws Exception {
        Path base = Path.of(basePath);
        Path seriesPath = base.resolveSibling(base.getFileName() + "_series.csv");
        Path stepsPath = base.resolveSibling(base.getFileName() + "_step.csv");

        seriesStorage.save(seriesManager.getSeries(), seriesPath);
        stepStorage.save(stepManager.getSteps(), stepsPath);
    }

    public void loadFromCsv(String basePath) throws Exception {
        Path base = Path.of(basePath);
        Path seriesPath = base.resolveSibling(base.getFileName() + "_series.csv");
        Path stepsPath = base.resolveSibling(base.getFileName() + "_step.csv");

        // Проверяем файлы
        if (!Files.exists(seriesPath)) {
            throw new Exception("Файл не найден: " + seriesPath);
        }
        if (!Files.exists(stepsPath)) {
            throw new Exception("Файл не найден: " + stepsPath);
        }

        // Загружаем
        List<DilutionSeries> loadedSeries = seriesStorage.load(seriesPath);
        List<DilutionStep> loadedSteps = stepStorage.load(stepsPath);

        // Проверяем целостность
        Set<Long> seriesIds = new HashSet<>();
        for (DilutionSeries s : loadedSeries) {
            seriesIds.add(s.getId());
        }

        for (DilutionStep step : loadedSteps) {
            if (!seriesIds.contains(step.getSeriesId())) {
                throw new Exception("Шаг id=" + step.getId() +
                        " ссылается на несуществующую серию: seriesId=" + step.getSeriesId());
            }
        }

        // Заменяем данные
        seriesManager.clear();
        stepManager.clear();

        seriesManager.addAll(loadedSeries);
        stepManager.addAll(loadedSteps);
    }
}
