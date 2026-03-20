package com.firomsa.inventory.v1.controller.unitTest;

import static org.assertj.core.api.Assertions.assertThat;

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
@WithMockUser(authorities = "SCOPE_EMPLOYEE")
public class EmployeeControllerUnitTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Test
    void shouldReturnInternalServerErrorForUnmappedEmployeeRoute() {
        MvcTestResult result = mockMvc.get().uri("/api/v1/employee").exchange();
        assertThat(result).hasStatus(500);
    }
}
