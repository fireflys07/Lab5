package ru.itmo.anya.mark.interpreter;

import java.util.Scanner;

public abstract class Command {
    private final boolean reqAdditionalInput;
    protected Command(boolean reqAdditionalInput) {
        this.reqAdditionalInput = reqAdditionalInput;
    }

    public boolean isReqAdditionalInput() {
        return reqAdditionalInput;
    }
    public void checkArgs(String[] args) throws CommandException {
        // по умолчанию ничего не проверяем
    }
    public void readAdditionalInput(Environment env) throws CommandException {
        // по умолчанию не нужен
    }
    public abstract void execute(Environment environment, String[] args) throws CommandException;

    public abstract String getName();

    public String getDescription() {
        return "Нет описания";
    }

    public String getHelp() {
        return "Нет справки";
    }
}