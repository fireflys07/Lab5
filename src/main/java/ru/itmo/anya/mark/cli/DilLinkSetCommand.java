package ru.itmo.anya.mark.cli;

import ru.itmo.anya.mark.model.DilutionSourceType;

import java.util.Locale;
import java.util.Set;

public final class DilLinkSetCommand extends BaseCommand {

    private final Set<Long> knownSampleIds;
    private final Set<Long> knownSolutionIds;

    public DilLinkSetCommand(Environment env,
                             Set<Long> knownSampleIds,
                             Set<Long> knownSolutionIds) {
        super(env);
        this.knownSampleIds = knownSampleIds;
        this.knownSolutionIds = knownSolutionIds;
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Ошибка: формат: dil_link_set <series_id>");
            return;
        }

        long seriesId;
        try {
            seriesId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Ошибки: series_id не число");
            return;
        }

        System.out.print("Источник (SAMPLE|SOLUTION): ");
        if (!env.getScanner().hasNextLine()) {
            System.out.println("Ошибки: не удалось прочитать тип");
            return;
        }
        String rawType = env.getScanner().nextLine().trim();
        DilutionSourceType type = parseSourceTypeOrNull(rawType);
        if (type == null) {
            System.out.println("Ошибки: неизвестный тип");
            return;
        }

        System.out.print("ID источника: ");
        if (!env.getScanner().hasNextLine()) {
            System.out.println("Ошибки: не удалось прочитать id");
            return;
        }
        String rawSourceId = env.getScanner().nextLine().trim();

        long sourceId;
        try {
            sourceId = Long.parseLong(rawSourceId);
        } catch (NumberFormatException e) {
            System.out.println("Ошибки: id не найден");
            return;
        }

        if (!isKnownSourceId(type, sourceId)) {
            System.out.println("Ошибки: id не найден");
            return;
        }

        try {
            env.getService().linkSource(seriesId, type, sourceId);
            System.out.println("OK");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
            if (msg.contains("series")) {
                System.out.println("Ошибки: series не найден");
            } else {
                System.out.println("Ошибки: " + e.getMessage());
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

