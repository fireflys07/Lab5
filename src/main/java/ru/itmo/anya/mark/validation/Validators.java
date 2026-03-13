package ru.itmo.anya.mark.validation;

import ru.itmo.anya.mark.model.DilutionSourceType;
import ru.itmo.anya.mark.model.FinalQuantityUnit;

public class Validators {

    public static void validateSeriesName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Название серии не может быть пустым");
        }
        if (name.length() > 128) {
            throw new ValidationException("Название серии не может быть длиннее 128 символов");
        }
    }

    public static double validatePositiveNumber(String str, String fieldName) throws ValidationException {
        try {
            double value = Double.parseDouble(str);
            if (value <= 0) {
                throw new ValidationException(fieldName + " должен быть положительным числом");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " должен быть числом", e);
        }
    }

    public static int validatePositiveInt(String str, String fieldName) throws ValidationException {
        try {
            int value = Integer.parseInt(str);
            if (value <= 0) {
                throw new ValidationException(fieldName + " должен быть положительным числом");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " должен быть целым числом", e);
        }
    }

    public static long validateId(String str) throws ValidationException {
        try {
            long value = Long.parseLong(str);
            if (value <= 0) {
                throw new ValidationException("ID должен быть положительным числом");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException("ID должен быть числом", e);
        }
    }

    public static DilutionSourceType validateSourceType(String typeStr) throws ValidationException {
        try {
            return DilutionSourceType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Тип источника должен быть SAMPLE или SOLUTION");
        }
    }

    public static FinalQuantityUnit validateUnit(String unitStr) throws ValidationException {
        try {
            return FinalQuantityUnit.valueOf(unitStr);
        } catch (IllegalArgumentException e) {
            // Возвращаем значение по умолчанию с предупреждением
            return FinalQuantityUnit.ML;
        }
    }

    public static int validateStepNumber(String str) throws ValidationException {
        return validatePositiveInt(str, "Номер шага");
    }
}