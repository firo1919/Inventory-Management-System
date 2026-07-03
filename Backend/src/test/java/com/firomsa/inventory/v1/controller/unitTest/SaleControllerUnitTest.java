package com.firomsa.inventory.v1.controller.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firomsa.inventory.support.TestCacheConfig;
import com.firomsa.inventory.v1.controller.SaleController;
import com.firomsa.inventory.v1.dto.SaleRequestDTO;
import com.firomsa.inventory.v1.dto.SaleResponseDTO;
import com.firomsa.inventory.v1.service.SaleService;

@WebMvcTest(SaleController.class)
@Import(TestCacheConfig.class)
@AutoConfigureMockMvc
public class SaleControllerUnitTest {

    @Autowired
    private MockMvcTester mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SaleService saleService;

    private static final String BASE_URL = "/api/v1/sales";

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldCreateASale() throws JsonProcessingException {
        // Arrange
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID()).quantity(5)
                .salePrice(10.0).build();
        var response = SaleResponseDTO.builder().productId(request.getProductId())
                .quantity(request.getQuantity()).salePrice(request.getSalePrice())
                .message("Sale recorded successfully").build();
        when(saleService.createSale(request, "user@example.com")).thenReturn(response);

        // Act
        MvcTestResult result = mockMvc.post().uri(BASE_URL).with(csrf()).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        // Assert
        assertThat(result).hasStatus(org.springframework.http.HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.message").isEqualTo("Sale recorded successfully");
        assertThat(result).bodyJson().extractingPath("$.quantity").isEqualTo(request.getQuantity());
        assertThat(result).bodyJson().extractingPath("$.salePrice").isEqualTo(request.getSalePrice());
        assertThat(result).bodyJson().extractingPath("$.productId")
                .isEqualTo(request.getProductId().toString());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldReturnBadRequestWhenQuantityIsMissing() throws JsonProcessingException {
        // Arrange
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID())
                .salePrice(10.0).build();

        // Act
        MvcTestResult result = mockMvc.post().uri(BASE_URL).with(csrf()).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        // Assert
        assertThat(result).hasStatus(400);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(400);
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("Validation failed for fields");
        assertThat(result).bodyJson().extractingPath("$.validationErrors.quantity")
                .isEqualTo("must not be null");
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldGetSaleById() {
        // Arrange
        UUID saleId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        var response = SaleResponseDTO.builder().productId(productId).quantity(3)
                .salePrice(12.5).build();
        when(saleService.getSaleById(saleId)).thenReturn(response);

        // Act
        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/{id}", saleId).exchange();

        // Assert
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.productId")
                .isEqualTo(productId.toString());
        assertThat(result).bodyJson().extractingPath("$.quantity").isEqualTo(3);
        assertThat(result).bodyJson().extractingPath("$.salePrice").isEqualTo(12.5);
    }

    @Test
    void shouldReturnForbiddenWhenUserNotAuthenticated() throws JsonProcessingException {
        // Arrange
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID()).quantity(5)
                .salePrice(10.0).build();

        // Act
        MvcTestResult result = mockMvc.post().uri(BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // Assert
        assertThat(result).hasStatus(403);
    }
}
