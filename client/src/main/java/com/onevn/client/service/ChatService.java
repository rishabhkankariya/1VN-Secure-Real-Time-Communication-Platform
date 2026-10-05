package com.onevn.client.service;

import com.onevn.client.network.ChatClient;
import com.onevn.client.network.MessageListener;

import java.io.IOException;

public class ChatService {

    private final ChatClient chatClient = new ChatClient();

    public void connect(MessageListener listener) throws IOException {
        connect("127.0.0.1", 5000, listener);
    }

    public void connect(String host, int port, MessageListener listener) throws IOException {
        chatClient.connect(host, port, listener);
    }

    public void register(String username, String email, String password) {

        chatClient.register(username, email, password);
    }

    public void login(String username, String password) {

        chatClient.login(username, password);
    }

    public void requestHistory(String username) {

        chatClient.requestHistory(username);
    }

    public void requestPresence(String username) {

        chatClient.requestPresence(username);
    }

    public void searchUsers(String query) {

        chatClient.searchUsers(query);
    }

    public void sendMessage(String text) {

        if (text == null || text.isBlank()) {
            return;
        }

        chatClient.sendMessage(text);
    }
}

