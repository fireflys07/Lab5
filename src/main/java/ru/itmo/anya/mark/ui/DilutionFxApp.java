package ru.itmo.anya.mark.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import ru.itmo.anya.mark.model.User;
import ru.itmo.anya.mark.storage.CsvUserStorage;
import ru.itmo.anya.mark.service.AuthService;
import ru.itmo.anya.mark.storage.DatabaseConnection;

import java.nio.file.Paths;


public class DilutionFxApp extends Application {

    private static final String TINY_GIF_B64 = "R==";

    private static final String CARD_NORMAL =
            "-fx-border-color: #b9b9b9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String CARD_SELECTED =
            "-fx-border-color: #2a6fdb; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; "
                    + "-fx-background-color: #f0f6ff; -fx-cursor: hand;";

    private String currentUsername;
    private final Path usersFile = Paths.get("users.csv");
    private final CsvUserStorage userStorage = new CsvUserStorage();
    private AuthService authService;
    private Label userStatusLabel;

    private final SeriesCollectionManager seriesManager = new SeriesCollectionManager();
    private final DilutionStepManager stepManager = new DilutionStepManager();
    private DilutionService service;

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
        DatabaseConnection.getInstance().testConnection();
        try {
            userStorage.load(usersFile);
        } catch (Exception e) {
            // Файл не существует
        }
        authService = new AuthService(userStorage, usersFile);
        service = new DilutionService(seriesManager, stepManager, authService);

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
        // передаем InputStream напрямую и используем конструктор с 5 аргументами
        try (InputStream in = DilutionFxApp.class.getResourceAsStream("/funny.gif")) {
            if (in != null) {
                return new Image(in, 160, 160, true, true);
            }
        } catch (IOException ignored) {
        }

        Path userGif = Paths.get(System.getProperty("user.home"), ".lab5-funny.gif");
        if (Files.isRegularFile(userGif)) {

            return new Image(userGif.toUri().toString(), 160, 160, true, true, true);
        }

