package com.smartspotsolutions.payment_service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class ApiKeyManager {
    private final SecureRandom secureRandom = new SecureRandom();
    private final Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder(
            "",
            16,
            185000,
            Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256
    );


    private static final int KEY_SIZE_BITS = 512;
    private static final int KEY_SIZE_BYTES = KEY_SIZE_BITS / 8;

    // Generate raw key
    public String generateRawKey() {
        byte[] keyBytes = new byte[KEY_SIZE_BYTES];
        secureRandom.nextBytes(keyBytes);

        StringBuilder hexKey = new StringBuilder();
        for (byte b : keyBytes) {
            hexKey.append(String.format("%02x", b));
        }
        return hexKey.toString();
    }

    // Hash key for storage
    public String hashKey(String rawKey) {
        return encoder.encode(rawKey);
    }

    // Validate
    public boolean matches(String rawKey, String hashedKey) {
        return encoder.matches(rawKey, hashedKey);
    }
}

