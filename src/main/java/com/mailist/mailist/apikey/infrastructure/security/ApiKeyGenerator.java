package com.mailist.mailist.apikey.infrastructure.security;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates and hashes API keys.
 */
@Component
public class ApiKeyGenerator {

    private static final String PREFIX_LIVE = "ml_live_";
    private static final String PREFIX_TEST = "ml_test_";
    private static final int KEY_LENGTH = 32; // bytes

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a new API key.
     *
     * @param isTest whether this is a test key
     * @return GeneratedKey containing the plain key and its hash
     */
    public GeneratedKey generateKey(boolean isTest) {
        String prefix = isTest ? PREFIX_TEST : PREFIX_LIVE;

        // Generate random bytes
        byte[] keyBytes = new byte[KEY_LENGTH];
        secureRandom.nextBytes(keyBytes);

        // Encode to base64 (URL-safe, no padding)
        String keyBody = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);

        // Create full key with prefix
        String fullKey = prefix + keyBody;

        // Get last 4 characters for display
        String lastFour = keyBody.substring(keyBody.length() - 4);

        // Hash the full key
        String keyHash = hashKey(fullKey);

        return new GeneratedKey(fullKey, keyHash, prefix, lastFour);
    }

    /**
     * Hash an API key using SHA-256.
     *
     * @param key the key to hash
     * @return hex-encoded hash
     */
    public String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Result of key generation.
     */
    public record GeneratedKey(
            String plainKey,
            String keyHash,
            String prefix,
            String lastFour
    ) {
    }
}
