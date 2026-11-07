package ua.poltava.client.ui;

import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ua.poltava.client.api.ApiClient;
import ua.poltava.client.api.ApiException;
import ua.poltava.client.model.Client;

import java.lang.reflect.Type;
import java.util.List;

public class ClientsTab extends Tab {
    private static final Type LIST = new TypeToken<List<Client>>() {}.getType();

    private final ApiClient api;
    private final TableView<Client> table = new TableView<>();
    private final TextField nameField = new TextField();
    private final TextField phoneField = new TextField();

    public ClientsTab(ApiClient api) {
        super("Клієнти");
        this.api = api;
        setClosable(false);

        TableColumn<Client, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id()));
        TableColumn<Client, String> nameCol = new TableColumn<>("ПІБ");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fullName()));
        TableColumn<Client, String> phoneCol = new TableColumn<>("Телефон");
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().phone()));
        table.getColumns().addAll(idCol, nameCol, phoneCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                nameField.setText(selected.fullName());
                phoneField.setText(selected.phone());
            }
        });

        nameField.setPromptText("ПІБ");
        phoneField.setPromptText("Телефон, +380...");
        Button addBtn = new Button("Додати");
        Button saveBtn = new Button("Зберегти зміни");
        Button deleteBtn = new Button("Видалити");
        Button refreshBtn = new Button("Оновити");
        addBtn.setOnAction(e -> add());
        saveBtn.setOnAction(e -> save());
        deleteBtn.setOnAction(e -> delete());
        refreshBtn.setOnAction(e -> refresh());

        HBox form = new HBox(8, nameField, phoneField, addBtn, saveBtn, deleteBtn, refreshBtn);
        HBox.setHgrow(nameField, javafx.scene.layout.Priority.ALWAYS);
        VBox root = new VBox(10, table, form);
        root.setPadding(new Insets(10));
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
        setContent(root);

        refresh();
    }

    private void refresh() {
        try {
            List<Client> clients = api.request("GET", "/clients", null, LIST);
            table.setItems(FXCollections.observableArrayList(clients));
        } catch (ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private Client readForm(int id) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        // швидка перевірка на клієнті (повна перевірка є в Service на сервері)
        if (name.isEmpty()) {
            throw new IllegalArgumentException("ПІБ не може бути порожнім");
        }
        if (!phone.matches("\\+?[0-9]{10,13}")) {
            throw new IllegalArgumentException("Телефон має містити 10-13 цифр");
        }
        return new Client(id, name, phone);
    }

    private void add() {
        try {
            api.request("POST", "/clients", readForm(0), Client.class);
            clearForm();
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void save() {
        Client selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiUtil.error("Оберіть клієнта в таблиці");
            return;
        }
        try {
            api.request("PUT", "/clients/" + selected.id(), readForm(selected.id()), Client.class);
            refresh();
        } catch (IllegalArgumentException | ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void delete() {
        Client selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiUtil.error("Оберіть клієнта в таблиці");
            return;
        }
        try {
            api.request("DELETE", "/clients/" + selected.id(), null, null);
            clearForm();
            refresh();
        } catch (ApiException e) {
            UiUtil.error(e.getMessage());
        }
    }

    private void clearForm() {
        nameField.clear();
        phoneField.clear();
    }
}
