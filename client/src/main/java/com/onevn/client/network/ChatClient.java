package com.onevn.client.network;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public void connect(String host, int port, MessageListener listener)
            throws IOException {

        socket = new Socket(host, port);

        input = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        output = new PrintWriter(
                socket.getOutputStream(), true
        );

        startListening(listener);
    }

    public void register(String username, String email, String password) {

        if (output != null) {
            output.println(
                    "REGISTER " + username + " " + email + " " + password
            );
        }
    }

    public void login(String username, String password) {

        if (output != null) {
            output.println("LOGIN " + username + " " + password);
        }
    }

    public void requestHistory(String username) {

        if (output != null) {
            output.println("HISTORY " + username);
        }
    }

    public void requestPresence(String username) {

        if (output != null) {
            output.println("PRESENCE " + username);
        }
    }

    public void sendMessage(String message) {

        if (output != null) {
            output.println(message);
        }
    }

    public void searchUsers(String query) {
        if (output != null) {
            output.println("SEARCH " + (query == null ? "" : query.trim()));
        }
    }


    private void startListening(MessageListener listener) {

        Thread listenerThread = new Thread(() -> {

            try {

                String message;

                while ((message = input.readLine()) != null) {
                    listener.onMessage(message);
                }

            } catch (IOException e) {

                System.out.println("Connection closed.");
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void disconnect() throws IOException {

        if (socket != null) {
            socket.close();
        }
    }
}
