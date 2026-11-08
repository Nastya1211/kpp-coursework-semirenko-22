package ua.poltava.client.ui;

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
import ua.poltava.client.model.Work;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

public class WorksTab extends Tab {
    private static final Type LIST = new TypeToken<List<Work>>() {}.getType();

    private final ApiClient api;
    private final TableView<Work> table = new TableView<>();
    private final TextField orderField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField priceField = new TextField();
    private final TextField hoursField = new TextField();

    public WorksTab(ApiClient api) {
        super("Роботи");
        this.api = api;
        setClosable(false);

        table.getColumns().addAll(
                col("ID", Work::id), col("ID замовлення", Work::orderId), col("Назва", Work::name),
                col("Ціна, грн", Work::price), col("Години", Work::hours));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, w) -> {
            if (w != null) {
                orderField.setText(String.valueOf(w.orderId()));
                nameField.setText(w.name());
                priceField.setText(w.price().toPlainString());
                hoursField.setText(w.hours().toPlainString());
            }
        });

        orderField.setPromptText("ID замовлення");
        orderField.setPrefWidth(120);
        nameField.setPromptText("Назва роботи");
        priceField.setPromptText("Ціна");
        priceField.setPrefWidth(90);
        hoursField.setPromptText("Години");
        hoursField.setPrefWidth(80);

        Button addBtn = new Button("Додати");
        Button saveBtn = new Button("Зберегти зміни");
        Button deleteBtn = new Button("Видалити");
        Button refreshBtn = new Button("Оновити");
        addBtn.setOnAction(e -> add());
        saveBtn.setOnAction(e -> save());
        deleteBtn.setOnAction(e -> delete());
        refreshBtn.setOnAction(e -> refresh());

        HBox form = new HBox(8, orderField, nameField, priceField, hoursField);
        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox buttons = new HBox(8, addBtn, saveBtn, deleteBtn, refreshBtn);
        VBox root = new VBox(10, table, form, buttons);
        root.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(root);

        refresh();
    }

    private static <T> TableColumn<Work, T> col(String title, Function<Work, T> getter) {
        TableColumn<Work, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleObjectProperty<>(getter.apply(cell.getValue())));
        return column;
    }

    private void refresh() {
        try {
            List<Work> works = api.request("GET", "/works", null, LIST);
            table.setItems(FXCollections.observableArrayList(works));
        } catch (ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private Work readForm(int id) {
        int orderId;
        BigDecimal price;
        BigDecimal hours;
        try {
            orderId = Integer.parseInt(orderField.getText().trim());
            price = new BigDecimal(priceField.getText().trim().replace(',', '.'));
            hours = new BigDecimal(hoursField.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID замовлення, ціна й години мають бути числами");
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Назва роботи не може бути порожньою");
        }
        return new Work(id, orderId, name, price, hours);
    }

    private Work selected() {
        Work selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Оберіть роботу в таблиці");
        }
        return selected;
    }

    private void add() {
        try {
            api.request("POST", "/works", readForm(0), Work.class);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void save() {
        try {
            int id = selected().id();
            api.request("PUT", "/works/" + id, readForm(id), Work.class);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void delete() {
        try {
            api.request("DELETE", "/works/" + selected().id(), null, null);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }
}
