package ru.itmo.anya.mark.cli;

public final class ExitCommand extends Command {

    private final Runnable onExit;

    public ExitCommand(Runnable onExit) {
        if (onExit == null) {
            throw new IllegalArgumentException("onExit: null");
        }
        this.onExit = onExit;
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            System.out.println("Ошибка: команда exit не принимает аргументы");
            return;
        }
        onExit.run();
    }

    @Override
    public String getHelp() {
        return "выход из программы";
    }
}

