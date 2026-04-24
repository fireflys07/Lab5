package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandArgsException;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;
import ru.itmo.anya.mark.model.DilutionSourceType;

public final class DilSeriesUpdateCommand extends BaseCommand {

    private long seriesId;
    private String newName;
    private DilutionSourceType newSourceType;
    private long newSourceId;

    public DilSeriesUpdateCommand(Environment env) {
        super(env, false);
    }

    @Override
    public String getName() {
        return "dil_series_update";
    }

    @Override
    public String getHelp() {
        return "обновить серию: dil_series_update <seriesId> <name> <sourceType> <sourceId>";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 4) {
            throw new CommandArgsException(
                    "формат: dil_series_update <seriesId> <name> <sourceType> <sourceId>"
            );
        }

        try {
            seriesId = Long.parseLong(args[0].trim());
        } catch (NumberFormatException e) {
            throw new CommandArgsException("seriesId должен быть числом");
        }

        newName = args[1].trim();
        if (newName.isEmpty()) {
            throw new CommandArgsException("name не может быть пустым");
        }

        try {
            newSourceType = DilutionSourceType.valueOf(args[2].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CommandArgsException(
                    "sourceType должен быть SAMPLE или SOLUTION"
            );
        }

        try {
            newSourceId = Long.parseLong(args[3].trim());
        } catch (NumberFormatException e) {
            throw new CommandArgsException("sourceId должен быть числом");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        try {
            env.getDilutionService().updateSeries(
                    seriesId, newName, newSourceType, newSourceId
            );
            System.out.println("OK серия #" + seriesId + " обновлена");
        } catch (SecurityException e) {
            throw new CommandException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        } catch (Exception e) {
            throw new CommandException("Ошибка при обновлении: " + e.getMessage());
        }
    }
}
