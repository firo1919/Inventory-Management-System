package com.firomsa.inventory.v1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.firomsa.inventory.repository.ConfirmationOtpRepository;
import com.firomsa.inventory.repository.RefreshTokenRepository;
import com.firomsa.inventory.repository.RoleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    @Test
    void generateOtpReturnsFiveDigitNumericValue() {
        AuthService authService = new AuthService(
                mock(PasswordEncoder.class),
                mock(UserRepository.class),
                mock(RoleRepository.class),
                mock(UserMapper.class),
                mock(EmailService.class),
                mock(ConfirmationOtpRepository.class),
                mock(AuthenticationManager.class),
                mock(JWTAuthService.class),
                mock(RefreshTokenRepository.class));

        String otp = authService.generateOtp();

        assertEquals(5, otp.length());
        assertTrue(otp.matches("\\d{5}"));
    }
}
