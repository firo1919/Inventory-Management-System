package com.firomsa.inventory.v1.controller.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.firomsa.inventory.support.TestCacheConfig;
import com.firomsa.inventory.v1.controller.EmployeeController;
import com.firomsa.inventory.v1.dto.PageResponse;
import com.firomsa.inventory.v1.dto.RestockResponseDTO;
import com.firomsa.inventory.v1.dto.SaleResponseDTO;
import com.firomsa.inventory.v1.service.RestockService;
import com.firomsa.inventory.v1.service.SaleService;

@WebMvcTest(EmployeeController.class)
@Import(TestCacheConfig.class)
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
                .salePrice(BigDecimal.valueOf(9.5)).build();
        PageResponse<SaleResponseDTO> page = PageResponse.<SaleResponseDTO>builder()
                .content(List.of(sale)).pageNumber(0).pageSize(10)
                .totalElements(1).totalPages(1).first(true).last(true).empty(false).build();
        when(saleService.getSalesByEmployee(any(String.class), any(Pageable.class)))
                .thenReturn(page);

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/sales").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.content[0].productId").asString()
                .isEqualTo(productId.toString());
        assertThat(result).bodyJson().extractingPath("$.content[0].quantity").isEqualTo(7);
        verify(saleService).getSalesByEmployee(any(String.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "employee.one@example.com", authorities = "SCOPE_EMPLOYEE")
    void shouldReturnOwnRestocksForEmployee() {
        UUID productId = UUID.randomUUID();
        RestockResponseDTO restock = RestockResponseDTO.builder().productId(productId)
                .quantity(9).build();
        PageResponse<RestockResponseDTO> page = PageResponse.<RestockResponseDTO>builder()
                .content(List.of(restock)).pageNumber(0).pageSize(10)
                .totalElements(1).totalPages(1).first(true).last(true).empty(false).build();
        when(restockService.getRestocksByEmployee(any(String.class), any(Pageable.class)))
                .thenReturn(page);

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/restocks").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.content[0].productId").asString()
                .isEqualTo(productId.toString());
        assertThat(result).bodyJson().extractingPath("$.content[0].quantity").isEqualTo(9);
        verify(restockService).getRestocksByEmployee(any(String.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "SCOPE_ADMIN")
    void shouldDelegateForAuthenticatedAdminInWebMvcSlice() {
        PageResponse<SaleResponseDTO> page = PageResponse.<SaleResponseDTO>builder()
                .content(List.of()).pageNumber(0).pageSize(10)
                .totalElements(0).totalPages(0).first(true).last(true).empty(true).build();
        when(saleService.getSalesByEmployee(any(String.class), any(Pageable.class)))
                .thenReturn(page);

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/sales").exchange();

        assertThat(result).hasStatusOk();
        verify(saleService).getSalesByEmployee(any(String.class), any(Pageable.class));
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
