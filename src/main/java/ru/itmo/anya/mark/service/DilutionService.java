package ru.itmo.anya.mark.service;


import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;
import ru.itmo.anya.mark.storage.CollectionStorage;
import ru.itmo.anya.mark.storage.CsvCollectionStorage;
import ru.itmo.anya.mark.storage.FileValidator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class DilutionService {


    private static final String RELATIVE_DATA_ROOT = "data";

    private final SeriesCollectionManager seriesManager;
    private final DilutionStepManager stepManager;

    private final CollectionStorage<DilutionSeries> seriesStorage;
    private final CollectionStorage<DilutionStep> stepStorage;

    public DilutionService(SeriesCollectionManager seriesManager, DilutionStepManager stepManager) {
        this.seriesManager = seriesManager;
        this.stepManager = stepManager;

        this.seriesStorage = new CsvCollectionStorage<>(DilutionSeries.class);
        this.stepStorage = new CsvCollectionStorage<>(DilutionStep.class);
        FileValidator fileValidator = new FileValidator();
    }
    // 7) dil_calc – возвращает список концентраций по шагам
    public List<Double> calculateConcentrations(long seriesId, double startConc) {
        // Получаем серию
        DilutionSeries series = seriesManager.getById(seriesId);
        if (series == null) {
            throw new IllegalArgumentException("Series not found: " + seriesId);
        }

        // Получаем шаги серии
        List<DilutionStep> steps = stepManager.getStepsBySeriesId(seriesId);
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Series has no dilution steps");
        }

        // Сортируем шаги по номеру
        steps.sort((s1, s2) -> Integer.compare(s1.getStepNumber(), s2.getStepNumber()));

        // Рассчитываем концентрации
        List<Double> results = new ArrayList<>();
        double currentConc = startConc;

        for (DilutionStep step : steps) {
            currentConc = currentConc / step.getFactor();
            results.add(currentConc);
        }

        return results;
    }
    // 8) dil_step_update
    public void updateStepFactor(long stepId, double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Factor must be positive");
        }

        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }

        step.setFactor(factor);
    }

    public void updateStepFinalQuantity(long stepId, double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }

        step.setFinalQuantity(quantity);
    }
    // 9) dil_step_delete
    public DilutionStep getStepById(long stepId) {
        return stepManager.getById(stepId);
    }

    public void deleteStep(long stepId) {
        DilutionStep step = stepManager.getById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }

        stepManager.remove(stepId);
        System.out.println("Шаг с ID " + stepId + " удалён из хранилища");
    }

    // 10) dil_export
    public String exportSeries(long seriesId) {
        DilutionSeries series = seriesManager.getById(seriesId);
        if (series == null) {
            throw new IllegalArgumentException("Серия с ID " + seriesId + " не найдена");
        }

        List<DilutionStep> steps = stepManager.getStepsBySeriesId(seriesId);

        StringBuilder sb = new StringBuilder();
        sb.append("=== Dilution Series #").append(seriesId).append(" ===\n");
        sb.append("Name: ").append(series.getName()).append("\n");
        sb.append("Owner: ").append(series.getOwnerUsername()).append("\n");

        if (series.getSourceType() != null) {
            sb.append("Source: ").append(series.getSourceType())
                    .append(" #").append(series.getSourceId()).append("\n");
        }

        sb.append("\nSteps:\n");
        sb.append(String.format("%-6s %-6s %-8s %-10s %s\n",
                "ID", "Step", "Factor", "Quantity", "Unit"));

        for (DilutionStep step : steps) {
            sb.append(String.format("%-6d %-6d %-8.2f %-10.2f %s\n",
                    step.getId(),
                    step.getStepNumber(),
                    step.getFactor(),
                    step.getFinalQuantity(),
                    step.getFinalUnit()));
        }

        return sb.toString();
    }

    // 1) dil_series_create – только валидация
    public DilutionSeries createSeries(String name, DilutionSourceType sourceType, long sourceId, String ownerUsername) {
        if (name == null || name.trim().isEmpty() || name.length() > 128) {
            throw new IllegalArgumentException("name: пустое или слишком длинное");
        }
        if (sourceType == null) {
            throw new IllegalArgumentException("sourceType: null");
        }
        if (sourceId <= 0) {
            throw new IllegalArgumentException("sourceId: должен быть > 0");
        }
        if (ownerUsername == null || ownerUsername.trim().isEmpty()) {
            ownerUsername = "SYSTEM";
        }

        long id = seriesManager.getSeriesNextID();
        DilutionSeries series = new DilutionSeries(id, Instant.now());
        series.setName(name);
        series.setSourceType(sourceType);
        series.setSourceId(sourceId);
        series.setOwnerUsername(ownerUsername);

        seriesManager.add(series);
        return series;
    }

    // 2) dil_series_list
    public List<DilutionSeries> listSeries() {
        return new ArrayList<>(seriesManager.getSeries());
    }

    // 3) dil_series_show
    public DilutionSeries getSeries(long seriesId) {
        DilutionSeries s = seriesManager.getById(seriesId);
        if (s == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        return s;
    }

    // 4) dil_step_add
    public DilutionStep addStep(long seriesId, int stepNumber, double factor, double finalQuantity, FinalQuantityUnit unit) {
        if (seriesManager.getById(seriesId) == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        if (stepNumber <= 0) {
            throw new IllegalArgumentException("stepNumber: должен быть > 0");
        }
        if (factor <= 0) {
            throw new IllegalArgumentException("factor: должен быть > 0");
        }
        if (finalQuantity <= 0) {
            throw new IllegalArgumentException("finalQuantity: должен быть > 0");
        }
        if (unit == null) {
            throw new IllegalArgumentException("не может быть: null");
        }

        long stepId = stepManager.getStepsNextID();
        DilutionStep step = new DilutionStep(stepId, seriesId, stepNumber, factor, finalQuantity, unit, Instant.now());
        stepManager.add(step);
        return step;
    }

    // 5) dil_step_list
    public List<DilutionStep> listSteps(long seriesId) {
        if (seriesManager.getById(seriesId) == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        List<DilutionStep> result = new ArrayList<>();
        for (DilutionStep st : stepManager.getSteps()) {
            if (st.getSeriesId() == seriesId) {
                result.add(st);
            }
        }

        return result;
    }

    // 6) dil_link_set
    public void linkSource(long seriesId, DilutionSourceType type, long sourceId) {
        DilutionSeries s = seriesManager.getById(seriesId);
        if (s == null) {
            throw new IllegalArgumentException("series: не найден");
        }
        if (type == null) {
            throw new IllegalArgumentException("sourceType: неизвестный тип");
        }
        if (sourceId <= 0) {
            throw new IllegalArgumentException("sourceId: должен быть > 0");
        }
        s.setSourceType(type);
        s.setSourceId(sourceId);
    }

    private static Path resolveCsvBasePath(String basePath) {
        Path p = Path.of(basePath.trim());
        if (p.isAbsolute()) {
            return p.toAbsolutePath().normalize();
        }
        return Path.of(RELATIVE_DATA_ROOT).resolve(p).normalize();
    }

    /** База — путь .../stem (без суффикса); файлы в той же папке: stem_series.csv и stem_step.csv. */
    private static Path csvSeriesFileNextToStem(Path stemBase) {
        Path parent = stemBase.getParent();
        String stem = stemBase.getFileName().toString();
        return parent == null ? Path.of(stem + "_series.csv") : parent.resolve(stem + "_series.csv");
    }

    private static Path csvStepFileNextToStem(Path stemBase) {
        Path parent = stemBase.getParent();
        String stem = stemBase.getFileName().toString();
        return parent == null ? Path.of(stem + "_step.csv") : parent.resolve(stem + "_step.csv");
    }

    /**
     * Приводит ввод к «базе» пары файлов: {@code stem} → {@code stem_series.csv} и {@code stem_step.csv}
     * в одной папке с {@code stem}.
     * <ul>
     *   <li>Путь, оканчивающийся на {@code *_series.csv} или {@code *_step.csv}, трактуется как один из файлов пары.</li>
     *   <li>Если указан существующий каталог и {@code scanDirectory}, ищется единственная пара внутри него.</li>
     * </ul>
     */
    private static Path resolveCsvPairBase(String basePath, boolean scanDirectory) throws Exception {
        Path p = resolveCsvBasePath(basePath);
        Path fileName = p.getFileName();
        if (fileName != null) {
            String name = fileName.toString();
            String lc = name.toLowerCase(Locale.ROOT);
            if (lc.endsWith("_series.csv")) {
                String stem = name.substring(0, name.length() - "_series.csv".length());
                Path parent = p.getParent();
                return parent != null ? parent.resolve(stem) : Path.of(stem);
            }
            if (lc.endsWith("_step.csv")) {
                String stem = name.substring(0, name.length() - "_step.csv".length());
                Path parent = p.getParent();
                return parent != null ? parent.resolve(stem) : Path.of(stem);
            }
        }
        if (scanDirectory && Files.exists(p) && Files.isDirectory(p)) {
            try {
                return findSingleCsvPairBaseInDirectory(p);
            } catch (Exception first) {
                Path dataDir = p.resolve(RELATIVE_DATA_ROOT);
                if (Files.isDirectory(dataDir)) {
                    return findSingleCsvPairBaseInDirectory(dataDir);
                }
                throw first;
            }
        }
        if (!scanDirectory && Files.exists(p) && Files.isDirectory(p)) {
            throw new Exception("для save укажите базовое имя (например mydata), а не каталог");
        }
        return p;
    }

    private static Path findSingleCsvPairBaseInDirectory(Path dir) throws Exception {
        List<Path> bases = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            for (Path seriesFile : stream.toList()) {
                if (!Files.isRegularFile(seriesFile)) {
                    continue;
                }
                Path onlyName = seriesFile.getFileName();
                if (onlyName == null) {
                    continue;
                }
                String name = onlyName.toString();
                String lower = name.toLowerCase(Locale.ROOT);
                if (!lower.endsWith("_series.csv")) {
                    continue;
                }
                String stem = name.substring(0, name.length() - "_series.csv".length());
                Path stepFile = dir.resolve(stem + "_step.csv");
                if (Files.exists(stepFile) && Files.isRegularFile(stepFile)) {
                    bases.add(dir.resolve(stem));
                }
            }
        }
        if (bases.isEmpty()) {
            throw new Exception("В каталоге нет пары файлов *_series.csv и *_step.csv: " + dir);
        }
        if (bases.size() > 1) {
            throw new Exception("В каталоге несколько пар CSV — укажите базовое имя или путь к одному из файлов");
        }
        return bases.get(0);
    }

    /**
     * Базовый путь пары файлов (для сообщений в UI).
     * {@code scanDirectory} — как в {@link #loadFromCsv}: искать единственную пару в каталоге.
     */
    public Path resolveCsvDataPath(String basePath, boolean scanDirectory) throws Exception {
        return resolveCsvPairBase(basePath, scanDirectory);
    }

    /** Пути к CSV для уже вычисленной базы stem (как в {@link #resolveCsvDataPath}). */
    public Path getCsvSeriesPathFromStem(Path stemBase) {
        return csvSeriesFileNextToStem(stemBase);
    }

    public Path getCsvStepPathFromStem(Path stemBase) {
        return csvStepFileNextToStem(stemBase);
    }

    public void saveToCsv(String basePath) throws Exception {
        Path base = resolveCsvPairBase(basePath, false);
        Path seriesPath = csvSeriesFileNextToStem(base);
        Path stepsPath = csvStepFileNextToStem(base);

        seriesStorage.save(seriesManager.getSeries(), seriesPath);
        stepStorage.save(stepManager.getSteps(), stepsPath);
    }

    public void loadFromCsv(String basePath) throws Exception {
        Path base = resolveCsvPairBase(basePath, true);
        Path seriesPath = csvSeriesFileNextToStem(base);
        Path stepsPath = csvStepFileNextToStem(base);

        // Проверяем файлы
        if (!Files.exists(seriesPath)) {
            throw new Exception("Файл не найден: " + seriesPath);
        }
        if (!Files.exists(stepsPath)) {
            throw new Exception("Файл не найден: " + stepsPath);
        }

        // Загружаем
        List<DilutionSeries> loadedSeries = seriesStorage.load(seriesPath);
        List<DilutionStep> loadedSteps = stepStorage.load(stepsPath);

        // Проверяем целостность
        Set<Long> seriesIds = new HashSet<>();
        for (DilutionSeries s : loadedSeries) {
            seriesIds.add(s.getId());
        }

        for (DilutionStep step : loadedSteps) {
            if (!seriesIds.contains(step.getSeriesId())) {
                throw new Exception("Шаг id=" + step.getId() +
                        " ссылается на несуществующую серию: seriesId=" + step.getSeriesId());
            }
        }

        // Заменяем данные
        seriesManager.clear();
        stepManager.clear();

        seriesManager.addAll(loadedSeries);
        stepManager.addAll(loadedSteps);
    }
}
