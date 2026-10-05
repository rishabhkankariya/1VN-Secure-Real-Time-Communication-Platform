package com.onevn.client.ui;

import javafx.geometry.Insets;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConversationPane {

    private final ListView<String> listView = new ListView<>();
    private final VBox root;

    private final Map<String, Boolean> onlineByName = new HashMap<>();
    private final List<String> allUsers = new ArrayList<>();

    private Consumer<String> onConversationSelected;
    private Consumer<String> onNewConversationRequested;
    private Consumer<String> onSearchRequested;
    private Runnable onRefreshDirectoryRequested;

    private final Button newChatButton = new Button("New Chat");
    private final VBox newChatPanel = new VBox(8);
    private final TextField newUsernameField = new TextField();
    private final TextField searchField = new TextField();
    private final Label newChatError = new Label();
    private final Button startButton = new Button("Start");
    private final Button cancelButton = new Button("Cancel");

    public ConversationPane() {
        // --- Sidebar Header with Refresh ---
        Label headerTitle = new Label("CONVERSATIONS");
        headerTitle.getStyleClass().add("sidebar-section-title");

        Button refreshBtn = new Button();
        refreshBtn.getStyleClass().add("btn-icon-subtle");
        refreshBtn.setGraphic(VectorIcons.icon(VectorIcons.SIGNAL, 12, "#94a3b8"));
        refreshBtn.setOnAction(e -> {
            if (onRefreshDirectoryRequested != null) {
                onRefreshDirectoryRequested.run();
            }
        });

        HBox headerRow = new HBox(headerTitle, refreshBtn);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerTitle, Priority.ALWAYS);

        // --- Search bar ---
        searchField.setPromptText("Search server users...");
        searchField.getStyleClass().add("pill-input");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterUsers(newVal);
            if (onSearchRequested != null) {
                onSearchRequested.accept(newVal);
            }
        });
        HBox searchBox = new HBox(8, VectorIcons.icon(VectorIcons.SEARCH, 14, "#64748b"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getStyleClass().add("sidebar-search-box");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // --- List View Styling ---
        listView.getStyleClass().add("conversation-list-view");
        Label placeholder = new Label("No contacts yet\nSearch or click + New Chat below");
        placeholder.setStyle("-fx-text-fill: #64748b; -fx-alignment: center; -fx-text-alignment: center; -fx-font-size: 11px;");
        listView.setPlaceholder(placeholder);

        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean online = onlineByName.getOrDefault(item, false);

                    String initial = !item.isEmpty() ? item.substring(0, 1).toUpperCase() : "?";
                    Label avatarLabel = new Label(initial);
                    avatarLabel.getStyleClass().add("avatar-label");

                    Circle avatarCircle = new Circle(16);
                    avatarCircle.getStyleClass().add("avatar-circle");

                    StackPane avatar = new StackPane(avatarCircle, avatarLabel);

                    Circle statusDot = new Circle(4);
                    statusDot.getStyleClass().add(online ? "online-dot" : "offline-dot");

                    StackPane avatarWrap = new StackPane(avatar, statusDot);
                    StackPane.setAlignment(statusDot, Pos.BOTTOM_RIGHT);

                    Label nameLabel = new Label(item);
                    nameLabel.getStyleClass().add("conversation-name");

                    Label statusLabel = new Label(online ? "Active now" : "Offline");
                    statusLabel.getStyleClass().add("conversation-status-hint");

                    VBox infoBox = new VBox(2, nameLabel, statusLabel);

                    HBox row = new HBox(12, avatarWrap, infoBox);
                    row.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(row);
                    setText(null);
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

        // --- New Chat Panel ---
        newUsernameField.setPromptText("Enter recipient username");
        newUsernameField.setOnAction(e -> startNewConversation());

        newChatError.setStyle("-fx-text-fill: #f87171; -fx-font-weight: 600; -fx-font-size: 11px;");
        newChatError.setVisible(false);
        newChatError.setManaged(false);

        startButton.getStyleClass().add("btn-primary");
        startButton.setGraphic(VectorIcons.icon(VectorIcons.CHECK, 12, "#ffffff"));
        startButton.setOnAction(e -> startNewConversation());

        cancelButton.getStyleClass().add("btn-secondary");
        cancelButton.setOnAction(e -> hideNewChat());

        HBox actions = new HBox(8, startButton, cancelButton);
        newChatPanel.getChildren().addAll(newUsernameField, actions, newChatError);
        newChatPanel.setVisible(false);
        newChatPanel.setManaged(false);
        newChatPanel.setPadding(new Insets(10));
        newChatPanel.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 8; -fx-border-color: #334155; -fx-border-radius: 8;");

        newChatButton.getStyleClass().add("btn-primary");
        newChatButton.setGraphic(VectorIcons.icon(VectorIcons.PLUS, 14, "#ffffff"));
        newChatButton.setOnAction(e -> showNewChat());
        newChatButton.setMaxWidth(Double.MAX_VALUE);

        VBox footer = new VBox(8, newChatPanel, newChatButton);
        footer.setPadding(new Insets(10, 14, 14, 14));

        root = new VBox(headerRow, searchBox, listView, footer);
        root.setPrefWidth(240);
        root.setMinWidth(200);
        root.getStyleClass().add("conversation-pane");
        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    private void filterUsers(String query) {
        if (query == null || query.isBlank()) {
            listView.getItems().setAll(allUsers);
        } else {
            String lower = query.trim().toLowerCase();
            List<String> filtered = new ArrayList<>();
            for (String u : allUsers) {
                if (u.toLowerCase().contains(lower)) {
                    filtered.add(u);
                }
            }
            listView.getItems().setAll(filtered);
        }
    }

    public VBox getRoot() {
        return root;
    }

    public void setUsers(List<String> usernames) {
        allUsers.clear();
        if (usernames != null) {
            allUsers.addAll(usernames);
        }
        filterUsers(searchField.getText());
    }

    public void setSelectedUser(String username) {
        if (username == null) {
            listView.getSelectionModel().clearSelection();
            return;
        }

        if (username.equals(listView.getSelectionModel().getSelectedItem())) {
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

    public void setOnSearchRequested(Consumer<String> handler) {
        this.onSearchRequested = handler;
    }

    public void setOnRefreshDirectoryRequested(Runnable handler) {
        this.onRefreshDirectoryRequested = handler;
    }

    public void setOnline(String username, boolean online) {
        onlineByName.put(username, online);
        listView.refresh();
    }

    public void setOnNewConversationRequested(Consumer<String> handler) {
        this.onNewConversationRequested = handler;
    }

    public void showNewChat() {
        newChatPanel.setVisible(true);
        newChatPanel.setManaged(true);
        newChatError.setVisible(false);
        newChatError.setManaged(false);
        newUsernameField.clear();
        newUsernameField.requestFocus();
    }

    public void hideNewChat() {
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
            showNewChatError("Enter a username.");
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
        allUsers.clear();
        listView.getItems().clear();
        onlineByName.clear();
        listView.getSelectionModel().clearSelection();
        hideNewChat();
        showNewChatError(null);
    }
}
