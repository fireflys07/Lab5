package ru.itmo.anya.mark.storage;

import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionStep;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileValidator {

    //Проверить существование и доступность файла.

    public void validateFileExists(Path path) throws Exception {
        if (!Files.exists(path)) {
            throw new Exception("Файл не существует: " + path);
        }
        if (!Files.isReadable(path)) {
            throw new Exception("Файл недоступен для чтения: " + path);
        }
        if (Files.size(path) == 0) {
            throw new Exception("Файл пустой: " + path);
        }
    }

    //Валидировать загруженные серии и шаги.
    public void validateData(List<DilutionSeries> series, List<DilutionStep> steps) throws Exception {
        List<String> errors = new ArrayList<>();

        // Проверка серий
        Set<Long> seriesIds = new HashSet<>();
        for (DilutionSeries s : series) {
            if (!seriesIds.add(s.getId())) {
                errors.add("Дублирующийся ID серии: " + s.getId());
            }

            try {
                validateSeries(s);
            } catch (IllegalArgumentException e) {
                errors.add("Серия id=" + s.getId() + ": " + e.getMessage());
            }
        }

        // Проверка шагов
        Set<Long> stepIds = new HashSet<>();
        for (DilutionStep step : steps) {
            if (!stepIds.add(step.getId())) {
                errors.add("Дублирующийся ID шага: " + step.getId());
            }

            // Проверка целостности ссылок
            if (!seriesIds.contains(step.getSeriesId())) {
                errors.add("Шаг id=" + step.getId() + " ссылается на несуществующую серию: seriesId=" + step.getSeriesId());
            }

            try {
                validateStep(step);
            } catch (IllegalArgumentException e) {
                errors.add("Шаг id=" + step.getId() + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new Exception("Ошибка валидации данных:\n" + String.join("\n", errors));
        }
    }

    private void validateSeries(DilutionSeries series) {
        if (series.getName() == null || series.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("поле name пустое");
        }
        if (series.getName().length() > 128) {
            throw new IllegalArgumentException("поле name длиннее 128 символов");
        }
        if (series.getSourceType() == null) {
            throw new IllegalArgumentException("поле sourceType null");
        }
        if (series.getSourceId() <= 0) {
            throw new IllegalArgumentException("поле sourceId должно быть > 0");
        }
        if (series.getOwnerUsername() == null || series.getOwnerUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("поле ownerUsername пустое");
        }
        if (series.getCreatedAt() == null) {
            throw new IllegalArgumentException("поле createdAt null");
        }
    }

    private void validateStep(DilutionStep step) {
        if (step.getStepNumber() <= 0) {
            throw new IllegalArgumentException("поле stepNumber должно быть > 0");
        }
        if (step.getFactor() <= 0) {
            throw new IllegalArgumentException("поле factor должно быть > 0");
        }
        if (step.getFinalQuantity() <= 0) {
            throw new IllegalArgumentException("поле finalQuantity должно быть > 0");
        }
        if (step.getFinalUnit() == null) {
            throw new IllegalArgumentException("поле finalUnit null");
        }
        if (step.getCreatedAt() == null) {
            throw new IllegalArgumentException("поле createdAt null");
        }
    }
}