        // передаем ByteArrayInputStream напрямую
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

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> showRegisterDialog());

        Button authButton = new Button("Login");
        authButton.setOnAction(e -> {
            if (currentUsername == null) {
                showLoginDialog();
            } else {
                authService.logout();
                currentUsername = null;
                statusLabel.setText("Выход выполнен");
                authButton.setText("Login");
                updateAuthUI();
                renderCards();
            }
        });

        userStatusLabel = new Label("Гость");
        userStatusLabel.setStyle("-fx-font-weight: bold;");

        HBox row = new HBox(8,
                new Label("Data path:"),
                dataPathField,
                loadButton,
                saveButton,
                refreshButton,
                showGifButton,
                new Region(),  // Разделитель (растягивается)
                new Label("Пользователь:"),
                userStatusLabel,
                registerButton,
                authButton
        );

        HBox.setHgrow(row.getChildren().get(6), Priority.ALWAYS);  // Разделитель растёт
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
            try {
                if (currentUsername == null) {
                    throw new SecurityException("Требуется авторизация для создания серии");
                }

                String name = nameField.getText().trim();
                long sourceId = Long.parseLong(sourceIdField.getText().trim());

                service.createSeries(name, sourceTypeBox.getValue(), sourceId, currentUsername);

                // ← АВТОМАТИЧЕСКИ СОХРАНЯЕМ В ФАЙЛ
                if (lastDataPath != null && !lastDataPath.isEmpty()) {
                    service.saveToCsv(lastDataPath);
                }

                Platform.runLater(() -> {
                    nameField.clear();
                    statusLabel.setText("Серия добавлена и сохранена. Владелец: " + currentUsername);
                    renderCards();
                });

            } catch (Exception ex) {
                System.out.println("EXCEPTION in createSeries: " + ex.getMessage());
                ex.printStackTrace();
                throw ex;
            }
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
        addStepButton.setOnAction(e -> {
            if (currentUsername == null) {
                showError("Требуется авторизация. Пожалуйста, войдите в систему.");
                return;
            }

            runInBackground("add-step", () -> {
                try {
                    long seriesId = Long.parseLong(stepSeriesIdField.getText().trim());
                    int stepNum = Integer.parseInt(stepNumField.getText().trim());
                    double factor = Double.parseDouble(factorField.getText().trim());
                    double finalQty = Double.parseDouble(finalQtyField.getText().trim());

                    DilutionSeries series = service.getSeries(seriesId);
                    if (series == null) {
                        throw new IllegalArgumentException("Серия не найдена");
                    }

                    String currentUser = currentUsername;  // или authService.getCurrentUser()
                    if (currentUser == null || !currentUser.equals(series.getOwnerUsername())) {
                        throw new SecurityException(
                                "Ошибка: у вас нет прав на добавление шагов к этой серии. " +
                                        "Владелец: " + series.getOwnerUsername()
                        );
                    }

                    service.addStep(seriesId, stepNum, factor, finalQty, unitBox.getValue());

                    if (lastDataPath != null && !lastDataPath.isEmpty()) {
                        service.saveToCsv(lastDataPath);
                    }

                    Platform.runLater(() -> {
                        statusLabel.setText("Шаг добавлен и сохранён");
                        renderCards();  // Обновляем список
                    });

                } catch (Exception ex) {
                    throw ex;
                }
            }, false);
        });

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

            Button deleteButton = new Button("Удалить");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15; -fx-border-radius: 5; -fx-background-radius: 5;");
            deleteButton.setOnAction(e -> confirmDeleteSeries(s));

            // Проверяем права: кнопки активны только если текущий пользователь — владелец
            String currentUser = currentUsername;  // или authService.getCurrentUser()
            boolean isOwner = currentUser != null && currentUser.equals(s.getOwnerUsername());
            deleteButton.setDisable(!isOwner);

            if (!isOwner) {
                deleteButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-padding: 5 15; -fx-border-radius: 5; -fx-background-radius: 5;");
                deleteButton.setTooltip(new Tooltip("Нет прав на удаление"));
            }

            HBox buttonBox = new HBox(10, deleteButton);

            card.getChildren().addAll(title, source, meta, hint, buttonBox);

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

        VBox editBox = new VBox(10);  // Отступ 10px между строками
        editBox.setPadding(new Insets(8, 0, 0, 0));

        boolean isOwner = currentUsername != null && currentUsername.equals(fresh.getOwnerUsername());

        Label nameLabel = new Label("Изменить название серии:");
        TextField nameField = new TextField(fresh.getName());
        nameField.setPromptText("Новое название");
        nameField.setPrefWidth(200);
        Button applyName = new Button("Применить");

        if (!isOwner) {
            nameField.setDisable(true);
            applyName.setDisable(true);
            applyName.setTooltip(new Tooltip("Нет прав на изменение"));
        }

        applyName.setOnAction(ev -> {
            try {
                String newName = nameField.getText().trim();
                if (newName.isEmpty()) {
                    showError("Название не может быть пустым");
                    return;
                }
                if (newName.length() > 128) {
                    showError("Название не может быть длиннее 128 символов");
                    return;
                }

                runInBackground("update-series-name", () -> {
                    // Обновляем серию
                    service.updateSeries(fresh.getId(), newName, fresh.getSourceType(), fresh.getSourceId());

                    // Автосохранение
                    if (lastDataPath != null && !lastDataPath.isEmpty()) {
                        service.saveToCsv(lastDataPath);
                    }

                    Platform.runLater(() -> {
                        // Обновляем информацию в диалоге
                        DilutionSeries updated = service.getSeries(fresh.getId());
                        details.setText(buildSeriesDetailsTextFromService(updated));
                        statusLabel.setText("Название серии обновлено");
                        renderCards();  // Обновляем список карточек
                    });
                }, false);

            } catch (Exception ex) {
                showError("Ошибка: " + ex.getMessage());
            }
        });

        HBox nameRow = new HBox(10, nameLabel, nameField, applyName);
        editBox.getChildren().add(nameRow);

        Label linkLabel = new Label("Сменить источник (link):");
        ComboBox<DilutionSourceType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(DilutionSourceType.values());
        typeBox.setValue(fresh.getSourceType());
        TextField sourceIdField = new TextField(Long.toString(fresh.getSourceId()));
        Button applyLink = new Button("Применить");

        if (!isOwner) {
            typeBox.setDisable(true);
            sourceIdField.setDisable(true);
            applyLink.setDisable(true);
            applyLink.setTooltip(new Tooltip("Нет прав на изменение"));
        }


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

        HBox linkRow = new HBox(10, linkLabel, typeBox, sourceIdField, applyLink);
        editBox.getChildren().add(linkRow);

        Label stepLabel = new Label("Редактирование шага по ID:");
        TextField stepIdField = new TextField();
        stepIdField.setPromptText("step id");
        stepIdField.setPrefWidth(120);
        TextField factorField = new TextField();
        factorField.setPromptText("factor");
        factorField.setPrefWidth(80);
        Button applyFactor = new Button("Обновить factor");

        if (!isOwner) {
            stepIdField.setDisable(true);
            factorField.setDisable(true);
            applyFactor.setDisable(true);
            applyFactor.setTooltip(new Tooltip("Нет прав на изменение"));
        }

        applyFactor.setOnAction(ev -> applyStepFactor(stepIdField, factorField, fresh.getId(), details));
        TextField qtyField = new TextField();
        qtyField.setPromptText("final qty");
        qtyField.setPrefWidth(80);
        Button applyQty = new Button("Обновить объём");

        if (!isOwner) {
            qtyField.setDisable(true);
            applyQty.setDisable(true);
            applyQty.setTooltip(new Tooltip("Нет прав на изменение"));
        }

        applyQty.setOnAction(ev -> applyStepQty(stepIdField, qtyField, fresh.getId(), details));

        HBox stepRow = new HBox(10, stepLabel, stepIdField, factorField, applyFactor, qtyField, applyQty);
        editBox.getChildren().add(stepRow);

        if (!isOwner) {
            Label noAccessLabel = new Label("У вас нет прав на редактирование этой серии");
            noAccessLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
            editBox.getChildren().add(noAccessLabel);
        }

        VBox content = new VBox(8, new Label("Подробная информация:"), details, editBox);
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

    /** Диалог регистрации */
    private void showRegisterDialog() {
        TextField loginField = new TextField();
        loginField.setPromptText("Логин (мин. 3 символа)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль (мин. 4 символа)");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Подтвердите пароль");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Логин:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Повтор:"), 0, 2);
        grid.add(confirmField, 1, 2);

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Регистрация");
        alert.setHeaderText("Создание нового аккаунта");
        alert.getDialogPane().setContent(grid);

        ButtonType registerBtn = new ButtonType("Зарегистрировать", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(registerBtn, cancelBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == registerBtn) {
                String login = loginField.getText().trim();
                String password = passwordField.getText();
                String confirm = confirmField.getText();

                if (login.length() < 3) {
                    showError("Логин должен содержать минимум 3 символа");
                    return;
                }
                if (password.length() < 4) {
                    showError("Пароль должен содержать минимум 4 символа");
                    return;
                }
                if (!password.equals(confirm)) {
                    showError("Пароли не совпадают");
                    return;
                }

                if (authService.register(login, password)) {
                    try {
                        // Создаём нового пользователя
                        User newUser = new User(login, password);

                        // Загружаем существующих из файла
                        List<User> usersToSave = new ArrayList<>();
                        for (User u : userStorage.load(usersFile)) {
                            usersToSave.add(u);
                        }

                        // Добавляем нового, если ещё нет
                        boolean exists = false;
                        for (User u : usersToSave) {
                            if (u.getLogin().equals(login)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            usersToSave.add(newUser);
                        }

                        // Сохраняем полный список в файл
                        userStorage.save(usersToSave, usersFile);

                    } catch (Exception e) {
                        showError("Ошибка сохранения: " + e.getMessage());
                        return;
                    }

                    statusLabel.setText("Регистрация успешна: " + login);
                    authService.login(login, password);
                    currentUsername = login;
                    updateAuthUI();
                    renderCards();
                } else {
                    showError("Пользователь уже существует");
                }
            }
        });
    }

    /** Диалог входа */
    private void showLoginDialog() {
        TextField loginField = new TextField();
        loginField.setPromptText("Логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Логин:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(passwordField, 1, 1);

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Вход");
        alert.setHeaderText("Введите данные для входа");
        alert.getDialogPane().setContent(grid);

        ButtonType loginBtn = new ButtonType("Войти", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(loginBtn, cancelBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == loginBtn) {
                String login = loginField.getText().trim();
                String password = passwordField.getText();

                try {
                    userStorage.load(usersFile);

                    var userOpt = userStorage.findByLogin(login);
                    if (userOpt.filter(user -> user.checkPassword(password)).isPresent()) {
                        authService.login(login, password);

                        currentUsername = login;
                        statusLabel.setText("Вход выполнен: " + login);
                        updateAuthUI();
                        renderCards();
                    } else {
                        showError("Неверный логин или пароль");
                    }
                } catch (Exception e) {
                    showError("Ошибка при входе: " + e.getMessage());
                }
            }
        });
    }

    /** Обновление UI авторизации */
    private void updateAuthUI() {
        if (userStatusLabel != null) {
            userStatusLabel.setText(currentUsername != null ? currentUsername : "Гость");
        }
    }

    /** Подтверждение удаления серии */
    private void confirmDeleteSeries(DilutionSeries series) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление серии #" + series.getId());
        alert.setContentText("Вы уверены, что хотите удалить серию \"" + series.getName() + "\"?\nЭто действие нельзя отменить.");

        ButtonType deleteBtn = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteBtn, cancelBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == deleteBtn) {
                runInBackground("delete-series", () -> {
                    service.deleteSeries(series.getId());

                    if (lastDataPath != null && !lastDataPath.isEmpty()) {
                        service.saveToCsv(lastDataPath);
                    }

                    Platform.runLater(() -> {
                        statusLabel.setText("Серия #" + series.getId() + " удалена");
                        renderCards();  // Обновляем список
                    });
                }, true);
            }
        });
    }

    /** Диалог редактирования серии */
    private void openEditDialog(DilutionSeries series) {
        DialogPane pane = new DialogPane();
        pane.setHeaderText("Редактирование серии #" + series.getId());

        // Поля для редактирования
        TextField nameField = new TextField(series.getName());
        nameField.setPromptText("Название серии");

        ComboBox<DilutionSourceType> sourceTypeBox = new ComboBox<>();
        sourceTypeBox.getItems().addAll(DilutionSourceType.values());
        sourceTypeBox.setValue(series.getSourceType());

        TextField sourceIdField = new TextField(Long.toString(series.getSourceId()));
        sourceIdField.setPromptText("ID источника");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Тип источника:"), 0, 1);
        grid.add(sourceTypeBox, 1, 1);
        grid.add(new Label("ID источника:"), 0, 2);
        grid.add(sourceIdField, 1, 2);

        pane.setContent(grid);

        ButtonType saveBtn = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().setAll(saveBtn, cancelBtn);

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setDialogPane(pane);
        alert.setTitle("Редактирование серии");
        alert.initOwner(cardsBox.getScene() == null ? null : cardsBox.getScene().getWindow());

        alert.showAndWait().ifPresent(response -> {
            if (response == saveBtn) {
                try {
                    String newName = nameField.getText().trim();
                    DilutionSourceType newType = sourceTypeBox.getValue();
                    long newSourceId = Long.parseLong(sourceIdField.getText().trim());

                    runInBackground("update-series", () -> {
                        service.updateSeries(series.getId(), newName, newType, newSourceId);

                        if (lastDataPath != null && !lastDataPath.isEmpty()) {
                            service.saveToCsv(lastDataPath);
                        }

                        Platform.runLater(() -> {
                            statusLabel.setText("Серия #" + series.getId() + " обновлена");
                            renderCards();  // Обновляем список
                        });
                    }, true);

                } catch (Exception e) {
                    showError("Ошибка при обновлении: " + e.getMessage());
                }
            }
        });
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static void main(String[] args) {
        launch(args);
    }
}