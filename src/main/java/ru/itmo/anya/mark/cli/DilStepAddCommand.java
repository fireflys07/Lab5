package ru.itmo.anya.mark.cli;

import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;

import java.util.Locale;
import java.util.Scanner;

public final class DilStepAddCommand extends BaseCommand {

    public DilStepAddCommand(Scanner scanner, DilutionService service) {
        super(scanner, service);
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Ошибка: формат: dil_step_add <series_id>");
            return;
        }

        long seriesId;
        try {
            seriesId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Ошибки: series_id не число");
            return;
        }

        System.out.print("Шаг номер: ");
        if (!scanner.hasNextLine()) {
            System.out.println("Ошибки: не удалось прочитать stepNumber");
            return;
        }
        String rawStepNumber = scanner.nextLine().trim();

        System.out.print("Коэффициент (например 10): ");
        if (!scanner.hasNextLine()) {
            System.out.println("Ошибки: не удалось прочитать factor");
            return;
        }
        String rawFactor = scanner.nextLine().trim();

        System.out.print("Итоговый объём: ");
        if (!scanner.hasNextLine()) {
            System.out.println("Ошибки: не удалось прочитать finalQuantity");
            return;
        }
        String rawFinalQty = scanner.nextLine().trim();

        System.out.print("Единицы (mL): ");
        if (!scanner.hasNextLine()) {
            System.out.println("Ошибки: не удалось прочитать unit");
            return;
        }
        String rawUnit = scanner.nextLine().trim();

        int stepNumber;
        double factor;
        double finalQty;
        try {
            stepNumber = Integer.parseInt(rawStepNumber);
            factor = Double.parseDouble(rawFactor);
            finalQty = Double.parseDouble(rawFinalQty);
        } catch (NumberFormatException e) {
            System.out.println("Ошибки: коэффициент/объём не числа");
            return;
        }

        if (factor <= 0) {
            System.out.println("Ошибки: коэффициент <=0");
            return;
        }

        FinalQuantityUnit unit = parseUnitOrNull(rawUnit);
        if (unit == null) {
            System.out.println("Ошибки: неизвестные единицы");
            return;
        }

        try {
            DilutionStep step = service.addStep(seriesId, stepNumber, factor, finalQty, unit);
            System.out.println("OK step_id=" + step.getId());
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
            if (msg.contains("series")) {
                System.out.println("Ошибки: series не найден");
            } else if (msg.contains("factor") || msg.contains("finalquantity")) {
                System.out.println("Ошибки: коэффициент <=0");
            } else {
                System.out.println("Ошибки: " + e.getMessage());
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

