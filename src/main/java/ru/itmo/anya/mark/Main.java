package ru.itmo.anya.mark;

import ru.itmo.anya.mark.service.CommandLineInterface;
import ru.itmo.anya.mark.service.DilutionStepManager;
import ru.itmo.anya.mark.service.SeriesCollectionManager;
import ru.itmo.anya.mark.service.DilutionService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DilutionService service = new DilutionService(new SeriesCollectionManager(), new DilutionStepManager());
        CommandLineInterface cli = new CommandLineInterface(new Scanner(System.in), service);
        cli.run();
    }
}
