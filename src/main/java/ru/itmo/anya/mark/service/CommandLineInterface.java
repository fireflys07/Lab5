package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.cli.DilutionService;
import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class CommandLineInterface {
    private final Scanner scanner;
    private final DilutionService service;

    public CommandLineInterface(Scanner scanner, DilutionService service) {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner: null");
        }
        if (service == null) {
            throw new IllegalArgumentException("service: null");
        }
        this.scanner = scanner;
        this.service = service;
    }

    public void run() {
        while (true) {
            if (!scanner.hasNextLine()) {
                return;
            }

            String line = scanner.nextLine();
            if (line == null) {
                continue;
            }

            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] parts = trimmed.split("\\s+");
            String cmd = parts[0].toLowerCase(Locale.ROOT);

            switch (cmd) {
                case "dil_series_create" -> {
                    if (parts.length != 1) {
                        System.out.println("Ошибка: команда dil_series_create не принимает аргументы");
                        continue;
                    }
                    handleSeriesCreate();
                }
                case "dil_series_list" -> {
                    if (parts.length != 1) {
                        System.out.println("Ошибка: команда dil_series_list не принимает аргументы");
                        continue;
                    }
                    handleSeriesList();
                }
                case "dil_series_show" -> {
                    if (parts.length != 2) {
                        System.out.println("Ошибка: формат: dil_series_show <series_id>");
                        continue;
                    }
                    handleSeriesShow(parts[1]);
                }
                case "dil_step_add" -> {
                    if (parts.length != 2) {
                        System.out.println("Ошибка: формат: dil_step_add <series_id>");
                        continue;
                    }
                    handleStepAdd(parts[1]);
                }
                case "dil_step_list" -> {
                    if (parts.length != 2) {
                        System.out.println("Ошибка: формат: dil_step_list <series_id>");
                        continue;
                    }
                    handleStepList(parts[1]);
                }
                case "help" -> {
                    if (parts.length != 1) {
                        System.out.println("Ошибка: команда help не принимает аргументы");
                        continue;
                    }
                    printHelp();
                }
                case "exit" -> {
                    if (parts.length != 1) {
                        System.out.println("Ошибка: команда exit не принимает аргументы");
                        continue;
                    }
                    return;
                }
                default -> System.out.println("Ошибка: неизвестная команда. Введите help");
            }
        }
    }

    private void handleSeriesCreate() {
        System.out.print("Название: ");
        if (!scanner.hasNextLine()) {
            System.out.println("Ошибка: не удалось прочитать название");
            return;
        }
        String name = scanner.nextLine();
        try {

            DilutionSeries series = service.createSeries(name, DilutionSourceType.SAMPLE, 1L, "SYSTEM");
            System.out.println("OK series_id=" + series.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибки: " + e.getMessage());
        }
    }

    private void handleSeriesList() {
        List<DilutionSeries> list = service.listSeries();
        System.out.println("ID Name");
        for (DilutionSeries s : list) {
            System.out.println(s.getId() + " " + s.getName());
        }
    }

    private void handleSeriesShow(String rawId) {
        long id;
        try {
            id = Long.parseLong(rawId);
        } catch (NumberFormatException e) {
            System.out.println("Ошибки: series_id не число");
            return;
        }

        try {
            DilutionSeries s = service.getSeries(id);
            int stepsCount = service.listSteps(id).size();
            System.out.println("DilutionSeries #" + s.getId());
            System.out.println("steps: " + stepsCount);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибки: " + e.getMessage());
        }
    }

    private void handleStepAdd(String rawSeriesId) {
        long seriesId;
        try {
            seriesId = Long.parseLong(rawSeriesId);
        } catch (NumberFormatException e) {
            System.out.println("Ошибки: series_id не число");
            return;
        }

        // 1) шаг номер
        System.out.print("Шаг номер: ");
        if (!scanner.hasNextLine()) {
            System.out.println("Ошибки: не удалось прочитать stepNumber");
            return;
        }
        String rawStepNumber = scanner.nextLine().trim();

        // 2) factor
        System.out.print("Коэффициент (например 10): ");
        if (!scanner.hasNextLine()) {
            System.out.println("Ошибки: не удалось прочитать factor");
            return;
        }
        String rawFactor = scanner.nextLine().trim();

        // 3) final
        System.out.print("Итоговый объём: ");
        if (!scanner.hasNextLine()) {
            System.out.println("Ошибки: не удалось прочитать finalQuantity");
            return;
        }
        String rawFinalQty = scanner.nextLine().trim();

        // 4) unit
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

    private void handleStepList(String rawSeriesId) {
        long seriesId;
        try {
            seriesId = Long.parseLong(rawSeriesId);
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

    private static void printHelp() {
        System.out.println("Доступные команды:");
        System.out.println("  help                - список команд");
        System.out.println("  exit                - выход");
        System.out.println();
        System.out.println("Команды предметной области (пока могут быть не реализованы в CLI):");
        System.out.println("  dil_series_create");
        System.out.println("  dil_series_list");
        System.out.println("  dil_series_show <series_id>");
        System.out.println("  dil_step_add <series_id>");
        System.out.println("  dil_step_list <series_id>");
        System.out.println("  dil_link_set <series_id>");
        System.out.println("  dil_calc <series_id> <start_conc> <unit>");
        System.out.println("  dil_step_update <step_id> field=value ...");
        System.out.println("  dil_step_delete <step_id>");
        System.out.println("  dil_export <series_id>");
    }
}

