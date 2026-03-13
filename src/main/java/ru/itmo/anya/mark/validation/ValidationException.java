package ru.itmo.anya.mark.validation;

/**
 * Исключение, выбрасываемое при ошибках валидации полей сущностей.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}