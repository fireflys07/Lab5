package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DilutionService {

    private final SeriesCollectionManager seriesManager;
    private final DilutionStepManager stepManager;
    private final AuthService authService;

    public DilutionService(SeriesCollectionManager seriesManager,
                           DilutionStepManager stepManager,
                           AuthService authService) {
        this.seriesManager = seriesManager;
        this.stepManager = stepManager;
        this.authService = authService;
    }

    // Проверка прав на запись
    private void checkWriteAccess(String ownerUsername) {
        if (!authService.isAuthenticated()) {
            throw new SecurityException("Требуется авторизация для этой операции");
        }
        String current = authService.getCurrentUser();
        if (!current.equals(ownerUsername)) {
            throw new SecurityException(
                    "Нет прав: объект принадлежит пользователю " + ownerUsername);
        }
    }

    // Проверка прав на владение
    private void checkOwnership(String ownerUsername) {
        if (!authService.isAuthenticated()) {
            throw new SecurityException("Требуется авторизация");
        }
        String currentUser = authService.getCurrentUser();
        if (!currentUser.equals(ownerUsername)) {
            throw new SecurityException(
                    "Ошибка: у вас нет прав на изменение этого объекта. " +
                            "Владелец: " + ownerUsername);
        }
    }

    // ==================== SERIES OPERATIONS ====================

    public DilutionSeries createSeries(String name, DilutionSourceType sourceType,
                                       long sourceId, String ownerUsername) {
        if (!authService.isAuthenticated()) {
            throw new SecurityException("Требуется авторизация для создания серии");
        }
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

        DilutionSeries series = new DilutionSeries(0, Instant.now());
        series.setName(name);
        series.setSourceType(sourceType);
        series.setSourceId(sourceId);
        series.setOwnerUsername(ownerUsername);

        return seriesManager.add(series);
    }

    public List<DilutionSeries> listSeries() {
        return new ArrayList<>(seriesManager.getSeries());
    }

    public DilutionSeries getSeries(long seriesId) {
        DilutionSeries s = seriesManager.getById(seriesId);
        if (s == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        return s;
    }

    public void deleteSeries(long id) {
        DilutionSeries series = seriesManager.getById(id);
        if (series == null) {
            throw new IllegalArgumentException("Серия не найдена");
        }
        checkOwnership(series.getOwnerUsername());
        seriesManager.remove(id);
        // Шаги удалятся каскадно через БД (ON DELETE CASCADE)
    }

    public void updateSeries(long id, String newName, DilutionSourceType newType, long newSourceId) {
        DilutionSeries series = seriesManager.getById(id);
        if (series == null) {
            throw new IllegalArgumentException("Серия не найдена");
        }
        checkOwnership(series.getOwnerUsername());

        series.setName(newName);
        series.setSourceType(newType);
        series.setSourceId(newSourceId);
        series.setUpdatedAt(Instant.now());

        seriesManager.update(id, series);
    }

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
        checkOwnership(s.getOwnerUsername());

        s.setSourceType(type);
        s.setSourceId(sourceId);
        s.setUpdatedAt(Instant.now());
        seriesManager.update(seriesId, s);
    }

    // ==================== STEP OPERATIONS ====================

    public DilutionStep addStep(long seriesId, int stepNumber, double factor,
                                double finalQuantity, FinalQuantityUnit unit) {
        DilutionSeries series = seriesManager.getById(seriesId);
        if (series == null) {
            throw new IllegalArgumentException("Серия не найдена");
        }
        if (!authService.isAuthenticated()) {
            throw new SecurityException("Требуется авторизация для добавления шагов");
        }
        String currentUser = authService.getCurrentUser();
        if (!currentUser.equals(series.getOwnerUsername())) {
            throw new SecurityException(
                    "Нет прав: владелец серии — " + series.getOwnerUsername());
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
            throw new IllegalArgumentException("unit: не может быть null");
        }

        // Проверка на дубликат шага
        boolean exists = stepManager.getStepsBySeriesId(seriesId).stream()
                .anyMatch(s -> s.getStepNumber() == stepNumber);
        if (exists) {
            throw new IllegalArgumentException("Шаг с таким номером уже существует");
        }

        DilutionStep step = new DilutionStep(0, seriesId, stepNumber, factor,
                finalQuantity, unit, Instant.now());
        return stepManager.add(step);
    }

    public List<DilutionStep> listSteps(long seriesId) {
        if (seriesManager.getById(seriesId) == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        return stepManager.getStepsBySeriesId(seriesId).stream()
                .sorted(Comparator.comparingInt(DilutionStep::getStepNumber))
                .toList();
    }

    public DilutionStep getStepById(long stepId) {
        return stepManager.getById(stepId);
    }

    public void deleteStep(long stepId) {
        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }
        // Проверка прав через серию
        DilutionSeries series = seriesManager.getById(step.getSeriesId());
        if (series != null) {
            checkOwnership(series.getOwnerUsername());
        }
        stepManager.remove(stepId);
    }

    public void updateStepFactor(long stepId, double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Factor must be positive");
        }
        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }
        // Проверка прав
        DilutionSeries series = seriesManager.getById(step.getSeriesId());
        if (series != null) {
            checkOwnership(series.getOwnerUsername());
        }
        step.setFactor(factor);
        stepManager.update(stepId, step);
    }

    public void updateStepFinalQuantity(long stepId, double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }
        // Проверка прав
        DilutionSeries series = seriesManager.getById(step.getSeriesId());
        if (series != null) {
            checkOwnership(series.getOwnerUsername());
        }
        step.setFinalQuantity(quantity);
        stepManager.update(stepId, step);
    }

    // ==================== CALCULATIONS & EXPORT ====================

    public List<Double> calculateConcentrations(long seriesId, double startConc) {
        DilutionSeries series = seriesManager.getById(seriesId);
        if (series == null) {
            throw new IllegalArgumentException("Series not found: " + seriesId);
        }
        List<DilutionStep> steps = stepManager.getStepsBySeriesId(seriesId);
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Series has no dilution steps");
        }
        steps.sort(Comparator.comparingInt(DilutionStep::getStepNumber));

        List<Double> results = new ArrayList<>();
        double currentConc = startConc;
        for (DilutionStep step : steps) {
            currentConc = currentConc / step.getFactor();
            results.add(currentConc);
        }
        return results;
    }
    public void loadFromDatabase() {
        seriesManager.loadFromDatabase();
        stepManager.loadFromDatabase();
    }

    public String exportSeries(long seriesId) {
        DilutionSeries series = seriesManager.getById(seriesId);
        if (series == null) {
            throw new IllegalArgumentException("Серия с ID " + seriesId + " не найдена");
        }
        List<DilutionStep> steps = listSteps(seriesId);

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
                    step.getId(), step.getStepNumber(), step.getFactor(),
                    step.getFinalQuantity(), step.getFinalUnit()));
        }
        return sb.toString();
    }
}