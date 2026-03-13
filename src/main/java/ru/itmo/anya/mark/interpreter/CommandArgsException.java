package ru.itmo.anya.mark.interpreter;

public class CommandArgsException extends CommandException{
    public CommandArgsException(String message) {
        super(message);
    }
    public CommandArgsException(String message, Throwable cause) {
        super(message, cause);
    }
}
