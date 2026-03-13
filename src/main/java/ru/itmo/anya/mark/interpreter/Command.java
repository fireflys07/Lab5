package ru.itmo.anya.mark.interpreter;

import java.util.Scanner;

public abstract class Command {
    private final String name;
    private final String help;
    private final boolean requiresAdditionalInput;

    public Command(String name, String help, boolean requiresAdditionalInput) {
        this.name = name;
        this.help = help;
        this.requiresAdditionalInput = requiresAdditionalInput;
    }
    public String getName() {
        return name;
    }
    public final String getHelp() {
        return help;
    }
    public final boolean isRequiresAdditionalInput() {
        return requiresAdditionalInput;
    }

    public abstract void execute(Environment env, String[] args) throws CommandArgsException;
    public void checkArgs(String[] args) throws CommandArgsException {
    }
    public void additionalInput(Environment environment, Scanner scanner) {
    }
}