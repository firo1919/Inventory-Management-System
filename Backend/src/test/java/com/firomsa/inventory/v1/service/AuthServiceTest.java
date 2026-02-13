package com.firomsa.inventory.v1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AuthServiceTest {

    @Test
    void generateOtpReturnsFiveDigitNumericValue() {
        AuthService authService = new AuthService(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        String otp = authService.generateOtp();

        assertEquals(5, otp.length());
        assertTrue(otp.matches("\\d{5}"));
    }
}
