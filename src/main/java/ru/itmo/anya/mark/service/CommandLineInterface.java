package ru.itmo.anya.mark.service;

import ru.itmo.anya.mark.cli.*;
import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;

import java.util.*;

public final class CommandLineInterface {
    private final Scanner scanner;
    private final DilutionService service;
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private boolean running = true;

    public CommandLineInterface(Scanner scanner, DilutionService service) {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner: null");
        }
        if (service == null) {
            throw new IllegalArgumentException("service: null");
        }
        this.scanner = scanner;
        this.service = service;
        registerCommands();
    }

    private void registerCommands() {
        Set<Long> knownSampleIds = new HashSet<>(Set.of(12L));
        Set<Long> knownSolutionIds = new HashSet<>(Set.of(1L));

        commands.put("dil_series_create", new DilSeriesCreateCommand(scanner, service));
        commands.put("dil_series_list", new DilSeriesListCommand(scanner, service));
        commands.put("dil_series_show", new DilSeriesShowCommand(scanner, service));
        commands.put("dil_step_add", new DilStepAddCommand(scanner, service));
        commands.put("dil_step_list", new DilStepListCommand(scanner, service));
        commands.put("dil_link_set", new DilLinkSetCommand(scanner, service, knownSampleIds, knownSolutionIds));
        commands.put("help", new HelpCommand(commands));
        commands.put("exit", new ExitCommand(() -> running = false));
    }

    public void run() {
        while (running) {
            System.out.print("> ");
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
            String cmdName = parts[0].toLowerCase(Locale.ROOT);
            Command command = commands.get(cmdName);
            if (command == null) {
                System.out.println("Ошибка: неизвестная команда. Введите help");
                continue;
            }

            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
            command.execute(args);
        }
    }
}

