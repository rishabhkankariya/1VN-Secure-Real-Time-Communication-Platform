package com.onevn.client.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Sidebar Pane for 1VN. Supports both:
 * 1. Room Mode (inspired by ip-chat: Room Code card, Copy button, Live room members)
 * 2. Account Mode (Direct 1-on-1 conversations, search, and user directory)
 */
public class ConversationPane {

    private final VBox root = new VBox();

    // --- Direct Chat / Account Mode Components ---
    private final VBox directChatBox = new VBox();
    private final ListView<String> listView = new ListView<>();
    private final Map<String, Boolean> onlineByName = new HashMap<>();
    private final List<String> allUsers = new ArrayList<>();
    private final TextField searchField = new TextField();
    private final Button newChatButton = new Button("New Chat");
    private final VBox newChatPanel = new VBox(8);
    private final TextField newUsernameField = new TextField();
    private final Label newChatError = new Label();
    private final Button startButton = new Button("Start");
    private final Button cancelButton = new Button("Cancel");

    // --- Room Mode Components (Inspired by ip-chat) ---
    private final VBox roomBox = new VBox(14);
    private final Label roomCodeDisplay = new Label("------");
    private final Button copyRoomCodeBtn = new Button("Copy Room Code");
    private final Label roomMembersHeading = new Label("👥 Room Members (0)");
    private final ListView<String> roomMembersListView = new ListView<>();
    private final Button leaveRoomBtn = new Button("Leave Room");

    private String currentRoomCode = null;
    private String roomAdmin = null;
    private Runnable onLeaveRoomRequested;

    private Consumer<String> onConversationSelected;
    private Consumer<String> onNewConversationRequested;
    private Consumer<String> onSearchRequested;
    private Runnable onRefreshDirectoryRequested;

    public ConversationPane() {
        buildDirectChatView();
        buildRoomSidebarView();

        root.getChildren().addAll(directChatBox, roomBox);
        root.setPrefWidth(250);
        root.setMinWidth(220);
        root.getStyleClass().add("conversation-pane");

        // Default to direct chat mode initially
        showDirectChatMode();
    }

