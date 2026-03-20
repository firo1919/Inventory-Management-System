package com.firomsa.inventory.v1.controller.unitTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import com.firomsa.inventory.v1.controller.EmployeeController;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmployeeControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnInternalServerErrorForUnmappedEmployeeRoute() throws Exception {
        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isInternalServerError());
    }
}
