package com.firomsa.inventory.v1.controller.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.firomsa.inventory.v1.controller.EmployeeController;
import com.firomsa.inventory.v1.dto.RestockResponseDTO;
import com.firomsa.inventory.v1.dto.SaleResponseDTO;
import com.firomsa.inventory.v1.service.RestockService;
import com.firomsa.inventory.v1.service.SaleService;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc
public class EmployeeControllerUnitTest {

    @MockitoBean
    private SaleService saleService;

    @MockitoBean
    private RestockService restockService;

    @Autowired
    private MockMvcTester mockMvc;

    private static final String BASE_URL = "/api/v1/employee";

    @Test
    @WithMockUser(username = "employee.one@example.com", authorities = "SCOPE_EMPLOYEE")
    void shouldReturnOwnSalesForEmployee() {
        UUID productId = UUID.randomUUID();
        SaleResponseDTO sale = SaleResponseDTO.builder().productId(productId).quantity(7)
                .salePrice(9.5).build();
        when(saleService.getSalesByEmployee("employee.one@example.com"))
                .thenReturn(List.of(sale));

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/sales").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$[0].productId").asString()
                .isEqualTo(productId.toString());
        assertThat(result).bodyJson().extractingPath("$[0].quantity").isEqualTo(7);

        verify(saleService).getSalesByEmployee("employee.one@example.com");
    }

    @Test
    @WithMockUser(username = "employee.one@example.com", authorities = "SCOPE_EMPLOYEE")
    void shouldReturnOwnRestocksForEmployee() {
        UUID productId = UUID.randomUUID();
        RestockResponseDTO restock = RestockResponseDTO.builder().productId(productId)
                .quantity(9).build();
        when(restockService.getRestocksByEmployee("employee.one@example.com"))
                .thenReturn(List.of(restock));

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/restocks").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$[0].productId").asString()
                .isEqualTo(productId.toString());
        assertThat(result).bodyJson().extractingPath("$[0].quantity").isEqualTo(9);

        verify(restockService).getRestocksByEmployee("employee.one@example.com");
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "SCOPE_ADMIN")
    void shouldDelegateForAuthenticatedAdminInWebMvcSlice() {
        when(saleService.getSalesByEmployee("admin@example.com")).thenReturn(List.of());

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/sales").exchange();

        assertThat(result).hasStatusOk();
        verify(saleService).getSalesByEmployee("admin@example.com");
    }

    @Test
    void shouldReturnUnauthorizedWhenMissingAuthentication() {
        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/sales").exchange();

        assertThat(result).hasStatus(401);
    }

    @Test
    void shouldReturnUnauthorizedWhenMissingAuthenticationForRestocks() {
        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/restocks").exchange();

        assertThat(result).hasStatus(401);
    }

}
