package com.onevn.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AuthPane {

    private final TextField usernameField = new TextField();
    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button registerButton = new Button("Register");
    private final Button loginButton = new Button("Login");
    private final Label errorLabel = new Label();

    private final VBox root;

    private Runnable onRegister;
    private Runnable onLogin;

    public AuthPane() {
        root = new VBox();
        root.getStyleClass().add("auth-pane");
        root.setSpacing(12);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("1VN Chat");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 800;");

        usernameField.setPromptText("Username");
        emailField.setPromptText("Email");
        passwordField.setPromptText("Password");

        registerButton.setOnAction(event -> {
            clearError();
            if (onRegister != null) {
                onRegister.run();
            }
        });

        loginButton.setOnAction(event -> {
            clearError();
            if (onLogin != null) {
                onLogin.run();
            }
        });

        HBox usernameRow = new HBox(10,
                new Label("Username"), usernameField);
        HBox emailRow = new HBox(10,
                new Label("Email"), emailField);
        HBox passwordRow = new HBox(10,
                new Label("Password"), passwordField);

        usernameRow.setAlignment(Pos.CENTER_LEFT);
        emailRow.setAlignment(Pos.CENTER_LEFT);
        passwordRow.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(usernameField, Priority.ALWAYS);
        HBox.setHgrow(emailField, Priority.ALWAYS);
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        HBox buttonRow = new HBox(10, registerButton, loginButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.getStyleClass().add("auth-error");
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 600;");

        root.getChildren().addAll(title, usernameRow, emailRow, passwordRow, buttonRow, errorLabel);
    }

    public void setOnRegister(Runnable handler) {
        this.onRegister = handler;
    }

    public void setOnLogin(Runnable handler) {
        this.onLogin = handler;
    }

    public VBox getRoot() {
        return root;
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public void setAuthenticated(boolean authenticated) {
        usernameField.setDisable(authenticated);
        emailField.setDisable(authenticated);
        passwordField.setDisable(authenticated);
        registerButton.setDisable(authenticated);
        loginButton.setDisable(authenticated);
        if (authenticated) {
            clearError();
        }
    }

    public void showError(String message) {
        if (message == null || message.isBlank()) {
            clearError();
            return;
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
