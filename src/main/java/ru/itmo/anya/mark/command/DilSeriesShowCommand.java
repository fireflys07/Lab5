package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;
import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.validation.ValidationException;
import ru.itmo.anya.mark.validation.Validators;

public final class DilSeriesShowCommand extends BaseCommand {

    private long cachedSeriesId;

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
            cachedSeriesId = Validators.validateId(args[0]);
        } catch (ValidationException e) {
            throw new CommandException(e.getMessage());
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
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    public String getHelp() {
        return "показать серию по id";
    }
}

