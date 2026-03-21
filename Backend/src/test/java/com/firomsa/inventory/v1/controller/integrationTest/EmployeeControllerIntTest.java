package com.firomsa.inventory.v1.controller.integrationTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

public class EmployeeControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvcTester mockMvc;

    private static final String BASE_URL = "/api/v1/employee";
}
