package ru.itmo.anya.mark.cli;

import ru.itmo.anya.mark.model.DilutionSeries;

import java.util.List;
import java.util.Scanner;

public final class DilSeriesListCommand extends BaseCommand {

    public DilSeriesListCommand(Scanner scanner, DilutionService service) {
        super(scanner, service);
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            System.out.println("Ошибка: команда dil_series_list не принимает аргументы");
            return;
        }

        List<DilutionSeries> list = service.listSeries();
        System.out.println("ID Name");
        for (DilutionSeries s : list) {
            System.out.println(s.getId() + " " + s.getName());
        }
    }

    @Override
    public String getHelp() {
        return "список серий";
    }
}

