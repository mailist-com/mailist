package com.mailist.mailist.auth.domain.service;

import com.mailist.mailist.auth.domain.aggregate.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Service for Two-Factor Authentication (2FA) using TOTP (Time-based One-Time Password)
 */
@Service
@Slf4j
public class TwoFactorAuthService {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int TIME_STEP = 30; // 30 seconds
    private static final int CODE_DIGITS = 6;

    /**
     * Generate a secret key for 2FA
     */
    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Verify a TOTP code
     */
    public boolean verifyCode(User user, String code) {
        if (user.getTwoFactorSecret() == null) {
            log.warn("User {} does not have a 2FA secret configured", user.getEmail());
            return false;
        }

        try {
            long currentTime = Instant.now().getEpochSecond() / TIME_STEP;

            // Check current time window and 1 window before/after to account for clock drift
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateCode(user.getTwoFactorSecret(), currentTime + i);
                if (code.equals(expectedCode)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("Error verifying 2FA code for user {}", user.getEmail(), e);
            return false;
        }
    }

    /**
     * Generate a TOTP code for the given secret and time
     */
    private String generateCode(String secret, long timeCounter) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] secretBytes = Base64.getDecoder().decode(secret);

        // Convert time to bytes
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(timeCounter);
        byte[] timeBytes = buffer.array();

        // Generate HMAC
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec signKey = new SecretKeySpec(secretBytes, HMAC_ALGORITHM);
        mac.init(signKey);
        byte[] hash = mac.doFinal(timeBytes);

        // Dynamic truncation
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, CODE_DIGITS);

        // Pad with zeros if necessary
        return String.format("%0" + CODE_DIGITS + "d", otp);
    }

    /**
     * Get QR code URL for Google Authenticator
     */
    public String getQRCodeUrl(String email, String secret, String issuer) {
        String otpAuthUrl = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,
                email,
                secret.replaceAll("=", ""), // Remove padding
                issuer
        );
        return otpAuthUrl;
    }
}
