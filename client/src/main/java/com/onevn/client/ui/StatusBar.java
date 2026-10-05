package com.onevn.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatusBar {

    private final Label label = new Label("Not connected");
    private final HBox root;

    public StatusBar() {

        label.getStyleClass().add("status-label");

        root = new HBox(label);
        root.getStyleClass().add("status-bar");
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(4, 10, 4, 10));
    }

    public HBox getRoot() {
        return root;
    }

    public void setStatus(String message) {
        label.setText(message);
    }

    public void clear() {
        label.setText("");
    }
}
