package com.onevn.server;


import com.onevn.server.repository.MessageRepository;
import com.onevn.server.repository.MySqlMessageRepository;
import com.onevn.server.repository.MySqlUserRepository;
import com.onevn.server.repository.UserRepository;
import com.onevn.server.service.MessageService;
import com.onevn.server.service.UserService;

public class Main {

    public static void main(String[] args) {

        com.onevn.server.security.MessageEncryption encryption =
                new com.onevn.server.security.MessageEncryption();

        UserRepository userRepository =
                com.onevn.server.repository.RepositoryFactory.createUserRepository();

        UserService userService =
                new UserService(userRepository);

        MessageRepository messageRepository =
                com.onevn.server.repository.RepositoryFactory.createMessageRepository(encryption);

        MessageService messageService =
                new MessageService(messageRepository);

        ChatServer server =
                new ChatServer(userService, messageService);

        server.start();
    }
}
