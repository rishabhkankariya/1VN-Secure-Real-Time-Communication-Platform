package com.onevn.server.repository;

import com.onevn.server.model.ChatMessage;

import java.util.List;

public interface MessageRepository {

    boolean save(ChatMessage message);

    List<ChatMessage> findConversation(long userA, long userB);
}
