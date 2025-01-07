package Main;

import javafx.application.Application;
import javafx.stage.Stage;
import data.UserManager;
import ui.LoginUI;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Load users into memory
        UserManager.loadUsers();

        // Show the login UI first
        LoginUI loginUI = new LoginUI(primaryStage);
        loginUI.showLoginScene();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
