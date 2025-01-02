package ui;

import data.UserManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;

public class LoginUI {

    private Stage primaryStage;

    public LoginUI(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showLoginScene() {
        Label titleLabel = new Label("Welcome to the Digital Diary");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username or Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        Label messageLabel = new Label();

        loginButton.setOnAction(e -> {
            String userOrEmail = usernameField.getText();
            String password = passwordField.getText();

            User user = UserManager.loginUser(userOrEmail, password);
            if (user != null) {
                messageLabel.setText("Login successful. Welcome " + user.getUsername() + "!");
                DigitalDiaryUI diaryUI = new DigitalDiaryUI(primaryStage, user);
                diaryUI.showDiaryScene();
            } else {
                messageLabel.setText("Login failed. Please check your credentials.");
            }
        });

        registerButton.setOnAction(e -> {
            showRegistrationScene();
        });

        VBox root = new VBox(10, titleLabel, usernameField, passwordField, loginButton, registerButton, messageLabel);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login - Digital Diary");
        primaryStage.show();
    }

    private void showRegistrationScene() {
        Label regLabel = new Label("Register a New Account");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Label messageLabel = new Label();

        Button registerButton = new Button("Register");
        Button backButton = new Button("Back to Login");

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String pass = passwordField.getText();

            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                messageLabel.setText("Please fill in all fields.");
                return;
            }

            boolean success = UserManager.registerUser(username, email, pass);
            if (success) {
                messageLabel.setText("Registration successful! You can now log in.");
            } else {
                messageLabel.setText("Registration failed. Username or email may already be taken.");
            }
        });

        backButton.setOnAction(e -> showLoginScene());

        VBox regRoot = new VBox(10, regLabel, usernameField, emailField, passwordField, registerButton, backButton, messageLabel);
        regRoot.setPadding(new Insets(20));
        Scene regScene = new Scene(regRoot, 400, 300);
        primaryStage.setScene(regScene);
        primaryStage.setTitle("Register - Digital Diary");
    }
}
