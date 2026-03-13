package ru.itmo.anya.mark.cli;

import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;

import java.util.Scanner;

public final class DilSeriesCreateCommand extends BaseCommand {

    public DilSeriesCreateCommand(Scanner scanner, DilutionService service) {
        super(scanner, service);
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            System.out.println("Ошибка: команда dil_series_create не принимает аргументы");
            return;
        }

        System.out.print("Название: ");
        if (!scanner.hasNextLine()) {
            System.out.println("Ошибка: не удалось прочитать название");
            return;
        }
        String name = scanner.nextLine();
        try {
            DilutionSeries series = service.createSeries(name, DilutionSourceType.SAMPLE, 1L, "SYSTEM");
            System.out.println("OK series_id=" + series.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибки: " + e.getMessage());
        }
    }

    @Override
    public String getHelp() {
        return "создать серию разбавлений";
    }
}

