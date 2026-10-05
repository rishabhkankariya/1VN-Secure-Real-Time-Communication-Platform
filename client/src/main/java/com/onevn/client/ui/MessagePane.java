package com.onevn.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MessagePane {

    private final VBox messageBox = new VBox(10);
    private final ScrollPane scrollPane;
    private final Label emptyLabel = new Label("Select a conversation to start chatting.");
    private final StackPane root;

    private String currentUser;

    public MessagePane() {

        messageBox.getStyleClass().add("message-list");

        scrollPane = new ScrollPane(messageBox);
        scrollPane.getStyleClass().add("message-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        emptyLabel.getStyleClass().add("message-empty");
        emptyLabel.setVisible(false);

        root = new StackPane(scrollPane, emptyLabel);
        root.getStyleClass().add("message-pane");

        StackPane.setAlignment(emptyLabel, Pos.CENTER);
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
        emptyLabel.setVisible(true);
    }

    public void addMessage(String sender, String content) {

        if (sender == null || sender.isBlank()) {
            return;
        }

        emptyLabel.setVisible(false);

        boolean own = sender.equals(currentUser);

        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("message-sender");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("message-content");

        VBox bubble = new VBox(2, senderLabel, contentLabel);
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

        emptyLabel.setVisible(false);

        Label label = new Label(content);
        label.setWrapText(true);
        label.getStyleClass().add("message-system");

        HBox row = new HBox(label);
        row.setAlignment(Pos.CENTER);

        messageBox.getChildren().add(row);
        scrollToBottom();
    }

    private void scrollToBottom() {

        scrollPane.requestLayout();
        scrollPane.setVvalue(1);
    }
}
