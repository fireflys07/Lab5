package ru.itmo.anya.mark.model;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DilutionSeriesInputHandler inputHandler = new DilutionSeriesInputHandler();

        String name = inputHandler.askName(scanner);
        DilutionSourceType sourceType = inputHandler.askSourceType(scanner);
        long sourceId = inputHandler.askSourceId(scanner);
        String ownerUsername = inputHandler.askOwnerUsername(scanner);

        DilutionSeries series = new DilutionSeries(name, sourceType, sourceId, ownerUsername);

        System.out.println("Создана серия разбавлений с ID: " + series.getId());

        DilutionStep step1 = new DilutionStep(series.getId(), 1, 10.0, 100.0, FinalQuantityUnit.ML);

        System.out.println("Создан шаг разбавления с ID: " + step1.getId() + ", номер шага: " + step1.getStepNumber());

        scanner.close();
    }
}
