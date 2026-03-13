package ru.itmo.anya.mark.cli;

import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class DilStepListCommand extends BaseCommand {

    public DilStepListCommand(Scanner scanner, DilutionService service) {
        super(scanner, service);
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Ошибка: формат: dil_step_list <series_id>");
            return;
        }

        long seriesId;
        try {
            seriesId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Ошибки: series_id не число");
            return;
        }

        try {
            List<DilutionStep> steps = service.listSteps(seriesId);
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
                System.out.println("Ошибки: series не найден");
            } else {
                System.out.println("Ошибки: " + e.getMessage());
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

