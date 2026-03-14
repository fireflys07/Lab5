package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

import java.util.List;
import java.util.Locale;

public class DilCalcCommand extends BaseCommand {
    private long cachedSeriesId;
    private double cachedStartConc;
    private String cachedUnit;

    public DilCalcCommand(Environment env) {
        super(env, false); // не интерактивная
    }

    @Override
    public String getName() {
        return "dil_calc";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 3) {
            throw new CommandException("формат: dil_calc <series_id> <start_conc> <unit>");
        }

        try {
            cachedSeriesId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("series_id не число", e);
        }

        try {
            cachedStartConc = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            throw new CommandException("start_conc не число", e);
        }

        cachedUnit = args[2];
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        try {
            List<Double> concentrations = env.getService().calculateConcentrations(cachedSeriesId, cachedStartConc);

            int stepNum = 1;
            for (double conc : concentrations) {
                System.out.printf("Step %d: %g %s%n", stepNum++, conc, cachedUnit);
            }
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
            if (msg.contains("series") || msg.contains("не найдена")) {
                throw new CommandException("series не найден");
            } else {
                throw new CommandException(e.getMessage());
            }
        }
    }

    @Override
    public String getHelp() {
        return "dil_calc <series_id> <start_conc> <unit> - посчитать концентрации по шагам";
    }
}
