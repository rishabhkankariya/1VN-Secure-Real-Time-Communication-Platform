package com.onevn.client.ui;

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
    private final Button serverToggleBtn = new Button();
    private final Label errorLabel = new Label();

    private final VBox root;

    // Modes in the Top Bar
    private final HBox lobbyTopBar;
    private final HBox accountTopBar;
    private final HBox profileBar;
    private final HBox roomActiveBar;

    private final Label profileNameLabel = new Label();
    private final Label profileServerLabel = new Label("127.0.0.1:5000");

    private final Label roomActiveCodeLabel = new Label();
    private final Label roomActiveUserLabel = new Label();

    private boolean registerMode = false;
    private boolean serverConfigOpen = false;

    private Runnable onRegister;
    private Runnable onLogin;
    private Runnable onLogout;
    private Runnable onLobbyRequested;
    private Runnable onAccountModeRequested;
    private Runnable onLeaveRoom;

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

        // --- 1. Lobby Top Bar (Default: Instant Rooms active) ---
        Button lobbyActiveBtn = new Button("⚡ Instant Rooms");
        lobbyActiveBtn.getStyleClass().add("btn-primary");
        lobbyActiveBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6 14;");

        Button switchToAccountBtn = new Button("🔐 Account Login");
        switchToAccountBtn.getStyleClass().add("btn-secondary");
        switchToAccountBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6 14;");
        switchToAccountBtn.setOnAction(e -> {
            if (onAccountModeRequested != null) onAccountModeRequested.run();
            showAccountBar();
        });

        serverAddressField.setPromptText("host:port (e.g. 127.0.0.1:5000)");
        serverAddressField.setPrefWidth(150);
        serverAddressField.setVisible(false);
        serverAddressField.setManaged(false);

        serverToggleBtn.getStyleClass().add("btn-icon-subtle");
        serverToggleBtn.setGraphic(VectorIcons.icon(VectorIcons.SIGNAL, 13, "#94a3b8"));
        serverToggleBtn.setOnAction(e -> toggleServerField());

        HBox serverBox = new HBox(6, VectorIcons.icon(VectorIcons.SIGNAL, 13, "#94a3b8"), serverAddressField);
        serverBox.setAlignment(Pos.CENTER_LEFT);
        serverBox.visibleProperty().bind(serverAddressField.visibleProperty());
        serverBox.managedProperty().bind(serverAddressField.managedProperty());

        HBox lobbyRight = new HBox(10, lobbyActiveBtn, switchToAccountBtn, serverBox, serverToggleBtn);
        lobbyRight.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(lobbyRight, Priority.ALWAYS);

        lobbyTopBar = new HBox(16, brandBox, lobbyRight);
        lobbyTopBar.setAlignment(Pos.CENTER_LEFT);

        // --- 2. Account Mode Top Bar (Inputs for Login/Register) ---
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(120);

        emailField.setPromptText("Email");
        emailField.setPrefWidth(130);
        emailField.setVisible(false);
        emailField.setManaged(false);

        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(110);

        HBox userBox = new HBox(6, VectorIcons.icon(VectorIcons.USER, 13, "#94a3b8"), usernameField);
        userBox.setAlignment(Pos.CENTER_LEFT);

        HBox emailBox = new HBox(6, VectorIcons.icon(VectorIcons.MAIL, 13, "#94a3b8"), emailField);
        emailBox.setAlignment(Pos.CENTER_LEFT);
        emailBox.visibleProperty().bind(emailField.visibleProperty());
        emailBox.managedProperty().bind(emailField.managedProperty());

        HBox passBox = new HBox(6, VectorIcons.icon(VectorIcons.LOCK, 13, "#94a3b8"), passwordField);
        passBox.setAlignment(Pos.CENTER_LEFT);

        actionButton.getStyleClass().add("btn-primary");
        actionButton.setGraphic(VectorIcons.icon(VectorIcons.CHECK, 13, "#ffffff"));
        actionButton.setOnAction(event -> {
            clearError();
            if (registerMode) {
                if (onRegister != null) onRegister.run();
            } else {
                if (onLogin != null) onLogin.run();
            }
        });

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

        Button backToLobbyBtn = new Button("⚡ Instant Rooms");
        backToLobbyBtn.getStyleClass().add("btn-secondary");
        backToLobbyBtn.setOnAction(e -> {
            if (onLobbyRequested != null) onLobbyRequested.run();
            showLobbyBar();
        });

        HBox accountInputs = new HBox(8, userBox, emailBox, passBox, actionButton, toggleModeButton, backToLobbyBtn);
        accountInputs.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(accountInputs, Priority.ALWAYS);

        // Recreate brand box copy for account bar
        Circle brandIconBg2 = new Circle(16);
        brandIconBg2.getStyleClass().add("avatar-circle");
        StackPane brandLogo2 = new StackPane(brandIconBg2, VectorIcons.icon(VectorIcons.CHAT_BUBBLE, 18, "#ffffff"));
        HBox brandBox2 = new HBox(8, brandLogo2, new Label("1VN"), new Label("ACCOUNTS"));
        brandBox2.setAlignment(Pos.CENTER_LEFT);
        ((Label) brandBox2.getChildren().get(1)).getStyleClass().add("brand-title");
        ((Label) brandBox2.getChildren().get(2)).getStyleClass().add("brand-badge");

        accountTopBar = new HBox(16, brandBox2, accountInputs);
        accountTopBar.setAlignment(Pos.CENTER_LEFT);
        accountTopBar.setVisible(false);
        accountTopBar.setManaged(false);

        // --- 3. Logged-In Profile Bar ---
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

        Button lobbyFromProfileBtn = new Button("⚡ Instant Rooms");
        lobbyFromProfileBtn.getStyleClass().add("btn-secondary");
        lobbyFromProfileBtn.setOnAction(e -> {
            if (onLobbyRequested != null) onLobbyRequested.run();
        });

        Circle brandIconBg3 = new Circle(16);
        brandIconBg3.getStyleClass().add("avatar-circle");
        StackPane brandLogo3 = new StackPane(brandIconBg3, VectorIcons.icon(VectorIcons.CHAT_BUBBLE, 18, "#ffffff"));
        HBox brandBox3 = new HBox(8, brandLogo3, new Label("1VN"), new Label("ONLINE"));
        brandBox3.setAlignment(Pos.CENTER_LEFT);
        ((Label) brandBox3.getChildren().get(1)).getStyleClass().add("brand-title");
        ((Label) brandBox3.getChildren().get(2)).getStyleClass().add("brand-badge");

        HBox profileLeft = new HBox(12, brandBox3, new Label("•"), avatarWrap, userDetails);
        profileLeft.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(profileLeft, Priority.ALWAYS);

        profileBar = new HBox(16, profileLeft, lobbyFromProfileBtn, logoutBtn);
        profileBar.setAlignment(Pos.CENTER_LEFT);
        profileBar.setVisible(false);
        profileBar.setManaged(false);

        // --- 4. Room Active Bar ---
        Circle brandIconBg4 = new Circle(16);
        brandIconBg4.getStyleClass().add("avatar-circle");
        StackPane brandLogo4 = new StackPane(brandIconBg4, VectorIcons.icon(VectorIcons.CHAT_BUBBLE, 18, "#ffffff"));
        HBox brandBox4 = new HBox(8, brandLogo4, new Label("1VN"), new Label("ROOM"));
        brandBox4.setAlignment(Pos.CENTER_LEFT);
        ((Label) brandBox4.getChildren().get(1)).getStyleClass().add("brand-title");
        ((Label) brandBox4.getChildren().get(2)).getStyleClass().add("brand-badge");

        roomActiveCodeLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: 800; -fx-font-size: 14px; -fx-background-color: rgba(245, 158, 11, 0.15); -fx-padding: 3 10; -fx-background-radius: 6;");
        roomActiveUserLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        HBox roomDetails = new HBox(12, brandBox4, roomActiveCodeLabel, roomActiveUserLabel);
        roomDetails.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(roomDetails, Priority.ALWAYS);

        Button leaveRoomTopBtn = new Button("Leave Room");
        leaveRoomTopBtn.getStyleClass().add("btn-secondary");
        leaveRoomTopBtn.setGraphic(VectorIcons.icon(VectorIcons.LEAVE, 14, "#f87171"));
        leaveRoomTopBtn.setOnAction(e -> {
            if (onLeaveRoom != null) onLeaveRoom.run();
        });

        roomActiveBar = new HBox(16, roomDetails, leaveRoomTopBtn);
        roomActiveBar.setAlignment(Pos.CENTER_LEFT);
        roomActiveBar.setVisible(false);
        roomActiveBar.setManaged(false);

        // --- Error Alert ---
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: 600; -fx-font-size: 12px; -fx-padding: 4 0 0 4;");

        root.getChildren().addAll(lobbyTopBar, accountTopBar, profileBar, roomActiveBar, errorLabel);
    }

    private void toggleServerField() {
        serverConfigOpen = !serverConfigOpen;
        serverAddressField.setVisible(serverConfigOpen);
        serverAddressField.setManaged(serverConfigOpen);
        if (serverConfigOpen) {
            serverAddressField.requestFocus();
        }
    }

    public void showLobbyBar() {
        lobbyTopBar.setVisible(true);
        lobbyTopBar.setManaged(true);
        accountTopBar.setVisible(false);
        accountTopBar.setManaged(false);
        profileBar.setVisible(false);
        profileBar.setManaged(false);
        roomActiveBar.setVisible(false);
        roomActiveBar.setManaged(false);
        clearError();
    }

    public void showAccountBar() {
        lobbyTopBar.setVisible(false);
        lobbyTopBar.setManaged(false);
        accountTopBar.setVisible(true);
        accountTopBar.setManaged(true);
        profileBar.setVisible(false);
        profileBar.setManaged(false);
        roomActiveBar.setVisible(false);
        roomActiveBar.setManaged(false);
        clearError();
    }

    public void showRoomActiveBar(String roomCode, String username) {
        roomActiveCodeLabel.setText("Room: #" + roomCode);
        roomActiveUserLabel.setText("Connected as @" + username);
        lobbyTopBar.setVisible(false);
        lobbyTopBar.setManaged(false);
        accountTopBar.setVisible(false);
        accountTopBar.setManaged(false);
        profileBar.setVisible(false);
        profileBar.setManaged(false);
        roomActiveBar.setVisible(true);
        roomActiveBar.setManaged(true);
        clearError();
    }

    public void setRegisterMode(boolean register) {
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

    public void setOnLobbyRequested(Runnable handler) {
        this.onLobbyRequested = handler;
    }

    public void setOnAccountModeRequested(Runnable handler) {
        this.onAccountModeRequested = handler;
    }

    public void setOnLeaveRoom(Runnable handler) {
        this.onLeaveRoom = handler;
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
        lobbyTopBar.setVisible(false);
        lobbyTopBar.setManaged(false);
        accountTopBar.setVisible(!authenticated);
        accountTopBar.setManaged(!authenticated);
        profileBar.setVisible(authenticated);
        profileBar.setManaged(authenticated);
        roomActiveBar.setVisible(false);
        roomActiveBar.setManaged(false);

        if (authenticated) {
            profileNameLabel.setText("@" + getUsername());
            profileServerLabel.setText("Connected to " + getServerHost() + ":" + getServerPort() + " • AES-256");
            clearError();
        } else {
            passwordField.clear();
            showLobbyBar();
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
