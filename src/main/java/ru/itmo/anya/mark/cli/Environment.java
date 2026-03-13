package ru.itmo.anya.mark.cli;

import java.util.Scanner;

/**
 * Общая среда выполнения для команд CLI.
 * Хранит объекты, которые могут понадобиться разным командам.
 */
public final class Environment {

    private final Scanner scanner;
    private final DilutionService service;

    public Environment(Scanner scanner, DilutionService service) {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner: null");
        }
        if (service == null) {
            throw new IllegalArgumentException("service: null");
        }
        this.scanner = scanner;
        this.service = service;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public DilutionService getService() {
        return service;
    }
}

