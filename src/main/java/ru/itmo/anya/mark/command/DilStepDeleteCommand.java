package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

import java.util.Locale;

public class DilStepDeleteCommand extends BaseCommand {
    private long cachedStepId;

    public DilStepDeleteCommand(Environment env) {
        super(env, false); // false - команда не требует дополнительного ввода
    }

    @Override
    public String getName() {
        return "dil_step_delete";
    }

    @Override
    public String getDescription() {
        return "удалить шаг разбавления";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        // Проверка количества аргументов
        if (args.length != 1) {
            throw new CommandException("формат: dil_step_delete <step_id>");
        }

        // Парсинг ID шага
        try {
            cachedStepId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("step_id должен быть числом", e);
        }

        // Дополнительная проверка на положительное число
        if (cachedStepId <= 0) {
            throw new CommandException("step_id должен быть положительным числом");
        }
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        try {
            // Проверяем существование шага перед удалением
            var step = environment.getService().getStepById(cachedStepId);
            if (step == null) {
                throw new CommandException("шаг с ID " + cachedStepId + " не найден");
            }

            // Вызов сервиса для удаления шага
            environment.getService().deleteStep(cachedStepId);

            System.out.println("OK deleted");

        } catch (IllegalArgumentException e) {
            // Обработка ошибок сервиса
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);

            if (msg.contains("step") || msg.contains("не найден")) {
                throw new CommandException("шаг с ID " + cachedStepId + " не найден");
            } else {
                throw new CommandException("ошибка удаления: " + e.getMessage());
            }
        }
    }

    @Override
    public String getHelp() {
        return "dil_step_delete <step_id> - удалить шаг разбавления";
    }
}
