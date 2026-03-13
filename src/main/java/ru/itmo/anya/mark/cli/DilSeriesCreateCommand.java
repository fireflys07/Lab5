package ru.itmo.anya.mark.cli;

import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;

public final class DilSeriesCreateCommand extends BaseCommand {

    public DilSeriesCreateCommand(Environment env) {
        super(env);
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            System.out.println("Ошибка: команда dil_series_create не принимает аргументы");
            return;
        }

        System.out.print("Название: ");
        if (!env.getScanner().hasNextLine()) {
            System.out.println("Ошибка: не удалось прочитать название");
            return;
        }
        String name = env.getScanner().nextLine();
        try {
            DilutionSeries series = env.getService().createSeries(name, DilutionSourceType.SAMPLE, 1L, "SYSTEM");
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

