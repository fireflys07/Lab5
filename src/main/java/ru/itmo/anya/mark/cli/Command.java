package ru.itmo.anya.mark.cli;

public abstract class Command {

    private final boolean reqAdditionalInput;

    protected Command(boolean reqAdditionalInput) {
        this.reqAdditionalInput = reqAdditionalInput;
    }

    /**
     * Нужен ли для команды дополнительный ввод помимо аргументов.
     */
    public boolean isReqAdditionalInput() {
        return reqAdditionalInput;
    }

    /**
     * Проверка аргументов команды.
     *
     * @param args аргументы команды (без имени команды)
     * @throws CommandException если аргументы некорректны
     */
    public void checkArgs(String[] args) throws CommandException {
        // по умолчанию ничего не проверяем
    }

    /**
     * Дополнительный ввод от пользователя (если isReqAdditionalInput == true).
     *
     * @param env среда выполнения
     * @throws CommandException при ошибках ввода
     */
    public void readAdditionalInput(Environment env) throws CommandException {
        // по умолчанию не нужен
    }

    /**
     * Выполнить логику команды.
     *
     * @param args аргументы команды (без имени команды)
     * @throws CommandException при ошибках выполнения
     */
    public abstract void execute(String[] args) throws CommandException;

    /**
     * Краткое описание команды для вывода в help.
     */
    public abstract String getHelp();
}
