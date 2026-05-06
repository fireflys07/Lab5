package ru.itmo.anya.mark.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javafx.util.Duration;
import ru.itmo.anya.mark.model.*;
import ru.itmo.anya.mark.service.*;
import ru.itmo.anya.mark.storage.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Comparator;

public class DilutionFxApp extends Application {

    private static final String TINY_GIF_B64 = "";

    private static final String CARD_NORMAL =
            "-fx-border-color: #b9b9b9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String CARD_SELECTED =
            "-fx-border-color: #2a6fdb; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; " +
                    "-fx-background-color: #f0f6ff; -fx-cursor: hand;";

    private String currentUsername;
    private AuthService authService;
    private DilutionService service;
    private SeriesCollectionManager seriesManager;
    private DilutionStepManager stepManager;

    private Label userStatusLabel;
    private final VBox cardsBox = new VBox(10);
    private final ProgressBar progressBar = new ProgressBar();
    private final Label statusLabel = new Label("Готово");
    private boolean databaseConnectionFailed = false;

    private VBox selectedCard;
    private BorderPane rootPane;
    private VBox funnyGifPanel;
    private Stage primaryStage;
    private ImageView gifView;
    private final String[] GifNames = {"funny1.gif", "funny2.gif", "funny3.gif"};
    private final java.util.Random random = new java.util.Random();

    @Override


    public void start(Stage stage) {
        this.primaryStage = stage;

        // Пробуем подключиться к БД
        try {
            DatabaseConnection.getInstance().testConnection();
        } catch (Exception e) {
            databaseConnectionFailed = true;
            System.err.println(" Предупреждение: " + e.getMessage());
            // Не прерываем запуск, просто запоминаем ошибку
        }

        try {
            UserRepository userRepo = new UserRepository();
            SeriesRepository seriesRepo = new SeriesRepository();
            StepRepository stepRepo = new StepRepository();

            authService = new AuthService(userRepo);
            seriesManager = new SeriesCollectionManager(seriesRepo);
            stepManager = new DilutionStepManager(stepRepo);
            service = new DilutionService(seriesManager, stepManager, authService);

            seriesManager.loadFromDatabase();
            stepManager.loadFromDatabase();
        } catch (Exception e) {
            databaseConnectionFailed = true;
            System.err.println("  Предупреждение: " + e.getMessage());
        }

        showLoginWindow(stage);

        if (databaseConnectionFailed) {

            Platform.runLater(() -> {

                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), e -> showDatabaseError("Connection refused - БДшка не отвечает на порту 5432")));
                timeline.play();
            });
        }
    }

    private void showLoginWindow(Stage stage) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(javafx.geometry.Pos.CENTER);
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // Загрузка иконки
        try {
            Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Не удалось загрузить иконку: " + e.getMessage());
        }

        Label titleLabel = new Label("Welcome to Dilution Manager! :3");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 0 0 5 0;");
        GridPane.setHalignment(titleLabel, javafx.geometry.HPos.CENTER);

        TextField loginField = new TextField();
        loginField.setPromptText("Логин");
        loginField.setPrefWidth(380);
        loginField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        passwordField.setPrefWidth(380);
        passwordField.setMaxWidth(Double.MAX_VALUE);

        Button loginBtn = new Button("Войти");
        loginBtn.setStyle("-fx-base: #2a6fdb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30;");
        loginBtn.setPrefWidth(130);

        Button registerBtn = new Button("Регистрация");
        registerBtn.setStyle("-fx-base: #757575; -fx-text-fill: white; -fx-padding: 10 20;");
        registerBtn.setPrefWidth(130);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 12px;");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(380);


        if (databaseConnectionFailed) {
            statusLabel.setText("  НЕТ ПОДКЛЮЧЕНИЯ К БД! Проверьте PostgreSQL и порт 5433");
            statusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 14px;");
        }

        // Обработчик кнопки ВХОД
        loginBtn.setOnAction(e -> {
            String login = loginField.getText().trim();
            String password = passwordField.getText();

            if (login.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Заполните все поля");
                return;
            }

            try {
                if (authService.login(login, password)) {
                    currentUsername = login;
                    openMainApplication(stage);
                } else {
                    statusLabel.setText("Неверный логин или пароль");
                }
            } catch (Exception ex) {
                // Ловим ошибку БД и показываем её красным
                String msg = ex.getMessage();
                if (msg != null && msg.contains("Ошибка базы данных")) {
                    statusLabel.setText(" НЕТ СВЯЗИ С БД!");
                    statusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 15px;");
                } else {
                    statusLabel.setText("Ошибка: " + msg);
                }
            }
        });

