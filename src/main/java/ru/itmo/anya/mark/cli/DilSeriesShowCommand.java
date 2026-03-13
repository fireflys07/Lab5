package ru.itmo.anya.mark.cli;

import ru.itmo.anya.mark.model.DilutionSeries;

public final class DilSeriesShowCommand extends BaseCommand {

    public DilSeriesShowCommand(Environment env) {
        super(env, false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("формат: dil_series_show <series_id>");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("series_id не число");
        }
    }

    @Override
    public void execute(String[] args) throws CommandException {
        long id = Long.parseLong(args[0]);

        try {
            DilutionSeries s = env.getService().getSeries(id);
            int stepsCount = env.getService().listSteps(id).size();
            System.out.println("DilutionSeries #" + s.getId());
            System.out.println("steps: " + stepsCount);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public String getHelp() {
        return "показать серию по id";
    }
}

