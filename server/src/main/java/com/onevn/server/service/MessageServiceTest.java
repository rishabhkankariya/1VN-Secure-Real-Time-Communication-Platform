package com.onevn.server.service;

import com.onevn.server.model.User;
import com.onevn.server.repository.MySqlMessageRepository;
import com.onevn.server.repository.MySqlUserRepository;

public class MessageServiceTest {

    public static void main(String[] args) {

        UserService userService =
                new UserService(new MySqlUserRepository());

        MessageService messageService =
                new MessageService(new MySqlMessageRepository());

        userService.register(
                "alice", "alice@example.com", "secret123");
        userService.register(
                "bob", "bob@example.com", "secret123");

        User alice = userService.findUser("alice");
        User bob = userService.findUser("bob");

        System.out.println("aliceId=" + alice.getId());
        System.out.println("bobId=" + bob.getId());

        boolean saved = messageService.saveMessage(
                alice.getId(), bob.getId(), "Hello Bob");

        System.out.println("saved=" + saved);

        System.out.println("invalid_sender=" +
                messageService.saveMessage(0, bob.getId(), "x"));

        System.out.println("invalid_receiver=" +
                messageService.saveMessage(alice.getId(), 0, "x"));

        System.out.println("blank_content=" +
                messageService.saveMessage(
                        alice.getId(), bob.getId(), "   "));

        System.out.println("no_such_user=" +
                messageService.saveMessage(
                        alice.getId(), 99999, "orphan"));
    }
}
