package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.cli.Environment;
import ru.itmo.anya.mark.model.DilutionSeries;

import java.util.List;

public final class DilSeriesListCommand extends BaseCommand {

    public DilSeriesListCommand(Environment env) {
        super(env);
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            System.out.println("Ошибка: команда dil_series_list не принимает аргументы");
            return;
        }

        List<DilutionSeries> list = env.getService().listSeries();
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

