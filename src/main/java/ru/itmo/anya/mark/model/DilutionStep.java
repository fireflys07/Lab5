package ru.itmo.anya.mark.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public final class DilutionStep implements Serializable {
    // Уникальный номер шага. Программа назначает сама.
    public final long id;
    // К какой серии относится (id серии).
    //Должен ссылаться на реально существующий DilutionSeries.
    public long seriesId;
    // Номер шага (1, 2, 3...). Должен быть > 0
    public int stepNumber;
    // Коэффициент разбавления (например 10 означает “в 10 раз”). Должен быть > 0
    public double factor;
    // Итоговый объём/масса на этом шаге (например 100 mL). Должен быть > 0
    public double finalQuantity;
    // Единицы итогового количества (обычно mL).
    public FinalQuantityUnit finalUnit;
    // Когда шаг добавлен. Программа ставит автоматически.
    public final Instant createdAt;

    public DilutionStep(long id, long seriesId, int stepNumber, double factor, double finalQuantity, FinalQuantityUnit finalUnit, Instant createdAt) {
        this.id = id;
        this.seriesId = seriesId;
        this.setStepNumber(stepNumber);
        this.setFactor(factor);
        this.setFinalQuantity(finalQuantity);
        this.finalUnit = finalUnit;
        this.createdAt = createdAt;
    }

    public DilutionStep(long id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getSeriesId() {
        return seriesId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public double getFactor() {
        return factor;
    }

    public double getFinalQuantity() {
        return finalQuantity;
    }

    public FinalQuantityUnit getFinalUnit() {
        return finalUnit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setSeriesId(long seriesId) {
        this.seriesId = seriesId;
    }

    public void setStepNumber(int stepNumber) {
        if (stepNumber > 0) {
            this.stepNumber = stepNumber;
        } else {
            throw new IllegalArgumentException("ID серии должен быть больше 0");
        }
    }

    public void setFactor(double factor) {
        if (factor > 0) {
            this.factor = factor;
        } else {
            throw new IllegalArgumentException("ID серии должен быть больше 0");
        }
    }

    public void setFinalQuantity(double finalQuantity) {
        if (finalQuantity > 0) {
            this.finalQuantity = finalQuantity;
        } else {
            throw new IllegalArgumentException("ID серии должен быть больше 0");
        }
    }

    public void setFinalUnit(FinalQuantityUnit finalUnit) {
        this.finalUnit = finalUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DilutionStep that = (DilutionStep) o;
        return id == that.id && seriesId == that.seriesId && stepNumber == that.stepNumber && Double.compare(factor, that.factor) == 0 && Double.compare(finalQuantity, that.finalQuantity) == 0 && finalUnit == that.finalUnit && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seriesId, stepNumber, factor, finalQuantity, finalUnit, createdAt);
    }

    @Override
    public String toString() {
        return "DilutionStep{" +
                "id=" + id +
                ", seriesId=" + seriesId +
                ", stepNumber=" + stepNumber +
                ", factor=" + factor +
                ", finalQuantity=" + finalQuantity +
                ", finalUnit=" + finalUnit +
                ", createdAt=" + createdAt +
                '}';
    }
}