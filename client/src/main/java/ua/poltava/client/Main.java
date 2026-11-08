package ua.poltava.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import ua.poltava.client.ui.OrdersTab;
import javafx.stage.Stage;
import ua.poltava.client.api.ApiClient;
import ua.poltava.client.ui.ClientsTab;
import ua.poltava.client.ui.CarsTab;
import ua.poltava.client.ui.WorksTab;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        ApiClient api = new ApiClient();
        TabPane tabs = new TabPane(new ClientsTab(api), new CarsTab(api), new OrdersTab(api), new WorksTab(api));
        stage.setTitle("Автосервіс");
        stage.setScene(new Scene(tabs, 900, 500));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
