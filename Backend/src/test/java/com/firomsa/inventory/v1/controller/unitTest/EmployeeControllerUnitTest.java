package com.firomsa.inventory.v1.controller.unitTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import com.firomsa.inventory.v1.controller.EmployeeController;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc
public class EmployeeControllerUnitTest {

    @Autowired
    private MockMvcTester mockMvc;

}
