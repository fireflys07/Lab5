package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;
import ru.itmo.anya.mark.model.DilutionSourceType;
import ru.itmo.anya.mark.validation.ValidationException;
import ru.itmo.anya.mark.validation.Validators;

import java.util.Locale;
import java.util.Set;

public final class DilLinkSetCommand extends BaseCommand {

    private final Set<Long> knownSampleIds;
    private final Set<Long> knownSolutionIds;
    private Long cachedSeriesId;
    private DilutionSourceType cachedType;
    private Long cachedSourceId;

    public DilLinkSetCommand(Environment env,
                             Set<Long> knownSampleIds,
                             Set<Long> knownSolutionIds) {
        super(env, true);
        this.knownSampleIds = knownSampleIds;
        this.knownSolutionIds = knownSolutionIds;
    }

    @Override
    public String getName() {
        return "dil_link_set";
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("формат: dil_link_set <series_id>");
        }

        try {
            long seriesId = Validators.validateId(args[0]);
            this.cachedSeriesId = seriesId;
        } catch (ValidationException e) {
            throw new CommandException(e.getMessage());
        }
    }


    @Override
    public void readAdditionalInput(Environment env) throws CommandException {
        try {
            System.out.print("Источник (SAMPLE|SOLUTION): ");
            if (!env.getScanner().hasNextLine()) {
                throw new CommandException("не удалось прочитать тип");
            }
            String rawType = env.getScanner().nextLine().trim();
            this.cachedType = Validators.validateSourceType(rawType);

            System.out.print("ID источника: ");
            if (!env.getScanner().hasNextLine()) {
                throw new CommandException("не удалось прочитать id");
            }
            String rawSourceId = env.getScanner().nextLine().trim();
            this.cachedSourceId = Validators.validateId(rawSourceId);
        } catch (ValidationException e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        if (cachedSeriesId == null) {
            throw new CommandException("Ошибка: ID серии не был установлен");
        }
        if (cachedType == null) {
            throw new CommandException("Ошибка: тип источника не был установлен");
        }
        if (cachedSourceId == null) {
            throw new CommandException("Ошибка: ID источника не был установлен");
        }

        DilutionSourceType type = cachedType;

        if (!isKnownSourceId(type, cachedSourceId)) {
            throw new CommandException("id не найден");
        }

        try {
            env.getService().linkSource(cachedSeriesId, type, cachedSourceId);
            System.out.println("OK");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
            if (msg.contains("series")) {
                throw new CommandException("series не найден");
            } else {
                throw new CommandException(e.getMessage());
            }
        }
    }

    private static DilutionSourceType parseSourceTypeOrNull(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty()) {
            return null;
        }
        try {
            return DilutionSourceType.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isKnownSourceId(DilutionSourceType type, long sourceId) {
        if (sourceId <= 0) {
            return false;
        }
        return switch (type) {
            case SAMPLE -> knownSampleIds.contains(sourceId);
            case SOLUTION -> knownSolutionIds.contains(sourceId);
        };
    }

    @Override
    public String getHelp() {
        return "указать источник серии (sample/solution)";
    }
}

