package com.smartspotsolutions.payment_service.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class TokenGenerator {
    private final Random random = new Random(); // Initialize once

    public String generateNumericVerificationToken(int length) {
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10)); // 0-9
        }
        return otp.toString();
    }
}
