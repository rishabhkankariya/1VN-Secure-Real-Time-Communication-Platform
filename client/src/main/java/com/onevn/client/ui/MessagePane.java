package com.onevn.client.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MessagePane {

    private final VBox messageBox = new VBox(12);
    private final ScrollPane scrollPane;
    private final VBox emptyCard;
    private final StackPane root;

    private String currentUser;

    public MessagePane() {
        messageBox.getStyleClass().add("message-list");

        scrollPane = new ScrollPane(messageBox);
        scrollPane.getStyleClass().add("message-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // --- Modern Empty State Card with Vector Icon ---
        Label emptyTitle = new Label("Welcome to 1VN Messenger");
        emptyTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: 700;");

        Label emptyDesc = new Label("Select a conversation from the sidebar or click 'New Chat' to begin.\nAll communications are protected with AES-256 encryption at rest.");
        emptyDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-text-alignment: center;");

        emptyCard = new VBox(12,
                VectorIcons.icon(VectorIcons.SHIELD_CHECK, 48, "#6366f1"),
                emptyTitle,
                emptyDesc
        );
        emptyCard.getStyleClass().add("message-empty-card");
        emptyCard.setMaxSize(480, 220);

        root = new StackPane(scrollPane, emptyCard);
        root.getStyleClass().add("message-pane");
        StackPane.setAlignment(emptyCard, Pos.CENTER);
    }

    public StackPane getRoot() {
        return root;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public void clear() {
        messageBox.getChildren().clear();
    }

    public boolean isEmpty() {
        return messageBox.getChildren().isEmpty();
    }

    public void showEmptyState() {
        emptyCard.setVisible(true);
    }

    public void addMessage(String sender, String content) {
        if (sender == null || sender.isBlank()) {
            return;
        }

        emptyCard.setVisible(false);

        boolean own = sender.equals(currentUser);

        Label senderLabel = new Label(own ? "You" : sender);
        senderLabel.getStyleClass().add("message-sender");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("message-content");

        VBox bubble = new VBox(4, senderLabel, contentLabel);
        bubble.getStyleClass().add("message-bubble");

        HBox row = new HBox(bubble);
        row.getStyleClass().add("message-row");

        if (own) {
            bubble.getStyleClass().add("message-own");
            row.setAlignment(Pos.CENTER_RIGHT);
        } else {
            row.setAlignment(Pos.CENTER_LEFT);
        }

        messageBox.getChildren().add(row);
        scrollToBottom();
    }

    public void addSystemMessage(String content) {
        if (content == null || content.isBlank()) {
            return;
        }

        emptyCard.setVisible(false);

        Label label = new Label(content);
        label.setWrapText(true);
        label.getStyleClass().add("message-system-text");

        HBox pill = new HBox(6, VectorIcons.icon(VectorIcons.SIGNAL, 12, "#64748b"), label);
        pill.setAlignment(Pos.CENTER);
        pill.getStyleClass().add("message-system-pill");

        HBox row = new HBox(pill);
        row.setAlignment(Pos.CENTER);
        row.getStyleClass().add("message-system-row");

        messageBox.getChildren().add(row);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}
