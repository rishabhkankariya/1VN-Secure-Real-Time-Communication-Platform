package com.onevn.server;


import com.onevn.server.model.User;
import com.onevn.server.repository.AddResult;
import com.onevn.server.service.LoginResult;
import com.onevn.server.service.MessageService;
import com.onevn.server.service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ChatServer {


    private static final int PORT = 5000;


    private final Set<ClientHandler> clients =
            ConcurrentHashMap.newKeySet();


    private final UserService userService;


    private final MessageService messageService;

    // Room management (inspired by ip-chat)
    private final java.util.Map<String, Set<ClientHandler>> rooms = new ConcurrentHashMap<>();
    private final java.util.Map<String, String> roomAdmins = new ConcurrentHashMap<>();

    public ChatServer(UserService userService, MessageService messageService) {

        this.userService = userService;
        this.messageService = messageService;
    }

    public String createRoom(ClientHandler admin, String username) {
        String code = generateRoomCode();
        Set<ClientHandler> members = ConcurrentHashMap.newKeySet();
        members.add(admin);
        rooms.put(code, members);
        if (username != null) {
            roomAdmins.put(code, username);
        }
        System.out.println("[Room] Created room " + code + " by " + username);
        return code;
    }

    public boolean joinRoom(String code, ClientHandler client) {
        if (code == null) return false;
        String normalized = code.trim().toUpperCase();
        Set<ClientHandler> members = rooms.get(normalized);
        if (members == null) {
            return false;
        }
        members.add(client);
        System.out.println("[Room] " + client.getUsername() + " joined room " + normalized);
        return true;
    }

    public void leaveRoom(String code, ClientHandler client) {
        if (code == null) return;
        String normalized = code.trim().toUpperCase();
        Set<ClientHandler> members = rooms.get(normalized);
        if (members != null) {
            members.remove(client);
            if (members.isEmpty()) {
                rooms.remove(normalized);
                roomAdmins.remove(normalized);
                System.out.println("[Room] Room " + normalized + " closed (empty)");
            }
        }
    }

    public Set<ClientHandler> getRoomMembers(String code) {
        if (code == null) return java.util.Collections.emptySet();
        return rooms.getOrDefault(code.trim().toUpperCase(), java.util.Collections.emptySet());
    }

    public void broadcastToRoom(String code, String message, ClientHandler sender) {
        if (code == null) return;
        Set<ClientHandler> members = rooms.get(code.trim().toUpperCase());
        if (members != null) {
            for (ClientHandler client : members) {
                if (sender == null || client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 100; i++) {
            StringBuilder sb = new StringBuilder(6);
            for (int c = 0; c < 6; c++) {
                sb.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            String code = sb.toString();
            if (!rooms.containsKey(code)) {
                return code;
            }
        }
        return "VN" + (System.currentTimeMillis() % 10000);
    }



    public void start() {


        try (ServerSocket serverSocket = new ServerSocket(PORT)) {


            System.out.println("1VN Server started on port " + PORT);


            while (true) {


                Socket socket = serverSocket.accept();


                ClientHandler client =
                        new ClientHandler(socket, this, messageService);


                clients.add(client);


                new Thread(client).start();
            }

        } catch (IOException e) {


            System.out.println("Server error: " + e.getMessage());
        }
    }


    public void broadcast(String message, ClientHandler sender) {


        for (ClientHandler client : clients) {


            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }


    public void removeClient(ClientHandler client) {


        clients.remove(client);
    }


    public AddResult register(String username, String email, String password) {


        return userService.register(username, email, password);
    }


    public LoginResult login(String username, String password) {


        return userService.login(username, password);
    }


    public User findUser(String username) {


        return userService.findUser(username);
    }


    public ClientHandler findConnectedClient(String username) {


        for (ClientHandler client : clients) {


            if (username.equals(client.getUsername())) {
                return client;
            }
        }

        return null;
    }


    public boolean isUserOnline(String username) {


        return findConnectedClient(username) != null;
    }


    public void removeUser(String username) {


        userService.removeUser(username);
    }

    public java.util.List<String> searchUsers(String query) {
        return userService.searchUsers(query);
    }
}

