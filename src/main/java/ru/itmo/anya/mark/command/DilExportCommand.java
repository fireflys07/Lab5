package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

import java.util.Locale;

public class DilExportCommand extends BaseCommand {
    private long cachedSeriesId;

    public DilExportCommand(Environment env) {
        super(env, false); // false - команда не требует дополнительного ввода
    }

    @Override
    public String getName() {
        return "dil_export";
    }

    @Override
    public String getDescription() {
        return "экспортировать серию разбавлений в текст";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        // Проверка количества аргументов
        if (args.length != 1) {
            throw new CommandException("формат: dil_export <series_id>");
        }

        // Парсинг ID серии
        try {
            cachedSeriesId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("series_id должен быть числом", e);
        }

        // Дополнительная проверка на положительное число
        if (cachedSeriesId <= 0) {
            throw new CommandException("series_id должен быть положительным числом");
        }
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        try {
            // Получаем экспорт серии от сервиса
            String export = environment.getService().exportSeries(cachedSeriesId);

            // Выводим экспорт
            System.out.println(export);
            System.out.println("Dilution series exported (text)");

        } catch (IllegalArgumentException e) {
            // Обработка ошибок сервиса
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);

            if (msg.contains("series") || msg.contains("не найдена")) {
                throw new CommandException("серия с ID " + cachedSeriesId + " не найдена");
            } else {
                throw new CommandException("ошибка экспорта: " + e.getMessage());
            }
        }
    }

    /**
     * @return подробная справка по использованию команды
     */
    @Override
    public String getHelp() {
        return "dil_export <series_id> - экспортировать серию разбавлений в текст\n" +
                "  Пример: dil_export 1";
    }
}
