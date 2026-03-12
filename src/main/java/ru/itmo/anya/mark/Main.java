package ru.itmo.anya.mark;

import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.DilutionSourceType;
import ru.itmo.anya.mark.model.FinalQuantityUnit;
import ru.itmo.anya.mark.service.DilutionStepManager;
import ru.itmo.anya.mark.service.SeriesCollectionManager;

import java.time.Instant;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        SeriesCollectionManager seriesCollection = new SeriesCollectionManager();
        DilutionStepManager stepsCollection = new DilutionStepManager();

        // Создаем тестовую серию "Nitrate 1:10 series"
        long seriesId1 = seriesCollection.getSeriesNextID();
        DilutionSeries nitrateSeries = new DilutionSeries(
                seriesId1,
                "Nitrate 1:10 series",
                DilutionSourceType.SAMPLE,
                12L,
                "SYSTEM",
                Instant.now(),
                Instant.now()
        );
        seriesCollection.add(nitrateSeries);

        // Пара шагов для этой серии
        long stepId1 = stepsCollection.getStepsNextID();
        DilutionStep step1 = new DilutionStep(
                stepId1,
                seriesId1,
                1,
                10.0,
                100.0,
                FinalQuantityUnit.ML,
                Instant.now()
        );
        stepsCollection.add(step1);

        long stepId2 = stepsCollection.getStepsNextID();
        DilutionStep step2 = new DilutionStep(
                stepId2,
                seriesId1,
                2,
                10.0,
                100.0,
                FinalQuantityUnit.ML,
                Instant.now()
        );
        stepsCollection.add(step2);

        // Выводим стартовые данные
        List<DilutionSeries> series = seriesCollection.getSeries();
        series.forEach(System.out::println);

        List<DilutionStep> steps = stepsCollection.getSteps();
        steps.forEach(System.out::println);
    }
}
