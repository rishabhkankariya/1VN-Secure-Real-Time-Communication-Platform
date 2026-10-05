package com.onevn.server.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class MessageEncryption {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final String VERSION = "v1";

    private final SecretKey secretKey;

    public MessageEncryption() {
        this.secretKey = loadKey();
    }

    private SecretKey loadKey() {
        String keyEnv = System.getenv("MESSAGE_ENCRYPTION_KEY");
        if (keyEnv == null || keyEnv.isBlank()) {
            keyEnv = System.getProperty("MESSAGE_ENCRYPTION_KEY", "");
        }
        if (keyEnv == null || keyEnv.isBlank()) {
            keyEnv = loadOrCreateDeviceKey();
        }
        byte[] keyBytes = normalizeKey(keyEnv);
        if (keyBytes.length != KEY_LENGTH) {
            byte[] padded = new byte[KEY_LENGTH];
            System.arraycopy(keyBytes, 0, padded, 0,
                    Math.min(keyBytes.length, KEY_LENGTH));
            keyBytes = padded;
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private String loadOrCreateDeviceKey() {
        try {
            String dbPath = com.onevn.server.database.SqliteConnection.getDatabasePath();
            java.io.File keyFile = new java.io.File(new java.io.File(dbPath).getParentFile(), ".master_key");
            if (keyFile.exists()) {
                return java.nio.file.Files.readString(keyFile.toPath(), StandardCharsets.UTF_8).trim();
            }
            byte[] randomKey = new byte[KEY_LENGTH];
            new SecureRandom().nextBytes(randomKey);
            String encoded = Base64.getEncoder().encodeToString(randomKey);
            java.nio.file.Files.writeString(keyFile.toPath(), encoded, StandardCharsets.UTF_8);
            return encoded;
        } catch (Exception e) {
            return "1VN-DefaultSecureAES256GCMKey2026";
        }
    }

    private byte[] normalizeKey(String key) {
        String trimmed = key.trim();
        if (trimmed.matches("^[A-Za-z0-9+/]+={0,2}$") && trimmed.length() >= 16) {
            try {
                byte[] decoded = Base64.getDecoder().decode(trimmed);
                if (decoded.length == KEY_LENGTH) {
                    return decoded;
                }
                if (decoded.length > 0) {
                    return decoded;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return trimmed.getBytes(StandardCharsets.UTF_8);
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            byte[] ciphertext = cipher.doFinal(
                    plaintext.getBytes(StandardCharsets.UTF_8)
            );
            String ivB64 = Base64.getEncoder().encodeToString(iv);
            String ctB64 = Base64.getEncoder().encodeToString(ciphertext);
            return VERSION + ":" + ivB64 + ":" + ctB64;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) {
            return encrypted;
        }
        if (!encrypted.startsWith(VERSION + ":")) {
            return encrypted;
        }
        try {
            String[] parts = encrypted.split(":", 3);
            if (parts.length != 3) {
                return encrypted;
            }
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
