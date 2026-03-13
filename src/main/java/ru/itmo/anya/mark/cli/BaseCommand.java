package ru.itmo.anya.mark.cli;

import java.util.Scanner;

/**
 * Базовая команда с доступом к Scanner и DilutionService.
 */
public abstract class BaseCommand extends Command {

    protected final Scanner scanner;
    protected final DilutionService service;

    protected BaseCommand(Scanner scanner, DilutionService service) {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner: null");
        }
        if (service == null) {
            throw new IllegalArgumentException("service: null");
        }
        this.scanner = scanner;
        this.service = service;
    }
}

