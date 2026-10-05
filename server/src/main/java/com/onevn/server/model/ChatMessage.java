package com.onevn.server.model;

public class ChatMessage {

    private final long senderId;
    private final long receiverId;
    private final String content;
    private final String createdAt;

    public ChatMessage(long senderId, long receiverId, String content) {
        this(senderId, receiverId, content, null);
    }

    public ChatMessage(long senderId, long receiverId,
                       String content, String createdAt) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
