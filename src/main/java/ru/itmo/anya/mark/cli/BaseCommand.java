package ru.itmo.anya.mark.cli;

import ru.itmo.anya.mark.interpreter.Command;
import ru.itmo.anya.mark.interpreter.Environment;

/**
 * Базовая команда с доступом к общему Environment.
 */
public abstract class BaseCommand extends Command {

    protected final Environment env;

    protected BaseCommand(Environment env, boolean reqAdditionalInput) {
        super(reqAdditionalInput);
        if (env == null) {
            throw new IllegalArgumentException("env: null");
        }
        this.env = env;
    }

    protected Environment getEnv() {
        return env;
    }
}
