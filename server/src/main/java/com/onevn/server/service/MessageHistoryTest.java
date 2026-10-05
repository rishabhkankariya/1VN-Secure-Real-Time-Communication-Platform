package com.onevn.server.service;

import com.onevn.server.model.ChatMessage;
import com.onevn.server.model.User;
import com.onevn.server.repository.MySqlMessageRepository;
import com.onevn.server.repository.MySqlUserRepository;

import java.util.List;

public class MessageHistoryTest {

    public static void main(String[] args) {

        UserService userService =
                new UserService(new MySqlUserRepository());

        MessageService messageService =
                new MessageService(new MySqlMessageRepository());

        userService.register(
                "alice", "alice@example.com", "secret123");
        userService.register(
                "bob", "bob@example.com", "secret123");
        userService.register(
                "carol", "carol@example.com", "secret123");

        User alice = userService.findUser("alice");
        User bob = userService.findUser("bob");
        User carol = userService.findUser("carol");

        messageService.saveMessage(
                alice.getId(), bob.getId(), "first");
        messageService.saveMessage(
                bob.getId(), alice.getId(), "second");
        messageService.saveMessage(
                alice.getId(), bob.getId(), "third");
        messageService.saveMessage(
                alice.getId(), carol.getId(), "secret to carol");

        List<ChatMessage> conversation =
                messageService.findConversation(
                        alice.getId(), bob.getId());

        System.out.println("conversation_count="
                + conversation.size());

        for (int i = 0; i < conversation.size(); i++) {
            ChatMessage message = conversation.get(i);
            System.out.println("  " + (i + 1) + ") sender="
                    + message.getSenderId()
                    + " receiver=" + message.getReceiverId()
                    + " content=" + message.getContent()
                    + " createdAt=" + message.getCreatedAt());
        }

        boolean orderOk =
                conversation.size() == 3
                        && "first".equals(
                                conversation.get(0).getContent())
                        && "second".equals(
                                conversation.get(1).getContent())
                        && "third".equals(
                                conversation.get(2).getContent());

        System.out.println("order_ok=" + orderOk);

        boolean isolationOk = conversation.stream().noneMatch(
                message -> message.getContent()
                        .contains("secret to carol"));

        System.out.println("isolation_ok=" + isolationOk);

        boolean timestampsPresent = conversation.stream().allMatch(
                message -> message.getCreatedAt() != null
                        && !message.getCreatedAt().isBlank());

        System.out.println("created_at_present="
                + timestampsPresent);

        List<ChatMessage> reverse =
                messageService.findConversation(
                        bob.getId(), alice.getId());

        System.out.println("reverse_count=" + reverse.size());
        System.out.println("reverse_matches=" +
                (reverse.size() == 3
                        && reverse.get(0).getContent()
                                .equals("first")));

        System.out.println("invalid_ids=" +
                messageService.findConversation(0, bob.getId()));
        System.out.println("empty_pair=" +
                messageService.findConversation(
                        alice.getId(), alice.getId()));
    }
}
