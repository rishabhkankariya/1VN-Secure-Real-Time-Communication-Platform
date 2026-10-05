package com.onevn.server.service;

import com.onevn.server.model.ChatMessage;
import com.onevn.server.repository.MessageRepository;

import java.util.List;

public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public boolean saveMessage(long senderId, long receiverId, String content) {

        if (senderId <= 0 || receiverId <= 0
                || content == null || content.isBlank()) {
            return false;
        }

        ChatMessage message =
                new ChatMessage(senderId, receiverId, content);

        return messageRepository.save(message);
    }

    public List<ChatMessage> findConversation(long userA, long userB) {

        if (userA <= 0 || userB <= 0) {
            return List.of();
        }

        return messageRepository.findConversation(userA, userB);
    }
}
