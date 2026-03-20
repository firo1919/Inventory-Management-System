package com.firomsa.inventory.v1.controller.unitTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.v1.controller.AuthController;
import com.firomsa.inventory.v1.dto.ConfirmOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ConfirmOtpResponseDTO;
import com.firomsa.inventory.v1.dto.LoginRequestDTO;
import com.firomsa.inventory.v1.dto.LoginResponseDTO;
import com.firomsa.inventory.v1.dto.LogoutRequestDTO;
import com.firomsa.inventory.v1.dto.LogoutResponseDTO;
import com.firomsa.inventory.v1.dto.RefreshTokenRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterAdminRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;
import com.firomsa.inventory.v1.dto.ResendOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ResendOtpResponseDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.service.AuthService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerUnitTest {

    @MockitoBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/auth";

    private UserResponseDTO sampleUser(UUID id, String username) {
        return new UserResponseDTO(id, "Admin", "User", username, "admin@example.com",
                "+251900000001", Roles.ADMIN.name(), null, "2026-03-19T10:15:30", true, true);
    }

    private String registerAdminRequestJson() {
        return """
                {
                    "firstName": "Admin",
                    "lastName": "User",
                    "username": "admin.user",
                    "password": "password123",
                    "email": "admin@example.com",
                    "phone": "+251900000001",
                    "bootstrapToken": "test-bootstrap-token-12345678901234"
                }
                """;
    }

    @Test
    void shouldRegisterAdmin() throws Exception {
        UserResponseDTO user = sampleUser(UUID.randomUUID(), "admin.user");
        RegisterResponseDTO response =
                new RegisterResponseDTO(user, "You have successfully registered");
        when(authService.createAdmin(any(RegisterAdminRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/admins").contentType(APPLICATION_JSON)
                .content(registerAdminRequestJson())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.message").value("You have successfully registered"));

        verify(authService).createAdmin(any(RegisterAdminRequestDTO.class));
    }

    @Test
    void shouldConfirmOtp() throws Exception {
        ConfirmOtpResponseDTO response = new ConfirmOtpResponseDTO("Successfully confirmed OTP");
        when(authService.confirmOtp(any(ConfirmOtpRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/confirm-otp").contentType(APPLICATION_JSON).content("""
                {
                    "otp": "12345",
                    "email": "admin@example.com"
                }
                """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully confirmed OTP"));

        verify(authService).confirmOtp(any(ConfirmOtpRequestDTO.class));
    }

    @Test
    void shouldResendOtp() throws Exception {
        ResendOtpResponseDTO response = new ResendOtpResponseDTO("Successfully resent OTP");
        when(authService.resendOtp(any(ResendOtpRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/resend-otp").contentType(APPLICATION_JSON).content("""
                {
                    "email": "admin@example.com"
                }
                """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully resent OTP"));

        verify(authService).resendOtp(any(ResendOtpRequestDTO.class));
    }

    @Test
    void shouldLoginUser() throws Exception {
        LoginResponseDTO response = new LoginResponseDTO(Roles.ADMIN, "access-token",
                "refresh-token", "admin.user", "admin@example.com");
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/login").contentType(APPLICATION_JSON).content("""
                {
                    "password": "password123",
                    "email": "admin@example.com"
                }
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    void shouldRefreshToken() throws Exception {
        LoginResponseDTO response = new LoginResponseDTO(Roles.ADMIN, "new-access-token",
                "refresh-token", "admin.user", "admin@example.com");
        when(authService.refreshAccessToken(any(RefreshTokenRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/refresh").contentType(APPLICATION_JSON).content("""
                {
                    "refreshToken": "refresh-token",
                    "email": "admin@example.com"
                }
                """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).refreshAccessToken(any(RefreshTokenRequestDTO.class));
    }

    @Test
    void shouldLogoutUser() throws Exception {
        LogoutResponseDTO response = new LogoutResponseDTO("Successfully logged out");
        when(authService.logoutUser(any(LogoutRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/logout").contentType(APPLICATION_JSON).content("""
                {
                    "refreshToken": "refresh-token",
                    "email": "admin@example.com"
                }
                """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully logged out"));

        verify(authService).logoutUser(any(LogoutRequestDTO.class));
    }
}