// Обработчик кнопки регистрацияяяяя
        registerBtn.setOnAction(e -> {
            try {
                showRegisterDialog(stage, statusLabel);
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("Ошибка базы данных")) {
                    statusLabel.setText(" НЕТ СВЯЗИ С БД!");
                    statusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 15px;");
                } else {
                    statusLabel.setText("Ошибка: " + msg);
                }
            }
        });

        // Кнопки в отдельной панели для центрирования
        HBox buttonBox = new HBox(15, loginBtn, registerBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(new Label("Логин:"), 0, 1);
        grid.add(loginField, 0, 2, 2, 1);
        grid.add(new Label("Пароль:"), 0, 3);
        grid.add(passwordField, 0, 4, 2, 1);
        grid.add(buttonBox, 0, 5, 2, 1);
        grid.add(statusLabel, 0, 6, 2, 1);

        Scene scene = new Scene(grid, 450, 350);
        stage.setTitle("Вход в систему");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    private void openMainApplication(Stage stage) {
        stage.setTitle("Dilution Manager v.2.1 Ohae, Hello world!");

        rootPane = new BorderPane();
        rootPane.setPadding(new Insets(12));
        funnyGifPanel = buildFunnyGifPanel();

        VBox top = new VBox(8, buildToolbar(), buildActionsRow(), buildHintRow(), buildStatusRow());
        rootPane.setTop(top);

        cardsBox.setPadding(new Insets(8));
        ScrollPane scrollPane = new ScrollPane(cardsBox);
        scrollPane.setFitToWidth(true);
        rootPane.setCenter(scrollPane);
        rootPane.setRight(funnyGifPanel);

        renderCards();

        Scene scene = new Scene(rootPane, 1000, 700);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private VBox buildFunnyGifPanel() {
        gifView = new ImageView(loadFunnyGifImage()); // Сохраняем ссылку
        gifView.setPreserveRatio(true);
        gifView.setFitWidth(160);
        gifView.setFitHeight(160);

        Label caption = new Label("Смешнявка");
        caption.setStyle("-fx-font-weight: bold;");

        Label hint = new Label("thanks for using our app\nmade with love by mazzyha and anutaf");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        Button hideBtn = new Button("Скрыть");
        hideBtn.setOnAction(e -> rootPane.setRight(null));

        VBox box = new VBox(8, caption, gifView, hint, hideBtn);
        box.setPadding(new Insets(0, 0, 0, 12));
        box.setMaxWidth(200);
        return box;
    }
    private void loadRandomGif() {
        if (GifNames == null || GifNames.length == 0) return;

        String randomName = GifNames[random.nextInt(GifNames.length)];
        try (InputStream in = getClass().getResourceAsStream("/" + randomName)) {
            if (in != null) {
                Image newImage = new Image(in, 160, 160, true, true);
                gifView.setImage(newImage);
            }
        } catch (IOException e) {
            System.err.println("Не удалось загрузить гифку: " + e.getMessage());
        }
    }
    private Image loadFunnyGifImage() {
        try (InputStream in = getClass().getResourceAsStream("/funny.gif")) {
            if (in != null) return new Image(in, 160, 160, true, true);
        } catch (IOException ignored) {}

        Path userGif = Paths.get(System.getProperty("user.home"), ".lab5-funny.gif");
        if (Files.isRegularFile(userGif)) {
            return new Image(userGif.toUri().toString(), 160, 160, true, true, true);
        }
        byte[] tiny = Base64.getDecoder().decode(TINY_GIF_B64);
        return new Image(new ByteArrayInputStream(tiny), 160, 160, true, true);
    }

    private HBox buildHintRow() {
        Label hint = new Label("Клик — выбор  Двойной клик — детали и редактирование. Данные сохраняются автоматически в БД");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
        HBox row = new HBox(hint);
        row.setPadding(new Insets(0, 0, 4, 0));
        return row;
    }

    private HBox buildToolbar() {
        userStatusLabel = new Label("Пользователь: " + currentUsername);
        userStatusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> {
            // меняем гифку
            loadRandomGif();

            // Затем обновляем данные из БД
            runInBackground("refresh", () -> {
                seriesManager.loadFromDatabase();
                stepManager.loadFromDatabase();
                Platform.runLater(() -> {
                    statusLabel.setText("Данные обновлены из БД");
                    renderCards();
                });
            }, true);
        });

        Button logoutButton = new Button("Выход");
        logoutButton.setOnAction(e -> {
            authService.logout();
            currentUsername = null;
            showLoginWindow(primaryStage);
        });

        HBox row = new HBox(10,
                userStatusLabel,
                new Region(),
                refreshButton,
                logoutButton
        );
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        row.setPadding(new Insets(4, 0, 0, 0));
        return row;
    }

    private HBox buildActionsRow() {
        TextField nameField = new TextField();
        nameField.setPromptText("Название серии");
        nameField.setPrefWidth(180);

        ComboBox<DilutionSourceType> sourceTypeBox = new ComboBox<>();
        sourceTypeBox.getItems().addAll(DilutionSourceType.values());
        sourceTypeBox.setValue(DilutionSourceType.SAMPLE);
        sourceTypeBox.setPrefWidth(100);

        TextField sourceIdField = new TextField("1");
        sourceIdField.setPrefWidth(70);

        Button createSeriesButton = new Button("Создать серию");
        createSeriesButton.setStyle("-fx-base: #4caf50; -fx-text-fill: white;");
        createSeriesButton.setOnAction(e -> runInBackground("create-series", () -> {
            try {
                if (currentUsername == null) throw new SecurityException("Требуется авторизация");

                String name = nameField.getText().trim();
                if (name.isEmpty() || name.length() > 128) {
                    throw new IllegalArgumentException("Название: 1-128 символов");
                }

                long sourceId = Long.parseLong(sourceIdField.getText().trim());
                if (sourceId <= 0) throw new IllegalArgumentException("ID источника > 0");

                service.createSeries(name, sourceTypeBox.getValue(), sourceId, currentUsername);

                Platform.runLater(() -> {
                    nameField.clear();
                    statusLabel.setText("Серия создана и сохранена в БД");
                    renderCards();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showError(ex.getMessage()));
                throw ex;
            }
        }, true));

        TextField stepSeriesIdField = new TextField();
        stepSeriesIdField.setPromptText("ID серии");
        stepSeriesIdField.setPrefWidth(80);

        TextField stepNumField = new TextField();
        stepNumField.setPromptText("Шаг");
        stepNumField.setPrefWidth(50);

        TextField factorField = new TextField();
        factorField.setPromptText("Factor");
        factorField.setPrefWidth(60);

        TextField finalQtyField = new TextField();
        finalQtyField.setPromptText("Объём");
        finalQtyField.setPrefWidth(60);

        ComboBox<FinalQuantityUnit> unitBox = new ComboBox<>();
        unitBox.getItems().addAll(FinalQuantityUnit.values());
        unitBox.setValue(FinalQuantityUnit.ML);
        unitBox.setPrefWidth(60);

        Button addStepButton = new Button("Добавить шаг");
        addStepButton.setStyle("-fx-base: #2196f3; -fx-text-fill: white;");
        addStepButton.setOnAction(e -> {
            if (currentUsername == null) {
                showError("Требуется авторизация");
                return;
            }
            runInBackground("add-step", () -> {
                try {
                    long seriesId = Long.parseLong(stepSeriesIdField.getText().trim());
                    int stepNum = Integer.parseInt(stepNumField.getText().trim());
                    double factor = Double.parseDouble(factorField.getText().trim());
                    double finalQty = Double.parseDouble(finalQtyField.getText().trim());

                    DilutionSeries series = service.getSeries(seriesId);
                    if (series == null) throw new IllegalArgumentException("Серия не найдена");

                    if (!currentUsername.equals(series.getOwnerUsername())) {
                        throw new SecurityException("Нет прав: владелец — " + series.getOwnerUsername());
                    }

                    service.addStep(seriesId, stepNum, factor, finalQty, unitBox.getValue());

                    Platform.runLater(() -> {
                        statusLabel.setText("Шаг добавлен в БД");
                        renderCards();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showError(ex.getMessage()));
                    throw ex;
                }
            }, false);
        });

        HBox row = new HBox(10,
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
        if (selectedCard != null) selectedCard.setStyle(CARD_NORMAL);
        selectedCard = card;
        if (selectedCard != null) selectedCard.setStyle(CARD_SELECTED);
    }

    private void renderCards() {
        cardsBox.getChildren().clear();
        selectedCard = null;

        List<DilutionSeries> series = service.listSeries().stream()
                .sorted(Comparator.comparingLong(DilutionSeries::getId))
                .toList();

        if (series.isEmpty()) {
            Label empty = new Label("Коллекция пуста. Создайте первую серию выше");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
            cardsBox.getChildren().add(empty);
            return;
        }

        for (DilutionSeries s : series) {
            int stepCount = (int) service.listSteps(s.getId()).stream()
                    .filter(st -> st.getSeriesId() == s.getId()).count();

            VBox card = new VBox(6);
            card.setPadding(new Insets(10));
            card.setStyle(CARD_NORMAL);
            card.setCursor(Cursor.HAND);

            Label title = new Label("#" + s.getId() + " - " + s.getName());
            title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

            Label source = new Label("Источник: " + s.getSourceType() + " #" + s.getSourceId());
            Label meta = new Label("Владелец: " + s.getOwnerUsername() + " Шагов: " + stepCount);
            Label hint = new Label("Жмякните ЛКМ дважды для редактирования");
            hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

            Button deleteButton = new Button("Удалить");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 4;");
            deleteButton.setOnAction(e -> confirmDeleteSeries(s));

            boolean isOwner = currentUsername != null && currentUsername.equals(s.getOwnerUsername());
            deleteButton.setDisable(!isOwner);
            if (!isOwner) {
                deleteButton.setStyle("-fx-background-color: #ccc; -fx-text-fill: #666;");
                deleteButton.setTooltip(new Tooltip("Только владелец может удалить"));
            }

            HBox buttonBox = new HBox(10, deleteButton);
            card.getChildren().addAll(title, source, meta, hint, buttonBox);

            card.setOnMouseClicked(ev -> {
                selectCard(card);
                if (ev.getClickCount() == 2) openSeriesDialog(s);
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

        VBox editBox = new VBox(10);
        editBox.setPadding(new Insets(8, 0, 0, 0));
        boolean isOwner = currentUsername != null && currentUsername.equals(fresh.getOwnerUsername());

        Label nameLabel = new Label("Название:");
        TextField nameField = new TextField(fresh.getName());
        nameField.setPrefWidth(200);
        Button applyName = new Button("Применить");
        if (!isOwner) { nameField.setDisable(true); applyName.setDisable(true); }

        applyName.setOnAction(ev -> {
            try {
                String newName = nameField.getText().trim();
                if (newName.isEmpty() || newName.length() > 128) {
                    showError("Название: 1-128 символов");
                    return;
                }
                runInBackground("update-name", () -> {
                    service.updateSeries(fresh.getId(), newName, fresh.getSourceType(), fresh.getSourceId());
                    Platform.runLater(() -> {
                        details.setText(buildSeriesDetailsTextFromService(service.getSeries(fresh.getId())));
                        statusLabel.setText("Название обновлено");
                        renderCards();
                    });
                }, false);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        editBox.getChildren().add(new HBox(10, nameLabel, nameField, applyName));

        Label linkLabel = new Label("Источник:");
        ComboBox<DilutionSourceType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(DilutionSourceType.values());
        typeBox.setValue(fresh.getSourceType());
        TextField sourceIdField = new TextField(Long.toString(fresh.getSourceId()));
        sourceIdField.setPrefWidth(80);
        Button applyLink = new Button("OK");
        if (!isOwner) { typeBox.setDisable(true); sourceIdField.setDisable(true); applyLink.setDisable(true); }

        applyLink.setOnAction(ev -> {
            try {
                service.linkSource(fresh.getId(), typeBox.getValue(), Long.parseLong(sourceIdField.getText().trim()));
                details.setText(buildSeriesDetailsTextFromService(service.getSeries(fresh.getId())));
                statusLabel.setText("Источник обновлён");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        editBox.getChildren().add(new HBox(10, linkLabel, typeBox, sourceIdField, applyLink));

        Label stepLabel = new Label("Шаг ID:");
        TextField stepIdField = new TextField();
        stepIdField.setPromptText("ID");
        stepIdField.setPrefWidth(60);
        TextField factorField = new TextField();
        factorField.setPromptText("Factor");
        factorField.setPrefWidth(70);
        Button applyFactor = new Button("Factor OK");
        if (!isOwner) { stepIdField.setDisable(true); factorField.setDisable(true); applyFactor.setDisable(true); }

        applyFactor.setOnAction(ev -> {
            try {
                long stepId = Long.parseLong(stepIdField.getText().trim());
                double f = Double.parseDouble(factorField.getText().trim());
                service.updateStepFactor(stepId, f);
                verifyStepBelongsToSeries(stepId, fresh.getId());
                details.setText(buildSeriesDetailsTextFromService(service.getSeries(fresh.getId())));
                statusLabel.setText("Factor обновлён");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        TextField qtyField = new TextField();
        qtyField.setPromptText("Qty");
        qtyField.setPrefWidth(70);
        Button applyQty = new Button("Qty OK");
        if (!isOwner) { qtyField.setDisable(true); applyQty.setDisable(true); }

        applyQty.setOnAction(ev -> {
            try {
                long stepId = Long.parseLong(stepIdField.getText().trim());
                double q = Double.parseDouble(qtyField.getText().trim());
                service.updateStepFinalQuantity(stepId, q);
                verifyStepBelongsToSeries(stepId, fresh.getId());
                details.setText(buildSeriesDetailsTextFromService(service.getSeries(fresh.getId())));
                statusLabel.setText("Объём обновлён");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        editBox.getChildren().add(new HBox(10, stepLabel, stepIdField, factorField, applyFactor, qtyField, applyQty));

        if (!isOwner) {
            Label noAccess = new Label("Только владелец может редактировать");
            noAccess.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
            editBox.getChildren().add(noAccess);
        }

        pane.setContent(new VBox(8, new Label("Детали:"), details, editBox));
        pane.getButtonTypes().add(new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE));

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setDialogPane(pane);
        alert.setTitle("Редактирование");
        alert.initOwner(cardsBox.getScene().getWindow());
        alert.showAndWait();
    }

    private void applyStepFactor(TextField stepIdField, TextField factorField, long seriesId, TextArea details) {
        try {
            long stepId = Long.parseLong(stepIdField.getText().trim());
            double f = Double.parseDouble(factorField.getText().trim());
            service.updateStepFactor(stepId, f);
            verifyStepBelongsToSeries(stepId, seriesId);
            details.setText(buildSeriesDetailsTextFromService(service.getSeries(seriesId)));
            statusLabel.setText("Factor обновлён");
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void applyStepQty(TextField stepIdField, TextField qtyField, long seriesId, TextArea details) {
        try {
            long stepId = Long.parseLong(stepIdField.getText().trim());
            double q = Double.parseDouble(qtyField.getText().trim());
            service.updateStepFinalQuantity(stepId, q);
            verifyStepBelongsToSeries(stepId, seriesId);
            details.setText(buildSeriesDetailsTextFromService(service.getSeries(seriesId)));
            statusLabel.setText("Объём обновлён");
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void verifyStepBelongsToSeries(long stepId, long seriesId) {
        DilutionStep st = service.getStepById(stepId);
        if (st == null || st.getSeriesId() != seriesId) {
            throw new IllegalArgumentException("Шаг не принадлежит этой серии");
        }
    }

    private String buildSeriesDetailsTextFromService(DilutionSeries s) {
        // Форматирование дат в читаемый вид
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                        .withZone(java.time.ZoneId.systemDefault());

        String createdAtStr = (s.getCreatedAt() != null) ?
                formatter.format(s.getCreatedAt()) : "не указано";
        String updatedAtStr = (s.getUpdatedAt() != null) ?
                formatter.format(s.getUpdatedAt()) : "не указано";

        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(s.getId()).append("\n");
        sb.append("Название: ").append(s.getName()).append("\n");
        sb.append("Источник: ").append(s.getSourceType()).append(" #").append(s.getSourceId()).append("\n");
        sb.append("Владелец: ").append(s.getOwnerUsername()).append("\n");
        sb.append("Создано: ").append(createdAtStr).append("\n");
        sb.append("Обновлено: ").append(updatedAtStr).append("\n\n");
        sb.append("=== Шаги ===\n");

        List<DilutionStep> steps = service.listSteps(s.getId()).stream()
                .sorted(Comparator.comparingInt(DilutionStep::getStepNumber)).toList();

        if (steps.isEmpty()) {
            sb.append("(нет шагов)\n");
        } else {
            for (DilutionStep st : steps) {
                sb.append(String.format("Шаг #%d | ID=%d | Factor=%.2f | Qty=%.2f %s\n",
                        st.getStepNumber(), st.getId(), st.getFactor(),
                        st.getFinalQuantity(), st.getFinalUnit()));
            }
        }
        return sb.toString();
    }

    private void runInBackground(String op, ThrowingRunnable task, boolean redraw) {
        Task<Void> bgTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                task.run();
                return null;
            }
        };

        progressBar.visibleProperty().bind(bgTask.runningProperty());
        progressBar.progressProperty().bind(bgTask.progressProperty());

        bgTask.setOnRunning(e -> statusLabel.setText(op + "..."));

        bgTask.setOnSucceeded(e -> {
            progressBar.visibleProperty().unbind();
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            progressBar.setProgress(0);
            if (redraw) renderCards();
        });

        bgTask.setOnFailed(e -> {
            progressBar.visibleProperty().unbind();
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            progressBar.setProgress(0);

            Throwable ex = bgTask.getException();
            showError(ex != null ? ex.getMessage() : "Неизвестная ошибка");
            statusLabel.setText("Ошибка: " + op);
        });

        bgTask.setOnCancelled(e -> {
            progressBar.visibleProperty().unbind();
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            progressBar.setProgress(0);
            statusLabel.setText("Отменено: " + op);
        });

        new Thread(bgTask, "bg-" + op).start();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Операция не выполнена");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showRegisterDialog(Stage stage, Label statusLabel) {
        TextField loginField = new TextField();
        loginField.setPromptText("Логин (мин. 3 символа)");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Пароль (мин. 4 символа)");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Повторите пароль");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Логин:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(passField, 1, 1);
        grid.add(new Label("Повтор:"), 0, 2);
        grid.add(confirmField, 1, 2);

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Регистрация");
        alert.setHeaderText("Создание аккаунта");
        alert.getDialogPane().setContent(grid);
        ButtonType regBtn = new ButtonType("Зарегистрировать", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(regBtn, cancelBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == regBtn) {
                String login = loginField.getText().trim();
                String pass = passField.getText();
                String confirm = confirmField.getText();

                if (login.length() < 3) {
                    statusLabel.setText("Логин: минимум 3 символа");
                    return;
                }
                if (pass.length() < 4) {
                    statusLabel.setText("Пароль: минимум 4 символа");
                    return;
                }
                if (!pass.equals(confirm)) {
                    statusLabel.setText("Пароли не совпадают");
                    return;
                }


                try {
                    if (authService.register(login, pass)) {
                        statusLabel.setText("Регистрация успешна! Теперь войдите.");
                    } else {
                        statusLabel.setText("Пользователь уже существует");
                    }
                } catch (Exception ex) {

                    showDatabaseError(ex.getMessage());
                    statusLabel.setText("Ошибка подключения к БД");
                }
                try {
                    if (authService.register(login, pass)) {
                        statusLabel.setText("Регистрация успешна! Теперь войдите.");
                    } else {
                        statusLabel.setText("Пользователь уже существует");
                    }
                } catch (Exception ex) {
                    // Проброс ошибки БД
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private void confirmDeleteSeries(DilutionSeries series) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление");
        alert.setHeaderText("Удалить серию #" + series.getId() + "?");
        alert.setContentText("Это действие нельзя отменить.");

        if (alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent()) {
            runInBackground("delete", () -> {
                service.deleteSeries(series.getId());
                Platform.runLater(() -> {
                    statusLabel.setText("Серия удалена из БД");
                    renderCards();
                });
            }, true);
        }
    }
    private void showDatabaseError(String details) {
        // Создаем окно ошибки
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка подключения");
        alert.setHeaderText("Не удалось подключиться к базе данных");
        alert.setContentText(
                "Проверьте:\n" +
                        " Запущен ли PostgreSQL\n" +
                        " Правильность порта в database.properties (сейчас: 5433)\n" +
                        " Доступность сети\n\n" +
                        "Детали: " + (details != null ? details : "Нет информации")
        );
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);


        alert.showAndWait();
    }

    @FunctionalInterface
    private interface ThrowingRunnable { void run() throws Exception; }

    public static void main(String[] args) { launch(args); }
}