package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;
import ru.itmo.anya.mark.validation.ValidationException;
import ru.itmo.anya.mark.validation.Validators;

import java.util.Locale;

public final class DilStepAddCommand extends BaseCommand {

    private Long cachedSeriesId;

    public DilStepAddCommand(Environment env) {
        super(env, true);
    }

    @Override
    public String getName() {
        return "dil_step_add";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("формат: dil_step_add <series_id>");
        }

        long seriesId;
        try {
            cachedSeriesId = Validators.validateId(args[0]);
        } catch (ValidationException e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public void readAdditionalInput(Environment env) throws CommandException {
        System.out.print("Шаг номер: ");
        if (!env.getScanner().hasNextLine()) {
            throw new CommandException("не удалось прочитать stepNumber");
        }
        String rawStepNumber = env.getScanner().nextLine().trim();

        System.out.print("Коэффициент (например 10): ");
        if (!env.getScanner().hasNextLine()) {
            throw new CommandException("не удалось прочитать factor");
        }
        String rawFactor = env.getScanner().nextLine().trim();

        System.out.print("Итоговый объём: ");
        if (!env.getScanner().hasNextLine()) {
            throw new CommandException("не удалось прочитать finalQuantity");
        }
        String rawFinalQty = env.getScanner().nextLine().trim();

        System.out.print("Единицы (mL): ");
        if (!env.getScanner().hasNextLine()) {
            throw new CommandException("не удалось прочитать unit");
        }
        String rawUnit = env.getScanner().nextLine().trim();

        // сохраняем во временные поля для execute
        this.cachedSeriesId = null; // будет установлен в execute на основе args
        this.cachedStepNumber = rawStepNumber;
        this.cachedFactor = rawFactor;
        this.cachedFinalQty = rawFinalQty;
        this.cachedUnit = rawUnit;
    }

    private String cachedStepNumber;
    private String cachedFactor;
    private String cachedFinalQty;
    private String cachedUnit;

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        long seriesId;
        try {
            seriesId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("series_id не число", e);
        }
        this.cachedSeriesId = seriesId;

        int stepNumber;
        double factor;
        double finalQty;
        try {
            stepNumber = Integer.parseInt(cachedStepNumber);
            factor = Double.parseDouble(cachedFactor);
            finalQty = Double.parseDouble(cachedFinalQty);
        } catch (NumberFormatException e) {
            throw new CommandException("коэффициент/объём не числа", e);
        }

        if (factor <= 0) {
            throw new CommandException("коэффициент <=0");
        }

        FinalQuantityUnit unit = parseUnitOrNull(cachedUnit);
        if (unit == null) {
            throw new CommandException("неизвестные единицы");
        }

        try {
            DilutionStep step = env.getService().addStep(seriesId, stepNumber, factor, finalQty, unit);
            System.out.println("OK step_id=" + step.getId());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
            if (msg.contains("series")) {
                throw new CommandException("series не найден");
            } else if (msg.contains("factor") || msg.contains("finalquantity")) {
                throw new CommandException("коэффициент <=0");
            } else {
                throw new CommandException(e.getMessage());
            }
        }
    }

    private static FinalQuantityUnit parseUnitOrNull(String rawUnit) {
        if (rawUnit == null) {
            return null;
        }
        String u = rawUnit.trim();
        if (u.isEmpty()) {
            u = "ml";
        }
        u = u.toLowerCase(Locale.ROOT);
        return switch (u) {
            case "ml", "ml.", "mл", "мл", "mл.", "мл." -> FinalQuantityUnit.ML;
            case "l", "л" -> FinalQuantityUnit.L;
            case "g", "г" -> FinalQuantityUnit.G;
            case "mg", "мг" -> FinalQuantityUnit.MG;
            default -> null;
        };
    }

    @Override
    public String getHelp() {
        return "добавить шаг к серии";
    }
}

