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

    public DilutionService(SeriesCollectionManager seriesManager, DilutionStepManager stepManager) {
        this.seriesManager = seriesManager;
        this.stepManager = stepManager;
    }

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

    // 7) dil_calc – возвращает список концентраций по шагам
    public List<Double> calcConcentrations(long seriesId, double startConc) {
        // Проверяем, что серия существует
        getSeries(seriesId);

        if (startConc <= 0) {
            throw new IllegalArgumentException("start_conc: должен быть > 0");
        }

        List<DilutionStep> steps = listSteps(seriesId);
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("у серии нет шагов");
        }

        // сортируем шаги по номеру, чтобы концентрации считались последовательно
        steps.sort(Comparator.comparingInt(DilutionStep::getStepNumber));

        List<Double> concentrations = new ArrayList<>();
        double current = startConc;
        for (DilutionStep step : steps) {
            current = current / step.getFactor();
            concentrations.add(current);
        }

        return concentrations;
    }

    // 8) dil_step_update
    public void updateStep(long stepId, Double factor, Double finalQuantity) {
        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("step: не найден");
        }
        if (factor != null) {
            if (factor <= 0) {
                throw new IllegalArgumentException("factor <= 0");
            }
            step.setFactor(factor);
        }
        if (finalQuantity != null) {
            if (finalQuantity <= 0) {
                throw new IllegalArgumentException("finalQuantity <= 0");
            }
            step.setFinalQuantity(finalQuantity);
        }
    }
}
