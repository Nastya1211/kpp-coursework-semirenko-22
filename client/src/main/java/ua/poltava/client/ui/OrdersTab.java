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
import ua.poltava.client.model.ServiceOrder;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

public class OrdersTab extends Tab {
    private static final Type LIST = new TypeToken<List<ServiceOrder>>() {}.getType();

    private final ApiClient api;
    private final TableView<ServiceOrder> table = new TableView<>();
    private final TextField carField = new TextField();
    private final TextField descField = new TextField();
    private final ComboBox<Integer> safetyBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
    private final ComboBox<Integer> mobilityBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));

    public OrdersTab(ApiClient api) {
        super("Замовлення");
        this.api = api;
        setClosable(false);

        TableColumn<ServiceOrder, Double> indexCol = col("Показник I", ServiceOrder::index);
        indexCol.setSortType(TableColumn.SortType.DESCENDING);
        table.getColumns().addAll(
                col("ID", ServiceOrder::id),
                col("Авто", ServiceOrder::carId),
                col("Опис", ServiceOrder::description),
                col("Статус", ServiceOrder::status),
                col("Безпека (1-5)", ServiceOrder::safetyLevel),
                col("Мобільність (1-5)", ServiceOrder::mobilityLevel),
                col("Створено", ServiceOrder::createdAt),
                indexCol);
        table.getSortOrder().add(indexCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, o) -> {
            if (o != null) {
                carField.setText(String.valueOf(o.carId()));
                descField.setText(o.description());
                safetyBox.setValue(o.safetyLevel());
                mobilityBox.setValue(o.mobilityLevel());
            }
        });

        carField.setPromptText("ID авто");
        carField.setPrefWidth(80);
        descField.setPromptText("Опис проблеми");
        safetyBox.setPromptText("Безпека");
        mobilityBox.setPromptText("Мобільність");

        Button addBtn = new Button("Додати");
        Button saveBtn = new Button("Зберегти зміни");
        Button advanceBtn = new Button("Наступний стан");
        Button deleteBtn = new Button("Видалити");
        Button refreshBtn = new Button("Оновити");
        addBtn.setOnAction(e -> add());
        saveBtn.setOnAction(e -> save());
        advanceBtn.setOnAction(e -> advance());
        deleteBtn.setOnAction(e -> delete());
        refreshBtn.setOnAction(e -> refresh());

        HBox form = new HBox(8, carField, descField, safetyBox, mobilityBox);
        HBox.setHgrow(descField, Priority.ALWAYS);
        HBox buttons = new HBox(8, addBtn, saveBtn, advanceBtn, deleteBtn, refreshBtn);
        VBox root = new VBox(10, table, form, buttons);
        root.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(root);

        refresh();
    }

    private static <T> TableColumn<ServiceOrder, T> col(String title, Function<ServiceOrder, T> getter) {
        TableColumn<ServiceOrder, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleObjectProperty<>(getter.apply(cell.getValue())));
        return column;
    }

    private void refresh() {
        try {
            List<ServiceOrder> orders = api.request("GET", "/orders", null, LIST);
            table.setItems(FXCollections.observableArrayList(orders));
            table.sort();
        } catch (ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private ServiceOrder readForm(int id) {
        int carId;
        try {
            carId = Integer.parseInt(carField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID автомобіля має бути числом");
        }
        String description = descField.getText().trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Опис не може бути порожнім");
        }
        if (safetyBox.getValue() == null || mobilityBox.getValue() == null) {
            throw new IllegalArgumentException("Оберіть рівні безпеки та мобільності");
        }
        return new ServiceOrder(id, carId, description, null, safetyBox.getValue(), mobilityBox.getValue(), null, 0);
    }

    private ServiceOrder selected() {
        ServiceOrder selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Оберіть замовлення в таблиці");
        }
        return selected;
    }

    private void add() {
        try {
            api.request("POST", "/orders", readForm(0), ServiceOrder.class);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void save() {
        try {
            int id = selected().id();
            api.request("PUT", "/orders/" + id, readForm(id), ServiceOrder.class);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void advance() {
        try {
            api.request("POST", "/orders/" + selected().id() + "/advance", null, ServiceOrder.class);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void delete() {
        try {
            api.request("DELETE", "/orders/" + selected().id(), null, null);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }
}
