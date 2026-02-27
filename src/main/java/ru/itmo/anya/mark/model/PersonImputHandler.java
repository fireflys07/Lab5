package ru.itmo.anya.mark.model;

import java.util.Scanner;

public class PersonImputHandler {
    public String askName(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Введите имя персонажа:");
                String input = scanner.nextLine().trim();

                if (input.isEmpty() || input.isBlank() ) throw new IllegalArgumentException("Имя не может быть пустым");

                return input;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }
    public long askXcords(Scanner scanner){
        while (true) {
            try {
                System.out.print("Введите координату X (целое число): ");

                long x = Long.parseLong(scanner.nextLine().trim());
                return x;
            } catch (NumberFormatException e) {

                System.out.println("Ошибка: введите целое число для координаты X.");
            }
        }
    }
    public double askYcords(Scanner scanner){
        while (true) {
            try {
                System.out.print("Введите координату Y : ");

                double y = Double.parseDouble(scanner.nextLine().trim());
                return y;
            } catch (NumberFormatException e) {

                System.out.println("Ошибка: введите верное число для координаты Y.");
            }
        }
    }
    public float askHeight(Scanner scanner) {
        while (true) {
            try {
                System.out.print("Введите рост (число больше 0): ");
                String input = scanner.nextLine().trim();


                float h = Float.parseFloat(input);


                if (h <= 0) {
                    System.out.println("Ошибка: рост должен быть больше 0!");
                    continue;
                }

                return h;

            } catch (NumberFormatException e) {

                System.out.println("Ошибка: введите число (например, 175.5)");
            }
        }
    }

}
