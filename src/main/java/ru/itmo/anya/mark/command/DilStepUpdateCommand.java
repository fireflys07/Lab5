package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DilStepUpdateCommand extends BaseCommand {
    private long cachedStepId;

    private final Map<String, String> cachedUpdates = new HashMap<>();

    public DilStepUpdateCommand(Environment env) {
        super(env, false); // false - команда не требует дополнительного ввода
    }

    @Override
    public String getName() {
        return "dil_step_update";
    }

    @Override
    public String getDescription() {
        return "обновить поля шага разбавления";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        // Проверка минимального количества аргументов
        if (args.length < 2) {
            throw new CommandException("формат: dil_step_update <step_id> field=value ...");
        }

        // Парсинг ID шага
        try {
            cachedStepId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("step_id должен быть числом", e);
        }

        // Очищаем предыдущие обновления
        cachedUpdates.clear();

        // Парсинг всех пар field=value (начиная с индекса 1)
        for (int i = 1; i < args.length; i++) {
            String[] parts = args[i].split("=", 2);

            // Проверка формата field=value
            if (parts.length != 2) {
                throw new CommandException("неверный формат: " + args[i] + ". Ожидается field=value");
            }

            String field = parts[0].toLowerCase(Locale.ROOT);
            String value = parts[1];

            // Проверка допустимых полей
            if (!field.equals("factor") && !field.equals("finalquantity")) {
                throw new CommandException("неизвестное поле: '" + field + "'. Допустимые: factor, finalQuantity");
            }

            // Проверка, что значение не пустое
            if (value.trim().isEmpty()) {
                throw new CommandException("значение для поля '" + field + "' не может быть пустым");
            }

            cachedUpdates.put(field, value);
        }
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        try {
            // Обработка каждого обновляемого поля
            for (Map.Entry<String, String> entry : cachedUpdates.entrySet()) {
                String field = entry.getKey();
                String value = entry.getValue();

                if (field.equals("factor")) {
                    // Обновление коэффициента разбавления
                    try {
                        double factor = Double.parseDouble(value);
                        if (factor <= 0) {
                            throw new CommandException("factor должен быть положительным числом");
                        }
                        environment.getService().updateStepFactor(cachedStepId, factor);
                        System.out.println("Обновлён factor = " + factor);

                    } catch (NumberFormatException e) {
                        throw new CommandException("factor должен быть числом", e);
                    }

                } else if (field.equals("finalquantity")) {
                    // Обновление итогового объёма
                    try {
                        double quantity = Double.parseDouble(value);
                        if (quantity <= 0) {
                            throw new CommandException("finalQuantity должен быть положительным числом");
                        }
                        environment.getService().updateStepFinalQuantity(cachedStepId, quantity);
                        System.out.println("Обновлён finalQuantity = " + quantity);

                    } catch (NumberFormatException e) {
                        throw new CommandException("finalQuantity должен быть числом", e);
                    }
                }
            }

            System.out.println("OK");

        } catch (IllegalArgumentException e) {
            // Обработка ошибок сервиса
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);

            if (msg.contains("step") || msg.contains("не найден")) {
                throw new CommandException("шаг с ID " + cachedStepId + " не найден");
            } else {
                throw new CommandException("ошибка обновления: " + e.getMessage());
            }
        }
    }

    @Override
    public String getHelp() {
        return "dil_step_update <step_id> field=value ... - обновить поля шага";
    }
}
