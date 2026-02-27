package ru.itmo.anya.mark.model;

import java.util.Scanner;

public class DilutionSeriesInputHandler {

    public String askName(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Введите название серии разбавлений:");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    throw new IllegalArgumentException("Название не может быть пустым");
                }
                if (input.length() > 128) {
                    throw new IllegalArgumentException("Название не может быть длиннее 128 символов");
                }

                return input;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public DilutionSourceType askSourceType(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Введите тип источника (SAMPLE или SOLUTION):");
                String input = scanner.nextLine().trim().toUpperCase();

                return DilutionSourceType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: введите SAMPLE или SOLUTION");
            }
        }
    }

    public long askSourceId(Scanner scanner) {
        while (true) {
            try {
                System.out.print("Введите ID источника (> 0): ");
                long id = Long.parseLong(scanner.nextLine().trim());
                if (id <= 0) {
                    System.out.println("Ошибка: ID должен быть больше 0");
                    continue;
                }
                return id;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число для ID источника.");
            }
        }
    }

    public String askOwnerUsername(Scanner scanner) {
        while (true) {
            System.out.print("Введите логин владельца серии (или SYSTEM): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Ошибка: логин не может быть пустым");
                continue;
            }
            return input;
        }
    }
}

