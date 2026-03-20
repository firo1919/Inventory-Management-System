package com.firomsa.inventory.v1.controller.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.firomsa.inventory.v1.controller.EmployeeController;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc
public class EmployeeControllerUnitTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Test
    void shouldReturnUnauthorizedForUnauthenticatedEmployeeRoute() {
        MvcTestResult result = mockMvc.get().uri("/api/v1/employee").exchange();
        assertThat(result).hasStatus(401);
    }

    @Test
    void shouldReturnUnauthorizedForUnauthenticatedEmployeeSubRoute() {
        MvcTestResult result = mockMvc.get().uri("/api/v1/employee/some-sub-route").exchange();
        assertThat(result).hasStatus(401);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldReturnOkForAdminOnEmployeeRoute() {
        MvcTestResult result = mockMvc.get().uri("/api/v1/employee").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyText().isEqualTo("Employee area is accessible");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldReturnInternalServerErrorForAdminOnEmployeeSubRoute() {
        MvcTestResult result = mockMvc.get().uri("/api/v1/employee/some-sub-route").exchange();
        assertThat(result).hasStatus(500);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_EMPLOYEE")
    void shouldReturnOkForEmployeeOnEmployeeRouteGet() {
        MvcTestResult result = mockMvc.get().uri("/api/v1/employee").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyText().isEqualTo("Employee area is accessible");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_EMPLOYEE")
    void shouldReturnInternalServerErrorForUnmappedEmployeeSubRouteGet() {
        MvcTestResult result = mockMvc.get().uri("/api/v1/employee/some-sub-route").exchange();
        assertThat(result).hasStatus(500);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_EMPLOYEE")
    void shouldReturnInternalServerErrorForUnmappedEmployeeRoutePost() {
        MvcTestResult result = mockMvc.post().uri("/api/v1/employee").with(csrf()).exchange();
        assertThat(result).hasStatus(500);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_EMPLOYEE")
    void shouldReturnInternalServerErrorForUnmappedEmployeeRoutePut() {
        MvcTestResult result = mockMvc.put().uri("/api/v1/employee").with(csrf()).exchange();
        assertThat(result).hasStatus(500);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_EMPLOYEE")
    void shouldReturnInternalServerErrorForUnmappedEmployeeRouteDelete() {
        MvcTestResult result = mockMvc.delete().uri("/api/v1/employee").with(csrf()).exchange();
        assertThat(result).hasStatus(500);
    }

}
