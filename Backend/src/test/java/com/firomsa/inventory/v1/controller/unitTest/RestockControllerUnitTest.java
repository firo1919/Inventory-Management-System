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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firomsa.inventory.v1.controller.RestockController;
import com.firomsa.inventory.v1.dto.RestockRequestDTO;
import com.firomsa.inventory.v1.dto.RestockResponseDTO;
import com.firomsa.inventory.v1.service.RestockService;

@WebMvcTest(RestockController.class)
@AutoConfigureMockMvc
public class RestockControllerUnitTest {
    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private RestockService restockService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = "/api/v1/restocks";

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldCreateARestock() throws JsonProcessingException {
        var request = RestockRequestDTO.builder().productId(UUID.randomUUID()).quantity(5).build();
        var response = RestockResponseDTO.builder().productId(request.getProductId())
                .quantity(request.getQuantity()).message("Restock recorded successfully").build();
        when(restockService.createRestock(request, "user@example.com")).thenReturn(response);

        MvcTestResult result = mockMvc.post().uri(BASE_URL).with(csrf()).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("Restock recorded successfully");
        assertThat(result).bodyJson().extractingPath("$.quantity")
                .isEqualTo(request.getQuantity());
        assertThat(result).bodyJson().extractingPath("$.productId")
                .isEqualTo(request.getProductId().toString());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldGetRestockById() {
        UUID restockId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        var response = RestockResponseDTO.builder().productId(productId).quantity(3).build();
        when(restockService.getRestockById(restockId)).thenReturn(response);

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/{id}", restockId).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.productId")
                .isEqualTo(productId.toString());
        assertThat(result).bodyJson().extractingPath("$.quantity").isEqualTo(3);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldReturnBadRequestWhenQuantityIsMissing() throws JsonProcessingException {
        var request = RestockRequestDTO.builder().productId(UUID.randomUUID()).build();

        MvcTestResult result = mockMvc.post().uri(BASE_URL).with(csrf()).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        assertThat(result).hasStatus(400);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(400);
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("Validation failed for fields");
        assertThat(result).bodyJson().extractingPath("$.validationErrors.quantity")
                .isEqualTo("must not be null");
    }

    @Test
    void shouldReturnForbiddenWhenUserNotAuthenticated() throws JsonProcessingException {
        var request = RestockRequestDTO.builder().productId(UUID.randomUUID()).quantity(5).build();

        MvcTestResult result = mockMvc.post().uri(BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        assertThat(result).hasStatus(403);
    }
}
