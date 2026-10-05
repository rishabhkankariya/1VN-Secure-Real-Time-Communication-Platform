package com.onevn.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class AuthPane {

    private final TextField usernameField = new TextField();
    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField serverAddressField = new TextField("127.0.0.1:5000");
    private final Button actionButton = new Button("Sign In");
    private final Button toggleModeButton = new Button("Register");
    private final Button serverToggleBtn = new Button("Server");
    private final Label errorLabel = new Label();

    private final VBox root;
    private final HBox authBar;
    private final HBox profileBar;
    private final Label profileNameLabel = new Label();
    private final Label profileServerLabel = new Label("127.0.0.1:5000");

    private boolean registerMode = false;
    private boolean serverConfigOpen = false;
    private Runnable onRegister;
    private Runnable onLogin;
    private Runnable onLogout;

    public AuthPane() {
        root = new VBox();
        root.getStyleClass().add("auth-banner-root");

        // --- Brand Header Left ---
        Circle brandIconBg = new Circle(16);
        brandIconBg.getStyleClass().add("avatar-circle");
        StackPane brandLogo = new StackPane(brandIconBg, VectorIcons.icon(VectorIcons.CHAT_BUBBLE, 18, "#ffffff"));

        Label title = new Label("1VN");
        title.getStyleClass().add("brand-title");

        Label badge = new Label("SECURE");
        badge.getStyleClass().add("brand-badge");

        HBox brandBox = new HBox(8, brandLogo, title, badge);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        // --- Inputs with Vector Icon Badges ---
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(130);

        emailField.setPromptText("Email Address");
        emailField.setPrefWidth(150);
        emailField.setVisible(false);
        emailField.setManaged(false);

        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(120);

        serverAddressField.setPromptText("host:port (e.g. 127.0.0.1:5000)");
        serverAddressField.setPrefWidth(150);
        serverAddressField.setVisible(false);
        serverAddressField.setManaged(false);

        HBox userBox = new HBox(6, VectorIcons.icon(VectorIcons.USER, 14, "#94a3b8"), usernameField);
        userBox.setAlignment(Pos.CENTER_LEFT);

        HBox emailBox = new HBox(6, VectorIcons.icon(VectorIcons.MAIL, 14, "#94a3b8"), emailField);
        emailBox.setAlignment(Pos.CENTER_LEFT);
        emailBox.visibleProperty().bind(emailField.visibleProperty());
        emailBox.managedProperty().bind(emailField.managedProperty());

        HBox passBox = new HBox(6, VectorIcons.icon(VectorIcons.LOCK, 14, "#94a3b8"), passwordField);
        passBox.setAlignment(Pos.CENTER_LEFT);

        HBox serverBox = new HBox(6, VectorIcons.icon(VectorIcons.SIGNAL, 14, "#94a3b8"), serverAddressField);
        serverBox.setAlignment(Pos.CENTER_LEFT);
        serverBox.visibleProperty().bind(serverAddressField.visibleProperty());
        serverBox.managedProperty().bind(serverAddressField.managedProperty());

        // --- Buttons ---
        actionButton.getStyleClass().add("btn-primary");
        actionButton.setGraphic(VectorIcons.icon(VectorIcons.CHECK, 14, "#ffffff"));
        actionButton.setOnAction(event -> {
            clearError();
            if (registerMode) {
                if (onRegister != null) onRegister.run();
            } else {
                if (onLogin != null) onLogin.run();
            }
        });

        // Trigger action on Enter press
        passwordField.setOnAction(e -> actionButton.fire());
        usernameField.setOnAction(e -> {
            if (registerMode && emailField.getText().isBlank()) {
                emailField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
        });

        toggleModeButton.getStyleClass().add("btn-secondary");
        toggleModeButton.setOnAction(e -> setRegisterMode(!registerMode));

        serverToggleBtn.getStyleClass().add("btn-icon-subtle");
        serverToggleBtn.setGraphic(VectorIcons.icon(VectorIcons.SIGNAL, 13, "#94a3b8"));
        serverToggleBtn.setOnAction(e -> {
            serverConfigOpen = !serverConfigOpen;
            serverAddressField.setVisible(serverConfigOpen);
            serverAddressField.setManaged(serverConfigOpen);
            if (serverConfigOpen) {
                serverAddressField.requestFocus();
            }
        });

        HBox inputsBox = new HBox(10, userBox, emailBox, passBox, serverBox, actionButton, toggleModeButton, serverToggleBtn);
        inputsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(inputsBox, Priority.ALWAYS);

        authBar = new HBox(16, brandBox, inputsBox);
        authBar.setAlignment(Pos.CENTER_LEFT);

        // --- Logged-In Profile Bar ---
        Circle avatarCircle = new Circle(14);
        avatarCircle.getStyleClass().add("avatar-circle");
        Label avatarLetter = new Label("U");
        avatarLetter.getStyleClass().add("avatar-label");
        StackPane avatarWrap = new StackPane(avatarCircle, avatarLetter);

        profileNameLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: 700; -fx-font-size: 13px;");
        profileServerLabel.setStyle("-fx-text-fill: #34d399; -fx-font-size: 11px;");

        VBox userDetails = new VBox(1, profileNameLabel, profileServerLabel);

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("btn-secondary");
        logoutBtn.setGraphic(VectorIcons.icon(VectorIcons.LOGOUT, 14, "#f8fafc"));
        logoutBtn.setOnAction(e -> {
            if (onLogout != null) onLogout.run();
        });

        HBox profileLeft = new HBox(10, brandBox, new Label("•"), avatarWrap, userDetails);
        profileLeft.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(profileLeft, Priority.ALWAYS);

        profileBar = new HBox(16, profileLeft, logoutBtn);
        profileBar.setAlignment(Pos.CENTER_LEFT);
        profileBar.setVisible(false);
        profileBar.setManaged(false);

        // --- Error Alert ---
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: 600; -fx-font-size: 12px; -fx-padding: 4 0 0 4;");

        root.getChildren().addAll(authBar, profileBar, errorLabel);
    }

    private void setRegisterMode(boolean register) {
        this.registerMode = register;
        emailField.setVisible(register);
        emailField.setManaged(register);
        if (register) {
            actionButton.setText("Create Account");
            toggleModeButton.setText("Back to Sign In");
        } else {
            actionButton.setText("Sign In");
            toggleModeButton.setText("Register");
        }
        clearError();
    }

    public void setOnRegister(Runnable handler) {
        this.onRegister = handler;
    }

    public void setOnLogin(Runnable handler) {
        this.onLogin = handler;
    }

    public void setOnLogout(Runnable handler) {
        this.onLogout = handler;
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

    public String getServerHost() {
        String val = serverAddressField.getText().trim();
        if (val.contains(":")) {
            return val.substring(0, val.indexOf(":")).trim();
        }
        return val.isEmpty() ? "127.0.0.1" : val;
    }

    public int getServerPort() {
        String val = serverAddressField.getText().trim();
        if (val.contains(":")) {
            try {
                return Integer.parseInt(val.substring(val.indexOf(":") + 1).trim());
            } catch (NumberFormatException ignored) {}
        }
        return 5000;
    }

    public void setAuthenticated(boolean authenticated) {
        authBar.setVisible(!authenticated);
        authBar.setManaged(!authenticated);
        profileBar.setVisible(authenticated);
        profileBar.setManaged(authenticated);

        if (authenticated) {
            profileNameLabel.setText("@" + getUsername());
            profileServerLabel.setText("Connected to " + getServerHost() + ":" + getServerPort() + " • AES-256");
            clearError();
        } else {
            passwordField.clear();
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
