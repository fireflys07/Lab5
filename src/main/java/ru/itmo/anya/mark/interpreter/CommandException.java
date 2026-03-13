package ru.itmo.anya.mark.interpreter;

/**
 * Исключение, выбрасываемое при ошибках выполнения команд.
 * Обрабатывается в командном интерпретаторе.
 */
public class CommandException extends Exception {

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message);
    }
}

