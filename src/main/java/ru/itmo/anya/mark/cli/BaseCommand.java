package ru.itmo.anya.mark.cli;

/**
 * Базовая команда с доступом к общему Environment.
 */
public abstract class BaseCommand extends Command {

    protected final Environment env;

    protected BaseCommand(Environment env) {
        if (env == null) {
            throw new IllegalArgumentException("env: null");
        }
        this.env = env;
    }

    protected Environment getEnv() {
        return env;
    }
}
