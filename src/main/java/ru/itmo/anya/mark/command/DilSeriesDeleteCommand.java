package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandArgsException;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

public final class DilSeriesDeleteCommand extends BaseCommand {

    private long seriesId;

    public DilSeriesDeleteCommand(Environment env) {
        super(env, false);  // false = не валидировать args автоматически
    }

    @Override
    public String getName() {
        return "dil_series_delete";
    }

    @Override
    public String getHelp() {
        return "удалить серию разбавлений: dil_series_delete <seriesId>";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandArgsException("формат: dil_series_delete <seriesId>");
        }
        try {
            seriesId = Long.parseLong(args[0].trim());
        } catch (NumberFormatException e) {
            throw new CommandArgsException("seriesId должен быть числом");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        try {
            env.getDilutionService().deleteSeries(seriesId);
            System.out.println("OK серия #" + seriesId + " удалена");
        } catch (SecurityException e) {
            throw new CommandException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        } catch (Exception e) {
            throw new CommandException("Ошибка при удалении: " + e.getMessage());
        }
    }
}