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

    public void createRoom(String username) {
        chatClient.createRoom(username);
    }

    public void joinRoom(String code, String username) {
        chatClient.joinRoom(code, username);
    }

    public void sendRoomMessage(String text) {
        chatClient.sendRoomMessage(text);
    }

    public void requestRoomMembers() {
        chatClient.requestRoomMembers();
    }

    public void leaveRoom() {
        chatClient.leaveRoom();
    }

    public boolean isConnected() {
        return chatClient.isConnected();
    }

    public void disconnect() throws IOException {
        chatClient.disconnect();
    }
}


