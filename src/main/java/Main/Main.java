package Main;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.LoginUI;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Show the login UI first
        LoginUI loginUI = new LoginUI(primaryStage);
        loginUI.showLoginScene();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
