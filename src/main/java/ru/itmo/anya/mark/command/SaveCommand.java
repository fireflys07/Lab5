package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandArgsException;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

import java.nio.file.Path;

public final class SaveCommand extends BaseCommand {

    private String cachedPath;

    public SaveCommand(Environment env) {
        super(env, false);
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getHelp() {
        return "сохранить данные в CSV; относительный путь — файлы в папке data/";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandArgsException("формат: save <path>");
        }
        String path = args[0].trim();
        if (path.isEmpty()) {
            throw new CommandArgsException("path не может быть пустым");
        }
        cachedPath = path;
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        try {
            env.getService().saveToCsv(cachedPath);
            Path base = env.getService().resolveCsvDataPath(cachedPath, false);
            Path seriesFile = env.getService().getCsvSeriesPathFromStem(base);
            Path stepsFile = env.getService().getCsvStepPathFromStem(base);
            System.out.println("OK saved: " + seriesFile + ", " + stepsFile);
        } catch (Exception e) {
            throw new CommandException("ошибка сохранения: " + e.getMessage(), e);
        }
    }
}