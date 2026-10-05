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


    public ChatServer(UserService userService, MessageService messageService) {

        this.userService = userService;
        this.messageService = messageService;
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
}
