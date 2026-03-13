package ru.itmo.anya.mark.cli;

public abstract class Command {

    /**
     * Выполнить логику команды.
     *
     * @param args аргументы команды (без имени команды)
     */
    public abstract void execute(String[] args);

    /**
     * Краткое описание команды для вывода в help.
     */
    public abstract String getHelp();
}

