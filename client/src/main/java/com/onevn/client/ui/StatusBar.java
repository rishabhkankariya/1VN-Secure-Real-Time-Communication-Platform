package com.onevn.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;

public class StatusBar {

    private final Circle statusDot = new Circle(4);
    private final Label statusLabel = new Label("Ready to connect");
    private final Label engineLabel = new Label("Embedded Engine (Port 5000) • SQLite Storage");
    private final HBox root;

    public StatusBar() {
        statusDot.getStyleClass().add("offline-dot");
        statusLabel.getStyleClass().add("status-label");

        engineLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: 500;");

        HBox left = new HBox(8, statusDot, statusLabel);
        left.setAlignment(Pos.CENTER_LEFT);

        root = new HBox(left, engineLabel);
        root.getStyleClass().add("status-bar");
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(6, 16, 6, 16));
        HBox.setHgrow(left, Priority.ALWAYS);
    }

    public HBox getRoot() {
        return root;
    }

    public void setStatus(String message) {
        if (message == null) {
            clear();
            return;
        }
        statusLabel.setText(message);

        statusDot.getStyleClass().removeAll("online-dot", "offline-dot");
        if (message.toLowerCase().contains("connected") || message.toLowerCase().contains("authenticated")) {
            statusDot.getStyleClass().add("online-dot");
        } else {
            statusDot.getStyleClass().add("offline-dot");
        }
    }

    public void clear() {
        statusLabel.setText("");
        statusDot.getStyleClass().removeAll("online-dot", "offline-dot");
        statusDot.getStyleClass().add("offline-dot");
    }
}
