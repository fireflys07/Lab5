package ru.itmo.anya.mark.service;
import ru.itmo.anya.mark.command.*;
import ru.itmo.anya.mark.interpreter.Command;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

import java.util.*;

public final class CommandLineInterface {
    private final Scanner scanner;
    private final Environment env;
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private boolean running = true;

    public CommandLineInterface(Scanner scanner,
                                DilutionService dilutionService,
                                SeriesCollectionManager seriesCollectionManager,
                                DilutionStepManager dilutionStepManager) {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner: null");
        }
        if (dilutionService == null) {
            throw new IllegalArgumentException("service: null");
        }

        if (seriesCollectionManager == null) {
            throw new IllegalArgumentException("service: null");
        }
        if (dilutionStepManager == null) {
            throw new IllegalArgumentException("service: null");
        }

        this.scanner = scanner;
        this.env = new Environment(
                seriesCollectionManager,
                dilutionStepManager,
                dilutionService,
                this
        );

        registerCommands();
    }

    private void registerCommands() {
        Set<Long> knownSampleIds = new HashSet<>(Set.of(12L));
        Set<Long> knownSolutionIds = new HashSet<>(Set.of(1L));

        commands.put("dil_series_create", new DilSeriesCreateCommand(env));
        commands.put("dil_series_list", new DilSeriesListCommand(env));
        commands.put("dil_series_show", new DilSeriesShowCommand(env));
        commands.put("dil_step_add", new DilStepAddCommand(env));
        commands.put("dil_step_list", new DilStepListCommand(env));
        commands.put("dil_link_set", new DilLinkSetCommand(env, knownSampleIds, knownSolutionIds));
        commands.put("dil_calc", new DilCalcCommand(env));
        commands.put("dil_step_update", new DilStepUpdateCommand(env));    // команда 8
        commands.put("dil_step_delete", new DilStepDeleteCommand(env));
        commands.put("dil_export", new DilExportCommand(env));
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
                System.err.println("Ошибка: неизвестная команда. Введите help");
                continue;
            }

            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
            try {
                command.checkArgs(args);
                if (command.isReqAdditionalInput()) {
                    command.readAdditionalInput(env);
                }
                command.execute(env, args);
            } catch (CommandException e) {
                if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                    System.err.println("Ошибки: " + e.getMessage());
                }
            }
        }
    }
}

