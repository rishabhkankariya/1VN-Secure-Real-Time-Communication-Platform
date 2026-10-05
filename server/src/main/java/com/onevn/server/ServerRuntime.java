package com.onevn.server;

import com.onevn.server.repository.MessageRepository;
import com.onevn.server.repository.UserRepository;
import com.onevn.server.service.MessageService;
import com.onevn.server.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerRuntime {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final int port;
    private Thread serverThread;
    private volatile boolean running = false;
    private ChatServer chatServer;

    public ServerRuntime(UserRepository userRepository, MessageRepository messageRepository) {
        this(userRepository, messageRepository, 5000);
    }

    public ServerRuntime(UserRepository userRepository, MessageRepository messageRepository, int port) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.port = port;
    }

    public synchronized void startAsync() {
        if (running) {
            return;
        }

        UserService userService = new UserService(userRepository);
        MessageService messageService = new MessageService(messageRepository);
        this.chatServer = new ChatServer(userService, messageService);

        this.running = true;
        this.serverThread = new Thread(() -> {
            try {
                System.out.println("[EmbeddedServer] Starting 1VN Server on port " + port + "...");
                chatServer.start();
            } catch (Exception e) {
                System.err.println("[EmbeddedServer] Error: " + e.getMessage());
            } finally {
                running = false;
            }
        }, "1VN-EmbeddedServer");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public static boolean isPortAvailable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 200);
            return false; // Connection succeeded, port is in use
        } catch (IOException e) {
            return true; // Connection failed, port is available
        }
    }

    public boolean isRunning() {
        return running;
    }
}
