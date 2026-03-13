package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.cli.Environment;
import ru.itmo.anya.mark.model.DilutionSeries;

public final class DilSeriesShowCommand extends BaseCommand {

    public DilSeriesShowCommand(Environment env) {
        super(env);
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Ошибка: формат: dil_series_show <series_id>");
            return;
        }

        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Ошибки: series_id не число");
            return;
        }

        try {
            DilutionSeries s = env.getService().getSeries(id);
            int stepsCount = env.getService().listSteps(id).size();
            System.out.println("DilutionSeries #" + s.getId());
            System.out.println("steps: " + stepsCount);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибки: " + e.getMessage());
        }
    }

    @Override
    public String getHelp() {
        return "показать серию по id";
    }
}