    private void buildDirectChatView() {
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

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && onConversationSelected != null) {
                onConversationSelected.accept(newValue);
            }
        });

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

        directChatBox.getChildren().addAll(headerRow, searchBox, listView, footer);
        VBox.setVgrow(listView, Priority.ALWAYS);
        VBox.setVgrow(directChatBox, Priority.ALWAYS);
    }

    private void buildRoomSidebarView() {
        roomBox.setPadding(new Insets(14));

        // 1. Room Code Sidebar Card (like ip-chat sidebar-card room-code-sidebar-card)
        Circle hashCircle = new Circle(14);
        hashCircle.setStyle("-fx-fill: rgba(245, 158, 11, 0.2); -fx-stroke: #f59e0b; -fx-stroke-width: 1.5;");
        Label hashText = new Label("#");
        hashText.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: 800; -fx-font-size: 13px;");
        StackPane hashIcon = new StackPane(hashCircle, hashText);

        Label cardHeading = new Label("Room Code");
        cardHeading.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: 700;");

        HBox cardTop = new HBox(8, hashIcon, cardHeading);
        cardTop.setAlignment(Pos.CENTER_LEFT);

        roomCodeDisplay.getStyleClass().add("room-code-badge-large");
        roomCodeDisplay.setAlignment(Pos.CENTER);
        roomCodeDisplay.setMaxWidth(Double.MAX_VALUE);

        copyRoomCodeBtn.getStyleClass().add("btn-copy-code");
        copyRoomCodeBtn.setGraphic(VectorIcons.icon(VectorIcons.COPY, 13, "#ffffff"));
        copyRoomCodeBtn.setMaxWidth(Double.MAX_VALUE);
        copyRoomCodeBtn.setOnAction(e -> copyRoomCode());

        Label hintLabel = new Label("Share this code with others to join.");
        hintLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-alignment: center;");
        hintLabel.setWrapText(true);

        VBox roomCodeCard = new VBox(10, cardTop, roomCodeDisplay, copyRoomCodeBtn, hintLabel);
        roomCodeCard.getStyleClass().add("sidebar-room-card");

        // 2. Members Sidebar Card (like ip-chat members-sidebar-card)
        roomMembersHeading.getStyleClass().add("sidebar-section-title");
        roomMembersHeading.setPadding(new Insets(8, 0, 4, 0));

        roomMembersListView.getStyleClass().add("conversation-list-view");
        roomMembersListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String memberName, boolean empty) {
                super.updateItem(memberName, empty);
                if (empty || memberName == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Circle greenDot = new Circle(4);
                    greenDot.getStyleClass().add("online-dot");

                    Label nameLbl = new Label(memberName);
                    nameLbl.setStyle("-fx-text-fill: #f1f5f9; -fx-font-weight: 600; -fx-font-size: 13px;");

                    HBox left = new HBox(8, greenDot, nameLbl);
                    left.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(left, Priority.ALWAYS);

                    HBox row = new HBox(8, left);
                    row.setAlignment(Pos.CENTER_LEFT);

                    if (roomAdmin != null && memberName.equalsIgnoreCase(roomAdmin)) {
                        Label adminBadge = new Label("Host");
                        adminBadge.setStyle("-fx-background-color: rgba(245, 158, 11, 0.2); -fx-text-fill: #fbbf24; -fx-font-size: 10px; -fx-font-weight: 700; -fx-background-radius: 4; -fx-padding: 1 5;");
                        row.getChildren().add(adminBadge);
                    }

                    setGraphic(row);
                    setText(null);
                }
            }
        });
        VBox.setVgrow(roomMembersListView, Priority.ALWAYS);

        // 3. Leave Room Button
        leaveRoomBtn.getStyleClass().add("btn-leave-room");
        leaveRoomBtn.setGraphic(VectorIcons.icon(VectorIcons.LEAVE, 14, "#f87171"));
        leaveRoomBtn.setMaxWidth(Double.MAX_VALUE);
        leaveRoomBtn.setOnAction(e -> {
            if (onLeaveRoomRequested != null) {
                onLeaveRoomRequested.run();
            }
        });

        roomBox.getChildren().addAll(roomCodeCard, roomMembersHeading, roomMembersListView, leaveRoomBtn);
        VBox.setVgrow(roomBox, Priority.ALWAYS);
    }

    public void showRoomMode(String roomCode, String adminUser, List<String> members) {
        this.currentRoomCode = roomCode;
        this.roomAdmin = adminUser;
        roomCodeDisplay.setText(roomCode);

        directChatBox.setVisible(false);
        directChatBox.setManaged(false);

        roomBox.setVisible(true);
        roomBox.setManaged(true);

        updateRoomMembers(members);
    }

    public void updateRoomMembers(List<String> members) {
        if (members == null) {
            members = new ArrayList<>();
        }
        roomMembersListView.getItems().setAll(members);
        roomMembersHeading.setText("👥 Room Members (" + members.size() + ")");
    }

    public void showDirectChatMode() {
        this.currentRoomCode = null;
        this.roomAdmin = null;

        roomBox.setVisible(false);
        roomBox.setManaged(false);

        directChatBox.setVisible(true);
        directChatBox.setManaged(true);
    }

    private void copyRoomCode() {
        if (currentRoomCode == null || currentRoomCode.isBlank()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(currentRoomCode);
        clipboard.setContent(content);

        copyRoomCodeBtn.setText("✓ Copied!");
        copyRoomCodeBtn.setGraphic(VectorIcons.icon(VectorIcons.CHECK, 13, "#ffffff"));
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(ev -> {
            copyRoomCodeBtn.setText("Copy Room Code");
            copyRoomCodeBtn.setGraphic(VectorIcons.icon(VectorIcons.COPY, 13, "#ffffff"));
        });
        pt.play();
    }

    public void setOnLeaveRoomRequested(Runnable handler) {
        this.onLeaveRoomRequested = handler;
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
