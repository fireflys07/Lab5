package ru.itmo.anya.mark.interpreter;

import ru.itmo.anya.mark.validation.ValidationException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandInterpreter {
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Environment environment;
    private final Scanner scanner;

    public CommandInterpreter(Environment environment, Scanner scanner) {
        this.environment = environment;
        this.scanner = scanner;
    }

    public void register(Command command) {
        commands.put(command.getName(), command);
    }
    public Map<String, Command> getCommands() {
        return commands;
    }
    public void run() {
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNext()) {
                return;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            String commandName = parts[0];
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);

            Command command = commands.get(commandName);
            if (command == null) {
                System.out.println("Ошибка: неизвестная команда. Введите help");
                continue;
            }
            try {command.checkArgs(args);

                if (command.isRequiresAdditionalInput()) {
                    command.additionalInput(environment, scanner);
                }
                command.execute(environment, args);

            } catch (CommandArgsException exception) {
                System.out.println("Ошибка аргументов: " + exception.getMessage());
                System.out.println("Подсказка: " + command.getHelp());
            } catch (ValidationException exception) {
                System.out.println("Ошибка валидации: " + exception.getMessage());
            } catch (Exception exception) {
                System.out.println("Непредвиденная ошибка: " + exception.getMessage());
            }
        }
    }
}
