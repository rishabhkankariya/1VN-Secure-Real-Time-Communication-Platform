package com.onevn.client.ui;

import com.onevn.client.service.ChatService;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChatView {

    private final AuthPane authPane = new AuthPane();
    private final ConversationPane conversationPane =
            new ConversationPane();
    private final MessagePane messagePane = new MessagePane();
    private final StatusBar statusBar = new StatusBar();
    private final TextField messageField = new TextField();
    private final Button sendButton = new Button("Send");

    private final TextField conversationField = new TextField();
    private final Button historyButton = new Button("Load History");
    private final Button presenceButton = new Button("Check Presence");

    private final ChatService chatService = new ChatService();

    private final Set<String> knownUsers = new LinkedHashSet<>();

    private boolean connected = false;
    private boolean authenticated = false;
    private boolean loadingHistory = false;
    private String selectedUser = null;

    public BorderPane createView() {

        messageField.setPromptText("Type a message...");

        messageField.setDisable(true);
        sendButton.setDisable(true);
        historyButton.setDisable(true);
        presenceButton.setDisable(true);

        conversationField.setPromptText("bob");
        conversationField.setDisable(true);

        authPane.setOnRegister(this::register);
        authPane.setOnLogin(this::login);

        conversationPane.setOnConversationSelected(
                this::onConversationSelected
        );
        conversationPane.setOnNewConversationRequested(
                this::onNewConversationRequested
        );

        sendButton.setOnAction(event -> sendMessage());
        historyButton.setOnAction(event -> loadHistory());
        presenceButton.setOnAction(event -> checkPresence());

        HBox inputBox = new HBox(10, messageField, sendButton);
        HBox.setHgrow(messageField, Priority.ALWAYS);
        inputBox.getStyleClass().add("message-input-row");

        HBox conversationRow = new HBox(10,
                new Label("Conversation with:"), conversationField,
                historyButton, presenceButton);
        HBox.setHgrow(conversationField, Priority.ALWAYS);
        conversationRow.getStyleClass().add("conversation-row");

        VBox centerPane = new VBox(
                messagePane.getRoot(),
                conversationRow,
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
            authPane.showError("Fill username, email and password.");
            statusBar.setStatus(
                    "Fill username, email and password."
            );
            return;
        }

        if (username.contains(" ") || email.contains(" ")) {
            authPane.showError("Username and email cannot contain spaces.");
            statusBar.setStatus(
                    "Username and email cannot contain spaces."
            );
            return;
        }

        if (!ensureConnected()) {
            return;
        }

        statusBar.setStatus("Registering...");

        chatService.register(username, email, password);
    }

    private void login() {

        String username = authPane.getUsername();
        String password = authPane.getPassword();

        if (username.isBlank() || password.isBlank()) {
            authPane.showError("Fill username and password.");
            statusBar.setStatus("Fill username and password.");
            return;
        }

        if (username.contains(" ")) {
            authPane.showError("Username cannot contain spaces.");
            statusBar.setStatus(
                    "Username cannot contain spaces."
            );
            return;
        }

        if (!ensureConnected()) {
            return;
        }

        statusBar.setStatus("Logging in...");

        chatService.login(username, password);
    }

    private boolean ensureConnected() {

        if (connected) {
            return true;
        }

        try {

            chatService.connect(this::onServerMessage);

            connected = true;
            statusBar.setStatus("Connected to server.");
            authPane.clearError();

            return true;

        } catch (IOException e) {

            statusBar.setStatus("Could not connect to server.");
            authPane.showError("Could not connect to server.");

            return false;
        }
    }

    private void onServerMessage(String message) {

        Platform.runLater(() -> handleServerMessage(message));
    }

    private void handleServerMessage(String message) {

        if (message.equals("REGISTER OK")
                || message.equals("LOGIN OK")) {

            authenticated = true;

            statusBar.setStatus(
                    "Authenticated as " + authPane.getUsername()
            );

            authPane.setAuthenticated(true);

            if (selectedUser == null) {
                messageField.setDisable(true);
                sendButton.setDisable(true);
            } else {
                messageField.setDisable(false);
                sendButton.setDisable(false);
                messageField.requestFocus();
            }

            conversationField.setDisable(false);
            historyButton.setDisable(false);
            presenceButton.setDisable(false);

            messagePane.setCurrentUser(authPane.getUsername());
            messagePane.addSystemMessage("Authenticated. Say hello!");

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
                if (selectedUser != null && msgSender != null
                        && !msgSender.equals(selectedUser)
                        && !msgSender.equals(authPane.getUsername())) {
                }
                messagePane.addMessage(msgSender, msgText);
            } else {
                messagePane.addSystemMessage(text);
            }

        } else if (message.startsWith("PRESENCE ")) {

            String[] parts = message.split(" ");

            if (parts.length == 3) {

                addKnownUser(parts[1]);
                conversationPane.setOnline(
                        parts[1], parts[2].equals("ONLINE")
                );
                messagePane.addSystemMessage(
                        "[Presence] " + parts[1] + " is " + parts[2]
                );
            } else {
                messagePane.addSystemMessage(message);
            }

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
        conversationField.setText(user);

        messageField.setDisable(false);
        sendButton.setDisable(false);

        messagePane.clear();
        chatService.requestHistory(user);
        chatService.requestPresence(user);
    }

    private void onNewConversationRequested(String user) {
        if (!authenticated || user == null) {
            return;
        }
        if (user.equals(authPane.getUsername())) {
            conversationPane.showNewChatError("Cannot chat with yourself.");
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

            conversationPane.setUsers(
                    new ArrayList<>(knownUsers)
            );

            if (user.equals(selectedUser)) {
                conversationPane.setSelectedUser(user);
            }
        }
    }

    private void loadHistory() {

        if (!authenticated) {
            return;
        }

        String target = readTarget();

        if (target == null) {
            return;
        }

        addKnownUser(target);

        chatService.requestHistory(target);
    }

    private void checkPresence() {

        if (!authenticated) {
            return;
        }

        String target = readTarget();

        if (target == null) {
            return;
        }

        addKnownUser(target);

        chatService.requestPresence(target);
    }

    private String readTarget() {

        String target = conversationField.getText().trim();

        if (target.isBlank()) {
            statusBar.setStatus("Enter a username first.");
            return null;
        }

        if (target.contains(" ")) {
            statusBar.setStatus("Username cannot contain spaces.");
            return null;
        }

        return target;
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
        if (text == null) {
            text = "";
        }
        if (text.trim().isBlank()) {
            statusBar.setStatus("Message cannot be empty.");
            return;
        }

        chatService.sendMessage("TO " + target + " " + text.trim());
        messageField.clear();
    }
}
