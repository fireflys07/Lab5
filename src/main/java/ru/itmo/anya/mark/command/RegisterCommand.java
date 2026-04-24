package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandArgsException;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;

public final class RegisterCommand extends BaseCommand {

    private String login;
    private String password;

    public RegisterCommand(Environment env) {
        super(env, false);
    }

    @Override
    public String getName() { return "register"; }

    @Override
    public String getHelp() { return "зарегистрировать нового пользователя: register <login> <password>"; }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandArgsException("формат: register <login> <password>");
        }
        login = args[0].trim();
        password = args[1];
        if (login.isEmpty()) {
            throw new CommandArgsException("login не может быть пустым");
        }
        if (password.length() < 4) {
            throw new CommandArgsException("пароль должен быть не менее 4 символов");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        try {
            if (env.getAuthService().register(login, password)) {
                System.out.println("OK пользователь зарегистрирован: " + login);
            } else {
                throw new CommandException("пользователь с таким логином уже существует");
            }
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }
}