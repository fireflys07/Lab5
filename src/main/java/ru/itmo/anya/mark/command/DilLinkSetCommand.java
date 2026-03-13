package ru.itmo.anya.mark.command;

import ru.itmo.anya.mark.cli.BaseCommand;
import ru.itmo.anya.mark.interpreter.CommandException;
import ru.itmo.anya.mark.interpreter.Environment;
import ru.itmo.anya.mark.model.DilutionSourceType;

import java.util.Locale;
import java.util.Set;

public final class DilLinkSetCommand extends BaseCommand {

    private final Set<Long> knownSampleIds;
    private final Set<Long> knownSolutionIds;

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

        long seriesId;
        try {
            seriesId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("series_id не число", e);
        }
        this.cachedSeriesId = seriesId;
    }

    private long cachedSeriesId;
    private String cachedType;
    private String cachedSourceId;

    @Override
    public void readAdditionalInput(Environment env) throws CommandException {
        System.out.print("Источник (SAMPLE|SOLUTION): ");
        if (!env.getScanner().hasNextLine()) {
            throw new CommandException("не удалось прочитать тип");
        }
        String rawType = env.getScanner().nextLine().trim();
        System.out.print("ID источника: ");
        if (!env.getScanner().hasNextLine()) {
            throw new CommandException("не удалось прочитать id");
        }
        String rawSourceId = env.getScanner().nextLine().trim();
        this.cachedType = rawType;
        this.cachedSourceId = rawSourceId;
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        long seriesId = cachedSeriesId;

        DilutionSourceType type = parseSourceTypeOrNull(cachedType);
        if (type == null) {
            throw new CommandException("неизвестный тип");
        }

        long sourceId;
        try {
            sourceId = Long.parseLong(cachedSourceId);
        } catch (NumberFormatException e) {
            throw new CommandException("id не найден");
        }

        if (!isKnownSourceId(type, sourceId)) {
            throw new CommandException("id не найден");
        }

        try {
            env.getService().linkSource(seriesId, type, sourceId);
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

