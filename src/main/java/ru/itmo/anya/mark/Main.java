package ru.itmo.anya.mark;

import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.service.DilutionStepManager;
import ru.itmo.anya.mark.service.SeriesCollectionManager;
import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SeriesCollectionManager seriesCollection = new SeriesCollectionManager();
        long seriesNextID = seriesCollection.getSeriesNextID();
        DilutionSeries serie = new DilutionSeries(seriesNextID, Instant.now());

        seriesCollection.add(serie);
        List<DilutionSeries> series = seriesCollection.getSeries();
        series.forEach(System.out::println);

        DilutionStepManager stepsCollection = new DilutionStepManager();
        long stepsNextID = stepsCollection.getStepsNextID();
        DilutionStep step = new DilutionStep(stepsNextID, Instant.now());

        stepsCollection.add(step);
        List<DilutionStep> steps = stepsCollection.getSteps();
        steps.forEach(System.out::println);
    }
}
