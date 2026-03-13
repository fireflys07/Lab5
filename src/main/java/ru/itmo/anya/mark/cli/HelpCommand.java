package ru.itmo.anya.mark.cli;

import java.util.Map;

public final class HelpCommand extends Command {

    private final Map<String, Command> commands;

    public HelpCommand(Map<String, Command> commands) {
        if (commands == null) {
            throw new IllegalArgumentException("commands: null");
        }
        this.commands = commands;
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            System.out.println("Ошибка: команда help не принимает аргументы");
            return;
        }
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().getHelp());
        }
    }

    @Override
    public String getHelp() {
        return "список команд";
    }
}

