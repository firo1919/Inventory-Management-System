package com.firomsa.inventory.v1.controller.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

public class EmployeeControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvcTester mockMvc;

    private static final String BASE_URL = "/api/v1/employee";

    @Test
    void shouldRejectUnauthorizedEmployeeAreaAccess() {
        assertThat(mockMvc.get().uri(BASE_URL).exchange()).hasStatus(401);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldReturnForbiddenForAdminOnEmployeeAreaAccess() {
        assertThat(mockMvc.get().uri(BASE_URL).exchange()).hasStatus(403);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_EMPLOYEE")
    void shouldReturnServerErrorForEmployeeOnUnmappedEmployeeAreaGet() {
        assertThat(mockMvc.get().uri(BASE_URL).exchange()).hasStatus(500);
    }

    @Test
    void shouldReturnUnauthorizedWhenBearerTokenIsInvalid() {
        assertThat(mockMvc.get().uri(BASE_URL).header("Authorization", "Bearer invalid.token")
                .exchange()).hasStatus(401);
    }
}
