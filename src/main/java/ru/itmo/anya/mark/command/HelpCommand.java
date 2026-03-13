package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.Command;

import java.util.Map;

public final class HelpCommand extends Command {

    private final Map<String, Command> commands;

    public HelpCommand(Map<String, Command> commands) {
        super(false);
        if (commands == null) {
            throw new IllegalArgumentException("commands: null");
        }
        this.commands = commands;
    }

    @Override
    public void execute(String[] args) throws CommandException {
        if (args.length != 0) {
            throw new CommandException("команда help не принимает аргументы");
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

