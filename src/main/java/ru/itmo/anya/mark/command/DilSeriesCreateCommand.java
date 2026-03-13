package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.cli.Environment;
import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;

public final class DilSeriesCreateCommand extends BaseCommand {

    public DilSeriesCreateCommand(Environment env) {
        super(env, true);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 0) {
            throw new CommandException("команда dil_series_create не принимает аргументы");
        }
    }

    @Override
    public void readAdditionalInput(Environment env) throws CommandException {
        System.out.print("Название: ");
        if (!env.getScanner().hasNextLine()) {
            throw new CommandException("не удалось прочитать название");
        }
    }

    @Override
    public void execute(String[] args) throws CommandException {
        String name = env.getScanner().nextLine();
        try {
            DilutionSeries series = env.getService().createSeries(name, DilutionSourceType.SAMPLE, 1L, "SYSTEM");
            System.out.println("OK series_id=" + series.getId());
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public String getHelp() {
        return "создать серию разбавлений";
    }
}

