package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;

import java.util.List;
import java.util.Locale;

public final class DilStepListCommand extends BaseCommand {

    public DilStepListCommand(Environment env) {
        super(env, false);
    }

    @Override
    public String getName() {
        return "dil_step_list";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("формат: dil_step_list <series_id>");
        }

        long seriesId;
        try {
            seriesId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("series_id не число", e);
        }
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        long seriesId = Long.parseLong(args[0]);

        try {
            List<DilutionStep> steps = env.getService().listSteps(seriesId);
            System.out.println("ID Step Factor FinalQty Unit");
            for (DilutionStep st : steps) {
                System.out.println(
                        st.getId() + " " +
                                st.getStepNumber() + " " +
                                formatNumber(st.getFactor()) + " " +
                                formatNumber(st.getFinalQuantity()) + " " +
                                formatUnit(st.getFinalUnit())
                );
            }
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
            if (msg.contains("series")) {
                throw new CommandException("series не найден");
            } else {
                throw new CommandException(e.getMessage());
            }
        }
    }

    private static String formatUnit(FinalQuantityUnit unit) {
        if (unit == null) {
            return "";
        }
        return switch (unit) {
            case ML -> "mL";
            case L -> "L";
            case G -> "g";
            case MG -> "mg";
        };
    }

    private static String formatNumber(double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    @Override
    public String getHelp() {
        return "список шагов серии";
    }
}

