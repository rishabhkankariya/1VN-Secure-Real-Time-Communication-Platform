package com.onevn.client.ui;

import com.onevn.client.service.ChatService;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChatView {

    private final AuthPane authPane = new AuthPane();
    private final ConversationPane conversationPane = new ConversationPane();
    private final MessagePane messagePane = new MessagePane();
    private final StatusBar statusBar = new StatusBar();
    private final TextField messageField = new TextField();
    private final Button sendButton = new Button();

    private final Button historyButton = new Button("History");
    private final Button presenceButton = new Button("Check Status");

    // Chat Header Components
    private final Label peerTitleLabel = new Label("Select a conversation");
    private final Label peerStatusLabel = new Label("Select a contact to begin messaging");
    private final Circle peerAvatarCircle = new Circle(16);
    private final Label peerAvatarLabel = new Label("?");
    private final HBox encryptedBadge = new HBox(6);

    private final ChatService chatService = new ChatService();
    private final Set<String> knownUsers = new LinkedHashSet<>();

    private boolean connected = false;
    private boolean authenticated = false;
    private boolean loadingHistory = false;
    private String selectedUser = null;

    public BorderPane createView() {

        messageField.setPromptText("Type a message... (Press Enter to send)");
        messageField.getStyleClass().add("pill-input");
        messageField.setDisable(true);
        messageField.setOnAction(event -> sendMessage());

        sendButton.getStyleClass().add("btn-round-send");
        sendButton.setGraphic(VectorIcons.icon(VectorIcons.SEND, 16, "#ffffff"));
        sendButton.setTooltip(new Tooltip("Send Message (Enter)"));
        sendButton.setDisable(true);
        sendButton.setOnAction(event -> sendMessage());

        historyButton.getStyleClass().add("btn-secondary");
        historyButton.setGraphic(VectorIcons.icon(VectorIcons.HISTORY, 13, "#f8fafc"));
        historyButton.setDisable(true);
        historyButton.setOnAction(event -> loadHistory());

        presenceButton.getStyleClass().add("btn-secondary");
        presenceButton.setGraphic(VectorIcons.icon(VectorIcons.SIGNAL, 13, "#f8fafc"));
        presenceButton.setDisable(true);
        presenceButton.setOnAction(event -> checkPresence());

        authPane.setOnRegister(this::register);
        authPane.setOnLogin(this::login);
        authPane.setOnLogout(this::logout);

        conversationPane.setOnConversationSelected(this::onConversationSelected);
        conversationPane.setOnNewConversationRequested(this::onNewConversationRequested);
        conversationPane.setOnSearchRequested(this::searchUsers);
        conversationPane.setOnRefreshDirectoryRequested(() -> {
            if (authenticated) {
                statusBar.setStatus("Refreshing server user directory...");
                chatService.searchUsers("");
            }
        });

        // --- Chat Header Bar ---
        peerAvatarCircle.getStyleClass().add("avatar-circle");
        peerAvatarLabel.getStyleClass().add("avatar-label");
        StackPane peerAvatar = new StackPane(peerAvatarCircle, peerAvatarLabel);

        peerTitleLabel.getStyleClass().add("chat-header-peer");
        peerStatusLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        Label encLabel = new Label("End-to-End Encrypted");
        encLabel.getStyleClass().add("encrypted-text");
        encryptedBadge.getChildren().addAll(VectorIcons.icon(VectorIcons.SHIELD_CHECK, 13, "#34d399"), encLabel);
        encryptedBadge.getStyleClass().add("encrypted-pill");
        encryptedBadge.setAlignment(Pos.CENTER_LEFT);

        VBox peerInfo = new VBox(2, peerTitleLabel, peerStatusLabel);
        HBox headerLeft = new HBox(12, peerAvatar, peerInfo, encryptedBadge);
        headerLeft.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerLeft, Priority.ALWAYS);

        HBox headerActions = new HBox(8, historyButton, presenceButton);
        headerActions.setAlignment(Pos.CENTER_RIGHT);

        HBox chatHeader = new HBox(16, headerLeft, headerActions);
        chatHeader.getStyleClass().add("chat-header-bar");
        chatHeader.setAlignment(Pos.CENTER_LEFT);

        // --- Bottom Message Input Row ---
        HBox inputBox = new HBox(12, messageField, sendButton);
        HBox.setHgrow(messageField, Priority.ALWAYS);
        inputBox.getStyleClass().add("message-input-row");
        inputBox.setAlignment(Pos.CENTER_LEFT);

        // --- Center Workspace ---
        VBox centerPane = new VBox(
                chatHeader,
                messagePane.getRoot(),
                inputBox
        );
        centerPane.getStyleClass().add("chat-center");
        VBox.setVgrow(messagePane.getRoot(), Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(authPane.getRoot());
        root.setLeft(conversationPane.getRoot());
        root.setCenter(centerPane);
        root.setBottom(statusBar.getRoot());

        return root;
    }

    private void register() {
        String username = authPane.getUsername();
        String email = authPane.getEmail();
        String password = authPane.getPassword();

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            authPane.showError("Please enter username, email and password.");
            statusBar.setStatus("Please fill in all fields.");
            return;
        }

        if (username.contains(" ") || email.contains(" ")) {
            authPane.showError("Username and email cannot contain spaces.");
            statusBar.setStatus("Username and email cannot contain spaces.");
            return;
        }

        if (!ensureConnected()) {
            return;
        }

        statusBar.setStatus("Registering new account...");
        chatService.register(username, email, password);
    }

    private void login() {
        String username = authPane.getUsername();
        String password = authPane.getPassword();

        if (username.isBlank() || password.isBlank()) {
            authPane.showError("Please enter username and password.");
            statusBar.setStatus("Please enter username and password.");
            return;
        }

        if (username.contains(" ")) {
            authPane.showError("Username cannot contain spaces.");
            statusBar.setStatus("Username cannot contain spaces.");
            return;
        }

        if (!ensureConnected()) {
            return;
        }

        statusBar.setStatus("Signing in...");
        chatService.login(username, password);
    }

    private void logout() {
        authenticated = false;
        selectedUser = null;
        knownUsers.clear();
        conversationPane.clear();
        messagePane.clear();
        messagePane.showEmptyState();
        peerTitleLabel.setText("Select a conversation");
        peerAvatarLabel.setText("?");
        peerStatusLabel.setText("Select a contact to begin messaging");
        messageField.setDisable(true);
        sendButton.setDisable(true);
        historyButton.setDisable(true);
        presenceButton.setDisable(true);
        authPane.setAuthenticated(false);
        statusBar.setStatus("Signed out. Ready to connect.");
    }

    private boolean ensureConnected() {
        if (connected) {
            return true;
        }

        String host = authPane.getServerHost();
        int port = authPane.getServerPort();

        try {
            chatService.connect(host, port, this::onServerMessage);
            connected = true;
            statusBar.setStatus("Connected to 1VN Server (" + host + ":" + port + ")");
            authPane.clearError();
            return true;
        } catch (IOException e) {
            statusBar.setStatus("Could not connect to " + host + ":" + port);
            authPane.showError("Could not connect to " + host + ":" + port + ". Make sure server is reachable.");
            return false;
        }
    }

    private void searchUsers(String query) {
        if (authenticated) {
            chatService.searchUsers(query);
        }
    }

    private void onServerMessage(String message) {
        Platform.runLater(() -> handleServerMessage(message));
    }

    private void handleServerMessage(String message) {
        if (message.equals("REGISTER OK") || message.equals("LOGIN OK")) {
            authenticated = true;
            statusBar.setStatus("Authenticated as @" + authPane.getUsername());
            authPane.setAuthenticated(true);

            if (selectedUser == null) {
                messageField.setDisable(true);
                sendButton.setDisable(true);
            } else {
                messageField.setDisable(false);
                sendButton.setDisable(false);
                messageField.requestFocus();
            }

            messagePane.setCurrentUser(authPane.getUsername());
            messagePane.addSystemMessage("Welcome, @" + authPane.getUsername() + "! End-to-end encrypted session active.");

            // Request user directory from server
            chatService.searchUsers("");

        } else if (message.startsWith("USERS_RESULT")) {
            String payload = message.length() > "USERS_RESULT".length()
                    ? message.substring("USERS_RESULT".length()).trim()
                    : "";
            if (!payload.isEmpty()) {
                String[] pairs = payload.split(",");
                for (String pair : pairs) {
                    String[] item = pair.split(":");
                    if (item.length == 2 && !item[0].isBlank()) {
                        addKnownUser(item[0]);
                        conversationPane.setOnline(item[0], item[1].equalsIgnoreCase("ONLINE"));
                    }
                }
            }

        } else if (message.equals("HISTORY BEGIN")) {
            loadingHistory = true;
            messagePane.clear();

        } else if (message.equals("HISTORY END")) {
            loadingHistory = false;
            if (messagePane.isEmpty()) {
                messagePane.showEmptyState();
            }

        } else if (loadingHistory && message.startsWith("MSG ")) {
            String text = message.substring("MSG ".length());
            int separator = text.indexOf(": ");

            if (separator > 0) {
                String msgSender = text.substring(0, separator);
                String msgText = text.substring(separator + 2);
                messagePane.addMessage(msgSender, msgText);
            } else {
                messagePane.addSystemMessage(text);
            }

        } else if (message.startsWith("PRESENCE ")) {
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                addKnownUser(parts[1]);
                boolean isOnline = parts[2].equalsIgnoreCase("ONLINE");
                conversationPane.setOnline(parts[1], isOnline);
                if (parts[1].equals(selectedUser)) {
                    peerStatusLabel.setText(isOnline ? "Active now" : "Offline");
                }
            } else {
                messagePane.addSystemMessage(message);
            }

        } else if (message.contains("User not found")) {
            statusBar.setStatus("User '" + (selectedUser != null ? selectedUser : "") + "' not registered on server.");
            peerStatusLabel.setText("User not registered on server");
            messagePane.showUserNotFound(selectedUser);

        } else if (message.startsWith("REGISTER ERROR")
                || message.startsWith("LOGIN ERROR")
                || message.startsWith("ERROR:")) {

            statusBar.setStatus(message);
            if (message.startsWith("LOGIN ERROR")) {
                authPane.showError(message.substring("LOGIN ERROR".length()).trim());
            } else if (message.startsWith("REGISTER ERROR")) {
                authPane.showError(message.substring("REGISTER ERROR".length()).trim());
            } else {
                authPane.clearError();
                messagePane.addSystemMessage(message);
            }

        } else {
            int separator = message.indexOf(": ");
            if (separator > 0 && !message.startsWith("You: ")) {
                String senderName = message.substring(0, separator);
                addKnownUser(senderName);
                messagePane.addMessage(senderName, message.substring(separator + 2));
            } else {
                messagePane.addSystemMessage(message);
            }
        }
    }

    private void onConversationSelected(String user) {
        if (!authenticated || user == null) {
            return;
        }
        if (user.equals(selectedUser)) {
            return;
        }

        selectedUser = user;
        peerTitleLabel.setText("@" + user);
        peerAvatarLabel.setText(!user.isEmpty() ? user.substring(0, 1).toUpperCase() : "?");
        peerStatusLabel.setText("Connecting session...");

        messageField.setDisable(false);
        sendButton.setDisable(false);
        historyButton.setDisable(false);
        presenceButton.setDisable(false);

        messagePane.clear();
        chatService.requestHistory(user);
        chatService.requestPresence(user);
        messageField.requestFocus();
    }

    private void onNewConversationRequested(String user) {
        if (!authenticated || user == null) {
            return;
        }
        if (user.equalsIgnoreCase(authPane.getUsername())) {
            conversationPane.showNewChatError("Cannot start conversation with yourself.");
            return;
        }
        addKnownUser(user);
        conversationPane.hideNewChat();
        conversationPane.setSelectedUser(user);
        onConversationSelected(user);
    }

    private void addKnownUser(String user) {
        if (user == null || user.isBlank() || user.contains(" ")) {
            return;
        }

        if (knownUsers.add(user)) {
            conversationPane.setUsers(new ArrayList<>(knownUsers));
            if (user.equals(selectedUser)) {
                conversationPane.setSelectedUser(user);
            }
        }
    }

    private void loadHistory() {
        if (!authenticated || selectedUser == null) {
            statusBar.setStatus("Select a conversation to load history.");
            return;
        }
        chatService.requestHistory(selectedUser);
    }

    private void checkPresence() {
        if (!authenticated || selectedUser == null) {
            statusBar.setStatus("Select a conversation to check status.");
            return;
        }
        chatService.requestPresence(selectedUser);
    }

    private void sendMessage() {
        if (!authenticated) {
            return;
        }

        if (selectedUser == null) {
            statusBar.setStatus("Select a conversation first.");
            messageField.setDisable(true);
            sendButton.setDisable(true);
            return;
        }

        String target = selectedUser;
        String text = messageField.getText();
        if (text == null || text.trim().isBlank()) {
            return;
        }

        chatService.sendMessage("TO " + target + " " + text.trim());
        messageField.clear();
    }
}
