package ru.itmo.anya.mark.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;
import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;
import ru.itmo.anya.mark.service.DilutionService;
import ru.itmo.anya.mark.service.DilutionStepManager;
import ru.itmo.anya.mark.service.SeriesCollectionManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

/**
 * JavaFX UI: карточки серий, ручной Refresh из файла-источника, операции через {@link DilutionService}.
 */
public class DilutionFxApp extends Application {
    /** Минимальный прозрачный GIF (1×1), если нет своего файла. */
    private static final String TINY_GIF_B64 = "R0lGODdhAQABAPAAAAAAAAAAACH5BAEAAAEALAAAAAABAAEAAAICTAEAOw==";

    private static final String CARD_NORMAL =
            "-fx-border-color: #b9b9b9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String CARD_SELECTED =
            "-fx-border-color: #2a6fdb; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; "
                    + "-fx-background-color: #f0f6ff; -fx-cursor: hand;";

    private final SeriesCollectionManager seriesManager = new SeriesCollectionManager();
    private final DilutionStepManager stepManager = new DilutionStepManager();
    private final DilutionService service = new DilutionService(seriesManager, stepManager);

    private final VBox cardsBox = new VBox(10);
    private final ProgressBar progressBar = new ProgressBar();
    private final Label statusLabel = new Label("Готово");
    private final TextField dataPathField = new TextField("ui_state");

    private String lastDataPath;
    private VBox selectedCard;
    private BorderPane rootPane;
    private VBox funnyGifPanel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Dilution Manager (JavaFX)");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        this.rootPane = root;
        this.funnyGifPanel = buildFunnyGifPanel();

        VBox top = new VBox(8, buildToolbar(), buildActionsRow(), buildHintRow(), buildStatusRow());
        root.setTop(top);

        cardsBox.setPadding(new Insets(8));
        ScrollPane scrollPane = new ScrollPane(cardsBox);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        root.setRight(funnyGifPanel);

