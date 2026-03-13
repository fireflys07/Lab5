package ru.itmo.anya.mark.cli;

/**
 * Исключение, выбрасываемое при ошибках выполнения команд.
 * Обрабатывается в командном интерпретаторе.
 */
public class CommandException extends Exception {

    public CommandException(String message) {
        super(message);
    }
}

