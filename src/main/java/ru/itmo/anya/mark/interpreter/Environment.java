package ru.itmo.anya.mark.interpreter;

import ru.itmo.anya.mark.service.CommandLineInterface;
import ru.itmo.anya.mark.service.DilutionService;
import ru.itmo.anya.mark.service.DilutionStepManager;
import ru.itmo.anya.mark.service.SeriesCollectionManager;

public class Environment {
    private final SeriesCollectionManager seriesCollectionManager;
    private final DilutionStepManager dilutionStepManager;
    private final DilutionService dilutionService;
    private final CommandLineInterface commandLineInterface;

    public Environment(SeriesCollectionManager seriesCollectionManager,
                       DilutionStepManager dilutionStepManager,
                       DilutionService dilutionService,
                       CommandLineInterface commandLineInterface) {
        this.seriesCollectionManager = seriesCollectionManager;
        this.dilutionStepManager = dilutionStepManager;
        this.dilutionService = dilutionService;
        this.commandLineInterface = commandLineInterface;
    }

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
}