        renderCards();

        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }

    private VBox buildFunnyGifPanel() {
        ImageView gifView = new ImageView(loadFunnyGifImage());
        gifView.setPreserveRatio(true);
        gifView.setFitWidth(160);
        gifView.setFitHeight(160);

        Label caption = new Label("Смешнявка");
        caption.setStyle("-fx-font-weight: bold;");

        Label hint = new Label(
                "Своя гифка:\n"
                        + "thanks, for using our app\n"
                        + "made with love by mazzyha and anutaf");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");

        Button hideBtn = new Button("Скрыть панель");
        hideBtn.setOnAction(e -> rootPane.setRight(null));

        VBox box = new VBox(8, caption, gifView, hint, hideBtn);
        box.setPadding(new Insets(0, 0, 0, 12));
        box.setMaxWidth(200);
        return box;
    }

    private Image loadFunnyGifImage() {
        // Исправлено: передаем InputStream напрямую и используем конструктор с 5 аргументами
        try (InputStream in = DilutionFxApp.class.getResourceAsStream("/funny.gif")) {
            if (in != null) {
                return new Image(in, 160, 160, true, true);
            }
        } catch (IOException ignored) {
        }

        Path userGif = Paths.get(System.getProperty("user.home"), ".lab5-funny.gif");
        if (Files.isRegularFile(userGif)) {
            // Здесь передаем String (URL), поэтому аргументов может быть 6
            return new Image(userGif.toUri().toString(), 160, 160, true, true, true);
        }

        // Исправлено: передаем ByteArrayInputStream напрямую
        byte[] tiny = Base64.getDecoder().decode(TINY_GIF_B64);
        return new Image(new ByteArrayInputStream(tiny), 160, 160, true, true);
    }

    private HBox buildHintRow() {
        Label hint = new Label(
                "Карточка: основные поля. Выберите кликом; двойной клик — подробности и редактирование. "
                        + "Список обновляется только кнопками Load / Refresh (после Save сделайте Refresh).");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #555;");
        HBox row = new HBox(hint);
        row.setPadding(new Insets(0, 0, 4, 0));
        return row;
    }

    private HBox buildToolbar() {
        dataPathField.setPrefWidth(320);

        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> {
            String path = dataPathField.getText().trim();
            if (path.isEmpty()) {
                showError("Путь к данным пустой");
                return;
            }
            runInBackground("load", () -> {
                service.loadFromCsv(path);
                lastDataPath = path;
                Platform.runLater(() -> statusLabel.setText("Загружено из: " + path));
            }, true);
        });

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String path = dataPathField.getText().trim();
            if (path.isEmpty()) {
                showError("Путь к данным пустой");
                return;
            }
            runInBackground("save", () -> {
                service.saveToCsv(path);
                lastDataPath = path;
                Platform.runLater(() -> statusLabel.setText(
                        "Сохранено. Чтобы перечитать список из файла, нажмите Refresh."));
            }, false);
        });

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            if (lastDataPath == null || lastDataPath.isBlank()) {
                showError("Сначала выполните Load или Save, чтобы задать источник");
                return;
            }
            runInBackground("refresh", () -> {
                service.loadFromCsv(lastDataPath);
                Platform.runLater(() -> statusLabel.setText("Обновлено из источника: " + lastDataPath));
            }, true);
        });

        Button showGifButton = new Button("Показать GIF");
        showGifButton.setOnAction(e -> rootPane.setRight(funnyGifPanel));

        HBox row = new HBox(8,
                new Label("Data path:"), dataPathField,
                loadButton, saveButton, refreshButton, showGifButton);
        row.setPadding(new Insets(4, 0, 0, 0));
        return row;
    }

    private HBox buildActionsRow() {
        TextField nameField = new TextField();
        nameField.setPromptText("Название серии");

        ComboBox<DilutionSourceType> sourceTypeBox = new ComboBox<>();
        sourceTypeBox.getItems().addAll(DilutionSourceType.values());
        sourceTypeBox.setValue(DilutionSourceType.SAMPLE);

        TextField sourceIdField = new TextField("1");
        sourceIdField.setPrefWidth(80);

        Button createSeriesButton = new Button("Создать серию");
        createSeriesButton.setOnAction(e -> runInBackground("create-series", () -> {
            String name = nameField.getText().trim();
            long sourceId = Long.parseLong(sourceIdField.getText().trim());
            service.createSeries(name, sourceTypeBox.getValue(), sourceId, "UI_USER");
            Platform.runLater(() -> {
                nameField.clear();
                statusLabel.setText("Серия добавлена в память. Сохраните (Save) и обновите список (Refresh).");
            });
        }, false));

        TextField stepSeriesIdField = new TextField();
        stepSeriesIdField.setPromptText("seriesId");
        stepSeriesIdField.setPrefWidth(110);

        TextField stepNumField = new TextField();
        stepNumField.setPromptText("step");
        stepNumField.setPrefWidth(70);

        TextField factorField = new TextField();
        factorField.setPromptText("factor");
        factorField.setPrefWidth(80);

        TextField finalQtyField = new TextField();
        finalQtyField.setPromptText("qty");
        finalQtyField.setPrefWidth(80);

        ComboBox<FinalQuantityUnit> unitBox = new ComboBox<>();
        unitBox.getItems().addAll(FinalQuantityUnit.values());
        unitBox.setValue(FinalQuantityUnit.ML);

        Button addStepButton = new Button("Добавить шаг");
        addStepButton.setOnAction(e -> runInBackground("add-step", () -> {
            long seriesId = Long.parseLong(stepSeriesIdField.getText().trim());
            int stepNum = Integer.parseInt(stepNumField.getText().trim());
            double factor = Double.parseDouble(factorField.getText().trim());
            double finalQty = Double.parseDouble(finalQtyField.getText().trim());
            service.addStep(seriesId, stepNum, factor, finalQty, unitBox.getValue());
            Platform.runLater(() -> statusLabel.setText(
                    "Шаг добавлен в память. Сохраните (Save) и обновите список (Refresh)."));
        }, false));

        HBox row = new HBox(8,
                new Label("Серия:"), nameField, sourceTypeBox, sourceIdField, createSeriesButton,
                new Region(),
                new Label("Шаг:"), stepSeriesIdField, stepNumField, factorField, finalQtyField, unitBox, addStepButton
        );
        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(5), Priority.ALWAYS);
        row.setPadding(new Insets(4, 0, 0, 0));
        return row;
    }

    private HBox buildStatusRow() {
        progressBar.setVisible(false);
        progressBar.setPrefWidth(160);
        HBox row = new HBox(10, progressBar, statusLabel);
        row.setPadding(new Insets(4, 0, 0, 0));
        return row;
    }

    private void selectCard(VBox card) {
        if (selectedCard != null) {
            selectedCard.setStyle(CARD_NORMAL);
        }
        selectedCard = card;
        if (selectedCard != null) {
            selectedCard.setStyle(CARD_SELECTED);
        }
    }

    private void renderCards() {
        cardsBox.getChildren().clear();
        selectedCard = null;
        List<DilutionSeries> series = service.listSeries().stream()
                .sorted(Comparator.comparingLong(DilutionSeries::getId))
                .toList();

        if (series.isEmpty()) {
            cardsBox.getChildren().add(new Label("Коллекция пуста. Выполните Load или добавьте данные."));
            return;
        }

        for (DilutionSeries s : series) {
            int stepCount = (int) service.listSteps(s.getId()).stream()
                    .filter(st -> st.getSeriesId() == s.getId())
                    .count();

            VBox card = new VBox(6);
            card.setPadding(new Insets(10));
            card.setStyle(CARD_NORMAL);
            card.setCursor(Cursor.HAND);

            Label title = new Label("Серия #" + s.getId() + " — " + s.getName());
            title.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
            Label source = new Label("Источник: " + s.getSourceType() + " #" + s.getSourceId());
            Label meta = new Label("Владелец: " + s.getOwnerUsername() + " · шагов: " + stepCount);
            Label hint = new Label("Клик — выбор · двойной клик — подробности");
            hint.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

            card.getChildren().addAll(title, source, meta, hint);

            card.setOnMouseClicked(ev -> {
                selectCard(card);
                if (ev.getClickCount() == 2) {
                    openSeriesDialog(s);
                }
            });

            cardsBox.getChildren().add(card);
        }
    }

    private void openSeriesDialog(DilutionSeries series) {
        DilutionSeries fresh = service.getSeries(series.getId());

        DialogPane pane = new DialogPane();
        pane.setHeaderText("Серия #" + fresh.getId());

        TextArea details = new TextArea(buildSeriesDetailsTextFromService(fresh));
        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefRowCount(12);

        GridPane editGrid = new GridPane();
        editGrid.setHgap(8);
        editGrid.setVgap(8);
        editGrid.setPadding(new Insets(8, 0, 0, 0));

        Label linkLabel = new Label("Сменить источник (link):");
        ComboBox<DilutionSourceType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(DilutionSourceType.values());
        typeBox.setValue(fresh.getSourceType());
        TextField sourceIdField = new TextField(Long.toString(fresh.getSourceId()));
        Button applyLink = new Button("Применить");
        applyLink.setOnAction(ev -> {
            try {
                DilutionSourceType t = typeBox.getValue();
                long sid = Long.parseLong(sourceIdField.getText().trim());
                service.linkSource(fresh.getId(), t, sid);
                DilutionSeries updated = service.getSeries(fresh.getId());
                details.setText(buildSeriesDetailsTextFromService(updated));
                statusLabel.setText("Источник обновлён. Сохраните (Save) и Refresh для синхронизации с файлом.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        editGrid.add(linkLabel, 0, 0);
        editGrid.add(typeBox, 1, 0);
        editGrid.add(sourceIdField, 2, 0);
        editGrid.add(applyLink, 3, 0);

        Label stepEditLabel = new Label("Редактирование шага по ID:");
        TextField stepIdField = new TextField();
        stepIdField.setPromptText("step id");
        stepIdField.setPrefWidth(120);
        TextField factorField = new TextField();
        factorField.setPromptText("factor");
        factorField.setPrefWidth(80);
        Button applyFactor = new Button("Обновить factor");
        applyFactor.setOnAction(ev -> applyStepFactor(stepIdField, factorField, fresh.getId(), details));
        TextField qtyField = new TextField();
        qtyField.setPromptText("final qty");
        qtyField.setPrefWidth(80);
        Button applyQty = new Button("Обновить объём");
        applyQty.setOnAction(ev -> applyStepQty(stepIdField, qtyField, fresh.getId(), details));

        editGrid.add(stepEditLabel, 0, 1);
        HBox stepRow = new HBox(8, stepIdField, factorField, applyFactor, qtyField, applyQty);
        editGrid.add(stepRow, 1, 1, 3, 1);

        VBox content = new VBox(8, new Label("Подробная информация:"), details, editGrid);
        pane.setContent(content);

        ButtonType close = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().add(close);

        Alert fake = new Alert(Alert.AlertType.NONE);
        fake.setDialogPane(pane);
        fake.setTitle("Серия");
        fake.initOwner(cardsBox.getScene() == null ? null : cardsBox.getScene().getWindow());
        fake.showAndWait();
    }

    private void applyStepFactor(TextField stepIdField, TextField factorField, long seriesId, TextArea details) {
        try {
            long stepId = Long.parseLong(stepIdField.getText().trim());
            double f = Double.parseDouble(factorField.getText().trim());
            service.updateStepFactor(stepId, f);
            verifyStepBelongsToSeries(stepId, seriesId);
            details.setText(buildSeriesDetailsTextFromService(service.getSeries(seriesId)));
            statusLabel.setText("Factor обновлён. Save + Refresh для файла.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void applyStepQty(TextField stepIdField, TextField qtyField, long seriesId, TextArea details) {
        try {
            long stepId = Long.parseLong(stepIdField.getText().trim());
            double q = Double.parseDouble(qtyField.getText().trim());
            service.updateStepFinalQuantity(stepId, q);
            verifyStepBelongsToSeries(stepId, seriesId);
            details.setText(buildSeriesDetailsTextFromService(service.getSeries(seriesId)));
            statusLabel.setText("Объём обновлён. Save + Refresh для файла.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void verifyStepBelongsToSeries(long stepId, long seriesId) {
        DilutionStep st = service.getStepById(stepId);
        if (st == null || st.getSeriesId() != seriesId) {
            throw new IllegalArgumentException("Шаг не принадлежит этой серии или не найден");
        }
    }

    private String buildSeriesDetailsTextFromService(DilutionSeries s) {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(s.getId()).append('\n');
        sb.append("name: ").append(s.getName()).append('\n');
        sb.append("sourceType: ").append(s.getSourceType()).append('\n');
        sb.append("sourceId: ").append(s.getSourceId()).append('\n');
        sb.append("owner: ").append(s.getOwnerUsername()).append('\n');
        sb.append("createdAt: ").append(s.getCreatedAt()).append('\n');
        sb.append("updatedAt: ").append(s.getUpdatedAt()).append('\n');
        sb.append("\n--- Шаги ---\n");
        List<DilutionStep> steps = service.listSteps(s.getId()).stream()
                .sorted(Comparator.comparingInt(DilutionStep::getStepNumber))
                .toList();
        if (steps.isEmpty()) {
            sb.append("(нет шагов)\n");
        } else {
            for (DilutionStep st : steps) {
                sb.append("step ").append(st.getStepNumber())
                        .append(" | id=").append(st.getId())
                        .append(" | factor=").append(st.getFactor())
                        .append(" | finalQuantity=").append(st.getFinalQuantity())
                        .append(" ").append(st.getFinalUnit())
                        .append(" | createdAt=").append(st.getCreatedAt())
                        .append('\n');
            }
        }
        return sb.toString();
    }

    private void runInBackground(String operationName, ThrowingRunnable operation, boolean redrawAfterSuccess) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                operation.run();
                return null;
            }
        };

        progressBar.visibleProperty().bind(task.runningProperty());
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnRunning(e -> statusLabel.setText("Выполняется: " + operationName + "..."));
        task.setOnSucceeded(e -> {
            progressBar.visibleProperty().unbind();
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            if (redrawAfterSuccess) {
                renderCards();
            }
        });
        task.setOnFailed(e -> {
            progressBar.visibleProperty().unbind();
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            Throwable ex = task.getException();
            showError(ex == null ? "Неизвестная ошибка" : ex.getMessage());
            statusLabel.setText("Ошибка операции: " + operationName);
        });

        Thread thread = new Thread(task, "ui-task-" + operationName);
        thread.setDaemon(true);
        thread.start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Операция не выполнена");
        alert.setContentText(message == null ? "Неизвестная ошибка" : message);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static void main(String[] args) {
        launch(args);
    }
}