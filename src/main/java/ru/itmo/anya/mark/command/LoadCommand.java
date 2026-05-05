package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

public final class LoadCommand extends BaseCommand {

    public LoadCommand(Environment env) {
        super(env, false);
    }

    @Override
    public String getName() {
        return "load";
    }

    @Override
    public String getHelp() {
        return "обновить данные из базы данных PostgreSQL";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {

    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        try {

            env.getService().loadFromDatabase();
            System.out.println("OK: Данные обновлены из PostgreSQL");
        } catch (Exception e) {
            throw new CommandException("Ошибка загрузки из БД: " + e.getMessage(), e);
        }
    }
}