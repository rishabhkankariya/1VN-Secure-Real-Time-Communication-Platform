package com.onevn.server;


import com.onevn.server.model.ChatMessage;
import com.onevn.server.model.User;
import com.onevn.server.repository.AddResult;
import com.onevn.server.service.LoginResult;
import com.onevn.server.service.MessageService;

import java.io.*;
import java.net.Socket;
import java.util.List;


public class ClientHandler implements Runnable {


    private final Socket socket;
    private final ChatServer server;
    private final MessageService messageService;


    private BufferedReader input;
    private PrintWriter output;


    private long userId;
    private String username;


    public ClientHandler(Socket socket, ChatServer server,
                         MessageService messageService) {
        this.socket = socket;
        this.server = server;
        this.messageService = messageService;
    }


    @Override
    public void run() {


        try {
            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );


            output = new PrintWriter(
                    socket.getOutputStream(), true
            );


            boolean authenticated = false;


            String firstLine = input.readLine();


            if (firstLine == null) {
                return;
            }


            if (firstLine.startsWith("REGISTER ")) {


                String[] parts = firstLine.split(" ", 4);


                if (parts.length != 4) {
                    output.println("REGISTER ERROR: invalid format.");
                    return;
                }


                AddResult result =
                        server.register(parts[1], parts[2], parts[3]);


                if (result == AddResult.SUCCESS) {


                    User user = server.findUser(parts[1]);


                    if (user == null) {
                        output.println("REGISTER ERROR: server error");
                        return;
                    }


                    userId = user.getId();
                    username = user.getUsername();

                    output.println("REGISTER OK");

                    System.out.println(
                            username + " (id=" + userId
                            + ") registered: "
                            + socket.getInetAddress()
                    );

                    authenticated = true;

                } else if (result == AddResult.DUPLICATE) {

                    output.println("REGISTER ERROR: username taken");
                    return;

                } else {

                    output.println("REGISTER ERROR: server error");
                    return;
                }


            } else if (firstLine.startsWith("LOGIN ")) {


                String[] parts = firstLine.split(" ", 3);


                if (parts.length != 3) {
                    output.println("LOGIN ERROR: invalid format.");
                    return;
                }


                LoginResult result =
                        server.login(parts[1], parts[2]);


                if (result == LoginResult.SUCCESS) {


                    User user = server.findUser(parts[1]);


                    if (user == null) {
                        output.println("LOGIN ERROR: server error");
                        return;
                    }


                    userId = user.getId();
                    username = user.getUsername();

                    output.println("LOGIN OK");

                    System.out.println(
                            username + " (id=" + userId
                            + ") logged in: "
                            + socket.getInetAddress()
                    );

                    authenticated = true;

                } else if (result == LoginResult.INVALID_CREDENTIALS) {

                    output.println(
                            "LOGIN ERROR: invalid username or password"
                    );
                    return;

                } else {

                    output.println("LOGIN ERROR: server error");
                    return;
                }


            } else {


                output.println("ERROR: Please REGISTER or LOGIN.");
                return;
            }


            if (!authenticated) {
                return;
            }


            String message;


            while ((message = input.readLine()) != null) {


                if (message.equals("TO") || message.startsWith("TO ")) {


                    handlePrivateMessage(message);

                } else if (message.equals("PRESENCE")
                        || message.startsWith("PRESENCE ")) {


                    handlePresence(message);

                } else if (message.equals("HISTORY")
                        || message.startsWith("HISTORY ")) {


                    handleHistory(message);

                } else if (message.equals("SEARCH")
                        || message.startsWith("SEARCH ")
                        || message.equals("USERS")) {


                    handleSearch(message.startsWith("SEARCH ") ? message.substring("SEARCH ".length()).trim() : "");

                } else if (message.equals("BROADCAST")
                        || message.startsWith("BROADCAST ")) {


                    handleBroadcast(stripCommand(message, "BROADCAST"));

                } else {


                    handleBroadcast(message);
                }
            }

        } catch (IOException e) {


            System.out.println("Client disconnected.");


        } finally {


            if (username != null) {
                System.out.println(
                        username + " disconnected."
                );
            }


            server.removeClient(this);


            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }


    private void handlePrivateMessage(String line) {


        String[] parts = line.split(" ", 3);


        if (parts.length < 3) {
            output.println("ERROR: Invalid private message format.");
            return;
        }


        String targetName = parts[1];
        String content = parts[2];


        if (targetName.equals(username)) {
            output.println("ERROR: You cannot message yourself.");
            return;
        }


        User target = server.findUser(targetName);


        if (target == null) {
            output.println("ERROR: User not found.");
            return;
        }


        ClientHandler targetClient =
                server.findConnectedClient(targetName);


        if (targetClient == null) {
            output.println("ERROR: User is offline.");
            return;
        }


        boolean saved = messageService.saveMessage(
                userId,
                target.getId(),
                content
        );


        if (!saved) {
            output.println("ERROR: Could not save message.");
            return;
        }


        System.out.println(
                username + " -> " + targetName + ": " + content
        );


        targetClient.sendMessage(username + ": " + content);
    }


    private void handleBroadcast(String content) {


        if (content.isBlank()) {
            output.println("ERROR: Invalid broadcast format.");
            return;
        }


        System.out.println(
                username + ": " + content
        );


        server.broadcast(
                username + ": " + content,
                this
        );
    }


    private void handlePresence(String line) {


        String[] parts = line.split(" ", 2);


        if (parts.length != 2 || parts[1].isBlank()) {
            output.println("ERROR: Invalid presence format.");
            return;
        }


        String targetUsername = parts[1];


        User target = server.findUser(targetUsername);


        if (target == null) {
            output.println("ERROR: User not found.");
            return;
        }


        if (server.isUserOnline(targetUsername)) {
            output.println("PRESENCE " + targetUsername + " ONLINE");
        } else {
            output.println("PRESENCE " + targetUsername + " OFFLINE");
        }
    }


    private void handleHistory(String line) {


        String[] parts = line.split(" ", 2);


        if (parts.length != 2 || parts[1].isBlank()) {
            output.println("ERROR: Invalid history format.");
            return;
        }


        String targetUsername = parts[1];


        User target = server.findUser(targetUsername);


        if (target == null) {
            output.println("ERROR: User not found.");
            return;
        }


        List<ChatMessage> history =
                messageService.findConversation(
                        userId,
                        target.getId()
                );


        output.println("HISTORY BEGIN");


        for (ChatMessage message : history) {


            String senderName =
                    message.getSenderId() == userId
                            ? username
                            : targetUsername;


            output.println(
                    "MSG " + senderName + ": " + message.getContent()
            );
        }


        output.println("HISTORY END");
    }


    private String stripCommand(String line, String command) {


        if (line.equals(command)) {
            return "";
        }

        return line.substring(command.length() + 1);
    }


    public long getUserId() {
        return userId;
    }


    public String getUsername() {
        return username;
    }


    public void sendMessage(String message) {


        output.println(message);
    }

    private void handleSearch(String query) {
        List<String> matches = server.searchUsers(query);
        StringBuilder sb = new StringBuilder("USERS_RESULT ");
        boolean first = true;
        for (String u : matches) {
            if (u.equalsIgnoreCase(username)) {
                continue;
            }
            if (!first) {
                sb.append(",");
            }
            sb.append(u).append(":").append(server.isUserOnline(u) ? "ONLINE" : "OFFLINE");
            first = false;
        }
        output.println(sb.toString());
    }
}

