package ru.itmo.anya.mark.interpreter;

import ru.itmo.anya.mark.service.*;

import java.util.Scanner;

public class Environment {
    private final SeriesCollectionManager seriesCollectionManager;
    private final DilutionStepManager dilutionStepManager;
    private final DilutionService dilutionService;
    private final CommandLineInterface commandLineInterface;
    private final Scanner scanner;
    private final AuthService authService;

    public Environment(SeriesCollectionManager seriesCollectionManager,
                       DilutionStepManager dilutionStepManager,
                       DilutionService dilutionService,
                       CommandLineInterface commandLineInterface,
                       Scanner scanner,
                       AuthService authService) {
        this.seriesCollectionManager = seriesCollectionManager;
        this.dilutionStepManager = dilutionStepManager;
        this.dilutionService = dilutionService;
        this.commandLineInterface = commandLineInterface;
        this.scanner = scanner;
        this.authService = authService;
    }

    public Scanner getScanner() {return scanner;}
    public DilutionService getService() {return dilutionService;}
    public SeriesCollectionManager getSeriesCollectionManager() {
        return seriesCollectionManager;
    }
    public DilutionStepManager getDilutionStepManager() {
        return dilutionStepManager;
    }
    public DilutionService getDilutionService() {
        return dilutionService;
    }
    public CommandLineInterface getCommandLineInterface() {
        return commandLineInterface;
    }
    public AuthService getAuthService() { return authService; }
}