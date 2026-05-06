package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandArgsException;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

public final class LoginCommand extends BaseCommand {

    private String login;
    private String password;

    public LoginCommand(Environment env) {
        super(env, false);
    }

    @Override
    public String getName() { return "login"; }

    @Override
    public String getHelp() { return "войти в систему: login <login> <password>"; }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandArgsException("формат: login <login> <password>");
        }
        login = args[0].trim();
        password = args[1];
        if (login.isEmpty()) {
            throw new CommandArgsException("login не может быть пустым");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        try {
            if (env.getAuthService().login(login, password)) {
                System.out.println("OK вход выполнен: " + login);
            } else {
                throw new CommandException("неверный логин или пароль");
            }
        } catch (Exception e) {
            // Ловим ошибку БД
            String msg = e.getMessage();
            if (msg != null && msg.contains("Ошибка БД")) {
                throw new CommandException("Ошибка подключения к базе данных: " + e.getCause().getMessage());
            } else {
                throw new CommandException("Ошибка входа: " + msg);
            }
        }
    }
}