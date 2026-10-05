package com.onevn.server.repository;

import com.onevn.server.security.MessageEncryption;

public class RepositoryFactory {

    public static UserRepository createUserRepository() {
        String storage = getStorageType();
        if ("mysql".equalsIgnoreCase(storage)) {
            System.out.println("[Storage] Initializing MySQL User Repository...");
            return new MySqlUserRepository();
        } else {
            System.out.println("[Storage] Initializing embedded SQLite User Repository...");
            return new SqliteUserRepository();
        }
    }

    public static MessageRepository createMessageRepository(MessageEncryption encryption) {
        String storage = getStorageType();
        if ("mysql".equalsIgnoreCase(storage)) {
            System.out.println("[Storage] Initializing MySQL Message Repository (Encrypted at rest)...");
            return new MySqlMessageRepository(encryption);
        } else {
            System.out.println("[Storage] Initializing embedded SQLite Message Repository (Encrypted at rest)...");
            return new SqliteMessageRepository(encryption);
        }
    }

    public static String getStorageType() {
        String prop = System.getProperty("1vn.storage");
        if (prop != null && !prop.isBlank()) {
            return prop.trim().toLowerCase();
        }
        String env = System.getenv("DB_TYPE");
        if (env != null && !env.isBlank()) {
            return env.trim().toLowerCase();
        }
        // Default to sqlite for seamless zero-dependency deployment
        return "sqlite";
    }
}
