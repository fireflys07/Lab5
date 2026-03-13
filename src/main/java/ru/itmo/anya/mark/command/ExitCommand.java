package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.Command;

public final class ExitCommand extends Command {

    private final Runnable onExit;

    public ExitCommand(Runnable onExit) {
        super(false);
        if (onExit == null) {
            throw new IllegalArgumentException("onExit: null");
        }
        this.onExit = onExit;
    }

    @Override
    public void execute(String[] args) throws CommandException {
        if (args.length != 0) {
            throw new CommandException("команда exit не принимает аргументы");
        }
        onExit.run();
    }

    @Override
    public String getHelp() {
        return "выход из программы";
    }
}

