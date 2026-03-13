package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;
import ru.itmo.anya.mark.model.DilutionSeries;

public final class DilSeriesShowCommand extends BaseCommand {

    public DilSeriesShowCommand(Environment env) {
        super(env, false);
    }

    @Override
    public String getName() {
        return "dil_series_command";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("формат: dil_series_show <series_id>");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("series_id не число", e);
        }
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        long id = Long.parseLong(args[0]);

        try {
            DilutionSeries s = env.getService().getSeries(id);
            int stepsCount = env.getService().listSteps(id).size();
            System.out.println("DilutionSeries #" + s.getId());
            System.out.println("steps: " + stepsCount);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e;
        }
    }

    @Override
    public String getHelp() {
        return "показать серию по id";
    }
}

