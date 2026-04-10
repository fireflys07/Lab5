package ru.itmo.anya.mark;

import ru.itmo.anya.mark.command.*;
import ru.itmo.anya.mark.interpreter.CommandInterpreter;
import ru.itmo.anya.mark.interpreter.Environment;
import ru.itmo.anya.mark.service.CommandLineInterface;
import ru.itmo.anya.mark.service.DilutionStepManager;
import ru.itmo.anya.mark.service.SeriesCollectionManager;
import ru.itmo.anya.mark.service.DilutionService;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        SeriesCollectionManager seriesCollectionManager = new SeriesCollectionManager();
        DilutionStepManager dilutionStepManager = new DilutionStepManager();
        DilutionService dilutionService = new DilutionService(seriesCollectionManager, dilutionStepManager);

        Scanner scanner = new Scanner(System.in);

        CommandLineInterface cli = new CommandLineInterface(
                scanner,
                dilutionService,
                seriesCollectionManager,
                dilutionStepManager
        );

        Environment environment = new Environment(
                seriesCollectionManager,
                dilutionStepManager,
                dilutionService,
                cli,
                scanner
        );

        CommandInterpreter interpreter = new CommandInterpreter(environment, scanner);

        registerAllCommands(interpreter, environment);

        interpreter.run();
    }
    private static void registerAllCommands(CommandInterpreter interpreter, Environment environment) {
        Set<Long> knownSampleIds = new HashSet<>(Set.of(12L));
        Set<Long> knownSolutionIds = new HashSet<>(Set.of(1L));

        interpreter.register(new DilSeriesCreateCommand(environment));
        interpreter.register(new DilSeriesListCommand(environment));
        interpreter.register(new DilSeriesShowCommand(environment));
        interpreter.register(new DilStepAddCommand(environment));
        interpreter.register(new DilStepListCommand(environment));
        interpreter.register(new DilStepUpdateCommand(environment));
        interpreter.register(new DilStepDeleteCommand(environment));
        interpreter.register(new DilLinkSetCommand(environment, knownSampleIds, knownSolutionIds));
        interpreter.register(new DilCalcCommand(environment));
        interpreter.register(new DilExportCommand(environment));
        interpreter.register(new SaveCommand(environment));
        interpreter.register(new LoadCommand(environment));
        interpreter.register(new HelpCommand(interpreter.getCommands()));
        interpreter.register(new ExitCommand(interpreter::stop));
        interpreter.register(new SaveCommand(environment));
        interpreter.register(new LoadCommand(environment));
    }
}
