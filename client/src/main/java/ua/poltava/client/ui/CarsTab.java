package ua.poltava.client.ui;

import javafx.stage.FileChooser;
import ua.poltava.client.csv.CsvImportTask;
import ua.poltava.client.csv.ImportResult;
import java.io.File;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ua.poltava.client.api.ApiClient;
import ua.poltava.client.api.ApiException;
import ua.poltava.client.model.Car;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

public class CarsTab extends Tab {
    private static final Type LIST = new TypeToken<List<Car>>() {}.getType();

    private final ApiClient api;
    private final TableView<Car> table = new TableView<>();
    private final TextField vinField = new TextField();
    private final TextField brandField = new TextField();
    private final TextField modelField = new TextField();
    private final TextField yearField = new TextField();
    private final TextField clientField = new TextField();
    private final ProgressBar progress = new ProgressBar(0);
    private final Label statusLabel = new Label();
    private final Button importBtn = new Button("Імпорт CSV");

    public CarsTab(ApiClient api) {
        super("Авто");
        this.api = api;
        setClosable(false);

        table.getColumns().addAll(
                col("ID", Car::id), col("VIN", Car::vin), col("Марка", Car::brand),
                col("Модель", Car::model), col("Рік", Car::year), col("ID власника", Car::clientId));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, c) -> {
            if (c != null) {
                vinField.setText(c.vin());
                brandField.setText(c.brand());
                modelField.setText(c.model());
                yearField.setText(String.valueOf(c.year()));
                clientField.setText(String.valueOf(c.clientId()));
            }
        });

        vinField.setPromptText("VIN (17 символів)");
        brandField.setPromptText("Марка");
        modelField.setPromptText("Модель");
        yearField.setPromptText("Рік");
        yearField.setPrefWidth(70);
        clientField.setPromptText("ID власника");
        clientField.setPrefWidth(100);

        Button addBtn = new Button("Додати");
        Button saveBtn = new Button("Зберегти зміни");
        Button deleteBtn = new Button("Видалити");
        Button refreshBtn = new Button("Оновити");
        addBtn.setOnAction(e -> add());
        saveBtn.setOnAction(e -> save());
        deleteBtn.setOnAction(e -> delete());
        refreshBtn.setOnAction(e -> refresh());

        HBox form = new HBox(8, vinField, brandField, modelField, yearField, clientField);
        HBox.setHgrow(vinField, Priority.ALWAYS);
        HBox buttons = new HBox(8, addBtn, saveBtn, deleteBtn, refreshBtn);
        importBtn.setOnAction(e -> importCsv());
        progress.setPrefWidth(250);
        HBox importRow = new HBox(8, importBtn, progress, statusLabel);
        VBox root = new VBox(10, table, form, buttons, importRow);
        root.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(root);

        refresh();
    }

    private static <T> TableColumn<Car, T> col(String title, Function<Car, T> getter) {
        TableColumn<Car, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleObjectProperty<>(getter.apply(cell.getValue())));
        return column;
    }

    private void refresh() {
        try {
            List<Car> cars = api.request("GET", "/cars", null, LIST);
            table.setItems(FXCollections.observableArrayList(cars));
        } catch (ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private Car readForm(int id) {
        int year;
        int clientId;
        try {
            year = Integer.parseInt(yearField.getText().trim());
            clientId = Integer.parseInt(clientField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Рік і ID власника мають бути числами");
        }
        String vin = vinField.getText().trim().toUpperCase();
        if (vin.length() != 17) {
            throw new IllegalArgumentException("VIN має містити 17 символів");
        }
        return new Car(id, vin, brandField.getText().trim(), modelField.getText().trim(), year, clientId);
    }

    private Car selected() {
        Car selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Оберіть автомобіль у таблиці");
        }
        return selected;
    }

    private void add() {
        try {
            api.request("POST", "/cars", readForm(0), Car.class);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void save() {
        try {
            int id = selected().id();
            api.request("PUT", "/cars/" + id, readForm(id), Car.class);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void delete() {
        try {
            api.request("DELETE", "/cars/" + selected().id(), null, null);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }
    private void importCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Оберіть CSV-файл");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showOpenDialog(getTabPane().getScene().getWindow());
        if (file == null) {
            return;
        }
        CsvImportTask task = new CsvImportTask(file.toPath(), api);
        progress.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        importBtn.setDisable(true);

        task.setOnSucceeded(e -> {
            finishImport();
            ImportResult result = task.getValue();
            StringBuilder text = new StringBuilder("Імпортовано: " + result.imported()
                    + "\nПомилок: " + result.errors().size());
            result.errors().stream().limit(10).forEach(err -> text.append("\n").append(err));
            refresh();
            UiUtil.info(text.toString());
        });
        task.setOnFailed(e -> {
            finishImport();
            UiUtil.error(task.getException().getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void finishImport() {
        progress.progressProperty().unbind();
        statusLabel.textProperty().unbind();
        importBtn.setDisable(false);
    }
}
