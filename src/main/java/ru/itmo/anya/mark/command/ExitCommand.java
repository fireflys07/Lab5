package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.interpreter.Command;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

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
    public String getName() {
        return "exit";
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
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

