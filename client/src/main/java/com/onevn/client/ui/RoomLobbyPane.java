package com.onevn.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Modern Lobby Pane inspired by ip-chat.
 * Provides instant 6-character Room Code creation and joining for zero-friction
 * cross-state and cross-network communication.
 */
public class RoomLobbyPane {

    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox root = new VBox(24);

    private final TextField createUsernameField = new TextField();
    private final Button createRoomButton = new Button("+ Create Room");

    private final TextField joinCodeField = new TextField();
    private final TextField joinUsernameField = new TextField();
    private final Button joinRoomButton = new Button("→ Join Room");

    private final Label errorLabel = new Label();
    private final Label noticeLabel = new Label();

    private Consumer<String> onCreateRoom;
    private BiConsumer<String, String> onJoinRoom;

    public RoomLobbyPane() {
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(32, 24, 40, 24));
        root.setMaxWidth(860);
        root.getStyleClass().add("lobby-root");

        // --- Hero Section ---
        Label eyebrow = new Label("PRIVATE • SECURE • CROSS-STATE COMMUNICATION");
        eyebrow.getStyleClass().add("lobby-eyebrow");

        Label title = new Label("1VN Instant Rooms");
        title.getStyleClass().add("lobby-title");

        Label subtitle = new Label("Real Conversations. Zero Friction. Instant Rooms.");
        subtitle.getStyleClass().add("lobby-subtitle");

        Label support = new Label("Chat directly with anyone across states and networks using a 6-character room code.\nNo registration required — simple, ultra-fast, and secure.");
        support.getStyleClass().add("lobby-support");

        VBox heroBox = new VBox(8, eyebrow, title, subtitle, support);
        heroBox.setAlignment(Pos.CENTER);

