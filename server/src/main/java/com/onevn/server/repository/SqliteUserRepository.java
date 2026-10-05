package com.onevn.server.repository;

import com.onevn.server.database.SqliteConnection;
import com.onevn.server.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteUserRepository implements UserRepository {

    @Override
    public AddResult add(User user) {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";

        try (Connection connection = SqliteConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.executeUpdate();

            return AddResult.SUCCESS;

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toUpperCase() : "";
            if (msg.contains("UNIQUE") || msg.contains("CONSTRAINT")) {
                return AddResult.DUPLICATE;
            }
            e.printStackTrace();
            return AddResult.ERROR;
        }
    }

    @Override
    public boolean exists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection connection = SqliteConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public User findByUsername(String username) {
        String sql = """
                SELECT id, username, email, password_hash
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = SqliteConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getLong("id"),
                            resultSet.getString("username"),
                            resultSet.getString("email"),
                            resultSet.getString("password_hash")
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void remove(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection connection = SqliteConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
