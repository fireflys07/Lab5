package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.cli.DilutionService;
import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class CommandLineInterface {
    private final Scanner scanner;
    private final DilutionService service;

    public CommandLineInterface(Scanner scanner) {
        this(scanner, new DilutionService(new SeriesCollectionManager(), new DilutionStepManager()));
    }

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

