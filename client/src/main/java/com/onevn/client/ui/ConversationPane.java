package com.onevn.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConversationPane {

    private final ListView<String> listView = new ListView<>();
    private final VBox root;

    private final Map<String, Boolean> onlineByName = new HashMap<>();

    private Consumer<String> onConversationSelected;
    private Consumer<String> onNewConversationRequested;
    private final Button newChatButton = new Button("+ New Chat");
    private final VBox newChatPanel = new VBox(8);
    private final TextField newUsernameField = new TextField();
    private final Label newChatError = new Label();
    private final Button startButton = new Button("Start");
    private final Button cancelButton = new Button("Cancel");
    private boolean newChatVisible = false;

    public ConversationPane() {

        Label header = new Label("Conversations");
        header.getStyleClass().add("conversation-header");

        Label placeholder = new Label("No conversations");
        placeholder.getStyleClass().add("message-empty");
        listView.setPlaceholder(placeholder);

        listView.setCellFactory(list -> new ListCell<>() {

            @Override
            protected void updateItem(String item, boolean empty) {

                super.updateItem(item, empty);
                getStyleClass().remove("online");
                getStyleClass().remove("offline");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean online =
                            onlineByName.getOrDefault(item, false);

                    String initial = item.length() > 0
                            ? item.substring(0, 1).toUpperCase()
                            : "?";
                    Label avatarLabel = new Label(initial);
                    avatarLabel.getStyleClass().add("avatar-label");

                    Circle avatarCircle = new Circle(14);
                    avatarCircle.getStyleClass().add("avatar-circle");

                    StackPane avatar = new StackPane(avatarCircle, avatarLabel);
                    avatar.getStyleClass().add("avatar");

                    Circle onlineDot = new Circle(3.5);
                    onlineDot.getStyleClass().add("online-dot");
                    onlineDot.setVisible(online);

                    StackPane avatarWrap = new StackPane(avatar, onlineDot);
                    StackPane.setAlignment(onlineDot, Pos.BOTTOM_RIGHT);

                    Label name = new Label(item);
                    HBox row = new HBox(10, avatarWrap, name);
                    row.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(row);
                    setText(null);
                    if (online) {
                        getStyleClass().add("online");
                    } else {
                        getStyleClass().add("offline");
                    }
                }
            }
        });

        listView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && onConversationSelected != null) {
                        onConversationSelected.accept(newValue);
                    }
                });

        newUsernameField.setPromptText("Username");
        newChatError.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 600;");
        newChatError.setVisible(false);
        newChatError.setManaged(false);
        startButton.setOnAction(e -> startNewConversation());
        cancelButton.setOnAction(e -> hideNewChat());
        HBox actions = new HBox(6, startButton, cancelButton);
        newChatPanel.getChildren().addAll(newUsernameField, actions, newChatError);
        newChatPanel.setVisible(false);
        newChatPanel.setManaged(false);
        newChatPanel.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));
        newChatPanel.getStyleClass().add("new-chat-panel");

        newChatButton.setOnAction(e -> showNewChat());
        newChatButton.setMaxWidth(Double.MAX_VALUE);
        VBox footer = new VBox(6, newChatPanel, newChatButton);
        footer.setPadding(new javafx.geometry.Insets(6, 10, 6, 10));

        root = new VBox(header, listView, footer);
        root.setPrefWidth(200);
        root.getStyleClass().add("conversation-pane");
        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    public VBox getRoot() {
        return root;
    }

    public void setUsers(List<String> usernames) {

        listView.getItems().setAll(usernames);
        listView.refresh();
    }

    public void setSelectedUser(String username) {

        if (username == null) {
            listView.getSelectionModel().clearSelection();
            return;
        }

        if (username.equals(
                listView.getSelectionModel().getSelectedItem())) {
            return;
        }

        int index = listView.getItems().indexOf(username);

        if (index >= 0) {
            listView.getSelectionModel().select(index);
        }
    }

    public void setOnConversationSelected(Consumer<String> handler) {
        this.onConversationSelected = handler;
    }

    public void setOnline(String username, boolean online) {

        onlineByName.put(username, online);
        listView.refresh();
    }

    public void setOnNewConversationRequested(Consumer<String> handler) {
        this.onNewConversationRequested = handler;
    }

    public void showNewChat() {
        newChatVisible = true;
        newChatPanel.setVisible(true);
        newChatPanel.setManaged(true);
        newChatError.setVisible(false);
        newChatError.setManaged(false);
        newUsernameField.clear();
        newUsernameField.requestFocus();
    }

    public void hideNewChat() {
        newChatVisible = false;
        newChatPanel.setVisible(false);
        newChatPanel.setManaged(false);
        newChatError.setVisible(false);
        newChatError.setManaged(false);
    }

    public void showNewChatError(String message) {
        if (message == null || message.isBlank()) {
            newChatError.setVisible(false);
            newChatError.setManaged(false);
            return;
        }
        newChatError.setText(message);
        newChatError.setVisible(true);
        newChatError.setManaged(true);
    }

    private void startNewConversation() {
        String username = newUsernameField.getText().trim();
        if (username.isBlank()) {
            showNewChatError("Enter username.");
            return;
        }
        if (username.contains(" ")) {
            showNewChatError("Username cannot contain spaces.");
            return;
        }
        if (onNewConversationRequested != null) {
            onNewConversationRequested.accept(username);
        }
    }

    public void clear() {

        listView.getItems().clear();
        onlineByName.clear();
        listView.getSelectionModel().clearSelection();
        hideNewChat();
        showNewChatError(null);
    }
}