        // --- Error / Notice Display ---
        errorLabel.getStyleClass().add("lobby-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        noticeLabel.getStyleClass().add("lobby-notice");
        noticeLabel.setVisible(false);
        noticeLabel.setManaged(false);

        // --- Left Card: Create a Room ---
        Circle createIconBg = new Circle(20);
        createIconBg.setStyle("-fx-fill: rgba(99, 102, 241, 0.2); -fx-stroke: #6366f1; -fx-stroke-width: 1.5;");
        StackPane createIcon = new StackPane(createIconBg, VectorIcons.icon(VectorIcons.PLUS, 18, "#818cf8"));

        Label createHeading = new Label("Create a private room");
        createHeading.getStyleClass().add("room-card-heading");

        Label createDesc = new Label("Start a new room and share the 6-character code with your friends in any state or network.");
        createDesc.getStyleClass().add("room-card-desc");
        createDesc.setWrapText(true);

        Label createNameLabel = new Label("Your display name");
        createNameLabel.getStyleClass().add("field-label");

        createUsernameField.setPromptText("Enter your name (e.g. Rishabh)");
        createUsernameField.getStyleClass().add("lobby-input");
        createUsernameField.setOnAction(e -> triggerCreate());

        createRoomButton.getStyleClass().add("btn-create-room");
        createRoomButton.setMaxWidth(Double.MAX_VALUE);
        createRoomButton.setOnAction(e -> triggerCreate());

        VBox createCard = new VBox(12, createIcon, createHeading, createDesc, createNameLabel, createUsernameField, createRoomButton);
        createCard.getStyleClass().add("room-card");
        createCard.setPrefWidth(340);
        HBox.setHgrow(createCard, Priority.ALWAYS);

        // --- "OR" Divider Badge ---
        Circle orCircle = new Circle(18);
        orCircle.getStyleClass().add("or-circle");
        Label orText = new Label("OR");
        orText.getStyleClass().add("or-text");
        StackPane orBadge = new StackPane(orCircle, orText);
        orBadge.setAlignment(Pos.CENTER);

        // --- Right Card: Join a Room ---
        Circle joinIconBg = new Circle(20);
        joinIconBg.setStyle("-fx-fill: rgba(16, 185, 129, 0.2); -fx-stroke: #10b981; -fx-stroke-width: 1.5;");
        StackPane joinIcon = new StackPane(joinIconBg, VectorIcons.icon(VectorIcons.KEY, 18, "#34d399"));

        Label joinHeading = new Label("Join an existing room");
        joinHeading.getStyleClass().add("room-card-heading");

        Label joinDesc = new Label("Enter the 6-character room code from your friend to join the conversation instantly.");
        joinDesc.getStyleClass().add("room-card-desc");
        joinDesc.setWrapText(true);

        Label joinCodeLabel = new Label("Room code");
        joinCodeLabel.getStyleClass().add("field-label");

        joinCodeField.setPromptText("6-character code (e.g. X8K2M9)");
        joinCodeField.getStyleClass().add("lobby-input");
        joinCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String clean = newVal.toUpperCase().replaceAll("[^A-Z0-9]", "");
                if (clean.length() > 6) clean = clean.substring(0, 6);
                if (!clean.equals(newVal)) {
                    joinCodeField.setText(clean);
                }
            }
        });

        Label joinNameLabel = new Label("Your display name");
        joinNameLabel.getStyleClass().add("field-label");

        joinUsernameField.setPromptText("Enter your name");
        joinUsernameField.getStyleClass().add("lobby-input");
        joinUsernameField.setOnAction(e -> triggerJoin());
        joinCodeField.setOnAction(e -> joinUsernameField.requestFocus());

        // Keep username synced between cards
        createUsernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (joinUsernameField.getText().isEmpty() || joinUsernameField.getText().equals(oldVal)) {
                joinUsernameField.setText(newVal);
            }
        });

        joinRoomButton.getStyleClass().add("btn-join-room");
        joinRoomButton.setMaxWidth(Double.MAX_VALUE);
        joinRoomButton.setOnAction(e -> triggerJoin());

        VBox joinCard = new VBox(10, joinIcon, joinHeading, joinDesc, joinCodeLabel, joinCodeField, joinNameLabel, joinUsernameField, joinRoomButton);
        joinCard.getStyleClass().add("room-card");
        joinCard.setPrefWidth(340);
        HBox.setHgrow(joinCard, Priority.ALWAYS);

        // Cards Row
        HBox cardsRow = new HBox(18, createCard, orBadge, joinCard);
        cardsRow.setAlignment(Pos.CENTER);
        cardsRow.setMaxWidth(760);

        // --- Features Grid (Inspired by ip-chat) ---
        HBox featuresRow = new HBox(16,
                createFeatureBadge(VectorIcons.SHIELD_CHECK, "#818cf8", "No Account", "Zero setup required"),
                createFeatureBadge(VectorIcons.SIGNAL, "#a78bfa", "Cross-State", "Direct network link"),
                createFeatureBadge(VectorIcons.LOCK, "#34d399", "Encrypted", "Secure room packets"),
                createFeatureBadge(VectorIcons.USERS, "#fbbf24", "Live Rooms", "6-character share codes")
        );
        featuresRow.setAlignment(Pos.CENTER);
        featuresRow.setMaxWidth(760);

        root.getChildren().addAll(heroBox, errorLabel, noticeLabel, cardsRow, featuresRow);

        scrollPane.setContent(new StackPane(root));
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("message-scroll");
    }

    private VBox createFeatureBadge(String iconPath, String colorHex, String title, String subtitle) {
        Circle iconBg = new Circle(14);
        iconBg.setStyle("-fx-fill: rgba(255, 255, 255, 0.05); -fx-stroke: " + colorHex + "; -fx-stroke-width: 1;");
        StackPane icon = new StackPane(iconBg, VectorIcons.icon(iconPath, 14, colorHex));

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #f1f5f9; -fx-font-weight: 700; -fx-font-size: 12px;");

        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        VBox box = new VBox(6, icon, t, s);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("feature-pill-card");
        box.setPrefWidth(170);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void triggerCreate() {
        clearMessages();
        String name = createUsernameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter your display name to create a room.");
            createUsernameField.requestFocus();
            return;
        }
        if (onCreateRoom != null) {
            onCreateRoom.accept(name);
        }
    }

    private void triggerJoin() {
        clearMessages();
        String code = joinCodeField.getText().trim().toUpperCase();
        String name = joinUsernameField.getText().trim();
        if (code.length() != 6) {
            showError("Please enter a valid 6-character room code.");
            joinCodeField.requestFocus();
            return;
        }
        if (name.isEmpty()) {
            showError("Please enter your display name to join the room.");
            joinUsernameField.requestFocus();
            return;
        }
        if (onJoinRoom != null) {
            onJoinRoom.accept(code, name);
        }
    }

    public void setOnCreateRoom(Consumer<String> handler) {
        this.onCreateRoom = handler;
    }

    public void setOnJoinRoom(BiConsumer<String, String> handler) {
        this.onJoinRoom = handler;
    }

    public void showError(String message) {
        if (message == null || message.isBlank()) {
            clearMessages();
            return;
        }
        errorLabel.setText("⚠️ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        noticeLabel.setVisible(false);
        noticeLabel.setManaged(false);
    }

    public void showNotice(String message) {
        if (message == null || message.isBlank()) {
            clearMessages();
            return;
        }
        noticeLabel.setText("ℹ️ " + message);
        noticeLabel.setVisible(true);
        noticeLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    public void clearMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        noticeLabel.setVisible(false);
        noticeLabel.setManaged(false);
    }

    public void setUsername(String username) {
        if (username != null && !username.isBlank()) {
            createUsernameField.setText(username);
            joinUsernameField.setText(username);
        }
    }

    public ScrollPane getRoot() {
        return scrollPane;
    }
}
