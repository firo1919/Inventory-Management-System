package com.firomsa.inventory.v1.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.v1.dto.LoginRequestDTO;
import com.firomsa.inventory.v1.dto.LoginResponseDTO;
import com.firomsa.inventory.v1.service.AuthService;
import com.firomsa.inventory.v1.service.JWTAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JWTAuthService jwtAuthService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void loginReturnsOkWithServiceResponse() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("password123", "employee@example.com");
        LoginResponseDTO response = new LoginResponseDTO(
                Roles.EMPLOYEE,
                "access-token",
                "refresh-token",
                "employee-user",
                "employee@example.com");

        when(authService.login(request)).thenReturn(response);

        String requestBody = """
                {"password":"password123","email":"employee@example.com"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("employee-user"))
                .andExpect(jsonPath("$.email").value("employee@example.com"));

        verify(authService).login(request);
    }

    @Test
    void loginReturnsBadRequestWhenValidationFails() throws Exception {
        String invalidRequestBody = """
                {"password":"short","email":"invalid-email"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
