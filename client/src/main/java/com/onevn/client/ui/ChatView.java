package com.onevn.client.ui;

import com.onevn.client.service.ChatService;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChatView {

    private final AuthPane authPane = new AuthPane();
    private final ConversationPane conversationPane = new ConversationPane();
    private final RoomLobbyPane roomLobbyPane = new RoomLobbyPane();
    private final MessagePane messagePane = new MessagePane();
    private final StatusBar statusBar = new StatusBar();
    private final TextField messageField = new TextField();
    private final Button sendButton = new Button();

    private final Button historyButton = new Button("History");
    private final Button presenceButton = new Button("Check Status");
    private final Button copyRoomCodeHeaderBtn = new Button("Copy Code");
    private final Button leaveRoomHeaderBtn = new Button("Leave Room");

    // Chat Header Components
    private final Label peerTitleLabel = new Label("Select a conversation");
    private final Label peerStatusLabel = new Label("Select a contact to begin messaging");
    private final Circle peerAvatarCircle = new Circle(16);
    private final Label peerAvatarLabel = new Label("?");
    private final StackPane peerAvatar = new StackPane(peerAvatarCircle, peerAvatarLabel);
    private final HBox encryptedBadge = new HBox(6);

    private final StackPane centerStack = new StackPane();
    private VBox chatCenter;

    private final ChatService chatService = new ChatService();
    private final Set<String> knownUsers = new LinkedHashSet<>();
    private final List<String> roomMembers = new ArrayList<>();

    private boolean connected = false;
    private boolean authenticated = false;
    private boolean loadingHistory = false;

    private String currentUsername = null;
    private String selectedUser = null;
    private String activeRoomCode = null;
    private String roomAdmin = null;

    public BorderPane createView() {

        // --- Bottom Message Input ---
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

        copyRoomCodeHeaderBtn.getStyleClass().add("btn-secondary");
        copyRoomCodeHeaderBtn.setGraphic(VectorIcons.icon(VectorIcons.COPY, 13, "#f8fafc"));
        copyRoomCodeHeaderBtn.setVisible(false);
        copyRoomCodeHeaderBtn.setManaged(false);
        copyRoomCodeHeaderBtn.setOnAction(e -> copyActiveRoomCode());

        leaveRoomHeaderBtn.getStyleClass().add("btn-leave-room");
        leaveRoomHeaderBtn.setGraphic(VectorIcons.icon(VectorIcons.LEAVE, 13, "#f87171"));
        leaveRoomHeaderBtn.setVisible(false);
        leaveRoomHeaderBtn.setManaged(false);
        leaveRoomHeaderBtn.setOnAction(e -> leaveCurrentRoom());

        // Wire AuthPane events
        authPane.setOnRegister(this::register);
        authPane.setOnLogin(this::login);
        authPane.setOnLogout(this::logout);
        authPane.setOnLobbyRequested(this::showLobby);
        authPane.setOnAccountModeRequested(() -> {
            if (activeRoomCode == null && !authenticated) {
                authPane.showAccountBar();
            }
        });
        authPane.setOnLeaveRoom(this::leaveCurrentRoom);

        // Wire RoomLobbyPane events
        roomLobbyPane.setOnCreateRoom(this::createRoom);
        roomLobbyPane.setOnJoinRoom(this::joinRoom);

        // Wire ConversationPane events
        conversationPane.setOnConversationSelected(this::onConversationSelected);
        conversationPane.setOnNewConversationRequested(this::onNewConversationRequested);
        conversationPane.setOnSearchRequested(this::searchUsers);
        conversationPane.setOnLeaveRoomRequested(this::leaveCurrentRoom);
        conversationPane.setOnRefreshDirectoryRequested(() -> {
            if (authenticated) {
                statusBar.setStatus("Refreshing server user directory...");
                chatService.searchUsers("");
            }
        });

        // --- Chat Header Bar ---
        peerAvatarCircle.getStyleClass().add("avatar-circle");
        peerAvatarLabel.getStyleClass().add("avatar-label");

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

        HBox headerActions = new HBox(8, copyRoomCodeHeaderBtn, leaveRoomHeaderBtn, historyButton, presenceButton);
        headerActions.setAlignment(Pos.CENTER_RIGHT);

        HBox chatHeader = new HBox(16, headerLeft, headerActions);
        chatHeader.getStyleClass().add("chat-header-bar");
        chatHeader.setAlignment(Pos.CENTER_LEFT);

        // --- Bottom Message Input Row ---
        HBox inputBox = new HBox(12, messageField, sendButton);
        HBox.setHgrow(messageField, Priority.ALWAYS);
        inputBox.getStyleClass().add("message-input-row");
        inputBox.setAlignment(Pos.CENTER_LEFT);

        // --- Center Workspace: Chat View ---
        chatCenter = new VBox(
                chatHeader,
                messagePane.getRoot(),
                inputBox
        );
        chatCenter.getStyleClass().add("chat-center");
        VBox.setVgrow(messagePane.getRoot(), Priority.ALWAYS);

        // Container Stack: toggles between Lobby view and Active Chat view
        centerStack.getChildren().addAll(chatCenter, roomLobbyPane.getRoot());
        VBox.setVgrow(centerStack, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(authPane.getRoot());
        root.setLeft(conversationPane.getRoot());
        root.setCenter(centerStack);
        root.setBottom(statusBar.getRoot());

        // Default: Show Lobby view
        showLobby();

        return root;
    }

    private void showLobby() {
        activeRoomCode = null;
        selectedUser = null;
        roomLobbyPane.getRoot().setVisible(true);
        roomLobbyPane.getRoot().setManaged(true);
        chatCenter.setVisible(false);
        chatCenter.setManaged(false);

        if (!authenticated) {
            authPane.showLobbyBar();
            conversationPane.showDirectChatMode();
        } else {
            authPane.setAuthenticated(true);
        }

        statusBar.setStatus("1VN Instant Rooms: Ready to create or join a room.");
    }

    private void showActiveRoomView(String roomCode, String username) {
        activeRoomCode = roomCode;
        selectedUser = null;
        currentUsername = username;

        roomLobbyPane.getRoot().setVisible(false);
        roomLobbyPane.getRoot().setManaged(false);
        chatCenter.setVisible(true);
        chatCenter.setManaged(true);

        // Configure Chat Header for Room Mode
        peerAvatarCircle.setStyle("-fx-fill: linear-gradient(to bottom right, #f59e0b, #d97706); -fx-stroke: #f59e0b;");
        peerAvatarLabel.setText("#");
        peerTitleLabel.setText("Room #" + roomCode);
        peerStatusLabel.setText(roomMembers.size() + " connected members • Instant Room");

        copyRoomCodeHeaderBtn.setVisible(true);
        copyRoomCodeHeaderBtn.setManaged(true);
        leaveRoomHeaderBtn.setVisible(true);
        leaveRoomHeaderBtn.setManaged(true);
        historyButton.setVisible(false);
        historyButton.setManaged(false);
        presenceButton.setVisible(false);
        presenceButton.setManaged(false);

        messageField.setDisable(false);
        sendButton.setDisable(false);
        messageField.setPromptText("Message #" + roomCode + "... (Press Enter to send)");
        messageField.requestFocus();

        messagePane.setCurrentUser(username);
        messagePane.clear();
        messagePane.addSystemMessage("🎉 You entered Room #" + roomCode + " as @" + username + ". Messages are broadcast to all room members.");

        // Update Top Bar & Sidebar
        authPane.showRoomActiveBar(roomCode, username);
        conversationPane.showRoomMode(roomCode, roomAdmin, roomMembers);

        statusBar.setStatus("Connected to Room #" + roomCode + " (" + roomMembers.size() + " members)");
    }

    private void showDirectChatView(String user) {
        activeRoomCode = null;
        selectedUser = user;

        roomLobbyPane.getRoot().setVisible(false);
        roomLobbyPane.getRoot().setManaged(false);
        chatCenter.setVisible(true);
        chatCenter.setManaged(true);

        peerAvatarCircle.setStyle("");
        peerAvatarCircle.getStyleClass().setAll("avatar-circle");
        peerAvatarLabel.setText(!user.isEmpty() ? user.substring(0, 1).toUpperCase() : "?");
        peerTitleLabel.setText("@" + user);
        peerStatusLabel.setText("Direct Encrypted Session");

        copyRoomCodeHeaderBtn.setVisible(false);
        copyRoomCodeHeaderBtn.setManaged(false);
        leaveRoomHeaderBtn.setVisible(false);
        leaveRoomHeaderBtn.setManaged(false);
        historyButton.setVisible(true);
        historyButton.setManaged(true);
        presenceButton.setVisible(true);
        presenceButton.setManaged(true);

        messageField.setDisable(false);
        sendButton.setDisable(false);
        messageField.setPromptText("Message @" + user + "... (Press Enter to send)");
        messageField.requestFocus();

        conversationPane.showDirectChatMode();
    }

    private void createRoom(String username) {
        if (!ensureConnected()) {
            roomLobbyPane.showError("Could not reach 1VN server. Ensure server is running.");
            return;
        }
        currentUsername = username;
        roomAdmin = username;
        roomMembers.clear();
        roomMembers.add(username);
        statusBar.setStatus("Creating private room for @" + username + "...");
        chatService.createRoom(username);
    }

    private void joinRoom(String code, String username) {
        if (!ensureConnected()) {
            roomLobbyPane.showError("Could not reach 1VN server. Ensure server is running.");
            return;
        }
        currentUsername = username;
        roomAdmin = null;
        roomMembers.clear();
        roomMembers.add(username);
        statusBar.setStatus("Joining Room #" + code + "...");
        chatService.joinRoom(code, username);
    }

    private void leaveCurrentRoom() {
        if (activeRoomCode != null) {
            chatService.leaveRoom();
            activeRoomCode = null;
            roomMembers.clear();
            showLobby();
        }
    }

    private void copyActiveRoomCode() {
        if (activeRoomCode == null) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(activeRoomCode);
        clipboard.setContent(content);

        copyRoomCodeHeaderBtn.setText("✓ Copied!");
        copyRoomCodeHeaderBtn.setGraphic(VectorIcons.icon(VectorIcons.CHECK, 13, "#ffffff"));
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(e -> {
            copyRoomCodeHeaderBtn.setText("Copy Code");
            copyRoomCodeHeaderBtn.setGraphic(VectorIcons.icon(VectorIcons.COPY, 13, "#f8fafc"));
        });
        pt.play();
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
        activeRoomCode = null;
        knownUsers.clear();
        roomMembers.clear();
        conversationPane.clear();
        messagePane.clear();
        messagePane.showEmptyState();
        showLobby();
        authPane.setAuthenticated(false);
        statusBar.setStatus("Signed out. Ready to connect.");
    }

    private boolean ensureConnected() {
        if (connected && chatService.isConnected()) {
            return true;
        }

        String host = authPane.getServerHost();
        int port = authPane.getServerPort();

        try {
            chatService.connect(host, port, this::onServerMessage);
            connected = true;
            statusBar.setStatus("Connected to 1VN Server (" + host + ":" + port + ")");
            authPane.clearError();
            roomLobbyPane.clearMessages();
            return true;
        } catch (IOException e) {
            connected = false;
            statusBar.setStatus("Could not connect to " + host + ":" + port);
            authPane.showError("Could not connect to " + host + ":" + port + ". Make sure server is reachable.");
            roomLobbyPane.showError("Could not reach 1VN server (" + host + ":" + port + ").");
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
        // --- Room Protocol Events ---
        if (message.startsWith("ROOM_CREATED ")) {
            String[] parts = message.split(" ", 3);
            if (parts.length >= 3) {
                String code = parts[1];
                String user = parts[2];
                roomAdmin = user;
                if (!roomMembers.contains(user)) roomMembers.add(user);
                showActiveRoomView(code, user);
            }

        } else if (message.startsWith("ROOM_JOINED ")) {
            String[] parts = message.split(" ", 3);
            if (parts.length >= 3) {
                String code = parts[1];
                String user = parts[2];
                if (!roomMembers.contains(user)) roomMembers.add(user);
                showActiveRoomView(code, user);
            }

        } else if (message.startsWith("ROOM_MEMBERS ")) {
            String list = message.substring("ROOM_MEMBERS ".length()).trim();
            roomMembers.clear();
            if (!list.isEmpty()) {
                String[] names = list.split(",");
                roomMembers.addAll(Arrays.asList(names));
            }
            if (activeRoomCode != null) {
                peerStatusLabel.setText(roomMembers.size() + " connected members • Instant Room");
                conversationPane.updateRoomMembers(roomMembers);
            }

        } else if (message.startsWith("ROOM_MEMBER_JOINED ")) {
            String newMember = message.substring("ROOM_MEMBER_JOINED ".length()).trim();
            if (!roomMembers.contains(newMember)) {
                roomMembers.add(newMember);
            }
            messagePane.addSystemMessage("👋 @" + newMember + " joined the room.");
            peerStatusLabel.setText(roomMembers.size() + " connected members • Instant Room");
            conversationPane.updateRoomMembers(roomMembers);

        } else if (message.startsWith("ROOM_MEMBER_LEFT ")) {
            String leavingMember = message.substring("ROOM_MEMBER_LEFT ".length()).trim();
            roomMembers.remove(leavingMember);
            messagePane.addSystemMessage("🚪 @" + leavingMember + " left the room.");
            peerStatusLabel.setText(roomMembers.size() + " connected members • Instant Room");
            conversationPane.updateRoomMembers(roomMembers);

        } else if (message.startsWith("ROOM_MSG ")) {
            String text = message.substring("ROOM_MSG ".length()).trim();
            int sep = text.indexOf(": ");
            if (sep > 0) {
                String sender = text.substring(0, sep);
                String body = text.substring(sep + 2);
                messagePane.addMessage(sender, body);
            } else {
                messagePane.addSystemMessage(text);
            }

        } else if (message.equals("ROOM_LEFT")) {
            activeRoomCode = null;
            roomMembers.clear();
            showLobby();
            statusBar.setStatus("Left room.");

        } else if (message.startsWith("ROOM_ERROR:")) {
            String err = message.substring("ROOM_ERROR:".length()).trim();
            roomLobbyPane.showError(err);
            statusBar.setStatus("Room Error: " + err);

        // --- Account / Direct Chat Events ---
        } else if (message.equals("REGISTER OK") || message.equals("LOGIN OK")) {
            authenticated = true;
            currentUsername = authPane.getUsername();
            statusBar.setStatus("Authenticated as @" + currentUsername);
            authPane.setAuthenticated(true);
            conversationPane.showDirectChatMode();

            messagePane.setCurrentUser(currentUsername);
            messagePane.addSystemMessage("Welcome, @" + currentUsername + "! End-to-end encrypted session active.");
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

        showDirectChatView(user);
        messagePane.clear();
        chatService.requestHistory(user);
        chatService.requestPresence(user);
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
        String text = messageField.getText();
        if (text == null || text.trim().isBlank()) {
            return;
        }
        text = text.trim();

        if (activeRoomCode != null) {
            chatService.sendRoomMessage(text);
            messageField.clear();
            return;
        }

        if (!authenticated) {
            return;
        }

        if (selectedUser == null) {
            statusBar.setStatus("Select a conversation or join a room first.");
            return;
        }

        chatService.sendMessage("TO " + selectedUser + " " + text);
        messageField.clear();
    }
}
