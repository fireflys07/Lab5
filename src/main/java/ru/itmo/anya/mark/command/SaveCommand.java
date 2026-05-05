package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

public final class SaveCommand extends BaseCommand {

    public SaveCommand(Environment env) {
        super(env, false);
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getHelp() {
        return "подтверждение сохранения (данные сохраняются в БД автоматически)";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        //
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {

        System.out.println("OK: Данные уже сохранены в PostgreSQL");
    }
}