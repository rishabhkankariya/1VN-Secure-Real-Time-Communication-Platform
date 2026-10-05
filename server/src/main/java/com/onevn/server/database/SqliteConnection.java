package com.onevn.server.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteConnection {

    private static String customDbPath = null;
    private static volatile boolean initialized = false;

    public static synchronized void setDatabasePath(String path) {
        customDbPath = path;
        initialized = false;
    }

    public static synchronized String getDatabasePath() {
        if (customDbPath != null) {
            return customDbPath;
        }

        // Try %LOCALAPPDATA%\1VN\data\1vn.db, then ~/.1vn/data/1vn.db, then ./data/1vn.db
        String localAppData = System.getenv("LOCALAPPDATA");
        File dataDir;
        if (localAppData != null && !localAppData.isBlank()) {
            dataDir = new File(localAppData, "1VN" + File.separator + "data");
        } else {
            String userHome = System.getProperty("user.home", ".");
            dataDir = new File(userHome, ".1vn" + File.separator + "data");
        }

        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        return new File(dataDir, "1vn.db").getAbsolutePath();
    }

    public static Connection getConnection() throws SQLException {
        String dbPath = getDatabasePath();
        File dbFile = new File(dbPath);
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        ensureTables(conn);
        return conn;
    }

    private static void ensureTables(Connection conn) {
        if (initialized) {
            return;
        }

        synchronized (SqliteConnection.class) {
            if (initialized) {
                return;
            }

            try (Statement stmt = conn.createStatement()) {
                // Create users table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        email TEXT,
                        password_hash TEXT NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                // Create messages table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        sender_id INTEGER NOT NULL,
                        receiver_id INTEGER NOT NULL,
                        content TEXT NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                // Index on sender/receiver for fast query performance
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_pair ON messages(sender_id, receiver_id);");

                initialized = true;
                System.out.println("[SQLite] Database schema verified at: " + getDatabasePath());
            } catch (SQLException e) {
                System.err.println("[SQLite] Error initializing tables: " + e.getMessage());
            }
        }
    }
}
