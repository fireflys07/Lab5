package ru.itmo.anya.mark.model;

import java.time.Instant;

public class DilutionStep {

    private static long nextId = 1L;

    private long id;                  // назначается программой
    private long seriesId;            // > 0 — ссылка на DilutionSeries.id
    private int stepNumber;           // > 0
    private double factor;            // > 0
    private double finalQuantity;     // > 0
    private FinalQuantityUnit finalUnit; // не null
    private Instant createdAt;        // автоматически

    public DilutionStep(long seriesId, int stepNumber, double factor, double finalQuantity, FinalQuantityUnit finalUnit) {
        this.id = generateNextId();
        this.createdAt = Instant.now();

        setSeriesId(seriesId);
        setStepNumber(stepNumber);
        setFactor(factor);
        setFinalQuantity(finalQuantity);
        setFinalUnit(finalUnit);
    }

    private static long generateNextId() {
        return nextId++;
    }

    public long getId() {
        return id;
    }

    public long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(long seriesId) {
        if (seriesId <= 0) {
            throw new IllegalArgumentException("ID серии должен быть больше 0");
        }
        this.seriesId = seriesId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        if (stepNumber <= 0) {
            throw new IllegalArgumentException("Номер шага должен быть больше 0");
        }
        this.stepNumber = stepNumber;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Коэффициент разбавления должен быть больше 0");
        }
        this.factor = factor;
    }

    public double getFinalQuantity() {
        return finalQuantity;
    }

    public void setFinalQuantity(double finalQuantity) {
        if (finalQuantity <= 0) {
            throw new IllegalArgumentException("Итоговое количество должно быть больше 0");
        }
        this.finalQuantity = finalQuantity;
    }

    public FinalQuantityUnit getFinalUnit() {
        return finalUnit;
    }

    public void setFinalUnit(FinalQuantityUnit finalUnit) {
        if (finalUnit == null) {
            throw new IllegalArgumentException("Единицы измерения не могут быть null");
        }
        this.finalUnit = finalUnit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

