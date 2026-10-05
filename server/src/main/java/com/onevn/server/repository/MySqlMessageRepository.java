package com.onevn.server.repository;

import com.onevn.server.database.DatabaseConnection;
import com.onevn.server.model.ChatMessage;
import com.onevn.server.security.MessageEncryption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlMessageRepository implements MessageRepository {

    private final MessageEncryption encryption;

    public MySqlMessageRepository() {
        this.encryption = new MessageEncryption();
    }

    public MySqlMessageRepository(MessageEncryption encryption) {
        this.encryption = encryption;
    }

    @Override
    public boolean save(ChatMessage message) {

        String sql = """
            INSERT INTO messages (sender_id, receiver_id, content)
            VALUES (?, ?, ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, message.getSenderId());
            statement.setLong(2, message.getReceiverId());
            String content = message.getContent();
            try {
                content = encryption.encrypt(content);
            } catch (RuntimeException ignored) {
            }
            statement.setString(3, content);

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<ChatMessage> findConversation(long userA, long userB) {

        String sql = """
            SELECT sender_id, receiver_id, content, created_at
            FROM messages
            WHERE (sender_id = ? AND receiver_id = ?)
               OR (sender_id = ? AND receiver_id = ?)
            ORDER BY created_at ASC, id ASC
            """;

        List<ChatMessage> messages = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userA);
            statement.setLong(2, userB);
            statement.setLong(3, userB);
            statement.setLong(4, userA);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String content = resultSet.getString("content");
                    try {
                        content = encryption.decrypt(content);
                    } catch (RuntimeException ignored) {
                    }
                    messages.add(new ChatMessage(
                            resultSet.getLong("sender_id"),
                            resultSet.getLong("receiver_id"),
                            content,
                            resultSet.getString("created_at")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }
}
