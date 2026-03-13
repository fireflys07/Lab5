package ru.itmo.anya.mark;

import ru.itmo.anya.mark.service.CommandLineInterface;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        CommandLineInterface cli = new CommandLineInterface(new Scanner(System.in));
        cli.run();
    }
}
