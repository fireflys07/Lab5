package ru.itmo.anya.mark;

import ru.itmo.anya.mark.command.*;
import ru.itmo.anya.mark.interpreter.CommandInterpreter;
import ru.itmo.anya.mark.interpreter.Environment;
import ru.itmo.anya.mark.service.*;
import ru.itmo.anya.mark.storage.*;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        //  Инициализация подключения к БД
        DatabaseConnection.getInstance().testConnection();

        // Создаём репозитории
        UserRepository userRepo = new UserRepository();
        SeriesRepository seriesRepo = new SeriesRepository();
        StepRepository stepRepo = new StepRepository();

        //  менеджеры с репозиториями
        SeriesCollectionManager seriesManager = new SeriesCollectionManager(seriesRepo);
        DilutionStepManager stepManager = new DilutionStepManager(stepRepo);

        //  Загружаем данные из БД в память
        seriesManager.loadFromDatabase();
        stepManager.loadFromDatabase();

        //  Создаём AuthService с репозиторием
        AuthService authService = new AuthService(userRepo);


        DilutionService dilutionService = new DilutionService(seriesManager, stepManager, authService);

        Scanner scanner = new Scanner(System.in);

        CommandLineInterface cli = new CommandLineInterface(
                scanner,
                dilutionService,
                seriesManager,
                stepManager
        );

        Environment environment = new Environment(
                seriesManager,
                stepManager,
                dilutionService,
                cli,
                scanner,
                authService
        );

        CommandInterpreter interpreter = new CommandInterpreter(environment, scanner);

        registerAllCommands(interpreter, environment);

        System.out.println(" Dilution Manager CLI (PostgreSQL)");
        System.out.println("Введите 'help' для списка команд или 'login' для входа");

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
        // Save/Load для CSV больше не нужны, но можно оставить для совместимости
        // interpreter.register(new SaveCommand(environment));
        // interpreter.register(new LoadCommand(environment));
        interpreter.register(new HelpCommand(interpreter.getCommands()));
        interpreter.register(new ExitCommand(interpreter::stop));
        interpreter.register(new RegisterCommand(environment));
        interpreter.register(new LoginCommand(environment));
        interpreter.register(new DilSeriesDeleteCommand(environment));
        interpreter.register(new DilSeriesUpdateCommand(environment));
    }
}